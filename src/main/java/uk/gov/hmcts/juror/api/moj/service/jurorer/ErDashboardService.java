package uk.gov.hmcts.juror.api.moj.service.jurorer;

import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErDashboardStatsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErLocalAuthorityStatusRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErLocalAuthorityStatusResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.LocalAuthoritiesResponseDto;

public interface ErDashboardService {

    ErDashboardStatsResponseDto getErDashboardStats();

    ErLocalAuthorityStatusResponseDto getLocalAuthorityStatus(ErLocalAuthorityStatusRequestDto requestDto);

    LocalAuthoritiesResponseDto getLocalAuthorities(boolean activeOnly);
}
