package uk.gov.hmcts.juror.api.moj.domain;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;

class JurorSearchTest extends AbstractValidatorTest<JurorSearch> {
    @Override
    protected JurorSearch createValidObject() {
        return JurorSearch.builder()
            .jurorName("Ben")
            .build();
    }

    @Test
    void positiveJurorName() {
        assertExpectNoViolations(JurorSearch.builder()
            .jurorName("Ben")
            .build());
    }

    @Test
    void positiveJurorNumber() {
        assertExpectNoViolations(JurorSearch.builder()
            .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
            .build());
    }

    @Test
    void positivePostcode() {
        assertExpectNoViolations(JurorSearch.builder()
            .postcode(TestConstants.VALID_POSTCODE)
            .build());
    }

    @Test
    void negativeJurorNameAndJurorNumber() {
        assertExpectViolations(JurorSearch.builder()
                .jurorName("Ben")
                .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                .build(),
            new Violation("jurorName",
                "Field jurorName should be excluded if any of the following fields are present:"
                    + " [jurorNumber, postcode]")
        );
    }

    @Test
    void negativeJurorNameAndPostcode() {
        assertExpectViolations(JurorSearch.builder()
                .jurorName("Ben")
                .postcode(TestConstants.VALID_POSTCODE)
                .build(),
            new Violation("jurorName",
                "Field jurorName should be excluded if any of the following fields are present:"
                    + " [jurorNumber, postcode]")
        );

    }

    @Test
    void negativeJurorNameAndJurorNumberAndPostcode() {
        assertExpectViolations(JurorSearch.builder()
                .jurorName("Ben")
                .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                .postcode(TestConstants.VALID_POSTCODE)
                .build(),
            new Violation("jurorName",
                "Field jurorName should be excluded if any of the following fields are present:"
                    + " [jurorNumber, postcode]")

        );

    }

    @Test
    void negativeJurorNumberAndPostcode() {
        assertExpectViolations(JurorSearch.builder()
                .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                .postcode(TestConstants.VALID_POSTCODE)
                .build(),
            new Violation("jurorNumber",
                "Field jurorNumber should be excluded if any of the following fields are present:"
                    + " [jurorName, postcode]")
        );
    }
}
