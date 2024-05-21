package uk.gov.hmcts.juror.api.moj.report.grouped.expense;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.Setter;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReport;
import uk.gov.hmcts.juror.api.moj.report.IDataType;
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

import static uk.gov.hmcts.juror.api.moj.domain.QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts;

public abstract class AbstractJurorExpenditureReport extends AbstractGroupedReport {

    private final CourtLocationService courtLocationService;
    private final IDataType expenseTotalPaidDataType;
    @Setter
    private boolean includeHeaderTotals;

    public AbstractJurorExpenditureReport(
        CourtLocationService courtLocationService,
        IDataType... dataType) {
        this(null, courtLocationService, false, dataType);
        this.includeHeaderTotals = false;
    }

    public AbstractJurorExpenditureReport(
        IDataType expenseTotalPaidDataType,
        CourtLocationService courtLocationService,
        boolean includeNested,
        IDataType... dataType) {
        super(
            lowLevelFinancialAuditDetailsIncludingApprovedAmounts,
            new GroupByPaymentType(includeNested), dataType);
        this.expenseTotalPaidDataType = expenseTotalPaidDataType;
        this.courtLocationService = courtLocationService;
        isCourtUserOnly();
        this.includeHeaderTotals = true;
    }


    @Override
    protected void preProcessQuery(JPAQuery<Tuple> query, StandardReportRequest request) {
        query.where(
            lowLevelFinancialAuditDetailsIncludingApprovedAmounts.locCode.eq(
                SecurityUtil.getLocCode()));
        query.where(
            lowLevelFinancialAuditDetailsIncludingApprovedAmounts.type.in(
                FinancialAuditDetails.Type.APPROVED_BACS,
                FinancialAuditDetails.Type.APPROVED_CASH,
                FinancialAuditDetails.Type.REAPPROVED_BACS,
                FinancialAuditDetails.Type.REAPPROVED_CASH
            ));
        query.where(
            lowLevelFinancialAuditDetailsIncludingApprovedAmounts.createdOn.between(
                request.getFromDate().atTime(LocalTime.MIN), request.getToDate().atTime(LocalTime.MAX)));
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> getHeadings(
        StandardReportRequest request,
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

        if (includeHeaderTotals) {

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

        }
        addCourtNameHeader(headings, courtLocationService.getCourtLocation(SecurityUtil.getLocCode()));

        return headings;
    }

    public BigDecimal addTotalHeader(Map<String, AbstractReportResponse.DataTypeValue> headings,
                                     AbstractReportResponse.TableData<GroupedTableData> tableData,
                                     String headingToSearchFor,
                                     String headerId, String headerText) {

        List<GroupedTableData> dataItems = tableData.getData()
            .getAllDataItemsIfExist(headingToSearchFor);

        BigDecimal total = dataItems
            .stream()
            .map(groupedTableData -> (BigDecimal) groupedTableData
                .getOrDefault(expenseTotalPaidDataType.getId(),
                    BigDecimal.ZERO))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        return addTotalHeader(headings, total, headerId, headerText);
    }

    public BigDecimal addTotalHeader(Map<String, AbstractReportResponse.DataTypeValue> headings,
                                     BigDecimal total,
                                     String headerId, String headerText) {
        headings.put(headerId, GroupedReportResponse.DataTypeValue.builder()
            .displayName(headerText)
            .dataType(String.class.getSimpleName())
            .value(BigDecimalUtils.currencyFormat(total))
            .build());
        return total;
    }

    @Override
    public Class<JurorExpenditureReportMidLevelReport.RequestValidator> getRequestValidatorClass() {
        return JurorExpenditureReportMidLevelReport.RequestValidator.class;
    }

    public interface RequestValidator extends
        Validators.AbstractRequestValidator,
        Validators.RequireFromDate,
        Validators.RequireToDate {
    }
}
