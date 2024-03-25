package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.domain.StatsAutoProcessed;
import uk.gov.hmcts.juror.api.bureau.domain.StatsNotResponded;
import uk.gov.hmcts.juror.api.bureau.domain.StatsNotRespondedTotals;
import uk.gov.hmcts.juror.api.bureau.domain.StatsResponseTime;
import uk.gov.hmcts.juror.api.bureau.domain.StatsResponseTimesTotals;
import uk.gov.hmcts.juror.api.bureau.domain.StatsThirdPartyOnlineResponse;
import uk.gov.hmcts.juror.api.bureau.domain.StatsUnprocessedResponse;
import uk.gov.hmcts.juror.api.bureau.domain.StatsWelshOnlineResponse;

import java.util.List;
import java.util.Map;

public interface JurorDashboardCalculateService {

    /**
     * Calculates the total number of responses.
     *
     * @return Count of responses.
     */
    Integer totalNoOfResponses(List<StatsResponseTime> responsesOverTime);


    /**
     * Calculates the total number of online  responses.
     *
     * @return Count of responses.
     */
    Integer totalNoOfOnlineResponsesTotal(List<StatsResponseTime> onlineResponsesOverTimeTotal);

    /**
     * Calculates the total number of responses.
     *
     * @return Count of responses.
     */
    Integer totalNoOfResponsesTotal(List<StatsResponseTime> responsesOverTimeTotal);

    /**
     * Calculates the total not responded to summons.
     *
     * @return Count of not responded summons.
     */
    Integer totalNoOfNotResponded(List<StatsNotResponded> notResponded);

    /**
     * Calculates the total not responded to summons.
     *
     * @return Count of not responded summons total.
     */
    //  Integer totalNoOfNotRespondedTotal(List<StatsNotResponded> notRespondedTotal);
    Integer totalNoOfNotRespondedTotal(List<StatsNotRespondedTotals> notRespondedTotal);

    /**
     * return responses + nonResponses.
     * @return responses + nonResponses
     */
    Integer totalNoOfSummoned(Integer responses, Integer nonResponses);

    /**
     * Calculates the number of unprocessed online responses at this point in time.
     *
     * @return count
     */
    Integer totalNoOfUnprocessed(List<StatsUnprocessedResponse> unprocessedList);

    /**
     * Retrieves the responses count breakdown by response method and over time.
     *
     * @return Map
     */
    Map<String, Map<String, Integer>> reponsesByMethod(List<StatsResponseTime> responsesOverTime);

    /**
     * Calculates the percentage of the proportion value in relation to the whole value
     * Returns a float rounded to 1 decimal place.
     *
     
     * @return Float
     */
    Float percentage(Float proportion, Float whole);

    /**
     * Calculates the percentage of the proportion value in relation to the whole value.
     * Returns a float rounded to the given precison number of places.
     *
     
     * @return Float
     */
    Float percentage(Float proportion, Float whole, int precision);

    /**
     * Calculates the total no of welsh responses.
     *
     * @return count
     */
    Integer totalNoofWelshOnlineResponses(List<StatsWelshOnlineResponse> welshResponses);

    /**
     * Calculates the total no of auto processed responses.
     *
     * @return count
     */
    Integer totalNoofAutoOnlineResponses(List<StatsAutoProcessed> autoProcessedResponses);

    /**
     * Calculates the total no of third party responses.
     *
     * @return count
     */
    Integer totalThirdPtyOnlineResponses(List<StatsThirdPartyOnlineResponse> thirdPtyResponses);

    /**
     * Calculates the total number of responses.
     *
     * @return Total responses.
     */
    Integer allResponsesTotal(List<StatsResponseTimesTotals> allresponsesOverTimeTotal);

    /**
     * Calculates the total number of responses.
     *
     * @return Total online responses.
     */
    Integer onlineResponsesTotal(List<StatsResponseTimesTotals> onlineResponsesOverTimeTotal);


}
