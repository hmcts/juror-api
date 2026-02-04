package uk.gov.hmcts.juror.api.jurorer.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;




/**
 * Response DTO after file upload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "File upload response")

public class FileUploadsResponseDto {
    @JsonProperty("success")
    @Schema(description = "Whether upload was successful", example = "true")
    private Boolean success;

    @JsonProperty("upload_id")
    @Schema(description = "ID of the created upload record", example = "123")
    private Long uploadId;

    @JsonProperty("message")
    @Schema(description = "Success or error message")
    private String message;

    @JsonProperty("upload_details")
    @Schema(description = "Details of the uploaded file")
    private FileUploadDto uploadDetails;

    @JsonProperty("updated_status")
    @Schema(description = "Updated upload status for the LA")
    private UploadStatusDto updatedStatus;
}
