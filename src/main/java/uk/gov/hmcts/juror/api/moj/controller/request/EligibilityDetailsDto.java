package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for Juror Paper Responses eligibility criteria updates.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Juror paper response eligibility information.")
public class EligibilityDetailsDto {

    @JsonProperty("eligibility")
    @Schema(description = "Jury service eligibility details in a juror response")
    private JurorPaperResponseDto.Eligibility eligibility;

}
