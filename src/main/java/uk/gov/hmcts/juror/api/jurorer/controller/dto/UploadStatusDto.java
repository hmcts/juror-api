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

import java.time.LocalDateTime;

/**
 * Response DTO for detailed upload status of a specific local authority.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Detailed upload status for a local authority")
public class UploadStatusDto {

    @JsonProperty("la_code")
    @Schema(description = "Local Authority code", example = "314")
    private String laCode;

    @JsonProperty("la_name")
    @Schema(description = "Local Authority name", example = "Birmingham")
    private String laName;

    @JsonProperty("is_active")
    @Schema(description = "Whether the LA is active", example = "true")
    private Boolean isActive;

    @JsonProperty("upload_status")
    @Schema(description = "Current upload status",
        allowableValues = {"NOT_UPLOADED", "UPLOADED"})
    private String uploadStatus;

    @JsonProperty("notes")
    @Schema(description = "Additional notes about the LA", nullable = true)
    private String notes;

    @JsonProperty("inactive_reason")
    @Schema(description = "Reason why LA is inactive", nullable = true)
    private String inactiveReason;

    @JsonProperty("updated_by")
    @Schema(description = "Username of person who last updated", nullable = true)
    private String updatedBy;

    @JsonProperty("last_updated")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Timestamp of last update", nullable = true)
    private LocalDateTime lastUpdated;
}
