package uk.gov.hmcts.juror.api.moj.service;

import com.querydsl.core.Tuple;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.HolidaysRepository;
import uk.gov.hmcts.juror.api.moj.controller.request.ActivePoolFilterQuery;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolRequestedFilterQuery;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolNumbersListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestActiveDataDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestDataDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolsAtCourtLocationListDto;
import uk.gov.hmcts.juror.api.moj.domain.DayType;
import uk.gov.hmcts.juror.api.moj.domain.HistoryCode;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.PoolHistory;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.exception.CurrentlyDeferredException;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.PoolRequestException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.IActivePoolsRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolTypeRepository;
import uk.gov.hmcts.juror.api.moj.service.deferralmaintenance.ManageDeferralsService;
import uk.gov.hmcts.juror.api.moj.utils.CourtLocationUtils;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.juror.api.juror.domain.HolidaysQueries.isCourtHoliday;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PoolRequestServiceImpl implements PoolRequestService {

    private static final Character NEW_REQUEST_STATE = 'Y';
    private static final Character CREATED_REQUEST_STATE = 'N';

    private final PoolRequestRepository poolRequestRepository;
    private final CourtLocationRepository courtLocationRepository;
    private final PoolTypeRepository poolTypeRepository;
    private final HolidaysRepository holidaysRepository;
    private final ManageDeferralsService manageDeferralsService;
    private final PoolHistoryRepository poolHistoryRepository;
    private final IActivePoolsRepository activePoolsRepository;

    @Override
    @Transactional(readOnly = true)
    public PaginatedList<PoolRequestDataDto> getFilteredPoolRequests(PoolRequestedFilterQuery filterQuery) {
        return poolRequestRepository.getPoolRequestList(filterQuery);
    }

    /**
     * Persist a new instance of a PoolRequest object in to the database mapping properties from a supplied DTO.
     *
     * @param poolRequestDto Data properties for the new Pool Request record supplied via a JSON payload
     */
    @Override
    @Transactional(noRollbackFor = {PoolRequestException.PoolRequestNotFound.class,
        CurrentlyDeferredException.DeferredMemberNotFound.class})
    public void savePoolRequest(PoolRequestDto poolRequestDto, BureauJwtPayload payload) {
        log.trace("Enter savePoolRequest");

        validateNewPoolRequest(poolRequestDto);

        log.debug(String.format("Pool Request: %s to be created (court-only flag: %s)",
            poolRequestDto.getPoolNumber(), poolRequestDto.isCourtOnly()));

        PoolRequest poolRequest = poolRequestDto.isCourtOnly()
            ? createPoolForCourtUse(poolRequestDto, payload)
            : requestPoolFromBureau(poolRequestDto, payload);

        poolRequestRepository.saveAndFlush(poolRequest);

        log.trace("Exit savePoolRequest");
    }

    /**
     * For Requesting a pool - only the count of Court deferrals is required for a given court location and
     * a given attendance date, the owner value will match the given location code indicating it is a Court deferral.
     *
     * @param locationCode 3-digit numeric string unique identifier for court locations
     * @param deferredTo   the date the pool is being requested for to check if there are any jurors who have
     *                     deferred to this date
     * @return a count of deferral records matching the predicate criteria
     */
    @Override
    @Transactional(readOnly = true)
    public long getCourtDeferrals(String locationCode, LocalDate deferredTo) {
        CourtLocation courtLocation = RepositoryUtils.retrieveFromDatabase(locationCode, courtLocationRepository);
        return manageDeferralsService.getDeferralsCount(courtLocation.getOwner(), locationCode, deferredTo);
    }

    /**
     * Validate the requested date to ensure it does not fall on a weekend or is not recorded in the
     * database as a holiday for the given court.
     *
     * @param attendanceDate the date the pool is being requested for, when jurors are first expected to attend court
     * @param locationCode   3-digit numeric string unique identifier for court locations
     * @return Whether the date is a valid BUSINESS_DAY or an invalid WEEKEND or other HOLIDAY
     */
    @Override
    @Transactional(readOnly = true)
    public DayType checkAttendanceDate(@NotNull LocalDate attendanceDate, @NotBlank String locationCode) {
        log.debug(String.format("Check attendance date %s for court location %s", attendanceDate, locationCode));
        DayOfWeek dayOfWeek = attendanceDate.getDayOfWeek();
        switch (dayOfWeek) {
            case SATURDAY, SUNDAY:
                return DayType.WEEKEND;
            default:
                break;
        }

        // validate the court location exists
        RepositoryUtils.retrieveFromDatabase(locationCode, courtLocationRepository);

        return holidaysRepository.findOne(isCourtHoliday(locationCode, attendanceDate))
            .isPresent() ? DayType.HOLIDAY : DayType.BUSINESS_DAY;
    }

    /**
     * Get pool numbers using pool number prefix.
     *
     * @param poolNumberPrefix The first 7 characters of a pool number containing the court location code,
     *                         attendance date year (yy) and attendance date month (mm)
     * @return A list of distinct pool numbers and their attendance date for the given poolNumberPrefix
     */
    @Override
    public PoolNumbersListDto getPoolNumbers(String poolNumberPrefix) {
        log.trace(String.format("Enter findAllPoolNumbersByPoolNumberPrefix: %s", poolNumberPrefix));
        int poolNumberIndex = 0;
        int attendanceDateIndex = 1;
        List<PoolNumbersListDto.PoolNumbersDataDto> poolNumbers = new ArrayList<>();

        Iterable<Tuple> poolRequestRecords;
        poolRequestRecords = poolRequestRepository.findAllPoolNumbersByPoolNumberPrefix(poolNumberPrefix);

        poolRequestRecords.forEach(poolRequest -> {
            log.debug(String.format(
                "Mapping pool data: %s to DTO",
                poolRequest.get(poolNumberIndex, String.class)
            ));
            PoolNumbersListDto.PoolNumbersDataDto poolNumbersData =
                new PoolNumbersListDto.PoolNumbersDataDto(
                    poolRequest.get(poolNumberIndex, String.class),
                    poolRequest.get(attendanceDateIndex, LocalDate.class)
                );

            poolNumbers.add(poolNumbersData);
            log.trace(String.format("Pool number and attendance date added: %s", poolNumbersData));
        });

        log.debug(String.format("%d pools retrieved", poolNumbers.size()));
        return new PoolNumbersListDto(poolNumbers);
    }

    /**
     * Validate that the newly requested pool is not a duplicate - make sure the pool number is not in use for any
     * other pools.
     *
     * @param poolRequestDto data properties for the new Pool Request record supplied via a JSON payload
     */
    private void validateNewPoolRequest(PoolRequestDto poolRequestDto) {
        String poolNumber = poolRequestDto.getPoolNumber();

        Optional<PoolRequest> existingPoolRequest = poolRequestRepository.findById(poolNumber);

        if (existingPoolRequest.isPresent()) {
            throw new PoolRequestException.DuplicatePoolRequest(poolNumber);
        }
    }

    /**
     * Initialise an instance of a Pool Request object using the properties from a poolRequestDTO. Requested pool
     * will be owned by the bureau initially for jurors to be summoned
     *
     * @return a new PoolRequest object
     */
    private PoolRequest requestPoolFromBureau(PoolRequestDto poolRequestDto, BureauJwtPayload payload) {
        String login = payload.getLogin();

        PoolRequest poolRequest = convertFromDto(poolRequestDto, payload.getOwner(), payload.getLogin());
        poolRequest.setOwner(JurorDigitalApplication.JUROR_OWNER);

        poolRequestRepository.save(poolRequest);

        useDeferrals(poolRequest, poolRequestDto.getDeferralsUsed(), login);
        return poolRequest;
    }

    /**
     * Initialise an instance of a Pool Request object using the properties from a poolRequestDTO. Requested pool is
     * for court use only - this pool will never be owned by the bureau
     *
     * @return a new PoolRequest object
     */
    private PoolRequest createPoolForCourtUse(PoolRequestDto poolRequestDto, BureauJwtPayload payload) {
        String owner = payload.getOwner();

        if (owner.equalsIgnoreCase(JurorDigitalApplication.JUROR_OWNER)) {
            throw new MojException.Forbidden("Bureau users are not permitted to create new pools "
                + "for court use only", null);
        }

        PoolRequest poolRequest = convertFromDto(poolRequestDto, owner, payload.getLogin());
        poolRequest.setOwner(payload.getOwner());
        poolRequest.setNewRequest(CREATED_REQUEST_STATE);
        poolRequest.setNumberRequested(null);

        poolRequestRepository.save(poolRequest);

        return poolRequest;
    }

    private PoolRequest convertFromDto(PoolRequestDto poolRequestDto, String owner, String login) {
        String courtLocationCode = poolRequestDto.getLocationCode();

        log.debug("Retrieve the Court Location object from the database for: {}", courtLocationCode);
        CourtLocation courtLocation = RepositoryUtils.retrieveFromDatabase(poolRequestDto.getLocationCode(),
            courtLocationRepository);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(poolRequestDto.getPoolNumber());
        poolRequest.setOwner(owner);
        poolRequest.setCourtLocation(courtLocation);
        poolRequest.setNewRequest(NEW_REQUEST_STATE);
        poolRequest.setReturnDate(poolRequestDto.getAttendanceDate());
        poolRequest.setTotalNoRequired(poolRequestDto.getNumberRequested());
        updatePoolHistory(poolRequestDto.getPoolNumber(), login,
            String.format("Pool Requested for %s Jurors", poolRequestDto.getNumberRequested()));

        poolRequest.setNumberRequested(poolRequestDto.getNumberRequested());
        if (poolRequestDto.getAttendanceTime() != null) {
            poolRequest.setAttendTime(LocalDateTime.of(poolRequestDto.getAttendanceDate(),
                poolRequestDto.getAttendanceTime()));
        }

        log.debug("Retrieve the Pool Type object from the database for: {}", poolRequestDto.getPoolType());
        poolRequest.setPoolType(RepositoryUtils.retrieveFromDatabase(poolRequestDto.getPoolType(), poolTypeRepository));

        poolRequestRepository.save(poolRequest);

        return poolRequest;
    }

    private void updatePoolHistory(String poolNumber, String login, String otherInformation) {
        poolHistoryRepository.save(
            new PoolHistory(poolNumber, LocalDateTime.now(), HistoryCode.PREQ, login, otherInformation));
    }

    private void useDeferrals(PoolRequest poolRequest, int deferralsRequested, String userId) {
        if (deferralsRequested > 0) {
            int actualDeferralsUsed = manageDeferralsService.useCourtDeferrals(poolRequest, deferralsRequested, userId);
            log.trace(String.format("Out of %d requested deferrals, %d deferrals have successfully been used for "
                + "pool: %s", deferralsRequested, actualDeferralsUsed, poolRequest.getPoolNumber()));

            int numberRequested = poolRequest.getNumberRequested();
            poolRequest.setNumberRequested(numberRequested - actualDeferralsUsed);
            log.debug(String.format("Number requested for this pool has been adjusted from %d to %d (deferrals used)",
                numberRequested, numberRequested - actualDeferralsUsed
            ));
        }
    }

    @Override
    public PaginatedList<PoolRequestActiveDataDto> getActivePoolRequests(ActivePoolFilterQuery filterQuery) {
        return activePoolsRepository.getActivePoolRequests(filterQuery);
    }

    @Override
    public PaginatedList<PoolRequestActiveDataDto> getActivePoolUnderResponded(ActivePoolFilterQuery filterQuery) {
        return activePoolsRepository.getActivePoolUnderResponded(filterQuery);
    }

    @Override
    @Transactional(readOnly = true)
    public PoolsAtCourtLocationListDto getActivePoolsAtCourtLocation(String locCode) {

        BureauJwtPayload payload = SecurityUtil.getActiveUsersBureauPayload();
        String userLogin = payload.getLogin();

        //check if user is allowed to query the locCode supplied
        CourtLocationUtils.validateAccessToCourtLocation(locCode, payload.getOwner(), courtLocationRepository);

        log.debug("User {}, Retrieving active Pool Requests at Court location {}", userLogin, locCode);
        List<PoolsAtCourtLocationListDto.PoolsAtCourtLocationDataDto> data = new ArrayList<>();
        try {
            List<String> poolsListing = poolRequestRepository.findPoolsByCourtLocation(locCode);
            logRecordsFound(poolsListing, userLogin);
            populatePoolsListing(poolsListing, data);
        } catch (Exception e) {
            logErrorRetrievingPools(locCode);
            throw new MojException.InternalServerError("Error retrieving pools at court location " + locCode, e);
        }
        return new PoolsAtCourtLocationListDto(data);
    }

    private static void logErrorRetrievingPools(String locCode) {
        log.error("Error retrieving active pools at court location {}", locCode);
    }

    private static void logRecordsFound(List<String> poolsListing, String userLogin) {
        log.debug("Found {} active pool records for current user, {}", poolsListing.size(), userLogin);
    }

    @Override
    @Transactional(readOnly = true)
    public PoolsAtCourtLocationListDto getAllActivePoolsAtCourtLocation(String locCode) {
        BureauJwtPayload payload = SecurityUtil.getActiveUsersBureauPayload();
        String userLogin = payload.getLogin();

        //check if user is allowed to query the locCode supplied
        CourtLocationUtils.validateAccessToCourtLocation(locCode, payload.getOwner(), courtLocationRepository);

        log.debug("User {}, Retrieving all active Pool Requests at Court location {}", userLogin, locCode);
        List<PoolsAtCourtLocationListDto.PoolsAtCourtLocationDataDto> data = new ArrayList<>();
        try {
            List<String> poolsListing = poolRequestRepository.findAllPoolsByCourtLocation(locCode);
            logRecordsFound(poolsListing, userLogin);
            populatePoolsListing(poolsListing, data);
        } catch (Exception e) {
            logErrorRetrievingPools(locCode);
            throw new MojException.InternalServerError("Error retrieving pools at court location " + locCode, e);
        }
        return new PoolsAtCourtLocationListDto(data);
    }

    private static void populatePoolsListing(List<String> poolsListing,
                                             List<PoolsAtCourtLocationListDto.PoolsAtCourtLocationDataDto> data) {
        poolsListing.forEach(pool -> {
            List<String> poolDetails = Arrays.asList(pool.split(","));

            int jurorsInAttendance = poolDetails.get(2).equals("null") ? 0 : Integer.parseInt(poolDetails.get(2));
            int jurorsOnCall = Integer.parseInt(poolDetails.get(3));

            // total possible in attendance - in attendance
            int others = Integer.parseInt(poolDetails.get(1)) - jurorsInAttendance;

            PoolsAtCourtLocationListDto.PoolsAtCourtLocationDataDto poolsAtCourtLocationDataDto =
                PoolsAtCourtLocationListDto.PoolsAtCourtLocationDataDto.builder()
                    .poolNumber(poolDetails.get(0))
                    .jurorsInAttendance(jurorsInAttendance)
                    .jurorsOnCall(jurorsOnCall)
                    .otherJurors(others)
                    .totalJurors(jurorsInAttendance + jurorsOnCall + others)
                    .jurorsOnTrials(poolDetails.get(4).equals("null") ? 0 : Integer.parseInt(poolDetails.get(4)))
                    .poolType(poolDetails.get(6))
                    .serviceStartDate(LocalDate.parse(poolDetails.get(7)))
                    .build();
            data.add(poolsAtCourtLocationDataDto);
        });
    }

    @Override
    public LocalDateTime getPoolAttendanceTime(String poolId) {
        log.trace("Looking up pool attendance time for pool ID {}", poolId);
        Optional<PoolRequest> optPoolRequest = poolRequestRepository.findById(poolId);
        final PoolRequest poolRequest = optPoolRequest.orElse(null);
        if (poolRequest == null) {
            log.trace("No pool request entry for pool ID {} (this is not necessarily an error)", poolId);
            return null;
        }
        final LocalDateTime attendanceTime = poolRequest.getAttendTime();
        if (attendanceTime == null) {
            log.trace("No attend time set in  pool for pool ID {}", poolId);
            return null;
        }
        log.trace("Attend time for pool ID {} is: {}", poolId, attendanceTime);

        return attendanceTime;
    }

    @Override
    public PoolRequest getPoolRequest(String poolNumber) {
        return poolRequestRepository.findById(poolNumber)
            .orElseThrow(() -> new MojException.NotFound("Pool Number not found", null));
    }
}
