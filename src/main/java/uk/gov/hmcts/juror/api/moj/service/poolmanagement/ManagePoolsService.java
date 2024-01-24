package uk.gov.hmcts.juror.api.moj.service.poolmanagement;

import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.response.SummoningProgressResponseDTO;
import uk.gov.hmcts.juror.api.moj.controller.response.poolmanagement.AvailablePoolsInCourtLocationDto;

public interface ManagePoolsService {

    AvailablePoolsInCourtLocationDto findAvailablePools(String locCode, BureauJWTPayload payload);

    SummoningProgressResponseDTO getPoolMonitoringStats(BureauJWTPayload payload, String courtLocationCode,
                                                        String poolType);
}
