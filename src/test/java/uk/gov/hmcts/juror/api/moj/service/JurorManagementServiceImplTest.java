package uk.gov.hmcts.juror.api.moj.service;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorManagementRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorManagementResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.poolmanagement.ReassignPoolMembersResultDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.PoolType;
import uk.gov.hmcts.juror.api.moj.exception.JurorRecordException;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.service.jurormanagement.JurorAppearanceService;
import uk.gov.hmcts.juror.api.moj.service.poolmanagement.JurorManagementConstants;
import uk.gov.hmcts.juror.api.moj.service.poolmanagement.JurorManagementServiceImpl;
import uk.gov.hmcts.juror.api.validation.ResponseInspector;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings({
    "PMD.ExcessiveImports",
    "PMD.TooManyMethods"
})
@RunWith(SpringRunner.class)
public class JurorManagementServiceImplTest {

    @Mock
    private PoolRequestRepository poolRequestRepository;
    @Mock
    private CourtLocationRepository courtLocationRepository;
    @Mock
    private JurorStatusRepository jurorStatusRepository;
    @Mock
    private JurorPoolRepository jurorPoolRepository;
    @Mock
    private JurorHistoryService jurorHistoryService;
    @Mock
    private GeneratePoolNumberService generatePoolNumberService;
    @Mock
    private ResponseInspector responseInspector;
    @Mock
    private PoolMemberSequenceService poolMemberSequenceService;
    @Mock
    private PrintDataService printDataService;
    @Mock
    private JurorAppearanceService appearanceService;
    @Mock
    private ReissueLetterService reissueLetterService;

    @InjectMocks
    JurorManagementServiceImpl jurorManagementService;

    @After
    public void after() {
        TestUtils.afterAll();
    }

    @Before
    public void setUpMocks() {
        doNothing().when(printDataService).printConfirmationLetter(any());
        doReturn(1).when(poolMemberSequenceService).getPoolMemberSequenceNumber(any());
        doReturn(null).when(poolRequestRepository).saveAndFlush(any());
        doReturn(null).when(jurorPoolRepository).save(any());

        doReturn(Optional.of(createJurorStatus(1, "Summoned")))
            .when(jurorStatusRepository).findById(1);
        doReturn(Optional.of(createJurorStatus(2, "Responded")))
            .when(jurorStatusRepository).findById(2);
        doReturn(Optional.of(createJurorStatus(8, "Reassigned")))
            .when(jurorStatusRepository).findById(10);
        doReturn(Optional.of(createJurorStatus(10, "Transferred")))
            .when(jurorStatusRepository).findById(10);

        doReturn(18).when(responseInspector).getYoungestJurorAgeAllowed();
        doReturn(76).when(responseInspector).getTooOldJurorAge();
    }


    @Test
    public void test_reassignJuror_invalidRequest() {

        BureauJwtPayload payload = buildPayload("400");
        JurorManagementRequestDto jurorManagementRequestDto = createInvalidValidJurorManagementRequestDto();

        assertThatExceptionOfType(MojException.BadRequest.class)
            .isThrownBy(() -> jurorManagementService.reassignJurors(payload, jurorManagementRequestDto));

        verify(poolRequestRepository, times(0))
            .findByPoolNumber(anyString());
        verify(courtLocationRepository, times(0))
            .findByLocCode(anyString());
        verify(jurorStatusRepository, times(0))
            .findById(anyInt());
        verify(jurorPoolRepository, times(0))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(
                anyList(), anyBoolean(), any(),
                any(CourtLocation.class), anyList()
            );
        verify(printDataService, times(0))
            .printConfirmationLetter(any());

    }

    @Test
    public void test_reassignJuror_bureauUser_validRequest_happy() {

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("123456789");
        poolRequest.setOwner("400");
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setName("Test Court");
        courtLocation.setLocCode("415");
        courtLocation.setOwner("400");

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(2);
        jurorStatus.setStatusDesc("Responded");

        List<JurorPool> poolMemberList = createJurorPoolList("400");
        poolMemberList.stream().forEach(p -> {
            p.setStatus(jurorStatus);
            p.getJuror().setPoliceCheck(PoliceCheck.ELIGIBLE);
        });

        when(poolRequestRepository.findByPoolNumber(anyString())).thenReturn(Optional.of(poolRequest));
        when(courtLocationRepository.findByLocCode(anyString())).thenReturn(Optional.of(courtLocation));
        when(jurorStatusRepository.findById(anyInt())).thenReturn(Optional.of(jurorStatus));
        when(jurorPoolRepository.findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(
            anyList(), anyBoolean(), anyString(), any(CourtLocation.class),
            anyList())).thenReturn(poolMemberList);
        when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumber(anyString(),
            anyString(), anyString()))
            .thenReturn(Optional.empty());
        when(poolMemberSequenceService
            .getPoolMemberSequenceNumber(anyString())).thenReturn(1);

        BureauJwtPayload payload = TestUtils.mockBureauUser();
        JurorManagementRequestDto jurorManagementRequestDto = createValidJurorManagementRequestDto();

        ReassignPoolMembersResultDto
            jurorsMoved = jurorManagementService.reassignJurors(payload, jurorManagementRequestDto);

        Assertions.assertThat(jurorsMoved.getNumberReassigned()).isEqualTo(1);

        verify(poolRequestRepository, times(2)).findByPoolNumber(anyString());
        verify(courtLocationRepository, times(2)).findByLocCode(anyString());
        verify(jurorStatusRepository, times(1)).findById(anyInt());
        verify(jurorPoolRepository, times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(
                anyList(), anyBoolean(), anyString(),
                any(CourtLocation.class), anyList());
        verify(jurorPoolRepository, times(1))
            .findByOwnerAndJurorJurorNumberAndPoolPoolNumber(anyString(), anyString(),
                anyString());
        verify(poolMemberSequenceService, times(1))
            .getPoolMemberSequenceNumber(anyString());
        verify(jurorHistoryService, times(1))
            .createReassignPoolMemberHistory(any(), any(), any());
        verify(printDataService, times(1)).printConfirmationLetter(any());
    }

    @Test
    public void test_reassignJuror_bureauUser_validRequest_SummonedJuror() {
        TestUtils.mockBureauUser();
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("123456789");
        poolRequest.setOwner("400");
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setName("Test Court");
        courtLocation.setLocCode("415");
        courtLocation.setOwner("400");

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(1);
        jurorStatus.setStatusDesc("Summoned");

        List<JurorPool> poolMemberList = createJurorPoolList("400");
        poolMemberList.stream().forEach(p -> {
            p.setStatus(jurorStatus);
            p.getJuror().setPoliceCheck(PoliceCheck.NOT_CHECKED);
        });

        when(poolRequestRepository.findByPoolNumber(anyString())).thenReturn(Optional.of(poolRequest));
        when(courtLocationRepository.findByLocCode(anyString())).thenReturn(Optional.of(courtLocation));
        when(jurorStatusRepository.findById(anyInt())).thenReturn(Optional.of(jurorStatus));
        when(jurorPoolRepository.findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(
            anyList(), anyBoolean(), anyString(), any(CourtLocation.class),
            anyList())).thenReturn(poolMemberList);
        when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumber(anyString(),
            anyString(), anyString()))
            .thenReturn(Optional.empty());
        when(poolMemberSequenceService
            .getPoolMemberSequenceNumber(anyString())).thenReturn(1);

        BureauJwtPayload payload = buildPayload("400");
        JurorManagementRequestDto jurorManagementRequestDto = createValidJurorManagementRequestDto();

        ReassignPoolMembersResultDto
            jurorsMoved = jurorManagementService.reassignJurors(payload, jurorManagementRequestDto);

        Assertions.assertThat(jurorsMoved.getNumberReassigned()).isEqualTo(1);

        verify(poolRequestRepository, times(2)).findByPoolNumber(anyString());
        verify(courtLocationRepository, times(2)).findByLocCode(anyString());
        verify(jurorStatusRepository, times(1)).findById(anyInt());
        verify(jurorPoolRepository, times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(
                anyList(), anyBoolean(), anyString(),
                any(CourtLocation.class), anyList());
        verify(jurorPoolRepository, times(1))
            .findByOwnerAndJurorJurorNumberAndPoolPoolNumber(anyString(), anyString(),
                anyString());
        verify(poolMemberSequenceService, times(1))
            .getPoolMemberSequenceNumber(anyString());
        verify(jurorHistoryService, times(1))
            .createReassignPoolMemberHistory(any(), any(), any());
        verifyNoInteractions(printDataService);
    }

    @Test
    public void test_reassignJuror_courtUser_validRequest_toCourtOwnedPool() {

        String courtOwner = "415";
        String sourcePoolNumber = "123456789";
        String targetPoolNumber = "987654321";

        PoolRequest sourcePoolRequest = new PoolRequest();
        sourcePoolRequest.setPoolNumber(sourcePoolNumber);
        sourcePoolRequest.setOwner(courtOwner);

        PoolRequest targetpoolRequest = new PoolRequest();
        targetpoolRequest.setPoolNumber(targetPoolNumber);
        targetpoolRequest.setOwner(courtOwner);

        CourtLocation primaryCourtLocation = new CourtLocation();
        primaryCourtLocation.setName("Test Primary Court");
        primaryCourtLocation.setLocCode(courtOwner);
        primaryCourtLocation.setOwner(courtOwner);

        CourtLocation satelliteCourtLocation = new CourtLocation();
        satelliteCourtLocation.setName("Test Satellite Court");
        String satelliteCourtCode = "767";
        satelliteCourtLocation.setLocCode(satelliteCourtCode);
        satelliteCourtLocation.setOwner(courtOwner);

        sourcePoolRequest.setCourtLocation(primaryCourtLocation);
        targetpoolRequest.setCourtLocation(satelliteCourtLocation);

        JurorStatus respondedStatus = new JurorStatus();
        respondedStatus.setStatus(2);
        respondedStatus.setStatusDesc("Responded");

        JurorStatus reassignedStatus = new JurorStatus();
        reassignedStatus.setStatus(8);
        reassignedStatus.setStatusDesc("Reassigned");

        List<JurorPool> poolMemberList = createJurorPoolList(courtOwner);

        when(poolRequestRepository.findByPoolNumber(sourcePoolNumber))
            .thenReturn(Optional.of(sourcePoolRequest));
        when(poolRequestRepository.findByPoolNumber(targetPoolNumber))
            .thenReturn(Optional.of(targetpoolRequest));
        when(courtLocationRepository.findByLocCode(courtOwner))
            .thenReturn(Optional.of(primaryCourtLocation));
        when(courtLocationRepository.findByLocCode(satelliteCourtCode))
            .thenReturn(Optional.of(satelliteCourtLocation));
        when(jurorStatusRepository.findById(2)).thenReturn(Optional.of(respondedStatus));
        when(jurorStatusRepository.findById(8)).thenReturn(Optional.of(reassignedStatus));
        when(jurorPoolRepository.findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(
            anyList(), anyBoolean(), anyString(), any(CourtLocation.class),
            anyList())).thenReturn(poolMemberList);
        when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumber(anyString(),
            anyString(), anyString()))
            .thenReturn(Optional.empty());
        when(poolMemberSequenceService
            .getPoolMemberSequenceNumber(anyString())).thenReturn(1);

        TestUtils.mockCourtUser(courtOwner, "COURT_USER");

        BureauJwtPayload payload = buildPayload(courtOwner);
        JurorManagementRequestDto jurorManagementRequestDto = new JurorManagementRequestDto(sourcePoolNumber,
            courtOwner, List.of("123456789"), targetPoolNumber, satelliteCourtCode, LocalDate.now());

        ReassignPoolMembersResultDto jurorsMoved =
            jurorManagementService.reassignJurors(payload, jurorManagementRequestDto);

        Assertions.assertThat(jurorsMoved.getNumberReassigned()).isEqualTo(1);

        verify(poolRequestRepository, times(2))
            .findByPoolNumber(anyString());
        verify(courtLocationRepository, times(4))
            .findByLocCode(anyString());
        verify(jurorStatusRepository, times(1))
            .findById(anyInt());
        verify(jurorPoolRepository, times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(
                anyList(), anyBoolean(), anyString(),
                any(CourtLocation.class), anyList());
        verify(jurorPoolRepository, times(1))
            .findByOwnerAndJurorJurorNumberAndPoolPoolNumber(anyString(), anyString(),
                anyString());
        verify(poolMemberSequenceService, times(1))
            .getPoolMemberSequenceNumber(anyString());
        verify(jurorHistoryService, times(1))
            .createReassignPoolMemberHistory(any(), any(), any());
        verify(printDataService, never())
            .printConfirmationLetter(any());

        ArgumentCaptor<JurorPool> jurorPoolArgumentCaptor = ArgumentCaptor.forClass(JurorPool.class);
        verify(jurorPoolRepository, times(2)).save(jurorPoolArgumentCaptor.capture());

        JurorPool newJurorPool =
            jurorPoolArgumentCaptor.getAllValues().stream().filter(jurorPool ->
                jurorPool.getPoolNumber().equalsIgnoreCase(targetPoolNumber)).findFirst().orElse(null);
        Assertions.assertThat(newJurorPool).isNotNull();
        Assertions.assertThat(newJurorPool.getOwner()).isEqualTo("415");
    }

    @Test
    public void testReassignJurorCourtUserValidRequestToCourtOwnedPoolSameLocation() {

        final String courtOwner = "415";
        final String sourcePoolNumber = "123456789";
        final String targetPoolNumber = "987654321";

        PoolRequest sourcePoolRequest = new PoolRequest();
        sourcePoolRequest.setPoolNumber(sourcePoolNumber);
        sourcePoolRequest.setOwner(courtOwner);
        sourcePoolRequest.setCourtLocation(createCourtLocation("415", "415"));

        PoolRequest targetpoolRequest = new PoolRequest();
        targetpoolRequest.setPoolNumber(targetPoolNumber);
        targetpoolRequest.setOwner(courtOwner);
        targetpoolRequest.setCourtLocation(createCourtLocation("415", "415"));

        CourtLocation primaryCourtLocation = new CourtLocation();
        primaryCourtLocation.setName("Test Primary Court");
        primaryCourtLocation.setLocCode(courtOwner);
        primaryCourtLocation.setOwner(courtOwner);

        JurorStatus respondedStatus = new JurorStatus();
        respondedStatus.setStatus(2);
        respondedStatus.setStatusDesc("Responded");

        JurorStatus reassignedStatus = new JurorStatus();
        reassignedStatus.setStatus(8);
        reassignedStatus.setStatusDesc("Reassigned");

        List<JurorPool> poolMemberList = createJurorPoolList(courtOwner);

        when(poolRequestRepository.findByPoolNumber(sourcePoolNumber))
            .thenReturn(Optional.of(sourcePoolRequest));
        when(poolRequestRepository.findByPoolNumber(targetPoolNumber))
            .thenReturn(Optional.of(targetpoolRequest));
        when(courtLocationRepository.findByLocCode(courtOwner))
            .thenReturn(Optional.of(primaryCourtLocation));
        when(courtLocationRepository.findByLocCode(primaryCourtLocation.getLocCode()))
            .thenReturn(Optional.of(primaryCourtLocation));
        when(courtLocationRepository.findByLocCode(targetpoolRequest.getCourtLocation().getLocCode()))
            .thenReturn(Optional.of(primaryCourtLocation));
        when(jurorStatusRepository.findById(2)).thenReturn(Optional.of(respondedStatus));
        when(jurorStatusRepository.findById(8)).thenReturn(Optional.of(reassignedStatus));
        when(jurorPoolRepository.findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(
            anyList(), anyBoolean(), anyString(), any(CourtLocation.class),
            anyList())).thenReturn(poolMemberList);
        when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumber(anyString(),
                                                                                 anyString(), anyString()))
            .thenReturn(Optional.empty());
        when(poolMemberSequenceService
                 .getPoolMemberSequenceNumber(anyString())).thenReturn(1);

        TestUtils.mockCourtUser(courtOwner, "COURT_USER");

        BureauJwtPayload payload = buildPayload(courtOwner);
        JurorManagementRequestDto jurorManagementRequestDto = new JurorManagementRequestDto(sourcePoolNumber,
            courtOwner, List.of("123456789"), targetPoolNumber, primaryCourtLocation.getLocCode(), LocalDate.now());

        ReassignPoolMembersResultDto jurorsMoved =
            jurorManagementService.reassignJurors(payload, jurorManagementRequestDto);

        Assertions.assertThat(jurorsMoved.getNumberReassigned()).isEqualTo(1);

        verify(poolRequestRepository, times(2))
            .findByPoolNumber(anyString());
        verify(courtLocationRepository, times(4))
            .findByLocCode(anyString());
        verify(jurorStatusRepository, times(1))
            .findById(anyInt());
        verify(jurorPoolRepository, times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(
                anyList(), anyBoolean(), anyString(),
                any(CourtLocation.class), anyList());
        verify(jurorPoolRepository, times(1))
            .findByOwnerAndJurorJurorNumberAndPoolPoolNumber(anyString(), anyString(),
                                                             anyString());
        verify(poolMemberSequenceService, times(1))
            .getPoolMemberSequenceNumber(anyString());
        verify(jurorHistoryService, times(1))
            .createReassignPoolMemberHistory(any(), any(), any());
        verify(printDataService, never())
            .printConfirmationLetter(any());

        ArgumentCaptor<JurorPool> jurorPoolArgumentCaptor = ArgumentCaptor.forClass(JurorPool.class);
        verify(jurorPoolRepository, times(2)).save(jurorPoolArgumentCaptor.capture());

        JurorPool newJurorPool =
            jurorPoolArgumentCaptor.getAllValues().stream().filter(jurorPool ->
                jurorPool.getPoolNumber().equalsIgnoreCase(targetPoolNumber)).findFirst().orElse(null);
        Assertions.assertThat(newJurorPool).isNotNull();
        Assertions.assertThat(newJurorPool.getOwner()).isEqualTo("415");
    }


    @Test
    public void testReassignJurorCourtUserValidRequestToCourtOwnedPoolWithAttendances() {

        final String courtOwner = "415";
        final String sourcePoolNumber = "123456789";
        final String targetPoolNumber = "987654321";
        final String jurorNumber = "123456789";

        PoolRequest sourcePoolRequest = new PoolRequest();
        sourcePoolRequest.setPoolNumber(sourcePoolNumber);
        sourcePoolRequest.setOwner(courtOwner);
        sourcePoolRequest.setCourtLocation(createCourtLocation("415", "415"));

        PoolRequest targetpoolRequest = new PoolRequest();
        targetpoolRequest.setPoolNumber(targetPoolNumber);
        targetpoolRequest.setOwner(courtOwner);
        targetpoolRequest.setCourtLocation(createCourtLocation("415", "415"));

        CourtLocation primaryCourtLocation = new CourtLocation();
        primaryCourtLocation.setName("Test Primary Court");
        primaryCourtLocation.setLocCode(courtOwner);
        primaryCourtLocation.setOwner(courtOwner);

        JurorStatus respondedStatus = new JurorStatus();
        respondedStatus.setStatus(2);
        respondedStatus.setStatusDesc("Responded");

        JurorStatus reassignedStatus = new JurorStatus();
        reassignedStatus.setStatus(8);
        reassignedStatus.setStatusDesc("Reassigned");

        final List<JurorPool> poolMemberList = createJurorPoolList(courtOwner);

        JurorPool targetJurorPool = createJurorPoolList(courtOwner).get(0);
        targetJurorPool.setPool(targetpoolRequest);

        when(appearanceService.hasAttendancesInPool(jurorNumber, targetPoolNumber)).thenReturn(true);

        when(poolRequestRepository.findByPoolNumber(sourcePoolNumber))
            .thenReturn(Optional.of(sourcePoolRequest));
        when(poolRequestRepository.findByPoolNumber(targetPoolNumber))
            .thenReturn(Optional.of(targetpoolRequest));
        when(courtLocationRepository.findByLocCode(courtOwner))
            .thenReturn(Optional.of(primaryCourtLocation));
        when(courtLocationRepository.findByLocCode(primaryCourtLocation.getLocCode()))
            .thenReturn(Optional.of(primaryCourtLocation));
        when(jurorStatusRepository.findById(2)).thenReturn(Optional.of(respondedStatus));
        when(jurorStatusRepository.findById(8)).thenReturn(Optional.of(reassignedStatus));
        when(jurorPoolRepository.findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(
            anyList(), anyBoolean(), anyString(), any(CourtLocation.class),
            anyList())).thenReturn(poolMemberList);
        when(jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumber(anyString(),
                                                                                 anyString(), anyString()))
            .thenReturn(Optional.of(targetJurorPool));

        TestUtils.mockCourtUser(courtOwner, "COURT_USER");

        BureauJwtPayload payload = buildPayload(courtOwner);
        JurorManagementRequestDto jurorManagementRequestDto = new JurorManagementRequestDto(sourcePoolNumber,
            courtOwner, List.of(jurorNumber), targetPoolNumber, primaryCourtLocation.getLocCode(), LocalDate.now());

        ReassignPoolMembersResultDto jurorsMoved =
            jurorManagementService.reassignJurors(payload, jurorManagementRequestDto);

        Assertions.assertThat(jurorsMoved.getNumberReassigned()).isEqualTo(1);

        verify(poolRequestRepository, times(2))
            .findByPoolNumber(anyString());
        verify(courtLocationRepository, times(4))
            .findByLocCode(anyString());
        verify(jurorStatusRepository, times(1))
            .findById(anyInt());
        verify(jurorPoolRepository, times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(
                anyList(), anyBoolean(), anyString(),
                any(CourtLocation.class), anyList());
        verify(jurorPoolRepository, times(1))
            .findByOwnerAndJurorJurorNumberAndPoolPoolNumber(anyString(), anyString(),
                                                             anyString());

        verify(jurorHistoryService, times(1))
            .createReassignPoolMemberHistory(any(), any(), any());
        verify(printDataService, never())
            .printConfirmationLetter(any());

        ArgumentCaptor<JurorPool> jurorPoolArgumentCaptor = ArgumentCaptor.forClass(JurorPool.class);
        verify(jurorPoolRepository, times(2)).save(jurorPoolArgumentCaptor.capture());

        JurorPool newJurorPool =
            jurorPoolArgumentCaptor.getAllValues().stream().filter(jurorPool ->
                jurorPool.getPoolNumber().equalsIgnoreCase(targetPoolNumber)).findFirst().orElse(null);
        Assertions.assertThat(newJurorPool).isNotNull();
        Assertions.assertThat(newJurorPool.getOwner()).isEqualTo("415");
    }

    @Test
    public void test_reassignJuror_courtUser_validRequest_toBureauOwnedPool() {

        String courtOwner = "415";
        String sourcePoolNumber = "123456789";
        String targetPoolNumber = "987654321";

        PoolRequest sourcePoolRequest = new PoolRequest();
        sourcePoolRequest.setPoolNumber(sourcePoolNumber);
        sourcePoolRequest.setOwner(courtOwner);

        PoolRequest targetpoolRequest = new PoolRequest();
        targetpoolRequest.setPoolNumber(targetPoolNumber);
        String bureauOwner = "400";
        targetpoolRequest.setOwner(bureauOwner);

        CourtLocation primaryCourtLocation = new CourtLocation();
        primaryCourtLocation.setName("Test Primary Court");
        primaryCourtLocation.setLocCode(courtOwner);
        primaryCourtLocation.setOwner(courtOwner);

        sourcePoolRequest.setCourtLocation(primaryCourtLocation);
        targetpoolRequest.setCourtLocation(primaryCourtLocation);

        JurorStatus respondedStatus = new JurorStatus();
        respondedStatus.setStatus(2);
        respondedStatus.setStatusDesc("Responded");

        JurorStatus reassignedStatus = new JurorStatus();
        reassignedStatus.setStatus(8);
        reassignedStatus.setStatusDesc("Reassigned");

        when(poolRequestRepository.findByPoolNumber(sourcePoolNumber)).thenReturn(Optional.of(sourcePoolRequest));
        when(poolRequestRepository.findByPoolNumber(targetPoolNumber)).thenReturn(Optional.of(targetpoolRequest));
        when(courtLocationRepository.findByLocCode(courtOwner)).thenReturn(Optional.of(primaryCourtLocation));
        BureauJwtPayload payload = buildPayload(courtOwner);
        JurorManagementRequestDto jurorManagementRequestDto = new JurorManagementRequestDto(sourcePoolNumber,
            courtOwner, List.of("123456789"), targetPoolNumber, courtOwner, LocalDate.now());

       jurorManagementService.reassignJurors(payload, jurorManagementRequestDto);

        verify(poolRequestRepository, times(2))
            .findByPoolNumber(anyString());
        verify(courtLocationRepository, times(4))
            .findByLocCode(anyString());
        verifyNoInteractions(jurorHistoryService);
        verify(printDataService, never())
            .printConfirmationLetter(any());
        verify(jurorPoolRepository, never())
            .save(any());
    }

    @Test
    public void test_reassignJuror_noSourcePoolRequest() {

        when(poolRequestRepository.findByPoolNumber(anyString()))
            .thenReturn(Optional.empty());

        BureauJwtPayload payload = buildPayload("400");
        JurorManagementRequestDto jurorManagementRequestDto = createValidJurorManagementRequestDto();

        assertThatExceptionOfType(MojException.NotFound.class)
            .isThrownBy(() -> jurorManagementService.reassignJurors(payload, jurorManagementRequestDto));

        verify(poolRequestRepository, times(0))
            .findByPoolNumber(anyString());
        verify(courtLocationRepository, times(1))
            .findByLocCode(anyString());
        verify(jurorStatusRepository, times(0))
            .findById(anyInt());
        verify(jurorPoolRepository, times(0))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(
                anyList(), anyBoolean(), any(),
                any(CourtLocation.class), anyList()
            );
        verify(printDataService, times(0))
            .printConfirmationLetter(any());
    }

    @Test
    public void test_reassignJuror_noTargetPoolRequest() {

        PoolRequest poolRequest = new PoolRequest();

        when(poolRequestRepository.findByPoolNumber(anyString()))
            .thenReturn(Optional.of(poolRequest));
        when(courtLocationRepository.findByLocCode(anyString()))
            .thenReturn(Optional.empty());

        BureauJwtPayload payload = buildPayload("400");
        JurorManagementRequestDto jurorManagementRequestDto = createValidJurorManagementRequestDto();

        assertThatExceptionOfType(MojException.NotFound.class)
            .isThrownBy(() -> jurorManagementService.reassignJurors(payload, jurorManagementRequestDto));

        verify(poolRequestRepository, times(0))
            .findByPoolNumber(anyString());
        verify(courtLocationRepository, times(1))
            .findByLocCode(anyString());
        verify(jurorStatusRepository, times(0))
            .findById(anyInt());
        verify(jurorPoolRepository, times(0))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(
                anyList(), anyBoolean(), any(),
                any(CourtLocation.class), anyList()
            );
        verify(printDataService, times(0))
            .printConfirmationLetter(any());
    }

    @Test
    public void test_reassignJuror_noSourceJurorsFound() {

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setName("Test Court");
        courtLocation.setLocCode("415");
        courtLocation.setOwner("400");

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(1);
        jurorStatus.setStatusDesc("Summoned");

        when(poolRequestRepository.findByPoolNumber(anyString()))
            .thenReturn(Optional.of(new PoolRequest()));
        when(courtLocationRepository.findByLocCode(anyString()))
            .thenReturn(Optional.of(courtLocation));
        when(jurorStatusRepository.findById(anyInt())).thenReturn(Optional.of(jurorStatus));

        BureauJwtPayload payload = buildPayload("400");
        JurorManagementRequestDto jurorManagementRequestDto = createValidJurorManagementRequestDto();

        assertThatExceptionOfType(MojException.NotFound.class)
            .isThrownBy(() -> jurorManagementService.reassignJurors(payload, jurorManagementRequestDto));

        verify(poolRequestRepository, times(2))
            .findByPoolNumber(anyString());
        verify(courtLocationRepository, times(2))
            .findByLocCode(anyString());
        verify(jurorStatusRepository, times(0))
            .findById(anyInt());
        verify(jurorPoolRepository, times(0))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(
                anyList(), anyBoolean(), any(),
                any(CourtLocation.class), anyList()
            );
        verify(printDataService, times(0))
            .printConfirmationLetter(any());
    }

    @Test
    public void test_transferPoolMembers_happyPath_onePoolMember_firstTransfer() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        BureauJwtPayload payload = TestUtils.mockCourtUser(sourceCourtLocCode, "COURT_USER");
        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode,
            targetCourtLocCode,
            targetStartDate,
            jurorNumbers
        );

        JurorPool testJurorPool = createTestJurorPool(sourceCourtLocCode, "111111111", sourcePoolNumber);
        CourtLocation courtLocation = testJurorPool.getCourt();
        CourtLocation targetCourtLocation = createCourtLocation(targetCourtLocCode, targetCourtLocCode);

        doReturn(Optional.of(createTestPoolRequest(sourcePoolNumber, sourceCourtLocCode, courtLocation)))
            .when(poolRequestRepository).findById(sourcePoolNumber);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(sourceCourtLocCode);
        doReturn(Optional.of(targetCourtLocation)).when(courtLocationRepository)
            .findByLocCode(targetCourtLocCode);
        doReturn("435230701").when(generatePoolNumberService)
            .generatePoolNumber(targetCourtLocCode, targetStartDate);

        doReturn(null).when(jurorPoolRepository)
            .findByJurorNumberAndIsActiveAndCourt(any(), anyBoolean(), any());
        doReturn(Collections.singletonList(testJurorPool)).when(jurorPoolRepository)
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(anyList(),
                anyBoolean(),
                anyString(),
                any(CourtLocation.class),
                anyList());

        doReturn(createTestPoolRequest("435230701", targetCourtLocCode,
            targetCourtLocation
        )).when(poolRequestRepository).save(any());

        int successfulCount = jurorManagementService.transferPoolMembers(payload, requestDto);

        Assertions.assertThat(successfulCount)
            .as("Expect the single pool member to be transferred successfully")
            .isEqualTo(1);

        verify(poolRequestRepository, times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, times(1))
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(anyList(),
                anyBoolean(),
                anyString(),
                any(CourtLocation.class),
                anyList()
            );
        verify(generatePoolNumberService, times(1))
            .generatePoolNumber(any(), any());
        verify(poolMemberSequenceService, times(jurorNumbers.size()))
            .getPoolMemberSequenceNumber(any());

        verify(poolRequestRepository, times(1))
            .save(any());
        // one save invocation for each newly created juror, and another save invocation to update the source juror
        // record
        verify(jurorPoolRepository, times(jurorNumbers.size() * 2))
            .save(any());
        verify(poolRequestRepository, times(1))
            .saveAndFlush(any());

        verify(jurorHistoryService, times(jurorNumbers.size()))
            .createTransferCourtHistory(any(), any());
    }

    @Test
    public void test_transferPoolMembers_happyPath_multiplePoolMembers() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333");

        BureauJwtPayload payload = TestUtils.mockCourtUser(sourceCourtLocCode, "COURT_USER");
        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode,
            targetCourtLocCode,
            targetStartDate,
            jurorNumbers
        );

        JurorPool testJurorPool1 = createTestJurorPool(sourceCourtLocCode, "111111111", sourcePoolNumber);
        JurorPool testJurorPool2 = createTestJurorPool(sourceCourtLocCode, "222222222", sourcePoolNumber);
        JurorPool testJurorPool3 = createTestJurorPool(sourceCourtLocCode, "333333333", sourcePoolNumber);
        CourtLocation courtLocation = testJurorPool1.getCourt();
        CourtLocation targetCourtLocation = createCourtLocation(targetCourtLocCode, targetCourtLocCode);

        doReturn(Optional.of(createTestPoolRequest(sourcePoolNumber, sourceCourtLocCode, courtLocation)))
            .when(poolRequestRepository).findById(sourcePoolNumber);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(sourceCourtLocCode);
        doReturn(Optional.of(targetCourtLocation)).when(courtLocationRepository)
            .findByLocCode(targetCourtLocCode);
        doReturn("435230701").when(generatePoolNumberService)
            .generatePoolNumber(targetCourtLocCode, targetStartDate);

        doReturn(null).when(jurorPoolRepository)
            .findByJurorNumberAndIsActiveAndCourt(any(), anyBoolean(), any());
        doReturn(Arrays.asList(testJurorPool1, testJurorPool2, testJurorPool3)).when(jurorPoolRepository)
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(anyList(),
                anyBoolean(),
                anyString(),
                any(CourtLocation.class),
                anyList());

        doReturn(createTestPoolRequest("435230701", targetCourtLocCode,
            targetCourtLocation
        )).when(poolRequestRepository).save(any());

        int successfulCount = jurorManagementService.transferPoolMembers(payload, requestDto);

        Assertions.assertThat(successfulCount)
            .as("Expect all 3 pool members to be transferred successfully")
            .isEqualTo(3);

        verify(poolRequestRepository, times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, times(1))
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(anyList(),
                anyBoolean(),
                anyString(),
                any(CourtLocation.class),
                anyList()
            );
        verify(generatePoolNumberService, times(1))
            .generatePoolNumber(any(), any());
        verify(poolMemberSequenceService, times(jurorNumbers.size()))
            .getPoolMemberSequenceNumber(any());

        verify(poolRequestRepository, times(1))
            .save(any());
        // one save invocation for each newly created juror, and another save invocation to update the source juror
        // record
        verify(jurorPoolRepository, times(jurorNumbers.size() * 2))
            .save(any());
        verify(poolRequestRepository, times(1))
            .saveAndFlush(any());

        verify(jurorHistoryService, times(jurorNumbers.size()))
            .createTransferCourtHistory(any(), any());

    }

    @Test
    public void test_transferPoolMembers_happyPath_onePoolMember_transferBack() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        final BureauJwtPayload payload = TestUtils.mockCourtUser(sourceCourtLocCode, "COURT_USER");
        final JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode,
            targetCourtLocCode,
            targetStartDate,
            jurorNumbers);

        JurorPool sourceJurorPool = createTestJurorPool(sourceCourtLocCode, "111111111", sourcePoolNumber);
        CourtLocation courtLocation = sourceJurorPool.getCourt();
        final CourtLocation targetCourtLocation = createCourtLocation(targetCourtLocCode, targetCourtLocCode);

        JurorPool historicJurorPool = createTestJurorPool(targetCourtLocCode, "111111111", "435230105");
        historicJurorPool.setStatus(createJurorStatus(10, "Transferred"));

        doReturn(Optional.of(createTestPoolRequest(sourcePoolNumber, sourceCourtLocCode, courtLocation)))
            .when(poolRequestRepository).findById(sourcePoolNumber);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(sourceCourtLocCode);
        doReturn(Optional.of(targetCourtLocation)).when(courtLocationRepository)
            .findByLocCode(targetCourtLocCode);
        doReturn("435230701").when(generatePoolNumberService)
            .generatePoolNumber(targetCourtLocCode, targetStartDate);

        doReturn(historicJurorPool).when(jurorPoolRepository)
            .findByJurorNumberAndIsActiveAndCourt(any(), anyBoolean(), any());
        doReturn(Collections.singletonList(sourceJurorPool)).when(jurorPoolRepository)
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(anyList(),
                anyBoolean(),
                anyString(),
                any(CourtLocation.class),
                anyList());

        doReturn(createTestPoolRequest("435230701", targetCourtLocCode, targetCourtLocation))
            .when(poolRequestRepository).save(any());

        int successfulCount = jurorManagementService.transferPoolMembers(payload, requestDto);

        Assertions.assertThat(successfulCount)
            .as("Expect the single pool member to be transferred successfully")
            .isEqualTo(1);

        verify(poolRequestRepository, times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, times(1))
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(anyList(),
                anyBoolean(),
                anyString(),
                any(CourtLocation.class),
                anyList());
        verify(generatePoolNumberService, times(1))
            .generatePoolNumber(any(), any());
        verify(poolMemberSequenceService, times(jurorNumbers.size()))
            .getPoolMemberSequenceNumber(any());

        verify(poolRequestRepository, times(1))
            .save(any());
        // one save invocation for each newly created juror, and another save invocation to update the source juror
        // record
        // plus one additional save invocation for logically deleting the previously transferred record
        verify(jurorPoolRepository, times(jurorNumbers.size() * 2 + 1))
            .save(any());
        verify(poolRequestRepository, times(1))
            .saveAndFlush(any());
        verify(jurorHistoryService, times(jurorNumbers.size()))
            .createTransferCourtHistory(any(), any());
    }

    @Test
    public void test_transferPoolMembers_multiplePoolMembers_someFailures() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333", "444444444", "555555555");

        final BureauJwtPayload payload = TestUtils.mockCourtUser(sourceCourtLocCode, "COURT_USER");
        final JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode,
            targetCourtLocCode,
            targetStartDate,
            jurorNumbers);

        JurorPool testJurorPool1 = createTestJurorPool(sourceCourtLocCode, "111111111", sourcePoolNumber);
        CourtLocation courtLocation = testJurorPool1.getCourt();
        final CourtLocation targetCourtLocation = createCourtLocation(targetCourtLocCode, targetCourtLocCode);

        JurorPool testJurorPool2 = createTestJurorPool(sourceCourtLocCode, "222222222", sourcePoolNumber);
        Juror testJuror2 = testJurorPool2.getJuror();
        testJuror2.setDateOfBirth(targetStartDate.minusYears(76));

        JurorPool testJurorPool3 = createTestJurorPool(sourceCourtLocCode, "333333333", sourcePoolNumber);
        testJurorPool3.setStatus(createJurorStatus(10, "Reassigned"));

        JurorPool testJurorPool4 = createTestJurorPool(sourceCourtLocCode, "444444444", sourcePoolNumber);

        // test pool member 5 intentionally missing

        doReturn(Optional.of(createTestPoolRequest(sourcePoolNumber, sourceCourtLocCode, courtLocation)))
            .when(poolRequestRepository).findById(sourcePoolNumber);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(sourceCourtLocCode);
        doReturn(Optional.of(targetCourtLocation)).when(courtLocationRepository)
            .findByLocCode(targetCourtLocCode);
        doReturn("435230701").when(generatePoolNumberService)
            .generatePoolNumber(targetCourtLocCode, targetStartDate);

        doReturn(null).when(jurorPoolRepository)
            .findByJurorNumberAndIsActiveAndCourt(any(), anyBoolean(), any());
        doReturn(Arrays.asList(testJurorPool1, testJurorPool2, testJurorPool4)).when(jurorPoolRepository)
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(anyList(),
                anyBoolean(),
                anyString(),
                any(CourtLocation.class),
                anyList());

        doReturn(createTestPoolRequest("435230701", targetCourtLocCode,
            targetCourtLocation
        )).when(poolRequestRepository).save(any());

        int successfulCount = jurorManagementService.transferPoolMembers(payload, requestDto);

        Assertions.assertThat(successfulCount)
            .as("Expect 2 pool members to be transferred successfully (3 to be unsuccessful)")
            .isEqualTo(2);

        verify(poolRequestRepository, times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, times(1))
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(anyList(),
                anyBoolean(),
                anyString(),
                any(CourtLocation.class),
                anyList()
            );
        verify(generatePoolNumberService, times(1))
            .generatePoolNumber(any(), any());
        verify(poolMemberSequenceService, times(2))
            .getPoolMemberSequenceNumber(any());

        verify(poolRequestRepository, times(1))
            .save(any());
        // one save invocation for each newly created juror, and another save invocation to update the source juror
        // record
        verify(jurorPoolRepository, times(4))
            .save(any());
        verify(poolRequestRepository, times(1))
            .saveAndFlush(any());
        verify(jurorHistoryService, times(2))
            .createTransferCourtHistory(any(), any());
    }

    @Test
    public void test_transferPoolMembers_unhappyPath_invalidAccess_bureauUser() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        BureauJwtPayload payload = TestUtils.mockCourtUser("400", "BUREAU_USER");
        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode,
            targetCourtLocCode,
            targetStartDate,
            jurorNumbers
        );

        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            jurorManagementService.transferPoolMembers(
                payload,
                requestDto));

        verify(poolRequestRepository, never())
            .findByPoolNumber(sourcePoolNumber);
        verify(courtLocationRepository, never())
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, never())
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, never())
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(anyList(),
                anyBoolean(),
                anyString(),
                any(CourtLocation.class),
                anyList()
            );
        verify(generatePoolNumberService, never())
            .generatePoolNumber(any(), any());
        verify(poolMemberSequenceService, never())
            .getPoolMemberSequenceNumber(any());

        verify(poolRequestRepository, never())
            .save(any());
        verify(jurorPoolRepository, never())
            .save(any());
        verify(poolRequestRepository, never())
            .saveAndFlush(any());
        verifyNoInteractions(jurorHistoryService);
    }

    @Test
    public void test_transferPoolMembers_unhappyPath_invalidJurorNumber() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        // juror number only 8 chars
        List<String> jurorNumbers = Collections.singletonList("11111111");

        BureauJwtPayload payload = TestUtils.mockCourtUser(sourceCourtLocCode, "COURT_USER");
        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode, targetCourtLocCode, targetStartDate, jurorNumbers);

        assertThatExceptionOfType(JurorRecordException.InvalidJurorNumber.class)
            .isThrownBy(() -> jurorManagementService.transferPoolMembers(
                payload,
                requestDto
            ));

        verify(poolRequestRepository, never())
            .findByPoolNumber(sourcePoolNumber);
        verify(courtLocationRepository, never())
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, never())
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, never())
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(anyList(),
                anyBoolean(),
                anyString(),
                any(CourtLocation.class),
                anyList()
            );
        verify(generatePoolNumberService, never())
            .generatePoolNumber(any(), any());
        verify(poolMemberSequenceService, never())
            .getPoolMemberSequenceNumber(any());

        verify(poolRequestRepository, never())
            .save(any());
        verify(jurorPoolRepository, never())
            .save(any());
        verify(poolRequestRepository, never())
            .saveAndFlush(any());
        verifyNoInteractions(jurorHistoryService);
    }

    @Test
    public void test_transferPoolMembers_unhappyPath_sourcePoolRequestNotFound() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        BureauJwtPayload payload = TestUtils.mockCourtUser(sourceCourtLocCode, "COURT_USER");
        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode, targetCourtLocCode, targetStartDate, jurorNumbers);

        doReturn(Optional.empty())
            .when(poolRequestRepository).findById(sourcePoolNumber);

        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            jurorManagementService.transferPoolMembers(
                payload,
                requestDto
            ));

        verify(poolRequestRepository, times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, never())
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, never())
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, never())
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(anyList(),
                anyBoolean(),
                anyString(),
                any(CourtLocation.class),
                anyList()
            );
        verify(generatePoolNumberService, never())
            .generatePoolNumber(any(), any());
        verify(poolMemberSequenceService, never())
            .getPoolMemberSequenceNumber(any());

        verify(poolRequestRepository, never())
            .save(any());
        verify(jurorPoolRepository, never())
            .save(any());
        verify(poolRequestRepository, never())
            .saveAndFlush(any());
        verifyNoInteractions(jurorHistoryService);
    }

    @Test
    public void test_transferPoolMembers_unhappyPath_invalidCourtLocation() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        BureauJwtPayload payload = TestUtils.mockCourtUser("415", "COURT_USER");
        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode,
            targetCourtLocCode,
            targetStartDate,
            jurorNumbers
        );

        JurorPool testJurorPool = createTestJurorPool(sourceCourtLocCode, "111111111", sourcePoolNumber);
        CourtLocation courtLocation = testJurorPool.getCourt();
        doReturn(Optional.of(createTestPoolRequest(sourcePoolNumber, sourceCourtLocCode, courtLocation)))
            .when(poolRequestRepository).findById(sourcePoolNumber);
        doReturn(Optional.empty()).when(courtLocationRepository).findByLocCode(any());

        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            jurorManagementService.transferPoolMembers(
                payload,
                requestDto
            ));

        verify(poolRequestRepository, times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, never())
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, never())
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(anyList(),
                anyBoolean(),
                anyString(),
                any(CourtLocation.class),
                anyList()
            );
        verify(generatePoolNumberService, never())
            .generatePoolNumber(any(), any());
        verify(poolMemberSequenceService, never())
            .getPoolMemberSequenceNumber(any());

        verify(poolRequestRepository, never())
            .save(any());
        verify(jurorPoolRepository, never())
            .save(any());
        verify(poolRequestRepository, never())
            .saveAndFlush(any());
        verifyNoInteractions(jurorHistoryService);
    }

    @Test
    public void test_transferPoolMembers_unhappyPath_invalidAccessToCourtLocation() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        final BureauJwtPayload payload = TestUtils.mockCourtUser("415", "COURT_USER");
        final JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode, targetCourtLocCode, targetStartDate, jurorNumbers);

        JurorPool testJurorPool = createTestJurorPool(sourceCourtLocCode, "111111111", sourcePoolNumber);
        CourtLocation sourceCourtLocation = testJurorPool.getCourt();
        sourceCourtLocation.setOwner("457");
        CourtLocation targetCourtLocation = createCourtLocation(targetCourtLocCode, targetCourtLocCode);

        doReturn(Optional.of(createTestPoolRequest(sourcePoolNumber, sourceCourtLocCode, sourceCourtLocation)))
            .when(poolRequestRepository).findById(sourcePoolNumber);
        doReturn(Optional.of(sourceCourtLocation)).when(courtLocationRepository)
            .findByLocCode(sourceCourtLocCode);
        doReturn(Optional.of(targetCourtLocation)).when(courtLocationRepository)
            .findByLocCode(targetCourtLocCode);

        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            jurorManagementService.transferPoolMembers(
                payload,
                requestDto
            ));

        verify(poolRequestRepository, times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, times(1))
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, never())
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(anyList(),
                anyBoolean(), anyString(), any(CourtLocation.class), anyList());
        verify(generatePoolNumberService, never())
            .generatePoolNumber(any(), any());
        verify(poolMemberSequenceService, never())
            .getPoolMemberSequenceNumber(any());

        verify(poolRequestRepository, never())
            .save(any());
        verify(jurorPoolRepository, never())
            .save(any());
        verify(poolRequestRepository, never())
            .saveAndFlush(any());
        verifyNoInteractions(jurorHistoryService);
    }

    @Test
    public void test_transferPoolMembers_unhappyPath_receivingCourtLocationNotFound() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        BureauJwtPayload payload = TestUtils.mockCourtUser("415", "COURT_USER");
        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode,
            targetCourtLocCode,
            targetStartDate,
            jurorNumbers
        );

        JurorPool testJurorPool = createTestJurorPool(sourceCourtLocCode, "111111111", sourcePoolNumber);
        CourtLocation courtLocation = testJurorPool.getCourt();
        doReturn(Optional.of(createTestPoolRequest(sourcePoolNumber, sourceCourtLocCode, courtLocation)))
            .when(poolRequestRepository).findById(sourcePoolNumber);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(sourceCourtLocCode);
        doReturn(Optional.empty()).when(courtLocationRepository).findByLocCode(targetCourtLocCode);

        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            jurorManagementService.transferPoolMembers(
                payload,
                requestDto
            ));

        verify(poolRequestRepository, times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, times(1))
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, never())
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(anyList(),
                anyBoolean(),
                anyString(),
                any(CourtLocation.class),
                anyList()
            );
        verify(generatePoolNumberService, never())
            .generatePoolNumber(any(), any());
        verify(poolMemberSequenceService, never())
            .getPoolMemberSequenceNumber(any());

        verify(poolRequestRepository, never())
            .save(any());
        verify(jurorPoolRepository, never())
            .save(any());
        verify(poolRequestRepository, never())
            .saveAndFlush(any());
        verifyNoInteractions(jurorHistoryService);
    }

    @Test
    public void test_transferPoolMembers_unhappyPath_transferBack_invalidStatus() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        final BureauJwtPayload payload = TestUtils.mockCourtUser(sourceCourtLocCode, "COURT_USER");
        final JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode,
            targetCourtLocCode,
            targetStartDate,
            jurorNumbers
        );

        JurorPool sourceJurorPool = createTestJurorPool(sourceCourtLocCode, "111111111", sourcePoolNumber);
        CourtLocation courtLocation = sourceJurorPool.getCourt();
        final CourtLocation targetCourtLocation = createCourtLocation(targetCourtLocCode, targetCourtLocCode);

        JurorPool historicJurorPool = createTestJurorPool(targetCourtLocCode, "111111111", "435230105");
        historicJurorPool.setStatus(createJurorStatus(8, "Reassigned"));

        doReturn(Optional.of(createTestPoolRequest(sourcePoolNumber, sourceCourtLocCode, courtLocation)))
            .when(poolRequestRepository).findById(sourcePoolNumber);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(sourceCourtLocCode);
        doReturn(Optional.of(targetCourtLocation)).when(courtLocationRepository)
            .findByLocCode(targetCourtLocCode);
        doReturn("435230701").when(generatePoolNumberService)
            .generatePoolNumber(targetCourtLocCode, targetStartDate);

        doReturn(historicJurorPool).when(jurorPoolRepository)
            .findByJurorNumberAndIsActiveAndCourt(any(), anyBoolean(), any());
        doReturn(Collections.singletonList(sourceJurorPool)).when(jurorPoolRepository)
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(anyList(),
                anyBoolean(),
                anyString(),
                any(CourtLocation.class),
                anyList());

        doReturn(createTestPoolRequest("435230701", targetCourtLocCode,
            targetCourtLocation
        )).when(poolRequestRepository).save(any());

        int successfulCount = jurorManagementService.transferPoolMembers(payload, requestDto);

        Assertions.assertThat(successfulCount)
            .as("Expect the single pool member not to be transferred successfully")
            .isEqualTo(0);

        verify(poolRequestRepository, times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, times(1))
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(anyList(),
                anyBoolean(),
                anyString(),
                any(CourtLocation.class),
                anyList()
            );
        verify(generatePoolNumberService, times(1))
            .generatePoolNumber(any(), any());
        verify(poolMemberSequenceService, never())
            .getPoolMemberSequenceNumber(any());

        verify(poolRequestRepository, times(1))
            .save(any());
        verify(jurorPoolRepository, never())
            .save(any());
        verify(poolRequestRepository, times(1))
            .saveAndFlush(any());
        verifyNoInteractions(jurorHistoryService);

    }

    @Test
    public void test_transferPoolMembers_unhappyPath_onePoolMember_invalidAge_tooOld() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        final BureauJwtPayload payload = TestUtils.mockCourtUser(sourceCourtLocCode, "COURT_USER");
        final JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode,
            targetCourtLocCode,
            targetStartDate,
            jurorNumbers
        );

        JurorPool sourceJurorPool = createTestJurorPool(sourceCourtLocCode, "111111111", sourcePoolNumber);
        Juror sourceJuror = sourceJurorPool.getJuror();
        sourceJuror.setDateOfBirth(targetStartDate.minusYears(76));
        CourtLocation courtLocation = sourceJurorPool.getCourt();
        CourtLocation targetCourtLocation = createCourtLocation(targetCourtLocCode, targetCourtLocCode);

        doReturn(Optional.of(createTestPoolRequest(sourcePoolNumber, sourceCourtLocCode, courtLocation)))
            .when(poolRequestRepository).findById(sourcePoolNumber);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(sourceCourtLocCode);
        doReturn(Optional.of(targetCourtLocation)).when(courtLocationRepository)
            .findByLocCode(targetCourtLocCode);
        doReturn("435230701").when(generatePoolNumberService)
            .generatePoolNumber(targetCourtLocCode, targetStartDate);

        doReturn(null).when(jurorPoolRepository)
            .findByJurorNumberAndIsActiveAndCourt(any(), anyBoolean(), any());
        doReturn(Collections.singletonList(sourceJurorPool)).when(jurorPoolRepository)
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(anyList(),
                anyBoolean(),
                anyString(),
                any(CourtLocation.class),
                anyList());

        doReturn(createTestPoolRequest("435230701", targetCourtLocCode,
            targetCourtLocation
        )).when(poolRequestRepository).save(any());

        int successfulCount = jurorManagementService.transferPoolMembers(payload, requestDto);

        Assertions.assertThat(successfulCount)
            .as("Expect the single pool member not to be transferred successfully")
            .isEqualTo(0);

        verify(poolRequestRepository, times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, times(1))
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(anyList(),
                anyBoolean(),
                anyString(),
                any(CourtLocation.class),
                anyList()
            );
        verify(generatePoolNumberService, times(1))
            .generatePoolNumber(any(), any());
        verify(poolMemberSequenceService, never())
            .getPoolMemberSequenceNumber(any());

        verify(poolRequestRepository, times(1))
            .save(any());
        verify(jurorPoolRepository, never())
            .save(any());
        verify(poolRequestRepository, times(1))
            .saveAndFlush(any());
        verifyNoInteractions(jurorHistoryService);
    }

    @Test
    public void test_transferPoolMembers_unhappyPath_onePoolMember_invalidAge_tooYoung() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");


        final BureauJwtPayload payload = TestUtils.mockCourtUser(sourceCourtLocCode, "COURT_USER");
        final JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode,
            targetCourtLocCode,
            targetStartDate,
            jurorNumbers);

        JurorPool sourceJurorPool = createTestJurorPool(sourceCourtLocCode, "111111111", sourcePoolNumber);
        Juror sourceJuror = sourceJurorPool.getJuror();
        sourceJuror.setDateOfBirth(targetStartDate.minusYears(18).plusDays(1));
        CourtLocation courtLocation = sourceJurorPool.getCourt();
        CourtLocation targetCourtLocation = createCourtLocation(targetCourtLocCode, targetCourtLocCode);

        doReturn(Optional.of(createTestPoolRequest(sourcePoolNumber, sourceCourtLocCode, courtLocation)))
            .when(poolRequestRepository).findById(sourcePoolNumber);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(sourceCourtLocCode);
        doReturn(Optional.of(targetCourtLocation)).when(courtLocationRepository)
            .findByLocCode(targetCourtLocCode);
        doReturn("435230701").when(generatePoolNumberService)
            .generatePoolNumber(targetCourtLocCode, targetStartDate);

        doReturn(null).when(jurorPoolRepository)
            .findByJurorNumberAndIsActiveAndCourt(any(), anyBoolean(), any());
        doReturn(Collections.singletonList(sourceJurorPool)).when(jurorPoolRepository)
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(anyList(),
                anyBoolean(),
                anyString(),
                any(CourtLocation.class),
                anyList());

        doReturn(createTestPoolRequest("435230701", targetCourtLocCode,
            targetCourtLocation
        )).when(poolRequestRepository).save(any());

        int successfulCount = jurorManagementService.transferPoolMembers(payload, requestDto);

        Assertions.assertThat(successfulCount)
            .as("Expect the single pool member not to be transferred successfully")
            .isEqualTo(0);

        verify(poolRequestRepository, times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, times(1))
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(anyList(),
                anyBoolean(),
                anyString(),
                any(CourtLocation.class),
                anyList()
            );
        verify(generatePoolNumberService, times(1))
            .generatePoolNumber(any(), any());
        verify(poolMemberSequenceService, never())
            .getPoolMemberSequenceNumber(any());

        verify(poolRequestRepository, times(1))
            .save(any());
        verify(jurorPoolRepository, never())
            .save(any());
        verify(poolRequestRepository, times(1))
            .saveAndFlush(any());
        verifyNoInteractions(jurorHistoryService);

    }

    @Test
    public void test_transferPoolMembers_unhappyPath_sameCourtOwner() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "767";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        BureauJwtPayload payload = TestUtils.mockCourtUser(sourceCourtLocCode, "COURT_USER");
        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode, targetCourtLocCode, targetStartDate, jurorNumbers);

        JurorPool testJurorPool = createTestJurorPool(sourceCourtLocCode, "111111111", sourcePoolNumber);
        CourtLocation courtLocation = testJurorPool.getCourt();
        CourtLocation targetCourtLocation = createCourtLocation(targetCourtLocCode, sourceCourtLocCode);

        doReturn(Optional.of(createTestPoolRequest(sourcePoolNumber, sourceCourtLocCode, courtLocation)))
            .when(poolRequestRepository).findById(sourcePoolNumber);
        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(sourceCourtLocCode);
        doReturn(Optional.of(targetCourtLocation)).when(courtLocationRepository)
            .findByLocCode(targetCourtLocCode);

        assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
            jurorManagementService.transferPoolMembers(payload, requestDto));

        verify(poolRequestRepository, times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, times(1))
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, never())
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(anyList(),
                anyBoolean(), anyString(), any(CourtLocation.class), anyList());
        verify(generatePoolNumberService, never())
            .generatePoolNumber(any(), any());
        verify(poolMemberSequenceService, never())
            .getPoolMemberSequenceNumber(any());

        verify(poolRequestRepository, never())
            .save(any());
        verify(jurorPoolRepository, never())
            .save(any());
        verify(poolRequestRepository, never())
            .saveAndFlush(any());
        verifyNoInteractions(jurorHistoryService);
    }

    @Test
    public void test_validatePoolMembers_multiplePoolMembers_allAvailable() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333", "444444444");

        final BureauJwtPayload payload = TestUtils.mockCourtUser(sourceCourtLocCode, "COURT_USER");
        final JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode,
            targetCourtLocCode,
            targetStartDate,
            jurorNumbers);

        JurorPool testJurorPool1 = createTestJurorPool(sourceCourtLocCode, "111111111", sourcePoolNumber);
        CourtLocation courtLocation = testJurorPool1.getCourt();

        JurorPool testJurorPool2 = createTestJurorPool(sourceCourtLocCode, "222222222", sourcePoolNumber);
        JurorPool testJurorPool3 = createTestJurorPool(sourceCourtLocCode, "333333333", sourcePoolNumber);
        JurorPool testJurorPool4 = createTestJurorPool(sourceCourtLocCode, "444444444", sourcePoolNumber);

        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(sourceCourtLocCode);
        doReturn(Arrays.asList(testJurorPool1, testJurorPool2, testJurorPool3, testJurorPool4))
            .when(jurorPoolRepository)
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(anyList(), anyBoolean(),
                anyString(), any(CourtLocation.class));

        JurorManagementResponseDto responseDto = jurorManagementService.validatePoolMembers(payload, requestDto);

        verify(courtLocationRepository, times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(jurorPoolRepository, times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(anyList(),
                eq(true), anyString(),
                any(CourtLocation.class));
        verify(jurorPoolRepository, times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(anyList(),
                eq(false), anyString(),
                any(CourtLocation.class));

        Assertions.assertThat(responseDto)
            .as("Expect response DTO to exist")
            .isNotNull();

        Assertions.assertThat(responseDto.getAvailableForMove().size())
            .as("Expect all 4 supplied juror numbers to be available for transfer")
            .isEqualTo(4);
        Assertions.assertThat(responseDto.getAvailableForMove())
            .containsAll(Arrays.asList("111111111", "222222222", "333333333", "444444444"));

        Assertions.assertThat(responseDto.getUnavailableForMove())
            .as("Expect no supplied juror numbers to be unavailable for transfer")
            .isNull();
    }


    @Test
    public void test_validatePoolMembers_reassign_bureau_user() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333", "444444444");

        BureauJwtPayload payload = TestUtils.mockCourtUser("400", "BUREAU_USER");
        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode, targetCourtLocCode, targetStartDate, jurorNumbers);

        JurorPool testJurorPool1 = createTestJurorPool(sourceCourtLocCode, "111111111", sourcePoolNumber);
        CourtLocation courtLocation = testJurorPool1.getCourt();

        JurorPool testJurorPool2 = createTestJurorPool(sourceCourtLocCode, "222222222", sourcePoolNumber);
        JurorPool testJurorPool3 = createTestJurorPool(sourceCourtLocCode, "333333333", sourcePoolNumber);
        JurorPool testJurorPool4 = createTestJurorPool(sourceCourtLocCode, "444444444", sourcePoolNumber);

        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(sourceCourtLocCode);
        doReturn(Arrays.asList(testJurorPool1, testJurorPool2, testJurorPool3, testJurorPool4))
            .when(jurorPoolRepository)
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(anyList(), anyBoolean(),
                anyString(), any(CourtLocation.class));

        JurorManagementResponseDto responseDto = jurorManagementService.validatePoolMembers(payload, requestDto);

        verify(courtLocationRepository, times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(jurorPoolRepository, times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(anyList(),
                eq(true), anyString(),
                any(CourtLocation.class));
        verify(jurorPoolRepository, times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(anyList(),
                eq(false), anyString(),
                any(CourtLocation.class));

        Assertions.assertThat(responseDto)
            .as("Expect response DTO to exist")
            .isNotNull();

        Assertions.assertThat(responseDto.getAvailableForMove().size())
            .as("Expect all 4 supplied juror numbers to be available for reassign")
            .isEqualTo(4);
        Assertions.assertThat(responseDto.getAvailableForMove())
            .containsAll(Arrays.asList("111111111", "222222222", "333333333", "444444444"));

        Assertions.assertThat(responseDto.getUnavailableForMove())
            .as("Expect no supplied juror numbers to be unavailable for reassign")
            .isNull();
    }

    @Test
    public void test_validatePoolMembers_reassign_court_user() {
        final String sourcePoolNumber = "415230701";
        final String sourceCourtLocCode = "415";
        final String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333", "444444444");

        BureauJwtPayload payload = TestUtils.mockCourtUser(sourceCourtLocCode, "COURT_USER");
        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode, targetCourtLocCode, targetStartDate, jurorNumbers);

        JurorPool testJurorPool1 = createTestJurorPool(sourceCourtLocCode, "111111111", sourcePoolNumber);
        CourtLocation courtLocation = testJurorPool1.getCourt();

        JurorPool testJurorPool2 = createTestJurorPool(sourceCourtLocCode, "222222222", sourcePoolNumber);
        JurorPool testJurorPool3 = createTestJurorPool(sourceCourtLocCode, "333333333", sourcePoolNumber);
        JurorPool testJurorPool4 = createTestJurorPool(sourceCourtLocCode, "444444444", sourcePoolNumber);

        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(sourceCourtLocCode);
        doReturn(Arrays.asList(testJurorPool1, testJurorPool2, testJurorPool3, testJurorPool4))
            .when(jurorPoolRepository)
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(anyList(), anyBoolean(),
                anyString(), any(CourtLocation.class));

        JurorManagementResponseDto responseDto = jurorManagementService.validatePoolMembers(payload, requestDto);

        verify(courtLocationRepository, times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(jurorPoolRepository, times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(anyList(),
                eq(true), anyString(),
                any(CourtLocation.class));
        verify(jurorPoolRepository, times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(anyList(),
                eq(false), anyString(),
                any(CourtLocation.class));

        Assertions.assertThat(responseDto)
            .as("Expect response DTO to exist")
            .isNotNull();

        Assertions.assertThat(responseDto.getAvailableForMove().size())
            .as("Expect all 4 supplied juror numbers to be available for reassign")
            .isEqualTo(4);
        Assertions.assertThat(responseDto.getAvailableForMove())
            .containsAll(Arrays.asList("111111111", "222222222", "333333333", "444444444"));

        Assertions.assertThat(responseDto.getUnavailableForMove())
            .as("Expect no supplied juror numbers to be unavailable for reassign")
            .isNull();
    }

    @Test
    public void test_validatePoolMembers_multiplePoolMembers_someAvailable_someUnavailable() {
        final String sourcePoolNumber = "415230701";
        final String sourceCourtLocCode = "415";
        final String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333", "444444444", "555555555");


        final JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode,
            targetCourtLocCode,
            targetStartDate,
            jurorNumbers);

        JurorPool testJurorPool1 = createTestJurorPool(sourceCourtLocCode, "111111111", sourcePoolNumber);
        CourtLocation courtLocation = testJurorPool1.getCourt();

        JurorPool testJurorPool2 = createTestJurorPool(sourceCourtLocCode, "222222222", sourcePoolNumber);
        Juror sourceJuror2 = testJurorPool2.getJuror();
        sourceJuror2.setDateOfBirth(targetStartDate.minusYears(76));

        JurorPool testJurorPool3 = createTestJurorPool(sourceCourtLocCode, "333333333", sourcePoolNumber);
        testJurorPool3.setStatus(createJurorStatus(10, "Transferred"));

        JurorPool testJurorPool4 = createTestJurorPool(sourceCourtLocCode, "444444444", sourcePoolNumber);

        // test pool member 555555555 intentionally missing

        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(sourceCourtLocCode);
        doReturn(Arrays.asList(testJurorPool1, testJurorPool2, testJurorPool3, testJurorPool4))
            .when(jurorPoolRepository)
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(anyList(), anyBoolean(),
                anyString(), any(CourtLocation.class));
        BureauJwtPayload payload = TestUtils.mockCourtUser(sourceCourtLocCode, "COURT_USER");
        JurorManagementResponseDto responseDto = jurorManagementService.validatePoolMembers(payload, requestDto);

        verify(courtLocationRepository, times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(jurorPoolRepository, times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(anyList(),
                eq(true), anyString(),
                any(CourtLocation.class));
        verify(jurorPoolRepository, times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(anyList(),
                eq(false), anyString(),
                any(CourtLocation.class));

        Assertions.assertThat(responseDto)
            .as("Expect response DTO to exist")
            .isNotNull();

        Assertions.assertThat(responseDto.getAvailableForMove().size())
            .as("Expect 2 supplied juror numbers to be available for transfer")
            .isEqualTo(2);
        Assertions.assertThat(responseDto.getAvailableForMove())
            .containsAll(Arrays.asList("111111111", "444444444"));

        List<JurorManagementResponseDto.ValidationFailure> validationFailures =
            responseDto.getUnavailableForMove();
        Assertions.assertThat(validationFailures.size())
            .as("Expect 3 supplied juror numbers to be unavailable for transfer")
            .isEqualTo(3);

        JurorManagementResponseDto.ValidationFailure validationFailure1 = validationFailures.stream()
            .filter(unavailable -> "222222222".equalsIgnoreCase(unavailable.getJurorNumber()))
            .findFirst().orElse(null);
        Assertions.assertThat(validationFailure1)
            .as("Expect juror 222222222 to be unavailable for transfer")
            .isNotNull();
        Assertions.assertThat(JurorManagementConstants.ABOVE_AGE_LIMIT_MESSAGE)
            .isEqualTo(validationFailure1.getFailureReason());
        Assertions.assertThat(validationFailure1.getFirstName()).isEqualTo("Test222222222");
        Assertions.assertThat(validationFailure1.getLastName()).isEqualTo("Person222222222");

        JurorManagementResponseDto.ValidationFailure validationFailure2 = validationFailures.stream()
            .filter(unavailable -> "333333333".equalsIgnoreCase(unavailable.getJurorNumber()))
            .findFirst().orElse(null);
        Assertions.assertThat(validationFailure2)
            .as("Expect juror 333333333 to be unavailable for transfer")
            .isNotNull();
        Assertions.assertThat(validationFailure2.getFailureReason())
            .isEqualTo(String.format(JurorManagementConstants.INVALID_STATUS_MESSAGE, "Transferred"));
        Assertions.assertThat(validationFailure2.getFirstName()).isEqualTo("Test333333333");
        Assertions.assertThat(validationFailure2.getLastName()).isEqualTo("Person333333333");

        JurorManagementResponseDto.ValidationFailure validationFailure3 = validationFailures.stream()
            .filter(unavailable -> "555555555".equalsIgnoreCase(unavailable.getJurorNumber()))
            .findFirst().orElse(null);
        Assertions.assertThat(validationFailure3)
            .as("Expect juror 555555555 to be unavailable for transfer")
            .isNotNull();
        Assertions.assertThat(JurorManagementConstants.NO_ACTIVE_RECORD_MESSAGE)
            .isEqualTo(validationFailure3.getFailureReason());
        Assertions.assertThat(validationFailure3.getFirstName()).isEqualTo("");
        Assertions.assertThat(validationFailure3.getLastName()).isEqualTo("");
    }

    @Test
    public void test_validatePoolMembers_multiplePoolMembers_allUnavailable() {
        final String sourcePoolNumber = "415230701";
        final String sourceCourtLocCode = "415";
        final String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        final JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode,
            targetCourtLocCode,
            targetStartDate,
            jurorNumbers);

        JurorPool testJurorPool1 = createTestJurorPool(sourceCourtLocCode, "111111111", sourcePoolNumber);
        Juror testJuror1 = testJurorPool1.getJuror();
        testJuror1.setDateOfBirth(targetStartDate.minusYears(18).plusDays(1));
        CourtLocation courtLocation = testJurorPool1.getCourt();

        doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(sourceCourtLocCode);
        doReturn(Collections.singletonList(testJurorPool1)).when(jurorPoolRepository)
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(anyList(),
                anyBoolean(), anyString(),
                any(CourtLocation.class));
        BureauJwtPayload payload = TestUtils.mockCourtUser(sourceCourtLocCode, "COURT_USER");
        JurorManagementResponseDto responseDto = jurorManagementService.validatePoolMembers(payload, requestDto);

        verify(courtLocationRepository, times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(jurorPoolRepository, times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(anyList(),
                eq(true), anyString(),
                any(CourtLocation.class));
        verify(jurorPoolRepository, times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(anyList(),
                eq(false), anyString(),
                any(CourtLocation.class));

        Assertions.assertThat(responseDto)
            .as("Expect response DTO to exist")
            .isNotNull();

        Assertions.assertThat(responseDto.getAvailableForMove())
            .as("Expect no supplied juror numbers to be available for transfer")
            .isEmpty();

        List<JurorManagementResponseDto.ValidationFailure> validationFailures =
            responseDto.getUnavailableForMove();
        Assertions.assertThat(validationFailures.size())
            .as("Expect the supplied juror number to be unavailable for transfer")
            .isEqualTo(1);

        JurorManagementResponseDto.ValidationFailure validationFailure = validationFailures.stream()
            .filter(unavailable -> "111111111".equalsIgnoreCase(unavailable.getJurorNumber()))
            .findFirst().orElse(null);
        Assertions.assertThat(validationFailure)
            .as("Expect juror 111111111 to be unavailable for transfer")
            .isNotNull();
        Assertions.assertThat(validationFailure.getFailureReason())
            .isEqualTo(JurorManagementConstants.BELOW_AGE_LIMIT_MESSAGE);
        Assertions.assertThat(validationFailure.getFirstName()).isEqualTo("Test111111111");
        Assertions.assertThat(validationFailure.getLastName()).isEqualTo("Person111111111");
    }

    @Test
    public void test_validatePoolMembers_sourceCourtLocation_notFound() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333", "444444444");

        BureauJwtPayload payload = TestUtils.mockCourtUser(sourceCourtLocCode, "COURT_USER");
        final JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode,
            targetCourtLocCode,
            targetStartDate,
            jurorNumbers);

        JurorPool testJurorPool1 = createTestJurorPool(sourceCourtLocCode, "111111111", sourcePoolNumber);
        JurorPool testJurorPool2 = createTestJurorPool(sourceCourtLocCode, "222222222", sourcePoolNumber);
        JurorPool testJurorPool3 = createTestJurorPool(sourceCourtLocCode, "333333333", sourcePoolNumber);
        JurorPool testJurorPool4 = createTestJurorPool(sourceCourtLocCode, "444444444", sourcePoolNumber);

        doReturn(Optional.empty()).when(courtLocationRepository).findByLocCode(sourceCourtLocCode);
        doReturn(Arrays.asList(testJurorPool1, testJurorPool2, testJurorPool3, testJurorPool4))
            .when(jurorPoolRepository)
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(anyList(),
                anyBoolean(), anyString(),
                any(CourtLocation.class));

        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            jurorManagementService.validatePoolMembers(
                payload,
                requestDto
            ));

        verify(courtLocationRepository, times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(jurorPoolRepository, never())
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(anyList(),
                anyBoolean(), anyString(),
                any(CourtLocation.class));
    }

    private JurorPool createTestJurorPool(String owner, String jurorNumber, String poolNumber) {
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(poolNumber);
        poolRequest.setCourtLocation(createCourtLocation("415", "415"));
        poolRequest.setPoolType(new PoolType("CRO", "Crown Court"));

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);
        juror.setPollNumber("5005");
        juror.setResponded(true);
        juror.setPoliceCheck(PoliceCheck.ELIGIBLE);
        juror.setNoDefPos(1);
        juror.setNotes("Some Example Notes");
        juror.setReasonableAdjustmentCode("H");
        juror.setReasonableAdjustmentMessage("Hearing impairment");
        juror.setWelsh(false);
        juror.setPermanentlyDisqualify(false);
        juror.setSortCode("010203");
        juror.setBankAccountName("Test Bank");
        juror.setBankAccountNumber("01234567");
        juror.setSummonsFile("File 230601");
        juror.setNotifications(1);
        setPersonalDetails(juror);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(owner);

        setJurorStatus(2, "Responded", jurorPool);

        jurorPool.setPoolSequence("0102");
        jurorPool.setNextDate(LocalDate.of(2023, 6, 14));
        jurorPool.setUserEdtq("TEST_USER");
        jurorPool.setNoAttended(2);
        jurorPool.setNoAttendances(2);

        jurorPool.setIsActive(true);
        jurorPool.setWasDeferred(true);
        jurorPool.setPostpone(false);
        jurorPool.setLastUpdate(LocalDateTime.now());

        jurorPool.setTimesSelected(1);
        jurorPool.setLocation("Some Location");
        jurorPool.setFailedToAttendCount(0);
        jurorPool.setUnauthorisedAbsenceCount(0);
        jurorPool.setEditTag('T');
        jurorPool.setOnCall(true);

        jurorPool.setPaidCash(false);

        jurorPool.setIdChecked('I');
        jurorPool.setScanCode("012345678");
        jurorPool.setReminderSent(false);

        juror.setAssociatedPools(Set.of(jurorPool));
        jurorPool.setJuror(juror);
        jurorPool.setPool(poolRequest);

        return jurorPool;
    }

    private void setPersonalDetails(Juror juror) {
        juror.setTitle("Mr");
        juror.setFirstName("Test" + juror.getJurorNumber());
        juror.setLastName("Person" + juror.getJurorNumber());

        juror.setAddressLine1("Address Line 1");
        juror.setAddressLine2("Address Line 2");
        juror.setAddressLine3("Address Line 3");
        juror.setAddressLine4("Some Town");
        juror.setAddressLine5("Some County");
        juror.setPostcode("CH1 2AN");

        juror.setDateOfBirth(LocalDate.of(1990, 6, 1));
        juror.setPhoneNumber("01234567890");
        juror.setAltPhoneNumber("07987654321");
        juror.setWorkPhone("01432987654");

        juror.setEmail("some@email.com");
        juror.setContactPreference(0);
    }

    private void setJurorStatus(int statusCode, String description, JurorPool jurorPool) {
        JurorStatus jurorStatus = new JurorStatus();

        jurorStatus.setStatus(statusCode);
        jurorStatus.setStatusDesc(description);
        jurorStatus.setActive(true);

        jurorPool.setStatus(jurorStatus);
    }

    private PoolRequest createTestPoolRequest(String poolNumber, String owner, CourtLocation courtLocation) {
        PoolRequest poolRequest = new PoolRequest();

        poolRequest.setOwner(owner);
        poolRequest.setPoolNumber(poolNumber);
        poolRequest.setPoolNumber(poolNumber);
        poolRequest.setCourtLocation(courtLocation);
        poolRequest.setReturnDate(LocalDate.of(2023, 6, 5));
        poolRequest.setAttendTime(LocalDateTime.of(2023, 6, 5, 9, 15));
        poolRequest.setNumberRequested(150);
        poolRequest.setPoolType(new PoolType("CRO", "Crown Court"));
        poolRequest.setNewRequest('N');

        return poolRequest;
    }

    private JurorStatus createJurorStatus(int statusCode, String description) {
        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(statusCode);
        jurorStatus.setStatusDesc(description);
        return jurorStatus;
    }

    private CourtLocation createCourtLocation(String locCode, String owner) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(locCode);
        courtLocation.setOwner(owner);
        return courtLocation;
    }

    private JurorManagementRequestDto createValidJurorManagementRequestDto() {
        return new JurorManagementRequestDto("123456789", "415",
            List.of("123456789"), "987654321",
            "416", LocalDate.now()
        );
    }

    private JurorManagementRequestDto createInvalidValidJurorManagementRequestDto() {
        return new JurorManagementRequestDto("123456789", "415",
            List.of("123456789"), "123456789",
            "415", LocalDate.now()
        );
    }

    private List<JurorPool> createJurorPoolList(String owner) {

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("415");

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("123456789");
        poolRequest.setCourtLocation(courtLocation);

        Juror juror = new Juror();
        juror.setJurorNumber("123456789");

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(owner);
        jurorPool.setPool(poolRequest);

        juror.setAssociatedPools(Set.of(jurorPool));
        jurorPool.setJuror(juror);

        List<JurorPool> poolMemberList = new ArrayList<>();
        poolMemberList.add(jurorPool);

        return poolMemberList;
    }

    private BureauJwtPayload buildPayload(String owner) {
        return BureauJwtPayload.builder()
            .userLevel("99")
            .login("SOME_USER")
            .owner(owner)
            .build();
    }

}
