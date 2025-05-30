package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
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
public class JurorPersonalDetailsDto {

    @JsonProperty("replyMethod")
    @Schema(name = "Reply method", description = "Reply method is either PAPER or DIGITAL", requiredMode =
        Schema.RequiredMode.REQUIRED)
    @NotNull
    private ReplyMethod replyMethod;

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
    @Length(max = 25)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror last name")
    private String lastName;

    @JsonProperty("addressLineOne")
    @NotBlank(message = "Juror address line 1 cannot be blank")
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

    @JsonProperty("thirdParty")
    @Schema(description = "Details of person who replies on behalf of the Juror")
    private JurorPaperResponseDto.ThirdParty thirdParty;

}
