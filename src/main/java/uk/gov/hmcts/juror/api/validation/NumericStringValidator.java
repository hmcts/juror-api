package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import static org.apache.commons.lang3.math.NumberUtils.isDigits;

/**
 * Validates fields annotated with {@link NumericString}.
 */
public class NumericStringValidator implements ConstraintValidator<NumericString, String> {

    @Override
    public void initialize(NumericString numericString) {
        // do nothing
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        // Null handling is left to @Null / @NotNull
        return s == null || isDigits(s);
    }
}
