package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.letter.ExcusalDeniedLetterMod;

@Repository
public interface ExcuseDeniedLetterRepository extends LetterRepository<ExcusalDeniedLetterMod> {

}
