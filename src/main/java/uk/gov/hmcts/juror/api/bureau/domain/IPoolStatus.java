package uk.gov.hmcts.juror.api.bureau.domain;

/**
 * Constants for <code>JUROR.POOL_STATUS</code>.
 */
public abstract class IPoolStatus {
    public static final Long SUMMONED = 1L;
    public static final Long RESPONDED = 2L;
    public static final Long EXCUSED = 5L;
    public static final Long DISQUALIFIED = 6L;
    public static final Long DEFERRED = 7L;
    public static final Long REASSIGNED = 8L;
    public static final Long TRANSFERRED = 10L;
    public static final Long ADDITIONAL_INFO = 11L;
}
