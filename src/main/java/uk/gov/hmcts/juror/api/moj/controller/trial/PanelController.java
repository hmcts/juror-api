package uk.gov.hmcts.juror.api.moj.controller.trial;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.config.security.IsCourtUser;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.CreatePanelDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.AvailableJurorsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.EmpanelListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.PanelListDto;
import uk.gov.hmcts.juror.api.moj.service.trial.PanelService;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/moj/trial/panel", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Panel Management")
@Validated
@IsCourtUser
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PanelController {

    @NonNull
    private PanelService panelService;

    @GetMapping("/available-jurors")
    @Operation(summary = "Retrieves a list of jurors for a court location")
    public ResponseEntity<List<AvailableJurorsDto>> getAvailableJurors(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @RequestParam("court_location_code") @PathVariable("locationCode") @Valid String locationCode) {
        List<AvailableJurorsDto> dto = panelService.getAvailableJurors(locationCode);
        return ResponseEntity.ok().body(dto);
    }

    @PostMapping("/create-panel")
    @Operation(summary = "Create a panel from a list of jurors from selected pools")
    public ResponseEntity<List<PanelListDto>> createPanel(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @RequestBody CreatePanelDto createPanelDto) {
        List<PanelListDto> dto = panelService.createPanel(createPanelDto.getNumberRequested(),
            createPanelDto.getTrialNumber(), createPanelDto.getPoolNumbers(),
            createPanelDto.getCourtLocationCode(), payload);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/add-panel-members")
    @Operation(summary = "Add panel members to a existing trial")
    public ResponseEntity<List<PanelListDto>> addPanelMembers(@RequestBody CreatePanelDto createPanelDto) {

        List<PanelListDto> dto = panelService.addPanelMembers(createPanelDto.getNumberRequested(),
            createPanelDto.getTrialNumber(), createPanelDto.getPoolNumbers(),
            createPanelDto.getCourtLocationCode());

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/request-empanel")
    @Operation(summary = "Retrieves the juror list to be empanelled")
    public ResponseEntity<EmpanelListDto> requestEmpanel(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @RequestParam("trial_number") @PathVariable("trialNumber") String trialNumber,
        @RequestParam("number_requested") @PathVariable("numberRequested") int numberRequested,
        @RequestParam("court_location_code") @PathVariable("courtLocationCode") String courtLocationCode) {
        EmpanelListDto dto = panelService.requestEmpanel(numberRequested, trialNumber, courtLocationCode);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/process-empanelled")
    @Operation(summary = "Processes the empanelled jurors")
    public ResponseEntity<List<PanelListDto>> processEmpanelled(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @RequestBody JurorListRequestDto jurorListRequestDto) {
        List<PanelListDto> dto = panelService.processEmpanelled(jurorListRequestDto, payload);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/list")
    @Operation(summary = "Gets the panel list for a given trial")
    public ResponseEntity<List<PanelListDto>> getPanel(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @RequestParam("trial_number") @PathVariable("trialNumber") String trialNumber,
        @RequestParam("court_location_code") @PathVariable("courtLocationCode") String courtLocationCode) {
        List<PanelListDto> dto = panelService.getPanelSummary(trialNumber, courtLocationCode);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/status")
    @Operation(summary = "Gets the panel creation status")
    public ResponseEntity<Boolean> getPanelCreationStatus(
        @RequestParam("trial_number") @PathVariable("trialNumber") String trialNumber,
        @RequestParam("court_location_code") @PathVariable("courtLocationCode") String courtLocationCode) {
        return ResponseEntity.ok(panelService.getPanelStatus(trialNumber, courtLocationCode));
    }
}


