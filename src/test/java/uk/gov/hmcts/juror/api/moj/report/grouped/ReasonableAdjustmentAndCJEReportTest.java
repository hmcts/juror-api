package uk.gov.hmcts.juror.api.moj.report.grouped;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings({
    "unchecked",
    "PMD.ExcessiveImports"
})
class ReasonableAdjustmentAndCJEReportTest extends AbstractGroupedReportTestSupport<ReasonableAdjustmentAndCjeReport> {

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;
    private CourtLocationService courtLocationService;

    public ReasonableAdjustmentAndCJEReportTest() {
        super(QJurorPool.jurorPool,
            ReasonableAdjustmentAndCjeReport.RequestValidator.class,
            ReportGroupBy.builder()
                .dataType(DataType.COURT_LOCATION_NAME_AND_CODE)
                .removeGroupByFromResponse(true)
                .build(),
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.POOL_NUMBER_BY_JP,
            DataType.CONTACT_DETAILS,
            DataType.NEXT_ATTENDANCE_DATE,
            DataType.OPTIC_REFERENCE,
            DataType.JUROR_REASONABLE_ADJUSTMENT_WITH_MESSAGE);

        setHasPoolRepository(false);
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
        securityUtilMockedStatic.close();
    }

    @Override
    public ReasonableAdjustmentAndCjeReport createReport(PoolRequestRepository poolRequestRepository) {
        return new ReasonableAdjustmentAndCjeReport(this.courtLocationService);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .fromDate(LocalDate.of(2024, 1, 1))
            .toDate(LocalDate.of(2024, 1, 2))
            .build();
    }

    @Override
    @DisplayName("positivePreProcessQueryTypicalCourt")
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        request.setFromDate(LocalDate.of(2024, 1, 1));
        request.setToDate(LocalDate.of(2024, 1, 2));
        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(true);
        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn(TestConstants.VALID_COURT_LOCATION);
        report.preProcessQuery(query, request);

        verify(query).where(
            QJuror.juror.reasonableAdjustmentCode.isNotNull()
                .and(QJurorPool.jurorPool.nextDate.between(request.getFromDate(), request.getToDate()))
        );

        verify(query).where(QJurorPool.jurorPool.owner.eq(SecurityUtil.getActiveOwner()));

        verify(query)
            .orderBy(QJuror.juror.jurorNumber.asc());
    }

    @Override
    @DisplayName("positiveGetHeadingsTypicalCourt")
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<GroupedTableData> tableData,
        GroupedTableData data) {

        request.setFromDate(LocalDate.of(2024, 1, 1));
        request.setToDate(LocalDate.of(2024, 1, 2));
        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(true);
        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn("415");

        when(data.getSize()).thenReturn(5L);

        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocationService.getCourtLocation(TestConstants.VALID_COURT_LOCATION)).thenReturn(courtLocation);
        doReturn(getCourtNameEntry()).when(report).getCourtNameHeader(courtLocation);

        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map,
            request,
            false,
            Map.of(
                "total_reasonable_adjustments",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total jurors with reasonable adjustments")
                    .dataType(Long.class.getSimpleName())
                    .value(5L)
                    .build(),
                "court_name",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType(String.class.getSimpleName())
                    .value("CHESTER (415)")
                    .build()
            )
        );
        verify(report).getHeadings(request, tableData);
        verify(report, times(1)).getCourtNameHeader(any());
        return map;
    }

    @Test
    void positivePreProcessQueryTypicalBureau() {
        StandardReportRequest request = new StandardReportRequest();

        request.setFromDate(LocalDate.of(2024, 1, 1));
        request.setToDate(LocalDate.of(2024, 1, 2));
        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(false);
        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn(TestConstants.VALID_COURT_LOCATION);

        JPAQuery<Tuple> query = mock(JPAQuery.class);
        report.preProcessQuery(query, request);

        verify(query).where(
            QJuror.juror.reasonableAdjustmentCode.isNotNull()
                .and(QJurorPool.jurorPool.nextDate.between(request.getFromDate(), request.getToDate()))
        );
        verify(query, times(0))
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.getActiveOwner()));
        verify(query).orderBy(QJuror.juror.jurorNumber.asc());
        verifyNoMoreInteractions(query);
    }

    @Test
    void positiveGetHeadingsTypicalBureau() {
        StandardReportRequest request = mock(StandardReportRequest.class);
        AbstractReportResponse.TableData<GroupedTableData> tableData = mock(AbstractReportResponse.TableData.class);
        GroupedTableData data = mock(GroupedTableData.class);

        when(data.getSize()).thenReturn(5L);
        doReturn(data).when(tableData).getData();

        request.setFromDate(LocalDate.of(2024, 1, 1));
        request.setToDate(LocalDate.of(2024, 1, 2));
        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(false);

        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map,
            request,
            false,
            Map.of(
                "total_reasonable_adjustments",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total jurors with reasonable adjustments")
                    .dataType(Long.class.getSimpleName())
                    .value(5L)
                    .build()
            )
        );
        verify(report).getHeadings(request, tableData);
        verify(report, never()).getCourtNameHeader(any());
        verifyNoMoreInteractions(report);
    }

    @Test
    void negativeMissingDateFrom() {
        StandardReportRequest request = getValidRequest();
        request.setFromDate(null);
        assertValidationFails(request, new ValidationFailure("fromDate", "must not be null"));
    }

    @Test
    void negativeMissingDateTo() {
        StandardReportRequest request = getValidRequest();
        request.setToDate(null);
        assertValidationFails(request, new ValidationFailure("toDate", "must not be null"));
    }
}
