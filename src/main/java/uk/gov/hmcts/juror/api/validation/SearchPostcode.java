package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for postcode input in the bureau juror response search.
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SearchPostcodeValidator.class)
public @interface SearchPostcode {

    String message() default "{uk.gov.hmcts.juror.api.validation.SearchPostcode.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
