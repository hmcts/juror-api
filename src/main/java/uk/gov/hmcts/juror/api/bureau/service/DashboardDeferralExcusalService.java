package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.controller.request.DashboardDeferralExcusalRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.DashboardDeferralExcusalResponseDto;


public interface DashboardDeferralExcusalService {

    /**
     * Get the Deferral Excusal Values.
     *
     */

    DashboardDeferralExcusalResponseDto.DeferralExcusalValues getDeferralExcusalValues(
        DashboardDeferralExcusalRequestDto requestDto);

}
