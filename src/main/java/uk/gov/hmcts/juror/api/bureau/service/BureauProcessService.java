package uk.gov.hmcts.juror.api.bureau.service;


import uk.gov.hmcts.juror.api.moj.client.contracts.SchedulerServiceClient;

/**
 * Service to process either via batch or the bureau application.
 */
public interface BureauProcessService extends ScheduledService {
    /**
     * Process a particular batch process.
     */
    @Override
    SchedulerServiceClient.Result process();
}
