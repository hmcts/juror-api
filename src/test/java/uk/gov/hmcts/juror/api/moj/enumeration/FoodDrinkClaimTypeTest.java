package uk.gov.hmcts.juror.api.moj.enumeration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;

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
        CourtLocation courtLocation = mock(CourtLocation.class);
        Assertions.assertThat(FoodDrinkClaimType.NONE.getRate(courtLocation))
            .isEqualTo(BigDecimal.ZERO);
        verifyNoInteractions(courtLocation);
    }

    @Test
    void getRateMoreThan10Hours() {
        CourtLocation courtLocation = mock(CourtLocation.class);
        BigDecimal rate = new BigDecimal("5.12");
        when(courtLocation.getSubsistenceRateLongDay()).thenReturn(rate);
        assertThat(FoodDrinkClaimType.MORE_THAN_10_HOURS.getRate(courtLocation))
            .isEqualTo(rate);
        verify(courtLocation, times(1)).getSubsistenceRateLongDay();
        verifyNoMoreInteractions(courtLocation);
    }
}
