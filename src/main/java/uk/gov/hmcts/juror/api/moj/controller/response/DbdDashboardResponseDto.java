package uk.gov.hmcts.juror.api.moj.controller.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.juror.api.bureau.controller.response.DashboardMandatoryKpiData;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for the Digital by Default pilot dashboard.
 *
 * <p>
 * Reuses DashboardMandatoryKpiData.ResponseMethod for the response-time buckets so the
 * within7/14/21/over21 shape stays consistent with the existing dashboard rather than
 * duplicating it.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data
@Schema(description = "Digital by Default pilot dashboard results, grouped by requested court group.")
public class DbdDashboardResponseDto implements Serializable {

    @Schema(description = "One entry per requested court group, in the order they were requested.")
    private List<CourtGroupResult> courtGroups;

    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Builder
    @Data
    @Schema(description = "Results for a single requested court group.")
    public static class CourtGroupResult implements Serializable {

        @Schema(description = "Echoes the requested groupName.")
        private String groupName;

        @Schema(description = "Results for dateRangeA.")
        private PeriodResult periodA;

        @Schema(description = "Results for dateRangeB. Null if dateRangeB was not supplied in the request.")
        private PeriodResult periodB;

        @Schema(description = "periodA minus periodB, metric by metric, one entry per location in the same "
            + "order as periodA/periodB (or one summed entry when sumGroups=true). Null if dateRangeB was "
            + "not supplied in the request.")
        private PeriodResult change;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Builder
    @Data
    @Schema(description = "Results for a single date range within a court group.")
    public static class PeriodResult implements Serializable {

        @Schema(description = "One entry when sumGroups=true (locationCode is null, values are summed "
            + "across the group); one entry per requested location when sumGroups=false.")
        private List<LocationMetrics> locations;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Builder
    @Data
    @Schema(description = "Take-up metrics for a single court, or a summed group when sumGroups=true.")
    public static class LocationMetrics implements Serializable {

        @Schema(description = "Court location code. Null when this row represents a summed group total.")
        private Integer locationCode;

        @Schema(description = "All responses received for the period (online + paper).")
        private Integer totalResponses;

        @Schema(description = "Not-responded total for the period.")
        private Integer notRespondedTotal;

        @Schema(description = "Third-party digital response total for the period. Currently always null - "
            + "not sourced from dbd_response_stats; wire in once the pilot-scoped third-party table/proc exists.")
        private Integer thirdPartyTotal;

        @Schema(description = "Total online (digital) responses for the period.")
        private Integer onlineResponseTotal;

        @Schema(description = "Total paper responses for the period.")
        private Integer paperResponseTotal;

        @Schema(description = "Responded jurors as a percentage of all jurors summoned "
            + "((online + paper) / (online + paper + notResponded) * 100), rounded to the nearest whole number. "
            + "Null if nothing was summoned.")
        private Integer responseRatePercent;

        @Schema(description = "Online responses as a percentage of all responses received "
            + "(online / (online + paper) * 100), rounded to the nearest whole number. "
            + "Null if there were no responses.")
        private Integer digitalResponsesPercent;

        @Schema(description = "Online response counts broken down by response-time bucket.")
        private DashboardMandatoryKpiData.ResponseMethod onlineResponseTimes;

        @Schema(description = "Paper response counts broken down by response-time bucket.")
        private DashboardMandatoryKpiData.ResponseMethod paperResponseTimes;

        @Schema(description = "Online and paper response times combined, each bucket expressed as a "
            + "percentage of all responses received (online + paper), rounded to the nearest whole number. "
            + "Null fields if there were no responses.")
        private ResponseTimesPercent responseTimesPercent;

        @Schema(description = "Juror counts broken down by age_group, summed across responded and "
            + "not-responded rows alike.")
        private Map<String, Integer> ageGroupBreakdown;

        @Schema(description = "Same breakdown as ageGroupBreakdown, expressed as a percentage of all "
            + "jurors summoned in the period (responded and not-responded alike), rounded to the nearest "
            + "whole number.")
        private Map<String, Integer> ageGroupBreakdownPercent;
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @Builder
    @Data
    @Schema(description = "Response-time buckets (online + paper combined) expressed as rounded percentages.")
    public static class ResponseTimesPercent implements Serializable {

        @Schema(description = "Percentage of all responses received within 7 days.")
        private Integer within7DaysPercent;

        @Schema(description = "Percentage of all responses received within 14 days.")
        private Integer within14DaysPercent;

        @Schema(description = "Percentage of all responses received within 21 days.")
        private Integer within21DaysPercent;

        @Schema(description = "Percentage of all responses received after 21 days.")
        private Integer over21DaysPercent;
    }
}
