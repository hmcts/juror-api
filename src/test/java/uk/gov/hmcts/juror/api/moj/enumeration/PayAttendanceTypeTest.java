package uk.gov.hmcts.juror.api.moj.enumeration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class PayAttendanceTypeTest {

    @Test
    void fullDay() {
        assertConstructor(PayAttendanceType.FULL_DAY, "Full day", AttendanceType.FULL_DAY);
    }

    @Test
    void halfDay() {
        assertConstructor(PayAttendanceType.HALF_DAY, "Half day", AttendanceType.HALF_DAY);
    }

    @Test
    void fullDayLongTrial() {
        assertConstructorLongTrial(PayAttendanceType.FULL_DAY, "Full day", AttendanceType.FULL_DAY_LONG_TRIAL);
    }

    @Test
    void halfDayLongTrial() {
        assertConstructorLongTrial(PayAttendanceType.HALF_DAY, "Half day", AttendanceType.HALF_DAY_LONG_TRIAL);
    }

    @Test
    void fullDayExtraLongTrial() {
        assertConstructorExtraLongTrial(PayAttendanceType.FULL_DAY, "Full day",
                                        AttendanceType.FULL_DAY_EXTRA_LONG_TRIAL);
    }

    @Test
    void halfDayExtraLongTrial() {
        assertConstructorExtraLongTrial(PayAttendanceType.HALF_DAY, "Half day",
                                        AttendanceType.HALF_DAY_EXTRA_LONG_TRIAL);
    }

    void assertConstructor(PayAttendanceType type, String displayName, AttendanceType attendanceType) {
        assertThat(type.getDisplayName())
            .isEqualTo(displayName);
        assertThat(type.getAttendanceType(false, false))
            .isEqualTo(attendanceType);
    }

    void assertConstructorLongTrial(PayAttendanceType type, String displayName, AttendanceType attendanceType) {
        assertThat(type.getDisplayName())
            .isEqualTo(displayName);
        assertThat(type.getAttendanceType(true, false))
            .isEqualTo(attendanceType);
    }

    void assertConstructorExtraLongTrial(PayAttendanceType type, String displayName, AttendanceType attendanceType) {
        assertThat(type.getDisplayName())
            .isEqualTo(displayName);
        assertThat(type.getAttendanceType(false, true))
            .isEqualTo(attendanceType);
    }

}
