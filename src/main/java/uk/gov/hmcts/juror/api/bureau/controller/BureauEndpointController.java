package uk.gov.hmcts.juror.api.bureau.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.bureau.controller.request.BureauResponseStatusUpdateDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauJurorDetailDto;
import uk.gov.hmcts.juror.api.bureau.exception.BureauOptimisticLockingException;
import uk.gov.hmcts.juror.api.bureau.service.BureauService;
import uk.gov.hmcts.juror.api.bureau.service.ResponseStatusUpdateService;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;

/**
 * API endpoints controller for Bureau Endpoints.
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/bureau", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Bureau API", description = "Bureau Interface API")
public class BureauEndpointController {
    private final BureauService bureauService;
    private final ResponseStatusUpdateService responseStatusUpdateService;

    @Autowired
    public BureauEndpointController(final ResponseStatusUpdateService responseStatusUpdateService,
                                    final BureauService bureauService) {
        Assert.notNull(bureauService, "BureauService cannot be null!");
        Assert.notNull(responseStatusUpdateService, "ResponseStatusUpdateService cannot be null!");
        this.bureauService = bureauService;
        this.responseStatusUpdateService = responseStatusUpdateService;
    }

    /**
     * Get the master view dto for the bureau screens of a single juror response.
     *
     * @param jurorId Juror number of the response to view
     * @return Fully populated dto.
     */
    @GetMapping(path = "/juror/{jurorId}")
    @Operation(summary = "/bureau/juror/{jurorId} - Get juror details by juror number",
        description = "Retrieve details of a single juror by his/her juror number")
    public ResponseEntity<BureauJurorDetailDto> retrieveBureauJurorDetailsById(@Parameter(description = "Valid juror "
        + "number", required = true) @PathVariable String jurorId) {
        final BureauJurorDetailDto details = bureauService.getDetailsByJurorNumber(jurorId);
        return ResponseEntity.ok().body(details);
    }

    /**
     * Update and process juror response.
     *
     * @param jurorNumber Juror number of response to process
     * @param updateDto   Update information
     * @param principal   Currently authenticated bureau officer details
     * @return HTTP 202 accepted - no body content
     * @throws BureauOptimisticLockingException Response data from the UI is outdated. Version mismatch with DB.
     */
    @PostMapping(path = "/status/{jurorNumber}")
    @Operation(summary = "/bureau/status/{jurorNumber} - Update and process juror response",
        description = "Update and process juror response")
    public ResponseEntity<Object> updateResponseStatus(
        @Parameter(description = "Juror number") @PathVariable String jurorNumber,
        @Parameter(description = "Status update details") @RequestBody BureauResponseStatusUpdateDto updateDto,
        @Parameter(hidden = true) BureauJwtAuthentication principal)
        throws BureauOptimisticLockingException {

        final BureauJWTPayload jwtPayload = (BureauJWTPayload) principal.getPrincipal();
        try {
            responseStatusUpdateService.updateJurorResponseStatus(jurorNumber, updateDto.getStatus(),
                updateDto.getVersion(), jwtPayload.getLogin()
            );
        } catch (OptimisticLockingFailureException olfe) {
            log.warn("Juror {} response was updated by another user!", jurorNumber);
            throw new BureauOptimisticLockingException(olfe);
        }

        log.info("Status updated successfully");
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .build();
    }
}
