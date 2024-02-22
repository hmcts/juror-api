package uk.gov.hmcts.juror.api.moj.service.letter.court;

import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.PrintLettersRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.PrintLetterDataResponseDto;

import java.util.List;

public interface CourtLetterPrintService {
    List<PrintLetterDataResponseDto> getPrintLettersData(PrintLettersRequestDto printLettersRequestDto, String login);
}
