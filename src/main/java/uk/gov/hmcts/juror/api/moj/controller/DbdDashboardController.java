package uk.gov.hmcts.juror.api.moj.controller;



import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.juror.api.moj.controller.request.DbdDashboardRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.DbdDashboardResponseDto;
import uk.gov.hmcts.juror.api.moj.service.DbdDashboardService;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/moj/dbd-dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
public class DbdDashboardController {

    private final DbdDashboardService dbdDashboardService;

    @Autowired
    public DbdDashboardController(DbdDashboardService dbdDashboardService) {
        Assert.notNull(dbdDashboardService, "DbdDashboardService cannot be null");
        this.dbdDashboardService = dbdDashboardService;
    }

    @PostMapping("/statistics")
    @Operation(summary = "/dbd-dashboard/statistics",
        description = "Retrieve Digital by Default pilot take-up statistics for one or more court "
            + "groups, over one or two date ranges.")
    public ResponseEntity<DbdDashboardResponseDto> statistics(
        @Parameter(hidden = true) BureauJwtAuthentication principal,
        @Validated @RequestBody DbdDashboardRequestDto dbdDashboardRequestDto) {

        return ResponseEntity.ok().body(dbdDashboardService.getGroupStatistics(dbdDashboardRequestDto));
    }
}
