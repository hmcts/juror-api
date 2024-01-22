package uk.gov.hmcts.juror.api.moj.controller.response.trial;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class CourtroomsDto {
    @JsonProperty
    @Schema(description = "Id field for courtroom")
    @NotNull
    private Long id;

    @JsonProperty("owner")
    @Schema(description = "owner of the court room", requiredMode = Schema.RequiredMode.REQUIRED)
    @Size(min = 3, max = 3)
    @NotBlank
    private String owner;

    @JsonProperty("room_number")
    @Schema(description = "Room number for courtroom", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 1, max = 6)
    private String roomNumber;

    @JsonProperty("description")
    @Schema(description = "Courtroom description", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    @Size(min = 1, max = 30)
    private String description;
}
