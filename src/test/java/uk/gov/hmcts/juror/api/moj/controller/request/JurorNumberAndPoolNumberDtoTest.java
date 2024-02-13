package uk.gov.hmcts.juror.api.moj.controller.request;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.AbstractValidatorTest;

class JurorNumberAndPoolNumberDtoTest extends AbstractValidatorTest<JurorNumberAndPoolNumberDto> {

    @Override
    protected JurorNumberAndPoolNumberDto createValidObject() {
        return createJurorNumberAndPoolNumberDto(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER);
    }

    protected JurorNumberAndPoolNumberDto createJurorNumberAndPoolNumberDto(
        String jurorNumber,
        String poolNumber
    ) {
        JurorNumberAndPoolNumberDto jurorNumberAndPoolNumberDto = new JurorNumberAndPoolNumberDto();
        jurorNumberAndPoolNumberDto.setJurorNumber(jurorNumber);
        jurorNumberAndPoolNumberDto.setPoolNumber(poolNumber);
        return jurorNumberAndPoolNumberDto;

    }

    @Test
    void positiveTypical() {
        JurorNumberAndPoolNumberDto dto = createJurorNumberAndPoolNumberDto(
            TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER);
        assertExpectNoViolations(dto);
    }

    @Test
    void negativeMissingJurorNumber() {
        JurorNumberAndPoolNumberDto dto = createJurorNumberAndPoolNumberDto(
            null, TestConstants.VALID_POOL_NUMBER);
        assertExpectViolations(dto,
            new Violation("jurorNumber", "must not be blank")
        );
    }

    @Test
    void negativeBlankJurorNumber() {
        JurorNumberAndPoolNumberDto dto = createJurorNumberAndPoolNumberDto(
            "", TestConstants.VALID_POOL_NUMBER);
        assertExpectViolations(dto,
            new Violation("jurorNumber", "must not be blank"),
            new Violation("jurorNumber", "must match \"^\\d{9}$\"")
        );
    }

    @Test
    void negativeInvalidJurorNumber() {
        JurorNumberAndPoolNumberDto dto = createJurorNumberAndPoolNumberDto(
            TestConstants.INVALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER);
        assertExpectViolations(dto,
            new Violation("jurorNumber", "must match \"^\\d{9}$\"")
        );
    }

    @Test
    void negativeMissingPoolNumber() {
        JurorNumberAndPoolNumberDto dto = createJurorNumberAndPoolNumberDto(
            TestConstants.VALID_JUROR_NUMBER, null);
        assertExpectViolations(dto,
            new Violation("poolNumber", "must not be blank")
        );
    }

    @Test
    void negativeBlankPoolNumber() {
        JurorNumberAndPoolNumberDto dto = createJurorNumberAndPoolNumberDto(
            TestConstants.VALID_JUROR_NUMBER, "");
        assertExpectViolations(dto,
            new Violation("poolNumber", "must not be blank"),
            new Violation("poolNumber", "must match \"^\\d{9}$\"")
        );
    }

    @Test
    void negativeInvalidPoolNumber() {
        JurorNumberAndPoolNumberDto dto = createJurorNumberAndPoolNumberDto(
            TestConstants.VALID_JUROR_NUMBER, TestConstants.INVALID_POOL_NUMBER);
        assertExpectViolations(dto,
            new Violation("poolNumber", "must match \"^\\d{9}$\"")
        );
    }
}
