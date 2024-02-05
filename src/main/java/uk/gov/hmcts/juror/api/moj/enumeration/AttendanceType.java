package uk.gov.hmcts.juror.api.moj.enumeration;

import lombok.Getter;

@Getter
public enum AttendanceType {
    FULL_DAY("Full day", false),
    HALF_DAY("Half day", false),
    FULL_DAY_LONG_TRIAL("Full day (>10 days)", true),
    HALF_DAY_LONG_TRIAL("Half day (>10 days)", true),
    NON_ATTENDANCE_LONG_TRIAL("Non-attendance day (>10 days)", true),
    NON_ATTENDANCE("Non-attendance day", false),
    ABSENT("Absent (no show)", null),;

    public final String displayName;
    private final Boolean isLongTrial;

    AttendanceType(String displayName, Boolean isLongTrial) {
        this.displayName = displayName;
        this.isLongTrial = isLongTrial;
    }

}
