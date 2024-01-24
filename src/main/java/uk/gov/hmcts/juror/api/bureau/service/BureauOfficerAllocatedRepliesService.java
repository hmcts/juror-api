package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.controller.response.BureauOfficerAllocatedResponses;


public interface BureauOfficerAllocatedRepliesService {

    /**
     * Get allocated  data.
     *
     * @return data related to all staff
     */
    BureauOfficerAllocatedResponses getBackLogData();
}
