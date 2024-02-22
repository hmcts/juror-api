package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ExpenseDetailsForTotalsTest {

    @Test
    void positiveGetTotalDue() {
        ExpenseDetailsForTotals expenseDetailsForTotals = new ExpenseDetailsForTotals();
        expenseDetailsForTotals.setTotalPaid(new BigDecimal("1.1"));
        assertThat(expenseDetailsForTotals.getTotalPaid()).isEqualTo(new BigDecimal("1.1"));
    }

    @Test
    void positiveGetTotalDueNull() {
        ExpenseDetailsForTotals expenseDetailsForTotals = new ExpenseDetailsForTotals();
        expenseDetailsForTotals.setTotalDue(null);
        assertThat(expenseDetailsForTotals.getTotalDue()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void positiveGetTotalPaid() {
        ExpenseDetailsForTotals expenseDetailsForTotals = new ExpenseDetailsForTotals();
        expenseDetailsForTotals.setTotalPaid(new BigDecimal("1.1"));
        assertThat(expenseDetailsForTotals.getTotalPaid()).isEqualTo(new BigDecimal("1.1"));
    }

    @Test
    void positiveGetTotalPaidNull() {
        ExpenseDetailsForTotals expenseDetailsForTotals = new ExpenseDetailsForTotals();
        expenseDetailsForTotals.setTotalPaid(null);
        assertThat(expenseDetailsForTotals.getTotalPaid()).isEqualTo(BigDecimal.ZERO);
    }

    @Test
    void positiveGetTotalFinancialLossApportioned() {
        ExpenseDetailsForTotals expenseDetailsForTotals = new ExpenseDetailsForTotals();
        expenseDetailsForTotals.setLossOfEarnings(new BigDecimal("1.1"));
        expenseDetailsForTotals.setExtraCare(new BigDecimal("1.2"));
        expenseDetailsForTotals.setOther(new BigDecimal("1.3"));
        assertThat(expenseDetailsForTotals.getTotalFinancialLossApportioned()).isEqualTo(new BigDecimal("3.6"));
    }

    @Test
    void positiveGetTotalTravelExpense() {
        ExpenseDetailsForTotals expenseDetailsForTotals = new ExpenseDetailsForTotals();
        expenseDetailsForTotals.setCar(new BigDecimal("1.1"));
        expenseDetailsForTotals.setMotorcycle(new BigDecimal("1.2"));
        expenseDetailsForTotals.setBicycle(new BigDecimal("1.3"));
        expenseDetailsForTotals.setParking(new BigDecimal("1.4"));
        assertThat(expenseDetailsForTotals.getTotalTravelExpense()).isEqualTo(new BigDecimal("5.0"));
    }


    @Test
    void positiveGetTotalOutstanding() {
        ExpenseDetailsForTotals expenseDetailsForTotals = new ExpenseDetailsForTotals();
        expenseDetailsForTotals.setTotalDue(new BigDecimal("4.1"));
        expenseDetailsForTotals.setTotalPaid(new BigDecimal("1.1"));
        assertThat(expenseDetailsForTotals.getTotalOutstanding()).isEqualTo(new BigDecimal("3.0"));
    }
}
