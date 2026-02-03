package uk.gov.hmcts.juror.api.jurorer.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Response DTO for deadline information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Deadline information for file uploads")
public class DeadlineDto {

    @JsonProperty("deadline_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "The deadline date", example = "2026-03-31")
    private LocalDate deadlineDate;

    @JsonProperty("days_remaining")
    @Schema(description = "Days remaining until deadline", example = "45")
    private Long daysRemaining;

    @JsonProperty("is_overdue")
    @Schema(description = "Whether deadline has passed", example = "false")
    private Boolean isOverdue;

    @JsonProperty("updated_by")
    @Schema(description = "Who last updated the deadline", nullable = true)
    private String updatedBy;

    @JsonProperty("last_updated")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    @Schema(description = "When deadline was last updated", nullable = true)
    private OffsetDateTime lastUpdated;
}
