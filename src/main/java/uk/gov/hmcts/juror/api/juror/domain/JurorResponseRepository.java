package uk.gov.hmcts.juror.api.juror.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link JurorResponse}.
 */
@Repository
public interface JurorResponseRepository extends CrudRepository<JurorResponse, String>,
    QuerydslPredicateExecutor<JurorResponse> {
    JurorResponse findByJurorNumber(String jurorNumber);
}
