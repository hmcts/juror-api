package uk.gov.hmcts.juror.api.moj.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.juror.api.moj.service.deferralmaintenance.ManageDeferralsServiceImpl;

/**
 * Exception type thrown when managing deferrals.
 * {@link ManageDeferralsServiceImpl}
 */
public class currentlyDeferredException extends RuntimeException {

    private currentlyDeferredException(String message) {
        super(message);
    }

    /**
     * Exception type thrown when a lookup in the POOL table from a DEFER_DBF record fails.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class DeferredMemberNotFound extends currentlyDeferredException {

        public DeferredMemberNotFound(String jurorNumber) {
            super(String.format("Unable to find an associated Pool Member for the deferred juror: %s", jurorNumber));
        }
    }

}
