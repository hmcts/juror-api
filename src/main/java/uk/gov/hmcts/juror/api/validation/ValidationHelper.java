package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ValidationHelper {
    /**
     * Validate a juror number path variable matches {@link ValidationConstants#JUROR_NUMBER}.
     *
     * @param jurorNumber Path variable supplied juror number
     */
    public static void validateJurorNumberPathVariable(final String jurorNumber) {
        if (!jurorNumber.matches(ValidationConstants.JUROR_NUMBER)) {
            log.warn("Juror number {} in path invalid", jurorNumber);
            throw new ValidationException("Juror number must be exactly 9 digits");
        }
        log.trace("Juror number valid");
    }

    private ValidationHelper() {

    }
}
