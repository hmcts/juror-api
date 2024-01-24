package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.moj.domain.PoolType;

@Repository
public interface PoolTypeRepository extends ReadOnlyRepository<PoolType, String> {

}
