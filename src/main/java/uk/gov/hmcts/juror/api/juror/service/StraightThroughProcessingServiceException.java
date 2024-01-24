package uk.gov.hmcts.juror.api.juror.service;

/**
 * Thrown by {@link StraightThroughProcessor} when a straight through processing fails business rule validation.
 */
public class StraightThroughProcessingServiceException extends RuntimeException {
    public StraightThroughProcessingServiceException(String message) {
        super(message);
    }


    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    public StraightThroughProcessingServiceException(String message, Throwable cause) {
        super(message, cause.fillInStackTrace());
    }


    public StraightThroughProcessingServiceException(Throwable cause) {
        super(cause);
    }

    /**
     * Thrown by {@link StraightThroughProcessor} when a straight-through age-excusal processing fails
     * business rule validation.
     */
    public static class AgeExcusal extends StraightThroughProcessingServiceException {

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }

        public AgeExcusal(String message) {
            super(message);
        }

        public AgeExcusal(Throwable cause) {
            super(cause);
        }
    }

    /**
     * Thrown by {@link StraightThroughProcessor} when a straight-through deceased-excusal processing
     * fails business rule validation.
     */
    public static class DeceasedExcusal extends StraightThroughProcessingServiceException {

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }

        public DeceasedExcusal(String message) {
            super(message);
        }

        public DeceasedExcusal(Throwable cause) {
            super(cause);
        }
    }
}
