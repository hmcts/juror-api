package uk.gov.hmcts.juror.api.bureau.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Juror response has already been marked as complete within Juror Digital.
 */
@ResponseStatus(HttpStatus.GONE)
public class ResponseAlreadyMergedException extends RuntimeException {
    public ResponseAlreadyMergedException() {
        super("Response already completed");
    }
}
