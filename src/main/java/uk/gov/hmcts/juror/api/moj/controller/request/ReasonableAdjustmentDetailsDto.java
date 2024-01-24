package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Request DTO for Juror Response Special needs Details.
 */
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Reasonable adjustments details of Juror paper response.")
public class ReasonableAdjustmentDetailsDto {

    @JsonProperty("specialNeeds")
    @Schema(description = "Array of any reasonable adjustments of the Juror")
    private List<JurorPaperResponseDto.ReasonableAdjustment> reasonableAdjustments;

}
