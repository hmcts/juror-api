package uk.gov.hmcts.juror.api.jurorer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.*;
import uk.gov.hmcts.juror.api.jurorer.domain.*;
import uk.gov.hmcts.juror.api.jurorer.controller.LaNotFoundException;
import uk.gov.hmcts.juror.api.jurorer.controller.UserNotFoundException;
import uk.gov.hmcts.juror.api.jurorer.repository.*;
import uk.gov.hmcts.juror.api.jurorer.service.UploadService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
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

    @Override
    @Transactional(readOnly = true)
    public UploadPageDataDto getUploadPageData(String username) {
        log.debug("Getting complete upload page data for user: {}", username);

        return UploadPageDataDto.builder()
            .dashboard(getDashboardInfo(username))
            .uploadGuidance(getUploadGuidance())
            .accountDetails(getAccountDetails(username))
            .uploadHistory(getUploadHistory(username, 10))
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardInfoDto getDashboardInfo(String username) {
        log.debug("Getting dashboard info for user: {}", username);

        LaUser user = laUserRepository.findByUsername(username);
        if (user == null) {
            log.error("User not found: {}", username);
            throw new UserNotFoundException("User not found: " + username);
        }

        Deadline deadline = deadlineRepository.getCurrentDeadline().orElse(null);
        LocalAuthority localAuthority = user.getLaCode();

        if (localAuthority == null) {
            throw new LaNotFoundException("Local Authority not found for user: " + username);
        }

        Long daysRemaining = null;
        Boolean isOverdue = false;

        if (deadline != null && deadline.getDeadlineDate() != null) {
            daysRemaining = ChronoUnit.DAYS.between(
                LocalDate.now(),
                deadline.getDeadlineDate()
            );
            isOverdue = daysRemaining < 0;
        }

        String statusMessage = buildStatusMessage(
            deadline,
            daysRemaining,
            isOverdue,
            localAuthority.getUploadStatusString()
        );

        return DashboardInfoDto.builder()
            .deadlineDate(deadline != null ? deadline.getDeadlineDate() : null)
            .daysRemaining(daysRemaining)
            .uploadStatus(localAuthority.getUploadStatusString())
            .laCode(localAuthority.getLaCode())
            .laName(localAuthority.getLaName())
            .isOverdue(isOverdue)
            .statusMessage(statusMessage)
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DeadlineDto getDeadlineInfo() {
        log.debug("Getting deadline information");

        Deadline deadline = deadlineRepository.getCurrentDeadline()
            .orElse(Deadline.builder()
                        .id((short) 1)
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
            .daysRemaining(daysRemaining)
            .isOverdue(isOverdue)
            .updatedBy(deadline.getUpdatedBy())
            .lastUpdated(deadline.getLastUpdated())
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

        LaUser user = laUserRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found: " + username);
        }

        LocalAuthority localAuthority = user.getLaCode();
        if (localAuthority == null) {
            throw new LaNotFoundException("Local Authority not found for user: " + username);
        }

        return buildUploadStatusDto(localAuthority);
    }

    @Override
    public UploadGuidanceDto getUploadGuidance() {
        log.debug("Getting upload guidance");

        return UploadGuidanceDto.builder()
            .allowedFormats(Arrays.asList("xlsx", "xls", "csv"))
            .maxFileSizeMb(10)
            .instructions(Arrays.asList(
                "Ensure your file contains all required juror excusal information",
                "Use the provided template format for your data",
                "File must be in Excel (.xlsx, .xls) or CSV (.csv) format",
                "Maximum file size is 10MB",
                "Click 'Choose File' to select your file",
                "Add any additional notes in the 'Other Information' field",
                "Click 'Upload' to submit your file"
            ))
            .requirements(Arrays.asList(
                "File must contain valid juror numbers",
                "Excusal reasons must be from the approved list",
                "All required fields must be populated",
                "No duplicate juror entries allowed",
                "Date formats must be DD/MM/YYYY"
            ))
            .supportContact(SupportContactDto.builder()
                                .email("juror.support@hmcts.gov.uk")
                                .phone("0300 456 1024")
                                .hours("Monday to Friday, 9am to 5pm (excluding bank holidays)")
                                .build())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AccountDetailsDto getAccountDetails(String username) {
        log.debug("Getting account details for user: {}", username);

        LaUser user = laUserRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found: " + username);
        }

        LocalAuthority la = user.getLaCode();

        return AccountDetailsDto.builder()
            .username(user.getUsername())
            .laCode(la != null ? la.getLaCode() : null)
            .laName(la != null ? la.getLaName() : null)
            .active(user.isActive())
            .lastLoggedIn(user.getLastLoggedIn())
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UploadHistoryDto getUploadHistory(String username, int limit) {
        log.debug("Getting upload history for user: {} (limit: {})", username, limit);

        LaUser user = laUserRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found: " + username);
        }

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
    @Transactional
    public FileUploadResponseDto processFileUpload(
        String username,
        FileUploadRequestDto request) {

        log.info("Processing file upload for user: {}", username);

        // Get user and validate
        LaUser user = laUserRepository.findByUsername(username);
        if (user == null) {
            throw new UserNotFoundException("User not found: " + username);
        }

        LocalAuthority localAuthority = user.getLaCode();
        if (localAuthority == null) {
            throw new LaNotFoundException("Local Authority not found for user: " + username);
        }

        // Validate user can upload
        if (!user.isActive()) {
            return FileUploadResponseDto.builder()
                .success(false)
                .message("User account is inactive. Cannot upload file.")
                .build();
        }

        if (!Boolean.TRUE.equals(localAuthority.getActive())) {
            return FileUploadResponseDto.builder()
                .success(false)
                .message("Local Authority is inactive. Cannot upload file.")
                .build();
        }

        // Create file upload record
        FileUploads fileUpload = FileUploads.builder()
            .localAuthority(localAuthority)
            .user(user)
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
        LocalAuthority updatedLA = localAuthorityRepository.save(localAuthority);
        log.info("Updated LA {} status to UPLOADED", localAuthority.getLaCode());

        // Build response
        FileUploadDto uploadDto = convertToFileUploadDto(savedUpload);
        UploadStatusDto statusDto = buildUploadStatusDto(updatedLA);

        return FileUploadResponseDto.builder()
            .success(true)
            .uploadId(savedUpload.getId())
            .message("File uploaded successfully")
            .uploadDetails(uploadDto)
            .updatedStatus(statusDto)
            .build();
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
            .uploadedBy(upload.getUser().getUsername())
            .uploadDate(upload.getUploadDate())
            .otherInformation(upload.getOtherInformation())
            .build();
    }

    private String buildStatusMessage(
        Deadline deadline,
        Long daysRemaining,
        Boolean isOverdue,
        String uploadStatus) {

        if (deadline == null || deadline.getDeadlineDate() == null) {
            return "No deadline has been set yet";
        }

        if ("UPLOADED".equalsIgnoreCase(uploadStatus)) {
            return "Your file has been uploaded successfully";
        }

        if (Boolean.TRUE.equals(isOverdue)) {
            long daysOverdue = Math.abs(daysRemaining);
            return String.format(
                "Deadline has passed (%d %s overdue). Please upload your file as soon as possible",
                daysOverdue,
                daysOverdue == 1 ? "day" : "days"
            );
        }

        if (daysRemaining != null) {
            if (daysRemaining == 0) {
                return "Deadline is today. Please upload your file";
            } else if (daysRemaining == 1) {
                return "You have 1 day remaining to upload your file";
            } else if (daysRemaining <= 7) {
                return String.format(
                    "Urgent: You have only %d days remaining to upload your file",
                    daysRemaining
                );
            } else {
                return String.format(
                    "You have %d days remaining to upload your file",
                    daysRemaining
                );
            }
        }

        return "Please upload your file before the deadline";
    }
}
