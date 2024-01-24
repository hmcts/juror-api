package uk.gov.hmcts.juror.api.bureau.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Juror response was not found in persistence.
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class JurorResponseNotFoundException extends RuntimeException {
    public JurorResponseNotFoundException(String message) {
        super(message);
    }
}
