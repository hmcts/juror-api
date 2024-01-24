package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;

/**
 * Operations for merging juror responses back into the legacy juror database.
 */
public interface ResponseMergeService {
    /**
     * Save a juror digital response back into the existing juror system.
     *
     * @param jurorResponse   The response entity
     * @param auditorUsername The user performing the merge
     */
    void mergeResponse(JurorResponse jurorResponse, String auditorUsername);
}
