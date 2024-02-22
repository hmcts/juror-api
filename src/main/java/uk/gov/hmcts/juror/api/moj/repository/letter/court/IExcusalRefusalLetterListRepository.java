package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.ExcusalRefusedLetterList;

import java.util.List;

public interface IExcusalRefusalLetterListRepository {
    List<ExcusalRefusedLetterList> findJurorsEligibleForExcusalRefusalLetter(CourtLetterSearchCriteria searchCriteria,
                                                                             String owner);
}
