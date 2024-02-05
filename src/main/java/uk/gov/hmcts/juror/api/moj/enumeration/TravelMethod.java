package uk.gov.hmcts.juror.api.moj.enumeration;

import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseTravel;

import java.math.BigDecimal;
import java.util.function.BiFunction;

public enum TravelMethod {
    CAR((courtLocation, travel1) -> {
        if (travel1.getJurorsTakenCar() == null || travel1.getJurorsTakenCar() <= 0) {
            return courtLocation.getCarMileageRatePerMile0Passengers();
        }
        if (travel1.getJurorsTakenCar() <= 1) {
            return courtLocation.getCarMileageRatePerMile1Passengers();
        }
        return courtLocation.getCarMileageRatePerMile2OrMorePassengers();
    }),
    MOTERCYCLE((courtLocation, travel1) -> {
        if (travel1.getJurorsTakenMotorcycle() == null || travel1.getJurorsTakenMotorcycle() <= 0) {
            return courtLocation.getMotorcycleMileageRatePerMile0Passengers();
        }
        return courtLocation.getMotorcycleMileageRatePerMile1Passengers();
    }),
    BICYCLE((courtLocation, travel1) -> courtLocation.getBikeRate());

    private final BiFunction<CourtLocation, DailyExpenseTravel, BigDecimal> getRateFunction;

    TravelMethod(BiFunction<CourtLocation, DailyExpenseTravel, BigDecimal> getRateFunction) {
        this.getRateFunction = getRateFunction;
    }


    public BigDecimal getRate(CourtLocation courtLocation, DailyExpenseTravel travel) {
        return getRateFunction.apply(courtLocation, travel);
    }
}
