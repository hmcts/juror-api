package uk.gov.hmcts.juror.api.moj.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.JurorNonAttendanceDto;

import java.util.Set;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class JurorNonAttendanceDtoTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    public static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    public static void close() {
        validatorFactory.close();
    }

    @Test
    public void happyPathNoViolations() {
        JurorNonAttendanceDto jurorNonAttendanceDto = buildJurorNonAttendanceDto();
        Set<ConstraintViolation<JurorNonAttendanceDto>> violations = validator.validate(jurorNonAttendanceDto);
        assertThat(violations.size()).isEqualTo(0);
    }

    @Test
    public void attendanceDateCannotBeNull() {
        JurorNonAttendanceDto jurorNonAttendanceDto = buildJurorNonAttendanceDto();
        jurorNonAttendanceDto.setNonAttendanceDate(null);
        ConstraintViolation<JurorNonAttendanceDto> violation = getFirstConstraintViolation(jurorNonAttendanceDto);
        assertThat(violation.getPropertyPath().toString()).isEqualTo("nonAttendanceDate");
        assertThat(violation.getMessage()).isEqualTo("must not be null");
    }

    @Test
    public void jurorNumberCannotBeBlank() {
        JurorNonAttendanceDto jurorNonAttendanceDto = buildJurorNonAttendanceDto();
        jurorNonAttendanceDto.setJurorNumber(null);
        ConstraintViolation<JurorNonAttendanceDto> violation = getFirstConstraintViolation(jurorNonAttendanceDto);
        assertThat(violation.getPropertyPath().toString()).isEqualTo("jurorNumber");
        assertThat(violation.getMessage()).isEqualTo("must not be blank");
    }

    @Test
    public void poolNumberCannotBeBlank() {
        JurorNonAttendanceDto jurorNonAttendanceDto = buildJurorNonAttendanceDto();
        jurorNonAttendanceDto.setPoolNumber(null);
        ConstraintViolation<JurorNonAttendanceDto> violation = getFirstConstraintViolation(jurorNonAttendanceDto);
        assertThat(violation.getPropertyPath().toString()).isEqualTo("poolNumber");
        assertThat(violation.getMessage()).isEqualTo("must not be blank");
    }

    @Test
    public void locationCodeCannotBeBlank() {
        JurorNonAttendanceDto jurorNonAttendanceDto = buildJurorNonAttendanceDto();
        jurorNonAttendanceDto.setLocationCode(null);
        ConstraintViolation<JurorNonAttendanceDto> violation = getFirstConstraintViolation(jurorNonAttendanceDto);
        assertThat(violation.getPropertyPath().toString()).isEqualTo("locationCode");
        assertThat(violation.getMessage()).isEqualTo("must not be blank");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "12",
        "2",
        "1234",
    })
    public void locationCodeMustBe3NumericalChars(String string) {
        JurorNonAttendanceDto jurorNonAttendanceDto = buildJurorNonAttendanceDto();
        jurorNonAttendanceDto.setLocationCode(string);
        ConstraintViolation<JurorNonAttendanceDto> violation = getFirstConstraintViolation(jurorNonAttendanceDto);
        assertThat(violation.getPropertyPath().toString()).isEqualTo("locationCode");
        assertThat(violation.getMessage()).isEqualTo("size must be between 3 and 3");
    }

    @Test
    public void locationCodeMustBeNumericalChars() {
        JurorNonAttendanceDto jurorNonAttendanceDto = buildJurorNonAttendanceDto();
        jurorNonAttendanceDto.setLocationCode("ABC");
        ConstraintViolation<JurorNonAttendanceDto> violation = getFirstConstraintViolation(jurorNonAttendanceDto);
        assertThat(violation.getPropertyPath().toString()).isEqualTo("locationCode");
        assertThat(violation.getMessage()).isEqualTo("This field must be a number");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "AAA",
    })
    public void locationCodeMustBeNumericString(String string) {
        JurorNonAttendanceDto jurorNonAttendanceDto = buildJurorNonAttendanceDto();
        jurorNonAttendanceDto.setLocationCode(string);
        ConstraintViolation<JurorNonAttendanceDto> violation = getFirstConstraintViolation(jurorNonAttendanceDto);
        assertThat(violation.getPropertyPath().toString()).isEqualTo("locationCode");
        assertThat(violation.getMessage()).isEqualTo("This field must be a number");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "AAA23er123124",
        "2344",
    })
    public void poolNumberMustBeValid(String string) {
        JurorNonAttendanceDto jurorNonAttendanceDto = buildJurorNonAttendanceDto();
        jurorNonAttendanceDto.setPoolNumber(string);
        ConstraintViolation<JurorNonAttendanceDto> violation = getFirstConstraintViolation(jurorNonAttendanceDto);
        assertThat(violation.getPropertyPath().toString()).isEqualTo("poolNumber");
        assertThat(violation.getMessage()).isEqualTo("must match \"^\\d{9}$\"");
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "AAA23er123124",
        "2312",
    })
    public void jurorNumberMustBeValid(String string) {
        JurorNonAttendanceDto jurorNonAttendanceDto = buildJurorNonAttendanceDto();
        jurorNonAttendanceDto.setJurorNumber(string);
        ConstraintViolation<JurorNonAttendanceDto> violation = getFirstConstraintViolation(jurorNonAttendanceDto);
        assertThat(violation.getPropertyPath().toString()).isEqualTo("jurorNumber");
        assertThat(violation.getMessage()).isEqualTo("must match \"^\\d{9}$\"");
    }

    private ConstraintViolation<JurorNonAttendanceDto> getFirstConstraintViolation(
        JurorNonAttendanceDto jurorNonAttendanceDto) {
        Set<ConstraintViolation<JurorNonAttendanceDto>> violations = validator.validate(jurorNonAttendanceDto);
        assertThat(violations).isNotEmpty();

        return violations.stream().findFirst().get();
    }

    private JurorNonAttendanceDto buildJurorNonAttendanceDto() {
        return JurorNonAttendanceDto.builder()
            .jurorNumber("111111111")
            .nonAttendanceDate(now())
            .poolNumber("415230101")
            .locationCode("415")
            .build();
    }
}