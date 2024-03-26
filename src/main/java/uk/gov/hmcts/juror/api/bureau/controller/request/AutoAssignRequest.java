package uk.gov.hmcts.juror.api.bureau.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import java.util.List;

/**
 * Request DTO for the auto-assignment feature.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Request body for auto-assignment")
public class AutoAssignRequest {

    @NotEmpty
    @Schema(description = "Capacity settings for bureau officers", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<StaffCapacity> data;

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    @Schema(description = "Capacity setting for one bureau officer")
    public static class StaffCapacity {

        @NotBlank
        @Schema(description = "Login name of the bureau officer", example = "jpowers", requiredMode =
            Schema.RequiredMode.REQUIRED)
        private String login;

        @NotNull
        @Min(0)
        @Schema(description = "Number of backlog items to assign to the bureau officer", example = "60",
            requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer capacity;
    }
}
