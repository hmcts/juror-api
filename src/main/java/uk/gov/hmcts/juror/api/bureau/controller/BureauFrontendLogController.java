package uk.gov.hmcts.juror.api.bureau.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Bureau Frontend application log sink endpoint.
 */
@RestController
@RequestMapping(value = "/api/v1/auth/bureau", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Bureau Frontend Logging API", description = "Bureau Frontend logging sink API")
@Slf4j
public class BureauFrontendLogController {
    /**
     * Endpoint to allow frontend application to send log messages to standard logger.
     *
     * @param message Log message
     * @return No content 204 response.
     */
    @PostMapping(path = "/log")
    @Operation(summary = "/auth/bureau/log - Bureau Log API", description = "Provide a logsink for the Bureau "
        + "frontend API to "
        + "send logging messages directly to the backend API logs.")
    public ResponseEntity<Object> log(@Parameter(description = "Log message content") @RequestBody String message) {
        log.info(message);
        return ResponseEntity.noContent().build();
    }
}
