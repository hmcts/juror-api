package uk.gov.hmcts.juror.api.moj.service.jurorer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.jurorer.domain.Deadline;
import uk.gov.hmcts.juror.api.jurorer.domain.LocalAuthority;
import uk.gov.hmcts.juror.api.jurorer.domain.UploadStatus;
import uk.gov.hmcts.juror.api.jurorer.repository.DeadlineRepository;
import uk.gov.hmcts.juror.api.jurorer.service.FileUploadsService;
import uk.gov.hmcts.juror.api.jurorer.service.LocalAuthorityService;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErDashboardStatsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErLocalAuthorityStatusRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErLocalAuthorityStatusResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.LocalAuthoritiesResponseDto;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ErDashboardServiceImpl implements ErDashboardService {

    private final LocalAuthorityService localAuthorityService;
    private final DeadlineRepository deadlineRepository;
    private final FileUploadsService fileUploadsService;

    @Override
    public ErDashboardStatsResponseDto getErDashboardStats() {

        log.info("Getting ER dashboard stats");

        Optional<Deadline> deadlineOpt = deadlineRepository.getCurrentDeadline();

        if (deadlineOpt.isEmpty()) {
            throw new MojException.InternalServerError("Upload deadline not found - it should always exist", null);
        }

        Deadline deadline = deadlineOpt.get();

        List<LocalAuthority> localAuthorities = localAuthorityService.getAllLocalAuthorities(true);

        int totalLocalAuthorities = localAuthorities.size();

        // find the number of local authorities who have uploaded
        long uploadedCount = localAuthorities.stream()
            .filter(la -> la.getUploadStatus().equals(UploadStatus.UPLOADED))
            .count();

        long notUploadedCount = localAuthorities.stream()
            .filter(la -> la.getUploadStatus().equals(UploadStatus.NOT_UPLOADED))
            .count();

        return ErDashboardStatsResponseDto.builder()
            .deadlineDate(deadline.getDeadlineDate())
            .daysRemaining(deadline.getDaysRemaining())
            .notUploadedCount(notUploadedCount)
            .uploadedCount(uploadedCount)
            .totalNumberOfLocalAuthorities(totalLocalAuthorities)
            .build();
    }

    @Override
    public ErLocalAuthorityStatusResponseDto getLocalAuthorityStatus(ErLocalAuthorityStatusRequestDto requestDto) {

        log.info("Getting local authority status for ER dashboard, activeOnly");

        List<FileUploadsService.FileUploadStatus> fileUploadsList = fileUploadsService.getLatestUploadForEachLa();

        List<LocalAuthority> localAuthorities = localAuthorityService.getAllLocalAuthorities(true);

        //need to filter out the local authorities based on la code if provided in the request
        if (requestDto.getLocalAuthorityCode() != null && !requestDto.getLocalAuthorityCode().isEmpty()) {
            localAuthorities = localAuthorities.stream()
                .filter(la -> requestDto.getLocalAuthorityCode().contains(la.getLaCode()))
                .toList();
        }

        // need to filter out the local authorities based on upload status if provided in the request
        if (requestDto.getUploadStatus() != null && !requestDto.getUploadStatus().isEmpty()) {
            localAuthorities = localAuthorities.stream()
                .filter(la -> requestDto.getUploadStatus().contains(la.getUploadStatus()))
                .toList();
        }

        List<ErLocalAuthorityStatusResponseDto.ErLocalAuthorityStatus> localAuthorityStatuses = new ArrayList<>();

        localAuthorities.forEach(la -> {
            FileUploadsService.FileUploadStatus fileUpload = fileUploadsList.stream()
                .filter(fu -> fu.getLocalAuthorityCode().equals(la.getLaCode()))
                .findFirst()
                .orElse(null);

            LocalDateTime lastUploadDate = fileUpload != null ? fileUpload.getLastUploadDate() : null;

            localAuthorityStatuses.add(ErLocalAuthorityStatusResponseDto.ErLocalAuthorityStatus.builder()
                .localAuthorityCode(la.getLaCode())
                .localAuthorityName(la.getLaName())
                .uploadStatus(la.getUploadStatus())
                .lastUploadDate(lastUploadDate)
                .build());
        });

        return ErLocalAuthorityStatusResponseDto.builder()
            .localAuthorityStatuses(localAuthorityStatuses)
            .build();
    }

    @Override
    public LocalAuthoritiesResponseDto getLocalAuthorities(boolean activeOnly) {

        log.info("Getting local authorities for ER dashboard, activeOnly: {}", activeOnly);

        List<LocalAuthority> localAuthorities = localAuthorityService.getAllLocalAuthorities(activeOnly);

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
