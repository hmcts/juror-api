package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link JurorResponseAudit}.
 */
@Repository
public interface JurorResponseAuditRepository extends CrudRepository<JurorResponseAudit, JurorResponseAuditKey> {
}
