package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.AbstractReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AvailableListByDateReportCourtTest
    extends AbstractGroupedReportTestSupport<AvailableListByDateReportCourt> {

    private static final LocalDate DATE = LocalDate.of(2024, 1, 1);
    private MockedStatic<SecurityUtil> securityUtilMockedStatic;
    private CourtLocationService courtLocationService;

    public AvailableListByDateReportCourtTest() {
        super(QJurorPool.jurorPool,
            AvailableListByDateReportCourt.RequestValidator.class,
            ReportGroupBy.builder()
                .dataType(DataType.POOL_NUMBER_AND_COURT_TYPE)
                .removeGroupByFromResponse(true)
                .build(),
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.STATUS,
            DataType.JUROR_REASONABLE_ADJUSTMENT_WITH_MESSAGE
        );
    }

    @BeforeEach
    @Override
    public void beforeEach() {
        this.securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        this.courtLocationService = mock(CourtLocationService.class);
        super.beforeEach();
    }

    @AfterEach
    void afterEach() {
        this.securityUtilMockedStatic.close();
    }

    @Override
    public AvailableListByDateReportCourt createReport(PoolRequestRepository poolRequestRepository) {
        return new AvailableListByDateReportCourt(poolRequestRepository, this.courtLocationService);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .date(DATE)
            .includeJurorsOnCall(false)
            .includePanelMembers(false)
            .respondedJurorsOnly(false)
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner)
            .thenReturn(TestConstants.VALID_COURT_LOCATION);
        doNothing().when(report).addStandardFilters(any(), any());
        report.preProcessQuery(query, request);
        verify(query, times(1)).where(QJurorPool.jurorPool.nextDate.eq(DATE));
        verify(query, times(1)).where(QJurorPool.jurorPool.pool.owner.eq(TestConstants.VALID_COURT_LOCATION));
        verify(query, times(1)).orderBy(
            QJurorPool.jurorPool.pool.poolNumber.asc(),
            QJurorPool.jurorPool.juror.jurorNumber.asc()
        );

        verify(report, times(1))
            .addStandardFilters(query, request);
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<GroupedTableData> tableData,
        GroupedTableData data) {

        CourtLocation courtLocation = mock(CourtLocation.class);
        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner)
            .thenReturn(TestConstants.VALID_COURT_LOCATION);
        doReturn(courtLocation).when(courtLocationService)
            .getCourtLocation(TestConstants.VALID_COURT_LOCATION);

        doNothing().when(report).addCourtNameHeader(any(), any());

        GroupedTableData groupedTableData = mock(GroupedTableData.class);
        when(groupedTableData.getSize()).thenReturn(5L);
        when(tableData.getData()).thenReturn(groupedTableData);


        Map<String, AbstractReportResponse.DataTypeValue> headings = report.getHeadings(request, tableData);
        assertHeadingContains(headings,
            request,
            false,
            Map.of(
                "total_available_pool_members",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total available pool members")
                    .dataType("Long")
                    .value(5L)
                    .build(),
                "attendance_date",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Attendance date")
                    .dataType("LocalDate")
                    .value("2024-01-01")
                    .build()
            ));
        verify(courtLocationService, times(1))
            .getCourtLocation(TestConstants.VALID_COURT_LOCATION);
        return headings;
    }


    @Test
    void negativeMissingDate() {
        StandardReportRequest request = getValidRequest();
        request.setDate(null);
        assertValidationFails(request, new AbstractReportTestSupport.ValidationFailure("date", "must not be null"));
    }

    @Test
    void negativeMissingIncludePanelMembers() {
        StandardReportRequest request = getValidRequest();
        request.setIncludePanelMembers(null);
        assertValidationFails(request,
            new AbstractReportTestSupport.ValidationFailure("includePanelMembers", "must not be null"));
    }

    @Test
    void negativeMissingIncludeJurorsOnCall() {
        StandardReportRequest request = getValidRequest();
        request.setIncludeJurorsOnCall(null);
        assertValidationFails(request,
            new AbstractReportTestSupport.ValidationFailure("includeJurorsOnCall", "must not be null"));
    }

    @Test
    void negativeMissingRespondedJurorsOnly() {
        StandardReportRequest request = getValidRequest();
        request.setRespondedJurorsOnly(null);
        assertValidationFails(request,
            new AbstractReportTestSupport.ValidationFailure("respondedJurorsOnly", "must not be null"));
    }
}
