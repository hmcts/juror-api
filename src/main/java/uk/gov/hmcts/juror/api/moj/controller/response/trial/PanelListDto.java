package uk.gov.hmcts.juror.api.moj.controller.response.trial;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.NO_PIPES_REGEX;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "A List of jurors for a panel")
public class PanelListDto {

    @JsonProperty("juror_number")
    @JurorNumber
    @Schema(name = "Juror number", description = "9 digit numeric string to uniquely identify a juror")
    private String jurorNumber;

    @JsonProperty("first_name")
    @Pattern(regexp = NO_PIPES_REGEX)
    private String firstName;

    @JsonProperty("last_name")
    @Pattern(regexp = NO_PIPES_REGEX)
    private String lastName;

    @JsonProperty("juror_status")
    @NotBlank
    @Schema(name = "Juror status", description = "A status representing the juror e.g. Panelled")
    private String jurorStatus;
}
