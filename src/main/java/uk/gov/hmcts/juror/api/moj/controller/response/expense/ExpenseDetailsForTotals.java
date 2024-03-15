package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.HasTotals;
import uk.gov.hmcts.juror.api.moj.enumeration.PayAttendanceType;

import java.math.BigDecimal;
import java.util.Optional;

import static uk.gov.hmcts.juror.api.moj.utils.BigDecimalUtils.getOrZero;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class ExpenseDetailsForTotals extends ExpenseDetailsDto implements HasTotals {

    private boolean financialLossApportionedApplied;

    private PayAttendanceType payAttendance;
    @NotNull
    private BigDecimal totalDue;
    @NotNull
    private BigDecimal totalPaid;


    public ExpenseDetailsForTotals(Appearance appearance) {
        super(appearance);
        this.financialLossApportionedApplied = false;
        this.payAttendance = appearance.getPayAttendanceType();
        this.totalDue = appearance.getTotalDue();
        this.totalPaid = appearance.getTotalPaid();
    }

    @JsonProperty("total_due")
    @Override
    public BigDecimal getTotalDue() {
        return Optional.ofNullable(totalDue).orElse(BigDecimal.ZERO);
    }

    @JsonProperty("total_paid")
    @Override
    public BigDecimal getTotalPaid() {
        return Optional.ofNullable(totalPaid).orElse(BigDecimal.ZERO);
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