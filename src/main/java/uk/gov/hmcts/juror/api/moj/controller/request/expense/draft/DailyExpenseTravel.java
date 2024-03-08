package uk.gov.hmcts.juror.api.moj.controller.request.expense.draft;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.juror.api.validation.ExpenseNumericLimit;

import java.math.BigDecimal;

@Data
@Builder
public class DailyExpenseTravel {

    @JsonProperty("traveled_by_car")
    private Boolean traveledByCar;

    @JsonProperty("jurors_taken_by_car")
    @ExpenseNumericLimit
    private Integer jurorsTakenCar;

    @JsonProperty("traveled_by_motorcycle")
    private Boolean traveledByMotorcycle;

    @JsonProperty("jurors_taken_by_motorcycle")
    @ExpenseNumericLimit
    private Integer jurorsTakenMotorcycle;

    @JsonProperty("traveled_by_bicycle")
    private Boolean traveledByBicycle;

    @JsonProperty("miles_traveled")
    @ExpenseNumericLimit
    private Integer milesTraveled;

    @JsonProperty("parking")
    @ExpenseNumericLimit
    private BigDecimal parking;

    @JsonProperty("public_transport")
    @ExpenseNumericLimit
    private BigDecimal publicTransport;

    @JsonProperty("taxi")
    @ExpenseNumericLimit
    private BigDecimal taxi;
}
