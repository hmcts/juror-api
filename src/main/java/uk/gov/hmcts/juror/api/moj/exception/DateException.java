package uk.gov.hmcts.juror.api.moj.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception type thrown when managing dates.
 */
public class DateException extends RuntimeException {

    private DateException(String message) {
        super(message);
    }

    /**
     * Exception type thrown when trying to parse a date from a string.
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class DateParseException extends DateException {

        public DateParseException(String dateString, String dateFormat) {
            super(String.format(
                "Unable to parse the String value of %s into a valid date in the format: %s",
                dateString,
                dateFormat
            ));
        }
    }

}
