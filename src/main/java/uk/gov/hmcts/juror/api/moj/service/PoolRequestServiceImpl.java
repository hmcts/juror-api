package uk.gov.hmcts.juror.api.moj.service;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.Expressions;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.HolidaysRepository;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolNumbersListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestActiveListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolsAtCourtLocationListDto;
import uk.gov.hmcts.juror.api.moj.domain.ActivePoolsBureau;
import uk.gov.hmcts.juror.api.moj.domain.ActivePoolsCourt;
import uk.gov.hmcts.juror.api.moj.domain.DayType;
import uk.gov.hmcts.juror.api.moj.domain.HistoryCode;
import uk.gov.hmcts.juror.api.moj.domain.PoolHistory;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequestListAndCount;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.exception.CurrentlyDeferredException;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.PoolRequestException;
import uk.gov.hmcts.juror.api.moj.repository.ActivePoolsBureauRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.IActivePoolsCourtRepository;
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
    private static final String BUREAU_TAB = "bureau";
    private static final String COURT_TAB = "court";
    private static final int PAGE_SIZE = 25;
    private static final int ACTIVE_POOL_DAYS_LIMIT = 28;
    private static final List<String> POOL_TYPES_DESC_LIST = Arrays.asList("CIVIL COURT", "CROWN COURT", "HIGH COURT");

    @NonNull
    private final PoolRequestRepository poolRequestRepository;
    @NonNull
    private final CourtLocationRepository courtLocationRepository;
    @NonNull
    private final PoolTypeRepository poolTypeRepository;
    @NonNull
    private final HolidaysRepository holidaysRepository;
    @NonNull
    private final ManageDeferralsService manageDeferralsService;
    @NonNull
    private final PoolHistoryRepository poolHistoryRepository;
    @NonNull
    private final ActivePoolsBureauRepository activePoolsBureauRepository;
    @NonNull
    private final IActivePoolsCourtRepository activePoolsCourtRepository;

    /**
     * Execute a database query to return a filtered list of Pool Request records satisfying the criteria supplied and
     * convert the returned entity records into a DTO to serve back as a response object.
     *
     * @param courtLocation Unique 3 digit code to identify a specific court location
     * @return Data Transfer Object (DTO) containing a list of pool requests including their relevant properties
     */
    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Transactional(readOnly = true)
    public PoolRequestListDto getFilteredPoolRequests(BureauJwtPayload payload, String courtLocation,
                                                      int offset, String sortBy, String sortOrder) {
        PoolRequestListAndCount poolRequests;

        Path<Object> sortField = Expressions.path(Object.class, QPoolRequest.poolRequest, sortBy);
        OrderSpecifier<?> order;
        if (sortOrder.equals("asc")) {
            order = new OrderSpecifier(Order.ASC, sortField);
        } else {
            order = new OrderSpecifier(Order.DESC, sortField);
        }

        if (payload.getOwner().equals(JurorDigitalApplication.JUROR_OWNER)) {
            log.debug("Retrieving Pool Request for the current Bureau user");
            poolRequests = poolRequestRepository.findBureauPoolRequestsList(
                POOL_TYPES_DESC_LIST,
                courtLocation,
                offset,
                PAGE_SIZE,
                order
            );
        } else {
            log.debug("Retrieving Pool Request for the current Courts user");
            List<String> courts = payload.getStaff().getCourts();
            poolRequests = poolRequestRepository.findCourtsPoolRequestsList(
                courts,
                POOL_TYPES_DESC_LIST,
                courtLocation,
                offset,
                PAGE_SIZE,
                order
            );
        }
        return this.buildPoolRequestListDtoResponse(
            poolRequests.getPoolRequestList(),
            poolRequests.getPoolRequestCount()
        );
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

    private PoolRequestListDto buildPoolRequestListDtoResponse(Iterable<PoolRequest> poolRequestList,
                                                               Long poolRequestCount) {
        log.trace("Enter buildPoolRequestListDtoResponse");
        List<PoolRequestListDto.PoolRequestDataDto> poolRequests = new ArrayList<>();

        poolRequestList.forEach(poolRequest -> {
            log.debug(String.format(
                "Mapping pool request: %s to DTO",
                poolRequest.getPoolNumber()
            ));
            PoolRequestListDto.PoolRequestDataDto poolRequestData =
                new PoolRequestListDto.PoolRequestDataDto(poolRequest);

            poolRequests.add(poolRequestData);
            log.trace(String.format("Pool request data added: %s", poolRequestData));
        });

        log.debug(String.format("%d pool requests retrieved", poolRequests.size()));
        return new PoolRequestListDto(poolRequests, poolRequestCount);
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

        log.debug("Retrieve the Court Location object from the database for: " + courtLocationCode);
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

        log.debug("Retrieve the Pool Type object from the database for: " + poolRequestDto.getPoolType());
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
    public PoolRequestActiveListDto getActivePoolRequests(BureauJwtPayload payload, String locCode, String tab,
                                                          int offset, String sortBy, String sortOrder) {

        List<PoolRequestActiveListDto.PoolRequestActiveDataDto> data = new ArrayList<>();
        long totalSize = 0;
        Sort sort = sortOrder.equals("desc")
            ? Sort.by(sortBy).descending()
            : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(offset, PAGE_SIZE, sort);
        Page<ActivePoolsBureau> activePoolsBureauList;
        Page<ActivePoolsCourt> activePoolsCourtPage;
        List<ActivePoolsCourt> activePoolsCourtList;

        if (payload.getOwner().equals(JurorDigitalApplication.JUROR_OWNER)) {

            if (tab.equals(BUREAU_TAB)) {

                log.debug("Retrieving active Pool Requests at Bureau for the current Bureau user");

                if (locCode == null) {
                    activePoolsBureauList =
                        activePoolsBureauRepository.findByPoolTypeIn(POOL_TYPES_DESC_LIST, pageable);
                } else {
                    Optional<CourtLocation> courtLocationOpt = courtLocationRepository.findByLocCode(locCode);
                    if (courtLocationOpt.isPresent()) {
                        CourtLocation courtLocation = courtLocationOpt.get();
                        activePoolsBureauList =
                            activePoolsBureauRepository.findByPoolTypeInAndCourtName(POOL_TYPES_DESC_LIST,
                                courtLocation.getName(), pageable);
                    } else {
                        log.error("Invalid Location code parameter for active pools search {}", locCode);
                        throw new IllegalArgumentException(
                            "Invalid court location code supplied for active pool search: " + locCode);
                    }
                }

                if (activePoolsBureauList == null) {
                    //nothing found for search criteria, return empty list
                    return new PoolRequestActiveListDto(data, 0);
                }

                totalSize = populateAtBureauDataList(activePoolsBureauList, data);

            } else if (tab.equals(COURT_TAB)) {

                log.debug("Retrieving active Pool Requests at Court for the current Bureau user");

                //set a date to limit results
                LocalDate returnDateAfter = LocalDate.now().minusDays(ACTIVE_POOL_DAYS_LIMIT);

                if (locCode == null) {
                    activePoolsCourtList = activePoolsCourtRepository.findActivePools(null, returnDateAfter, sortBy,
                        sortOrder, POOL_TYPES_DESC_LIST);
                } else {
                    activePoolsCourtList = activePoolsCourtRepository.findActivePools(List.of(locCode), null,
                        sortBy, sortOrder, POOL_TYPES_DESC_LIST);
                }

                activePoolsCourtPage = convertListToPage(activePoolsCourtList, pageable);

                if (activePoolsCourtPage.isEmpty()) {
                    //nothing found for search criteria, return empty list
                    return new PoolRequestActiveListDto(data, 0);
                }

                totalSize = populateAtCourtDataList(activePoolsCourtPage, data);
            }

        } else {
            //This is a court user

            if (tab.equals(BUREAU_TAB)) {

                log.debug("Retrieving active Pool Requests at Bureau for the current Courts user");

                //find all courts user has access to and search for pool requests
                List<String> courts = payload.getStaff().getCourts();

                if (locCode == null) {
                    List<CourtLocation> courtLocations = courtLocationRepository.findByLocCodeIn(courts);
                    List<String> courtNames = new ArrayList<>();

                    for (CourtLocation c : courtLocations) {
                        courtNames.add(c.getName());
                    }
                    activePoolsBureauList = activePoolsBureauRepository.findByPoolTypeInAndCourtNameIn(
                        POOL_TYPES_DESC_LIST, courtNames, pageable);
                } else {

                    //check if user is allowed to query the locCode supplied
                    if (!courts.contains(locCode)) {
                        log.error("Location code is not in users courts list {}", locCode);
                        throw new IllegalArgumentException("Location code {} is not in users courts list" + locCode);
                    }

                    Optional<CourtLocation> courtLocationOpt = courtLocationRepository.findByLocCode(locCode);
                    if (courtLocationOpt.isPresent()) {
                        CourtLocation courtLocation = courtLocationOpt.get();
                        activePoolsBureauList = activePoolsBureauRepository
                            .findByPoolTypeInAndCourtName(POOL_TYPES_DESC_LIST, courtLocation.getName(), pageable);
                    } else {
                        log.error("Invalid Location code parameter for active pools search {}", locCode);
                        throw new IllegalArgumentException(
                            "Invalid court location code supplied for active pool search: " + locCode);
                    }
                }

                if (activePoolsBureauList == null) {
                    //nothing found for search criteria, return empty list
                    return new PoolRequestActiveListDto(data, 0);
                }

                totalSize = populateAtBureauDataList(activePoolsBureauList, data);

            } else if (tab.equals(COURT_TAB)) {

                log.debug("Retrieving active Pool Requests at Court for the current Courts user");

                if (locCode == null) {
                    //find all courts user has access to and search for pool requests
                    List<String> courts = payload.getStaff().getCourts();
                    activePoolsCourtList = activePoolsCourtRepository.findActivePools(
                        courts,
                        null,
                        sortBy,
                        sortOrder,
                        POOL_TYPES_DESC_LIST
                    );

                } else {
                    activePoolsCourtList = activePoolsCourtRepository.findActivePools(
                        List.of(locCode),
                        null,
                        sortBy,
                        sortOrder,
                        POOL_TYPES_DESC_LIST
                    );
                }

                activePoolsCourtPage = convertListToPage(activePoolsCourtList, pageable);

                if (activePoolsCourtPage.isEmpty()) {
                    //nothing found for search criteria, return empty list
                    return new PoolRequestActiveListDto(data, 0);
                }

                totalSize = populateAtCourtDataList(activePoolsCourtPage, data);
            }

        }
        log.debug(String.format("Found %s active pool records for current user, %s", totalSize, payload.getLogin()));
        return new PoolRequestActiveListDto(data, totalSize);
    }

    @Override
    @Transactional(readOnly = true)
    public PoolsAtCourtLocationListDto getActivePoolsAtCourtLocation(String locCode) {

        BureauJwtPayload payload = SecurityUtil.getActiveUsersBureauPayload();
        String userLogin = payload.getLogin();

        //check if user is allowed to query the locCode supplied
        CourtLocationUtils.validateAccessToCourtLocation(locCode, payload.getOwner(), courtLocationRepository);

        log.debug("User %s, Retrieving active Pool Requests at Court location %s", userLogin, locCode);
        List<PoolsAtCourtLocationListDto.PoolsAtCourtLocationDataDto> data = new ArrayList<>();
        try {
            List<String> poolsListing = poolRequestRepository.findPoolsByCourtLocation(locCode);
            log.debug("Found {} active pool records for current user, {}", poolsListing.size(), userLogin);

            poolsListing.stream().forEach(pool -> {
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
        } catch (Exception e) {
            log.error("Error retrieving active pools at court location {}", locCode);
            throw new MojException.InternalServerError("Error retrieving pools at court location " + locCode, e);
        }
        return new PoolsAtCourtLocationListDto(data);
    }

    private long populateAtCourtDataList(Page<ActivePoolsCourt> activePoolsCourtPage,
                                         List<PoolRequestActiveListDto.PoolRequestActiveDataDto> data) {
        long totalSize;
        activePoolsCourtPage.forEach(p -> {
            PoolRequestActiveListDto.PoolRequestActiveDataDto poolRequestActiveDataDto =
                PoolRequestActiveListDto.PoolRequestActiveDataDto.builder()
                    .poolNumber(p.getPoolNumber())
                    .poolCapacity(p.getPoolCapacity())
                    .jurorsInPool(p.getJurorsInPool())
                    .courtName(p.getCourtName())
                    .poolType(p.getPoolType())
                    .attendanceDate(p.getServiceStartDate())
                    .build();
            data.add(poolRequestActiveDataDto);
        });

        totalSize = activePoolsCourtPage.getTotalElements();
        return totalSize;
    }

    private long populateAtBureauDataList(Page<ActivePoolsBureau> activePoolsBureauList,
                                          List<PoolRequestActiveListDto.PoolRequestActiveDataDto> data) {
        long totalSize;
        activePoolsBureauList.forEach(p -> {
            PoolRequestActiveListDto.PoolRequestActiveDataDto poolRequestActiveDataDto =
                PoolRequestActiveListDto.PoolRequestActiveDataDto.builder()
                    .poolNumber(p.getPoolNumber())
                    .requestedFromBureau(p.getJurorsRequested())
                    .confirmedFromBureau(p.getConfirmedJurors())
                    .courtName(p.getCourtName())
                    .poolType(p.getPoolType())
                    .attendanceDate(p.getServiceStartDate())
                    .build();
            data.add(poolRequestActiveDataDto);
        });

        totalSize = activePoolsBureauList.getTotalElements();
        return totalSize;
    }

    @Override
    public String getPoolAttendanceTime(String poolId) {
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

        return attendanceTime.toString();
    }

    public static <T> Page<T> convertListToPage(List<T> objectList, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), objectList.size());
        List<T> subList = start >= end
            ?
            new ArrayList<>()
            :
                objectList.subList(start, end);
        return new PageImpl<>(subList, pageable, objectList.size());
    }
}
