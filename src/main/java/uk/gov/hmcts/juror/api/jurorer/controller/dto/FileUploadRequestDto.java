package uk.gov.hmcts.juror.api.jurorer.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for file upload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "File upload request metadata")
public class FileUploadRequestDto {

    @JsonProperty("filename")
    @NotBlank(message = "Filename is required")
    @Size(max = 200, message = "Filename cannot exceed 200 characters")
    @Schema(description = "Name of the file", example = "juror_excusals_2026.xlsx", required = true)
    private String filename;

    @JsonProperty("file_format")
    @NotBlank(message = "File format is required")
    @Size(max = 100, message = "File format cannot exceed 100 characters")
    @Schema(description = "File format", example = "express", required = true)
    private String fileFormat;

    @JsonProperty("file_size_bytes")
    @Schema(description = "File size in bytes", example = "2048576")
    private Long fileSizeBytes;

    @JsonProperty("other_information")
    @Size(max = 1000, message = "Other information cannot exceed 1000 characters")
    @Schema(description = "Additional notes about the upload", example = "Q1 2026 excusals")
    private String otherInformation;
}
