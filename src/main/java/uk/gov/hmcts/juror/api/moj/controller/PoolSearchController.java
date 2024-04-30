package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.CoronerPoolFilterRequestQuery;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolSearchRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestSearchListDto;
import uk.gov.hmcts.juror.api.moj.domain.FilterCoronerPool;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.exception.UserPermissionsException;
import uk.gov.hmcts.juror.api.moj.service.PoolRequestSearchService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Validated
@RequestMapping(value = "/api/v1/moj/pool-search", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Search")
public class PoolSearchController {

    @NonNull
    private final PoolRequestSearchService poolRequestSearchService;


    @PostMapping
    @Operation(summary = "GET With Body", description = "Retrieve a list of all pools, filtered by the provided "
        + "search criteria")
    public ResponseEntity<PoolRequestSearchListDto> searchPoolRequests(
        @Parameter(hidden = true) BureauJwtAuthentication auth,
        @RequestBody @Valid PoolSearchRequestDto poolSearchRequestDto) {

        BureauJwtPayload payload = (BureauJwtPayload) auth.getPrincipal();
        List<String> courts = JurorDigitalApplication.JUROR_OWNER.equalsIgnoreCase(payload.getOwner())
            ? new ArrayList<>() : payload.getStaff().getCourts();

        if (validateCourtLocation(payload, poolSearchRequestDto.getLocCode(), courts)) {
            PoolRequestSearchListDto poolRequests =
                poolRequestSearchService.searchForPoolRequest(poolSearchRequestDto, courts);
            return ResponseEntity.ok().body(poolRequests);
        }

        throw new UserPermissionsException.CourtUnavailable();
    }

    @PostMapping("/coroner-pools")
    @Operation(summary = "GET With Body", description = "Retrieve a list of all coroner pools, filtered by the "
        + "provided search criteria (Bureau users only)")
    @PreAuthorize(SecurityUtil.IS_BUREAU)
    public ResponseEntity<PaginatedList<FilterCoronerPool>> searchCoronerPoolRequests(
        @RequestBody @Valid CoronerPoolFilterRequestQuery query) {

        PaginatedList<FilterCoronerPool> poolRequests =
            poolRequestSearchService.searchForCoronerPools(query);

        if (poolRequests == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().body(poolRequests);

    }

    private boolean validateCourtLocation(BureauJwtPayload payload, String locCode, List<String> courts) {
        log.trace(String.format("User %s is searching for pools in court location: %s", payload.getLogin(), locCode));
        return locCode == null || locCode.isEmpty()
            || payload.getOwner().equalsIgnoreCase(JurorDigitalApplication.JUROR_OWNER)
            || courts.contains(locCode);
    }
}