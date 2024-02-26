package uk.gov.hmcts.juror.api.moj.enumeration;

import lombok.Getter;

@Getter
public enum PayAttendanceType {
    FULL_DAY("Full day", AttendanceType.FULL_DAY, AttendanceType.FULL_DAY_LONG_TRIAL),
    HALF_DAY("Half day", AttendanceType.HALF_DAY, AttendanceType.HALF_DAY_LONG_TRIAL);

    public final String displayName;
    private final AttendanceType attendanceType;
    private final AttendanceType longTrialAttendanceType;

    PayAttendanceType(String displayName, AttendanceType attendanceType, AttendanceType longTrialAttendanceType) {
        this.displayName = displayName;
        this.attendanceType = attendanceType;
        this.longTrialAttendanceType = longTrialAttendanceType;
    }

    public AttendanceType getAttendanceType(boolean isLongTrail) {
        return isLongTrail ? longTrialAttendanceType : attendanceType;
    }
}
