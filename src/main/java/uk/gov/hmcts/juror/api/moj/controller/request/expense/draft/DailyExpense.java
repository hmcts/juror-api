package uk.gov.hmcts.juror.api.moj.controller.request.expense.draft;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.juror.api.moj.enumeration.PaymentMethod;
import uk.gov.hmcts.juror.api.validation.EnumValidator;
import uk.gov.hmcts.juror.api.validation.ValidateIf;
import uk.gov.hmcts.juror.api.validation.ValidateIfTrigger;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@ValidateIfTrigger(classToValidate = DailyExpense.class, groups = DailyExpense.CalculateTotals.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DailyExpense {

    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull(groups = {AttendanceDay.class, NonAttendanceDay.class, EditDay.class, CalculateTotals.class})
    private LocalDate dateOfExpense;


    @NotNull(groups = {AttendanceDay.class, NonAttendanceDay.class, EditDay.class})
    @ValidateIf(fields = {"time", "financialLoss", "travel", "foodAndDrink"},
        condition = ValidateIf.Condition.ANY_PRESENT,
        type = ValidateIf.Type.REQUIRE)
    private PaymentMethod paymentMethod;

    @Valid
    private DailyExpenseTime time;

    @Valid
    private DailyExpenseFinancialLoss financialLoss;

    @Null(groups = {NonAttendanceDay.class})
    @Valid
    private DailyExpenseTravel travel;

    @Null(groups = {NonAttendanceDay.class})
    @Valid
    private DailyExpenseFoodAndDrink foodAndDrink;

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

    public boolean shouldPullFromDatabase() {
        return paymentMethod == null
            && time == null
            && financialLoss == null
            && travel == null
            && foodAndDrink == null;
    }

    public interface AttendanceDay {

    }

    public interface NonAttendanceDay {

    }

    public interface EditDay {

    }

    public interface CalculateTotals {

    }
}
