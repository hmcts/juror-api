package uk.gov.hmcts.juror.api.moj.repository;

import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetailsAppearances;

import java.util.Optional;

public interface IFinancialAuditDetailsAppearancesRepository {
    Optional<FinancialAuditDetailsAppearances> findPreviousFinancialAuditDetailsAppearances(String jurorNumber,
                                                                                            Appearance appearance);

}
