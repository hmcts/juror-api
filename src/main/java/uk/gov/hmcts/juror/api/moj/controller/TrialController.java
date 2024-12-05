package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.EndTrialDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorDetailRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorPanelReassignRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.ReturnJuryDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.TrialDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.TrialSearch;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.TrialListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.TrialSummaryDto;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.trial.TrialService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.util.List;


@RestController
@RequestMapping(value = "/api/v1/moj/trial", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@Tag(name = "Trial Management")
public class TrialController {
    private final TrialService trialService;

    @Autowired
    public TrialController(TrialService trialService) {
        this.trialService = trialService;
    }

    /**
     * Enable the officer to create a trial.
     *
     * @param payload - login information
     * @throws MojException.BadRequest - thrown if there is a validation issue?
     */
    @PostMapping("/create")
    @Operation(summary = "Enable the officer to create a trial")
    @PreAuthorize(SecurityUtil.IS_COURT)
    public ResponseEntity<TrialSummaryDto> createTrial(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @RequestBody @Valid TrialDto trialDto) {
        TrialSummaryDto trialSummaryDto = trialService.createTrial(payload, trialDto);
        return ResponseEntity.ok().body(trialSummaryDto);
    }

    @PatchMapping("/edit")
    @Operation(summary = "Enable the officer to edit a trial")
    @PreAuthorize(SecurityUtil.IS_COURT)
    public ResponseEntity<TrialSummaryDto> editTrial(
        @RequestBody @Valid TrialDto trialDto) {
        TrialSummaryDto trialSummaryDto = trialService.editTrial(trialDto);
        return ResponseEntity.ok().body(trialSummaryDto);
    }

    @PostMapping("/list")
    @Operation(summary = "Get a list of all trials")
    @PreAuthorize(SecurityUtil.IS_COURT)
    public ResponseEntity<PaginatedList<TrialListDto>> getTrials(
        @Valid @RequestBody TrialSearch trialSearch) {
        return ResponseEntity.ok().body(trialService.getTrials(trialSearch));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get summary details for a trial")
    @PreAuthorize(SecurityUtil.IS_COURT)
    public ResponseEntity<TrialSummaryDto> getTrialSummary(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @RequestParam(name = "trial_number") @PathVariable(name = "trialNumber") String trialNumber,
        @RequestParam(name = "location_code") @PathVariable(name = "locationCode") String locationCode) {
        return ResponseEntity.ok().body(trialService.getTrialSummary(payload, trialNumber, locationCode));
    }

    @PostMapping("/return-panel")
    @Operation(summary = "Return panel members back to jurors in waiting")
    @PreAuthorize(SecurityUtil.IS_COURT)
    public ResponseEntity<Void> returnPanel(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @RequestParam(name = "trial_number") @PathVariable(name = "trialNumber") String trialNumber,
        @RequestParam(name = "location_code") @PathVariable(name = "locationCode") String locationCode,
        @RequestBody @Valid List<JurorDetailRequestDto> jurorDetailRequestDto
    ) {
        trialService.returnPanel(payload, trialNumber, locationCode, jurorDetailRequestDto);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/return-jury")
    @Operation(summary = "Return jury members back to jurors in waiting")
    @PreAuthorize(SecurityUtil.IS_COURT)
    public ResponseEntity<Void> returnJury(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @RequestParam(name = "trial_number") @PathVariable(name = "trialNumber") String trialNumber,
        @RequestParam(name = "location_code") @PathVariable(name = "locationCode") String locationCode,
        @RequestBody @Valid ReturnJuryDto returnJuryDto) {

        trialService.returnJury(payload, trialNumber, locationCode, returnJuryDto);
        return ResponseEntity.ok(null);
    }

    @PatchMapping("/end-trial")
    @Operation(summary = "End the trial")
    @PreAuthorize(SecurityUtil.IS_COURT)
    public ResponseEntity<Void> endTrial(@RequestBody EndTrialDto endTrialDto) {
        trialService.endTrial(endTrialDto);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/reassign-panel-members")
    @Operation(summary = "Reassign one or more panel members to another active trial")
    @PreAuthorize(SecurityUtil.IS_COURT)
    public ResponseEntity<Void> movePanelMembers(
        @RequestBody @Valid JurorPanelReassignRequestDto jurorPanelMoveRequest) {
        trialService.reassignPanelMembers(jurorPanelMoveRequest);
        return ResponseEntity.ok(null);
    }
}
