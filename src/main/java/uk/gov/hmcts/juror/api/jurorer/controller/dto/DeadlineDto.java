package uk.gov.hmcts.juror.api.jurorer.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Response DTO for deadline information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Deadline information for file uploads")
public class DeadlineDto {

    @JsonProperty("deadline_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "The deadline date", example = "2026-03-31")
    private LocalDate deadlineDate;

    @JsonProperty("is_deadline_passed")
    @Schema(description = "Whether the deadline has passed", example = "false")
    private boolean isDeadlinePassed;

    @JsonProperty("upload_start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "The date when uploads can start", example = "2026-01-01")
    private LocalDate uploadStartDate;

    @JsonProperty("days_remaining")
    @Schema(description = "Days remaining until deadline", example = "45")
    private Long daysRemaining;

}
