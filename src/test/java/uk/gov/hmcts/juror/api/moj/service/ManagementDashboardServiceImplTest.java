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
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.IncompleteServiceReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.OverdueUtilisationReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.WeekendAttendanceReportResponseDto;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.UtilisationStatsRepository;
import uk.gov.hmcts.juror.api.moj.service.report.AttendanceReportService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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


    @InjectMocks
    private ManagementDashboardServiceImpl managementDashboardService;

    @BeforeEach
    void beforeEach() {
        utilisationStatsRepository = mock(UtilisationStatsRepository.class);
        jurorPoolRepository = mock(JurorPoolRepository.class);
        attendanceReportService = mock(AttendanceReportService.class);
        courtLocationRepository = mock(CourtLocationRepository.class);

        this.managementDashboardService =
            new ManagementDashboardServiceImpl(utilisationStatsRepository,
            jurorPoolRepository, attendanceReportService, courtLocationRepository);

        setSecurityContext();
    }


    @Test
    void overdueUtilisationReportNoData() {

        OverdueUtilisationReportResponseDto overdueUtilisationReportResponseDto =
            managementDashboardService.getOverdueUtilisationReport(false);
        assertThat(overdueUtilisationReportResponseDto.getRecords()).isEmpty();

    }

    @Test
    void incompleteServiceReportNoData() {

        IncompleteServiceReportResponseDto incompleteServiceReportResponseDto =
            managementDashboardService.getIncompleteServiceReport();

        assertThat(incompleteServiceReportResponseDto.getRecords()).isEmpty();
    }

    @Test
    void weekendAttendanceReportNoData() {

        WeekendAttendanceReportResponseDto attendanceReportResponse =
            managementDashboardService.getWeekendAttendanceReport();
        assertThat(attendanceReportResponse.getRecords()).isEmpty();
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
