package uk.gov.hmcts.juror.api.juror.notify;

/**
 * Exception thrown configuring or contacting the Notify.gov service.
 */
public class NotifyApiException extends RuntimeException {
    public NotifyApiException(String message) {
        super(message);
    }

    public NotifyApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
