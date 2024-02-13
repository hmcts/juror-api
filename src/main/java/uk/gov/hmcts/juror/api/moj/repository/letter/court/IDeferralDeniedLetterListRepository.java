package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.DeferralDeniedLetterList;

import java.util.List;

public interface IDeferralDeniedLetterListRepository {

    List<DeferralDeniedLetterList> findJurorsEligibleForDeferralDeniedLetter(CourtLetterSearchCriteria searchCriteria,
                                                                             String owner);
}
