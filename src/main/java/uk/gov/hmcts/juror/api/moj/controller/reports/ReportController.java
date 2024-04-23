package uk.gov.hmcts.juror.api.moj.controller.reports;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.FinancialAuditReportResponse;
import uk.gov.hmcts.juror.api.moj.service.report.FinancialAuditReportService;
import uk.gov.hmcts.juror.api.moj.service.report.IncompleteServiceReportService;
import uk.gov.hmcts.juror.api.moj.service.report.ReportService;
import uk.gov.hmcts.juror.api.validation.CourtLocationCode;

import java.time.LocalDate;

import static uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails.F_AUDIT_PREFIX;

@RestController
@Validated
@RequestMapping(value = "/api/v1/moj/reports", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Reports")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ReportController {

    private final ReportService reportService;
    private final FinancialAuditReportService financialAuditReportService;
    private final IncompleteServiceReportService incompleteServiceReportService;

    @PostMapping("/standard")
    @Operation(summary = "View a given report")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AbstractReportResponse<?>> viewReportStandard(
        @RequestBody
        @Valid StandardReportRequest standardReportRequest
    ) {
        return ResponseEntity.ok(reportService.viewStandardReport(standardReportRequest));
    }

    @GetMapping("/financial-audit")
    @Operation(summary = "View a given report")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<FinancialAuditReportResponse> viewFinancialAuditReport(
        @RequestParam(name = "audit-number")
        @Pattern(regexp = "^" + F_AUDIT_PREFIX + "\\d*$")
        @Valid String financialAuditNumber
    ) {
        return ResponseEntity.ok(financialAuditReportService.viewFinancialAuditReport(financialAuditNumber));
    }

    @GetMapping("/incomplete-service")
    @Operation(summary = "View incomplete service report for a specific court")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<AbstractReportResponse<?>> viewIncompleteServiceReport(
        @RequestParam(name = "location") @CourtLocationCode @Valid String location,
        @RequestParam(name = "cut-off-date") @Valid @JsonFormat(pattern = "dd-MM-yyyy") LocalDate cutOffDate
    ) {
        return ResponseEntity.ok(incompleteServiceReportService.viewIncompleteServiceReport(location, cutOffDate));
    }
}
