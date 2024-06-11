package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link ChangeLog} entities.
 */
@Repository
@Deprecated(forRemoval = true)
public interface ChangeLogRepository extends CrudRepository<ChangeLog, Long>, QuerydslPredicateExecutor<ChangeLog> {
}
