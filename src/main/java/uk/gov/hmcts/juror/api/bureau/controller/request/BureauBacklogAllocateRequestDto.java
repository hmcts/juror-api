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
 * Request DTO for the backlog allocate replies feature.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Request body for backlog allocate replies.")
public class BureauBacklogAllocateRequestDto {

    @NotEmpty()
    @Schema(description = "Allocations requested for bureau officers", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<StaffAllocation> officerAllocations;

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    @Schema(description = "Allocations requested for one bureau officer")
    public static class StaffAllocation {

        @NotBlank(message = "Officer login name is required")
        @Schema(description = "Login name of the bureau officer", example = "jpowers", requiredMode =
            Schema.RequiredMode.REQUIRED)
        private String userId;

        @NotNull(message = "No Of Non Urgent responses to be allocated is required.")
        @Min(0)
        @Schema(description = "Number of Non-Urgent backlog items to allocate to the bureau officer", example = "60",
            requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer nonUrgentCount;

        @NotNull(message = "No Of Urgent responses to be allocated is required.")
        @Min(0)
        @Schema(description = "Number of Urgent backlog items to allocate to the bureau officer", example = "60",
            requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer urgentCount;

        @NotNull(message = "No of Super Urgent responses to be allocated is required.")
        @Min(0)
        @Schema(description = "Number of Super-Urgent backlog items to allocate to the bureau officer", example = "60"
            , requiredMode = Schema.RequiredMode.REQUIRED)
        private Integer superUrgentCount;

    }
}
