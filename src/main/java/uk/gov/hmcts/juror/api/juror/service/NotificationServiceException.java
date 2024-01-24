package uk.gov.hmcts.juror.api.juror.service;

public class NotificationServiceException extends RuntimeException {
    public NotificationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
