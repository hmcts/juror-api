package uk.gov.hmcts.juror.api.moj.service;

import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.bureau.domain.SystemParameter;
import uk.gov.hmcts.juror.api.bureau.domain.SystemParameterRepository;
import uk.gov.hmcts.juror.api.juror.service.JurorService;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolCreateRequestDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.QVoters;
import uk.gov.hmcts.juror.api.moj.domain.Voters;
import uk.gov.hmcts.juror.api.moj.repository.VotersRepository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class VotersServiceImpl implements VotersService {
    static final int AGE_LOWER_SP_ID = 101;
    static final int AGE_UPPER_SP_ID = 100;

    @Autowired
    private JurorService jurorService;

    private final VotersRepository votersRepository;
    private final SystemParameterRepository systemParameterRepository;

    @PersistenceContext
    EntityManager entityManager;


    @Override
    public List<Voters> getVotersForCoronerPool(String postcode, int number, String locCode) {
        return getVoters(List.of(postcode), number, LocalDate.now(), locCode, true);
    }

    @Override
    public List<Voters> getVoters(PoolCreateRequestDto poolCreateRequestDto) {
        return getVoters(poolCreateRequestDto.getPostcodes(),
            poolCreateRequestDto.getCitizensToSummon(),
            poolCreateRequestDto.getStartDate(),
            poolCreateRequestDto.getCatchmentArea(), false);
    }

    @Override
    public List<Voters> getVoters(List<String> postcodes,
                                  int citizensToSummon,
                                  LocalDate attendanceDate,
                                  String locCode,
                                  boolean isCoroners) {
        //Determine Max and Min DOB allowed based on attendance date
        LocalDate maxDateOfBirth = calculateDateLimit(attendanceDate, AGE_LOWER_SP_ID, 18);
        LocalDate minDateOfBirth = calculateDateLimit(attendanceDate, AGE_UPPER_SP_ID, 76);

        JPAQueryFactory queryFactory = getQueryFactory();
        JPAQuery<Voters> query = queryFactory.selectFrom(QVoters.voters)
            .where(QVoters.voters.dateSelected1.isNull()
                .and(QVoters.voters.dateOfBirth.isNull()
                    .or(QVoters.voters.dateOfBirth.between(minDateOfBirth, maxDateOfBirth)))
                .and(QVoters.voters.permDisqual.isNull())
                .and(QVoters.voters.postcodeStart.in(postcodes)));

        if (isCoroners) {
            query.where(QVoters.voters.flags.isNull());
        }
        List<Voters> votersList = query.orderBy(NumberExpression.random().asc())
            .limit((long) (citizensToSummon * 1.4))
            .fetch();

        /* Filter out deceased voters -
        Not running a query to join juror with the voters table as that would be potentially slower
        (work was done on optimising the performance of the getVoters function)
        Not using a join with pool for loc_code/owner of user as voters postcodes may cover multiple courts
        and jurors could have been moved between catchment areas */

        // create a list of all postcodes from votersList and iterate through returned jurors to remove any matches
        List<String> voterPostcodes = votersList.stream()
            .map(Voters::getPostcode)
            .distinct()
            .toList();

        List<Juror> deceasedJurors = jurorService.getDeceasedJurors(voterPostcodes);

        votersList.removeIf(voter -> deceasedJurors.stream()
            .anyMatch(juror -> juror.getFirstName().equals(voter.getFirstName())
                && juror.getLastName().equals(voter.getLastName())
                && juror.getAddressLine1().equals(voter.getAddress())
                && juror.getPostcode().equals(voter.getPostcode())));

        return votersList;
    }

    JPAQueryFactory getQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }


    @Override
    public void markVotersAsSelected(List<Voters> voters, Date attendanceDate) {
        if (voters.isEmpty()) {
            return;
        }
        voters.forEach(voter -> {
            voter.setDateSelected1(attendanceDate);
            log.info("Voter with Juror number {} has been selected for a pool.", voter.getJurorNumber());
        });
        votersRepository.saveAll(voters);
    }

    private LocalDate calculateDateLimit(LocalDate attendanceDate, int systemParamId, int defaultAge) {
        Optional<SystemParameter> optJurAgeParam = systemParameterRepository.findById(systemParamId);
        final SystemParameter jurorAgeParameter = optJurAgeParam.orElse(null);
        int jurorAgeLimit = defaultAge;
        if (jurorAgeParameter != null) {
            try {
                jurorAgeLimit = Integer.parseInt(jurorAgeParameter.getSpValue());
            } catch (Exception e) {
                log.error("Failed to parse age constraint parameter from database: Using default {}",
                    jurorAgeLimit, e);
            }
        }
        return attendanceDate.minusYears(jurorAgeLimit);
    }

}
