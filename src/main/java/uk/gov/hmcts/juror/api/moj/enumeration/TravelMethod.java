package uk.gov.hmcts.juror.api.moj.enumeration;

import org.apache.commons.lang3.function.TriFunction;
import uk.gov.hmcts.juror.api.moj.domain.ExpenseRates;

import java.math.BigDecimal;

public enum TravelMethod {
    CAR((expenseRates, jurorsByCar, jurorsByMotorcycle) -> {
        if (jurorsByCar == null || jurorsByCar <= 0) {
            return expenseRates.getCarMileageRatePerMile0Passengers();
        }
        if (jurorsByCar <= 1) {
            return expenseRates.getCarMileageRatePerMile1Passengers();
        }
        return expenseRates.getCarMileageRatePerMile2OrMorePassengers();
    }),
    MOTERCYCLE((expenseRates, jurorsByCar, jurorsByMotorcycle) -> {
        if (jurorsByMotorcycle == null || jurorsByMotorcycle <= 0) {
            return expenseRates.getMotorcycleMileageRatePerMile0Passengers();
        }
        return expenseRates.getMotorcycleMileageRatePerMile1Passengers();
    }),
    BICYCLE((expenseRates, jurorsByCar, jurorsByMotorcycle) -> expenseRates.getBikeRate());

    private final TriFunction<ExpenseRates, Integer, Integer, BigDecimal> getRateFunction;

    TravelMethod(TriFunction<ExpenseRates, Integer, Integer, BigDecimal> getRateFunction) {
        this.getRateFunction = getRateFunction;
    }

    public BigDecimal getRate(ExpenseRates expenseRates, Integer jurorsByCar, Integer jurorsByMotorcycle) {
        return getRateFunction.apply(expenseRates, jurorsByCar, jurorsByMotorcycle);
    }
}
