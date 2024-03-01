package uk.gov.hmcts.juror.api.moj.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ExpenseRatesDtoTest {

    @Test
    void positiveConstructorTest() {
        ExpenseRates expenseRates = ExpenseRates.builder()
            .carMileageRatePerMile0Passengers(new BigDecimal("1.01"))
            .carMileageRatePerMile1Passengers(new BigDecimal("1.02"))
            .carMileageRatePerMile2OrMorePassengers(new BigDecimal("1.03"))
            .motorcycleMileageRatePerMile0Passengers(new BigDecimal("1.04"))
            .motorcycleMileageRatePerMile1Passengers(new BigDecimal("1.05"))
            .bikeRate(new BigDecimal("1.06"))
            .limitFinancialLossHalfDay(new BigDecimal("1.07"))
            .limitFinancialLossFullDay(new BigDecimal("1.08"))
            .limitFinancialLossHalfDayLongTrial(new BigDecimal("1.09"))
            .limitFinancialLossFullDayLongTrial(new BigDecimal("1.10"))
            .subsistenceRateStandard(new BigDecimal("1.11"))
            .subsistenceRateLongDay(new BigDecimal("1.12"))
            .build();

        assertThat(new ExpenseRatesDto(expenseRates)).isEqualTo(
            ExpenseRatesDto.builder()
                .carMileageRatePerMile0Passengers(new BigDecimal("1.01"))
                .carMileageRatePerMile1Passengers(new BigDecimal("1.02"))
                .carMileageRatePerMile2OrMorePassengers(new BigDecimal("1.03"))
                .motorcycleMileageRatePerMile0Passengers(new BigDecimal("1.04"))
                .motorcycleMileageRatePerMile1Passengers(new BigDecimal("1.05"))
                .bikeRate(new BigDecimal("1.06"))
                .limitFinancialLossHalfDay(new BigDecimal("1.07"))
                .limitFinancialLossFullDay(new BigDecimal("1.08"))
                .limitFinancialLossHalfDayLongTrial(new BigDecimal("1.09"))
                .limitFinancialLossFullDayLongTrial(new BigDecimal("1.10"))
                .subsistenceRateStandard(new BigDecimal("1.11"))
                .subsistenceRateLongDay(new BigDecimal("1.12"))
                .build()
        );
    }
}
