package uk.gov.hmcts.juror.api.moj.enumeration;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.domain.ExpenseRates;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class FoodDrinkClaimTypeTest {

    @Test
    void positiveGetRateNone() {
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        assertThat(FoodDrinkClaimType.NONE.getRate(expenseRates))
            .isEqualTo(BigDecimal.ZERO);
        verifyNoInteractions(expenseRates);
    }

    @Test
    void positiveGetRateMoreThan10Hours() {
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        BigDecimal rate = new BigDecimal("5.12");
        when(expenseRates.getSubsistenceRateLongDay()).thenReturn(rate);
        assertThat(FoodDrinkClaimType.MORE_THAN_10_HOURS.getRate(expenseRates))
            .isEqualTo(rate);
        verify(expenseRates, times(1)).getSubsistenceRateLongDay();
        verifyNoMoreInteractions(expenseRates);
    }
}
