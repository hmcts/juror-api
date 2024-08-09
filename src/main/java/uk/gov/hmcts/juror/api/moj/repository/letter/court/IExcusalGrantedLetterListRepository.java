package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.ExcusalGrantedLetterList;

import java.util.List;

public interface IExcusalGrantedLetterListRepository {

    List<ExcusalGrantedLetterList> findJurorsEligibleForExcusalGrantedLetter(CourtLetterSearchCriteria searchCriteria);
}
