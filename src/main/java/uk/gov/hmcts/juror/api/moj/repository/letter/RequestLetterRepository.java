package uk.gov.hmcts.juror.api.moj.repository.letter;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.letter.RequestLetter;
import uk.gov.hmcts.juror.api.moj.repository.LetterRepository;

@Repository
public interface RequestLetterRepository extends LetterRepository<RequestLetter> {
}
