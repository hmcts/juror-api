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
}
