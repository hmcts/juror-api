package uk.gov.hmcts.juror.api.moj.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception type thrown when creating a paper response fails.
 * {@link uk.gov.hmcts.juror.api.moj.service.JurorPaperResponseServiceImpl}
 */
public class JurorPaperResponseException extends RuntimeException {

    private JurorPaperResponseException(String message) {
        super(message);
    }


    /**
     * Exception type thrown when the paper response already exists for the juror.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class JurorPaperResponseAlreadyExists extends JurorPaperResponseException {

        public JurorPaperResponseAlreadyExists(String jurorNumber) {
            super(String.format("The Juror Paper Response already exists for Juror %s", jurorNumber));
        }
    }

    /**
     * Exception type thrown when an invalid value supplied for CJS employment.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidCjsEmploymentEntry extends JurorPaperResponseException {

        public InvalidCjsEmploymentEntry() {
            super("There was an error adding CJS Employment to paper response");
        }

    }

    /**
     * Exception type thrown when invalid value supplied for special need.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidSpecialNeedEntry extends JurorPaperResponseException {

        public InvalidSpecialNeedEntry() {
            super("There was an error adding Special Need to paper response");
        }
    }

    /**
     * Exception type thrown when null value supplied for eligibility object.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidEligibilityEntry extends JurorPaperResponseException {

        public InvalidEligibilityEntry() {
            super("There was an error adding eligibility to paper response");
        }
    }

    /**
     * Exception type thrown when invalid values supplied for reply type.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidReplyTypeEntry extends JurorPaperResponseException {

        public InvalidReplyTypeEntry() {
            super("There was an error adding reply type to paper response");
        }
    }

    /**
     * Exception type thrown when the paper response does not exist for the juror.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class JurorPaperResponseDoesNotExist extends JurorPaperResponseException {

        public JurorPaperResponseDoesNotExist(String jurorNumber) {
            super(String.format("The Juror Paper response does not exist for juror %s", jurorNumber));
        }
    }

    /**
     * Exception type thrown when unable to find associated Juror record.
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class UnableToFindJurorRecord extends JurorPaperResponseException {

        public UnableToFindJurorRecord() {
            super("There was an error finding associated Juror record");
        }
    }

    /**
     * Exception type thrown when no juror paper response record can be found for a given juror number.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class NoJurorPaperResponseRecordFound extends JurorPaperResponseException {

        public NoJurorPaperResponseRecordFound(String jurorNumber) {
            super(String.format(
                "Unable to find a valid Juror Paper response record for juror number: %s",
                jurorNumber
            ));
        }
    }

    /**
     * Exception type thrown when a juror paper response record is missing mandatory information preventing processing.
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class JurorPaperResponseMissingMandatoryFields extends JurorPaperResponseException {

        public JurorPaperResponseMissingMandatoryFields() {
            super("Summons reply is missing essential information");
        }
    }
}
