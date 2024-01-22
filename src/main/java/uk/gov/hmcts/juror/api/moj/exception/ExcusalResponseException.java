package uk.gov.hmcts.juror.api.moj.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception type thrown when responding to an excusal request fails.
 * {@link uk.gov.hmcts.juror.api.moj.service.ExcusalResponseServiceImpl}
 */
public class ExcusalResponseException extends RuntimeException {

    public ExcusalResponseException(String message) {
        super(message);
    }


    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class UnableToRetrieveExcusalCodeList extends ExcusalResponseException {

        public UnableToRetrieveExcusalCodeList() {
            super("Unable to retrieve list of excusal codes from database");
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidExcusalCode extends ExcusalResponseException {

        public InvalidExcusalCode(String excusalCode) {
            super(String.format("Provided excusal code %s is not valid", excusalCode));
        }
    }
}
