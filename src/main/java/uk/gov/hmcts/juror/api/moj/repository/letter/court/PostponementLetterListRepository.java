package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.PostponedLetterList;

import java.util.List;

public interface PostponementLetterListRepository {
    List<PostponedLetterList> findJurorsEligibleForPostponementLetter(CourtLetterSearchCriteria searchCriteria);
}
