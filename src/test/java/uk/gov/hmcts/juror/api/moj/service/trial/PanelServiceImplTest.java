package uk.gov.hmcts.juror.api.moj.service.trial;

import com.querydsl.core.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorDetailRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorListRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.AvailableJurorsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.EmpanelListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.PanelListDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.trial.Courtroom;
import uk.gov.hmcts.juror.api.moj.domain.trial.Judge;
import uk.gov.hmcts.juror.api.moj.domain.trial.Panel;
import uk.gov.hmcts.juror.api.moj.domain.trial.Trial;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.TrialType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.PanelRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.NUMBER_OF_JURORS_EXCEEDS_AVAILABLE;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.NUMBER_OF_JURORS_EXCEEDS_LIMITS;

@ExtendWith(SpringExtension.class)
@SuppressWarnings({
    "PMD.ExcessiveImports",
    "PMD.TooManyMethods",
    "PMD.GodClass"
})
class PanelServiceImplTest {
    @Mock
    private TrialRepository trialRepository;
    @Mock
    private PanelRepository panelRepository;
    @Mock
    private JurorPoolRepository jurorPoolRepository;
    @Mock
    private AppearanceRepository appearanceRepository;
    @Mock
    private JurorHistoryRepository jurorHistoryRepository;

    @InjectMocks
    PanelServiceImpl panelService;

    @Test
    void testAvailableJurors() {
        Tuple tuple = mock(Tuple.class);

        doReturn("111111111").when(tuple).get(0, String.class);
        doReturn(10L).when(tuple).get(1, Long.class);
        doReturn(now()).when(tuple).get(2, LocalDate.class);
        doReturn("Chester").when(tuple).get(3, String.class);
        doReturn("415").when(tuple).get(4, String.class);

        List<Tuple> results = new ArrayList<>();
        results.add(tuple);
        doReturn(results).when(appearanceRepository)
            .getAvailableJurors("415");

        List<AvailableJurorsDto> getAvailableJurors = panelService.getAvailableJurors("415");

        verify(appearanceRepository, times(1)).getAvailableJurors("415");

        assertThat(getAvailableJurors.get(0).getAvailableJurors()).isEqualTo(10);
        assertThat(getAvailableJurors.get(0).getCourtLocationCode()).isEqualTo("415");
        assertThat(getAvailableJurors.get(0).getCourtLocation()).isEqualTo("Chester");
        assertThat(getAvailableJurors.get(0).getServiceStartDate())
            .isEqualTo(now());
        assertThat(getAvailableJurors.get(0).getPoolNumber()).isEqualTo("111111111");
    }

    @Test
    void createPanelHappyPathSelectedPools() {
        final LocalDate date = now();
        final String locCode = "415";

        ArrayList<String> poolNumbers = new ArrayList<>();
        poolNumbers.add("415231201");

        doReturn(true).when(trialRepository)
            .existsByTrialNumberAndCourtLocationLocCode("T100000025", locCode);
        doReturn(Optional.of(createTrial())).when(trialRepository)
            .findByTrialNumberAndCourtLocationLocCode("T100000025", locCode);

        List<String> jurorNumbers = new ArrayList<>();
        jurorNumbers.add("121212121");
        jurorNumbers.add("111111111");

        doReturn(createJurorPool(jurorNumbers, "415231201")).when(appearanceRepository)
            .retrieveAllJurors(locCode, date);

        doReturn(createJurorPool(jurorNumbers, poolNumbers.get(0))).when(appearanceRepository)
            .getJurorsInPools(locCode, poolNumbers, date);
        doReturn(Optional.of(createAppearance("121212121"))).when(appearanceRepository)
            .findByJurorNumberAndAttendanceDate("121212121", now());
        doReturn(Optional.of(createAppearance("121212121"))).when(appearanceRepository)
            .findByJurorNumberAndAttendanceDate("121212121", now());
        doReturn(Optional.of(createAppearance("111111111"))).when(appearanceRepository)
            .findByJurorNumberAndAttendanceDate("111111111", now());


        List<PanelListDto> dtoList = panelService.createPanel(2,
            "T100000025",
            poolNumbers,
            locCode,
            date,
            buildPayload());

        verify(appearanceRepository, times(1)).getJurorsInPools(locCode, poolNumbers, date);
        verify(appearanceRepository, never()).retrieveAllJurors(locCode, date);
        verify(panelRepository, times(2)).saveAndFlush(any());
        assertThat(dtoList).as("Expected size to be two").hasSize(2);
        assertThat(dtoList.get(0).getFirstName()).as("Expected first name to be FNAME").isEqualTo("FNAME");
        assertThat(dtoList.get(0).getLastName()).as("Expected first name to be LNAME").isEqualTo("LNAME");
        assertThat(dtoList.get(0).getJurorStatus()).as("Expected status to be Panelled").isEqualTo("Panelled");

    }

    @Test
    void createPanelHappyPathNoPoolsSelected() {
        final String locCode = "415";
        final LocalDate date = now();
        doReturn(true).when(trialRepository)
            .existsByTrialNumberAndCourtLocationLocCode("T100000025", locCode);
        doReturn(Optional.of(createTrial())).when(trialRepository)
            .findByTrialNumberAndCourtLocationLocCode("T100000025", locCode);

        List<String> jurorNumbers = new ArrayList<>();
        jurorNumbers.add("121212121");
        jurorNumbers.add("111111111");

        doReturn(createJurorPool(jurorNumbers, "415231201")).when(appearanceRepository)
            .retrieveAllJurors(locCode, date);
        doReturn(Optional.of(createAppearance("111111111"))).when(appearanceRepository)
            .findByJurorNumberAndAttendanceDate("111111111", now());
        doReturn(Optional.of(createAppearance("121212121"))).when(appearanceRepository)
            .findByJurorNumberAndAttendanceDate("121212121", now());

        List<PanelListDto> dtoList = panelService.createPanel(2,
            "T100000025",
            new ArrayList<>(),
            locCode,
            date,
            buildPayload());

        verify(appearanceRepository, never()).getJurorsInPools(locCode, new ArrayList<>(), date);
        verify(appearanceRepository, times(1)).retrieveAllJurors(locCode, date);
        verify(panelRepository, times(2)).saveAndFlush(any());
        assertThat(dtoList).as("Expected size to be two").hasSize(2);
        assertThat(dtoList.get(0).getFirstName()).as("Expected first name to be FNAME").isEqualTo("FNAME");
        assertThat(dtoList.get(0).getLastName()).as("Expected first name to be LNAME").isEqualTo("LNAME");
        assertThat(dtoList.get(0).getJurorStatus()).as("Expected status to be Panelled").isEqualTo("Panelled");

    }

    @Test
    void createPanelHappyPathEmptyPoolSelection() {
        final LocalDate date = now();
        doReturn(true).when(trialRepository)
            .existsByTrialNumberAndCourtLocationLocCode("T100000025", "415");
        doReturn(Optional.of(createTrial())).when(trialRepository)
            .findByTrialNumberAndCourtLocationLocCode("T100000025", "415");

        List<String> jurorNumbers = new ArrayList<>();
        jurorNumbers.add("121212121");
        jurorNumbers.add("111111111");

        final String locCode = "415";

        List<JurorPool> jurorPools = createJurorPool(jurorNumbers, "415231201");

        doReturn(jurorPools).when(appearanceRepository).retrieveAllJurors(locCode, date);
        doReturn(Optional.of(createAppearance("121212121"))).when(appearanceRepository)
            .findByJurorNumberAndAttendanceDate("121212121", now());
        doReturn(Optional.of(createAppearance("111111111"))).when(appearanceRepository)
            .findByJurorNumberAndAttendanceDate("111111111", now());

        ArrayList<String> poolNumbers = new ArrayList<>();
        List<PanelListDto> dtoList = panelService.createPanel(2,
            "T100000025",
            poolNumbers,
            "415",
            date,
            buildPayload());

        verify(appearanceRepository, never()).getJurorsInPools(locCode, poolNumbers, date);
        verify(appearanceRepository, times(1)).retrieveAllJurors(locCode, date);
        verify(panelRepository, times(2)).saveAndFlush(any());
        assertThat(dtoList).as("Expected size to be two").hasSize(2);
        assertThat(dtoList.get(0).getFirstName()).as("Expected first name to be FNAME").isEqualTo("FNAME");
        assertThat(dtoList.get(0).getLastName()).as("Expected first name to be LNAME").isEqualTo("LNAME");
        assertThat(dtoList.get(0).getJurorStatus()).as("Expected status to be Panelled").isEqualTo("Panelled");
    }

    @Test
    void createPanelNoCourtLocationProvided() {
        doReturn(false).when(trialRepository)
            .existsByTrialNumberAndCourtLocationLocCode(anyString(), anyString());
        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(
            () -> panelService.createPanel(1,
                "T100000025",
                new ArrayList<>(),
                anyString(),
                now(),
                any()));

        verify(trialRepository, times(1))
            .existsByTrialNumberAndCourtLocationLocCode("T100000025", "");
    }

    @Test
    void createPanelTrialExistsForPanel() {
        BureauJwtPayload payload = buildPayload();
        doReturn(true).when(trialRepository)
            .existsByTrialNumberAndCourtLocationLocCode(anyString(), anyString());
        doReturn(true).when(panelRepository)
            .existsByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", "415");

        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(
            () -> panelService.createPanel(1,
                "T100000025",
                new ArrayList<>(),
                "415",
                now(),
                payload));

        verify(trialRepository, times(1))
            .existsByTrialNumberAndCourtLocationLocCode(anyString(), anyString());
        verify(panelRepository, times(1))
            .existsByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", "415");
    }

    @Test
    void createPanelTrialDoesNotExist() {
        doReturn(false).when(trialRepository)
            .existsByTrialNumberAndCourtLocationLocCode(anyString(), anyString());
        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(
            () -> panelService.createPanel(1,
                "T100000025",
                new ArrayList<>(),
                anyString(),
                now(),
                any()));

        verify(trialRepository, times(1))
            .existsByTrialNumberAndCourtLocationLocCode("T100000025", "");
    }

    @Test
    void createPanelTrialNotEnoughRequested() {
        doReturn(true).when(trialRepository)
            .existsByTrialNumberAndCourtLocationLocCode(anyString(), anyString());
        MojException.BusinessRuleViolation exception = assertThrows(MojException.BusinessRuleViolation.class,
            () -> panelService.createPanel(0,
                "T100000025",
                new ArrayList<>(),
                anyString(),
                now(),
                any()),
            "Expected exception to be thrown when not enough jurors");

        assertEquals("Cannot create panel - Number requested must be between 1 and 1000",
            exception.getMessage(),
            "Expected exception message to be: Cannot create panel - Number requested must be between 1 and 1000");
        assertEquals(NUMBER_OF_JURORS_EXCEEDS_LIMITS, exception.getErrorCode());

        verify(trialRepository, times(1))
            .existsByTrialNumberAndCourtLocationLocCode("T100000025", "");
    }

    @Test
    void createPanelTrialTooManyRequested() {
        doReturn(true).when(trialRepository)
            .existsByTrialNumberAndCourtLocationLocCode(anyString(), anyString());

        MojException.BusinessRuleViolation exception = assertThrows(MojException.BusinessRuleViolation.class,
            () -> panelService.createPanel(1001,
                "T100000025",
                new ArrayList<>(),
                anyString(),
                now(),
                any()),
            "Expected exception to be thrown when not enough jurors");

        assertEquals("Cannot create panel - Number requested must be between 1 and 1000",
            exception.getMessage(),
            "Expected exception message to be: Cannot create panel - Number requested must be between 1 and 1000");
        assertEquals(NUMBER_OF_JURORS_EXCEEDS_LIMITS, exception.getErrorCode());

        verify(trialRepository, times(1))
            .existsByTrialNumberAndCourtLocationLocCode("T100000025", "");
    }

    @Test
    void createPanelTrialNotEnoughJurors() {
        final LocalDate date = now();
        final String locCode = "415";

        List<JurorPool> appearanceList;

        List<String> jurorNumbers = new ArrayList<>();
        jurorNumbers.add("121212121");
        jurorNumbers.add("111111111");
        appearanceList = createJurorPool(jurorNumbers, "415231201");

        doReturn(appearanceList).when(appearanceRepository)
            .retrieveAllJurors(locCode, date);
        doReturn(true).when(trialRepository)
            .existsByTrialNumberAndCourtLocationLocCode(anyString(), anyString());
        doReturn(false).when(panelRepository)
            .existsByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", "415");

        MojException.BusinessRuleViolation exception = assertThrows(MojException.BusinessRuleViolation.class,
            () -> panelService.createPanel(50,
                "T100000025",
                new ArrayList<>(),
                locCode,
                date,
                buildPayload()),
            "Expected exception to be thrown when not enough jurors");

        assertEquals("Cannot create panel - Not enough jurors available",
            exception.getMessage(),
            "Expected exception message to be: Cannot create panel - Not enough jurors available");
        assertEquals(NUMBER_OF_JURORS_EXCEEDS_AVAILABLE, exception.getErrorCode());
        verify(trialRepository, times(1))
            .existsByTrialNumberAndCourtLocationLocCode("T100000025", locCode);
        verify(panelRepository, times(1))
            .existsByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", "415");
        verify(appearanceRepository, times(1))
            .retrieveAllJurors(locCode, date);
    }

    @Test
    void requestEmpanelHappyPath() {
        doReturn(Collections.singletonList(createSinglePanelData())).when(panelRepository)
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(
                "T100000025", "415");
        doReturn(true).when(trialRepository)
            .existsByTrialNumberAndCourtLocationLocCode("T100000025", "415");

        EmpanelListDto empanelListDtoCall = panelService.requestEmpanel(1, "T100000025", "415");

        verify(panelRepository, times(1))
            .findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", "415");

        assertThat(empanelListDtoCall.getEmpanelList().get(0).getFirstName()).isEqualTo("FNAME");
        assertThat(empanelListDtoCall.getEmpanelList().get(0).getLastName()).isEqualTo("LNAME");
        assertThat(empanelListDtoCall.getEmpanelList().get(0).getStatus()).isNull();

    }

    @Test
    void processEmpanelledAsJurorHappyPath() {
        final int totalPanelMembers = 10;
        int totalUnusedJurors = 0;
        List<Panel> panelMembers = createPanelMembers(totalPanelMembers);
        BureauJwtPayload payload = buildPayload();

        for (Panel member : panelMembers) {
            doReturn(member).when(panelRepository)
                .findByTrialTrialNumberAndTrialCourtLocationLocCodeAndJurorPoolJurorJurorNumber(
                    "T100000025", "415", member.getJurorNumber());
            doReturn(Optional.of(createAppearance(member.getJurorNumber()))).when(appearanceRepository)
                .findByJurorNumberAndAttendanceDateAndAppearanceStage(member.getJurorNumber(),
                    now(), AppearanceStage.CHECKED_IN);
            if (member.getResult() != PanelResult.JUROR) {
                totalUnusedJurors++;
            }
        }

        doReturn(Optional.of(createTrial())).when(trialRepository).findByTrialNumberAndCourtLocationLocCode(anyString(),
            anyString());

        JurorListRequestDto jurorListRequestDto =
            createEmpanelledListRequestDto(panelMembers);

        panelService.processEmpanelled(jurorListRequestDto, payload);

        verify(appearanceRepository, times(totalUnusedJurors)).saveAndFlush(any());
        verify(panelRepository, times(totalPanelMembers)).saveAndFlush(any());
        verify(jurorHistoryRepository, times(totalPanelMembers)).save(any());
    }

    @Test
    void processEmpanelledChallenged() {
        final int totalPanelMembers = 10;
        List<Panel> panelMembers = createPanelMembers(totalPanelMembers);

        for (Panel panelMember : panelMembers) {
            panelMember.setResult(PanelResult.CHALLENGED);
        }

        for (Panel member : panelMembers) {
            doReturn(member).when(panelRepository)
                .findByTrialTrialNumberAndTrialCourtLocationLocCodeAndJurorPoolJurorJurorNumber(
                    "T100000025",
                    "415",
                    member.getJurorNumber());
            doReturn(Optional.of(createAppearance(member.getJurorNumber()))).when(appearanceRepository)
                .findByJurorNumberAndAttendanceDateAndAppearanceStage(member.getJurorNumber(),
                    now(), AppearanceStage.CHECKED_IN);
        }

        doReturn(Optional.of(createTrial())).when(trialRepository).findByTrialNumberAndCourtLocationLocCode(anyString(),
            anyString());

        JurorListRequestDto jurorListRequestDto =
            createEmpanelledListRequestDto(panelMembers);

        BureauJwtPayload payload = buildPayload();
        panelService.processEmpanelled(jurorListRequestDto, payload);

        ArgumentCaptor<Appearance> appearanceArgumentCaptor = ArgumentCaptor.forClass(Appearance.class);

        verify(appearanceRepository, times(panelMembers.size())).saveAndFlush(appearanceArgumentCaptor.capture());
        verify(panelRepository, times(totalPanelMembers)).saveAndFlush(any());
        verify(jurorHistoryRepository, times(totalPanelMembers)).save(any());

        for (Panel member : panelMembers) {
            if (member.getResult().equals(PanelResult.CHALLENGED) || member.getResult().equals(PanelResult.JUROR)) {
                Optional<Appearance> memberAppearance =
                    appearanceArgumentCaptor.getAllValues().stream().filter(appearance ->
                        appearance.getJurorNumber().equals(member.getJurorNumber())).findFirst();
                assertThat(memberAppearance.get().getSatOnJury()).isTrue();
            }
        }
    }

    @Test
    void processEmpanelledAsJurorNumberRequestedNotEnough() {
        JurorListRequestDto jurorListRequestDto =
            createEmpanelledListRequestDto(Collections.singletonList(createSinglePanelData()));
        jurorListRequestDto.setNumberRequested(0);
        BureauJwtPayload payload = buildPayload();
        assertThrows(MojException.BadRequest.class, () ->
            panelService.processEmpanelled(jurorListRequestDto, payload)
        );
    }

    @Test
    void processEmpanelledNoJurorStatusSet() {
        Panel panel = createSinglePanelData();
        panel.setResult(null);
        doReturn(panel).when(panelRepository)
            .findByTrialTrialNumberAndTrialCourtLocationLocCodeAndJurorPoolJurorJurorNumber(anyString(), anyString(),
                anyString());
        JurorListRequestDto jurorListRequestDto =
            createEmpanelledListRequestDto(Collections.singletonList(panel));
        jurorListRequestDto.setNumberRequested(1);
        BureauJwtPayload payload = buildPayload();
        assertThrows(MojException.BadRequest.class, () ->
            panelService.processEmpanelled(jurorListRequestDto, payload)
        );
    }

    @Test
    void processEmpanelledWrongJurorStatusSet() {
        Panel panel = createSinglePanelData();
        panel.setResult(PanelResult.RETURNED);
        doReturn(panel).when(panelRepository)
            .findByTrialTrialNumberAndTrialCourtLocationLocCodeAndJurorPoolJurorJurorNumber(anyString(), anyString(),
                anyString());
        JurorListRequestDto jurorListRequestDto =
            createEmpanelledListRequestDto(Collections.singletonList(panel));
        jurorListRequestDto.setNumberRequested(1);
        BureauJwtPayload payload = buildPayload();
        assertThrows(MojException.BadRequest.class, () ->
            panelService.processEmpanelled(jurorListRequestDto, payload)
        );
    }

    @Test
    void processEmpanelledNoAppearance() {
        final int totalPanelMembers = 10;
        int totalUnusedJurors = 0;
        List<Panel> panelMembers = createPanelMembers(totalPanelMembers);
        BureauJwtPayload payload = buildPayload();

        for (Panel member : panelMembers) {
            doReturn(member).when(panelRepository)
                .findByTrialTrialNumberAndTrialCourtLocationLocCodeAndJurorPoolJurorJurorNumber(
                    "T100000025", "415", member.getJurorNumber());
            doReturn(Optional.empty()).when(appearanceRepository)
                .findByJurorNumberAndAttendanceDateAndAppearanceStage(member.getJurorNumber(),
                    now(), AppearanceStage.CHECKED_IN);
            if (member.getResult() != PanelResult.JUROR) {
                totalUnusedJurors++;
            }
        }

        doReturn(Optional.of(createTrial())).when(trialRepository).findByTrialNumberAndCourtLocationLocCode(anyString(),
            anyString());

        JurorListRequestDto jurorListRequestDto =
            createEmpanelledListRequestDto(panelMembers);

        assertThatExceptionOfType(MojException.BusinessRuleViolation.class).isThrownBy(() ->
            panelService.processEmpanelled(jurorListRequestDto, payload));
    }

    @Test
    void panelSummary() {
        doReturn(createPanelMembers(10)).when(panelRepository)
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(anyString(), anyString());

        List<PanelListDto> dtoList = panelService.getPanelSummary("T11111111", "415");
        assertThat(dtoList.size()).as("Expected size to be 10").isEqualTo(10);
    }

    @Test
    void jurySummary() {
        Panel panel = createSinglePanelData();

        JurorPool jurorPool = createJurorPool(panel.getJuror(), panel.getTrial().getCourtLocation());
        jurorPool.getStatus().setStatusDesc("Juror");
        jurorPool.getStatus().setStatus(IJurorStatus.JUROR);

        doReturn(Collections.singletonList(panel)).when(panelRepository)
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(anyString(), anyString());

        List<PanelListDto> dtoList = panelService.getJurySummary("T11111111", "415");
        assertThat(dtoList).as("Expected size to be 1").hasSize(1);
        assertThat(dtoList.get(0).getJurorStatus()).as("Expected status to be Juror")
            .isEqualTo("Juror");
    }

    @Test
    void jurySummaryNoJury() {
        Panel panel = createSinglePanelData();

        JurorPool jurorPool = createJurorPool(panel.getJuror(), panel.getTrial().getCourtLocation());
        jurorPool.getStatus().setStatusDesc("Panelled");
        jurorPool.getStatus().setStatus(IJurorStatus.PANEL);

        doReturn(Collections.singletonList(panel)).when(panelRepository)
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(anyString(), anyString());

        List<PanelListDto> dtoList = panelService.getJurySummary("T11111111", "415");
        assertThat(dtoList).as("Expected size to be 1").hasSize(0);
    }

    @Test
    void processEmpanelledAsJurorNumberRequestedTooMany() {
        JurorListRequestDto jurorListRequestDto =
            createEmpanelledListRequestDto(Collections.singletonList(createSinglePanelData()));
        jurorListRequestDto.setNumberRequested(31);
        BureauJwtPayload payload = buildPayload();

        assertThrows(MojException.BadRequest.class, () ->
            panelService.processEmpanelled(jurorListRequestDto, payload)
        );
    }


    @Nested
    @DisplayName("Panel Service - Add panel members to existing trials")
    class AddPanelMembers {

        @Nested
        class Positive {
            @Test
            @DisplayName("Add panel members - no pool number provided")
            void addPanelMembersNoPoolProvided() {
                final LocalDate date = now();
                TestUtils.setUpMockAuthentication("415", "COURT_USER", "99", Collections.singletonList("415"));
                final int maxJurors = 10;
                final String jurorNumberFormat = "1111112%02d";
                doReturn(true).when(trialRepository)
                    .existsByTrialNumberAndCourtLocationLocCode("T100000025", "415");
                doReturn(Optional.of(createTrial())).when(trialRepository)
                    .findByTrialNumberAndCourtLocationLocCode("T100000025", "415");

                doReturn(createPanelMembers(10)).when(panelRepository)
                    .findByTrialTrialNumberAndTrialCourtLocationLocCode(anyString(), anyString());

                List<String> jurorNumbers = new ArrayList<>();
                for (int i = 0;
                     i < maxJurors;
                     i++) {
                    jurorNumbers.add(jurorNumberFormat.formatted(i));
                }

                final String locCode = "415";
                doReturn(createJurorPool(jurorNumbers, "415231201")).when(appearanceRepository)
                    .retrieveAllJurors(locCode, date);

                List<Appearance> appearanceList = new ArrayList<>();
                for (int i = 0;
                     i < maxJurors;
                     i++) {
                    String jurorNumber = jurorNumbers.get(i);
                    Appearance appearance = createAppearance(jurorNumber);
                    doReturn(Optional.of(appearance)).when(appearanceRepository)
                        .findByJurorNumberAndAttendanceDate(jurorNumber, now());
                    appearanceList.add(appearance);
                }

                List<PanelListDto> dtoList = panelService.addPanelMembers(2,
                    "T100000025",
                    new ArrayList<>(),
                    "415", date);

                verify(trialRepository, times(2))
                    .findByTrialNumberAndCourtLocationLocCode("T100000025", "415");
                verify(panelRepository, times(1))
                    .findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", "415");
                verify(appearanceRepository, never()).getJurorsInPools(locCode, new ArrayList<>(), date);
                verify(appearanceRepository, times(1)).retrieveAllJurors(locCode, date);
                verify(panelRepository, times(2)).saveAndFlush(any());
                assertThat(dtoList).as("panel members added to be 2").hasSize(2);
                assertThat(panelRepository
                    .findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", "415").size() + dtoList.size())
                    .as("Total panel members")
                    .isEqualTo(12);

                for (PanelListDto dto : dtoList) {
                    assertThat(appearanceList.stream().filter(appearance -> appearance.getJurorNumber()
                        .equals(dto.getJurorNumber())).findAny())
                        .as("Expected panel member to exist in appearance list")
                        .isPresent();
                    assertThat(dto.getFirstName()).isEqualTo("FNAME");
                    assertThat(dto.getLastName()).isEqualTo("LNAME");
                    assertThat(dto.getJurorStatus()).isEqualTo("Panelled");
                }
            }

            @Test
            @DisplayName("Add panel members - included pool number")
            void addPanelMembersPoolNumberProvided() {
                final LocalDate date = now();
                String locCode = "415";

                TestUtils.setUpMockAuthentication(locCode, "COURT_USER", "99", Collections.singletonList(locCode));
                final int maxJuror = 10;
                final String jurorNumberFormat = "1111112%02d";
                doReturn(true).when(trialRepository)
                    .existsByTrialNumberAndCourtLocationLocCode("T100000025", locCode);
                doReturn(Optional.of(createTrial())).when(trialRepository)
                    .findByTrialNumberAndCourtLocationLocCode("T100000025", locCode);

                doReturn(createPanelMembers(10)).when(panelRepository)
                    .findByTrialTrialNumberAndTrialCourtLocationLocCode(anyString(), anyString());

                List<String> jurorNumbers = new ArrayList<>();
                for (int i = 0;
                     i < maxJuror;
                     i++) {
                    jurorNumbers.add(jurorNumberFormat.formatted(i));
                }

                doReturn(createJurorPool(jurorNumbers, "415231201")).when(appearanceRepository)
                    .getJurorsInPools(locCode, Collections.singletonList("415231201"), date);

                List<Appearance> appearanceList = new ArrayList<>();
                for (int i = 0;
                     i < maxJuror;
                     i++) {
                    String jurorNumber = jurorNumbers.get(i);
                    Appearance appearance = createAppearance(jurorNumber);
                    doReturn(Optional.of(appearance)).when(appearanceRepository)
                        .findByJurorNumberAndAttendanceDate(jurorNumber, now());
                    appearanceList.add(appearance);
                }

                List<PanelListDto> dtoList = panelService.addPanelMembers(2,
                    "T100000025",
                    Collections.singletonList("415231201"),
                    locCode, date);

                verify(trialRepository, times(2))
                    .findByTrialNumberAndCourtLocationLocCode("T100000025", locCode);
                verify(panelRepository, times(1))
                    .findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", locCode);
                verify(appearanceRepository, times(1)).getJurorsInPools(locCode,
                    Collections.singletonList("415231201"), date);
                verify(appearanceRepository, never()).retrieveAllJurors(locCode, date);
                verify(panelRepository, times(2)).saveAndFlush(any());
                assertThat(dtoList).as("panel members added to be 2").hasSize(2);
                assertThat(panelRepository
                    .findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", locCode).size() + dtoList.size())
                    .as("Total panel members")
                    .isEqualTo(12);

                for (PanelListDto dto : dtoList) {
                    assertThat(appearanceList.stream().filter(appearance -> appearance.getJurorNumber()
                        .equals(dto.getJurorNumber())).findAny())
                        .as("Expected panel member to exist in appearance list")
                        .isPresent();
                    assertThat(dto.getFirstName()).isEqualTo("FNAME");
                    assertThat(dto.getLastName()).isEqualTo("LNAME");
                    assertThat(dto.getJurorStatus()).isEqualTo("Panelled");
                }
            }
        }

        @Nested
        class Negative {
            @Test
            @DisplayName("Add panel members - no trial")
            void noTrial() {
                final LocalDate date = now();
                String locCode = "415";
                TestUtils.setUpMockAuthentication(locCode, "COURT_USER", "99", Collections.singletonList(locCode));
                MojException.NotFound exception = assertThrows(MojException.NotFound.class, () -> {
                    panelService.addPanelMembers(2,
                        "T100000025",
                        new ArrayList<>(),
                        locCode,
                        date
                    );
                });
                assertThat(exception.getMessage())
                    .as("Exception message")
                    .isEqualTo("Cannot find trial with number: T100000025 for court location 415");

                verify(trialRepository, times(1))
                    .findByTrialNumberAndCourtLocationLocCode("T100000025", locCode);
                verify(panelRepository, never())
                    .findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", locCode);
                verify(appearanceRepository, never())
                    .getJurorsInPools(locCode, Collections.singletonList(""), date);
                verify(appearanceRepository, never())
                    .retrieveAllJurors(locCode, date);
                verify(panelRepository, never()).saveAndFlush(any());
                verify(jurorHistoryRepository, never()).save(any());
                verify(appearanceRepository, never()).save(any());
            }

            @Test
            @DisplayName("Add panel members - no panel members")
            void noPanelMembers() {
                final LocalDate date = now();
                String locCode = "415";
                TestUtils.setUpMockAuthentication(locCode, "COURT_USER", "99", Collections.singletonList(locCode));
                doReturn(Optional.of(createTrial())).when(trialRepository)
                    .findByTrialNumberAndCourtLocationLocCode("T100000025", locCode);
                MojException.BusinessRuleViolation exception = assertThrows(MojException.BusinessRuleViolation.class,
                    () -> {
                        panelService.addPanelMembers(2,
                            "T100000025",
                            new ArrayList<>(),
                            locCode,
                            date
                        );
                    });
                assertThat(exception.getMessage())
                    .isEqualTo("Cannot add panel members - panel has not been created for trial");

                verify(trialRepository, times(1))
                    .findByTrialNumberAndCourtLocationLocCode("T100000025", locCode);
                verify(panelRepository, times(1))
                    .findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", locCode);
                verify(appearanceRepository, never())
                    .getJurorsInPools(locCode, Collections.singletonList(""), date);
                verify(appearanceRepository, never())
                    .retrieveAllJurors(locCode, date);
                verify(panelRepository, never()).saveAndFlush(any());
                verify(jurorHistoryRepository, never()).save(any());
                verify(appearanceRepository, never()).save(any());
            }

            @Test
            @DisplayName("Add panel members - none requested")
            void zeroRequested() {
                final LocalDate date = now();
                String locCode = "415";
                TestUtils.setUpMockAuthentication(locCode, "COURT_USER", "99", Collections.singletonList(locCode));
                doReturn(Optional.of(createTrial())).when(trialRepository)
                    .findByTrialNumberAndCourtLocationLocCode("T100000025", locCode);
                doReturn(createPanelMembers(2)).when(panelRepository)
                    .findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", locCode);
                MojException.BusinessRuleViolation exception = assertThrows(MojException.BusinessRuleViolation.class,
                    () -> {
                        panelService.addPanelMembers(0,
                            "T100000025",
                            new ArrayList<>(),
                            locCode,
                            date
                        );
                    });
                assertThat(exception.getMessage())
                    .isEqualTo("Cannot add panel members - Number requested must be between 1 and 1000");

                verify(trialRepository, times(1))
                    .findByTrialNumberAndCourtLocationLocCode("T100000025", locCode);
                verify(panelRepository, times(1))
                    .findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", locCode);
                verify(appearanceRepository, never())
                    .getJurorsInPools(locCode, Collections.singletonList(""), date);
                verify(appearanceRepository, never())
                    .retrieveAllJurors(locCode, date);
                verify(panelRepository, never()).saveAndFlush(any());
                verify(jurorHistoryRepository, never()).save(any());
                verify(appearanceRepository, never()).save(any());
            }

            @Test
            @DisplayName("Add panel members - too many requested")
            void tooManyRequested() {
                final LocalDate date = now();
                String locCode = "415";
                TestUtils.setUpMockAuthentication(locCode, "COURT_USER", "99", Collections.singletonList(locCode));
                doReturn(Optional.of(createTrial())).when(trialRepository)
                    .findByTrialNumberAndCourtLocationLocCode("T100000025", locCode);
                doReturn(createPanelMembers(1000)).when(panelRepository)
                    .findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", locCode);
                MojException.BusinessRuleViolation exception = assertThrows(MojException.BusinessRuleViolation.class,
                    () -> {
                        panelService.addPanelMembers(0,
                            "T100000025",
                            new ArrayList<>(),
                            locCode,
                            date
                        );
                    });
                assertThat(exception.getMessage())
                    .isEqualTo("Cannot add panel members - Number requested must be between 1 and 1000");

                verify(trialRepository, times(1))
                    .findByTrialNumberAndCourtLocationLocCode("T100000025", locCode);
                verify(panelRepository, times(1))
                    .findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", locCode);
                verify(appearanceRepository, never())
                    .getJurorsInPools(locCode, Collections.singletonList(""), date);
                verify(appearanceRepository, never())
                    .retrieveAllJurors(locCode, date);
                verify(panelRepository, never()).saveAndFlush(any());
                verify(jurorHistoryRepository, never()).save(any());
                verify(appearanceRepository, never()).save(any());
            }

            @Test
            @DisplayName("Add panel members - not enough available jurors")
            void notEnoughAvailableJurors() {
                final LocalDate date = now();
                String locCode = "415";
                TestUtils.setUpMockAuthentication(locCode, "COURT_USER", "99", Collections.singletonList(locCode));
                doReturn(Optional.of(createTrial())).when(trialRepository)
                    .findByTrialNumberAndCourtLocationLocCode("T100000025", locCode);
                doReturn(createPanelMembers(2)).when(panelRepository)
                    .findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", locCode);
                MojException.BusinessRuleViolation exception = assertThrows(MojException.BusinessRuleViolation.class,
                    () -> {
                        panelService.addPanelMembers(3,
                            "T100000025",
                            new ArrayList<>(),
                            locCode,
                            date
                        );
                    });
                assertThat(exception.getMessage())
                    .isEqualTo("Cannot create panel - Not enough jurors available");

                verify(trialRepository, times(1))
                    .findByTrialNumberAndCourtLocationLocCode("T100000025", locCode);
                verify(panelRepository, times(1))
                    .findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", locCode);
                verify(appearanceRepository, never())
                    .getJurorsInPools(locCode, Collections.singletonList(""), date);
                verify(appearanceRepository, times(1))
                    .retrieveAllJurors(locCode, date);
                verify(panelRepository, never()).saveAndFlush(any());
                verify(jurorHistoryRepository, never()).save(any());
                verify(appearanceRepository, never()).save(any());
            }

            @Test
            @DisplayName("Add panel members - trial has ended")
            void trialHasEnded() {
                final LocalDate date = now();
                String locCode = "415";
                TestUtils.setUpMockAuthentication(locCode, "COURT_USER", "99", Collections.singletonList(locCode));
                Trial trial = createTrial();
                trial.setTrialEndDate(now());
                doReturn(Optional.of(trial)).when(trialRepository)
                    .findByTrialNumberAndCourtLocationLocCode("T100000025", locCode);
                MojException.BusinessRuleViolation exception = assertThrows(MojException.BusinessRuleViolation.class,
                    () -> {
                        panelService.addPanelMembers(2,
                            "T100000025",
                            new ArrayList<>(),
                            locCode,
                            date
                        );
                    });
                assertThat(exception.getMessage())
                    .isEqualTo("Cannot add panel members - Trial has ended");

                verify(trialRepository, times(1))
                    .findByTrialNumberAndCourtLocationLocCode("T100000025", locCode);
                verify(panelRepository, never())
                    .findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", locCode);
                verify(appearanceRepository, never())
                    .getJurorsInPools(locCode, Collections.singletonList(""), date);
                verify(appearanceRepository, never())
                    .retrieveAllJurors(locCode, date);
                verify(panelRepository, never()).saveAndFlush(any());
                verify(jurorHistoryRepository, never()).save(any());
                verify(appearanceRepository, never()).save(any());
            }

            @Test
            @DisplayName("Add panel members - no pool found")
            void noPoolFound() {
                final LocalDate date = now();
                String locCode = "415";
                TestUtils.setUpMockAuthentication(locCode, "COURT_USER", "99", Collections.singletonList(locCode));
                doReturn(true).when(trialRepository)
                    .existsByTrialNumberAndCourtLocationLocCode("T100000025", locCode);
                doReturn(Optional.of(createTrial())).when(trialRepository)
                    .findByTrialNumberAndCourtLocationLocCode("T100000025", locCode);
                doReturn(createPanelMembers(10)).when(panelRepository)
                    .findByTrialTrialNumberAndTrialCourtLocationLocCode(anyString(), anyString());
                doReturn(createJurorPool(Collections.singletonList("111111111"), "415231201")).when(
                    appearanceRepository).retrieveAllJurors(locCode, date);
                doReturn(Optional.of(createAppearance("111111111"))).when(appearanceRepository)
                    .findByJurorNumberAndAttendanceDate("111111111", now());

                MojException.BusinessRuleViolation exception = assertThrows(MojException.BusinessRuleViolation.class,
                    () -> {
                        panelService.addPanelMembers(1,
                            "T100000025",
                            Collections.singletonList("1"),
                            locCode,
                            date
                        );
                    });

                assertThat(exception.getMessage()).isEqualTo("Cannot create panel - Not enough jurors available");

                verify(trialRepository, times(1))
                    .findByTrialNumberAndCourtLocationLocCode("T100000025", locCode);
                verify(panelRepository, times(1))
                    .findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", locCode);
                verify(appearanceRepository, times(1))
                    .getJurorsInPools(locCode, Collections.singletonList("1"), date);
                verify(appearanceRepository, never())
                    .retrieveAllJurors(locCode, date);
                verify(panelRepository, never()).saveAndFlush(any());
                verify(jurorHistoryRepository, never()).save(any());
                verify(appearanceRepository, never()).save(any());
            }
        }
    }

    @Nested
    @DisplayName("Panel Service - Panel status")
    class PanelStatus {
        @Nested
        class Positive {
            @DisplayName("Panel status - no panel created")
            @Test
            void noPanelCreated() {
                String trialNumber = "T100000000";
                String courtLocationCode = "415";
                Boolean isCreated = panelService.getPanelStatus(trialNumber, courtLocationCode);

                verify(panelRepository, times(1))
                    .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, courtLocationCode);
                assertThat(isCreated).as("Panel Created?").isFalse();
            }

            @DisplayName("Panel status - panel created")
            @Test
            void panelCreated() {
                String trialNumber = "T100000000";
                String courtLocationCode = "415";

                doReturn(createPanelMembers(10)).when(panelRepository)
                    .findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000000", "415");

                Boolean isCreated = panelService.getPanelStatus(trialNumber, courtLocationCode);

                verify(panelRepository, times(1))
                    .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, courtLocationCode);
                assertThat(isCreated).as("Panel Created?").isTrue();
            }
        }

        @Nested
        class Negative {
            @DisplayName("Panel status - no trial")
            @Test
            void noTrial() {
                String trialNumber = "";
                String courtLocationCode = "415";
                Boolean isCreated = panelService.getPanelStatus(trialNumber, courtLocationCode);

                verify(panelRepository, times(1))
                    .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, courtLocationCode);
                assertThat(isCreated).as("Panel Created?").isFalse();
            }

            @DisplayName("Panel status - no trial")
            @Test
            void noCourtLocationCode() {
                String trialNumber = "T100000000";
                String courtLocationCode = "";
                Boolean isCreated = panelService.getPanelStatus(trialNumber, courtLocationCode);

                verify(panelRepository, times(1))
                    .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, courtLocationCode);
                assertThat(isCreated).as("Panel Created?").isFalse();
            }
        }
    }

    private static JurorListRequestDto createEmpanelledListRequestDto(List<Panel> panelMembers) {
        JurorListRequestDto jurorListRequestDto = new JurorListRequestDto();
        jurorListRequestDto.setJurors(createEmpanelDetailRequestDto(panelMembers));
        jurorListRequestDto.setNumberRequested(1);
        jurorListRequestDto.setTrialNumber("T100000025");
        jurorListRequestDto.setCourtLocationCode("415");
        return jurorListRequestDto;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private static List<JurorDetailRequestDto> createEmpanelDetailRequestDto(List<Panel> panelMembers) {

        List<JurorDetailRequestDto> listDto = new ArrayList<>();
        for (Panel member : panelMembers) {
            JurorDetailRequestDto jurorDetailRequestDto = new JurorDetailRequestDto();
            jurorDetailRequestDto.setFirstName(member.getJuror().getFirstName());
            jurorDetailRequestDto.setLastName(member.getJuror().getLastName());
            jurorDetailRequestDto.setJurorNumber(member.getJurorNumber());
            jurorDetailRequestDto.setResult(member.getResult());
            listDto.add(jurorDetailRequestDto);
        }
        return listDto;
    }

    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    private List<JurorPool> createJurorPool(List<String> jurorNumbers, String poolNumber) {
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(poolNumber);

        List<JurorPool> jurorPoolList = new ArrayList<>();
        for (String jurorNumber : jurorNumbers) {
            Juror juror = new Juror();
            juror.setJurorNumber(jurorNumber);
            juror.setFirstName("FNAME");
            juror.setLastName("LNAME");

            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(2);
            jurorStatus.setActive(true);

            JurorPool jurorPool = new JurorPool();
            jurorPool.setOwner("415");
            jurorPool.setJuror(juror);
            jurorPool.setPool(poolRequest);
            jurorPool.setStatus(jurorStatus);
            jurorPool.setLocation("Court 1");

            doReturn(jurorPool).when(jurorPoolRepository).findByJurorNumberAndIsActiveAndCourt(eq(jurorNumber),
                eq(true), any(CourtLocation.class));
            jurorPoolList.add(jurorPool);
        }
        return jurorPoolList;
    }

    JurorPool createJurorPool(Juror juror, CourtLocation courtLocation) {

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("111111111");
        poolRequest.setCourtLocation(courtLocation);

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.PANEL);
        jurorStatus.setActive(true);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("415");
        jurorPool.setJuror(juror);
        jurorPool.setPool(poolRequest);
        jurorPool.setStatus(jurorStatus);
        jurorPool.setLocation("Court 1");
        doReturn(jurorPool).when(jurorPoolRepository).findByJurorNumberAndIsActiveAndCourt(juror.getJurorNumber(),
            true, courtLocation);

        return jurorPool;
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

    private Trial createTrial() {
        Trial trial = new Trial();
        trial.setTrialNumber("T100000025");
        trial.setCourtLocation(createCourtLocation());
        trial.setDescription("Joe, Jo, Jon");
        trial.setCourtroom(createCourtroom());
        trial.setJudge(createJudge());
        trial.setTrialType(TrialType.CRI);
        trial.setTrialStartDate(now().plusMonths(1));
        trial.setAnonymous(Boolean.TRUE);

        return trial;
    }

    private Judge createJudge() {
        Judge judge = new Judge();
        judge.setId(21L);
        judge.setOwner("415");
        judge.setCode("1234");
        judge.setName("Mr Judge");

        return judge;
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

    private Panel createSinglePanelData() {

        Juror juror = new Juror();
        juror.setJurorNumber("111111111");
        juror.setFirstName("FNAME");
        juror.setLastName("LNAME");

        Panel panel = new Panel();
        panel.setJuror(juror);
        panel.setCompleted(false);
        panel.setTrial(createTrial());
        panel.setResult(PanelResult.JUROR);

        JurorPool jurorPool = createJurorPool(juror, panel.getTrial().getCourtLocation());
        doReturn(jurorPool).when(jurorPoolRepository).findByJurorNumberAndIsActiveAndCourt(juror.getJurorNumber(),
            true, panel.getTrial().getCourtLocation());

        return panel;
    }

    List<Panel> createPanelMembers(int totalMembers) {
        Random random = new Random();
        List<Panel> panelList = new ArrayList<>();
        String jurorNumber = "1111111%02d";
        PanelResult result = PanelResult.JUROR;
        for (int i = 0; i < totalMembers; i++) {

            Panel temp = createSinglePanelData();
            temp.setResult(result);

            Juror juror = temp.getJuror();
            juror.setJurorNumber(jurorNumber.formatted(i + 1));

            JurorPool jurorPool = createJurorPool(juror, temp.getTrial().getCourtLocation());
            jurorPool.setTimesSelected(random.nextInt(0, 2));

            if (result == PanelResult.JUROR) {
                result = PanelResult.CHALLENGED;
            } else if (result == PanelResult.CHALLENGED) {
                result = PanelResult.NOT_USED;
            } else {
                result = PanelResult.JUROR;
            }

            panelList.add(temp);

        }
        return panelList;
    }

    private BureauJwtPayload buildPayload() {
        return BureauJwtPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("SOME_USER")
            .daysToExpire(89)
            .owner("415")
            .build();
    }

}
