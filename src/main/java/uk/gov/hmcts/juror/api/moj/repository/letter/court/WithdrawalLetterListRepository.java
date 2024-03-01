package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.LetterListId;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.WithdrawalLetterList;

@Repository
public interface WithdrawalLetterListRepository extends IWithdrawalLetterListRepository,
    ReadOnlyRepository<WithdrawalLetterList, LetterListId> {

}
