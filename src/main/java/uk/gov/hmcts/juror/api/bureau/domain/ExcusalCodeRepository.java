package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.ExcusalCode;

/**
 * Excusal Codes domain.
 */
@Repository
public interface ExcusalCodeRepository extends ReadOnlyRepository<ExcusalCode, String> {

}
