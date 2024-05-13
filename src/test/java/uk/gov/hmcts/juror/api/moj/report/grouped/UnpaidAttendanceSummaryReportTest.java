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
import uk.gov.hmcts.juror.api.juror.domain.QPool;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.ExcusalCodeEnum;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@SuppressWarnings({
    "PMD.LawOfDemeter",
    "PMD.ExcessiveImports"
})
class UnpaidAttendanceSummaryReportTest extends AbstractGroupedReportTestSupport<UnpaidAttendanceSummaryReport> {

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;
    private CourtLocationService courtLocationService;

    public UnpaidAttendanceSummaryReportTest() {
        super(QAppearance.appearance,
            UnpaidAttendanceSummaryReport.RequestValidator.class,
            DataType.POOL_NUMBER,
            true,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME);
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
    public UnpaidAttendanceSummaryReport createReport(PoolRequestRepository poolRequestRepository) {
        return new UnpaidAttendanceSummaryReport(poolRequestRepository, this.courtLocationService);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .fromDate(LocalDate.of(2020, 1, 1))
            .toDate(LocalDate.of(2020, 1, 2))
            .build();
    }

    @Override
    @DisplayName("positivePreProcessQueryTypicalCourt")
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        request.setToDate(LocalDate.of(2023, 1, 1));
        request.setToDate(LocalDate.of(2023, 1, 2));
        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(true);
        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn(TestConstants.VALID_COURT_LOCATION);
        report.preProcessQuery(query, request);

        verify(query).where(
            QAppearance.appearance.attendanceDate.between(request.getFromDate(), request.getToDate())
                .and(QAppearance.appearance.appearanceStage.eq(AppearanceStage.EXPENSE_ENTERED))
                .or(QAppearance.appearance.appearanceStage.eq(AppearanceStage.EXPENSE_EDITED))
                .and(QAppearance.appearance.isDraftExpense.isFalse()));

        verify(query)
            .orderBy(QPool.pool.poolNumber.asc(), QJuror.juror.jurorNumber.asc());
    }

    @Override
    @DisplayName("positiveGetHeadingsTypicalCourt")
    @SuppressWarnings("unchecked")
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<Map<String, List<LinkedHashMap<String, Object>>>> tableData,
        Map<String, List<LinkedHashMap<String, Object>>> data) {

        when(request.getFromDate()).thenReturn(LocalDate.of(2023, 3, 1));
        when(request.getToDate()).thenReturn(LocalDate.of(2023, 3, 2));
        when(request.getLocCode()).thenReturn("415");
        when(tableData.getData()).thenReturn(
            Map.of(
                "2023-03-01", List.of(new LinkedHashMap<>(), new LinkedHashMap()),
                "2023-03-02", List.of(new LinkedHashMap<>())
            )
        );
        securityUtilMockedStatic.when(SecurityUtil::isCourt).thenReturn(true);
        securityUtilMockedStatic.when(SecurityUtil::getActiveOwner).thenReturn(TestConstants.VALID_COURT_LOCATION);

        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocationService.getCourtLocation(TestConstants.VALID_COURT_LOCATION)).thenReturn(courtLocation);
        doReturn(getCourtNameEntry()).when(report).getCourtNameHeader(courtLocation);

        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map,
            request,
            false,
            Map.of(
                "date_from",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date From")
                    .dataType("LocalDate")
                    .value("2023-03-01")
                    .build(),
                "date_to",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Date To")
                    .dataType("LocalDate")
                    .value("2023-03-02")
                    .build(),
                "total_unpaid_attendances",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Total Unpaid Attendances")
                    .dataType(Long.class.getSimpleName())
                    .value(3)
                    .build(),
                "court_name",
                StandardReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType(String.class.getSimpleName())
                    .value("CHESTER (415)")
                    .build()
            )
        );
        verify(report).getCourtNameHeader(courtLocation);
        return map;
    }

    @Test
    void negativeMissingFromDate() {
        StandardReportRequest request = getValidRequest();
        request.setFromDate(null);
        assertValidationFails(request, new ValidationFailure("fromDate", "must not be null"));
    }

    @Test
    void negativeMissingToDate() {
        StandardReportRequest request = getValidRequest();
        request.setToDate(null);
        assertValidationFails(request, new ValidationFailure("toDate", "must not be null"));
    }
}