package uk.gov.hmcts.juror.api.moj.service.poolmanagement;

import com.querydsl.core.Tuple;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.response.SummoningProgressResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.poolmanagement.AvailablePoolsInCourtLocationDto;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.enumeration.PoolUtilisationDescription;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolStatisticsRepository;
import uk.gov.hmcts.juror.api.moj.utils.DateUtils;
import uk.gov.hmcts.juror.api.moj.utils.NumberUtils;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static uk.gov.hmcts.juror.api.moj.utils.NumberUtils.unboxIntegerValues;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManagePoolsServiceImpl implements ManagePoolsService {

    @NonNull
    CourtLocationRepository courtLocationRepository;
    @NonNull
    PoolRequestRepository poolRequestRepository;

    @NonNull
    PoolStatisticsRepository poolStatisticsRepository;


    @Override
    @Transactional(readOnly = true)
    public AvailablePoolsInCourtLocationDto findAvailablePools(String locCode, BureauJWTPayload payload) {
        log.trace("Location code: {}: Enter method findAvailablePools", locCode);

        String owner = payload.getOwner();

        // validate the user has access to the court location -
        // bureau user will have it, court user should have access to the court and secondary courts
        if (owner.equals(JurorDigitalApplication.JUROR_OWNER)) {
            RepositoryUtils.retrieveFromDatabase(locCode, courtLocationRepository);
        } else {
            validateCourtLocationAndOwnership(locCode, owner);
        }

        log.debug("Court location code {}: Find available pools for user {}", locCode, payload.getLogin());
        AvailablePoolsInCourtLocationDto availablePools = new AvailablePoolsInCourtLocationDto();
        availablePools.setAvailablePools(populateAvailablePoolsDto(locCode, owner));

        log.trace("Court location code {}: Exit method findAvailablePools", locCode);
        return availablePools;
    }

    @Override
    @Transactional(readOnly = true)
    public AvailablePoolsInCourtLocationDto findAvailablePoolsCourtOwned(String locCode, BureauJWTPayload payload) {
        log.trace("Location code: {}: Enter method findAvailablePoolsCourtOwned", locCode);

        String owner = payload.getOwner();

        validateCourtLocationAndOwnership(locCode, owner);

        log.debug("Court location code {}: Find available pools for user {}", locCode, payload.getLogin());

        List<AvailablePoolsInCourtLocationDto.AvailablePoolsDto> availablePoolsDtos =
            populateAvailablePoolsDto(locCode, owner);

        List<String> poolNumbers = availablePoolsDtos.stream().map(pool -> pool.getPoolNumber()).toList();

        List<PoolRequest> poolRequests = poolRequestRepository.findByOwnerAndPoolNumberIn(owner, poolNumbers);

        List<String> courtOwnedPoolNumbers = poolRequests.stream()
            .map(poolRequest -> poolRequest.getPoolNumber()).toList();

        List<AvailablePoolsInCourtLocationDto.AvailablePoolsDto> availablePoolsDtosCourtOwned =
            availablePoolsDtos.stream()
                .filter(pool -> courtOwnedPoolNumbers.contains(pool.getPoolNumber())).toList();

        AvailablePoolsInCourtLocationDto availablePools = new AvailablePoolsInCourtLocationDto();
        availablePools.setAvailablePools(availablePoolsDtosCourtOwned);

        log.trace("Court location code {}: Exit method findAvailablePoolsCourtOwned", locCode);
        return availablePools;
    }

    private void validateCourtLocationAndOwnership(String locCode, String owner) {
        List<CourtLocation> courtLocations = courtLocationRepository.findByOwner(owner);
        if (courtLocations.isEmpty()) {
            throw new MojException.NotFound(String.format(
                "Juror record owner: %s - No records found for the given owner",
                owner
            ), null);
        }
        if (courtLocations.stream().noneMatch(courtLocation -> courtLocation.getLocCode().equals(locCode))) {
            throw new MojException.NotFound(String.format(
                "Court location: %s - No records found for the given location code",
                locCode
            ), null);
        }
    }

    @Override
    public SummoningProgressResponseDto getPoolMonitoringStats(BureauJWTPayload payload, String courtLocationCode,
                                                               String poolType) {
        final int numberOfWeeks = 8;

        List<Tuple> nilPools =
            poolStatisticsRepository.getNilPools(payload.getOwner(), courtLocationCode, poolType, numberOfWeeks);

        if (nilPools == null) {
            nilPools = new ArrayList<>();
        }

        List<Tuple> poolStatistics = poolStatisticsRepository
            .getStatisticsByCourtLocationAndPoolType(payload.getOwner(), courtLocationCode, poolType, numberOfWeeks);

        if (poolStatistics == null) {
            poolStatistics = new ArrayList<>();
        }

        if (poolStatistics.isEmpty() && nilPools.isEmpty()) {
            throw new MojException.NotFound("Court location with pool type cannot be found", null);
        }

        LocalDate startOfWorkingNextWeek = DateUtils.getStartOfWeekFromDate(LocalDate.now());
        List<LocalDate> eightWeeks = DateUtils.getNumberOfStartingWeeks(numberOfWeeks, startOfWorkingNextWeek);

        return getPoolMonitoringStatsForPeriod(poolStatistics, nilPools, eightWeeks);
    }

    private SummoningProgressResponseDto getPoolMonitoringStatsForPeriod(final List<Tuple> poolStatistics,
                                                                         final List<Tuple> nilPools,
                                                                         List<LocalDate> datePeriod) {
        SummoningProgressResponseDto dto = new SummoningProgressResponseDto();
        List<SummoningProgressResponseDto.WeekFilter> weekStatistics = new ArrayList<>();

        datePeriod.forEach(startOfWeek -> {
            List<SummoningProgressResponseDto.SummoningProgressStats> listStats = new ArrayList<>();
            List<Tuple> poolStats = new ArrayList<>();
            List<Tuple> nilPool = new ArrayList<>();

            if (!poolStatistics.isEmpty()) {
                poolStats = poolStatistics.stream().filter(p -> {
                    LocalDate serviceStartDate = Objects.requireNonNull(p.get(0, LocalDate.class));
                    return serviceStartDate.isAfter(startOfWeek.minusDays(1))
                        && serviceStartDate.isBefore(startOfWeek.plusWeeks(1));
                }).toList();
            }

            if (!nilPools.isEmpty()) {
                nilPool = nilPools.stream().filter(p -> {
                    LocalDate serviceStartDate = Objects.requireNonNull(p.get(2, LocalDate.class));
                    return serviceStartDate.isAfter(startOfWeek.minusDays(1))
                        && serviceStartDate.isBefore(startOfWeek.plusWeeks(1));
                }).toList();
            }

            populateStatisticsFromPoolStats(poolStats, listStats);
            populateStatisticsFromNilPools(nilPool, listStats);

            SummoningProgressResponseDto.WeekFilter week = new SummoningProgressResponseDto.WeekFilter();
            week.setStartOfWeek(startOfWeek);
            week.setStats(listStats);
            weekStatistics.add(week);
        });

        dto.setStatsByWeek(weekStatistics);
        return dto;
    }

    private void populateStatisticsFromPoolStats(List<Tuple> pool,
                                                 List<SummoningProgressResponseDto.SummoningProgressStats> listStats) {
        for (Tuple t : pool) {
            listStats.add(createSummoningProgressStat(
                t.get(0, LocalDate.class),
                t.get(1, String.class),
                NumberUtils.unboxIntegerValues(t.get(7, int.class)),
                NumberUtils.unboxIntegerValues(t.get(6, int.class)),
                NumberUtils.unboxIntegerValues(t.get(4, int.class)),
                NumberUtils.unboxIntegerValues(t.get(5, int.class))));
        }
    }

    private void populateStatisticsFromNilPools(List<Tuple> nilPool,
                                                List<SummoningProgressResponseDto.SummoningProgressStats> listStats) {
        for (Tuple t : nilPool) {
            List<SummoningProgressResponseDto.SummoningProgressStats> results =
                listStats.stream().filter(p -> p.getPoolNumber().equals(t.get(0, String.class))).toList();
            // checking to see if a nil pool has been picked up from the POOL_STATS view - to remove duplication
            if (!results.isEmpty()) {
                continue;
            }

            listStats.add(createSummoningProgressStat(
                t.get(2, LocalDate.class),
                t.get(0, String.class),
                NumberUtils.unboxIntegerValues(t.get(1, int.class)),
                0, 0, 0));
        }
    }

    private List<AvailablePoolsInCourtLocationDto.AvailablePoolsDto> populateAvailablePoolsDto(String courtLocation,
                                                                                               String owner) {
        log.debug("Owner: {}, Court Location: {} - Get available active pools for the given court location", owner,
            courtLocation);

        // Get all active pools with minimum time span of 1 year ago
        LocalDate weekCommencing = DateUtils.getStartOfWeekFromDate(LocalDate.now().minusYears(1));
        List<Tuple> activePoolsData = poolRequestRepository.findActivePoolsForDateRange(owner, courtLocation,
            weekCommencing, null);

        log.debug("Owner: {}, Court Location: {} - Found {} available active pools for the given court location",
            owner, courtLocation, activePoolsData.size());

        return mapAvailablePoolsToDto(activePoolsData, owner);
    }

    private List<AvailablePoolsInCourtLocationDto.AvailablePoolsDto> mapAvailablePoolsToDto(List<Tuple> activePoolsData,
                                                                                            String owner) {
        log.trace("Juror record owner: {} - Enter method - mapActivePoolStatsToDto", owner);

        List<AvailablePoolsInCourtLocationDto.AvailablePoolsDto> availablePoolsList = new ArrayList<>();

        for (Tuple activePool : activePoolsData) {
            AvailablePoolsInCourtLocationDto.AvailablePoolsDto availablePool =
                new AvailablePoolsInCourtLocationDto.AvailablePoolsDto();
            availablePool.setPoolNumber(activePool.get(0, String.class));
            availablePool.setServiceStartDate(activePool.get(1, LocalDate.class));

            int confirmedPoolMembers = unboxIntegerValues(activePool.get(3, Integer.class));

            if (owner.equalsIgnoreCase(JurorDigitalApplication.JUROR_OWNER)) {
                log.debug("Juror record owner: {} - Calculate current pool utilisation stats for {}", owner,
                    activePool.get(0, String.class));
                int bureauUtilisation = calculateUtilisation(activePool.get(2, Integer.class), confirmedPoolMembers);
                log.debug("Juror record owner: {} - Calculate current pool utilisation calculated as {}", owner,
                    bureauUtilisation);

                availablePool.setUtilisation(Math.abs(bureauUtilisation));
                availablePool.setUtilisationDescription(bureauUtilisation < 0
                    ? PoolUtilisationDescription.SURPLUS
                    : PoolUtilisationDescription.NEEDED);
            } else {
                availablePool.setUtilisation(Math.abs(confirmedPoolMembers));
                availablePool.setUtilisationDescription(PoolUtilisationDescription.CONFIRMED);
            }
            availablePoolsList.add(availablePool);
        }
        log.trace("Juror record owner: {} - Exit method mapActivePoolStatsToDto", owner);
        return availablePoolsList;
    }

    private int calculateUtilisation(Integer requested, int poolMemberCount) {
        int numberRequested = unboxIntegerValues(requested);
        return numberRequested - poolMemberCount;
    }

    private SummoningProgressResponseDto.SummoningProgressStats createSummoningProgressStat(
        LocalDate serviceStartDate, String poolNumber, int numberOfRequested,
        int summoned, int confirmed, int unavailable) {
        SummoningProgressResponseDto.SummoningProgressStats stats
            = new SummoningProgressResponseDto.SummoningProgressStats();

        stats.setServiceStartDate(serviceStartDate);
        stats.setPoolNumber(poolNumber);
        stats.setRequested(numberOfRequested);
        if (stats.getRequested() != 0) {
            stats.setSummoned(summoned);
        }
        stats.setConfirmed(confirmed);
        stats.setBalance(stats.getConfirmed() - stats.getRequested());
        stats.setUnavailable(unavailable);
        return stats;
    }
}
