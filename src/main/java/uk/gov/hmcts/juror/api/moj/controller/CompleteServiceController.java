package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.security.IsCourtUser;
import uk.gov.hmcts.juror.api.moj.controller.request.CompleteServiceJurorNumberListDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CompleteServiceValidationResponseDto;
import uk.gov.hmcts.juror.api.moj.service.CompleteServiceService;
import uk.gov.hmcts.juror.api.validation.PoolNumber;

@RestController
@Validated
@RequestMapping(value = "/api/v1/moj/complete-service", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Validation")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@IsCourtUser
public class CompleteServiceController {

    private final CompleteServiceService completeServiceService;

    @PatchMapping("/{poolNumber}/complete")
    @Operation(summary = "/api/v1/moj/complete-service/{poolNumber}/complete - Send a payload containing a list of "
        + "juror numbers and completion date so that the jurors can complete their service")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void completeService(
        @PathVariable @PoolNumber
        @Parameter(description = "Pool number", required = true)
        @Valid String poolNumber,
        @Valid @RequestBody CompleteServiceJurorNumberListDto completeServiceJurorNumberListDto) {
        completeServiceService.completeService(poolNumber, completeServiceJurorNumberListDto);
    }

    @PatchMapping("/dismissal")
    @Operation(summary = "/api/v1/moj/complete-service/dismissal - Send a payload containing a list of "
        + "juror numbers and completion date so that dismissed jurors can complete their service")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void completeDismissedJurorsService(
        @Valid @RequestBody CompleteServiceJurorNumberListDto completeServiceJurorNumberListDto) {
        completeServiceService.completeDismissedJurorsService(completeServiceJurorNumberListDto);
    }

    @PostMapping("/{poolNumber}/validate")
    @Operation(summary = "/api/v1/moj/complete-service/{poolNumber}/validate - Send a payload containing a list of "
        + "juror numbers to validate if they can complete service")
    public ResponseEntity<CompleteServiceValidationResponseDto> validateCompleteService(
        @PathVariable @PoolNumber
        @Parameter(description = "Pool number", required = true)
        @Valid String poolNumber,
        @Valid @RequestBody JurorNumberListDto jurorNumberListDto) {
        CompleteServiceValidationResponseDto completeServiceValidationResponseDto =
            completeServiceService.validateCanCompleteService(poolNumber, jurorNumberListDto);
        return ResponseEntity.ok(completeServiceValidationResponseDto);
    }
}
