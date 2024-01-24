package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;


@Repository
public interface FinancialAuditDetailsRepository extends CrudRepository<FinancialAuditDetails, Long> {

}
