package uk.gov.hmcts.juror.api.moj.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeRepository;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.ExcusalDecisionDto;
import uk.gov.hmcts.juror.api.moj.domain.ExcusalCode;
import uk.gov.hmcts.juror.api.moj.domain.ExcusalDecision;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.enumeration.ExcusalCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.exception.ExcusalResponseException;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;
import uk.gov.hmcts.juror.api.moj.service.summonsmanagement.JurorResponseService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
@ExtendWith(SpringExtension.class)
class ExcusalResponseServiceImplTest {

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
    private JurorStatusRepository jurorStatusRepository;
    @Mock
    private JurorHistoryRepository jurorHistoryRepository;
    @Mock
    private PrintDataService printDataService;
    @Mock
    private JurorPoolService jurorPoolService;
    @Mock
    private JurorResponseService jurorResponseService;

    @InjectMocks
    private ExcusalResponseServiceImpl excusalResponseService;

    @BeforeEach
    void setUpMocks() {
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

        Mockito.doReturn(Optional.of(createJurorStatus(1))).when(jurorStatusRepository).findById(1);
        Mockito.doReturn(Optional.of(createJurorStatus(2))).when(jurorStatusRepository).findById(2);
        Mockito.doReturn(Optional.of(createJurorStatus(5))).when(jurorStatusRepository).findById(5);

        Mockito.doNothing().when(printDataService).printExcusalDeniedLetter(any());

        Mockito.doReturn(null).when(jurorHistoryRepository).save(any());
    }

    @AfterEach
    void afterEach() {
        TestUtils.afterAll();
    }

    @Test
    void testRefuseExcusalRequestHappyPathPaperResponseBureauUserBureauOwner() {
        TestUtils.mockBureauUser();
        String jurorNumber = "123456789";
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber);

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verify(jurorResponseService, times(1)).setResponseProcessingStatusToClosed(jurorNumber);
        verifyHappyRefuseJurorPoolPath(2, true);
        verifyHappyExcusalDeniedLetter();
    }

    @Test
    void testRefuseExcusalRequestHappyPathPaperResponseCourtUserCourtOwner() {
        String jurorNumber = "987654321";
        TestUtils.mockCourtUser("415");
        BureauJwtPayload courtPayload = TestUtils.createJwt("415", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();

        excusalResponseService.respondToExcusalRequest(courtPayload, excusalDecisionDto, jurorNumber);

        verify(jurorPoolService, times(1))
            .getJurorPoolFromUser(jurorNumber);

        verify(jurorResponseService, times(1)).setResponseProcessingStatusToClosed(jurorNumber);
        verifyHappyRefuseJurorPoolPath(2, false);

        verify(printDataService, times(0))
            .printExcusalDeniedLetter(any());
    }

    @Test
    void testGrantExcusalRequestHappyPathPaperResponseBureauUserBureauOwner() {

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);

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

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verify(jurorResponseService, times(1)).setResponseProcessingStatusToClosed(JUROR_NUMBER);
        verifyHappyGrantJurorPoolPath();
        verifyHappyExcusalLetter(jurorPool, excusalDecisionDto);
    }

    @Test
    void testGrantExcusalRequestHappyPathPaperResponseCourtUserCourtOwner() {
        TestUtils.mockCourtUser("415");
        final BureauJwtPayload courtPayload = TestUtils.createJwt("415", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);

        excusalResponseService.respondToExcusalRequest(courtPayload, excusalDecisionDto, JUROR_NUMBER2);

        verify(jurorPoolService, times(1))
            .getJurorPoolFromUser(any());

        verify(jurorResponseService, times(1)).setResponseProcessingStatusToClosed(JUROR_NUMBER2);
        verifyHappyGrantJurorPoolPathNoLetter(); // court users don't automatically send letters
    }

    @Test
    void testRefuseExcusalRequestHappyPathDigitalResponseBureauUserBureauOwner() {
        TestUtils.mockBureauUser();
        final BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setReplyMethod(ReplyMethod.DIGITAL);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, JUROR_NUMBER);

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verify(jurorResponseService, times(1)).setResponseProcessingStatusToClosed(JUROR_NUMBER);
        verifyHappyRefuseJurorPoolPath(2, true);
        verifyHappyExcusalDeniedLetter();
    }

    @Test
    void testRefuseExcusalRequestHappyPathDigitalResponseCourtUserCourtOwner() {
        TestUtils.mockCourtUser("415");
        final BureauJwtPayload courtPayload = TestUtils.createJwt("415", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setReplyMethod(ReplyMethod.DIGITAL);

        excusalResponseService.respondToExcusalRequest(courtPayload, excusalDecisionDto, JUROR_NUMBER2);

        verify(jurorPoolService, times(1))
            .getJurorPoolFromUser(any());

        verify(jurorResponseService, times(1)).setResponseProcessingStatusToClosed(JUROR_NUMBER2);
        verifyHappyRefuseJurorPoolPath(2, false);
        verify(printDataService, times(0))
            .printExcusalDeniedLetter(any());
    }

    @Test
    void testGrantExcusalRequestHappyPathDigitalResponseBureauUserBureauOwner() {
        TestUtils.mockBureauUser();
        final BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setReplyMethod(ReplyMethod.DIGITAL);
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);

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

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verify(jurorResponseService, times(1)).setResponseProcessingStatusToClosed(JUROR_NUMBER);
        verifyHappyGrantJurorPoolPath();
        verifyHappyExcusalLetter(jurorPool, excusalDecisionDto);
    }

    @Test
    void testGrantExcusalRequestHappyPathDigitalResponseCourtUserCourtOwner() {
        TestUtils.mockCourtUser("415");
        final BureauJwtPayload courtPayload = TestUtils.createJwt("415", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setReplyMethod(ReplyMethod.DIGITAL);
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);

        excusalResponseService.respondToExcusalRequest(courtPayload, excusalDecisionDto, JUROR_NUMBER2);

        verify(jurorPoolService, times(1))
            .getJurorPoolFromUser(any());

        verify(jurorResponseService, times(1)).setResponseProcessingStatusToClosed(JUROR_NUMBER2);
        verifyHappyGrantJurorPoolPathNoLetter(); // court users don't automatically send letters
    }

    @Test
    void testGrantExcusalRequestHappyPathExcusalCodeDeceased() {
        TestUtils.mockBureauUser();
        final BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);
        excusalDecisionDto.setExcusalReasonCode(ExcusalCodeEnum.D.getCode());

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

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());
        verifyHappyGrantJurorPoolPathNoLetter(); // deceased jurors don't get letters
        verifyHappyExcusalLetter(jurorPool, excusalDecisionDto);
    }

    @Test
    void testExcusalRequestPaperResponseBureauUserCourtOwner() {
        TestUtils.mockBureauUser();
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto,
                JUROR_NUMBER2));

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyFailedInitialChecksPath();
    }

    @Test
    void testExcusalRequestPaperResponseCourtUserBureauOwner() {
        TestUtils.mockCourtUser("415");
        BureauJwtPayload courtPayload = TestUtils.createJwt("415", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> excusalResponseService.respondToExcusalRequest(courtPayload, excusalDecisionDto,
                JUROR_NUMBER));

        verify(jurorPoolService, times(1))
            .getJurorPoolFromUser(any());

        verifyFailedInitialChecksPath();
    }

    @Test
    void testExcusalRequestPaperResponseAlreadyClosed() {
        final ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);


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

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyExcusalLetter(jurorPool, excusalDecisionDto);
    }

    @Test
    void testExcusalRequestDigitalResponseAlreadyClosed() {
        TestUtils.mockBureauUser();
        final BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setReplyMethod(ReplyMethod.DIGITAL);
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);

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

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());
        verifyHappyExcusalLetter(jurorPool, excusalDecisionDto);
    }

    @Test
    void testExcusalRequestInvalidExcusalCode() {
        final BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setExcusalReasonCode("E");

        Assertions.assertThatExceptionOfType(ExcusalResponseException.InvalidExcusalCode.class)
            .isThrownBy(() -> excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto,
                JUROR_NUMBER));

        verify(jurorPoolRepository, never())
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyFailedInitialChecksPath();
    }

    @Test
    void testExcusalRequestUnableToRetrieveValidExcusalCodeList() {
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        List<ExcusalCode> emptyList = Collections.emptyList();
        Mockito.when(excusalCodeRepository.findAll()).thenReturn(emptyList);

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();

        Assertions.assertThatExceptionOfType(ExcusalResponseException.UnableToRetrieveExcusalCodeList.class)
            .isThrownBy(() -> excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto,
                JUROR_NUMBER));

        verify(jurorPoolRepository, never())
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyFailedInitialChecksPath();
    }

    @Test
    void testExcusalRequestNoJurorRecord() {
        String jurorNumber = "111111111";
        TestUtils.mockBureauUser();
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();

        Assertions.assertThatExceptionOfType(MojException.NotFound.class)
            .isThrownBy(() -> excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber));

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyFailedInitialChecksPath();
    }

    @Test
    void testExcusalRequestPaperResponseDoesNotExist() {
        TestUtils.mockBureauUser();
        BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setExcusalReasonCode("A");

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, JUROR_NUMBER);

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verify(jurorResponseService, times(1)).setResponseProcessingStatusToClosed(JUROR_NUMBER);
    }

    @Test
    void testExcusalRequestWithoutResponseGrantBureauUserBureauOwner() {
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

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyGrantJurorPoolPath();
        verifyHappyExcusalLetter(jurorPool, excusalDecisionDto);
    }

    @Test
    void testExcusalRequestWithoutResponseRefuseBureauUserBureauOwner() {
        TestUtils.mockBureauUser();
        final BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        final ExcusalDecisionDto excusalDecisionDto =
            createTestExcusalDecisionRequestNoResponse(ExcusalDecision.REFUSE);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, JUROR_NUMBER);

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyRefuseJurorPoolPath(2, true);
        verifyHappyExcusalDeniedLetter();
    }

    @Test
    void testExcusalRequestWithoutResponseGrantCourtUserCourtOwner() {
        TestUtils.mockCourtUser("415");
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

        Mockito.doReturn(jurorPool).when(jurorPoolService)
            .getJurorPoolFromUser(JUROR_NUMBER);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, JUROR_NUMBER);

        verify(jurorPoolService, times(1))
            .getJurorPoolFromUser(any());

        verifyHappyGrantJurorPoolPathNoLetter(); // court users don't automatically send letters
    }

    @Test
    void testExcusalRequestWithoutResponseRefuseCourtUserCourtOwner() {
        TestUtils.mockCourtUser("415");
        final BureauJwtPayload payload = TestUtils.createJwt("415", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequestNoResponse(ExcusalDecision.REFUSE);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, JUROR_NUMBER2);

        verify(jurorPoolService, times(1))
            .getJurorPoolFromUser(any());

        verifyHappyRefuseJurorPoolPath(2, false);
        verify(printDataService, times(0))
            .printExcusalDeniedLetter(any());
    }

    @Test
    void testExcusalRequestWithoutResponseGrantBureauUserCourtOwner() {
        TestUtils.mockBureauUser();
        final BureauJwtPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequestNoResponse(ExcusalDecision.GRANT);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> {
                excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, JUROR_NUMBER2);
            });

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyFailedInitialChecksPath();
    }

    @Test
    void testExcusalRequestWithoutResponseGrantCourtUserBureauOwner() {
        TestUtils.mockCourtUser("415");
        final BureauJwtPayload payload = TestUtils.createJwt("415", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequestNoResponse(ExcusalDecision.GRANT);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> {
                excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, JUROR_NUMBER);
            });

        verify(jurorPoolService, times(1))
            .getJurorPoolFromUser(any());

        verifyFailedInitialChecksPath();
    }

    private void verifyHappyRefuseJurorPoolPath(int jurorHistoryRepositoryTimes, boolean shouldCreateNonExcusedLetter) {
        verify(jurorPoolRepository, times(1)).save(any());
        verify(jurorHistoryRepository, times(jurorHistoryRepositoryTimes)).save(any());
        if (shouldCreateNonExcusedLetter) {
            verify(jurorHistoryService).createNonExcusedLetterHistory(any(), eq("Refused Excusal"));
        } else {
            verify(jurorHistoryService, never()).createNonExcusedLetterHistory(any(), any());
        }
    }

    private void verifyHappyGrantJurorPoolPath() {
        verify(jurorPoolRepository, times(1)).save(any());
        verify(jurorHistoryRepository, times(1)).save(any());
        verify(jurorHistoryService).createExcusedLetter(any());
    }

    private void verifyHappyGrantJurorPoolPathNoLetter() {
        verify(jurorPoolRepository, times(1)).save(any());
        verify(jurorHistoryRepository, times(1)).save(any());
        verify(printDataService, never()).printExcusalLetter(Mockito.any());
    }


    private void verifyHappyExcusalDeniedLetter() {
        verify(printDataService, times(1))
            .printExcusalDeniedLetter(any());
    }

    private void verifyHappyExcusalLetter(JurorPool jurorPool,
                                          ExcusalDecisionDto excusalDecisionDto) {
        if (ExcusalCodeEnum.D.getCode().equals(excusalDecisionDto.getExcusalReasonCode())) {
            verify(printDataService, never())
                .printExcusalLetter(jurorPool);
        } else {
            verify(printDataService, times(1))
                .printExcusalLetter(jurorPool);
        }
        verify(printDataService, never())
            .printExcusalDeniedLetter(any());
    }

    private void verifyFailedInitialChecksPath() {
        verify(jurorResponseService, never()).setResponseProcessingStatusToClosed(any());
        verify(jurorPoolRepository, never()).save(any());
        verify(jurorHistoryRepository, never()).save(any());
        verify(printDataService, never()).printExcusalDeniedLetter(any());
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
}
