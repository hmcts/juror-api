package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

/**
 * Custom validator to check a Date field is a valid date of birth.
 *
 * @implNote Valid DOB is from 125 years ago until yesterday.
 */
public class DateOfBirthValidator implements ConstraintValidator<DateOfBirth, Date> {
    @Override
    public void initialize(DateOfBirth dateOfBirth) {
        //do nothing.
    }

    @Override
    public boolean isValid(Date date, ConstraintValidatorContext constraintValidatorContext) {
        // do not validate a null date, allow actual @NotNull to validate that state!
        return date == null || (
            date.toInstant().isAfter(LocalDateTime.now().minusYears(125).toInstant(ZoneOffset.UTC))
                && date.toInstant().isBefore(LocalDateTime.now().minusDays(1).toInstant(ZoneOffset.UTC))
        );
    }
}
