package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.moj.client.contracts.SchedulerServiceClient;

public interface ScheduledService {

    SchedulerServiceClient.Result process();
}
