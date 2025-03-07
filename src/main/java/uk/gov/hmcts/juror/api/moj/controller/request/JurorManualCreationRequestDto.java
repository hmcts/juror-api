package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.validation.LocalDateOfBirth;
import uk.gov.hmcts.juror.api.validation.NumericString;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

import java.time.LocalDate;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.EMAIL_ADDRESS_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;
import static uk.gov.hmcts.juror.api.validation.ValidationConstants.PHONE_NO_REGEX;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class JurorManualCreationRequestDto {

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
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    @Schema(description = "Juror last name")
    private String lastName;

    @JsonProperty("address")
    @NotNull
    private JurorAddressDto address;

    @JsonProperty("date_of_birth")
    @LocalDateOfBirth
    @Schema(description = "Juror date of birth")
    private LocalDate dateOfBirth;

    @JsonProperty("primary_phone")
    @Pattern(regexp = PHONE_NO_REGEX)
    @Schema(description = "Juror primary telephone number")
    private String primaryPhone;

    @JsonProperty("alternative_phone")
    @Pattern(regexp = PHONE_NO_REGEX)
    @Schema(description = "Juror alternative telephone number")
    private String alternativePhone;

    @JsonProperty("email_address")
    @Length(max = 254)
    @Pattern(regexp = EMAIL_ADDRESS_REGEX)
    @Schema(description = "Juror email address")
    private String emailAddress;

    @JsonProperty("notes")
    @Length(max = 2000)
    @Schema(description = "Notes on the juror")
    private String notes;

    @JsonProperty("pool_number")
    @PoolNumber
    @NotBlank
    @Schema(name = "Pool number", description = "The unique number for the existing pool")
    private String poolNumber;

    @JsonProperty("court_code")
    @NotBlank
    @Length(min = 3, max = 3)
    @NumericString
    @Schema(description = "Unique 3 digit code to identify a court location")
    private String locationCode;

}
