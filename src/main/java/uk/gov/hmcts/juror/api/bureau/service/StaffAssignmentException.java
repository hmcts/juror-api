package uk.gov.hmcts.juror.api.bureau.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Failed to assign a staff member to a juror response.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class StaffAssignmentException extends RuntimeException {
    private static final String LOG_MESSAGE = "StaffAssignment exception caught during assignment";
    private static final String REASON_CODE = "OTHER";

    public StaffAssignmentException() {
        super();
    }

    StaffAssignmentException(String message) {
        super(message);
    }

    public String getLogMessage() {
        return LOG_MESSAGE;
    }

    public String getReasonCode() {
        return REASON_CODE;
    }

    /**
     * Exception type thrown when unable to assign to backlog as response is Urgent.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class StatusUrgent extends StaffAssignmentException {
        StatusUrgent(final String jurorId, final String assignTo) {
            super(String.format(
                "Could not assign response %s to %s as the response status is Urgent",
                jurorId,
                assignTo
            ));
        }

        @Override
        public String getLogMessage() {
            return "StaffAssignment exception caught during assignment - status is urgent";
        }

        @Override
        public String getReasonCode() {
            return "URGENT";
        }
    }

    /**
     * Exception type thrown when unable to assign to backlog as response is Super-Urgent.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class StatusSuperUrgent extends StaffAssignmentException {
        StatusSuperUrgent(final String jurorId, final String assignTo) {
            super(String.format(
                "Could not assign response %s to %s as the response status is Super-Urgent",
                jurorId,
                assignTo
            ));
        }

        @Override
        public String getLogMessage() {
            return "StaffAssignment exception caught during assignment - status is super-urgent";
        }

        @Override
        public String getReasonCode() {
            return "SUPER_URGENT";
        }
    }

    /**
     * Exception type thrown when unable to assign to backlog as response is Closed.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class StatusClosed extends StaffAssignmentException {
        StatusClosed(final String jurorId, final String assignTo) {
            super(String.format(
                "Could not assign response %s to %s as the response status is Closed",
                jurorId,
                assignTo
            ));
        }

        @Override
        public String getLogMessage() {
            return "StaffAssignment exception caught during assignment - response is closed";
        }

        @Override
        public String getReasonCode() {
            return "CLOSED";
        }
    }
}
