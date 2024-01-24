package uk.gov.hmcts.juror.api.moj.repository.letter;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.letter.CertLetter;
import uk.gov.hmcts.juror.api.moj.repository.LetterRepository;

@Repository
public interface CertLetterRepository extends LetterRepository<CertLetter> {

}
