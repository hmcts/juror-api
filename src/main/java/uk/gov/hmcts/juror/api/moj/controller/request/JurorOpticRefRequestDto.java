package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static uk.gov.hmcts.juror.api.validation.ValidationConstants.JUROR_NUMBER;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Juror Optics Reference request")
public class JurorOpticRefRequestDto {

    @JsonProperty("jurorNumber")
    @Pattern(regexp = JUROR_NUMBER)
    @Schema(description = "Juror number", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String jurorNumber;

    @JsonProperty("poolNumber")
    @Size(min = 9, max = 9)
    @NotEmpty
    @Schema(name = "Pool number", description = "Pool Request number", requiredMode = Schema.RequiredMode.REQUIRED)
    private String poolNumber;

    @JsonProperty("opticReference")
    @Size(min = 8, max = 8)
    @NotEmpty
    @Schema(name = "Optic Reference", description = "Eight digit Optic Reference Number for Juror", requiredMode =
        Schema.RequiredMode.REQUIRED)
    private String opticReference;

}
