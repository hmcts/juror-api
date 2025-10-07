package uk.gov.hmcts.juror.api.moj.service;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.controller.request.CjsEmploymentDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.EligibilityDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPaperResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReasonableAdjustmentDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ReplyTypeDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.SignatureDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorPaperResponseDetailDto;
import uk.gov.hmcts.juror.api.moj.domain.CjsEmploymentType;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.SummonsSnapshot;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorReasonableAdjustment;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseCjsEmployment;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.ReasonableAdjustments;
import uk.gov.hmcts.juror.api.moj.enumeration.jurorresponse.ReasonableAdjustmentsEnum;
import uk.gov.hmcts.juror.api.moj.exception.JurorPaperResponseException;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.SummonsSnapshotRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorReasonableAdjustmentRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseCjsEmploymentRepositoryMod;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;

@SuppressWarnings({
    "PMD.ExcessiveImports",
    "PMD.CouplingBetweenObjects",
    "PMD.ExcessivePublicCount",
    "PMD.TooManyMethods",
    "PMD.CyclomaticComplexity"
})
@RunWith(SpringRunner.class)
public class JurorPaperResponseServiceImplTest {

    private static final String VALID_JUROR_NUMBER_BUREAU = "123456789";
    private static final String VALID_JUROR_NUMBER_COURT = "987654321";

    @Mock
    private PoolRequestRepository poolRequestRepository;
    @Mock
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    @Mock
    private JurorResponseCjsEmploymentRepositoryMod jurorResponseCjsRepository;
    @Mock
    private JurorReasonableAdjustmentRepository jurorReasonableAdjustmentsRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private JurorRepository jurorRepository;
    @Mock
    private JurorDigitalResponseRepositoryMod jurorDigitalResponseRepositoryMod;
    @Mock
    private JurorPoolRepository jurorPoolRepository;
    @Mock
    private SummonsSnapshotRepository summonsSnapshotRepository;
    @Mock
    private WelshCourtLocationRepository welshCourtLocationRepository;
    @Mock
    private StraightThroughProcessorService straightThroughProcessorService;
    @Mock
    private JurorResponseAuditRepositoryMod jurorResponseAuditRepository;
    @Mock
    private JurorPoolService jurorPoolService;
    @Mock
    private JurorHistoryService jurorHistoryService;
    @InjectMocks
    private JurorPaperResponseServiceImpl jurorPaperResponseService;

    @Before
    public void setUpMocks() {
        JurorPool bureauOwnerJurorPool = createTestJurorPool("400", VALID_JUROR_NUMBER_BUREAU);
        JurorPool courtOwnerJurorPool = createTestJurorPool("415", VALID_JUROR_NUMBER_COURT);

        Juror bureauJuror = bureauOwnerJurorPool.getJuror();
        Juror courtJuror = courtOwnerJurorPool.getJuror();

        Mockito.when(jurorRepository.findByJurorNumber(bureauJuror.getJurorNumber())).thenReturn(bureauJuror);
        Mockito.when(jurorRepository.findByJurorNumber(courtJuror.getJurorNumber())).thenReturn(courtJuror);

        Mockito.doReturn(Collections.singletonList(bureauOwnerJurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(VALID_JUROR_NUMBER_BUREAU, true);
        Mockito.doReturn(Collections.singletonList(courtOwnerJurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(VALID_JUROR_NUMBER_COURT, true);

        Mockito.doReturn(bureauOwnerJurorPool).when(jurorPoolService)
            .getJurorPoolFromUser(VALID_JUROR_NUMBER_BUREAU);
        Mockito.doReturn(courtOwnerJurorPool).when(jurorPoolService)
            .getJurorPoolFromUser(VALID_JUROR_NUMBER_COURT);

        Mockito.doReturn(Collections.singletonList(bureauOwnerJurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc("123456789", true);
        Mockito.doReturn(Collections.singletonList(courtOwnerJurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc("987654321", true);

        Mockito.doReturn(null).when(jurorPaperResponseRepository).save(any());
        Mockito.doReturn(null).when(jurorResponseCjsRepository).save(any());
        Mockito.doReturn(null).when(jurorReasonableAdjustmentsRepository).save(any());
        Mockito.doReturn(
                User.builder().username("SOME_USER").name("Test User").active(true).build())
            .when(userRepository).save(any());

        Mockito.when(poolRequestRepository.findByPoolNumber(any()))
            .thenReturn(Optional.of(mockPoolRequest("415230101", "415")));

        Mockito.doReturn(Optional.empty()).when(welshCourtLocationRepository).findById(any());

        Mockito.doReturn(false).when(straightThroughProcessorService)
            .isValidForStraightThroughAgeDisqualification(any(PaperResponse.class),
                any(LocalDate.class), any(JurorPool.class));

        TestUtils.setUpMockAuthentication("400", "Bureau", "1", List.of("400"));
    }

    @Test
    public void test_getJurorPaperResponse_happyPath_bureauUser_bureauOwner() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = TestUtils.getSecurityUtilMock();
        securityUtilMockedStatic.when(SecurityUtil::canEditApprovalLimit).thenReturn(false);

        BureauJwtPayload payload = buildPayload();
        PaperResponse jurorPaperResponse = createTestJurorResponse("123456789");
        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository).findByJurorNumber("123456789");
        Mockito.when(poolRequestRepository.findByPoolNumber(any()))
            .thenReturn(Optional.of(mockPoolRequest("12345678", "415")));

        JurorPaperResponseDetailDto responseDto = jurorPaperResponseService
            .getJurorPaperResponse(VALID_JUROR_NUMBER_BUREAU, payload);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc("123456789", true);
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).findByJurorNumber("123456789");

        verifyPaperResponseIsCopiedToDto(jurorPaperResponse, responseDto);
        Assertions.assertThat(responseDto.isWelshCourt()).isFalse();
        Assertions.assertThat(responseDto.getCurrentOwner()).isEqualTo("400");
    }

    @Test
    public void test_getJurorPaperResponse_happyPath_bureauUser_bureauOwner_welsh() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = TestUtils.getSecurityUtilMock();
        securityUtilMockedStatic.when(SecurityUtil::canEditApprovalLimit).thenReturn(false);

        BureauJwtPayload payload = buildPayload();
        PaperResponse jurorPaperResponse = createTestJurorResponse(VALID_JUROR_NUMBER_BUREAU);
        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository)
            .findByJurorNumber(VALID_JUROR_NUMBER_BUREAU);
        Mockito.doReturn(Optional.of(new WelshCourtLocation()))
            .when(welshCourtLocationRepository).findById(Mockito.anyString());

        JurorPaperResponseDetailDto responseDto = jurorPaperResponseService
            .getJurorPaperResponse(VALID_JUROR_NUMBER_BUREAU, payload);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc("123456789", true);
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).findByJurorNumber("123456789");

        verifyPaperResponseIsCopiedToDto(jurorPaperResponse, responseDto);
        Assertions.assertThat(responseDto.isWelshCourt()).isTrue();
    }

    @Test
    public void test_getJurorPaperResponse_happyPath_summonsSnapshotPresent() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = TestUtils.getSecurityUtilMock();
        securityUtilMockedStatic.when(SecurityUtil::canEditApprovalLimit).thenReturn(false);

        BureauJwtPayload payload = buildPayload();
        String jurorNumber = VALID_JUROR_NUMBER_BUREAU;
        PaperResponse jurorPaperResponse = createTestJurorResponse(jurorNumber);
        SummonsSnapshot summonsSnapshot = new SummonsSnapshot(jurorNumber, "435230101",
            "435", "MANCHESTER", "THE CROWN COURT AT MANCHESTER",
            LocalDate.of(2023, 6, 5),
            LocalDateTime.of(2023, 4, 11, 9, 15));

        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);
        Mockito.doReturn(Optional.of(summonsSnapshot)).when(summonsSnapshotRepository).findById(jurorNumber);

        final JurorPaperResponseDetailDto responseDto = jurorPaperResponseService.getJurorPaperResponse(jurorNumber,
            payload);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).findByJurorNumber(jurorNumber);
        Mockito.verify(summonsSnapshotRepository, Mockito.times(1)).findById(jurorNumber);

        verifyPaperResponseIsCopiedToDto(jurorPaperResponse, responseDto);
        verifyPoolDetailsMapping(summonsSnapshot, responseDto);
    }

    @Test
    public void test_getJurorPaperResponse_happyPath_bureauUser_courtOwner() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = TestUtils.getSecurityUtilMock();
        securityUtilMockedStatic.when(SecurityUtil::canEditApprovalLimit).thenReturn(false);

        BureauJwtPayload payload = buildPayload();
        PaperResponse jurorPaperResponse = createTestJurorResponse(VALID_JUROR_NUMBER_COURT);
        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository)
            .findByJurorNumber(VALID_JUROR_NUMBER_COURT);

        JurorPaperResponseDetailDto responseDto = jurorPaperResponseService
            .getJurorPaperResponse(VALID_JUROR_NUMBER_COURT, payload);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc("987654321", true);
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).findByJurorNumber("987654321");

        verifyPaperResponseIsCopiedToDto(jurorPaperResponse, responseDto);
        Assertions.assertThat(responseDto.getCurrentOwner()).isEqualTo("415");
    }

    @Test
    public void test_getJurorPaperResponse_happyPath_courtUser_courtOwner() {
        MockedStatic<SecurityUtil> securityUtilMockedStatic = TestUtils.getSecurityUtilMock();
        securityUtilMockedStatic.when(SecurityUtil::canEditApprovalLimit).thenReturn(false);

        BureauJwtPayload payload = buildPayload();
        payload.setOwner("415");
        PaperResponse jurorPaperResponse = createTestJurorResponse(VALID_JUROR_NUMBER_COURT);
        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository)
            .findByJurorNumber(VALID_JUROR_NUMBER_COURT);

        JurorPaperResponseDetailDto responseDto = jurorPaperResponseService
            .getJurorPaperResponse(VALID_JUROR_NUMBER_COURT, payload);

        Mockito.verify(jurorPoolRepository, Mockito.times(2))
            .findByJurorJurorNumberAndIsActive(VALID_JUROR_NUMBER_COURT, true);
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1))
            .findByJurorNumber(VALID_JUROR_NUMBER_COURT);

        verifyPaperResponseIsCopiedToDto(jurorPaperResponse, responseDto);
    }

    @Test
    public void test_getJurorPaperResponse_courtUser_bureauOwner() {
        BureauJwtPayload payload = buildPayload();
        payload.setOwner("415");
        String jurorNumber = VALID_JUROR_NUMBER_BUREAU;
        PaperResponse jurorPaperResponse = createTestJurorResponse(jurorNumber);
        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorPaperResponseService.getJurorPaperResponse(jurorNumber, payload));
    }

    @Test
    public void test_getJurorPaperResponse_bureauUser_noJurorPool() {
        BureauJwtPayload payload = buildPayload();
        PaperResponse jurorPaperResponse = createTestJurorResponse("111111111");
        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository).findByJurorNumber("111111111");

        Assertions.assertThatExceptionOfType(MojException.NotFound.class)
            .isThrownBy(() -> jurorPaperResponseService.getJurorPaperResponse("111111111", payload));

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc("111111111", true);
        Mockito.verify(jurorPaperResponseRepository, Mockito.never()).findById("111111111");
    }

    @Test
    public void test_getJurorPaperResponse_courtUser_noAccess() {
        BureauJwtPayload payload = buildPayload();
        payload.setOwner("416");
        PaperResponse jurorPaperResponse = createTestJurorResponse(VALID_JUROR_NUMBER_COURT);
        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository)
            .findByJurorNumber(VALID_JUROR_NUMBER_COURT);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorPaperResponseService.getJurorPaperResponse(VALID_JUROR_NUMBER_COURT, payload));

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActive(VALID_JUROR_NUMBER_COURT, true);
        Mockito.verify(jurorPaperResponseRepository, Mockito.never()).findById(VALID_JUROR_NUMBER_COURT);
    }

    @Test
    public void test_getJurorPaperResponse_courtUser_noPaperResponseExists() {
        BureauJwtPayload payload = buildPayload();
        payload.setOwner("415");
        Mockito.doReturn(null).when(jurorPaperResponseRepository)
            .findByJurorNumber(VALID_JUROR_NUMBER_COURT);

        Assertions.assertThatExceptionOfType(MojException.NotFound.class)
            .isThrownBy(() -> jurorPaperResponseService.getJurorPaperResponse(VALID_JUROR_NUMBER_COURT, payload));

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActive(VALID_JUROR_NUMBER_COURT, true);
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1))
            .findByJurorNumber(VALID_JUROR_NUMBER_COURT);
    }


    @Test
    public void test_saveResponse_bureauUser_bureauOwnedJurorRecord() {
        final ArgumentCaptor<PaperResponse> paperResponseArgumentCaptor =
            ArgumentCaptor.forClass(PaperResponse.class);
        final BureauJwtPayload payload = buildPayload();

        JurorPaperResponseDto responseDto = buildJurorPaperResponseDto();
        setEligibilityDetails(responseDto);
        setThirdPartyDetails(responseDto);

        Mockito.doReturn(true).when(straightThroughProcessorService)
            .isValidForStraightThroughAgeDisqualification(any(PaperResponse.class),
                any(LocalDate.class), any(JurorPool.class));
        Mockito.doNothing().when(straightThroughProcessorService)
            .processAgeDisqualification(any(PaperResponse.class), any(LocalDate.class),
                any(JurorPool.class), any());

        jurorPaperResponseService.saveResponse(payload, responseDto);

        Mockito.verify(jurorPoolService, Mockito.times(1))
            .getJurorPoolFromUser(VALID_JUROR_NUMBER_BUREAU);
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1))
            .save(paperResponseArgumentCaptor.capture());
        PaperResponse paperResponse = paperResponseArgumentCaptor.getValue();
        verifyJurorPaperResponseDto(paperResponse, responseDto);

        Mockito.verify(jurorResponseCjsRepository, Mockito.never()).save(any());
        Mockito.verify(jurorReasonableAdjustmentsRepository, Mockito.never()).save(any());
        Mockito.verify(straightThroughProcessorService, Mockito.times(1))
            .isValidForStraightThroughAgeDisqualification(any(PaperResponse.class),
                any(LocalDate.class), any(JurorPool.class));
        Mockito.verify(straightThroughProcessorService, Mockito.times(1))
            .processAgeDisqualification(any(PaperResponse.class), any(LocalDate.class),
                any(JurorPool.class), any());
    }

    @Test
    public void test_saveResponse_bureauUser_courtOwnedJurorRecord() {
        final BureauJwtPayload payload = buildPayload();

        JurorPaperResponseDto responseDto = buildJurorPaperResponseDto();
        responseDto.setJurorNumber(VALID_JUROR_NUMBER_COURT);
        setEligibilityDetails(responseDto);
        setThirdPartyDetails(responseDto);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            jurorPaperResponseService.saveResponse(payload, responseDto));

        Mockito.verify(jurorPoolService, Mockito.times(1))
            .getJurorPoolFromUser("987654321");
        Mockito.verify(jurorPaperResponseRepository, Mockito.never()).save(any());
        Mockito.verify(jurorResponseCjsRepository, Mockito.never()).save(any());
        Mockito.verify(jurorReasonableAdjustmentsRepository, Mockito.never()).save(any());
        Mockito.verify(straightThroughProcessorService, Mockito.never())
            .isValidForStraightThroughAgeDisqualification(any(PaperResponse.class),
                any(LocalDate.class), any(JurorPool.class));
        Mockito.verify(straightThroughProcessorService, Mockito.never())
            .processAgeDisqualification(any(PaperResponse.class), any(LocalDate.class),
                any(JurorPool.class), any());
    }

    @Test
    public void test_saveResponse_courtUser_bureauOwnedJurorRecord() {
        BureauJwtPayload payload = buildPayload();
        payload.setOwner("415");

        JurorPaperResponseDto responseDto = buildJurorPaperResponseDto();
        setEligibilityDetails(responseDto);
        setThirdPartyDetails(responseDto);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            jurorPaperResponseService.saveResponse(payload, responseDto));

        Mockito.verify(jurorPoolService, Mockito.times(1))
            .getJurorPoolFromUser("123456789");
        Mockito.verify(jurorPaperResponseRepository, Mockito.never()).save(any());
        Mockito.verify(jurorResponseCjsRepository, Mockito.never()).save(any());
        Mockito.verify(jurorReasonableAdjustmentsRepository, Mockito.never()).save(any());
        Mockito.verify(straightThroughProcessorService, Mockito.never())
            .isValidForStraightThroughAgeDisqualification(any(PaperResponse.class),
                any(LocalDate.class), any(JurorPool.class));
        Mockito.verify(straightThroughProcessorService, Mockito.never())
            .processAgeDisqualification(any(PaperResponse.class), any(LocalDate.class),
                any(JurorPool.class), any());
    }

    @Test
    public void test_saveResponse_courtUser_sameCourtOwnedJurorRecord() {
        final ArgumentCaptor<PaperResponse> paperResponseArgumentCaptor =
            ArgumentCaptor.forClass(PaperResponse.class);
        BureauJwtPayload payload = buildPayload();
        payload.setOwner("415");

        JurorPaperResponseDto responseDto = buildJurorPaperResponseDto();
        responseDto.setJurorNumber(VALID_JUROR_NUMBER_COURT);
        setEligibilityDetails(responseDto);
        setThirdPartyDetails(responseDto);

        jurorPaperResponseService.saveResponse(payload, responseDto);

        Mockito.verify(jurorPoolService, Mockito.times(1))
            .getJurorPoolFromUser(VALID_JUROR_NUMBER_COURT);
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1))
            .save(paperResponseArgumentCaptor.capture());
        PaperResponse paperResponse = paperResponseArgumentCaptor.getValue();
        verifyJurorPaperResponseDto(paperResponse, responseDto);

        Mockito.verify(jurorResponseCjsRepository, Mockito.never()).save(any());
        Mockito.verify(jurorReasonableAdjustmentsRepository, Mockito.never()).save(any());
        Mockito.verify(straightThroughProcessorService, Mockito.times(1))
            .isValidForStraightThroughAgeDisqualification(any(PaperResponse.class), any(LocalDate.class),
                any(JurorPool.class));
        Mockito.verify(straightThroughProcessorService, Mockito.never())
            .processAgeDisqualification(any(PaperResponse.class), any(LocalDate.class),
                any(JurorPool.class), any());
    }

    @Test
    public void test_saveResponse_courtUser_differentCourtOwnedJurorRecord() {
        BureauJwtPayload payload = buildPayload();
        payload.setOwner("416");

        JurorPaperResponseDto responseDto = buildJurorPaperResponseDto();
        responseDto.setJurorNumber(VALID_JUROR_NUMBER_COURT);
        setEligibilityDetails(responseDto);
        setThirdPartyDetails(responseDto);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            jurorPaperResponseService.saveResponse(payload, responseDto));

        Mockito.verify(jurorPoolService, Mockito.times(1))
            .getJurorPoolFromUser("987654321");
        Mockito.verify(jurorPaperResponseRepository, Mockito.never()).save(any());
        Mockito.verify(jurorResponseCjsRepository, Mockito.never()).save(any());
        Mockito.verify(jurorReasonableAdjustmentsRepository, Mockito.never()).save(any());
        Mockito.verify(straightThroughProcessorService, Mockito.never())
            .isValidForStraightThroughAgeDisqualification(any(PaperResponse.class), any(LocalDate.class),
                any(JurorPool.class));
        Mockito.verify(straightThroughProcessorService, Mockito.never())
            .processAgeDisqualification(any(PaperResponse.class), any(LocalDate.class),
                any(JurorPool.class), any());
    }

    @Test
    public void test_saveResponse_duplicateResponseRecord() {
        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());
        BureauJwtPayload payload = buildPayload();

        JurorPaperResponseDto responseDto = buildJurorPaperResponseDto();
        setEligibilityDetails(responseDto);
        setThirdPartyDetails(responseDto);
        Assertions.assertThatExceptionOfType(JurorPaperResponseException.JurorPaperResponseAlreadyExists.class)
            .isThrownBy(() -> jurorPaperResponseService.saveResponse(payload, responseDto));
    }

    @Test
    public void test_saveResponse_withCjs_noSpecialNeeds() {
        BureauJwtPayload payload = buildPayload();
        JurorPaperResponseDto responseDto = buildJurorPaperResponseDto();
        List<JurorPaperResponseDto.CjsEmployment> cjsEmployment =
            Collections.singletonList(buildCjsEmployment(CjsEmploymentType.POLICE.getEmployer()));
        responseDto.setCjsEmployment(cjsEmployment);

        jurorPaperResponseService.saveResponse(payload, responseDto);

        Mockito.verify(jurorPoolService, Mockito.times(1))
            .getJurorPoolFromUser("123456789");
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).save(any());
        Mockito.verify(jurorResponseCjsRepository, Mockito.times(1)).saveAll(any());
        Mockito.verify(jurorReasonableAdjustmentsRepository, Mockito.times(1)).saveAll(any());
        Mockito.verify(straightThroughProcessorService, Mockito.times(1))
            .isValidForStraightThroughAgeDisqualification(any(PaperResponse.class), any(),
                any());
        Mockito.verify(straightThroughProcessorService, Mockito.never())
            .processAgeDisqualification(any(PaperResponse.class), any(LocalDate.class),
                any(JurorPool.class), any());
    }

    @Test
    public void test_saveResponse_noCjs_withSpecialNeeds() {
        BureauJwtPayload payload = buildPayload();
        JurorPaperResponseDto responseDto = buildJurorPaperResponseDto();
        List<JurorPaperResponseDto.ReasonableAdjustment> specialNeeds =
            Collections.singletonList(buildSpecialNeeds(ReasonableAdjustmentsEnum.VISUAL_IMPAIRMENT.getCode()));
        responseDto.setReasonableAdjustments(specialNeeds);

        jurorPaperResponseService.saveResponse(payload, responseDto);

        Mockito.verify(jurorPoolService, Mockito.times(1))
            .getJurorPoolFromUser("123456789");
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).save(any());
        Mockito.verify(jurorResponseCjsRepository, Mockito.times(1)).saveAll(any());
        Mockito.verify(jurorReasonableAdjustmentsRepository, Mockito.times(1)).saveAll(any());
        Mockito.verify(straightThroughProcessorService, Mockito.times(1))
            .isValidForStraightThroughAgeDisqualification(any(PaperResponse.class), any(LocalDate.class),
                any(JurorPool.class));
        Mockito.verify(straightThroughProcessorService, Mockito.never())
            .processAgeDisqualification(any(PaperResponse.class), any(LocalDate.class),
                any(JurorPool.class), any());
    }

    @Test
    public void test_saveResponse_withCjs_withSpecialNeeds() {
        BureauJwtPayload payload = buildPayload();
        JurorPaperResponseDto responseDto = buildJurorPaperResponseDto();
        List<JurorPaperResponseDto.CjsEmployment> cjsEmployment =
            Collections.singletonList(buildCjsEmployment(CjsEmploymentType.POLICE.getEmployer()));
        responseDto.setCjsEmployment(cjsEmployment);
        List<JurorPaperResponseDto.ReasonableAdjustment> specialNeeds =
            Collections.singletonList(buildSpecialNeeds(ReasonableAdjustmentsEnum.VISUAL_IMPAIRMENT.getCode()));
        responseDto.setReasonableAdjustments(specialNeeds);

        jurorPaperResponseService.saveResponse(payload, responseDto);

        Mockito.verify(jurorPoolService, Mockito.times(1))
            .getJurorPoolFromUser("123456789");
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).save(any());
        Mockito.verify(jurorResponseCjsRepository, Mockito.times(1)).saveAll(any());
        Mockito.verify(jurorReasonableAdjustmentsRepository, Mockito.times(1)).saveAll(any());
        Mockito.verify(straightThroughProcessorService, Mockito.times(1))
            .isValidForStraightThroughAgeDisqualification(any(PaperResponse.class), any(LocalDate.class),
                any(JurorPool.class));
        Mockito.verify(straightThroughProcessorService, Mockito.never())
            .processAgeDisqualification(any(PaperResponse.class), any(LocalDate.class),
                any(JurorPool.class), any());
    }

    @Test
    public void test_saveResponse_invalidCjsEmployer() {
        BureauJwtPayload payload = buildPayload();
        JurorPaperResponseDto responseDto = buildJurorPaperResponseDto();
        List<JurorPaperResponseDto.CjsEmployment> cjsEmployment =
            Collections.singletonList(buildCjsEmployment("!£$%^&*()_+"));
        responseDto.setCjsEmployment(cjsEmployment);

        Assertions.assertThatExceptionOfType(JurorPaperResponseException.InvalidCjsEmploymentEntry.class)
            .isThrownBy(() -> jurorPaperResponseService.saveResponse(payload, responseDto));
    }

    @Test
    public void test_saveResponse_duplicateCjsEmployer() {
        final BureauJwtPayload payload = buildPayload();
        JurorPaperResponseDto responseDto = buildJurorPaperResponseDto();
        JurorPaperResponseDto.CjsEmployment cjsEmployment = buildCjsEmployment(CjsEmploymentType.POLICE.getEmployer());
        List<JurorPaperResponseDto.CjsEmployment> cjsEmploymentList = new ArrayList<>();
        cjsEmploymentList.add(cjsEmployment);
        cjsEmploymentList.add(cjsEmployment);
        responseDto.setCjsEmployment(cjsEmploymentList);

        Assertions.assertThatExceptionOfType(JurorPaperResponseException.InvalidCjsEmploymentEntry.class)
            .isThrownBy(() -> jurorPaperResponseService.saveResponse(payload, responseDto));
    }

    @Test
    public void test_saveResponse_invalidSpecialNeeds() {
        final BureauJwtPayload payload = buildPayload();
        JurorPaperResponseDto responseDto = buildJurorPaperResponseDto();
        List<JurorPaperResponseDto.ReasonableAdjustment> specialNeeds =
            Collections.singletonList(buildSpecialNeeds("!£$%^&*()_+"));
        responseDto.setReasonableAdjustments(specialNeeds);

        Assertions.assertThatExceptionOfType(JurorPaperResponseException.InvalidSpecialNeedEntry.class)
            .isThrownBy(() -> jurorPaperResponseService.saveResponse(payload, responseDto));
    }

    @Test
    public void test_saveResponse_duplicateSpecialNeeds() {
        final BureauJwtPayload payload = buildPayload();
        JurorPaperResponseDto responseDto = buildJurorPaperResponseDto();
        JurorPaperResponseDto.ReasonableAdjustment specialNeed
            = buildSpecialNeeds(ReasonableAdjustmentsEnum.VISUAL_IMPAIRMENT.getCode());
        List<JurorPaperResponseDto.ReasonableAdjustment> specialNeedsList = new ArrayList<>();
        specialNeedsList.add(specialNeed);
        specialNeedsList.add(specialNeed);
        responseDto.setReasonableAdjustments(specialNeedsList);

        Assertions.assertThatExceptionOfType(JurorPaperResponseException.InvalidSpecialNeedEntry.class)
            .isThrownBy(() -> jurorPaperResponseService.saveResponse(payload, responseDto));
    }

    @Test
    public void test_saveResponse_welshTicked() {
        final ArgumentCaptor<PaperResponse> paperResponseArgumentCaptor =
            ArgumentCaptor.forClass(PaperResponse.class);
        final BureauJwtPayload payload = buildPayload();

        JurorPaperResponseDto responseDto = buildJurorPaperResponseDto();
        responseDto.setWelsh(Boolean.TRUE);
        setEligibilityDetails(responseDto);
        setThirdPartyDetails(responseDto);

        Mockito.doReturn(true).when(straightThroughProcessorService)
            .isValidForStraightThroughAgeDisqualification(any(PaperResponse.class), any(LocalDate.class),
                any());
        Mockito.doNothing().when(straightThroughProcessorService)
            .processAgeDisqualification(any(PaperResponse.class), any(LocalDate.class), any(),
                any());

        jurorPaperResponseService.saveResponse(payload, responseDto);

        Mockito.verify(jurorPoolService, Mockito.times(1))
            .getJurorPoolFromUser(VALID_JUROR_NUMBER_BUREAU);
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1))
            .save(paperResponseArgumentCaptor.capture());
        PaperResponse paperResponse = paperResponseArgumentCaptor.getValue();
        verifyJurorPaperResponseDto(paperResponse, responseDto);

        Mockito.verify(jurorResponseCjsRepository, Mockito.never()).save(any());
        Mockito.verify(jurorReasonableAdjustmentsRepository, Mockito.never()).save(any());
        Mockito.verify(straightThroughProcessorService, Mockito.times(1))
            .isValidForStraightThroughAgeDisqualification(any(PaperResponse.class), any(LocalDate.class),
                any(JurorPool.class));
        Mockito.verify(straightThroughProcessorService, Mockito.times(1))
            .processAgeDisqualification(any(PaperResponse.class), any(LocalDate.class),
                any(JurorPool.class), any());
    }


    @Test
    public void test_saveResponse_bureauUser_with_eligibility_details() {
        final ArgumentCaptor<PaperResponse> paperResponseArgumentCaptor =
            ArgumentCaptor.forClass(PaperResponse.class);
        final BureauJwtPayload payload = buildPayload();

        JurorPaperResponseDto responseDto = buildJurorPaperResponseDto();
        setEligibilityDetailsIneligible(responseDto);
        setThirdPartyDetails(responseDto);

        Mockito.doReturn(true).when(straightThroughProcessorService)
            .isValidForStraightThroughAgeDisqualification(any(PaperResponse.class),
                                                          any(LocalDate.class), any(JurorPool.class));
        Mockito.doNothing().when(straightThroughProcessorService)
            .processAgeDisqualification(any(PaperResponse.class), any(LocalDate.class),
                                        any(JurorPool.class), any());

        jurorPaperResponseService.saveResponse(payload, responseDto);

        Mockito.verify(jurorPoolService, Mockito.times(1))
            .getJurorPoolFromUser(VALID_JUROR_NUMBER_BUREAU);
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1))
            .save(paperResponseArgumentCaptor.capture());
        PaperResponse paperResponse = paperResponseArgumentCaptor.getValue();
        verifyJurorPaperResponseDto(paperResponse, responseDto);

        Assertions.assertThat(paperResponse.getResidencyDetail()).isEqualTo("Lived in the UK for less than 5 years");
        Assertions.assertThat(paperResponse.getConvictionsDetails()).isEqualTo(
            "I have a conviction for a criminal offence");
        Assertions.assertThat(paperResponse.getMentalHealthActDetails()).isEqualTo(
            "I am detained under the Mental Health Act [MENTAL HEALTH Q2] "
                + "I have mental health capacity issues");
        Assertions.assertThat(paperResponse.getBailDetails()).isEqualTo("I am on bail for a criminal offence");

        Mockito.verify(jurorResponseCjsRepository, Mockito.never()).save(any());
        Mockito.verify(jurorReasonableAdjustmentsRepository, Mockito.never()).save(any());
        Mockito.verify(straightThroughProcessorService, Mockito.times(1))
            .isValidForStraightThroughAgeDisqualification(any(PaperResponse.class),
                                                          any(LocalDate.class), any(JurorPool.class));
        Mockito.verify(straightThroughProcessorService, Mockito.times(1))
            .processAgeDisqualification(any(PaperResponse.class), any(LocalDate.class),
                                        any(JurorPool.class), any());
    }

    @Test
    public void test_updatePaperResponse_CjsEmployment_Happy_bureauUser_bureauOwner() {
        BureauJwtPayload payload = buildPayload();
        CjsEmploymentDetailsDto cjsEmploymentDto = buildCjsEmploymentDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        jurorPaperResponseService.updateCjsDetails(payload, cjsEmploymentDto, VALID_JUROR_NUMBER_BUREAU);


        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).findByJurorNumber(any());
        Mockito.verify(jurorResponseCjsRepository, Mockito.times(1))
            .findByJurorNumber(any());
        Mockito.verify(jurorResponseCjsRepository, Mockito.times(2))
            .findByJurorNumberAndCjsEmployer(any(), any());
        Mockito.verify(jurorResponseCjsRepository, Mockito.times(2)).save(any());

    }

    @Test
    public void test_updatePaperResponse_CjsEmployment_Happy_courtUser_courtOwner() {
        BureauJwtPayload payload = buildPayload();
        payload.setOwner("415");
        CjsEmploymentDetailsDto cjsEmploymentDto = buildCjsEmploymentDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        jurorPaperResponseService.updateCjsDetails(payload, cjsEmploymentDto, VALID_JUROR_NUMBER_COURT);

        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).findByJurorNumber(any());
        Mockito.verify(jurorResponseCjsRepository, Mockito.times(1))
            .findByJurorNumber(any());
        Mockito.verify(jurorResponseCjsRepository, Mockito.times(2))
            .findByJurorNumberAndCjsEmployer(any(), any());
        Mockito.verify(jurorResponseCjsRepository, Mockito.times(2)).save(any());

    }

    @Test
    public void test_updatePaperResponse_CjsEmployment_bureauUser_courtOwner() {
        BureauJwtPayload payload = buildPayload();
        CjsEmploymentDetailsDto cjsEmploymentDto = buildCjsEmploymentDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorPaperResponseService.updateCjsDetails(payload, cjsEmploymentDto,
                VALID_JUROR_NUMBER_COURT));
    }


    @Test
    public void test_updatePaperResponse_CjsEmployment_courtUser_bureauOwner() {
        BureauJwtPayload payload = buildPayload();
        payload.setOwner("415");
        CjsEmploymentDetailsDto cjsEmploymentDto = buildCjsEmploymentDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorPaperResponseService.updateCjsDetails(payload, cjsEmploymentDto,
                VALID_JUROR_NUMBER_BUREAU));
    }

    @Test
    public void test_updatePaperResponse_CjsEmployment_courtUser_noAccess_courtOwner() {
        BureauJwtPayload payload = buildPayload();
        payload.setOwner("416");
        CjsEmploymentDetailsDto cjsEmploymentDto = buildCjsEmploymentDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorPaperResponseService.updateCjsDetails(payload, cjsEmploymentDto,
                VALID_JUROR_NUMBER_COURT));

    }

    @Test
    public void test_updatePaperResponse_CjsEmployment_DuplicateEmployer() {
        BureauJwtPayload payload = buildPayload();
        CjsEmploymentDetailsDto cjsEmploymentDto = buildDuplicateCjsEmploymentDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Assertions.assertThatExceptionOfType(JurorPaperResponseException.InvalidCjsEmploymentEntry.class)
            .isThrownBy(() -> jurorPaperResponseService.updateCjsDetails(payload, cjsEmploymentDto,
                VALID_JUROR_NUMBER_BUREAU));
    }

    @Test
    public void test_updatePaperResponse_CjsEmployment_InvalidEmployer() {
        final BureauJwtPayload payload = buildPayload();
        CjsEmploymentDetailsDto cjsEmploymentDto = buildCjsEmploymentDetailsDto();
        List<JurorPaperResponseDto.CjsEmployment> cjsEmploymentList = new ArrayList<>();
        JurorPaperResponseDto.CjsEmployment cjsEmployment = JurorPaperResponseDto.CjsEmployment.builder()
            .cjsEmployer("Air Force")
            .cjsEmployerDetails("Air force Details text").build();
        cjsEmploymentList.add(cjsEmployment);
        cjsEmploymentDto.setCjsEmployment(cjsEmploymentList);

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Assertions.assertThatExceptionOfType(JurorPaperResponseException.InvalidCjsEmploymentEntry.class)
            .isThrownBy(() -> jurorPaperResponseService.updateCjsDetails(payload, cjsEmploymentDto,
                VALID_JUROR_NUMBER_BUREAU));
    }

    @Test
    public void test_updatePaperResponse_SpecialNeeds_Happy_bureauUser_bureauOwner() {
        BureauJwtPayload payload = buildPayload();
        ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto = buildSpecialNeedsDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Mockito.when(jurorRepository.findById(VALID_JUROR_NUMBER_BUREAU))
            .thenReturn(Optional.of(buildMockJuror(VALID_JUROR_NUMBER_BUREAU)));

        jurorPaperResponseService.updateReasonableAdjustmentsDetails(payload, reasonableAdjustmentDetailsDto,
            VALID_JUROR_NUMBER_BUREAU);

        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).findByJurorNumber(any());
        Mockito.verify(jurorReasonableAdjustmentsRepository, Mockito.times(1))
            .findByJurorNumber(any());
        Mockito.verify(jurorReasonableAdjustmentsRepository, Mockito.times(2))
            .findByJurorNumberAndReasonableAdjustment(any(), any());
        Mockito.verify(jurorReasonableAdjustmentsRepository, Mockito.times(2))
            .save(any());
        Mockito.verify(jurorRepository, Mockito.times(1)).save(any());
    }


    /**
     * Ensures juror reasonable adjustments can be updated with an empty request body (i.e., juror no longer requires
     * any reasonable adjustments, so any pre-existing reasonable adjustments are erased)
     */
    @Test
    public void testUpdatePaperResponseSpecialNeedsEmptyReasonableAdjustmentsHappyBureauUserBureauOwner() {
        ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto = new ReasonableAdjustmentDetailsDto();
        reasonableAdjustmentDetailsDto.setReasonableAdjustments(new ArrayList<>());

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Juror mockJuror = buildMockJuror(VALID_JUROR_NUMBER_BUREAU);
        mockJuror.setReasonableAdjustmentCode("V");
        mockJuror.setReasonableAdjustmentMessage("Visual Impairment");

        Mockito.when(jurorRepository.findById(VALID_JUROR_NUMBER_BUREAU)).thenReturn(Optional.of(mockJuror));

        BureauJwtPayload payload = buildPayload();
        jurorPaperResponseService.updateReasonableAdjustmentsDetails(payload, reasonableAdjustmentDetailsDto,
            VALID_JUROR_NUMBER_BUREAU);

        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1))
            .findByJurorNumber(any());
        Mockito.verify(jurorReasonableAdjustmentsRepository, Mockito.times(1))
            .findByJurorNumber(any());
        assertNull(mockJuror.getReasonableAdjustmentCode(), "Expected reasonable adjustment code to be null");
        assertNull(mockJuror.getReasonableAdjustmentMessage(), "Expected reasonable adjustment message to be null");
        assertEquals(Collections.EMPTY_LIST, jurorReasonableAdjustmentsRepository
            .findByJurorNumber(mockJuror.getJurorNumber()), "Expected no reasonable adjustments to be present");
    }


    @Test
    public void test_updatePaperResponse_SpecialNeeds_Happy_courtUser_courtOwner() {
        BureauJwtPayload payload = buildPayload();
        payload.setOwner("415");
        ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto = buildSpecialNeedsDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());
        Mockito.when(jurorRepository.findById(VALID_JUROR_NUMBER_COURT))
            .thenReturn(Optional.of(buildMockJuror(VALID_JUROR_NUMBER_COURT)));

        jurorPaperResponseService.updateReasonableAdjustmentsDetails(payload, reasonableAdjustmentDetailsDto,
            VALID_JUROR_NUMBER_COURT);

        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).findByJurorNumber(any());
        Mockito.verify(jurorReasonableAdjustmentsRepository, Mockito.times(1))
            .findByJurorNumber(any());
        Mockito.verify(jurorReasonableAdjustmentsRepository, Mockito.times(2))
            .findByJurorNumberAndReasonableAdjustment(any(), any());
        Mockito.verify(jurorReasonableAdjustmentsRepository, Mockito.times(2))
            .save(any());
        Mockito.verify(jurorRepository, Mockito.times(1)).save(any());

    }

    @Test
    public void test_updatePaperResponse_SpecialNeeds_bureauUser_courtOwner() {
        BureauJwtPayload payload = buildPayload();
        ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto = buildSpecialNeedsDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorPaperResponseService.updateReasonableAdjustmentsDetails(payload,
                reasonableAdjustmentDetailsDto,
                VALID_JUROR_NUMBER_COURT));
    }

    @Test
    public void test_updatePaperResponse_SpecialNeeds_courtUser_bureauOwner() {
        BureauJwtPayload payload = buildPayload();
        payload.setOwner("415");
        ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto = buildSpecialNeedsDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorPaperResponseService.updateReasonableAdjustmentsDetails(payload,
                reasonableAdjustmentDetailsDto,
                VALID_JUROR_NUMBER_BUREAU));
    }

    @Test
    public void test_updatePaperResponse_SpecialNeeds_courtUser_noAccess_courtOwner() {
        BureauJwtPayload payload = buildPayload();
        payload.setOwner("416");
        ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto = buildSpecialNeedsDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorPaperResponseService.updateReasonableAdjustmentsDetails(payload,
                reasonableAdjustmentDetailsDto,
                VALID_JUROR_NUMBER_COURT));
    }

    @Test
    public void test_updatePaperResponse_SpecialNeeds_InvalidAssistanceType() {
        BureauJwtPayload payload = buildPayload();
        ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto = buildInvalidSpecialNeedsDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Assertions.assertThatExceptionOfType(JurorPaperResponseException.InvalidSpecialNeedEntry.class)
            .isThrownBy(() -> jurorPaperResponseService.updateReasonableAdjustmentsDetails(payload,
                reasonableAdjustmentDetailsDto,
                VALID_JUROR_NUMBER_BUREAU));
    }

    @Test
    public void test_updatePaperResponse_SpecialNeeds_DuplicateAssistanceType() {
        BureauJwtPayload payload = buildPayload();
        ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto = buildDuplicateSpecialNeedsDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Assertions.assertThatExceptionOfType(JurorPaperResponseException.InvalidSpecialNeedEntry.class)
            .isThrownBy(() -> jurorPaperResponseService.updateReasonableAdjustmentsDetails(payload,
                reasonableAdjustmentDetailsDto,
                VALID_JUROR_NUMBER_BUREAU));
    }

    @Test
    public void test_updatePaperResponse_EligibilityDetails_Happy_bureauUser_bureauOwner() {
        BureauJwtPayload payload = buildPayload();

        EligibilityDetailsDto eligibilityDto = buildJurorEligibilityDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        jurorPaperResponseService.updateJurorEligibilityDetails(payload, eligibilityDto, VALID_JUROR_NUMBER_BUREAU);

        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).findByJurorNumber(any());
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).save(any());
    }

    @Test
    public void test_updatePaperResponse_EligibilityDetails_Happy_courtUser_courtOwner() {
        BureauJwtPayload payload = buildPayload();
        payload.setOwner("415");

        EligibilityDetailsDto eligibilityDto = buildJurorEligibilityDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());


        jurorPaperResponseService.updateJurorEligibilityDetails(payload, eligibilityDto, VALID_JUROR_NUMBER_COURT);

        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).findByJurorNumber(any());
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).save(any());
    }

    @Test
    public void test_updatePaperResponse_EligibilityDetails_bureauUser_courtOwner() {
        BureauJwtPayload payload = buildPayload();

        EligibilityDetailsDto eligibilityDto = buildJurorEligibilityDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorPaperResponseService.updateJurorEligibilityDetails(payload, eligibilityDto,
                VALID_JUROR_NUMBER_COURT));
    }

    @Test
    public void test_updatePaperResponse_EligibilityDetails_courtUser_noAccess_courtOwner() {
        BureauJwtPayload payload = buildPayload();
        payload.setOwner("416");

        EligibilityDetailsDto eligibilityDto = buildJurorEligibilityDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorPaperResponseService.updateJurorEligibilityDetails(payload, eligibilityDto,
                VALID_JUROR_NUMBER_COURT));
    }

    @Test
    public void test_updatePaperResponse_EligibilityDetails_courtUser_bureauOwner() {
        BureauJwtPayload payload = buildPayload();
        payload.setOwner("415");

        EligibilityDetailsDto eligibilityDto = buildJurorEligibilityDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorPaperResponseService.updateJurorEligibilityDetails(payload, eligibilityDto,
                VALID_JUROR_NUMBER_BUREAU));
    }

    @Test
    public void test_updatePaperResponse_EligibilityDetails_Null() {
        BureauJwtPayload payload = buildPayload();

        EligibilityDetailsDto eligibilityDto = buildJurorEligibilityDetailsDto();
        eligibilityDto.setEligibility(null);

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Assertions.assertThatExceptionOfType(JurorPaperResponseException.InvalidEligibilityEntry.class)
            .isThrownBy(() -> jurorPaperResponseService.updateJurorEligibilityDetails(payload, eligibilityDto,
                VALID_JUROR_NUMBER_BUREAU));
    }

    @Test
    public void test_updatePaperResponse_EligibilityDetails_make_ineligible() {
        final BureauJwtPayload payload = buildPayload();
        final EligibilityDetailsDto eligibilityDto = buildJurorIneligibilityDetailsDto();

        PaperResponse paperResponse = new PaperResponse();
        paperResponse.setJurorNumber(VALID_JUROR_NUMBER_BUREAU);
        paperResponse.setResidency(true);
        paperResponse.setMentalHealthAct(false);
        paperResponse.setMentalHealthCapacity(false);
        paperResponse.setBail(false);
        paperResponse.setConvictions(false);

        Mockito.doReturn(paperResponse).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        jurorPaperResponseService.updateJurorEligibilityDetails(payload, eligibilityDto,
                                                                VALID_JUROR_NUMBER_BUREAU);

        ArgumentCaptor<PaperResponse> responseArgumentCaptor = ArgumentCaptor.forClass(PaperResponse.class);

        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).findByJurorNumber(any());
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).save(responseArgumentCaptor.capture());

        PaperResponse updatedPaperResponse = responseArgumentCaptor.getValue();
        Assertions.assertThat(updatedPaperResponse.getResidency()).isFalse();
        Assertions.assertThat(updatedPaperResponse.getMentalHealthAct()).isTrue();
        Assertions.assertThat(updatedPaperResponse.getMentalHealthCapacity()).isTrue();
        Assertions.assertThat(updatedPaperResponse.getBail()).isTrue();
        Assertions.assertThat(updatedPaperResponse.getConvictions()).isTrue();

        Assertions.assertThat(updatedPaperResponse.getResidencyDetail()).isEqualTo(
            "Lived in the UK for less than 5 years");
        Assertions.assertThat(updatedPaperResponse.getMentalHealthActDetails()).isEqualTo(
            "I am detained under the Mental Health Act [MENTAL HEALTH Q2] "
                + "I have mental health capacity issues");
        Assertions.assertThat(updatedPaperResponse.getBailDetails()).isEqualTo("I am on bail for a criminal offence");
        Assertions.assertThat(updatedPaperResponse.getConvictionsDetails()).isEqualTo(
            "I have a conviction for a criminal offence");

    }

    @Test
    public void test_updatePaperResponse_ReplyTypeDetails_Happy_bureauUser_bureauOwner() {
        BureauJwtPayload payload = buildPayload();

        ReplyTypeDetailsDto replyTypeDto = buildJurorReplyTypeDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        jurorPaperResponseService.updateJurorReplyTypeDetails(payload, replyTypeDto, VALID_JUROR_NUMBER_BUREAU);

        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).findByJurorNumber(any());
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).save(any());
    }

    @Test
    public void test_updatePaperResponse_ReplyTypeDetails_Happy_courtUser_courtOwner() {
        BureauJwtPayload payload = buildPayload();
        payload.setOwner("415");

        ReplyTypeDetailsDto replyTypeDto = buildJurorReplyTypeDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        jurorPaperResponseService.updateJurorReplyTypeDetails(payload, replyTypeDto, VALID_JUROR_NUMBER_COURT);

        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).findByJurorNumber(any());
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).save(any());
    }

    @Test
    public void test_updatePaperResponse_ReplyTypeDetails_bureauUser_courtOwner() {
        BureauJwtPayload payload = buildPayload();

        ReplyTypeDetailsDto replyTypeDto = buildJurorReplyTypeDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorPaperResponseService.updateJurorReplyTypeDetails(payload, replyTypeDto,
                VALID_JUROR_NUMBER_COURT));
    }

    @Test
    public void test_updatePaperResponse_ReplyTypeDetails_courtUser_bureauOwner() {
        BureauJwtPayload payload = buildPayload();
        payload.setOwner("415");

        ReplyTypeDetailsDto replyTypeDto = buildJurorReplyTypeDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorPaperResponseService.updateJurorReplyTypeDetails(payload, replyTypeDto,
                VALID_JUROR_NUMBER_BUREAU));
    }

    @Test
    public void test_updatePaperResponse_ReplyTypeDetails_courtUser_noAccess_courtOwner() {
        BureauJwtPayload payload = buildPayload();
        payload.setOwner("416");

        ReplyTypeDetailsDto replyTypeDto = buildJurorReplyTypeDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorPaperResponseService.updateJurorReplyTypeDetails(payload, replyTypeDto,
                VALID_JUROR_NUMBER_COURT));
    }

    @Test
    public void test_updatePaperResponse_ReplyTypeDetails_InvalidReplyTypes() {
        BureauJwtPayload payload = buildPayload();

        ReplyTypeDetailsDto replyTypeDto = buildJurorInvalidReplyTypeDetailsDto();

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Assertions.assertThatExceptionOfType(JurorPaperResponseException.InvalidReplyTypeEntry.class)
            .isThrownBy(() -> jurorPaperResponseService.updateJurorReplyTypeDetails(payload, replyTypeDto,
                VALID_JUROR_NUMBER_BUREAU));
    }

    @Test
    public void test_updatePaperResponse_SignatureDetails_Happy_bureauUser_bureauOwner() {
        BureauJwtPayload payload = buildPayload();

        SignatureDetailsDto signatureDetailsDto = new SignatureDetailsDto();
        signatureDetailsDto.setSignature(false);

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        jurorPaperResponseService.updateJurorSignatureDetails(payload, signatureDetailsDto, VALID_JUROR_NUMBER_BUREAU);

        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).findByJurorNumber(any());
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).save(any());
    }

    @Test
    public void test_updatePaperResponse_SignatureDetails_Happy_courtUser_courtOwner() {
        BureauJwtPayload payload = buildPayload();
        payload.setOwner("415");

        SignatureDetailsDto signatureDetailsDto = new SignatureDetailsDto();
        signatureDetailsDto.setSignature(false);

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        jurorPaperResponseService.updateJurorSignatureDetails(payload, signatureDetailsDto, VALID_JUROR_NUMBER_COURT);

        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).findByJurorNumber(any());
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).save(any());
    }

    @Test
    public void test_updatePaperResponse_SignatureDetails_bureauUser_courtOwner() {
        BureauJwtPayload payload = buildPayload();

        SignatureDetailsDto signatureDetailsDto = new SignatureDetailsDto();
        signatureDetailsDto.setSignature(false);

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorPaperResponseService.updateJurorSignatureDetails(payload, signatureDetailsDto,
                VALID_JUROR_NUMBER_COURT));
    }

    @Test
    public void test_updatePaperResponse_SignatureDetails_courtUser_bureauOwner() {
        BureauJwtPayload payload = buildPayload();
        payload.setOwner("415");

        SignatureDetailsDto signatureDetailsDto = new SignatureDetailsDto();
        signatureDetailsDto.setSignature(false);

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorPaperResponseService.updateJurorSignatureDetails(payload, signatureDetailsDto,
                VALID_JUROR_NUMBER_BUREAU));
    }

    @Test
    public void test_updatePaperResponse_SignatureDetails_courtUser_noAccess_courtOwner() {
        BureauJwtPayload payload = buildPayload();
        payload.setOwner("416");

        SignatureDetailsDto signatureDetailsDto = new SignatureDetailsDto();
        signatureDetailsDto.setSignature(false);

        Mockito.doReturn(new PaperResponse()).when(jurorPaperResponseRepository)
            .findByJurorNumber(any());

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> jurorPaperResponseService.updateJurorSignatureDetails(payload, signatureDetailsDto,
                VALID_JUROR_NUMBER_COURT));
    }

    private Juror buildMockJuror(String jurorNumber) {
        Juror juror = new Juror();
        juror.setTitle("Dr");
        juror.setFirstName("Test");
        juror.setLastName("Person");
        juror.setAddressLine1("Address Line One");
        juror.setAddressLine2("Address Line Two");
        juror.setAddressLine3("Address Line Three");
        juror.setAddressLine4("Town");
        juror.setAddressLine5("County");
        juror.setPostcode("PO19 1SX");
        juror.setJurorNumber(jurorNumber);
        return juror;
    }


    private ReplyTypeDetailsDto buildJurorInvalidReplyTypeDetailsDto() {
        ReplyTypeDetailsDto replyTypeDetailsDto = new ReplyTypeDetailsDto();

        replyTypeDetailsDto.setDeferral(true);
        replyTypeDetailsDto.setExcusal(true);

        return replyTypeDetailsDto;
    }

    private ReplyTypeDetailsDto buildJurorReplyTypeDetailsDto() {

        ReplyTypeDetailsDto replyTypeDetailsDto = new ReplyTypeDetailsDto();

        replyTypeDetailsDto.setDeferral(false);
        replyTypeDetailsDto.setExcusal(false);

        return replyTypeDetailsDto;
    }


    private EligibilityDetailsDto buildJurorEligibilityDetailsDto() {

        EligibilityDetailsDto eligibilityDetailsDto = new EligibilityDetailsDto();


        JurorPaperResponseDto.Eligibility eligibility = JurorPaperResponseDto.Eligibility.builder()
            .livedConsecutive(true)
            .onBail(false)
            .mentalHealthCapacity(false)
            .mentalHealthAct(false)
            .convicted(false)
            .build();

        eligibilityDetailsDto.setEligibility(eligibility);

        return eligibilityDetailsDto;
    }

    private EligibilityDetailsDto buildJurorIneligibilityDetailsDto() {

        EligibilityDetailsDto eligibilityDetailsDto = new EligibilityDetailsDto();


        JurorPaperResponseDto.Eligibility eligibility = JurorPaperResponseDto.Eligibility.builder()
            .livedConsecutive(false)
            .livedConsecutiveDetails("Lived in the UK for less than 5 years")
            .onBail(true)
            .onBailDetails("I am on bail for a criminal offence")
            .mentalHealthCapacity(true)
            .mentalHealthAct(true)
            .mentalHealthActDetails("I am detained under the Mental Health Act [MENTAL HEALTH Q2] "
                                        + "I have mental health capacity issues")
            .convicted(true)
            .convictedDetails("I have a conviction for a criminal offence")
            .build();

        eligibilityDetailsDto.setEligibility(eligibility);

        return eligibilityDetailsDto;
    }


    private CjsEmploymentDetailsDto buildCjsEmploymentDetailsDto() {
        final CjsEmploymentDetailsDto cjsEmploymentDetailsDto = new CjsEmploymentDetailsDto();

        List<JurorPaperResponseDto.CjsEmployment> cjsEmploymentList = new ArrayList<>();

        JurorPaperResponseDto.CjsEmployment cjsEmployment = JurorPaperResponseDto.CjsEmployment.builder()
            .cjsEmployer(CjsEmploymentType.POLICE.getEmployer())
            .cjsEmployerDetails("Police Details text").build();
        cjsEmploymentList.add(cjsEmployment);
        cjsEmployment = JurorPaperResponseDto.CjsEmployment.builder()
            .cjsEmployer(CjsEmploymentType.NCA.getEmployer())
            .cjsEmployerDetails("NCA Details text").build();
        cjsEmploymentList.add(cjsEmployment);
        cjsEmploymentDetailsDto.setCjsEmployment(cjsEmploymentList);
        return cjsEmploymentDetailsDto;
    }

    private CjsEmploymentDetailsDto buildDuplicateCjsEmploymentDetailsDto() {
        final CjsEmploymentDetailsDto cjsEmploymentDetailsDto = new CjsEmploymentDetailsDto();

        List<JurorPaperResponseDto.CjsEmployment> cjsEmploymentList = new ArrayList<>();

        JurorPaperResponseDto.CjsEmployment cjsEmployment = JurorPaperResponseDto.CjsEmployment.builder()
            .cjsEmployer(CjsEmploymentType.POLICE.getEmployer())
            .cjsEmployerDetails("Police Details text").build();
        cjsEmploymentList.add(cjsEmployment);
        cjsEmployment = JurorPaperResponseDto.CjsEmployment.builder()
            .cjsEmployer(CjsEmploymentType.POLICE.getEmployer())
            .cjsEmployerDetails("Police Details text").build();
        cjsEmploymentList.add(cjsEmployment);
        cjsEmploymentDetailsDto.setCjsEmployment(cjsEmploymentList);
        return cjsEmploymentDetailsDto;
    }

    private ReasonableAdjustmentDetailsDto buildSpecialNeedsDetailsDto() {
        final ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto = new ReasonableAdjustmentDetailsDto();

        List<JurorPaperResponseDto.ReasonableAdjustment> reasonableAdjustmentList = new ArrayList<>();

        JurorPaperResponseDto.ReasonableAdjustment reasonableAdjustment
            = JurorPaperResponseDto.ReasonableAdjustment.builder()
            .assistanceType("V")
            .assistanceTypeDetails("Some details on type V")
            .build();
        reasonableAdjustmentList.add(reasonableAdjustment);
        reasonableAdjustment = JurorPaperResponseDto.ReasonableAdjustment.builder()
            .assistanceType("R")
            .assistanceTypeDetails("Some details on type R")
            .build();
        reasonableAdjustmentList.add(reasonableAdjustment);
        reasonableAdjustmentDetailsDto.setReasonableAdjustments(reasonableAdjustmentList);
        return reasonableAdjustmentDetailsDto;
    }

    private ReasonableAdjustmentDetailsDto buildDuplicateSpecialNeedsDetailsDto() {
        final ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto = new ReasonableAdjustmentDetailsDto();

        List<JurorPaperResponseDto.ReasonableAdjustment> reasonableAdjustmentList = new ArrayList<>();

        JurorPaperResponseDto.ReasonableAdjustment reasonableAdjustment
            = JurorPaperResponseDto.ReasonableAdjustment.builder()
            .assistanceType("V")
            .assistanceTypeDetails("Some details on type V")
            .build();
        reasonableAdjustmentList.add(reasonableAdjustment);
        reasonableAdjustment = JurorPaperResponseDto.ReasonableAdjustment.builder()
            .assistanceType("V")
            .assistanceTypeDetails("Some details on type V")
            .build();
        reasonableAdjustmentList.add(reasonableAdjustment);
        reasonableAdjustmentDetailsDto.setReasonableAdjustments(reasonableAdjustmentList);
        return reasonableAdjustmentDetailsDto;
    }

    private ReasonableAdjustmentDetailsDto buildInvalidSpecialNeedsDetailsDto() {
        ReasonableAdjustmentDetailsDto reasonableAdjustmentDetailsDto = new ReasonableAdjustmentDetailsDto();

        List<JurorPaperResponseDto.ReasonableAdjustment> reasonableAdjustmentList = new ArrayList<>();

        JurorPaperResponseDto.ReasonableAdjustment reasonableAdjustment
            = JurorPaperResponseDto.ReasonableAdjustment.builder()
            .assistanceType("Z")
            .assistanceTypeDetails("Some details on type Z")
            .build();
        reasonableAdjustmentList.add(reasonableAdjustment);
        reasonableAdjustmentDetailsDto.setReasonableAdjustments(reasonableAdjustmentList);
        return reasonableAdjustmentDetailsDto;
    }

    private BureauJwtPayload buildPayload() {
        return BureauJwtPayload.builder()
            .userLevel("99")
            .login("SOME_USER")
            .owner("400")
            .build();
    }

    private JurorPaperResponseDto buildJurorPaperResponseDto() {
        JurorPaperResponseDto jurorPaperResponseDto = new JurorPaperResponseDto();

        jurorPaperResponseDto.setJurorNumber("123456789");

        jurorPaperResponseDto.setTitle("Mr");
        jurorPaperResponseDto.setFirstName("Test");
        jurorPaperResponseDto.setLastName("Person");
        jurorPaperResponseDto.setDateOfBirth(LocalDate.now().minusYears(25));

        jurorPaperResponseDto.setPrimaryPhone("01234567890");
        jurorPaperResponseDto.setSecondaryPhone("07123456789");
        jurorPaperResponseDto.setEmailAddress("email@address.com");

        setAddressDetails(jurorPaperResponseDto);

        jurorPaperResponseDto.setSigned(true);
        return jurorPaperResponseDto;
    }

    private void verifyJurorPaperResponseDto(PaperResponse jurorPaperResponse,
                                             JurorPaperResponseDto jurorPaperResponseDto) {
        Assertions.assertThat(jurorPaperResponse.getJurorNumber())
            .as("Expect Juror Record to updated with data mapped from the request DTO")
            .isEqualTo(jurorPaperResponseDto.getJurorNumber());

        Assertions.assertThat(jurorPaperResponse.getTitle())
            .as("Expect Juror Record to updated with data mapped from the request DTO")
            .isEqualTo(jurorPaperResponseDto.getTitle());
        Assertions.assertThat(jurorPaperResponse.getFirstName())
            .as("Expect Juror Record to updated with data mapped from the request DTO")
            .isEqualTo(jurorPaperResponseDto.getFirstName());
        Assertions.assertThat(jurorPaperResponse.getLastName())
            .as("Expect Juror Record to updated with data mapped from the request DTO")
            .isEqualTo(jurorPaperResponseDto.getLastName());
        Assertions.assertThat(jurorPaperResponse.getDateOfBirth())
            .as("Expect Juror Record to updated with data mapped from the request DTO")
            .isEqualTo(jurorPaperResponseDto.getDateOfBirth());

        Assertions.assertThat(jurorPaperResponse.getPhoneNumber())
            .as("Expect Juror Record to updated with data mapped from the request DTO")
            .isEqualTo(jurorPaperResponseDto.getPrimaryPhone());
        Assertions.assertThat(jurorPaperResponse.getAltPhoneNumber())
            .as("Expect Juror Record to updated with data mapped from the request DTO")
            .isEqualTo(jurorPaperResponseDto.getSecondaryPhone());
        Assertions.assertThat(jurorPaperResponse.getEmail())
            .as("Expect Juror Record to updated with data mapped from the request DTO")
            .isEqualTo(jurorPaperResponseDto.getEmailAddress());

        Assertions.assertThat(jurorPaperResponse.getAddressLine1())
            .as("Expect Juror Record to updated with data mapped from the request DTO")
            .isEqualTo(jurorPaperResponseDto.getAddressLineOne());
        Assertions.assertThat(jurorPaperResponse.getAddressLine2())
            .as("Expect Juror Record to updated with data mapped from the request DTO")
            .isEqualTo(jurorPaperResponseDto.getAddressLineTwo());
        Assertions.assertThat(jurorPaperResponse.getAddressLine3())
            .as("Expect Juror Record to updated with data mapped from the request DTO")
            .isEqualTo(jurorPaperResponseDto.getAddressLineThree());
        Assertions.assertThat(jurorPaperResponse.getAddressLine4())
            .as("Expect Juror Record to updated with data mapped from the request DTO")
            .isEqualTo(jurorPaperResponseDto.getAddressTown());
        Assertions.assertThat(jurorPaperResponse.getAddressLine5())
            .as("Expect Juror Record to updated with data mapped from the request DTO")
            .isEqualTo(jurorPaperResponseDto.getAddressCounty());
        Assertions.assertThat(jurorPaperResponse.getPostcode())
            .as("Expect Juror Record to updated with data mapped from the request DTO")
            .isEqualTo(jurorPaperResponseDto.getAddressPostcode());

        Assertions.assertThat(jurorPaperResponse.getWelsh())
            .as("Expect Juror Record to updated with data mapped from the request DTO")
            .isEqualTo(jurorPaperResponseDto.getWelsh());

        Assertions.assertThat(jurorPaperResponse.getSigned())
            .as("Expect Juror Record to updated with data mapped from the request DTO")
            .isEqualTo(jurorPaperResponseDto.getSigned());
    }

    private void setAddressDetails(JurorPaperResponseDto jurorPaperResponseDto) {
        jurorPaperResponseDto.setAddressLineOne("Address Line 1");
        jurorPaperResponseDto.setAddressLineTwo("Address Line 2");
        jurorPaperResponseDto.setAddressLineThree("Address Line 3");
        jurorPaperResponseDto.setAddressTown("Some Town");
        jurorPaperResponseDto.setAddressCounty("Some County");
        jurorPaperResponseDto.setAddressPostcode("CH1 2AN");
    }

    private JurorPaperResponseDto.CjsEmployment buildCjsEmployment(String employerName) {
        return JurorPaperResponseDto.CjsEmployment.builder()
            .cjsEmployer(employerName)
            .cjsEmployerDetails("Some test details")
            .build();
    }

    private JurorPaperResponseDto.ReasonableAdjustment buildSpecialNeeds(String assistanceType) {
        return JurorPaperResponseDto.ReasonableAdjustment.builder()
            .assistanceType(assistanceType)
            .assistanceTypeDetails("Some test details")
            .build();
    }

    private void setEligibilityDetails(JurorPaperResponseDto jurorPaperResponseDto) {
        JurorPaperResponseDto.Eligibility eligibility = JurorPaperResponseDto.Eligibility.builder()
            .livedConsecutive(true)
            .mentalHealthAct(false)
            .mentalHealthCapacity(false)
            .onBail(false)
            .convicted(false)
            .build();

        jurorPaperResponseDto.setEligibility(eligibility);
    }

    private void setEligibilityDetailsIneligible(JurorPaperResponseDto jurorPaperResponseDto) {
        JurorPaperResponseDto.Eligibility eligibility = JurorPaperResponseDto.Eligibility.builder()
            .livedConsecutive(false)
            .livedConsecutiveDetails("Lived in the UK for less than 5 years")
            .mentalHealthAct(true)
            .mentalHealthActDetails("I am detained under the Mental Health Act [MENTAL HEALTH Q2] "
                + "I have mental health capacity issues")
            .mentalHealthCapacity(true)
            .onBail(true)
            .onBailDetails("I am on bail for a criminal offence")
            .convicted(true)
            .convictedDetails("I have a conviction for a criminal offence")
            .build();

        jurorPaperResponseDto.setEligibility(eligibility);
    }

    private void setThirdPartyDetails(JurorPaperResponseDto jurorPaperResponseDto) {
        JurorPaperResponseDto.ThirdParty thirdParty = JurorPaperResponseDto.ThirdParty.builder()
            .relationship("Spouse")
            .thirdPartyReason("Some test reason")
            .build();

        jurorPaperResponseDto.setThirdParty(thirdParty);
    }

    private JurorPool createTestJurorPool(String owner, String jurorNumber) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("415");
        courtLocation.setName("Chester");

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setCourtLocation(courtLocation);
        poolRequest.setPoolNumber("415230101");
        poolRequest.setReturnDate(LocalDate.now().plusWeeks(3));

        Juror juror = new Juror();
        juror.setTitle("Dr");
        juror.setFirstName("Test");
        juror.setLastName("Person");
        juror.setAddressLine1("Address Line One");
        juror.setAddressLine2("Address Line Two");
        juror.setAddressLine3("Address Line Three");
        juror.setAddressLine4("Town");
        juror.setAddressLine5("County");
        juror.setPostcode("PO19 1SX");
        juror.setJurorNumber(jurorNumber);

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(IJurorStatus.RESPONDED);
        jurorStatus.setStatusDesc("Responded");

        JurorPool jurorPool = new JurorPool();
        jurorPool.setPool(poolRequest);
        jurorPool.setOwner(owner);
        jurorPool.setStatus(jurorStatus);

        juror.setAssociatedPools(Set.of(jurorPool));
        jurorPool.setJuror(juror);

        return jurorPool;
    }

    private PoolRequest mockPoolRequest(String poolNumber, String owner) {
        PoolRequest mockPoolRequest = new PoolRequest();
        mockPoolRequest.setPoolNumber(poolNumber);
        mockPoolRequest.setOwner(owner);
        mockPoolRequest.setReturnDate(LocalDate.now().plusWeeks(3));
        return mockPoolRequest;
    }

    private PaperResponse createTestJurorResponse(String jurorNumber) {
        PaperResponse response = Mockito.spy(new PaperResponse());
        response.setJurorNumber(jurorNumber);
        response.setDateReceived(LocalDateTime.now());

        response.setFirstName("FName");
        response.setLastName("LName");
        response.setDateOfBirth(LocalDate.now().minusYears(25));

        response.setRelationship("Spouse");
        response.setThirdPartyReason("Some test reason");

        response.setAddressLine1("Address Line 1");
        response.setAddressLine2("Address Line 2");
        response.setAddressLine3("Address Line 3");
        response.setAddressLine4("Address Town");
        response.setAddressLine5("Address County");
        response.setPostcode("CH1 2AN");

        response.setResidency(true);
        response.setMentalHealthAct(false);
        response.setMentalHealthCapacity(false);
        response.setBail(false);
        response.setConvictions(false);

        JurorResponseCjsEmployment cjsEmployment = new JurorResponseCjsEmployment();
        cjsEmployment.setJurorNumber(jurorNumber);
        cjsEmployment.setCjsEmployer("Test Employer");
        cjsEmployment.setCjsEmployerDetails("Some test details");

        Mockito.doReturn(Collections.singletonList(cjsEmployment)).when(response).getCjsEmployments();

        ReasonableAdjustments reasonableAdjustment = new ReasonableAdjustments();
        reasonableAdjustment.setCode("V");
        reasonableAdjustment.setDescription("Some test description");

        JurorReasonableAdjustment adjustment = new JurorReasonableAdjustment();
        adjustment.setJurorNumber(jurorNumber);
        adjustment.setReasonableAdjustment(reasonableAdjustment);
        adjustment.setReasonableAdjustmentDetail("Some test details");

        Mockito.doReturn(Collections.singletonList(adjustment)).when(response).getReasonableAdjustments();

        response.setSigned(true);
        response.setProcessingStatus(jurorResponseAuditRepository, ProcessingStatus.TODO);

        response.setStaff(User.builder().username("SOME_USER").build());

        return response;
    }

    private void verifyPaperResponseIsCopiedToDto(PaperResponse jurorPaperResponse,
                                                  JurorPaperResponseDetailDto responseDto) {
        Assertions.assertThat(responseDto.getJurorNumber()).isEqualTo(jurorPaperResponse.getJurorNumber());
        Assertions.assertThat(responseDto.getDateReceived())
            .isEqualTo(jurorPaperResponse.getDateReceived().toLocalDate());

        Assertions.assertThat(responseDto.getFirstName()).isEqualTo(jurorPaperResponse.getFirstName());
        Assertions.assertThat(responseDto.getLastName()).isEqualTo(jurorPaperResponse.getLastName());
        Assertions.assertThat(responseDto.getDateOfBirth()).isEqualTo(jurorPaperResponse.getDateOfBirth());

        Assertions.assertThat(responseDto.getAddressLineOne()).isEqualTo(jurorPaperResponse.getAddressLine1());
        Assertions.assertThat(responseDto.getAddressLineTwo()).isEqualTo(jurorPaperResponse.getAddressLine2());
        Assertions.assertThat(responseDto.getAddressLineThree()).isEqualTo(jurorPaperResponse.getAddressLine3());
        Assertions.assertThat(responseDto.getAddressTown()).isEqualTo(jurorPaperResponse.getAddressLine4());
        Assertions.assertThat(responseDto.getAddressCounty()).isEqualTo(jurorPaperResponse.getAddressLine5());
        Assertions.assertThat(responseDto.getAddressPostcode()).isEqualTo(jurorPaperResponse.getPostcode());

        JurorPaperResponseDetailDto.ThirdParty thirdParty = responseDto.getThirdParty();
        Assertions.assertThat(thirdParty.getRelationship()).isEqualTo(jurorPaperResponse.getRelationship());
        Assertions.assertThat(thirdParty.getThirdPartyReason()).isEqualTo(jurorPaperResponse.getThirdPartyReason());

        JurorPaperResponseDetailDto.Eligibility eligibility = responseDto.getEligibility();
        Assertions.assertThat(eligibility.getLivedConsecutive()).isEqualTo(jurorPaperResponse.getResidency());
        Assertions.assertThat(eligibility.getMentalHealthAct()).isEqualTo(jurorPaperResponse.getMentalHealthAct());
        Assertions.assertThat(eligibility.getMentalHealthCapacity())
            .isEqualTo(jurorPaperResponse.getMentalHealthCapacity());
        Assertions.assertThat(eligibility.getOnBail()).isEqualTo(jurorPaperResponse.getBail());
        Assertions.assertThat(eligibility.getConvicted()).isEqualTo(jurorPaperResponse.getConvictions());

        JurorPaperResponseDetailDto.CjsEmployment actualCjsEmployment = responseDto.getCjsEmployment().get(0);
        JurorResponseCjsEmployment expectedCjsEmployment = jurorPaperResponse.getCjsEmployments().get(0);
        Assertions.assertThat(actualCjsEmployment.getCjsEmployer()).isEqualTo(expectedCjsEmployment.getCjsEmployer());
        Assertions.assertThat(actualCjsEmployment.getCjsEmployerDetails())
            .isEqualTo(expectedCjsEmployment.getCjsEmployerDetails());

        JurorPaperResponseDetailDto.ReasonableAdjustment actualReasonableAdjustment
            = responseDto.getReasonableAdjustments().get(0);
        JurorReasonableAdjustment expectedSpecialNeeds = jurorPaperResponse.getReasonableAdjustments().get(0);
        Assertions.assertThat(actualReasonableAdjustment.getAssistanceType())
            .isEqualTo(expectedSpecialNeeds.getReasonableAdjustment().getCode());
        Assertions.assertThat(actualReasonableAdjustment.getAssistanceTypeDetails())
            .isEqualTo(expectedSpecialNeeds.getReasonableAdjustmentDetail());

        Assertions.assertThat(responseDto.getSigned()).isEqualTo(jurorPaperResponse.getSigned());
        Assertions.assertThat(responseDto.getProcessingStatus())
            .isEqualTo(jurorPaperResponse.getProcessingStatus().getDescription());

        JurorPool existingJurorPoolRecord = createTestJurorPool("400", responseDto.getJurorNumber());
        Juror existingJurorRecord = existingJurorPoolRecord.getJuror();

        Assertions.assertThat(responseDto.getExistingTitle()).isEqualTo(existingJurorRecord.getTitle());
        Assertions.assertThat(responseDto.getExistingFirstName()).isEqualTo(existingJurorRecord.getFirstName());
        Assertions.assertThat(responseDto.getExistingLastName()).isEqualTo(existingJurorRecord.getLastName());
        Assertions.assertThat(responseDto.getExistingAddressLineOne()).isEqualTo(existingJurorRecord.getAddressLine1());
        Assertions.assertThat(responseDto.getExistingAddressLineTwo()).isEqualTo(existingJurorRecord.getAddressLine2());
        Assertions.assertThat(responseDto.getExistingAddressLineThree())
            .isEqualTo(existingJurorRecord.getAddressLine3());
        Assertions.assertThat(responseDto.getExistingAddressTown()).isEqualTo(existingJurorRecord.getAddressLine4());
        Assertions.assertThat(responseDto.getExistingAddressCounty()).isEqualTo(existingJurorRecord.getAddressLine5());
        Assertions.assertThat(responseDto.getExistingAddressPostcode()).isEqualTo(existingJurorRecord.getPostcode());
    }

    private void verifyPoolDetailsMapping(SummonsSnapshot summonsSnapshot, JurorPaperResponseDetailDto responseDto) {
        Assertions.assertThat(responseDto.getPoolNumber())
            .as("Pool number should be mapped from summons snapshot")
            .isEqualTo(summonsSnapshot.getPoolNumber());
        Assertions.assertThat(responseDto.getCourtName())
            .as("Court name should be mapped from summons snapshot")
            .isEqualTo(summonsSnapshot.getCourtLocationName());
        Assertions.assertThat(responseDto.getServiceStartDate())
            .as("Service Start Date should be mapped from summons snapshot")
            .isEqualTo(summonsSnapshot.getServiceStartDate());
    }
}
