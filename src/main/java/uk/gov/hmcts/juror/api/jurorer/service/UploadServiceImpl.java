package uk.gov.hmcts.juror.api.jurorer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.jurorer.controller.LaNotFoundException;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.DashboardInfoDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.DeadlineDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.FileUploadDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.FileUploadRequestDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.UploadHistoryDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.UploadStatusDto;
import uk.gov.hmcts.juror.api.jurorer.domain.Deadline;
import uk.gov.hmcts.juror.api.jurorer.domain.FileUploads;
import uk.gov.hmcts.juror.api.jurorer.domain.LaUser;
import uk.gov.hmcts.juror.api.jurorer.domain.LocalAuthority;
import uk.gov.hmcts.juror.api.jurorer.domain.UploadStatus;
import uk.gov.hmcts.juror.api.jurorer.repository.DeadlineRepository;
import uk.gov.hmcts.juror.api.jurorer.repository.FileUploadsRepository;
import uk.gov.hmcts.juror.api.jurorer.repository.LaUserRepository;
import uk.gov.hmcts.juror.api.jurorer.repository.LocalAuthorityRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of UploadService.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final DeadlineRepository deadlineRepository;
    private final LocalAuthorityRepository localAuthorityRepository;
    private final LaUserRepository laUserRepository;
    private final FileUploadsRepository fileUploadsRepository;
    private final LaUserService laUserService;


    @Override
    @Transactional(readOnly = true)
    public DashboardInfoDto getDashboardInfo(String username) {
        log.info("Getting dashboard info for user: {}", username);

        LaUser user = laUserService.findUserByUsernameAndLa(username, SecurityUtil.getActiveLaCode());

        Deadline deadline = deadlineRepository.getCurrentDeadline().orElse(null);
        LocalAuthority localAuthority = user.getLaCode();

        if (localAuthority == null) {
            throw new LaNotFoundException("Local Authority not found for user: " + username);
        }

        // Calculate days remaining
        Long daysRemaining = null;
        if (deadline != null && deadline.getDeadlineDate() != null) {
            daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), deadline.getDeadlineDate());
        }

        // Get last upload date for this LA
        LocalDate lastUploadDate = fileUploadsRepository
                .findTopByLocalAuthority_LaCodeOrderByUploadDateDesc(localAuthority.getLaCode())
                .map(upload -> upload.getUploadDate().toLocalDate())
                .orElse(null);

        return DashboardInfoDto.builder()
                .deadlineDate(deadline != null ? deadline.getDeadlineDate() : null)
                .daysRemaining(daysRemaining)
                .uploadStatus(localAuthority.getUploadStatusString())
                .lastUploadDate(lastUploadDate)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UploadHistoryDto getUploadHistory(String username) {
        log.debug("Getting complete upload page data for user: {}", username);

        return getUploadHistory(username, 10);
    }

    @Override
    public UploadHistoryDto getUploadHistory(String username, int limit) {
        log.debug("Getting upload history for user: {} (limit: {})", username, limit);

        LaUser user = laUserService.findUserByUsernameAndLa(username, SecurityUtil.getActiveLaCode());

        LocalAuthority la = user.getLaCode();
        if (la == null) {
            throw new LaNotFoundException("Local Authority not found for user: " + username);
        }

        String laCode = la.getLaCode();

        Long totalUploads = fileUploadsRepository.countByLaCode(laCode);

        List<FileUploads> recentUploads = fileUploadsRepository
                .findByLaCodeOrderByUploadDateDesc(laCode, PageRequest.of(0, limit));

        List<FileUploadDto> uploadDtos = recentUploads.stream()
                .map(this::convertToFileUploadDto)
                .collect(Collectors.toList());

        return UploadHistoryDto.builder()
                .totalUploads(totalUploads)
                .recentUploads(uploadDtos)
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public DeadlineDto getDeadlineInfo() {
        log.debug("Getting deadline information");

        Deadline deadline = deadlineRepository.getCurrentDeadline()
                .orElse(Deadline.builder()
                        .id(1)
                        .deadlineDate(null)
                        .build());

        Long daysRemaining = null;
        Boolean isOverdue = false;

        if (deadline.getDeadlineDate() != null) {
            daysRemaining = ChronoUnit.DAYS.between(
                    LocalDate.now(),
                    deadline.getDeadlineDate()
            );
            isOverdue = daysRemaining < 0;
        }

        return DeadlineDto.builder()
                .deadlineDate(deadline.getDeadlineDate())
                .uploadStartDate(deadline.getUploadStartDate())
                .daysRemaining(daysRemaining)
                .isDeadlinePassed(isOverdue)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UploadStatusDto getUploadStatus(String laCode) {
        log.debug("Getting upload status for LA: {}", laCode);

        LocalAuthority localAuthority = localAuthorityRepository
                .findByLaCode(laCode)
                .orElseThrow(() -> new LaNotFoundException(
                        "Local Authority not found: " + laCode
                ));

        return buildUploadStatusDto(localAuthority);
    }

    @Override
    @Transactional(readOnly = true)
    public UploadStatusDto getUploadStatusForUser(String username) {
        log.debug("Getting upload status for user: {}", username);

        LaUser user = laUserService.findUserByUsernameAndLa(username, SecurityUtil.getActiveLaCode());

        LocalAuthority localAuthority = user.getLaCode();
        if (localAuthority == null) {
            throw new LaNotFoundException("Local Authority not found for user: " + username);
        }

        return buildUploadStatusDto(localAuthority);
    }


    @Override
    @Transactional
    public void processFileUpload(String username, FileUploadRequestDto request) {

        log.info("Processing file upload for user: {}", username);

        LaUser user = laUserService.findUserByUsernameAndLa(username, SecurityUtil.getActiveLaCode());

        LocalAuthority localAuthority = localAuthorityRepository.findByLaCode(SecurityUtil.getActiveLaCode())
                .orElse(null);
        if (localAuthority == null) {
            throw new LaNotFoundException("Local Authority not found for user: " + username);
        }

        // Create file upload record
        FileUploads fileUpload = FileUploads.builder()
                .localAuthority(localAuthority)
                .username(user.getUsername())
                .filename(request.getFilename())
                .fileFormat(request.getFileFormat())
                .fileSizeBytes(request.getFileSizeBytes())
                .otherInformation(request.getOtherInformation())
                .uploadDate(LocalDateTime.now())
                .build();

        FileUploads savedUpload = fileUploadsRepository.save(fileUpload);
        log.info("File upload record created with ID: {}", savedUpload.getId());

        // Update LA upload status to UPLOADED
        localAuthority.setUploadStatus(UploadStatus.UPLOADED);
        localAuthority.setUpdatedBy(username);
        localAuthority.setLastUpdated(LocalDateTime.now());
        localAuthorityRepository.save(localAuthority);

        log.info("Updated LA {} status to UPLOADED", localAuthority.getLaCode());
    }

    // Helper methods

    private UploadStatusDto buildUploadStatusDto(LocalAuthority la) {
        return UploadStatusDto.builder()
                .laCode(la.getLaCode())
                .laName(la.getLaName())
                .isActive(la.getActive())
                .uploadStatus(la.getUploadStatusString())
                .notes(la.getNotes())
                .inactiveReason(la.getInactiveReason())
                .updatedBy(la.getUpdatedBy())
                .lastUpdated(la.getLastUpdated())
                .build();
    }

    private FileUploadDto convertToFileUploadDto(FileUploads upload) {
        return FileUploadDto.builder()
                .id(upload.getId())
                .filename(upload.getFilename())
                .fileFormat(upload.getFileFormat())
                .fileSizeBytes(upload.getFileSizeBytes())
                .fileSizeFormatted(upload.getFileSizeFormatted())
                .uploadedBy(upload.getUsername())
                .uploadDate(upload.getUploadDate())
                .otherInformation(upload.getOtherInformation())
                .build();
    }
}
