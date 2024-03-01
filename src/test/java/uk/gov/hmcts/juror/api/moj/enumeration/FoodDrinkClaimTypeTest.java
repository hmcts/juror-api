package uk.gov.hmcts.juror.api.moj.enumeration;

import org.assertj.core.api.Assertions;
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
    void getRateNone() {
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        Assertions.assertThat(FoodDrinkClaimType.NONE.getRate(expenseRates))
            .isEqualTo(BigDecimal.ZERO);
        verifyNoInteractions(expenseRates);
    }

    @Test
    void getRateMoreThan10Hours() {
        ExpenseRates expenseRates = mock(ExpenseRates.class);
        BigDecimal rate = new BigDecimal("5.12");
        when(expenseRates.getSubsistenceRateLongDay()).thenReturn(rate);
        assertThat(FoodDrinkClaimType.MORE_THAN_10_HOURS.getRate(expenseRates))
            .isEqualTo(rate);
        verify(expenseRates, times(1)).getSubsistenceRateLongDay();
        verifyNoMoreInteractions(expenseRates);
    }
}
