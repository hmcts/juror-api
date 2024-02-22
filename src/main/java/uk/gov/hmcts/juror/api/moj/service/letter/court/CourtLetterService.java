package uk.gov.hmcts.juror.api.moj.service.letter.court;

import uk.gov.hmcts.juror.api.moj.controller.request.letter.court.CourtLetterListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.letter.court.LetterListResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;

public interface CourtLetterService {

    // String constants for response headings
    String JUROR_NUMBER = "Juror number";
    String FIRST_NAME = "First name";
    String LAST_NAME = "Last name";
    String POSTCODE = "Postcode";
    String STATUS = "Status";
    String REASON = "Reason";
    String DATE_PRINTED = "Date printed";
    String POOL_NUMBER = "Pool number";

    // String constants for data type descriptions
    String STRING = "string";
    String DATE = "date";
    String HIDDEN = "hidden";

    LetterListResponseDto getEligibleList(CourtLetterListRequestDto courtLetterListRequestDto);

    default CourtLetterSearchCriteria buildSearchCriteria(CourtLetterListRequestDto courtLetterListRequestDto) {
        return CourtLetterSearchCriteria.builder()
            .jurorNumber(courtLetterListRequestDto.getJurorNumber())
            .jurorName(courtLetterListRequestDto.getJurorName())
            .postcode(courtLetterListRequestDto.getJurorPostcode())
            .poolNumber(courtLetterListRequestDto.getPoolNumber())
            .includePrinted(courtLetterListRequestDto.isIncludePrinted())
            .build();
    }
}
