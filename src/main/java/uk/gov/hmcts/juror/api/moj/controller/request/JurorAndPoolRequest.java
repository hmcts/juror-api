package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.juror.api.validation.JurorNumber;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

@Data
@Builder
@Valid
@Schema(description = "Request payload containing a Juror number and there related pool number")
public class JurorAndPoolRequest {
    @JsonProperty("juror_number")
    @NotBlank
    @JurorNumber
    @Schema(description = "Juror number", requiredMode = Schema.RequiredMode.REQUIRED)
    private String jurorNumber;

    @JsonProperty("pool_number")
    @NotBlank
    @PoolNumber
    @Schema(description = "Pool number", requiredMode = Schema.RequiredMode.REQUIRED)
    private String poolNumber;
}
