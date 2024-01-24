package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;

public interface DeletePoolService {

    void deletePool(BureauJWTPayload payload, String poolNumber);

}
