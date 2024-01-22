package uk.gov.hmcts.juror.api.moj.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception type thrown when accessing a Juror record fails.
 * {@link uk.gov.hmcts.juror.api.moj.service.JurorRecordServiceImpl}
 */
public class JurorRecordException extends RuntimeException {

    private JurorRecordException(String message) {
        super(message);
    }

    /**
     * Exception type thrown when a court user is trying to access a juror record from another court or Bureau.
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public static class CannotAccessJurorRecord extends JurorRecordException {

        public CannotAccessJurorRecord() {
            super("User does not have the access to perform this operation on this Juror record");
        }
    }

    /**
     * Exception type thrown when no juror record can be found for a given juror number.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class NoJurorRecordFound extends JurorRecordException {

        public NoJurorRecordFound(String jurorNumber) {
            super(String.format("Unable to find a valid Juror record for juror number: %s", jurorNumber));
        }

    }

    /**
     * Exception type thrown when a Juror number is invalid.
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidJurorNumber extends JurorRecordException {

        public InvalidJurorNumber(String jurorNumber) {
            super(String.format("Invalid Juror number found: %s", jurorNumber));
        }

    }

    /**
     * Exception type thrown when a single, active, juror record is expected for a single Juror Number
     * but multiple juror records have been returned - sometimes active multiple juror records can be valid or expected
     * for example when a juror is transferred to a different court they could have one active record per court they
     * have served at. However, there are times when only a single record is expected, for example before the juror has
     * attended court they are being managed by the Bureau, having their summons reply processed for a single location
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public static class MultipleJurorRecordsFound extends JurorRecordException {

        public MultipleJurorRecordsFound(String jurorNumber) {
            super(String.format("Unexpected number of Juror Records found for juror number: %s", jurorNumber));
        }

    }

}
