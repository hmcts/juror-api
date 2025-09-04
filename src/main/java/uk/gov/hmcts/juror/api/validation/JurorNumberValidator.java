package uk.gov.hmcts.juror.api.validation;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

@Slf4j
public class JurorNumberValidator {

    private JurorNumberValidator() {
        // Private constructor to prevent instantiation
    }

    public static void isValidJurorNumber(String jurorNumber) {
        if (jurorNumber == null || jurorNumber.isEmpty()
            || !jurorNumber.matches(ValidationConstants.JUROR_NUMBER)) {
            log.warn("Invalid juror number format: {}", jurorNumber);
            // throw a bad request exception if needed
            throw new MojException.BadRequest("Invalid juror number format: " + jurorNumber, null);
        }
    }
}
