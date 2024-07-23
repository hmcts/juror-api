package uk.gov.hmcts.juror.api.bureau.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
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

import java.util.Date;
import java.util.List;


@Slf4j
@Service
public class JurorDashboardDataServiceImpl implements JurorDashboardDataService {


    private final StatsResponseTimeRepository statsResponseTimeRepository;
    private final StatsNotRespondedRepository statsNotRespondedRepository;
    private final StatsNotRespondedTotalsRepsoitory statsNotRespondedTotalsRepsoitory;
    private final StatsUnprocessedResponseRepository statsUnprocessedResponseRepository;
    private final StatsWelshOnlineResponseRepository statsWelshOnlineResponseRepository;
    private final StatsAutoProcessedRepository statsAutoProcessedRepository;
    private final StatsThirdPartyOnlineResponseRepository statsThirdPtyOnlnResponseRepository;
    private final SurveyResponseRepository surveyResponseRepository;
    private final StatsResponseTimesTotalRepository statsResponseTimesTotalRepository;


    @Autowired
    public JurorDashboardDataServiceImpl(
        final StatsResponseTimeRepository statsResponseTimeRepository,
        final StatsNotRespondedRepository statsNotRespondedRepository,
        final StatsNotRespondedTotalsRepsoitory statsNotRespondedTotalsRepsoitory,
        final StatsUnprocessedResponseRepository statsUnprocessedResponseRepository,
        final StatsWelshOnlineResponseRepository statsWelshOnlineResponseRepository,
        final StatsAutoProcessedRepository statsAutoProcessedRepository,
        final StatsThirdPartyOnlineResponseRepository statsThirdPartyOnlineResponseRepository,
        final SurveyResponseRepository surveyResponseRepository,
        final StatsResponseTimesTotalRepository statsResponseTimesTotalRepository) {
        Assert.notNull(statsResponseTimeRepository, "StatsResponseTimeRepository cannot be null");
        Assert.notNull(statsNotRespondedRepository, "StatsNotRespondedRepository cannot be null");
        Assert.notNull(statsNotRespondedTotalsRepsoitory, "StatsNotRespondedTotalsRepository cannot be null");
        Assert.notNull(statsUnprocessedResponseRepository, "StatsUnprocessedResponseRepository cannot be null");
        Assert.notNull(statsWelshOnlineResponseRepository, "StatsWelshOnlineResponseRepository cannot be null");
        Assert.notNull(statsAutoProcessedRepository, "StatsAutoProcessedRepository cannot be null");
        Assert.notNull(
            statsThirdPartyOnlineResponseRepository,
            "StatsThirdPartyOnlineResponseRepository cannot be null"
        );
        Assert.notNull(surveyResponseRepository, "SurveyResponseRepository cannot be null");
        Assert.notNull(statsResponseTimesTotalRepository, " StatsResponseTimesTotalRepository cannot be null");
        this.statsResponseTimeRepository = statsResponseTimeRepository;
        this.statsNotRespondedRepository = statsNotRespondedRepository;
        this.statsNotRespondedTotalsRepsoitory = statsNotRespondedTotalsRepsoitory;
        this.statsUnprocessedResponseRepository = statsUnprocessedResponseRepository;
        this.statsWelshOnlineResponseRepository = statsWelshOnlineResponseRepository;
        this.statsAutoProcessedRepository = statsAutoProcessedRepository;
        this.statsThirdPtyOnlnResponseRepository = statsThirdPartyOnlineResponseRepository;
        this.surveyResponseRepository = surveyResponseRepository;
        this.statsResponseTimesTotalRepository = statsResponseTimesTotalRepository;
    }


    @Override
    public List<StatsResponseTime> getResponsesOverTime(Date startDate, Date endDate) {
        log.debug("Called Service : JurorDashboardDataServiceImpl.getResponsesOverTime()... ");

        List<StatsResponseTime> responsesOverTime = null;

        responsesOverTime = Lists.newLinkedList(
            statsResponseTimeRepository.findBySummonsMonthBetween(startDate, endDate));
        log.debug("ResponseOverTimeContents Counts : {} ", responsesOverTime.size());

        return responsesOverTime;
    }


    @Override
    public List<StatsNotResponded> getNotResponded(Date startDate, Date endDate) {
        log.debug("Called Service : JurorDashboardDataServiceImpl.getNotResponded()... ");

        List<StatsNotResponded> notResponded;

        notResponded = Lists.newLinkedList(
            statsNotRespondedRepository.findBySummonsMonthBetween(startDate, endDate));
        log.debug("getNotResponded Counts : {} ", notResponded.size());

        return notResponded;

    }

    @Override

    public List<StatsNotRespondedTotals> getNotRespondedTotal() {
        log.debug("Called Service : JurorDashboardDataServiceImpl.getNotRespondedTotal()... ");


        List<StatsNotRespondedTotals> notRespondedTotal = Lists.newLinkedList(

            statsNotRespondedTotalsRepsoitory.findAll());
        log.debug("getNotRespondedTotal Counts : {} ", notRespondedTotal.size());

        return notRespondedTotal;
    }

    @Override
    public List<StatsUnprocessedResponse> getUnprocessedOnlineResponses() {
        log.debug("Called Service : JurorDashboardDataServiceImpl.getUnprocessedOnlineResponses()... ");


        List<StatsUnprocessedResponse> unProcessedOnlineResponse =
            Lists.newLinkedList(statsUnprocessedResponseRepository.findAll());
        log.debug("unProcessedOnlineResponse Counts : {} ", unProcessedOnlineResponse.size());

        return unProcessedOnlineResponse;
    }

    @Override
    public List<StatsWelshOnlineResponse> getWelshOnlineResponses(Date startDate, Date endDate) {
        log.debug("Called Service : JurorDashboardDataServiceImpl.getWelshOnlineResponses()... ");


        List<StatsWelshOnlineResponse> welshResponses = Lists.newLinkedList(
            statsWelshOnlineResponseRepository.findBySummonsMonthBetween(startDate, endDate));
        log.debug("welshResponses Counts : {} ", welshResponses.size());

        return welshResponses;
    }

    @Override
    public List<StatsAutoProcessed> getAutoOnlineResponses(Date startDate, Date endDate) {
        log.debug("Called Service : JurorDashboardDataServiceImpl.getAutoOnlineResponses()... ");


        List<StatsAutoProcessed> autoResponses = Lists.newLinkedList(
            statsAutoProcessedRepository.findByProcessedDateBetween(startDate, endDate));
        log.debug("autoResponses Counts : {} ", autoResponses.size());

        return autoResponses;
    }


    @Override
    public List<StatsThirdPartyOnlineResponse> getThirdPtyOnlineResponses(Date startDate, Date endDate) {
        log.debug("Called Service : JurorDashboardDataServiceImpl.getThirdPtyOnlineResponses()... ");

        List<StatsThirdPartyOnlineResponse> thirdPtyResponses = Lists.newLinkedList(
            statsThirdPtyOnlnResponseRepository.findBySummonsMonthBetween(startDate, endDate));
        log.debug("thirdPtyResponses Counts : {} ", thirdPtyResponses.size());

        return thirdPtyResponses;
    }

    @Override
    public List<SurveyResponse> getSurveyResponses(Date startDate, Date endDate) {
        log.debug("Called Service : JurorDashboardDataServiceImpl.getSurveyResponses()... ");


        List<SurveyResponse> surveyResponses = Lists.newLinkedList(
            surveyResponseRepository.findBySurveyResponseDateBetween(startDate, endDate));
        log.debug("surveyResponses Counts : ", surveyResponses.size());

        return surveyResponses;
    }


    @Override
    public List<StatsResponseTimesTotals> getAllStatsResponseTimesTotals() {
        log.debug("Called Service : JurorDashboardDataServiceImpl.getAllStatsResponseTimesTotals()....");


        List<StatsResponseTimesTotals> allResponseTimesTotals =
            Lists.newLinkedList(statsResponseTimesTotalRepository.findAll());
        log.debug("All Responses Times Totals : {} ", allResponseTimesTotals.size());

        return allResponseTimesTotals;
    }

    @Override
    public List<StatsResponseTimesTotals> getOnlineStatsResponseTimesTotals() {
        log.debug("Called Service : JurorDashboardDataServiceImpl.getOnlineStatsResponseTimesTotals....");


        List<StatsResponseTimesTotals> onlineResponseTimesTotals =
            Lists.newLinkedList(statsResponseTimesTotalRepository.findAll());
        log.debug("All Responses Times Totals : {} ", onlineResponseTimesTotals.size());

        return onlineResponseTimesTotals;
    }


}


