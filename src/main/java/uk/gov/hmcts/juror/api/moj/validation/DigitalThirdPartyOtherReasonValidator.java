package uk.gov.hmcts.juror.api.moj.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;

public class DigitalThirdPartyOtherReasonValidator implements ConstraintValidator<DigitalThirdPartyOtherReason,
    DigitalResponse> {

    @Override
    public void initialize(DigitalThirdPartyOtherReason constraintAnnotation) {
        //do nothing.

    }

    @Override
    @SuppressWarnings("PMD.SimplifyBooleanReturns")
    public boolean isValid(DigitalResponse jurorResponse, ConstraintValidatorContext context) {
        if (jurorResponse != null
            && StringUtils.isNotBlank(jurorResponse.getThirdPartyReason())
            && "other".equals(jurorResponse.getThirdPartyReason().trim())) {
            return StringUtils.isNotBlank(jurorResponse.getThirdPartyOtherReason());
        }
        return true;
    }
}
