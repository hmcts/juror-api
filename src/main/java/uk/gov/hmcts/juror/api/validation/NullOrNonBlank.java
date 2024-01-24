package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for string fields which should either be null or non-blank (i.e. not empty / whitespace-only)
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {NullOrNonBlankValidator.class, NullOrNonBlankCollectionValidator.class})
public @interface NullOrNonBlank {
    String message() default "{uk.gov.hmcts.juror.api.validation.NullOrNonBlank.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
