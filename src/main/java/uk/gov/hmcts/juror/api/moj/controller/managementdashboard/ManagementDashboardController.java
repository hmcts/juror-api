package uk.gov.hmcts.juror.api.moj.controller.managementdashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.moj.service.ManagementDashboardService;

@RestController
@RequestMapping(value = "/api/v1/moj/management-dashboard/", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Management Dashboard", description = "Management Dashboard API")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class ManagementDashboardController {

    @NonNull
    private final ManagementDashboardService managementDashboardService;

    @GetMapping("/overdue-utilisation")
    @Operation(summary = "Get the overdue utilisation report for all courts")
    public ResponseEntity<OverdueUtilisationReportResponseDto> getOverdueUtilisationReport() {
        OverdueUtilisationReportResponseDto dto = managementDashboardService.getOverdueUtilisationReport(true);
        return ResponseEntity.ok().body(dto);
    }

    @GetMapping("/incomplete-service")
    @Operation(summary = "Get the incomplete service report for all courts")
    public ResponseEntity<IncompleteServiceReportResponseDto> getIncompleteServiceReport() {
        IncompleteServiceReportResponseDto dto = managementDashboardService.getIncompleteServiceReport();
        return ResponseEntity.ok().body(dto);
    }

    @GetMapping("/weekend-attendance")
    @Operation(summary = "Get the weekend attendance report for all courts")
    public ResponseEntity<WeekendAttendanceReportResponseDto> getWeekendAttendanceReport() {
        WeekendAttendanceReportResponseDto dto = managementDashboardService.getWeekendAttendanceReport();
        return ResponseEntity.ok().body(dto);
    }

    @GetMapping("/expense-limits")
    @Operation(summary = "Get the expense limits report for all courts")
    public ResponseEntity<ExpenseLimitsReportResponseDto> getExpenseLimitsReport() {
        ExpenseLimitsReportResponseDto dto = managementDashboardService.getExpenseLimitsReport();
        return ResponseEntity.ok().body(dto);
    }

    @GetMapping("/sms-messages")
    @Operation(summary = "Get the outgoing SMS messages report for top 10 courts")
    public ResponseEntity<SmsMessagesReportResponseDto> getSmsMessagesReport() {
        SmsMessagesReportResponseDto dto = managementDashboardService.getSmsMessagesReport();
        return ResponseEntity.ok().body(dto);
    }

}
