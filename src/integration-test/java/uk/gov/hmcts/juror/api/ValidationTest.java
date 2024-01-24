package uk.gov.hmcts.juror.api;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.EMAIL_ADDRESS_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.PHONE_PRIMARY_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.PHONE_SECONDARY_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.THIRD_PARTY_PHONE_PRIMARY_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.THIRD_PARTY_PHONE_SECONDARY_REGEX;

/**
 * Testing validations.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class ValidationTest {

    @Autowired
    private Validator validator;

    @Test
    public void testEmailValidation_validEmails() throws Exception {
        ArrayList<String> emailList = new ArrayList<>();
        emailList.add("address@domain.com");
        emailList.add("firstname.lastname@domain.com");
        emailList.add("address@subdomain.domain.com");
        emailList.add("firstname+lastname@domain.com");
        emailList.add("1234567890@domain.com");
        emailList.add("address@domain-one.com");
        emailList.add("_______@domain.com");
        emailList.add("address@domain.co.jp");
        emailList.add("firstname-lastname@domain.com");
        emailList.add("Firstname.Lastname@Domain.com");
        emailList.add("pipes|are|allowed@domain.com");

        for (String s : emailList) {
            new InternetAddress(s);
        }

        EmailValidationTestDto dto = new EmailValidationTestDto("");
        Set<ConstraintViolation<EmailValidationTestDto>> validationErrors;
        for (String email : emailList) {
            dto.setEmail(email);
            validationErrors = validator.validate(dto);
            assertThat(validationErrors).hasSize(0);
        }
    }

    @Test
    public void testEmailValidation_invalidEmails() throws Exception {
        ArrayList<String> emailList = new ArrayList<>();
        emailList.add("@domain.com");
        emailList.add("#%^|*£$@#.com");
        emailList.add("address@address@domain.com");

        SoftAssertions softly = new SoftAssertions();

        for (String s : emailList) {
            try {
                new InternetAddress(s);
                softly.assertThat(s).as("The validation of address " + s + " should not have reached here").isNull();
            } catch (AddressException e) {
                // email address successfully caught as invalid
            }
        }
        softly.assertAll();

        // The following addresses are technically valid according to InternetAddress class,
        // but we want our regex to recognise them as invalid
        emailList.add("Address Domain <address@domain.com>");
        emailList.add("address.domain.com");
        emailList.add("address");
        emailList.add("address@domain");

        EmailValidationTestDto dto = new EmailValidationTestDto("");
        Set<ConstraintViolation<EmailValidationTestDto>> validationErrors;
        for (String email : emailList) {
            dto.setEmail(email);
            validationErrors = validator.validate(dto);
            assertThat(validationErrors).hasSize(1);
        }
    }

    @Test
    public void phoneValidation_validNumbers() throws Exception {
        ArrayList<String> phoneNumberList = new ArrayList<>();
        phoneNumberList.add("012345678");
        phoneNumberList.add("123456789");
        phoneNumberList.add("1234567890");
        phoneNumberList.add("12345678901");
        phoneNumberList.add("123456789012");
        phoneNumberList.add("1234567890123");
        phoneNumberList.add("12345678901234");
        phoneNumberList.add("123456789012345");
        phoneNumberList.add("123 567 90123 5");

        PhoneValidationTestDto dto = new PhoneValidationTestDto();
        Set<ConstraintViolation<PhoneValidationTestDto>> validationErrors;
        for (String phoneNumber : phoneNumberList) {
            dto.setMainPhone(phoneNumber);
            dto.setOtherPhone(phoneNumber);
            validationErrors = validator.validate(dto);
            assertThat(validationErrors).hasSize(0);
        }
    }

    @Test
    public void phoneValidation_invalidNumbers() throws Exception {
        ArrayList<String> phoneNumberList = new ArrayList<>();
        phoneNumberList.add("test");
        phoneNumberList.add("1234567");
        phoneNumberList.add("1234567890123456");
        phoneNumberList.add("1234567890a");
        phoneNumberList.add("1234567890-");

        PhoneValidationTestDto dto = new PhoneValidationTestDto();
        Set<ConstraintViolation<PhoneValidationTestDto>> validationErrors;
        for (String phoneNumber : phoneNumberList) {
            dto.setMainPhone(phoneNumber);
            validationErrors = validator.validate(dto);
            assertThat(validationErrors).hasSize(1);
        }

        // numbers that should fail the secondary phone validation
        phoneNumberList = new ArrayList<>();
        phoneNumberList.add("1234567");
        phoneNumberList.add("1234567890123456");

        dto = new PhoneValidationTestDto();
        for (String phoneNumber : phoneNumberList) {
            dto.setOtherPhone(phoneNumber);
            validationErrors = validator.validate(dto);
            assertThat(validationErrors).hasSize(1);
        }
    }

    @Test
    public void testThirdPartyPhoneValidation_validNumbers() throws Exception {
        ArrayList<String> phoneNumberList = new ArrayList<>();
        phoneNumberList.add("");   // should be allowed when using Jurors phone number as contact
        phoneNumberList.add(null); // should be allowed when using Jurors phone number as contact
        phoneNumberList.add("012345678");
        phoneNumberList.add("123456789");
        phoneNumberList.add("1234567890");
        phoneNumberList.add("12345678901");
        phoneNumberList.add("123456789012345");
        phoneNumberList.add("123 567 90123 5");

        ThirdPartyPhoneValidationTestDto dto = new ThirdPartyPhoneValidationTestDto();
        Set<ConstraintViolation<ThirdPartyPhoneValidationTestDto>> validationErrors;
        for (String phoneNumber : phoneNumberList) {
            dto.setMainPhone(phoneNumber);
            dto.setOtherPhone(phoneNumber);
            validationErrors = validator.validate(dto);
            assertThat(validationErrors).hasSize(0);
        }
    }

    @Test
    public void testThirdPartyPhoneValidation_invalidNumbers() throws Exception {
        ArrayList<String> phoneNumberList = new ArrayList<>();
        phoneNumberList.add("test");
        phoneNumberList.add("1234567");
        phoneNumberList.add("1234567890123456");
        phoneNumberList.add("1234567890a");
        phoneNumberList.add("1234567890-");

        ThirdPartyPhoneValidationTestDto dto = new ThirdPartyPhoneValidationTestDto();
        Set<ConstraintViolation<ThirdPartyPhoneValidationTestDto>> validationErrors;
        for (String phoneNumber : phoneNumberList) {
            dto.setMainPhone(phoneNumber);
            validationErrors = validator.validate(dto);
            assertThat(validationErrors).hasSize(1);
        }

        // numbers that should fail the secondary phone validation
        phoneNumberList = new ArrayList<>();
        phoneNumberList.add("1234567");
        phoneNumberList.add("1234567890123456");

        dto = new ThirdPartyPhoneValidationTestDto();
        for (String phoneNumber : phoneNumberList) {
            dto.setOtherPhone(phoneNumber);
            validationErrors = validator.validate(dto);
            assertThat(validationErrors).hasSize(1);
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmailValidationTestDto implements Serializable {

        @Pattern(regexp = EMAIL_ADDRESS_REGEX)
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhoneValidationTestDto implements Serializable {

        @Pattern(regexp = PHONE_PRIMARY_REGEX)
        private String mainPhone;

        @Pattern(regexp = PHONE_SECONDARY_REGEX)
        private String otherPhone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ThirdPartyPhoneValidationTestDto implements Serializable {

        @Pattern(regexp = THIRD_PARTY_PHONE_PRIMARY_REGEX)
        private String mainPhone;

        @Pattern(regexp = THIRD_PARTY_PHONE_SECONDARY_REGEX)
        private String otherPhone;
    }
}
