package uk.gov.hmcts.juror.api.moj.service.jurorer;

import uk.gov.hmcts.juror.api.moj.controller.jurorer.DeactiveLaRequestDto;

public interface ErAdministrationService {
    void deactivateLa(DeactiveLaRequestDto requestDto);
}
