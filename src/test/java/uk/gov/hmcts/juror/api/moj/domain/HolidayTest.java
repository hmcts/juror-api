package uk.gov.hmcts.juror.api.moj.domain;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.juror.domain.Holidays;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
public class HolidayTest {
    private static ValidatorFactory validatorFactory;
    private static Validator validator;


    @BeforeClass
    public static void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterClass
    public static void close() {
        validatorFactory.close();
    }

    @Test
    public void testValidCourtHoliday() {
        Holidays holiday = new Holidays();
        holiday.setHoliday(LocalDate.now());
        holiday.setOwner("400");
        holiday.setDescription("TEST");
        holiday.setPublicHoliday(false);
        Set<ConstraintViolation<Holidays>> violations = validator.validate(holiday);

        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void testValidPublicHoliday() {
        Holidays holiday = new Holidays();
        holiday.setHoliday(LocalDate.now());
        holiday.setDescription("TEST");
        holiday.setPublicHoliday(true);
        Set<ConstraintViolation<Holidays>> violations = validator.validate(holiday);

        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    @Test
    public void testInvalidHolidayNoHolidayDate() {
        Holidays holiday = new Holidays();
        holiday.setDescription("TEST");
        holiday.setPublicHoliday(true);
        Set<ConstraintViolation<Holidays>> violations = validator.validate(holiday);

        assertThat(violations.size()).as("Expect one violation").isEqualTo(1);
        violations.forEach(holidaysConstraintViolation -> {
            assertThat(holidaysConstraintViolation.getPropertyPath().toString()).as("Expected holiday constraint to "
                + "fail").isEqualTo("holiday");
        });
    }

    @Test
    public void testInvalidHolidayNoDescription() {
        Holidays holiday = new Holidays();
        holiday.setHoliday(LocalDate.now());
        holiday.setPublicHoliday(true);
        Set<ConstraintViolation<Holidays>> violations = validator.validate(holiday);

        assertThat(violations.size()).as("Expect one violation").isEqualTo(1);
        violations.forEach(holidaysConstraintViolation -> {
            assertThat(holidaysConstraintViolation.getPropertyPath().toString()).as("Expected description constraint "
                + "to "
                + "fail").isEqualTo("description");
        });
    }

    @Test
    public void testInvalidHolidayNoPublicHolidayFlagSet() {
        Holidays holiday = new Holidays();
        holiday.setHoliday(LocalDate.now());
        holiday.setDescription("TEST");
        Set<ConstraintViolation<Holidays>> violations = validator.validate(holiday);

        assertThat(violations.size()).as("Expect one violation").isEqualTo(1);
        violations.forEach(holidaysConstraintViolation -> {
            assertThat(holidaysConstraintViolation.getPropertyPath().toString()).as("Expected public constraint to "
                + "fail").isEqualTo("publicHoliday");
        });
    }
}
