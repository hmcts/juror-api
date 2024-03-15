package uk.gov.hmcts.juror.api.moj.controller.request.expense;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpense;
import uk.gov.hmcts.juror.api.validation.JurorNumber;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CalculateTotalExpenseRequestDto {

    @JurorNumber(groups = DailyExpense.CalculateTotals.class)
    @NotBlank(groups = DailyExpense.CalculateTotals.class)
    private String jurorNumber;

    @PoolNumber
    @JsonProperty("pool_number")
    @NotBlank(groups = {DailyExpense.CalculateTotals.class})
    private String poolNumber;

    @NotEmpty(groups = DailyExpense.EditDay.class)
    private List<@Valid @NotNull(groups = DailyExpense.CalculateTotals.class) DailyExpense> expenseList;
}
