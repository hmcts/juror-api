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
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({
    "PMD.LawOfDemeter",
    "PMD.ExcessiveImports"
})
public class UnconfirmedAttendanceReportTest extends AbstractGroupedReportTestSupport<UnconfirmedAttendanceReport> {

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;
    private CourtLocationService courtLocationService;

    public UnconfirmedAttendanceReportTest() {
        super(
            QAppearance.appearance,
            UnconfirmedAttendanceReport.RequestValidator.class,
            ReportGroupBy.builder()
                .dataType(DataType.APPEARANCE_DATE_AND_POOL_TYPE)
                .removeGroupByFromResponse(true)
                .build(),
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.APPEARANCE_POOL_NUMBER,
            DataType.APPEARANCE_TRIAL_NUMBER,
            DataType.APPEARANCE_CHECKED_IN,
            DataType.APPEARANCE_CHECKED_OUT);

        setHasPoolRepository(false);
    }

    @BeforeEach
    @Override
    public void beforeEach() {
        securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        courtLocationService = mock(CourtLocationService.class);
        super.beforeEach();
    }

    @AfterEach
    void afterEach() {
        securityUtilMockedStatic.close();
    }

    @Override
    public UnconfirmedAttendanceReport createReport(PoolRequestRepository poolRequestRepository) {
        return new UnconfirmedAttendanceReport(this.courtLocationService);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType("UnconfirmedAttendanceReport")
            .fromDate(LocalDate.of(2024, 1, 1))
            .toDate(LocalDate.of(2024, 1, 2))
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        request.setFromDate(LocalDate.of(2024, 1, 1));
        request.setToDate(LocalDate.of(2024, 1, 2));
        securityUtilMockedStatic.when(SecurityUtil::getLocCode).thenReturn(TestConstants.VALID_COURT_LOCATION);
        report.preProcessQuery(query, request);

        verify(query).where(QAppearance.appearance.attendanceDate.between(request.getFromDate(), request.getToDate())
            .and(QAppearance.appearance.appearanceStage.in(
                AppearanceStage.CHECKED_IN,
                AppearanceStage.CHECKED_OUT
            )));
        verify(query).where(QAppearance.appearance.locCode.eq(SecurityUtil.getLocCode()));
        verify(query).orderBy(QAppearance.appearance.attendanceDate.desc(), QAppearance.appearance.jurorNumber.asc());
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<GroupedTableData> tableData,
        GroupedTableData data) {

        request.setFromDate(LocalDate.of(2024, 1, 1));
        request.setToDate(LocalDate.of(2024, 1, 2));
        securityUtilMockedStatic.when(SecurityUtil::getLocCode).thenReturn(TestConstants.VALID_COURT_LOCATION);

        when(data.getSize()).thenReturn(50);

        CourtLocation courtLocation = mock(CourtLocation.class);
        when(courtLocationService.getCourtLocation(TestConstants.VALID_COURT_LOCATION)).thenReturn(courtLocation);
        doReturn(getCourtNameEntry()).when(report).getCourtNameHeader(courtLocation);

        Map<String, GroupedReportResponse.DataTypeValue> expected = new ConcurrentHashMap<>();
        expected.put("total_unconfirmed_attendances", GroupedReportResponse.DataTypeValue.builder()
             .displayName("Total unconfirmed attendances")
             .dataType(Long.class.getSimpleName())
             .value(50)
             .build());
        expected.put("court_name", getCourtNameEntry().getValue());

        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map,
            request,
            false,
            expected
        );

        verify(report).getHeadings(request, tableData);
        verify(report, times(1)).getCourtNameHeader(courtLocation);

        return map;
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
