package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.ExcusalCode;

/**
 * Excusal Codes domain.
 */
@Repository
@Deprecated(forRemoval = true)
public interface ExcusalCodeRepository extends ReadOnlyRepository<ExcusalCode, String> {

}
