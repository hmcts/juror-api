package uk.gov.hmcts.juror.api.moj.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Map;

/**
 * A generic exceptions class for exceptions handled in the moj package.
 */

@Slf4j
public class MojException extends RuntimeException {

    private MojException(String customErrorMessage, Throwable rootException) {
        super(customErrorMessage, rootException);
        log.error(customErrorMessage, rootException);
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

        @JsonProperty("meta_data")
        private final Map<String, Object> metaData;

        public BusinessRuleViolation(String customErrorMessage, ErrorCode errorCode) {
            this(customErrorMessage, errorCode, null);
        }

        public BusinessRuleViolation(String customErrorMessage, ErrorCode errorCode, Map<String,Object> metaData) {
            this(customErrorMessage, errorCode, metaData, null);
        }

        public BusinessRuleViolation(String customErrorMessage, ErrorCode errorCode, Map<String,Object> metaData,
                                     Throwable cause) {
            super(customErrorMessage, cause);
            this.errorCode = errorCode;
            this.metaData = metaData;
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
            TRIAL_HAS_MEMBERS,
            ATTENDANCE_RECORD_ALREADY_EXISTS,
            APPEARANCE_RECORD_BEFORE_SERVICE_START_DATE,
            DATA_OUT_OF_DATE,
            CAN_NOT_APPROVE_OWN_EDIT,
            EXPENSE_VALUES_REDUCED_LESS_THAN_PAID,
            CAN_NOT_APPROVE_MORE_THAN_LIMIT,
            WRONG_EXPENSE_TYPE,
            CANNOT_DELETE_USED_JUDGE,
            DAY_ALREADY_EXISTS,
            EMAIL_IN_USE,
            CODE_ALREADY_IN_USE,
            NUMBER_OF_JURORS_EXCEEDS_AVAILABLE,
            APPORTION_SMART_CARD_NON_DRAFT_DAYS,
            NUMBER_OF_JURORS_EXCEEDS_LIMITS,
            CANNOT_REFUSE_FIRST_DEFERRAL,
            TRIAL_HAS_ENDED,
            NO_PANEL_EXIST,
            APPEARANCE_MUST_HAVE_NO_APPROVED_EXPENSES,
            JUROR_MUST_BE_CHECKED_IN,
            CANNOT_EDIT_TRIAL_WITH_JURORS,
            CANNOT_EDIT_COMPLETED_TRIAL,
            JUROR_DATE_OF_BIRTH_REQUIRED,
            INVALID_APPEARANCES_STATUS,
            DATA_IS_OUT_OF_DATE,
            JUROR_HAS_BEEN_DEFERRED_BEFORE,
            COULD_NOT_FIND_ENOUGH_VOTERS,
            COULD_NOT_FIND_ENOUGH_ELIGIBLE_VOTERS,
            DAY_ALREADY_CONFIRMED,
            CANNOT_DEFER_TO_EXISTING_POOL,
            CANNOT_DEFER_JUROR_WITH_APPEARANCE,
            CANNOT_PROCESS_EMPANELLED_JUROR,
            CANNOT_RE_ADD_JUROR_TO_PANEL
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
