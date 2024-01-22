package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.repository.CrudRepository;

/**
 * Repository for {@link StaffJurorResponseAudit}.
 */
public interface StaffJurorResponseAuditRepository extends CrudRepository<StaffJurorResponseAudit,
    StaffJurorResponseAuditKey> {
}
