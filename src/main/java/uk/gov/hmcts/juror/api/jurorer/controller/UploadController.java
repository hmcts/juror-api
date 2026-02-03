package uk.gov.hmcts.juror.api.jurorer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.*;
import uk.gov.hmcts.juror.api.jurorer.service.UploadService;

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
@Tag(name = "Upload Management", description = "Endpoints for managing file upload deadlines and status")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
public class UploadController {

    private final UploadService uploadService;

    /**
     * GET COMPLETE PAGE DATA
     *
     * Returns all data needed for the upload page:
     * - Dashboard (deadline, days remaining, upload status)
     * - Upload guidance (instructions, requirements, support contact)
     * - Account details (user info, LA info)
     * - Upload history (recent uploads)
     */
    @GetMapping("/page-data")
    @Operation(
        summary = "Get complete upload page data",
        description = "Returns all information needed for the upload page including dashboard, " +
            "guidance, account details, and upload history"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved page data",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UploadPageDataDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User or Local Authority not found",
            content = @Content
        )
    })
    public ResponseEntity<UploadPageDataDto> getUploadPageData() {
        log.info("GET /api/v1/juror-er/upload/page-data - Get complete page data");

        String username = getAuthenticatedUsername();
        log.debug("Authenticated user: {}", username);

        UploadPageDataDto pageData = uploadService.getUploadPageData(username);

        log.info("Successfully retrieved page data for user: {}", username);
        return ResponseEntity.ok(pageData);
    }

    /**
     * POST FILE UPLOAD
     *
     * Handles file upload and updates LA upload status.
     * Creates a file upload record and sets LA status to UPLOADED.
     */
    @PostMapping(value = "/file", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
        summary = "Upload file and update status",
        description = "Processes file upload, creates upload record, and updates LA status to UPLOADED"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "File uploaded successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = FileUploadsResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request - Invalid file metadata",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User or Local Authority not found",
            content = @Content
        )
    })
    public ResponseEntity<FileUploadsResponseDto> uploadFile(
        @Valid @RequestBody FileUploadRequestDto request) {

        log.info("POST /api/v1/juror-er/upload/file - Upload file");
        log.debug("File upload request: filename={}, format={}, size={}",
                  request.getFilename(), request.getFileFormat(), request.getFileSizeBytes());

        String username = getAuthenticatedUsername();

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
    @Operation(
        summary = "Get dashboard information",
        description = "Returns deadline and upload status information for the authenticated user's Local Authority"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved dashboard information",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DashboardInfoDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User or Local Authority not found",
            content = @Content
        )
    })
    public ResponseEntity<DashboardInfoDto> getDashboardInfo() {
        log.info("GET /api/v1/juror-er/upload/dashboard - Get dashboard info");

        String username = getAuthenticatedUsername();
        DashboardInfoDto dashboard = uploadService.getDashboardInfo(username);

        return ResponseEntity.ok(dashboard);
    }

    /**
     * GET DEADLINE INFO
     *
     * Returns current system deadline information.
     */
    @GetMapping("/deadline")
    @Operation(
        summary = "Get deadline information",
        description = "Returns the current system-wide deadline for file uploads"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved deadline information",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = DeadlineDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content
        )
    })
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
    @Operation(
        summary = "Get upload status for user's Local Authority",
        description = "Returns detailed upload status for the authenticated user's Local Authority"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved upload status",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UploadStatusDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User or Local Authority not found",
            content = @Content
        )
    })
    public ResponseEntity<UploadStatusDto> getUploadStatus() {
        log.info("GET /api/v1/juror-er/upload/status - Get upload status");

        String username = getAuthenticatedUsername();
        UploadStatusDto status = uploadService.getUploadStatusForUser(username);

        return ResponseEntity.ok(status);
    }

    /**
     * GET UPLOAD STATUS BY LA CODE
     *
     * Admin endpoint to get upload status for any LA.
     */
    @GetMapping("/status/{laCode}")
    @Operation(
        summary = "Get upload status by LA code",
        description = "Returns detailed upload status for a specific Local Authority (admin endpoint)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved upload status",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UploadStatusDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing JWT token",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Local Authority not found",
            content = @Content
        )
    })
    public ResponseEntity<UploadStatusDto> getUploadStatusByLaCode(
        @Parameter(description = "Local Authority code", example = "314", required = true)
        @PathVariable String laCode) {

        log.info("GET /api/v1/juror-er/upload/status/{} - Get upload status by LA code", laCode);

        UploadStatusDto status = uploadService.getUploadStatus(laCode);
        return ResponseEntity.ok(status);
    }

    // Helper methods

    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("No authentication found in security context");
            throw new RuntimeException("User not authenticated");
        }

        return authentication.getName();
    }

    // Exception handlers

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(LaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLaNotFound(LaNotFoundException ex) {
        log.error("Local Authority not found: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @Data
    @Builder
    @Schema(description = "Error response")
    static class ErrorResponse {
        @Schema(description = "HTTP status code", example = "404")
        private int status;

        @Schema(description = "Error type", example = "Not Found")
        private String error;

        @Schema(description = "Error message", example = "User not found: user@example.com")
        private String message;
    }
}
