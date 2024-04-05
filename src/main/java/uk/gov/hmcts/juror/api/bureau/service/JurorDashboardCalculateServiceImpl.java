package uk.gov.hmcts.juror.api.bureau.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.juror.api.bureau.domain.StatsAutoProcessed;
import uk.gov.hmcts.juror.api.bureau.domain.StatsNotResponded;
import uk.gov.hmcts.juror.api.bureau.domain.StatsNotRespondedTotals;
import uk.gov.hmcts.juror.api.bureau.domain.StatsResponseTime;
import uk.gov.hmcts.juror.api.bureau.domain.StatsResponseTimesTotals;
import uk.gov.hmcts.juror.api.bureau.domain.StatsThirdPartyOnlineResponse;
import uk.gov.hmcts.juror.api.bureau.domain.StatsUnprocessedResponse;
import uk.gov.hmcts.juror.api.bureau.domain.StatsWelshOnlineResponse;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.summingInt;


@Slf4j
@Service
public class JurorDashboardCalculateServiceImpl implements JurorDashboardCalculateService {

    @Override
    public Integer allResponsesTotal(List<StatsResponseTimesTotals> allStatsResponseTimesTotals) {
        return allStatsResponseTimesTotals.stream()
            .mapToInt(StatsResponseTimesTotals::getAllResponsesTotal)
            .sum();
    }

    @Override
    public Integer onlineResponsesTotal(List<StatsResponseTimesTotals> onlineStatsResponseTimesTotals) {
        return onlineStatsResponseTimesTotals.stream()
            .mapToInt(StatsResponseTimesTotals::getOnlineResponsesTotal)
            .sum();
    }

    @Override
    public Integer totalNoOfResponses(List<StatsResponseTime> responsesOverTime) {
        return responsesOverTime.stream()
            .mapToInt(StatsResponseTime::getResponseCount)
            .sum();
    }

    @Override
    public Integer totalNoOfOnlineResponsesTotal(List<StatsResponseTime> onlineResponsesOverTimeTotal) {
        return onlineResponsesOverTimeTotal.stream()
            .mapToInt(StatsResponseTime::getResponseCount)
            .sum();
    }

    @Override
    public Integer totalNoOfResponsesTotal(List<StatsResponseTime> responsesOverTimeTotal) {
        return responsesOverTimeTotal.stream()
            .mapToInt(StatsResponseTime::getResponseCount)
            .sum();
    }

    @Override
    public Integer totalNoOfNotResponded(List<StatsNotResponded> notResponded) {
        return notResponded.stream()
            .mapToInt(StatsNotResponded::getNotRespondedCount)
            .sum();
    }

    @Override
    //  public Integer totalNoOfNotRespondedTotal(List<StatsNotResponded> notRespondedTotal) {
    public Integer totalNoOfNotRespondedTotal(List<StatsNotRespondedTotals> notRespondedTotal) {

        return notRespondedTotal.stream()
            //         .mapToInt(StatsNotResponded::getNonResponseCount)
            .mapToInt(StatsNotRespondedTotals::getNotRespondedTotals)
            .sum();
    }

    @Override
    public Integer totalNoOfSummoned(Integer responses, Integer nonresponses) {
        return responses + nonresponses;
    }

    @Override
    public Integer totalNoOfUnprocessed(List<StatsUnprocessedResponse> unprocessedList) {
        return unprocessedList.stream()
            .mapToInt(StatsUnprocessedResponse::getUnprocessedCount)
            .sum();
    }

    @Override
    public Map<String, Map<String, Integer>> reponsesByMethod(List<StatsResponseTime> responsesOverTime) {
        Map<String, Map<String, Integer>> responsesCounts = responsesOverTime.stream()
            .collect(groupingBy(
                StatsResponseTime::getResponseMethod,
                groupingBy(
                    StatsResponseTime::getResponsePeriod,
                    summingInt(StatsResponseTime::getResponseCount)
                )
            ));
        log.info("responsesCounts : {}", responsesCounts);
        return responsesCounts;
    }

    @Override
    public Integer totalNoofWelshOnlineResponses(List<StatsWelshOnlineResponse> welshResponses) {
        return welshResponses.stream()
            .mapToInt(StatsWelshOnlineResponse::getWelshResponseCount)
            .sum();
    }

    @Override
    public Integer totalNoofAutoOnlineResponses(List<StatsAutoProcessed> autoProcessedResponses) {
        return autoProcessedResponses.stream()
            .mapToInt(StatsAutoProcessed::getProcessedCount)
            .sum();
    }

    @Override
    public Integer totalThirdPtyOnlineResponses(List<StatsThirdPartyOnlineResponse> thirdPtyResponses) {
        return thirdPtyResponses.stream()
            .mapToInt(StatsThirdPartyOnlineResponse::getThirdPartyResponseCount)
            .sum();
    }

    @Override
    public Float percentage(Float proportion, Float whole) {
        if (proportion == 0 || whole == 0) {
            return 0f;
        }
        return round(proportion / whole * 100, 1);
    }

    @Override
    public Float percentage(Float proportion, Float whole, int precision) {
        if (proportion == 0 || whole == 0) {
            return 0f;
        }
        return round(proportion / whole * 100, precision);
    }

    private static float round(float value, int places) {
        if (places < 0) {
            throw new IllegalArgumentException();
        }

        DecimalFormat df = new DecimalFormat("##0.00");
        BigDecimal bd = new BigDecimal(df.format(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();

    }
}
