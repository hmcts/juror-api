package uk.gov.hmcts.juror.api.jurorer.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Upload history for a Local Authority.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Upload history for the Local Authority")
public class UploadHistoryDto {

    @JsonProperty("total_uploads")
    @Schema(description = "Total number of uploads", example = "5")
    private Long totalUploads;

    @JsonProperty("recent_uploads")
    @Schema(description = "List of recent uploads (last 10)")
    private List<FileUploadDto> recentUploads;
}
