package uk.gov.hmcts.juror.api.moj.controller.request;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.juror.api.TestUtils.buildStringToLength;

@SuppressWarnings({
    "PMD.LawOfDemeter",
    "PMD.ExcessivePublicCount",
    "PMD.TooManyMethods"
})
@RunWith(SpringRunner.class)
public class JurorPaperResponseDtoTest {

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
    public void test_setJurorNumber_valid() {
        JurorPaperResponseDto response = createBasicResponse();
        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);

        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_setJurorNumber_tooShort() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setJurorNumber("12345678");
        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);

        assertThat(violations).as("Validation violation expected").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Juror Number property to be the cause of the constraint violation")
            .isEqualTo("jurorNumber");
    }

    @Test
    public void test_setJurorNumber_tooLong() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setJurorNumber("1234567890");
        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);

        assertThat(violations).as("Validation violation expected (numeric string too long)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Juror Number property to be the cause of the constraint violation")
            .isEqualTo("jurorNumber");
    }

    @Test
    public void test_setJurorNumber_containsNonNumericChar() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setJurorNumber("1234|6789");
        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);

        assertThat(violations).as("Validation violation expected (non-numeric character present)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Juror Number property to be the cause of the constraint violation")
            .isEqualTo("jurorNumber");
    }

    @Test
    public void test_setJurorNumber_notPresent() {
        JurorPaperResponseDto response = new JurorPaperResponseDto();
        response.setAddressLineOne("Test");
        response.setFirstName("FName");
        response.setLastName("LName");
        response.setAddressPostcode("CH1 2AN");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);

        assertThat(violations).as("Validation violation expected (juror number not present)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Juror Number property to be the cause of the constraint violation")
            .isEqualTo("jurorNumber");
    }

    @Test
    public void test_setTitle_valid() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setTitle("Mr");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected (valid title entered)").isEmpty();
    }

    @Test
    public void test_setTitle_tooLong() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setTitle("MrMrsDrMiss");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid title)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Title property to be the cause of the constraint violation")
            .isEqualTo("title");
    }

    @Test
    public void test_setTitle_containsPipe() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setTitle("Mr|Mrs");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid title)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Title property to be the cause of the constraint violation")
            .isEqualTo("title");
    }

    @Test
    public void test_setFirstName_valid() {
        JurorPaperResponseDto response = createBasicResponse();

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected (valid first name entered)").isEmpty();
    }

    @Test
    public void test_setFirstName_tooLong() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setFirstName(buildStringToLength(21));

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid first name)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the First Name property to be the cause of the constraint violation")
            .isEqualTo("firstName");
    }

    @Test
    public void test_setFirstName_containsPipe() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setFirstName("First|Name");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid first name)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the First Name property to be the cause of the constraint violation")
            .isEqualTo("firstName");
    }

    @Test
    public void test_setLastName_valid() {
        JurorPaperResponseDto response = createBasicResponse();

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected (valid last name entered)").isEmpty();
    }

    @Test
    public void test_setLastName_tooLong() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setLastName(buildStringToLength(21));

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid last name)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Last Name property to be the cause of the constraint violation")
            .isEqualTo("lastName");
    }

    @Test
    public void test_setLastName_containsPipe() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setLastName("Last|Name");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid last name)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Last Name property to be the cause of the constraint violation")
            .isEqualTo("lastName");
    }

    @Test
    public void test_setAddressLineOne_valid() {
        JurorPaperResponseDto response = createBasicResponse();

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected (valid address entered)").isEmpty();
    }

    @Test
    public void test_setAddressLineOne_tooLong() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setAddressLineOne(buildStringToLength(36));

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid address)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Address property to be the cause of the constraint violation")
            .isEqualTo("addressLineOne");
    }

    @Test
    public void test_setAddressLineOne_containsPipe() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setAddressLineOne("Test|Address");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid address)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Address property to be the cause of the constraint violation")
            .isEqualTo("addressLineOne");
    }

    @Test
    public void test_setAddressLineOne_notPresent() {
        JurorPaperResponseDto response = new JurorPaperResponseDto();
        response.setJurorNumber("123456789");
        response.setFirstName("FName");
        response.setLastName("LName");
        response.setAddressPostcode("CH1 2AN");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_setAddressLineTwo_valid() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setAddressLineTwo("Test");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected (valid addressLineTwo entered)").isEmpty();
    }

    @Test
    public void test_setAddressLineTwo_tooLong() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setAddressLineTwo(buildStringToLength(36));

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid addressLineTwo)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the addressLineTwo property to be the cause of the constraint violation")
            .isEqualTo("addressLineTwo");
    }

    @Test
    public void test_setAddressLineTwo_containsPipe() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setAddressLineTwo("Test|addressLineTwo");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid addressLineTwo)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the addressLineTwo property to be the cause of the constraint violation")
            .isEqualTo("addressLineTwo");
    }

    @Test
    public void test_setAddressLineThree_valid() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setAddressLineThree("Test");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected (valid addressLineThree entered)").isEmpty();
    }

    @Test
    public void test_setAddressLineThree_tooLong() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setAddressLineThree(buildStringToLength(36));

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid addressLineThree)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the addressLineThree property to be the cause of the constraint violation")
            .isEqualTo("addressLineThree");
    }

    @Test
    public void test_setAddressLineThree_containsPipe() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setAddressLineThree("Test|addressLineThree");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid addressLineThree)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the addressLineThree property to be the cause of the constraint violation")
            .isEqualTo("addressLineThree");
    }

    @Test
    public void test_setAddressTown_valid() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setAddressTown("Test");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected (valid addressTown entered)").isEmpty();
    }

    @Test
    public void test_setAddressTown_tooLong() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setAddressTown(buildStringToLength(36));

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid addressTown)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the addressTown property to be the cause of the constraint violation")
            .isEqualTo("addressTown");
    }

    @Test
    public void test_setAddressTown_containsPipe() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setAddressTown("Test|addressTown");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid addressTown)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the addressTown property to be the cause of the constraint violation")
            .isEqualTo("addressTown");
    }

    @Test
    public void test_setAddressCounty_valid() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setAddressCounty("Test");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected (valid addressCounty entered)").isEmpty();
    }

    @Test
    public void test_setAddressCounty_tooLong() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setAddressCounty(buildStringToLength(36));

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid addressCounty)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the addressCounty property to be the cause of the constraint violation")
            .isEqualTo("addressCounty");
    }

    @Test
    public void test_setAddressCounty_containsPipe() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setAddressCounty("Test|addressCounty");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid addressCounty)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the addressCounty property to be the cause of the constraint violation")
            .isEqualTo("addressCounty");
    }

    @Test
    public void test_setAddressPostcode_valid() {
        JurorPaperResponseDto response = createBasicResponse();

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_setAddressPostcode_invalid() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setAddressPostcode("ABC 123");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (Address Postcode format invalid)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Address Postcode property to be the cause of the constraint violation")
            .isEqualTo("addressPostcode");
    }

    @Test
    public void test_setAddressPostcode_pipeCharacter() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setAddressPostcode("CH1|2AN");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (Address Postcode format invalid)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Address Postcode property to be the cause of the constraint violation")
            .isEqualTo("addressPostcode");
    }

    @Test
    public void test_setAddressPostcode_notPresent() {
        JurorPaperResponseDto response = new JurorPaperResponseDto();
        response.setJurorNumber("123456789");
        response.setFirstName("FName");
        response.setLastName("LName");
        response.setAddressLineOne("Test");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (Address Postcode format invalid)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Address Postcode property to be the cause of the constraint violation")
            .isEqualTo("addressPostcode");
    }

    @Test
    public void test_setDateOfBirth_valid() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setDateOfBirth(LocalDate.now().minusYears(25));

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_setDateOfBirth_tooRecent() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setDateOfBirth(LocalDate.now());

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (date of birth is invalid)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Date of Birth property to be the cause of the constraint violation")
            .isEqualTo("dateOfBirth");
    }

    @Test
    public void test_setDateOfBirth_tooLongAgo() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setDateOfBirth(LocalDate.now().minusYears(125));

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (date of birth is invalid)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Date of Birth property to be the cause of the constraint violation")
            .isEqualTo("dateOfBirth");
    }

    @Test
    public void test_setPrimaryPhone_valid() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setPrimaryPhone("+44(0)123-45678");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_setPrimaryPhone_validNoSpecials() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setPrimaryPhone("01234 567890");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_setPrimaryPhone_tooLong() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setPrimaryPhone("+44 1234 - 56789");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (phone number format invalid)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Phone Number property to be the cause of the constraint violation")
            .isEqualTo("primaryPhone");
    }

    @Test
    public void test_setPrimaryPhone_invalidSpecialChar() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setPrimaryPhone("01234|567890");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (phone number format invalid)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Phone Number property to be the cause of the constraint violation")
            .isEqualTo("primaryPhone");
    }

    @Test
    public void test_setSecondaryPhone_valid() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setSecondaryPhone("+44(123)-45678");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_setSecondaryPhone_validNoSpecials() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setSecondaryPhone("01234 567890");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_setSecondaryPhone_tooLong() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setSecondaryPhone("+44 1234 - 56789");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (alternative phone number format invalid)")
            .isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Alt Phone Number property to be the cause of the constraint violation")
            .isEqualTo("secondaryPhone");
    }

    @Test
    public void test_setSecondaryPhone_invalidSpecialChar() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setSecondaryPhone("01234|567890");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (alternative phone number format invalid)")
            .isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Alt Phone Number property to be the cause of the constraint violation")
            .isEqualTo("secondaryPhone");
    }

    @Test
    public void test_setEmailAddress_valid() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setEmailAddress("test@email.com");

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_setEmailAddress_tooLong() {
        JurorPaperResponseDto response = createBasicResponse();
        String emailSuffix = "@email.com";
        response.setEmailAddress(buildStringToLength(255 - emailSuffix.length()) + emailSuffix);

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (email format invalid)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Email property to be the cause of the constraint violation")
            .isEqualTo("emailAddress");
    }

    @Test
    public void test_setEmailAddress_invalid() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setEmailAddress(buildStringToLength(250));

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (email format invalid)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Email property to be the cause of the constraint violation")
            .isEqualTo("emailAddress");
    }

    @Test
    public void test_setCjsEmployment_valid() {
        JurorPaperResponseDto response = createBasicResponse();
        JurorPaperResponseDto.CjsEmployment cjsEmployment = JurorPaperResponseDto.CjsEmployment.builder()
            .cjsEmployer("Police")
            .cjsEmployerDetails("Some test details")
            .build();
        response.setCjsEmployment(Collections.singletonList(cjsEmployment));

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_cjsEmployment_valid() {
        JurorPaperResponseDto.CjsEmployment cjsEmployment = JurorPaperResponseDto.CjsEmployment.builder()
            .cjsEmployer("Police")
            .cjsEmployerDetails("Some test details")
            .build();

        Set<ConstraintViolation<JurorPaperResponseDto.CjsEmployment>> violations = validator.validate(cjsEmployment);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_cjsEmployment_invalidEmployer() {
        JurorPaperResponseDto.CjsEmployment cjsEmployment = JurorPaperResponseDto.CjsEmployment.builder()
            .cjsEmployerDetails("Some test details")
            .build();

        Set<ConstraintViolation<JurorPaperResponseDto.CjsEmployment>> violations = validator.validate(cjsEmployment);
        assertThat(violations).as("Validation violation expected (cjs employer is null)").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto.CjsEmployment> constraintViolation =
            violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the CJS Employer property to be the cause of the constraint violation")
            .isEqualTo("cjsEmployer");
    }

    @Test
    public void test_setSpecialNeeds_valid() {
        JurorPaperResponseDto response = createBasicResponse();
        JurorPaperResponseDto.ReasonableAdjustment specialNeed = JurorPaperResponseDto.ReasonableAdjustment.builder()
            .assistanceType("V")
            .assistanceTypeDetails("Some test details")
            .build();
        response.setReasonableAdjustments(Collections.singletonList(specialNeed));

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_specialNeeds_valid() {
        JurorPaperResponseDto.ReasonableAdjustment specialNeed = JurorPaperResponseDto.ReasonableAdjustment.builder()
            .assistanceType("V")
            .assistanceTypeDetails("Some test details")
            .build();

        Set<ConstraintViolation<JurorPaperResponseDto.ReasonableAdjustment>> violations =
            validator.validate(specialNeed);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_specialNeeds_invalidAssistanceType() {
        JurorPaperResponseDto.ReasonableAdjustment specialNeed = JurorPaperResponseDto.ReasonableAdjustment.builder()
            .assistanceTypeDetails("Some test details")
            .build();

        Set<ConstraintViolation<JurorPaperResponseDto.ReasonableAdjustment>> violations =
            validator.validate(specialNeed);
        assertThat(violations).as("Validation violation expected").isNotEmpty();

        ConstraintViolation<JurorPaperResponseDto.ReasonableAdjustment> constraintViolation =
            violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Assistance Type property to be the cause of the constraint violation")
            .isEqualTo("assistanceType");
    }

    @Test
    public void test_setDeferral_true() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setDeferral(Boolean.TRUE);

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getDeferral()).isTrue();
    }

    @Test
    public void test_setDeferral_false() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setDeferral(Boolean.FALSE);

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getDeferral()).isFalse();
    }

    @Test
    public void test_setDeferral_empty() {
        JurorPaperResponseDto response = createBasicResponse();

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getDeferral()).isNull();
    }

    @Test
    public void test_setExcusal_true() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setExcusal(Boolean.TRUE);

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getExcusal()).isTrue();
    }

    @Test
    public void test_setExcusal_false() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setExcusal(Boolean.FALSE);

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getExcusal()).isFalse();
    }

    @Test
    public void test_setExcusal_empty() {
        JurorPaperResponseDto response = createBasicResponse();

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getExcusal()).isNull();
    }

    @Test
    public void test_setEligibility_valid() {
        JurorPaperResponseDto response = createBasicResponse();
        JurorPaperResponseDto.Eligibility eligibility = JurorPaperResponseDto.Eligibility.builder()
            .livedConsecutive(true)
            .mentalHealthAct(false)
            .mentalHealthCapacity(false)
            .onBail(false)
            .convicted(false)
            .build();
        response.setEligibility(eligibility);

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_eligibility_valid() {
        JurorPaperResponseDto.Eligibility eligibility = JurorPaperResponseDto.Eligibility.builder()
            .livedConsecutive(true)
            .mentalHealthAct(false)
            .mentalHealthCapacity(false)
            .onBail(false)
            .convicted(false)
            .build();

        Set<ConstraintViolation<JurorPaperResponseDto.Eligibility>> violations = validator.validate(eligibility);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_setSigned_true() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setSigned(Boolean.TRUE);

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getSigned()).isTrue();
    }

    @Test
    public void test_setSigned_false() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setSigned(Boolean.FALSE);

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getSigned()).isFalse();
    }

    @Test
    public void test_setSigned_empty() {
        JurorPaperResponseDto response = createBasicResponse();

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getSigned()).isNull();
    }

    @Test
    public void test_setThirdParty_valid() {
        JurorPaperResponseDto response = createBasicResponse();
        JurorPaperResponseDto.ThirdParty thirdParty = JurorPaperResponseDto.ThirdParty.builder()
            .relationship("Spouse")
            .thirdPartyReason("Some test reason")
            .build();
        response.setThirdParty(thirdParty);

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_setWelsh_valid() {
        JurorPaperResponseDto response = createBasicResponse();
        response.setWelsh(Boolean.TRUE);

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getWelsh()).isTrue();
    }

    @Test
    public void test_setWelsh_default() {
        JurorPaperResponseDto response = createBasicResponse();

        Set<ConstraintViolation<JurorPaperResponseDto>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getWelsh()).isFalse();
    }

    private JurorPaperResponseDto createBasicResponse() {
        JurorPaperResponseDto response = new JurorPaperResponseDto();
        response.setJurorNumber("123456789");
        response.setFirstName("FName");
        response.setLastName("LName");
        response.setAddressLineOne("Test");
        response.setAddressPostcode("CH1 2AN");
        return response;
    }

}
