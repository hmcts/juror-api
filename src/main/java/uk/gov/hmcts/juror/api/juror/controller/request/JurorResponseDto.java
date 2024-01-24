package uk.gov.hmcts.juror.api.juror.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.ScriptAssert;
import uk.gov.hmcts.juror.api.validation.DateOfBirth;
import uk.gov.hmcts.juror.api.validation.ThirdPartyRequireAtLeastOneOf;

import java.util.Date;
import java.util.List;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.EMAIL_ADDRESS_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.PHONE_PRIMARY_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.PHONE_SECONDARY_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.POSTCODE_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.THIRD_PARTY_PHONE_PRIMARY_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.THIRD_PARTY_PHONE_SECONDARY_REGEX;

/**
 * Request DTO for Juror Responses (as a business process).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "realBuilder")
@Schema(description = "Juror response information from a Juror Response updating exiting information. Parameters "
    + "should be the correct values, not just changes.")
public class JurorResponseDto {
    /**
     * Required arguments builder for common flows.
     */
    public static JurorResponseDtoBuilder builder(String jurorNumber, String firstName, String lastName,
                                                  String addressLineOne, String addressLineTwo, String addressLineThree,
                                                  String addressPostcode, Date dateOfBirth, String primaryPhone,
                                                  String emailAddress, Qualify qualify, Integer version) {
        return realBuilder()
            .jurorNumber(jurorNumber)
            .firstName(firstName)
            .lastName(lastName)
            .addressLineOne(addressLineOne)
            .addressLineTwo(addressLineTwo)
            .addressLineThree(addressLineThree)
            .addressPostcode(addressPostcode)
            .dateOfBirth(dateOfBirth)
            .primaryPhone(primaryPhone)
            .emailAddress(emailAddress)
            .qualify(qualify)
            .version(version)
            ;
    }

    /**
     * Required arguments builder for Third Party Deceased flow.
     */
    public static JurorResponseDtoBuilder builderThirdPartyDeceased(String jurorNumber, ThirdParty thirdParty) {
        return realBuilder()
            .jurorNumber(jurorNumber)
            .thirdParty(thirdParty)
            ;
    }

    @Schema(description = "Juror number", requiredMode = Schema.RequiredMode.REQUIRED)
    private String jurorNumber;

    @Length(max = 10)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Null(groups = ThirdPartyDeceasedValidationGroup.class)
    @Schema(description = "Juror title")
    private String title;// optional field

    @NotEmpty
    @Pattern(regexp = NO_PIPES_REGEX)
    @Length(max = 20)
    @Null(groups = ThirdPartyDeceasedValidationGroup.class)
    @Schema(description = "Juror first name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @NotEmpty
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Null(groups = ThirdPartyDeceasedValidationGroup.class)
    @Schema(description = "Juror last name", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    //    @NotEmpty
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Null(groups = ThirdPartyDeceasedValidationGroup.class)
    @Schema(description = "Juror address line 1", requiredMode = Schema.RequiredMode.REQUIRED)
    private String addressLineOne;

    //    @NotEmpty
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Null(groups = ThirdPartyDeceasedValidationGroup.class)
    @Schema(description = "Juror address line 2", requiredMode = Schema.RequiredMode.REQUIRED)
    private String addressLineTwo;

    //    @NotEmpty
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Null(groups = ThirdPartyDeceasedValidationGroup.class)
    @Schema(description = "Juror address line 3", requiredMode = Schema.RequiredMode.REQUIRED)
    private String addressLineThree;

    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Null(groups = ThirdPartyDeceasedValidationGroup.class)
    @Schema(description = "Juror address line 4")
    private String addressTown;// optional field

    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Null(groups = ThirdPartyDeceasedValidationGroup.class)
    @Schema(description = "Juror address line 5")
    private String addressCounty;// optional field

    @NotEmpty
    @Length(max = 8)
    @Pattern(regexp = POSTCODE_REGEX)
    @Null(groups = ThirdPartyDeceasedValidationGroup.class)
    @Schema(description = "Juror address post code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String addressPostcode;

    @NotNull
    @DateOfBirth(groups = IneligibleAgeValidationGroup.class)
    @Null(groups = ThirdPartyDeceasedValidationGroup.class)
    @Schema(description = "Juror date of birth")
    private Date dateOfBirth;

    @NotEmpty
    @Pattern(regexp = PHONE_PRIMARY_REGEX, groups = {ThirdPartyValidationGroup.class, Default.class})
    @Null(groups = ThirdPartyDeceasedValidationGroup.class)
    @Schema(description = "Juror primary telephone number")
    private String primaryPhone;

    @Pattern(regexp = PHONE_SECONDARY_REGEX, groups = {ThirdPartyValidationGroup.class, Default.class})
    @Null(groups = ThirdPartyDeceasedValidationGroup.class)
    @Schema(description = "Juror secondary telephone number")
    private String secondaryPhone;

    @NotEmpty
    @Length(max = 254)
    @Pattern(regexp = EMAIL_ADDRESS_REGEX)
    @Null(groups = ThirdPartyDeceasedValidationGroup.class)
    @Schema(description = "Juror email address")
    private String emailAddress;

    @Null(groups = ThirdPartyDeceasedValidationGroup.class)
    @Schema(description = "Array of any Criminal Justice System employment of the Juror")
    private List<CJSEmployment> cjsEmployment;

    @Null(groups = ThirdPartyDeceasedValidationGroup.class)
    @Schema(description = "Array of any special requirements of the Juror at the court location")
    private List<SpecialNeed> specialNeeds;

    @Null(groups = ThirdPartyDeceasedValidationGroup.class)
    @Schema(description = "Juror text description of special arrangements required")
    private String assistanceSpecialArrangements;

    @Null(groups = ThirdPartyDeceasedValidationGroup.class)
    @Schema(description = "Object of information required seeking a deferral of jury service")
    private Deferral deferral;

    @Null(groups = ThirdPartyDeceasedValidationGroup.class)
    @Schema(description = "Object of information required seeking an excusal from jury service")
    private Excusal excusal;

    @NotNull(groups = FirstPersonValidationGroup.class)
    @Null(groups = {ThirdPartyDeceasedValidationGroup.class})
    @Schema(description = "Jury service qualification details in a juror response")
    private Qualify qualify;

    @Schema(description = "Optimistic locking version number (maintain across requests)")
    private Integer version;

    @Valid
    @NotNull(groups = ThirdPartyDeceasedValidationGroup.class)
    @Schema(description = "Details of person who replies on behalf of the Juror")
    private ThirdParty thirdParty;

    @Schema(description = "Is this response Welsh language?")
    @Builder.Default
    private Boolean welsh = Boolean.FALSE;

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Schema(description = "Deferral information in a juror response")
    public static class Deferral {
        @Schema(description = "Reason juror is seeking a deferral", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        private String reason;

        @Schema(description = "Textual description of suitable dates the juror will be available for service",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        private String dates;
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Schema(description = "Excusal information in a juror response")
    public static class Excusal {
        @Schema(description = "Reason juror is seeking an excusal", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        private String reason;
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Schema(description = "Jury service qualification details in a juror response")
    public static class Qualify {
        @Schema(description = "Whether the Juror has lived in the UK for the required period", requiredMode =
            Schema.RequiredMode.REQUIRED)
        @NotNull
        private Answerable livedConsecutive;

        @Schema(description = "Whether the Juror has been sectioned under the Mental Health Act", requiredMode =
            Schema.RequiredMode.REQUIRED)
        @NotNull
        private Answerable mentalHealthAct;

        @Schema(description = "Whether the Juror is on bail", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        private Answerable onBail;

        @Schema(description = "Whether the Juror has a criminal conviction", requiredMode =
            Schema.RequiredMode.REQUIRED)
        @NotNull
        private Answerable convicted;
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Schema(description = "Qualification criteria object")
    public static class Answerable {
        @Schema(description = "Criteria confirmed", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        private boolean answer;
        @Schema(description = "Textual description of the criteria")
        private String details;
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Schema(description = "Special need object. Single requirement category the Juror requests assistance for. "
        + "("
        + "V=Visual Impairment"
        + "W=Wheel Chair Access"
        + "H=Hearing Impairment"
        + "O=Other"
        + "I=Diabetic"
        + "D=Diet"
        + "P=Pregnancy"
        + "R=Reading"
        + "L=Limited Mobility"
        + ")"
    )
    public static class SpecialNeed {
        @Schema(description = "Assistance type", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues =
            "null,V,W,H,O,I,D,P,R,L")
        @NotNull
        private String assistanceType;

        @Schema(description = "Textual description of the support requested by the Juror", requiredMode =
            Schema.RequiredMode.REQUIRED)
        @NotNull
        private String assistanceTypeDetails;
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Schema(description = "Criminal Justice System employment object for the Juror")
    public static class CJSEmployment {
        @Schema(description = "CJS employer name", requiredMode = Schema.RequiredMode.REQUIRED, example = "Police")
        @NotNull
        private String cjsEmployer;

        @Schema(description = "Textual description of the Juror's employment", requiredMode =
            Schema.RequiredMode.REQUIRED,
            example = "Worked as a mechanic maintaining police vehicles")
        @NotNull
        private String cjsEmployerDetails;
    }

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Schema(description = "Response submitted by a third party")
    @ThirdPartyRequireAtLeastOneOf(
        groups = {ThirdPartyValidationCommonGroup.class},
        message = "Third party must supply a main phone or an email address")
    public static class ThirdParty {
        @Schema(description = "Third party firstname", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(groups = {ThirdPartyValidationGroup.class, ThirdPartyDeceasedValidationGroup.class})
        private String thirdPartyFName;

        @Schema(description = "Third party lastname", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(groups = {ThirdPartyValidationGroup.class, ThirdPartyDeceasedValidationGroup.class})
        private String thirdPartyLName;

        @Schema(description = "Third party relationship to the juror", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(groups = {ThirdPartyValidationGroup.class, ThirdPartyDeceasedValidationGroup.class})
        private String relationship;

        /**
         * Removed redundant field.
         *
         * @deprecated
         */
        @Schema(description = "Third party email contact preference address")
        @Null(groups = ThirdPartyDeceasedValidationGroup.class)
        @Deprecated
        private String contactEmail;

        /**
         * @implNote Validated by {@link ScriptAssert}
         */
        @Pattern(regexp = THIRD_PARTY_PHONE_PRIMARY_REGEX, groups = {ThirdPartyValidationGroup.class})
        @Schema(description = "Third party landline phone number", requiredMode = Schema.RequiredMode.REQUIRED)
        private String mainPhone;

        @Pattern(regexp = THIRD_PARTY_PHONE_SECONDARY_REGEX, groups = {ThirdPartyValidationGroup.class})
        @Schema(description = "Third party alternative phone number")
        private String otherPhone;

        /**
         * @implNote Validated by {@link ScriptAssert}
         */
        @Length(max = 254)
        @Schema(description = "Third party personal email address", requiredMode = Schema.RequiredMode.REQUIRED)
        private String emailAddress;

        @Schema(description = "Third party reason", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(groups = {ThirdPartyValidationGroup.class, ThirdPartyDeceasedValidationGroup.class})
        private String thirdPartyReason;

        @Schema(description = "Third party other reason")
        private String thirdPartyOtherReason;

        @Schema(description = "Whether the juror's email details should be used (false = use third party details)")
        @Builder.Default
        private Boolean useJurorEmailDetails = Boolean.FALSE;

        @Schema(description = "Whether the juror's phone details should be used (false = use third party details)")
        @Builder.Default
        private Boolean useJurorPhoneDetails = Boolean.TRUE;
    }

    /**
     * Validation group for all third party responses.
     */
    public interface ThirdPartyValidationCommonGroup {
        // note this interface does not extend jakarta.validation.groups.Default
    }

    /**
     * Validation group for all third party responses (where the juror is alive).
     */
    public interface ThirdPartyValidationGroup extends ThirdPartyValidationCommonGroup, IneligibleAgeValidationGroup {
        // note this interface does not extend jakarta.validation.groups.Default
    }

    /**
     * Validation group for third party deceased responses.
     */
    public interface ThirdPartyDeceasedValidationGroup extends ThirdPartyValidationCommonGroup {
        // note this interface does not extend jakarta.validation.groups.Default
    }

    /**
     * Validation group for age ineligible responses.
     */
    public interface IneligibleAgeValidationGroup {
        // note this interface does not extend jakarta.validation.groups.Default
    }

    /**
     * Validation group for first person responses INCLUDING default validation.
     */
    public interface FirstPersonValidationGroup extends IneligibleAgeValidationGroup, Default {
        // no-op
    }
}
