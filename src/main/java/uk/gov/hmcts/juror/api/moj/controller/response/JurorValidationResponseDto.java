package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;

@Getter
@Setter
@Valid
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class JurorValidationResponseDto {
    @NotBlank
    @JsonProperty("juror_number")
    @JurorNumber
    private String jurorNumber;

    @NotBlank
    @Length(max = 20)
    @Pattern(regexp = NO_PIPES_REGEX)
    @JsonProperty("first_name")
    private String firstName;

    @NotBlank
    @Length(max = 25)
    @Pattern(regexp = NO_PIPES_REGEX)
    @JsonProperty("last_name")
    private String lastName;
}
