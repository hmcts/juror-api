package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request DTO for Juror Paper Response signature update.
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Juror paper response signature information.")
public class SignatureDetailsDto {

    @JsonProperty("signature")
    @Schema(description = "Flag indicating if signature is present in paper response")
    private Boolean signature;

}
