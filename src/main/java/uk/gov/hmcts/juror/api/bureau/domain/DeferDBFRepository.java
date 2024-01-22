package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeferDBFRepository extends CrudRepository<DeferDBF, String>, QuerydslPredicateExecutor<DeferDBF> {
}
