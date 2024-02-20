package uk.gov.hmcts.juror.api.moj.enumeration;

import org.apache.commons.lang3.function.TriFunction;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;

import java.math.BigDecimal;

public enum TravelMethod {
    CAR((courtLocation, jurorsByCar, jurorsByMotorcycle) -> {
        if (jurorsByCar == null || jurorsByCar <= 0) {
            return courtLocation.getCarMileageRatePerMile0Passengers();
        }
        if (jurorsByCar <= 1) {
            return courtLocation.getCarMileageRatePerMile1Passengers();
        }
        return courtLocation.getCarMileageRatePerMile2OrMorePassengers();
    }),
    MOTERCYCLE((courtLocation, jurorsByCar, jurorsByMotorcycle) -> {
        if (jurorsByMotorcycle == null || jurorsByMotorcycle <= 0) {
            return courtLocation.getMotorcycleMileageRatePerMile0Passengers();
        }
        return courtLocation.getMotorcycleMileageRatePerMile1Passengers();
    }),
    BICYCLE((courtLocation, jurorsByCar, jurorsByMotorcycle) -> courtLocation.getBikeRate());

    private final TriFunction<CourtLocation, Integer, Integer, BigDecimal> getRateFunction;

    TravelMethod(TriFunction<CourtLocation, Integer, Integer, BigDecimal> getRateFunction) {
        this.getRateFunction = getRateFunction;
    }

    public BigDecimal getRate(CourtLocation courtLocation, Integer jurorsByCar, Integer jurorsByMotorcycle) {
        return getRateFunction.apply(courtLocation, jurorsByCar, jurorsByMotorcycle);
    }
}
