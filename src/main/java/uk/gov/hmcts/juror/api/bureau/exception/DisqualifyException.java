package uk.gov.hmcts.juror.api.bureau.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception type thrown when disqualification of a juror fails.
 * {@link uk.gov.hmcts.juror.api.bureau.service.ResponseDisqualifyServiceImpl}
 * {@link uk.gov.hmcts.juror.api.bureau.controller.ResponseDisqualifyController}
 */
public class DisqualifyException extends RuntimeException {

    private DisqualifyException(String message) {
        super(message);
    }

    /**
     * Exception type thrown when unable to retrieve valid disqualification codes.
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class UnableToRetrieveDisqualifyCodeList extends DisqualifyException {
        public UnableToRetrieveDisqualifyCodeList() {
            super("Backend could not retrieve list of valid disqualification codes.");
        }
    }

    /**
     * Exception type thrown when request specifies an invalid disqualification code.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class RequestedCodeNotValid extends DisqualifyException {
        public RequestedCodeNotValid(final String jurorId, final String code) {
            super(String.format(
                "Request to disqualify Juror %s failed as requested code %s is not valid",
                jurorId,
                code
            ));
        }
    }

    /**
     * Exception type thrown when requested to disqualify juror who does not exist.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class JurorNotFound extends DisqualifyException {
        public JurorNotFound(final String jurorId) {
            super(String.format("Request to disqualify Juror %s failed as Juror was not found", jurorId));
        }
    }

    /**
     * Exception type thrown when requested to disqualify juror whose processing status is completed.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class ResponseAlreadyCompleted extends DisqualifyException {
        public ResponseAlreadyCompleted(final String jurorId) {
            super(String.format(
                "Request to disqualify Juror %s failed as Juror's response processing is already completed",
                jurorId
            ));
        }
    }

    /**
     * Exception type thrown when request doesn't feature either a body or a version.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class RequestIsMissingDetails extends DisqualifyException {
        public RequestIsMissingDetails(final String jurorId) {
            super(String.format(
                "Request to disqualify Juror %s failed as request is missing either a body or a version",
                jurorId
            ));
        }
    }

    /**
     * Exception type thrown when optimistic locking fails.
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class OptimisticLockingFailure extends DisqualifyException {
        public OptimisticLockingFailure(final String jurorId) {
            super(String.format("Request to disqualify Juror %s failed due to optimistic locking failure", jurorId));
        }
    }
}
