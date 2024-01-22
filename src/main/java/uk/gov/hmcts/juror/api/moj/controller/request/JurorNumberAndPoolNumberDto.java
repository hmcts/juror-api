package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import uk.gov.hmcts.juror.api.validation.JurorNumber;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

@Data
public class JurorNumberAndPoolNumberDto {

    @JurorNumber
    @NotBlank
    @JsonProperty("juror_number")
    private String jurorNumber;

    @PoolNumber
    @NotBlank
    @JsonProperty("pool_number")
    private String poolNumber;
}
