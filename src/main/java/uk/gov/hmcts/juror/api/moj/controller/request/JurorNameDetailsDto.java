package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class JurorNameDetailsDto {

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

}
