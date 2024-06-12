package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;

import java.util.List;

public interface FinancialAuditService {

    FinancialAuditDetails createFinancialAuditDetail(String jurorNumber,
                                                     String courtLocationCode,
                                                     FinancialAuditDetails.Type type,
                                                     List<Appearance> appearances);

    List<FinancialAuditDetails> getFinancialAuditDetails(Appearance appearance);

    FinancialAuditDetails getFinancialAuditDetails(long financialAuditNumber, String locCode);

    List<Appearance> getAppearances(FinancialAuditDetails financialAuditDetails);

    FinancialAuditDetails getLastFinancialAuditDetailsWithType(
        FinancialAuditDetails financialAuditDetails, FinancialAuditDetails.Type.GenericType genericType);

    Appearance getPreviousAppearance(FinancialAuditDetails financialAuditDetails, Appearance appearance);

    Appearance getPreviousApprovedValue(FinancialAuditDetails financialAuditDetails, Appearance appearance);

    FinancialAuditDetails findFromAppearance(Appearance appearance);
}
