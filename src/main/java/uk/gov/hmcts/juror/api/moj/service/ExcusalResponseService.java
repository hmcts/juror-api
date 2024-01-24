package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.ExcusalDecisionDto;

public interface ExcusalResponseService {

    void respondToExcusalRequest(BureauJWTPayload payload, ExcusalDecisionDto excusalDecisionDto, String jurorNumber);


}
