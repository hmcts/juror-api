package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RequireAtLeastOneOf validation annotation.
 *
 * @see ThirdPartyRequireAtLeastOneOfValidator
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ThirdPartyRequireAtLeastOneOfValidator.class)
public @interface ThirdPartyRequireAtLeastOneOf {
    String message();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
