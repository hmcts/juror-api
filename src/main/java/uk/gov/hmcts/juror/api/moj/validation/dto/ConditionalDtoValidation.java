package uk.gov.hmcts.juror.api.moj.validation.dto;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Repeatable(ConditionalDtoValidations.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {ConditionalDtoValidator.class})
public @interface ConditionalDtoValidation {

    String message() default "Field is required.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    String conditionalProperty();

    String[] values();

    String[] requiredProperties();
}

