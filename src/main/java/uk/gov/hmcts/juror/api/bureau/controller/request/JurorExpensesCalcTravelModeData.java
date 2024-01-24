package uk.gov.hmcts.juror.api.bureau.controller.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import java.io.Serializable;


/**
 * Travel Mode Data used by DTO request/response for the expenses calculator juror details
 */
@AllArgsConstructor
@Builder
@Data
public class JurorExpensesCalcTravelModeData implements Serializable {

    @NotBlank(message = "Mode of Travel is required")
    @Schema(description = "Mode of Travel", example = "car", requiredMode = Schema.RequiredMode.REQUIRED)
    private String modeOfTravel;

    @Schema(description = "Miles juror will travel to/from court daily", example = "60")
    @Min(0)
    private Float dailyMiles;

    @Schema(description = "Cost of Daily Public Transport ticket.", example = "60")
    @Min(0)
    private Float dailyCost;

    @Schema(description = "Daily rate per mile for this mode of travel.", example = "60")
    @Min(0)
    private Float ratePerMile;

}
