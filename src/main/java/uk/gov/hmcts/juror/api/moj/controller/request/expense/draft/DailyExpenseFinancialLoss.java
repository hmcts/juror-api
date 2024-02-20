package uk.gov.hmcts.juror.api.moj.controller.request.expense.draft;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class DailyExpenseFinancialLoss {

    @JsonProperty("loss_of_earnings")
    @Min(0)
    private BigDecimal lossOfEarningsOrBenefits;

    @JsonProperty("extra_care_cost")
    @Min(0)
    private BigDecimal extraCareCost;

    @JsonProperty("other_cost")
    @Min(0)
    private BigDecimal otherCosts;

    @JsonProperty("other_cost_description")
    private String otherCostsDescription;
}
