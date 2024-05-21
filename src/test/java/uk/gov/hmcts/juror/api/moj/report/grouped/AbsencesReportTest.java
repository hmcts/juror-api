package uk.gov.hmcts.juror.api.moj.report.grouped;

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
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.GroupedTableData;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.report.AbstractGroupedReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.report.ReportGroupBy;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SuppressWarnings("PMD.LawOfDemeter")
class AbsencesReportTest extends AbstractGroupedReportTestSupport<AbsencesReport> {

    private CourtLocationRepository courtLocationRepository;

    public AbsencesReportTest() {
        super(QJurorPool.jurorPool,
              AbsencesReport.RequestValidator.class,
              ReportGroupBy.builder()
                  .dataType(DataType.POOL_NUMBER_AND_COURT_TYPE)
                  .removeGroupByFromResponse(true)
                  .build(),
              DataType.JUROR_NUMBER,
              DataType.FIRST_NAME,
              DataType.LAST_NAME,
              DataType.JUROR_POSTAL_ADDRESS,
              DataType.DATE_OF_ABSENCE);
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
    public AbsencesReport createReport(PoolRequestRepository poolRequestRepository) {
        return new AbsencesReport(poolRequestRepository, this.courtLocationRepository);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .fromDate(LocalDate.now().minusDays(5))
            .toDate(LocalDate.now())
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        String locCode = "415";
        TestUtils.mockSecurityUtil(BureauJwtPayload.builder().locCode(locCode).userType(UserType.COURT).build());

        report.preProcessQuery(query, request);
        verify(query, times(1)).where(QAppearance.appearance.attendanceType.eq(AttendanceType.ABSENT));
        verify(query, times(1))
            .where(QAppearance.appearance.attendanceDate.between(request.getFromDate(), request.getToDate()));
        verify(query, times(1))
            .where(QJurorPool.jurorPool.pool.courtLocation.locCode.eq(locCode));
        verify(query, times(1)).orderBy(
            QJurorPool.jurorPool.juror.jurorNumber.asc(),
            QAppearance.appearance.attendanceDate.asc());
    }

    @Override
    public Map<String, GroupedReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        GroupedReportResponse.TableData<GroupedTableData> tableData,
        GroupedTableData data) {
        String locCode = "415";
        TestUtils.mockSecurityUtil(BureauJwtPayload.builder().locCode(locCode).userType(UserType.COURT).build());

        when(request.getToDate()).thenReturn(LocalDate.now());
        when(request.getFromDate()).thenReturn(LocalDate.now().minusDays(5));
        when(request.getLocCode()).thenReturn(TestConstants.VALID_COURT_LOCATION);

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(TestConstants.VALID_COURT_LOCATION);
        courtLocation.setName("CHESTER");
        when(courtLocationRepository.findByLocCode(TestConstants.VALID_COURT_LOCATION))
            .thenReturn(Optional.of(courtLocation));

        Map<String, GroupedReportResponse.DataTypeValue> map = report.getHeadings(request, tableData);
        assertHeadingContains(map, request, false, Map.of(
            "date_to", GroupedReportResponse.DataTypeValue.builder()
                .displayName("Date to")
                .dataType("LocalDate")
                .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now()))
                .build(),
            "date_from", GroupedReportResponse.DataTypeValue.builder()
                .displayName("Date from")
                .dataType("LocalDate")
                .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now().minusDays(5)))
                .build(),
            "total_absences", GroupedReportResponse.DataTypeValue.builder()
                .displayName("Total absences")
                .dataType("Integer")
                .value(0)
                .build(),
            "court_name", GroupedReportResponse.DataTypeValue.builder()
                .displayName("Court Name")
                .dataType("String")
                .value("CHESTER (415)")
                .build()));
        return map;
    }

    @Test
    void negativeMissingToDate() {
        StandardReportRequest request = getValidRequest();
        request.setToDate(null);
        assertValidationFails(request, new ValidationFailure("toDate", "must not be null"));
    }

    @Test
    void negativeMissingFromDate() {
        StandardReportRequest request = getValidRequest();
        request.setFromDate(null);
        assertValidationFails(request, new ValidationFailure("fromDate", "must not be null"));
    }
}
