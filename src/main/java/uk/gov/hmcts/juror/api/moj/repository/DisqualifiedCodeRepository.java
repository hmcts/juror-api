package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.moj.domain.DisqualifiedCode;

/**
 * Disqualified Code repository interface.
 */
@Repository
public interface DisqualifiedCodeRepository extends ReadOnlyRepository<DisqualifiedCode, String> {

}
