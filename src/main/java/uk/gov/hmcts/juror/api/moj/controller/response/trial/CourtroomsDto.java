package uk.gov.hmcts.juror.api.moj.controller.response.trial;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Schema(description = "Courtroom details")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CourtroomsDto {

    @Schema(description = "Id field for courtroom")
    @NotNull
    private Long id;

    @Schema(description = "owner of the court room", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 3, max = 3)
    @NotBlank
    private String owner;

    @Schema(description = "loc code of the court room", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 3, max = 3)
    @NotBlank
    private String locCode;

    @Schema(description = "Room number for courtroom", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 1, max = 6)
    private String roomNumber;

    @Schema(description = "Courtroom description", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 1, max = 30)
    private String description;
}
