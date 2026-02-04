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
 * Response DTO for dashboard deadline and upload status information.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Dashboard information showing deadline and upload status")
public class DashboardInfoDto {

    @JsonProperty("deadline_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "The deadline date for file uploads", example = "2026-03-31")
    private LocalDate deadlineDate;

    @JsonProperty("days_remaining")
    @Schema(description = "Number of days remaining until deadline (negative if overdue)",
        example = "45")
    private Long daysRemaining;

    @JsonProperty("upload_status")
    @Schema(description = "Current upload status for the user's local authority",
        example = "NOT_UPLOADED",
        allowableValues = {"NOT_UPLOADED", "UPLOADED"})
    private String uploadStatus;

    @JsonProperty("la_code")
    @Schema(description = "Local Authority code", example = "314")
    private String laCode;

    @JsonProperty("la_name")
    @Schema(description = "Local Authority name", example = "Birmingham")
    private String laName;

    @JsonProperty("is_overdue")
    @Schema(description = "Flag indicating if deadline has passed", example = "false")
    private Boolean isOverdue;

    @JsonProperty("status_message")
    @Schema(description = "User-friendly status message",
        example = "You have 45 days remaining to upload your file")
    private String statusMessage;
}
