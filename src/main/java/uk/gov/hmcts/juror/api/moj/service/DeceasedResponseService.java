package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.MarkAsDeceasedDto;

public interface DeceasedResponseService {

    /**
     * Mark a Juror as deceased with specific code.
     */
    void markAsDeceased(BureauJWTPayload payload, MarkAsDeceasedDto markAsDeceasedDto);

}
