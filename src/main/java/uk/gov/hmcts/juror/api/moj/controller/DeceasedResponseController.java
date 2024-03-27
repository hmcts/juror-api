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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.bureau.exception.ExcusalException;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.MarkAsDeceasedDto;
import uk.gov.hmcts.juror.api.moj.service.DeceasedResponseService;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/moj/deceased-response", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Validated
@Tag(name = "Summons Management")
public class DeceasedResponseController {

    @NonNull
    private final DeceasedResponseService deceasedResponseService;

    @PostMapping("/excuse-deceased-juror")
    @Operation(summary = "POST deceased information for a specific juror",
        description = "Mark a Juror as deceased with information provided")
    public ResponseEntity<Void> markJurorAsDeceased(@Parameter(hidden = true) BureauJwtAuthentication auth,
                                                    @Valid @RequestBody MarkAsDeceasedDto markAsDeceasedDto)
        throws ExcusalException {

        BureauJwtPayload payload = (BureauJwtPayload) auth.getPrincipal();

        deceasedResponseService.markAsDeceased(payload, markAsDeceasedDto);
        return ResponseEntity.ok().build();
    }
}
