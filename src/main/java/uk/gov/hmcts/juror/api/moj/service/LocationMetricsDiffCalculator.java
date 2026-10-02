package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.bureau.controller.response.DashboardMandatoryKpiData;
import uk.gov.hmcts.juror.api.moj.controller.response.DbdDashboardResponseDto.LocationMetrics;
import uk.gov.hmcts.juror.api.moj.controller.response.DbdDashboardResponseDto.PeriodResult;
import uk.gov.hmcts.juror.api.moj.controller.response.DbdDashboardResponseDto.ResponseTimesPercent;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Computes periodB-minus-periodA differences for the DbD dashboard's "change" column.
 *
 * <p>
 * periodB is the later/comparison period and periodA the earlier/baseline period (e.g.
 * periodA = "Last quarter", periodB = "This quarter"), so a positive change reads as
 * "went up from periodA to periodB" - matching how the dashboard wants to show an upward
 * trend as a positive number rather than as current-minus-comparison.
 *
 * <p>
 * Stateless and dependency-free by design - it only ever operates on {@link LocationMetrics}
 * that {@link DbdDashboardServiceImpl} has already built, so it needs no repository, no Spring
 * wiring, and is trivially unit-testable in isolation from the rest of the service.
 */
class LocationMetricsDiffCalculator {

    /**
     * periodA and periodB are expected to be built from the same location list in the same order
     * (see DbdDashboardServiceImpl.buildPeriodResult), so their location lists line up
     * index-for-index - no locationCode matching is attempted here.
     */
    PeriodResult buildChangeResult(PeriodResult periodA, PeriodResult periodB) {
        List<LocationMetrics> changes = IntStream.range(0, periodA.getLocations().size())
            .mapToObj(i -> diffLocationMetrics(periodB.getLocations().get(i), periodA.getLocations().get(i)))
            .toList();

        return PeriodResult.builder().locations(changes).build();
    }

    // later minus earlier, metric by metric, so an upward trend from periodA to periodB shows as
    // a positive change. A metric missing (null) on either side is treated as 0 for the
    // subtraction, except where both sides are null - then the difference is null too, since
    // "no data minus no data" isn't meaningfully zero.
    private LocationMetrics diffLocationMetrics(LocationMetrics later, LocationMetrics earlier) {
        return LocationMetrics.builder()
            .locationCode(later.getLocationCode())
            .totalResponses(diffInt(later.getTotalResponses(), earlier.getTotalResponses()))
            .notRespondedTotal(diffInt(later.getNotRespondedTotal(), earlier.getNotRespondedTotal()))
            .thirdPartyTotal(diffInt(later.getThirdPartyTotal(), earlier.getThirdPartyTotal()))
            .onlineResponseTotal(diffInt(later.getOnlineResponseTotal(), earlier.getOnlineResponseTotal()))
            .paperResponseTotal(diffInt(later.getPaperResponseTotal(), earlier.getPaperResponseTotal()))
            .responseRatePercent(diffInt(later.getResponseRatePercent(), earlier.getResponseRatePercent()))
            .digitalResponsesPercent(
                diffInt(later.getDigitalResponsesPercent(), earlier.getDigitalResponsesPercent()))
            .onlineResponseTimes(diffResponseMethod(later.getOnlineResponseTimes(), earlier.getOnlineResponseTimes()))
            .paperResponseTimes(diffResponseMethod(later.getPaperResponseTimes(), earlier.getPaperResponseTimes()))
            .responseTimesPercent(
                diffResponseTimesPercent(later.getResponseTimesPercent(), earlier.getResponseTimesPercent()))
            .ageGroupBreakdown(diffIntMap(later.getAgeGroupBreakdown(), earlier.getAgeGroupBreakdown()))
            .ageGroupBreakdownPercent(
                diffIntMap(later.getAgeGroupBreakdownPercent(), earlier.getAgeGroupBreakdownPercent()))
            .build();
    }

    private DashboardMandatoryKpiData.ResponseMethod diffResponseMethod(
        DashboardMandatoryKpiData.ResponseMethod later, DashboardMandatoryKpiData.ResponseMethod earlier) {

        return DashboardMandatoryKpiData.ResponseMethod.builder()
            .within7days(later.getWithin7days() - earlier.getWithin7days())
            .within14days(later.getWithin14days() - earlier.getWithin14days())
            .within21days(later.getWithin21days() - earlier.getWithin21days())
            .over21days(later.getOver21days() - earlier.getOver21days())
            .build();
    }

    private ResponseTimesPercent diffResponseTimesPercent(ResponseTimesPercent later, ResponseTimesPercent earlier) {
        return ResponseTimesPercent.builder()
            .within7DaysPercent(diffInt(later.getWithin7DaysPercent(), earlier.getWithin7DaysPercent()))
            .within14DaysPercent(diffInt(later.getWithin14DaysPercent(), earlier.getWithin14DaysPercent()))
            .within21DaysPercent(diffInt(later.getWithin21DaysPercent(), earlier.getWithin21DaysPercent()))
            .over21DaysPercent(diffInt(later.getOver21DaysPercent(), earlier.getOver21DaysPercent()))
            .build();
    }

    private Map<String, Integer> diffIntMap(Map<String, Integer> later, Map<String, Integer> earlier) {
        return Stream.concat(later.keySet().stream(), earlier.keySet().stream())
            .distinct()
            .collect(Collectors.toMap(key -> key,
                                      key -> diffInt(later.get(key), earlier.get(key))));
    }

    private Integer diffInt(Integer later, Integer earlier) {
        if (later == null && earlier == null) {
            return null;
        }
        return (later != null ? later : 0) - (earlier != null ? earlier : 0);
    }
}
