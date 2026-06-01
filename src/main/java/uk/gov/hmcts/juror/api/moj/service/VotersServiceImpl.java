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
import uk.gov.hmcts.juror.api.moj.domain.DeceasedJuror;
import uk.gov.hmcts.juror.api.moj.domain.ExcludedVoters;
import uk.gov.hmcts.juror.api.moj.domain.QExcludedVoters;
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

        // need to do some filtering in case excluded or deceased voters were fetched

        // create a list of all distinct postcodes from votersList
        List<String> voterPostcodes = votersList.stream()
            .map(Voters::getPostcode)
            .distinct()
            .toList();

        List<DeceasedJuror> deceasedJurors = jurorService.getDeceasedJurors(voterPostcodes);
        
        // Filter out deceased voters
        votersList.removeIf(voter -> deceasedJurors.stream()
            .anyMatch(juror -> juror.getAddressLine1().trim().equalsIgnoreCase(voter.getAddress().trim())
                && juror.getLastName().trim().equalsIgnoreCase(voter.getLastName().trim())
                && juror.getFirstName().trim().equalsIgnoreCase(voter.getFirstName().trim())
                && juror.getPostcode().trim().equalsIgnoreCase(voter.getPostcode().trim())));

    
        List<ExcludedVoters> excludedVoters = getExcludedVoters();

        // filter out excluded voters
        votersList.removeIf(voter -> excludedVoters.stream()
            .anyMatch(excludedVoter -> excludedVoter.getAddress1().trim().equalsIgnoreCase(voter.getAddress().trim())
                && excludedVoter.getFirstName().trim().equalsIgnoreCase(voter.getFirstName().trim())
                && excludedVoter.getLastName().trim().equalsIgnoreCase(voter.getLastName().trim())
                && excludedVoter.getPostcode().trim().equalsIgnoreCase(voter.getPostcode().trim())));

        return votersList;
    }

    @Override
    public List<ExcludedVoters> getExcludedVoters() {
        JPAQueryFactory queryFactory = getQueryFactory();
        return queryFactory.selectFrom(QExcludedVoters.excludedVoters).fetch();
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
            log.info("Voter with hash id {} has been selected for a pool.", voter.getHashId());
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
