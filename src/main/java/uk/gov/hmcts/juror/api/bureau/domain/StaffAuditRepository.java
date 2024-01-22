package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link StaffAudit}.
 */
@Repository
public interface StaffAuditRepository extends CrudRepository<StaffAudit, StaffAuditKey>,
    QuerydslPredicateExecutor<StaffAudit> {
}
