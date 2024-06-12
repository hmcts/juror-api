package uk.gov.hmcts.juror.api.moj.enumeration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SuppressWarnings("PMD.LinguisticNaming")
class AttendanceTypeTest {

    @Test
    void fullDay() {
        assertConstructor(AttendanceType.FULL_DAY, "Full day", false, PayAttendanceType.FULL_DAY);
    }

    @Test
    void halfDay() {
        assertConstructor(AttendanceType.HALF_DAY, "Half day", false, PayAttendanceType.HALF_DAY);
    }

    @Test
    void fullDayLongTrial() {
        assertConstructor(AttendanceType.FULL_DAY_LONG_TRIAL, "Full day (>10 days)", true, PayAttendanceType.FULL_DAY);
    }

    @Test
    void halfDayLongTrial() {
        assertConstructor(AttendanceType.HALF_DAY_LONG_TRIAL, "Half day (>10 days)", true, PayAttendanceType.HALF_DAY);
    }

    @Test
    void absent() {
        assertConstructor(AttendanceType.ABSENT, "Absent (no show)", null, null);
    }

    @Test
    void nonAttendance() {
        assertConstructor(AttendanceType.NON_ATTENDANCE, "Non-attendance day", false, PayAttendanceType.FULL_DAY);
    }

    @Test
    void nonAttendanceLongTrial() {
        assertConstructor(AttendanceType.NON_ATTENDANCE_LONG_TRIAL, "Non-attendance day (>10 days)", true,
            PayAttendanceType.FULL_DAY);
    }


    void assertConstructor(AttendanceType type, String displayName, Boolean isLongTrial,
                           PayAttendanceType payAttendanceType) {
        assertThat(type.getDisplayName())
            .isEqualTo(displayName);
        assertThat(type.getIsLongTrial()).isEqualTo(isLongTrial);
        assertThat(type.getPayAttendanceType()).isEqualTo(payAttendanceType);
    }

    @Test
    void getPayAttendanceTypeIsFullDayNull() {
        assertThat(AttendanceType.ABSENT.getPayAttendanceType()).isNull();
    }

    @Test
    void getPayAttendanceTypeIsFullTrue() {
        assertThat(AttendanceType.FULL_DAY.getPayAttendanceType())
            .isEqualTo(PayAttendanceType.FULL_DAY);
    }

    @Test
    void getPayAttendanceTypeIsFullFalse() {
        assertThat(AttendanceType.HALF_DAY.getPayAttendanceType())
            .isEqualTo(PayAttendanceType.HALF_DAY);
    }
}
