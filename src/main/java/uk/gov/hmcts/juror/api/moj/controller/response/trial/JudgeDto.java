package uk.gov.hmcts.juror.api.moj.controller.response.trial;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Schema(description = "Information about a judge")
public class JudgeDto {
    @JsonProperty
    @Schema(description = "Id field for judge")
    @NotNull
    private Long id;

    @JsonProperty("code")
    @Schema(description = "A 4 digit abbreviation used as a code", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, max = 4)
    @NotBlank
    private String code;

    @JsonProperty("description")
    @Schema(description = "Name of the judge", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 1, max = 30)
    @NotBlank
    private String description;
}
