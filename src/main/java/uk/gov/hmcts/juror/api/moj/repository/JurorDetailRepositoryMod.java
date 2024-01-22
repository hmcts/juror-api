package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.moj.domain.ModJurorDetail;

/**
 * Repository for {@link ModJurorDetail} entity.
 */
@Repository
public interface JurorDetailRepositoryMod extends ReadOnlyRepository<ModJurorDetail, String> {

}
