package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.juror.api.validation.JurorNumber;


/**
 * Request DTO for marking a Juror as deceased.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Information required to mark a juror as deceased")
public class MarkAsDeceasedDto {

    @JsonProperty("jurorNumber")
    @JurorNumber
    @Schema(description = "Juror number", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty
    private String jurorNumber;

    @JsonProperty("deceasedComment")
    @Schema(description = "Comment from user regarding deceased juror")
    @NotEmpty
    private String deceasedComment;

    @JsonProperty("paperResponseExists")
    @Schema(description = "Flag indicating if there is a paper response for the juror")
    private Boolean paperResponseExists;

}
