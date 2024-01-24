package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * Repository for finding {@link TSpecial} entities.
 */
@Repository
public interface TSpecialRepository extends ReadOnlyRepository<TSpecial, String>, QuerydslPredicateExecutor<TSpecial> {

    TSpecial findByCode(String code);
}
