package uk.gov.hmcts.juror.api.moj.report.grouped.expense;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QLowLevelFinancialAuditDetailsIncludingApprovedAmounts;
import uk.gov.hmcts.juror.api.moj.report.IDataType;
import uk.gov.hmcts.juror.api.moj.report.datatypes.ExpenseDataTypes;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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

public class JurorExpenditureReportMidLevelReportTest
    extends AbstractJurorExpenditureReportTestSupport<JurorExpenditureReportMidLevelReport> {

    public JurorExpenditureReportMidLevelReportTest() {
        super(false,
            ExpenseDataTypes.CREATED_ON_DATE,
            ExpenseDataTypes.TOTAL_APPROVED_SUM
        );
        setHasPoolRepository(false);
    }

    @Override
    public JurorExpenditureReportMidLevelReport createReport(PoolRequestRepository poolRequestRepository) {
        return new JurorExpenditureReportMidLevelReport(courtLocationService);
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(true);
        doNothing().when(report).addGroupBy(any(), any(IDataType[].class));

        report.preProcessQuery(query, request);
        verifyStandardPreProcessQuery(query, request);
        verify(query, times(1))
            .orderBy(
                QLowLevelFinancialAuditDetailsIncludingApprovedAmounts.lowLevelFinancialAuditDetailsIncludingApprovedAmounts.createdOnDate.asc());

        verify(report, times(1)).addGroupBy(
            query,
            ExpenseDataTypes.IS_CASH,
            ExpenseDataTypes.CREATED_ON_DATE
        );

    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(StandardReportRequest request,
                                                                                        AbstractReportResponse.TableData<GroupedTableData> tableData,
                                                                                        GroupedTableData data) {
        when(request.getFromDate()).thenReturn(LocalDate.of(2023, 3, 1));
        when(request.getToDate()).thenReturn(LocalDate.of(2023, 3, 2));

        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocationService.getCourtLocation(TestConstants.VALID_COURT_LOCATION)).thenReturn(courtLocation);


        doReturn(new BigDecimal("5.08")).when(report)
            .addTotalHeader(any(), any(), eq("total_cash"), any());
        doReturn(new BigDecimal("15.07")).when(report)
            .addTotalHeader(any(), any(), eq("total_bacs_and_cheque"), any());

        doReturn(new BigDecimal("4.01")).when(report)
            .addTotalHeader(any(), any(), eq("overall_total"), any());

        Function<BigDecimal, GroupedTableData> mockGroupedTableData = amount -> {
            GroupedTableData groupedTableData = mock(GroupedTableData.class);
            when(groupedTableData.getOrDefault("total_approved_sum", BigDecimal.ZERO)).thenReturn(amount);
            return groupedTableData;
        };

        List<GroupedTableData> cashGroupedTableDataList = List.of(
            mockGroupedTableData.apply(new BigDecimal("1.01")),
            mockGroupedTableData.apply(new BigDecimal("2.03")),
            mockGroupedTableData.apply(new BigDecimal("2.04"))
        );
        List<GroupedTableData> bacsGroupedTableDataList = List.of(
            mockGroupedTableData.apply(new BigDecimal("4.01")),
            mockGroupedTableData.apply(new BigDecimal("5.03")),
            mockGroupedTableData.apply(new BigDecimal("6.03"))
        );

        when(data.getOrDefault("Cash", new ArrayList<>()))
            .thenReturn(cashGroupedTableDataList);
        when(data.getOrDefault("BACS and cheque approvals", new ArrayList<>()))
            .thenReturn(bacsGroupedTableDataList);

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
                eq(new BigDecimal("5.08")),
                eq("total_cash"),
                eq("Total Cash"));

        verify(report, times(1))
            .addTotalHeader(anyMap(),
                eq(new BigDecimal("15.07")),
                eq("total_bacs_and_cheque"),
                eq("Total BACS and cheque"));

        verify(report, times(1))
            .addTotalHeader(anyMap(),
                eq(new BigDecimal("20.15")),
                eq("overall_total"),
                eq("Overall total"));

        verify(report).addCourtNameHeader(anyMap(), eq(courtLocation));
        return map;
    }
}
