package uk.gov.hmcts.juror.api.moj.controller.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.validation.JurorNumber;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
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
