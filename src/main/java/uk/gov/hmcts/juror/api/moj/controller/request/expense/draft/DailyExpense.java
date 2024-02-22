package uk.gov.hmcts.juror.api.moj.controller.request.expense.draft;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.juror.api.validation.EnumValidator;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Data
@Builder
public class DailyExpense {

    @JsonProperty("date_of_expense")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(groups = {AttendanceDay.class, NonAttendanceDay.class, EditDay.class})
    private LocalDate dateOfExpense;

    @PoolNumber
    @JsonProperty("pool_number")
    @NotBlank(groups = {AttendanceDay.class, NonAttendanceDay.class, EditDay.class})
    private String poolNumber;

    @JsonProperty("pay_cash")
    @NotNull(groups = {AttendanceDay.class, NonAttendanceDay.class, EditDay.class})
    private Boolean payCash;

    @JsonProperty("time")
    @Valid
    private DailyExpenseTime time;

    @JsonProperty("financial_loss")
    @Valid
    private DailyExpenseFinancialLoss financialLoss;

    @JsonProperty("travel")
    @Null(groups = {NonAttendanceDay.class})
    @Valid
    private DailyExpenseTravel travel;

    @JsonProperty("food_and_drink")
    @Null(groups = {NonAttendanceDay.class})
    @Valid
    private DailyExpenseFoodAndDrink foodAndDrink;

    @JsonProperty("apply_to_days")
    @Valid
    @Null(groups = {EditDay.class})
    private List<
        @Valid
        @NotNull
        @EnumValidator(values = {
            "EXTRA_CARE_COSTS", "OTHER_COSTS", "PAY_CASH"},
            message = "Non Attendance day can only apply to all for [EXTRA_CARE_COSTS, OTHER_COSTS, PAY_CASH]",
            groups = NonAttendanceDay.class)
            DailyExpenseApplyToAllDays> applyToAllDays;

    @JsonIgnore
    public List<DailyExpenseApplyToAllDays> getApplyToAllDays() {
        if (applyToAllDays == null) {
            return List.of();
        }
        return Collections.unmodifiableList(applyToAllDays);
    }

    public interface AttendanceDay {

    }

    public interface NonAttendanceDay {

    }

    public interface EditDay {

    }
}
