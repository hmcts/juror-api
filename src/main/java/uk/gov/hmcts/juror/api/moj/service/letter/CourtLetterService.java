package uk.gov.hmcts.juror.api.moj.service.letter;

import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.CourtLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.LetterListResponseDto;

public interface CourtLetterService {

    LetterListResponseDto getEligibleList(CourtLetterListRequestDto courtLetterListRequestDto);

}
