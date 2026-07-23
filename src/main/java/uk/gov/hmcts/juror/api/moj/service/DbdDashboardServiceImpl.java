package uk.gov.hmcts.juror.api.moj.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import uk.gov.hmcts.juror.api.bureau.controller.response.DashboardMandatoryKpiData;
import uk.gov.hmcts.juror.api.moj.controller.request.DbdDashboardRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DbdDashboardResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DbdDashboardResponseDto.CourtGroupResult;
import uk.gov.hmcts.juror.api.moj.controller.response.DbdDashboardResponseDto.LocationMetrics;
import uk.gov.hmcts.juror.api.moj.controller.response.DbdDashboardResponseDto.PeriodResult;
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

    @Autowired
    public DbdDashboardServiceImpl(DbdResponseStatsRepository dbdResponseStatsRepository) {
        Assert.notNull(dbdResponseStatsRepository, "DbdResponseStatsRepository cannot be null");
        this.dbdResponseStatsRepository = dbdResponseStatsRepository;
    }

    @Override
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

        return DbdDashboardResponseDto.builder().courtGroups(results).build();
    }

    private Map<String, List<DbdResponseStats>> fetchByLocCode(
        Set<String> locCodes, DbdDashboardRequestDto.DateRangeDto range) {

        var start = range.getStartDate().atStartOfDay();
        var end = range.getEndDate().atTime(23, 59, 59);

        List<DbdResponseStats> rows =
            dbdResponseStatsRepository.findByLocCodeInAndSummonsDateBetween(locCodes, start, end);

        return rows.stream().collect(Collectors.groupingBy(DbdResponseStats::getLocCode));
    }

    private CourtGroupResult buildGroupResult(
        DbdDashboardRequestDto.CourtGroupDto group,
        Map<String, List<DbdResponseStats>> periodAData,
        Map<String, List<DbdResponseStats>> periodBData,
        boolean sumGroups) {

        List<String> groupLocCodes = group.getGroupLocations().stream()
            .map(loc -> String.format("%03d", loc))
            .toList();

        return CourtGroupResult.builder()
            .groupName(group.getGroupName())
            .periodA(buildPeriodResult(groupLocCodes, periodAData, sumGroups))
            .periodB(periodBData != null ? buildPeriodResult(groupLocCodes, periodBData, sumGroups) : null)
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

        int notResponded = sumWhere(rows, row -> NOT_RESPONDED.equals(row.getResponseMethod()));
        int online = sumWhere(rows, row -> ONLINE.equals(row.getResponseMethod()));
        int paper = sumWhere(rows, row -> PAPER.equals(row.getResponseMethod()));

        Map<String, Integer> ageGroupBreakdown = rows.stream()
            .collect(Collectors.groupingBy(DbdResponseStats::getAgeGroup,
                                           Collectors.summingInt(DbdResponseStats::getJurorCount)));

        return LocationMetrics.builder()
            .locationCode(locationCode)
            .notRespondedTotal(notResponded)
            .onlineResponseTotal(online)
            .paperResponseTotal(paper)
            // TODO: thirdPartyTotal isn't sourced from dbd_response_stats - wire in once the
            // pilot-scoped third-party table/proc exists, following the same fetch-once pattern.
            .thirdPartyTotal(null)
            .onlineResponseTimes(bucketByResponsePeriod(rows, ONLINE))
            .paperResponseTimes(bucketByResponsePeriod(rows, PAPER))
            .ageGroupBreakdown(ageGroupBreakdown)
            .build();
    }

    private DashboardMandatoryKpiData.ResponseMethod bucketByResponsePeriod(
        List<DbdResponseStats> rows, String responseMethod) {

        Map<String, Integer> byPeriod = rows.stream()
            .filter(row -> responseMethod.equals(row.getResponseMethod()))
            .collect(Collectors.groupingBy(DbdResponseStats::getResponsePeriod,
                                           Collectors.summingInt(DbdResponseStats::getJurorCount)));

        return DashboardMandatoryKpiData.ResponseMethod.builder()
            .within7days(byPeriod.getOrDefault(WITHIN_7_DAYS, 0))
            .within14days(byPeriod.getOrDefault(WITHIN_14_DAYS, 0))
            .within21days(byPeriod.getOrDefault(WITHIN_21_DAYS, 0))
            .over21days(byPeriod.getOrDefault(OVER_21_DAYS, 0))
            .build();
    }

    private int sumWhere(List<DbdResponseStats> rows, Predicate<DbdResponseStats> filter) {
        return rows.stream()
            .filter(filter)
            .mapToInt(DbdResponseStats::getJurorCount)
            .sum();
    }
}
