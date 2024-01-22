package uk.gov.hmcts.juror.api.bureau.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Failed to create new staff member - business rules violated.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class StaffMemberCrudException extends RuntimeException {
    public StaffMemberCrudException(String message) {
        super(message);
    }

    public StaffMemberCrudException(String message, Throwable cause) {
        super(message, cause);
    }
}
