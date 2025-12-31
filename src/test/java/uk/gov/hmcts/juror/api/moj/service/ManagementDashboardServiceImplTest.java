package uk.gov.hmcts.juror.api.moj.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.ExpenseLimitsReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.IncompleteServiceReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.OverdueUtilisationReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.SmsMessagesReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.WeekendAttendanceReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.WeekendAttendanceReportResponse;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.MessageRepository;
import uk.gov.hmcts.juror.api.moj.repository.UtilisationStatsRepository;
import uk.gov.hmcts.juror.api.moj.service.report.AttendanceReportService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.withinPercentage;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SuppressWarnings("PMD.SingularField") // will be fixed when tests are updated
class ManagementDashboardServiceImplTest {

    @Mock
    private UtilisationStatsRepository utilisationStatsRepository;

    @Mock
    private JurorPoolRepository jurorPoolRepository;

    @Mock
    private AttendanceReportService attendanceReportService;

    @Mock
    private CourtLocationRepository courtLocationRepository;

    @Mock
    private MessageRepository messageRepository;


    @InjectMocks
    private ManagementDashboardServiceImpl managementDashboardService;

    @BeforeEach
    void beforeEach() {
        utilisationStatsRepository = mock(UtilisationStatsRepository.class);
        jurorPoolRepository = mock(JurorPoolRepository.class);
        attendanceReportService = mock(AttendanceReportService.class);
        courtLocationRepository = mock(CourtLocationRepository.class);
        messageRepository = mock(MessageRepository.class);

        this.managementDashboardService =
            new ManagementDashboardServiceImpl(utilisationStatsRepository,
            jurorPoolRepository, attendanceReportService, courtLocationRepository, messageRepository);

        setSecurityContext();
    }


    @Test
    void overdueUtilisationReportNoData() {

        when(utilisationStatsRepository.getCourtUtilisationStats()).thenReturn(List.of());

        OverdueUtilisationReportResponseDto overdueUtilisationReportResponseDto =
                        managementDashboardService.getOverdueUtilisationReport(false);
        assertThat(overdueUtilisationReportResponseDto.getRecords()).isEmpty();

    }

    @Test
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void overdueUtilisationReportHappyPath() {

        LocalDate dateLastRun = LocalDate.now().minusMonths(2).withDayOfMonth(1);
        String dateThresholdExceeded = LocalDate.now().minusDays(32).atStartOfDay().toString()
                                                            .replace("T", " ") + ":05.0";
        String dateThresholdWithin30Days = LocalDate.now().minusDays(10).atStartOfDay().toString()
                                                            .replace("T", " ") + ":05.0";

        List<String> mockData = List.of("428,BLACKFRIARS,2024-11-01,452,52,2024-09-18 17:22:05.0",
                                        "415,CHESTER," + dateLastRun + ",456,56," + dateThresholdWithin30Days,
                                        "417,COVENTRY," + dateLastRun + ",452,52," + dateThresholdExceeded);

        when(utilisationStatsRepository.getCourtUtilisationStats()).thenReturn(mockData);

        OverdueUtilisationReportResponseDto overdueUtilisationReportResponseDto =
                        managementDashboardService.getOverdueUtilisationReport(false);
        assertThat(overdueUtilisationReportResponseDto.getRecords()).isNotEmpty();
        assertThat(overdueUtilisationReportResponseDto.getRecords()).hasSize(1);

        OverdueUtilisationReportResponseDto.OverdueUtilisationRecord utilisationRecord =
                        overdueUtilisationReportResponseDto.getRecords().get(0);

        assertThat(utilisationRecord.getCourt()).isEqualTo("COVENTRY (417)");
        assertThat(utilisationRecord.getDaysElapsed()).isEqualTo(32);
        assertThat(utilisationRecord.getUtilisation()).isCloseTo(11.507,  withinPercentage(0.1));
        assertThat(utilisationRecord.getReportLastRun()).isEqualTo(LocalDate.now().minusDays(32));

        verify(utilisationStatsRepository).getCourtUtilisationStats();


    }

    @Test
    void incompleteServiceReportNoData() {

        when(jurorPoolRepository.getIncompleteServiceCountsByCourt()).thenReturn(List.of());
        IncompleteServiceReportResponseDto incompleteServiceReportResponseDto =
                                        managementDashboardService.getIncompleteServiceReport();

        assertThat(incompleteServiceReportResponseDto.getRecords()).isEmpty();

        verify(jurorPoolRepository).getIncompleteServiceCountsByCourt();
    }

    @Test
    void weekendAttendanceReportNoData() {
        when(attendanceReportService.getWeekendAttendanceReport())
                                        .thenReturn(new WeekendAttendanceReportResponse(Map.of()));
        WeekendAttendanceReportResponseDto attendanceReportResponse =
                                        managementDashboardService.getWeekendAttendanceReport();
        assertThat(attendanceReportResponse.getRecords()).isEmpty();
        verify(attendanceReportService).getWeekendAttendanceReport();
    }

    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    void weekendAttendanceReportLimitToTen() {

        List<WeekendAttendanceReportResponse.TableData.DataRow> dataRows = new ArrayList<>();

        for (int i = 1; i <= 15; i++) {
            WeekendAttendanceReportResponse.TableData.DataRow row =
                new WeekendAttendanceReportResponse.TableData.DataRow();
            row.setCourtLocationNameAndCode("Court " + i + " (" + String.format("%03d", i) + ")");
            row.setSaturdayTotal(i);
            row.setSundayTotal(i);
            row.setHolidayTotal(i);
            row.setTotalPaid(BigDecimal.valueOf(i * 10.00));
            dataRows.add(row);
        }

        WeekendAttendanceReportResponse response = new WeekendAttendanceReportResponse(Map.of());

        response.getTableData().setData(dataRows);

        when(attendanceReportService.getWeekendAttendanceReport())
            .thenReturn(response);

        WeekendAttendanceReportResponseDto attendanceReportResponse =
                                managementDashboardService.getWeekendAttendanceReport();
        assertThat(attendanceReportResponse.getRecords()).isNotEmpty();
        assertThat(attendanceReportResponse.getRecords()).hasSize(10);
        verify(attendanceReportService).getWeekendAttendanceReport();
    }



    @Test
    void expenseLimitsReportNoData() {
        // Todo: need to confirm the functionality of this report before completing the test

        when(courtLocationRepository.getRecentlyUpdatedRecords()).thenReturn(List.of());

        ExpenseLimitsReportResponseDto dto = managementDashboardService.getExpenseLimitsReport();
        assertThat(dto.getRecords()).isEmpty();
        verify(courtLocationRepository).getRecentlyUpdatedRecords();
    }

    @Test
    void smsMessagesReportNoData() {

        when(messageRepository.getSmsMessageCounts()).thenReturn(List.of());

        SmsMessagesReportResponseDto dto = managementDashboardService.getSmsMessagesReport();
        assertThat(dto.getRecords()).isEmpty();

        verify(messageRepository).getSmsMessageCounts();
    }

    @Test
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    void smsMessagesReportLimitToTen() {

        // need more than 10 records to test the limit
        List<SmsMessagesReportResponseDto.SmsMessagesRecord> records = new ArrayList<>();

        for (int i = 1; i <= 15; i++) {
            records.add(new SmsMessagesReportResponseDto.SmsMessagesRecord(
                String.format("%03d", i), i));
        }

        when(messageRepository.getSmsMessageCounts()).thenReturn(records);

        SmsMessagesReportResponseDto dto = managementDashboardService.getSmsMessagesReport();
        assertThat(dto.getRecords()).isNotEmpty();
        assertThat(dto.getRecords()).hasSize(10);
        assertThat(dto.getTotalMessagesSent()).isEqualTo(120); // sum of all 15 records

        verify(messageRepository).getSmsMessageCounts();
    }

    private void setSecurityContext() {

        final BureauJwtPayload bureauJwtPayload = TestUtils.getJwtPayloadSuperUser("415", "Chester");

        BureauJwtAuthentication auth = mock(BureauJwtAuthentication.class);
        when(auth.getPrincipal()).thenReturn(bureauJwtPayload);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);

        SecurityContextHolder.setContext(securityContext);
    }

}
