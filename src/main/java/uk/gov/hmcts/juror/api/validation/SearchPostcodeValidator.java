package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * Validator for fields annotated with {@link SearchPostcode}.
 */
public class SearchPostcodeValidator implements ConstraintValidator<SearchPostcode, String> {

    @Override
    public void initialize(SearchPostcode nullOrNonBlank) {
        //Do nothing
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        // Null check needed because 'isBlank' returns true for null
        return s == null || !isBlank(s) && StringUtils.countMatches(s.trim(), ' ') <= 1;
    }
}
