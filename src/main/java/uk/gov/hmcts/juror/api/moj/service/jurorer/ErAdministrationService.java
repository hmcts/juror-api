package uk.gov.hmcts.juror.api.moj.service.jurorer;

import uk.gov.hmcts.juror.api.moj.controller.jurorer.ActiveLaRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.DeactiveLaRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.MarkAsDeliveredRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateDeadlineRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateDeadlineResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateEmailRequestSentDto;

public interface ErAdministrationService {

    void deactivateLa(DeactiveLaRequestDto requestDto);

    void activateLa(ActiveLaRequestDto requestDto);

    UpdateDeadlineResponseDto updateDeadline(UpdateDeadlineRequestDto request);

    void markAsDelivered(MarkAsDeliveredRequestDto request);
}
