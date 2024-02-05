package uk.gov.hmcts.juror.api.moj.controller.request.expense.draft;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.juror.api.moj.enumeration.FoodDrinkClaimType;

import java.math.BigDecimal;

@Data
@Builder
public class DailyExpenseFoodAndDrink {
    @JsonProperty("food_and_drink_claim_type")
    @NotNull(groups = {DailyExpense.AttendanceDay.class, DailyExpense.NonAttendanceDay.class})
    private FoodDrinkClaimType foodAndDrinkClaimType;

    @JsonProperty("smart_card_amount")
    @Min(0)
    private BigDecimal smartCardAmount;

}
