package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.moj.client.contracts.PncCheckServiceClient;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

@RestController
@Validated
@RequestMapping(value = "/api/v1/moj/pnc", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Pnc Management")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class PncController {

    @NonNull
    private final PncCheckServiceClient pncCheckServiceClient;

    @PatchMapping("/manual")
    @Operation(summary = "Manually run a police check ",
        description = "Updates the juror police national computer check status")
    public ResponseEntity<Void> manualPncCheck(
        @Valid @JurorNumber @Parameter(description = "Valid juror number", required = true)
        @RequestParam("juror_number") @PathVariable("jurorNumber") String jurorNumber) {
        pncCheckServiceClient.checkJuror(jurorNumber);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
