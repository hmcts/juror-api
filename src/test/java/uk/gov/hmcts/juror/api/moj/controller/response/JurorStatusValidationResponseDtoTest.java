package uk.gov.hmcts.juror.api.moj.controller.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;

@DisplayName("JurorStatusValidationResponseDto")
@SuppressWarnings(
    "PMD.JUnitTestsShouldIncludeAssert"//False positive done via inheritance
)
public class JurorStatusValidationResponseDtoTest extends JurorValidationResponseDtoTest {


    @Override
    protected JurorValidationResponseDto createJurorValidationResponseDto(
        String jurorNumber,
        String firstName,
        String lastName
    ) {
        return createJurorStatusValidationResponseDto(
            jurorNumber, firstName, lastName, IJurorStatus.RESPONDED);
    }

    protected JurorStatusValidationResponseDto createJurorStatusValidationResponseDto(
        String jurorNumber,
        String firstName,
        String lastName,
        Integer status
    ) {
        return JurorStatusValidationResponseDto.builder()
            .jurorNumber(jurorNumber)
            .firstName(firstName)
            .lastName(lastName)
            .status(status)
            .build();
    }

    @Override
    @Test
    void positiveTypical() {
        expectNoViolations(
            createJurorStatusValidationResponseDto(
                "123456789",
                "John",
                "Smith",
                IJurorStatus.RESPONDED
            )
        );
    }

    @DisplayName("status")
    @Nested
    class Status {

        @Test
        void negativeNull() {
            expectViolations(
                createJurorStatusValidationResponseDto(
                    "123456789",
                    "John",
                    "Smith",
                    null
                ),
                new Violation("status", "must not be null")
            );
        }

        @Test
        void negativeTooLarge() {
            expectViolations(
                createJurorStatusValidationResponseDto(
                    "123456789",
                    "John",
                    "Smith",
                    12
                ),
                new Violation("status", "must be less than or equal to 11")
            );
        }

        @Test
        void negativeTooSmall() {
            expectViolations(
                createJurorStatusValidationResponseDto(
                    "123456789",
                    "John",
                    "Smith",
                    -1
                ),
                new Violation("status", "must be greater than or equal to 0")
            );
        }
    }
}
