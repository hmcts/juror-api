package uk.gov.hmcts.juror.api.validation;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;


public class NameValidator implements ConstraintValidator<Name, String> {
    @Override
    public void initialize(Name constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        Pattern noPipesPattern = Pattern.compile(ValidationConstants.NO_PIPES_REGEX);
        return !value.isBlank() && value.length() <= 20 && !noPipesPattern.matcher(value).find();
    }
}
