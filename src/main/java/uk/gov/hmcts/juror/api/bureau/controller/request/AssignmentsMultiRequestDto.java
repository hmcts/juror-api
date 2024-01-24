package uk.gov.hmcts.juror.api.bureau.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Request DTO for staff assigned to a list of responses.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Schema(description = "List of response numbers to get staff assignment information from")
public class AssignmentsMultiRequestDto implements Serializable {
    @NotEmpty
    @Schema(description = "Target juror response juror number", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> jurorNumbers;
}
