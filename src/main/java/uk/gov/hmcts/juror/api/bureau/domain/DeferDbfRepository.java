package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
@Deprecated(forRemoval = true)
public interface DeferDbfRepository extends CrudRepository<DeferDbf, String>, QuerydslPredicateExecutor<DeferDbf> {
}
