package uk.gov.hmcts.juror.api.bureau.controller.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Request DTO for staff assignment responses.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
@Schema(description = "Staff to juror response assignment response")
public class StaffAssignmentResponseDto implements Serializable {
    @Schema(description = "Target juror response juror number", requiredMode = Schema.RequiredMode.REQUIRED)
    private String jurorResponse;

    @Schema(description = "Login name of the staff member who performed performed the update.")
    private String assignedBy;

    @Schema(description = "Login name of the staff member assigned. (null value indicates the response put to "
        + "backlog).")
    private String assignedTo;

    @Schema(description = "Assignment date")
    private LocalDate assignmentDate;
}
