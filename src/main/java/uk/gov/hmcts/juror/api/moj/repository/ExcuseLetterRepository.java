package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.letter.ExcusalLetterMod;

@Repository
public interface ExcuseLetterRepository extends LetterRepository<ExcusalLetterMod> {

}
