package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;

public interface DeletePoolService {

    void deletePool(BureauJwtPayload payload, String poolNumber);

}
