package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Validator for fields annotated with {@link NullOrNonBlank}.
 */
public class NullOrNonBlankValidator implements ConstraintValidator<NullOrNonBlank, String> {

    @Override
    public void initialize(NullOrNonBlank nullOrNonBlank) {
        //Do nothing
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        // Null check needed because 'isBlank' returns true for null
        return s == null || !isBlank(s);
    }
}
