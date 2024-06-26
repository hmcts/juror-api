package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.controller.request.DashboardRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.DashboardMandatoryKpiData;
import uk.gov.hmcts.juror.api.bureau.controller.response.DashboardResponseDto;
import uk.gov.hmcts.juror.api.bureau.domain.StatsAutoProcessed;
import uk.gov.hmcts.juror.api.bureau.domain.StatsNotResponded;
import uk.gov.hmcts.juror.api.bureau.domain.StatsResponseTime;
import uk.gov.hmcts.juror.api.bureau.domain.StatsThirdPartyOnlineResponse;
import uk.gov.hmcts.juror.api.bureau.domain.StatsUnprocessedResponse;
import uk.gov.hmcts.juror.api.bureau.domain.StatsWelshOnlineResponse;
import uk.gov.hmcts.juror.api.bureau.exception.DashboardException;
import uk.gov.hmcts.juror.api.moj.service.AppSettingService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SuppressWarnings("Duplicates")
@RunWith(MockitoJUnitRunner.class)
public class JurorDashboardServiceImplTest {

    private List<StatsResponseTime> responsesList = new ArrayList<>();
    private List<StatsNotResponded> notRespondedList = new ArrayList<>();
    private List<StatsUnprocessedResponse> unprocessedList = new ArrayList<>();
    private List<StatsWelshOnlineResponse> welshOnlineResponseList = new ArrayList<>();
    private List<StatsAutoProcessed> autoOnlineResponseList = new ArrayList<>();
    private List<StatsThirdPartyOnlineResponse> thirdPartyOnlineRespList = new ArrayList<>();


    private Map<String, Map<String, Integer>> channelResp;
    private DashboardRequestDto requestDto;
    private Date startDate;
    private Date endDate;

    @Mock
    private JurorDashboardDataService jurorDashboardData;

    @Mock
    private JurorDashboardCalculateService jurorDashboardCalculateService;

    @Mock
    private AppSettingService appSetting;

    @InjectMocks
    private JurorDashboardServiceImpl jurorDashboardService;


    @Before
    public void setUp() throws Exception {

        LocalDate today = LocalDate.now(ZoneId.systemDefault()).atStartOfDay().toLocalDate();

        //create valid Date Range params (2 months).
        startDate =
            Date.from(
                today.minusMonths(1).atStartOfDay(ZoneId.systemDefault()).with(TemporalAdjusters.firstDayOfMonth())
                    .toInstant());

        endDate =
            Date.from(today.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).with(TemporalAdjusters.lastDayOfMonth())
                .toInstant());

        responsesList = new ArrayList<>(Arrays.asList(
            StatsResponseTime.builder().summonsMonth(getDate(today, 1)).responseMonth(getDate(today, 1))
                .responsePeriod("Within 21 days").locCode("400").responseMethod("Online").responseCount(5).build(),
            StatsResponseTime.builder().summonsMonth(getDate(today, 1)).responseMonth(getDate(today, 1))
                .responsePeriod("Over 21 days").locCode("401").responseMethod("Paper").responseCount(10).build(),
            StatsResponseTime.builder().summonsMonth(getDate(today, 2)).responseMonth(getDate(today, 0))
                .responsePeriod("Within 14 days").locCode("402").responseMethod("Online").responseCount(15).build(),
            StatsResponseTime.builder().summonsMonth(getDate(today, 2)).responseMonth(getDate(today, 0))
                .responsePeriod("Within 7 days").locCode("403").responseMethod("Online").responseCount(20).build(),
            StatsResponseTime.builder().summonsMonth(getDate(today, 1)).responseMonth(getDate(today, 1))
                .responsePeriod("Within 14 days").locCode("403").responseMethod("Online").responseCount(5).build(),
            StatsResponseTime.builder().summonsMonth(getDate(today, 1)).responseMonth(getDate(today, 1))
                .responsePeriod("Within 14 days").locCode("403").responseMethod("Paper").responseCount(10).build(),
            StatsResponseTime.builder().summonsMonth(getDate(today, 2)).responseMonth(getDate(today, 0))
                .responsePeriod("Over 21 days").locCode("403").responseMethod("Paper").responseCount(15).build()
        ));

        notRespondedList = new ArrayList<>(Arrays.asList(
            StatsNotResponded.builder().summonsMonth(getDate(today, 1))
                .locCode("400").notRespondedCount(5).build(),
            StatsNotResponded.builder().summonsMonth(getDate(today, 1))
                .locCode("401").notRespondedCount(10).build(),
            StatsNotResponded.builder().summonsMonth(getDate(today, 1))
                .locCode("402").notRespondedCount(15).build()
        ));

        unprocessedList = new ArrayList<>(Arrays.asList(
            StatsUnprocessedResponse.builder().locCode("400").unprocessedCount(5).build(),
            StatsUnprocessedResponse.builder().locCode("401").unprocessedCount(10).build(),
            StatsUnprocessedResponse.builder().locCode("402").unprocessedCount(15).build(),
            StatsUnprocessedResponse.builder().locCode("403").unprocessedCount(20).build(),
            StatsUnprocessedResponse.builder().locCode("404").unprocessedCount(25).build()
        ));

        welshOnlineResponseList = new ArrayList<>(Arrays.asList(
            StatsWelshOnlineResponse.builder().summonsMonth(getDate(today, 2)).welshResponseCount(5).build(),
            StatsWelshOnlineResponse.builder().summonsMonth(getDate(today, 1)).welshResponseCount(2).build(),
            StatsWelshOnlineResponse.builder().summonsMonth(getDate(today, 0)).welshResponseCount(3).build()
        ));

        channelResp = new HashMap<String, Map<String, Integer>>() {
            {
                put("Paper", new HashMap<String, Integer>() {
                    {
                        put("Within 14 days", 10);
                        put("Over 21 days", 25);
                    }
                });
                put("Online", new HashMap<String, Integer>() {
                    {
                        put("Within 7 days", 20);
                        put("Within 14 days", 20);
                        put("Within 21 days", 5);
                    }
                });
            }
        };

        autoOnlineResponseList = new ArrayList<>(Arrays.asList(
            StatsAutoProcessed.builder().processedDate(getDate(today, 0)).processedCount(5).build(),
            StatsAutoProcessed.builder().processedDate(getDate(today, 1)).processedCount(5).build(),
            StatsAutoProcessed.builder().processedDate(getDateLessDays(today, 1)).processedCount(7).build(),
            StatsAutoProcessed.builder().processedDate(getDateLessDays(today, 2)).processedCount(8).build(),
            StatsAutoProcessed.builder().processedDate(getDateLessDays(today, 40)).processedCount(2).build(),
            StatsAutoProcessed.builder().processedDate(getDateLessDays(today, 41)).processedCount(3).build()
        ));

        thirdPartyOnlineRespList = new ArrayList<>(Arrays.asList(
            StatsThirdPartyOnlineResponse.builder().summonsMonth(getDate(today, 1))
                .thirdPartyResponseCount(4).build(),
            StatsThirdPartyOnlineResponse.builder().summonsMonth(getDate(today, 0))
                .thirdPartyResponseCount(5).build(),
            StatsThirdPartyOnlineResponse.builder().summonsMonth(getDate(today, 0))
                .thirdPartyResponseCount(3).build()
        ));

    }

    private Date getDate(LocalDate date, int monthsToMinus) {
        return Date.from(date.minusMonths(monthsToMinus).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date getDateLessDays(LocalDate date, int daysToMinus) {
        return Date.from(date.minusDays(daysToMinus).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Test
    public void getCumulativeTotalsHappyPath() {

        given(jurorDashboardData.getResponsesOverTime(startDate, endDate)).willReturn(responsesList);
        given(jurorDashboardData.getNotResponded(startDate, endDate)).willReturn(notRespondedList);
        given(jurorDashboardData.getUnprocessedOnlineResponses()).willReturn(unprocessedList);

        given(jurorDashboardCalculateService.totalNoOfResponses(responsesList)).willReturn(80);
        given(jurorDashboardCalculateService.totalNoOfNotResponded(notRespondedList)).willReturn(30);
        given(jurorDashboardCalculateService.totalNoOfUnprocessed(unprocessedList)).willReturn(75);
        given(jurorDashboardCalculateService.totalNoOfSummoned(80, 30)).willReturn(110);

        requestDto = DashboardRequestDto.builder().startDate(startDate).endDate(endDate).build();
        DashboardResponseDto.CumulativeTotal cumulativeTotals = jurorDashboardService.getCumulativeTotals(requestDto);

        assertThat(cumulativeTotals.getSummonedTotal()).isEqualTo(110);
        assertThat(cumulativeTotals.getRespondedTotal()).isEqualTo(80);
        assertThat(cumulativeTotals.getNotRespondedTotal()).isEqualTo(30);
        assertThat(cumulativeTotals.getCurrentUnprocessed()).isEqualTo(75);

        verify(jurorDashboardData).getResponsesOverTime(startDate, endDate);
        verify(jurorDashboardData).getNotResponded(startDate, endDate);
        verify(jurorDashboardData).getUnprocessedOnlineResponses();

        verify(jurorDashboardCalculateService).totalNoOfResponses(responsesList);
        verify(jurorDashboardCalculateService).totalNoOfNotResponded(notRespondedList);
        verify(jurorDashboardCalculateService).totalNoOfUnprocessed(unprocessedList);
        verify(jurorDashboardCalculateService).totalNoOfSummoned(80, 30);

    }

    @Test
    public void getMandatoryKpisHappyPath() {

        given(jurorDashboardData.getResponsesOverTime(startDate, endDate)).willReturn(responsesList);
        given(jurorDashboardData.getNotResponded(startDate, endDate)).willReturn(notRespondedList);
        given(jurorDashboardData.getUnprocessedOnlineResponses()).willReturn(unprocessedList);

        given(jurorDashboardCalculateService.totalNoOfResponses(responsesList)).willReturn(80);
        given(jurorDashboardCalculateService.totalNoOfNotResponded(notRespondedList)).willReturn(30);
        given(jurorDashboardCalculateService.totalNoOfUnprocessed(unprocessedList)).willReturn(75);
        given(jurorDashboardCalculateService.totalNoOfSummoned(80, 30)).willReturn(110);

        given(jurorDashboardCalculateService.reponsesByMethod(responsesList)).willReturn(channelResp);

        given(jurorDashboardCalculateService.percentage(35f, 80f)).willReturn(43.8f);   // paper uptake
        given(jurorDashboardCalculateService.percentage(45f, 80f)).willReturn(56.3f);   // online uptake

        requestDto = DashboardRequestDto.builder().startDate(startDate).endDate(endDate).build();
        DashboardMandatoryKpiData mandatoryKpiData = jurorDashboardService.getMandatoryKpis(requestDto);

        assertThat(mandatoryKpiData).isNotNull();
        assertThat(mandatoryKpiData.getOnlineResponsesOverTime().getWithin14days()
            + mandatoryKpiData.getPaperResponsesOverTime().getWithin14days())
            .isEqualTo(mandatoryKpiData.getAllResponsesOverTime().getWithin14days());

        assertThat(mandatoryKpiData.getPercentPaperTakeUp()).isEqualTo(43.8f);
        assertThat(mandatoryKpiData.getPercentOnlineTakeUp()).isEqualTo(56.3f);

        verify(jurorDashboardData, times(2)).getResponsesOverTime(startDate, endDate);
        verify(jurorDashboardData).getNotResponded(startDate, endDate);
        verify(jurorDashboardData).getUnprocessedOnlineResponses();

        verify(jurorDashboardCalculateService).totalNoOfResponses(responsesList);
        verify(jurorDashboardCalculateService).totalNoOfNotResponded(notRespondedList);
        verify(jurorDashboardCalculateService).totalNoOfUnprocessed(unprocessedList);
        verify(jurorDashboardCalculateService).totalNoOfSummoned(80, 30);

    }

    @Test
    public void getWeslshResponseDataHappyPath() {

        given(jurorDashboardData.getResponsesOverTime(startDate, endDate)).willReturn(responsesList);
        given(jurorDashboardData.getWelshOnlineResponses(startDate, endDate)).willReturn(welshOnlineResponseList);

        given(jurorDashboardCalculateService.reponsesByMethod(responsesList)).willReturn(channelResp);
        given(jurorDashboardCalculateService.percentage(10f, 45f, 2)).willReturn(22.22f);   // welsh online resp
        given(jurorDashboardCalculateService.totalNoofWelshOnlineResponses(welshOnlineResponseList)).willReturn(10);

        requestDto = DashboardRequestDto.builder().startDate(startDate).endDate(endDate).build();
        DashboardResponseDto.WelshOnlineResponseData welshResponses =
            jurorDashboardService.getWelshResponses(requestDto);

        assertThat(welshResponses).isNotNull();
        assertThat(welshResponses.getWelshOnlineResponseTotal()).isEqualTo(10);
        assertThat(welshResponses.getOnlineResponseTotal()).isEqualTo(45);
        assertThat(welshResponses.getPercentWelshOnlineResponses()).isEqualTo(22.22f);

    }

    @Test
    public void getAutoResponseDataHappyPath() {
        given(jurorDashboardData.getResponsesOverTime(startDate, endDate)).willReturn(responsesList);
        given(jurorDashboardData.getAutoOnlineResponses(startDate, endDate)).willReturn(autoOnlineResponseList);

        given(jurorDashboardCalculateService.reponsesByMethod(responsesList)).willReturn(channelResp);

        given(jurorDashboardCalculateService.percentage(30f, 45f)).willReturn(66.7f);   // welsh online resp
        given(jurorDashboardCalculateService.totalNoofAutoOnlineResponses(autoOnlineResponseList)).willReturn(30);

        requestDto = DashboardRequestDto.builder().startDate(startDate).endDate(endDate).build();
        DashboardResponseDto.AutoOnlineResponseData autoResponses =
            jurorDashboardService.getAutoProcessedResponses(requestDto);

        assertThat(autoResponses).isNotNull();
        assertThat(autoResponses.getAutoProcessedOnlineResponseTotal()).isEqualTo(30);
        assertThat(autoResponses.getOnlineResponseTotal()).isEqualTo(45);
        assertThat(autoResponses.getPercentAutoProcessedOnlineResponses()).isEqualTo(66.7f);
    }

    @Test
    public void getThirdPtyResponseDataHappyPath() {

        given(jurorDashboardData.getResponsesOverTime(startDate, endDate)).willReturn(responsesList);
        given(jurorDashboardData.getThirdPtyOnlineResponses(startDate, endDate)).willReturn(thirdPartyOnlineRespList);

        given(jurorDashboardCalculateService.reponsesByMethod(responsesList)).willReturn(channelResp);
        given(jurorDashboardCalculateService.percentage(12f, 45f)).willReturn(26.7f);   // 3rd pty online resp
        given(jurorDashboardCalculateService.totalThirdPtyOnlineResponses(thirdPartyOnlineRespList)).willReturn(12);

        requestDto = DashboardRequestDto.builder().startDate(startDate).endDate(endDate).build();
        DashboardResponseDto.ThirdPtyOnlineResponseData thirdPtyResponses =
            jurorDashboardService.getThirdPtyResponses(requestDto);

        assertThat(thirdPtyResponses).isNotNull();
        assertThat(thirdPtyResponses.getThirdPtyOnlineResponseTotal()).isEqualTo(12);
        assertThat(thirdPtyResponses.getOnlineResponseTotal()).isEqualTo(45);
        assertThat(thirdPtyResponses.getPercentThirdPtyOnlineResponses()).isEqualTo(26.7f);
    }

    @Test(expected = DashboardException.InvalidDateRange.class)
    public void getCumulativeTotalsInvalidDateRange() {
        requestDto = DashboardRequestDto.builder().startDate(endDate).endDate(startDate).build();
        jurorDashboardService.getCumulativeTotals(requestDto);
    }

    @Test
    public void getMandatoryKpisNoData() {

        requestDto = DashboardRequestDto.builder().startDate(startDate).endDate(endDate).build();
        DashboardMandatoryKpiData mandatoryKpiData = jurorDashboardService.getMandatoryKpis(requestDto);

        assertThat(mandatoryKpiData).isNotNull();
        assertThat(mandatoryKpiData.getOnlineResponsesTotal()).isEqualTo(0);
        assertThat(mandatoryKpiData.getPaperResponsesTotal()).isEqualTo(0);
        assertThat(mandatoryKpiData.getResponsesTotal()).isEqualTo(0);

    }

}
