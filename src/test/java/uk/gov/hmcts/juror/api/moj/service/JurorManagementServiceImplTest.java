package uk.gov.hmcts.juror.api.moj.service;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorManagementRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorManagementResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoliceCheck;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.PoolType;
import uk.gov.hmcts.juror.api.moj.exception.JurorRecordException;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.service.letter.CertLetterServiceImpl;
import uk.gov.hmcts.juror.api.moj.service.letter.ConfirmationLetterServiceImpl;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private JurorHistoryRepository jurorHistoryRepository;

    @Mock
    GeneratePoolNumberService generatePoolNumberService;

    @Mock
    CertLetterServiceImpl certLetterService;

    @Mock
    ResponseInspector responseInspector;
    @Mock
    private PoolMemberSequenceService poolMemberSequenceService;
    @Mock
    private ConfirmationLetterServiceImpl confirmationLetterService;

    @InjectMocks
    JurorManagementServiceImpl jurorManagementService;

    @Before
    public void setUpMocks() {
        doNothing().when(certLetterService).enqueueNewLetter(Mockito.any(), Mockito.any());
        doReturn(1).when(poolMemberSequenceService).getPoolMemberSequenceNumber(Mockito.any());
        doReturn(null).when(poolRequestRepository).saveAndFlush(Mockito.any());
        doReturn(null).when(jurorPoolRepository).save(Mockito.any());
        doReturn(null).when(jurorHistoryRepository).save(Mockito.any());

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

        BureauJWTPayload payload = buildPayload("400");
        JurorManagementRequestDto jurorManagementRequestDto = createInvalidValidJurorManagementRequestDto();

        assertThatExceptionOfType(MojException.BadRequest.class)
            .isThrownBy(() -> jurorManagementService.reassignJurors(payload, jurorManagementRequestDto));

        verify(poolRequestRepository, Mockito.times(0))
            .findByPoolNumber(Mockito.anyString());
        verify(courtLocationRepository, Mockito.times(0))
            .findByLocCode(Mockito.anyString());
        verify(jurorStatusRepository, Mockito.times(0))
            .findById(Mockito.anyInt());
        verify(jurorPoolRepository, Mockito.times(0))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(
                Mockito.anyList(), Mockito.anyBoolean(), Mockito.any(),
                Mockito.any(CourtLocation.class), Mockito.anyList()
            );
        verify(confirmationLetterService, Mockito.times(0))
            .enqueueLetter(Mockito.any());

    }

    @Test
    public void test_reassignJuror_validRequest_happy() {

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

        List<JurorPool> poolMemberList = createJurorPoolList();

        when(poolRequestRepository.findByPoolNumber(Mockito.anyString()))
            .thenReturn(Optional.of(poolRequest));
        when(courtLocationRepository.findByLocCode(Mockito.anyString()))
            .thenReturn(Optional.of(courtLocation));
        when(jurorStatusRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(jurorStatus));
        when(jurorPoolRepository.findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(
            Mockito.anyList(), Mockito.anyBoolean(), Mockito.anyString(),
            Mockito.any(CourtLocation.class), Mockito.anyList()
        )).thenReturn(poolMemberList);
        when(jurorPoolRepository
                .findByOwnerAndJurorJurorNumberAndPoolPoolNumber(Mockito.anyString(), Mockito.anyString(),
                    Mockito.anyString()
                ))
            .thenReturn(Optional.empty());
        when(poolMemberSequenceService
            .getPoolMemberSequenceNumber(Mockito.anyString())).thenReturn(1);

        BureauJWTPayload payload = buildPayload("400");
        JurorManagementRequestDto jurorManagementRequestDto = createValidJurorManagementRequestDto();

        int jurorsMoved = jurorManagementService.reassignJurors(payload, jurorManagementRequestDto);

        Assertions.assertThat(jurorsMoved).isEqualTo(1);

        verify(poolRequestRepository, Mockito.times(2))
            .findByPoolNumber(Mockito.anyString());
        verify(courtLocationRepository, Mockito.times(2))
            .findByLocCode(Mockito.anyString());
        verify(jurorStatusRepository, Mockito.times(1))
            .findById(Mockito.anyInt());
        verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(
                Mockito.anyList(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.any(CourtLocation.class), Mockito.anyList()
            );
        verify(jurorPoolRepository, Mockito.times(1))
            .findByOwnerAndJurorJurorNumberAndPoolPoolNumber(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString()
            );
        verify(poolMemberSequenceService, Mockito.times(1))
            .getPoolMemberSequenceNumber(Mockito.anyString());
        verify(jurorHistoryRepository, Mockito.times(1))
            .save(Mockito.any(JurorHistory.class));
        verify(confirmationLetterService, Mockito.times(1))
            .enqueueLetter(Mockito.any());

    }

    @Test
    public void test_reassignJuror_noSourcePoolRequest() {

        when(poolRequestRepository.findByPoolNumber(Mockito.anyString()))
            .thenReturn(Optional.empty());

        BureauJWTPayload payload = buildPayload("400");
        JurorManagementRequestDto jurorManagementRequestDto = createValidJurorManagementRequestDto();

        assertThatExceptionOfType(MojException.NotFound.class)
            .isThrownBy(() -> jurorManagementService.reassignJurors(payload, jurorManagementRequestDto));

        verify(poolRequestRepository, Mockito.times(1))
            .findByPoolNumber(Mockito.anyString());
        verify(courtLocationRepository, Mockito.times(0))
            .findByLocCode(Mockito.anyString());
        verify(jurorStatusRepository, Mockito.times(0))
            .findById(Mockito.anyInt());
        verify(jurorPoolRepository, Mockito.times(0))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(
                Mockito.anyList(), Mockito.anyBoolean(), Mockito.any(),
                Mockito.any(CourtLocation.class), Mockito.anyList()
            );
        verify(confirmationLetterService, Mockito.times(0))
            .enqueueLetter(Mockito.any());
    }

    @Test
    public void test_reassignJuror_noTargetPoolRequest() {

        PoolRequest poolRequest = new PoolRequest();

        when(poolRequestRepository.findByPoolNumber(Mockito.anyString()))
            .thenReturn(Optional.of(poolRequest));
        when(courtLocationRepository.findByLocCode(Mockito.anyString()))
            .thenReturn(Optional.empty());

        BureauJWTPayload payload = buildPayload("400");
        JurorManagementRequestDto jurorManagementRequestDto = createValidJurorManagementRequestDto();

        assertThatExceptionOfType(MojException.NotFound.class)
            .isThrownBy(() -> jurorManagementService.reassignJurors(payload, jurorManagementRequestDto));

        verify(poolRequestRepository, Mockito.times(2))
            .findByPoolNumber(Mockito.anyString());
        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(Mockito.anyString());
        verify(jurorStatusRepository, Mockito.times(0))
            .findById(Mockito.anyInt());
        verify(jurorPoolRepository, Mockito.times(0))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(
                Mockito.anyList(), Mockito.anyBoolean(), Mockito.any(),
                Mockito.any(CourtLocation.class), Mockito.anyList()
            );
        verify(confirmationLetterService, Mockito.times(0))
            .enqueueLetter(Mockito.any());
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

        List<JurorPool> poolMemberList = new ArrayList<>();

        Mockito.when(poolRequestRepository.findByPoolNumber(Mockito.anyString()))
            .thenReturn(Optional.of(new PoolRequest()));
        Mockito.when(courtLocationRepository.findByLocCode(Mockito.anyString()))
            .thenReturn(Optional.of(courtLocation));
        when(jurorStatusRepository.findById(Mockito.anyInt())).thenReturn(Optional.of(jurorStatus));
        when(jurorPoolRepository
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(
                Mockito.anyList(), Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.any(CourtLocation.class), Mockito.anyList()
            )).thenReturn(poolMemberList);

        BureauJWTPayload payload = buildPayload("400");
        JurorManagementRequestDto jurorManagementRequestDto = createValidJurorManagementRequestDto();

        int jurorsMoved = jurorManagementService.reassignJurors(payload, jurorManagementRequestDto);

        Assertions.assertThat(jurorsMoved).isEqualTo(0);

        verify(poolRequestRepository, Mockito.times(2))
            .findByPoolNumber(Mockito.anyString());
        verify(courtLocationRepository, Mockito.times(2))
            .findByLocCode(Mockito.anyString());
        verify(jurorStatusRepository, Mockito.times(0))
            .findById(Mockito.anyInt());
        verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(
                Mockito.anyList(), Mockito.anyBoolean(), Mockito.any(),
                Mockito.any(CourtLocation.class), Mockito.anyList()
            );
        verify(confirmationLetterService, Mockito.times(0))
            .enqueueLetter(Mockito.any());
    }

    @Test
    public void test_transferPoolMembers_happyPath_onePoolMember_firstTransfer() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        BureauJWTPayload payload = TestUtils.createJwt(sourceCourtLocCode, "COURT_USER");
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
            .findByJurorNumberAndIsActiveAndCourt(Mockito.any(), Mockito.anyBoolean(), Mockito.any());
        doReturn(Collections.singletonList(testJurorPool)).when(jurorPoolRepository)
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(Mockito.anyList(),
                Mockito.anyBoolean(),
                Mockito.anyString(),
                Mockito.any(CourtLocation.class),
                Mockito.anyList());

        doReturn(createTestPoolRequest("435230701", targetCourtLocCode,
            targetCourtLocation
        )).when(poolRequestRepository).save(Mockito.any());

        int successfulCount = jurorManagementService.transferPoolMembers(payload, requestDto);

        Assertions.assertThat(successfulCount)
            .as("Expect the single pool member to be transferred successfully")
            .isEqualTo(1);

        verify(poolRequestRepository, Mockito.times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(Mockito.anyList(),
                Mockito.anyBoolean(),
                Mockito.anyString(),
                Mockito.any(CourtLocation.class),
                Mockito.anyList()
            );
        verify(generatePoolNumberService, Mockito.times(1))
            .generatePoolNumber(Mockito.any(), Mockito.any());
        verify(poolMemberSequenceService, Mockito.times(jurorNumbers.size()))
            .getPoolMemberSequenceNumber(Mockito.any());

        verify(poolRequestRepository, Mockito.times(1))
            .save(Mockito.any());
        // one save invocation for each newly created juror, and another save invocation to update the source juror
        // record
        verify(jurorPoolRepository, Mockito.times(jurorNumbers.size() * 2))
            .save(Mockito.any());
        verify(poolRequestRepository, Mockito.times(1))
            .saveAndFlush(Mockito.any());
        verify(jurorHistoryRepository, Mockito.times(jurorNumbers.size()))
            .save(Mockito.any());
        verify(certLetterService, Mockito.times(jurorNumbers.size()))
            .enqueueNewLetter(Mockito.any(), Mockito.any());
    }

    @Test
    public void test_transferPoolMembers_happyPath_multiplePoolMembers() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333");

        BureauJWTPayload payload = TestUtils.createJwt(sourceCourtLocCode, "COURT_USER");
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
            .findByJurorNumberAndIsActiveAndCourt(Mockito.any(), Mockito.anyBoolean(), Mockito.any());
        doReturn(Arrays.asList(testJurorPool1, testJurorPool2, testJurorPool3)).when(jurorPoolRepository)
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(Mockito.anyList(),
                Mockito.anyBoolean(),
                Mockito.anyString(),
                Mockito.any(CourtLocation.class),
                Mockito.anyList());

        doReturn(createTestPoolRequest("435230701", targetCourtLocCode,
            targetCourtLocation
        )).when(poolRequestRepository).save(Mockito.any());

        int successfulCount = jurorManagementService.transferPoolMembers(payload, requestDto);

        Assertions.assertThat(successfulCount)
            .as("Expect all 3 pool members to be transferred successfully")
            .isEqualTo(3);

        verify(poolRequestRepository, Mockito.times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(Mockito.anyList(),
                Mockito.anyBoolean(),
                Mockito.anyString(),
                Mockito.any(CourtLocation.class),
                Mockito.anyList()
            );
        verify(generatePoolNumberService, Mockito.times(1))
            .generatePoolNumber(Mockito.any(), Mockito.any());
        verify(poolMemberSequenceService, Mockito.times(jurorNumbers.size()))
            .getPoolMemberSequenceNumber(Mockito.any());

        verify(poolRequestRepository, Mockito.times(1))
            .save(Mockito.any());
        // one save invocation for each newly created juror, and another save invocation to update the source juror
        // record
        verify(jurorPoolRepository, Mockito.times(jurorNumbers.size() * 2))
            .save(Mockito.any());
        verify(poolRequestRepository, Mockito.times(1))
            .saveAndFlush(Mockito.any());
        verify(jurorHistoryRepository, Mockito.times(jurorNumbers.size()))
            .save(Mockito.any());
        verify(certLetterService, Mockito.times(jurorNumbers.size()))
            .enqueueNewLetter(Mockito.any(), Mockito.any());
    }

    @Test
    public void test_transferPoolMembers_happyPath_onePoolMember_transferBack() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        final BureauJWTPayload payload = TestUtils.createJwt(sourceCourtLocCode, "COURT_USER");
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
            .findByJurorNumberAndIsActiveAndCourt(Mockito.any(), Mockito.anyBoolean(), Mockito.any());
        doReturn(Collections.singletonList(sourceJurorPool)).when(jurorPoolRepository)
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(Mockito.anyList(),
                Mockito.anyBoolean(),
                Mockito.anyString(),
                Mockito.any(CourtLocation.class),
                Mockito.anyList());

        Mockito.doReturn(createTestPoolRequest("435230701", targetCourtLocCode, targetCourtLocation))
            .when(poolRequestRepository).save(Mockito.any());

        int successfulCount = jurorManagementService.transferPoolMembers(payload, requestDto);

        Assertions.assertThat(successfulCount)
            .as("Expect the single pool member to be transferred successfully")
            .isEqualTo(1);

        verify(poolRequestRepository, Mockito.times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(Mockito.anyList(),
                Mockito.anyBoolean(),
                Mockito.anyString(),
                Mockito.any(CourtLocation.class),
                Mockito.anyList());
        Mockito.verify(generatePoolNumberService, Mockito.times(1))
            .generatePoolNumber(Mockito.any(), Mockito.any());
        verify(poolMemberSequenceService, Mockito.times(jurorNumbers.size()))
            .getPoolMemberSequenceNumber(Mockito.any());

        verify(poolRequestRepository, Mockito.times(1))
            .save(Mockito.any());
        // one save invocation for each newly created juror, and another save invocation to update the source juror
        // record
        // plus one additional save invocation for logically deleting the previously transferred record
        verify(jurorPoolRepository, Mockito.times((jurorNumbers.size() * 2) + 1))
            .save(Mockito.any());
        verify(poolRequestRepository, Mockito.times(1))
            .saveAndFlush(Mockito.any());
        verify(jurorHistoryRepository, Mockito.times(jurorNumbers.size()))
            .save(Mockito.any());
        verify(certLetterService, Mockito.times(jurorNumbers.size()))
            .enqueueNewLetter(Mockito.any(), Mockito.any());
    }

    @Test
    public void test_transferPoolMembers_multiplePoolMembers_someFailures() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333", "444444444", "555555555");

        final BureauJWTPayload payload = TestUtils.createJwt(sourceCourtLocCode, "COURT_USER");
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
            .findByJurorNumberAndIsActiveAndCourt(Mockito.any(), Mockito.anyBoolean(), Mockito.any());
        doReturn(Arrays.asList(testJurorPool1, testJurorPool2, testJurorPool4)).when(jurorPoolRepository)
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(Mockito.anyList(),
                Mockito.anyBoolean(),
                Mockito.anyString(),
                Mockito.any(CourtLocation.class),
                Mockito.anyList());

        doReturn(createTestPoolRequest("435230701", targetCourtLocCode,
            targetCourtLocation
        )).when(poolRequestRepository).save(Mockito.any());

        int successfulCount = jurorManagementService.transferPoolMembers(payload, requestDto);

        Assertions.assertThat(successfulCount)
            .as("Expect 2 pool members to be transferred successfully (3 to be unsuccessful)")
            .isEqualTo(2);

        verify(poolRequestRepository, Mockito.times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(Mockito.anyList(),
                Mockito.anyBoolean(),
                Mockito.anyString(),
                Mockito.any(CourtLocation.class),
                Mockito.anyList()
            );
        verify(generatePoolNumberService, Mockito.times(1))
            .generatePoolNumber(Mockito.any(), Mockito.any());
        verify(poolMemberSequenceService, Mockito.times(2))
            .getPoolMemberSequenceNumber(Mockito.any());

        verify(poolRequestRepository, Mockito.times(1))
            .save(Mockito.any());
        // one save invocation for each newly created juror, and another save invocation to update the source juror
        // record
        verify(jurorPoolRepository, Mockito.times(4))
            .save(Mockito.any());
        verify(poolRequestRepository, Mockito.times(1))
            .saveAndFlush(Mockito.any());
        verify(jurorHistoryRepository, Mockito.times(2))
            .save(Mockito.any());
        verify(certLetterService, Mockito.times(2))
            .enqueueNewLetter(Mockito.any(), Mockito.any());
    }

    @Test
    public void test_transferPoolMembers_unhappyPath_invalidAccess_bureauUser() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        BureauJWTPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
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

        verify(poolRequestRepository, Mockito.never())
            .findByPoolNumber(sourcePoolNumber);
        verify(courtLocationRepository, Mockito.never())
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, Mockito.never())
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, Mockito.never())
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(Mockito.anyList(),
                Mockito.anyBoolean(),
                Mockito.anyString(),
                Mockito.any(CourtLocation.class),
                Mockito.anyList()
            );
        verify(generatePoolNumberService, Mockito.never())
            .generatePoolNumber(Mockito.any(), Mockito.any());
        verify(poolMemberSequenceService, Mockito.never())
            .getPoolMemberSequenceNumber(Mockito.any());

        verify(poolRequestRepository, Mockito.never())
            .save(Mockito.any());
        verify(jurorPoolRepository, Mockito.never())
            .save(Mockito.any());
        verify(poolRequestRepository, Mockito.never())
            .saveAndFlush(Mockito.any());
        verify(jurorHistoryRepository, Mockito.never())
            .save(Mockito.any());
        verify(certLetterService, Mockito.never())
            .enqueueNewLetter(Mockito.any(), Mockito.any());
    }

    @Test
    public void test_transferPoolMembers_unhappyPath_invalidJurorNumber() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        // juror number only 8 chars
        List<String> jurorNumbers = Collections.singletonList("11111111");

        BureauJWTPayload payload = TestUtils.createJwt(sourceCourtLocCode, "COURT_USER");
        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode, targetCourtLocCode, targetStartDate, jurorNumbers);

        assertThatExceptionOfType(JurorRecordException.InvalidJurorNumber.class)
            .isThrownBy(() -> jurorManagementService.transferPoolMembers(
                payload,
                requestDto
            ));

        verify(poolRequestRepository, Mockito.never())
            .findByPoolNumber(sourcePoolNumber);
        verify(courtLocationRepository, Mockito.never())
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, Mockito.never())
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, Mockito.never())
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(Mockito.anyList(),
                Mockito.anyBoolean(),
                Mockito.anyString(),
                Mockito.any(CourtLocation.class),
                Mockito.anyList()
            );
        verify(generatePoolNumberService, Mockito.never())
            .generatePoolNumber(Mockito.any(), Mockito.any());
        verify(poolMemberSequenceService, Mockito.never())
            .getPoolMemberSequenceNumber(Mockito.any());

        verify(poolRequestRepository, Mockito.never())
            .save(Mockito.any());
        verify(jurorPoolRepository, Mockito.never())
            .save(Mockito.any());
        verify(poolRequestRepository, Mockito.never())
            .saveAndFlush(Mockito.any());
        verify(jurorHistoryRepository, Mockito.never())
            .save(Mockito.any());
        verify(certLetterService, Mockito.never())
            .enqueueNewLetter(Mockito.any(), Mockito.any());
    }

    @Test
    public void test_transferPoolMembers_unhappyPath_sourcePoolRequestNotFound() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        BureauJWTPayload payload = TestUtils.createJwt(sourceCourtLocCode, "COURT_USER");
        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode, targetCourtLocCode, targetStartDate, jurorNumbers);

        doReturn(Optional.empty())
            .when(poolRequestRepository).findById(sourcePoolNumber);

        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            jurorManagementService.transferPoolMembers(
                payload,
                requestDto
            ));

        verify(poolRequestRepository, Mockito.times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, Mockito.never())
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, Mockito.never())
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, Mockito.never())
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(Mockito.anyList(),
                Mockito.anyBoolean(),
                Mockito.anyString(),
                Mockito.any(CourtLocation.class),
                Mockito.anyList()
            );
        verify(generatePoolNumberService, Mockito.never())
            .generatePoolNumber(Mockito.any(), Mockito.any());
        verify(poolMemberSequenceService, Mockito.never())
            .getPoolMemberSequenceNumber(Mockito.any());

        verify(poolRequestRepository, Mockito.never())
            .save(Mockito.any());
        verify(jurorPoolRepository, Mockito.never())
            .save(Mockito.any());
        verify(poolRequestRepository, Mockito.never())
            .saveAndFlush(Mockito.any());
        verify(jurorHistoryRepository, Mockito.never())
            .save(Mockito.any());
        verify(certLetterService, Mockito.never())
            .enqueueNewLetter(Mockito.any(), Mockito.any());
    }

    @Test
    public void test_transferPoolMembers_unhappyPath_invalidAccessToSourcePoolRequest() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        BureauJWTPayload payload = TestUtils.createJwt("437", "COURT_USER");
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

        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            jurorManagementService.transferPoolMembers(
                payload,
                requestDto
            ));

        verify(poolRequestRepository, Mockito.times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, Mockito.never())
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, Mockito.never())
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, Mockito.never())
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(Mockito.anyList(),
                Mockito.anyBoolean(),
                Mockito.anyString(),
                Mockito.any(CourtLocation.class),
                Mockito.anyList()
            );
        verify(generatePoolNumberService, Mockito.never())
            .generatePoolNumber(Mockito.any(), Mockito.any());
        verify(poolMemberSequenceService, Mockito.never())
            .getPoolMemberSequenceNumber(Mockito.any());

        verify(poolRequestRepository, Mockito.never())
            .save(Mockito.any());
        verify(jurorPoolRepository, Mockito.never())
            .save(Mockito.any());
        verify(poolRequestRepository, Mockito.never())
            .saveAndFlush(Mockito.any());
        verify(jurorHistoryRepository, Mockito.never())
            .save(Mockito.any());
        verify(certLetterService, Mockito.never())
            .enqueueNewLetter(Mockito.any(), Mockito.any());
    }

    @Test
    public void test_transferPoolMembers_unhappyPath_invalidCourtLocation() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        BureauJWTPayload payload = TestUtils.createJwt("415", "COURT_USER");
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
        doReturn(Optional.empty()).when(courtLocationRepository).findByLocCode(Mockito.any());

        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            jurorManagementService.transferPoolMembers(
                payload,
                requestDto
            ));

        verify(poolRequestRepository, Mockito.times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, Mockito.never())
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, Mockito.never())
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(Mockito.anyList(),
                Mockito.anyBoolean(),
                Mockito.anyString(),
                Mockito.any(CourtLocation.class),
                Mockito.anyList()
            );
        verify(generatePoolNumberService, Mockito.never())
            .generatePoolNumber(Mockito.any(), Mockito.any());
        verify(poolMemberSequenceService, Mockito.never())
            .getPoolMemberSequenceNumber(Mockito.any());

        verify(poolRequestRepository, Mockito.never())
            .save(Mockito.any());
        verify(jurorPoolRepository, Mockito.never())
            .save(Mockito.any());
        verify(poolRequestRepository, Mockito.never())
            .saveAndFlush(Mockito.any());
        verify(jurorHistoryRepository, Mockito.never())
            .save(Mockito.any());
        verify(certLetterService, Mockito.never())
            .enqueueNewLetter(Mockito.any(), Mockito.any());
    }

    @Test
    public void test_transferPoolMembers_unhappyPath_invalidAccessToCourtLocation() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        final BureauJWTPayload payload = TestUtils.createJwt("415", "COURT_USER");
        final JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode, targetCourtLocCode, targetStartDate, jurorNumbers);

        JurorPool testJurorPool = createTestJurorPool(sourceCourtLocCode, "111111111", sourcePoolNumber);
        CourtLocation sourceCourtLocation = testJurorPool.getCourt();
        sourceCourtLocation.setOwner("457");
        CourtLocation targetCourtLocation = createCourtLocation(targetCourtLocCode, targetCourtLocCode);

        Mockito.doReturn(Optional.of(createTestPoolRequest(sourcePoolNumber, sourceCourtLocCode, sourceCourtLocation)))
            .when(poolRequestRepository).findById(sourcePoolNumber);
        Mockito.doReturn(Optional.of(sourceCourtLocation)).when(courtLocationRepository)
            .findByLocCode(sourceCourtLocCode);
        Mockito.doReturn(Optional.of(targetCourtLocation)).when(courtLocationRepository)
            .findByLocCode(targetCourtLocCode);

        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            jurorManagementService.transferPoolMembers(
                payload,
                requestDto
            ));

        verify(poolRequestRepository, Mockito.times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(sourceCourtLocCode);
        Mockito.verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, Mockito.never())
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(Mockito.anyList(),
                Mockito.anyBoolean(), Mockito.anyString(), Mockito.any(CourtLocation.class), Mockito.anyList());
        Mockito.verify(generatePoolNumberService, Mockito.never())
            .generatePoolNumber(Mockito.any(), Mockito.any());
        verify(poolMemberSequenceService, Mockito.never())
            .getPoolMemberSequenceNumber(Mockito.any());

        verify(poolRequestRepository, Mockito.never())
            .save(Mockito.any());
        verify(jurorPoolRepository, Mockito.never())
            .save(Mockito.any());
        verify(poolRequestRepository, Mockito.never())
            .saveAndFlush(Mockito.any());
        verify(jurorHistoryRepository, Mockito.never())
            .save(Mockito.any());
        verify(certLetterService, Mockito.never())
            .enqueueNewLetter(Mockito.any(), Mockito.any());
    }

    @Test
    public void test_transferPoolMembers_unhappyPath_receivingCourtLocationNotFound() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        BureauJWTPayload payload = TestUtils.createJwt("415", "COURT_USER");
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

        verify(poolRequestRepository, Mockito.times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, Mockito.never())
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(Mockito.anyList(),
                Mockito.anyBoolean(),
                Mockito.anyString(),
                Mockito.any(CourtLocation.class),
                Mockito.anyList()
            );
        verify(generatePoolNumberService, Mockito.never())
            .generatePoolNumber(Mockito.any(), Mockito.any());
        verify(poolMemberSequenceService, Mockito.never())
            .getPoolMemberSequenceNumber(Mockito.any());

        verify(poolRequestRepository, Mockito.never())
            .save(Mockito.any());
        verify(jurorPoolRepository, Mockito.never())
            .save(Mockito.any());
        verify(poolRequestRepository, Mockito.never())
            .saveAndFlush(Mockito.any());
        verify(jurorHistoryRepository, Mockito.never())
            .save(Mockito.any());
        verify(certLetterService, Mockito.never())
            .enqueueNewLetter(Mockito.any(), Mockito.any());
    }

    @Test
    public void test_transferPoolMembers_unhappyPath_transferBack_invalidStatus() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        final BureauJWTPayload payload = TestUtils.createJwt(sourceCourtLocCode, "COURT_USER");
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
            .findByJurorNumberAndIsActiveAndCourt(Mockito.any(), Mockito.anyBoolean(), Mockito.any());
        doReturn(Collections.singletonList(sourceJurorPool)).when(jurorPoolRepository)
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(Mockito.anyList(),
                Mockito.anyBoolean(),
                Mockito.anyString(),
                Mockito.any(CourtLocation.class),
                Mockito.anyList());

        doReturn(createTestPoolRequest("435230701", targetCourtLocCode,
            targetCourtLocation
        )).when(poolRequestRepository).save(Mockito.any());

        int successfulCount = jurorManagementService.transferPoolMembers(payload, requestDto);

        Assertions.assertThat(successfulCount)
            .as("Expect the single pool member not to be transferred successfully")
            .isEqualTo(0);

        verify(poolRequestRepository, Mockito.times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(Mockito.anyList(),
                Mockito.anyBoolean(),
                Mockito.anyString(),
                Mockito.any(CourtLocation.class),
                Mockito.anyList()
            );
        verify(generatePoolNumberService, Mockito.times(1))
            .generatePoolNumber(Mockito.any(), Mockito.any());
        verify(poolMemberSequenceService, Mockito.never())
            .getPoolMemberSequenceNumber(Mockito.any());

        verify(poolRequestRepository, Mockito.times(1))
            .save(Mockito.any());
        verify(jurorPoolRepository, Mockito.never())
            .save(Mockito.any());
        verify(poolRequestRepository, Mockito.times(1))
            .saveAndFlush(Mockito.any());
        verify(jurorHistoryRepository, Mockito.never())
            .save(Mockito.any());
        verify(certLetterService, Mockito.never())
            .enqueueNewLetter(Mockito.any(), Mockito.any());
    }

    @Test
    public void test_transferPoolMembers_unhappyPath_onePoolMember_invalidAge_tooOld() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        final BureauJWTPayload payload = TestUtils.createJwt(sourceCourtLocCode, "COURT_USER");
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
            .findByJurorNumberAndIsActiveAndCourt(Mockito.any(), Mockito.anyBoolean(), Mockito.any());
        doReturn(Collections.singletonList(sourceJurorPool)).when(jurorPoolRepository)
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(Mockito.anyList(),
                Mockito.anyBoolean(),
                Mockito.anyString(),
                Mockito.any(CourtLocation.class),
                Mockito.anyList());

        doReturn(createTestPoolRequest("435230701", targetCourtLocCode,
            targetCourtLocation
        )).when(poolRequestRepository).save(Mockito.any());

        int successfulCount = jurorManagementService.transferPoolMembers(payload, requestDto);

        Assertions.assertThat(successfulCount)
            .as("Expect the single pool member not to be transferred successfully")
            .isEqualTo(0);

        verify(poolRequestRepository, Mockito.times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(Mockito.anyList(),
                Mockito.anyBoolean(),
                Mockito.anyString(),
                Mockito.any(CourtLocation.class),
                Mockito.anyList()
            );
        verify(generatePoolNumberService, Mockito.times(1))
            .generatePoolNumber(Mockito.any(), Mockito.any());
        verify(poolMemberSequenceService, Mockito.never())
            .getPoolMemberSequenceNumber(Mockito.any());

        verify(poolRequestRepository, Mockito.times(1))
            .save(Mockito.any());
        verify(jurorPoolRepository, Mockito.never())
            .save(Mockito.any());
        verify(poolRequestRepository, Mockito.times(1))
            .saveAndFlush(Mockito.any());
        verify(jurorHistoryRepository, Mockito.never())
            .save(Mockito.any());
        verify(certLetterService, Mockito.never())
            .enqueueNewLetter(Mockito.any(), Mockito.any());
    }

    @Test
    public void test_transferPoolMembers_unhappyPath_onePoolMember_invalidAge_tooYoung() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        final BureauJWTPayload payload = TestUtils.createJwt(sourceCourtLocCode, "COURT_USER");
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
            .findByJurorNumberAndIsActiveAndCourt(Mockito.any(), Mockito.anyBoolean(), Mockito.any());
        doReturn(Collections.singletonList(sourceJurorPool)).when(jurorPoolRepository)
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(Mockito.anyList(),
                Mockito.anyBoolean(),
                Mockito.anyString(),
                Mockito.any(CourtLocation.class),
                Mockito.anyList());

        doReturn(createTestPoolRequest("435230701", targetCourtLocCode,
            targetCourtLocation
        )).when(poolRequestRepository).save(Mockito.any());

        int successfulCount = jurorManagementService.transferPoolMembers(payload, requestDto);

        Assertions.assertThat(successfulCount)
            .as("Expect the single pool member not to be transferred successfully")
            .isEqualTo(0);

        verify(poolRequestRepository, Mockito.times(1))
            .findById(sourcePoolNumber);
        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(targetCourtLocCode);
        verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(Mockito.anyList(),
                Mockito.anyBoolean(),
                Mockito.anyString(),
                Mockito.any(CourtLocation.class),
                Mockito.anyList()
            );
        verify(generatePoolNumberService, Mockito.times(1))
            .generatePoolNumber(Mockito.any(), Mockito.any());
        verify(poolMemberSequenceService, Mockito.never())
            .getPoolMemberSequenceNumber(Mockito.any());

        verify(poolRequestRepository, Mockito.times(1))
            .save(Mockito.any());
        verify(jurorPoolRepository, Mockito.never())
            .save(Mockito.any());
        verify(poolRequestRepository, Mockito.times(1))
            .saveAndFlush(Mockito.any());
        verify(jurorHistoryRepository, Mockito.never())
            .save(Mockito.any());
        verify(certLetterService, Mockito.never())
            .enqueueNewLetter(Mockito.any(), Mockito.any());
    }

    @Test
    public void test_transferPoolMembers_unhappyPath_sameCourtOwner() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "767";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        BureauJWTPayload payload = TestUtils.createJwt(sourceCourtLocCode, "COURT_USER");
        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocCode, targetCourtLocCode, targetStartDate, jurorNumbers);

        JurorPool testJurorPool = createTestJurorPool(sourceCourtLocCode, "111111111", sourcePoolNumber);
        CourtLocation courtLocation = testJurorPool.getCourt();
        CourtLocation targetCourtLocation = createCourtLocation(targetCourtLocCode, sourceCourtLocCode);

        Mockito.doReturn(Optional.of(createTestPoolRequest(sourcePoolNumber, sourceCourtLocCode, courtLocation)))
            .when(poolRequestRepository).findById(sourcePoolNumber);
        Mockito.doReturn(Optional.of(courtLocation)).when(courtLocationRepository).findByLocCode(sourceCourtLocCode);
        Mockito.doReturn(Optional.of(targetCourtLocation)).when(courtLocationRepository)
            .findByLocCode(targetCourtLocCode);

        Assertions.assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
            jurorManagementService.transferPoolMembers(payload, requestDto));

        Mockito.verify(poolRequestRepository, Mockito.times(1))
            .findById(sourcePoolNumber);
        Mockito.verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(sourceCourtLocCode);
        Mockito.verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(targetCourtLocCode);
        Mockito.verify(jurorPoolRepository, Mockito.never())
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(Mockito.anyList(),
                Mockito.anyBoolean(), Mockito.anyString(), Mockito.any(CourtLocation.class), Mockito.anyList());
        Mockito.verify(generatePoolNumberService, Mockito.never())
            .generatePoolNumber(Mockito.any(), Mockito.any());
        Mockito.verify(poolMemberSequenceService, Mockito.never())
            .getPoolMemberSequenceNumber(Mockito.any());

        Mockito.verify(poolRequestRepository, Mockito.never())
            .save(Mockito.any());
        Mockito.verify(jurorPoolRepository, Mockito.never())
            .save(Mockito.any());
        Mockito.verify(poolRequestRepository, Mockito.never())
            .saveAndFlush(Mockito.any());
        Mockito.verify(jurorHistoryRepository, Mockito.never())
            .save(Mockito.any());
        Mockito.verify(certLetterService, Mockito.never())
            .enqueueNewLetter(Mockito.any(), Mockito.any());
    }

    @Test
    public void test_validatePoolMembers_multiplePoolMembers_allAvailable() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333", "444444444");

        final BureauJWTPayload payload = TestUtils.createJwt(sourceCourtLocCode, "COURT_USER");
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
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(Mockito.anyList(), Mockito.anyBoolean(),
                Mockito.anyString(), Mockito.any(CourtLocation.class));

        JurorManagementResponseDto responseDto = jurorManagementService.validatePoolMembers(payload, requestDto);

        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(Mockito.anyList(),
                Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.any(CourtLocation.class));

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

        BureauJWTPayload payload = TestUtils.createJwt("400", "BUREAU_USER");
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
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(Mockito.anyList(), Mockito.anyBoolean(),
                Mockito.anyString(), Mockito.any(CourtLocation.class));

        JurorManagementResponseDto responseDto = jurorManagementService.validatePoolMembers(payload, requestDto);

        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(Mockito.anyList(),
                Mockito.anyBoolean(), Mockito.anyString(), Mockito.any(CourtLocation.class));

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

        BureauJWTPayload payload = TestUtils.createJwt(sourceCourtLocCode, "COURT_USER");
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
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(Mockito.anyList(), Mockito.anyBoolean(),
                Mockito.anyString(), Mockito.any(CourtLocation.class));

        JurorManagementResponseDto responseDto = jurorManagementService.validatePoolMembers(payload, requestDto);

        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(Mockito.anyList(),
                Mockito.anyBoolean(), Mockito.anyString(), Mockito.any(CourtLocation.class));

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

        BureauJWTPayload payload = TestUtils.createJwt(sourceCourtLocCode, "COURT_USER");
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
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(Mockito.anyList(), Mockito.anyBoolean(),
                Mockito.anyString(), Mockito.any(CourtLocation.class));

        JurorManagementResponseDto responseDto = jurorManagementService.validatePoolMembers(payload, requestDto);

        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(Mockito.anyList(),
                Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.any(CourtLocation.class));

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
            .filter(unavailable -> unavailable.getJurorNumber().equalsIgnoreCase("222222222"))
            .findFirst().orElse(null);
        Assertions.assertThat(validationFailure1)
            .as("Expect juror 222222222 to be unavailable for transfer")
            .isNotNull();
        Assertions.assertThat(validationFailure1.getFailureReason())
            .isEqualTo(JurorManagementConstants.ABOVE_AGE_LIMIT_MESSAGE);

        JurorManagementResponseDto.ValidationFailure validationFailure2 = validationFailures.stream()
            .filter(unavailable -> unavailable.getJurorNumber().equalsIgnoreCase("333333333"))
            .findFirst().orElse(null);
        Assertions.assertThat(validationFailure2)
            .as("Expect juror 333333333 to be unavailable for transfer")
            .isNotNull();
        Assertions.assertThat(validationFailure2.getFailureReason())
            .isEqualTo(String.format(JurorManagementConstants.INVALID_STATUS_MESSAGE, "Transferred"));

        JurorManagementResponseDto.ValidationFailure validationFailure3 = validationFailures.stream()
            .filter(unavailable -> unavailable.getJurorNumber().equalsIgnoreCase("555555555"))
            .findFirst().orElse(null);
        Assertions.assertThat(validationFailure3)
            .as("Expect juror 555555555 to be unavailable for transfer")
            .isNotNull();
        Assertions.assertThat(validationFailure3.getFailureReason())
            .isEqualTo(JurorManagementConstants.NO_ACTIVE_RECORD_MESSAGE);
    }

    @Test
    public void test_validatePoolMembers_multiplePoolMembers_allUnavailable() {
        final String sourcePoolNumber = "415230701";
        final String sourceCourtLocCode = "415";
        final String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        BureauJWTPayload payload = TestUtils.createJwt(sourceCourtLocCode, "COURT_USER");
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
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(Mockito.anyList(),
                Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.any(CourtLocation.class));

        JurorManagementResponseDto responseDto = jurorManagementService.validatePoolMembers(payload, requestDto);

        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(Mockito.anyList(),
                Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.any(CourtLocation.class));

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
            .filter(unavailable -> unavailable.getJurorNumber().equalsIgnoreCase("111111111"))
            .findFirst().orElse(null);
        Assertions.assertThat(validationFailure)
            .as("Expect juror 111111111 to be unavailable for transfer")
            .isNotNull();
        Assertions.assertThat(validationFailure.getFailureReason())
            .isEqualTo(JurorManagementConstants.BELOW_AGE_LIMIT_MESSAGE);
    }

    @Test
    public void test_validatePoolMembers_sourceCourtLocation_notFound() {
        String sourcePoolNumber = "415230701";
        String sourceCourtLocCode = "415";
        String targetCourtLocCode = "435";
        LocalDate targetStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333", "444444444");

        BureauJWTPayload payload = TestUtils.createJwt(sourceCourtLocCode, "COURT_USER");
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
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(Mockito.anyList(),
                Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.any(CourtLocation.class));

        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            jurorManagementService.validatePoolMembers(
                payload,
                requestDto
            ));

        verify(courtLocationRepository, Mockito.times(1))
            .findByLocCode(sourceCourtLocCode);
        verify(jurorPoolRepository, Mockito.never())
            .findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(Mockito.anyList(),
                Mockito.anyBoolean(), Mockito.anyString(),
                Mockito.any(CourtLocation.class));
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
        juror.setFirstName("Test");
        juror.setLastName("Person");

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

    private List<JurorPool> createJurorPoolList() {

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("415");

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("123456789");
        poolRequest.setCourtLocation(courtLocation);

        Juror juror = new Juror();
        juror.setJurorNumber("123456789");

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("400");
        jurorPool.setPool(poolRequest);

        juror.setAssociatedPools(Set.of(jurorPool));
        jurorPool.setJuror(juror);

        List<JurorPool> poolMemberList = new ArrayList<>();
        poolMemberList.add(jurorPool);

        return poolMemberList;
    }

    private BureauJWTPayload buildPayload(String owner) {
        return BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("SOME_USER")
            .daysToExpire(89)
            .owner(owner)
            .build();
    }

}
