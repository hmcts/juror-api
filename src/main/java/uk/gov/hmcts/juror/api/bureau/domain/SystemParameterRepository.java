package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link SystemParameter} entities.
 */
@Repository
@Deprecated(forRemoval = true)
public interface SystemParameterRepository extends ReadOnlyRepository<SystemParameter, Integer>,
    QuerydslPredicateExecutor<SystemParameter> {
}
