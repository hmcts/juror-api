package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.letter.DisqualificationLetterMod;

@Repository
public interface DisqualifyLetterModRepository extends LetterRepository<DisqualificationLetterMod> {

}
