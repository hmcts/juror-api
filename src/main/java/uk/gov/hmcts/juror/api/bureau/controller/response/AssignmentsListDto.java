package uk.gov.hmcts.juror.api.bureau.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;

import java.util.List;

/**
 * Response DTO for staff assignment list endpoint.
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Builder
@Data
@Schema(description = "Staff assignments list response")
public class AssignmentsListDto {


    @Schema(description = "response data")
    private List<AssignmentListDataDto> data;

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    @Schema(description = "Staff assignment list data")
    public static class AssignmentListDataDto {


        @Schema(description = "Juror response number")
        private String jurorNumber;


        @Schema(description = "Response optimistic locking version")
        private Integer version;


        @Schema(description = "Staff member assigned to response (null == backlog)")
        private String assignedTo;


        @Schema(description = "Current processing status of response")
        private ProcessingStatus processingStatus;


        @Schema(description = "Urgent flag")
        private Boolean urgent;


        @Schema(description = "Juror Name display format")
        private String jurorName;
    }
}

