package uk.gov.hmcts.juror.api.moj.repository.letter.court;

import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.ExcusalRefusedLetterList;
import uk.gov.hmcts.juror.api.moj.domain.letter.court.LetterListId;

public interface ExcusalRefusalLetterListRepository extends IExcusalRefusalLetterListRepository,
    ReadOnlyRepository<ExcusalRefusedLetterList, LetterListId> {
}
