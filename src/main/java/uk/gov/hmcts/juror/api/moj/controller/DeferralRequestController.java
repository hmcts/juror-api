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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralRequestDto;
import uk.gov.hmcts.juror.api.moj.service.DeferralResponseService;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/moj/deferral-response", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Validated
@Tag(name = "Juror Management")
public class DeferralRequestController {

    @NonNull
    private final DeferralResponseService deferralResponseService;

    @PutMapping(path = "/juror/{jurorNumber}")
    @Operation(summary = "Enter and respond to an deferral request from a Juror")
    public ResponseEntity<Void> respondToDeferralRequest(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
        @RequestBody @Valid DeferralRequestDto deferralRequestDto) {
        log.info("Begin process for response to deferral request");
        deferralResponseService.respondToDeferralRequest(payload, deferralRequestDto);
        return ResponseEntity.ok().build();
    }

}
