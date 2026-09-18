package uk.gov.hmcts.juror.api.moj.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class PoolDeleteException extends RuntimeException {

    private PoolDeleteException(String message) {
        super(message);
    }

    /**
     * Exception type thrown when a pool cannot be deleted for general reasons.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class UnableToDeletePoolException extends PoolDeleteException {

        public UnableToDeletePoolException(String poolNumber) {
            super("Could not delete pool with Pool Number: %s".formatted(poolNumber));
        }
    }

    /**
     * Exception type thrown when a pool has members and cannot be deleted.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class PoolHasMembersException extends PoolDeleteException {

        public PoolHasMembersException(String poolNumber) {
            super("This pool has members and cannot be deleted, Pool Number: %s".formatted(poolNumber));
        }
    }

    /**
     * Exception type thrown when a user does not have permission to delete an empty pool.
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class InsufficientPermission extends PoolDeleteException {

        public InsufficientPermission(String login, String poolNumber) {
            super("User %s has insufficient permission to delete a pool, Pool Number: %s".formatted(
                login,
                poolNumber
            ));
        }
    }

    /**
     * Exception type thrown when a pool is locked.
     */
    @ResponseStatus(HttpStatus.LOCKED)
    public static class PoolIsCurrentlyLocked extends PoolDeleteException {

        public PoolIsCurrentlyLocked(String poolNumber) {
            super("The pool is currently locked and cannot be deleted, Pool Number: %s".formatted(poolNumber));
        }
    }

}
