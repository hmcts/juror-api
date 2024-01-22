package uk.gov.hmcts.juror.api.moj.repository.letter;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.letter.PostponementLetter;
import uk.gov.hmcts.juror.api.moj.repository.LetterRepository;

@Repository
public interface PostponementLetterRepository extends LetterRepository<PostponementLetter> {

}
