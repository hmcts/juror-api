package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates a court location code. (Meta-annotation)
 */
@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Pattern(regexp = ValidationConstants.LOCATION_CODE)
public @interface CourtLocationCode {
    String message() default "{uk.gov.hmcts.juror.api.validation.CourtLocationCode.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
