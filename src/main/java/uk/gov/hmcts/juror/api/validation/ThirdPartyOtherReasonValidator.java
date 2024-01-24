package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;


public class ThirdPartyOtherReasonValidator implements ConstraintValidator<ThirdPartyOtherReason, JurorResponse> {

    @Override
    public void initialize(ThirdPartyOtherReason constraintAnnotation) {
        //do nothing.

    }

    @Override
    public boolean isValid(JurorResponse jurorResponse, ConstraintValidatorContext context) {
        if (jurorResponse != null
            && jurorResponse.getThirdPartyReason() != null
            && !jurorResponse.getThirdPartyReason().trim().isEmpty()
            && "other".equals(jurorResponse.getThirdPartyReason().trim())) {
            return (jurorResponse.getThirdPartyOtherReason() != null)
                && !jurorResponse.getThirdPartyOtherReason().trim().isEmpty();
        }
        return true;
    }
}
