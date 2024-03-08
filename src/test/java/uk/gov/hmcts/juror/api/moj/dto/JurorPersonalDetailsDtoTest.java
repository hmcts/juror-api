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
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPersonalDetailsDto;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class JurorPersonalDetailsDtoTest {

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
    public void addressLineOne_validAddressLineOne_hasNoViolation() {
        JurorPersonalDetailsDto jurorPersonalDetailsDto = buildJurorPersonalDetails();

        Set<ConstraintViolation<JurorPersonalDetailsDto>> violations = validator.validate(jurorPersonalDetailsDto);

        assertThat(violations.size()).isEqualTo(0);
    }

    @Test
    public void addressLineOne_spaceOnly_hasViolation() {
        JurorPersonalDetailsDto jurorPersonalDetailsDto = buildJurorPersonalDetails();
        jurorPersonalDetailsDto.setAddressLineOne("    ");

        ConstraintViolation<JurorPersonalDetailsDto> violation = getFirstConstraintViolation(jurorPersonalDetailsDto);

        assertThat(violation.getPropertyPath().toString()).isEqualTo("addressLineOne");
        assertThat(violation.getMessage()).isEqualTo("Juror address line 1 cannot be blank");
    }

    @Test
    public void addressLineOne_empty_hasViolation() {
        JurorPersonalDetailsDto jurorPersonalDetailsDto = buildJurorPersonalDetails();
        jurorPersonalDetailsDto.setAddressLineOne("");

        ConstraintViolation<JurorPersonalDetailsDto> violation = getFirstConstraintViolation(jurorPersonalDetailsDto);

        assertThat(violation.getPropertyPath().toString()).isEqualTo("addressLineOne");
        assertThat(violation.getMessage()).isEqualTo("Juror address line 1 cannot be blank");
    }

    @Test
    public void addressLineOne_null_hasViolation() {
        JurorPersonalDetailsDto jurorPersonalDetailsDto = buildJurorPersonalDetails();
        jurorPersonalDetailsDto.setAddressLineOne(null);

        ConstraintViolation<JurorPersonalDetailsDto> violation = getFirstConstraintViolation(jurorPersonalDetailsDto);

        assertThat(violation.getPropertyPath().toString()).isEqualTo("addressLineOne");
        assertThat(violation.getMessage()).isEqualTo("Juror address line 1 cannot be blank");
    }

    @Test
    public void addressLineOne_spaceAtStart_hasNoViolation() {
        JurorPersonalDetailsDto jurorPersonalDetailsDto = buildJurorPersonalDetails();
        jurorPersonalDetailsDto.setAddressLineOne("   1 First Street");

        Set<ConstraintViolation<JurorPersonalDetailsDto>> violations = validator.validate(jurorPersonalDetailsDto);

        assertThat(violations.size()).isEqualTo(0);
    }

    @Test
    public void addressLineOne_specialCharAtStartOfAddressLineOne_hasNoViolation() {
        JurorPersonalDetailsDto jurorPersonalDetailsDto = buildJurorPersonalDetails();
        jurorPersonalDetailsDto.setAddressLineOne("@1 First Street");

        Set<ConstraintViolation<JurorPersonalDetailsDto>> violations = validator.validate(jurorPersonalDetailsDto);

        assertThat(violations.size()).isEqualTo(0);
    }

    @Test
    public void addressLineOne_specialCharactersOnly_hasNoViolation() {
        JurorPersonalDetailsDto jurorPersonalDetailsDto = buildJurorPersonalDetails();
        jurorPersonalDetailsDto.setAddressLineOne("-*@_+/");

        Set<ConstraintViolation<JurorPersonalDetailsDto>> violations = validator.validate(jurorPersonalDetailsDto);

        assertThat(violations.size()).isEqualTo(0);
    }

    @Test
    public void addressLineOne_emoji_hasNoViolation() {
        JurorPersonalDetailsDto jurorPersonalDetailsDto = buildJurorPersonalDetails();
        jurorPersonalDetailsDto.setAddressLineOne("1 First Street 😉");

        Set<ConstraintViolation<JurorPersonalDetailsDto>> violations = validator.validate(jurorPersonalDetailsDto);

        assertThat(violations.size()).isEqualTo(0);
    }

    @Test
    public void addressLineOne_alphaNumericSpecialChars_hasNoViolation() {
        JurorPersonalDetailsDto jurorPersonalDetailsDto = buildJurorPersonalDetails();
        jurorPersonalDetailsDto.setAddressLineOne("Delete from xx where xxx = 12345;");

        Set<ConstraintViolation<JurorPersonalDetailsDto>> violations = validator.validate(jurorPersonalDetailsDto);

        assertThat(violations.size()).isEqualTo(0);
    }

    private ConstraintViolation<JurorPersonalDetailsDto> getFirstConstraintViolation(
        JurorPersonalDetailsDto jurorPersonalDetailsDto) {
        Set<ConstraintViolation<JurorPersonalDetailsDto>> violations = validator.validate(jurorPersonalDetailsDto);
        assertThat(violations).isNotEmpty();

        return violations.stream().findFirst().get();
    }

    private JurorPersonalDetailsDto buildJurorPersonalDetails() {
        JurorPersonalDetailsDto jurorPersonalDetailsDto = new JurorPersonalDetailsDto();

        jurorPersonalDetailsDto.setReplyMethod(ReplyMethod.DIGITAL);
        jurorPersonalDetailsDto.setTitle("Senorita");
        jurorPersonalDetailsDto.setFirstName("FName");
        jurorPersonalDetailsDto.setLastName("LName");
        jurorPersonalDetailsDto.setAddressLineOne("1 First Street");
        jurorPersonalDetailsDto.setAddressPostcode("MK5 6LA");

        return jurorPersonalDetailsDto;
    }
}