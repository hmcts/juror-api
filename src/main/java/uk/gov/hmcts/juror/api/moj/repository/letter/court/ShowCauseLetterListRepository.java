package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.ShowCauseLetterList;

import java.util.List;

public interface ShowCauseLetterListRepository {
    List<ShowCauseLetterList> findJurorsEligibleForShowCauseLetter(CourtLetterSearchCriteria searchCriteria, String owner);
}
