package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.controller.request.DbdDashboardRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DbdDashboardResponseDto.MonthlyResponseRate;
import uk.gov.hmcts.juror.api.moj.domain.DbdResponseStats;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Builds the "response rate by month jurors were summoned" table: one row per calendar month
 * covered by dateRangeA/dateRangeB, with a response-rate percentage per requested court group.
 *
 * <p>
 * Month is derived here from each row's summons_date rather than stored/maintained on the table
 * or in the stored procedure: it's a pure function of summons_date, so keeping it in Java means
 * there's nothing that can drift out of sync with the source column.
 *
 * <p>
 * Stateless and dependency-free by design - it only ever operates on the {@link DbdResponseStats}
 * rows {@link DbdDashboardServiceImpl} has already fetched (no extra queries), so it needs no
 * repository, no Spring wiring, and is trivially unit-testable in isolation from the rest of the
 * service.
 */
class MonthlyResponseRateCalculator {

    private static final String ONLINE = "Online";
    private static final String PAPER = "Paper";
    private static final String NOT_RESPONDED = "None";

    List<MonthlyResponseRate> buildMonthlyResponseRates(
        List<DbdDashboardRequestDto.CourtGroupDto> courtGroups,
        Map<String, List<DbdResponseStats>> periodAData,
        Map<String, List<DbdResponseStats>> periodBData) {

        // groupName -> (month -> response rate %), built group by group so a group's own rows never
        // leak into another group's percentage.
        Map<String, Map<YearMonth, Integer>> responseRateByGroupAndMonth = courtGroups.stream()
            .collect(Collectors.toMap(
                DbdDashboardRequestDto.CourtGroupDto::getGroupName,
                group -> responseRateByMonth(group, periodAData, periodBData)));

        TreeSet<YearMonth> allMonths = responseRateByGroupAndMonth.values().stream()
            .flatMap(byMonth -> byMonth.keySet().stream())
            .collect(Collectors.toCollection(TreeSet::new));

        return allMonths.stream()
            .map(month -> MonthlyResponseRate.builder()
                .month(month)
                .groupResponseRatePercent(responseRateByGroupAndMonth.entrySet().stream()
                                              .filter(entry -> entry.getValue().containsKey(month))
                                              .collect(Collectors.toMap(
                                                  Map.Entry::getKey, entry -> entry.getValue().get(month))))
                .recentCohort(isWithinLastSixWeeks(month))
                .build())
            .toList();
    }

    private Map<YearMonth, Integer> responseRateByMonth(
        DbdDashboardRequestDto.CourtGroupDto group,
        Map<String, List<DbdResponseStats>> periodAData,
        Map<String, List<DbdResponseStats>> periodBData) {

        Set<String> groupLocCodes = group.getGroupLocations().stream()
            .map(loc -> String.format("%03d", loc))
            .collect(Collectors.toSet());

        Map<YearMonth, List<DbdResponseStats>> rowsByMonth = Stream.concat(
                periodAData.values().stream(), periodBData != null ? periodBData.values().stream() : Stream.empty())
            .flatMap(List::stream)
            .filter(row -> groupLocCodes.contains(row.getLocCode()))
            .collect(Collectors.toMap(DbdResponseStats::getId, Function.identity(), (first, second) -> first))
            .values().stream()
            .collect(Collectors.groupingBy(row -> YearMonth.from(row.getSummonsDate())));

        return rowsByMonth.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> calculateResponseRatePercent(entry.getValue())));
    }

    private Integer calculateResponseRatePercent(List<DbdResponseStats> rows) {
        int notResponded = sumJurorCountWhere(rows, row -> NOT_RESPONDED.equals(row.getResponseMethod()));
        int responded = sumJurorCountWhere(rows, row -> ONLINE.equals(row.getResponseMethod())
            || PAPER.equals(row.getResponseMethod()));

        return calculatePercentage(responded, responded + notResponded);
    }

    // The dashboard footnotes the most recently summoned month(s): "at the latest data refresh,
    // this cohort was summoned within the last 6 weeks - its response rate may still increase".
    // There's no separate "data refresh" timestamp to key off, so today's date stands in for it.
    private boolean isWithinLastSixWeeks(YearMonth month) {
        return !month.atEndOfMonth().isBefore(LocalDate.now().minusWeeks(6));
    }

    private Integer calculatePercentage(int part, int total) {
        return total > 0 ? Math.round(part * 100f / total) : null;
    }

    private int sumJurorCountWhere(List<DbdResponseStats> rows, Predicate<DbdResponseStats> filter) {
        return rows.stream()
            .filter(filter)
            .mapToInt(DbdResponseStats::getJurorCount)
            .sum();
    }
}
