package uk.gov.hmcts.juror.api.moj.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * A generic exceptions class for exceptions handled in the moj package.
 */

@Slf4j
public class MojException extends RuntimeException {

    private MojException(String customErrorMessage, Throwable rootException) {
        super(customErrorMessage, rootException);
        log.error(customErrorMessage);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class BadRequest extends MojException {
        public BadRequest(String customErrorMessage, Throwable rootException) {
            super(customErrorMessage, rootException);
        }
    }

    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @Getter
    public static class BusinessRuleViolation extends MojException {

        private final ErrorCode errorCode;

        public BusinessRuleViolation(String customErrorMessage, ErrorCode errorCode) {
            this(customErrorMessage, errorCode, null);
        }

        public BusinessRuleViolation(String customErrorMessage, ErrorCode errorCode, Throwable cause) {
            super(customErrorMessage, cause);
            this.errorCode = errorCode;
        }


        public enum ErrorCode {
            COMPLETE_SERVICE_JUROR_IN_INVALID_STATE,
            FAILED_TO_ATTEND_HAS_COMPLETION_DATE,
            FAILED_TO_ATTEND_HAS_ATTENDANCE_RECORD,
            JUROR_STATUS_MUST_BE_FAILED_TO_ATTEND,
            JUROR_STATUS_MUST_BE_RESPONDED,
            LETTER_CANNOT_GENERATE_ON_WEEKEND,
            EXPENSES_CANNOT_BE_LESS_THAN_ZERO,
            INVALID_FORMAT,
            PLACEHOLDER_MUST_HAVE_VALUE,
            INVALID_SEND_TYPE,
            JUROR_MUST_HAVE_EMAIL,
            JUROR_MUST_HAVE_PHONE_NUMBER,
            JUROR_NOT_APART_OF_TRIAL,
            MAX_ITEMS_EXCEEDED,
            TRIAL_HAS_MEMBERS
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class NotFound extends MojException {
        public NotFound(String customErrorMessage, Throwable rootException) {
            super(customErrorMessage, rootException);
        }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class Forbidden extends MojException {
        public Forbidden(String customErrorMessage, Throwable rootException) {
            super(customErrorMessage, rootException);
        }
    }

    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public static class NotImplemented extends MojException {
        public NotImplemented(String customErrorMessage, Throwable rootException) {
            super(customErrorMessage, rootException);
        }
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class InternalServerError extends MojException {
        public InternalServerError(String customErrorMessage, Throwable rootException) {
            super(customErrorMessage, rootException);
        }
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public static class RemoteGatewayException extends MojException {
        public RemoteGatewayException(String customErrorMessage, Throwable rootException) {
            super(customErrorMessage, rootException);
        }
    }
}
