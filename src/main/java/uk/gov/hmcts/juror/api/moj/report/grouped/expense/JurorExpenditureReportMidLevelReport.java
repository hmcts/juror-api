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
public class JurorExpenditureReportMidLevelReport extends AbstractJurorExpenditureReport {


    @Autowired
    public JurorExpenditureReportMidLevelReport(CourtLocationService courtLocationService) {
        super(ExpenseDataTypes.TOTAL_PAID_SUM, courtLocationService, ExpenseDataTypes.TOTAL_PAID_SUM);
        setIncludeTotalApprovedHeader(false);
    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        super.preProcessQuery(query, request);
        addGroupBy(
            query,
            ExpenseDataTypes.IS_CASH,
            ExpenseDataTypes.ATTENDANCE_DATE
        );
        query.orderBy(
            QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.attendanceDate.asc()
        );
    }
}
