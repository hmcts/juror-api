package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;

/**
 * Operations for merging juror responses back into the legacy juror database.
 */
public interface SummonsReplyMergeService {

    /**
     * Save the paper response back into the existing juror system.
     *
     * @param paperResponse   The paper response entity
     * @param auditorUsername The user performing the merge
     */
    void mergePaperResponse(PaperResponse paperResponse, String auditorUsername);

    /**
     * Save the digital response back into the existing juror system.
     *
     * @param digitalResponse The digital response entity
     * @param auditorUsername The user performing the merge
     */
    void mergeDigitalResponse(DigitalResponse digitalResponse, String auditorUsername);
}

