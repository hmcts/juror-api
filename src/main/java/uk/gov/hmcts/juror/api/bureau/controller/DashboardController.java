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
import uk.gov.hmcts.juror.api.bureau.controller.request.DashboardRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.DashboardDeferralExcusalResponseDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.DashboardResponseDto;
import uk.gov.hmcts.juror.api.bureau.service.DashboardDeferralExcusalService;
import uk.gov.hmcts.juror.api.bureau.service.JurorDashboardService;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/bureau/dashboard", produces = MediaType.APPLICATION_JSON_VALUE)
public class DashboardController {

    private final JurorDashboardService jurorDashboardService;
    private final DashboardDeferralExcusalService dashboardDeferralExcusalService;

    @Autowired
    public DashboardController(JurorDashboardService jurorDashboardService,
                               DashboardDeferralExcusalService dashboardDeferralExcusalService) {
        Assert.notNull(jurorDashboardService, " JurorDashboardService cannot be null");
        Assert.notNull(dashboardDeferralExcusalService, "DashboardDeferralExcusalservice cannot be null");
        this.dashboardDeferralExcusalService = dashboardDeferralExcusalService;
        this.jurorDashboardService = jurorDashboardService;
    }

    @PostMapping("/cumulativeTotals")
    @Operation(summary = "/dashboard/cumulativeTotals",
        description = "Retrieve the cumulative summons,responses totals for a given period.")
    public ResponseEntity<DashboardResponseDto> cumulativeTotals(
        @Parameter(hidden = true) BureauJwtAuthentication principal,
        @Validated @RequestBody DashboardRequestDto dashboardRequestDto) {

        final DashboardResponseDto dashboardResponseDto = new DashboardResponseDto();
        dashboardResponseDto.setCumulativeTotals(jurorDashboardService.getCumulativeTotals(dashboardRequestDto));
        return ResponseEntity.ok().body(dashboardResponseDto);
    }

    @PostMapping("/mandatoryKpis")
    @Operation(summary = "/dashboard/mandatoryKpis",
        description = "Retrieve the mandatory kpis for response channels over a given period.")
    public ResponseEntity<DashboardResponseDto> mandatoryKpis(
        @Parameter(hidden = true) BureauJwtAuthentication principal,
        @Validated @RequestBody DashboardRequestDto dashboardRequestDto) {

        final DashboardResponseDto dashboardResponseDto = new DashboardResponseDto();
        dashboardResponseDto.setMandatoryKpis(jurorDashboardService.getMandatoryKpis(dashboardRequestDto));
        return ResponseEntity.ok().body(dashboardResponseDto);
    }

    @PostMapping("/welshResponses")
    @Operation(summary = "/dashboard/welshResponses",
        description = "Retrieve the welsh responses counts for online responses over a given period.")
    public ResponseEntity<DashboardResponseDto> welshResponses(
        @Parameter(hidden = true) BureauJwtAuthentication principal,
        @Validated @RequestBody DashboardRequestDto dashboardRequestDto) {

        final DashboardResponseDto dashboardResponseDto = new DashboardResponseDto();
        dashboardResponseDto.setWelshResponseData(jurorDashboardService.getWelshResponses(dashboardRequestDto));
        return ResponseEntity.ok().body(dashboardResponseDto);
    }

    @PostMapping("/autoProcessedResponses")
    @Operation(summary = "/dashboard/autoProcessedResponses",
        description = "Retrieve the auto processed responses counts for online responses over a given period.")
    public ResponseEntity<DashboardResponseDto> autoProcessedResponses(
        @Parameter(hidden = true) BureauJwtAuthentication principal,
        @Validated @RequestBody DashboardRequestDto dashboardRequestDto) {

        final DashboardResponseDto dashboardResponseDto = new DashboardResponseDto();
        dashboardResponseDto.setAutoProcessedResponseData(jurorDashboardService.getAutoProcessedResponses(
            dashboardRequestDto));
        return ResponseEntity.ok().body(dashboardResponseDto);
    }

    @PostMapping("/thirdPartyResponses")
    @Operation(summary = "/dashboard/thirdPartyResponses",
        description = "Retrieve the third party responses counts for online responses over a given period.")
    public ResponseEntity<DashboardResponseDto> thirdPartyResponses(
        @Parameter(hidden = true) BureauJwtAuthentication principal,
        @Validated @RequestBody DashboardRequestDto dashboardRequestDto) {

        final DashboardResponseDto dashboardResponseDto = new DashboardResponseDto();
        dashboardResponseDto.setThirdPtyResponseData(jurorDashboardService.getThirdPtyResponses(dashboardRequestDto));
        return ResponseEntity.ok().body(dashboardResponseDto);
    }

    @PostMapping("/surveyResponses")
    @Operation(summary = "/dashboard/surveyResponses",
        description = "Retrieve the satisfaction survey response counts over a given period.")
    public ResponseEntity<DashboardResponseDto> surveyResponses(
        @Parameter(hidden = true) BureauJwtAuthentication principal,
        @Validated @RequestBody DashboardRequestDto dashboardRequestDto) {

        final DashboardResponseDto dashboardResponseDto = new DashboardResponseDto();
        dashboardResponseDto.setSurveyResponseData(jurorDashboardService.getSurveyResponses(dashboardRequestDto));
        return ResponseEntity.ok().body(dashboardResponseDto);
    }

    @PostMapping("/statistics")
    @Operation(summary = "/dashboard/statistics",
        description = "Retrieve the responses statistices over a given period.")
    public ResponseEntity<DashboardResponseDto> statistics(
        @Parameter(hidden = true) BureauJwtAuthentication principal,
        @Validated @RequestBody DashboardRequestDto dashboardRequestDto) {

        final DashboardResponseDto dashboardResponseDto = new DashboardResponseDto();
        dashboardResponseDto.setCumulativeTotals(jurorDashboardService.getCumulativeTotals(dashboardRequestDto));
        dashboardResponseDto.setMandatoryKpis(jurorDashboardService.getMandatoryKpis(dashboardRequestDto));
        dashboardResponseDto.setWelshResponseData(jurorDashboardService.getWelshResponses(dashboardRequestDto));
        dashboardResponseDto.setAutoProcessedResponseData(jurorDashboardService.getAutoProcessedResponses(
            dashboardRequestDto));
        dashboardResponseDto.setThirdPtyResponseData(jurorDashboardService.getThirdPtyResponses(dashboardRequestDto));
        dashboardResponseDto.setSurveyResponseData(jurorDashboardService.getSurveyResponses(dashboardRequestDto));
        return ResponseEntity.ok().body(dashboardResponseDto);
    }

    @PostMapping("/deferral-Excusal")
    @Operation(summary = "/dashboard/deferral-Excusal",
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
