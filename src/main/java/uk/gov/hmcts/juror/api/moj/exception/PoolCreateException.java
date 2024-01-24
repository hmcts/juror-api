package uk.gov.hmcts.juror.api.moj.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception type thrown when creating a pool fails.
 * {@link uk.gov.hmcts.juror.api.moj.service.PoolCreateServiceImpl}
 */
public class PoolCreateException extends RuntimeException {

    private PoolCreateException(String message) {
        super(message);
    }

    /**
     * Exception type thrown when an invalid value is supplied for the no of Citizens to summon.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidNoOfCitizensToSummon extends PoolCreateException {

        public InvalidNoOfCitizensToSummon() {
            super("New Pool must be created with a valid number of citizens as per yield for location");
        }

    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "invalid_yield")
    public static class InvalidNoOfCitizensToSummonForYield extends PoolCreateException {

        public InvalidNoOfCitizensToSummonForYield() {
            super("Cannot summon more citizens than allowed by the yield for location");
        }

    }

    /**
     * Exception type thrown when an invalid Map is sent to add citizens to coroners pool.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidAddCitizensToCoronersPool extends PoolCreateException {

        public InvalidAddCitizensToCoronersPool(String poolNumber) {
            super(String.format(
                "Must supply a valid combination of postcodes and numbers to add to coroner pool %s",
                poolNumber
            ));
        }

    }

    /**
     * Exception type thrown when unable to obtain a lock on Voters table.
     */
    @ResponseStatus(HttpStatus.LOCKED)
    public static class UnableToObtainVotersLock extends PoolCreateException {

        public UnableToObtainVotersLock() {
            super("Unable to obtain Voters lock for location on Pool creation");
        }
    }

    /**
     * Exception type thrown when error occurred on pool creation.
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class UnableToCreatePool extends PoolCreateException {

        public UnableToCreatePool() {
            super("Error occurred on Pool creation");
        }
    }

    /**
     * Exception type thrown when an invalid value is supplied for the pool status.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidPoolStatus extends PoolCreateException {

        public InvalidPoolStatus() {
            super("Pool status is invalid or empty");
        }

    }

    /**
     * Exception type thrown when the location code does not exist in our records.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class CourtLocationNotFound extends PoolCreateException {

        public CourtLocationNotFound() {
            super("Court location is invalid or does not exist");
        }
    }

    /**
     * Exception type thrown when an invalid value is supplied for the no of jurors requested.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidNoOfJurorsRequested extends PoolCreateException {

        public InvalidNoOfJurorsRequested(int lowLimit, int upperLimit) {
            super(String.format("Jurors requested must be within the limits %s and %s (inclusive)",
                lowLimit, upperLimit
            ));
        }

    }

    /**
     * Exception type thrown when Coroner Pool does not exist.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class CoronerPoolNotFound extends PoolCreateException {

        public CoronerPoolNotFound(String poolNumber) {
            super(String.format("Unable to find coroner pool %s", poolNumber));
        }
    }

}
