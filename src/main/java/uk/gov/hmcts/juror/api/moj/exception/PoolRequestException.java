package uk.gov.hmcts.juror.api.moj.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception type thrown when requesting a new pool fails.
 * {@link uk.gov.hmcts.juror.api.moj.service.PoolRequestServiceImpl}
 * {@link uk.gov.hmcts.juror.api.moj.controller.RequestPoolController}
 */
public class PoolRequestException extends RuntimeException {

    private PoolRequestException(String message) {
        super(message);
    }

    /**
     * Exception type thrown when a new Pool is requested using an existing Pool Number and Owner combination.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class DuplicatePoolRequest extends PoolRequestException {

        public DuplicatePoolRequest(String poolNumber) {
            super(String.format("Failed to create a new Pool. A Pool already exists with Pool Number: %s",
                poolNumber
            ));
        }
    }

    /**
     * Exception type thrown when an active Pool does not exist for a given pool ID (poolNumber + owner).
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class PoolRequestNotFoundForPoolId extends PoolRequestException {

        public PoolRequestNotFoundForPoolId(String poolNumber, String owner) {
            super(String.format("Unable to find a pool with Pool Number: %s and Owner: %s",
                poolNumber, owner
            ));
        }
    }

    /**
     * Exception type thrown when an active Pool does not exist for a given pool number.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class PoolRequestNotFound extends PoolRequestException {

        public PoolRequestNotFound(String poolNumber) {
            super(String.format("Unable to find an active pool for %s", poolNumber));
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class PoolRequestDateInvalid extends PoolRequestException {

        public PoolRequestDateInvalid(String poolNumber) {
            super(String.format("Unable to find valid date for pool %s", poolNumber));
        }
    }

}
