package uk.gov.hmcts.juror.api.moj.controller.reports;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.config.security.IsBureauUser;
import uk.gov.hmcts.juror.api.config.security.IsCourtUser;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.CourtUtilisationStatsReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.JurySummoningMonitorReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.YieldPerformanceReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.CourtUtilisationStatsReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DailyUtilisationReportJurorsResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DailyUtilisationReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DigitalSummonsRepliesReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.FinancialAuditReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.JurySummoningMonitorReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.MonthlyUtilisationReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.ResponsesCompletedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.WeekendAttendanceReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.YieldPerformanceReportResponse;
import uk.gov.hmcts.juror.api.moj.service.report.AttendanceReportService;
import uk.gov.hmcts.juror.api.moj.service.report.FinancialAuditReportService;
import uk.gov.hmcts.juror.api.moj.service.report.JurySummoningMonitorReportService;
import uk.gov.hmcts.juror.api.moj.service.report.ReportService;
import uk.gov.hmcts.juror.api.moj.service.report.SummonsRepliesReportService;
import uk.gov.hmcts.juror.api.moj.service.report.UtilisationReportService;
import uk.gov.hmcts.juror.api.moj.service.report.YieldPerformanceReportService;
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
    private final UtilisationReportService utilisationReportService;
    private final JurySummoningMonitorReportService jurySummoningMonitorReportService;
    private final YieldPerformanceReportService yieldPerformanceReportService;
    private final SummonsRepliesReportService summonsRepliesReportService;
    private final AttendanceReportService attendanceReportService;

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

    @GetMapping("/daily-utilisation/{locCode}")
    @Operation(summary = "View daily utilisation report")
    @ResponseStatus(HttpStatus.OK)
    @IsCourtUser
    public ResponseEntity<DailyUtilisationReportResponse> viewDailyUtilisationReport(
        @P("locCode") @PathVariable("locCode") @CourtLocationCode @Valid String locCode,
        @RequestParam(value = "reportFromDate") @DateTimeFormat(pattern = "yyyy-MM-dd") @Valid LocalDate reportFromDate,
        @RequestParam(value = "reportToDate") @DateTimeFormat(pattern = "yyyy-MM-dd") @Valid LocalDate reportToDate
    ) {

        return ResponseEntity.ok(utilisationReportService.viewDailyUtilisationReport(locCode, reportFromDate,
            reportToDate));
    }

    @GetMapping("/daily-utilisation-jurors/{locCode}")
    @Operation(summary = "View daily utilisation report jurors for a given date")
    @ResponseStatus(HttpStatus.OK)
    @IsCourtUser
    public ResponseEntity<DailyUtilisationReportJurorsResponse> viewDailyUtilisationJurors(
        @P("locCode") @PathVariable("locCode") @CourtLocationCode @Valid String locCode,
        @RequestParam(value = "reportDate") @DateTimeFormat(pattern = "yyyy-MM-dd") @Valid LocalDate reportDate
    ) {

        return ResponseEntity.ok(utilisationReportService.viewDailyUtilisationJurors(locCode, reportDate));
    }

    @GetMapping("/generate-monthly-utilisation/{locCode}")
    @Operation(summary = "Generate monthly utilisation report for a given month")
    @ResponseStatus(HttpStatus.OK)
    @IsCourtUser
    public ResponseEntity<MonthlyUtilisationReportResponse> generateMonthlyUtilisationReport(
        @P("locCode") @PathVariable("locCode") @CourtLocationCode @Valid String locCode,
        @RequestParam(value = "reportDate") @DateTimeFormat(pattern = "yyyy-MM-dd") @Valid LocalDate reportDate
    ) {
        return ResponseEntity.ok(utilisationReportService.generateMonthlyUtilisationReport(locCode, reportDate));
    }

    @GetMapping("/view-monthly-utilisation/{locCode}")
    @Operation(summary = "View monthly utilisation report")
    @ResponseStatus(HttpStatus.OK)
    @IsCourtUser
    public ResponseEntity<MonthlyUtilisationReportResponse> viewMonthlyUtilisationReport(
        @P("locCode") @PathVariable("locCode") @CourtLocationCode @Valid String locCode,
        @RequestParam(value = "reportDate") @DateTimeFormat(pattern = "yyyy-MM-dd") @Valid LocalDate reportDate,
        @RequestParam(value = "previousMonths", required = false) @Valid boolean previousMonths
    ) {
        return ResponseEntity.ok(utilisationReportService.viewMonthlyUtilisationReport(locCode, reportDate,
            previousMonths));
    }

    @GetMapping("/monthly-utilisation-reports/{locCode}")
    @Operation(summary = "Get a list of generated monthly utilisation reports for last 12 months")
    @ResponseStatus(HttpStatus.OK)
    @IsCourtUser
    public ResponseEntity<String> getMonthlyUtilisationReports(
        @P("locCode") @PathVariable("locCode") @CourtLocationCode @Valid String locCode
    ) {
        return ResponseEntity.ok(utilisationReportService.getMonthlyUtilisationReports(locCode));
    }

    @GetMapping("/digital-summons-replies-report/{month}")
    @Operation(summary = "Get a breakdown of digital summons replies on each day of a specific month"
        + ", provide the start date of month, e.g. 2025-10-01 (for bureau users only)")
    @ResponseStatus(HttpStatus.OK)
    @IsBureauUser
    public ResponseEntity<DigitalSummonsRepliesReportResponse>  getDigitalSummonsRepliesReport(
        @P("month") @PathVariable("month") LocalDate month
    ) {
        return ResponseEntity.ok(summonsRepliesReportService.getDigitalSummonsRepliesReport(month));
    }

    @PostMapping("/court-utilisation-stats-report")
    @Operation(summary = "GET With Body", description = "View court utilisation stats report for "
        + "a number of courts")
    @ResponseStatus(HttpStatus.OK)
    @IsCourtUser
    public ResponseEntity<CourtUtilisationStatsReportResponse> getCourtUtilisationStatsReport(
        @RequestBody CourtUtilisationStatsReportRequest request
    ) {
        return ResponseEntity.ok(utilisationReportService.courtUtilisationStatsReport(request));
    }

    @PostMapping("/jury-summoning-monitor")
    @Operation(summary = "View a jury summoning monitor report")
    @ResponseStatus(HttpStatus.OK)
    @IsBureauUser
    public ResponseEntity<JurySummoningMonitorReportResponse> viewJurySummoningMonitorReport(
        @RequestBody
        @Valid JurySummoningMonitorReportRequest jurySummoningMonitorReportRequest
    ) {
        return ResponseEntity.ok(jurySummoningMonitorReportService
            .viewJurySummoningMonitorReport(jurySummoningMonitorReportRequest));
    }

    @PostMapping("/yield-performance")
    @Operation(summary = "View a yield performance report")
    @ResponseStatus(HttpStatus.OK)
    @IsBureauUser
    public ResponseEntity<YieldPerformanceReportResponse> viewYieldPerformanceReport(
        @RequestBody
        @Valid YieldPerformanceReportRequest yieldPerformanceReportRequest
    ) {
        return ResponseEntity.ok(yieldPerformanceReportService
            .viewYieldPerformanceReport(yieldPerformanceReportRequest));
    }

    @GetMapping("/weekend-attendance")
    @Operation(summary = "Get a list of attendance on weekends and bank holidays for the current month")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<WeekendAttendanceReportResponse> getWeekendAttendanceReport() {
        return ResponseEntity.ok(attendanceReportService.getWeekendAttendanceReport());
    }

    @GetMapping("/responses-completed/{month}")
    @Operation(summary = "Get a table of number of digital responses completed by staff for a month"
        + ", provide the start date of month, e.g. 2025-10-01 (for bureau users only)")
    @IsBureauUser
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ResponsesCompletedReportResponse> getResponsesCompletedReport(
        @P("month") @PathVariable("month") LocalDate monthStartDate) {
        return ResponseEntity.ok(summonsRepliesReportService.getResponsesCompletedReport(monthStartDate));
    }

}
