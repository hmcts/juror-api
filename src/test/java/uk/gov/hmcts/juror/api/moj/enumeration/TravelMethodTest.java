package uk.gov.hmcts.juror.api.moj.enumeration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.domain.ExpenseRates;

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
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        when(expenseRates.getCarMileageRatePerMile0Passengers()).thenReturn(rate);

        Assertions.assertThat(TravelMethod.CAR.getRate(expenseRates, null, 0))
            .isEqualTo(rate);
        verify(expenseRates, times(1)).getCarMileageRatePerMile0Passengers();
        verifyNoMoreInteractions(expenseRates);
    }

    @Test
    void getRateCar0Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        when(expenseRates.getCarMileageRatePerMile0Passengers()).thenReturn(rate);

        assertThat(TravelMethod.CAR.getRate(expenseRates, 0, 0))
            .isEqualTo(rate);
        verify(expenseRates, times(1)).getCarMileageRatePerMile0Passengers();
        verifyNoMoreInteractions(expenseRates);
    }

    @Test
    void getRateCar1Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        when(expenseRates.getCarMileageRatePerMile1Passengers()).thenReturn(rate);

        assertThat(TravelMethod.CAR.getRate(expenseRates, 1, null))
            .isEqualTo(rate);
        verify(expenseRates, times(1)).getCarMileageRatePerMile1Passengers();
        verifyNoMoreInteractions(expenseRates);
    }

    @Test
    void getRateCar2Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        when(expenseRates.getCarMileageRatePerMile2OrMorePassengers()).thenReturn(rate);

        assertThat(TravelMethod.CAR.getRate(expenseRates, 2, 0))
            .isEqualTo(rate);
        verify(expenseRates, times(1)).getCarMileageRatePerMile2OrMorePassengers();
        verifyNoMoreInteractions(expenseRates);
    }

    @Test
    void getRateCarMoreThan2Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        when(expenseRates.getCarMileageRatePerMile2OrMorePassengers()).thenReturn(rate);

        assertThat(TravelMethod.CAR.getRate(expenseRates, 5, 0))
            .isEqualTo(rate);
        verify(expenseRates, times(1)).getCarMileageRatePerMile2OrMorePassengers();
        verifyNoMoreInteractions(expenseRates);
    }

    @Test
    void getRateMotorCycleNullJurors() {
        BigDecimal rate = new BigDecimal("5.12");
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        when(expenseRates.getMotorcycleMileageRatePerMile0Passengers()).thenReturn(rate);

        assertThat(TravelMethod.MOTERCYCLE.getRate(expenseRates, 0, null))
            .isEqualTo(rate);
        verify(expenseRates, times(1)).getMotorcycleMileageRatePerMile0Passengers();
        verifyNoMoreInteractions(expenseRates);
    }

    @Test
    void getRateMotorCycle0Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        when(expenseRates.getMotorcycleMileageRatePerMile0Passengers()).thenReturn(rate);

        assertThat(TravelMethod.MOTERCYCLE.getRate(expenseRates, 0, 0))
            .isEqualTo(rate);
        verify(expenseRates, times(1)).getMotorcycleMileageRatePerMile0Passengers();
        verifyNoMoreInteractions(expenseRates);
    }

    @Test
    void getRateMotorCycle1Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        when(expenseRates.getMotorcycleMileageRatePerMile1Passengers()).thenReturn(rate);

        assertThat(TravelMethod.MOTERCYCLE.getRate(expenseRates, 0, 1))
            .isEqualTo(rate);
        verify(expenseRates, times(1)).getMotorcycleMileageRatePerMile1Passengers();
        verifyNoMoreInteractions(expenseRates);
    }

    @Test
    void getRateMotorCycleMoreThan1Jurors() {
        BigDecimal rate = new BigDecimal("5.12");
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        when(expenseRates.getMotorcycleMileageRatePerMile1Passengers()).thenReturn(rate);

        assertThat(TravelMethod.MOTERCYCLE.getRate(expenseRates, 0, 5))
            .isEqualTo(rate);

        verify(expenseRates, times(1)).getMotorcycleMileageRatePerMile1Passengers();
        verifyNoMoreInteractions(expenseRates);
    }

    @Test
    void getRateBike() {
        BigDecimal rate = new BigDecimal("5.12");
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        when(expenseRates.getBikeRate()).thenReturn(rate);

        assertThat(TravelMethod.BICYCLE.getRate(expenseRates, 0, 0))
            .isEqualTo(rate);
        verify(expenseRates, times(1)).getBikeRate();
        verifyNoMoreInteractions(expenseRates);
    }
}
