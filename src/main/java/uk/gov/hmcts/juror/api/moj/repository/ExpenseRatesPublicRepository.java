package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.moj.domain.ExpenseRatesPublic;

@Repository
public interface ExpenseRatesPublicRepository extends ReadOnlyRepository<ExpenseRatesPublic, String>,
    QuerydslPredicateExecutor<ExpenseRatesPublic>{

}
