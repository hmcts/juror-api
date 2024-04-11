package uk.gov.hmcts.juror.api.moj.controller.reports;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.service.report.ReportService;

@RestController
@Validated
@RequestMapping(value = "/api/v1/moj/reports", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Reports")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/standard")
    @Operation(summary = "View a given report")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AbstractReportResponse<?>> viewReportStandard(
        @RequestBody
        @Valid StandardReportRequest standardReportRequest
    ) {
        return ResponseEntity.ok(reportService.viewStandardReport(standardReportRequest));
    }
}
