package uk.gov.hmcts.juror.api.bureau.service;


/**
 * Service to process either via batch or the bureau application.
 */
public interface BureauProcessService extends ScheduledService {
    /**
     * Process a particular batch process
     *
     * @param
     * @return
     */
    void process();
}
