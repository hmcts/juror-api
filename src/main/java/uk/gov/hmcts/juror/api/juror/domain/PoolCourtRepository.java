package uk.gov.hmcts.juror.api.juror.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PoolCourtRepository extends CrudRepository<PoolCourt, String>, QuerydslPredicateExecutor<PoolCourt> {
    PoolCourt findByJurorNumber(String jurorNumber);
}
