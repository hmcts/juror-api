package uk.gov.hmcts.juror.api.moj.enumeration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class AttendanceTypeTest {

    @Test
    void fullDay() {
        validate(AttendanceType.FULL_DAY, "Full day", false);
    }

    @Test
    void halfDay() {
        validate(AttendanceType.HALF_DAY, "Half day", false);
    }

    @Test
    void fullDayLongTrial() {
        validate(AttendanceType.FULL_DAY_LONG_TRIAL, "Full day (>10 days)", true);
    }

    @Test
    void halfDayLongTrial() {
        validate(AttendanceType.HALF_DAY_LONG_TRIAL, "Half day (>10 days)", true);
    }

    @Test
    void absent() {
        validate(AttendanceType.ABSENT, "Absent (no show)", null);
    }

    @Test
    void nonAttendance() {
        validate(AttendanceType.NON_ATTENDANCE, "Non-attendance day", false);
    }

    @Test
    void nonAttendanceLongTrial() {
        validate(AttendanceType.NON_ATTENDANCE_LONG_TRIAL, "Non-attendance day (>10 days)", true);
    }


    void validate(AttendanceType type, String displayName, Boolean isLongTrial) {
        assertThat(type.getDisplayName())
            .isEqualTo(displayName);
        assertThat(type.getIsLongTrial()).isEqualTo(isLongTrial);
    }
}
