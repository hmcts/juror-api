package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import uk.gov.hmcts.juror.api.moj.domain.letter.CourtLetterSearchCriteria;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.WithdrawalLetterList;

import java.util.List;

public interface IWithdrawalLetterListRepository {

    List<WithdrawalLetterList> findJurorsEligibleForWithdrawalLetter(CourtLetterSearchCriteria searchCriteria,
                                                                               String owner);
}
