package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.controller.request.messages.BureauEmailRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.messages.BureauEmailResponseDto;

public interface BureauMessagingService {

    BureauEmailResponseDto sendEmailsToJurors(BureauEmailRequestDto request);
}
