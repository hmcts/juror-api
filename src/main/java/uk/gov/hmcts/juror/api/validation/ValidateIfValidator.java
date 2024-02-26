package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.logging.log4j.util.Strings;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;


public class ValidateIfValidator implements ConstraintValidator<ValidateIfTrigger, Object> {

    final List<FieldIf> fieldsToValidate;
    ValidateIfTrigger validateIfTrigger;

    public ValidateIfValidator() {
        fieldsToValidate = new ArrayList<>();
    }

    @Override
    public void initialize(ValidateIfTrigger constraintAnnotation) {
        this.validateIfTrigger = constraintAnnotation;
        Class<?> classToValidate = constraintAnnotation.classToValidate();

        for (Field field : classToValidate.getDeclaredFields()) {
            if (field.isAnnotationPresent(ValidateIf.class) || field.isAnnotationPresent(ValidateIfs.class)) {
                ValidateIf[] validateIfs = field.getAnnotationsByType(ValidateIf.class);
                processValidIfAnnotations(classToValidate, getMethodFromField(field), validateIfs);
            }
        }
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")//Done once at startup so no issue
    private void processValidIfAnnotations(Class<?> classToValidate, Method method, ValidateIf... validateIfs) {
        for (ValidateIf requiredIf : validateIfs) {
            this.fieldsToValidate.add(new FieldIf(
                method,
                getMethods(classToValidate, List.of(requiredIf.fields())),
                requiredIf
            ));
        }
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (!validateIfTrigger.classToValidate().isInstance(value)) {
            throw new IllegalArgumentException(
                "Invalid class to validate expected: " + validateIfTrigger.classToValidate() + " but got "
                    + value.getClass());
        }
        return fieldsToValidate.stream().allMatch(fieldIf -> fieldIf.validate(value, context));
    }


    @AllArgsConstructor
    @Data
    static class FieldIf {
        final Method fieldToValidate;
        final List<Method> fieldsToCheck;
        ValidateIf validateIf;

        public boolean validate(Object value, ConstraintValidatorContext context) {
            if (validateIf.condition() == ValidateIf.Condition.ANY_PRESENT) {
                return validate(value, context, this::validateAnyPresent);
            }
            if (validateIf.condition() == ValidateIf.Condition.NONE_PRESENT) {
                return validate(value, context, this::validateNonePresent);
            }
            throw new IllegalArgumentException("Unsupported condition: " + validateIf.condition());
        }

        public boolean validate(Object value, ConstraintValidatorContext context,
                                Predicate<Object> validator) {
            final Object fieldToValidateValue = getMethodValue(fieldToValidate, value);

            if (validateIf.type() == ValidateIf.Type.EXCLUDE && fieldToValidateValue == null) {
                return true;
            }

            if (validateIf.type() == ValidateIf.Type.REQUIRE && fieldToValidateValue != null) {
                return true;
            }

            if (!validator.test(value)) {
                return true;
            }
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(getMessage())
                .addPropertyNode(getFieldNameFromMethodName(fieldToValidate.getName()))
                .addConstraintViolation();

            return false;
        }

        private boolean validateNonePresent(Object value) {
            return fieldsToCheck.stream().allMatch(field -> getMethodValue(field, value) == null);
        }

        private boolean validateAnyPresent(Object value) {
            return fieldsToCheck.stream().anyMatch(field -> getMethodValue(field, value) != null);
        }

        private Object getMethodValue(Method method, Object value) {
            try {
                return method.invoke(value);
            } catch (Exception e) {
                throw new MojException.InternalServerError(
                    "Unexpected exception when running ValidateIf for method: " + method.getName(), e);
            }
        }

        private String getFieldNameFromMethodName(String methodName) {
            String fieldName = methodName.replaceFirst("get|is", "");
            return fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
        }

        private String getMessage() {
            if (Strings.isNotBlank(validateIf.message())) {
                return validateIf.message();
            }


            return "Field " + getFieldNameFromMethodName(fieldToValidate.getName())
                + " " + validateIf.type().getMessage()
                + " if " + validateIf.condition().getMessage() + ": "
                + Arrays.toString(validateIf.fields());
        }
    }

    private Method getMethodFromField(Field field) {
        String methodName = null;
        String prefix = field.getType().isPrimitive() && field.getType().equals(Boolean.class) ? "is" : "get";
        try {
            methodName = prefix + field.getName().substring(0, 1).toUpperCase()
                + field.getName().substring(1);

            return field.getDeclaringClass().getMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Failed to find method: " + methodName, e);
        }
    }


    public List<Method> getMethods(Class<?> classToCheck, List<String> fields) {
        List<Field> foundFields = getFields(classToCheck, fields);
        return foundFields.stream().map(this::getMethodFromField).toList();
    }

    public List<Field> getFields(Class<?> classToCheck, List<String> fields) {
        List<Field> foundFields = new ArrayList<>();

        for (Field field : classToCheck.getDeclaredFields()) {
            if (fields.contains(field.getName())) {
                foundFields.add(field);
            }
        }
        if (foundFields.size() != fields.size()) {
            throw new IllegalArgumentException("One or more fields not found in class " + classToCheck.getName()
                + " - fields: " + fields);
        }
        return foundFields;
    }
}

