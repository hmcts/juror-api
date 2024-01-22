package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.domain.UniquePool;

/**
 * Service for operations relating to {@link UniquePool} entities.
 *
 * @since JDB-2042
 */
public interface UniquePoolService {

    /**
     * Retrieves the pool attendance time for a pool ID (if one is specified in the JUROR.UNIQUE_POOL table)
     *
     * @param poolId pool ID to retrieve attendance time for, not null
     * @return pool attendance time, nullable
     */
    String getPoolAttendanceTime(String poolId);
}
