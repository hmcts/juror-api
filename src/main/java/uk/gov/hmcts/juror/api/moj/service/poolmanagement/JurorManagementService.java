package uk.gov.hmcts.juror.api.moj.service.poolmanagement;

import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorManagementRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorManagementResponseDto;

public interface JurorManagementService {

    int transferPoolMembers(BureauJWTPayload payload, JurorManagementRequestDto requestDto);

    JurorManagementResponseDto validatePoolMembers(BureauJWTPayload payload,
                                                   JurorManagementRequestDto requestDto);

    int reassignJurors(BureauJWTPayload payload, JurorManagementRequestDto requestDto);

}
