package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CombinedSimplifiedExpenseDetailDtoTest {


    @Test
    void positiveCombinedSimplifiedExpenseDetailConstructor() {
        CombinedSimplifiedExpenseDetailDto combinedSimplifiedExpenseDetailDto =
            new CombinedSimplifiedExpenseDetailDto();
        assertThat(combinedSimplifiedExpenseDetailDto.getExpenseDetails()).isNotNull();
        assertThat(combinedSimplifiedExpenseDetailDto.getTotal()).isNotNull();
    }

    @Test
    void positiveAddExpenseDetail() {
        CombinedSimplifiedExpenseDetailDto combinedSimplifiedExpenseDetailDto =
            new CombinedSimplifiedExpenseDetailDto();
        CombinedSimplifiedExpenseDetailDto.Total total = mock(CombinedSimplifiedExpenseDetailDto.Total.class);
        combinedSimplifiedExpenseDetailDto.setTotal(total);
        SimplifiedExpenseDetailDto simplifiedExpenseDetailDto = mock(SimplifiedExpenseDetailDto.class);

        combinedSimplifiedExpenseDetailDto.addSimplifiedExpenseDetailDto(simplifiedExpenseDetailDto);
        verify(total, times(1)).add(simplifiedExpenseDetailDto);
        assertThat(combinedSimplifiedExpenseDetailDto.getExpenseDetails()).hasSize(1)
            .contains(simplifiedExpenseDetailDto);
    }

    @Nested
    class TotalTest {

        @Test
        void positiveTotalConstructor() {
            CombinedSimplifiedExpenseDetailDto.Total total = new CombinedSimplifiedExpenseDetailDto.Total();
            assertThat(total.getTotalAttendances()).isZero();
            assertThat(total.getFinancialLoss()).isZero();
            assertThat(total.getTravel()).isZero();
            assertThat(total.getFoodAndDrink()).isZero();
            assertThat(total.getSmartcard()).isZero();
            assertThat(total.getTotalDue()).isZero();
            assertThat(total.getTotalPaid()).isZero();
            assertThat(total.getBalanceToPay()).isZero();
        }

        @Test
        void positiveAdd() {
            final CombinedSimplifiedExpenseDetailDto.Total total = new CombinedSimplifiedExpenseDetailDto.Total();
            SimplifiedExpenseDetailDto simplifiedExpenseDetailDto = new SimplifiedExpenseDetailDto();
            simplifiedExpenseDetailDto.setFinancialLoss(new BigDecimal("20.01"));
            simplifiedExpenseDetailDto.setTravel(new BigDecimal("20.02"));
            simplifiedExpenseDetailDto.setSmartcard(new BigDecimal("20.03"));
            simplifiedExpenseDetailDto.setFoodAndDrink(new BigDecimal("20.04"));
            simplifiedExpenseDetailDto.setTotalDue(new BigDecimal("20.05"));
            simplifiedExpenseDetailDto.setTotalPaid(new BigDecimal("20.06"));
            simplifiedExpenseDetailDto.setBalanceToPay(new BigDecimal("20.07"));

            total.add(simplifiedExpenseDetailDto);
            assertThat(total.getTotalAttendances()).isEqualTo(1);
            assertThat(total.getFinancialLoss()).isEqualTo(new BigDecimal("20.01"));
            assertThat(total.getTravel()).isEqualTo(new BigDecimal("20.02"));
            assertThat(total.getSmartcard()).isEqualTo(new BigDecimal("20.03"));
            assertThat(total.getFoodAndDrink()).isEqualTo(new BigDecimal("20.04"));
            assertThat(total.getTotalDue()).isEqualTo(new BigDecimal("20.05"));
            assertThat(total.getTotalPaid()).isEqualTo(new BigDecimal("20.06"));
            assertThat(total.getBalanceToPay()).isEqualTo(new BigDecimal("20.07"));
            total.add(simplifiedExpenseDetailDto);
            assertThat(total.getTotalAttendances()).isEqualTo(2);
            assertThat(total.getFinancialLoss()).isEqualTo(new BigDecimal("40.02"));
            assertThat(total.getTravel()).isEqualTo(new BigDecimal("40.04"));
            assertThat(total.getSmartcard()).isEqualTo(new BigDecimal("40.06"));
            assertThat(total.getFoodAndDrink()).isEqualTo(new BigDecimal("40.08"));
            assertThat(total.getTotalDue()).isEqualTo(new BigDecimal("40.10"));
            assertThat(total.getTotalPaid()).isEqualTo(new BigDecimal("40.12"));
            assertThat(total.getBalanceToPay()).isEqualTo(new BigDecimal("40.14"));
        }
    }

}
