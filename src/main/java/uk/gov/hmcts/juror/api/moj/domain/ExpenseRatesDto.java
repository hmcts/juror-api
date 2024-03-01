package uk.gov.hmcts.juror.api.moj.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ExpenseRatesDto {
    @NotNull
    @Min(0)
    private BigDecimal carMileageRatePerMile0Passengers;
    @NotNull
    @Min(0)
    private BigDecimal carMileageRatePerMile1Passengers;
    @NotNull
    @Min(0)
    private BigDecimal carMileageRatePerMile2OrMorePassengers;

    @NotNull
    @Min(0)
    private BigDecimal motorcycleMileageRatePerMile0Passengers;
    @NotNull
    @Min(0)
    private BigDecimal motorcycleMileageRatePerMile1Passengers;

    @NotNull
    @Min(0)
    private BigDecimal bikeRate;

    @NotNull
    @Min(0)
    private BigDecimal limitFinancialLossHalfDay;
    @NotNull
    @Min(0)
    private BigDecimal limitFinancialLossFullDay;
    @NotNull
    @Min(0)
    private BigDecimal limitFinancialLossHalfDayLongTrial;
    @NotNull
    @Min(0)
    private BigDecimal limitFinancialLossFullDayLongTrial;

    private BigDecimal subsistenceRateStandard;
    private BigDecimal subsistenceRateLongDay;

    public ExpenseRatesDto(ExpenseRates expenseRates) {
        this.carMileageRatePerMile0Passengers = expenseRates.getCarMileageRatePerMile0Passengers();
        this.carMileageRatePerMile1Passengers = expenseRates.getCarMileageRatePerMile1Passengers();
        this.carMileageRatePerMile2OrMorePassengers = expenseRates.getCarMileageRatePerMile2OrMorePassengers();
        this.motorcycleMileageRatePerMile0Passengers = expenseRates.getMotorcycleMileageRatePerMile0Passengers();
        this.motorcycleMileageRatePerMile1Passengers = expenseRates.getMotorcycleMileageRatePerMile1Passengers();
        this.bikeRate = expenseRates.getBikeRate();
        this.limitFinancialLossHalfDay = expenseRates.getLimitFinancialLossHalfDay();
        this.limitFinancialLossFullDay = expenseRates.getLimitFinancialLossFullDay();
        this.limitFinancialLossHalfDayLongTrial = expenseRates.getLimitFinancialLossHalfDayLongTrial();
        this.limitFinancialLossFullDayLongTrial = expenseRates.getLimitFinancialLossFullDayLongTrial();
        this.subsistenceRateStandard = expenseRates.getSubsistenceRateStandard();
        this.subsistenceRateLongDay = expenseRates.getSubsistenceRateLongDay();
    }

    @JsonIgnore
    public ExpenseRates toEntity() {
        return ExpenseRates.builder()
            .carMileageRatePerMile0Passengers(carMileageRatePerMile0Passengers)
            .carMileageRatePerMile1Passengers(carMileageRatePerMile1Passengers)
            .carMileageRatePerMile2OrMorePassengers(carMileageRatePerMile2OrMorePassengers)
            .motorcycleMileageRatePerMile0Passengers(motorcycleMileageRatePerMile0Passengers)
            .motorcycleMileageRatePerMile1Passengers(motorcycleMileageRatePerMile1Passengers)
            .bikeRate(bikeRate)
            .limitFinancialLossHalfDay(limitFinancialLossHalfDay)
            .limitFinancialLossFullDay(limitFinancialLossFullDay)
            .limitFinancialLossHalfDayLongTrial(limitFinancialLossHalfDayLongTrial)
            .limitFinancialLossFullDayLongTrial(limitFinancialLossFullDayLongTrial)
            .subsistenceRateStandard(subsistenceRateStandard)
            .subsistenceRateLongDay(subsistenceRateLongDay)
            .build();
    }
}
