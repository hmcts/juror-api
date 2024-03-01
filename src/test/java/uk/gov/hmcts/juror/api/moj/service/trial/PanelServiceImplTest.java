package uk.gov.hmcts.juror.api.moj.service.trial;

import com.querydsl.core.Tuple;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SuppressWarnings({
    "PMD.ExcessiveImports",
    "PMD.TooManyMethods"
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

        ArrayList<String> poolNumbers = new ArrayList<>();
        poolNumbers.add("415231201");

        doReturn(true).when(trialRepository)
            .existsByTrialNumberAndCourtLocationLocCode("T100000025", "415");
        doReturn(createTrial()).when(trialRepository)
            .findByTrialNumberAndCourtLocationLocCode("T100000025", "415");

        doReturn(createJurorPool()).when(appearanceRepository).getJurorsInPools(poolNumbers);
        doReturn(createAppearance("121212121")).when(appearanceRepository).findByJurorNumber("121212121");
        doReturn(createAppearance("111111111")).when(appearanceRepository).findByJurorNumber("111111111");

        List<PanelListDto> dtoList = panelService.createPanel(2,
            "T100000025",
            Optional.of(poolNumbers),
            "415",
            buildPayload());

        verify(appearanceRepository, times(1)).getJurorsInPools(any());
        verify(appearanceRepository, times(0)).retrieveAllJurors();
        verify(panelRepository, times(2)).saveAndFlush(any());
        assertThat(dtoList.size()).as("Expected size to be two").isEqualTo(2);
        assertThat(dtoList.get(0).getFirstName()).as("Expected first name to be FNAME").isEqualTo("FNAME");
        assertThat(dtoList.get(0).getLastName()).as("Expected first name to be LNAME").isEqualTo("LNAME");
        assertThat(dtoList.get(0).getJurorStatus()).as("Expected status to be Panelled").isEqualTo("Panelled");

    }

    @Test
    void createPanelHappyPathNoPoolsSelected() {
        doReturn(true).when(trialRepository)
            .existsByTrialNumberAndCourtLocationLocCode("T100000025", "415");
        doReturn(createTrial()).when(trialRepository)
            .findByTrialNumberAndCourtLocationLocCode("T100000025", "415");

        doReturn(createJurorPool()).when(appearanceRepository).retrieveAllJurors();
        doReturn(createAppearance("121212121")).when(appearanceRepository).findByJurorNumber("121212121");
        doReturn(createAppearance("111111111")).when(appearanceRepository).findByJurorNumber("111111111");

        List<PanelListDto> dtoList = panelService.createPanel(2,
            "T100000025",
            Optional.empty(),
            "415",
            buildPayload());

        verify(appearanceRepository, times(0)).getJurorsInPools(any());
        verify(appearanceRepository, times(1)).retrieveAllJurors();
        verify(panelRepository, times(2)).saveAndFlush(any());
        assertThat(dtoList.size()).as("Expected size to be two").isEqualTo(2);
        assertThat(dtoList.get(0).getFirstName()).as("Expected first name to be FNAME").isEqualTo("FNAME");
        assertThat(dtoList.get(0).getLastName()).as("Expected first name to be LNAME").isEqualTo("LNAME");
        assertThat(dtoList.get(0).getJurorStatus()).as("Expected status to be Panelled").isEqualTo("Panelled");

    }

    @Test
    void createPanelHappyPathEmptyPoolSelection() {
        ArrayList<String> poolNumbers = new ArrayList<>();

        doReturn(true).when(trialRepository)
            .existsByTrialNumberAndCourtLocationLocCode("T100000025", "415");
        doReturn(createTrial()).when(trialRepository)
            .findByTrialNumberAndCourtLocationLocCode("T100000025", "415");

        doReturn(createJurorPool()).when(appearanceRepository).retrieveAllJurors();
        doReturn(createAppearance("121212121")).when(appearanceRepository).findByJurorNumber("121212121");
        doReturn(createAppearance("111111111")).when(appearanceRepository).findByJurorNumber("111111111");

        List<PanelListDto> dtoList = panelService.createPanel(2,
            "T100000025",
            Optional.of(poolNumbers),
            "415",
            buildPayload());

        verify(appearanceRepository, times(0)).getJurorsInPools(any());
        verify(appearanceRepository, times(1)).retrieveAllJurors();
        verify(panelRepository, times(2)).saveAndFlush(any());
        assertThat(dtoList.size()).as("Expected size to be two").isEqualTo(2);
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
                Optional.of(new ArrayList<>()),
                anyString(),
                any()));

        verify(trialRepository, times(1))
            .existsByTrialNumberAndCourtLocationLocCode("T100000025", "");
    }

    @Test
    void createPanelTrialExistsForPanel() {
        doReturn(true).when(trialRepository)
            .existsByTrialNumberAndCourtLocationLocCode(anyString(), anyString());
        doReturn(true).when(panelRepository)
            .existsByTrialTrialNumber(anyString());

        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(
            () -> panelService.createPanel(1,
                "T100000025",
                Optional.of(new ArrayList<>()),
                anyString(),
                any()));

        verify(trialRepository, times(1))
            .existsByTrialNumberAndCourtLocationLocCode(anyString(), anyString());
        verify(panelRepository, times(1))
            .existsByTrialTrialNumber(anyString());

    }

    @Test
    void createPanelTrialDoesNotExist() {
        doReturn(false).when(trialRepository)
            .existsByTrialNumberAndCourtLocationLocCode(anyString(), anyString());
        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(
            () -> panelService.createPanel(1,
                "T100000025",
                Optional.of(new ArrayList<>()),
                anyString(),
                any()));

        verify(trialRepository, times(1))
            .existsByTrialNumberAndCourtLocationLocCode("T100000025", "");
    }

    @Test
    void createPanelTrialNotEnoughRequested() {
        doReturn(true).when(trialRepository)
            .existsByTrialNumberAndCourtLocationLocCode(anyString(), anyString());
        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(
            () -> panelService.createPanel(0,
                "T100000025",
                Optional.of(new ArrayList<>()),
                anyString(),
                any()));

        verify(trialRepository, times(1))
            .existsByTrialNumberAndCourtLocationLocCode("T100000025", "");
    }

    @Test
    void createPanelTrialTooManyRequested() {
        doReturn(true).when(trialRepository)
            .existsByTrialNumberAndCourtLocationLocCode(anyString(), anyString());
        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(
            () -> panelService.createPanel(1001,
                "T100000025",
                Optional.of(new ArrayList<>()),
                anyString(),
                any()));

        verify(trialRepository, times(1))
            .existsByTrialNumberAndCourtLocationLocCode("T100000025", "");
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
        BureauJWTPayload payload = buildPayload();

        for (Panel member : panelMembers) {
            doReturn(member).when(panelRepository).findByTrialTrialNumberAndJurorPoolJurorJurorNumber(
                "T100000025",
                member.getJurorPool().getJurorNumber());
            doReturn(createAppearance(member.getJurorPool().getJurorNumber()))
                .when(appearanceRepository).findByJurorNumber(member.getJurorPool().getJurorNumber());
            if (member.getResult() != PanelResult.JUROR) {
                totalUnusedJurors++;
            }
        }

        doReturn(createTrial()).when(trialRepository).findByTrialNumberAndCourtLocationLocCode(anyString(),
            anyString());

        JurorListRequestDto jurorListRequestDto =
            createEmpanelledListRequestDto(panelMembers);

        panelService.processEmpanelled(jurorListRequestDto, payload);

        verify(appearanceRepository, times(totalUnusedJurors)).saveAndFlush(any());
        verify(panelRepository, times(totalPanelMembers)).saveAndFlush(any());
        verify(jurorHistoryRepository, times(totalPanelMembers)).save(any());

    }

    @Test
    void processEmpanelledAsJurorNumberRequestedNotEnough() {
        JurorListRequestDto jurorListRequestDto =
            createEmpanelledListRequestDto(Collections.singletonList(createSinglePanelData()));
        jurorListRequestDto.setNumberRequested(0);
        BureauJWTPayload payload = buildPayload();
        Assertions.assertThrows(MojException.BadRequest.class, () ->
            panelService.processEmpanelled(jurorListRequestDto, payload)
        );
    }

    @Test
    void processEmpanelledNoJurorStatusSet() {
        Panel panel = createSinglePanelData();
        panel.setResult(null);
        doReturn(panel).when(panelRepository).findByTrialTrialNumberAndJurorPoolJurorJurorNumber(anyString(),
            anyString());
        JurorListRequestDto jurorListRequestDto =
            createEmpanelledListRequestDto(Collections.singletonList(panel));
        jurorListRequestDto.setNumberRequested(1);
        BureauJWTPayload payload = buildPayload();
        Assertions.assertThrows(MojException.BadRequest.class, () ->
            panelService.processEmpanelled(jurorListRequestDto, payload)
        );
    }

    @Test
    void processEmpanelledWrongJurorStatusSet() {
        Panel panel = createSinglePanelData();
        panel.setResult(PanelResult.RETURNED);
        doReturn(panel).when(panelRepository).findByTrialTrialNumberAndJurorPoolJurorJurorNumber(anyString(),
            anyString());
        JurorListRequestDto jurorListRequestDto =
            createEmpanelledListRequestDto(Collections.singletonList(panel));
        jurorListRequestDto.setNumberRequested(1);
        BureauJWTPayload payload = buildPayload();
        Assertions.assertThrows(MojException.BadRequest.class, () ->
            panelService.processEmpanelled(jurorListRequestDto, payload)
        );
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
        panel.getJurorPool().getStatus().setStatusDesc("Juror");
        panel.getJurorPool().getStatus().setStatus(IJurorStatus.JUROR);

        doReturn(Collections.singletonList(panel)).when(panelRepository)
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(anyString(), anyString());

        List<PanelListDto> dtoList = panelService.getJurySummary("T11111111", "415");
        assertThat(dtoList.size()).as("Expected size to be 1").isEqualTo(1);
        assertThat(dtoList.get(0).getJurorStatus()).as("Expected status to be Juror")
            .isEqualTo("Juror");
    }

    @Test
    void jurySummaryNoJury() {
        Panel panel = createSinglePanelData();
        panel.getJurorPool().getStatus().setStatusDesc("Panelled");
        panel.getJurorPool().getStatus().setStatus(IJurorStatus.PANEL);

        doReturn(Collections.singletonList(panel)).when(panelRepository)
            .findByTrialTrialNumberAndTrialCourtLocationLocCode(anyString(), anyString());

        List<PanelListDto> dtoList = panelService.getJurySummary("T11111111", "415");
        assertThat(dtoList.size()).as("Expected size to be 1").isEqualTo(0);
    }

    @Test
    void processEmpanelledAsJurorNumberRequestedTooMany() {
        JurorListRequestDto jurorListRequestDto =
            createEmpanelledListRequestDto(Collections.singletonList(createSinglePanelData()));
        jurorListRequestDto.setNumberRequested(31);
        BureauJWTPayload payload = buildPayload();

        Assertions.assertThrows(MojException.BadRequest.class, () ->
            panelService.processEmpanelled(jurorListRequestDto, payload)
        );
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
            jurorDetailRequestDto.setFirstName(member.getJurorPool().getJuror().getFirstName());
            jurorDetailRequestDto.setLastName(member.getJurorPool().getJuror().getLastName());
            jurorDetailRequestDto.setJurorNumber(member.getJurorPool().getJurorNumber());
            jurorDetailRequestDto.setResult(member.getResult());
            listDto.add(jurorDetailRequestDto);
        }
        return listDto;
    }

    private List<JurorPool> createJurorPool() {
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("415231201");

        Juror juror = new Juror();
        juror.setJurorNumber("121212121");
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

        Juror juror2 = new Juror();
        juror2.setJurorNumber("111111111");
        juror2.setFirstName("FNAME");
        juror2.setLastName("LNAME");

        JurorPool jurorPool2 = new JurorPool();
        jurorPool2.setOwner("415");
        jurorPool2.setJuror(juror2);
        jurorPool2.setPool(poolRequest);
        jurorPool2.setStatus(jurorStatus);
        jurorPool2.setLocation("Court 1");
        jurorPool2.setTimesSelected(1);


        List<JurorPool> jurorPoolList = new ArrayList<>();
        jurorPoolList.add(jurorPool);
        jurorPoolList.add(jurorPool2);

        return jurorPoolList;
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
        judge.setDescription("Mr Judge");

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
        courtLocation.setOwner("AYLESBURY");
        return courtLocation;
    }


    private Panel createSinglePanelData() {

        Juror juror = new Juror();
        juror.setJurorNumber("111111111");
        juror.setFirstName("FNAME");
        juror.setLastName("LNAME");

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.PANEL);
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
        panel.setTrial(createTrial());
        panel.setResult(PanelResult.JUROR);

        return panel;
    }

    List<Panel> createPanelMembers(int totalMembers) {
        Random random = new Random();
        List<Panel> panelList = new ArrayList<>();
        String jurorNumber = "1111111%02d";
        PanelResult result = PanelResult.JUROR;
        for (int i = 0;
             i < totalMembers;
             i++) {
            Panel temp = createSinglePanelData();
            temp.getJurorPool().getJuror().setJurorNumber(jurorNumber.formatted(i + 1));
            temp.getJurorPool().setTimesSelected(random.nextInt(0, 2));
            temp.setResult(result);

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

    private BureauJWTPayload buildPayload() {
        return BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("SOME_USER")
            .daysToExpire(89)
            .owner("415")
            .build();
    }

}
