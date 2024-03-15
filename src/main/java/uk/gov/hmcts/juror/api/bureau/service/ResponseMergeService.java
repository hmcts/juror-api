package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;

/**
 * Operations for merging juror responses back into the legacy juror database.
 */
public interface ResponseMergeService {
    /**
     * Save a juror digital response back into the existing juror system.
     *
     * @param digitalResponse   The response entity
     * @param auditorUsername The user performing the merge
     */
    void mergeResponse(DigitalResponse digitalResponse, String auditorUsername);
  //  void mergeResponse (JurorResponse jurorResponse, String auditorUsername);
}
