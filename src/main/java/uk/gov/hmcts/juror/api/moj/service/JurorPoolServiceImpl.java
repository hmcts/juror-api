package uk.gov.hmcts.juror.api.moj.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPoolSearch;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.INVALID_JUROR_ATTENDANCE_RECORD;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class JurorPoolServiceImpl implements JurorPoolService {

    private final PoolRequestRepository poolRequestRepository;
    private final JurorPoolRepository jurorPoolRepository;

    @Override
    public PoolRequest getPoolRequest(String poolNumber) {
        return poolRequestRepository.findByPoolNumber(poolNumber)
            .orElseThrow(() -> new MojException.NotFound(
                "Pool not found: " + poolNumber, null));
    }

    @Override
    public boolean hasPoolWithLocCode(String jurorNumber, List<String> locCodes) {
        return jurorPoolRepository.hasPoolWithLocCode(jurorNumber, locCodes);
    }

    @Override
    public PaginatedList<JurorDetailsDto> search(JurorPoolSearch search) {
        PaginatedList<JurorDetailsDto> jurorDetailsDtoList =
            jurorPoolRepository.findJurorPoolsBySearch(search, SecurityUtil.getActiveOwner(),
                jurorPoolJPQLQuery -> jurorPoolJPQLQuery.where(
                    QJurorPool.jurorPool.status.status.eq(search.getJurorStatus())),
                jurorPool -> {
                    Juror juror = jurorPool.getJuror();
                    return JurorDetailsDto.builder()
                        .jurorNumber(jurorPool.getJurorNumber())
                        .poolNumber(jurorPool.getPoolNumber())
                        .firstName(juror.getFirstName())
                        .lastName(juror.getLastName())
                        .postCode(juror.getPostcode())
                        .completionDate(juror.getCompletionDate())
                        .build();
                },
                500L);

        if (jurorDetailsDtoList == null || jurorDetailsDtoList.isEmpty()) {
            throw new MojException.NotFound("No juror pools found that meet your search criteria.", null);
        }

        return jurorDetailsDtoList;
    }

    @Override
    public JurorPool getJurorPoolFromUser(String jurorNumber) {
        if (!SecurityUtil.hasBureauJwtPayload() || SecurityUtil.isBureau() || SecurityUtil.isSystem()) {
            return getLastActiveJurorPool(jurorNumber);
        }
        return Optional.ofNullable(jurorPoolRepository.findByJurorJurorNumberAndIsActiveAndOwner(
                jurorNumber, true, SecurityUtil.getActiveOwner()))
            .orElseThrow(() -> new MojException.NotFound("Juror not found: " + jurorNumber, null));
    }

    @Override
    public JurorPool getLastJurorPoolForJuror(String locCode, String jurorNumber) {
        List<JurorPool> jurorPools = getJurorPools(locCode, jurorNumber)
            .stream()
            .sorted(Comparator.comparing(JurorPool::getDateCreated).reversed())
            .toList();

        Optional<JurorPool> jurorPool = jurorPools.stream().filter(JurorPool::getIsActive).findFirst();
        if (jurorPool.isPresent()) {
            return jurorPool.orElse(null);
        } else if (!jurorPools.isEmpty()) {
            return jurorPools.get(0);
        }

        throw new MojException.BusinessRuleViolation("Invalid attendance record found for Juror: "
                                                         + jurorNumber, INVALID_JUROR_ATTENDANCE_RECORD);
    }

    @Override
    public JurorPool getJurorPoolForJuror(String jurorNumber, String poolNumber) {
        return jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(jurorNumber, poolNumber);
    }

    @Override
    public JurorPool save(JurorPool jurorPool) {
        return jurorPoolRepository.save(jurorPool);
    }

    @Override
    public int getCountJurorsDueToAttendCourtNextWeek(String locCode, boolean reasonableAdjustments) {

        LocalDate startDate = LocalDate.now().plusDays(1); // Start from tomorrow
        LocalDate endDate = startDate.plusDays(6); // Up to 7 days from tomorrow

        return jurorPoolRepository
            .getCountJurorsDueToAttendCourt(locCode, startDate, endDate, reasonableAdjustments);
    }


    private List<JurorPool> getJurorPools(String locCode, String jurorNumber) {
        return jurorPoolRepository.findByPoolCourtLocationLocCodeAndJurorJurorNumber(locCode, jurorNumber);
    }

    private JurorPool getLastActiveJurorPool(String jurorNumber) {
        List<JurorPool> jurorPools =
            jurorPoolRepository.findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);
        if (!jurorPools.isEmpty()) {
            return jurorPools.stream()
                .filter(jurorPool -> jurorPool.getOwner().equals(SecurityUtil.BUREAU_OWNER))
                .findFirst()
                .orElse(jurorPools.get(0));
        }
        throw new MojException.NotFound("Juror not found: " + jurorNumber, null);
    }
}
