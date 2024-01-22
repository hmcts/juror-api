package uk.gov.hmcts.juror.api.moj.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class PoolEditException extends RuntimeException {

    private PoolEditException(String message) {
        super(message);
    }


    /**
     * Exception type thrown when a pool request is not in an editable state.
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class CannotEditPoolRequest extends PoolEditException {

        public CannotEditPoolRequest(String login, String poolNumber) {
            super(String.format("Pool Request is not editable, User: %s, Pool Number: %s", login, poolNumber));
        }
    }

    /**
     * Exception type thrown when a number requested for a pool is invalid or total required is invalid.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidNoUpdate extends PoolEditException {

        public InvalidNoUpdate(String login, String poolNumber, String updatedType) {
            super(String.format("Edit for %s is invalid, User: %s, Pool Number: %s", updatedType, login, poolNumber));
        }
    }

    /**
     * Exception type thrown trying to retrieve the Pool Total required value.
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class PoolExtendRecordNotFound extends PoolEditException {

        public PoolExtendRecordNotFound(String poolNumber) {
            super(String.format("Unable to retrieve the total required value for the pool: %s", poolNumber));
        }

    }

}
