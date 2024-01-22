package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolEditRequestDto;

public interface EditPoolService {

    void editPool(BureauJWTPayload payload, PoolEditRequestDto poolEditRequestDto);

}
