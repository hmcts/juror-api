package uk.gov.hmcts.juror.api.bureau.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.bureau.controller.request.AutoAssignRequest;
import uk.gov.hmcts.juror.api.bureau.controller.request.JurorResponseSearchRequest;
import uk.gov.hmcts.juror.api.bureau.controller.request.ReassignResponsesDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.AutoAssignResponse;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseOverviewDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseSummaryWrapper;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauYourWorkCounts;
import uk.gov.hmcts.juror.api.bureau.controller.response.CourtResponseSummaryWrapper;
import uk.gov.hmcts.juror.api.bureau.controller.response.CourtYourWorkCounts;
import uk.gov.hmcts.juror.api.bureau.controller.response.JurorResponseSearchResults;
import uk.gov.hmcts.juror.api.bureau.exception.AutoAssignException;
import uk.gov.hmcts.juror.api.bureau.exception.ReassignException;
import uk.gov.hmcts.juror.api.bureau.service.AutoAssignmentService;
import uk.gov.hmcts.juror.api.bureau.service.BureauService;
import uk.gov.hmcts.juror.api.bureau.service.JurorResponseSearchService;
import uk.gov.hmcts.juror.api.bureau.service.UserService;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.response.poolmanagement.AvailablePoolsInCourtLocationDto;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

/**
 * API endpoints controller for response-related operations.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/bureau/responses", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Bureau Responses API", description = "Bureau operations relating to juror responses")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class BureauResponsesController {

    private final BureauService bureauService;
    private final JurorResponseSearchService searchService;
    private final AutoAssignmentService autoAssignmentService;
    private final UserService userService;


    @GetMapping
    @Operation(summary = "Retrieve all juror details filtered by status category(todo,pending,"
        + "completed)",
        description = "Retrieve all juror details filtered by status category(todo,pending,completed)")
    public ResponseEntity<BureauResponseSummaryWrapper> filterBureauDetailsByStatus(@Parameter(description =
        "Response category filter") @RequestParam("filterBy") String filterBy) {
        BureauResponseSummaryWrapper wrapper = bureauService.getDetailsByProcessingStatus(filterBy);
        return ResponseEntity.ok().body(wrapper);
    }

    @GetMapping(path = "/counts")
    @Operation(summary = "Retrieve counts of responses assigned to the current user")
    public ResponseEntity<BureauYourWorkCounts> getCurrentUserTodo() {
        return ResponseEntity.ok().body(bureauService.getCounts(SecurityUtil.getActiveLogin()));
    }

    @GetMapping(path = "/courtCounts")
    @Operation(summary = "Retrieve counts of responses assigned to the current user")
    public ResponseEntity<CourtYourWorkCounts> getCourtCountsTodo() {
        return ResponseEntity.ok().body(bureauService.getCountsForCourt(SecurityUtil.getActiveOwner()));
    }





    @GetMapping(path = "/todo")
    @Operation(summary = "Retrieve all todo responses assigned to the current user")
    public ResponseEntity<BureauResponseSummaryWrapper> getCurrentUserTodo(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload) {
        BureauResponseSummaryWrapper wrapper = bureauService.getTodo(payload.getLogin());
        return ResponseEntity.ok().body(wrapper);
    }

    @GetMapping("/courtTodo/{locCode}")
    @Operation(summary = "Retrieve all todo responses transferred to a given court location")
    public ResponseEntity<CourtResponseSummaryWrapper> getTodoInCourtLocation(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @Parameter(description = "3-digit numeric string to identify the court") @PathVariable(name = "locCode")
        @Size(min = 3, max = 3) @Valid String locCode){
        CourtResponseSummaryWrapper wrapper = bureauService.getTodoCourt(SecurityUtil.getActiveOwner());
        return ResponseEntity.ok().body(wrapper);
    }

    @GetMapping(path = "/pending")
    @Operation(summary = "Retrieve all pending responses assigned to the current user")
    public ResponseEntity<BureauResponseSummaryWrapper> getCurrentUserPending(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload) {
        BureauResponseSummaryWrapper wrapper = bureauService.getPending(payload.getLogin());
        return ResponseEntity.ok().body(wrapper);
    }

    @GetMapping(path = "/courtPending")
    @Operation(summary = "Retrieve all pending responses assigned to the current court")
    public ResponseEntity<CourtResponseSummaryWrapper> getCurrentCourtPending(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload) {
        CourtResponseSummaryWrapper wrapper = bureauService.getCourtPending(SecurityUtil.getActiveOwner());
        return ResponseEntity.ok().body(wrapper);
    }

    @GetMapping(path = "/completedToday")
    @Operation(summary = "Retrieve all responses assigned to the current user "
        + "which were completed today")
    public ResponseEntity<BureauResponseSummaryWrapper> getCurrentUserCompletedToday(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload) {
        BureauResponseSummaryWrapper wrapper = bureauService.getCompletedToday(payload.getLogin());
        return ResponseEntity.ok().body(wrapper);
    }

    @GetMapping(path = "/courtCompletedToday")
    @Operation(summary = "Retrieve all responses assigned to the current user "
        + "which were completed today")
    public ResponseEntity<CourtResponseSummaryWrapper> getCurrentCourtCompletedToday(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload) {
        CourtResponseSummaryWrapper wrapper = bureauService.getCourtCompletedToday(SecurityUtil.getActiveOwner());
        return ResponseEntity.ok().body(wrapper);
    }

    @GetMapping(path = "/overview/{login}")
    @Operation(summary = "Retrieve overview of responses assigned to a specific "
        + "user")
    public ResponseEntity<BureauResponseOverviewDto> getUserResponseOverview(
        @PathVariable String login,
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload) {
        BureauResponseOverviewDto overviewDto = bureauService.getOverview(login);
        return ResponseEntity.ok().body(overviewDto);
    }

    @PostMapping(path = "/search")
    @Operation(summary = "Retrieve responses based on search criteria")
    public ResponseEntity<JurorResponseSearchResults> searchForJurorResponses(
        @Parameter(hidden = true) BureauJwtAuthentication auth,
        @Parameter(description = "Search "
            + "criteria") @RequestBody @Validated JurorResponseSearchRequest searchRequestDto) {
        JurorResponseSearchResults resultsDto = searchService.searchForResponses(
            searchRequestDto,
            SecurityUtil.isBureauManager()
        );
        return ResponseEntity.ok().body(resultsDto);
    }

    @GetMapping(path = "/autoassign")
    @Operation(summary = "Get auto-assignment capacity data")
    public ResponseEntity<AutoAssignResponse> getAutoAssignmentData(
        @Parameter(hidden = true) BureauJwtAuthentication auth) {
        if (!SecurityUtil.isBureauManager()) {
            log.error("Auto-assign endpoint called by non-team leader");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(autoAssignmentService.getAutoAssignmentData());
    }

    @PostMapping(path = "/autoassign")
    @Operation(summary = "Auto-assign response backlog")
    public ResponseEntity<Void> autoAssign(@Parameter(hidden = true) BureauJwtAuthentication auth,
                                           @RequestBody @Validated AutoAssignRequest autoAssignRequest)
        throws AutoAssignException {

        if (!SecurityUtil.isBureauManager()) {
            log.error("Auto-assign endpoint called by non-team leader");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            autoAssignmentService.autoAssign(autoAssignRequest, SecurityUtil.getUsername());
            return ResponseEntity.ok().build();
        } catch (AutoAssignException e) {
            log.error("Failed to auto-assign:", e);
            throw e;
        }
    }

    @PostMapping(path = "/reassign")
    @Operation(summary = "Deactivate officer and reassign any assigned responses")
    public ResponseEntity<Void> reassign(@Parameter(hidden = true) BureauJwtAuthentication auth,
                                         @RequestBody @Validated ReassignResponsesDto reassignResponsesDto) {
        if (!SecurityUtil.isBureauManager()) {
            log.error("Reassign endpoint called by non-team leader");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            userService.reassignResponses(SecurityUtil.getUsername(), reassignResponsesDto);
            return ResponseEntity.ok().build();
        } catch (ReassignException e) {
            log.error("Failed to re-assign:", e);
            throw e;
        }
    }
}
