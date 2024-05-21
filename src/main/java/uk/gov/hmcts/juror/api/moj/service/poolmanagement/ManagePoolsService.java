package uk.gov.hmcts.juror.api.moj.service.poolmanagement;

import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.response.SummoningProgressResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.poolmanagement.AvailablePoolsInCourtLocationDto;

public interface ManagePoolsService {

    AvailablePoolsInCourtLocationDto findAvailablePools(String locCode, BureauJwtPayload payload, boolean isReassign);

    SummoningProgressResponseDto getPoolMonitoringStats(BureauJwtPayload payload, String courtLocationCode,
                                                        String poolType);

    AvailablePoolsInCourtLocationDto findAvailablePoolsCourtOwned(String locCode, BureauJwtPayload payload);
}
