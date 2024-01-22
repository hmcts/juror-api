package uk.gov.hmcts.juror.api.moj.repository;

import uk.gov.hmcts.juror.api.moj.domain.PoolHistory;

import java.util.List;

/**
 * Custom Repository definition for the PoolHistory entity.
 * Allowing for additional query functions to be explicitly declared
 */

public interface IPoolHistoryRepository {
    List<PoolHistory> findPoolHistorySincePoolCreated(String poolNumber);
}
