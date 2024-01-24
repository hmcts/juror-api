package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;

/**
 * Custom validator to check a Date field is a valid date of birth.
 */
public class LocalDateOfBirthValidator implements ConstraintValidator<LocalDateOfBirth, LocalDate> {
    @Override
    public void initialize(LocalDateOfBirth dateOfBirth) {
        //do nothing.
    }

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext constraintValidatorContext) {
        // do not validate a null date, allow actual @NotNull to validate that state!
        return date == null || (
            date.isAfter(ChronoLocalDate.from(LocalDateTime.now().minusYears(125)))
                && date.isBefore(ChronoLocalDate.from(LocalDateTime.now().minusDays(1)))
        );
    }
}
