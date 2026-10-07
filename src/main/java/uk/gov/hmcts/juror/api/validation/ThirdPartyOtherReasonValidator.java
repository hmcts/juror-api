package uk.gov.hmcts.juror.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;

public class ThirdPartyOtherReasonValidator implements ConstraintValidator<ThirdPartyOtherReason, JurorResponse> {

    @Override
    public void initialize(ThirdPartyOtherReason constraintAnnotation) {
        //do nothing.

    }

    @Override
    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    public boolean isValid(JurorResponse jurorResponse, ConstraintValidatorContext context) {
        if (jurorResponse != null
            && StringUtils.isNotBlank(jurorResponse.getThirdPartyReason())
            && "other".equals(jurorResponse.getThirdPartyReason().trim())) {
            return StringUtils.isNotBlank(jurorResponse.getThirdPartyOtherReason());
        }
        return true;
    }
}
