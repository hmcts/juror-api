package uk.gov.hmcts.juror.api.bureau.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception type thrown when excusal of a juror fails.
 * {@link uk.gov.hmcts.juror.api.bureau.service.ResponseExcusalServiceImpl}
 * {@link uk.gov.hmcts.juror.api.bureau.controller.ResponseExcusalController}
 */
public class ExcusalException extends RuntimeException {

    private ExcusalException(String message) {
        super(message);
    }

    private ExcusalException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Exception type thrown when unable to retrieve valid excusal codes.
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class UnableToRetrieveExcusalCodeList extends ExcusalException {
        public UnableToRetrieveExcusalCodeList() {
            super("Backend could not retrieve list of valid excusal codes.");
        }
    }

    /**
     * Exception type thrown when request specifies an invalid excusal code.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class RequestedCodeNotValid extends ExcusalException {
        public RequestedCodeNotValid(final String jurorId, final String code) {
            super(String.format("Request to excuse Juror %s failed as requested code %s is not valid", jurorId, code));
        }
    }

    /**
     * Exception type thrown when requested to excuse juror who does not exist.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class JurorNotFound extends ExcusalException {
        public JurorNotFound(final String jurorId) {
            super(String.format("Request to excuse Juror %s failed as Juror was not found", jurorId));
        }
    }

    /**
     * Exception type thrown when requested to excuse/reject excusal request for juror whose processing status is
     * completed.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class ResponseAlreadyCompleted extends ExcusalException {
        public ResponseAlreadyCompleted(final String jurorId) {
            super(String.format("Request to excuse/reject excusal for Juror %s failed as Juror's response processing "
                + "is already completed", jurorId));
        }
    }

    /**
     * Exception type thrown when request doesn't feature either a body or a version.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class RequestIsMissingDetails extends ExcusalException {
        public RequestIsMissingDetails(final String jurorId) {
            super(String.format("Request to excuse/reject excusal for Juror %s failed as request is missing either "
                + "a body or a version", jurorId));
        }
    }

    /**
     * Exception type thrown when optimistic locking fails.
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class OptimisticLockingFailure extends ExcusalException {
        public OptimisticLockingFailure(final String jurorId, Throwable cause) {
            super(String.format("Request to excuse/reject excusal for Juror %s failed due to "
                + "optimistic locking failure", jurorId), cause);
        }
    }
}
