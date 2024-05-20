package uk.gov.hmcts.juror.api.moj.report.grouped.expense;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.report.datatypes.ExpenseDataTypes;
import uk.gov.hmcts.juror.api.moj.report.grouped.groupby.GroupByPaymentType;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.juror.api.moj.domain.QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts;

@Component
public class JurorExpenditureReportMidLevelReport extends AbstractJurorExpenditureReport {


    @Autowired
    public JurorExpenditureReportMidLevelReport(CourtLocationService courtLocationService) {
        super(ExpenseDataTypes.TOTAL_APPROVED_SUM, courtLocationService, false,
            ExpenseDataTypes.CREATED_ON_DATE,
            ExpenseDataTypes.TOTAL_APPROVED_SUM);
        setIncludeHeaderTotals(false);
    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        super.preProcessQuery(query, request);
        addGroupBy(
            query,
            ExpenseDataTypes.IS_CASH,
            ExpenseDataTypes.CREATED_ON_DATE
        );
        query.orderBy(
            lowLevelFinancialAuditDetailsIncludingApprovedAmounts.createdOnDate.asc()
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
        AbstractReportResponse.TableData<GroupedTableData> tableData) {

        Map<String, AbstractReportResponse.DataTypeValue> headings = super.getHeadings(request, tableData);


        BigDecimal cashTotal = addTotalHeader(headings, ((List<GroupedTableData>) tableData.getData()
                .getOrDefault(GroupByPaymentType.CASH_TEXT, new ArrayList<>()))
                .stream()
                .map(o -> (BigDecimal) o.getOrDefault(ExpenseDataTypes.TOTAL_APPROVED_SUM.getId(),
                    BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add),
            "total_cash",
            "Total Cash"
        );

        BigDecimal bacsAndChequeTotal = addTotalHeader(headings, ((List<GroupedTableData>) tableData.getData()
                .getOrDefault(GroupByPaymentType.BACS_OR_CHECK_TEXT, new ArrayList<>()))
                .stream()
                .map(o -> (BigDecimal) o.getOrDefault(ExpenseDataTypes.TOTAL_APPROVED_SUM.getId(),
                    BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add),
            "total_bacs_and_cheque",
            "Total BACS and cheque"
        );
        addTotalHeader(headings, cashTotal.add(bacsAndChequeTotal), "overall_total", "Overall total");
        return headings;
    }
}
