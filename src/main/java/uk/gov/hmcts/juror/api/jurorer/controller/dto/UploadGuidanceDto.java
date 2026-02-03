package uk.gov.hmcts.juror.api.jurorer.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Upload guidance and instructions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Guidance and requirements for file upload")
public class UploadGuidanceDto {

    @JsonProperty("allowed_formats")
    @Schema(description = "List of allowed file formats",
        example = "[\"xlsx\", \"csv\", \"xls\"]")
    private List<String> allowedFormats;

    @JsonProperty("max_file_size_mb")
    @Schema(description = "Maximum file size in megabytes", example = "10")
    private Integer maxFileSizeMb;

    @JsonProperty("instructions")
    @Schema(description = "Step-by-step upload instructions")
    private List<String> instructions;

    @JsonProperty("requirements")
    @Schema(description = "File content requirements")
    private List<String> requirements;

    @JsonProperty("support_contact")
    @Schema(description = "Contact information for support")
    private SupportContactDto supportContact;
}
