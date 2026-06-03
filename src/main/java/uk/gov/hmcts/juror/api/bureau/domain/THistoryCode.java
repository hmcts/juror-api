package uk.gov.hmcts.juror.api.bureau.domain;

/**
 * Constants for <code>JUROR.T_HISTORY_CODE</code>.
 */
@Deprecated(forRemoval = true)
public final class THistoryCode {
    public static final String RESPONDED = "RESP";
    public static final String DEFERRED = "PDEF";

    private THistoryCode() {
        throw new IllegalArgumentException("THistoryCode should not be instantiated.");
    }
}
