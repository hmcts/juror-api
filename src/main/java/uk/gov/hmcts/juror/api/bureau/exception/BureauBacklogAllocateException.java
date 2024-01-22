package uk.gov.hmcts.juror.api.bureau.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception type thrown when backlog allocation fails.
 *
 * @see uk.gov.hmcts.juror.api.bureau.service.BureauBacklogAllocateService#allocateBacklogReplies
 * (BureauBacklogAllocateRequestDto, String)
 */
public class BureauBacklogAllocateException extends RuntimeException {

    private BureauBacklogAllocateException(String message) {
        super(message);
    }

    /**
     * Exception type thrown when requesting username is missing.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class RequestingUserIsRequired extends BureauBacklogAllocateException {
        public RequestingUserIsRequired() {
            super("Backend could not retrieve username from its own login token");
        }
    }

    /**
     * Exception type thrown when there is an issue attempting to persist the allocations.
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class FailedToSaveAllocations extends BureauBacklogAllocateException {
        public FailedToSaveAllocations(String login) {
            super("There was a problem attempting to save the allocations for officer : " + login);
        }
    }

}
