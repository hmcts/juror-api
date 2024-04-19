package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseDetailsWithTotalsDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.enumeration.PayAttendanceType;

import java.math.BigDecimal;

import static uk.gov.hmcts.juror.api.moj.utils.BigDecimalUtils.getOrZero;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
public class ExpenseDetailsForTotals extends ExpenseDetailsWithTotalsDto {

    private boolean financialLossApportionedApplied;

    private PayAttendanceType payAttendance;


    public ExpenseDetailsForTotals(Appearance appearance) {
        super(appearance);
        this.financialLossApportionedApplied = false;
        this.payAttendance = appearance.getPayAttendanceType();
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
}