package uk.gov.hmcts.juror.api.moj.report.standard;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.request.StandardReportRequest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.AbstractReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.StandardTableData;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.report.AbstractStandardReportTestSupport;
import uk.gov.hmcts.juror.api.moj.report.DataType;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceService;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JuryAttendanceAuditReportTest extends AbstractStandardReportTestSupport<JuryAttendanceAuditReport> {

    private JurorAppearanceService appearanceService;
    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    public JuryAttendanceAuditReportTest() {
        super(QAppearance.appearance,
            JuryAttendanceAuditReport.RequestValidator.class,
            DataType.JUROR_NUMBER,
            DataType.FIRST_NAME,
            DataType.LAST_NAME,
            DataType.APPEARANCE_CHECKED_IN,
            DataType.APPEARANCE_CHECKED_OUT);
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
        this.appearanceService = mock(JurorAppearanceService.class);
        super.beforeEach();
    }

    @Override
    public JuryAttendanceAuditReport createReport(PoolRequestRepository poolRequestRepository) {
        return new JuryAttendanceAuditReport(this.appearanceService);
    }

    @Override
    protected StandardReportRequest getValidRequest() {
        return StandardReportRequest.builder()
            .reportType(report.getName())
            .juryAuditNumber("J1234")
            .build();
    }

    @Override
    public void positivePreProcessQueryTypical(JPAQuery<Tuple> query, StandardReportRequest request) {
        List<String> courts = List.of("123", "456");
        securityUtilMockedStatic.when(SecurityUtil::getCourts).thenReturn(courts);

        report.preProcessQuery(query, request);
        verify(query, times(1))
            .where(QAppearance.appearance.attendanceAuditNumber.eq("J1234"));
        verify(query, times(1))
            .where(QAppearance.appearance.locCode.in(courts));
    }

    @Override
    public Map<String, AbstractReportResponse.DataTypeValue> positiveGetHeadingsTypical(
        StandardReportRequest request,
        AbstractReportResponse.TableData<StandardTableData> tableData,
        StandardTableData data) {
        List<String> courts = List.of("123", "456");
        securityUtilMockedStatic.when(SecurityUtil::getCourts).thenReturn(courts);
        when(request.getJuryAuditNumber()).thenReturn("J1234");
        when(data.size()).thenReturn(5);
        when(data.isEmpty()).thenReturn(false);

        Appearance appearance = mock(Appearance.class);
        when(appearance.getAttendanceDate()).thenReturn(LocalDate.of(2024, 1, 1));
        when(appearance.getTrialNumber()).thenReturn("T1234");
        when(appearanceService.getFirstAppearanceWithAuditNumber("J1234", courts))
            .thenReturn(Optional.of(appearance));

        CourtLocation courtLocation = mock(CourtLocation.class);
        when(appearance.getCourtLocation()).thenReturn(courtLocation);

        doNothing().when(report).addCourtNameHeader(any(), any());

        Map<String, AbstractReportResponse.DataTypeValue> headings = report.getHeadings(request, tableData);
        assertHeadingContains(
            headings,
            request,
            false,
            Map.of(
                "attendance_date", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Attendance date")
                    .dataType("LocalDate")
                    .value("2024-01-01")
                    .build(),
                "audit_number", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Audit number")
                    .dataType("String")
                    .value("J1234")
                    .build(),
                "trial_number", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Trial number")
                    .dataType(String.class.getSimpleName())
                    .value("T1234")
                    .build(),
                "total", AbstractReportResponse.DataTypeValue.builder()
                    .displayName("Total")
                    .dataType(Long.class.getSimpleName())
                    .value(5)
                    .build()
            )
        );


        verify(appearanceService, times(1))
            .getFirstAppearanceWithAuditNumber("J1234", courts);

        verify(report).addCourtNameHeader(headings, courtLocation);
        return headings;
    }

    @Test
    @SuppressWarnings("unchecked")
    void negativeNotFoundEmpty() {
        StandardReportRequest request = mock(StandardReportRequest.class);
        AbstractReportResponse.TableData<StandardTableData> tableData = mock(AbstractReportResponse.TableData.class);
        StandardTableData data = mock(StandardTableData.class);
        when(tableData.getData()).thenReturn(data);

        List<String> courts = List.of("123", "456");
        securityUtilMockedStatic.when(SecurityUtil::getCourts).thenReturn(courts);
        when(data.size()).thenReturn(5);
        when(data.isEmpty()).thenReturn(true);

        Appearance appearance = mock(Appearance.class);
        when(appearanceService.getFirstAppearanceWithAuditNumber("J1234", courts))
            .thenReturn(Optional.of(appearance));

        MojException.NotFound exception = assertThrows(MojException.NotFound.class,
            () -> report.getHeadings(request, tableData),
            "Should throw an exception when no data is found");

        assertThat(exception.getMessage()).isEqualTo("Audit Number not found");
        assertThat(exception.getCause()).isNull();
    }

    @Test
    void negativeNotFoundNull() {
        StandardReportRequest request = mock(StandardReportRequest.class);
        AbstractReportResponse.TableData<StandardTableData> tableData = mock(AbstractReportResponse.TableData.class);
        StandardTableData data = mock(StandardTableData.class);
        when(tableData.getData()).thenReturn(data);

        List<String> courts = List.of("123", "456");
        securityUtilMockedStatic.when(SecurityUtil::getCourts).thenReturn(courts);
        when(request.getJuryAuditNumber()).thenReturn("J1234");
        when(data.size()).thenReturn(5);
        when(data.isEmpty()).thenReturn(false);

        when(appearanceService.getFirstAppearanceWithAuditNumber("J1234", courts))
            .thenReturn(Optional.empty());

        MojException.NotFound exception = assertThrows(MojException.NotFound.class,
            () -> report.getHeadings(request, tableData),
            "Should throw an exception when no data is found");

        assertThat(exception.getMessage()).isEqualTo("Audit Number not found");
        assertThat(exception.getCause()).isNull();

    }

    @Test
    void negativeMissingJuryAuditNumber() {
        StandardReportRequest request = getValidRequest();
        request.setJuryAuditNumber(null);
        assertValidationFails(request, new ValidationFailure("juryAuditNumber", "must not be null"));
    }

    @Test
    void negativeInvalidJuryAuditNumber() {
        StandardReportRequest request = getValidRequest();
        request.setJuryAuditNumber("123");
        assertValidationFails(request, new ValidationFailure("juryAuditNumber", "must match \"^J\\d*$\""));
    }
}
