package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.juror.api.TestUtils.buildStringToLength;

@RunWith(SpringRunner.class)
@SuppressWarnings({
    "PMD.ExcessivePublicCount",
    "PMD.TooManyMethods"
})
public class JurorPaperResponseTest {

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
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);

        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_setJurorNumber_tooShort() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("12345678");
        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);

        assertThat(violations).as("Validation violation expected (numeric string too short)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Juror Number property to be the cause of the constraint violation")
            .isEqualTo("jurorNumber");
    }

    @Test
    public void test_setJurorNumber_tooLong() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("1234567890");
        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);

        assertThat(violations).as("Validation violation expected (numeric string too long)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Juror Number property to be the cause of the constraint violation")
            .isEqualTo("jurorNumber");
    }

    @Test
    public void test_setJurorNumber_containsNonNumericChar() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("1234|6789");
        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);

        assertThat(violations).as("Validation violation expected (non-numeric character present)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Juror Number property to be the cause of the constraint violation")
            .isEqualTo("jurorNumber");
    }

    @Test
    public void test_setJurorNumber_notPresent() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);

        assertThat(violations).as("Validation violation expected (juror number not present)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Juror Number property to be the cause of the constraint violation")
            .isEqualTo("jurorNumber");
    }

    @Test
    public void test_setDateReceived_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected (date received present)").isEmpty();
    }

    @Test
    public void test_setDateReceived_notPresent() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setJurorNumber("123456789");
        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);

        assertThat(violations).as("Validation violation expected (date received not present)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Date Received property to be the cause of the constraint violation")
            .isEqualTo("dateReceived");
    }

    @Test
    public void test_setTitle_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setTitle("Mr");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected (valid title entered)").isEmpty();
    }

    @Test
    public void test_setTitle_tooLong() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setTitle("MrMrsDrMiss");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid title)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Title property to be the cause of the constraint violation")
            .isEqualTo("title");
    }

    @Test
    public void test_setTitle_containsPipe() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setTitle("Mr|Mrs");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid title)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Title property to be the cause of the constraint violation")
            .isEqualTo("title");
    }

    @Test
    public void test_setFirstName_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setFirstName("FName");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected (valid first name entered)").isEmpty();
    }

    @Test
    public void test_setFirstName_tooLong() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setFirstName(buildStringToLength(21));

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid first name)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the First Name property to be the cause of the constraint violation")
            .isEqualTo("firstName");
    }

    @Test
    public void test_setFirstName_containsPipe() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setFirstName("First|Name");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid first name)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the First Name property to be the cause of the constraint violation")
            .isEqualTo("firstName");
    }

    @Test
    public void test_setLastName_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setLastName("LName");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected (valid last name entered)").isEmpty();
    }

    @Test
    public void test_setLastName_tooLong() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setLastName(buildStringToLength(21));

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid last name)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Last Name property to be the cause of the constraint violation")
            .isEqualTo("lastName");
    }

    @Test
    public void test_setLastName_containsPipe() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setLastName("Last|Name");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid last name)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Last Name property to be the cause of the constraint violation")
            .isEqualTo("lastName");
    }

    @Test
    public void test_setAddress_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected (valid address entered)").isEmpty();
    }

    @Test
    public void test_setAddress_tooLong() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1(buildStringToLength(36));
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid addressLine1)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the addressLine1 property to be the cause of the constraint violation")
            .isEqualTo("addressLine1");
    }

    @Test
    public void test_setAddress_containsPipe() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test|addressLine1");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid addressLine1)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the addressLine1 property to be the cause of the constraint violation")
            .isEqualTo("addressLine1");
    }

    @Test
    public void test_setAddress_notPresent() {
        PaperResponse response = new PaperResponse();
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (addressLine1 not present)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the addressLine1 property to be the cause of the constraint violation")
            .isEqualTo("addressLine1");
    }

    @Test
    public void test_setAddressLine2_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setAddressLine2("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected (valid addressLine2 entered)").isEmpty();
    }

    @Test
    public void test_setAddressLine2_tooLong() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setAddressLine2(buildStringToLength(36));
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid addressLine2)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the addressLine2 property to be the cause of the constraint violation")
            .isEqualTo("addressLine2");
    }

    @Test
    public void test_setAddressLine2_containsPipe() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setAddressLine2("Test|addressLine2");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid addressLine2)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the addressLine2 property to be the cause of the constraint violation")
            .isEqualTo("addressLine2");
    }

    @Test
    public void test_setAddressLine3_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setAddressLine3("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected (valid addressLine3 entered)").isEmpty();
    }

    @Test
    public void test_setAddressLine3_tooLong() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setAddressLine3(buildStringToLength(36));
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid addressLine3)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the addressLine3 property to be the cause of the constraint violation")
            .isEqualTo("addressLine3");
    }

    @Test
    public void test_setAddressLine3_containsPipe() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setAddressLine3("Test|addressLine3");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid addressLine3)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the addressLine3 property to be the cause of the constraint violation")
            .isEqualTo("addressLine3");
    }

    @Test
    public void test_setAddressLine4_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setAddressLine4("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected (valid addressLine4 entered)").isEmpty();
    }

    @Test
    public void test_setAddressLine4_tooLong() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setAddressLine4(buildStringToLength(36));
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid addressLine4)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the addressLine4 property to be the cause of the constraint violation")
            .isEqualTo("addressLine4");
    }

    @Test
    public void test_setAddressLine4_containsPipe() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setAddressLine4("Test|addressLine4");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid addressLine4)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the addressLine4 property to be the cause of the constraint violation")
            .isEqualTo("addressLine4");
    }

    @Test
    public void test_setAddressLine5_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setAddressLine5("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected (valid addressLine5 entered)").isEmpty();
    }

    @Test
    public void test_setAddressLine5_tooLong() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setAddressLine5(buildStringToLength(36));
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid addressLine5)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the addressLine5 property to be the cause of the constraint violation")
            .isEqualTo("addressLine5");
    }

    @Test
    public void test_setAddressLine5_containsPipe() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setAddressLine5("Test|addressLine5");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid addressLine5)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the addressLine5 property to be the cause of the constraint violation")
            .isEqualTo("addressLine5");
    }

    @Test
    public void test_setPostCode_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setPostcode("CH1 2AN");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_setPostCode_invalid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setPostcode("ABC 123");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (postcode format invalid)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Postcode property to be the cause of the constraint violation")
            .isEqualTo("postcode");
    }

    @Test
    public void test_setPostCode_pipeCharacter() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setPostcode("CH1|2AN");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (postcode format invalid)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Postcode property to be the cause of the constraint violation")
            .isEqualTo("postcode");
    }

    @Test
    public void test_setProcessingStatus_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setProcessingStatus(mock(JurorResponseAuditRepositoryMod.class), ProcessingStatus.AWAITING_CONTACT);

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getProcessingStatus()).isEqualTo(ProcessingStatus.AWAITING_CONTACT);
    }

    @Test
    public void test_setProcessingStatus_default() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setPostcode("CH1 2AN");

        assertThat(response.getProcessingStatus()).isEqualTo(ProcessingStatus.TODO);
    }

    @Test
    public void test_setPhoneNumber_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setPhoneNumber("+44(123)-45678");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_setDateOfBirth_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setDateOfBirth(LocalDate.now().minusYears(25));

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_setDateOfBirth_tooSoon() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setDateOfBirth(LocalDate.now());

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (date of birth is invalid)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Date of Birth property to be the cause of the constraint violation")
            .isEqualTo("dateOfBirth");
    }

    @Test
    public void test_setDateOfBirth_tooLongAgo() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setDateOfBirth(LocalDate.now().minusYears(125));

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (date of birth is invalid)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Date of Birth property to be the cause of the constraint violation")
            .isEqualTo("dateOfBirth");
    }

    @Test
    public void test_setPhoneNumber_validNoSpecials() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setPhoneNumber("01234 567890");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_setPhoneNumber_tooLong() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setPhoneNumber("+44 1234 - 567895678956");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (phone number format invalid)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Phone Number property to be the cause of the constraint violation")
            .isEqualTo("phoneNumber");
    }

    @Test
    public void test_setPhoneNumber_invalidSpecialChar() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setPhoneNumber("01234|567890");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (phone number format invalid)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Phone Number property to be the cause of the constraint violation")
            .isEqualTo("phoneNumber");
    }

    @Test
    public void test_setAltPhoneNumber_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setAltPhoneNumber("+44(123)-45678");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_setAltPhoneNumber_validNoSpecials() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setAltPhoneNumber("01234 567890");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_setAltPhoneNumber_tooLong() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setAltPhoneNumber("+44 1234 - 567895678956");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (alternative phone number format invalid)")
            .isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Alt Phone Number property to be the cause of the constraint violation")
            .isEqualTo("altPhoneNumber");
    }

    @Test
    public void test_setAltPhoneNumber_invalidSpecialChar() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setAltPhoneNumber("01234|567890");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (alternative phone number format invalid)")
            .isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Alt Phone Number property to be the cause of the constraint violation")
            .isEqualTo("altPhoneNumber");
    }

    @Test
    public void test_setEmail_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setEmail("test@email.com");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_setEmail_tooLong() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        String emailSuffix = "@email.com";
        response.setEmail(buildStringToLength(255 - emailSuffix.length()) + emailSuffix);

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (email format invalid)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Email property to be the cause of the constraint violation")
            .isEqualTo("email");
    }

    @Test
    public void test_setEmail_invalid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setEmail(buildStringToLength(250));

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (email format invalid)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Email property to be the cause of the constraint violation")
            .isEqualTo("email");
    }

    @Test
    public void test_setResidency_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setResidency(Boolean.FALSE);

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getResidency()).isFalse();
    }

    @Test
    public void test_setResidency_default() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getResidency()).isNull();
    }

    @Test
    public void test_setMentalHealthAct_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setMentalHealthAct(Boolean.TRUE);

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getMentalHealthAct()).isTrue();
    }

    @Test
    public void test_setMentalHealthAct_default() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getMentalHealthAct()).isNull();
    }

    @Test
    public void test_setMentalHealthCapacity_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setMentalHealthCapacity(Boolean.TRUE);

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getMentalHealthCapacity()).isTrue();
    }

    @Test
    public void test_setMentalHealthCapacity_default() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getMentalHealthCapacity()).isNull();
    }

    @Test
    public void test_setBail_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setBail(Boolean.TRUE);

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getBail()).isTrue();
    }

    @Test
    public void test_setBail_default() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getBail()).isNull();
    }

    @Test
    public void test_setConvictions_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setConvictions(Boolean.TRUE);

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getConvictions()).isTrue();
    }

    @Test
    public void test_setConvictions_default() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getConvictions()).isNull();
    }

    @Test
    public void test_setReasonableAdjustments_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setReasonableAdjustmentsArrangements(buildStringToLength(1000));

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_setReasonableAdjustmentsArrangements_tooLong() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setReasonableAdjustmentsArrangements(buildStringToLength(2001));

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid Reasonable Adjustments Arrangements)")
            .isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Reasonable Adjustment Arrangements property to be the cause of the constraint violation")
            .isEqualTo("reasonableAdjustmentsArrangements");
    }

    @Test
    public void test_setReasonableAdjustments_containsPipe() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setReasonableAdjustmentsArrangements("Test|String");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid Special Needs Arrangements)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Special Needs Arrangements property to be the cause of the constraint violation")
            .isEqualTo("reasonableAdjustmentsArrangements");
    }

    @Test
    public void test_setProcessingComplete_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setProcessingComplete(Boolean.TRUE);

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getProcessingComplete()).isTrue();
    }

    @Test
    public void test_setProcessingComplete_default() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getProcessingComplete()).isFalse();
    }

    @Test
    public void test_setRelationship_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setRelationship(buildStringToLength(50));

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_setRelationship_tooLong() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setRelationship(buildStringToLength(51));

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid Relationship)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Relationship property to be the cause of the constraint violation")
            .isEqualTo("relationship");
    }

    @Test
    public void test_setRelationship_containsPipe() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setRelationship("Test|String");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid Relationship)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Relationship property to be the cause of the constraint violation")
            .isEqualTo("relationship");
    }

    @Test
    public void test_setThirdPartyReason_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setThirdPartyReason(buildStringToLength(1000));

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void test_setThirdPartyReason_tooLong() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setThirdPartyReason(buildStringToLength(1001));

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid Third Party Reason)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Third Party Reason property to be the cause of the constraint violation")
            .isEqualTo("thirdPartyReason");
    }

    @Test
    public void test_setThirdPartyReason_containsPipe() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setThirdPartyReason("Test|String");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("Validation violation expected (invalid Third Party Reason)").isNotEmpty();

        ConstraintViolation<PaperResponse> constraintViolation = violations.stream().findFirst().get();
        assertThat(constraintViolation.getPropertyPath().toString())
            .as("Expect the Third Party Reason property to be the cause of the constraint violation")
            .isEqualTo("thirdPartyReason");
    }

    @Test
    public void test_setWelsh_valid() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");
        response.setWelsh(Boolean.TRUE);

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getWelsh()).isTrue();
    }

    @Test
    public void test_setWelsh_default() {
        PaperResponse response = new PaperResponse();
        response.setAddressLine1("Test");
        response.setDateReceived(LocalDateTime.now());
        response.setJurorNumber("123456789");

        Set<ConstraintViolation<PaperResponse>> violations = validator.validate(response);
        assertThat(violations).as("No validation violations expected").isEmpty();

        assertThat(response.getWelsh()).isFalse();
    }

}