package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for string fields used to represent numbers.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NumericStringValidator.class)
public @interface NumericString {
    String message() default "{uk.gov.hmcts.juror.api.validation.NumericString.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
