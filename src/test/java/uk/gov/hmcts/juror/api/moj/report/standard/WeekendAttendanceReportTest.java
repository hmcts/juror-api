package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;
import uk.gov.hmcts.juror.api.moj.service.administration.AdministrationHolidaysService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WeekendAttendanceReportTest extends AbstractStandardReportTestSupport<WeekendAttendanceReport> {

    private AdministrationHolidaysService holidaysService;
    private CourtLocationService courtLocationService;
    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    public WeekendAttendanceReportTest() {
        super(QAppearance.appearance,
            WeekendAttendanceReport.RequestValidator.class,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.ATTENDANCE_DATE,
            DataType.DAY,
            DataType.APPEARANCE_POOL_NUMBER,
            DataType.APPEARANCE_TRIAL_NUMBER);
        setHasPoolRepository(false);
    }


    @AfterEach
    void afterEach() {
        securityUtilMockedStatic.close();
    }

    @BeforeEach
    @Override
    public void beforeEach() {
        this.securityUtilMockedStatic = mockStatic(SecurityUtil.class);
        this.holidaysService = mock(AdministrationHolidaysService.class);
        this.courtLocationService = mock(CourtLocationService.class);
        super.beforeEach();
    }

    @Override
    public WeekendAttendanceReport createReport(PoolRequestRepository poolRequestRepository) {
        return new WeekendAttendanceReport(this.holidaysService, this.courtLocationService);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .locCode("123")
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        List<String> courts = List.of("123", "456");
        securityUtilMockedStatic.when(SecurityUtil::getCourts).thenReturn(courts);

        report.preProcessQuery(query, request);

        verify(query, times(1))
            .where(QAppearance.appearance.locCode.in(courts));
        verify(query, times(1))
            .where(QAppearance.appearance.attendanceDate.in(List.of()));
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData,
        StandardTableData data) {
        List<String> courts = List.of("123", "456");
        securityUtilMockedStatic.when(SecurityUtil::getCourts).thenReturn(courts);

        CourtLocation courtLocation = CourtLocation.builder()
            .locCode("123")
            .name("Test Court")
            .build();
        when(courtLocationService.getCourtLocation(any())).thenReturn(courtLocation);

        Map<String, AbstractReportResponse.DataTypeValue> headings = report.getHeadings(request, tableData);
        assertHeadingContains(
            headings,
            request,
            false,
            Map.of(
                "date_from", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Date from")
                    .dataType(LocalDate.class.getSimpleName())
                    .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now().withDayOfMonth(1)))
                    .build(),
                "date_to", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Date to")
                    .dataType(LocalDate.class.getSimpleName())
                    .value(DateTimeFormatter.ISO_DATE.format(LocalDate.now()))
                    .build(),
                "total", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Total")
                    .dataType(Long.class.getSimpleName())
                    .value(tableData.getData().size())
                    .build(),
                "report_created", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Report created")
                    .dataType(LocalDate.class.getSimpleName())
                    .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .build(),
                "time_created", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Time created")
                    .dataType(LocalDateTime.class.getSimpleName())
                    .value(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build(),
                "court_name", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Court Name")
                    .dataType(String.class.getSimpleName())
                    .value("Test Court (123)")
                    .build()
            )
        );

        return headings;
    }

}
