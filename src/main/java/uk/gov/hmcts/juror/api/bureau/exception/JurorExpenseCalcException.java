package uk.gov.hmcts.juror.api.bureau.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import uk.gov.hmcts.juror.api.bureau.controller.request.JurorExpensesCalcRequestDto;

/**
 * Exception type thrown when Payment Expenses Calculator calculation fails.
 *
 * @see uk.gov.hmcts.juror.api.bureau.service.JurorExpenseCalcService#getExpensesCalcResults(JurorExpensesCalcRequestDto)
 */

public class JurorExpenseCalcException extends RuntimeException {

    private JurorExpenseCalcException(String message) {
        super(message);
    }

    /**
     * Exception type thrown when.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class MissingParamsInRequest extends JurorExpenseCalcException {
        public MissingParamsInRequest() {
            super("One or more required parameters missing in Request.");
        }
    }

    /**
     * Exception type thrown when Rate Information Configuration is missing.
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class FailedToRetrieveRateData extends JurorExpenseCalcException {
        public FailedToRetrieveRateData() {
            super("Calculation cannot be performed. Missing Rates Data.");
        }
    }

}
