package uk.gov.hmcts.juror.api.moj.controller.request.trial;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;


@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "The details containing the empanelled result for jurors")
public class JurorDetailRequestDto {
    @JsonProperty("juror_number")
    @JurorNumber
    @Schema(description = "9 digit numeric string to uniquely identify a juror", example =
        "111111111")
    private String jurorNumber;

    @JsonProperty("first_name")
    @NotBlank
    @Pattern(regexp = NO_PIPES_REGEX)
    @Length(max = 20)
    @Schema(description = "Juror first name", example = "FNAME")
    private String firstName;

    @JsonProperty("last_name")
    @NotBlank
    @Pattern(regexp = NO_PIPES_REGEX)
    @Length(max = 20)
    @Schema(description = "Juror last name", example = "LNAME")
    private String lastName;

    @JsonProperty("empanel_status")
    @Enumerated(EnumType.STRING)
    @Schema(description = "An enum representing the empanelled status e.g J - Juror")
    private PanelResult result;
}
