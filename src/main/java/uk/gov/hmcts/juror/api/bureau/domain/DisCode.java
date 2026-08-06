package uk.gov.hmcts.juror.api.bureau.domain;

/**
 * Constants for <code>JUROR.DIS_CODE</code>.
 */
@Deprecated(forRemoval = true)
public final class DisCode {
    public static final String AGE = "A";
    public static final String ELECTRONIC_POLICE_CHECK_FAILURE = "E";

    private DisCode() {
        throw new IllegalArgumentException("DisCode should not be instantiated.");
    }
}
