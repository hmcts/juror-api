package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.moj.domain.system.SystemParameterMod;

@Repository
public interface SystemParameterRepositoryMod extends ReadOnlyRepository<SystemParameterMod, Integer> {
}
