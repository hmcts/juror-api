package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for Juror Paper Responses reply type update.
 */
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Getter
@Setter
@Schema(description = "Juror paper response reply type information.")
public class ReplyTypeDetailsDto {

    @JsonProperty("deferral")
    @Schema(description = "Flag indicating if seeking a deferral of jury service")
    private Boolean deferral;

    @JsonProperty("excusal")
    @Schema(description = "Flag indicating if seeking an excusal from jury service")
    private Boolean excusal;

}
