package uk.gov.hmcts.juror.api.moj.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.HistoryCode;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PoolHistory;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.exception.PoolDeleteException;
import uk.gov.hmcts.juror.api.moj.exception.PoolRequestException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class DeletePoolServiceImpl implements DeletePoolService {

    @NonNull
    private final PoolRequestRepository poolRequestRepository;
    @NonNull
    private final PoolHistoryRepository poolHistoryRepository;
    @NonNull
    private final JurorPoolRepository jurorPoolRepository;

    @Override
    @Transactional
    public void deletePool(BureauJwtPayload payload, String poolNumber) {
        log.trace(String.format("Enter deletePoolRequest for Pool Number: %s", poolNumber));

        // according to specifications from heritage, users with a userLevel higher than 3 cannot delete empty pools
        if (Integer.parseInt(payload.getUserLevel()) > 3) {
            throw new PoolDeleteException.InsufficientPermission(payload.getLogin(), poolNumber);
        }

        // this will theoretically never happen unless there is a malfunction in the frontend code,
        // and we end up rendering the "delete pool (active)" button to a court user.
        String payloadOwner = payload.getOwner();
        if (poolRequestRepository.isActive(poolNumber) && !payloadOwner.equals(JurorDigitalApplication.JUROR_OWNER)) {
            throw new PoolDeleteException.InsufficientPermission(payload.getLogin(), poolNumber);
        }

        canDelete(poolNumber);
        deleteInactiveJurorPools(poolNumber);
        poolRequestRepository.deletePoolRequestByPoolNumber(poolNumber);

        poolHistoryRepository.save(new PoolHistory(poolNumber, LocalDateTime.now(), HistoryCode.DELP,
            payload.getLogin(), "Empty pool deleted"));

        log.trace(String.format("Deleted a pool request with Pool Number: %s", poolNumber));
    }

    /**
     * Check if the pool exists and can be deleted. In case it does not exist or cannot be deleted
     * throw an according exception
     *
     * @param poolNumber 9-digit number that identifies the pool
     */
    private void canDelete(String poolNumber) {
        Optional<PoolRequest> poolRequest = poolRequestRepository.findByPoolNumber(poolNumber);

        if (poolRequest.isEmpty()) {
            throw new PoolRequestException.PoolRequestNotFound(poolNumber);
        }

        CourtLocation courtLocation = poolRequest.get().getCourtLocation();
        assert courtLocation != null;

        if (courtLocation.getVotersLock() == 1) {
            throw new PoolDeleteException.PoolIsCurrentlyLocked(poolNumber);
        }

        List<JurorPool> members = jurorPoolRepository.findByPoolPoolNumberAndIsActive(poolNumber, true);

        if (!members.isEmpty()) {
            throw new PoolDeleteException.PoolHasMembersException(poolNumber);
        }
    }

    /**
     * Check for inactive pool members assigned to a pool and delete them.
     *
     * @param poolNumber 9-digit number that identifies the pool to find inactive pool members
     */
    private void deleteInactiveJurorPools(String poolNumber) {
        log.trace("Remove pool members called");
        List<JurorPool> jurorPoolList = jurorPoolRepository.findByPoolPoolNumberAndIsActive(poolNumber, false);

        if (jurorPoolList.isEmpty()) {
            log.info(String.format("No inactive pool members were found whilst deleting Pool: %s", poolNumber));
        } else {
            jurorPoolList.forEach(jurorPool -> {
                jurorPoolRepository.delete(jurorPool);
                jurorPoolRepository.flush();
                log.trace(String.format("Deleted a pool member with Pool Number: %s", jurorPool.getPoolNumber()));
            });
        }
    }

}
