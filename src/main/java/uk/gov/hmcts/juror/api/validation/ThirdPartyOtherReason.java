package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.TYPE}) //TYPE Is Class Level Annotation
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ThirdPartyOtherReasonValidator.class)
public @interface ThirdPartyOtherReason {
    String message() default "{uk.gov.hmcts.juror.api.validation.ThirdPartyOtherReason.message}";

    //Required by validation Runtime
    Class<?>[] groups() default {};

    //Required by validation Runtime
    Class<? extends Payload>[] payload() default {};
}
