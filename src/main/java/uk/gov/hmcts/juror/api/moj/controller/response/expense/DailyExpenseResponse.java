package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class DailyExpenseResponse {

    @JsonProperty("financial_loss_warning")
    private FinancialLossWarning financialLossWarning;

}
