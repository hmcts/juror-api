package uk.gov.hmcts.juror.api.moj.service.letter;

import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.AdditionalInformationDto;

public interface RequestInformationLetterService {

    void requestInformation(BureauJWTPayload payload, AdditionalInformationDto additionalInformationDto);
}
