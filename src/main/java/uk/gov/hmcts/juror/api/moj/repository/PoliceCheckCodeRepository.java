package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCode;

/**
 * Police check repository interface.
 */
@Repository
public interface PoliceCheckCodeRepository extends ReadOnlyRepository<PoliceCode, String> {

}
