package uk.gov.hmcts.juror.api.moj.service.summonsmanagement;

import uk.gov.hmcts.juror.api.moj.controller.request.summonsmanagement.JurorResponseRetrieveRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.summonsmanagement.JurorResponseRetrieveResponseDto;

public interface JurorResponseRetrieveService {
    JurorResponseRetrieveResponseDto retrieveJurorResponse(JurorResponseRetrieveRequestDto request);
}
