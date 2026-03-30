package uk.gov.hmcts.juror.api.bureau.controller.request;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Request DTO for the  DeferralExcusalDashboard.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Request filters for DeferralExcusal dashboard. Given Period.")
public class DashboardDeferralExcusalRequestDto implements Serializable {


    @Schema(description = "Start date parameter in format of  year week ", requiredMode = Schema.RequiredMode.REQUIRED)
    private String startYearWeek;

    @Schema(description = "End date parameter in format  year week ", requiredMode = Schema.RequiredMode.REQUIRED)
    private String endYearWeek;

    @Schema(description = "Deferral Selection ")
    private String deferral;

    @Schema(description = "Excusal Selection ")
    private String excusal;

    @Schema(description = "Bureau Selection ")
    private String bureau;

    @Schema(description = "Court Selection")
    private String court;

}
