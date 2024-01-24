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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.ExcusalDecisionDto;
import uk.gov.hmcts.juror.api.moj.service.ExcusalResponseService;
import uk.gov.hmcts.juror.api.validation.JurorNumber;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/moj/excusal-response", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Validated
@Tag(name = "Summons Management")
public class ExcusalResponseController {

    @NonNull
    private final ExcusalResponseService excusalResponseService;

    /**
     * Enter and respond to an excusal request from a Juror.
     *
     * @param payload            JSON Web Token principal
     * @param excusalDecisionDto DTO containing the excusal code, officer's decision and reply type
     * @param jurorNumber        The juror number relevant to the excusal request
     */
    @PutMapping(path = "/juror/{jurorNumber}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Enter and respond to an excusal request from a Juror")
    public void respondToExcusalRequest(@Parameter(hidden = true) @AuthenticationPrincipal BureauJWTPayload payload,
                                        @RequestBody @Valid ExcusalDecisionDto excusalDecisionDto,
                                        @Parameter(description = "Valid juror number", required = true)
                                        @PathVariable("jurorNumber") @JurorNumber
                                        @Valid String jurorNumber) {
        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber);
    }
}
