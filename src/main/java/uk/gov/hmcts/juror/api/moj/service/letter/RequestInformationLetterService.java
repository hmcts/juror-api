package uk.gov.hmcts.juror.api.moj.service.letter;

import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.AdditionalInformationDto;

public interface RequestInformationLetterService {

    void requestInformation(BureauJwtPayload payload, AdditionalInformationDto additionalInformationDto);
}
