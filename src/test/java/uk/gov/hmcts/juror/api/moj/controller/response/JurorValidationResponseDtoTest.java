package uk.gov.hmcts.juror.api.moj.controller.response;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;

@DisplayName("JurorValidationResponseDto")
@SuppressWarnings(
    "PMD.JUnitTestsShouldIncludeAssert"//False positive done via inheritance
)
class JurorValidationResponseDtoTest extends AbstractValidatorTest<JurorValidationResponseDto> {

    @Override
    protected JurorValidationResponseDto createValidObject() {
        return createJurorValidationResponseDto(
            TestConstants.VALID_JUROR_NUMBER,
            "FNAME",
            "LNAME");
    }

    protected JurorValidationResponseDto createJurorValidationResponseDto(
        String jurorNumber,
        String firstName,
        String lastName
    ) {
        return JurorValidationResponseDto.builder()
            .jurorNumber(jurorNumber)
            .firstName(firstName)
            .lastName(lastName)
            .build();
    }

    @Test
    void positiveTypical() {
        expectNoViolations(
            createJurorValidationResponseDto(
                "123456789",
                "John",
                "Smith"
            )
        );
    }

    @DisplayName("jurorNumber")
    @Nested
    class JurorNumber {

        @Test
        void negativeBlank() {
            expectViolations(
                createJurorValidationResponseDto(
                    "",
                    "John",
                    "Smith"
                ),
                new Violation("jurorNumber", "must not be blank"),
                new Violation("jurorNumber", "must match \"^\\d{9}$\"")
            );
        }


        @Test
        void negativeNull() {
            expectViolations(
                createJurorValidationResponseDto(
                    null,
                    "John",
                    "Smith"
                ),
                new Violation("jurorNumber", "must not be blank")
            );
        }

        @Test
        void negativeInvalidJurorNumber() {
            expectViolations(
                createJurorValidationResponseDto(
                    "ABC",
                    "John",
                    "Smith"
                ),
                new Violation("jurorNumber", "must match \"^\\d{9}$\"")
            );
        }
    }

    @DisplayName("firstName")
    @Nested
    class FirstName {
        @Test
        void negativeBlank() {
            expectViolations(
                createJurorValidationResponseDto(
                    "123456789",
                    "",
                    "Smith"
                ),
                new Violation("firstName", "must not be blank")
            );

        }

        @Test
        void negativeNull() {
            expectViolations(
                createJurorValidationResponseDto(
                    "123456789",
                    null,
                    "Smith"
                ),
                new Violation("firstName", "must not be blank")
            );

        }

        @Test
        void negativeTooLarge() {
            expectViolations(
                createJurorValidationResponseDto(
                    "123456789",
                    RandomStringUtils.randomAlphabetic(21),
                    "Smith"
                ),
                new Violation("firstName", "length must be between 0 and 20")
            );
        }

        @Test
        void negativeHasPipes() {
            expectViolations(
                createJurorValidationResponseDto(
                    "123456789",
                    "John|",
                    "Smith"
                ),
                new Violation("firstName", "must match \"^$|^[^|]+$\"")
            );
        }
    }

    @DisplayName("lastName")
    @Nested
    class LastName {
        @Test
        void negativeBlank() {
            expectViolations(
                createJurorValidationResponseDto(
                    "123456789",
                    "John",
                    ""
                ),
                new Violation("lastName", "must not be blank")
            );

        }

        @Test
        void negativeNull() {
            expectViolations(
                createJurorValidationResponseDto(
                    "123456789",
                    "John",
                    null
                ),
                new Violation("lastName", "must not be blank")
            );

        }

        @Test
        void negativeTooLarge() {
            expectViolations(
                createJurorValidationResponseDto(
                    "123456789",
                    "John",
                    RandomStringUtils.randomAlphabetic(21)
                ),
                new Violation("lastName", "length must be between 0 and 20")
            );
        }

        @Test
        void negativeHasPipes() {
            expectViolations(
                createJurorValidationResponseDto(
                    "123456789",
                    "John",
                    "Smith|"
                ),
                new Violation("lastName", "must match \"^$|^[^|]+$\"")
            );
        }
    }
}
