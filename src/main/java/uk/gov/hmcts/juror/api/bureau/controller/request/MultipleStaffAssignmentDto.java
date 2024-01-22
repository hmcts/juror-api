package uk.gov.hmcts.juror.api.bureau.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Request DTO for multiple staff assignment requests.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "Multiple juror response to staff assignment request")
public class MultipleStaffAssignmentDto implements Serializable {

    @Size(max = 20)
    @Schema(description = "Login name of the staff member to assign. (null value assigns the response to backlog).")
    private String assignTo;

    @Schema(description = "Responses to be assigned to the assignTo staff.")
    @NotEmpty
    private List<ResponseMetadata> responses;

    /**
     * Individual response in the list of responses.
     */
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @Schema(description = "Multiple juror response to staff assignment request")
    public static class ResponseMetadata implements Serializable {
        @NotEmpty
        @Size(max = 9)
        @Schema(description = "Target juror response juror number", requiredMode = Schema.RequiredMode.REQUIRED)
        private String responseJurorNumber;

        @Min(0)
        @Max(Integer.MAX_VALUE)
        @Schema(description = "Optimistic locking version", requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer version;
    }
}