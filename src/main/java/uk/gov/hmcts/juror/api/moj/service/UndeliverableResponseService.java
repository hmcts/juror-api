package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;

public interface UndeliverableResponseService {

    /**
     * Mark a Juror as Undeliverable with specific code.
     */
    void markAsUndeliverable(BureauJwtPayload payload, String jurorNumber);
}
