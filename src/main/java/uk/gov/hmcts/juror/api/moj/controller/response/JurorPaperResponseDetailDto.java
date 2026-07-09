package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.controller.response.jurorresponse.IJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.authentication.UserDetailsDto;
import uk.gov.hmcts.juror.api.validation.LocalDateOfBirth;
import uk.gov.hmcts.juror.api.validation.ValidationConstants;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.EMAIL_ADDRESS_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.PHONE_NO_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.POSTCODE_REGEX;

/**
 * Request DTO for Juror Paper Responses (as a business process).
 */
@SuppressWarnings("PMD.TooManyFields")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Juror paper response details submitted by an Officer.")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class JurorPaperResponseDetailDto implements IJurorResponse {

    @Pattern(regexp = JUROR_NUMBER)
    @Schema(description = "Juror number", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String jurorNumber;

    @NotEmpty
    @Schema(name = "Pool number", description = "Pool Request number")
    private String poolNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotEmpty
    @Schema(description = "Service start date")
    private LocalDate serviceStartDate;

    @NotEmpty
    @Schema(description = "Court name")
    private String courtName;

    @NotEmpty
    @Schema(name = "Welsh Court", description = "Flag indicating whether the court location is in Wales or not")
    private boolean welshCourt;

    @NotEmpty
    @Schema(name = "Juror Status", description = "Jurors status")
    private String jurorStatus;

    @Length(max = 10)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror title")
    private String title;

    @NotEmpty
    @Pattern(regexp = NO_PIPES_REGEX)
    @Length(max = 20)
    @Schema(description = "Juror first name")
    private String firstName;


    @NotEmpty
    @Length(max = 25)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror last name")
    private String lastName;

    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 1")
    private String addressLineOne;

    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 2")
    private String addressLineTwo;

    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 3")
    private String addressLineThree;

    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 4")
    private String addressTown;

    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 5")
    private String addressCounty;

    @NotEmpty
    @Length(max = 8)
    @Pattern(regexp = POSTCODE_REGEX)
    @Schema(description = "Juror address post code")
    private String addressPostcode;

    @Length(max = 10)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror title in current Juror record")
    private String existingTitle;

    @NotEmpty
    @Pattern(regexp = NO_PIPES_REGEX)
    @Length(max = 20)
    @Schema(description = "Juror first name in current Juror record")
    private String existingFirstName;

    @NotEmpty
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror last name in current Juror record")
    private String existingLastName;

    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 1 in current Juror record")
    private String existingAddressLineOne;

    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 2 in current Juror record")
    private String existingAddressLineTwo;

    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 3 in current Juror record")
    private String existingAddressLineThree;

    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 4 in current Juror record")
    private String existingAddressTown;

    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 5 in current Juror record")
    private String existingAddressCounty;

    @NotEmpty
    @Length(max = 8)
    @Pattern(regexp = POSTCODE_REGEX)
    @Schema(description = "Juror address post code in current Juror record")
    private String existingAddressPostcode;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @LocalDateOfBirth
    @Schema(description = "Juror date of birth")
    private LocalDate dateOfBirth;

    @Pattern(regexp = PHONE_NO_REGEX)
    @Schema(description = "Juror primary telephone number")
    private String primaryPhone;

    @Pattern(regexp = PHONE_NO_REGEX)
    @Schema(description = "Juror secondary telephone number")
    private String secondaryPhone;

    @Length(max = 254)
    @Pattern(regexp = EMAIL_ADDRESS_REGEX)
    @Schema(description = "Juror email address")
    private String emailAddress;

    @JsonProperty("cjsEmployment")
    @Schema(description = "Array of any Criminal Justice System employment of the Juror")
    private List<CjsEmployment> cjsEmployment;

    @JsonProperty("specialNeeds")
    @Schema(description = "Array of any reasonable adjustment requirements of the Juror at the court location")
    private List<ReasonableAdjustment> reasonableAdjustments;

    @JsonProperty("deferral")
    @Schema(description = "Flag indicating if seeking a deferral of jury service")
    private Boolean deferral;

    @JsonProperty("excusal")
    @Schema(description = "Flag indicating if seeking an excusal from jury service")
    private Boolean excusal;

    @JsonProperty("excusalReason")
    @Schema(description = "Key for the juror excusal reason")
    private String excusalReason;

    @JsonProperty("eligibility")
    @Schema(description = "Jury service eligibility details in a juror response")
    private Eligibility eligibility;

    @JsonProperty("signed")
    @Schema(description = "Has the paper response been signed?")
    private Boolean signed;

    @JsonProperty("thirdParty")
    @Schema(description = "Details of person who replies on behalf of the Juror")
    private ThirdParty thirdParty;

    @JsonProperty("welsh")
    @Schema(description = "Is this response Welsh language?")
    private Boolean welsh = Boolean.FALSE;

    @JsonProperty("dateReceived")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Date the Juror Paper Response was received")
    private LocalDate dateReceived;

    @JsonProperty("processingStatus")
    @Schema(description = "Juror Paper Response processing status")
    private String processingStatus;

    @JsonProperty("opticReference")
    @Schema(description = "Juror Optic Reference Number")
    private String opticReference;

    @JsonProperty("assignedStaffMember")
    @Schema(description = "Assigned staff member")
    private UserDetailsDto assignedStaffMember;

    @JsonProperty("contactLog")
    @Schema(description = "Juror contact log")
    private List<ContactLogListDto.ContactLogDataDto> contactLog;

    @JsonProperty("notes")
    @Schema(description = "Juror notes")
    private String notes;

    @JsonProperty("current_owner")
    @Schema(name = "Current Owner", description = "Current owner (3 digit code) of the juror record")
    private String currentOwner;

    @JsonProperty("completed_at")
    @JsonFormat(pattern = ValidationConstants.DATETIME_FORMAT)
    @Schema(name = "Completed At", description = "Date and time the juror paper response was completed")
    private LocalDateTime completedAt;

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Schema(description = "Jury service eligibility details in a juror paper response")
    public static class Eligibility {

        @JsonProperty("livedConsecutive")
        @Schema(description = "Whether the Juror has lived in the UK for the required period")
        private Boolean livedConsecutive;

        @JsonProperty("mentalHealthAct")
        @Schema(description = "Whether the Juror has been sectioned under the Mental Health Act")
        private Boolean mentalHealthAct;

        @JsonProperty("mentalHealthCapacity")
        @Schema(description = "Whether the Juror has the Mental Health Capacity")
        private Boolean mentalHealthCapacity;

        @JsonProperty("onBail")
        @Schema(description = "Whether the Juror is on bail")
        private Boolean onBail;

        @JsonProperty("convicted")
        @Schema(description = "Whether the Juror has a criminal conviction")
        private Boolean convicted;

        @JsonProperty("livedConsecutiveDetails")
        @Schema(description = "Textual description of the Juror's lived consecutive details")
        private String livedConsecutiveDetails;

        @JsonProperty("mentalHealthActDetails")
        @Schema(description = "Textual description of the Juror's mental health act details")
        private String mentalHealthActDetails;

        @JsonProperty("onBailDetails")
        @Schema(description = "Textual description of the Juror's on bail details")
        private String onBailDetails;

        @JsonProperty("convictedDetails")
        @Schema(description = "Textual description of the Juror's criminal conviction details")
        private String convictedDetails;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Schema(description = "Special need object. Single requirement category the Juror requests assistance for. "
        + "("
        + "V=Visual Impairment"
        + "W=Wheel Chair Access"
        + "H=Hearing Impairment"
        + "M=Multiple"
        + "O=Other"
        + ")"
    )
    public static class ReasonableAdjustment {
        @JsonProperty("assistanceType")
        @Schema(description = "Assistance type", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues =
            "null,V,W,H,M,O")
        @NotNull
        private String assistanceType;

        @JsonProperty("assistanceTypeDetails")
        @Schema(description = "Textual description of the support requested by the Juror")
        private String assistanceTypeDetails;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Schema(description = "Criminal Justice System employment object for the Juror")
    public static class CjsEmployment {
        @JsonProperty("cjsEmployer")
        @Schema(description = "CJS employer name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Police")
        @NotNull
        private String cjsEmployer;

        @JsonProperty("cjsEmployerDetails")
        @Schema(description = "Textual description of the Juror's employment",
            example = "Worked as a mechanic maintaining police vehicles")
        private String cjsEmployerDetails;
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Schema(description = "Response submitted by a third party")
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ThirdParty {

        @Schema(description = "Third party firstname")
        private String thirdPartyFName;

        @Schema(description = "Third party lastname")
        private String thirdPartyLName;

        @Schema(description = "Third party main phone number")
        @Pattern(regexp = PHONE_NO_REGEX)
        private String mainPhone;

        @Schema(description = "Third party alternative phone number")
        private String otherPhone;

        @Schema(description = "Third party email address")
        @Pattern(regexp = EMAIL_ADDRESS_REGEX)
        private String emailAddress;


        @Schema(description = "Third party relationship to the juror")
        private String relationship;


        @Schema(description = "Third party reason")
        private String thirdPartyReason;


        @Schema(description = "Third party other reason")
        private String thirdPartyOtherReason;

        @Schema(description = "Whether the juror's email details should be used (false = use third party details)")
        @Builder.Default
        private Boolean useJurorEmailDetails = Boolean.TRUE;

        @Schema(description = "Whether the juror's phone details should be used (false = use third party details)")
        @Builder.Default
        private Boolean useJurorPhoneDetails = Boolean.TRUE;


    }

}
