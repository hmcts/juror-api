package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.validation.LocalDateOfBirth;

import java.time.LocalDate;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.EMAIL_ADDRESS_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.PHONE_NO_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.POSTCODE_REGEX;

/**
 * Request DTO for Juror Paper Response personal details.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Juror paper response personal details")
public class EditJurorRecordRequestDto {

    @JsonProperty("title")
    @Length(max = 10)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror title")
    private String title;

    @JsonProperty("first_name")
    @NotBlank
    @Pattern(regexp = NO_PIPES_REGEX)
    @Length(max = 20)
    @Schema(description = "Juror first name")
    private String firstName;

    @JsonProperty("last_name")
    @NotBlank
    @Length(max = 25)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror last name")
    private String lastName;

    @JsonProperty("address_line_one")
    @NotBlank(message = "Juror address line 1 cannot be blank")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 1")
    private String addressLineOne;

    @JsonProperty("address_line_two")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 2")
    private String addressLineTwo;

    @JsonProperty("address_line_three")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 3")
    private String addressLineThree;

    @JsonProperty("address_town")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 4")
    private String addressTown;

    @JsonProperty("address_county")
    @Length(max = 35)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror address line 5")
    private String addressCounty;

    @JsonProperty("address_postcode")
    @NotBlank
    @Length(max = 8)
    @Pattern(regexp = POSTCODE_REGEX)
    @Schema(description = "Juror address post code")
    private String addressPostcode;

    @JsonProperty("date_of_birth")
    @LocalDateOfBirth
    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "Juror date of birth")
    private LocalDate dateOfBirth;

    @JsonProperty("primary_phone")
    @Pattern(regexp = PHONE_NO_REGEX)
    @Schema(description = "Juror primary telephone number")
    private String primaryPhone;

    @JsonProperty("secondary_phone")
    @Pattern(regexp = PHONE_NO_REGEX)
    @Schema(description = "Juror secondary telephone number")
    private String secondaryPhone;

    @JsonProperty("email_address")
    @Length(max = 254)
    @Pattern(regexp = EMAIL_ADDRESS_REGEX)
    @Schema(description = "Juror email address")
    private String emailAddress;

    @JsonProperty("spec_need_value")
    @Length(max = 1)
    @Schema(description = "Dropdown value for special needs")
    private String specialNeed;

    @JsonProperty("spec_need_msg")
    @Schema(description = "Special needs message")
    private String specialNeedMessage;

    @JsonProperty("optic_reference")
    @Size(min = 8, max = 8)
    @Schema(description = "Eight digit Optic Reference Number for Juror")
    private String opticReference;

    @JsonProperty("pending_title")
    @Length(max = 10)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "New juror title to submit for approval")
    private String pendingTitle;

    @JsonProperty("pending_first_name")
    @Pattern(regexp = NO_PIPES_REGEX)
    @Length(max = 20)
    @Schema(description = "New juror first name to submit for approval")
    private String pendingFirstName;

    @JsonProperty("pending_last_name")
    @Length(max = 25)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "New juror last name to submit for approval")
    private String pendingLastName;

    @JsonProperty("welsh_language_required")
    @Schema(description = "Determines if Juror requires Welsh language")
    private Boolean welshLanguageRequired;

    @JsonProperty("living_overseas")
    @Schema(description = "Determines if Juror is living overseas")
    private Boolean livingOverseas;

    @JsonProperty("third_party")
    private JurorThirdPartyDto thirdParty;

}
