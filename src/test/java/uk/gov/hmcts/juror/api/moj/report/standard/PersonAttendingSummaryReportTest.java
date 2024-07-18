package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class PersonAttendingSummaryReportTest extends AbstractStandardReportTestSupport<PersonAttendingSummaryReport> {

    private CourtLocationRepository courtLocationRepository;

    public PersonAttendingSummaryReportTest() {
        super(QJurorPool.jurorPool,
            PersonAttendingSummaryReport.RequestValidator.class,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME);
    }

    @BeforeEach
    @Override
    public void beforeEach() {
        this.courtLocationRepository = mock(CourtLocationRepository.class);
        super.beforeEach();
    }

    @AfterEach
    public void afterEach() {
        TestUtils.afterAll();
    }

    @Override
    public PersonAttendingSummaryReport createReport(PoolRequestRepository poolRequestRepository) {
        return new PersonAttendingSummaryReport(poolRequestRepository, this.courtLocationRepository);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .date(LocalDate.now())
            .includeSummoned(false)
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        String locCode = "415";
        TestUtils.mockSecurityUtil(BureauJwtPayload.builder().locCode(locCode).userType(UserType.COURT).build());

        StandardReportRequest requestMock = mock(StandardReportRequest.class);
        when(requestMock.getDate()).thenReturn(LocalDate.now());

        report.preProcessQuery(query, request);
        verify(query, times(1))
            .where(QJurorPool.jurorPool.nextDate.eq(requestMock.getDate()));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.courtLocation.locCode.eq(locCode));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.status.status.in(IJurorStatus.RESPONDED,
                                                         IJurorStatus.PANEL,
                                                         IJurorStatus.JUROR));
        verify(query, times(1)).orderBy(QJurorPool.jurorPool.juror.lastName.asc());
    }

    @Test
    void positivePreProcessQueryWithSummoned() {
        String locCode = "415";
        TestUtils.mockSecurityUtil(BureauJwtPayload.builder().locCode(locCode).userType(UserType.COURT).build());

        StandardReportRequest requestMock = mock(StandardReportRequest.class);
        when(requestMock.getDate()).thenReturn(LocalDate.now());

        JPAQuery<Tuple> query = mock(JPAQuery.class,
                                     withSettings().defaultAnswer(RETURNS_SELF));
        StandardReportRequest request = getValidRequest();

        request.setIncludeSummoned(true);

        report.preProcessQuery(query, request);
        verify(query, times(1))
            .where(QJurorPool.jurorPool.nextDate.eq(requestMock.getDate()));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.courtLocation.locCode.eq(locCode));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.status.status.in(IJurorStatus.SUMMONED,
                                                         IJurorStatus.RESPONDED,
                                                         IJurorStatus.PANEL,
                                                         IJurorStatus.JUROR));
        verify(query, times(1)).orderBy(QJurorPool.jurorPool.juror.lastName.asc());

        verifyNoMoreInteractions(query);
    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData,
        StandardTableData data) {
        String locCode = "415";
        TestUtils.mockSecurityUtil(BureauJwtPayload.builder().locCode(locCode).userType(UserType.COURT).build());

        when(request.getDate()).thenReturn(LocalDate.now());
        when(request.getLocCode()).thenReturn(TestConstants.VALID_COURT_LOCATION);
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(TestConstants.VALID_COURT_LOCATION);
        courtLocation.setName("CHESTER");
        when(courtLocationRepository.findByLocCode(TestConstants.VALID_COURT_LOCATION))
            .thenReturn(Optional.of(courtLocation));

        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map, request, false, Map.of(
            "attendance_date", StandardReportResponse.DataTypeValue.builder()
                .displayName("Attendance date")
                .dataType("LocalDate")
                .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now()))
                .build(),
            "total_due", StandardReportResponse.DataTypeValue.builder()
                .displayName("Total due to attend")
                .dataType("Integer")
                .value(0)
                .build(),
            "court_name", StandardReportResponse.DataTypeValue.builder()
                .displayName("Court Name")
                .dataType("String")
                .value("CHESTER (415)")
                .build()));
        return map;
    }

    @Test
    void negativeMissingIncludeSummoned() {
        StandardReportRequest request = getValidRequest();
        request.setIncludeSummoned(null);
        assertValidationFails(request, new ValidationFailure("includeSummoned", "must not be null"));
    }

    @Test
    void negativeMissingDate() {
        StandardReportRequest request = getValidRequest();
        request.setDate(null);
        assertValidationFails(request, new ValidationFailure("date", "must not be null"));
    }
}
