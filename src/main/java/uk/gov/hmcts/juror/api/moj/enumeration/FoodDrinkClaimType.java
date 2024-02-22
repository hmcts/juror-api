package uk.gov.hmcts.juror.api.moj.enumeration;

import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;

import java.math.BigDecimal;
import java.util.function.Function;

public enum FoodDrinkClaimType {
    NONE(courtLocation -> BigDecimal.ZERO),
    LESS_THAN_OR_EQUAL_TO_10_HOURS(CourtLocation::getSubsistenceRateStandard),
    MORE_THAN_10_HOURS(CourtLocation::getSubsistenceRateLongDay);

    private final Function<CourtLocation, BigDecimal> getRateFunction;

    FoodDrinkClaimType(Function<CourtLocation, BigDecimal> getRateFunction) {
        this.getRateFunction = getRateFunction;
    }

    public BigDecimal getRate(CourtLocation courtLocation) {
        return getRateFunction.apply(courtLocation);
    }
}
