package uk.gov.hmcts.juror.api.moj.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.bureau.controller.request.BureauResponseStatusUpdateDto;
import uk.gov.hmcts.juror.api.bureau.exception.BureauOptimisticLockingException;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPersonalDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.summonsmanagement.JurorResponseRetrieveRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.summonsmanagement.JurorResponseRetrieveResponseDto;
import uk.gov.hmcts.juror.api.moj.service.SummonsReplyStatusUpdateService;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.JurorResponseRetrieveService;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.JurorResponseService;

/**
 * API endpoints controller for Juror Response Endpoints - paper and digital responses.
 */

@Slf4j
@RestController
@Tag(name = "Summons Management")
@RequestMapping(value = "/api/v1/moj/juror-response", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class JurorResponseController {

    @NonNull
    private final JurorResponseService jurorResponseService;

    @NotNull
    private final SummonsReplyStatusUpdateService summonsReplyStatusUpdateService;

    @NonNull
    private final JurorResponseRetrieveService jurorResponseRetrieveService;

    /**
     * Update and process juror response.
     *
     * @param jurorNumber Juror number of response to process
     * @param updateDto   Update information
     * @param payload     Currently authenticated bureau officer details
     * @return HTTP 202 accepted - no content
     * @throws BureauOptimisticLockingException Response data from the UI is outdated. Version mismatch with DB.
     */
    @PostMapping(path = "/update-status/{jurorNumber}")
    @Operation(summary = "Update the processing status of a Juror Paper Response",
        description = "Juror records with a pending response can be uniquely identified by just the Juror Number")
    public ResponseEntity<Object> updateResponseStatus(
        @Parameter(description = "Juror number") @PathVariable String jurorNumber,
        @Parameter(description = "Status update details") @RequestBody BureauResponseStatusUpdateDto updateDto,
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload)
        throws BureauOptimisticLockingException {
        try {
            summonsReplyStatusUpdateService.updateDigitalJurorResponseStatus(jurorNumber, updateDto.getStatus(),
                payload);
        } catch (OptimisticLockingFailureException olfe) {
            log.warn("Juror {} response was updated by another user!", jurorNumber);
            throw new BureauOptimisticLockingException(olfe);
        }

        log.info("Status updated successfully");
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .build();
    }

    /**
     * Update Paper or Digital response for a given Juror.
     *
     * @param payload                 JSON Web Token containing user authentication context
     * @param jurorPersonalDetailsDto Response information to persist
     * @param jurorNumber             The juror number to update the personal details for
     */
    @PatchMapping(path = "/juror/{jurorNumber}/details/personal")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Update personal details of an existing paper or digital response")
    public void updateJurorPersonalDetails(@Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
                                           @RequestBody @Valid JurorPersonalDetailsDto jurorPersonalDetailsDto,
                                           @Parameter(description = "Valid juror number", required = true)
                                           @Size(min = 9, max = 9)
                                           @PathVariable("jurorNumber")
                                           @Valid String jurorNumber) {
        jurorResponseService.updateJurorPersonalDetails(payload, jurorPersonalDetailsDto, jurorNumber);
    }

    @PostMapping(path = "/retrieve")
    @Operation(summary = "Retrieve responses based on search criteria")
    public ResponseEntity<JurorResponseRetrieveResponseDto> retrieveJurorResponse(
        @Parameter(hidden = true) @AuthenticationPrincipal BureauJwtPayload payload,
        @RequestBody @Valid JurorResponseRetrieveRequestDto request) {
        return ResponseEntity.ok().body(jurorResponseRetrieveService.retrieveJurorResponse(request, payload));
    }
}
