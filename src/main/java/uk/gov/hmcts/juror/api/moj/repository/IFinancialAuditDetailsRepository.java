package uk.gov.hmcts.juror.api.moj.repository;

import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;

import java.util.Set;

public interface IFinancialAuditDetailsRepository {
    FinancialAuditDetails findLastFinancialAuditDetailsWithAnyTypeWithin(FinancialAuditDetails financialAuditDetails,
                                                                         Set<FinancialAuditDetails.Type> types,
                                                                         SortMethod sortMethod);
}
