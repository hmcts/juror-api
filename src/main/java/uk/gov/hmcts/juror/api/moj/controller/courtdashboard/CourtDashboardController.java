package uk.gov.hmcts.juror.api.moj.controller.courtdashboard;

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
import uk.gov.hmcts.juror.api.config.security.IsCourtUser;
import uk.gov.hmcts.juror.api.moj.service.CourtDashboardService;

@RestController
@RequestMapping(value = "/api/v1/moj/court-dashboard/", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Court Dashboard", description = "Court Dashboard API")
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class CourtDashboardController {

    @NonNull
    private final CourtDashboardService courtDashboardService;

    @IsCourtUser
    @GetMapping("/notifications/{locCode}")
    @Operation(summary = "Retrieves a list of notifications for court location")
    public ResponseEntity<CourtNotificationListDto> getCourtNotifications(
        @Parameter(description = "3-digit numeric string to identify the court") @PathVariable(name = "locCode")
        @Size(min = 3, max = 3) @Valid String locCode) {
        CourtNotificationListDto dto = courtDashboardService.getCourtNotifications(locCode);
        return ResponseEntity.ok().body(dto);
    }


}
