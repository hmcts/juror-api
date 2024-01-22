package uk.gov.hmcts.juror.api.bureau.service;

import io.jsonwebtoken.lang.Assert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.bureau.domain.UniquePool;
import uk.gov.hmcts.juror.api.bureau.domain.UniquePoolRepository;

import java.util.Optional;

/**
 * Implementation class for {@link UniquePoolService}.
 *
 * @since JDB-2042
 */
@Slf4j
@Service
public class UniquePoolServiceImpl implements UniquePoolService {

    private final UniquePoolRepository uniquePoolRepository;

    @Autowired
    public UniquePoolServiceImpl(UniquePoolRepository uniquePoolRepository) {
        Assert.notNull(uniquePoolRepository, "UniquePoolRepository must not be null");
        this.uniquePoolRepository = uniquePoolRepository;
    }

    @Override
    public String getPoolAttendanceTime(String poolId) {
        log.trace("Looking up pool attendance time for pool ID {}", poolId);
        Optional<UniquePool> optUniquePool = uniquePoolRepository.findById(poolId);
        final UniquePool uniquePool = optUniquePool.isPresent()
            ?
            optUniquePool.get()
            :
                null;
        if (uniquePool == null) {
            log.trace("No unique pool entry for pool ID {} (this is not necessarily an error)", poolId);
            return null;
        }
        final String attendanceTime = uniquePool.getAttendTime();
        if (attendanceTime == null) {
            log.trace("No attend time set in unique pool for pool ID {}", poolId);
        } else {
            log.trace("Attend time for pool ID {} is: {}", poolId, attendanceTime);
        }
        return attendanceTime;
    }
}
