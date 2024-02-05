package uk.gov.hmcts.juror.api.moj.controller.request.expense.draft;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DailyExpenseTravel {

    @JsonProperty("traveled_by_car")
    private Boolean traveledByCar;

    @JsonProperty("jurors_taken_by_car")
    @Min(0)
    private Integer jurorsTakenCar;

    @JsonProperty("traveled_by_motorcycle")
    private Boolean traveledByMotorcycle;

    @JsonProperty("jurors_taken_by_motorcycle")
    @Min(0)
    private Integer jurorsTakenMotorcycle;

    @JsonProperty("traveled_by_bicycle")
    private Boolean traveledByBicycle;

    @JsonProperty("miles_traveled")
    @Min(0)
    private Integer milesTraveled;

    @JsonProperty("parking")
    @Min(0)
    private BigDecimal parking;

    @JsonProperty("public_transport")
    @Min(0)
    private BigDecimal publicTransport;

    @JsonProperty("taxi")
    @Min(0)
    private BigDecimal taxi;
}
