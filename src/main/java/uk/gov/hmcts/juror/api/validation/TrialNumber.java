package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates a juror number. (Meta-annotation)
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Pattern(regexp = ValidationConstants.TRIAL_NUMBER)
public @interface TrialNumber {
    String message() default "{uk.gov.hmcts.juror.api.validation.TrialNumber.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
