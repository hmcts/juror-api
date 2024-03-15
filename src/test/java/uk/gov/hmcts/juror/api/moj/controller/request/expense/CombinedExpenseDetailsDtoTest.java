package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.ExpenseDetailsForTotals;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CombinedExpenseDetailsDtoTest {

    @Test
    void positiveCombinedExpenseDetailsDtoConstructor() {
        CombinedExpenseDetailsDto<ExpenseDetailsDto> combinedExpenseDetailsDto = new CombinedExpenseDetailsDto<>();
        assertThat(combinedExpenseDetailsDto.getExpenseDetails()).isNotNull().isEmpty();
        assertThat(combinedExpenseDetailsDto.getTotal()).isNotNull();
    }

    @Test
    void positiveAddExpenseDetail() {
        CombinedExpenseDetailsDto<ExpenseDetailsDto> combinedExpenseDetailsDto = new CombinedExpenseDetailsDto<>();
        CombinedExpenseDetailsDto.Total total = mock(CombinedExpenseDetailsDto.Total.class);
        combinedExpenseDetailsDto.setTotal(total);
        ExpenseDetailsDto expenseDetailsDto = mock(ExpenseDetailsDto.class);

        combinedExpenseDetailsDto.addExpenseDetail(expenseDetailsDto);
        verify(total, times(1)).add(expenseDetailsDto);
        assertThat(combinedExpenseDetailsDto.getExpenseDetails()).hasSize(1).contains(expenseDetailsDto);
    }


    @Nested
    class TotalTest {

        @Test
        void positiveTotalConstructor() {
            CombinedExpenseDetailsDto.Total total = new CombinedExpenseDetailsDto.Total(false);
            assertThat(total.getTotalDays()).isEqualTo(0);
            assertThat(total.getLossOfEarnings()).isEqualTo(BigDecimal.ZERO);
            assertThat(total.getExtraCare()).isEqualTo(BigDecimal.ZERO);
            assertThat(total.getOther()).isEqualTo(BigDecimal.ZERO);
            assertThat(total.getPublicTransport()).isEqualTo(BigDecimal.ZERO);
            assertThat(total.getTaxi()).isEqualTo(BigDecimal.ZERO);
            assertThat(total.getMotorcycle()).isEqualTo(BigDecimal.ZERO);
            assertThat(total.getCar()).isEqualTo(BigDecimal.ZERO);
            assertThat(total.getBicycle()).isEqualTo(BigDecimal.ZERO);
            assertThat(total.getParking()).isEqualTo(BigDecimal.ZERO);
            assertThat(total.getFoodAndDrink()).isEqualTo(BigDecimal.ZERO);
            assertThat(total.getSmartCard()).isEqualTo(BigDecimal.ZERO);
            assertThat(total.getTotal()).isEqualTo(BigDecimal.ZERO);
        }

        @Test
        void positiveAdd() {
            final CombinedExpenseDetailsDto.Total total = new CombinedExpenseDetailsDto.Total(false);
            ExpenseDetailsDto expenseDetailsDto = new ExpenseDetailsDto();
            expenseDetailsDto.setLossOfEarnings(new BigDecimal("20.01"));
            expenseDetailsDto.setExtraCare(new BigDecimal("20.02"));
            expenseDetailsDto.setOther(new BigDecimal("20.03"));
            expenseDetailsDto.setPublicTransport(new BigDecimal("20.04"));
            expenseDetailsDto.setTaxi(new BigDecimal("20.05"));
            expenseDetailsDto.setMotorcycle(new BigDecimal("20.06"));
            expenseDetailsDto.setCar(new BigDecimal("20.07"));
            expenseDetailsDto.setBicycle(new BigDecimal("20.08"));
            expenseDetailsDto.setParking(new BigDecimal("20.09"));
            expenseDetailsDto.setFoodAndDrink(new BigDecimal("20.10"));
            expenseDetailsDto.setSmartCard(new BigDecimal("20.11"));
            assertThat(total.getTotalDue()).isNull();
            assertThat(total.getTotalPaid()).isNull();
            assertThat(total.getTotalOutstanding()).isNull();

            total.add(expenseDetailsDto);
            assertThat(total.getTotalDays()).isEqualTo(1);
            assertThat(total.getLossOfEarnings()).isEqualTo(new BigDecimal("20.01"));
            assertThat(total.getExtraCare()).isEqualTo(new BigDecimal("20.02"));
            assertThat(total.getOther()).isEqualTo(new BigDecimal("20.03"));
            assertThat(total.getPublicTransport()).isEqualTo(new BigDecimal("20.04"));
            assertThat(total.getTaxi()).isEqualTo(new BigDecimal("20.05"));
            assertThat(total.getMotorcycle()).isEqualTo(new BigDecimal("20.06"));
            assertThat(total.getCar()).isEqualTo(new BigDecimal("20.07"));
            assertThat(total.getBicycle()).isEqualTo(new BigDecimal("20.08"));
            assertThat(total.getParking()).isEqualTo(new BigDecimal("20.09"));
            assertThat(total.getFoodAndDrink()).isEqualTo(new BigDecimal("20.10"));
            assertThat(total.getSmartCard()).isEqualTo(new BigDecimal("20.11"));

            assertThat(total.getTotal()).isEqualTo(new BigDecimal("180.44"));
            assertThat(total.getTotalDue()).isNull();
            assertThat(total.getTotalPaid()).isNull();
            assertThat(total.getTotalOutstanding()).isNull();

            total.add(expenseDetailsDto);
            assertThat(total.getTotalDays()).isEqualTo(2);
            assertThat(total.getLossOfEarnings()).isEqualTo(new BigDecimal("40.02"));
            assertThat(total.getExtraCare()).isEqualTo(new BigDecimal("40.04"));
            assertThat(total.getOther()).isEqualTo(new BigDecimal("40.06"));
            assertThat(total.getPublicTransport()).isEqualTo(new BigDecimal("40.08"));
            assertThat(total.getTaxi()).isEqualTo(new BigDecimal("40.10"));
            assertThat(total.getMotorcycle()).isEqualTo(new BigDecimal("40.12"));
            assertThat(total.getCar()).isEqualTo(new BigDecimal("40.14"));
            assertThat(total.getBicycle()).isEqualTo(new BigDecimal("40.16"));
            assertThat(total.getParking()).isEqualTo(new BigDecimal("40.18"));
            assertThat(total.getFoodAndDrink()).isEqualTo(new BigDecimal("40.20"));
            assertThat(total.getSmartCard()).isEqualTo(new BigDecimal("40.22"));
            assertThat(total.getTotal()).isEqualTo(new BigDecimal("360.88"));
            assertThat(total.getTotalDue()).isNull();
            assertThat(total.getTotalPaid()).isNull();
            assertThat(total.getTotalOutstanding()).isNull();
        }

        @Test
        void positiveAddWithTotals() {
            final CombinedExpenseDetailsDto.Total total = new CombinedExpenseDetailsDto.Total(true);
            ExpenseDetailsForTotals expenseDetailsDto = new ExpenseDetailsForTotals();
            expenseDetailsDto.setLossOfEarnings(new BigDecimal("20.01"));
            expenseDetailsDto.setExtraCare(new BigDecimal("20.02"));
            expenseDetailsDto.setOther(new BigDecimal("20.03"));
            expenseDetailsDto.setPublicTransport(new BigDecimal("20.04"));
            expenseDetailsDto.setTaxi(new BigDecimal("20.05"));
            expenseDetailsDto.setMotorcycle(new BigDecimal("20.06"));
            expenseDetailsDto.setCar(new BigDecimal("20.07"));
            expenseDetailsDto.setBicycle(new BigDecimal("20.08"));
            expenseDetailsDto.setParking(new BigDecimal("20.09"));
            expenseDetailsDto.setFoodAndDrink(new BigDecimal("20.10"));
            expenseDetailsDto.setSmartCard(new BigDecimal("20.11"));
            expenseDetailsDto.setTotalDue(new BigDecimal("20.12"));
            expenseDetailsDto.setTotalPaid(new BigDecimal("10.13"));

            assertThat(total.getTotalDue()).isEqualTo(BigDecimal.ZERO);
            assertThat(total.getTotalPaid()).isEqualTo(BigDecimal.ZERO);
            assertThat(total.getTotalOutstanding()).isEqualTo(BigDecimal.ZERO);

            total.add(expenseDetailsDto);
            assertThat(total.getTotalDays()).isEqualTo(1);
            assertThat(total.getLossOfEarnings()).isEqualTo(new BigDecimal("20.01"));
            assertThat(total.getExtraCare()).isEqualTo(new BigDecimal("20.02"));
            assertThat(total.getOther()).isEqualTo(new BigDecimal("20.03"));
            assertThat(total.getPublicTransport()).isEqualTo(new BigDecimal("20.04"));
            assertThat(total.getTaxi()).isEqualTo(new BigDecimal("20.05"));
            assertThat(total.getMotorcycle()).isEqualTo(new BigDecimal("20.06"));
            assertThat(total.getCar()).isEqualTo(new BigDecimal("20.07"));
            assertThat(total.getBicycle()).isEqualTo(new BigDecimal("20.08"));
            assertThat(total.getParking()).isEqualTo(new BigDecimal("20.09"));
            assertThat(total.getFoodAndDrink()).isEqualTo(new BigDecimal("20.10"));
            assertThat(total.getSmartCard()).isEqualTo(new BigDecimal("20.11"));

            assertThat(total.getTotal()).isEqualTo(new BigDecimal("180.44"));
            assertThat(total.getTotalDue()).isEqualTo(new BigDecimal("20.12"));
            assertThat(total.getTotalPaid()).isEqualTo(new BigDecimal("10.13"));
            assertThat(total.getTotalOutstanding()).isEqualTo(new BigDecimal("9.99"));

            total.add(expenseDetailsDto);
            assertThat(total.getTotalDays()).isEqualTo(2);
            assertThat(total.getLossOfEarnings()).isEqualTo(new BigDecimal("40.02"));
            assertThat(total.getExtraCare()).isEqualTo(new BigDecimal("40.04"));
            assertThat(total.getOther()).isEqualTo(new BigDecimal("40.06"));
            assertThat(total.getPublicTransport()).isEqualTo(new BigDecimal("40.08"));
            assertThat(total.getTaxi()).isEqualTo(new BigDecimal("40.10"));
            assertThat(total.getMotorcycle()).isEqualTo(new BigDecimal("40.12"));
            assertThat(total.getCar()).isEqualTo(new BigDecimal("40.14"));
            assertThat(total.getBicycle()).isEqualTo(new BigDecimal("40.16"));
            assertThat(total.getParking()).isEqualTo(new BigDecimal("40.18"));
            assertThat(total.getFoodAndDrink()).isEqualTo(new BigDecimal("40.20"));
            assertThat(total.getSmartCard()).isEqualTo(new BigDecimal("40.22"));
            assertThat(total.getTotal()).isEqualTo(new BigDecimal("360.88"));

            assertThat(total.getTotalDue()).isEqualTo(new BigDecimal("40.24"));
            assertThat(total.getTotalPaid()).isEqualTo(new BigDecimal("20.26"));
            assertThat(total.getTotalOutstanding()).isEqualTo(new BigDecimal("19.98"));
        }


        @Test
        void positiveGetTotalOutstandingDoesNotHaveTotals() {
            final CombinedExpenseDetailsDto.Total total = spy(new CombinedExpenseDetailsDto.Total(false));
            doReturn(new BigDecimal("20.00")).when(total).getTotalDue();
            doReturn(new BigDecimal("10.00")).when(total).getTotalPaid();
            assertThat(total.getTotalOutstanding()).isNull();
        }

        @Test
        void positiveGetTotalOutstandingHasTotals() {
            final CombinedExpenseDetailsDto.Total total = spy(new CombinedExpenseDetailsDto.Total(true));
            doReturn(new BigDecimal("25.00")).when(total).getTotalDue();
            doReturn(new BigDecimal("10.00")).when(total).getTotalPaid();
            assertThat(total.getTotalOutstanding()).isEqualTo(new BigDecimal("15.00"));
        }

        @Test
        void positiveGetTotalDueDoesNotHaveTotals() {
            final CombinedExpenseDetailsDto.Total total = spy(new CombinedExpenseDetailsDto.Total(false));
            total.setTotalDue(new BigDecimal("20.00"));
            assertThat(total.getTotalDue()).isNull();

        }

        @Test
        void positiveGetTotalDueHasTotals() {
            final CombinedExpenseDetailsDto.Total total = spy(new CombinedExpenseDetailsDto.Total(true));
            total.setTotalDue(new BigDecimal("20.00"));
            assertThat(total.getTotalDue()).isEqualTo(new BigDecimal("20.00"));
        }

        @Test
        void positiveGetTotalPaidDoesNotHaveTotals() {
            final CombinedExpenseDetailsDto.Total total = spy(new CombinedExpenseDetailsDto.Total(false));
            total.setTotalPaid(new BigDecimal("10.00"));
            assertThat(total.getTotalPaid()).isNull();
        }

        @Test
        void positiveGetTotalPaidHasTotals() {
            final CombinedExpenseDetailsDto.Total total = spy(new CombinedExpenseDetailsDto.Total(true));
            total.setTotalPaid(new BigDecimal("10.00"));
            assertThat(total.getTotalPaid()).isEqualTo(new BigDecimal("10.00"));
        }
    }
}
