package uk.gov.hmcts.juror.api.bureau.controller.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Response DTO for the dashboard totals endpoints.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Schema(description = "Dashboard totals dto.")
public class DashboardResponseDto implements Serializable {

    @Schema(description = "Cumulative Totals component on the Dashboard.")
    private CumulativeTotal cumulativeTotals;

    @Schema(description = "Mandatory KPI Totals component on the Dashboard.")
    private DashboardMandatoryKpiData mandatoryKpis;

    @Schema(description = "Welsh Responses Totals component on the Dashboard.")
    private WelshOnlineResponseData welshResponseData;

    @Schema(description = "Auto Processed Responses Totals component on the Dashboard.")
    private AutoOnlineResponseData autoProcessedResponseData;

    @Schema(description = "Third Responses Totals component on the Dashboard.")
    private ThirdPtyOnlineResponseData thirdPtyResponseData;

    @Schema(description = "Satisfaction Survey component on the Dashboard.")
    private SurveySatisfactionData surveyResponseData;

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    @Schema(description = "Dashboard Cumulative Totals.")
    public static class CumulativeTotal implements Serializable {

        @Schema(description = "Total Summoned for given period")
        @Min(0)
        private Integer summonedTotal;

        @Schema(description = "Total Responded for given period")
        @Min(0)
        private Integer respondedTotal;

        @Schema(description = "Not Responded for given period")
        @Min(0)
        private Integer notRespondedTotal;

        @Schema(description = "Current Unprocessed total")
        @Min(0)
        private Integer currentUnprocessed;

        @Schema(description = "Number Summonses sent total")
        @Min(0)
        private Integer totalNumberSummonsesSent;

        @Schema(description = "Number Online replies total")
        @Min(0)
        private Integer totalNumberOnlineReplies;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    @Schema(description = "Dashboard Online Welsh Responses Counts.")
    public static class WelshOnlineResponseData implements Serializable {

        @Schema(description = "Total online Welsh responses")
        @Min(0)
        private Integer welshOnlineResponseTotal;

        @Schema(description = "Total online responses")
        @Min(0)
        private Integer onlineResponseTotal;

        @Schema(description = "Welsh online responses as a percentage of online responses.")
        @Min(0)
        private Float percentWelshOnlineResponses;       // welshOnlineResponseTotal / onlineResponseTotal

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    @Schema(description = "Dashboard Online Auto Processed Responses Counts.")
    public static class AutoOnlineResponseData implements Serializable {

        @Schema(description = "Total online auto processed responses")
        @Min(0)
        private Integer autoProcessedOnlineResponseTotal;

        @Schema(description = "Total online responses")
        @Min(0)
        private Integer onlineResponseTotal;

        @Schema(description = "Auto processed online responses as a percentage of online responses.")
        @Min(0)
        private Float percentAutoProcessedOnlineResponses;       // autoProcessedOnlineResponseTotal /
        // onlineResponseTotal

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    @Schema(description = "Dashboard Online Third Party Responses Counts.")
    public static class ThirdPtyOnlineResponseData implements Serializable {

        @Schema(description = "Total online Third Party responses")
        @Min(0)
        private Integer thirdPtyOnlineResponseTotal;

        @Schema(description = "Total online responses")
        @Min(0)
        private Integer onlineResponseTotal;

        @Schema(description = "Third party online responses as a percentage of online responses.")
        @Min(0)
        private Float percentThirdPtyOnlineResponses;       // thirdPtyOnlineResponseTotal / onlineResponseTotal

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    @Data
    @Schema(description = "Survey satisfaction totals.")
    public static class SurveySatisfactionData implements Serializable {

        @Schema(description = "total responses")
        @Min(0)
        private Integer responsesTotal;

        @Schema(description = "total very satisfied")
        @Min(0)
        private Integer verySatisfiedTotal;

        @Schema(description = "total satisfied")
        @Min(0)
        private Integer satisfiedTotal;

        @Schema(description = "total neither satisfied or dissatisfied")
        @Min(0)
        private Integer neitherSatisfiedOrDissatisfiedTotal;

        @Schema(description = "total dissatisfied")
        @Min(0)
        private Integer dissatisfiedTotal;

        @Schema(description = "total very dissatisfied")
        @Min(0)
        private Integer veryDissatisfiedTotal;

    }

}
