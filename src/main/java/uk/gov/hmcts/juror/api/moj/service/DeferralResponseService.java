package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralRequestDto;

public interface DeferralResponseService {

    void respondToDeferralRequest(BureauJWTPayload payload, DeferralRequestDto deferralRequestDto);
}
