package uk.gov.hmcts.juror.api.bureau.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.jsonwebtoken.lang.Assert;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.bureau.service.ResponseUpdateService;
import uk.gov.hmcts.juror.api.validation.LocalDateOfBirth;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.io.Serializable;
import java.time.LocalDate;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.EMAIL_ADDRESS_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.PHONE_PRIMARY_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.PHONE_SECONDARY_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.POSTCODE_REGEX;

/**
 * Controller endpoints for updating a juror response.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/bureau/juror/{jurorId}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Bureau Response Edit API", description = "Bureau operations relating to editing a juror response.")
public class ResponseUpdateController {
    private final ResponseUpdateService responseUpdateService;

    @Autowired
    public ResponseUpdateController(final ResponseUpdateService responseUpdateService) {
        Assert.notNull(responseUpdateService, "ResponseUpdateService cannot be null");
        this.responseUpdateService = responseUpdateService;
    }

    /**
     * Validate a juror number path variable matches {@link ValidationConstants#JUROR_NUMBER}.
     *
     * @param jurorNumber Path variable supplied juror number
     */
    static void assertJurorNumberPathVariable(final String jurorNumber) {
        if (!jurorNumber.matches(ValidationConstants.JUROR_NUMBER)) {
            log.warn("Juror number {} in path invalid", jurorNumber);
            throw new ValidationException("Juror number must be exactly 9 digits");
        }
        log.trace("Juror number valid");
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "Request body for updating the notes field of a juror response")
    public static class JurorNoteDto implements Serializable {

        @Schema(description = "Juror response notes", example = "Some free form text", requiredMode =
            Schema.RequiredMode.REQUIRED)
        @Length(max = 2000)
        @NotEmpty
        private String notes;

        @Schema(description = "Juror response Optimistic locking version", requiredMode = Schema.RequiredMode.REQUIRED)
        @Pattern(regexp = ValidationConstants.MD5_HASHCODE)
        private String version;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "Request body for adding a phone log to a juror response")
    public static class JurorPhoneLogDto implements Serializable {

        @Schema(description = "Phone log notes", example = "Call related to something", requiredMode =
            Schema.RequiredMode.REQUIRED)
        @Length(max = 2000)
        private String notes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    private static class AbstractJurorDetailsDto implements Serializable {

        @NotNull
        @Schema(description = "Optimistic locking version", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer version;

        @NotEmpty
        @Length(max = 2000)
        @Schema(description = "Notes regarding update", requiredMode = Schema.RequiredMode.REQUIRED)
        private String notes;

        @Length(max = 10)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Juror title")
        private String title;

        @Length(max = 20)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Jurors first name")
        private String firstName;

        @Length(max = 25)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Jurors last name")
        private String lastName;

        @NotEmpty
        @Length(max = 35)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Juror address line 1")
        @JsonProperty("address1")
        private String address;

        @Length(max = 35)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Juror address line 2")
        private String address2;

        @Length(max = 35)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Juror address line 3")
        private String address3;

        @Length(max = 35)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Juror address line 4")
        private String address4;

        @Length(max = 35)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Juror address line 5")
        private String address5;

        @Pattern.List({
            @Pattern(regexp = NO_PIPES_REGEX),
            @Pattern(regexp = POSTCODE_REGEX)
        })
        @Length(max = 10)
        @Schema(description = "Juror postcode")
        private String postcode;

        @LocalDateOfBirth
        @Past
        @Schema(description = "Juror date of birth")
        private LocalDate dob;

        @Pattern(regexp = PHONE_PRIMARY_REGEX)
        @Schema(description = "Juror main phone number")
        private String mainPhone;

        @Pattern(regexp = PHONE_SECONDARY_REGEX)
        @Schema(description = "Juror secondary phone number")
        private String altPhone;

        @Pattern(regexp = EMAIL_ADDRESS_REGEX)
        @Length(max = 254)
        @Schema(description = "Juror email address")
        private String emailAddress;
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "Request body for editing the juror details section of a first person juror response")
    public static final class FirstPersonJurorDetailsDto extends AbstractJurorDetailsDto {
        @Builder
        private FirstPersonJurorDetailsDto(Integer version, String notes, String title, String firstName,
                                           String lastName, String address, String address2, String address3,
                                           String address4, String address5, String postcode, LocalDate dob,
                                           String mainPhone, String altPhone, String emailAddress) {
            super(
                version,
                notes,
                title,
                firstName,
                lastName,
                address,
                address2,
                address3,
                address4,
                address5,
                postcode,
                dob,
                mainPhone,
                altPhone,
                emailAddress
            );
        }
    }

    @Data
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "Request body for editing the juror details section of a third party juror response")
    public static final class ThirdPartyJurorDetailsDto extends AbstractJurorDetailsDto {

        @Schema(description = "Flag for using Jurors phone as contact")
        @JsonProperty("useJurorPhoneDetails")
        private Boolean useJurorPhone;

        @Schema(description = "Flag for using Jurors email as contact")
        @JsonProperty("useJurorEmailDetails")
        private Boolean useJurorEmail;

        @Length(max = 20)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Third party respondents first name")
        private String thirdPartyFirstName;

        @Length(max = 20)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Third party respondents last name")
        private String thirdPartyLastName;

        @NotEmpty
        @Length(max = 50)
        @Schema(description = "Third party respondents relationship to Juror")
        private String relationship;

        @NotEmpty
        @Length(max = 1000)
        @Schema(description = "Reason response is a third party response")
        private String thirdPartyReason;

        @Pattern(regexp = NO_PIPES_REGEX)
        @Length(max = 1000)
        @Schema(description = "Details of why other was selected as the thirdPartyReason")
        private String thirdPartyOtherReason;

        @Pattern(regexp = PHONE_PRIMARY_REGEX)
        @Schema(description = "Third party respondents main phone number")
        private String thirdPartyMainPhone;

        @Pattern(regexp = PHONE_SECONDARY_REGEX)
        @Schema(description = "Third party respondents secondary phone number")
        private String thirdPartyAltPhone;

        @Length(max = 254)
        @Pattern(regexp = EMAIL_ADDRESS_REGEX)
        @Schema(description = "Third party respondents email address")
        private String thirdPartyEmail;

        @Builder
        private ThirdPartyJurorDetailsDto(Integer version, String notes, String title, String firstName,
                                          String lastName, String address, String address2, String address3,
                                          String address4, String address5, String postcode, LocalDate dob,
                                          String mainPhone, String altPhone, String emailAddress,
                                          Boolean useJurorPhone, Boolean useJurorEmail, String thirdPartyFirstName,
                                          String thirdPartyLastName, String relationship, String thirdPartyReason,
                                          String thirdPartyOtherReason, String thirdPartyMainPhone,
                                          String thirdPartyAltPhone, String thirdPartyEmail) {
            super(
                version,
                notes,
                title,
                firstName,
                lastName,
                address,
                address2,
                address3,
                address4,
                address5,
                postcode,
                dob,
                mainPhone,
                altPhone,
                emailAddress
            );
            this.useJurorPhone = useJurorPhone;
            this.useJurorEmail = useJurorEmail;
            this.thirdPartyFirstName = thirdPartyFirstName;
            this.thirdPartyLastName = thirdPartyLastName;
            this.relationship = relationship;
            this.thirdPartyReason = thirdPartyReason;
            this.thirdPartyOtherReason = thirdPartyOtherReason;
            this.thirdPartyMainPhone = thirdPartyMainPhone;
            this.thirdPartyAltPhone = thirdPartyAltPhone;
            this.thirdPartyEmail = thirdPartyEmail;
        }
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Builder
    public static class DeferralExcusalDto implements Serializable {

        @NotNull
        @Schema(description = "Optimistic locking version", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer version;

        @NotEmpty
        @Length(max = 2000)
        @Schema(description = "Notes regarding the change to details", requiredMode = Schema.RequiredMode.REQUIRED)
        private String notes;

        /**
         * Is this an excusal = true, or deferral = false.
         */
        @NotNull
        @Schema(description = "Flag whether this is an excusal (true) or deferral (false)", requiredMode =
            Schema.RequiredMode.REQUIRED)
        private DeferralExcusalUpdateType excusal;

        @Length(max = 1000)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "List of nominated deferral dates")
        private String deferralDates;

        /**
         * Excusal / deferral reason string.
         */
        @Length(max = 1000)
        @Pattern(regexp = NO_PIPES_REGEX)
        @Schema(description = "Reason for excusal/deferral")
        private String reason;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Builder
    public static class ReasonableAdjustmentsDto implements Serializable {

        @NotNull
        @Schema(description = "Optimistic locking version", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer version;

        @NotEmpty
        @Length(max = 2000)
        @Schema(description = "Notes regarding the change to details", requiredMode = Schema.RequiredMode.REQUIRED)
        private String notes;

        @Length(max = 1000)
        @Schema(description = "Details about Jurors limited mobility")
        private String limitedMobility;

        @Length(max = 1000)
        @Schema(description = "Details about Jurors hearing impairment")
        private String hearingImpairment;

        @Length(max = 1000)
        @Schema(description = "Details about Jurors diabetes")
        private String diabetes;

        @Length(max = 1000)
        @Schema(description = "Details about Jurors sight impairment")
        private String sightImpairment;

        @Length(max = 1000)
        @Schema(description = "Details about Jurors learning disability")
        private String learningDisability;

        @Length(max = 1000)
        @Schema(description = "Details about any other special needs")
        private String other;

        /**
         * Maps to {@link uk.gov.hmcts.juror.api.juror.domain.JurorResponse#specialNeedsArrangements}.
         */
        @Length(max = 1000)
        @Schema(description = "Details about required special arrangements")
        private String specialArrangements;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Builder
    public static class CjsEmploymentDetailsDto implements Serializable {
        @NotNull
        @Schema(description = "Optimistic locking version", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer version;

        @NotEmpty
        @Length(max = 2000)
        @Schema(description = "Notes regarding the change to details", requiredMode = Schema.RequiredMode.REQUIRED)
        private String notes;

        @NotEmpty
        @Schema(description = "Juror number", requiredMode = Schema.RequiredMode.REQUIRED)
        private String jurorNumber;

        @Length(max = 1000)
        @Schema(description = "Details on police force employment")
        private String policeForceDetails;

        @Length(max = 1000)
        @Schema(description = "Details on HM Prison Service employment")
        private String prisonServiceDetails;

        @Schema(description = "Whether juror has had NCA employment")
        private Boolean ncaEmployment;

        @Schema(description = "Whether juror has had Judiciary employment")
        private Boolean judiciaryEmployment;

        @Schema(description = "Whether juror has had HMCTS employment")
        private Boolean hmctsEmployment;

        @Length(max = 1000)
        @Schema(description = "Details on other CJS employment")
        private String otherDetails;

    }

    public enum DeferralExcusalUpdateType {
        CONFIRMATION,
        DEFERRAL,
        EXCUSAL
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Schema(description = "Request body for editing the juror eligibility section of a response")
    public static class JurorEligibilityDto {
        @NotNull
        private Integer version;

        @NotEmpty
        @Length(max = 2000)
        private String notes;

        @Schema(description = "Whether the Juror has lived in the UK for the required period", requiredMode =
            Schema.RequiredMode.REQUIRED)
        @NotNull
        private boolean residency;

        @Schema(description = "Textual description of the residency criteria")
        private String residencyDetails;

        @Schema(description = "Whether the Juror has been sectioned under the Mental Health Act", requiredMode =
            Schema.RequiredMode.REQUIRED)
        @NotNull
        private boolean mentalHealthAct;

        @Schema(description = "Textual description of the mental health act criteria")
        private String mentalHealthActDetails;

        @Schema(description = "Whether the Juror is on bail", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        private boolean bail;

        @Schema(description = "Textual description of the bail criteria")
        private String bailDetails;

        @Schema(description = "Whether the Juror has a criminal conviction", requiredMode =
            Schema.RequiredMode.REQUIRED)
        @NotNull
        private boolean convictions;

        @Schema(description = "Textual description of the convictions criteria")
        private String convictionsDetails;
    }

}
