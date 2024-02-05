package uk.gov.hmcts.juror.api.moj.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.moj.controller.response.ReissueLetterListResponseDto;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
public class ReissueLetterListResponseDtoTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeClass
    public static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterClass
    public static void close() {
        validatorFactory.close();
    }

    @Test
    public void noViolations() {
        ReissueLetterListResponseDto reissueLetterListResponseDto = buildLetterListResponseDto();

        Set<ConstraintViolation<ReissueLetterListResponseDto>> violations = validator.validate(reissueLetterListResponseDto);

        assertThat(violations.size()).isEqualTo(0);
    }

    @Test
    public void headingsHadViolation() {
        ReissueLetterListResponseDto reissueLetterListResponseDto = buildLetterListResponseDto();
        reissueLetterListResponseDto.setHeadings(List.of());

        Set<ConstraintViolation<ReissueLetterListResponseDto>> violations = validator.validate(reissueLetterListResponseDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.size()).isEqualTo(1);
        ConstraintViolation<ReissueLetterListResponseDto> violation = violations.stream().findFirst().get();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("headings");
        assertThat(violation.getMessage()).isEqualTo("must not be empty");
    }

    @Test
    public void dataTypesViolation() {
        ReissueLetterListResponseDto reissueLetterListResponseDto = buildLetterListResponseDto();
        reissueLetterListResponseDto.setDataTypes(List.of());

        Set<ConstraintViolation<ReissueLetterListResponseDto>> violations = validator.validate(reissueLetterListResponseDto);
        assertThat(violations).isNotEmpty();
        assertThat(violations.size()).isEqualTo(1);
        ConstraintViolation<ReissueLetterListResponseDto> violation = violations.stream().findFirst().get();
        assertThat(violation.getPropertyPath().toString()).isEqualTo("dataTypes");
        assertThat(violation.getMessage()).isEqualTo("must not be empty");
    }

    private ReissueLetterListResponseDto buildLetterListResponseDto() {
        ReissueLetterListResponseDto reissueLetterListResponseDto = new ReissueLetterListResponseDto();

        reissueLetterListResponseDto.setHeadings(List.of("heading1", "heading2"));
        reissueLetterListResponseDto.setDataTypes(List.of("string", "date"));
        reissueLetterListResponseDto.setData(List.of(List.of("data1", "data2")));

        return reissueLetterListResponseDto;
    }
}