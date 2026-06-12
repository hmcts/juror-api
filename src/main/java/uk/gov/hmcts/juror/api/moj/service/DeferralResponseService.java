package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.DeferralRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.deferralmaintenance.DeferralAgeDisqualificationResponseDto;

@FunctionalInterface
public interface DeferralResponseService {

    DeferralAgeDisqualificationResponseDto respondToDeferralRequest(BureauJwtPayload payload,
                                                                    DeferralRequestDto deferralRequestDto);
}
