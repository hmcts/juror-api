package uk.gov.hmcts.juror.api.bureau.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Dto for responding to an operation on multiple failureDtos when there are failures.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Wrapper for returning details on operation failures")
public class OperationFailureListDto {
    @JsonProperty("failures")
    @Schema(description = "List of juror response numbers and why the requested operation failed")
    private List<OperationFailureDto> failureDtos;
}
