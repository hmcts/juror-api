package uk.gov.hmcts.juror.api.moj.enumeration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class PayAttendanceTypeTest {

    @Test
    void fullDay() {
        assertConstructor(PayAttendanceType.FULL_DAY, "Full day", AttendanceType.FULL_DAY,
            AttendanceType.FULL_DAY_LONG_TRIAL);
    }

    @Test
    void halfDay() {
        assertConstructor(PayAttendanceType.HALF_DAY, "Half day", AttendanceType.HALF_DAY,
            AttendanceType.HALF_DAY_LONG_TRIAL);
    }


    void assertConstructor(PayAttendanceType type, String displayName, AttendanceType attendanceType,
                           AttendanceType longTrialAttendanceType) {
        assertThat(type.getDisplayName())
            .isEqualTo(displayName);
        assertThat(type.getAttendanceType(false))
            .isEqualTo(attendanceType);
        assertThat(type.getAttendanceType(true))
            .isEqualTo(longTrialAttendanceType);
    }
}
