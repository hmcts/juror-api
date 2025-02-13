package uk.gov.hmcts.juror.api.moj.enumeration;

import lombok.Getter;

import java.util.Collection;
import java.util.List;

@Getter
public enum AttendanceType {
    FULL_DAY("Full day", false, true, false),
    HALF_DAY("Half day", false, false, false),
    FULL_DAY_LONG_TRIAL("Full day (>10 days)", true, true, false),
    HALF_DAY_LONG_TRIAL("Half day (>10 days)", true, false, false),
    FULL_DAY_EXTRA_LONG_TRIAL("Full day (>201 days)", false, true, true),
    HALF_DAY_EXTRA_LONG_TRIAL("Half day (>201 days)", false, false, true),
    NON_ATTENDANCE_EXTRA_LONG_TRIAL("Non-attendance day (>201 days)", false, true, true),
    NON_ATTENDANCE_LONG_TRIAL("Non-attendance day (>10 days)", true, true, false),
    NON_ATTENDANCE("Non-attendance day", false, true, false),
    ABSENT("Absent (no show)", null, null, null);

    public final String displayName;
    private final Boolean isLongTrial;
    private final Boolean isFullDay;
    private final Boolean isExtraLongTrial;

    AttendanceType(String displayName, Boolean isLongTrial, Boolean isFullDay, Boolean isExtraLongTrial) {
        this.displayName = displayName;
        this.isLongTrial = isLongTrial;
        this.isFullDay = isFullDay;
        this.isExtraLongTrial = isExtraLongTrial;

    }

    public PayAttendanceType getPayAttendanceType() {
        return isFullDay == null ? null : isFullDay ? PayAttendanceType.FULL_DAY : PayAttendanceType.HALF_DAY;
    }

    public static Collection<AttendanceType> getNonAttendanceTypes() {
        return List.of(NON_ATTENDANCE, NON_ATTENDANCE_LONG_TRIAL, NON_ATTENDANCE_EXTRA_LONG_TRIAL);
    }
}
