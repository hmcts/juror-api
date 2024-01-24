package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Date of birth validation annotation.
 *
 * @see DateOfBirthValidator
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateOfBirthValidator.class)
public @interface DateOfBirth {
    String message() default "{uk.gov.hmcts.juror.api.validation.DateOfBirth.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
