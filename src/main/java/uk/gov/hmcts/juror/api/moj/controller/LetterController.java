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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.moj.controller.request.AdditionalInformationDto;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.service.letter.RequestInformationLetterService;

/**
 * API endpoints related to letters.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/moj/letter", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@Tag(name = "Summons Management")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class LetterController {

    @NonNull
    private final RequestInformationLetterService requestInformationLetterService;

    @PostMapping(path = "/request-information")
    @Operation(summary = "Request information",
        description = "Request information from the juror related to the juror response form")
    public ResponseEntity<String> requestInformation(@Parameter(hidden = true) BureauJwtAuthentication auth,
                                                     @RequestBody @Valid AdditionalInformationDto additionalInformationDto) {
        final String jurorNumber = additionalInformationDto.getJurorNumber();
        log.trace("Process to queue the Request Letter started for juror {} ", jurorNumber);

        BureauJWTPayload payload = (BureauJWTPayload) auth.getPrincipal();
        if (!payload.getOwner().equalsIgnoreCase(JurorDigitalApplication.JUROR_OWNER)) {
            throw new MojException.Forbidden("Request additional information "
                + "letter is a Bureau only process", null);
        }

        requestInformationLetterService.requestInformation(payload, additionalInformationDto);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(String.format("Request Letter queued for juror number %s", jurorNumber));
    }
}

