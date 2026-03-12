package uk.gov.hmcts.juror.api.moj.controller.jurorer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Request to update local authority notes")
public class UpdateLocalAuthorityNotesRequestDto {

    @NotNull(message = "LA code is required")
    @Size(min = 3, max = 3, message = "LA code must be 3 characters")
    @JsonProperty("la_code")
    @Schema(description = "Local Authority code", example = "001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String laCode;

    @JsonProperty("notes")
    @Size(max = 2000, message = "Notes cannot exceed 2000 characters")
    @Schema(description = "Notes for the local authority (null to clear notes)",
        example = "Contact attempted on 15/02/2025",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String notes;
}
