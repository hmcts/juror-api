package uk.gov.hmcts.juror.api.moj.report.grouped.expense;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.IDataType;
import uk.gov.hmcts.juror.api.moj.report.datatypes.ExpenseDataTypes;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.juror.api.moj.domain.QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts;

public class JurorExpenditureReportLowLevelReportTest
    extends AbstractJurorExpenditureReportTestSupport<JurorExpenditureReportLowLevelReport> {

    public JurorExpenditureReportLowLevelReportTest() {
        super(true,
            ExpenseDataTypes.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            ExpenseDataTypes.PAYMENT_AUDIT,
            ExpenseDataTypes.TOTAL_LOSS_OF_EARNINGS_APPROVED_SUM,
            ExpenseDataTypes.TOTAL_SUBSISTENCE_APPROVED_SUM,
            ExpenseDataTypes.TOTAL_SMARTCARD_APPROVED_SUM,
            ExpenseDataTypes.TOTAL_TRAVEL_APPROVED_SUM,
            ExpenseDataTypes.TOTAL_APPROVED_SUM);
        setHasPoolRepository(false);
    }

    @Override
    public JurorExpenditureReportLowLevelReport createReport(PoolRequestRepository poolRequestRepository) {
        return new JurorExpenditureReportLowLevelReport(courtLocationService);
    }


    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(true);
        doNothing().when(report).addGroupBy(any(), any(IDataType[].class));

        report.preProcessQuery(query, request);
        verifyStandardPreProcessQuery(query, request);
        verify(query, times(1))
            .orderBy(
                lowLevelFinancialAuditDetailsIncludingApprovedAmounts.createdOnDate.asc(),
                lowLevelFinancialAuditDetailsIncludingApprovedAmounts.jurorNumber.asc(),
                lowLevelFinancialAuditDetailsIncludingApprovedAmounts.financialAudit.asc()
            );

        verify(report, times(1)).addGroupBy(
            query,
            ExpenseDataTypes.JUROR_NUMBER,
            ExpenseDataTypes.IS_CASH,
            ExpenseDataTypes.CREATED_ON_DATE,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            ExpenseDataTypes.PAYMENT_AUDIT_RAW
        );
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<GroupedTableData> tableData,
        GroupedTableData data) {

        when(request.getFromDate()).thenReturn(LocalDate.of(2023, 3, 1));
        when(request.getToDate()).thenReturn(LocalDate.of(2023, 3, 2));

        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocationService.getCourtLocation(TestConstants.VALID_COURT_LOCATION)).thenReturn(courtLocation);

        doReturn(new BigDecimal("2.02"))
            .when(report)
            .addTotalHeader(anyMap(),
                eq(tableData),
                eq("Cash"),
                eq("total_cash"),
                eq("Total Cash"));

        doReturn(new BigDecimal("3.03"))
            .when(report)
            .addTotalHeader(anyMap(),
                eq(tableData),
                eq("BACS and cheque approvals"),
                eq("total_bacs_and_cheque"),
                eq("Total BACS and cheque"));

        doReturn(new BigDecimal("3.03"))
            .when(report)
            .addTotalHeader(anyMap(),
                eq(new BigDecimal("5.05")),
                eq("overall_total"),
                eq("Overall total"));

        Function<String, GroupedTableData> mockGroupedTableData = auditNumber -> {
            GroupedTableData groupedTableData = mock(GroupedTableData.class);
            when(groupedTableData.get("payment_audit")).thenReturn(auditNumber);
            return groupedTableData;

        };

        List<GroupedTableData> groupedTableDataList = List.of(
            mockGroupedTableData.apply("F123"),
            mockGroupedTableData.apply("F123"),
            mockGroupedTableData.apply("F1234"),
            mockGroupedTableData.apply("F12345"),
            mockGroupedTableData.apply("F1234")
        );

        doReturn(groupedTableDataList).when(data).getAllDataItems();

        doNothing().when(report).addCourtNameHeader(any(), any());


        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map,
            request,
            false,
            Map.of(
                "approved_from",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Approved From")
                    .dataType("LocalDate")
                    .value("2023-03-01")
                    .build(),
                "approved_to",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Approved to")
                    .dataType("LocalDate")
                    .value("2023-03-02")
                    .build(),
                "total_approvals",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total approvals")
                    .dataType("Long")
                    .value(3L)
                    .build()
            )
        );

        verify(report, times(1))
            .addCourtNameHeader(
                anyMap(),
                eq(courtLocation)
            );
        verify(courtLocationService, times(1))
            .getCourtLocation(TestConstants.VALID_COURT_LOCATION);

        verify(report, times(1))
            .addTotalHeader(anyMap(),
                eq(tableData),
                eq("Cash"),
                eq("total_cash"),
                eq("Total Cash"));

        verify(report, times(1))
            .addTotalHeader(anyMap(),
                eq(tableData),
                eq("BACS and cheque approvals"),
                eq("total_bacs_and_cheque"),
                eq("Total BACS and cheque"));

        verify(report, times(1))
            .addTotalHeader(anyMap(),
                eq(new BigDecimal("5.05")),
                eq("overall_total"),
                eq("Overall total"));

        verify(report).addCourtNameHeader(anyMap(), eq(courtLocation));
        return map;
    }
}
