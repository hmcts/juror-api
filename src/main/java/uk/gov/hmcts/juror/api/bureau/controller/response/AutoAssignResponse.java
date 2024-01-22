package uk.gov.hmcts.juror.api.bureau.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.bureau.controller.request.AutoAssignRequest;

import java.util.List;

/**
 * Response DTO for GET autoassign.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Staff member capacity")
public class AutoAssignResponse {

    @Schema(description = "Staff capacity data")
    @JsonProperty("data")
    private List<StaffCapacityResponse> data;

    @Schema
    private AutoAssignmentMetadata meta;

    @NoArgsConstructor
    @Data
    @EqualsAndHashCode(callSuper = true)
    @Schema(description = "Staff member capacity")
    public static class StaffCapacityResponse extends AutoAssignRequest.StaffCapacity {

        @JsonProperty("name")
        @Schema(description = "Staff member name", example = "Joanna Powers")
        private String name;

        @JsonProperty("urgents")
        @Schema(description = "Number of urgent/super-urgent responses assigned", example = "15")
        private Long urgents;

        @JsonProperty("allocation")
        @Schema(description = "Maximum workload for this staff member (capacity + urgents)", example = "75")
        private Long allocation;

        @JsonProperty("incompletes")
        @Schema(description = "Current incomplete responses assigned to this staff member", example = "4")
        private Long incompletes;

        @Builder(builderMethodName = "responseBuilder")
        public StaffCapacityResponse(String login, Integer capacity, String name, Long urgents, Long allocation,
                                     Long incompletes) {
            super(login, capacity);
            this.name = name;
            this.urgents = urgents;
            this.allocation = allocation;
            this.incompletes = incompletes;
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    @Schema(description = "Metadata for auto-assignment")
    public static class AutoAssignmentMetadata {

        @JsonProperty("backlogCount")
        @Schema(description = "Number of backlog items available for auto-assignment", example = "150")
        private Long backlogSize;
    }
}
