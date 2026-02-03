package uk.gov.hmcts.juror.api.moj.service.jurorer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.jurorer.domain.LocalAuthority;
import uk.gov.hmcts.juror.api.jurorer.service.LocalAuthorityService;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErDashboardStatsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErLocalAuthorityStatusRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErLocalAuthorityStatusResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.LocalAuthoritiesResponseDto;

import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ErDashboardServiceImpl implements ErDashboardService {

    private final LocalAuthorityService localAuthorityService;

    @Override
    public ErDashboardStatsResponseDto getErDashboardStats() {
        // Dummy implementation for illustration
        return ErDashboardStatsResponseDto.builder().build();
    }

    @Override
    public ErLocalAuthorityStatusResponseDto getLocalAuthorityStatus(ErLocalAuthorityStatusRequestDto requestDto) {
        // Dummy implementation for illustration
        return ErLocalAuthorityStatusResponseDto.builder().build();
    }

    @Override
    public LocalAuthoritiesResponseDto getLocalAuthorities(boolean isActive) {

        List<LocalAuthority> localAuthorities = localAuthorityService.getAllLocalAuthorities(isActive);

        // we need to build list of LocalAuthorityDto from LocalAuthority
        List<LocalAuthoritiesResponseDto.LocalAuthorityData> localAuthorityDtos = localAuthorities.stream()
            .map(la -> LocalAuthoritiesResponseDto.LocalAuthorityData.builder()
                .localAuthorityCode(la.getLaCode())
                .localAuthorityName(la.getLaName())
                .build())
            .toList();

        return LocalAuthoritiesResponseDto.builder()
            .localAuthorities(localAuthorityDtos)
            .build();

    }

}
