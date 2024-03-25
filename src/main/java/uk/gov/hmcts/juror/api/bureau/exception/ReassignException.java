package uk.gov.hmcts.juror.api.bureau.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception type thrown when re-assignment of a group of responses and deactivation of officer fails.
 */
public class ReassignException extends RuntimeException {

    private ReassignException(String message) {
        super(message);
    }

    /**
     * Exception type thrown when request specifies a staff member that cannot be found.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class StaffMemberNotFound extends ReassignException {
        public StaffMemberNotFound(String staffLogin) {
            super(String.format("Reassignment failed as staff member %s cannot be found.", staffLogin));
        }
    }

    /**
     * Exception type thrown when request specifies assigning to backlog but this is not allowed.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class UnableToAssignToBacklog extends ReassignException {
        public UnableToAssignToBacklog(String staffLogin, String status) {
            super(String.format(
                "Reassignment of staff members %s responses failed, as responses with status %s cannot be assigned to"
                    + " backlog.",
                staffLogin,
                status
            ));
        }
    }
}
