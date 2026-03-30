package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Schema(description = "List of notes relating to a specific juror record")
public class JurorNotesDto {

    @JsonProperty("notes")
    @Schema(description = "Juror notes")
    private String notes;

    @Schema(description = "Common details for every Juror record")
    private JurorDetailsCommonResponseDto commonDetails;
}
