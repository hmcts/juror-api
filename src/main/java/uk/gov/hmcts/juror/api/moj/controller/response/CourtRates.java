package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CourtRates {

    @JsonProperty("public_transport_soft_limit")
    @Min(0)
    private BigDecimal publicTransportSoftLimit;

    @JsonProperty("taxi_soft_limit")
    @Min(0)
    private BigDecimal taxiSoftLimit;

}
