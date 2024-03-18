package uk.gov.hmcts.juror.api.moj.service.trial;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.EndTrialDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.JurorDetailRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.ReturnJuryDto;
import uk.gov.hmcts.juror.api.moj.controller.request.trial.TrialDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.TrialListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.TrialSummaryDto;
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
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.TrialType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.CourtroomRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.JudgeRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.PanelRepository;
import uk.gov.hmcts.juror.api.moj.repository.trial.TrialRepository;
import uk.gov.hmcts.juror.api.moj.service.CompleteServiceServiceImpl;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.time.LocalDate.now;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
    private CompleteServiceServiceImpl completeService;

    @InjectMocks
    TrialServiceImpl trialService;

    BureauJWTPayload payload = createJwtPayload("415", "COURT_USER");

    @Test
    void testCreateTrial() {
        TrialDto trialDto = createTrialDto();

        when(courtroomRepository.findById(trialDto.getCourtroomId())).thenReturn(Optional.of(createCourtroom()));

        when(courtLocationRepository.findByLocCode(trialDto.getCourtLocation())).thenReturn(
            Optional.of(createCourtLocation()));

        when(judgeRepository.findById(trialDto.getJudgeId())).thenReturn(Optional.of(createJudge()));

        TrialSummaryDto trialSummary = trialService.createTrial(
            createJwtPayload("415", "COURT_USER"), trialDto);

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
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void testGetTrials() {
        when(trialRepository.getListOfTrialsForCourtLocations(createCourtList(), Boolean.TRUE, null, createPageable()))
            .thenReturn(createTrialList());

        Page<TrialListDto> trials = trialService.getTrials(createJwtPayload("415", "COURT_USER"),
            0, "trialNumber", "desc", Boolean.TRUE, null);

        assertThat(trials)
            .as("List of trials should be in desc order based on trial number")
            .hasSize(2)
            .extracting(TrialListDto::getTrialNumber)
            .containsExactly("T100000025", "T100000024");
    }

    @Test
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
    void testGetTrialsWithJurorNumber() {
        when(
            trialRepository.getListOfTrialsForCourtLocations(createCourtList(), Boolean.TRUE, "1234", createPageable()))
            .thenReturn(createTrialList());

        Page<TrialListDto> trials = trialService.getTrials(createJwtPayload("415", "COURT_USER"),
            0, "trialNumber", "desc", Boolean.TRUE, "1234");

        assertThat(trials)
            .as("List of trials should be in desc order based on trial number")
            .hasSize(2)
            .extracting(TrialListDto::getTrialNumber)
            .containsExactly("T100000025", "T100000024");
    }

    @Test
    void testGetTrialSummary() {
        when(trialRepository.findByTrialNumberAndCourtLocationLocCode("T100000025", "415"))
            .thenReturn(createTrial("T100000025"));

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
            .thenReturn(inactiveTrial);

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
            .thenReturn(createTrial("T100000025"));
        when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", "415"))
            .thenReturn(createJurors(0, PanelResult.JUROR));
        TrialSummaryDto trialSummary = trialService.getTrialSummary(payload, "T100000025", "415");

        assertThat(trialSummary.getIsJuryEmpanelled()).isEqualTo(false);
    }

    @Test
    void testIsEmpanelledWithOneJurorStatusJuror() {
        when(trialRepository.findByTrialNumberAndCourtLocationLocCode("T100000025", "415"))
            .thenReturn(createTrial("T100000025"));
        when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", "415"))
            .thenReturn(createJurors(1, PanelResult.JUROR));
        TrialSummaryDto trialSummary = trialService.getTrialSummary(payload, "T100000025", "415");

        assertThat(trialSummary.getIsJuryEmpanelled()).isEqualTo(true);
    }

    @Test
    void testIsEmpanelledWithNoJurorStatusJurors() {
        when(trialRepository.findByTrialNumberAndCourtLocationLocCode("T100000025", "415"))
            .thenReturn(createTrial("T100000025"));
        when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", "415"))
            .thenReturn(createJurors(12, PanelResult.NOT_USED));

        TrialSummaryDto trialSummary = trialService.getTrialSummary(payload, "T100000025", "415");

        assertThat(trialSummary.getIsJuryEmpanelled()).isEqualTo(false);
    }

    @Test
    void testIsEmpanelledWithMixedStatusesWithOneJurorStatusJuror() {
        when(trialRepository.findByTrialNumberAndCourtLocationLocCode("T100000025", "415"))
            .thenReturn(createTrial("T100000025"));
        when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode("T100000025", "415"))
            .thenReturn(createJurors(12, PanelResult.JUROR));

        TrialSummaryDto trialSummary = trialService.getTrialSummary(payload, "T100000025", "415");

        assertThat(trialSummary.getIsJuryEmpanelled()).isEqualTo(true);
    }

    @Test
    void testReturnPanel() {
        final String trialNumber = "T100000000";
        List<Panel> panelMembers = createPanelMembers(10, null, trialNumber, IJurorStatus.PANEL);
        when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415"))
            .thenReturn(panelMembers);

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
            Appearance appearance = createAppearance(panel.getJurorPool().getJurorNumber());

            if ("null".equals(checkInTime)) {
                appearance.setTimeIn(null);
                appearance.setTimeOut(null);
            } else {
                appearance.setTimeIn(LocalTime.parse(checkInTime));
                appearance.setTimeOut(LocalTime.parse(checkInTime));
            }

            when(appearanceRepository.findByJurorNumber(panel.getJurorPool().getJurorNumber()))
                .thenReturn(appearance);
        }

        trialService.returnJury(payload, trialNumber, "415",
            createReturnJuryDto(false, "09:00", "10:00"));

        verify(panelRepository, times(1))
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415");
        verify(panelRepository, times(panelMembers.size())).saveAndFlush(any());
        verify(jurorHistoryRepository, times(panelMembers.size())).save(any());
        if ("null".equals(checkInTime)) {
            verify(appearanceRepository, times(panelMembers.size())).saveAndFlush(any());
        } else {
            verify(appearanceRepository, times(0)).saveAndFlush(any());
        }

    }

    @Test
    void testReturnJuryNoConfirmAttendanceEmptyTimes() {
        final String trialNumber = "T100000000";
        List<Panel> panelMembers = createPanelMembers(10, PanelResult.JUROR, trialNumber, IJurorStatus.JUROR);
        when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415"))
            .thenReturn(panelMembers);

        for (Panel panel : panelMembers) {
            Appearance appearance = createAppearance(panel.getJurorPool().getJurorNumber());
            appearance.setTimeIn(null);
            when(appearanceRepository.findByJurorNumber(panel.getJurorPool().getJurorNumber()))
                .thenReturn(appearance);
        }

        trialService
            .returnJury(payload, trialNumber, "415",
                createReturnJuryDto(false, "", ""));

        verify(panelRepository, times(1))
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415");
        verify(panelRepository, times(panelMembers.size())).saveAndFlush(any());
        verify(jurorHistoryRepository, times(panelMembers.size())).save(any());
        verify(appearanceRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void testReturnJuryNoConfirmAttendanceNullTimes() {
        final String trialNumber = "T100000000";
        List<Panel> panelMembers = createPanelMembers(10, PanelResult.JUROR, trialNumber, IJurorStatus.JUROR);
        when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415"))
            .thenReturn(panelMembers);

        for (Panel panel : panelMembers) {
            Appearance appearance = createAppearance(panel.getJurorPool().getJurorNumber());
            appearance.setTimeIn(null);
            when(appearanceRepository.findByJurorNumber(panel.getJurorPool().getJurorNumber()))
                .thenReturn(appearance);
        }

        trialService
            .returnJury(payload, trialNumber, "415",
                createReturnJuryDto(false, null, null));

        verify(panelRepository, times(1))
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415");
        verify(panelRepository, times(panelMembers.size())).saveAndFlush(any());
        verify(jurorHistoryRepository, times(panelMembers.size())).save(any());
        verify(appearanceRepository, times(0)).saveAndFlush(any());
    }

    @Test
    void testReturnJuryConfirmAttendanceCompleteService() {
        final String trialNumber = "T100000000";
        List<Panel> panelMembers = createPanelMembers(10, PanelResult.JUROR, trialNumber, IJurorStatus.JUROR);
        when(panelRepository.findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415"))
            .thenReturn(panelMembers);

        for (Panel panel : panelMembers) {
            Appearance appearance = createAppearance(panel.getJurorPool().getJurorNumber());
            when(appearanceRepository.findByJurorNumber(panel.getJurorPool().getJurorNumber()))
                .thenReturn(appearance);
        }

        trialService.returnJury(payload, trialNumber, "415",
            createReturnJuryDto(true, "09:00", "10:00"));

        verify(panelRepository, times(1))
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(trialNumber, "415");
        verify(panelRepository, times(panelMembers.size())).saveAndFlush(any());
        verify(completeService, times(panelMembers.size())).completeService(anyString(), any());
        verify(jurorHistoryRepository, times(panelMembers.size())).save(any());
        verify(appearanceRepository, times(panelMembers.size())).saveAndFlush(any());
    }

    @Test
    void testEndTrialHappyPath() {
        final String trialNumber = "T100000000";
        when(trialRepository.findByTrialNumberAndCourtLocationLocCode(trialNumber, "415"))
            .thenReturn(createTrial(trialNumber));
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
            .thenReturn(createTrial(trialNumber));
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

    private List<Trial> createTrialList() {
        Trial trial1 = new Trial();
        trial1.setTrialNumber("T100000025");
        trial1.setCourtLocation(createCourtLocation());
        trial1.setDescription("CHESTER");
        trial1.setCourtroom(createCourtroom());
        trial1.setJudge(createJudge());
        trial1.setTrialType(TrialType.CRI);
        trial1.setTrialStartDate(now().plusMonths(1));
        trial1.setAnonymous(Boolean.TRUE);

        Courtroom courtroom = createCourtroom();
        courtroom.setId(2L);
        courtroom.setCourtLocation(trial1.getCourtLocation());
        courtroom.setRoomNumber("68");
        courtroom.setDescription("Courtroom 2");

        Trial trial2 = new Trial();
        trial2.setTrialNumber("T100000024");
        trial2.setCourtLocation(createCourtLocation());
        trial2.setDescription("CHESTER");
        trial2.setCourtroom(courtroom);
        trial2.setJudge(createJudge());
        trial2.setTrialType(TrialType.CIV);
        trial2.setTrialStartDate(now().plusMonths(2));
        trial2.setAnonymous(Boolean.FALSE);

        List<Trial> trialList = new ArrayList<>();
        trialList.add(trial1);
        trialList.add(trial2);

        return trialList;
    }

    private Pageable createPageable() {
        return PageRequest.of(0, 25, Sort.by("trialNumber").descending());
    }

    private List<String> createCourtList() {
        List<String> courts = new ArrayList<>();
        courts.add("415");
        courts.add("462");

        return courts;
    }

    private BureauJWTPayload createJwtPayload(String owner, String userType) {
        BureauJWTPayload bureauJwtPayload = new BureauJWTPayload();

        List<String> courtList = createCourtList();
        bureauJwtPayload.setStaff(staffBuilder("Ms Bean", 1, courtList));

        bureauJwtPayload.setOwner(owner);
        bureauJwtPayload.setLogin(userType);
        bureauJwtPayload.setUserLevel("99");
        bureauJwtPayload.setDaysToExpire(89);
        bureauJwtPayload.setPasswordWarning(Boolean.FALSE);
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
        courtLocation.setOwner("AYLESBURY");

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

    public List<Panel> createPanelMembers(int totalMembers, PanelResult panelResult, String trialNumber,
                                          int status) {
        List<Panel> panelList = new ArrayList<>();
        String jurorNumber = "1111111%02d";
        for (int i = 0;
             i < totalMembers;
             i++) {
            Panel temp = createSinglePanelData(panelResult, trialNumber, status, jurorNumber);
            temp.getJurorPool().getJuror().setJurorNumber(jurorNumber.formatted(i + 1));
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
            dto.setFirstName(panel.getJurorPool().getJuror().getFirstName());
            dto.setFirstName(panel.getJurorPool().getJuror().getLastName());
            dto.setJurorNumber(panel.getJurorPool().getJurorNumber());
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

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(jurorStatusValue);
        jurorStatus.setActive(true);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("111111111");


        poolRequest.setCourtLocation(createCourtLocation());

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("415");
        jurorPool.setJuror(juror);
        jurorPool.setPool(poolRequest);
        jurorPool.setStatus(jurorStatus);
        jurorPool.setLocation("Court 1");


        Panel panel = new Panel();
        panel.setJurorPool(jurorPool);
        panel.setCompleted(false);
        panel.setTrial(createTrial(trialNumber));
        panel.setResult(panelResult);

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
}

