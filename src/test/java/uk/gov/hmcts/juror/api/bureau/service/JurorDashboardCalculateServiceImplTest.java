package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.juror.api.bureau.domain.StatsAutoProcessed;
import uk.gov.hmcts.juror.api.bureau.domain.StatsNotResponded;
import uk.gov.hmcts.juror.api.bureau.domain.StatsResponseTime;
import uk.gov.hmcts.juror.api.bureau.domain.StatsThirdPartyOnlineResponse;
import uk.gov.hmcts.juror.api.bureau.domain.StatsUnprocessedResponse;
import uk.gov.hmcts.juror.api.bureau.domain.StatsWelshOnlineResponse;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("Duplicates")
@RunWith(MockitoJUnitRunner.class)
public class JurorDashboardCalculateServiceImplTest {

    private List<StatsResponseTime> responsesList = new ArrayList<>();
    private List<StatsNotResponded> notRespondedList = new ArrayList<>();
    private List<StatsUnprocessedResponse> unprocessedList = new ArrayList<>();
    private List<StatsWelshOnlineResponse> welshOnlineResponseList = new ArrayList<>();
    private List<StatsAutoProcessed> autoOnlineResponseList = new ArrayList<>();
    private List<StatsThirdPartyOnlineResponse> thirdPartyOnlineRespList = new ArrayList<>();


    @InjectMocks
    private JurorDashboardCalculateServiceImpl jurorDashboardCalculateService;

    @Before
    public void setUp() throws Exception {

        LocalDate today = LocalDate.now(ZoneId.systemDefault()).atStartOfDay().toLocalDate();

        responsesList = new ArrayList<>(Arrays.asList(
            StatsResponseTime.builder().summonsMonth(getDate(today, 1)).responseMonth(getDate(today, 1))
                .responsePeriod("Within 7 days").locCode("400").responseMethod("Paper").responseCount(5).build(),
            StatsResponseTime.builder().summonsMonth(getDate(today, 1)).responseMonth(getDate(today, 1))
                .responsePeriod("Within 14 days").locCode("401").responseMethod("Online").responseCount(10).build(),
            StatsResponseTime.builder().summonsMonth(getDate(today, 2)).responseMonth(getDate(today, 0))
                .responsePeriod("Over 21 days").locCode("402").responseMethod("Paper").responseCount(15).build(),
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
            StatsWelshOnlineResponse.builder().summonsMonth(getDate(today, 2)).welshResponseCount(5).build(),
            StatsWelshOnlineResponse.builder().summonsMonth(getDate(today, 1)).welshResponseCount(2).build(),
            StatsWelshOnlineResponse.builder().summonsMonth(getDate(today, 0)).welshResponseCount(3).build()
        ));

        autoOnlineResponseList = new ArrayList<>(Arrays.asList(
            StatsAutoProcessed.builder().processedDate(getDate(today, 0)).processedCount(10).build(),
            StatsAutoProcessed.builder().processedDate(getDate(today, 1)).processedCount(5).build()
        ));

        thirdPartyOnlineRespList = new ArrayList<>(Arrays.asList(
            StatsThirdPartyOnlineResponse.builder().summonsMonth(getDate(today, 1))
                .thirdPartyResponseCount(5).build(),
            StatsThirdPartyOnlineResponse.builder().summonsMonth(getDate(today, 0))
                .thirdPartyResponseCount(9).build()
        ));

    }

    private Date getDate(LocalDate date, int monthsToMinus) {
        return Date.from(date.minusMonths(monthsToMinus).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Test
    public void calculateTotalNoOfResponses() {

        Integer expectedResponsesTotal = responsesList.stream()
            .mapToInt(StatsResponseTime::getResponseCount)
            .sum();

        Integer responsesCountTotal = jurorDashboardCalculateService.totalNoOfResponses(responsesList);

        assertThat(responsesCountTotal).isEqualTo(expectedResponsesTotal);
        assertThat(responsesCountTotal).isEqualTo(50);

    }

    @Test
    public void calculateTotalNotResponded() {

        Integer expectedNotRespondedTotal = notRespondedList.stream()
            .mapToInt(StatsNotResponded::getNotRespondedCount)
            .sum();

        Integer notRespondedTotal = jurorDashboardCalculateService.totalNoOfNotResponded(notRespondedList);

        assertThat(notRespondedTotal).isEqualTo(expectedNotRespondedTotal);
        assertThat(notRespondedTotal).isEqualTo(30);

    }

    @Test
    public void calculateTotalUnprocessed() {

        Integer expectedUnprocessedTotal = unprocessedList.stream()
            .mapToInt(StatsUnprocessedResponse::getUnprocessedCount)
            .sum();

        Integer unprocessedTotal = jurorDashboardCalculateService.totalNoOfUnprocessed(unprocessedList);

        assertThat(unprocessedTotal).isEqualTo(expectedUnprocessedTotal);
        assertThat(unprocessedTotal).isEqualTo(75);

    }

    @Test
    public void calculateTotalSummoned() {

        Integer expectedResponsesTotal = responsesList.stream()
            .mapToInt(StatsResponseTime::getResponseCount)
            .sum();

        Integer expectedNotRespondedTotal = notRespondedList.stream()
            .mapToInt(StatsNotResponded::getNotRespondedCount)
            .sum();

        Integer summonedTotal = jurorDashboardCalculateService.totalNoOfSummoned(
            jurorDashboardCalculateService.totalNoOfResponses(responsesList),
            jurorDashboardCalculateService.totalNoOfNotResponded(notRespondedList));

        assertThat(summonedTotal).isEqualTo(expectedResponsesTotal + expectedNotRespondedTotal);
        assertThat(summonedTotal).isEqualTo(80);

    }

    @Test
    public void calculateTotalWelshResponses() {

        Integer expectedWelshOnlineRespTotal = welshOnlineResponseList.stream()
            .mapToInt(StatsWelshOnlineResponse::getWelshResponseCount)
            .sum();

        Integer welshOnlineResponseTotal =
            jurorDashboardCalculateService.totalNoofWelshOnlineResponses(welshOnlineResponseList);

        assertThat(welshOnlineResponseTotal).isEqualTo(expectedWelshOnlineRespTotal);
        assertThat(welshOnlineResponseTotal).isEqualTo(10);

    }

    @Test
    public void calculateTotalAutoProcessedResponses() {

        Integer expectedAutoOnlineRespTotal = autoOnlineResponseList.stream()
            .mapToInt(StatsAutoProcessed::getProcessedCount)
            .sum();

        Integer autoOnlineResponseTotal =
            jurorDashboardCalculateService.totalNoofAutoOnlineResponses(autoOnlineResponseList);

        assertThat(autoOnlineResponseTotal).isEqualTo(expectedAutoOnlineRespTotal);
        assertThat(autoOnlineResponseTotal).isEqualTo(15);

    }

    @Test
    public void calculateTotalThirdPtyResponses() {

        Integer expectedThirdOnlineRespTotal = thirdPartyOnlineRespList.stream()
            .mapToInt(StatsThirdPartyOnlineResponse::getThirdPartyResponseCount)
            .sum();

        Integer thirdPtyOnlineResponseTotal =
            jurorDashboardCalculateService.totalThirdPtyOnlineResponses(thirdPartyOnlineRespList);

        assertThat(thirdPtyOnlineResponseTotal).isEqualTo(expectedThirdOnlineRespTotal);
        assertThat(thirdPtyOnlineResponseTotal).isEqualTo(14);

    }

    @Test
    public void responsesByMethodCountsAll() {

        Integer expectedOnlineResponsesTotal = responsesList.stream()
            .filter(r -> r.getResponseMethod().equals("Online"))
            .mapToInt(StatsResponseTime::getResponseCount)
            .sum();

        Map<String, Map<String, Integer>> responsesCounts =
            jurorDashboardCalculateService.reponsesByMethod(responsesList);
        assertThat(responsesCounts.containsKey("Online")).isTrue();
        assertThat(responsesCounts.containsKey("Paper")).isTrue();
        Integer actualOnlineResponsesTotal =
            responsesCounts.get("Online").values().stream().mapToInt(Integer::intValue).sum();
        assertThat(actualOnlineResponsesTotal).isEqualTo(expectedOnlineResponsesTotal);

    }

    @Test
    public void responsesByMethodCountsPaperOnly() {

        LocalDate today = LocalDate.now(ZoneId.systemDefault()).atStartOfDay().toLocalDate();

        List<StatsResponseTime> paperResponsesList = Arrays.asList(
            StatsResponseTime.builder().summonsMonth(getDate(today, 1)).responseMonth(getDate(today, 1))
                .responsePeriod("Within 7 days").locCode("400").responseMethod("Paper").responseCount(5).build(),
            StatsResponseTime.builder().summonsMonth(getDate(today, 2)).responseMonth(getDate(today, 0))
                .responsePeriod("Over 21 days").locCode("402").responseMethod("Paper").responseCount(15).build()
        );

        Map<String, Map<String, Integer>> responsesCounts =
            jurorDashboardCalculateService.reponsesByMethod(paperResponsesList);
        assertThat(responsesCounts.containsKey("Online")).isFalse();
        assertThat(responsesCounts.containsKey("Paper")).isTrue();

        assertThat(responsesCounts.get("Paper").getOrDefault("Within 7 days", 0)).isEqualTo(5);
        assertThat(responsesCounts.get("Paper").getOrDefault("Within 14 days", 0)).isEqualTo(0);

    }

    @Test
    public void responsesByMethodCountsEmpty() {

        List<StatsResponseTime> responsesList = new ArrayList<>();

        Map<String, Map<String, Integer>> responsesCounts =
            jurorDashboardCalculateService.reponsesByMethod(responsesList);
        assertThat(responsesCounts.containsKey("Online")).isFalse();
        assertThat(responsesCounts.containsKey("Paper")).isFalse();

    }

    @Test(expected = NullPointerException.class)
    public void responsesByMethodCountsZeroResponses() {
        jurorDashboardCalculateService.reponsesByMethod(null);
    }

}
