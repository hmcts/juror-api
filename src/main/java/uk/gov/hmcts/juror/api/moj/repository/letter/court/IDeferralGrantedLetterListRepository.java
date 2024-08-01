package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.DeferralGrantedLetterList;

import java.util.List;

public interface IDeferralGrantedLetterListRepository {

    List<DeferralGrantedLetterList> findJurorsEligibleForDeferralGrantedLetter(
        CourtLetterSearchCriteria searchCriteria);
}
