package uk.gov.hmcts.juror.api.moj.service.jurorer;

import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErDashboardStatsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErLocalAuthorityStatusRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErLocalAuthorityStatusResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.LocalAuthoritiesResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.LocalAuthorityInfoResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateDeadlineRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateDeadlineResponseDto;

public interface ErDashboardService {

    ErDashboardStatsResponseDto getErDashboardStats();

    ErLocalAuthorityStatusResponseDto getLocalAuthorityStatus(ErLocalAuthorityStatusRequestDto requestDto);

    LocalAuthoritiesResponseDto getLocalAuthorities(boolean activeOnly);

    LocalAuthorityInfoResponseDto getLocalAuthorityInfo(String laCode);

    UpdateDeadlineResponseDto updateDeadline(UpdateDeadlineRequestDto request);
}


