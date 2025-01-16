package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.validation.LocalDateOfBirth;

import java.time.LocalDate;
import java.util.List;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.EMAIL_ADDRESS_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.PHONE_NO_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.POSTCODE_REGEX;

/**
 * Request DTO for Juror Paper Responses (as a business process).
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Juror paper response information submitted by an Officer.")
public class JurorPaperResponseDto {

    @JsonProperty("jurorNumber")
    @Pattern(regexp = JUROR_NUMBER)
    @Schema(description = "Juror number", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String jurorNumber;

    @JsonProperty("title")
    @Length(max = 10)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror title")
    private String title;

    @JsonProperty("firstName")
    @NotEmpty
    @Pattern(regexp = NO_PIPES_REGEX)
    @Length(max = 20)
    @Schema(description = "Juror first name")
    private String firstName;

    @JsonProperty("lastName")
    @NotEmpty
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror last name")
    private String lastName;

    @JsonProperty("addressLineOne")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 1")
    private String addressLineOne;

    @JsonProperty("addressLineTwo")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 2")
    private String addressLineTwo;

    @JsonProperty("addressLineThree")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 3")
    private String addressLineThree;

    @JsonProperty("addressTown")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 4")
    private String addressTown;

    @JsonProperty("addressCounty")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 5")
    private String addressCounty;

    @JsonProperty("addressPostcode")
    @NotEmpty
    @Length(max = 8)
    @Pattern(regexp = POSTCODE_REGEX)
    @Schema(description = "Juror address post code")
    private String addressPostcode;

    @JsonProperty("dateOfBirth")
    @LocalDateOfBirth
    @Schema(description = "Juror date of birth")
    private LocalDate dateOfBirth;

    @JsonProperty("primaryPhone")
    @Pattern(regexp = PHONE_NO_REGEX)
    @Schema(description = "Juror primary telephone number")
    private String primaryPhone;

    @JsonProperty("secondaryPhone")
    @Pattern(regexp = PHONE_NO_REGEX)
    @Schema(description = "Juror secondary telephone number")
    private String secondaryPhone;

    @JsonProperty("emailAddress")
    @Length(max = 254)
    @Pattern(regexp = EMAIL_ADDRESS_REGEX)
    @Schema(description = "Juror email address")
    private String emailAddress;

    @JsonProperty("cjsEmployment")
    @Schema(description = "Array of any Criminal Justice System employment of the Juror")
    private List<CjsEmployment> cjsEmployment;

    @JsonProperty("specialNeeds")
    @Schema(description = "Array of any special requirements of the Juror at the court location")
    private List<ReasonableAdjustment> reasonableAdjustments;

    @JsonProperty("canServeOnSummonsDate")
    @Schema(description = "Flag indicating if the juror is available to attend jury service on the date they have "
        + "been summoned for")
    private Boolean canServeOnSummonsDate;

    @JsonProperty("deferral")
    @Schema(description = "Flag indicating if seeking a deferral of jury service")
    private Boolean deferral;

    @JsonProperty("excusal")
    @Schema(description = "Flag indicating if seeking an excusal from jury service")
    private Boolean excusal;

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

    @Builder
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
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Schema(description = "Special need object. Single requirement category the Juror requests assistance for. "
        + "("
        + "V=Visual Impairment"
        + "W=Wheel Chair Access"
        + "H=Hearing Loss"
        + "M=Multiple"
        + "O=Other"
        + "I=Diabetic"
        + "D=Diet"
        + "P=Pregnancy/Breastfeeding"
        + "R=Reading"
        + "L=Limited Mobility"
        + "C=Caring responsibilities"
        + "U=Medication"
        + "J=CJS Employee"
        + "E=Epilepsy"
        + "A=Religious Reasons"
        + "T=Travelling Difficulties"
        + ")"
    )

    @EqualsAndHashCode
    public static class ReasonableAdjustment {
        @JsonProperty("assistanceType")
        @Schema(description = "Assistance type", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "V,W,"
            + "H,M,O,I,D,P,R,L,C,U,J,E,A,T")
        @NotNull
        private String assistanceType;

        @JsonProperty("assistanceTypeDetails")
        @Schema(description = "Textual description of the support requested by the Juror")
        private String assistanceTypeDetails;
    }

    @Builder
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
    public static class ThirdParty {

        @JsonProperty("thirdPartyFName")
        @Schema(description = "Third party firstname")
        private String thirdPartyFName;

        @JsonProperty("thirdPartyLName")
        @Schema(description = "Third party lastname")
        private String thirdPartyLName;

        @JsonProperty("thirdPartyPhone")
        @Schema(description = "Third party main phone number")
        @Pattern(regexp = PHONE_NO_REGEX)
        private String mainPhone;

        @JsonProperty("otherPhone")
        @Schema(description = "Third party alternative phone number")
        private String otherPhone;

        @JsonProperty("thirdPartyEmail")
        @Schema(description = "Third party email address")
        @Pattern(regexp = EMAIL_ADDRESS_REGEX)
        private String emailAddress;

        @JsonProperty("relationship")
        @Schema(description = "Third party relationship to the juror")
        private String relationship;

        @JsonProperty("thirdPartyReason")
        @Schema(description = "Third party reason")
        private String thirdPartyReason;

        @JsonProperty("thirdPartyOtherReason")
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
