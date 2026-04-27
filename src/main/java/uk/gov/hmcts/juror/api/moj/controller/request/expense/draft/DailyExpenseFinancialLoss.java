package uk.gov.hmcts.juror.api.moj.controller.request.expense.draft;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.juror.api.validation.ExpenseNumericLimit;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DailyExpenseFinancialLoss {

    @ExpenseNumericLimit
    private BigDecimal lossOfEarningsOrBenefits;

    @ExpenseNumericLimit
    private BigDecimal extraCareCost;

    @ExpenseNumericLimit
    private BigDecimal otherCosts;

    private String otherCostsDescription;
}
