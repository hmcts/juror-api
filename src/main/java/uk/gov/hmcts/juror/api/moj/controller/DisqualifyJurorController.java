package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.summonsmanagement.DisqualifyJurorDto;
import uk.gov.hmcts.juror.api.moj.controller.response.summonsmanagement.DisqualifyReasonsDto;
import uk.gov.hmcts.juror.api.moj.enumeration.DisqualifyCode;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.DisqualifyJurorService;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/moj/disqualify", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Validated
@Tag(name = "Summons Management")
public class DisqualifyJurorController {

    @NonNull
    private final DisqualifyJurorService disqualifyJurorService;

    @GetMapping("/reasons")
    @Operation(summary = "Disqualification reasons", description = "Returns a list of disqualification reasons")
    public ResponseEntity<DisqualifyReasonsDto> disqualifyReasons(
        @Parameter(hidden = true) BureauJwtAuthentication auth) {
        final BureauJwtPayload payload = (BureauJwtPayload) auth.getPrincipal();
        log.trace(
            "Api controller method disqualifyReasons() started by user {} to retrieve disqualification reasons",
            payload.getLogin()
        );

        DisqualifyReasonsDto disqualifyReasons = disqualifyJurorService.getDisqualifyReasons(payload);

        return ResponseEntity.status(HttpStatus.OK).body(disqualifyReasons);
    }

    @PatchMapping("/juror/{jurorNumber}")
    @Operation(summary = "Disqualify a juror", description = "Update the juror record with the disqualification code")
    public ResponseEntity<String> disqualifyJuror(
        @Parameter(hidden = true) BureauJwtAuthentication auth,
        @Parameter(description = "9-digit numeric string to identify the juror")
        @PathVariable(name = "jurorNumber") @JurorNumber @Valid String jurorNumber,
        @Valid @RequestBody DisqualifyJurorDto disqualifyJuror) {
        final BureauJwtPayload payload = (BureauJwtPayload) auth.getPrincipal();
        log.trace(
            "Juror Number {} - Api controller method disqualifyJuror() started by user {} to disqualify juror with "
                + "disqualification code {}",
            jurorNumber,
            payload.getLogin(),
            disqualifyJuror.getCode()
        );

        disqualifyJurorService.disqualifyJuror(jurorNumber, disqualifyJuror, payload);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(String.format("Juror %s disqualified with code %s", jurorNumber, disqualifyJuror.getCode()));
    }

    /**
     * Method to disqualify a juror due to age when date of birth is updated in juror details to a value outside
     * the acceptable range.
     */
    @PatchMapping("/juror/{jurorNumber}/age")
    @Operation(summary = "Disqualify juror due to age", description = "Update the juror record with the appropriate "
        + "disqualification code.")
    public ResponseEntity<String> disqualifyJurorDueToAge(
        @Parameter(hidden = true) BureauJwtAuthentication bureauJwtAuthentication,
        @Parameter(description = "9-digit numerical String which uniquely identifies the juror")
        @PathVariable(name = "jurorNumber") @JurorNumber @Valid String jurorNumber) {

        final BureauJwtPayload payload = (BureauJwtPayload) bureauJwtAuthentication.getPrincipal();
        log.trace(
            "Juror Number {} - Api controller method disqualifyJurorDueToAge() started by user {} to disqualify juror "
                + "with disqualification code A",
            jurorNumber,
            payload.getLogin()
        );

        disqualifyJurorService.disqualifyJurorDueToAgeOutOfRange(jurorNumber, payload);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(String.format("Juror %s disqualified with code %s", jurorNumber, DisqualifyCode.A));
    }

}
