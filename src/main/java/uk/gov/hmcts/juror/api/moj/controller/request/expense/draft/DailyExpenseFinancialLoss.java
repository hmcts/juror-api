package uk.gov.hmcts.juror.api.moj.controller.request.expense.draft;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.juror.api.validation.ExpenseNumericLimit;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class DailyExpenseFinancialLoss {

    @JsonProperty("loss_of_earnings")
    @ExpenseNumericLimit
    private BigDecimal lossOfEarningsOrBenefits;

    @JsonProperty("extra_care_cost")
    @ExpenseNumericLimit
    private BigDecimal extraCareCost;

    @JsonProperty("other_cost")
    @ExpenseNumericLimit
    private BigDecimal otherCosts;

    @JsonProperty("other_cost_description")
    private String otherCostsDescription;
}
