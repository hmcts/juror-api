package uk.gov.hmcts.juror.api.moj.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;

public class DigitalThirdPartyOtherReasonValidator implements ConstraintValidator<DigitalThirdPartyOtherReason,
    DigitalResponse> {

    @Override
    public void initialize(DigitalThirdPartyOtherReason constraintAnnotation) {
        //do nothing.

    }

    @Override
    public boolean isValid(DigitalResponse jurorResponse, ConstraintValidatorContext context) {
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