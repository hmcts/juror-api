package uk.gov.hmcts.juror.api.moj.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.moj.controller.request.DbdDashboardRequestDto.CourtGroupDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DbdDashboardResponseDto.MonthlyResponseRate;
import uk.gov.hmcts.juror.api.moj.domain.DbdResponseStats;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

@SuppressWarnings("PMD.TooManyMethods")
class MonthlyResponseRateCalculatorTest {

    private static final LocalDate SUMMONS_DATE = LocalDate.of(2026, 1, 1);
    private static final String GROUP_NAME = "Test group";

    private final MonthlyResponseRateCalculator calculator = new MonthlyResponseRateCalculator();

    @Test
    void shouldCalculateRoundedResponseRateUsingJurorCounts() {
        List<MonthlyResponseRate> result = calculator.buildMonthlyResponseRates(
            List.of(group(GROUP_NAME, List.of(415))),
            Map.of("415", List.of(
                responseStats("415", SUMMONS_DATE, "Online", 8),
                responseStats("415", SUMMONS_DATE, "Paper", 6),
                responseStats("415", SUMMONS_DATE, "None", 9))),
            null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMonth()).isEqualTo(YearMonth.of(2026, 1));
        assertThat(result.get(0).getGroupResponseRatePercent()).containsExactlyEntriesOf(Map.of(GROUP_NAME, 61));
    }

    @Test
    void shouldCombineCountsAcrossLocationsAndBothPeriodsBeforeCalculatingPercentage() {
        List<MonthlyResponseRate> result = calculator.buildMonthlyResponseRates(
            List.of(group(GROUP_NAME, List.of(415, 416))),
            Map.of(
                "415", List.of(responseStats("415", SUMMONS_DATE, "Online", 1)),
                "416", List.of(responseStats("416", SUMMONS_DATE, "None", 2))),
            Map.of(
                "415", List.of(responseStats("415", SUMMONS_DATE.plusDays(10), "Paper", 5)),
                "416", List.of(responseStats("416", SUMMONS_DATE.plusDays(10), "None", 12))));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGroupResponseRatePercent()).containsExactlyEntriesOf(Map.of(GROUP_NAME, 30));
    }

    @Test
    void shouldKeepGroupsSeparateAndMatchZeroPaddedLocationCodes() {
        List<MonthlyResponseRate> result = calculator.buildMonthlyResponseRates(
            List.of(group("First group", List.of(1, 42)), group("Second group", List.of(415))),
            Map.of(
                "001", List.of(responseStats("001", SUMMONS_DATE, "Online", 1)),
                "042", List.of(responseStats("042", SUMMONS_DATE, "None", 3)),
                "415", List.of(responseStats("415", SUMMONS_DATE, "Paper", 7)),
                "999", List.of(responseStats("999", SUMMONS_DATE, "None", 100))),
            null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGroupResponseRatePercent())
            .containsExactlyInAnyOrderEntriesOf(Map.of("First group", 25, "Second group", 100));
    }

    @Test
    void shouldIncludeSharedLocationsInEachRequestedGroup() {
        List<MonthlyResponseRate> result = calculator.buildMonthlyResponseRates(
            List.of(group("First group", List.of(415)), group("Second group", List.of(415, 416))),
            Map.of(
                "415", List.of(responseStats("415", SUMMONS_DATE, "Online", 2)),
                "416", List.of(responseStats("416", SUMMONS_DATE, "None", 6))),
            null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGroupResponseRatePercent())
            .containsExactlyInAnyOrderEntriesOf(Map.of("First group", 100, "Second group", 25));
    }

    @Test
    void shouldOrderSummonsMonthsAcrossYearsAndOmitGroupsWithoutData() {
        LocalDate december = LocalDate.of(2025, 12, 31);
        LocalDate february = LocalDate.of(2026, 2, 1);
        DbdResponseStats januaryResponse = responseStats("415", SUMMONS_DATE, "Online", 2);
        januaryResponse.setResponseDate(february);

        List<MonthlyResponseRate> result = calculator.buildMonthlyResponseRates(
            List.of(group("First group", List.of(415)), group("Second group", List.of(416))),
            Map.of("415", List.of(
                responseStats("415", february, "None", 4),
                januaryResponse)),
            Map.of("416", List.of(responseStats("416", december, "Paper", 3))));

        assertThat(result).extracting(MonthlyResponseRate::getMonth)
            .containsExactly(YearMonth.of(2025, 12), YearMonth.of(2026, 1), YearMonth.of(2026, 2));
        assertThat(result.get(0).getGroupResponseRatePercent())
            .containsExactlyEntriesOf(Map.of("Second group", 100));
        assertThat(result.get(1).getGroupResponseRatePercent())
            .containsExactlyEntriesOf(Map.of("First group", 100));
        assertThat(result.get(2).getGroupResponseRatePercent())
            .containsExactlyEntriesOf(Map.of("First group", 0));
    }

    @Test
    void shouldIgnoreUnknownAndNullResponseMethods() {
        List<MonthlyResponseRate> result = calculator.buildMonthlyResponseRates(
            List.of(group(GROUP_NAME, List.of(415))),
            Map.of("415", List.of(
                responseStats("415", SUMMONS_DATE, "Online", 1),
                responseStats("415", SUMMONS_DATE, "None", 3),
                responseStats("415", SUMMONS_DATE, "Unknown", 100),
                responseStats("415", SUMMONS_DATE, null, 100))),
            null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getGroupResponseRatePercent()).containsExactlyEntriesOf(Map.of(GROUP_NAME, 25));
    }

    @Test
    void shouldReturnNoMonthsWhenBothPeriodsAreEmpty() {
        assertThat(calculator.buildMonthlyResponseRates(
            List.of(group(GROUP_NAME, List.of(415))), Map.of(), Map.of())).isEmpty();
    }

    @Test
    void shouldReturnNoMonthsWhenOptionalPeriodIsAbsentAndPeriodAHasNoRows() {
        assertThat(calculator.buildMonthlyResponseRates(
            List.of(group(GROUP_NAME, List.of(415))), Map.of("415", List.of()), null)).isEmpty();
    }

    @Test
    void shouldExcludeMonthsContainingOnlyUnrequestedLocations() {
        assertThat(calculator.buildMonthlyResponseRates(
            List.of(group(GROUP_NAME, List.of(415))),
            Map.of("416", List.of(responseStats("416", SUMMONS_DATE, "Online", 10))),
            null)).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
        "2026-04-10, true",
        "2026-04-11, true",
        "2026-04-12, false"
    })
    void shouldMarkRecentCohortUsingInclusiveMonthEndBoundary(String today, boolean expectedRecentCohort) {
        LocalDate currentDate = LocalDate.parse(today);
        LocalDate summonsDate = LocalDate.of(2026, 2, 1);
        Map<String, List<DbdResponseStats>> periodData = Map.of(
            "415", List.of(responseStats("415", summonsDate, "Online", 1)));

        try (MockedStatic<LocalDate> localDate = mockStatic(LocalDate.class, CALLS_REAL_METHODS)) {
            localDate.when(LocalDate::now).thenReturn(currentDate);

            List<MonthlyResponseRate> result = calculator.buildMonthlyResponseRates(
                List.of(group(GROUP_NAME, List.of(415))), periodData, null);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getRecentCohort()).isEqualTo(expectedRecentCohort);
        }
    }

    private CourtGroupDto group(String groupName, List<Integer> locations) {
        return CourtGroupDto.builder().groupName(groupName).groupLocations(locations).build();
    }

    private DbdResponseStats responseStats(String locCode, LocalDate summonsDate,
                                           String responseMethod, int jurorCount) {
        DbdResponseStats stats = new DbdResponseStats();
        stats.setLocCode(locCode);
        stats.setSummonsDate(summonsDate);
        stats.setResponseMethod(responseMethod);
        stats.setJurorCount(jurorCount);
        return stats;
    }
}
