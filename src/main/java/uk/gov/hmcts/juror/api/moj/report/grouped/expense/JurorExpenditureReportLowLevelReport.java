package uk.gov.hmcts.juror.api.moj.report.grouped.expense;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.domain.QLowLevelFinancialAuditDetailsIncludingApprovedAmounts;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.datatypes.ExpenseDataTypes;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;

@Component
public class JurorExpenditureReportLowLevelReport extends AbstractJurorExpenditureReport {

    @Autowired
    public JurorExpenditureReportLowLevelReport(CourtLocationService courtLocationService) {
        super(ExpenseDataTypes.TOTAL_APPROVED_SUM,
            courtLocationService,
            true,
            ExpenseDataTypes.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            ExpenseDataTypes.PAYMENT_AUDIT,
            ExpenseDataTypes.TOTAL_LOSS_OF_EARNINGS_APPROVED_SUM,
            ExpenseDataTypes.TOTAL_SUBSISTENCE_APPROVED_SUM,
            ExpenseDataTypes.TOTAL_SMARTCARD_APPROVED_SUM,
            ExpenseDataTypes.TOTAL_TRAVEL_APPROVED_SUM,
            ExpenseDataTypes.TOTAL_APPROVED_SUM
        );
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        super.preProcessQuery(query, request);
        query.orderBy(
            QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.createdOnDate.asc(),
            QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.jurorNumber.asc(),
            QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.fAudit.asc()
        );

        addGroupBy(
            query,
            ExpenseDataTypes.JUROR_NUMBER,
            ExpenseDataTypes.IS_CASH,
            ExpenseDataTypes.CREATED_ON_DATE,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            ExpenseDataTypes.PAYMENT_AUDIT_RAW
        );
    }
}
