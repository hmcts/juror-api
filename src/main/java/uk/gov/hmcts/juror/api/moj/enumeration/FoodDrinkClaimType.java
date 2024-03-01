package uk.gov.hmcts.juror.api.moj.enumeration;

import uk.gov.hmcts.juror.api.moj.domain.ExpenseRates;

import java.math.BigDecimal;
import java.util.function.Function;

public enum FoodDrinkClaimType {
    NONE(courtLocation -> BigDecimal.ZERO),
    LESS_THAN_OR_EQUAL_TO_10_HOURS(ExpenseRates::getSubsistenceRateStandard),
    MORE_THAN_10_HOURS(ExpenseRates::getSubsistenceRateLongDay);

    private final Function<ExpenseRates, BigDecimal> getRateFunction;

    FoodDrinkClaimType(Function<ExpenseRates, BigDecimal> getRateFunction) {
        this.getRateFunction = getRateFunction;
    }

    public BigDecimal getRate(ExpenseRates expenseRates) {
        return getRateFunction.apply(expenseRates);
    }
}
