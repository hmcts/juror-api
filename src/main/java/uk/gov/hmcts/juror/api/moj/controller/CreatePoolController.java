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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.CoronerPoolAddCitizenRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.CoronerPoolRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.NilPoolRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolAdditionalSummonsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolCreateRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolMemberFilterRequestQuery;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.SummonsFormRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CoronerPoolItemDto;
import uk.gov.hmcts.juror.api.moj.controller.response.NilPoolResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestItemDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PostcodesListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.SummonsFormResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.FilterPoolMember;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.VotersLocPostcodeTotals;
import uk.gov.hmcts.juror.api.moj.service.PoolCreateService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping(value = "/api/v1/moj/pool-create", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@Tag(name = "Pool Management")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@PreAuthorize("isAuthenticated()")
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.TooManyMethods"})
public class CreatePoolController {

    @NonNull
    private final PoolCreateService poolCreateService;

    @GetMapping("/pool")
    @Operation(summary = "Retrieve details of an existing pool request")
    public ResponseEntity<PoolRequestItemDto> getPoolRequest(@RequestParam() String poolNumber,
                                                             @RequestParam() String owner) {
        PoolRequestItemDto poolRequest = poolCreateService.getPoolRequest(poolNumber, owner);
        if (poolRequest == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(poolRequest);
    }

    /**
     * Post request operation for returning values required in the summons form.
     * Including the Bureau deferrals available, total citizens in each postcode area etc.
     *
     * @param summonsFormRequestDto JSON payload of Summons form data
     */
    @PostMapping("/summons-form")
    @Operation(summary = "Retrieve details for Summons form for a particular Pool")
    public ResponseEntity<SummonsFormResponseDto> getPoolRequest(
        @Validated @RequestBody SummonsFormRequestDto summonsFormRequestDto) {
        SummonsFormResponseDto summonsForms = poolCreateService.summonsForm(summonsFormRequestDto);
        return ResponseEntity.ok().body(summonsForms);
    }

    @PostMapping("/members")
    @Operation(summary = "Retrieve a list of all pool members by pool number. Post to effect a GET with body")
    public ResponseEntity<PaginatedList<FilterPoolMember>> getPoolMembers(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @Validated @RequestBody PoolMemberFilterRequestQuery query) {
        PaginatedList<FilterPoolMember> poolMembers = poolCreateService.getJurorPoolsList(payload, query);
        if (poolMembers == null) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok().body(poolMembers);
    }

    @GetMapping("/postcodes")
    @Operation(summary = "Retrieve a list of all postcodes and number of citizens available for a given area code")
    public ResponseEntity<PostcodesListDto> getCourtCatchmentItems(@RequestParam(name = "areaCode") String areaCode,
                                                                   @RequestParam(name = "isCoronersPool",
                                                                       required = false, defaultValue = "false")
                                                                   boolean isCoronersPool) {

        List<VotersLocPostcodeTotals.CourtCatchmentSummaryItem> catchmentItems =
            poolCreateService.getAvailableVotersByLocation(areaCode, isCoronersPool);
        if (catchmentItems.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        PostcodesListDto postcodesList = new PostcodesListDto(catchmentItems);
        return ResponseEntity.ok().body(postcodesList);
    }

    /**
     * Get request operation to execute a query and return the number of available Bureau deferrals for a given
     * Court Location and a given Attendance Date.
     *
     * @param locationCode 3 digit numeric String to uniquely identify a court location
     * @param deferredTo   The date this pool is first due to attend court
     */
    @GetMapping("/bureau-deferrals")
    @Operation(summary = "Retrieve the number of Bureau deferred jurors for a given court location and a given date")
    public ResponseEntity<Long> getBureauDeferrals(@RequestParam @Size(min = 3, max = 3) @Valid String locationCode,
                                                   @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") @Valid
                                                   LocalDate deferredTo) {
        long bureauDeferrals = poolCreateService.getBureauDeferrals(locationCode, deferredTo);
        return ResponseEntity.ok().body(bureauDeferrals);
    }

    /**
     * Post request operation for creating a new Pool.
     * On successful creation of a Pool this will return an empty payload with a Http Status of 201 (Created).
     *
     * @param poolCreateRequestDto JSON payload of Pool data
     */
    @PostMapping("/create-pool")
    @Operation(summary = "Create a Pool and summon citizens")
    //Only a Bureau user is allowed to create a pool
    @PreAuthorize(SecurityUtil.BUREAU_AUTH)
    public ResponseEntity<String> createPoolRequest(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @Validated @RequestBody PoolCreateRequestDto poolCreateRequestDto) {
        poolCreateService.lockVotersAndCreatePool(payload, poolCreateRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * Post request operation for additional summons for a pool.
     * On successful addition of citizens to the Pool this will return an empty payload with a
     * Http Status of 201 (Created).
     *
     * @param poolAdditionalSummonsDto JSON payload of Pool data
     */
    @PostMapping("/additional-summons")
    @Operation(summary = "Summon additional citizens to a Pool")
    @PreAuthorize(SecurityUtil.BUREAU_AUTH)
    public ResponseEntity<String> additionalSummonsForPool(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @Validated @RequestBody PoolAdditionalSummonsDto poolAdditionalSummonsDto) {
        poolCreateService.lockVotersAndSummonAdditionalCitizens(payload, poolAdditionalSummonsDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * Post request operation for checking if ok to create a Nil pool.
     * Total number of deferred jurors for the given date will be returned.
     *
     * @param nilPoolRequestDto JSON payload of Pool data to check
     */
    @PostMapping("/nil-pool-check")
    @Operation(summary = "GET With Body", description = "Check number of deferred jurors for this court/date")
    @PreAuthorize(SecurityUtil.COURT_AUTH)
    public ResponseEntity<NilPoolResponseDto> checkNilPoolRequest(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @Validated @RequestBody NilPoolRequestDto nilPoolRequestDto) {
        NilPoolResponseDto nilPoolResponseDto = poolCreateService.checkForDeferrals(payload.getOwner(),
            nilPoolRequestDto);

        if (nilPoolResponseDto == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().body(nilPoolResponseDto);
    }


    /**
     * Post request operation for creating a Nil pool.
     *
     * @param nilPoolRequestDto JSON payload of Pool data to check
     */
    @PostMapping("/nil-pool-create")
    @Operation(summary = "create a Nil Pool for this court/date")
    @PreAuthorize(SecurityUtil.COURT_AUTH)
    public ResponseEntity<String> createNilPool(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @Validated @RequestBody NilPoolRequestDto nilPoolRequestDto) {
        poolCreateService.createNilPool(payload.getOwner(), nilPoolRequestDto);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    /**
     * Put request operation for converting an existing Nil Pool record to an active pool.
     * On successful conversion of a Nil Pool, this will return an empty payload with a Http Status of 200 (OK).
     *
     * @param poolRequestDto JSON payload of PoolRequest data using validation constraints to ensure data integrity
     */
    @PutMapping("/nil-pool-convert")
    @Operation(summary = "Convert an existing Nil pool")
    @PreAuthorize(SecurityUtil.BUREAU_AUTH)
    public ResponseEntity<String> convertNilPool(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @RequestBody @Valid PoolRequestDto poolRequestDto) {
        poolCreateService.convertNilPool(poolRequestDto, payload);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * Post request operation for creating a Coroner pool.
     *
     * @param coronerPoolRequestDto JSON payload of Coroner Pool data to check
     * @return created response with a part of the URL for the newly created resource
     */
    @PostMapping("/create-coroner-pool")
    @Operation(summary = "create a Coroner Pool for this court/date")
    @PreAuthorize(SecurityUtil.BUREAU_AUTH)
    public ResponseEntity<?> createCoronerPool(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @Validated @RequestBody CoronerPoolRequestDto coronerPoolRequestDto) {

        String poolNumber = poolCreateService.createCoronerPool(payload.getOwner(), coronerPoolRequestDto);

        return ResponseEntity.created(URI.create("?poolNumber=" + poolNumber)).build();
    }

    @GetMapping("/coroner-pool")
    @Operation(summary = "Retrieve details of an existing coroner pool")
    @PreAuthorize(SecurityUtil.BUREAU_AUTH
    )
    public ResponseEntity<CoronerPoolItemDto> getCoronerPoolRequest(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @RequestParam() String poolNumber) {
        CoronerPoolItemDto coronerPool = poolCreateService.getCoronerPool(poolNumber);
        if (coronerPool == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(coronerPool);
    }

    /**
     * Post request operation for adding citizens to a coroner pool.
     *
     * @param coronerPoolAddCitizenRequestDto JSON payload of Postcodes and numbers to add to pool
     */
    @PostMapping("/add-citizens")
    @Operation(summary = "Add citizens to coroner pool")
    @PreAuthorize(SecurityUtil.BUREAU_AUTH)
    public ResponseEntity<String> addCitizensToCoronerPool(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @Validated @RequestBody CoronerPoolAddCitizenRequestDto coronerPoolAddCitizenRequestDto) {
        poolCreateService.addCitizensToCoronerPool(payload.getOwner(), coronerPoolAddCitizenRequestDto);

        return new ResponseEntity<>(HttpStatus.CREATED);
    }

}
