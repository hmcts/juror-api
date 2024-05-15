package uk.gov.hmcts.juror.api.moj.report.grouped.expense;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
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

public class JurorExpenditureReportHighLevelReportTest
    extends AbstractJurorExpenditureReportTestSupport<JurorExpenditureReportHighLevelReport> {

    public JurorExpenditureReportHighLevelReportTest() {
        super(false,
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
        setHasPoolRepository(false);
    }

    @Override
    public JurorExpenditureReportHighLevelReport createReport(PoolRequestRepository poolRequestRepository) {
        return new JurorExpenditureReportHighLevelReport(courtLocationService);
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(true);
        doNothing().when(report).addGroupBy(any(), any(IDataType[].class));
        report.preProcessQuery(query, request);
        verifyStandardPreProcessQuery(query, request);
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(StandardReportRequest request,
                                                                                        AbstractReportResponse.TableData<GroupedTableData> tableData,
                                                                                        GroupedTableData data) {
        when(request.getFromDate()).thenReturn(LocalDate.of(2023, 3, 1));
        when(request.getToDate()).thenReturn(LocalDate.of(2023, 3, 2));

        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocationService.getCourtLocation(TestConstants.VALID_COURT_LOCATION)).thenReturn(courtLocation);

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

        verify(report).addCourtNameHeader(anyMap(), eq(courtLocation));
        return map;
    }
}