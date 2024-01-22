package uk.gov.hmcts.juror.api.juror.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PoolRepository extends CrudRepository<Pool, String>, QuerydslPredicateExecutor<Pool> {

    Pool findByJurorNumber(String jurorNumber);
}
