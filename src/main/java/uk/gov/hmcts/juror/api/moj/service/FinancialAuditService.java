package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FinancialAuditService {

    FinancialAuditDetails createFinancialAuditDetail(String jurorNumber,
                                                     String courtLocationCode,
                                                     FinancialAuditDetails.Type type,
                                                     List<Appearance> appearances);

    List<FinancialAuditDetails> getFinancialAuditDetails(Appearance appearance);

    FinancialAuditDetails getFinancialAuditDetails(long financialAuditNumber, String locCode);

    List<Appearance> getAppearances(FinancialAuditDetails financialAuditDetails);

    Appearance getPreviousAppearance(FinancialAuditDetails financialAuditDetails, Appearance appearance);

    Appearance getPreviousApprovedValue(FinancialAuditDetails financialAuditDetails, Appearance appearance);

    FinancialAuditDetails findFromAppearance(Appearance appearance);

    FinancialAuditDetails getLastFinancialAuditDetailsFromTypes(FinancialAuditDetails financialAuditDetails,
                                                                Set<FinancialAuditDetails.Type> forApproval);

    Optional<FinancialAuditDetails> getLastFinancialAuditDetailsFromAppearanceAndGenericType(
        Appearance appearance,
        FinancialAuditDetails.Type.GenericType genericType);
}
