package uk.gov.hmcts.juror.api.moj.enumeration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings({
    "PMD.LinguisticNaming",
    "PMD.LawOfDemeter"
})
class TravelMethodTest {

    @Test
    void getRateCarNullJurors() {
        BigDecimal rate = new BigDecimal("5.12");
        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocation.getCarMileageRatePerMile0Passengers()).thenReturn(rate);

        Assertions.assertThat(TravelMethod.CAR.getRate(courtLocation, null, 0))
            .isEqualTo(rate);
        verify(courtLocation, times(1)).getCarMileageRatePerMile0Passengers();
        verifyNoMoreInteractions(courtLocation);
    }

    @Test
    void getRateCar0Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocation.getCarMileageRatePerMile0Passengers()).thenReturn(rate);

        assertThat(TravelMethod.CAR.getRate(courtLocation, 0, 0))
            .isEqualTo(rate);
        verify(courtLocation, times(1)).getCarMileageRatePerMile0Passengers();
        verifyNoMoreInteractions(courtLocation);
    }

    @Test
    void getRateCar1Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocation.getCarMileageRatePerMile1Passengers()).thenReturn(rate);

        assertThat(TravelMethod.CAR.getRate(courtLocation, 1, null))
            .isEqualTo(rate);
        verify(courtLocation, times(1)).getCarMileageRatePerMile1Passengers();
        verifyNoMoreInteractions(courtLocation);
    }

    @Test
    void getRateCar2Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocation.getCarMileageRatePerMile2OrMorePassengers()).thenReturn(rate);

        assertThat(TravelMethod.CAR.getRate(courtLocation, 2, 0))
            .isEqualTo(rate);
        verify(courtLocation, times(1)).getCarMileageRatePerMile2OrMorePassengers();
        verifyNoMoreInteractions(courtLocation);
    }

    @Test
    void getRateCarMoreThan2Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocation.getCarMileageRatePerMile2OrMorePassengers()).thenReturn(rate);

        assertThat(TravelMethod.CAR.getRate(courtLocation, 5, 0))
            .isEqualTo(rate);
        verify(courtLocation, times(1)).getCarMileageRatePerMile2OrMorePassengers();
        verifyNoMoreInteractions(courtLocation);
    }

    @Test
    void getRateMotorCycleNullJurors() {
        BigDecimal rate = new BigDecimal("5.12");
        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocation.getMotorcycleMileageRatePerMile0Passengers()).thenReturn(rate);

        assertThat(TravelMethod.MOTERCYCLE.getRate(courtLocation, 0, null))
            .isEqualTo(rate);
        verify(courtLocation, times(1)).getMotorcycleMileageRatePerMile0Passengers();
        verifyNoMoreInteractions(courtLocation);
    }

    @Test
    void getRateMotorCycle0Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocation.getMotorcycleMileageRatePerMile0Passengers()).thenReturn(rate);

        assertThat(TravelMethod.MOTERCYCLE.getRate(courtLocation, 0, 0))
            .isEqualTo(rate);
        verify(courtLocation, times(1)).getMotorcycleMileageRatePerMile0Passengers();
        verifyNoMoreInteractions(courtLocation);
    }

    @Test
    void getRateMotorCycle1Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocation.getMotorcycleMileageRatePerMile1Passengers()).thenReturn(rate);

        assertThat(TravelMethod.MOTERCYCLE.getRate(courtLocation, 0, 1))
            .isEqualTo(rate);
        verify(courtLocation, times(1)).getMotorcycleMileageRatePerMile1Passengers();
        verifyNoMoreInteractions(courtLocation);
    }

    @Test
    void getRateMotorCycleMoreThan1Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocation.getMotorcycleMileageRatePerMile1Passengers()).thenReturn(rate);

        assertThat(TravelMethod.MOTERCYCLE.getRate(courtLocation, 0, 5))
            .isEqualTo(rate);

        verify(courtLocation, times(1)).getMotorcycleMileageRatePerMile1Passengers();
        verifyNoMoreInteractions(courtLocation);
    }

    @Test
    void getRateBike() {
        BigDecimal rate = new BigDecimal("5.12");
        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocation.getBikeRate()).thenReturn(rate);

        assertThat(TravelMethod.BICYCLE.getRate(courtLocation, 0, 0))
            .isEqualTo(rate);
        verify(courtLocation, times(1)).getBikeRate();
        verifyNoMoreInteractions(courtLocation);
    }
}
