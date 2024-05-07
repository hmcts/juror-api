package uk.gov.hmcts.juror.api.moj.repository;

import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetailsAppearances;

import java.util.Optional;

@SuppressWarnings("LineLength")
public interface IFinancialAuditDetailsAppearancesRepository {
    Optional<FinancialAuditDetailsAppearances> findPreviousFinancialAuditDetailsAppearances(
        FinancialAuditDetails financialAuditDetails,
        Appearance appearance);

    Optional<FinancialAuditDetailsAppearances> findPreviousFinancialAuditDetailsAppearancesWithGenericTypeExcludingProvidedAuditDetails(
        FinancialAuditDetails.Type.GenericType genericType, FinancialAuditDetails financialAuditDetails, Appearance appearance);
}
