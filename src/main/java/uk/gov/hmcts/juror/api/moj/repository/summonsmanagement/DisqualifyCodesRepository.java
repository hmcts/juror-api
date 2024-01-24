package uk.gov.hmcts.juror.api.moj.repository.summonsmanagement;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.DisqualifyCodeEntity;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;

/**
 * Repository to retrieve list of allowable disqualification codes.
 */
@Repository
public interface DisqualifyCodesRepository extends ReadOnlyRepository<DisqualifyCodeEntity, String> {
}
