package uk.gov.hmcts.juror.api.bureau.controller;

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
import uk.gov.hmcts.juror.api.bureau.controller.request.DashboardDeferralExcusalRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.DashboardDeferralExcusalResponseDto;
import uk.gov.hmcts.juror.api.bureau.service.DashboardDeferralExcusalService;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;

@Slf4j
@RestController
@RequestMapping(value = "/api/vi/bureau/dashboard/deferral-excusal", produces = MediaType.APPLICATION_JSON_VALUE)
public class DashboardDeferralExcusalController {

    private final DashboardDeferralExcusalService dashboardDeferralExcusalService;

    @Autowired
    public DashboardDeferralExcusalController(DashboardDeferralExcusalService dashboardDeferralExcusalService) {
        Assert.notNull(dashboardDeferralExcusalService, "DashboardDeferralExcusalservice cannot be null");
        this.dashboardDeferralExcusalService = dashboardDeferralExcusalService;
    }

    @PostMapping("/deferralExcusalValues")
    @Operation(summary = "/deferral-excusal/deferralExcusalValues",
        description = "Retrieve Deferral Excusal values from deferral,excusals table.")
    public ResponseEntity<DashboardDeferralExcusalResponseDto> deferralExcusalValues(
        @Parameter(hidden = true) BureauJwtAuthentication principal,
        @Validated @RequestBody DashboardDeferralExcusalRequestDto dashboardDeferralExcusalRequestDto) {

        final DashboardDeferralExcusalResponseDto dashboardDeferralExcusalResponseDto =
            new DashboardDeferralExcusalResponseDto();
        dashboardDeferralExcusalResponseDto.setDeferralExcusalValues(
            dashboardDeferralExcusalService.getDeferralExcusalValues(
                dashboardDeferralExcusalRequestDto));
        return ResponseEntity.ok().body(dashboardDeferralExcusalResponseDto);
    }

}
