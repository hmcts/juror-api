package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.controller.response.BureauBacklogCountData;

public interface BureauBacklogCountService {

    long getBacklogNonUrgentCount();

    long getBacklogUrgentCount();

    long getBacklogAllRepliesCount();

    BureauBacklogCountData getBacklogResponseCount();


}
