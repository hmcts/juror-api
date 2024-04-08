package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.domain.StatsAutoProcessed;
import uk.gov.hmcts.juror.api.bureau.domain.StatsAutoProcessedRepository;
import uk.gov.hmcts.juror.api.bureau.domain.StatsNotResponded;
import uk.gov.hmcts.juror.api.bureau.domain.StatsNotRespondedRepository;
import uk.gov.hmcts.juror.api.bureau.domain.StatsNotRespondedTotals;
import uk.gov.hmcts.juror.api.bureau.domain.StatsNotRespondedTotalsRepsoitory;
import uk.gov.hmcts.juror.api.bureau.domain.StatsResponseTime;
import uk.gov.hmcts.juror.api.bureau.domain.StatsResponseTimeRepository;
import uk.gov.hmcts.juror.api.bureau.domain.StatsResponseTimesTotalRepository;
import uk.gov.hmcts.juror.api.bureau.domain.StatsResponseTimesTotals;
import uk.gov.hmcts.juror.api.bureau.domain.StatsThirdPartyOnlineResponse;
import uk.gov.hmcts.juror.api.bureau.domain.StatsThirdPartyOnlineResponseRepository;
import uk.gov.hmcts.juror.api.bureau.domain.StatsUnprocessedResponse;
import uk.gov.hmcts.juror.api.bureau.domain.StatsUnprocessedResponseRepository;
import uk.gov.hmcts.juror.api.bureau.domain.StatsWelshOnlineResponse;
import uk.gov.hmcts.juror.api.bureau.domain.StatsWelshOnlineResponseRepository;
import uk.gov.hmcts.juror.api.bureau.domain.SurveyResponse;
import uk.gov.hmcts.juror.api.bureau.domain.SurveyResponseRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@SuppressWarnings("Duplicates")
@RunWith(MockitoJUnitRunner.class)
public class JurorDashboardDataServiceImplTest {

    private Date startDate;
    private Date endDate;

    private List<StatsResponseTime> responsesList = new ArrayList<>();
    private List<StatsNotResponded> notRespondedList = new ArrayList<>();
    private List<StatsUnprocessedResponse> unprocessedList = new ArrayList<>();
    private List<StatsWelshOnlineResponse> welshOnlineResponseList = new ArrayList<>();
    private List<StatsAutoProcessed> autoOnlineResponseList = new ArrayList<>();
    private List<StatsThirdPartyOnlineResponse> thirdPartyOnlineRespList = new ArrayList<>();
    private List<SurveyResponse> surveyResponseList = new ArrayList<>();

    private List<StatsResponseTimesTotals> statsResponseTimesTotalsList = new ArrayList<>();

    private List<StatsNotRespondedTotals> statsNotRespondedTotals = new ArrayList<>();

    @Mock
    private StatsResponseTimeRepository statsResponseTimeRepository;

    @Mock
    private StatsNotRespondedRepository statsNotRespondedRepository;

    @Mock
    private StatsUnprocessedResponseRepository statsUnprocessedResponseRepository;

    @Mock
    private StatsWelshOnlineResponseRepository statsWelshOnlineResponseRepository;

    @Mock
    private StatsAutoProcessedRepository statsAutoProcessedRepository;

    @Mock
    private StatsThirdPartyOnlineResponseRepository statsThirdPtyOnlineResponseRepository;

    @Mock
    private SurveyResponseRepository surveyResponseRepository;

    @Mock
    private StatsResponseTimesTotalRepository statsResponseTimesTotalRepository;

    @Mock
    private StatsNotRespondedTotalsRepsoitory statsNotRespondedTotalsRepsoitory;

    @InjectMocks
    private JurorDashboardDataServiceImpl jurorDashboardDataService;

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
                .responsePeriod("Over 21 days").locCode("400").responseMethod("Online").responseCount(5).build(),
            StatsResponseTime.builder().summonsMonth(getDate(today, 1)).responseMonth(getDate(today, 1))
                .responsePeriod("Over 21 days").locCode("401").responseMethod("Online").responseCount(10).build(),
            StatsResponseTime.builder().summonsMonth(getDate(today, 2)).responseMonth(getDate(today, 0))
                .responsePeriod("Over 21 days").locCode("402").responseMethod("Online").responseCount(15).build(),
            StatsResponseTime.builder().summonsMonth(getDate(today, 2)).responseMonth(getDate(today, 0))
                .responsePeriod("Over 21 days").locCode("403").responseMethod("Online").responseCount(20).build()
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
            StatsWelshOnlineResponse.builder().summonsMonth(getDate(today, 1))
                .welshResponseCount(2).build(),
            StatsWelshOnlineResponse.builder().summonsMonth(getDate(today, 0))
                .welshResponseCount(3).build()

        ));

        autoOnlineResponseList = new ArrayList<>(Arrays.asList(
            StatsAutoProcessed.builder().processedDate(getDate(today, 0)).processedCount(10).build(),
            StatsAutoProcessed.builder().processedDate(getDate(today, 1)).processedCount(5).build()
        ));

        thirdPartyOnlineRespList = new ArrayList<>(Arrays.asList(
            StatsThirdPartyOnlineResponse.builder().summonsMonth(getDate(today, 1))
                .thirdPartyResponseCount(3).build(),
            StatsThirdPartyOnlineResponse.builder().summonsMonth(getDate(today, 0))
                .thirdPartyResponseCount(4).build()
        ));

    }

    private Date getDate(LocalDate date, int monthsToMinus) {
        return Date.from(date.minusMonths(monthsToMinus).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Test
    public void getResponsesHappyPath() {
        given(statsResponseTimeRepository.findBySummonsMonthBetween(startDate, endDate)).willReturn(responsesList);
        List<StatsResponseTime> responsesResult = jurorDashboardDataService.getResponsesOverTime(startDate, endDate);
        assertThat(responsesResult).isNotNull();
        assertThat(responsesResult.size()).isEqualTo(responsesList.size());
        verify(statsResponseTimeRepository).findBySummonsMonthBetween(startDate, endDate);

    }

    @Test
    public void getNotRespondedHappyPath() {
        given(statsNotRespondedRepository.findBySummonsMonthBetween(startDate, endDate)).willReturn(notRespondedList);
        List<StatsNotResponded> notRespondedResults = jurorDashboardDataService.getNotResponded(startDate, endDate);
        assertThat(notRespondedResults).isNotNull();
        assertThat(notRespondedResults.size()).isEqualTo(notRespondedList.size());
        verify(statsNotRespondedRepository).findBySummonsMonthBetween(startDate, endDate);

    }

    @Test
    public void getUnprocessedHappyPath() {
        given(statsUnprocessedResponseRepository.findAll()).willReturn(unprocessedList);
        List<StatsUnprocessedResponse> unprocessedResponses = jurorDashboardDataService.getUnprocessedOnlineResponses();
        assertThat(unprocessedResponses).isNotNull();
        assertThat(unprocessedResponses.size()).isEqualTo(unprocessedList.size());
        verify(statsUnprocessedResponseRepository).findAll();

    }

    @Test
    public void getWelshResponsesHappyPath() {
        given(statsWelshOnlineResponseRepository.findBySummonsMonthBetween(startDate, endDate)).willReturn(
            welshOnlineResponseList);
        List<StatsWelshOnlineResponse> welshOnlineResponses =
            jurorDashboardDataService.getWelshOnlineResponses(startDate, endDate);
        assertThat(welshOnlineResponses).isNotNull();
        assertThat(welshOnlineResponses.size()).isEqualTo(welshOnlineResponseList.size());
        verify(statsWelshOnlineResponseRepository).findBySummonsMonthBetween(startDate, endDate);
    }

    @Test
    public void getAuroResponsesHappyPath() {
        given(statsAutoProcessedRepository.findByProcessedDateBetween(startDate, endDate)).willReturn(
            autoOnlineResponseList);
        List<StatsAutoProcessed> autoOnlineResponses = jurorDashboardDataService.getAutoOnlineResponses(startDate,
            endDate);
        assertThat(autoOnlineResponses).isNotNull();
        assertThat(autoOnlineResponses.size()).isEqualTo(autoOnlineResponseList.size());
        verify(statsAutoProcessedRepository).findByProcessedDateBetween(startDate, endDate);
    }

    @Test
    public void getThirdResponsesHappyPath() {
        given(statsThirdPtyOnlineResponseRepository.findBySummonsMonthBetween(startDate, endDate))
            .willReturn(thirdPartyOnlineRespList);
        List<StatsThirdPartyOnlineResponse> thirdOnlineResponses = jurorDashboardDataService
            .getThirdPtyOnlineResponses(startDate, endDate);
        assertThat(thirdOnlineResponses).isNotNull();
        assertThat(thirdOnlineResponses.size()).isEqualTo(thirdPartyOnlineRespList.size());
        verify(statsThirdPtyOnlineResponseRepository).findBySummonsMonthBetween(startDate, endDate);
    }

}
