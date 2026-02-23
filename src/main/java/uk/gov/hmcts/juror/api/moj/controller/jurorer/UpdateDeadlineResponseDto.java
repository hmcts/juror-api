package uk.gov.hmcts.juror.api.moj.controller.jurorer;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Response after updating deadline date")
public class UpdateDeadlineResponseDto {

    @Schema(description = "Updated deadline date", example = "2025-03-31")
    private LocalDate deadlineDate;

    @Schema(description = "Username of who updated the deadline", example = "bureau.admin@hmcts.gov.uk")
    private String updatedBy;

    @Schema(description = "Date when the deadline was updated", example = "2025-02-19")
    private LocalDate lastUpdated;

    @Schema(description = "Days remaining until the deadline", example = "40")
    private Long daysRemaining;
}
