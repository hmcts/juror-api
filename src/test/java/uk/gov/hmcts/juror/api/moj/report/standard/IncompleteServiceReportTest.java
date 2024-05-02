package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.LawOfDemeter")
class IncompleteServiceReportTest extends AbstractStandardReportTestSupport<IncompleteServiceReport> {

    private CourtLocationRepository courtLocationRepository;

    public IncompleteServiceReportTest() {
        super(QJuror.juror,
            IncompleteServiceReport.RequestValidator.class,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.POOL_NUMBER_BY_JP,
            DataType.LAST_ATTENDANCE_DATE,
            DataType.NEXT_ATTENDANCE_DATE);
    }

    @BeforeEach
    @Override
    public void beforeEach() {
        this.courtLocationRepository = mock(CourtLocationRepository.class);
        super.beforeEach();
    }

    @Override
    public IncompleteServiceReport createReport(PoolRequestRepository poolRequestRepository) {
        return new IncompleteServiceReport(poolRequestRepository, this.courtLocationRepository);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .locCode(TestConstants.VALID_COURT_LOCATION)
            .date(LocalDate.now())
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {

        TestUtils.setupAuthentication("415", "COURT_USER", "1");

        request = mock(StandardReportRequest.class);
        when(request.getDate()).thenReturn(LocalDate.now());
        when(request.getLocCode()).thenReturn(TestConstants.VALID_COURT_LOCATION);

        report.preProcessQuery(query, request);
        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.returnDate.loe(request.getDate()));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.courtLocation.locCode.eq(request.getLocCode()));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.owner.eq(SecurityUtil.getActiveOwner()));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.isActive.eq(true));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.status.status.in(List.of(IJurorStatus.RESPONDED, IJurorStatus.PANEL,
                IJurorStatus.JUROR)));
        verify(query, times(1))
                    .where((QAppearance.appearance.attendanceType.isNull()
                .or(QAppearance.appearance.attendanceType.notIn(AttendanceType.ABSENT, AttendanceType.NON_ATTENDANCE,
                    AttendanceType.NON_ATTENDANCE_LONG_TRIAL)))
        );
        verify(query, times(1)).orderBy(QJuror.juror.jurorNumber.asc());
        verify(query, times(1))
            .groupBy(QJuror.juror.jurorNumber, QJuror.juror.firstName, QJuror.juror.lastName,
                QJurorPool.jurorPool.pool.poolNumber, QJurorPool.jurorPool.nextDate);

    }

    @Override
    public Map<String, StandardReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<List<LinkedHashMap<String, Object>>> tableData,
        List<LinkedHashMap<String, Object>> data) {

        when(request.getDate()).thenReturn(LocalDate.now());
        when(request.getLocCode()).thenReturn(TestConstants.VALID_COURT_LOCATION);
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(TestConstants.VALID_COURT_LOCATION);
        courtLocation.setName("CHESTER");
        when(courtLocationRepository.findByLocCode(TestConstants.VALID_COURT_LOCATION))
            .thenReturn(Optional.of(courtLocation));

        Map<String, StandardReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map, request, false, Map.of(
            "cut_off_date", StandardReportResponse.DataTypeValue.builder()
                .displayName("Cut-off Date")
                .dataType("LocalDate")
                .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now()))
                .build(),
            "total_incomplete_service", StandardReportResponse.DataTypeValue.builder()
                .displayName("Total incomplete service")
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
    void negativeMissingLocationCode() {
        StandardReportRequest request = getValidRequest();
        request.setLocCode(null);
        assertValidationFails(request, new ValidationFailure("locCode", "must not be null"));
    }

    @Test
    void negativeMissingDate() {
        StandardReportRequest request = getValidRequest();
        request.setDate(null);
        assertValidationFails(request, new ValidationFailure("date", "must not be null"));
    }

    @Test
    void negativeInvalidLocationCode() {
        StandardReportRequest request = getValidRequest();
        request.setLocCode(TestConstants.INVALID_COURT_LOCATION);
        assertValidationFails(request, new ValidationFailure("locCode", "must match \"^\\d{3}$\""));
    }

}
