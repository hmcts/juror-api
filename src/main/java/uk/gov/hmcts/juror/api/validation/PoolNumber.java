package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates a pool number. (Meta-annotation)
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Pattern(regexp = ValidationConstants.POOL_NUMBER)
public @interface PoolNumber {
    String message() default "{uk.gov.hmcts.juror.api.validation.PoolNumber.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
