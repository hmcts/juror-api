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
 * File upload record DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "File upload record")
public class FileUploadDto {

    @JsonProperty("id")
    @Schema(description = "Upload ID", example = "123")
    private Long id;

    @JsonProperty("filename")
    @Schema(description = "Name of uploaded file", example = "juror_excusals_2026.xlsx")
    private String filename;

    @JsonProperty("file_format")
    @Schema(description = "File format/extension", example = "xlsx")
    private String fileFormat;

    @JsonProperty("file_size_bytes")
    @Schema(description = "File size in bytes", example = "2048576")
    private Long fileSizeBytes;

    @JsonProperty("file_size_formatted")
    @Schema(description = "Human-readable file size", example = "2.05 MB")
    private String fileSizeFormatted;

    @JsonProperty("uploaded_by")
    @Schema(description = "Username who uploaded", example = "user@birmingham.gov.uk")
    private String uploadedBy;

    @JsonProperty("upload_date")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Schema(description = "Upload timestamp")
    private LocalDateTime uploadDate;

    @JsonProperty("other_information")
    @Schema(description = "Additional notes about upload")
    private String otherInformation;
}
