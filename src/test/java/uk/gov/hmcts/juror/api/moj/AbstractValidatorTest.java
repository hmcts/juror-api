package uk.gov.hmcts.juror.api.moj;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.AbstractCollectionAssert;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
public class AbstractValidatorTest {

    protected ValidatorFactory validatorFactory;
    protected Validator validator;

    @BeforeEach
    public void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterEach
    public void close() {
        validatorFactory.close();
    }

    protected <T> void expectNoViolations(T objectToValidate) {
        Set<ConstraintViolation<T>> violations = validator.validate(objectToValidate);
        assertThat(violations).as("No validation violations expected").isEmpty();
    }


    protected <T> void expectViolations(T objectToValidate, Violation... expectedViolations) {
        expectViolations(objectToValidate, List.of(expectedViolations));
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    protected <T> void expectViolations(T objectToValidate, List<Violation> expectedViolations) {
        expectViolations(objectToValidate, expectedViolations, false);
    }

    protected <T> void expectViolations(T objectToValidate, List<Violation> expectedViolations,
                                        boolean ignoredAdditionalErrors) {
        Set<ConstraintViolation<T>> violations = validator.validate(objectToValidate);

        AbstractCollectionAssert<?, Collection<? extends ConstraintViolation<T>>, ConstraintViolation<T>,
            ObjectAssert<ConstraintViolation<T>>>
            validationSizeAssertion = assertThat(violations).as("Validation violation expected").isNotEmpty();
        if (ignoredAdditionalErrors) {
            validationSizeAssertion.hasSizeGreaterThanOrEqualTo(expectedViolations.size());
        } else {
            validationSizeAssertion.hasSize(expectedViolations.size());
        }

        for (Violation expectedViolation : expectedViolations) {
            ConstraintViolation<T> constraintViolation = violations.stream()
                .filter(violation -> violation.getMessage().equals(expectedViolation.message())
                    && violation.getPropertyPath().toString().equals(expectedViolation.propertyPath()))
                .findFirst()
                .orElseThrow(
                    () -> new AssertionError("Expected violation not found: '" + expectedViolation.propertyPath()
                        + "' with message: '" + expectedViolation.message() + "' but found " + violations));

            assertThat(constraintViolation.getPropertyPath().toString())
                .as("Expect the property path of the error to be '" + expectedViolation.propertyPath() + "'")
                .isEqualTo(expectedViolation.propertyPath());

            assertThat(constraintViolation.getMessage())
                .as("Expect the message of the error to be '" + expectedViolation.message() + "'")
                .isEqualTo(expectedViolation.message());
        }
    }

    public record Violation(String propertyPath, String message) {
    }


    @Setter
    @Getter
    public class FieldTestSupport {
        private String message;
        private Violation[] violations;

        public FieldTestSupport(String message, Violation... violations) {
            this.message = message;
            this.violations = violations;
        }
    }

    protected abstract class AbstractValidationFieldTestBase<O, T> {
        protected final List<DynamicTest> tests;
        protected final String fieldName;

        protected boolean ignoreAdditionalFailures = false;

        protected AbstractValidationFieldTestBase(String fieldName) {
            this.tests = new ArrayList<>();
            this.fieldName = fieldName;
        }

        protected void ignoreAdditionalFailures() {
            this.ignoreAdditionalFailures = true;
        }

        void expectViolations(O dto, String fieldName, String defaultMessage, FieldTestSupport fieldTestSupport) {
            List<Violation> violations = new ArrayList<>();
            violations.add(new Violation(fieldName, getMessage(fieldTestSupport, defaultMessage)));
            violations.addAll(Arrays.asList(getOtherViolations(fieldTestSupport)));
            AbstractValidatorTest.this.expectViolations(dto, violations, ignoreAdditionalFailures);
        }

        protected abstract void setField(O baseObject, T value);

        protected abstract O createValidObject();

        protected final String getMessage(FieldTestSupport fieldTestSupport, String defaultValue) {
            if (fieldTestSupport != null && fieldTestSupport.message != null) {
                return fieldTestSupport.message;
            }
            return defaultValue;
        }

        protected final Violation[] getOtherViolations(FieldTestSupport fieldTestSupport) {
            if (fieldTestSupport != null) {
                return fieldTestSupport.violations;
            }
            return new Violation[0];
        }

        protected void addRequiredTest(FieldTestSupport fieldTestSupport) {
            tests.add(DynamicTest.dynamicTest(fieldName + " should reject null values", () -> {
                O dto = createValidObject();
                setField(dto, null);
                expectViolations(dto, fieldName, "must not be null", fieldTestSupport);
            }));
        }

        protected void addNotRequiredTest(T validValue) {
            tests.add(DynamicTest.dynamicTest(fieldName + " should be required", () -> {
                O dto = createValidObject();
                setField(dto, validValue);
                expectNoViolations(dto);
            }));
        }

        @TestFactory
        Stream<DynamicTest> tests() {
            return tests.stream();
        }
    }

    protected abstract class AbstractValidationFieldTestLocalDate<O> extends AbstractValidationFieldTestBase<O,
        LocalDate> {

        protected AbstractValidationFieldTestLocalDate(String fieldName) {
            super(fieldName);
        }


        protected void addDateRangeTest(LocalDate minDate, LocalDate maxDate, FieldTestSupport fieldTestSupport) {
            addMinDateTest(minDate, fieldTestSupport);
            addMaxDateTest(maxDate, fieldTestSupport);
        }

        protected void addMinDateTest(LocalDate minDate, FieldTestSupport fieldTestSupport) {
            tests.add(DynamicTest.dynamicTest(fieldName + " should be after " + minDate + " - day before allowed",
                () -> {
                    O dto = createValidObject();
                    setField(dto, minDate.minusDays(1));
                    expectViolations(dto, fieldName, "tbc", fieldTestSupport);
                }));
            tests.add(DynamicTest.dynamicTest(fieldName + " should be after " + minDate + " - day of allowed", () -> {
                O dto = createValidObject();
                setField(dto, minDate);
                expectViolations(dto, fieldName, "tbc", fieldTestSupport);
            }));
        }

        protected void addMaxDateTest(LocalDate maxDate, FieldTestSupport fieldTestSupport) {
            tests.add(DynamicTest.dynamicTest(fieldName + " should be after " + maxDate + " - day after allowed",
                () -> {
                    O dto = createValidObject();
                    setField(dto, maxDate.plusDays(1));
                    expectViolations(dto, fieldName, "tbc", fieldTestSupport);
                }));
            tests.add(DynamicTest.dynamicTest(fieldName + " should be after " + maxDate + " - day of allowed", () -> {
                O dto = createValidObject();
                setField(dto, maxDate);
                expectViolations(dto, fieldName, "tbc", fieldTestSupport);
            }));

        }
    }

    protected abstract class AbstractValidationFieldTestNumeric<O, T extends Number>
        extends AbstractValidationFieldTestBase<O, T> {

        protected AbstractValidationFieldTestNumeric(String fieldName) {
            super(fieldName);
        }

        protected abstract T toNumber(String value);

        protected void addMustBePositive(FieldTestSupport fieldTestSupport) {
            tests.add(DynamicTest.dynamicTest(fieldName + " be a positive number", () -> {
                O dto = createValidObject();
                setField(dto, toNumber("0"));
                expectViolations(dto, fieldName, "must be greater than 0", fieldTestSupport);
            }));
        }

    }

    protected abstract class AbstractValidationFieldTestString<O> extends AbstractValidationFieldTestBase<O, String> {

        protected AbstractValidationFieldTestString(String fieldName) {
            super(fieldName);
        }


        protected void addMaxLengthTest(int maxLength, FieldTestSupport fieldTestSupport) {
            addMaxLengthTest(RandomStringUtils.randomAlphabetic(maxLength + 1), maxLength, fieldTestSupport);
        }

        protected void addMaxLengthTest(String invalidValue, int maxLength, FieldTestSupport fieldTestSupport) {
            addLengthTest("", invalidValue, 0, maxLength, fieldTestSupport);
        }

        protected void addLengthTest(int minLength, int maxLength,
                                     FieldTestSupport fieldTestSupport) {
            addLengthTest(
                RandomStringUtils.randomAlphabetic(minLength > 0 ? minLength - 1 : 0),
                RandomStringUtils.randomAlphabetic(maxLength + 1),
                minLength, maxLength, fieldTestSupport);
        }

        protected void addLengthTest(String invalidMinValue, String invalidMaxValue, int minLength, int maxLength,
                                     FieldTestSupport fieldTestSupport) {

            tests.add(DynamicTest.dynamicTest(fieldName + " should not be longer then " + maxLength, () -> {
                O dto = createValidObject();
                setField(dto, invalidMaxValue);
                expectViolations(dto, fieldName, "length must be between " + minLength + " and " + maxLength,
                    fieldTestSupport);
            }));
            if (minLength > 0) {
                tests.add(DynamicTest.dynamicTest(fieldName + " should not be shorter then " + minLength, () -> {
                    O dto = createValidObject();
                    setField(dto, invalidMinValue);
                    expectViolations(dto, fieldName, "length must be between " + minLength + " and " + maxLength,
                        fieldTestSupport);
                }));
            }
        }

        protected void addContainsPipesTest(FieldTestSupport fieldTestSupport) {
            tests.add(DynamicTest.dynamicTest(fieldName + " should not contain pipes", () -> {
                O dto = createValidObject();
                setField(dto, "ABC|DEF");
                expectViolations(dto, fieldName, "must match \"^$|^[^|]+$\"", fieldTestSupport);
            }));
        }

        protected void addAllowBlankTest(String validValue) {
            tests.add(DynamicTest.dynamicTest(fieldName + " should allow blank values", () -> {
                O dto = createValidObject();
                setField(dto, "");
                expectNoViolations(dto);
            }));
            addNotRequiredTest(validValue);
        }

        protected void addNotBlankTest(FieldTestSupport fieldTestSupport) {
            tests.add(DynamicTest.dynamicTest(fieldName + " should reject blank values", () -> {
                O dto = createValidObject();
                setField(dto, "");
                expectViolations(dto, fieldName, "must not be blank", fieldTestSupport);
            }));
            if (fieldTestSupport == null) {
                addRequiredTest(new FieldTestSupport("must not be blank"));
            } else {
                fieldTestSupport.setMessage(getMessage(fieldTestSupport, "must not be blank"));
                addRequiredTest(fieldTestSupport);
            }
        }

        protected void addInvalidPatternTest(String invalidValue, String pattern,
                                             FieldTestSupport fieldTestSupport) {
            tests.add(DynamicTest.dynamicTest(fieldName + " must match pattern: " + pattern, () -> {
                O dto = createValidObject();
                setField(dto, invalidValue);
                expectViolations(dto, fieldName, "must match \"" + pattern + "\"", fieldTestSupport);
            }));
        }
    }

    protected abstract class AbstractValidationFieldTestList<O, T> extends AbstractValidationFieldTestBase<O, List<T>> {

        protected AbstractValidationFieldTestList(String fieldName) {
            super(fieldName);
        }

        protected void addNotEmptyTest(FieldTestSupport fieldTestSupport) {
            tests.add(DynamicTest.dynamicTest(fieldName + " must not be empty", () -> {
                O dto = createValidObject();
                setField(dto, Collections.emptyList());
                expectViolations(dto, fieldName, "must not be empty", fieldTestSupport);
            }));
        }

        protected void addNullValueInListTest(FieldTestSupport fieldTestSupport) {
            tests.add(DynamicTest.dynamicTest(fieldName + " must not contain a null value", () -> {
                O dto = createValidObject();
                ArrayList<T> list = new ArrayList<>();
                list.add(null);
                setField(dto, list);
                expectViolations(dto, fieldName + "[0].<list element>", "must not be null", fieldTestSupport);
            }));
        }
    }
}
