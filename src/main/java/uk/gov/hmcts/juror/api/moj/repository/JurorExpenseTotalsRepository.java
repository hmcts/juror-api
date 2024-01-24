package uk.gov.hmcts.juror.api.moj.repository;

import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.moj.domain.JurorExpenseTotals;
import uk.gov.hmcts.juror.api.moj.domain.JurorExpenseTotalsId;

@Repository
public interface JurorExpenseTotalsRepository extends IJurorExpenseTotalsRepository,
    ReadOnlyRepository<JurorExpenseTotals, JurorExpenseTotalsId> {

    long countByCourtLocationCodeAndTotalUnapprovedGreaterThan(String locCode, float totalUnapproved);

}
