package uk.gov.hmcts.juror.api.bureau.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * Request DTO for the expenses calculator juror details.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Request body for expense calculator details for juror.")
public class JurorExpensesCalcRequestDto implements Serializable {

    @NotEmpty()
    @Schema(description = "Methods of travel taken by juror to court.", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<JurorExpensesCalcTravelModeData> travellingModes;

    @Schema(description = "Will juror loose income?", requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean looseIncome;

    @Schema(description = "Is income more than max threshold?")
    private Boolean incomeExceedsThreshold;

    @Schema(description = "Daily Earnings Amount - Loss of Earning")
    @Min(0)
    private Float dailyEarnings;

    @Schema(description = "Does Juror have any additional costs ie child care?", requiredMode =
        Schema.RequiredMode.REQUIRED)
    private Boolean extraCosts;

    @Schema(description = "Additional costs amount.")
    @Min(0)
    private Float extraCostsAmount;

    @Schema(description = "Is Parking required? ")
    private Boolean parking;

}
