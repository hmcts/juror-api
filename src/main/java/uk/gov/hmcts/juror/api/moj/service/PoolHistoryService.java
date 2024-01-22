package uk.gov.hmcts.juror.api.moj.service;


import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolHistoryListDto;

public interface PoolHistoryService {

    PoolHistoryListDto getPoolHistoryListData(BureauJWTPayload payload, String poolNumber);

}
