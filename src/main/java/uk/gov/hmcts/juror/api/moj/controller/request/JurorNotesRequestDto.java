package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Schema(description = "Juror notes request")
public class JurorNotesRequestDto {

    @JsonProperty("notes")
    @Size(max = 2000)
    @Schema(description = "A notes string", example = "Some free text as a note")
    private String notes;

}
