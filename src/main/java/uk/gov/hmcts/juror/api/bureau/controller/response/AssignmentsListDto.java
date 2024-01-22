package uk.gov.hmcts.juror.api.bureau.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.bureau.controller.request.AssignmentsMultiRequestDto;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;

import java.util.List;

/**
 * Response DTO for staff assignment list endpoint.
 *
 * @see uk.gov.hmcts.juror.api.bureau.controller.BureauStaffController#getStaffAssignments(AssignmentsMultiRequestDto, BureauJwtAuthentication)
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Staff assignments list response")
public class AssignmentsListDto {

    @JsonProperty("data")
    @Schema(description = "response data")
    private List<AssignmentListDataDto> data;

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    @Schema(description = "Staff assignment list data")
    public static class AssignmentListDataDto {

        @JsonProperty("jurorNumber")
        @Schema(description = "Juror response number")
        private String jurorNumber;

        @JsonProperty("version")
        @Schema(description = "Response optimistic locking version")
        private Integer version;

        @JsonProperty("assignedTo")
        @Schema(description = "Staff member assigned to response (null == backlog)")
        private String assignedTo;

        @JsonProperty("processingStatus")
        @Schema(description = "Current processing status of response")
        private ProcessingStatus processingStatus;

        @JsonProperty("urgent")
        @Schema(description = "Urgent flag")
        private Boolean urgent;

        @JsonProperty("superUrgent")
        @Schema(description = "Super Urgent flag")
        private Boolean superUrgent;

        @JsonProperty("jurorNameDisplay")
        @Schema(description = "Juror Name display format")
        private String jurorName;
    }
}

