package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.hmcts.juror.api.moj.domain.ExpenseRates;

public interface ExpenseRatesRepository extends JpaRepository<ExpenseRates, Long> {

    @Query("SELECT er FROM ExpenseRates er ORDER BY er.id DESC LIMIT 1")
    ExpenseRates getCurrentRates();
}
