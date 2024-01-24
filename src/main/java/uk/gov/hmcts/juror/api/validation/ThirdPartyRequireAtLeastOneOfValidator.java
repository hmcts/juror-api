package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.hmcts.juror.api.juror.controller.request.JurorResponseDto;


/**
 * Custom validator to check a that at least one of the provided fields is not null.
 */
public class ThirdPartyRequireAtLeastOneOfValidator implements ConstraintValidator<ThirdPartyRequireAtLeastOneOf,
    JurorResponseDto.ThirdParty> {
    @Override
    public boolean isValid(JurorResponseDto.ThirdParty value, ConstraintValidatorContext context) {
        return value == null || value.getMainPhone() != null || value.getEmailAddress() != null;
    }
}
