package uk.gov.hmcts.juror.api.validation;

import lombok.Getter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ValidateIf validation annotation.
 * For this to work you also need have the @ValidateIfTrigger annotation associated to the class
 *
 * @see ValidateIfValidator
 * @see ValidateIfTrigger
 *
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ValidateIfs.class)
public @interface ValidateIf {
    String[] fields();

    ValidateIf.Condition condition();

    ValidateIf.Type type();

    String message() default "";

    @Getter
    enum Condition {
        ANY_PRESENT("any of the following fields are present"),
        NONE_PRESENT("none of the following fields are present");
        private final String message;

        Condition(String message) {
            this.message = message;
        }
    }

    @Getter
    enum Type {
        REQUIRE("is required"),
        EXCLUDE("should be excluded");

        private final String message;

        Type(String message) {
            this.message = message;
        }
    }

}
