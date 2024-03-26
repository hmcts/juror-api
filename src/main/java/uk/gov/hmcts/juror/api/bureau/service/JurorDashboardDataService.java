package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.domain.StatsAutoProcessed;
import uk.gov.hmcts.juror.api.bureau.domain.StatsNotResponded;
import uk.gov.hmcts.juror.api.bureau.domain.StatsNotRespondedTotals;
import uk.gov.hmcts.juror.api.bureau.domain.StatsResponseTime;
import uk.gov.hmcts.juror.api.bureau.domain.StatsResponseTimesTotals;
import uk.gov.hmcts.juror.api.bureau.domain.StatsThirdPartyOnlineResponse;
import uk.gov.hmcts.juror.api.bureau.domain.StatsUnprocessedResponse;
import uk.gov.hmcts.juror.api.bureau.domain.StatsWelshOnlineResponse;
import uk.gov.hmcts.juror.api.bureau.domain.SurveyResponse;

import java.util.Date;
import java.util.List;

public interface JurorDashboardDataService {

    /**
     * Get the records from the StatsResponseTime.
     *
     
     * @return List
     */
    List<StatsResponseTime> getResponsesOverTime(Date startDate, Date endDate);


    /**
     * Get the records from the StatsNotResponded.
     *
     
     * @return List
     */
    List<StatsNotResponded> getNotResponded(Date startDate, Date endDate);

    /**
     * Get the records from the StatsNotResponded.
     *
     * <p>no date parameter just total
     *
     * @return List
     */
    //  List<StatsNotResponded> getNotRespondedTotal();
    List<StatsNotRespondedTotals> getNotRespondedTotal();

    /**
     * Get the records from the StatsUnprocessed.
     * No date period in this.  Provides that data a snapshot in time.
     *
     * @return List
     */
    List<StatsUnprocessedResponse> getUnprocessedOnlineResponses();

    /**
     * Get the records from the StatsWelshOnlineResponse.
     *
     
     * @return List
     */
    List<StatsWelshOnlineResponse> getWelshOnlineResponses(Date startDate, Date endDate);

    /**
     * Get the records from the StatsAutoProcessed.
     *
     
     * @return List
     */
    List<StatsAutoProcessed> getAutoOnlineResponses(Date startDate, Date endDate);

    /**
     * Get the records from the StatsThirdPartyOnlineResponse.
     *
     
     * @return List
     */
    List<StatsThirdPartyOnlineResponse> getThirdPtyOnlineResponses(Date startDate, Date endDate);

    /**
     * Get the records from the SurveyResponse.
     *
     
     * @return List
     */
    List<SurveyResponse> getSurveyResponses(Date startDate, Date endDate);

    /**
     * Get the Total Count from the STATS_RESPONSE_TIMES_TOTALS.
     *
     * @return List
     */
    List<StatsResponseTimesTotals> getAllStatsResponseTimesTotals();

    /**
     * Get the Total Count from the STATS_RESPONSE_TIMES_TOTALS.ONLINE
     *
     * @return List
     */
    List<StatsResponseTimesTotals> getOnlineStatsResponseTimesTotals();


}


