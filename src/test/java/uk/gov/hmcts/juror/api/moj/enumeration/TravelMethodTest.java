package uk.gov.hmcts.juror.api.moj.enumeration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseTravel;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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

        DailyExpenseTravel dailyExpenseTravel = mock(DailyExpenseTravel.class);
        when(dailyExpenseTravel.getJurorsTakenCar()).thenReturn(null);

        Assertions.assertThat(TravelMethod.CAR.getRate(courtLocation, dailyExpenseTravel))
            .isEqualTo(rate);
        verify(courtLocation, times(1)).getCarMileageRatePerMile0Passengers();
        verify(dailyExpenseTravel, times(1)).getJurorsTakenCar();
        verifyNoMoreInteractions(courtLocation, dailyExpenseTravel);
    }

    @Test
    void getRateCar0Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocation.getCarMileageRatePerMile0Passengers()).thenReturn(rate);

        DailyExpenseTravel dailyExpenseTravel = mock(DailyExpenseTravel.class);
        when(dailyExpenseTravel.getJurorsTakenCar()).thenReturn(0);

        assertThat(TravelMethod.CAR.getRate(courtLocation, dailyExpenseTravel))
            .isEqualTo(rate);
        verify(courtLocation, times(1)).getCarMileageRatePerMile0Passengers();
        verify(dailyExpenseTravel, times(2)).getJurorsTakenCar();
        verifyNoMoreInteractions(courtLocation, dailyExpenseTravel);
    }

    @Test
    void getRateCar1Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocation.getCarMileageRatePerMile1Passengers()).thenReturn(rate);

        DailyExpenseTravel dailyExpenseTravel = mock(DailyExpenseTravel.class);
        when(dailyExpenseTravel.getJurorsTakenCar()).thenReturn(1);

        assertThat(TravelMethod.CAR.getRate(courtLocation, dailyExpenseTravel))
            .isEqualTo(rate);
        verify(courtLocation, times(1)).getCarMileageRatePerMile1Passengers();
        verify(dailyExpenseTravel, times(3)).getJurorsTakenCar();
        verifyNoMoreInteractions(courtLocation, dailyExpenseTravel);
    }

    @Test
    void getRateCar2Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocation.getCarMileageRatePerMile2OrMorePassengers()).thenReturn(rate);

        DailyExpenseTravel dailyExpenseTravel = mock(DailyExpenseTravel.class);
        when(dailyExpenseTravel.getJurorsTakenCar()).thenReturn(2);

        assertThat(TravelMethod.CAR.getRate(courtLocation, dailyExpenseTravel))
            .isEqualTo(rate);
        verify(courtLocation, times(1)).getCarMileageRatePerMile2OrMorePassengers();
        verify(dailyExpenseTravel, times(3)).getJurorsTakenCar();
        verifyNoMoreInteractions(courtLocation, dailyExpenseTravel);
    }

    @Test
    void getRateCarMoreThan2Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocation.getCarMileageRatePerMile2OrMorePassengers()).thenReturn(rate);

        DailyExpenseTravel dailyExpenseTravel = mock(DailyExpenseTravel.class);
        when(dailyExpenseTravel.getJurorsTakenCar()).thenReturn(5);

        assertThat(TravelMethod.CAR.getRate(courtLocation, dailyExpenseTravel))
            .isEqualTo(rate);
        verify(courtLocation, times(1)).getCarMileageRatePerMile2OrMorePassengers();
        verify(dailyExpenseTravel, times(3)).getJurorsTakenCar();
        verifyNoMoreInteractions(courtLocation, dailyExpenseTravel);
    }

    @Test
    void getRateMotorCycleNullJurors() {
        BigDecimal rate = new BigDecimal("5.12");
        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocation.getMotorcycleMileageRatePerMile0Passengers()).thenReturn(rate);

        DailyExpenseTravel dailyExpenseTravel = mock(DailyExpenseTravel.class);
        when(dailyExpenseTravel.getJurorsTakenCar()).thenReturn(null);

        assertThat(TravelMethod.MOTERCYCLE.getRate(courtLocation, dailyExpenseTravel))
            .isEqualTo(rate);
        verify(courtLocation, times(1)).getMotorcycleMileageRatePerMile0Passengers();
        verify(dailyExpenseTravel, times(2)).getJurorsTakenMotorcycle();
        verifyNoMoreInteractions(courtLocation, dailyExpenseTravel);
    }

    @Test
    void getRateMotorCycle0Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocation.getMotorcycleMileageRatePerMile0Passengers()).thenReturn(rate);

        DailyExpenseTravel dailyExpenseTravel = mock(DailyExpenseTravel.class);
        when(dailyExpenseTravel.getJurorsTakenMotorcycle()).thenReturn(0);

        assertThat(TravelMethod.MOTERCYCLE.getRate(courtLocation, dailyExpenseTravel))
            .isEqualTo(rate);
        verify(courtLocation, times(1)).getMotorcycleMileageRatePerMile0Passengers();
        verify(dailyExpenseTravel, times(2)).getJurorsTakenMotorcycle();
        verifyNoMoreInteractions(courtLocation, dailyExpenseTravel);
    }

    @Test
    void getRateMotorCycle1Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocation.getMotorcycleMileageRatePerMile1Passengers()).thenReturn(rate);

        DailyExpenseTravel dailyExpenseTravel = mock(DailyExpenseTravel.class);
        when(dailyExpenseTravel.getJurorsTakenMotorcycle()).thenReturn(1);

        assertThat(TravelMethod.MOTERCYCLE.getRate(courtLocation, dailyExpenseTravel))
            .isEqualTo(rate);
        verify(courtLocation, times(1)).getMotorcycleMileageRatePerMile1Passengers();
        verify(dailyExpenseTravel, times(2)).getJurorsTakenMotorcycle();
        verifyNoMoreInteractions(courtLocation, dailyExpenseTravel);
    }

    @Test
    void getRateMotorCycleMoreThan1Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocation.getMotorcycleMileageRatePerMile1Passengers()).thenReturn(rate);

        DailyExpenseTravel dailyExpenseTravel = mock(DailyExpenseTravel.class);
        when(dailyExpenseTravel.getJurorsTakenMotorcycle()).thenReturn(5);

        assertThat(TravelMethod.MOTERCYCLE.getRate(courtLocation, dailyExpenseTravel))
            .isEqualTo(rate);
        verify(courtLocation, times(1)).getMotorcycleMileageRatePerMile1Passengers();
        verify(dailyExpenseTravel, times(2)).getJurorsTakenMotorcycle();
        verifyNoMoreInteractions(courtLocation, dailyExpenseTravel);
    }

    @Test
    void getRateBike() {
        BigDecimal rate = new BigDecimal("5.12");
        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocation.getBikeRate()).thenReturn(rate);
        DailyExpenseTravel dailyExpenseTravel = mock(DailyExpenseTravel.class);

        assertThat(TravelMethod.BICYCLE.getRate(courtLocation, dailyExpenseTravel))
            .isEqualTo(rate);
        verify(courtLocation, times(1)).getBikeRate();
        verifyNoMoreInteractions(courtLocation);
        verifyNoInteractions(dailyExpenseTravel);
    }

}
