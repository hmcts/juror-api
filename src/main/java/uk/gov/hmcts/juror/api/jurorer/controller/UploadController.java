package uk.gov.hmcts.juror.api.jurorer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.DashboardInfoDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.DeadlineDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.FileUploadRequestDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.FileUploadsResponseDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.UploadPageDataDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.UploadStatusDto;
import uk.gov.hmcts.juror.api.jurorer.service.UploadService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

/**
 * REST Controller for upload-related operations.
 *
 * Provides endpoints for:
 * - Complete upload page data
 * - File upload processing
 * - Dashboard information
 * - Deadline information
 * - Upload status
 */
@RestController
@Validated
@RequestMapping(value = "/api/v1/juror-er/upload", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Upload Management")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class UploadController {

    private final UploadService uploadService;

    /**
     * GET COMPLETE PAGE DATA
     *
     * Returns all data needed for the upload page:
     * - Dashboard (deadline, days remaining, upload status)
     * - Account details (user info, LA info)
     * - Upload history (recent uploads)
     */
    @GetMapping("/page-data")
    @Operation(summary = "Get complete upload page data")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UploadPageDataDto> getUploadPageData() {
        log.info("GET /api/v1/juror-er/upload/page-data - Get complete page data");

        String username = SecurityUtil.getLaUsername();
        log.debug("Authenticated LA user from JWT: {}", username);

        UploadPageDataDto pageData = uploadService.getUploadPageData(username);

        log.info("Successfully retrieved page data for user: {}", username);
        return ResponseEntity.ok(pageData);
    }

    /**
     * POST FILE UPLOAD
     *
     * Handles file upload and updates LA upload status.
     */
    @PostMapping(value = "/file", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Upload file and update status")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<FileUploadsResponseDto> uploadFile(
            @Valid @RequestBody FileUploadRequestDto request) {

        log.info("POST /api/v1/juror-er/upload/file - Upload file");
        log.debug("File upload request: filename={}, format={}, size={}",
                request.getFilename(), request.getFileFormat(), request.getFileSizeBytes());

        String username = SecurityUtil.getLaUsername();

        FileUploadsResponseDto response = uploadService.processFileUpload(username, request);

        if (Boolean.TRUE.equals(response.getSuccess())) {
            log.info("File uploaded successfully by user: {}, upload ID: {}",
                    username, response.getUploadId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            log.warn("File upload failed for user: {}, reason: {}",
                    username, response.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * GET DASHBOARD INFO
     *
     * Returns dashboard information with deadline and upload status.
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard information")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<DashboardInfoDto> getDashboardInfo() {
        log.info("GET /api/v1/juror-er/upload/dashboard - Get dashboard info");

        String username = SecurityUtil.getLaUsername();
        DashboardInfoDto dashboard = uploadService.getDashboardInfo(username);

        return ResponseEntity.ok(dashboard);
    }

    /**
     * GET DEADLINE INFO
     *
     * Returns current system deadline information.
     */
    @GetMapping("/deadline")
    @Operation(summary = "Get deadline information")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<DeadlineDto> getDeadlineInfo() {
        log.info("GET /api/v1/juror-er/upload/deadline - Get deadline info");

        DeadlineDto deadline = uploadService.getDeadlineInfo();
        return ResponseEntity.ok(deadline);
    }

    /**
     * GET UPLOAD STATUS
     *
     * Returns upload status for authenticated user's LA.
     */
    @GetMapping("/status")
    @Operation(summary = "Get upload status for user's Local Authority")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UploadStatusDto> getUploadStatus() {
        log.info("GET /api/v1/juror-er/upload/status - Get upload status");

        String username = SecurityUtil.getLaUsername();
        UploadStatusDto status = uploadService.getUploadStatusForUser(username);

        return ResponseEntity.ok(status);
    }

    /**
     * GET UPLOAD STATUS BY LA CODE
     *
     * Get upload status for a specific LA (validates user has access).
     */
    @GetMapping("/status/{la_code}")
    @Operation(summary = "Get upload status by LA code")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UploadStatusDto> getUploadStatusByLaCode(
            @P("la_code")
            @PathVariable("la_code")
            @Parameter(description = "Local Authority code", required = true)
            @Valid String laCode) {

        log.info("GET /api/v1/juror-er/upload/status/{} - Get upload status by LA code", laCode);

        // Validate user has access to this LA code
        SecurityUtil.validateCanAccessLaCode(laCode);

        UploadStatusDto status = uploadService.getUploadStatus(laCode);
        return ResponseEntity.ok(status);
    }
}
