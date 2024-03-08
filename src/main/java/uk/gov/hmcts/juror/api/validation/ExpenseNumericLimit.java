package uk.gov.hmcts.juror.api.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Min(0)
@Max(1000000)
public @interface ExpenseNumericLimit {

    String message() default "{uk.gov.hmcts.juror.api.validation.ExpenseBigDecimalLimit.message}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
