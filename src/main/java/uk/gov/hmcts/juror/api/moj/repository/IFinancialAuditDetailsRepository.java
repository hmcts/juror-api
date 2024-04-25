package uk.gov.hmcts.juror.api.moj.repository;

import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;

public interface IFinancialAuditDetailsRepository {
    FinancialAuditDetails findLastFinancialAuditDetailsWithType(FinancialAuditDetails financialAuditDetails,
                                                                FinancialAuditDetails.Type.GenericType genericType,
                                                                SortMethod sortMethod);
}
