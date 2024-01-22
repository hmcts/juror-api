package uk.gov.hmcts.juror.api.moj.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.juror.api.moj.service.deferralmaintenance.ManageDeferralsServiceImpl;

/**
 * Exception type thrown when interacting with database sequences.
 * {@link ManageDeferralsServiceImpl}
 */
public class JurorSequenceException extends RuntimeException {

    private JurorSequenceException(String message) {
        super(message);
    }

    /**
     * Exception type thrown trying to retrieve the next value in a sequence.
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class SequenceNextValNotFound extends JurorSequenceException {

        public SequenceNextValNotFound(String sequenceName) {
            super(String.format("Unable to retrieve the next value for the sequence: %s", sequenceName));
        }

    }

}
