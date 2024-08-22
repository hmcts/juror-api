package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.service.JurorThirdPartyService;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.EMAIL_ADDRESS_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.PHONE_NO_REGEX;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class JurorThirdPartyDto implements JurorThirdPartyService.ThirdPartyUpdateDto {

    @JsonProperty("first_name")
    @Length(max = 50)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Third party first name")
    private String firstName;

    @JsonProperty("last_name")
    @Length(max = 50)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Third party last name")
    private String lastName;

    @JsonProperty("relationship")
    @Length(max = 50)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Relationship to juror")
    private String relationship;

    @JsonProperty("main_phone")
    @Length(max = 50)
    @Pattern(regexp = PHONE_NO_REGEX)
    @Schema(description = "Main phone number")
    private String mainPhone;

    @JsonProperty("other_phone")
    @Length(max = 50)
    @Pattern(regexp = PHONE_NO_REGEX)
    @Schema(description = "Other phone number")
    private String otherPhone;

    @JsonProperty("email_address")
    @Length(max = 254)
    @Pattern(regexp = EMAIL_ADDRESS_REGEX)
    @Schema(description = "Email address")
    private String emailAddress;

    @JsonProperty("reason")
    @Length(max = 1250)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Reason for third party")
    private String reason;

    @JsonProperty("other_reason")
    @Length(max = 1250)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Other reason for third party")
    private String otherReason;

    @JsonProperty("contact_juror_by_phone")
    private boolean contactJurorByPhone;

    @JsonProperty("contact_juror_by_email")
    private boolean contactJurorByEmail;

    @Override
    public String getThirdPartyOtherReason() {
        return this.getOtherReason();
    }

    @Override
    public String getThirdPartyReason() {
        return this.getReason();
    }

    @Override
    public String getThirdPartyEmailAddress() {
        return this.getEmailAddress();
    }

    @Override
    public String getThirdPartyOtherPhone() {
        return this.getOtherPhone();
    }

    @Override
    public String getThirdPartyMainPhone() {
        return this.getMainPhone();
    }

    @Override
    public String getThirdPartyRelationship() {
        return this.getRelationship();
    }

    @Override
    public String getThirdPartyLastName() {
        return this.getLastName();
    }

    @Override
    public String getThirdPartyFirstName() {
        return this.getFirstName();
    }
}