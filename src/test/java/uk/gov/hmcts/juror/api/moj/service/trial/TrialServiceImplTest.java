package uk.gov.hmcts.juror.api.moj.service.trial;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.EndTrialDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorDetailRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorPanelReassignRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.ReinstateJurorsRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.ReturnJuryDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.TrialDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.TrialSearch;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.PanelListDto;
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
import uk.gov.hmcts.juror.api.moj.service.AppearanceCreationServiceImpl;
import uk.gov.hmcts.juror.api.moj.service.CompleteServiceServiceImpl;
import uk.gov.hmcts.juror.api.moj.service.JurorHistoryService;
import uk.gov.hmcts.juror.api.moj.service.expense.JurorExpenseService;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
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

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private AppearanceCreationServiceImpl appearanceCreationService;

    @InjectMocks
    TrialServiceImpl trialService;

    BureauJwtPayload payload = createJwtPayload("415", "COURT_USER");


    @BeforeEach
    void beforeEach() {
        doAnswer(invocation -> invocation.getArgument(0)).when(appearanceCreationService)
            .addStandardAttributes(any(), any(), any(), any());
    }

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
        verify(jurorHistoryService, times(panelMembers.size())).createReturnFromPanelHistory(any(), any());
    }

    @Test
    void testReturnJuryConfirmAttendance() {
        final String trialNumber = "T100000000";
        List<Panel> panelMembers = createPanelMembers(10, PanelResult.JUROR, trialNumber, IJurorStatus.JUROR);
        when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415"))
            .thenReturn(panelMembers);

        doAnswer(invocationOnMock -> {
            Appearance appearance = invocationOnMock.getArgument(0);
            appearance.setAttendanceType(AttendanceType.HALF_DAY);
            return null;
        }).when(jurorAppearanceService).realignAttendanceType(any(Appearance.class));

        trialService.returnJury(payload, trialNumber, "415",
            createReturnJuryDto(false, "09:00", "10:00"));

        ArgumentCaptor<Appearance> appearanceArgumentCaptor = ArgumentCaptor.forClass(Appearance.class);

        verify(panelRepository, times(1))
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415");
        verify(panelRepository, times(panelMembers.size())).saveAndFlush(any());
        verify(jurorHistoryService, times(panelMembers.size())).createJuryAttendanceHistory(any(), any(), any());
        verify(jurorAppearanceService, times(panelMembers.size()))
            .realignAttendanceType(appearanceArgumentCaptor.capture());
        Appearance appearance = appearanceArgumentCaptor.getValue();
        assertThat(appearance.getSatOnJury()).as("Sat on Jury").isTrue();
        assertThat(appearance.getAttendanceType()).as("Attendance type").isEqualTo(AttendanceType.HALF_DAY);
        verify(jurorExpenseService, times(panelMembers.size()))
            .applyDefaultExpenses(any(), any());
    }

    @Test
    void testReturnJuryConfirmAttendanceFullDay() {
        final String trialNumber = "T100000000";
        List<Panel> panelMembers = createPanelMembers(10, PanelResult.JUROR, trialNumber, IJurorStatus.JUROR);
        when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415"))
            .thenReturn(panelMembers);

        doAnswer(invocationOnMock -> {
            Appearance appearance = invocationOnMock.getArgument(0);
            appearance.setAttendanceType(AttendanceType.FULL_DAY);
            return null;
        }).when(jurorAppearanceService).realignAttendanceType(any(Appearance.class));

        trialService.returnJury(payload, trialNumber, "415",
            createReturnJuryDto(false, "09:00", "17:30"));

        ArgumentCaptor<Appearance> appearanceArgumentCaptor = ArgumentCaptor.forClass(Appearance.class);

        verify(panelRepository, times(1))
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415");
        verify(panelRepository, times(panelMembers.size())).saveAndFlush(any());
        verify(jurorHistoryService, times(panelMembers.size())).createJuryAttendanceHistory(any(), any(), any());
        verify(jurorAppearanceService, times(panelMembers.size()))
            .realignAttendanceType(appearanceArgumentCaptor.capture());
        Appearance appearance = appearanceArgumentCaptor.getValue();
        assertThat(appearance.getSatOnJury()).as("Sat on Jury").isTrue();
        assertThat(appearance.getAttendanceType()).as("Attendance type").isEqualTo(AttendanceType.FULL_DAY);
        verify(jurorExpenseService, times(panelMembers.size()))
            .applyDefaultExpenses(any(), any());

    }

    @Test
    void testReturnJuryConfirmAttendanceFullDayLongTrial() {
        final String trialNumber = "T100000000";
        List<Panel> panelMembers = createPanelMembers(10, PanelResult.JUROR, trialNumber, IJurorStatus.JUROR);
        when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415"))
            .thenReturn(panelMembers);

        doAnswer(invocationOnMock -> {
            Appearance appearance = invocationOnMock.getArgument(0);
            appearance.setAttendanceType(AttendanceType.FULL_DAY_LONG_TRIAL);
            return null;
        }).when(jurorAppearanceService).realignAttendanceType(any(Appearance.class));

        trialService.returnJury(payload, trialNumber, "415",
            createReturnJuryDto(false, "09:00", "17:30"));

        ArgumentCaptor<Appearance> appearanceArgumentCaptor = ArgumentCaptor.forClass(Appearance.class);

        verify(panelRepository, times(1))
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415");
        verify(panelRepository, times(panelMembers.size())).saveAndFlush(any());
        verify(jurorHistoryService, times(panelMembers.size())).createJuryAttendanceHistory(any(), any(), any());
        verify(jurorAppearanceService, times(panelMembers.size()))
            .realignAttendanceType(appearanceArgumentCaptor.capture());
        Appearance appearance = appearanceArgumentCaptor.getValue();
        assertThat(appearance.getSatOnJury()).as("Sat on Jury").isTrue();
        assertThat(appearance.getAttendanceType()).as("Attendance type").isEqualTo(AttendanceType.FULL_DAY_LONG_TRIAL);
        verify(jurorExpenseService, times(panelMembers.size()))
            .applyDefaultExpenses(any(), any());
    }

    @Test
    void testReturnJuryConfirmAttendanceHalfDayLongTrial() {
        final String trialNumber = "T100000000";
        List<Panel> panelMembers = createPanelMembers(10, PanelResult.JUROR, trialNumber, IJurorStatus.JUROR);
        when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415"))
            .thenReturn(panelMembers);

        doAnswer(invocationOnMock -> {
            Appearance appearance = invocationOnMock.getArgument(0);
            appearance.setAttendanceType(AttendanceType.HALF_DAY_LONG_TRIAL);
            return null;
        }).when(jurorAppearanceService).realignAttendanceType(any(Appearance.class));

        trialService.returnJury(payload, trialNumber, "415",
            createReturnJuryDto(false, "09:00", "11:30"));

        ArgumentCaptor<Appearance> appearanceArgumentCaptor = ArgumentCaptor.forClass(Appearance.class);

        verify(panelRepository, times(1))
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415");
        verify(panelRepository, times(panelMembers.size())).saveAndFlush(any());
        verify(jurorHistoryService, times(panelMembers.size())).createJuryAttendanceHistory(any(), any(), any());
        verify(jurorAppearanceService, times(panelMembers.size()))
            .realignAttendanceType(appearanceArgumentCaptor.capture());
        Appearance appearance = appearanceArgumentCaptor.getValue();
        assertThat(appearance.getSatOnJury()).as("Sat on Jury").isTrue();
        assertThat(appearance.getAttendanceType()).as("Attendance type").isEqualTo(AttendanceType.HALF_DAY_LONG_TRIAL);
        verify(jurorExpenseService, times(panelMembers.size()))
            .applyDefaultExpenses(any(), any());
    }


    @Test
    void testReturnJuryNoConfirmAttendanceNullTimes() {
        final String trialNumber = "T100000000";
        List<Panel> panelMembers = createPanelMembers(10, PanelResult.JUROR, trialNumber, IJurorStatus.JUROR);
        doReturn(panelMembers).when(panelRepository)
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415");

        trialService
            .returnJury(payload, trialNumber, "415",
                createReturnJuryDto(false, "09:30", null));

        verify(panelRepository, times(1))
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415");
        verify(panelRepository, times(panelMembers.size())).saveAndFlush(any());
        verify(jurorAppearanceService, never()).realignAttendanceType(anyString());
        verify(jurorHistoryService, never()).createJuryAttendanceHistory(any(), any(), any());
        verifyNoInteractions(jurorExpenseService);
    }

    @Test
    void testReturnJuryConfirmAttendanceCompleteService() {
        final String trialNumber = "T100000000";
        List<Panel> panelMembers = createPanelMembers(10, PanelResult.JUROR, trialNumber, IJurorStatus.JUROR);
        doReturn(panelMembers).when(panelRepository)
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415");

        trialService.returnJury(payload, trialNumber, "415",
            createReturnJuryDto(true, "09:00", "10:00"));

        verify(panelRepository, times(1))
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415");
        verify(panelRepository, times(panelMembers.size())).saveAndFlush(any());
        verify(completeService, times(panelMembers.size())).completeServiceSingle(any(), any());
        verify(jurorHistoryService, times(panelMembers.size())).createJuryAttendanceHistory(any(), any(), any());
        ArgumentCaptor<Appearance> appearanceArgumentCaptor = ArgumentCaptor.forClass(Appearance.class);
        verify(jurorAppearanceService, times(panelMembers.size()))
            .realignAttendanceType(appearanceArgumentCaptor.capture());
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

    @Nested
    class ReassignPanelMembers {

        @Test
        void reassignPanelMembersHappy() {

            payload = TestUtils.createJwt("415", "COURT_USER", "1", Collections.singletonList("415"));
            TestUtils.mockSecurityUtil(payload);
            final String sourceTrialNumber = "T100000000";
            final String targetTrialNumber = "T100000001";
            final String locCode = "415";
            final List<String> jurors = Arrays.asList("111111101", "111111102", "111111103");
            List<Panel> panelMembers = createPanelMembers(10, null, sourceTrialNumber, IJurorStatus.PANEL);

            Trial trial = createTrial(targetTrialNumber);
            when(trialRepository.findByTrialNumberAndCourtLocationLocCode(targetTrialNumber, locCode))
                .thenReturn(Optional.of(trial));

            when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(sourceTrialNumber, "415"))
                .thenReturn(panelMembers);

            when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(targetTrialNumber, "415"))
                .thenReturn(Collections.emptyList());

            trialService.reassignPanelMembers(createReassignPanelMembersRequestDto(
                jurors, sourceTrialNumber, targetTrialNumber, locCode));

            verify(panelRepository, times(1))
                .findByTrialTrialNumberAndTrialCourtLocationLocCode(sourceTrialNumber, "415");
            verify(panelRepository, times(6)).saveAndFlush(any()); // 2 * 3 jurors
            verify(jurorHistoryService, times(jurors.size())).createReassignedToPanelHistory(any(), any());
        }

        @Test
        void reassignPanelMembersTrialEnded() {

            payload = TestUtils.createJwt("415", "COURT_USER", "1", Collections.singletonList("415"));
            TestUtils.mockSecurityUtil(payload);
            final String sourceTrialNumber = "T100000000";
            final String targetTrialNumber = "T100000001";
            final String locCode = "415";
            final List<String> jurors = Arrays.asList("111111101", "111111102", "111111103");

            Trial trial = createTrial(targetTrialNumber);
            trial.setTrialEndDate(now());

            when(trialRepository.findByTrialNumberAndCourtLocationLocCode(targetTrialNumber, locCode))
                .thenReturn(Optional.of(trial));

            assertThatExceptionOfType(MojException.BusinessRuleViolation.class).isThrownBy(() ->
                                                                           trialService.reassignPanelMembers(
                                                                               createReassignPanelMembersRequestDto(
                                                                                   jurors,
                                                                                   sourceTrialNumber,
                                                                                   targetTrialNumber,
                                                                                   locCode
                                                                               )));

            verify(panelRepository, never()).findByTrialTrialNumberAndTrialCourtLocationLocCode(
                                                                            anyString(), anyString());
            verify(panelRepository, never()).saveAndFlush(any());
            verify(jurorHistoryService, never()).createReassignedToPanelHistory(any(), any());

        }

        @Test
        void reassignPanelMembersSameTrialNumbers() {

            payload = TestUtils.createJwt("415", "COURT_USER", "1", Collections.singletonList("415"));
            TestUtils.mockSecurityUtil(payload);
            final String sourceTrialNumber = "T100000000";
            final String targetTrialNumber = "T100000000";
            final String locCode = "415";
            final List<String> jurors = Arrays.asList("111111101", "111111102", "111111103");

            assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
                                                                    trialService.reassignPanelMembers(
                                                                        createReassignPanelMembersRequestDto(
                                                                            jurors,
                                                                            sourceTrialNumber,
                                                                            targetTrialNumber,
                                                                            locCode
                                                                        )));

            verify(panelRepository, never()).findByTrialTrialNumberAndTrialCourtLocationLocCode(
                anyString(),
                anyString()
            );
            verify(panelRepository, never()).saveAndFlush(any());
            verify(jurorHistoryService, never()).createReassignedToPanelHistory(any(), any());

        }

        @Test
        void reassignPanelMembersInvalidCourtLocation() {

            payload = TestUtils.createJwt("415", "COURT_USER", "1", Collections.singletonList("415"));
            TestUtils.mockSecurityUtil(payload);
            final String sourceTrialNumber = "T100000000";
            final String targetTrialNumber = "T100000001";
            final String locCode = "416";
            final List<String> jurors = Arrays.asList("111111101", "111111102", "111111103");

            assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
                                                                               trialService.reassignPanelMembers(
                                                                                   createReassignPanelMembersRequestDto(
                                                                                       jurors,
                                                                                       sourceTrialNumber,
                                                                                       targetTrialNumber,
                                                                                       locCode
                                                                                   )));

            verify(panelRepository, never()).findByTrialTrialNumberAndTrialCourtLocationLocCode(
                anyString(),
                anyString()
            );
            verify(panelRepository, never()).saveAndFlush(any());
            verify(jurorHistoryService, never()).createReassignedToPanelHistory(any(), any());

        }
    }

    @Nested
    class ReinstateJurors {

        static final String TRIAL_NUMBER = "TRIAL2";
        static final String LOC_CODE = "415";
        final List<String> jurors = Arrays.asList("641684001", "641674001");
        final ReinstateJurorsRequestDto dto = new ReinstateJurorsRequestDto();

        {
            dto.setTrialNumber(TRIAL_NUMBER);
            dto.setCourtLocationCode(LOC_CODE);
            dto.setJurors(jurors);
        }

        @Test
        void returnedJurorsNoResults() {

            List<PanelListDto> returnedJurors = trialService.getReturnedJurors(TRIAL_NUMBER, LOC_CODE);
            assertThat(returnedJurors).isNotNull().isEmpty();
        }

        @Test
        void reinstateJurorsHappy() {

            payload = TestUtils.createJwt("415", "COURT_USER", "1", Collections.singletonList("415"));
            TestUtils.mockSecurityUtil(payload);

            JurorStatus status = new JurorStatus();
            status.setStatus(IJurorStatus.RESPONDED);

            JurorPool juror1 = getJurorPool(status, "641684001");
            JurorPool juror2 = getJurorPool(status, "641674001");
            List<JurorPool> jurorPoolsList = Arrays.asList(juror1, juror2);

            when(jurorPoolRepository.findByPoolCourtLocationLocCodeAndIsActiveAndJurorJurorNumberIn(
                LOC_CODE, true, Arrays.asList("641684001", "641674001")))
                .thenReturn(jurorPoolsList);

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setLocCode(LOC_CODE);
            courtLocation.setOwner("415");

            Trial trial = createTrial(TRIAL_NUMBER);
            trial.setTrialNumber(TRIAL_NUMBER);
            trial.setCourtLocation(courtLocation);

            when(trialRepository.findByTrialNumberAndCourtLocationLocCode(
                TRIAL_NUMBER, LOC_CODE)).thenReturn(Optional.of(trial));

            final List<Panel> panelList = new ArrayList<>();
            Panel panel1 = new Panel();
            panel1.setJuror(Juror.builder().jurorNumber("641684001").build());
            panel1.setTrial(trial);
            Panel panel2 = new Panel();
            panel2.setJuror(Juror.builder().jurorNumber("641674001").build());
            panel2.setTrial(trial);
            panelList.add(panel1);
            panelList.add(panel2);

            when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCodeAndJurorJurorNumberIn(
                TRIAL_NUMBER, LOC_CODE, jurors)).thenReturn(panelList);
            when(jurorPoolRepository.findByJurorNumberAndIsActiveAndCourt(
                juror1.getJurorNumber(), true,
                courtLocation
            )).thenReturn(juror1);
            when(jurorPoolRepository.findByJurorNumberAndIsActiveAndCourt(
                juror2.getJurorNumber(), true,
                courtLocation
            )).thenReturn(juror2);

            // invoke the method to test
            trialService.reinstateJurors(dto);

            verify(jurorPoolRepository, times(1)).findByPoolCourtLocationLocCodeAndIsActiveAndJurorJurorNumberIn(
                LOC_CODE, true, Arrays.asList("641684001", "641674001"));
            verify(trialRepository, times(1)).findByTrialNumberAndCourtLocationLocCode(
                TRIAL_NUMBER, LOC_CODE);
            verify(panelRepository, times(1))
                .findByTrialTrialNumberAndTrialCourtLocationLocCodeAndJurorJurorNumberIn(
                    TRIAL_NUMBER, LOC_CODE, jurors);

            verify(panelRepository, times(2)).saveAndFlush(any(Panel.class));
            verify(jurorHistoryService, times(2)).createJuryReinstatementHistory(any(), any());

        }

        @Test
        void reinstateJurorsJurorNotInPool() {

            payload = TestUtils.createJwt("415", "COURT_USER", "1", Collections.singletonList("415"));
            TestUtils.mockSecurityUtil(payload);

            JurorStatus status = new JurorStatus();
            status.setStatus(IJurorStatus.RESPONDED);

            JurorPool juror1 = getJurorPool(status, "641684001");

            List<JurorPool> jurorPoolsList = List.of(juror1);

            when(jurorPoolRepository.findByPoolCourtLocationLocCodeAndIsActiveAndJurorJurorNumberIn(
                LOC_CODE, true, Arrays.asList("641684001", "641674001"))).thenReturn(jurorPoolsList);

            // invoke the method to test
            MojException.NotFound notFoundException = Assertions.assertThrows(MojException.NotFound.class, () ->
                                                                                      trialService.reinstateJurors(
                                                                                          dto));
            assertThat(notFoundException.getMessage())
                .isEqualTo("Juror 641674001 not found in the pool at court");

            verify(jurorPoolRepository, times(1)).findByPoolCourtLocationLocCodeAndIsActiveAndJurorJurorNumberIn(
                LOC_CODE, true, Arrays.asList("641684001", "641674001"));

            verifyNoInteractions(panelRepository);
            verifyNoInteractions(trialRepository);
            verifyNoInteractions(jurorHistoryService);
            verifyNoMoreInteractions(jurorPoolRepository);

        }

        @Test
        void reinstateJurorsJurorInWrongStatus() {

            payload = TestUtils.createJwt("415", "COURT_USER", "1", Collections.singletonList("415"));
            TestUtils.mockSecurityUtil(payload);

            JurorStatus status = new JurorStatus();
            status.setStatus(IJurorStatus.JUROR);

            JurorPool juror1 = getJurorPool(status, "641684001");
            JurorPool juror2 = getJurorPool(status, "641674001");
            List<JurorPool> jurorPoolsList = Arrays.asList(juror1, juror2);

            when(jurorPoolRepository.findByPoolCourtLocationLocCodeAndIsActiveAndJurorJurorNumberIn(
                LOC_CODE, true, Arrays.asList("641684001", "641674001"))).thenReturn(jurorPoolsList);

            // invoke the method to test
            MojException.BusinessRuleViolation brv = Assertions.assertThrows(
                MojException.BusinessRuleViolation.class, () ->
                                                               trialService.reinstateJurors(
                                                               dto));
            assertThat(brv.getMessage())
                .isEqualTo("Juror 641684001 is not in responded status");

            verify(jurorPoolRepository, times(1)).findByPoolCourtLocationLocCodeAndIsActiveAndJurorJurorNumberIn(
                LOC_CODE, true, Arrays.asList("641684001", "641674001"));

            verifyNoInteractions(panelRepository);
            verifyNoInteractions(trialRepository);
            verifyNoInteractions(jurorHistoryService);
            verifyNoMoreInteractions(jurorPoolRepository);
        }


        @Test
        void reinstateJurorsTrialNotFound() {
            payload = TestUtils.createJwt("415", "COURT_USER", "1", Collections.singletonList("415"));
            TestUtils.mockSecurityUtil(payload);

            JurorStatus status = new JurorStatus();
            status.setStatus(IJurorStatus.RESPONDED);

            JurorPool juror1 = getJurorPool(status, "641684001");
            JurorPool juror2 = getJurorPool(status, "641674001");
            List<JurorPool> jurorPoolsList = Arrays.asList(juror1, juror2);

            when(jurorPoolRepository.findByPoolCourtLocationLocCodeAndIsActiveAndJurorJurorNumberIn(
                LOC_CODE, true, Arrays.asList("641684001", "641674001"))).thenReturn(jurorPoolsList);

            when(trialRepository.findByTrialNumberAndCourtLocationLocCode(
                TRIAL_NUMBER, LOC_CODE)).thenReturn(Optional.empty());

            // invoke the method to test
            MojException.NotFound notFoundException = Assertions.assertThrows(MojException.NotFound.class, () ->
                                                                                  trialService.reinstateJurors(dto));
            assertThat(notFoundException.getMessage())
                .isEqualTo("Cannot find trial with number: TRIAL2 for court location 415");

            verify(jurorPoolRepository, times(1)).findByPoolCourtLocationLocCodeAndIsActiveAndJurorJurorNumberIn(
                LOC_CODE, true, Arrays.asList("641684001", "641674001"));
            verify(trialRepository, times(1)).findByTrialNumberAndCourtLocationLocCode(
                TRIAL_NUMBER, LOC_CODE);

            verifyNoInteractions(panelRepository);
            verifyNoMoreInteractions(trialRepository);
            verifyNoInteractions(jurorHistoryService);
            verifyNoMoreInteractions(jurorPoolRepository);
        }


        @Test
        void reinstateJurorsJurorNotOnTrial() {
            payload = TestUtils.createJwt("415", "COURT_USER", "1", Collections.singletonList("415"));
            TestUtils.mockSecurityUtil(payload);

            JurorStatus status = new JurorStatus();
            status.setStatus(IJurorStatus.RESPONDED);

            JurorPool juror1 = getJurorPool(status, "641684001");
            JurorPool juror2 = getJurorPool(status, "641674001");
            List<JurorPool> jurorPoolsList = Arrays.asList(juror1, juror2);

            when(jurorPoolRepository.findByPoolCourtLocationLocCodeAndIsActiveAndJurorJurorNumberIn(
                LOC_CODE, true, Arrays.asList("641684001", "641674001"))).thenReturn(jurorPoolsList);

            when(trialRepository.findByTrialNumberAndCourtLocationLocCode(
                TRIAL_NUMBER, LOC_CODE)).thenReturn(Optional.empty());

            CourtLocation courtLocation = new CourtLocation();
            courtLocation.setLocCode(LOC_CODE);
            courtLocation.setOwner("415");

            Trial trial = createTrial(TRIAL_NUMBER);
            trial.setTrialNumber(TRIAL_NUMBER);
            trial.setCourtLocation(courtLocation);

            when(trialRepository.findByTrialNumberAndCourtLocationLocCode(
                TRIAL_NUMBER, LOC_CODE)).thenReturn(Optional.of(trial));

            List<Panel> panelList = new ArrayList<>();
            Panel panel1 = new Panel();
            panel1.setJuror(Juror.builder().jurorNumber("641684001").build());
            panel1.setTrial(trial);
            panelList.add(panel1);
            // note juror 641674001 is not on the trial

            when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCodeAndJurorJurorNumberIn(
                TRIAL_NUMBER, LOC_CODE, jurors)).thenReturn(panelList);

            MojException.NotFound notFoundException = Assertions.assertThrows(MojException.NotFound.class, () ->
                                                                                  trialService.reinstateJurors(dto));
            assertThat(notFoundException.getMessage()).isEqualTo("Juror 641674001 not found on the trial");

            verify(jurorPoolRepository, times(1)).findByPoolCourtLocationLocCodeAndIsActiveAndJurorJurorNumberIn(
                LOC_CODE, true, Arrays.asList("641684001", "641674001"));
            verify(trialRepository, times(1)).findByTrialNumberAndCourtLocationLocCode(
                TRIAL_NUMBER, LOC_CODE);

            verify(panelRepository, times(1))
                .findByTrialTrialNumberAndTrialCourtLocationLocCodeAndJurorJurorNumberIn(
                    TRIAL_NUMBER, LOC_CODE, jurors);

            verifyNoMoreInteractions(trialRepository);
            verifyNoInteractions(jurorHistoryService);
            verifyNoMoreInteractions(jurorPoolRepository);
        }

        private JurorPool getJurorPool(JurorStatus status, String jurorNumber) {
            JurorPool juror = new JurorPool();
            juror.setJuror(Juror.builder().jurorNumber(jurorNumber).build());
            juror.setStatus(status);
            juror.setIsActive(true);
            return juror;
        }

    }

    private JurorPanelReassignRequestDto createReassignPanelMembersRequestDto(List<String> jurors,
        String sourceTrialNumber, String targetTrialNumber, String locCode) {

        JurorPanelReassignRequestDto dto = new JurorPanelReassignRequestDto();
        dto.setJurors(jurors);
        dto.setSourceTrialNumber(sourceTrialNumber);
        dto.setSourceTrialLocCode(locCode);
        dto.setTargetTrialNumber(targetTrialNumber);
        dto.setTargetTrialLocCode(locCode);
        return dto;
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
            temp.setDateSelected(now().atStartOfDay());
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

