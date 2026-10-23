package uk.gov.hmcts.juror.api.moj.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import uk.gov.hmcts.juror.api.bureau.controller.response.DashboardMandatoryKpiData;
import uk.gov.hmcts.juror.api.moj.controller.request.DbdDashboardRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DbdDashboardResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DbdDashboardResponseDto.CourtGroupResult;
import uk.gov.hmcts.juror.api.moj.controller.response.DbdDashboardResponseDto.LocationMetrics;
import uk.gov.hmcts.juror.api.moj.controller.response.DbdDashboardResponseDto.MonthlyResponseRate;
import uk.gov.hmcts.juror.api.moj.controller.response.DbdDashboardResponseDto.PeriodResult;
import uk.gov.hmcts.juror.api.moj.controller.response.DbdDashboardResponseDto.ResponseTimesPercent;
import uk.gov.hmcts.juror.api.moj.domain.DbdResponseStats;
import uk.gov.hmcts.juror.api.moj.repository.DbdResponseStatsRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DbdDashboardServiceImpl implements DbdDashboardService {

    private static final String ONLINE = "Online";
    private static final String PAPER = "Paper";
    private static final String NOT_RESPONDED = "None";

    private static final String WITHIN_7_DAYS = "Within 7 days";
    private static final String WITHIN_14_DAYS = "Within 14 days";
    private static final String WITHIN_21_DAYS = "Within 21 days";
    private static final String OVER_21_DAYS = "Over 21 days";

    private final DbdResponseStatsRepository dbdResponseStatsRepository;

    // Package-visible phases, each a stateless, dependency-free slice of the overall build - a
    // plain instance is enough for either, no Spring wiring needed for something with no
    // collaborators of its own. See each class's Javadoc for why it's split out.
    private final LocationMetricsDiffCalculator locationMetricsDiffCalculator = new LocationMetricsDiffCalculator();
    private final MonthlyResponseRateCalculator monthlyResponseRateCalculator = new MonthlyResponseRateCalculator();

    @Autowired
    public DbdDashboardServiceImpl(DbdResponseStatsRepository dbdResponseStatsRepository) {
        Assert.notNull(dbdResponseStatsRepository, "DbdResponseStatsRepository cannot be null");
        this.dbdResponseStatsRepository = dbdResponseStatsRepository;
    }

    // Keep both period queries on the same snapshot if the statistics refresh commits between them.
    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public DbdDashboardResponseDto getGroupStatistics(DbdDashboardRequestDto request) {
        log.debug("Called Service : DbdDashboardServiceImpl.getGroupStatistics()");

        // 1. Flatten every location across every group into one distinct set -
        //    this is what keeps query count independent of group count.
        Set<String> allLocCodes = request.getCourtGroups().stream()
            .flatMap(group -> group.getGroupLocations().stream())
            .map(loc -> String.format("%03d", loc))
            .collect(Collectors.toSet());

        // 2. One fetch per date range - not per group, not per location. Pilot inclusion
        //    is enforced upstream (dbd_response_stats only ever contains pilot courts), so
        //    no separate "is this court in the pilot" check is needed here.
        Map<String, List<DbdResponseStats>> periodAData = fetchByLocCode(allLocCodes, request.getDateRangeA());
        Map<String, List<DbdResponseStats>> periodBData = request.getDateRangeB() != null
            ? fetchByLocCode(allLocCodes, request.getDateRangeB())
            : null;

        // 3. Aggregate per group from the already-fetched, loc_code-keyed data - no further queries.
        List<CourtGroupResult> results = request.getCourtGroups().stream()
            .map(group -> buildGroupResult(group, periodAData, periodBData, request.isSumGroups()))
            .toList();

        // 4. Re-slice the same already-fetched rows by calendar month of summons_date - still no
        //    further queries. Month is derived here rather than stored/maintained on the table or
        //    in the stored procedure: it's a pure function of summons_date, so keeping it in Java
        //    means there's nothing that can drift out of sync with the source column.
        List<MonthlyResponseRate> monthlyResponseRates = monthlyResponseRateCalculator
            .buildMonthlyResponseRates(request.getCourtGroups(), periodAData, periodBData);

        return DbdDashboardResponseDto.builder()
            .courtGroups(results)
            .monthlyResponseRates(monthlyResponseRates)
            .build();
    }

    private Map<String, List<DbdResponseStats>> fetchByLocCode(
        Set<String> locCodes, DbdDashboardRequestDto.DateRangeDto range) {

        List<DbdResponseStats> rows = dbdResponseStatsRepository.findByLocCodeInAndSummonsDateBetween(
            locCodes, range.getStartDate(), range.getEndDate());

        return rows.stream().collect(Collectors.groupingBy(DbdResponseStats::getLocCode));
    }

    private CourtGroupResult buildGroupResult(
        DbdDashboardRequestDto.CourtGroupDto group,
        Map<String, List<DbdResponseStats>> periodAData,
        Map<String, List<DbdResponseStats>> periodBData,
        boolean sumGroups) {

        List<String> groupLocCodes = group.getGroupLocations().stream()
            .distinct()
            .map(loc -> String.format("%03d", loc))
            .toList();

        PeriodResult periodA = buildPeriodResult(groupLocCodes, periodAData, sumGroups);
        PeriodResult periodB = periodBData != null
            ? buildPeriodResult(groupLocCodes, periodBData, sumGroups)
            : null;

        return CourtGroupResult.builder()
            .groupName(group.getGroupName())
            .periodA(periodA)
            .periodB(periodB)
            .change(periodB != null ? locationMetricsDiffCalculator.buildChangeResult(periodA, periodB) : null)
            .build();
    }

    private PeriodResult buildPeriodResult(
        List<String> locCodes, Map<String, List<DbdResponseStats>> periodData, boolean sumGroups) {

        if (sumGroups) {
            List<DbdResponseStats> merged = locCodes.stream()
                .flatMap(loc -> periodData.getOrDefault(loc, Collections.emptyList()).stream())
                .toList();

            return PeriodResult.builder()
                .locations(List.of(toLocationMetrics(null, merged)))
                .build();
        }

        List<LocationMetrics> perLocation = locCodes.stream()
            .map(loc -> toLocationMetrics(Integer.parseInt(loc), periodData.getOrDefault(loc, Collections.emptyList())))
            .toList();

        return PeriodResult.builder().locations(perLocation).build();
    }

    private LocationMetrics toLocationMetrics(Integer locationCode, List<DbdResponseStats> rows) {

        int notResponded = sumJurorCountWhere(rows, row -> NOT_RESPONDED.equals(row.getResponseMethod()));
        int online = sumJurorCountWhere(rows, row -> ONLINE.equals(row.getResponseMethod()));
        int paper = sumJurorCountWhere(rows, row -> PAPER.equals(row.getResponseMethod()));

        Map<String, Integer> ageGroupBreakdown = rows.stream()
            .collect(Collectors.groupingBy(DbdResponseStats::getAgeGroup,
                                           Collectors.summingInt(DbdResponseStats::getJurorCount)));

        int responded = online + paper;
        int summoned = responded + notResponded;

        Integer responseRatePercent = calculatePercentage(responded, summoned);
        Integer digitalResponsesPercent = calculatePercentage(online, responded);

        Map<String, Integer> ageGroupBreakdownPercent =
            expressCountsAsPercentageOfTotal(ageGroupBreakdown, summoned);

        DashboardMandatoryKpiData.ResponseMethod onlineResponseTimes = countResponsesByResponsePeriod(rows, ONLINE);
        DashboardMandatoryKpiData.ResponseMethod paperResponseTimes = countResponsesByResponsePeriod(rows, PAPER);
        ResponseTimesPercent responseTimesPercent =
            combineOnlineAndPaperResponsePeriodsAsPercentages(onlineResponseTimes, paperResponseTimes, responded);

        return LocationMetrics.builder()
            .locationCode(locationCode)
            .totalResponses(responded)
            .notRespondedTotal(notResponded)
            .onlineResponseTotal(online)
            .paperResponseTotal(paper)
            // TODO: thirdPartyTotal isn't sourced from dbd_response_stats - wire in once the
            // pilot-scoped third-party table/proc exists, following the same fetch-once pattern.
            .thirdPartyTotal(null)
            .responseRatePercent(responseRatePercent)
            .digitalResponsesPercent(digitalResponsesPercent)
            .onlineResponseTimes(onlineResponseTimes)
            .paperResponseTimes(paperResponseTimes)
            .responseTimesPercent(responseTimesPercent)
            .ageGroupBreakdown(ageGroupBreakdown)
            .ageGroupBreakdownPercent(ageGroupBreakdownPercent)
            .build();
    }

    /**
     * Counts, for one response method (online or paper), how many jurors fall into each
     * response_period (within7days/within14days/within21days/over21days) already assigned
     * by the dbd_responses stored procedure - no day-range math happens here.
     */
    private DashboardMandatoryKpiData.ResponseMethod countResponsesByResponsePeriod(
        List<DbdResponseStats> rows, String responseMethod) {

        Map<String, Integer> jurorCountByResponsePeriod = rows.stream()
            .filter(row -> responseMethod.equals(row.getResponseMethod()))
            .collect(Collectors.groupingBy(DbdResponseStats::getResponsePeriod,
                                           Collectors.summingInt(DbdResponseStats::getJurorCount)));

        return DashboardMandatoryKpiData.ResponseMethod.builder()
            .within7days(jurorCountByResponsePeriod.getOrDefault(WITHIN_7_DAYS, 0))
            .within14days(jurorCountByResponsePeriod.getOrDefault(WITHIN_14_DAYS, 0))
            .within21days(jurorCountByResponsePeriod.getOrDefault(WITHIN_21_DAYS, 0))
            .over21days(jurorCountByResponsePeriod.getOrDefault(OVER_21_DAYS, 0))
            .build();
    }

    /**
     * Adds the online and paper counts for each response_period together and expresses that
     * combined figure as a percentage of all responses received (online + paper) - one shared set
     * of response_period percentages, matching the single "Response times by court" %-column set
     * in the dashboard, rather than separate online-% and paper-% breakdowns.
     */
    private ResponseTimesPercent combineOnlineAndPaperResponsePeriodsAsPercentages(
        DashboardMandatoryKpiData.ResponseMethod onlineResponseTimes,
        DashboardMandatoryKpiData.ResponseMethod paperResponseTimes,
        int totalResponded) {

        if (totalResponded == 0) {
            return ResponseTimesPercent.builder().build();
        }

        return ResponseTimesPercent.builder()
            .within7DaysPercent(calculatePercentage(
                onlineResponseTimes.getWithin7days() + paperResponseTimes.getWithin7days(), totalResponded))
            .within14DaysPercent(calculatePercentage(
                onlineResponseTimes.getWithin14days() + paperResponseTimes.getWithin14days(), totalResponded))
            .within21DaysPercent(calculatePercentage(
                onlineResponseTimes.getWithin21days() + paperResponseTimes.getWithin21days(), totalResponded))
            .over21DaysPercent(calculatePercentage(
                onlineResponseTimes.getOver21days() + paperResponseTimes.getOver21days(), totalResponded))
            .build();
    }

    /**
     * Re-expresses a map of raw counts (e.g. jurors per age group) as a percentage of the given
     * total, keeping the same keys so the percentage map lines up one-to-one with the count map.
     */
    private Map<String, Integer> expressCountsAsPercentageOfTotal(Map<String, Integer> countsByKey, int total) {
        if (total == 0) {
            return countsByKey.keySet().stream()
                .collect(Collectors.toMap(key -> key, key -> 0));
        }

        return countsByKey.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> calculatePercentage(entry.getValue(), total)));
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
