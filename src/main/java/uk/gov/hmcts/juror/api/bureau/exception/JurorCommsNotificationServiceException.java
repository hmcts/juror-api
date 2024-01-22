package uk.gov.hmcts.juror.api.bureau.exception;

public class JurorCommsNotificationServiceException extends RuntimeException {

    public JurorCommsNotificationServiceException(String message) {
        super(message);
    }

    public JurorCommsNotificationServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
