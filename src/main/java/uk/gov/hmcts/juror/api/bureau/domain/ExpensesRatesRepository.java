package uk.gov.hmcts.juror.api.bureau.domain;

import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

/**
 * Expenses Rates domain.
 */
@Repository
public interface ExpensesRatesRepository extends ReadOnlyRepository<ExpensesRates, String>,
    QuerydslPredicateExecutor<ExpensesRates> {
}
