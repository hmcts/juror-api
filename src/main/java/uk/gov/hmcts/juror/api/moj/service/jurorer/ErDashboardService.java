package uk.gov.hmcts.juror.api.moj.service.jurorer;

import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErDashboardStatsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErLocalAuthorityStatusRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErLocalAuthorityStatusResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.LocalAuthoritiesResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.LocalAuthorityInfoResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateLocalAuthorityNotesRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateLocalAuthorityNotesResponseDto;

public interface ErDashboardService {

    ErDashboardStatsResponseDto getErDashboardStats();

    ErLocalAuthorityStatusResponseDto getLocalAuthorityStatus(ErLocalAuthorityStatusRequestDto requestDto);

    LocalAuthoritiesResponseDto getLocalAuthorities(boolean activeOnly);

    LocalAuthorityInfoResponseDto getLocalAuthorityInfo(String laCode);

    UpdateLocalAuthorityNotesResponseDto updateLocalAuthorityNotes(UpdateLocalAuthorityNotesRequestDto request);


}


