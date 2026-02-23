package uk.gov.hmcts.juror.api.moj.controller.jurorer;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Response after updating local authority notes")
public class UpdateLocalAuthorityNotesResponseDto {

    @Schema(description = "Local Authority code", example = "001")
    private String laCode;

    @Schema(description = "Local Authority name", example = "Birmingham")
    private String laName;

    @Schema(description = "Updated notes", example = "Contact attempted on 15/02/2025")
    private String notes;

    @Schema(description = "Username of who updated the notes", example = "bureau.admin@hmcts.gov.uk")
    private String updatedBy;

    @Schema(description = "Timestamp when the notes were last updated", example = "2025-02-19T14:30:00")
    private LocalDateTime lastUpdated;
}
