package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;

/**
 * Operations for updating the status of a Juror's response by a Bureau officer.
 */
public interface SummonsReplyStatusUpdateService {
    /**
     * Update the processing status of a Juror response within Juror Digital.
     *
     * @param jurorNumber The juror number of the response
     * @param status      The processing status to change to
     * @param payload     Bureau authentication Json Web Token payload.
     */
    void updateJurorResponseStatus(String jurorNumber, ProcessingStatus status, BureauJwtPayload payload);

    void updateDigitalJurorResponseStatus(String jurorNumber, ProcessingStatus status,
                                          BureauJwtPayload payload);

}
