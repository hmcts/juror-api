package uk.gov.hmcts.juror.api.moj.report.grouped.expense;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.report.datatypes.ExpenseDataTypes;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;

@Component
public class JurorExpenditureReportHighLevelReport extends AbstractJurorExpenditureReport {


    @Autowired
    public JurorExpenditureReportHighLevelReport(CourtLocationService courtLocationService) {
        super(courtLocationService,
            ExpenseDataTypes.TOTAL_LOSS_OF_EARNINGS_APPROVED_SUM,
            ExpenseDataTypes.TOTAL_LOSS_OF_EARNINGS_APPROVED_COUNT,
            ExpenseDataTypes.TOTAL_SUBSISTENCE_APPROVED_SUM,
            ExpenseDataTypes.TOTAL_SUBSISTENCE_APPROVED_COUNT,
            ExpenseDataTypes.TOTAL_SMARTCARD_APPROVED_SUM,
            ExpenseDataTypes.TOTAL_SMARTCARD_APPROVED_COUNT,
            ExpenseDataTypes.TOTAL_TRAVEL_APPROVED_SUM,
            ExpenseDataTypes.TOTAL_TRAVEL_APPROVED_COUNT,
            ExpenseDataTypes.TOTAL_APPROVED_SUM
        );
    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        super.preProcessQuery(query, request);
        addGroupBy(
            query,
            ExpenseDataTypes.IS_CASH
        );
    }
}
