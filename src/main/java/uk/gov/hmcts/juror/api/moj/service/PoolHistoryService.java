package uk.gov.hmcts.juror.api.moj.service;


import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolHistoryListDto;

public interface PoolHistoryService {

    PoolHistoryListDto getPoolHistoryListData(BureauJwtPayload payload, String poolNumber);

}
