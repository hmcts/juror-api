package uk.gov.hmcts.juror.api.jurorer.service;

import uk.gov.hmcts.juror.api.jurorer.controller.dto.*;

/**
 * Service interface for upload-related operations.
 */
public interface UploadService {


    /**
     * Get dashboard information for a user.
     */
    DashboardInfoDto getDashboardInfo(String username);

    /**
     * Get upload history for a user's LA, including recent uploads and their statuses.
     *
     * @param username User's email (username)
     * @return Complete page data DTO
     */
    UploadHistoryDto getUploadHistory(String username);


    /**
     * Get current system deadline information.
     */
    DeadlineDto getDeadlineInfo();

    /**
     * Get upload status for a specific local authority.
     */
    UploadStatusDto getUploadStatus(String laCode);

    /**
     * Get upload status for the authenticated user's LA.
     */
    UploadStatusDto getUploadStatusForUser(String username);

    /**
     * Get upload history for a user's LA.
     */
    UploadHistoryDto getUploadHistory(String username, int limit);

    /**
     * Process file upload and update LA status.
     *
     * @param username User uploading the file
     * @param request File upload metadata
     * @return Upload response with details and updated status
     */
    FileUploadsResponseDto processFileUpload(String username, FileUploadRequestDto request);
}
