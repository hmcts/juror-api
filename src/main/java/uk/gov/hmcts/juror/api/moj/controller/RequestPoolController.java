package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.config.security.IsCourtUser;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolRequestedFilterQuery;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtLocationListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolNumbersListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestActiveListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolsAtCourtLocationListDto;
import uk.gov.hmcts.juror.api.moj.domain.DayType;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;
import uk.gov.hmcts.juror.api.moj.service.GeneratePoolNumberService;
import uk.gov.hmcts.juror.api.moj.service.PoolRequestService;

import java.time.LocalDate;

@Slf4j
@RestController
@Validated
@RequestMapping(value = "/api/v1/moj/pool-request", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Pool Management")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class RequestPoolController {

    @NonNull
    private final CourtLocationService courtLocationService;
    @NonNull
    private final PoolRequestService poolRequestService;
    @NonNull
    private final GeneratePoolNumberService generatePoolNumberService;

    @GetMapping("/court-locations")
    @Operation(summary = "Retrieve a list of all court locations")
    public ResponseEntity<CourtLocationListDto> getCourtLocations(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload) {
        CourtLocationListDto courtLocations =
            courtLocationService.buildCourtLocationDataResponse(payload);
        return ResponseEntity.ok().body(courtLocations);
    }

    /**
     * Retrieve a list of all pools filtered by status, pool type and court location.
     *
     * @param payload   Decoded JWT principal data from the user
     * @param locCode   Single location code to filter pools by
     * @param offset    The page number for result table
     * @param sortBy    The sort by criteria, this can be poolNumber, courtName, poolType, serviceStartDate and for
     *                  bureau tab only,
     *                  jurorsRequested and confirmedJurors, and for court tab only, totalNumber and jurorsInPool
     * @param sortOrder Sort order can be either "asc" or "desc"
     *
     * @return The list of requested pools including CIV and CRO and also the total of results for the query
     */
    @GetMapping("/pools-requested")
    @Operation(summary = "Retrieve a list of all pools filtered by status, pool type and court location")
    public ResponseEntity<PaginatedList<PoolRequestListDto>> getPoolRequests(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @RequestParam(required = false) @Size(min = 3, max = 3) @Valid String locCode,
        @RequestParam @Valid int pageNumber,
        @RequestParam @Valid String sortBy) {

        PoolRequestedFilterQuery filterQuery = PoolRequestedFilterQuery.builder()
            .sortField(PoolRequestedFilterQuery.SortField.RETURN_DATE)
            .sortMethod(SortMethod.ASC)
            .pageLimit(25)
            .pageNumber(pageNumber)
            .build();

        PaginatedList<PoolRequestListDto> poolRequests = poolRequestService.getFilteredPoolRequests(payload, locCode,
            filterQuery);

        return ResponseEntity.ok().body(poolRequests);
    }

    /**
     * Post request operation for creating a new Pool Request record.
     * On successful creation of a Pool Request this will return an empty payload with a Http Status of 201 (Created)
     *
     * @param poolRequestDto JSON payload of PoolRequest data using validation constraints to ensure data integrity
     */
    @PostMapping("/new-pool")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create and persist a new pool request")
    public void createPoolRequest(@Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
                                  @RequestBody @Valid PoolRequestDto poolRequestDto) {
        poolRequestService.savePoolRequest(poolRequestDto, payload);
    }

    /**
     * Get request operation to execute a query and return the number of available court deferrals for a given
     * Court Location and a given Attendance Date.
     *
     * @param locationCode 3 digit numeric String to uniquely identify a court location
     * @param deferredTo   The date this pool is first due to attend court
     */
    @GetMapping("/deferrals")
    @Operation(summary = "Retrieve the number of deferred jurors for a given court location and a given date")
    public ResponseEntity<Long> getCourtDeferrals(@RequestParam @Size(min = 3, max = 3) @Valid String locationCode,
                                                  @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") @Valid
                                                  LocalDate deferredTo) {
        long courtDeferrals = poolRequestService.getCourtDeferrals(locationCode, deferredTo);
        return ResponseEntity.ok().body(courtDeferrals);
    }

    /**
     * Get request operation to determine if a given date is a valid business day
     * weekends or dates matching records in the HOLIDAYS table are not valid business days.
     *
     * @param locationCode   3 digit numeric String to uniquely identify a court location
     * @param attendanceDate The date this pool is first due to attend court
     */
    @GetMapping("/day-type")
    @Operation(summary = "Check a whether a given date is a valid day when the courts will be sitting")
    public ResponseEntity<DayType> getDayType(@RequestParam @Size(min = 3, max = 3) @Valid String locationCode,
                                              @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") @Valid
                                              LocalDate attendanceDate) {
        DayType dayType = poolRequestService.checkAttendanceDate(attendanceDate, locationCode);
        log.debug(String.format("Proposed attendance date %s is a %s", attendanceDate, dayType));
        return ResponseEntity.ok().body(dayType);
    }

    /**
     * Get request operation to automatically generate a pool number for a new pool request.
     *
     * @param locationCode   3 digit numeric String to uniquely identify a court location
     * @param attendanceDate The date this pool is first due to attend court
     */
    @GetMapping("/generate-pool-number")
    @Operation(summary = "Generate a pool number for a new pool request")
    public ResponseEntity<String> generatePoolNumber(@RequestParam @Size(min = 3, max = 3) @Valid String locationCode,
                                                     @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") @Valid
                                                     LocalDate attendanceDate) {
        String poolNumber = generatePoolNumberService.generatePoolNumber(locationCode, attendanceDate);
        return ResponseEntity.ok().body(poolNumber);
    }

    /**
     * Get request operation to retrieve pool numbers for a given poolNumberPrefix.
     *
     * @param poolNumberPrefix The first 7 characters of a Pool Number containing the Court Location Code,
     *                         Attendance Date Year (YY) and Attendance Date Month (MM)
     */
    @GetMapping("/pool-numbers")
    @Operation(summary = "Retrieve the pool numbers list for a given pool number prefix")
    public ResponseEntity<PoolNumbersListDto> getPoolNumbers(
        @RequestParam @Size(min = 7, max = 7) @Valid String poolNumberPrefix) {
        PoolNumbersListDto poolNumbers = poolRequestService.getPoolNumbers(poolNumberPrefix);
        return ResponseEntity.ok().body(poolNumbers);
    }


    /**
     * Get a list of Active pools as a Bureau or Court user for At Bureau or At Court.
     * The Pool Types currently returned are Civil and Crown Court only.
     *
     * @param payload   The JWT principal from front end
     * @param locCode   Single location code of court to filter by, e.g. 415
     * @param tab       Which tab the user is on, i.e, "court" or "bureau"
     * @param offset    The page number for result table
     * @param sortBy    The sort by criteria, this can be poolNumber, courtName, poolType, serviceStartDate and for
     *                  bureau tab only,
     *                  jurorsRequested and confirmedJurors, and for court tab only, totalNumber and jurorsInPool
     * @param sortOrder Sort order can be either "asc" or "desc"
     *
     * @return A list of active pools fitting the search criteria
     */
    @GetMapping("/pools-active")
    @Operation(summary = "Retrieve a list of all active pools for Court or Bureau users")
    public ResponseEntity<PoolRequestActiveListDto> getActivePoolRequests(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @RequestParam(required = false) @Size(min = 3, max = 3) @Valid String locCode,
        @RequestParam @Valid String tab,
        @RequestParam @Valid int offset,
        @RequestParam @Valid String sortBy,
        @RequestParam @Valid String sortOrder) {

        PoolRequestActiveListDto poolRequests = poolRequestService.getActivePoolRequests(payload, locCode, tab,
            offset, sortBy, sortOrder);

        return ResponseEntity.ok().body(poolRequests);
    }

    /**
     * Get a list of Active pools at a specified Court location for a court user only.
     *
     * @return A list of active pools at a Court location
     */
    @GetMapping("/pools-at-court")
    @IsCourtUser
    @Operation(summary = "Retrieve a list of all active pools at a location for Court users")
    public ResponseEntity<PoolsAtCourtLocationListDto> getActivePoolsAtCourt(
        @RequestParam(required = false) @Size(min = 3, max = 3) @Valid String locCode) {

        PoolsAtCourtLocationListDto pools = poolRequestService.getActivePoolsAtCourtLocation(locCode);

        return ResponseEntity.ok().body(pools);
    }

}
