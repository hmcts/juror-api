package uk.gov.hmcts.juror.api.bureau.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.bureau.controller.request.JurorExpensesCalcTravelModeData;

import java.io.Serializable;
import java.util.List;

/**
 * Results DTO for the expenses calculator juror details.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Expense calculator results dto.")
public class JurorExpensesCalcResults implements Serializable {

    @NotEmpty()
    @Schema(description = "Methods of travel taken by juror to court.", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<JurorExpensesCalcTravelModeData> travellingModes;

    @Schema(description = "Total claimable daily.")
    private Float dailyTotal;

    @Schema(description = "Loss of earnings total")
    private Float dailyLossOfEarningsTotal;

    @Schema(description = "Loss of earnings Claimable")
    private Float dailyLossOfEarningsClaim;

    @Schema(description = "Daily travel total.")
    @Min(0)
    private Float dailyTravelTotal;

    @Schema(description = "Daily Loss of earnings threshold for 10 days more than 4hrs spent at court.")
    private Float lossOfEarningsTenDaysFourHrsMore;

    @Schema(description = "Daily Loss of earnings threshold for 10 days less than 4hrs spent at court.")
    private Float lossOfEarningsTenDaysFourHrsLess;

    @Schema(description = "Daily Subsistence. - Food & Drink")
    @Min(0)
    private Float subsistence;

}
