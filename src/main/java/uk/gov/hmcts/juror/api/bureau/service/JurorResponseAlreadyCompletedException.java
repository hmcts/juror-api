package uk.gov.hmcts.juror.api.bureau.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class JurorResponseAlreadyCompletedException extends RuntimeException {
    public JurorResponseAlreadyCompletedException(String message) {
        super(message);
    }
}
