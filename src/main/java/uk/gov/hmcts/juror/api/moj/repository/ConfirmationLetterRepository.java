package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.letter.ConfirmationLetter;

@Repository
public interface ConfirmationLetterRepository extends LetterRepository<ConfirmationLetter> {

}
