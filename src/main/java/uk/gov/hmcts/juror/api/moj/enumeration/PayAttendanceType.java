package uk.gov.hmcts.juror.api.moj.enumeration;

import lombok.Getter;

@Getter
public enum PayAttendanceType {
    FULL_DAY("Full day", AttendanceType.FULL_DAY, AttendanceType.FULL_DAY_LONG_TRIAL,
             AttendanceType.FULL_DAY_EXTRA_LONG_TRIAL),
    HALF_DAY("Half day", AttendanceType.HALF_DAY, AttendanceType.HALF_DAY_LONG_TRIAL,
             AttendanceType.HALF_DAY_EXTRA_LONG_TRIAL);

    public final String displayName;
    private final AttendanceType attendanceType;
    private final AttendanceType longTrialAttendanceType;
    private final AttendanceType extraLongTrialAttendanceType;

    PayAttendanceType(String displayName, AttendanceType attendanceType, AttendanceType longTrialAttendanceType,
                      AttendanceType extraLongTrialAttendanceType) {
        this.displayName = displayName;
        this.attendanceType = attendanceType;
        this.longTrialAttendanceType = longTrialAttendanceType;
        this.extraLongTrialAttendanceType = extraLongTrialAttendanceType;
    }

    public AttendanceType getAttendanceType(boolean isLongTrail, boolean isExtraLongTrial) {
        if (isLongTrail) {
            return longTrialAttendanceType;
        } else if (isExtraLongTrial) {
            return extraLongTrialAttendanceType;
        }
        return attendanceType;
    }
}
