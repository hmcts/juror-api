package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.ConstraintValidatorContext;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD")
class ValidateIfValidatorTest extends AbstractValidatorTest<ValidateIfTrigger> {

    private ValidateIfValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ValidateIfValidator();
    }

    @Test
    void positiveConstructorTest() {
        assertNotNull(validator.fieldsToValidate,
            "fieldsToValidate should not be null");
        assertEquals(0, validator.fieldsToValidate.size(),
            "fieldsToValidate should be empty");
        assertNull(validator.validateIfTrigger,
            "validateIfTrigger should be null");
    }

    @Test
    void positiveInitializeTest() {
        MessageTestObject messageTestObject = new MessageTestObject();
        ValidateIfTrigger validateIfTrigger =
            createAnnotation(Map.of("classToValidate", messageTestObject.getClass()),
                ValidateIfTrigger.class);
        validator.initialize(validateIfTrigger);

        assertNotNull(validator.fieldsToValidate,
            "fieldsToValidate should not be null");
        assertEquals(1, validator.fieldsToValidate.size(),
            "fieldsToValidate should have 1 entry");

        ValidateIfValidator.FieldIf fieldIf = validator.fieldsToValidate.get(0);
        assertEquals("getField1", fieldIf.getFieldToValidate().getName(),
            "Field name should be correct");
        assertEquals(2, fieldIf.getFieldsToCheck().size(),
            "fieldsToCheck should have 2 entries");
        assertEquals("getField2", fieldIf.getFieldsToCheck().get(0).getName(),
            "Field name should be correct");
        assertEquals("getField3", fieldIf.getFieldsToCheck().get(1).getName(),
            "Field name should be correct");
        ValidateIf validateIf = fieldIf.getValidateIf();
        assertEquals(ValidateIf.Condition.ANY_PRESENT,
            validateIf.condition(),
            "Condition should be correct");
        assertEquals(ValidateIf.Type.REQUIRE,
            validateIf.type(),
            "Type should be correct");
        assertEquals("Some message that I have overridden",
            validateIf.message(),
            "Message should be correct");
        assertEquals(2,
            validateIf.fields().length,
            "Class to validate should be correct");
        assertEquals("field2",
            validateIf.fields()[0],
            "Class to validate should be correct");
        assertEquals("field3",
            validateIf.fields()[1],
            "Class to validate should be correct");
    }

    @Test
    void negativeFieldNotFound() {
        FieldNotFoundObject fieldNotFoundObject = new FieldNotFoundObject();
        ValidateIfTrigger validateIfTrigger =
            createAnnotation(Map.of("classToValidate", fieldNotFoundObject.getClass()),
                ValidateIfTrigger.class);
        IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> validator.initialize(validateIfTrigger),
                "Exception should be thrown");
        assertEquals("One or more fields not found in class "
                + "uk.gov.hmcts.juror.api.validation"
                + ".ValidateIfValidatorTest$FieldNotFoundObject "
                + "- fields: [unknownField, field3]",
            exception.getMessage(),
            "Exception message should be correct");
    }

    @Test
    void negativeMethodNotFound() {
        NotFoundMethodTestObject notFoundMethodTestObject = new NotFoundMethodTestObject();
        ValidateIfTrigger validateIfTrigger =
            createAnnotation(Map.of("classToValidate", notFoundMethodTestObject.getClass()),
                ValidateIfTrigger.class);

        IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, () -> validator.initialize(validateIfTrigger),
                "Exception should be thrown");
        assertEquals("Failed to find method: getField2",
            exception.getMessage(),
            "Exception message should be correct");
    }

    @DisplayName("Validation Checks")
    @Nested
    class ValidationChecks {

        private ConstraintValidatorContext context;
        private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext
            nodeBuilderCustomizableContext;
        private ConstraintValidatorContext.ConstraintViolationBuilder builder;

        @BeforeEach
        void setUp() {
            context = mock(ConstraintValidatorContext.class);
            nodeBuilderCustomizableContext = mock(ConstraintValidatorContext.ConstraintViolationBuilder
                .NodeBuilderCustomizableContext.class);
            builder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

            when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
            when(builder.addPropertyNode(anyString())).thenReturn(nodeBuilderCustomizableContext);
            when(nodeBuilderCustomizableContext.addConstraintViolation()).thenReturn(context);
        }

        void validateErrorMessage(String fieldName, String expectedMessage) {
            verify(context, times(1)).disableDefaultConstraintViolation();

            verify(context, times(1)).buildConstraintViolationWithTemplate(expectedMessage);
            verify(builder, times(1)).addPropertyNode(fieldName);
            verify(nodeBuilderCustomizableContext, times(1)).addConstraintViolation();

            verifyNoMoreInteractions(context, builder, nodeBuilderCustomizableContext);
        }

        void validateNoErrorMessage() {
            verifyNoInteractions(context, builder, nodeBuilderCustomizableContext);
        }

        @Test
        void negativeMessageOverride() {
            MessageTestObject messageTestObject = new MessageTestObject();
            ValidateIfTrigger validateIfTrigger =
                createAnnotation(Map.of("classToValidate", messageTestObject.getClass()),
                    ValidateIfTrigger.class);
            validator.initialize(validateIfTrigger);

            assertFalse(validator.isValid(messageTestObject, context),
                "Validation should fail");
            validateErrorMessage("field1",
                "Some message that I have overridden");
        }


        @Test
        void negativeClassDoesNotMatch() {
            FieldNotFoundObject fieldNotFoundObject = new FieldNotFoundObject();
            ValidateIfTrigger validateIfTrigger =
                createAnnotation(Map.of("classToValidate", String.class),
                    ValidateIfTrigger.class);

            validator.initialize(validateIfTrigger);

            IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> validator.isValid(fieldNotFoundObject, context),
                    "Exception should be thrown");
            assertEquals(
                "Invalid class to validate expected: class java.lang.String but got "
                    + "class uk.gov.hmcts.juror.api.validation.ValidateIfValidatorTest$FieldNotFoundObject",
                exception.getMessage(),
                "Exception message should be correct");
        }

        @Test
        void negativeMethodThrowsException() {
            MethodExceptionTestObject methodExceptionTestObject = new MethodExceptionTestObject();
            ValidateIfTrigger validateIfTrigger =
                createAnnotation(Map.of("classToValidate", methodExceptionTestObject.getClass()),
                    ValidateIfTrigger.class);

            validator.initialize(validateIfTrigger);

            MojException.InternalServerError exception =
                assertThrows(MojException.InternalServerError.class,
                    () -> validator.isValid(methodExceptionTestObject, context),
                    "Exception should be thrown");
            assertEquals("Unexpected exception when running ValidateIf for method: getField2",
                exception.getMessage(),
                "Exception message should be correct");
        }

        @Test
        void positiveNullValue() {
            MultipleValidationsTestObject multipleValidationsTestObject = new MultipleValidationsTestObject();
            ValidateIfTrigger validateIfTrigger =
                createAnnotation(Map.of("classToValidate", multipleValidationsTestObject.getClass()),
                    ValidateIfTrigger.class);
            validator.initialize(validateIfTrigger);

            assertTrue(validator.isValid(null, context),
                "Validation should pass");
            validateNoErrorMessage();
        }

        @Test
        void positiveMultipleValidateIf() {
            MultipleValidationsTestObject multipleValidationsTestObject = new MultipleValidationsTestObject();
            ValidateIfTrigger validateIfTrigger =
                createAnnotation(Map.of("classToValidate", multipleValidationsTestObject.getClass()),
                    ValidateIfTrigger.class);
            validator.initialize(validateIfTrigger);

            assertTrue(validator.isValid(multipleValidationsTestObject, context),
                "Validation should pass");
            validateNoErrorMessage();
        }

        @Test
        void negativeMultipleValidateIf() {
            MultipleValidationsTestObject multipleValidationsTestObject = new MultipleValidationsTestObject();
            multipleValidationsTestObject.setField4("abc");
            ValidateIfTrigger validateIfTrigger =
                createAnnotation(Map.of("classToValidate", multipleValidationsTestObject.getClass()),
                    ValidateIfTrigger.class);
            validator.initialize(validateIfTrigger);

            assertFalse(validator.isValid(multipleValidationsTestObject, context),
                "Validation should fail");
            validateErrorMessage("field1",
                "Field field1 should be excluded if any of the following fields are present: [field4]");
        }

        @Getter
        @Setter
        @SuppressWarnings("PMD.RedundantFieldInitializer")
        public static class MultipleValidationsTestObject {
            @ValidateIf(condition = ValidateIf.Condition.ANY_PRESENT,
                type = ValidateIf.Type.REQUIRE,
                fields = {"field2", "field3"},
                message = "Some message that I have overridden")
            @ValidateIf(condition = ValidateIf.Condition.ANY_PRESENT,
                type = ValidateIf.Type.EXCLUDE,
                fields = {"field4"})
            public String field1 = "abc";
            public String field2 = "abc";
            public String field3 = "abc";

            public String field4 = null;

        }


        @DisplayName("Required if")
        @Nested
        class RequiredIf {
            @Getter
            @Setter
            @SuppressWarnings("PMD.RedundantFieldInitializer")
            public static class RequireTestObject {
                @ValidateIf(condition = ValidateIf.Condition.ANY_PRESENT,
                    type = ValidateIf.Type.REQUIRE,
                    fields = {"field2", "field3"})
                public String field1 = "abc";
                public String field2 = "abc";
                public String field3 = "abc";


                @ValidateIf(condition = ValidateIf.Condition.NONE_PRESENT,
                    type = ValidateIf.Type.REQUIRE,
                    fields = {"field5", "field6"})
                public String field4 = "abc";
                public String field5 = null;
                public String field6 = null;
            }

            @Test
            void positiveTypeRequiredConditionAnyPresent() {
                RequireTestObject requireTestObject = new RequireTestObject();
                requireTestObject.setField2(null);
                requireTestObject.setField3("abc");
                ValidateIfTrigger validateIfTrigger =
                    createAnnotation(Map.of("classToValidate", requireTestObject.getClass()),
                        ValidateIfTrigger.class);
                validator.initialize(validateIfTrigger);

                assertTrue(validator.isValid(requireTestObject, context),
                    "Validation should pass");
                validateNoErrorMessage();
            }

            @Test
            void negativeTypeRequiredConditionAnyPresent() {
                RequireTestObject requireTestObject = new RequireTestObject();
                requireTestObject.setField1(null);
                ValidateIfTrigger validateIfTrigger =
                    createAnnotation(Map.of("classToValidate", requireTestObject.getClass()),
                        ValidateIfTrigger.class);
                validator.initialize(validateIfTrigger);

                assertFalse(validator.isValid(requireTestObject, context),
                    "Validation should fail");
                validateErrorMessage("field1",
                    "Field field1 is required if any of the following fields are present: [field2, field3]");
            }

            @Test
            void positiveTypeRequiredConditionNonePresent() {
                RequireTestObject requireTestObject = new RequireTestObject();
                ValidateIfTrigger validateIfTrigger =
                    createAnnotation(Map.of("classToValidate", requireTestObject.getClass()),
                        ValidateIfTrigger.class);
                validator.initialize(validateIfTrigger);

                assertTrue(validator.isValid(requireTestObject, context),
                    "Validation should pass");
                validateNoErrorMessage();
            }

            @Test
            void negativeTypeRequiredConditionNonePresent() {
                RequireTestObject requireTestObject = new RequireTestObject();
                requireTestObject.setField4(null);
                ValidateIfTrigger validateIfTrigger =
                    createAnnotation(Map.of("classToValidate", requireTestObject.getClass()),
                        ValidateIfTrigger.class);
                validator.initialize(validateIfTrigger);

                assertFalse(validator.isValid(requireTestObject, context),
                    "Validation should fail");
                validateErrorMessage("field4",
                    "Field field4 is required if none of the following fields are present: [field5, field6]");
            }
        }

        @DisplayName("Exclude if")
        @Nested
        class ExcludeIf {
            @Getter
            @Setter
            @SuppressWarnings("PMD.RedundantFieldInitializer")
            public static class ExcludeTestObject {
                @ValidateIf(condition = ValidateIf.Condition.ANY_PRESENT,
                    type = ValidateIf.Type.EXCLUDE,
                    fields = {"field2", "field3"})
                public String field1 = null;
                public String field2 = "abc";
                public String field3 = "abc";

                @ValidateIf(condition = ValidateIf.Condition.NONE_PRESENT,
                    type = ValidateIf.Type.EXCLUDE,
                    fields = {"field5", "field6"})
                public String field4 = null;
                public String field5 = null;
                public String field6 = null;
            }

            @Test
            void positiveTypeExcludeConditionAnyPresent() {
                ExcludeTestObject excludeTestObject = new ExcludeTestObject();
                excludeTestObject.setField1(null);
                excludeTestObject.setField2(null);
                excludeTestObject.setField3("abc");

                ValidateIfTrigger validateIfTrigger =
                    createAnnotation(Map.of("classToValidate", excludeTestObject.getClass()),
                        ValidateIfTrigger.class);
                validator.initialize(validateIfTrigger);

                assertTrue(validator.isValid(excludeTestObject, context),
                    "Validation should pass");
                validateNoErrorMessage();
            }

            @Test
            void negativeTypeExcludeConditionAnyPresent() {
                ExcludeTestObject excludeTestObject = new ExcludeTestObject();
                excludeTestObject.setField1("abc");
                excludeTestObject.setField2(null);
                excludeTestObject.setField3("abc");
                ValidateIfTrigger validateIfTrigger =
                    createAnnotation(Map.of("classToValidate", excludeTestObject.getClass()),
                        ValidateIfTrigger.class);
                validator.initialize(validateIfTrigger);

                assertFalse(validator.isValid(excludeTestObject, context),
                    "Validation should fail");
                validateErrorMessage("field1",
                    "Field field1 should be excluded if any of the following fields are present: [field2, field3]");
            }

            @Test
            void positiveTypeExcludeConditionNonePresent() {
                ExcludeTestObject excludeTestObject = new ExcludeTestObject();
                excludeTestObject.setField4("abc");
                excludeTestObject.setField5(null);
                excludeTestObject.setField6("abc");

                ValidateIfTrigger validateIfTrigger =
                    createAnnotation(Map.of("classToValidate", excludeTestObject.getClass()),
                        ValidateIfTrigger.class);
                validator.initialize(validateIfTrigger);

                assertTrue(validator.isValid(excludeTestObject, context),
                    "Validation should pass");
                validateNoErrorMessage();
            }

            @Test
            void negativeTypeExcludeConditionNonePresent() {
                ExcludeTestObject excludeTestObject = new ExcludeTestObject();
                excludeTestObject.setField4("abc");
                excludeTestObject.setField5(null);
                excludeTestObject.setField6(null);
                ValidateIfTrigger validateIfTrigger =
                    createAnnotation(Map.of("classToValidate", excludeTestObject.getClass()),
                        ValidateIfTrigger.class);
                validator.initialize(validateIfTrigger);

                assertFalse(validator.isValid(excludeTestObject, context),
                    "Validation should fail");
                validateErrorMessage("field4",
                    "Field field4 should be excluded if none of the following fields are present: [field5, field6]");
            }
        }
    }

    @Getter
    @Setter
    @SuppressWarnings("PMD.RedundantFieldInitializer")
    public static class FieldNotFoundObject {
        @ValidateIf(condition = ValidateIf.Condition.ANY_PRESENT,
            type = ValidateIf.Type.REQUIRE,
            fields = {"unknownField", "field3"})
        public String field1 = null;
        public String field2 = "abc";
        public String field3 = "abc";
    }

    @Getter
    @Setter
    @SuppressWarnings("PMD.RedundantFieldInitializer")
    public static class MessageTestObject {
        @ValidateIf(condition = ValidateIf.Condition.ANY_PRESENT,
            type = ValidateIf.Type.REQUIRE,
            fields = {"field2", "field3"},
            message = "Some message that I have overridden")
        public String field1 = null;
        public String field2 = "abc";
        public String field3 = "abc";
    }

    @Getter
    @Setter
    @SuppressWarnings("PMD.RedundantFieldInitializer")
    public static class PrivateTestObject {
        @ValidateIf(condition = ValidateIf.Condition.ANY_PRESENT,
            type = ValidateIf.Type.REQUIRE,
            fields = {"field2", "field3"},
            message = "Some message that I have overridden")
        private String field1 = null;
        private String field2 = "abc";
        private String field3 = "abc";
    }


    @Getter
    @SuppressWarnings("PMD.RedundantFieldInitializer")
    public static class MethodExceptionTestObject {
        @ValidateIf(condition = ValidateIf.Condition.ANY_PRESENT,
            type = ValidateIf.Type.REQUIRE,
            fields = {"field2", "field3"},
            message = "Some message that I have overridden")
        private String field1 = null;
        private String field2 = "abc";
        private String field3 = "abc";

        public String getField2() {
            throw new RuntimeException("Some exception");
        }
    }

    @SuppressWarnings("PMD.RedundantFieldInitializer")
    public static class NotFoundMethodTestObject {
        @ValidateIf(condition = ValidateIf.Condition.ANY_PRESENT,
            type = ValidateIf.Type.REQUIRE,
            fields = {"field2", "field3"},
            message = "Some message that I have overridden")
        @Getter
        private final String field1 = null;
        private final String field2 = "abc";
        @Getter
        private final String field3 = "abc";

    }
}
