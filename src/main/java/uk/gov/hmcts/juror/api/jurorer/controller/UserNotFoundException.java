package uk.gov.hmcts.juror.api.jurorer.controller;

/**
 * Exception thrown when a user is not found.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
