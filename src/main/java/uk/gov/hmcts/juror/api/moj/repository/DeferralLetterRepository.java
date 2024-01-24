package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.letter.DeferralLetter;

@Repository
public interface DeferralLetterRepository extends LetterRepository<DeferralLetter> {

}
