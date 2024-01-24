package uk.gov.hmcts.juror.api.moj.enumeration;

public enum AttendanceType {
    FULL_DAY("Full day"),
    HALF_DAY("Half day"),
    FULL_DAY_LONG_TRIAL("Full day (>10 days"),
    HALF_DAY_LONG_TRIAL("Half day (>10 days"),
    ABSENT("Absent (no show)"),
    NON_ATTENDANCE("Non-attendance day");

    public final String displayName;

    AttendanceType(String displayName) {
        this.displayName = displayName;
    }

}
