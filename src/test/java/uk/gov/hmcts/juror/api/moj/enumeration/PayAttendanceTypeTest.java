package uk.gov.hmcts.juror.api.moj.enumeration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class PayAttendanceTypeTest {

    @Test
    void fullDay() {
        validate(PayAttendanceType.FULL_DAY, "Full day");
    }

    @Test
    void halfDay() {
        validate(PayAttendanceType.HALF_DAY, "Half day");
    }


    void validate(PayAttendanceType type, String displayName) {
        assertThat(type.getDisplayName())
            .isEqualTo(displayName);
    }
}
