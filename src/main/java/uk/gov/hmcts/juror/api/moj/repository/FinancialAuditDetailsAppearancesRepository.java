package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetailsAppearances;


@Repository
public interface FinancialAuditDetailsAppearancesRepository
    extends CrudRepository<FinancialAuditDetailsAppearances, FinancialAuditDetailsAppearances>,
    IFinancialAuditDetailsAppearancesRepository {
}
