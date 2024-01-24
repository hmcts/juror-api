package uk.gov.hmcts.juror.api.moj.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception type thrown when the user has insufficient permissions to perform an action.
 */
public class UserPermissionsException extends RuntimeException {

    private UserPermissionsException(String message) {
        super(message);
    }

    /**
     * Exception type thrown when a court user tries to view/access records
     * relating to a court location they do not have permission for.
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class CourtUnavailable extends UserPermissionsException {

        public CourtUnavailable() {
            super("Court user does not have permission to access records for the provided court location");
        }

    }

}
