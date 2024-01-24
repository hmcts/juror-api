package uk.gov.hmcts.juror.api.bureau.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.juror.api.bureau.controller.request.AutoAssignRequest;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Exception type thrown when auto-assignment fails.
 *
 * @see uk.gov.hmcts.juror.api.bureau.service.AutoAssignmentService#autoAssign(AutoAssignRequest, String)
 */
public class AutoAssignException extends RuntimeException {

    private AutoAssignException(String message) {
        super(message);
    }

    /**
     * Exception type thrown when requesting username is missing.
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class RequestingUserIsRequired extends AutoAssignException {
        public RequestingUserIsRequired() {
            super("Backend could not retrieve username from its own login token");
        }
    }

    /**
     * Exception type thrown when request specifies too much capacity for the size of the backlog.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class CapacityBiggerThanBacklog extends AutoAssignException {
        public CapacityBiggerThanBacklog(final int totalCapacity, final int backlogSize) {
            super(String.format("Total capacity of %d exceeds backlog size of %d", totalCapacity, backlogSize));
        }
    }

    /**
     * Exception type thrown when request specifies more than one capacity for the same staff member.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class DuplicateCapacityValues extends AutoAssignException {
        public DuplicateCapacityValues(final Collection<String> duplicateLogins) {
            super("Duplicate capacity values supplied for: " + duplicateLogins.stream().collect(Collectors.joining(","
                + " ")));
        }
    }

    /**
     * Exception type thrown when request specifies staff members who do not exist for auto-assignment.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class MissingLogins extends AutoAssignException {
        public MissingLogins(Collection<String> missing) {
            super("Some logins did not represent valid staff members: " + missing.stream().collect(Collectors.joining(
                ", ")));
        }
    }

    /**
     * Exception type thrown when request specifies ineligible staff members for auto-assignment.
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class IneligibleStaff extends AutoAssignException {
        public IneligibleStaff(Collection<String> ineligibleStaffLogins) {
            super("The following staff members are not eligible for auto-assignment: " + ineligibleStaffLogins.stream()
                .collect(
                    Collectors.joining(", ")));
        }
    }
}
