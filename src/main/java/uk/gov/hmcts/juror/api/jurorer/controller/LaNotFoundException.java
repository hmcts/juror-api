package uk.gov.hmcts.juror.api.jurorer.controller;

/**
 * Exception thrown when a Local Authority is not found.
 */
public class LaNotFoundException extends RuntimeException {
    public LaNotFoundException(String message) {
        super(message);
    }
}
