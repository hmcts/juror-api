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
 * Computes periodA-minus-periodB differences for the DbD dashboard's "change" column.
 *
 * <p>
 * periodA is the primary/current period and periodB is the comparison period, so a positive change
 * means periodA is higher than periodB.
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
            .mapToObj(i -> diffLocationMetrics(periodA.getLocations().get(i), periodB.getLocations().get(i)))
            .toList();

        return PeriodResult.builder().locations(changes).build();
    }

    // periodA minus periodB, metric by metric. A metric missing (null) on either side is treated as 0 for the
    // subtraction, except where both sides are null - then the difference is null too, since
    // "no data minus no data" isn't meaningfully zero.
    private LocationMetrics diffLocationMetrics(LocationMetrics periodA, LocationMetrics periodB) {
        return LocationMetrics.builder()
            .locationCode(periodA.getLocationCode())
            .totalResponses(diffInt(periodA.getTotalResponses(), periodB.getTotalResponses()))
            .notRespondedTotal(diffInt(periodA.getNotRespondedTotal(), periodB.getNotRespondedTotal()))
            .thirdPartyTotal(diffInt(periodA.getThirdPartyTotal(), periodB.getThirdPartyTotal()))
            .onlineResponseTotal(diffInt(periodA.getOnlineResponseTotal(), periodB.getOnlineResponseTotal()))
            .paperResponseTotal(diffInt(periodA.getPaperResponseTotal(), periodB.getPaperResponseTotal()))
            .responseRatePercent(diffInt(periodA.getResponseRatePercent(), periodB.getResponseRatePercent()))
            .digitalResponsesPercent(
                diffInt(periodA.getDigitalResponsesPercent(), periodB.getDigitalResponsesPercent()))
            .onlineResponseTimes(diffResponseMethod(periodA.getOnlineResponseTimes(), periodB.getOnlineResponseTimes()))
            .paperResponseTimes(diffResponseMethod(periodA.getPaperResponseTimes(), periodB.getPaperResponseTimes()))
            .responseTimesPercent(
                diffResponseTimesPercent(periodA.getResponseTimesPercent(), periodB.getResponseTimesPercent()))
            .ageGroupBreakdown(diffIntMap(periodA.getAgeGroupBreakdown(), periodB.getAgeGroupBreakdown()))
            .ageGroupBreakdownPercent(
                diffIntMap(periodA.getAgeGroupBreakdownPercent(), periodB.getAgeGroupBreakdownPercent()))
            .build();
    }

    private DashboardMandatoryKpiData.ResponseMethod diffResponseMethod(
        DashboardMandatoryKpiData.ResponseMethod periodA, DashboardMandatoryKpiData.ResponseMethod periodB) {

        return DashboardMandatoryKpiData.ResponseMethod.builder()
            .within7days(periodA.getWithin7days() - periodB.getWithin7days())
            .within14days(periodA.getWithin14days() - periodB.getWithin14days())
            .within21days(periodA.getWithin21days() - periodB.getWithin21days())
            .over21days(periodA.getOver21days() - periodB.getOver21days())
            .build();
    }

    private ResponseTimesPercent diffResponseTimesPercent(ResponseTimesPercent periodA, ResponseTimesPercent periodB) {
        return ResponseTimesPercent.builder()
            .within7DaysPercent(diffInt(periodA.getWithin7DaysPercent(), periodB.getWithin7DaysPercent()))
            .within14DaysPercent(diffInt(periodA.getWithin14DaysPercent(), periodB.getWithin14DaysPercent()))
            .within21DaysPercent(diffInt(periodA.getWithin21DaysPercent(), periodB.getWithin21DaysPercent()))
            .over21DaysPercent(diffInt(periodA.getOver21DaysPercent(), periodB.getOver21DaysPercent()))
            .build();
    }

    private Map<String, Integer> diffIntMap(Map<String, Integer> periodA, Map<String, Integer> periodB) {
        return Stream.concat(periodA.keySet().stream(), periodB.keySet().stream())
            .distinct()
            .collect(Collectors.toMap(key -> key,
                                      key -> diffInt(periodA.get(key), periodB.get(key))));
    }

    private Integer diffInt(Integer periodA, Integer periodB) {
        if (periodA == null && periodB == null) {
            return null;
        }
        return (periodA != null ? periodA : 0) - (periodB != null ? periodB : 0);
    }
}
