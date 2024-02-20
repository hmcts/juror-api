package uk.gov.hmcts.juror.api.moj.controller.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;

@DisplayName("CompleteServiceValidationResponseDto")
class CompleteServiceValidationResponseDtoTest extends AbstractValidatorTest<CompleteServiceValidationResponseDto> {


    @Override
    protected CompleteServiceValidationResponseDto createValidObject() {
        return createCompleteServiceValidationResponseDto(
            List.of(mock(JurorStatusValidationResponseDto.class)),
            List.of(mock(JurorStatusValidationResponseDto.class))
        );
    }

    private CompleteServiceValidationResponseDto createCompleteServiceValidationResponseDto(
        List<JurorStatusValidationResponseDto> valid,
        List<JurorStatusValidationResponseDto> invalidNotResponded
    ) {
        return CompleteServiceValidationResponseDto.builder()
            .valid(valid)
            .invalidNotResponded(invalidNotResponded)
            .build();
    }

    @Test
    void positiveTypical() {
        assertExpectNoViolations(
            createCompleteServiceValidationResponseDto(
                List.of(mock(JurorStatusValidationResponseDto.class)),
                List.of(mock(JurorStatusValidationResponseDto.class))
            )
        );
    }


    @DisplayName("status")
    @Nested
    class Valid {

        @Test
        void negativeNullList() {
            assertExpectViolations(
                createCompleteServiceValidationResponseDto(
                    null,
                    List.of(mock(JurorStatusValidationResponseDto.class))
                ),
                new Violation("valid", "must not be null")
            );
        }

        @Test
        void negativeNullValue() {
            List<JurorStatusValidationResponseDto> listWithNull = new ArrayList<>();
            listWithNull.add(mock(JurorStatusValidationResponseDto.class));
            listWithNull.add(null);
            listWithNull.add(mock(JurorStatusValidationResponseDto.class));

            assertExpectViolations(
                createCompleteServiceValidationResponseDto(
                    listWithNull,
                    List.of(mock(JurorStatusValidationResponseDto.class))
                ),
                new Violation("valid[1].<list element>", "must not be null")
            );
        }
    }

    @DisplayName("invalidNotResponded")
    @Nested
    class InvalidNotResponded {
        @Test
        void negativeNullList() {
            assertExpectViolations(
                createCompleteServiceValidationResponseDto(
                    List.of(mock(JurorStatusValidationResponseDto.class)),
                    null
                ),
                new Violation("invalidNotResponded", "must not be null")
            );
        }

        @Test
        void negativeNullValue() {
            List<JurorStatusValidationResponseDto> listWithNull = new ArrayList<>();
            listWithNull.add(mock(JurorStatusValidationResponseDto.class));
            listWithNull.add(null);
            listWithNull.add(mock(JurorStatusValidationResponseDto.class));

            assertExpectViolations(
                createCompleteServiceValidationResponseDto(
                    List.of(mock(JurorStatusValidationResponseDto.class)),
                    listWithNull
                ),
                new Violation("invalidNotResponded[1].<list element>", "must not be null")
            );
        }
    }
}
