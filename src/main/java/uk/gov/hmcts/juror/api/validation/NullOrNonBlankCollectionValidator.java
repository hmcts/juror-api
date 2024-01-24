package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

/**
 * Validator for collection fields annotated with {@link NullOrNonBlank}.
 */
public class NullOrNonBlankCollectionValidator implements ConstraintValidator<NullOrNonBlank, Collection<String>> {

    @Override
    public void initialize(NullOrNonBlank nullOrNonBlank) {
        // Do nothing
    }

    @Override
    public boolean isValid(Collection<String> strings, ConstraintValidatorContext constraintValidatorContext) {
        return strings == null || strings.isEmpty() || strings.parallelStream().noneMatch(StringUtils::isBlank);
    }
}
