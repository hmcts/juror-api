package uk.gov.hmcts.juror.api.bureau.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dto for responding to a request on a single response when there is an operational failure.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Schema(description = "Dto listing a reason code an operation on a response failed")
public class OperationFailureDto {
    @Schema(description = "Juror number")
    private String jurorNumber;
    @Schema(description = "Reason code the requested operation failed")
    private String reason;
}
