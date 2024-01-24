package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;

/**
 * Operations for updating the status of a Juror's response by a Bureau officer.
 */
public interface ResponseStatusUpdateService {
    /**
     * Update the processing status of a Juror response within Juror Digital.
     *
     * @param jurorNumber     The juror number of the response
     * @param status          The processing status to change to
     * @param version         Optimistic locking version of the record to update (maintained across requests by UI)
     * @param auditorUsername Username performing the update
     */
    void updateJurorResponseStatus(String jurorNumber, ProcessingStatus status, Integer version,
                                   String auditorUsername);
}
