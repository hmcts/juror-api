package uk.gov.hmcts.juror.api.moj.service;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.bureau.controller.response.DashboardMandatoryKpiData;
import uk.gov.hmcts.juror.api.moj.controller.response.DbdDashboardResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DbdDashboardResponseDto.LocationMetrics;
import uk.gov.hmcts.juror.api.moj.controller.response.DbdDashboardResponseDto.PeriodResult;
import uk.gov.hmcts.juror.api.moj.controller.response.DbdDashboardResponseDto.ResponseTimesPercent;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LocationMetricsDiffCalculatorTest {

    private final LocationMetricsDiffCalculator diffCalculator = new LocationMetricsDiffCalculator();

    @Test
    void shouldCalculatePeriodBMinusPeriodAChange() {
        PeriodResult periodA = PeriodResult.builder()
            .locations(List.of(locationMetrics(
                415,
                10,
                4,
                7,
                3,
                71,
                70,
                responseMethod(4, 3, 2, 1),
                responseMethod(2, 1, 0, 0),
                responseTimesPercent(60, 40, 0, 0),
                Map.of("18-24", 8, "25-34", 6),
                Map.of("18-24", 57, "25-34", 43)
            )))
            .build();
        PeriodResult periodB = PeriodResult.builder()
            .locations(List.of(locationMetrics(
                415,
                13,
                2,
                9,
                4,
                87,
                69,
                responseMethod(5, 4, 3, 2),
                responseMethod(3, 1, 1, 0),
                responseTimesPercent(62, 38, 8, 0),
                Map.of("18-24", 7, "35-44", 8),
                Map.of("18-24", 47, "35-44", 53)
            )))
            .build();

        DbdDashboardResponseDto.LocationMetrics result =
            diffCalculator.buildChangeResult(periodA, periodB).getLocations().get(0);

        assertThat(result.getLocationCode()).isEqualTo(415);
        assertThat(result.getTotalResponses()).isEqualTo(3);
        assertThat(result.getNotRespondedTotal()).isEqualTo(-2);
        assertThat(result.getThirdPartyTotal()).isNull();
        assertThat(result.getOnlineResponseTotal()).isEqualTo(2);
        assertThat(result.getPaperResponseTotal()).isEqualTo(1);
        assertThat(result.getResponseRatePercent()).isEqualTo(16);
        assertThat(result.getDigitalResponsesPercent()).isEqualTo(-1);
        assertThat(result.getOnlineResponseTimes().getWithin7days()).isEqualTo(1);
        assertThat(result.getOnlineResponseTimes().getWithin14days()).isEqualTo(1);
        assertThat(result.getOnlineResponseTimes().getWithin21days()).isEqualTo(1);
        assertThat(result.getOnlineResponseTimes().getOver21days()).isEqualTo(1);
        assertThat(result.getPaperResponseTimes().getWithin7days()).isEqualTo(1);
        assertThat(result.getPaperResponseTimes().getWithin14days()).isZero();
        assertThat(result.getPaperResponseTimes().getWithin21days()).isEqualTo(1);
        assertThat(result.getPaperResponseTimes().getOver21days()).isZero();
        assertThat(result.getResponseTimesPercent().getWithin7DaysPercent()).isEqualTo(2);
        assertThat(result.getResponseTimesPercent().getWithin14DaysPercent()).isEqualTo(-2);
        assertThat(result.getResponseTimesPercent().getWithin21DaysPercent()).isEqualTo(8);
        assertThat(result.getResponseTimesPercent().getOver21DaysPercent()).isZero();
        assertThat(result.getAgeGroupBreakdown()).containsEntry("18-24", -1);
        assertThat(result.getAgeGroupBreakdown()).containsEntry("25-34", -6);
        assertThat(result.getAgeGroupBreakdown()).containsEntry("35-44", 8);
        assertThat(result.getAgeGroupBreakdownPercent()).containsEntry("18-24", -10);
        assertThat(result.getAgeGroupBreakdownPercent()).containsEntry("25-34", -43);
        assertThat(result.getAgeGroupBreakdownPercent()).containsEntry("35-44", 53);
    }

    @SuppressWarnings("PMD.ExcessiveParameterList")
    private LocationMetrics locationMetrics(
        Integer locationCode,
        Integer totalResponses,
        Integer notRespondedTotal,
        Integer onlineResponseTotal,
        Integer paperResponseTotal,
        Integer responseRatePercent,
        Integer digitalResponsesPercent,
        DashboardMandatoryKpiData.ResponseMethod onlineResponseTimes,
        DashboardMandatoryKpiData.ResponseMethod paperResponseTimes,
        ResponseTimesPercent responseTimesPercent,
        Map<String, Integer> ageGroupBreakdown,
        Map<String, Integer> ageGroupBreakdownPercent) {

        return LocationMetrics.builder()
            .locationCode(locationCode)
            .totalResponses(totalResponses)
            .notRespondedTotal(notRespondedTotal)
            .thirdPartyTotal(null)
            .onlineResponseTotal(onlineResponseTotal)
            .paperResponseTotal(paperResponseTotal)
            .responseRatePercent(responseRatePercent)
            .digitalResponsesPercent(digitalResponsesPercent)
            .onlineResponseTimes(onlineResponseTimes)
            .paperResponseTimes(paperResponseTimes)
            .responseTimesPercent(responseTimesPercent)
            .ageGroupBreakdown(ageGroupBreakdown)
            .ageGroupBreakdownPercent(ageGroupBreakdownPercent)
            .build();
    }

    private DashboardMandatoryKpiData.ResponseMethod responseMethod(
        int within7Days,
        int within14Days,
        int within21Days,
        int over21Days) {

        return DashboardMandatoryKpiData.ResponseMethod.builder()
            .within7days(within7Days)
            .within14days(within14Days)
            .within21days(within21Days)
            .over21days(over21Days)
            .build();
    }

    private ResponseTimesPercent responseTimesPercent(
        Integer within7DaysPercent,
        Integer within14DaysPercent,
        Integer within21DaysPercent,
        Integer over21DaysPercent) {

        return ResponseTimesPercent.builder()
            .within7DaysPercent(within7DaysPercent)
            .within14DaysPercent(within14DaysPercent)
            .within21DaysPercent(within21DaysPercent)
            .over21DaysPercent(over21DaysPercent)
            .build();
    }
}
