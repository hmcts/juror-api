package uk.gov.hmcts.juror.api.moj.service;


import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolHistoryListDto;
import uk.gov.hmcts.juror.api.moj.domain.HistoryCode;

public interface PoolHistoryService {

    PoolHistoryListDto getPoolHistoryListData(BureauJwtPayload payload, String poolNumber);

    void createPoolHistory(String poolNumber, HistoryCode historyCode, String otherInfo);
}
