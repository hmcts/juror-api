package uk.gov.hmcts.juror.api.moj.service.poolmanagement;

import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorManagementRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorManagementResponseDto;

public interface JurorManagementService {

    int transferPoolMembers(BureauJwtPayload payload, JurorManagementRequestDto requestDto);

    JurorManagementResponseDto validatePoolMembers(BureauJwtPayload payload,
                                                   JurorManagementRequestDto requestDto);

    int reassignJurors(BureauJwtPayload payload, JurorManagementRequestDto requestDto);

}
