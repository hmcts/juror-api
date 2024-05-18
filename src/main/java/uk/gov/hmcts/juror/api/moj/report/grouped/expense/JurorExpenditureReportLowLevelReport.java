package uk.gov.hmcts.juror.api.moj.report.grouped.expense;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.QLowLevelFinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReport;
import uk.gov.hmcts.juror.api.moj.report.datatypes.ExpenseDataTypes;
import uk.gov.hmcts.juror.api.moj.report.grouped.groupby.GroupByPaymentType;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;
import uk.gov.hmcts.juror.api.moj.utils.BigDecimalUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JurorExpenditureReportLowLevelReport extends AbstractGroupedReport {

    private final CourtLocationService courtLocationService;

    @Autowired
    public JurorExpenditureReportLowLevelReport(CourtLocationService courtLocationService) {
        super(QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails,
            new GroupByPaymentType(),
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
        this.courtLocationService = courtLocationService;
        isCourtUserOnly();
    }

    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.locCode.eq(SecurityUtil.getLocCode()));
        query.where(QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.type.in(
            FinancialAuditDetails.Type.APPROVED_BACS,
            FinancialAuditDetails.Type.APPROVED_CASH,
            FinancialAuditDetails.Type.REAPPROVED_BACS,
            FinancialAuditDetails.Type.REAPPROVED_CASH
        ));
        query.where(QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.createdOn.between(
            request.getFromDate().atTime(LocalTime.MIN), request.getToDate().atTime(LocalTime.MAX)));

        query.orderBy(
            QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.attendanceDate.asc(),
            QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.jurorNumber.asc(),
            QLowLevelFinancialAuditDetails.lowLevelFinancialAuditDetails.fAudit.asc()
        );
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(StandardReportRequest request,
                                                                         AbstractReportResponse.TableData<GroupedTableData> tableData) {

        Map<String, AbstractReportResponse.DataTypeValue> headings = new ConcurrentHashMap<>();

        headings.put("approved_from", AbstractReportResponse.DataTypeValue.builder()
            .displayName("Approved From")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(request.getFromDate()))
            .build());
        headings.put("approved_to", GroupedReportResponse.DataTypeValue.builder()
            .displayName("Approved to")
            .dataType(LocalDate.class.getSimpleName())
            .value(DateTimeFormatter.ISO_DATE.format(request.getToDate()))
            .build());


        BigDecimal cashTotal = addTotalHeader(headings, tableData, GroupByPaymentType.CASH_TEXT,
            "total_cash", "Total Cash");
        BigDecimal bacsAndChequeTotal = addTotalHeader(headings, tableData, GroupByPaymentType.BACS_OR_CHECK_TEXT,
            "total_bacs_and_cheque", "Total BACS and cheque");

        addTotalHeader(headings, cashTotal.add(bacsAndChequeTotal), "overall_total", "Overall total");

        long totalApprovals = tableData.getData()
            .getAllDataItems()
            .stream()
            .map(groupedTableData -> groupedTableData.get(ExpenseDataTypes.PAYMENT_AUDIT.getId()))
            .distinct()
            .count();

        headings.put("total_approvals", GroupedReportResponse.DataTypeValue.builder()
            .displayName("Total approvals")
            .dataType(Long.class.getSimpleName())
            .value(totalApprovals)
            .build());

        addCourtNameHeader(headings, courtLocationService.getCourtLocation(SecurityUtil.getLocCode()));

        return headings;
    }

    private BigDecimal addTotalHeader(Map<String, AbstractReportResponse.DataTypeValue> headings,
                                      AbstractReportResponse.TableData<GroupedTableData> tableData,
                                      String headingToSearchFor,
                                      String headerId, String headerText) {

        List<GroupedTableData> cashDataItems = tableData.getData()
            .getAllDataItemsIfExist(headingToSearchFor);

        BigDecimal total = cashDataItems
            .stream()
            .map(groupedTableData -> (BigDecimal) groupedTableData
                .getOrDefault(ExpenseDataTypes.TOTAL_PAID.getId(),
                    BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        addTotalHeader(headings, total, headerId, headerText);
        return total;
    }

    private void addTotalHeader(Map<String, AbstractReportResponse.DataTypeValue> headings,
                                BigDecimal total,
                                String headerId, String headerText) {
        headings.put(headerId, GroupedReportResponse.DataTypeValue.builder()
            .displayName(headerText)
            .dataType(String.class.getSimpleName())
            .value(BigDecimalUtils.currencyFormat(total))
            .build());
    }


    @Override
    public Class<RequestValidator> getRequestValidatorClass() {
        return RequestValidator.class;
    }

    public interface RequestValidator extends
        Validators.AbstractRequestValidator,
        Validators.RequireFromDate,
        Validators.RequireToDate {
    }
}
