package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import uk.gov.hmcts.juror.api.bureau.controller.request.DashboardRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.DashboardMandatoryKpiData;
import uk.gov.hmcts.juror.api.bureau.controller.response.DashboardResponseDto;
import uk.gov.hmcts.juror.api.bureau.domain.StatsAutoProcessed;
import uk.gov.hmcts.juror.api.bureau.domain.StatsNotResponded;
import uk.gov.hmcts.juror.api.bureau.domain.StatsNotRespondedTotals;
import uk.gov.hmcts.juror.api.bureau.domain.StatsResponseTime;
import uk.gov.hmcts.juror.api.bureau.domain.StatsResponseTimesTotals;
import uk.gov.hmcts.juror.api.bureau.domain.StatsThirdPartyOnlineResponse;
import uk.gov.hmcts.juror.api.bureau.domain.StatsUnprocessedResponse;
import uk.gov.hmcts.juror.api.bureau.domain.StatsWelshOnlineResponse;
import uk.gov.hmcts.juror.api.bureau.domain.SurveyResponse;
import uk.gov.hmcts.juror.api.bureau.exception.DashboardException;
import uk.gov.hmcts.juror.api.moj.service.AppSettingService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class JurorDashboardServiceImpl implements JurorDashboardService {

    private static final String PAPER = "Paper";
    private static final String ONLINE = "Online";

    private static final String WITHIN7DAYS = "Within 7 days";
    private static final String WITHIN14DAYS = "Within 14 days";
    private static final String WITHIN21DAYS = "Within 21 days";
    private static final String OVER21DAYS = "Over 21 days";

    private final JurorDashboardDataService jurorDashboardData;
    private final JurorDashboardCalculateService jurorDashboardCalculateService;
    private final AppSettingService appSetting;

    @Autowired
    public JurorDashboardServiceImpl(
        final JurorDashboardDataService jurorDashboardData,
        final AppSettingService appSetting,
        final JurorDashboardCalculateService jurorDashboardCalculateService) {
        Assert.notNull(jurorDashboardData, "JurorDashboardDataService cannot be null");
        Assert.notNull(jurorDashboardCalculateService, "JurorDashboardCalculateService cannot be null");
        Assert.notNull(appSetting, "AppSettingService cannot be null.");
        this.appSetting = appSetting;
        this.jurorDashboardData = jurorDashboardData;
        this.jurorDashboardCalculateService = jurorDashboardCalculateService;
    }

    @Override
    public DashboardResponseDto.CumulativeTotal getCumulativeTotals(DashboardRequestDto request) {
        log.debug("Called Service : JurorDashboardServiceImpl.getCumulativeTotals() ");


        //Validate date range
        if (isdateRangeInvalid(request)) {
            log.error("Totals cannot be retrieved. Invalid Date Range.");
            throw new DashboardException.InvalidDateRange();
        }
        // Amend date ranges to include whole months in range.
        List<Date> dateRange = getAmendedDateRange(request);
        String methodResponse = "Online";
        // Get responded total from db within a date range.
        final List<StatsResponseTime> responsesOverTime = jurorDashboardData.getResponsesOverTime(
            dateRange.get(0), dateRange.get(1));

        // Get not-responded total from db within a date range.
        final List<StatsNotResponded> notResponded = jurorDashboardData.getNotResponded(
            dateRange.get(0), dateRange.get(1));


        // GET all response count totals from the view STAT_RESPONSE_TIMES_TOTAL
        final List<StatsResponseTimesTotals> allResponseTimesTotals =
            jurorDashboardData.getAllStatsResponseTimesTotals();

        // GET online response count totals from the view STAT_RESPONSE_TIMES_TOTAL
        final List<StatsResponseTimesTotals> onlineResponseTimesTotals =
            jurorDashboardData.getOnlineStatsResponseTimesTotals();


        // Get the total responses count from the STATS_NOT_RESPONDED table
        final List<StatsNotRespondedTotals> notRespondedTotal = jurorDashboardData.getNotRespondedTotal();

        // Get unprocessed snapshot from db.
        final List<StatsUnprocessedResponse> unprocessedOnlineResponses =
            jurorDashboardData.getUnprocessedOnlineResponses();

        DashboardResponseDto.CumulativeTotal cumulativeTotal = new DashboardResponseDto.CumulativeTotal();

        cumulativeTotal.setRespondedTotal(jurorDashboardCalculateService.totalNoOfResponses(responsesOverTime));
        cumulativeTotal.setNotRespondedTotal(jurorDashboardCalculateService.totalNoOfNotResponded(notResponded));

        int countTotalResponses = (jurorDashboardCalculateService.allResponsesTotal(allResponseTimesTotals));
        int countTotalNotResponded = (jurorDashboardCalculateService.totalNoOfNotRespondedTotal(notRespondedTotal));
        int countTotalOnlineResponses =
            (jurorDashboardCalculateService.onlineResponsesTotal(onlineResponseTimesTotals));
        int totalSummonses = countTotalResponses + countTotalNotResponded;
        cumulativeTotal.setTotalNumberSummonsesSent(appSetting.getTotalNumberSummonsesSent() + (totalSummonses));
        cumulativeTotal.setTotalNumberOnlineReplies(
            appSetting.getTotalNumberOnlineReplies() + (countTotalOnlineResponses));
        cumulativeTotal.setSummonedTotal(jurorDashboardCalculateService.totalNoOfSummoned(
            cumulativeTotal.getRespondedTotal(),
            cumulativeTotal.getNotRespondedTotal()
        ));
        cumulativeTotal.setCurrentUnprocessed(jurorDashboardCalculateService.totalNoOfUnprocessed(
            unprocessedOnlineResponses));

        log.debug("countTotalResponses jurorDashboardCalculateService.allResponsesTotal : {} ", countTotalResponses);
        log.debug(
            "countTotalNotResponded jurorDashboardCalculateService.totalNoOfNotRespondedTotal: {} ",
            countTotalNotResponded
        );
        log.debug(
            "countTotalOnlineResponses : jurorDashboardCalculateService.onlineResponsesTotal {} ",
            countTotalOnlineResponses
        );


        return cumulativeTotal;
    }


    @Override
    public DashboardMandatoryKpiData getMandatoryKpis(DashboardRequestDto request) {

        DashboardResponseDto.CumulativeTotal totals;

        totals = getCumulativeTotals(request);
        // Amend date ranges to include whole months in range.
        List<Date> dateRange = getAmendedDateRange(request);

        // Get responded total from db.
        final List<StatsResponseTime> responsesOverTime = jurorDashboardData.getResponsesOverTime(
            dateRange.get(0), dateRange.get(1));

        //Check data returned
        if (isRetrievedDataEmpty(responsesOverTime)) {
            log.error("Responses not found. Cannot produce dashboard statistics.");
            throw new DashboardException.NoResponsesFound();
        }

        DashboardMandatoryKpiData mandatoryKpiData = new DashboardMandatoryKpiData();

        // Call calculate services to work out breakdowns by paper/online over time.
        // get totals for paper/online responses.
        Map<String, Map<String, Integer>> channelResponses = jurorDashboardCalculateService.reponsesByMethod(
            responsesOverTime);

        // Add the Paper, Online and Total response counts.
        mandatoryKpiData.setPaperResponsesOverTime(getChannelResponse(channelResponses, PAPER));
        log.debug("Paper responses over time : {}", mandatoryKpiData.getPaperResponsesOverTime());

        mandatoryKpiData.setOnlineResponsesOverTime(getChannelResponse(channelResponses, ONLINE));
        log.debug("Online responses over time : {}", mandatoryKpiData.getOnlineResponsesOverTime());

        mandatoryKpiData.setAllResponsesOverTime(getTotalResponses(
            mandatoryKpiData.getOnlineResponsesOverTime(),
            mandatoryKpiData.getPaperResponsesOverTime()
        ));

        // PaperResponsesTotal
        mandatoryKpiData.setPaperResponsesTotal(sumTotals(mandatoryKpiData.getPaperResponsesOverTime()));
        // OnlineResponsesTotal
        mandatoryKpiData.setOnlineResponsesTotal(sumTotals(mandatoryKpiData.getOnlineResponsesOverTime()));
        // responsesTotal
        mandatoryKpiData.setResponsesTotal(sumTotals(mandatoryKpiData.getAllResponsesOverTime()));
        //summonedTotal
        mandatoryKpiData.setSummonedTotal(totals.getSummonedTotal());

        // percentages - channel take up
        mandatoryKpiData.setPercentPaperTakeUp(jurorDashboardCalculateService.percentage(
            mandatoryKpiData.getPaperResponsesTotal().floatValue(), mandatoryKpiData.getResponsesTotal().floatValue()));

        mandatoryKpiData.setPercentOnlineTakeUp(jurorDashboardCalculateService.percentage(
            mandatoryKpiData.getOnlineResponsesTotal().floatValue(),
            mandatoryKpiData.getResponsesTotal().floatValue()
        ));

        // percentages - responses by time over total responses

        //percentResponsesWithin7days;               // allResponsesOverTime.within7days / responsesTotal
        mandatoryKpiData.setPercentResponsesWithin7days(jurorDashboardCalculateService.percentage(
            mandatoryKpiData.getAllResponsesOverTime().getWithin7days().floatValue(),
            mandatoryKpiData.getResponsesTotal().floatValue()
        ));

        //percentResponsesWithin14days;               // allResponsesOverTime.within14days / responsesTotal
        mandatoryKpiData.setPercentResponsesWithin14days(jurorDashboardCalculateService.percentage(
            mandatoryKpiData.getAllResponsesOverTime().getWithin14days().floatValue(),
            mandatoryKpiData.getResponsesTotal().floatValue()
        ));

        //percentResponsesWithin21days;               // allResponsesOverTime.within21days / responsesTotal
        mandatoryKpiData.setPercentResponsesWithin21days(jurorDashboardCalculateService.percentage(
            mandatoryKpiData.getAllResponsesOverTime().getWithin21days().floatValue(),
            mandatoryKpiData.getResponsesTotal().floatValue()
        ));

        //percentResponsesOver21days;               // allResponsesOverTime.over21days / responsesTotal
        mandatoryKpiData.setPercentResponsesOver21days(jurorDashboardCalculateService.percentage(
            mandatoryKpiData.getAllResponsesOverTime().getOver21days().floatValue(),
            mandatoryKpiData.getResponsesTotal().floatValue()
        ));


        // percentages - digital responses by time over total responses by time

        //percentOnlineResponsesWithin7days;               // onlineResponsesOverTime.within7days /
        // allResponsesOverTime.within7days
        mandatoryKpiData.setPercentOnlineResponsesWithin7days(jurorDashboardCalculateService.percentage(
            mandatoryKpiData.getOnlineResponsesOverTime().getWithin7days().floatValue(),
            mandatoryKpiData.getAllResponsesOverTime().getWithin7days().floatValue()
        ));

        //percentOnlineResponsesWithin14days;               // onlineResponsesOverTime.within14days /
        // allResponsesOverTime.within14days
        mandatoryKpiData.setPercentOnlineResponsesWithin14days(jurorDashboardCalculateService.percentage(
            mandatoryKpiData.getOnlineResponsesOverTime().getWithin14days().floatValue(),
            mandatoryKpiData.getAllResponsesOverTime().getWithin14days().floatValue()
        ));

        //percentOnlineResponsesWithin21days;               // onlineResponsesOverTime.within21days /
        // allResponsesOverTime.within21days
        mandatoryKpiData.setPercentOnlineResponsesWithin21days(jurorDashboardCalculateService.percentage(
            mandatoryKpiData.getOnlineResponsesOverTime().getWithin21days().floatValue(),
            mandatoryKpiData.getAllResponsesOverTime().getWithin21days().floatValue()
        ));

        //percentOnlineResponsesOver21days;               // onlineResponsesOverTime.over21days /
        // allResponsesOverTime.over21days
        mandatoryKpiData.setPercentOnlineResponsesOver21days(jurorDashboardCalculateService.percentage(
            mandatoryKpiData.getOnlineResponsesOverTime().getOver21days().floatValue(),
            mandatoryKpiData.getAllResponsesOverTime().getOver21days().floatValue()
        ));

        return mandatoryKpiData;
    }


    @Override
    public DashboardResponseDto.WelshOnlineResponseData getWelshResponses(DashboardRequestDto request) {
        // Obtain the online responses total
        DashboardMandatoryKpiData mandatoryKpiData = getMandatoryKpis(request);

        // Amend date ranges to include whole months in range.
        List<Date> dateRange = getAmendedDateRange(request);

        // Get welsh responses from db for given period.
        final List<StatsWelshOnlineResponse> welshResponses = jurorDashboardData.getWelshOnlineResponses(
            dateRange.get(0), dateRange.get(1));

        //Check data returned
        if (isRetrievedDataEmpty(welshResponses)) {
            log.error("Welsh responses not found. Cannot produce dashboard statistics.");
            throw new DashboardException.NoResponsesFound();
        }

        DashboardResponseDto.WelshOnlineResponseData welshOnlineResponseData =
            new DashboardResponseDto.WelshOnlineResponseData();

        //get total count of welsh responses.
        welshOnlineResponseData.setWelshOnlineResponseTotal(
            jurorDashboardCalculateService.totalNoofWelshOnlineResponses(welshResponses));

        welshOnlineResponseData.setOnlineResponseTotal(mandatoryKpiData.getOnlineResponsesTotal());
        welshOnlineResponseData.setPercentWelshOnlineResponses(jurorDashboardCalculateService.percentage(
            welshOnlineResponseData.getWelshOnlineResponseTotal().floatValue(),
            welshOnlineResponseData.getOnlineResponseTotal().floatValue(), 2
        ));

        return welshOnlineResponseData;
    }

    @Override
    public DashboardResponseDto.AutoOnlineResponseData getAutoProcessedResponses(DashboardRequestDto request) {
        // Obtain the online responses total
        DashboardMandatoryKpiData mandatoryKpiData = getMandatoryKpis(request);

        // Amend date ranges to include whole months in range.
        List<Date> dateRange = getAmendedDateRange(request);

        // Get welsh responses from db for given period.
        final List<StatsAutoProcessed> autoResponses = jurorDashboardData.getAutoOnlineResponses(
            dateRange.get(0), dateRange.get(1));

        //Check data returned
        if (isRetrievedDataEmpty(autoResponses)) {
            log.error("Auto processed responses not found. Cannot produce dashboard statistics.");
            throw new DashboardException.NoResponsesFound();
        }

        DashboardResponseDto.AutoOnlineResponseData autoOnlineResponseData =
            new DashboardResponseDto.AutoOnlineResponseData();

        //get total count of auto processed responses.
        autoOnlineResponseData.setAutoProcessedOnlineResponseTotal(
            jurorDashboardCalculateService.totalNoofAutoOnlineResponses(autoResponses));

        autoOnlineResponseData.setOnlineResponseTotal(mandatoryKpiData.getOnlineResponsesTotal());
        autoOnlineResponseData.setPercentAutoProcessedOnlineResponses(jurorDashboardCalculateService.percentage(
            autoOnlineResponseData.getAutoProcessedOnlineResponseTotal().floatValue(),
            autoOnlineResponseData.getOnlineResponseTotal().floatValue()
        ));

        return autoOnlineResponseData;
    }

    @Override
    public DashboardResponseDto.ThirdPtyOnlineResponseData getThirdPtyResponses(DashboardRequestDto request) {
        // Obtain the online responses total
        DashboardMandatoryKpiData mandatoryKpiData = getMandatoryKpis(request);

        // Amend date ranges to include whole months in range.
        List<Date> dateRange = getAmendedDateRange(request);

        // Get third party responses from db for given period.
        final List<StatsThirdPartyOnlineResponse> thirdPtyResponses = jurorDashboardData.getThirdPtyOnlineResponses(
            dateRange.get(0), dateRange.get(1));

        //Check data returned
        if (isRetrievedDataEmpty(thirdPtyResponses)) {
            log.error("Third party responses not found. Cannot produce dashboard statistics.");
            throw new DashboardException.NoResponsesFound();
        }

        DashboardResponseDto.ThirdPtyOnlineResponseData thirdOnlineResponseData =
            new DashboardResponseDto.ThirdPtyOnlineResponseData();

        //get total count of third party responses.
        thirdOnlineResponseData.setThirdPtyOnlineResponseTotal(
            jurorDashboardCalculateService.totalThirdPtyOnlineResponses(thirdPtyResponses));

        thirdOnlineResponseData.setOnlineResponseTotal(mandatoryKpiData.getOnlineResponsesTotal());
        thirdOnlineResponseData.setPercentThirdPtyOnlineResponses(jurorDashboardCalculateService.percentage(
            thirdOnlineResponseData.getThirdPtyOnlineResponseTotal().floatValue(),
            thirdOnlineResponseData.getOnlineResponseTotal().floatValue()
        ));

        return thirdOnlineResponseData;

    }

    @Override
    public DashboardResponseDto.SurveySatisfactionData getSurveyResponses(DashboardRequestDto request) {

        // Amend date ranges to include whole months in range.
        List<Date> dateRange = getAmendedDateRange(request);

        int responseCount = 0;
        int[] surveyCounts = new int[5];

        for (int i = 0;
             i < 5;
             i++) {
            surveyCounts[i] = 0;
        }

        // Get survey responses responses from db for given period.
        final List<SurveyResponse> surveyResponses = jurorDashboardData.getSurveyResponses(
            dateRange.get(0), dateRange.get(1));

        log.debug("Satisfaction survey responses count: " + surveyResponses.size());

        // Check data returned
        if (isRetrievedDataEmpty(surveyResponses)) {
            log.error("Satisfaction survey responses not found. Cannot produce dashboard statistics.");
            throw new DashboardException.NoResponsesFound();
        } else {
            //Get ratings counts
            for (int i = 0;
                 i < surveyResponses.size();
                 i++) {
                SurveyResponse responseObj = surveyResponses.get(i);

                switch (responseObj.getSatisfactionDesc().toLowerCase()) {
                    case "very satisfied":
                        responseCount++;
                        surveyCounts[0]++;
                        break;
                    case "satisfied":
                        responseCount++;
                        surveyCounts[1]++;
                        break;
                    case "neither satisfied or dissatisfied":
                        responseCount++;
                        surveyCounts[2]++;
                        break;
                    case "dissatisfied":
                        responseCount++;
                        surveyCounts[3]++;
                        break;
                    case "very dissatisfied":
                        responseCount++;
                        surveyCounts[4]++;
                        break;
                    default:
                        log.error("Unknown survey rating description: " + responseObj.getSatisfactionDesc());
                }
            }
        }

        DashboardResponseDto.SurveySatisfactionData surveySatisfactionData =
            new DashboardResponseDto.SurveySatisfactionData();

        surveySatisfactionData.setResponsesTotal(responseCount);
        surveySatisfactionData.setVerySatisfiedTotal(surveyCounts[0]);
        surveySatisfactionData.setSatisfiedTotal(surveyCounts[1]);
        surveySatisfactionData.setNeitherSatisfiedOrDissatisfiedTotal(surveyCounts[2]);
        surveySatisfactionData.setDissatisfiedTotal(surveyCounts[3]);
        surveySatisfactionData.setVeryDissatisfiedTotal(surveyCounts[4]);

        return surveySatisfactionData;
    }

    private List<Date> getAmendedDateRange(DashboardRequestDto requestDto) {
        LocalDate sdate =
            convertToLocalDateViaInstant(requestDto.getStartDate()).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate edate =
            convertToLocalDateViaInstant(requestDto.getEndDate()).with(TemporalAdjusters.lastDayOfMonth());

        log.debug("ResponseOverTimeContents sdate : {} ", sdate);
        log.debug("ResponseOverTimeContents edate : {} ", edate);

        Date startRepDate = Date.from(sdate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endRepDate = Date.from(edate.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant());
        log.info("ResponseOverTimeContents startRepDate : {} ", startRepDate);
        log.info("ResponseOverTimeContents endRepDate : {} ", endRepDate);
        return Lists.newArrayList(startRepDate, endRepDate);

    }

    private LocalDate convertToLocalDateViaInstant(Date dateToConvert) {
        return dateToConvert.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDate();
    }

    private boolean isdateRangeInvalid(DashboardRequestDto request) {
        LocalDateTime startDate = LocalDateTime.ofInstant(request.getStartDate().toInstant(), ZoneId.systemDefault());
        LocalDateTime endDate = LocalDateTime.ofInstant(request.getEndDate().toInstant(), ZoneId.systemDefault());

        // if start date is later than end date
        return startDate.isAfter(endDate);
    }

    private boolean isRetrievedDataEmpty(List<?> retrievedData) {
        return retrievedData == null;  //|| retrievedData.isEmpty();
    }

    private DashboardMandatoryKpiData.ResponseMethod getChannelResponse(
        Map<String, Map<String, Integer>> channelResponses, String channel) {

        Map<String, Integer> timeResponses = channelResponses.getOrDefault(channel, null);
        return timeResponses != null && timeResponses.size() > 0
            ?
            DashboardMandatoryKpiData.ResponseMethod.builder()
                .within7days(timeResponses.getOrDefault(WITHIN7DAYS, 0))
                .within14days(timeResponses.getOrDefault(WITHIN14DAYS, 0))
                .within21days(timeResponses.getOrDefault(WITHIN21DAYS, 0))
                .over21days(timeResponses.getOrDefault(OVER21DAYS, 0))
                .build()
            :
                DashboardMandatoryKpiData.ResponseMethod.builder()
                    .within7days(0).within14days(0).within21days(0).over21days(0).build();
    }

    private DashboardMandatoryKpiData.ResponseMethod getTotalResponses(
        DashboardMandatoryKpiData.ResponseMethod channelAResponses,
        DashboardMandatoryKpiData.ResponseMethod channelBResponses) {
        return DashboardMandatoryKpiData.ResponseMethod.builder()
            .within7days(channelAResponses.getWithin7days() + channelBResponses.getWithin7days())
            .within14days(channelAResponses.getWithin14days() + channelBResponses.getWithin14days())
            .within21days(channelAResponses.getWithin21days() + channelBResponses.getWithin21days())
            .over21days(channelAResponses.getOver21days() + channelBResponses.getOver21days())
            .build();

    }

    private Integer sumTotals(DashboardMandatoryKpiData.ResponseMethod responseMethod) {
        return responseMethod.getWithin7days() + responseMethod.getWithin14days()
            + responseMethod.getWithin21days() + responseMethod.getOver21days();
    }
}
