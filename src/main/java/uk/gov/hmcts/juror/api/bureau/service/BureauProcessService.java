package uk.gov.hmcts.juror.api.bureau.service;


import uk.gov.hmcts.juror.api.moj.client.contracts.SchedulerServiceClient;

import java.time.format.DateTimeFormatter;

/**
 * Service to process either via batch or the bureau application.
 */
public interface BureauProcessService extends ScheduledService {

    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Process a particular batch process.
     */
    @Override
    SchedulerServiceClient.Result process();
}
