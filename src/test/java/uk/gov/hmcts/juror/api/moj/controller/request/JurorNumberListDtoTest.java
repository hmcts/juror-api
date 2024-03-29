package uk.gov.hmcts.juror.api.moj.controller.request;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;

import java.util.List;

class JurorNumberListDtoTest extends AbstractValidatorTest<JurorNumberListDto> {

    @Override
    protected JurorNumberListDto createValidObject() {
        return createJurorNumberListDto(List.of("123456789", "123456788"));
    }

    protected JurorNumberListDto createJurorNumberListDto(List<String> jurorNumbers) {
        JurorNumberListDto jurorNumberListDto = new JurorNumberListDto();
        jurorNumberListDto.setJurorNumbers(jurorNumbers);
        return jurorNumberListDto;
    }

    @Test
    void positiveJurorNumbers() {
        JurorNumberListDto jurorNumberListDto = createJurorNumberListDto(List.of("123456789", "123456788"));
        assertExpectNoViolations(jurorNumberListDto);
    }

    @Test
    void negativeJurorNumbersEmpty() {
        JurorNumberListDto jurorNumberListDto = createJurorNumberListDto(List.of());

        assertExpectViolations(jurorNumberListDto,
            new Violation("jurorNumbers", "must not be empty")
        );
    }

    @Test
    void negativeJurorNumbersNull() {
        JurorNumberListDto jurorNumberListDto = createJurorNumberListDto(null);
        assertExpectViolations(jurorNumberListDto,
            new Violation("jurorNumbers", "must not be empty")
        );
    }

    @Test
    void negativeJurorNumberInvalidTooLarge() {
        JurorNumberListDto jurorNumberListDto = createJurorNumberListDto(List.of("1234567890"));
        assertExpectViolations(jurorNumberListDto,
            new Violation("jurorNumbers[0].<list element>", "must match \"^\\d{9}$\"")
        );
    }

    @Test
    void negativeJurorNumberInvalidTooSmall() {
        JurorNumberListDto jurorNumberListDto = createJurorNumberListDto(List.of("12345678"));
        assertExpectViolations(jurorNumberListDto,
            new Violation("jurorNumbers[0].<list element>", "must match \"^\\d{9}$\"")
        );
    }

    @Test
    void negativeJurorNumberInvalidNotNumeric() {
        JurorNumberListDto jurorNumberListDto = createJurorNumberListDto(List.of("ABC"));
        assertExpectViolations(jurorNumberListDto,
            new Violation("jurorNumbers[0].<list element>", "must match \"^\\d{9}$\"")
        );
    }
}
