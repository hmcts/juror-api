package uk.gov.hmcts.juror.api.juror.controller;

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
 * Public Frontend application log sink endpoint.
 */
@RestController
@RequestMapping(value = "/api/v1/auth/public", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Public Frontend Logging API", description = "Public Frontend logging sink API")
@Slf4j
public class PublicFrontendLogController {
    /**
     * Endpoint to allow frontend application to send log messages to standard logger.
     *
     * @param message Log message
     * @return No content 204 response.
     */
    @PostMapping(path = "/log")
    @Operation(summary = "Public Log API", description = "Provide a log sink for the Public "
        + "frontend API to "
        + "send logging messages directly to the backend API logs.")
    public ResponseEntity<Object> log(@Parameter(description = "Log message content") @RequestBody String message) {
        log.info(message);
        return ResponseEntity.noContent().build();
    }
}
