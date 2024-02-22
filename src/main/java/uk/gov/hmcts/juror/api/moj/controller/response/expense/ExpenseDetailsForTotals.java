package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.enumeration.PayAttendanceType;

import java.math.BigDecimal;

import static uk.gov.hmcts.juror.api.moj.utils.BigDecimalUtils.getOrZero;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ExpenseDetailsForTotals extends ExpenseDetailsDto {

    private boolean financialLossApportionedApplied;

    private PayAttendanceType payAttendance;
    @NotNull
    private BigDecimal totalDue;
    @NotNull
    private BigDecimal totalPaid;

    @JsonProperty("total_due")
    public BigDecimal getTotalDue() {
        if (totalDue == null) {
            return BigDecimal.ZERO;
        }
        return totalDue;
    }

    @JsonProperty("total_paid")
    public BigDecimal getTotalPaid() {
        if (totalPaid == null) {
            return BigDecimal.ZERO;
        }
        return totalPaid;
    }

    @JsonProperty("total_financial_loss_apportioned")
    public BigDecimal getTotalFinancialLossApportioned() {
        return getLossOfEarnings()
            .add(getExtraCare())
            .add(getOther());
    }

    @JsonProperty("total_travel_expenses")
    public BigDecimal getTotalTravelExpense() {
        return getOrZero(getCar())
            .add(getMotorcycle())
            .add(getBicycle())
            .add(getParking())
            .add(getPublicTransport())
            .add(getTaxi());
    }

    @JsonProperty("total_outstanding")
    @NotNull
    public BigDecimal getTotalOutstanding() {
        return getTotalDue()
            .subtract(getTotalPaid());
    }
}