package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class EnumValidatorImpl implements ConstraintValidator<EnumValidator, Enum<?>> {

    List<String> values;
    EnumValidator.Mode mode;

    @Override
    public boolean isValid(Enum<?> value, ConstraintValidatorContext context) {
        return values.contains(value.name()) ^ EnumValidator.Mode.EXCLUDE == mode;
    }

    @Override
    public void initialize(EnumValidator constraintAnnotation) {
        values = List.of(constraintAnnotation.values());
        mode = constraintAnnotation.mode();
    }
}
