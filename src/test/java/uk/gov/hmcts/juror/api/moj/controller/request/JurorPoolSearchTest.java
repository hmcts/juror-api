package uk.gov.hmcts.juror.api.moj.controller.request;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;

@SuppressWarnings({
    "PMD.TooManyMethods",
    "PMD.JUnitTestsShouldIncludeAssert" //False positive
})
class JurorPoolSearchTest extends AbstractValidatorTest<JurorPoolSearch> {

    @Override
    protected JurorPoolSearch createValidObject() {
        return JurorPoolSearch.builder()
            .jurorName("BE")
            .pageNumber(1)
            .pageLimit(25)
            .build();
    }

    @Test
    void positiveJurorName() {
        expectNoViolations(createValidObject());
    }

    @Test
    void negativeJurorNameHasJurorNumber() {
        expectViolations(JurorPoolSearch.builder()
                .jurorName("BE")
                .jurorNumber("123")
                .pageNumber(1)
                .pageLimit(25)
                .build(),
            new Violation("jurorName",
                "Field jurorName should be excluded if any of the following fields are present: "
                    + "[jurorNumber, postcode]")
        );
    }

    @Test
    void negativeJurorNameHasPostCode() {
        expectViolations(JurorPoolSearch.builder()
                .jurorName("BE")
                .postcode("CA")
                .pageNumber(1)
                .pageLimit(25)
                .build(),
            new Violation("jurorName",
                "Field jurorName should be excluded if any of the following fields are present: "
                    + "[jurorNumber, postcode]")
        );

    }

    @Test
    void positiveJurorNumber() {
        expectNoViolations(JurorPoolSearch.builder()
            .jurorNumber("123")
            .pageNumber(1)
            .pageLimit(25)
            .build());
    }

    @Test
    void negativeJurorNumberHasPostCode() {
        expectViolations(JurorPoolSearch.builder()
                .poolNumber("BE")
                .jurorNumber("123")
                .pageNumber(1)
                .pageLimit(25)
                .build(),
            new Violation("poolNumber",
                "Field poolNumber should be excluded if any of the following fields are present: "
                    + "[postcode, jurorNumber, jurorName]")
        );
    }

    @Test
    void positivePostCode() {
        expectNoViolations(JurorPoolSearch.builder()
            .postcode("CA")
            .pageNumber(1)
            .pageLimit(25)
            .build());
    }

    @Test
    void positivePoolNumber() {
        expectNoViolations(JurorPoolSearch.builder()
            .poolNumber("123")
            .pageNumber(1)
            .pageLimit(25)
            .build());

    }

    @Test
    void negativePoolNumberHasPostCode() {
        expectViolations(JurorPoolSearch.builder()
                .postcode("BE")
                .poolNumber("123")
                .pageNumber(1)
                .pageLimit(25)
                .build(),
            new Violation("poolNumber",
                "Field poolNumber should be excluded if any of the following fields are present: "
                    + "[postcode, jurorNumber, jurorName]")
        );
    }

    @Test
    void negativePoolNumberHasJurorNumber() {
        expectViolations(JurorPoolSearch.builder()
                .jurorNumber("12")
                .poolNumber("123")
                .pageNumber(1)
                .pageLimit(25)
                .build(),
            new Violation("poolNumber",
                "Field poolNumber should be excluded if any of the following fields are present: "
                    + "[postcode, jurorNumber, jurorName]")
        );

    }

    @Test
    void negativePoolNumberHasJurorName() {
        expectViolations(JurorPoolSearch.builder()
                .jurorName("BE")
                .poolNumber("123")
                .pageNumber(1)
                .pageLimit(25)
                .build(),
            new Violation("poolNumber",
                "Field poolNumber should be excluded if any of the following fields are present: "
                    + "[postcode, jurorNumber, jurorName]")
        );

    }

    @Test
    void negativeEmptyPayload() {
        expectViolations(JurorPoolSearch.builder()
                .pageNumber(1)
                .pageLimit(25)
                .build(),
            new Violation("jurorName",
                "Field jurorName is required if none of the following fields are present: "
                    + "[jurorNumber, postcode, poolNumber]")
        );
    }

    @Test
    void negativePageNumberMin() {
        expectViolations(JurorPoolSearch.builder()
                .jurorName("Name")
                .pageNumber(0)
                .pageLimit(25)
                .build(),
            new Violation("pageNumber",
                "must be greater than or equal to 1")
        );
    }

    @Test
    void positivePageNumberMin() {
        expectNoViolations(JurorPoolSearch.builder()
            .jurorName("Name")
            .pageNumber(1)
            .pageLimit(25)
            .build()
        );
    }

    @Test
    void negativePageLimitMin() {
        expectViolations(JurorPoolSearch.builder()
                .jurorName("Name")
                .pageNumber(1)
                .pageLimit(0)
                .build(),
            new Violation("pageLimit",
                "must be greater than or equal to 1")
        );
    }

    @Test
    void positivePageLimitMin() {
        expectNoViolations(JurorPoolSearch.builder()
            .jurorName("Name")
            .pageNumber(1)
            .pageLimit(1)
            .build()
        );
    }
}
