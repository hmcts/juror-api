package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ValidateIfTrigger validation annotation. Registers the class to check for @ValidateIf annotations
 *
 * @see ValidateIfValidator
 * @see ValidateIf
 *
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidateIfValidator.class)
public @interface ValidateIfTrigger {
    String message() default "{uk.gov.hmcts.juror.api.validation.ValidateIfTrigger.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<?> classToValidate();
}
