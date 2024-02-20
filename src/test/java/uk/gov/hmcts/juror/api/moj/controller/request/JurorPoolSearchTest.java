package uk.gov.hmcts.juror.api.moj.controller.request;

import com.querydsl.core.types.dsl.ComparableExpressionBase;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;

import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SuppressWarnings({
    "PMD.TooManyMethods"
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
        assertExpectNoViolations(createValidObject());
    }

    @Test
    void negativeJurorNameHasJurorNumber() {
        assertExpectViolations(JurorPoolSearch.builder()
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
        assertExpectViolations(JurorPoolSearch.builder()
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
        assertExpectNoViolations(JurorPoolSearch.builder()
            .jurorNumber("123")
            .pageNumber(1)
            .pageLimit(25)
            .build());
    }

    @Test
    void negativeJurorNumberHasPostCode() {
        assertExpectViolations(JurorPoolSearch.builder()
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
        assertExpectNoViolations(JurorPoolSearch.builder()
            .postcode("CA")
            .pageNumber(1)
            .pageLimit(25)
            .build());
    }

    @Test
    void positivePoolNumber() {
        assertExpectNoViolations(JurorPoolSearch.builder()
            .poolNumber("123")
            .pageNumber(1)
            .pageLimit(25)
            .build());

    }

    @Test
    void negativePoolNumberHasPostCode() {
        assertExpectViolations(JurorPoolSearch.builder()
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
        assertExpectViolations(JurorPoolSearch.builder()
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
        assertExpectViolations(JurorPoolSearch.builder()
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
        assertExpectViolations(JurorPoolSearch.builder()
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
        assertExpectViolations(JurorPoolSearch.builder()
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
        assertExpectNoViolations(JurorPoolSearch.builder()
            .jurorName("Name")
            .pageNumber(1)
            .pageLimit(25)
            .build()
        );
    }

    @Test
    void negativePageLimitMin() {
        assertExpectViolations(JurorPoolSearch.builder()
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
        assertExpectNoViolations(JurorPoolSearch.builder()
            .jurorName("Name")
            .pageNumber(1)
            .pageLimit(1)
            .build()
        );
    }

    @TestFactory
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    Stream<DynamicTest> sortFieldTests() {
        return Stream.of(
            sortFieldTest(JurorPoolSearch.SortField.JUROR_NUMBER, QJuror.juror.jurorNumber),
            sortFieldTest(JurorPoolSearch.SortField.FIRST_NAME, QJuror.juror.firstName),
            sortFieldTest(JurorPoolSearch.SortField.LAST_NAME, QJuror.juror.lastName),
            sortFieldTest(JurorPoolSearch.SortField.POSTCODE, QJuror.juror.postcode),
            sortFieldTest(JurorPoolSearch.SortField.COMPLETION_DATE, QJuror.juror.completionDate)
        );
    }

    DynamicTest sortFieldTest(JurorPoolSearch.SortField sortField, ComparableExpressionBase<?> expected) {
        return DynamicTest.dynamicTest(sortField + " - Returns correct expression",
            () -> {
                assertThat(sortField).isNotNull();
                assertThat(sortField.getComparableExpression()).isEqualTo(expected);
            });
    }
}
