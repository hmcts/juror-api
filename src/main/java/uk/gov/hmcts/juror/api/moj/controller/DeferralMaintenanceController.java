package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralAllocateRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralDatesRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralReasonRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DeferralListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DeferralOptionsDto;
import uk.gov.hmcts.juror.api.moj.service.deferralmaintenance.ManageDeferralsService;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/moj/deferral-maintenance", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Deferral Maintenance")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Validated
public class DeferralMaintenanceController {

    @NonNull
    private final ManageDeferralsService manageDeferralsService;

    @PostMapping("/juror/defer/{jurorNumber}")
    @Operation(summary = "/juror/defer/{jurorNumber} - POST deferral for a specific juror",
        description = "Mark a single juror response with a deferral decision")
    public ResponseEntity<Void> processJurorDeferral(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @Parameter(description = "9-digit numeric string to identify the"
            + " juror") @PathVariable(name = "jurorNumber")
        @JurorNumber @Valid String jurorNumber,
        @RequestBody @Valid DeferralReasonRequestDto deferralReasonDto) {
        manageDeferralsService.processJurorDeferral(payload, jurorNumber, deferralReasonDto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/available-pools/{jurorNumber}")
    @Operation(description = "GET With Body",
        summary = "Retrieve active pools, including utilisation stats, for the given deferral dates")
    public ResponseEntity<DeferralOptionsDto> getDeferralOptionsForDates(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @Parameter(description = "9-digit numeric string to identify the juror") @PathVariable(name = "jurorNumber")
        @JurorNumber @Valid String jurorNumber, @RequestBody @Valid DeferralDatesRequestDto deferralDatesRequestDto) {
        DeferralOptionsDto responseBody =
            manageDeferralsService.findActivePoolsForDates(deferralDatesRequestDto, jurorNumber, payload);
        return ResponseEntity.ok().body(responseBody);
    }

    @GetMapping(value = "/available-pools/{courtLocationCode}")
    @Operation(summary = "Retrieve active pools, including utilisation stats, for the given court location")
    public ResponseEntity<DeferralOptionsDto> getDeferralOptionsForCourtLocation(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @PathVariable(name = "courtLocationCode") String courtLocationCode) {
        DeferralOptionsDto responseBody = manageDeferralsService
            .findActivePoolsForCourtLocation(payload, courtLocationCode);
        return ResponseEntity.ok().body(responseBody);
    }

    @PostMapping("/available-pools/{locationCode}/{jurorNumber}/deferral_dates")
    @Operation(description = "GET With Body",
        summary = "Retrieve active pools, including utilisation stats, for the given deferral dates")
    public ResponseEntity<DeferralOptionsDto> getDeferralOptionsForDatesAndCourtLocation(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @Parameter(description = "9-digit numeric string to identify the juror") @PathVariable(name = "jurorNumber")
        @JurorNumber @Valid String jurorNumber, @RequestBody @Valid DeferralDatesRequestDto deferralDatesRequestDto,
        @PathVariable(name = "locationCode") @Size(min = 3, max = 3) String locationCode) {
        DeferralOptionsDto responseBody =
            manageDeferralsService.findActivePoolsForDatesAndLocCode(deferralDatesRequestDto, jurorNumber,
                locationCode, payload);
        return ResponseEntity.ok().body(responseBody);
    }

    @GetMapping("/deferral-dates/{jurorNumber}")
    @Operation(summary = "Retrieve a juror's preferred deferral dates which they provided on their digital summons "
        + "reply",
        description = "Deferral dates are stored in localised format (d/M/yyyy) with a mixture of zero-padded and "
            + "non-zero padded days/months (where applicable). This will return a list of dates converted into "
            + "ISO format (yyyy-MM-dd) with months and days always zero-padded where applicable")
    public ResponseEntity<List<String>> getPreferredDates(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @Parameter(description = "9-digit numeric string to "
            + "identify the juror")
        @PathVariable(name = "jurorNumber") @JurorNumber @Valid
        String jurorNumber) {
        List<String> responseBody = manageDeferralsService.getPreferredDeferralDates(jurorNumber, payload);
        return ResponseEntity.ok().body(responseBody);
    }

    @GetMapping("/deferrals/{courtLocationCode}")
    @Operation(summary = "Retrieve a list of all deferred jurors for a court location")
    public ResponseEntity<DeferralListDto> getDeferralsByCourtLocationCode(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @Parameter(description = "3 Digit numeric string to identify court location")
        @PathVariable("courtLocationCode") @Size(min = 3, max = 3) String courtLocationCode) {
        DeferralListDto dto = manageDeferralsService.getDeferralsByCourtLocationCode(payload, courtLocationCode);
        return ResponseEntity.ok().body(dto);
    }

    @GetMapping("/available-pools/{courtLocationCode}/{jurorNumber}")
    @Operation(summary = "Retrieve available pools, including utilisation stats, for the given court location code and "
        + "juror")
    public ResponseEntity<DeferralOptionsDto> getDeferralPoolsByJurorNumberAndCourtLocationCode(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @Parameter(description = "3-digit numeric string to identify the court")
        @PathVariable("courtLocationCode") @Size(min = 3, max = 3) String courtLocationCode,
        @Parameter(description = "9-digit numeric string to identify the juror")
        @PathVariable("jurorNumber") @JurorNumber @Valid String jurorNumber) {
        DeferralOptionsDto dto = manageDeferralsService.getAvailablePoolsByCourtLocationCodeAndJurorNumber(
            payload,
            courtLocationCode,
            jurorNumber);
        return ResponseEntity.ok().body(dto);
    }

    @PostMapping("/deferrals/allocate-jurors-to-pool")
    @Operation(summary = "Move juror(s) to the selected active pool")
    public ResponseEntity<Void> moveJurorsToActivePool(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @RequestBody @Valid DeferralAllocateRequestDto deferralAllocateRequestDto) {
        manageDeferralsService.allocateJurorsToActivePool(payload, deferralAllocateRequestDto);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/deferrals/change-deferral-date/{jurorNumber}")
    @Operation(summary = "Change the date of a deferred juror")
    public ResponseEntity<Void> changeJurorsDeferralDate(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @PathVariable(name = "jurorNumber") @JurorNumber @Valid String jurorNumber,
        @RequestBody @Valid DeferralReasonRequestDto deferralReasonRequestDto) {
        manageDeferralsService.changeJurorDeferralDate(payload, jurorNumber, deferralReasonRequestDto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete-deferral/{jurorNumber}")
    @Operation(summary = "Delete a deferral")
    public void deleteDeferral(@Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
                               @PathVariable(name = "jurorNumber") @JurorNumber @Valid
                               String jurorNumber) {
        manageDeferralsService.deleteDeferral(payload, jurorNumber);
    }

    // Postponement is a special case of deferral, this endpoint makes it explicit that it's a postponement but has
    // the same Dto
    @PostMapping("/juror/postpone/{jurorNumber}")
    @Operation(summary = "Postpone a specific juror",
        description = "Mark a single juror as postponed")
    public ResponseEntity<Void> processJurorPostponement(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @Parameter(description = "9-digit numeric string to identify"
            + " the juror") @PathVariable(name = "jurorNumber")
        @JurorNumber @Valid String jurorNumber,
        @RequestBody @Valid DeferralReasonRequestDto deferralReasonDto) {
        manageDeferralsService.processJurorPostponement(payload, jurorNumber, deferralReasonDto);
        return ResponseEntity.noContent().build();
    }

}