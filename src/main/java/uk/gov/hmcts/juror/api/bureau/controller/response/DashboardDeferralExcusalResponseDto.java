package uk.gov.hmcts.juror.api.bureau.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.bureau.domain.StatsDeferrals;
import uk.gov.hmcts.juror.api.bureau.domain.StatsExcusals;

import java.io.Serializable;
import java.util.List;

/**
 * Response DTO for the deferralexcusal dashboard filtered amounts.
 */

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Dashboard deferral excusal totals dto.")
public class DashboardDeferralExcusalResponseDto implements Serializable {

    @Schema(description = "Deferral & Excusal Filtered Values")
    private DeferralExcusalValues deferralExcusalValues;

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    @Schema(description = "Dashboard deferral excusal values .")
    public static class DeferralExcusalValues implements Serializable {


        @Schema(description = "Total Excusal Count for given period")
        @Min(0)
        private Integer excusalCount;

        @Schema(description = "Reason for Excusal")
        private String reason;

        @Schema(description = "The Calendar Year")
        private String calendarYear;

        @Schema(description = "The Financial Year")
        private String financialYear;

        @Schema(description = "The Week of the Year")
        private String week;

        @Schema(description = "Deferral Stats", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<StatsDeferrals> deferralStats;

        @Schema(description = "Excusal Stats", requiredMode = Schema.RequiredMode.REQUIRED)
        private List<StatsExcusals> excusalStats;


    }
}
