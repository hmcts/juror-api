package uk.gov.hmcts.juror.api.moj.controller.response.expense;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseFinancialLoss;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseFoodAndDrink;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseTime;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseTravel;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GetEnteredExpenseResponse {

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfExpense;

    private Boolean noneAttendanceDay;

    private AppearanceStage stage;

    private BigDecimal totalDue;

    private BigDecimal totalPaid;

    private PaymentMethod paymentMethod;

    private DailyExpenseTimeEntered time;

    private DailyExpenseFinancialLoss financialLoss;

    private DailyExpenseTravel travel;

    private DailyExpenseFoodAndDrink foodAndDrink;


    @SuperBuilder
    @Getter
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DailyExpenseTimeEntered extends DailyExpenseTime {
        @JsonFormat(pattern = "HH:mm")
        private LocalTime timeSpentAtCourt;
    }
}
