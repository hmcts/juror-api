package uk.gov.hmcts.juror.api.moj.service.jurorer;

import uk.gov.hmcts.juror.api.moj.controller.jurorer.DeactiveLaRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateDeadlineRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.jurorer.UpdateDeadlineResponseDto;

public interface ErAdministrationService {

    void deactivateLa(DeactiveLaRequestDto requestDto);

    UpdateDeadlineResponseDto updateDeadline(UpdateDeadlineRequestDto request);
}
