package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for {@link BureauJurorDetail} entities.
 */
@Repository
public interface BureauJurorDetailRepository extends CrudRepository<BureauJurorDetail, String>,
    QuerydslPredicateExecutor<BureauJurorDetail> {

}
