package uk.gov.hmcts.juror.api.moj.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.moj.controller.courtdashboard.CourtAdminInfoDto;
import uk.gov.hmcts.juror.api.moj.controller.courtdashboard.CourtAttendanceInfoDto;
import uk.gov.hmcts.juror.api.moj.controller.courtdashboard.CourtNotificationInfoDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PendingJurorsResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.PendingJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.repository.PendingJurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.UtilisationStatsRepository;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceService;
import uk.gov.hmcts.juror.api.moj.service.report.UtilisationReportService;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.JurorResponseService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(SpringExtension.class)
class CourtDashboardServiceImplTest {


    @Mock
    private PendingJurorRepository pendingJurorRepository;
    @Mock
    private JurorResponseService jurorResponseService;
    @Mock
    private JurorPoolService jurorPoolService;
    @Mock
    private JurorAppearanceService appearanceService;
    @Mock
    private UtilisationStatsRepository utilisationStatsRepository;
    @Mock
    private UtilisationReportService utilisationReportService;

    private static final String LOC_CODE = "415";


    @InjectMocks
    private CourtDashboardServiceImpl courtDashboardService;

    @Test
    void positiveGetCourtNotificationsSjo() {

        TestUtils.mockCourtUser("415", "415", Set.of(Role.SENIOR_JUROR_OFFICER));

        doReturn(2).when(jurorResponseService).getOpenSummonsRepliesCount(LOC_CODE);

        PendingJurorsResponseDto.PendingJurorsResponseData pendingJurorsResponseData =
            PendingJurorsResponseDto.PendingJurorsResponseData
                .builder()
                .jurorNumber("1234567890")
                .firstName("John")
                .lastName("Doe")
                .notes("Test note")
                .postcode("AB12 3CD")
                .build();

        doReturn(List.of(pendingJurorsResponseData)).when(pendingJurorRepository)
            .findPendingJurorsForCourt(anyString(), any(PendingJurorStatus.class));

        CourtNotificationInfoDto dto = courtDashboardService.getCourtNotifications(LOC_CODE);

        // Verify that the dto is not null and contains the expected values
        assertThat(dto).isNotNull();
        assertThat(dto.getOpenSummonsReplies()).isEqualTo(2);
        assertThat(dto.getPendingJurors()).isEqualTo(1);

        // verify the method calls
        verify(jurorResponseService, times(1)).getOpenSummonsRepliesCount(LOC_CODE);
        verify(pendingJurorRepository, times(1)).findPendingJurorsForCourt(anyString(), any(PendingJurorStatus.class));

    }

    @Test
    void getCourtNotificationsSjoNoData() {

        TestUtils.mockCourtUser("415", "415", Set.of(Role.SENIOR_JUROR_OFFICER));

        CourtNotificationInfoDto dto = courtDashboardService.getCourtNotifications(LOC_CODE);

        // Verify that the dto is not null
        assertThat(dto).isNotNull();

        // verify the method calls
        verify(jurorResponseService, times(1)).getOpenSummonsRepliesCount(LOC_CODE);
        verify(pendingJurorRepository, times(1)).findPendingJurorsForCourt(anyString(), any(PendingJurorStatus.class));

    }


    @Test
    void positiveGetCourtNotifications() {

        TestUtils.mockCourtUser("415", "415", Collections.emptySet());
        doReturn(2).when(jurorResponseService).getOpenSummonsRepliesCount(LOC_CODE);
        CourtNotificationInfoDto dto = courtDashboardService.getCourtNotifications(LOC_CODE);

        // Verify that the dto is not null and contains the expected values
        assertThat(dto).isNotNull();
        assertThat(dto.getOpenSummonsReplies()).isEqualTo(2);
        assertThat(dto.getPendingJurors()).isZero();

        verify(jurorResponseService, times(1)).getOpenSummonsRepliesCount(LOC_CODE);
        verifyNoInteractions(pendingJurorRepository);
    }

    @Test
    void getCourtAdminInfoNoData() {
        TestUtils.mockCourtUser("415", "415", Collections.emptySet());
        CourtAdminInfoDto dto = courtDashboardService.getCourtAdminInfo(LOC_CODE);
        assertThat(dto).isNotNull();
        assertThat(dto.getUnpaidAttendances()).isZero();

        verify(appearanceService, times(1)).getUnpaidAttendancesAtCourt(LOC_CODE);
        verify(utilisationStatsRepository, times(1)).findTop12ByLocCodeOrderByMonthStartDesc(LOC_CODE);
    }

    @Test
    void getCourtAttendanceInfoNoUtilStats() {
        TestUtils.mockCourtUser("415", "415", Collections.emptySet());
        CourtAttendanceInfoDto dto = courtDashboardService.getCourtAttendanceInfo(LOC_CODE);
        assertThat(dto).isNotNull();

        verify(jurorPoolService, times(1)).getCountJurorsDueToAttendCourtNextWeek(LOC_CODE, false);
        verify(jurorPoolService, times(1)).getCountJurorsDueToAttendCourtNextWeek(LOC_CODE, true);
        verify(utilisationReportService, times(1)).viewDailyUtilisationReport(LOC_CODE,  LocalDate.now().minusDays(7),
                                                                              LocalDate.now());
        verify(appearanceService, times(1)).getUnconfirmedAttendanceCountAtCourt(LOC_CODE);
        verifyNoMoreInteractions(appearanceService);

    }

}
