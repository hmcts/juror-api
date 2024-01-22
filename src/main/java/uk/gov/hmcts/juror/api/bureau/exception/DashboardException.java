package uk.gov.hmcts.juror.api.bureau.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.juror.api.bureau.controller.request.DashboardRequestDto;

/**
 * Exception type thrown when there are problems retrieving/calculating data for the Dashboard.
 *
 * @see uk.gov.hmcts.juror.api.bureau.service.JurorDashboardService#getCumulativeTotals(DashboardRequestDto)
 */

public class DashboardException extends RuntimeException {

    private DashboardException(String message) {
        super(message);
    }

    /**
     * Exception type thrown when.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidDateRange extends DashboardException {
        public InvalidDateRange() {
            super("The date range requested is invalid.");
        }
    }

    /**
     * Exception type thrown when.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class NoResponsesFound extends DashboardException {
        public NoResponsesFound() {
            super("Responses not found. Cannot produce dashboard statistics.");
        }
    }
}
