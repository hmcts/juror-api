package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.controller.request.DashboardRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.DashboardMandatoryKpiData;
import uk.gov.hmcts.juror.api.bureau.controller.response.DashboardResponseDto;

public interface JurorDashboardService {

    /**
     * Get the cumulative totals for the given period.
     *
     * @param request - contains the date range.
     * @return DashboardResponseDto.CumulativeTotal
     */
    DashboardResponseDto.CumulativeTotal getCumulativeTotals(DashboardRequestDto request);

    /**
     * Get the mandatory kpi totals for the given period by response method (paper, online).
     *
     * @param request - contains the date range.
     * @return DashboardMandatoryKpiData
     */
    DashboardMandatoryKpiData getMandatoryKpis(DashboardRequestDto request);

    /**
     * Get the welsh responses as a percentage of all online responses for the given period.
     *
     * @param request - contains the date range.
     * @return DashboardResponseDto.WelshOnlineResponseData
     */
    DashboardResponseDto.WelshOnlineResponseData getWelshResponses(DashboardRequestDto request);

    /**
     * Get the auto processed responses as a percentage of all online responses for the given period.
     *
     * @param request - contains the date range.
     * @return DashboardResponseDto.AutoOnlineResponseData
     */
    DashboardResponseDto.AutoOnlineResponseData getAutoProcessedResponses(DashboardRequestDto request);

    /**
     * Get the third party responses as a percentage of all online responses for the given period.
     *
     * @param request - contains the date range.
     * @return DashboardResponseDto.ThirdPtyOnlineResponseData
     */
    DashboardResponseDto.ThirdPtyOnlineResponseData getThirdPtyResponses(DashboardRequestDto request);

    /**
     * Get the satisfaction survey response totals for the given period.
     *
     * @param request - contains the date range.
     * @return DashboardResponseDto.SurveySatisfactionData
     */
    DashboardResponseDto.SurveySatisfactionData getSurveyResponses(DashboardRequestDto request);

}
