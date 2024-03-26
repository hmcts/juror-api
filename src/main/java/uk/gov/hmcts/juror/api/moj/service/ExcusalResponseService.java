package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.ExcusalDecisionDto;

public interface ExcusalResponseService {

    void respondToExcusalRequest(BureauJwtPayload payload, ExcusalDecisionDto excusalDecisionDto, String jurorNumber);


}
