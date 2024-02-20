package uk.gov.hmcts.juror.api.moj;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.AbstractCollectionAssert;
import org.assertj.core.api.ObjectAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SuppressWarnings({
    "PMD.TooManyMethods"
})
public abstract class AbstractValidatorTest<T> {

    protected ValidatorFactory validatorFactory;
    protected Validator validator;

    @BeforeEach
    public void createValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    protected abstract T createValidObject();

    @AfterEach
    public void close() {
        validatorFactory.close();
    }

    protected final String getMessage(FieldTestSupport fieldTestSupport, String defaultValue) {
        if (fieldTestSupport != null && fieldTestSupport.message != null) {
            return fieldTestSupport.message;
        }
        return defaultValue;
    }

    protected String getTestSuffix(FieldTestSupport fieldTestSupport) {
        String value = "";
        if (fieldTestSupport == null) {
            return value;
        }
        if (fieldTestSupport.getGroups() != null) {
            value += " for groups ["
                + Arrays.stream(fieldTestSupport.getGroups())
                .map(Class::getSimpleName).collect(
                    Collectors.joining(", ")) + "]";
        }
        return value;
    }

    private Class<?>[] getGroups(FieldTestSupport fieldTestSupport) {
        if (fieldTestSupport != null && fieldTestSupport.groups != null) {
            return fieldTestSupport.groups;
        }
        return null;
    }

    protected final Violation[] getOtherViolations(FieldTestSupport fieldTestSupport) {
        if (fieldTestSupport != null && fieldTestSupport.violations != null) {
            return fieldTestSupport.violations;
        }
        return new Violation[0];
    }

    protected void assertExpectNoViolations(T objectToValidate, Class<?>... groups) {
        Set<ConstraintViolation<T>> violations;
        if (groups == null) {
            violations = validator.validate(objectToValidate);
        } else {
            violations = validator.validate(objectToValidate, groups);
        }
        assertThat(violations).as("No validation violations expected").isEmpty();
    }

    protected void assertExpectViolations(T objectToValidate, Violation... expectedViolations) {
        assertExpectViolations(objectToValidate, null, expectedViolations);
    }

    protected void assertExpectViolations(T objectToValidate, FieldTestSupport fieldTestSupport,
                                          Violation... expectedViolations) {
        assertExpectViolations(objectToValidate, fieldTestSupport, List.of(expectedViolations));
    }


    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    protected void assertExpectViolations(T objectToValidate, FieldTestSupport fieldTestSupport,
                                          List<Violation> expectedViolations) {
        assertExpectViolations(objectToValidate, expectedViolations, getGroups(fieldTestSupport), false);
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    protected void assertExpectViolations(T objectToValidate, List<Violation> expectedViolations, Class<?>[] groups,
                                          boolean ignoredAdditionalErrors) {
        Set<ConstraintViolation<T>> violations;
        if (groups == null) {
            violations = validator.validate(objectToValidate);
        } else {
            violations = validator.validate(objectToValidate, groups);
        }

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
    @Accessors(chain = true)
    public static class FieldTestSupport {
        private String message;
        private Violation[] violations;
        private Class<?>[] groups;

        @SuppressWarnings("PMD.LinguisticNaming")
        public FieldTestSupport setGroups(Class<?>... groups) {
            this.groups = groups.clone();
            return this;
        }
    }

    @SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
    protected abstract class AbstractValidationFieldTestBase<V> {
        protected final List<DynamicTest> tests;
        protected final String fieldName;
        protected final BiConsumer<T, V> setFieldConsumer;

        protected boolean ignoreAdditionalFailures;

        protected AbstractValidationFieldTestBase(String fieldName,
                                                  BiConsumer<T, V> setFieldConsumer) {
            this.tests = new ArrayList<>();
            this.setFieldConsumer = setFieldConsumer;
            this.fieldName = fieldName;
        }

        protected void ignoreAdditionalFailures() {
            this.ignoreAdditionalFailures = true;
        }

        void expectViolations(T dto, String fieldName, String defaultMessage, FieldTestSupport fieldTestSupport) {
            List<Violation> violations = new ArrayList<>();
            violations.add(new Violation(fieldName, getMessage(fieldTestSupport, defaultMessage)));
            violations.addAll(Arrays.asList(getOtherViolations(fieldTestSupport)));
            AbstractValidatorTest.this.assertExpectViolations(dto, violations, getGroups(fieldTestSupport),
                ignoreAdditionalFailures);
        }

        protected T createValidObject() {
            return AbstractValidatorTest.this.createValidObject();
        }


        protected void setField(T baseObject, V value) {
            setFieldConsumer.accept(baseObject, value);
        }


        protected void addRequiredTest(FieldTestSupport fieldTestSupport) {
            tests.add(
                DynamicTest.dynamicTest(fieldName + " should reject null values" + getTestSuffix(fieldTestSupport),
                    () -> {
                        T dto = createValidObject();
                        setField(dto, null);
                        expectViolations(dto, fieldName, "must not be null", fieldTestSupport);
                    }));
        }

        protected void addNotRequiredTest(V validValue, FieldTestSupport fieldTestSupport) {
            tests.add(
                DynamicTest.dynamicTest(fieldName + " should be required" + getTestSuffix(fieldTestSupport), () -> {
                    T dto = createValidObject();
                    setField(dto, validValue);
                    assertExpectNoViolations(dto, getGroups(fieldTestSupport));
                }));
        }

        protected void addNotRequiredTest(V validValue) {
            addNotRequiredTest(validValue, null);
        }

        protected void addAllowNotNullTest(V validValue, FieldTestSupport fieldTestSupport) {
            tests.add(
                DynamicTest.dynamicTest(fieldName + " must allow none null values" + getTestSuffix(fieldTestSupport),
                    () -> {
                        T dto = createValidObject();
                        setField(dto, validValue);
                        assertExpectNoViolations(dto, getGroups(fieldTestSupport));
                    }));
        }

        protected void addAllowNullTest(FieldTestSupport fieldTestSupport) {
            tests.add(
                DynamicTest.dynamicTest(fieldName + " must allow null values" + getTestSuffix(fieldTestSupport),
                    () -> {
                        T dto = createValidObject();
                        setField(dto, null);
                        assertExpectNoViolations(dto, getGroups(fieldTestSupport));
                    }));
        }


        protected void addNullTest(V validValue, FieldTestSupport fieldTestSupport) {
            tests.add(
                DynamicTest.dynamicTest(fieldName + " must be null - Null Value" + getTestSuffix(fieldTestSupport),
                    () -> {
                        T dto = createValidObject();
                        setField(dto, null);
                        assertExpectNoViolations(dto, getGroups(fieldTestSupport));
                    }));
            tests.add(
                DynamicTest.dynamicTest(fieldName + " must be null - Non-Null Value" + getTestSuffix(fieldTestSupport),
                    () -> {
                        T dto = createValidObject();
                        setField(dto, validValue);
                        expectViolations(dto, fieldName, "must be null", fieldTestSupport);
                    }));
        }


        @TestFactory
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        Stream<DynamicTest> tests() {
            return tests.stream();
        }
    }

    protected abstract class AbstractValidationFieldTestLocalDate extends AbstractValidationFieldTestBase<LocalDate> {

        protected AbstractValidationFieldTestLocalDate(String fieldName, BiConsumer<T, LocalDate> setFieldConsumer) {
            super(fieldName, setFieldConsumer);
        }


        protected void addDateRangeTest(LocalDate minDate, LocalDate maxDate, FieldTestSupport fieldTestSupport) {
            addMinDateTest(minDate, fieldTestSupport);
            addMaxDateTest(maxDate, fieldTestSupport);
        }

        protected void addMinDateTest(LocalDate minDate, FieldTestSupport fieldTestSupport) {
            tests.add(DynamicTest.dynamicTest(
                fieldName + " should be after " + minDate + " - day before allowed" + getTestSuffix(fieldTestSupport),
                () -> {
                    T dto = createValidObject();
                    setField(dto, minDate.minusDays(1));
                    expectViolations(dto, fieldName, "tbc", fieldTestSupport);
                }));
            tests.add(DynamicTest.dynamicTest(
                fieldName + " should be after " + minDate + " - day of allowed" + getTestSuffix(fieldTestSupport),
                () -> {
                    T dto = createValidObject();
                    setField(dto, minDate);
                    expectViolations(dto, fieldName, "tbc", fieldTestSupport);
                }));
        }

        protected void addMaxDateTest(LocalDate maxDate, FieldTestSupport fieldTestSupport) {
            tests.add(DynamicTest.dynamicTest(
                fieldName + " should be after " + maxDate + " - day after allowed" + getTestSuffix(fieldTestSupport),
                () -> {
                    T dto = createValidObject();
                    setField(dto, maxDate.plusDays(1));
                    expectViolations(dto, fieldName, "tbc", fieldTestSupport);
                }));
            tests.add(DynamicTest.dynamicTest(
                fieldName + " should be after " + maxDate + " - day of allowed" + getTestSuffix(fieldTestSupport),
                () -> {
                    T dto = createValidObject();
                    setField(dto, maxDate);
                    expectViolations(dto, fieldName, "tbc", fieldTestSupport);
                }));

        }
    }

    protected abstract class AbstractValidationFieldTestLong
        extends AbstractValidationFieldTestNumeric<Long> {

        protected AbstractValidationFieldTestLong(String fieldName, BiConsumer<T, Long> setFieldConsumer) {
            super(fieldName, setFieldConsumer);
        }

        @Override
        protected Long toNumber(String value) {
            return Long.parseLong(value);
        }

        @Override
        protected Long add(Long minimum, int value) {
            return minimum + value;
        }
    }

    protected abstract class AbstractValidationFieldTestInteger
        extends AbstractValidationFieldTestNumeric<Integer> {

        protected AbstractValidationFieldTestInteger(String fieldName, BiConsumer<T, Integer> setFieldConsumer) {
            super(fieldName, setFieldConsumer);
        }

        @Override
        protected Integer toNumber(String value) {
            return Integer.parseInt(value);
        }

        @Override
        protected Integer add(Integer minimum, int value) {
            return minimum + value;
        }
    }

    protected abstract class AbstractValidationFieldTestBigDecimal
        extends AbstractValidationFieldTestNumeric<BigDecimal> {

        protected AbstractValidationFieldTestBigDecimal(String fieldName, BiConsumer<T, BigDecimal> setFieldConsumer) {
            super(fieldName, setFieldConsumer);
        }

        @Override
        protected BigDecimal toNumber(String value) {
            return new BigDecimal(value);
        }

        @Override
        protected BigDecimal add(BigDecimal minimum, int value) {
            return minimum.add(toNumber(String.valueOf(value)));
        }
    }

    protected abstract class AbstractValidationFieldTestNumeric<V extends Number>
        extends AbstractValidationFieldTestBase<V> {

        protected AbstractValidationFieldTestNumeric(String fieldName, BiConsumer<T, V> setFieldConsumer) {
            super(fieldName, setFieldConsumer);
        }

        protected abstract V toNumber(String value);

        protected void addMustBePositive(FieldTestSupport fieldTestSupport) {
            tests.add(
                DynamicTest.dynamicTest(fieldName + " be a positive number" + getTestSuffix(fieldTestSupport), () -> {
                    T dto = createValidObject();
                    setField(dto, toNumber("0"));
                    expectViolations(dto, fieldName, "must be greater than 0", fieldTestSupport);
                }));
        }

        protected void addMin(V minimum, FieldTestSupport fieldTestSupport) {
            tests.add(
                DynamicTest.dynamicTest(fieldName + " be a least " + minimum + getTestSuffix(fieldTestSupport), () -> {
                    T dto = createValidObject();
                    setField(dto, add(minimum, -1));
                    expectViolations(dto, fieldName, "must be greater than or equal to " + minimum, fieldTestSupport);
                }));
        }

        protected abstract V add(V minimum, int value);

    }

    protected abstract class AbstractValidationFieldTestString extends AbstractValidationFieldTestBase<String> {

        protected AbstractValidationFieldTestString(String fieldName, BiConsumer<T, String> setFieldConsumer) {
            super(fieldName, setFieldConsumer);
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

            tests.add(DynamicTest.dynamicTest(
                fieldName + " should not be longer then " + maxLength + getTestSuffix(fieldTestSupport), () -> {
                    T dto = createValidObject();
                    setField(dto, invalidMaxValue);
                    expectViolations(dto, fieldName, "length must be between " + minLength + " and " + maxLength,
                        fieldTestSupport);
                }));
            if (minLength > 0) {
                tests.add(DynamicTest.dynamicTest(
                    fieldName + " should not be shorter then " + minLength + getTestSuffix(fieldTestSupport), () -> {
                        T dto = createValidObject();
                        setField(dto, invalidMinValue);
                        expectViolations(dto, fieldName, "length must be between " + minLength + " and " + maxLength,
                            fieldTestSupport);
                    }));
            }
        }

        protected void addContainsPipesTest(FieldTestSupport fieldTestSupport) {
            tests.add(DynamicTest.dynamicTest(fieldName + " should not contain pipes" + getTestSuffix(fieldTestSupport),
                () -> {
                    T dto = createValidObject();
                    setField(dto, "ABC|DEF");
                    expectViolations(dto, fieldName, "must match \"^$|^[^|]+$\"", fieldTestSupport);
                }));
        }

        protected void addAllowBlankTest(String validValue) {
            tests.add(
                DynamicTest.dynamicTest(fieldName + " should allow blank values",
                    () -> {
                        T dto = createValidObject();
                        setField(dto, "");
                        assertExpectNoViolations(dto);
                    }));
            addNotRequiredTest(validValue);
        }

        protected void addNotBlankTest(FieldTestSupport fieldTestSupport) {
            tests.add(
                DynamicTest.dynamicTest(fieldName + " should reject blank values" + getTestSuffix(fieldTestSupport),
                    () -> {
                        T dto = createValidObject();
                        setField(dto, "");
                        expectViolations(dto, fieldName, "must not be blank", fieldTestSupport);
                    }));
            if (fieldTestSupport == null) {
                addRequiredTest(new FieldTestSupport().setMessage("must not be blank"));
            } else {
                fieldTestSupport.setMessage(getMessage(fieldTestSupport, "must not be blank"));
                addRequiredTest(fieldTestSupport);
            }
        }

        protected void addInvalidPatternTest(String invalidValue, String pattern,
                                             FieldTestSupport fieldTestSupport) {
            tests.add(
                DynamicTest.dynamicTest(fieldName + " must match pattern: " + pattern + getTestSuffix(fieldTestSupport),
                    () -> {
                        T dto = createValidObject();
                        setField(dto, invalidValue);
                        expectViolations(dto, fieldName, "must match \"" + pattern + "\"", fieldTestSupport);
                    }));
        }
    }

    protected abstract class AbstractValidationFieldTestList<V> extends AbstractValidationFieldTestBase<List<V>> {

        protected AbstractValidationFieldTestList(String fieldName, BiConsumer<T, List<V>> setFieldConsumer) {
            super(fieldName, setFieldConsumer);
        }

        protected void addNotEmptyTest(FieldTestSupport fieldTestSupport) {
            tests.add(DynamicTest.dynamicTest(fieldName + " must not be empty", () -> {
                T dto = createValidObject();
                setField(dto, Collections.emptyList());
                expectViolations(dto, fieldName, "must not be empty", fieldTestSupport);
            }));
        }

        protected void addNullValueInListTest(FieldTestSupport fieldTestSupport) {
            tests.add(DynamicTest.dynamicTest(fieldName + " must not contain a null value", () -> {
                T dto = createValidObject();
                ArrayList<V> list = new ArrayList<>();
                list.add(null);
                setField(dto, list);
                expectViolations(dto, fieldName + "[0].<list element>", "must not be null", fieldTestSupport);
            }));
        }
    }

    protected abstract class AbstractValidationFieldTestMap<K, V> extends AbstractValidationFieldTestBase<Map<K, V>> {

        protected AbstractValidationFieldTestMap(String fieldName, BiConsumer<T, Map<K, V>> setFieldConsumer) {
            super(fieldName, setFieldConsumer);
        }

        @SuppressWarnings("PMD.UseConcurrentHashMap")
        protected void addNullKeyValueInMapTest(FieldTestSupport fieldTestSupport) {
            tests.add(DynamicTest.dynamicTest(fieldName + " must not contain a null key ", () -> {
                T dto = createValidObject();
                Map<K, V> map = new HashMap<>();
                map.put(null, getValidValue());
                setField(dto, map);
                expectViolations(dto, fieldName + "<K>[].<map key>", "must not be null", fieldTestSupport);
            }));
        }

        @SuppressWarnings("PMD.UseConcurrentHashMap")
        protected void addNullValueInMapTest(FieldTestSupport fieldTestSupport) {
            tests.add(DynamicTest.dynamicTest(fieldName + " must not contain a null value ", () -> {
                T dto = createValidObject();
                Map<K, V> map = new HashMap<>();
                K key = getValidKey();
                map.put(key, null);
                setField(dto, map);
                expectViolations(dto, fieldName + "[" + key + "].<map value>",
                    "must not be null", fieldTestSupport);
            }));
        }

        protected Map<K, V> getValidMap() {
            return Map.of(getValidKey(), getValidValue());
        }

        protected abstract K getValidKey();

        protected abstract V getValidValue();
    }
}
