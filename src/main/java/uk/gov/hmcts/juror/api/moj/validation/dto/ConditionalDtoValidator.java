package uk.gov.hmcts.juror.api.moj.validation.dto;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static org.springframework.util.ObjectUtils.isEmpty;

public class ConditionalDtoValidator implements ConstraintValidator<ConditionalDtoValidation, Object> {

    private String conditionalProperty;
    private String[] requiredProperties;
    private String message;
    private String[] values;

    @Override
    public void initialize(ConditionalDtoValidation constraint) {
        conditionalProperty = constraint.conditionalProperty();
        requiredProperties = constraint.requiredProperties();
        message = constraint.message();
        values = constraint.values();
    }

    @Override
    public boolean isValid(Object dtoObject, ConstraintValidatorContext context) {
        BeanWrapper dtoWrapper = PropertyAccessorFactory.forBeanPropertyAccess(dtoObject);

        try {
            Object conditionalPropertyValue = dtoWrapper.getPropertyValue(conditionalProperty);

            if (conditionalPropertyValue == null) {
                buildConstraintValidatorContext(context, conditionalProperty);
                return false;
            }

            if (doConditionalValidation(conditionalPropertyValue)) {
                return validateRequiredProperties(dtoObject, context);

            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException ex) {
            return false;
        }
        return true;
    }

    private boolean validateRequiredProperties(Object dtoObject, ConstraintValidatorContext context)
        throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        boolean isValid = true;
        BeanWrapper dtoWrapper = PropertyAccessorFactory.forBeanPropertyAccess(dtoObject);

        for (String property : requiredProperties) {
            Object requiredValue = dtoWrapper.getPropertyValue(property);
            boolean isPresent = requiredValue != null && !isEmpty(requiredValue);

            // ints and longs are never null/blank therefore check for default values
            if (requiredValue instanceof Long  || requiredValue instanceof Integer) {
                isPresent = !"-1".equals(requiredValue.toString());
            }

            if (!isPresent) {
                isValid = false;
                context.disableDefaultConstraintViolation();
                buildConstraintValidatorContext(context, property);
            }
        }
        return isValid;
    }

    private void buildConstraintValidatorContext(ConstraintValidatorContext context, String property) {
        context
            .buildConstraintViolationWithTemplate(message)
            .addPropertyNode(property)
            .addConstraintViolation();
    }

    private boolean doConditionalValidation(Object actualValue) {
        return Arrays.asList(values).contains(actualValue.toString());
    }
}
