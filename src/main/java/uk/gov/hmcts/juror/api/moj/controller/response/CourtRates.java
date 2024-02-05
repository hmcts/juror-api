package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CourtRates {
    @JsonProperty("car_rate_0_passengers")
    private BigDecimal carRate0Passengers;
    @JsonProperty("car_rate_1_passenger")
    private BigDecimal carRate1Passenger;
    @JsonProperty("car_rate_2_or_more_passengers")
    private BigDecimal carRate2OrMorePassenger;

    @JsonProperty("motorcycle_rate_0_passengers")
    private BigDecimal motorcycleRate0Passenger;
    @JsonProperty("motorcycle_rate_1_or_more_passengers")
    private BigDecimal motorcycleRate1OrMorePassenger;

    @JsonProperty("bicycle_rate_0_or_more_passengers")
    private BigDecimal bicycleRate0OrMorePassenger;

    @JsonProperty("substance_rate_standard")
    private BigDecimal substanceRateStandard;
    @JsonProperty("substance_rate_long_day")
    private BigDecimal substanceRateLongDay;

    @JsonProperty("financial_loss_half_day_limit")
    private BigDecimal financialLossHalfDayLimit;
    @JsonProperty("financial_loss_full_day_limit")
    private BigDecimal financialLossFullDayLimit;
    @JsonProperty("financial_loss_half_day_long_trial_limit")
    private BigDecimal financialLossHalfDayLongTrialLimit;
    @JsonProperty("financial_loss_full_day_long_trial_limit")
    private BigDecimal financialLossFullDayLongTrialLimit;

    @JsonProperty("public_transport_soft_limit")
    private BigDecimal publicTransportSoftLimit;

}
