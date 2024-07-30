package uk.gov.hmcts.juror.api.moj.service;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeRepository;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.controller.request.ExcusalDecisionDto;
import uk.gov.hmcts.juror.api.moj.domain.ExcusalCode;
import uk.gov.hmcts.juror.api.moj.domain.ExcusalDecision;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.ExcusalCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.exception.ExcusalResponseException;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
@RunWith(SpringRunner.class)
public class ExcusalResponseServiceImplTest {

    private static final String JUROR_NUMBER = "123456789";
    private static final String JUROR_NUMBER2 = "987654321";
    @Mock
    private ExcusalCodeRepository excusalCodeRepository;
    @Mock
    private JurorRepository jurorRepository;
    @Mock
    private JurorHistoryService jurorHistoryService;
    @Mock
    private JurorPoolRepository jurorPoolRepository;
    @Mock
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    @Mock
    private JurorDigitalResponseRepositoryMod jurorResponseRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SummonsReplyMergeService mergeService;
    @Mock
    private JurorStatusRepository jurorStatusRepository;
    @Mock
    private JurorHistoryRepository jurorHistoryRepository;
    @Mock
    private PrintDataService printDataService;
    @Mock
    private JurorResponseAuditRepositoryMod jurorResponseAuditRepository;
    @Mock
    private JurorPoolService jurorPoolService;

    @InjectMocks
    private ExcusalResponseServiceImpl excusalResponseService;

    @Before
    public void setUpMocks() {
        List<ExcusalCode> excusalCodes = new ArrayList<>();
        ExcusalCode excusalCodeEntity = new ExcusalCode("A", "MOVED FROM AREA", false, true, false, false);
        excusalCodes.add(excusalCodeEntity);
        ExcusalCode excusalCodeEntity1 = new ExcusalCode("D", "DECEASED", false, true, false, false);
        excusalCodes.add(excusalCodeEntity1);
        Mockito.when(excusalCodeRepository.findAll()).thenReturn(excusalCodes);

        JurorPool jurorPool400 = createTestJurorPool("400", "123456789");
        Mockito.doReturn(jurorPool400)
                .when(jurorPoolService).getJurorPoolFromUser("123456789");
        Mockito.doReturn(Collections.singletonList(jurorPool400))
            .when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc("123456789", true);
        JurorPool jurorPool415 = createTestJurorPool("415", "987654321");
        Mockito.doReturn(jurorPool415)
            .when(jurorPoolService).getJurorPoolFromUser("987654321");
        Mockito.doReturn(Collections.singletonList(jurorPool415))
            .when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc("987654321", true);

        Mockito.doNothing().when(mergeService).mergePaperResponse(any(), any());

        Mockito.doReturn(Optional.of(createJurorStatus(1))).when(jurorStatusRepository).findById(1);
        Mockito.doReturn(Optional.of(createJurorStatus(2))).when(jurorStatusRepository).findById(2);
        Mockito.doReturn(Optional.of(createJurorStatus(5))).when(jurorStatusRepository).findById(5);

        Mockito.doNothing().when(printDataService).printExcusalDeniedLetter(any());

        Mockito.doReturn(null).when(jurorHistoryRepository).save(any());
        Mockito.doReturn(null).when(jurorPaperResponseRepository).save(any());
        Mockito.doReturn(null).when(jurorResponseRepository).save(any());
    }

    @AfterEach
    void afterEach(){
        TestUtils.afterAll();
    }

    @Test
    public void test_refuseExcusalRequest_happyPath_paperResponse_bureauUser_bureauOwner() {
        TestUtils.mockBureauUser();
        String jurorNumber = "123456789";
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();

        PaperResponse jurorPaperResponse = createTestJurorPaperResponse(jurorNumber);
        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyPaperPath(payload, jurorNumber);
        verifyHappyRefuseJurorPoolPath(2,true);
        verifyHappyExcusalDeniedLetter();
    }

    @Test
    public void test_refuseExcusalRequest_happyPath_paperResponse_courtUser_courtOwner() {
        String jurorNumber = "987654321";
        TestUtils.mockCourtUser("415");
        BureauJwtPayload courtPayload = TestUtils.createJwt("415", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();

        PaperResponse jurorPaperResponse = createTestJurorPaperResponse(jurorNumber);
        jurorPaperResponse.setStaff(null);
        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        excusalResponseService.respondToExcusalRequest(courtPayload, excusalDecisionDto, jurorNumber);

        verify(jurorPoolService, times(1))
            .getJurorPoolFromUser(jurorNumber);

        verifyHappyPaperPath(courtPayload, jurorNumber);
        verifyHappyRefuseJurorPoolPath(2,false);

        Mockito.verify(printDataService, Mockito.times(0))
            .printExcusalDeniedLetter(any());
    }

    @Test
    public void test_grantExcusalRequest_happyPath_paperResponse_bureauUser_bureauOwner() {

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);

        PaperResponse jurorPaperResponse = createTestJurorPaperResponse(JUROR_NUMBER);
        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository).findByJurorNumber(JUROR_NUMBER);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("400");
        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_NUMBER);
        jurorPool.setJuror(new Juror());
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("987654321");
        jurorPool.setPool(new PoolRequest());

        Mockito.doReturn(List.of(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_NUMBER, true);

        TestUtils.mockBureauUser();
        final BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");
        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, JUROR_NUMBER);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyPaperPath(payload, JUROR_NUMBER);
        verifyHappyGrantJurorPoolPath();
        verifyHappyExcusalLetter(jurorPool, excusalDecisionDto);
    }

    @Test
    public void test_grantExcusalRequest_happyPath_paperResponse_courtUser_courtOwner() {
        final BureauJwtPayload courtPayload = TestUtils.createJwt("415", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);

        PaperResponse jurorPaperResponse = createTestJurorPaperResponse(JUROR_NUMBER2);
        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository).findByJurorNumber(JUROR_NUMBER2);

        excusalResponseService.respondToExcusalRequest(courtPayload, excusalDecisionDto, JUROR_NUMBER2);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyPaperPath(courtPayload, JUROR_NUMBER2);
        verifyHappyGrantJurorPoolPathNoLetter(); // court users don't automatically send letters
    }

    @Test
    public void test_refuseExcusalRequest_happyPath_digitalResponse_bureauUser_bureauOwner() {
        TestUtils.mockBureauUser();
        final BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setReplyMethod(ReplyMethod.DIGITAL);

        DigitalResponse jurorResponse = createTestJurorDigitalResponse(JUROR_NUMBER);
        Mockito.doReturn(jurorResponse).when(jurorResponseRepository).findByJurorNumber(JUROR_NUMBER);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, JUROR_NUMBER);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyDigitalPath(payload, JUROR_NUMBER);
        verifyHappyRefuseJurorPoolPath(2,true);
        verifyHappyExcusalDeniedLetter();
    }

    @Test
    public void test_refuseExcusalRequest_happyPath_digitalResponse_courtUser_courtOwner() {
        final BureauJwtPayload courtPayload = TestUtils.createJwt("415", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setReplyMethod(ReplyMethod.DIGITAL);

        DigitalResponse jurorResponse = createTestJurorDigitalResponse(JUROR_NUMBER2);
        Mockito.doReturn(jurorResponse).when(jurorResponseRepository).findByJurorNumber(JUROR_NUMBER2);

        excusalResponseService.respondToExcusalRequest(courtPayload, excusalDecisionDto, JUROR_NUMBER2);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyDigitalPath(courtPayload, JUROR_NUMBER2);
        verifyHappyRefuseJurorPoolPath(2,false);
        Mockito.verify(printDataService, Mockito.times(0))
            .printExcusalDeniedLetter(any());
    }

    @Test
    public void test_grantExcusalRequest_happyPath_digitalResponse_bureauUser_bureauOwner() {
        TestUtils.mockBureauUser();
        final BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setReplyMethod(ReplyMethod.DIGITAL);
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);

        DigitalResponse jurorResponse = createTestJurorDigitalResponse(JUROR_NUMBER);
        Mockito.doReturn(jurorResponse).when(jurorResponseRepository).findByJurorNumber(JUROR_NUMBER);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("400");
        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_NUMBER);
        jurorPool.setJuror(new Juror());
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("987654321");
        jurorPool.setPool(new PoolRequest());

        Mockito.doReturn(List.of(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_NUMBER, true);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, JUROR_NUMBER);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyDigitalPath(payload, JUROR_NUMBER);
        verifyHappyGrantJurorPoolPath();
        verifyHappyExcusalLetter(jurorPool, excusalDecisionDto);
    }

    @Test
    public void test_grantExcusalRequest_happyPath_digitalResponse_courtUser_courtOwner() {
        final BureauJwtPayload courtPayload = TestUtils.createJwt("415", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setReplyMethod(ReplyMethod.DIGITAL);
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);

        DigitalResponse jurorResponse = createTestJurorDigitalResponse(JUROR_NUMBER2);
        Mockito.doReturn(jurorResponse).when(jurorResponseRepository).findByJurorNumber(JUROR_NUMBER2);

        excusalResponseService.respondToExcusalRequest(courtPayload, excusalDecisionDto, JUROR_NUMBER2);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyDigitalPath(courtPayload, JUROR_NUMBER2);
        verifyHappyGrantJurorPoolPathNoLetter(); // court users don't automatically send letters
    }

    @Test
    public void test_grantExcusalRequest_happyPath_excusalCodeDeceased() {
        final BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);
        excusalDecisionDto.setExcusalReasonCode(ExcusalCodeEnum.D.getCode());

        PaperResponse jurorPaperResponse = createTestJurorPaperResponse(JUROR_NUMBER);
        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository).findByJurorNumber(JUROR_NUMBER);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("400");
        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_NUMBER);
        jurorPool.setJuror(new Juror());
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("987654321");
        jurorPool.setPool(new PoolRequest());

        Mockito.doReturn(List.of(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_NUMBER, true);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, JUROR_NUMBER);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        Mockito.verify(jurorPaperResponseRepository, Mockito.times(2)).findByJurorNumber(JUROR_NUMBER);
        Mockito.verify(jurorResponseRepository, Mockito.never()).findById(JUROR_NUMBER);
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(payload.getLogin());
        Mockito.verify(mergeService, Mockito.times(1))
            .mergePaperResponse(any(), any());
        Mockito.verify(mergeService, Mockito.never()).mergeDigitalResponse(any(), any());
        verifyHappyGrantJurorPoolPathNoLetter(); // deceased jurors don't get letters
        verifyHappyExcusalLetter(jurorPool, excusalDecisionDto);
    }

    @Test
    public void test_excusalRequest_paperResponse_bureauUser_courtOwner() {
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();

        PaperResponse jurorPaperResponse = createTestJurorPaperResponse(JUROR_NUMBER2);
        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository).findByJurorNumber(JUROR_NUMBER2);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto,
                JUROR_NUMBER2));

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyFailedInitialChecksPath(payload, JUROR_NUMBER2);
    }

    @Test
    public void test_excusalRequest_paperResponse_courtUser_bureauOwner() {
        BureauJwtPayload courtPayload = TestUtils.createJwt("415", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();

        PaperResponse jurorPaperResponse = createTestJurorPaperResponse(JUROR_NUMBER);
        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository).findByJurorNumber(JUROR_NUMBER);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> excusalResponseService.respondToExcusalRequest(courtPayload, excusalDecisionDto,
                JUROR_NUMBER));

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyFailedInitialChecksPath(courtPayload, JUROR_NUMBER);
    }

    @Test
    public void test_excusalRequest_paperResponse_alreadyClosed() {
        final ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();

        PaperResponse jurorPaperResponse = createTestJurorPaperResponse(JUROR_NUMBER);
        jurorPaperResponse.setProcessingStatus(jurorResponseAuditRepository, ProcessingStatus.CLOSED);
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);

        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository).findByJurorNumber(JUROR_NUMBER);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("400");
        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_NUMBER);
        jurorPool.setJuror(new Juror());
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("987654321");
        jurorPool.setPool(new PoolRequest());

        Mockito.doReturn(List.of(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_NUMBER, true);

        TestUtils.mockBureauUser();
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");
        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, JUROR_NUMBER);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        Mockito.verify(jurorPaperResponseRepository, Mockito.times(2)).findByJurorNumber(JUROR_NUMBER);
        Mockito.verify(jurorResponseRepository, Mockito.never()).findByJurorNumber(JUROR_NUMBER);
        verifyHappyExcusalLetter(jurorPool, excusalDecisionDto);
    }

    @Test
    public void test_excusalRequest_digitalResponse_alreadyClosed() {
        final BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setReplyMethod(ReplyMethod.DIGITAL);
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);

        DigitalResponse jurorResponse = createTestJurorDigitalResponse(JUROR_NUMBER);
        jurorResponse.setProcessingStatus(jurorResponseAuditRepository, ProcessingStatus.CLOSED);
        Mockito.doReturn(jurorResponse).when(jurorResponseRepository).findByJurorNumber(JUROR_NUMBER);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("400");
        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_NUMBER);
        jurorPool.setJuror(new Juror());
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("987654321");
        jurorPool.setPool(new PoolRequest());

        Mockito.doReturn(List.of(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_NUMBER, true);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, JUROR_NUMBER);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        Mockito.verify(jurorPaperResponseRepository, Mockito.never()).findById(JUROR_NUMBER);
        Mockito.verify(jurorResponseRepository, Mockito.times(1)).findByJurorNumber(JUROR_NUMBER);
        verifyHappyExcusalLetter(jurorPool, excusalDecisionDto);
    }

    @Test
    public void test_excusalRequest_invalidExcusalCode() {
        final BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setExcusalReasonCode("E");

        Assertions.assertThatExceptionOfType(ExcusalResponseException.InvalidExcusalCode.class)
            .isThrownBy(() -> excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto,
                JUROR_NUMBER));

        Mockito.verify(jurorPoolRepository, Mockito.never())
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyFailedInitialChecksPath(payload, JUROR_NUMBER);
    }

    @Test
    public void test_excusalRequest_unableToRetrieveValidExcusalCodeList() {
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        List<ExcusalCode> emptyList = Collections.emptyList();
        Mockito.when(excusalCodeRepository.findAll()).thenReturn(emptyList);

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();

        Assertions.assertThatExceptionOfType(ExcusalResponseException.UnableToRetrieveExcusalCodeList.class)
            .isThrownBy(() -> excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto,
                JUROR_NUMBER));

        Mockito.verify(jurorPoolRepository, Mockito.never())
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyFailedInitialChecksPath(payload, JUROR_NUMBER);
    }

    @Test
    public void test_excusalRequest_noJurorRecord() {
        String jurorNumber = "111111111";
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();

        Assertions.assertThatExceptionOfType(MojException.NotFound.class)
            .isThrownBy(() -> excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber));

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyFailedInitialChecksPath(payload, jurorNumber);
    }

    @Test
    public void test_excusalRequest_paperResponseDoesNotExist() {
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setExcusalReasonCode("A");

        Mockito.doReturn(null).when(jurorPaperResponseRepository).findByJurorNumber(JUROR_NUMBER);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, JUROR_NUMBER);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1))
            .findByJurorNumber(JUROR_NUMBER);
        Mockito.verify(jurorResponseRepository, Mockito.never()).findByJurorNumber(JUROR_NUMBER);

        verifyFailedAtResponseStatusPath(payload);
    }

    @Test
    public void test_excusalRequest_withoutResponse_grant_bureauUser_bureauOwner() {
        TestUtils.mockBureauUser();
        final BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        final ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequestNoResponse(ExcusalDecision.GRANT);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("400");
        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_NUMBER);
        jurorPool.setJuror(new Juror());
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("987654321");
        jurorPool.setPool(new PoolRequest());

        Mockito.doReturn(List.of(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_NUMBER, true);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, JUROR_NUMBER);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyGrantJurorPoolPath();
        verifyHappyExcusalLetter(jurorPool, excusalDecisionDto);
    }

    @Test
    public void test_excusalRequest_withoutResponse_refuse_bureauUser_bureauOwner() {
        TestUtils.mockBureauUser();
        final BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        final ExcusalDecisionDto excusalDecisionDto =
            createTestExcusalDecisionRequestNoResponse(ExcusalDecision.REFUSE);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, JUROR_NUMBER);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyRefuseJurorPoolPath(2,true);
        verifyHappyExcusalDeniedLetter();
    }

    @Test
    public void test_excusalRequest_withoutResponse_grant_courtUser_courtOwner() {
        final BureauJwtPayload payload = TestUtils.createJwt("415", "SOME_USER");

        final ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequestNoResponse(ExcusalDecision.GRANT);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("415");
        Juror juror = new Juror();
        juror.setJurorNumber(JUROR_NUMBER);
        jurorPool.setJuror(new Juror());
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("987654321");
        jurorPool.setPool(new PoolRequest());

        Mockito.doReturn(List.of(jurorPool)).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(JUROR_NUMBER, true);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, JUROR_NUMBER);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyGrantJurorPoolPathNoLetter(); // court users don't automatically send letters
    }

    @Test
    public void test_excusalRequest_withoutResponse_refuse_courtUser_courtOwner() {
        final BureauJwtPayload payload = TestUtils.createJwt("415", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequestNoResponse(ExcusalDecision.REFUSE);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, JUROR_NUMBER2);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyRefuseJurorPoolPath(2,false);
        Mockito.verify(printDataService, Mockito.times(0))
            .printExcusalDeniedLetter(any());
    }

    @Test
    public void test_excusalRequest_withoutResponse_grant_bureauUser_courtOwner() {
        final BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequestNoResponse(ExcusalDecision.GRANT);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> {
                excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, JUROR_NUMBER2);
            });

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyFailedInitialChecksPath(payload, JUROR_NUMBER2);
    }

    @Test
    public void test_excusalRequest_withoutResponse_grant_courtUser_bureauOwner() {
        final BureauJwtPayload payload = TestUtils.createJwt("415", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequestNoResponse(ExcusalDecision.GRANT);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> {
                excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, JUROR_NUMBER);
            });

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyFailedInitialChecksPath(payload, JUROR_NUMBER);
    }

    private void verifyHappyRefuseJurorPoolPath(int jurorHistoryRepositoryTimes, boolean shouldCreateNonExcusedLetter) {
        Mockito.verify(jurorPoolRepository, Mockito.times(1)).save(any());
        Mockito.verify(jurorHistoryRepository, Mockito.times(jurorHistoryRepositoryTimes)).save(any());
        if (shouldCreateNonExcusedLetter) {
            Mockito.verify(jurorHistoryService).createNonExcusedLetterHistory(any(), eq("Refused Excusal"));
        } else {
            Mockito.verify(jurorHistoryService, Mockito.never()).createNonExcusedLetterHistory(any(), any());
        }
    }

    private void verifyHappyGrantJurorPoolPath() {
        Mockito.verify(jurorPoolRepository, Mockito.times(1)).save(any());
        Mockito.verify(jurorHistoryRepository, Mockito.times(1)).save(any());
        Mockito.verify(jurorHistoryService).createExcusedLetter(any());
    }

    private void verifyHappyGrantJurorPoolPathNoLetter() {
        Mockito.verify(jurorPoolRepository, Mockito.times(1)).save(any());
        Mockito.verify(jurorHistoryRepository, Mockito.times(1)).save(any());
        Mockito.verify(printDataService, Mockito.never()).printExcusalLetter(Mockito.any());
    }

    private void verifyHappyPaperPath(BureauJwtPayload payload, String jurorNumber) {
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(2)).findByJurorNumber(jurorNumber);
        Mockito.verify(jurorResponseRepository, Mockito.never()).findById(jurorNumber);
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(payload.getLogin());
        Mockito.verify(mergeService, Mockito.times(1))
            .mergePaperResponse(any(), any());
        Mockito.verify(mergeService, Mockito.never()).mergeDigitalResponse(any(), any());
    }

    private void verifyHappyDigitalPath(BureauJwtPayload payload, String jurorNumber) {
        Mockito.verify(jurorPaperResponseRepository, Mockito.never()).findById(jurorNumber);
        Mockito.verify(jurorResponseRepository, Mockito.times(1)).findByJurorNumber(jurorNumber);
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(payload.getLogin());
        Mockito.verify(mergeService, Mockito.never()).mergePaperResponse(any(), any());
        Mockito.verify(mergeService, Mockito.times(1)).mergeDigitalResponse(any(), any());
    }

    private void verifyHappyExcusalDeniedLetter() {
        Mockito.verify(printDataService, Mockito.times(1))
            .printExcusalDeniedLetter(any());
        // Negative check for excusal letter
        //Mockito.verify(printDataService, Mockito.never())
        //.getLetterToEnqueue(payload.getOwner(), jurorNumber);
        //Mockito.verify(excusalLetterService, Mockito.never()).enqueueLetter(any());
    }

    private void verifyHappyExcusalLetter(JurorPool jurorPool,
                                          ExcusalDecisionDto excusalDecisionDto) {
        if (ExcusalCodeEnum.D.getCode().equals(excusalDecisionDto.getExcusalReasonCode())) {
            Mockito.verify(printDataService, Mockito.never())
                .printExcusalLetter(jurorPool);
        } else {
            Mockito.verify(printDataService, Mockito.times(1))
                .printExcusalLetter(jurorPool);
        }
        Mockito.verify(printDataService, Mockito.never())
            .printExcusalDeniedLetter(any());
    }

    private void verifyFailedAtResponseStatusPath(BureauJwtPayload payload) {
        Mockito.verify(userRepository, Mockito.never()).findByUsername(payload.getLogin());
        Mockito.verify(mergeService, Mockito.never()).mergePaperResponse(any(), any());
        Mockito.verify(mergeService, Mockito.never()).mergeDigitalResponse(any(), any());
    }

    private void verifyFailedInitialChecksPath(BureauJwtPayload payload, String jurorNumber) {
        Mockito.verify(jurorPaperResponseRepository, Mockito.never()).findById(jurorNumber);
        Mockito.verify(jurorResponseRepository, Mockito.never()).findByJurorNumber(jurorNumber);
        Mockito.verify(userRepository, Mockito.never()).findByUsername(payload.getLogin());
        Mockito.verify(mergeService, Mockito.never()).mergePaperResponse(any(), any());
        Mockito.verify(mergeService, Mockito.never()).mergeDigitalResponse(any(), any());
        Mockito.verify(jurorPoolRepository, Mockito.never()).save(any());
        Mockito.verify(jurorHistoryRepository, Mockito.never()).save(any());
        Mockito.verify(printDataService, Mockito.never()).printExcusalDeniedLetter(any());
    }

    private ExcusalDecisionDto createTestExcusalDecisionRequest() {
        ExcusalDecisionDto excusalDecisionDto = new ExcusalDecisionDto();
        excusalDecisionDto.setExcusalReasonCode("A");
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.REFUSE);
        excusalDecisionDto.setReplyMethod(ReplyMethod.PAPER);
        return excusalDecisionDto;
    }

    private ExcusalDecisionDto createTestExcusalDecisionRequestNoResponse(ExcusalDecision decision) {
        ExcusalDecisionDto excusalDecisionDto = new ExcusalDecisionDto();
        excusalDecisionDto.setExcusalReasonCode("A");
        excusalDecisionDto.setExcusalDecision(decision);
        return excusalDecisionDto;
    }

    private JurorPool createTestJurorPool(String owner, String jurorNumber) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCourtName("CHESTER");
        courtLocation.setLocCode("415");

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("415230101");
        poolRequest.setCourtLocation(courtLocation);

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);
        juror.setFirstName("juror1");
        juror.setLastName("juror1L");
        juror.setPostcode("M24 4GT");

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(1);
        jurorStatus.setStatusDesc("Responded");
        jurorStatus.setActive(true);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(owner);
        jurorPool.setStatus(jurorStatus);
        jurorPool.setPool(poolRequest);

        juror.setAssociatedPools(Set.of(jurorPool));
        jurorPool.setJuror(juror);

        return jurorPool;
    }

    private JurorStatus createJurorStatus(int statusCode) {
        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(statusCode);
        return jurorStatus;
    }

    private PaperResponse createTestJurorPaperResponse(String jurorNumber) {
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

        response.setSigned(true);
        response.setProcessingStatus(jurorResponseAuditRepository, ProcessingStatus.TODO);

        return response;
    }

    private DigitalResponse createTestJurorDigitalResponse(String jurorNumber) {
        DigitalResponse response = new DigitalResponse();

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
        response.setBail(false);
        response.setConvictions(false);

        response.setProcessingStatus(jurorResponseAuditRepository, ProcessingStatus.TODO);

        return response;
    }
}
