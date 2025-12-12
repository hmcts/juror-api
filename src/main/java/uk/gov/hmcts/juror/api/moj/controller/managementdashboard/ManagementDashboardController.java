package uk.gov.hmcts.juror.api.moj.controller.managementdashboard;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
        OverdueUtilisationReportResponseDto dto = managementDashboardService.getOverdueUtilisationReport();
        return ResponseEntity.ok().body(dto);
    }

}
