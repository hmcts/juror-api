package uk.gov.hmcts.juror.api.juror.domain;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.enumeration.CourtType;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class CourtLocationTest {


    @Test
    void positiveGetTypeMain() {
        CourtLocation courtLocation = CourtLocation.builder()
            .owner(TestConstants.VALID_COURT_LOCATION)
            .locCode(TestConstants.VALID_COURT_LOCATION)
            .build();
        assertThat(courtLocation.getType()).isEqualTo(CourtType.MAIN);
    }

    @Test
    void positiveGetTypeSatellite() {
        CourtLocation courtLocation = CourtLocation.builder()
            .owner("415")
            .locCode("416")
            .build();
        assertThat(courtLocation.getType()).isEqualTo(CourtType.SATELLITE);
    }

    @Test
    void positiveGetNameWithLocCode() {
        CourtLocation courtLocation = CourtLocation.builder()
            .name("Some name")
            .locCode("416")
            .build();
        assertThat(courtLocation.getNameWithLocCode()).isEqualTo("Some name (416)");
    }
}
