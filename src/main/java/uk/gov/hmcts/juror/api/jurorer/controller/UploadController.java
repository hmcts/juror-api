package uk.gov.hmcts.juror.api.jurorer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
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
import uk.gov.hmcts.juror.api.jurorer.controller.dto.UploadHistoryDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.UploadStatusDto;
import uk.gov.hmcts.juror.api.jurorer.service.UploadService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;


@RestController
@Validated
@RequestMapping(value = "/api/v1/juror-er/upload", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Upload Management")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
@Slf4j
public class UploadController {

    private final UploadService uploadService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard information on file upload page")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<DashboardInfoDto> getDashboardInfo() {

        String username = SecurityUtil.getLaUsername();
        DashboardInfoDto dashboard = uploadService.getDashboardInfo(username);

        return ResponseEntity.ok(dashboard);
    }

    @PostMapping(value = "/file")
    @Operation(summary = "Save uploaded file information")
    public ResponseEntity<String> uploadFile(
            @Valid @RequestBody FileUploadRequestDto request) {

        log.debug("File upload request: filename={}, format={}, size={}",
                request.getFilename(), request.getFileFormat(), request.getFileSizeBytes());

        String username = SecurityUtil.getLaUsername();

        uploadService.processFileUpload(username, request);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @GetMapping("/upload-history")
    @Operation(summary = "Get file upload history for user's Local Authority")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UploadHistoryDto> getUploadPageData() {
        log.info("GET /api/v1/juror-er/upload/page-data - Get complete page data");

        String username = SecurityUtil.getLaUsername();
        log.debug("Authenticated LA user from JWT: {}", username);

        UploadHistoryDto pageData = uploadService.getUploadHistory(username);

        log.info("Successfully retrieved page data for user: {}", username);
        return ResponseEntity.ok(pageData);
    }

    @GetMapping("/deadline")
    @Operation(summary = "Get deadline information")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<DeadlineDto> getDeadlineInfo() {

        DeadlineDto deadline = uploadService.getDeadlineInfo();
        return ResponseEntity.ok(deadline);
    }

    @GetMapping("/status")
    @Operation(summary = "Get upload status for user's Local Authority")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UploadStatusDto> getUploadStatus() {
        log.info("GET /api/v1/juror-er/upload/status - Get upload status");

        String username = SecurityUtil.getLaUsername();
        UploadStatusDto status = uploadService.getUploadStatusForUser(username);

        return ResponseEntity.ok(status);
    }

    @GetMapping("/status/{la_code}")
    @Operation(summary = "Get upload status by LA code")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<UploadStatusDto> getUploadStatusByLaCode(
            @P("la_code")
            @PathVariable("la_code")
            @Parameter(description = "Local Authority code", required = true)
            @Pattern(regexp = "^\\d{3}$", message = "LA code must be exactly 3 digits")
            @Valid String laCode) {

        log.info("GET /api/v1/juror-er/upload/status/{} - Get upload status by LA code", laCode);

        // Validate user has access to this LA code
        SecurityUtil.validateCanAccessLaCode(laCode);

        UploadStatusDto status = uploadService.getUploadStatus(laCode);
        return ResponseEntity.ok(status);
    }
}
