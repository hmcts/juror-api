package uk.gov.hmcts.juror.api.validation;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

@Slf4j
public class LaCodeValidator {

    private LaCodeValidator() {
        // Private constructor to prevent instantiation
    }

    public static void isValidLaCode(String laCode) {
        if (laCode == null || laCode.isEmpty()
            || !laCode.matches(ValidationConstants.LA_CODE)) {
            log.warn("Invalid LA code format: {}", laCode);
            // throw a bad request exception if needed
            throw new MojException.BadRequest("Invalid LA code format: " + laCode, null);
        }
    }
}
