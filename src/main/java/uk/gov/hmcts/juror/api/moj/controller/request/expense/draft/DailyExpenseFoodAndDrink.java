package uk.gov.hmcts.juror.api.moj.controller.request.expense.draft;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.juror.api.moj.enumeration.FoodDrinkClaimType;

import java.math.BigDecimal;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class DailyExpenseFoodAndDrink {
    @NotNull(groups = {DailyExpense.AttendanceDay.class, DailyExpense.NonAttendanceDay.class})
    private FoodDrinkClaimType foodAndDrinkClaimType;

    @Min(0)
    private BigDecimal smartCardAmount;

}
