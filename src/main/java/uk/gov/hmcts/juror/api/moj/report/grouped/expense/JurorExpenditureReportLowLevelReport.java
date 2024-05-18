package uk.gov.hmcts.juror.api.moj.report.grouped.expense;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.domain.QLowLevelFinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.report.datatypes.ExpenseDataTypes;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;

@Component
public class JurorExpenditureReportLowLevelReport extends AbstractJurorExpenditureReport {

    @Autowired
    public JurorExpenditureReportLowLevelReport(CourtLocationService courtLocationService) {
        super(ExpenseDataTypes.TOTAL_PAID,
            courtLocationService,
            ExpenseDataTypes.JUROR_NUMBER,
            ExpenseDataTypes.FIRST_NAME,
            ExpenseDataTypes.LAST_NAME,
            ExpenseDataTypes.PAYMENT_AUDIT,
            ExpenseDataTypes.TOTAL_LOSS_OF_EARNINGS_PAID,
            ExpenseDataTypes.TOTAL_SUBSISTENCE_PAID,
            ExpenseDataTypes.TOTAL_SMARTCARD_PAID,
            ExpenseDataTypes.TOTAL_TRAVEL_PAID,
            ExpenseDataTypes.TOTAL_PAID
        );
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        super.preProcessQuery(query, request);
        query.orderBy(
            QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.attendanceDate.asc(),
            QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.jurorNumber.asc(),
            QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.fAudit.asc()
        );
    }
}
