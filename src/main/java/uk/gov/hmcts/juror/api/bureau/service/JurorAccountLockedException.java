package uk.gov.hmcts.juror.api.bureau.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a staff account is locked in the legacy Juror application.
 */
@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class JurorAccountLockedException extends RuntimeException {
    public JurorAccountLockedException(String message) {
        super(message);
    }
}
