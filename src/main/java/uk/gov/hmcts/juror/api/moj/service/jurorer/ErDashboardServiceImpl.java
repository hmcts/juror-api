package uk.gov.hmcts.juror.api.moj.service.jurorer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.jurorer.domain.Deadline;
import uk.gov.hmcts.juror.api.jurorer.domain.FileUploads;
import uk.gov.hmcts.juror.api.jurorer.domain.LaUser;
import uk.gov.hmcts.juror.api.jurorer.domain.LocalAuthority;
import uk.gov.hmcts.juror.api.jurorer.domain.ReminderHistory;
import uk.gov.hmcts.juror.api.jurorer.domain.UploadStatus;
import uk.gov.hmcts.juror.api.jurorer.repository.DeadlineRepository;
import uk.gov.hmcts.juror.api.jurorer.repository.ReminderHistoryRepository;
import uk.gov.hmcts.juror.api.jurorer.service.FileUploadsService;
import uk.gov.hmcts.juror.api.jurorer.service.LaUserService;
import uk.gov.hmcts.juror.api.jurorer.service.LocalAuthorityService;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErDashboardStatsResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErLocalAuthorityStatusRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.ErLocalAuthorityStatusResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.LocalAuthoritiesResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.LocalAuthorityInfoResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateDeadlineRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateDeadlineResponseDto;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.juror.api.validation.LaCodeValidator.isValidLaCode;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ErDashboardServiceImpl implements ErDashboardService {

    private final LocalAuthorityService localAuthorityService;
    private final DeadlineRepository deadlineRepository;
    private final FileUploadsService fileUploadsService;
    private final LaUserService laUserService;
    private final ReminderHistoryRepository reminderHistoryRepository;

    @Override
    public ErDashboardStatsResponseDto getErDashboardStats() {

        log.info("Getting ER dashboard stats");

        Optional<Deadline> deadlineOpt = deadlineRepository.getCurrentDeadline();

        if (deadlineOpt.isEmpty()) {
            throw new MojException.InternalServerError("Upload deadline data not found - it should always exist", null);
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

    @Override
    public LocalAuthorityInfoResponseDto getLocalAuthorityInfo(String laCode) {

        log.info("Getting local authority info for ER dashboard, laCode: {}", laCode);
        // validate the laCode format
        isValidLaCode(laCode);

        LocalAuthority localAuthority = localAuthorityService.getLocalAuthorityByCode(laCode);

        if (localAuthority == null) {
            throw new MojException.NotFound("Local authority not found for code: " + laCode, null);
        }

        LocalAuthorityInfoResponseDto localAuthorityInfoResponseDto = LocalAuthorityInfoResponseDto.builder()
            .localAuthorityCode(localAuthority.getLaCode())
            .localAuthorityName(localAuthority.getLaName())
            .isActive(localAuthority.getActive())
            .uploadStatus(localAuthority.getUploadStatus())
            .emailRequestStatus(localAuthority.getEmailRequestStatus())
            .notes(localAuthority.getNotes())
            .build();

        if (Boolean.FALSE.equals(localAuthority.getActive())) {
            localAuthorityInfoResponseDto.setInactiveInfo(
                LocalAuthorityInfoResponseDto.InactiveInfo.builder()
                    .inactiveReason(localAuthority.getInactiveReason())
                    .madeInactiveBy(localAuthority.getUpdatedBy())
                    .madeInactiveAt(localAuthority.getLastUpdated())
                    .build()
            );
        }

        if (localAuthority.getEmailRequestSent() != null) {
            localAuthorityInfoResponseDto.setDateEmailRequestSent(localAuthority.getEmailRequestSent().toLocalDate());
        }

        FileUploads fileUploads = fileUploadsService.getLatestUploadForLa(laCode);

        if (fileUploads != null) {
            localAuthorityInfoResponseDto.setLastUploadDate(fileUploads.getUploadDate().toLocalDate());
        }

        laUserService.findLastLoggedInUserByLaCode(laCode).ifPresent(lastLoggedInUser -> {
            if (lastLoggedInUser.getLastLoggedIn() != null) {
                localAuthorityInfoResponseDto.setLastLoggedInDate(lastLoggedInUser.getLastLoggedIn().toLocalDate());
            }
        });

        List<LaUser> laUsers = laUserService.findUsersByLaCode(laCode);
        localAuthorityInfoResponseDto.setEmailAddresses(laUsers.stream().map(LaUser::getUsername).toList());

        List<ReminderHistory> reminderHistory = reminderHistoryRepository.findByLaCodeOrderByTimeSentDesc(laCode);

        if (reminderHistory != null && !reminderHistory.isEmpty()) {
            List<LocalAuthorityInfoResponseDto.ReminderHistoryInfo> reminderHistoryInfoList
                = reminderHistory.stream().map(rh -> LocalAuthorityInfoResponseDto
                    .ReminderHistoryInfo.builder().sentBy(rh.getSentBy()).timeSent(rh.getTimeSent())
                    .sentTo(rh.getSentTo())
                    .build()).toList();
            localAuthorityInfoResponseDto.setReminderHistory(reminderHistoryInfoList);
        }

        return localAuthorityInfoResponseDto;
    }

    @Override
    @Transactional
    public UpdateDeadlineResponseDto updateDeadline(UpdateDeadlineRequestDto request) {
        log.info("Updating deadline date to: {}", request.getDeadlineDate());

        // Get current deadline (should always exist)
        Deadline deadline = deadlineRepository.getCurrentDeadline()
            .orElseThrow(() -> new MojException.InternalServerError(
                "Deadline record not found - it should always exist", null));

        // Get current user from JWT
        String currentUser = SecurityUtil.getActiveLogin();

        // Update deadline
        deadline.setDeadlineDate(request.getDeadlineDate());
        deadline.setUpdatedBy(currentUser);
        deadline.setLastUpdated(LocalDate.now());

        // Save updated deadline
        Deadline updatedDeadline = deadlineRepository.save(deadline);

        log.info("Deadline updated successfully by: {}", currentUser);

        return UpdateDeadlineResponseDto.builder()
            .deadlineDate(updatedDeadline.getDeadlineDate())
            .updatedBy(updatedDeadline.getUpdatedBy())
            .lastUpdated(updatedDeadline.getLastUpdated())
            .daysRemaining(updatedDeadline.getDaysRemaining())
            .build();
    }


}
