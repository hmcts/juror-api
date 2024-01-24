package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.stereotype.Repository;

/**
 * Excusal Codes domain.
 */
@Repository
public interface ExcusalCodeRepository extends ReadOnlyRepository<ExcusalCodeEntity, String> {
}
