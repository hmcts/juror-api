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
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCode;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeEntity;
import uk.gov.hmcts.juror.api.bureau.domain.ExcusalCodeRepository;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.controller.request.ExcusalDecisionDto;
import uk.gov.hmcts.juror.api.moj.domain.ExcusalDecision;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.domain.letter.ExcusalDeniedLetterMod;
import uk.gov.hmcts.juror.api.moj.domain.letter.ExcusalLetterMod;
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
import uk.gov.hmcts.juror.api.moj.service.letter.ExcusalDeniedLetterServiceImpl;
import uk.gov.hmcts.juror.api.moj.service.letter.ExcusalLetterServiceImpl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
public class ExcusalResponseServiceImplTest {

    @Mock
    private ExcusalCodeRepository excusalCodeRepository;
    @Mock
    private JurorRepository jurorRepository;
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
    private ExcusalDeniedLetterServiceImpl excusalDeniedLetterService;
    @Mock
    private ExcusalLetterServiceImpl excusalLetterService;

    @InjectMocks
    private ExcusalResponseServiceImpl excusalResponseService;

    @Before
    public void setUpMocks() {
        List<ExcusalCodeEntity> excusalCodes = new ArrayList<>();
        ExcusalCodeEntity excusalCodeEntity = new ExcusalCodeEntity("A", "MOVED FROM AREA");
        excusalCodes.add(excusalCodeEntity);
        ExcusalCodeEntity excusalCodeEntity1 = new ExcusalCodeEntity("D", "DECEASED");
        excusalCodes.add(excusalCodeEntity1);
        Mockito.when(excusalCodeRepository.findAll()).thenReturn(excusalCodes);

        Mockito.doReturn(Collections.singletonList(createTestJurorPool("400", "123456789")))
            .when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc("123456789", true);
        Mockito.doReturn(Collections.singletonList(createTestJurorPool("415", "987654321")))
            .when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc("987654321", true);

        Mockito.doNothing().when(mergeService).mergePaperResponse(any(), any());

        Mockito.doReturn(Optional.of(createJurorStatus(1))).when(jurorStatusRepository).findById(1);
        Mockito.doReturn(Optional.of(createJurorStatus(2))).when(jurorStatusRepository).findById(2);
        Mockito.doReturn(Optional.of(createJurorStatus(5))).when(jurorStatusRepository).findById(5);

        Mockito.doReturn(new ExcusalDeniedLetterMod()).when(excusalDeniedLetterService)
            .getLetterToEnqueue(any(), any());
        Mockito.doNothing().when(excusalDeniedLetterService).enqueueLetter(any());

        Mockito.doReturn(new ExcusalLetterMod()).when(excusalLetterService).getLetterToEnqueue(any(),
            any());
        Mockito.doNothing().when(excusalLetterService).enqueueLetter(any());

        Mockito.doReturn(null).when(jurorHistoryRepository).save(any());
        Mockito.doReturn(null).when(jurorPaperResponseRepository).save(any());
        Mockito.doReturn(null).when(jurorResponseRepository).save(any());
    }

    @Test
    public void test_refuseExcusalRequest_happyPath_paperResponse_bureauUser_bureauOwner() {
        String jurorNumber = "123456789";
        BureauJWTPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();

        PaperResponse jurorPaperResponse = createTestJurorPaperResponse(jurorNumber);
        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyPaperPath(payload, jurorNumber);
        verifyHappyRefuseJurorPoolPath();
        verifyHappyExcusalDeniedLetter(payload, jurorNumber);
    }

    @Test
    public void test_refuseExcusalRequest_happyPath_paperResponse_courtUser_courtOwner() {
        String jurorNumber = "987654321";
        BureauJWTPayload courtPayload = TestUtils.createJwt("415", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();

        PaperResponse jurorPaperResponse = createTestJurorPaperResponse(jurorNumber);
        jurorPaperResponse.setStaff(null);
        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        excusalResponseService.respondToExcusalRequest(courtPayload, excusalDecisionDto, jurorNumber);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyPaperPath(courtPayload, jurorNumber);
        verifyHappyRefuseJurorPoolPath();
        verifyHappyExcusalDeniedLetter(courtPayload, jurorNumber);
    }

    @Test
    public void test_grantExcusalRequest_happyPath_paperResponse_bureauUser_bureauOwner() {
        String jurorNumber = "123456789";
        BureauJWTPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);

        PaperResponse jurorPaperResponse = createTestJurorPaperResponse(jurorNumber);
        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyPaperPath(payload, jurorNumber);
        verifyHappyGrantJurorPoolPath();
        verifyHappyExcusalLetter(payload, jurorNumber, excusalDecisionDto);
    }

    @Test
    public void test_grantExcusalRequest_happyPath_paperResponse_courtUser_courtOwner() {
        String jurorNumber = "987654321";
        BureauJWTPayload courtPayload = TestUtils.createJwt("415", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);

        PaperResponse jurorPaperResponse = createTestJurorPaperResponse(jurorNumber);
        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        excusalResponseService.respondToExcusalRequest(courtPayload, excusalDecisionDto, jurorNumber);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyPaperPath(courtPayload, jurorNumber);
        verifyHappyGrantJurorPoolPath();
        verifyHappyExcusalLetter(courtPayload, jurorNumber, excusalDecisionDto);
    }

    @Test
    public void test_refuseExcusalRequest_happyPath_digitalResponse_bureauUser_bureauOwner() {
        String jurorNumber = "123456789";
        BureauJWTPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setReplyMethod(ReplyMethod.DIGITAL);

        DigitalResponse jurorResponse = createTestJurorDigitalResponse(jurorNumber);
        Mockito.doReturn(jurorResponse).when(jurorResponseRepository).findByJurorNumber(jurorNumber);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyDigitalPath(payload, jurorNumber);
        verifyHappyRefuseJurorPoolPath();
        verifyHappyExcusalDeniedLetter(payload, jurorNumber);
    }

    @Test
    public void test_refuseExcusalRequest_happyPath_digitalResponse_courtUser_courtOwner() {
        String jurorNumber = "987654321";
        BureauJWTPayload courtPayload = TestUtils.createJwt("415", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setReplyMethod(ReplyMethod.DIGITAL);

        DigitalResponse jurorResponse = createTestJurorDigitalResponse(jurorNumber);
        Mockito.doReturn(jurorResponse).when(jurorResponseRepository).findByJurorNumber(jurorNumber);

        excusalResponseService.respondToExcusalRequest(courtPayload, excusalDecisionDto, jurorNumber);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyDigitalPath(courtPayload, jurorNumber);
        verifyHappyRefuseJurorPoolPath();
        verifyHappyExcusalDeniedLetter(courtPayload, jurorNumber);
    }

    @Test
    public void test_grantExcusalRequest_happyPath_digitalResponse_bureauUser_bureauOwner() {
        String jurorNumber = "123456789";
        final BureauJWTPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setReplyMethod(ReplyMethod.DIGITAL);
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);

        DigitalResponse jurorResponse = createTestJurorDigitalResponse(jurorNumber);
        Mockito.doReturn(jurorResponse).when(jurorResponseRepository).findByJurorNumber(jurorNumber);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyDigitalPath(payload, jurorNumber);
        verifyHappyGrantJurorPoolPath();
        verifyHappyExcusalLetter(payload, jurorNumber, excusalDecisionDto);
    }

    @Test
    public void test_grantExcusalRequest_happyPath_digitalResponse_courtUser_courtOwner() {
        String jurorNumber = "987654321";
        final BureauJWTPayload courtPayload = TestUtils.createJwt("415", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setReplyMethod(ReplyMethod.DIGITAL);
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);

        DigitalResponse jurorResponse = createTestJurorDigitalResponse(jurorNumber);
        Mockito.doReturn(jurorResponse).when(jurorResponseRepository).findByJurorNumber(jurorNumber);

        excusalResponseService.respondToExcusalRequest(courtPayload, excusalDecisionDto, jurorNumber);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyDigitalPath(courtPayload, jurorNumber);
        verifyHappyGrantJurorPoolPath();
        verifyHappyExcusalLetter(courtPayload, jurorNumber, excusalDecisionDto);
    }

    @Test
    public void test_grantExcusalRequest_happyPath_excusalCodeDeceased() {
        String jurorNumber = "123456789";
        final BureauJWTPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);
        excusalDecisionDto.setExcusalReasonCode(ExcusalCode.DECEASED);

        PaperResponse jurorPaperResponse = createTestJurorPaperResponse(jurorNumber);
        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyPaperPath(payload, jurorNumber);
        verifyHappyGrantJurorPoolPath();
        verifyHappyExcusalLetter(payload, jurorNumber, excusalDecisionDto);
    }

    @Test
    public void test_excusalRequest_paperResponse_bureauUser_courtOwner() {
        String jurorNumber = "987654321";
        BureauJWTPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();

        PaperResponse jurorPaperResponse = createTestJurorPaperResponse(jurorNumber);
        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber));

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyFailedInitialChecksPath(payload, jurorNumber);
    }

    @Test
    public void test_excusalRequest_paperResponse_courtUser_bureauOwner() {
        String jurorNumber = "123456789";
        BureauJWTPayload courtPayload = TestUtils.createJwt("415", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();

        PaperResponse jurorPaperResponse = createTestJurorPaperResponse(jurorNumber);
        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> excusalResponseService.respondToExcusalRequest(courtPayload, excusalDecisionDto,
                jurorNumber));

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyFailedInitialChecksPath(courtPayload, jurorNumber);
    }

    @Test
    public void test_excusalRequest_paperResponse_alreadyClosed() {
        String jurorNumber = "123456789";
        BureauJWTPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();

        PaperResponse jurorPaperResponse = createTestJurorPaperResponse(jurorNumber);
        jurorPaperResponse.setProcessingStatus(ProcessingStatus.CLOSED);
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);

        Mockito.doReturn(jurorPaperResponse).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).findByJurorNumber(jurorNumber);
        Mockito.verify(jurorResponseRepository, Mockito.never()).findByJurorNumber(jurorNumber);
        verifyHappyExcusalLetter(payload, jurorNumber, excusalDecisionDto);
    }

    @Test
    public void test_excusalRequest_digitalResponse_alreadyClosed() {
        String jurorNumber = "123456789";
        final BureauJWTPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setReplyMethod(ReplyMethod.DIGITAL);
        excusalDecisionDto.setExcusalDecision(ExcusalDecision.GRANT);

        DigitalResponse jurorResponse = createTestJurorDigitalResponse(jurorNumber);
        jurorResponse.setProcessingStatus(ProcessingStatus.CLOSED);
        Mockito.doReturn(jurorResponse).when(jurorResponseRepository).findByJurorNumber(jurorNumber);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        Mockito.verify(jurorPaperResponseRepository, Mockito.never()).findById(jurorNumber);
        Mockito.verify(jurorResponseRepository, Mockito.times(1)).findByJurorNumber(jurorNumber);
        verifyHappyExcusalLetter(payload, jurorNumber, excusalDecisionDto);
    }

    @Test
    public void test_excusalRequest_invalidExcusalCode() {
        String jurorNumber = "123456789";
        BureauJWTPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setExcusalReasonCode("E");

        Assertions.assertThatExceptionOfType(ExcusalResponseException.InvalidExcusalCode.class)
            .isThrownBy(() -> excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber));

        Mockito.verify(jurorPoolRepository, Mockito.never())
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyFailedInitialChecksPath(payload, jurorNumber);
    }

    @Test
    public void test_excusalRequest_unableToRetrieveValidExcusalCodeList() {
        String jurorNumber = "123456789";
        BureauJWTPayload payload = TestUtils.createJwt("400", "SOME_USER");

        List<ExcusalCodeEntity> emptyList = Collections.emptyList();
        Mockito.when(excusalCodeRepository.findAll()).thenReturn(emptyList);

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();

        Assertions.assertThatExceptionOfType(ExcusalResponseException.UnableToRetrieveExcusalCodeList.class)
            .isThrownBy(() -> excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber));

        Mockito.verify(jurorPoolRepository, Mockito.never())
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyFailedInitialChecksPath(payload, jurorNumber);
    }

    @Test
    public void test_excusalRequest_noJurorRecord() {
        String jurorNumber = "111111111";
        BureauJWTPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();

        Assertions.assertThatExceptionOfType(MojException.NotFound.class)
            .isThrownBy(() -> excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber));

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyFailedInitialChecksPath(payload, jurorNumber);
    }

    @Test
    public void test_excusalRequest_paperResponseDoesNotExist() {
        String jurorNumber = "123456789";
        BureauJWTPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequest();
        excusalDecisionDto.setExcusalReasonCode("A");

        Mockito.doReturn(null).when(jurorPaperResponseRepository).findByJurorNumber(jurorNumber);

        Assertions.assertThatExceptionOfType(MojException.NotFound.class)
            .isThrownBy(() -> excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber));

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).findByJurorNumber(jurorNumber);
        Mockito.verify(jurorResponseRepository, Mockito.never()).findByJurorNumber(jurorNumber);

        verifyFailedAtResponseStatusPath(payload, jurorNumber);
    }

    @Test
    public void test_excusalRequest_withoutResponse_grant_bureauUser_bureauOwner() {
        String jurorNumber = "123456789";
        BureauJWTPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequestNoResponse(ExcusalDecision.GRANT);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyGrantJurorPoolPath();
        verifyHappyExcusalLetter(payload, jurorNumber, excusalDecisionDto);
    }

    @Test
    public void test_excusalRequest_withoutResponse_refuse_bureauUser_bureauOwner() {
        String jurorNumber = "123456789";
        BureauJWTPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequestNoResponse(ExcusalDecision.REFUSE);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyRefuseJurorPoolPath();
        verifyHappyExcusalDeniedLetter(payload, jurorNumber);
    }

    @Test
    public void test_excusalRequest_withoutResponse_grant_courtUser_courtOwner() {
        String jurorNumber = "987654321";
        BureauJWTPayload payload = TestUtils.createJwt("415", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequestNoResponse(ExcusalDecision.GRANT);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyGrantJurorPoolPath();
        verifyHappyExcusalLetter(payload, jurorNumber, excusalDecisionDto);
    }

    @Test
    public void test_excusalRequest_withoutResponse_refuse_courtUser_courtOwner() {
        String jurorNumber = "987654321";
        BureauJWTPayload payload = TestUtils.createJwt("415", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequestNoResponse(ExcusalDecision.REFUSE);

        excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyHappyRefuseJurorPoolPath();
        verifyHappyExcusalDeniedLetter(payload, jurorNumber);
    }

    @Test
    public void test_excusalRequest_withoutResponse_grant_bureauUser_courtOwner() {
        String jurorNumber = "987654321";
        BureauJWTPayload payload = TestUtils.createJwt("400", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequestNoResponse(ExcusalDecision.GRANT);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> {
                excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber);
            });

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyFailedInitialChecksPath(payload, jurorNumber);
    }

    @Test
    public void test_excusalRequest_withoutResponse_grant_courtUser_bureauOwner() {
        String jurorNumber = "123456789";
        BureauJWTPayload payload = TestUtils.createJwt("415", "SOME_USER");

        ExcusalDecisionDto excusalDecisionDto = createTestExcusalDecisionRequestNoResponse(ExcusalDecision.GRANT);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class)
            .isThrownBy(() -> {
                excusalResponseService.respondToExcusalRequest(payload, excusalDecisionDto, jurorNumber);
            });

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), Mockito.anyBoolean());

        verifyFailedInitialChecksPath(payload, jurorNumber);
    }

    private void verifyHappyRefuseJurorPoolPath() {
        Mockito.verify(jurorPoolRepository, Mockito.times(1)).save(any());
        Mockito.verify(jurorHistoryRepository, Mockito.times(2)).save(any());
    }

    private void verifyHappyGrantJurorPoolPath() {
        Mockito.verify(jurorPoolRepository, Mockito.times(1)).save(any());
        Mockito.verify(jurorHistoryRepository, Mockito.times(1)).save(any());
    }

    private void verifyHappyPaperPath(BureauJWTPayload payload, String jurorNumber) {
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).findByJurorNumber(jurorNumber);
        Mockito.verify(jurorResponseRepository, Mockito.never()).findById(jurorNumber);
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(payload.getLogin());
        Mockito.verify(mergeService, Mockito.times(1))
            .mergePaperResponse(any(), any());
        Mockito.verify(mergeService, Mockito.never()).mergeDigitalResponse(any(), any());
    }

    private void verifyHappyDigitalPath(BureauJWTPayload payload, String jurorNumber) {
        Mockito.verify(jurorPaperResponseRepository, Mockito.never()).findById(jurorNumber);
        Mockito.verify(jurorResponseRepository, Mockito.times(1)).findByJurorNumber(jurorNumber);
        Mockito.verify(userRepository, Mockito.times(1)).findByUsername(payload.getLogin());
        Mockito.verify(mergeService, Mockito.never()).mergePaperResponse(any(), any());
        Mockito.verify(mergeService, Mockito.times(1)).mergeDigitalResponse(any(), any());
    }

    private void verifyHappyExcusalDeniedLetter(BureauJWTPayload payload, String jurorNumber) {
        Mockito.verify(excusalDeniedLetterService, Mockito.times(1))
            .getLetterToEnqueue(payload.getOwner(), jurorNumber);
        Mockito.verify(excusalDeniedLetterService, Mockito.times(1)).enqueueLetter(any());
        Mockito.verify(excusalLetterService, Mockito.never())
            .getLetterToEnqueue(payload.getOwner(), jurorNumber);
        Mockito.verify(excusalLetterService, Mockito.never()).enqueueLetter(any());
    }

    private void verifyHappyExcusalLetter(BureauJWTPayload payload, String jurorNumber,
                                          ExcusalDecisionDto excusalDecisionDto) {
        if (excusalDecisionDto.getExcusalReasonCode().equals(ExcusalCode.DECEASED)) {
            Mockito.verify(excusalLetterService, Mockito.never())
                .getLetterToEnqueue(payload.getOwner(), jurorNumber);
            Mockito.verify(excusalLetterService, Mockito.never()).enqueueLetter(any());
        } else {
            Mockito.verify(excusalLetterService, Mockito.times(1))
                .getLetterToEnqueue(payload.getOwner(), jurorNumber);
            Mockito.verify(excusalLetterService, Mockito.times(1)).enqueueLetter(any());
        }
        Mockito.verify(excusalDeniedLetterService, Mockito.never())
            .getLetterToEnqueue(payload.getOwner(), jurorNumber);
        Mockito.verify(excusalDeniedLetterService, Mockito.never()).enqueueLetter(any());
    }

    private void verifyFailedAtResponseStatusPath(BureauJWTPayload payload, String jurorNumber) {
        Mockito.verify(userRepository, Mockito.never()).findByUsername(payload.getLogin());
        Mockito.verify(mergeService, Mockito.never()).mergePaperResponse(any(), any());
        Mockito.verify(mergeService, Mockito.never()).mergeDigitalResponse(any(), any());
        Mockito.verify(jurorPoolRepository, Mockito.never()).save(any());
        Mockito.verify(jurorHistoryRepository, Mockito.never()).save(any());
        Mockito.verify(excusalDeniedLetterService, Mockito.never()).getLetterToEnqueue(payload.getOwner(), jurorNumber);
        Mockito.verify(excusalDeniedLetterService, Mockito.never()).enqueueLetter(any());
    }

    private void verifyFailedInitialChecksPath(BureauJWTPayload payload, String jurorNumber) {
        Mockito.verify(jurorPaperResponseRepository, Mockito.never()).findById(jurorNumber);
        Mockito.verify(jurorResponseRepository, Mockito.never()).findByJurorNumber(jurorNumber);
        Mockito.verify(userRepository, Mockito.never()).findByUsername(payload.getLogin());
        Mockito.verify(mergeService, Mockito.never()).mergePaperResponse(any(), any());
        Mockito.verify(mergeService, Mockito.never()).mergeDigitalResponse(any(), any());
        Mockito.verify(jurorPoolRepository, Mockito.never()).save(any());
        Mockito.verify(jurorHistoryRepository, Mockito.never()).save(any());
        Mockito.verify(excusalDeniedLetterService, Mockito.never()).getLetterToEnqueue(payload.getOwner(), jurorNumber);
        Mockito.verify(excusalDeniedLetterService, Mockito.never()).enqueueLetter(any());
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
        response.setDateReceived(LocalDate.now());

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
        response.setProcessingStatus(ProcessingStatus.TODO);

        return response;
    }

    private DigitalResponse createTestJurorDigitalResponse(String jurorNumber) {
        DigitalResponse response = new DigitalResponse();

        response.setJurorNumber(jurorNumber);
        response.setDateReceived(LocalDate.now());

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

        response.setProcessingStatus(ProcessingStatus.TODO);

        return response;
    }
}
