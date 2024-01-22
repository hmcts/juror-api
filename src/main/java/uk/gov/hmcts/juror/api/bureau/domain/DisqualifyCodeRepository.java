package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.stereotype.Repository;

/**
 * Disqualification Codes domain.
 */
@Repository
public interface DisqualifyCodeRepository extends ReadOnlyRepository<DisqualifyCodeEntity, String> {
}
