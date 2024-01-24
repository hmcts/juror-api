package uk.gov.hmcts.juror.api.moj.service;

import org.springframework.transaction.annotation.Transactional;

/**
 * Pool sequence numbers start from 0001 and increment by 1 for each new member added to the pool.
 */
public interface PoolMemberSequenceService {

    @Transactional(readOnly = true)
    int getPoolMemberSequenceNumber(String poolNumber);

    String leftPadInteger(int intValue);
}
