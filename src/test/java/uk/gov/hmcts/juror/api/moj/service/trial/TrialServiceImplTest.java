package uk.gov.hmcts.juror.api.moj.service.trial;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.EndTrialDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorDetailRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.ReturnJuryDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.TrialDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.TrialSearch;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.TrialListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.TrialSummaryDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.trial.Courtroom;
import uk.gov.hmcts.juror.api.moj.domain.trial.Judge;
import uk.gov.hmcts.juror.api.moj.domain.trial.Panel;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.TrialType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.CourtroomRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.JudgeRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.PanelRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;
import uk.gov.hmcts.juror.api.moj.service.CompleteServiceServiceImpl;
import uk.gov.hmcts.juror.api.moj.service.JurorHistoryService;
import uk.gov.hmcts.juror.api.moj.service.expense.JurorExpenseService;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceService;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.juror.api.TestUtils.staffBuilder;

@ExtendWith(SpringExtension.class)
@SuppressWarnings({
    "PMD.ExcessiveImports",
    "PMD.TooManyMethods"
})
class TrialServiceImplTest {

    @Mock
    private TrialRepository trialRepository;
    @Mock
    private JudgeRepository judgeRepository;
    @Mock
    private CourtroomRepository courtroomRepository;
    @Mock
    private CourtLocationRepository courtLocationRepository;
    @Mock
    private PanelRepository panelRepository;
    @Mock
    private AppearanceRepository appearanceRepository;
    @Mock
    private JurorHistoryRepository jurorHistoryRepository;
    @Mock
    private JurorPoolRepository jurorPoolRepository;
    @Mock
    private CompleteServiceServiceImpl completeService;
    @Mock
    private JurorAppearanceService jurorAppearanceService;

    @Mock
    private JurorExpenseService jurorExpenseService;

    @Mock
    private JurorHistoryService jurorHistoryService;

    @InjectMocks
    TrialServiceImpl trialService;

    BureauJwtPayload payload = createJwtPayload("415", "COURT_USER");

    @Test
    void testCreateTrial() {
        TrialDto trialDto = createTrialDto();

        when(trialRepository.existsByTrialNumberAndCourtLocationLocCode(trialDto.getCaseNumber(),
            trialDto.getCourtLocation())).thenReturn(false);

        when(courtroomRepository.findById(trialDto.getCourtroomId())).thenReturn(Optional.of(createCourtroom()));

        when(courtLocationRepository.findByLocCode(trialDto.getCourtLocation())).thenReturn(
            Optional.of(createCourtLocation()));

        when(judgeRepository.findById(trialDto.getJudgeId())).thenReturn(Optional.of(createJudge()));

        TrialSummaryDto trialSummary = trialService.createTrial(
            createJwtPayload("415", "COURT_USER"), trialDto);

        verify(trialRepository, times(1))
            .existsByTrialNumberAndCourtLocationLocCode(trialDto.getCaseNumber(), trialDto.getCourtLocation());
        verify(courtroomRepository, times(1)).findById(anyLong());
        verify(courtLocationRepository, times(1)).findByLocCode(anyString());
        verify(judgeRepository, times(1)).findById(anyLong());
        verify(trialRepository, times(1)).save(any(Trial.class));

        assertThat(trialSummary.getTrialNumber()).isEqualTo("TEST000001");
        assertThat(trialSummary.getDefendants()).isEqualTo("Joe, Jo, Jon");
        assertThat(trialSummary.getTrialType()).isEqualTo("Criminal");
        assertThat(trialSummary.getJudge().getId()).isEqualTo(21L);
        assertThat(trialSummary.getJudge().getCode()).isEqualTo("1234");
        assertThat(trialSummary.getJudge().getDescription()).isEqualTo("Mr Judge");
        assertThat(trialSummary.getCourtroomsDto().getId()).isEqualTo(1L);
        assertThat(trialSummary.getCourtroomsDto().getRoomNumber()).isEqualTo("67");
        assertThat(trialSummary.getCourtroomsDto().getDescription()).isEqualTo("Courtroom 1");
        assertThat(trialSummary.getProtectedTrial()).isEqualTo(Boolean.TRUE);
        assertThat(trialSummary.getIsActive()).isEqualTo(Boolean.TRUE);
    }

    @Test
    void testCreateTrialAlreadyExists() {
        TrialDto trialDto = createTrialDto();

        when(trialRepository.existsByTrialNumberAndCourtLocationLocCode(trialDto.getCaseNumber(),
            trialDto.getCourtLocation())).thenReturn(true);

        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() -> trialService.createTrial(
            createJwtPayload("415", "COURT_USER"), trialDto));

        verify(courtroomRepository, never()).findById(anyLong());
        verify(courtLocationRepository, never()).findByLocCode(anyString());
        verify(judgeRepository, never()).findById(anyLong());
        verify(trialRepository, never()).save(any(Trial.class));
    }

    @Test
    @SuppressWarnings({
        "PMD.JUnitAssertionsShouldIncludeMessage",
        "unchecked"
    })
    void testGetTrials() {
        TrialSearch trialSearch = mock(TrialSearch.class);
        PaginatedList<TrialListDto> result = mock(PaginatedList.class);

        doReturn(result).when(trialRepository).getListOfTrials(any(), any());

        PaginatedList<TrialListDto> trials = trialService.getTrials(trialSearch);

        assertThat(trials).isEqualTo(result);
        verify(trialRepository, times(1))
            .getListOfTrials(eq(trialSearch), any());
    }

    @Test
    void testGetTrialSummary() {
        when(trialRepository.findByTrialNumberAndCourtLocationLocCode("T100000025", "415"))
            .thenReturn(Optional.of(createTrial("T100000025")));

        TrialSummaryDto trialSummary = trialService.getTrialSummary(
            createJwtPayload("415", "COURT_USER"), "T100000025", "415");

        verify(trialRepository, times(1))
            .findByTrialNumberAndCourtLocationLocCode(anyString(), anyString());
        verify(courtroomRepository, never()).findById(anyLong());
        verify(courtLocationRepository, never()).findByLocCode(anyString());
        verify(judgeRepository, never()).findById(anyLong());
        verify(trialRepository, never()).save(any(Trial.class));

        assertThat(trialSummary.getTrialNumber()).isEqualTo("T100000025");
        assertThat(trialSummary.getDefendants()).isEqualTo("Joe, Jo, Jon");
        assertThat(trialSummary.getTrialType()).isEqualTo("Criminal");
        assertThat(trialSummary.getJudge().getId()).isEqualTo(21L);
        assertThat(trialSummary.getJudge().getCode()).isEqualTo("1234");
        assertThat(trialSummary.getJudge().getDescription()).isEqualTo("Mr Judge");
        assertThat(trialSummary.getCourtroomsDto().getId()).isEqualTo(1L);
        assertThat(trialSummary.getCourtroomsDto().getRoomNumber()).isEqualTo("67");
        assertThat(trialSummary.getCourtroomsDto().getDescription()).isEqualTo("Courtroom 1");
        assertThat(trialSummary.getProtectedTrial()).isEqualTo(Boolean.TRUE);
        assertThat(trialSummary.getTrialEndDate()).isNull();
        assertThat(trialSummary.getIsActive()).isEqualTo(Boolean.TRUE);
    }

    @Test
    void testGetTrialSummaryInactiveTrial() {
        Trial inactiveTrial = createTrial("T100000025");
        inactiveTrial.setTrialEndDate(now());
        when(trialRepository.findByTrialNumberAndCourtLocationLocCode("T100000025", "415"))
            .thenReturn(Optional.of(inactiveTrial));

        TrialSummaryDto trialSummary = trialService.getTrialSummary(
            createJwtPayload("415", "COURT_USER"), "T100000025", "415");

        verify(trialRepository, times(1))
            .findByTrialNumberAndCourtLocationLocCode(anyString(), anyString());
        verify(courtroomRepository, never()).findById(anyLong());
        verify(courtLocationRepository, never()).findByLocCode(anyString());
        verify(judgeRepository, never()).findById(anyLong());
        verify(trialRepository, never()).save(any(Trial.class));

        assertThat(trialSummary.getTrialNumber()).isEqualTo("T100000025");
        assertThat(trialSummary.getDefendants()).isEqualTo("Joe, Jo, Jon");
        assertThat(trialSummary.getTrialType()).isEqualTo("Criminal");
        assertThat(trialSummary.getJudge().getId()).isEqualTo(21L);
        assertThat(trialSummary.getJudge().getCode()).isEqualTo("1234");
        assertThat(trialSummary.getJudge().getDescription()).isEqualTo("Mr Judge");
        assertThat(trialSummary.getCourtroomsDto().getId()).isEqualTo(1L);
        assertThat(trialSummary.getCourtroomsDto().getRoomNumber()).isEqualTo("67");
        assertThat(trialSummary.getCourtroomsDto().getDescription()).isEqualTo("Courtroom 1");
        assertThat(trialSummary.getProtectedTrial()).isEqualTo(Boolean.TRUE);
        assertThat(trialSummary.getTrialEndDate()).isEqualTo(now());
        assertThat(trialSummary.getIsActive()).isEqualTo(Boolean.FALSE);
    }

    @Test
    void testIsEmpanelledWithNoPanelledJurors() {
        when(trialRepository.findByTrialNumberAndCourtLocationLocCode("T100000025", "415"))
            .thenReturn(Optional.of(createTrial("T100000025")));
        when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", "415"))
            .thenReturn(createJurors(0, PanelResult.JUROR));
        TrialSummaryDto trialSummary = trialService.getTrialSummary(payload, "T100000025", "415");

        assertThat(trialSummary.getIsJuryEmpanelled()).isEqualTo(false);
    }

    @Test
    void testIsEmpanelledWithOneJurorStatusJuror() {
        when(trialRepository.findByTrialNumberAndCourtLocationLocCode("T100000025", "415"))
            .thenReturn(Optional.of(createTrial("T100000025")));
        when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", "415"))
            .thenReturn(createJurors(1, PanelResult.JUROR));
        TrialSummaryDto trialSummary = trialService.getTrialSummary(payload, "T100000025", "415");

        assertThat(trialSummary.getIsJuryEmpanelled()).isEqualTo(true);
    }

    @Test
    void testIsEmpanelledWithNoJurorStatusJurors() {
        when(trialRepository.findByTrialNumberAndCourtLocationLocCode("T100000025", "415"))
            .thenReturn(Optional.of(createTrial("T100000025")));
        when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", "415"))
            .thenReturn(createJurors(12, PanelResult.NOT_USED));

        TrialSummaryDto trialSummary = trialService.getTrialSummary(payload, "T100000025", "415");

        assertThat(trialSummary.getIsJuryEmpanelled()).isEqualTo(false);
    }

    @Test
    void testIsEmpanelledWithMixedStatusesWithOneJurorStatusJuror() {
        when(trialRepository.findByTrialNumberAndCourtLocationLocCode("T100000025", "415"))
            .thenReturn(Optional.of(createTrial("T100000025")));
        when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", "415"))
            .thenReturn(createJurors(12, PanelResult.JUROR));

        TrialSummaryDto trialSummary = trialService.getTrialSummary(payload, "T100000025", "415");

        assertThat(trialSummary.getIsJuryEmpanelled()).isEqualTo(true);
    }

    @Test
    void testReturnPanel() {
        final String trialNumber = "T100000000";
        List<Panel> panelMembers = createPanelMembers(10, null, trialNumber, IJurorStatus.PANEL);
        doReturn(panelMembers).when(panelRepository)
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415");

        trialService.returnPanel(payload, trialNumber, "415", createJurorDetailRequestDto(panelMembers));

        verify(panelRepository, times(1))
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415");
        verify(panelRepository, times(panelMembers.size())).saveAndFlush(any());
        verify(jurorHistoryRepository, times(panelMembers.size())).save(any());

    }

    @ParameterizedTest
    @ValueSource(strings = {"09:00", "null"})
    void testReturnJuryConfirmAttendance(String checkInTime) {
        final String trialNumber = "T100000000";
        List<Panel> panelMembers = createPanelMembers(10, PanelResult.JUROR, trialNumber, IJurorStatus.JUROR);
        when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415"))
            .thenReturn(panelMembers);


        for (Panel panel : panelMembers) {
            Appearance appearance = createAppearance(panel.getJurorNumber());

            if ("null".equals(checkInTime)) {
                appearance.setTimeIn(null);
                appearance.setTimeOut(null);
            } else {
                appearance.setTimeIn(LocalTime.parse(checkInTime));
                appearance.setTimeOut(LocalTime.parse(checkInTime));
            }

            when(jurorExpenseService.isLongTrialDay("415", panel.getJurorNumber(), now()))
                .thenReturn(false);
            when(appearanceRepository.findByJurorNumberAndAttendanceDate(panel.getJurorNumber(),
                now())).thenReturn(Optional.of(appearance));
        }

        trialService.returnJury(payload, trialNumber, "415",
            createReturnJuryDto(false, "09:00", "10:00"));

        ArgumentCaptor<Appearance> appearanceArgumentCaptor = ArgumentCaptor.forClass(Appearance.class);

        verify(panelRepository, times(1))
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415");
        verify(panelRepository, times(panelMembers.size())).saveAndFlush(any());
        verify(jurorHistoryRepository, times(panelMembers.size())).save(any());
        verify(appearanceRepository, times(panelMembers.size())).saveAndFlush(appearanceArgumentCaptor.capture());
        Appearance appearance = appearanceArgumentCaptor.getValue();
        assertThat(appearance.getSatOnJury()).as("Sat on Jury").isTrue();
        assertThat(appearance.getAttendanceType()).as("Attendance type").isEqualTo(AttendanceType.HALF_DAY);

    }

    @Test
    void testReturnJuryConfirmAttendanceFullDay() {
        final String trialNumber = "T100000000";
        List<Panel> panelMembers = createPanelMembers(10, PanelResult.JUROR, trialNumber, IJurorStatus.JUROR);
        when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415"))
            .thenReturn(panelMembers);


        for (Panel panel : panelMembers) {
            Appearance appearance = createAppearance(panel.getJurorNumber());

            appearance.setTimeIn(null);
            appearance.setTimeOut(null);

            when(jurorExpenseService.isLongTrialDay("415", panel.getJurorNumber(), now()))
                .thenReturn(false);
            when(appearanceRepository.findByJurorNumberAndAttendanceDate(panel.getJurorNumber(),
                now())).thenReturn(Optional.of(appearance));
        }

        trialService.returnJury(payload, trialNumber, "415",
            createReturnJuryDto(false, "09:00", "17:30"));

        ArgumentCaptor<Appearance> appearanceArgumentCaptor = ArgumentCaptor.forClass(Appearance.class);

        verify(panelRepository, times(1))
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415");
        verify(panelRepository, times(panelMembers.size())).saveAndFlush(any());
        verify(jurorHistoryRepository, times(panelMembers.size())).save(any());
        verify(appearanceRepository, times(panelMembers.size())).saveAndFlush(appearanceArgumentCaptor.capture());
        Appearance appearance = appearanceArgumentCaptor.getValue();
        assertThat(appearance.getSatOnJury()).as("Sat on Jury").isTrue();
        assertThat(appearance.getAttendanceType()).as("Attendance type").isEqualTo(AttendanceType.FULL_DAY);

    }

    @Test
    void testReturnJuryConfirmAttendanceFullDayLongTrial() {
        final String trialNumber = "T100000000";
        List<Panel> panelMembers = createPanelMembers(10, PanelResult.JUROR, trialNumber, IJurorStatus.JUROR);
        when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415"))
            .thenReturn(panelMembers);


        for (Panel panel : panelMembers) {
            Appearance appearance = createAppearance(panel.getJurorNumber());

            appearance.setTimeIn(null);
            appearance.setTimeOut(null);

            when(jurorExpenseService.isLongTrialDay("415", panel.getJurorNumber(), now()))
                .thenReturn(true);
            when(appearanceRepository.findByJurorNumberAndAttendanceDate(panel.getJurorNumber(),
                now())).thenReturn(Optional.of(appearance));
        }

        trialService.returnJury(payload, trialNumber, "415",
            createReturnJuryDto(false, "09:00", "17:30"));

        ArgumentCaptor<Appearance> appearanceArgumentCaptor = ArgumentCaptor.forClass(Appearance.class);

        verify(panelRepository, times(1))
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415");
        verify(panelRepository, times(panelMembers.size())).saveAndFlush(any());
        verify(jurorHistoryRepository, times(panelMembers.size())).save(any());
        verify(appearanceRepository, times(panelMembers.size())).saveAndFlush(appearanceArgumentCaptor.capture());
        Appearance appearance = appearanceArgumentCaptor.getValue();
        assertThat(appearance.getSatOnJury()).as("Sat on Jury").isTrue();
        assertThat(appearance.getAttendanceType()).as("Attendance type").isEqualTo(AttendanceType.FULL_DAY_LONG_TRIAL);

    }

    @Test
    void testReturnJuryConfirmAttendanceHalfDayLongTrial() {
        final String trialNumber = "T100000000";
        List<Panel> panelMembers = createPanelMembers(10, PanelResult.JUROR, trialNumber, IJurorStatus.JUROR);
        when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415"))
            .thenReturn(panelMembers);


        for (Panel panel : panelMembers) {
            Appearance appearance = createAppearance(panel.getJurorNumber());

            appearance.setTimeIn(null);
            appearance.setTimeOut(null);

            when(jurorExpenseService.isLongTrialDay("415", panel.getJurorNumber(), now()))
                .thenReturn(true);
            when(appearanceRepository.findByJurorNumberAndAttendanceDate(panel.getJurorNumber(),
                now())).thenReturn(Optional.of(appearance));
        }

        trialService.returnJury(payload, trialNumber, "415",
            createReturnJuryDto(false, "09:00", "11:30"));

        ArgumentCaptor<Appearance> appearanceArgumentCaptor = ArgumentCaptor.forClass(Appearance.class);

        verify(panelRepository, times(1))
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415");
        verify(panelRepository, times(panelMembers.size())).saveAndFlush(any());
        verify(jurorHistoryRepository, times(panelMembers.size())).save(any());
        verify(appearanceRepository, times(panelMembers.size())).saveAndFlush(appearanceArgumentCaptor.capture());
        Appearance appearance = appearanceArgumentCaptor.getValue();
        assertThat(appearance.getSatOnJury()).as("Sat on Jury").isTrue();
        assertThat(appearance.getAttendanceType()).as("Attendance type").isEqualTo(AttendanceType.HALF_DAY_LONG_TRIAL);

    }


    @Test
    void testReturnJuryNoConfirmAttendanceNullTimes() {
        final String trialNumber = "T100000000";
        List<Panel> panelMembers = createPanelMembers(10, PanelResult.JUROR, trialNumber, IJurorStatus.JUROR);
        doReturn(panelMembers).when(panelRepository)
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415");

        for (Panel panel : panelMembers) {
            Appearance appearance = createAppearance(panel.getJurorNumber());
            appearance.setTimeIn(null);
            when(appearanceRepository.findByJurorNumberAndAttendanceDate(panel.getJurorNumber(),
                now())).thenReturn(Optional.of(appearance));
        }

        trialService
            .returnJury(payload, trialNumber, "415",
                createReturnJuryDto(false, "09:30", null));

        verify(panelRepository, times(1))
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415");
        verify(panelRepository, times(panelMembers.size())).saveAndFlush(any());
        verify(jurorHistoryRepository, times(panelMembers.size())).save(any());
        ArgumentCaptor<Appearance> appearanceArgumentCaptor = ArgumentCaptor.forClass(Appearance.class);
        verify(appearanceRepository, times(panelMembers.size())).saveAndFlush(appearanceArgumentCaptor.capture());
    }

    @Test
    void testReturnJuryConfirmAttendanceCompleteService() {
        final String trialNumber = "T100000000";
        List<Panel> panelMembers = createPanelMembers(10, PanelResult.JUROR, trialNumber, IJurorStatus.JUROR);
        doReturn(panelMembers).when(panelRepository)
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415");

        for (Panel panel : panelMembers) {
            createJurorPool(panel.getJuror(), panel.getTrial().getCourtLocation(), IJurorStatus.JUROR);
            Appearance appearance = createAppearance(panel.getJurorNumber());
            when(appearanceRepository.findByJurorNumberAndAttendanceDate(panel.getJurorNumber(),
                now())).thenReturn(Optional.of(appearance));
        }

        trialService.returnJury(payload, trialNumber, "415",
            createReturnJuryDto(true, "09:00", "10:00"));

        verify(panelRepository, times(1))
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415");
        verify(panelRepository, times(panelMembers.size())).saveAndFlush(any());
        verify(completeService, times(panelMembers.size())).completeService(anyString(), any());
        verify(jurorHistoryRepository, times(panelMembers.size())).save(any());
        ArgumentCaptor<Appearance> appearanceArgumentCaptor = ArgumentCaptor.forClass(Appearance.class);
        verify(appearanceRepository, times(panelMembers.size())).saveAndFlush(appearanceArgumentCaptor.capture());
        assertThat(appearanceArgumentCaptor.getValue().getSatOnJury()).as("Sat on Jury").isTrue();
    }

    @Test
    void testEndTrialHappyPath() {
        final String trialNumber = "T100000000";
        when(trialRepository.findByTrialNumberAndCourtLocationLocCode(trialNumber, "415"))
            .thenReturn(Optional.of(createTrial(trialNumber)));
        when(panelRepository.retrieveMembersOnTrial(trialNumber, "415"))
            .thenReturn(new ArrayList<>());

        trialService.endTrial(createEndTrialDto());

        verify(panelRepository, times(1)).retrieveMembersOnTrial(anyString(), anyString());
        verify(trialRepository, times(1)).save(any());
    }

    @Test
    void testEndTrialMembersInTrial() {
        final String trialNumber = "T100000000";
        List<Panel> panelMembers = createPanelMembers(10, PanelResult.JUROR, trialNumber, IJurorStatus.JUROR);
        when(panelRepository.retrieveMembersOnTrial(trialNumber, "415"))
            .thenReturn(panelMembers);

        Assertions.assertThrows(
            MojException.BusinessRuleViolation.class, () -> trialService.endTrial(createEndTrialDto()));
    }

    @Test
    void testEndTrialCannotFindTrial() {
        final String trialNumber = "T100000000";
        when(trialRepository.findByTrialNumberAndCourtLocationLocCode(trialNumber, "415"))
            .thenReturn(Optional.of(createTrial(trialNumber)));
        EndTrialDto dto = createEndTrialDto();
        dto.setTrialNumber("T1");
        Assertions.assertThrows(MojException.NotFound.class, () -> trialService.endTrial(dto));
    }

    private Trial createTrial(String trialNumber) {
        Trial trial = new Trial();
        trial.setTrialNumber(trialNumber);
        trial.setCourtLocation(createCourtLocation());
        trial.setDescription("Joe, Jo, Jon");
        trial.setCourtroom(createCourtroom());
        trial.setJudge(createJudge());
        trial.setTrialType(TrialType.CRI);
        trial.setTrialStartDate(now().plusMonths(1));
        trial.setAnonymous(Boolean.TRUE);

        return trial;
    }

    private List<Panel> createJurors(int count, PanelResult status) {
        List<Panel> jury = new ArrayList<>();
        IntStream.rangeClosed(1, count).forEach(i -> {
            Panel panel = new Panel();
            if (i == count) {
                panel.setResult(status);
            }

            jury.add(panel);
        });

        return jury;
    }

    private List<String> createCourtList() {
        List<String> courts = new ArrayList<>();
        courts.add("415");
        courts.add("462");

        return courts;
    }

    private BureauJwtPayload createJwtPayload(String owner, String userType) {
        BureauJwtPayload bureauJwtPayload = new BureauJwtPayload();

        List<String> courtList = createCourtList();
        bureauJwtPayload.setStaff(staffBuilder("Ms Bean", 1, courtList));

        bureauJwtPayload.setOwner(owner);
        bureauJwtPayload.setLogin(userType);
        bureauJwtPayload.setUserLevel("99");
        return bureauJwtPayload;
    }

    private TrialDto createTrialDto() {
        TrialDto trialDto = new TrialDto();
        trialDto.setCaseNumber("TEST000001");
        trialDto.setTrialType(TrialType.CRI);
        trialDto.setDefendant("Joe, Jo, Jon");
        trialDto.setStartDate(now().plusMonths(1));
        trialDto.setJudgeId(21L);
        trialDto.setCourtLocation("415");
        trialDto.setCourtroomId(66L);
        trialDto.setProtectedTrial(Boolean.TRUE);

        return trialDto;
    }

    private Courtroom createCourtroom() {
        CourtLocation courtLocation = CourtLocation.builder()
            .owner("415")
            .locCode("415")
            .build();
        Courtroom courtroom = new Courtroom();
        courtroom.setId(1L);
        courtroom.setCourtLocation(courtLocation);
        courtroom.setRoomNumber("67");
        courtroom.setDescription("Courtroom 1");

        return courtroom;
    }

    private CourtLocation createCourtLocation() {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("415");
        courtLocation.setOwner("415");
        courtLocation.setName("CHESTER");

        return courtLocation;
    }

    private Judge createJudge() {
        Judge judge = new Judge();
        judge.setId(21L);
        judge.setOwner("415");
        judge.setCode("1234");
        judge.setName("Mr Judge");

        return judge;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private ReturnJuryDto createReturnJuryDto(boolean completeServiceFlag, String checkIn, String checkOut) {
        ReturnJuryDto dto = new ReturnJuryDto();

        dto.setCheckIn(checkIn);
        dto.setCheckOut(checkOut);
        dto.setCompleted(completeServiceFlag);

        List<JurorDetailRequestDto> jurorDetailRequestDtos = new ArrayList<>();

        for (int i = 0;
             i < 10;
             i++) {
            JurorDetailRequestDto detailRequestDto = new JurorDetailRequestDto();
            detailRequestDto.setFirstName("FNAME");
            detailRequestDto.setLastName("LNAME");
            detailRequestDto.setJurorNumber(String.format("1111111%02d", i + 1));
            detailRequestDto.setResult(PanelResult.JUROR);
            jurorDetailRequestDtos.add(detailRequestDto);
        }
        dto.setJurors(jurorDetailRequestDtos);

        return dto;
    }

    public List<Panel> createPanelMembers(int totalMembers, PanelResult panelResult, String trialNumber, int status) {
        List<Panel> panelList = new ArrayList<>();
        String jurorNumber = "1111111%02d";
        for (int i = 0;
             i < totalMembers;
             i++) {
            Panel temp = createSinglePanelData(panelResult, trialNumber, status, String.format(jurorNumber, i + 1));
            temp.getJuror().setJurorNumber(jurorNumber.formatted(i + 1));
            temp.setResult(panelResult);
            panelList.add(temp);

        }
        return panelList;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public List<JurorDetailRequestDto> createJurorDetailRequestDto(List<Panel> panelList) {
        List<JurorDetailRequestDto> dtoList = new ArrayList<>();
        for (Panel panel : panelList) {
            JurorDetailRequestDto dto = new JurorDetailRequestDto();
            dto.setResult(panel.getResult());
            dto.setFirstName(panel.getJuror().getFirstName());
            dto.setFirstName(panel.getJuror().getLastName());
            dto.setJurorNumber(panel.getJurorNumber());
            dtoList.add(dto);
        }
        return dtoList;
    }

    private Panel createSinglePanelData(PanelResult panelResult, String trialNumber, int jurorStatusValue,
                                        String jurorNumber) {

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);
        juror.setFirstName("FNAME");
        juror.setLastName("LNAME");

        Panel panel = new Panel();
        panel.setJuror(juror);
        panel.setCompleted(false);
        panel.setTrial(createTrial(trialNumber));
        panel.setResult(panelResult);

        createJurorPool(juror, panel.getTrial().getCourtLocation(), jurorStatusValue);

        return panel;
    }

    private Appearance createAppearance(String jurorNumber) {
        CourtLocation courtlocation = new CourtLocation();
        courtlocation.setLocCode("415");
        courtlocation.setName("Chester");

        Appearance appearance = new Appearance();
        appearance.setAttendanceDate(now());
        appearance.setTimeIn(LocalTime.now());
        appearance.setJurorNumber(jurorNumber);
        appearance.setCourtLocation(courtlocation);

        return appearance;
    }

    private EndTrialDto createEndTrialDto() {
        EndTrialDto dto = new EndTrialDto();
        dto.setTrialEndDate(now());
        dto.setTrialNumber("T100000000");
        dto.setLocationCode("415");
        return dto;
    }

    private JurorPool createJurorPool(Juror juror, CourtLocation courtLocation, int jurorStatusValue) {

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(jurorStatusValue);
        jurorStatus.setActive(true);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("111111111");
        poolRequest.setCourtLocation(courtLocation);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("415");
        jurorPool.setJuror(juror);
        jurorPool.setPool(poolRequest);
        jurorPool.setStatus(jurorStatus);
        jurorPool.setLocation("Court 1");

        doReturn(jurorPool).when(jurorPoolRepository).findByJurorNumberAndIsActiveAndCourt(eq(juror.getJurorNumber()),
            eq(true), any(CourtLocation.class));

        return jurorPool;
    }
}

