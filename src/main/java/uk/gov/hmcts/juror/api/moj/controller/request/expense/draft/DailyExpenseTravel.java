package uk.gov.hmcts.juror.api.moj.controller.request.expense.draft;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.juror.api.validation.ExpenseNumericLimit;

import java.math.BigDecimal;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DailyExpenseTravel {

    private Boolean traveledByCar;

    @ExpenseNumericLimit
    private Integer jurorsTakenCar;

    private Boolean traveledByMotorcycle;

    @ExpenseNumericLimit
    private Integer jurorsTakenMotorcycle;

    private Boolean traveledByBicycle;

    @ExpenseNumericLimit
    private Integer milesTraveled;

    @ExpenseNumericLimit
    private BigDecimal parking;

    @ExpenseNumericLimit
    private BigDecimal publicTransport;

    @ExpenseNumericLimit
    private BigDecimal taxi;
}
