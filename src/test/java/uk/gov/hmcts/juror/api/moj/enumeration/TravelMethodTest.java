package uk.gov.hmcts.juror.api.moj.enumeration;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.domain.ExpenseRates;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class TravelMethodTest {

    @Test
    void rateCarNullJurors() {
        BigDecimal rate = new BigDecimal("5.12");
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        when(expenseRates.getCarMileageRatePerMile0Passengers()).thenReturn(rate);

        assertThat(TravelMethod.CAR.getRate(expenseRates, null, 0))
            .isEqualTo(rate);
        verify(expenseRates, times(1)).getCarMileageRatePerMile0Passengers();
        verifyNoMoreInteractions(expenseRates);
    }

    @Test
    void rateCar0Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        when(expenseRates.getCarMileageRatePerMile0Passengers()).thenReturn(rate);

        assertThat(TravelMethod.CAR.getRate(expenseRates, 0, 0))
            .isEqualTo(rate);
        verify(expenseRates, times(1)).getCarMileageRatePerMile0Passengers();
        verifyNoMoreInteractions(expenseRates);
    }

    @Test
    void rateCar1Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        when(expenseRates.getCarMileageRatePerMile1Passengers()).thenReturn(rate);

        assertThat(TravelMethod.CAR.getRate(expenseRates, 1, null))
            .isEqualTo(rate);
        verify(expenseRates, times(1)).getCarMileageRatePerMile1Passengers();
        verifyNoMoreInteractions(expenseRates);
    }

    @Test
    void rateCar2Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        when(expenseRates.getCarMileageRatePerMile2OrMorePassengers()).thenReturn(rate);

        assertThat(TravelMethod.CAR.getRate(expenseRates, 2, 0))
            .isEqualTo(rate);
        verify(expenseRates, times(1)).getCarMileageRatePerMile2OrMorePassengers();
        verifyNoMoreInteractions(expenseRates);
    }

    @Test
    void rateCarMoreThan2Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        when(expenseRates.getCarMileageRatePerMile2OrMorePassengers()).thenReturn(rate);

        assertThat(TravelMethod.CAR.getRate(expenseRates, 5, 0))
            .isEqualTo(rate);
        verify(expenseRates, times(1)).getCarMileageRatePerMile2OrMorePassengers();
        verifyNoMoreInteractions(expenseRates);
    }

    @Test
    void rateMotorCycleNullJurors() {
        BigDecimal rate = new BigDecimal("5.12");
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        when(expenseRates.getMotorcycleMileageRatePerMile0Passengers()).thenReturn(rate);

        assertThat(TravelMethod.MOTERCYCLE.getRate(expenseRates, 0, null))
            .isEqualTo(rate);
        verify(expenseRates, times(1)).getMotorcycleMileageRatePerMile0Passengers();
        verifyNoMoreInteractions(expenseRates);
    }

    @Test
    void rateMotorCycle0Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        when(expenseRates.getMotorcycleMileageRatePerMile0Passengers()).thenReturn(rate);

        assertThat(TravelMethod.MOTERCYCLE.getRate(expenseRates, 0, 0))
            .isEqualTo(rate);
        verify(expenseRates, times(1)).getMotorcycleMileageRatePerMile0Passengers();
        verifyNoMoreInteractions(expenseRates);
    }

    @Test
    void rateMotorCycle1Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        when(expenseRates.getMotorcycleMileageRatePerMile1Passengers()).thenReturn(rate);

        assertThat(TravelMethod.MOTERCYCLE.getRate(expenseRates, 0, 1))
            .isEqualTo(rate);
        verify(expenseRates, times(1)).getMotorcycleMileageRatePerMile1Passengers();
        verifyNoMoreInteractions(expenseRates);
    }

    @Test
    void rateMotorCycleMoreThan1Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        when(expenseRates.getMotorcycleMileageRatePerMile1Passengers()).thenReturn(rate);

        assertThat(TravelMethod.MOTERCYCLE.getRate(expenseRates, 0, 5))
            .isEqualTo(rate);

        verify(expenseRates, times(1)).getMotorcycleMileageRatePerMile1Passengers();
        verifyNoMoreInteractions(expenseRates);
    }

    @Test
    void rateBike() {
        BigDecimal rate = new BigDecimal("5.12");
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        when(expenseRates.getBikeRate()).thenReturn(rate);

        assertThat(TravelMethod.BICYCLE.getRate(expenseRates, 0, 0))
            .isEqualTo(rate);
        verify(expenseRates, times(1)).getBikeRate();
        verifyNoMoreInteractions(expenseRates);
    }
}
