package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.controller.response.juror.JurorHistoryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;

public interface HistoryTemplateService {
    JurorHistoryResponseDto.JurorHistoryEntryDto toJurorHistoryEntryDto(JurorHistory item);
}
