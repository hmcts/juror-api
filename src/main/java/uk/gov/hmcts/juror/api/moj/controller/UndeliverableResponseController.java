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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.bureau.exception.ExcusalException;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.service.UndeliverableResponseService;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/moj/undeliverable-response", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Validated
@Tag(name = "Summons Management")
public class UndeliverableResponseController {
    @NonNull
    private final UndeliverableResponseService undeliverableResponseService;

    @PutMapping("/{jurorNumber}")
    @Operation(summary = "Mark a Juror as undeliverable with information provided")
    public ResponseEntity<Void> markJurorAsUndeliverable(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @PathVariable(name = "jurorNumber") @JurorNumber @Valid String jurorNumber) throws ExcusalException {
        undeliverableResponseService.markAsUndeliverable(payload, jurorNumber);
        return ResponseEntity.ok().build();
    }
}
