package uk.gov.hmcts.juror.api.moj.enumeration;

import lombok.Getter;

@Getter
public enum AttendanceType {
    FULL_DAY("Full day", false, true),
    HALF_DAY("Half day", false, false),
    FULL_DAY_LONG_TRIAL("Full day (>10 days)", true, true),
    HALF_DAY_LONG_TRIAL("Half day (>10 days)", true, false),
    NON_ATTENDANCE_LONG_TRIAL("Non-attendance day (>10 days)", true, true),
    NON_ATTENDANCE("Non-attendance day", false, true),
    ABSENT("Absent (no show)", null, null);

    public final String displayName;
    private final Boolean isLongTrial;
    private final Boolean isFullDay;

    AttendanceType(String displayName, Boolean isLongTrial, Boolean isFullDay) {
        this.displayName = displayName;
        this.isLongTrial = isLongTrial;
        this.isFullDay = isFullDay;

    }

    public PayAttendanceType getPayAttendanceType() {
        return isFullDay == null ? null : isFullDay ? PayAttendanceType.FULL_DAY : PayAttendanceType.HALF_DAY;
    }
}
