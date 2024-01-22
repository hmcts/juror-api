package uk.gov.hmcts.juror.api.moj.service.summonsmanagement;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAudit;
import uk.gov.hmcts.juror.api.bureau.domain.JurorResponseAuditRepository;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.DisqualificationLetter;
import uk.gov.hmcts.juror.api.juror.domain.DisqualificationLetterRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.DisqualifyCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.AssignOnUpdateServiceMod;
import uk.gov.hmcts.juror.api.moj.service.SummonsReplyMergeService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.juror.api.moj.domain.IJurorStatus.DISQUALIFIED;

@RunWith(SpringRunner.class)
public class DisqualifyJurorDueToAgeServiceImplTest {
    private static final String BUREAU_USER = "400";
    private static final String JUROR_NUMBER = "123456789";
    private static final String OTHER_INFORMATION = "Code A";
    private static final String POOL_NUMBER = "416230101";

    @Mock
    private JurorPoolRepository jurorPoolRepository;

    @Mock
    private JurorHistoryRepository jurorHistoryRepository;

    @Mock
    private SummonsReplyMergeService summonsReplyMergeService;

    @Mock
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepository;

    @Mock
    private DisqualificationLetterRepository disqualificationLetterRepository;

    @Mock
    private JurorDigitalResponseRepositoryMod jurorDigitalResponseRepository;

    @Mock
    private JurorResponseAuditRepository jurorResponseAuditRepository;

    @Mock
    private AssignOnUpdateServiceMod assignOnUpdateService;

    private DisqualifyJurorServiceImpl disqualifyJurorServiceImpl;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        disqualifyJurorServiceImpl = new DisqualifyJurorServiceImpl(jurorPoolRepository,
            jurorPaperResponseRepository, jurorDigitalResponseRepository, jurorResponseAuditRepository,
            jurorHistoryRepository, disqualificationLetterRepository, assignOnUpdateService, summonsReplyMergeService);
    }


    @Test
    public void disqualifyDueToAge_bureau_partialPaperResponsePresent_happyPath() {
        final ArgumentCaptor<String> jurorNumberCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<PaperResponse> jurorPaperResponseEntityCaptor =
            ArgumentCaptor.forClass(PaperResponse.class);
        final ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<JurorPool> jurorPoolEntityCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<JurorHistory> jurorHistoryEntityCaptor = ArgumentCaptor.forClass(JurorHistory.class);
        final ArgumentCaptor<DisqualificationLetter> disqLetterEntityCaptor =
            ArgumentCaptor.forClass(DisqualificationLetter.class);

        BureauJWTPayload courtPayload = buildBureauPayload();
        List<JurorPool> jurorPoolList = createJurorPoolList(JUROR_NUMBER, courtPayload.getOwner());
        PaperResponse paperResponse = createPaperResponse(JUROR_NUMBER);

        when(jurorDigitalResponseRepository.findByJurorNumber(JUROR_NUMBER)).thenReturn(null);
        when(jurorPaperResponseRepository.findByJurorNumber(JUROR_NUMBER)).thenReturn(paperResponse);
        doNothing().when(summonsReplyMergeService).mergePaperResponse(any(PaperResponse.class), anyString());

        doReturn(jurorPoolList).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
        doReturn(null).when(jurorPoolRepository).save(any(JurorPool.class));
        doReturn(null).when(jurorHistoryRepository).save(any(JurorHistory.class));
        doReturn(null).when(disqualificationLetterRepository).save(any(DisqualificationLetter.class));

        //call the 'actual' service method
        disqualifyJurorServiceImpl.disqualifyJurorDueToAgeOutOfRange(JUROR_NUMBER, courtPayload);

        //verification of the JurorDigitalResponseRepository
        verify(jurorDigitalResponseRepository, times(1)).findByJurorNumber(JUROR_NUMBER);

        // verification of the JurorPaperResponseRepository
        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumberCaptor.capture());
        assertThat(jurorNumberCaptor.getValue()).isEqualTo(JUROR_NUMBER);
        verify(jurorPaperResponseRepository, times(0)).save(any());

        //verification of the SummonsReplyMergeService invocation
        verifySummonsReplyMergeService_Paper(jurorPaperResponseEntityCaptor, userCaptor);

        //verification of the JurorPoolRepository invocation
        verifyJurorPoolRepository(jurorPoolEntityCaptor, Boolean.TRUE, BUREAU_USER, DISQUALIFIED);

        //verification of the JurorHistoryRepository invocation
        verifyJurorHistoryRepository(jurorHistoryEntityCaptor, JUROR_NUMBER, HistoryCodeMod.DISQUALIFY_POOL_MEMBER,
            BUREAU_USER, POOL_NUMBER, OTHER_INFORMATION);

        //verification of the DisqualificationLetterRepository invocation
        verifyDisqualificationLetterRepository(disqLetterEntityCaptor, JUROR_NUMBER,
            String.valueOf(DisqualifyCodeEnum.A));

        //verify that the below services are never invoked
        verify(assignOnUpdateService, never()).assignToCurrentLogin(any(DigitalResponse.class),
            anyString());
        verify(summonsReplyMergeService, never()).mergeDigitalResponse(any(DigitalResponse.class), anyString());
        verify(jurorResponseAuditRepository, never()).save(any(JurorResponseAudit.class));
    }


    @Test
    public void disqualifyDueToAge_bureau_partialDigitalResponsePresent_happyPath() {
        final ArgumentCaptor<String> jurorNumberCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<DigitalResponse> jurorDigitalResponseEntityCaptor =
            ArgumentCaptor.forClass(DigitalResponse.class);
        final ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<JurorPool> jurorPoolEntityCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<JurorHistory> jurorHistoryEntityCaptor = ArgumentCaptor.forClass(JurorHistory.class);
        final ArgumentCaptor<DisqualificationLetter> disqLetterEntityCaptor =
            ArgumentCaptor.forClass(DisqualificationLetter.class);
        final ArgumentCaptor<JurorResponseAudit> jurorResponseAuditArgumentCaptor =
            ArgumentCaptor.forClass(JurorResponseAudit.class);

        DigitalResponse digitalResponse = createDigitalResponse(JUROR_NUMBER);
        digitalResponse.setProcessingComplete(false);
        digitalResponse.setCompletedAt(null);
        digitalResponse.setProcessingStatus(ProcessingStatus.TODO);

        when(jurorDigitalResponseRepository.findByJurorNumber(JUROR_NUMBER)).thenReturn(digitalResponse);
        when(jurorPaperResponseRepository.findByJurorNumber(JUROR_NUMBER)).thenReturn(null);

        BureauJWTPayload courtPayload = buildBureauPayload();
        List<JurorPool> jurorPoolList = createJurorPoolList(JUROR_NUMBER, courtPayload.getOwner());

        doReturn(jurorPoolList).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
        doReturn(null).when(jurorPoolRepository).save(any(JurorPool.class));
        doReturn(null).when(jurorHistoryRepository).save(any(JurorHistory.class));
        doReturn(null).when(disqualificationLetterRepository).save(any(DisqualificationLetter.class));

        assertThat(digitalResponse.getProcessingStatus()).isEqualTo(ProcessingStatus.TODO);
        assertThat(digitalResponse.getProcessingComplete()).isEqualTo(false);

        //call the 'actual' service method
        disqualifyJurorServiceImpl.disqualifyJurorDueToAgeOutOfRange(JUROR_NUMBER, courtPayload);

        //verification of the JurorDigitalResponseRepository
        verify(jurorDigitalResponseRepository, times(1))
            .findByJurorNumber(jurorNumberCaptor.capture());
        assertThat(jurorNumberCaptor.getValue()).isEqualTo(JUROR_NUMBER);
        verify(jurorDigitalResponseRepository, times(0)).save(any());

        // verification of the JurorPaperResponseRepository
        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumberCaptor.capture());
        verify(jurorPaperResponseRepository, times(0)).save(any());

        //verification of the SummonsReplyMergeService invocation
        verify(summonsReplyMergeService, times(1))
            .mergeDigitalResponse(jurorDigitalResponseEntityCaptor.capture(),
                userCaptor.capture());
        assertThat(jurorDigitalResponseEntityCaptor.getValue().getProcessingStatus())
            .isEqualTo(ProcessingStatus.CLOSED);
        assertThat(jurorDigitalResponseEntityCaptor.getValue().getProcessingComplete()).isTrue();
        assertThat(jurorDigitalResponseEntityCaptor.getValue().getCompletedAt()).isNotNull();
        assertThat(userCaptor.getValue()).isEqualTo(BUREAU_USER);

        //verification of the JurorPoolRepository activity
        verifyJurorPoolRepository(jurorPoolEntityCaptor, Boolean.TRUE, BUREAU_USER, DISQUALIFIED);

        //verification of the JurorHistoryRepository activity
        verifyJurorHistoryRepository(jurorHistoryEntityCaptor, JUROR_NUMBER, HistoryCodeMod.DISQUALIFY_POOL_MEMBER,
            BUREAU_USER, POOL_NUMBER, OTHER_INFORMATION);

        //verification of the DisqualificationLetterRepository
        verifyDisqualificationLetterRepository(disqLetterEntityCaptor, JUROR_NUMBER,
            String.valueOf(DisqualifyCodeEnum.A));

        //verification of the JurorResponseAuditRepository
        verify(jurorResponseAuditRepository, times(1))
            .save(jurorResponseAuditArgumentCaptor.capture());
        verify(jurorResponseAuditRepository, times(1)).save(any(JurorResponseAudit.class));
        assertThat(jurorResponseAuditArgumentCaptor.getValue().getJurorNumber()).isEqualTo(JUROR_NUMBER);
        assertThat(jurorResponseAuditArgumentCaptor.getValue().getNewProcessingStatus())
            .isEqualTo(ProcessingStatus.CLOSED);
        assertThat(jurorResponseAuditArgumentCaptor.getValue().getOldProcessingStatus())
            .isEqualTo(ProcessingStatus.TODO);

        //verify that the below services are never invoked
        verify(summonsReplyMergeService, never()).mergePaperResponse(any(PaperResponse.class), anyString());
    }

    @Test
    public void disqualifyJuror_bureau_noResponse_happy() {
        final ArgumentCaptor<String> jurorNumberCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<PaperResponse> jurorPaperResponseEntityCaptor =
            ArgumentCaptor.forClass(PaperResponse.class);
        final ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<JurorPool> jurorPoolEntityCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<JurorHistory> jurorHistoryEntityCaptor = ArgumentCaptor.forClass(JurorHistory.class);
        final ArgumentCaptor<DisqualificationLetter> disqLetterEntityCaptor =
            ArgumentCaptor.forClass(DisqualificationLetter.class);
        final ArgumentCaptor<JurorResponseAudit> jurorResponseAuditArgumentCaptor =
            ArgumentCaptor.forClass(JurorResponseAudit.class);

        BureauJWTPayload courtPayload = buildBureauPayload();
        List<JurorPool> jurorPoolList = createJurorPoolList(JUROR_NUMBER, courtPayload.getOwner());

        when(jurorDigitalResponseRepository.findByJurorNumber(JUROR_NUMBER)).thenReturn(null);
        when(jurorPaperResponseRepository.findByJurorNumber(JUROR_NUMBER)).thenReturn(null);

        doReturn(jurorPoolList).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
        doReturn(null).when(jurorPoolRepository).save(any(JurorPool.class));
        doReturn(null).when(jurorHistoryRepository).save(any(JurorHistory.class));
        doReturn(null).when(disqualificationLetterRepository).save(any(DisqualificationLetter.class));

        disqualifyJurorServiceImpl.disqualifyJurorDueToAgeOutOfRange(JUROR_NUMBER, courtPayload);

        //verification of the JurorDigitalResponseRepository
        verify(jurorDigitalResponseRepository, times(1))
            .findByJurorNumber(jurorNumberCaptor.capture());
        assertThat(jurorNumberCaptor.getValue()).isEqualTo(JUROR_NUMBER);
        verify(jurorDigitalResponseRepository, times(0)).save(any());

        // verification of the JurorPaperResponseRepository
        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumberCaptor.capture());
        assertThat(jurorNumberCaptor.getValue()).isEqualTo(JUROR_NUMBER);
        verify(jurorPaperResponseRepository, times(1)).save(any());

        //verification of the SummonsReplyMergeService invocation
        verifySummonsReplyMergeService_Paper(jurorPaperResponseEntityCaptor, userCaptor);

        //verification of the JurorPoolRepository activity
        verifyJurorPoolRepository(jurorPoolEntityCaptor, Boolean.TRUE, BUREAU_USER, DISQUALIFIED);

        //verification of the JurorHistoryRepository activity
        verifyJurorHistoryRepository(jurorHistoryEntityCaptor, JUROR_NUMBER, HistoryCodeMod.DISQUALIFY_POOL_MEMBER,
            BUREAU_USER, POOL_NUMBER, OTHER_INFORMATION);

        //verification of the DisqualificationLetterRepository invocation
        verifyDisqualificationLetterRepository(disqLetterEntityCaptor, JUROR_NUMBER,
            String.valueOf(DisqualifyCodeEnum.A));

        //verification of the JurorResponseAuditRepository
        verify(jurorResponseAuditRepository, times(0)).save(jurorResponseAuditArgumentCaptor.capture());
        verify(jurorResponseAuditRepository, times(0)).save(any(JurorResponseAudit.class));
    }

    @Test
    public void disqualifyJurorDueToAge_bureau_completedDigitalResponse() {
        DigitalResponse digitalResponse = createDigitalResponse(JUROR_NUMBER);
        digitalResponse.setProcessingComplete(true);
        digitalResponse.setCompletedAt(LocalDate.now());
        digitalResponse.setProcessingStatus(ProcessingStatus.CLOSED);

        when(jurorDigitalResponseRepository.findByJurorNumber(JUROR_NUMBER)).thenReturn(digitalResponse);
        when(jurorPaperResponseRepository.findByJurorNumber(JUROR_NUMBER)).thenReturn(null);

        BureauJWTPayload courtPayload = buildBureauPayload();
        List<JurorPool> jurorPoolList = createJurorPoolList(JUROR_NUMBER, courtPayload.getOwner());

        doReturn(jurorPoolList).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
        doReturn(null).when(jurorPoolRepository).save(any(JurorPool.class));
        doReturn(null).when(jurorHistoryRepository).save(any(JurorHistory.class));
        doReturn(null).when(disqualificationLetterRepository).save(any(DisqualificationLetter.class));

        Assertions.assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
            disqualifyJurorServiceImpl.disqualifyJurorDueToAgeOutOfRange(JUROR_NUMBER, courtPayload));

        //Digital related
        verify(jurorDigitalResponseRepository, times(1)).findByJurorNumber(anyString());
        verify(assignOnUpdateService, never()).assignToCurrentLogin(any(DigitalResponse.class), anyString());
        verify(summonsReplyMergeService, never()).mergeDigitalResponse(any(DigitalResponse.class), anyString());
        verify(jurorResponseAuditRepository, never()).save(any(JurorResponseAudit.class));

        //Paper related
        verify(jurorPaperResponseRepository, never()).findById(anyString());
        verify(summonsReplyMergeService, never()).mergePaperResponse(any(PaperResponse.class), anyString());

        //Common
        verify(disqualificationLetterRepository, never()).save(any(DisqualificationLetter.class));
        verify(jurorHistoryRepository, never()).save(any(JurorHistory.class));
        verify(jurorPoolRepository, never()).save(any(JurorPool.class));
        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
    }

    @Test
    public void disqualifyJurorDueToAge_bureau_completedPaperResponse() {
        PaperResponse paperResponse = createPaperResponse(JUROR_NUMBER);
        paperResponse.setProcessingComplete(true);
        paperResponse.setCompletedAt(LocalDate.now());
        paperResponse.setProcessingStatus(ProcessingStatus.CLOSED);

        when(jurorPaperResponseRepository.findByJurorNumber(JUROR_NUMBER)).thenReturn(paperResponse);
        when(jurorDigitalResponseRepository.findByJurorNumber(JUROR_NUMBER)).thenReturn(null);

        BureauJWTPayload courtPayload = buildBureauPayload();
        List<JurorPool> jurorPoolList = createJurorPoolList(JUROR_NUMBER, courtPayload.getOwner());

        doReturn(jurorPoolList).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
        doReturn(null).when(jurorPoolRepository).save(any(JurorPool.class));
        doReturn(null).when(jurorHistoryRepository).save(any(JurorHistory.class));
        doReturn(null).when(disqualificationLetterRepository).save(any(DisqualificationLetter.class));

        Assertions.assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
            disqualifyJurorServiceImpl.disqualifyJurorDueToAgeOutOfRange(JUROR_NUMBER, courtPayload));

        //Paper response-related
        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(anyString());
        verify(summonsReplyMergeService, never()).mergePaperResponse(any(PaperResponse.class), anyString());

        //Digital response-related
        verify(jurorDigitalResponseRepository, never()).findById(anyString());
        verify(assignOnUpdateService, never()).assignToCurrentLogin(any(DigitalResponse.class), anyString());
        verify(summonsReplyMergeService, never()).mergeDigitalResponse(any(DigitalResponse.class), anyString());
        verify(jurorResponseAuditRepository, never()).save(any(JurorResponseAudit.class));

        //Common
        verify(disqualificationLetterRepository, never()).save(any(DisqualificationLetter.class));
        verify(jurorHistoryRepository, never()).save(any(JurorHistory.class));
        verify(jurorPoolRepository, never()).save(any(JurorPool.class));
        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
    }

    @Test
    public void disqualifyJurorDueToAge_noActivePoolRecord() {
        BureauJWTPayload courtPayload = buildBureauPayload();

        doReturn(new ArrayList<JurorPool>()).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(anyString(), anyBoolean());

        Assertions.assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            disqualifyJurorServiceImpl.disqualifyJurorDueToAgeOutOfRange(JUROR_NUMBER, courtPayload));

        //Digital related
        verify(jurorDigitalResponseRepository, never()).findByJurorNumber(anyString());
        verify(assignOnUpdateService, never()).assignToCurrentLogin(any(DigitalResponse.class), anyString());
        verify(summonsReplyMergeService, never()).mergeDigitalResponse(any(DigitalResponse.class), anyString());
        verify(jurorResponseAuditRepository, never()).save(any(JurorResponseAudit.class));

        //Paper related
        verify(jurorPaperResponseRepository, never()).findById(anyString());
        verify(summonsReplyMergeService, never()).mergePaperResponse(any(PaperResponse.class), anyString());

        //Common
        verify(disqualificationLetterRepository, never()).save(any(DisqualificationLetter.class));
        verify(jurorHistoryRepository, never()).save(any(JurorHistory.class));
        verify(jurorPoolRepository, never()).save(any(JurorPool.class));
        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
    }

    private void verifySummonsReplyMergeService_Paper(ArgumentCaptor<PaperResponse> jurorPaperResponseEntityCaptor,
                                                      ArgumentCaptor<String> userCaptor) {
        verify(summonsReplyMergeService, times(1))
            .mergePaperResponse(jurorPaperResponseEntityCaptor.capture(),
                userCaptor.capture());
        assertThat(jurorPaperResponseEntityCaptor.getValue().getProcessingStatus()).isEqualTo(ProcessingStatus.CLOSED);
        assertThat(jurorPaperResponseEntityCaptor.getValue().getProcessingComplete()).isTrue();
        assertThat(jurorPaperResponseEntityCaptor.getValue().getCompletedAt()).isNotNull();
        assertThat(userCaptor.getValue()).isEqualTo(BUREAU_USER);
    }

    private void verifyJurorPoolRepository(final ArgumentCaptor<JurorPool> jurorPoolEntityCaptor, boolean responded,
                                           String bureauUser, int status) {
        verify(jurorPoolRepository, times(1)).save(jurorPoolEntityCaptor.capture());
        JurorPool capturedJurorPool = jurorPoolEntityCaptor.getValue();
        Juror capturedJuror = capturedJurorPool.getJuror();
        assertThat(capturedJuror.isResponded()).isEqualTo(responded);
        assertThat(capturedJuror.getDisqualifyDate()).isNotNull();
        assertThat(capturedJurorPool.getUserEdtq()).isEqualTo(bureauUser);
        assertThat(capturedJurorPool.getNextDate()).isNull();
        assertThat(capturedJurorPool.getStatus().getStatus()).isEqualTo(status);
    }

    private void verifyJurorHistoryRepository(final ArgumentCaptor<JurorHistory> jurorHistoryEntityCaptor,
                                              String jurorNumber, HistoryCodeMod historyCode, String bureauUser,
                                              String poolNumber, String otherInformation) {
        verify(jurorHistoryRepository, times(1)).save(jurorHistoryEntityCaptor.capture());
        assertThat(jurorHistoryEntityCaptor.getValue().getJurorNumber()).isEqualTo(jurorNumber);
        assertThat(jurorHistoryEntityCaptor.getValue().getHistoryCode()).isEqualTo(
            historyCode);
        assertThat(jurorHistoryEntityCaptor.getValue().getCreatedBy()).isEqualTo(bureauUser);
        assertThat(jurorHistoryEntityCaptor.getValue().getPoolNumber()).isEqualTo(poolNumber);
        assertThat(jurorHistoryEntityCaptor.getValue().getOtherInformation()).isEqualTo(otherInformation);
    }

    private void verifyDisqualificationLetterRepository(final ArgumentCaptor<DisqualificationLetter>
                                                            disqLetterEntityCaptor, String jurorNumber,
                                                        String disqualifyCodeEnum) {
        verify(disqualificationLetterRepository, times(1)).save(disqLetterEntityCaptor.capture());
        assertThat(disqLetterEntityCaptor.getValue().getJurorNumber()).isEqualTo(jurorNumber);
        assertThat(disqLetterEntityCaptor.getValue().getDisqCode()).isEqualTo(disqualifyCodeEnum);
        assertThat(disqLetterEntityCaptor.getValue().getDateDisq()).isNotNull();
    }

    private PaperResponse createPaperResponse(String jurorNumber) {
        PaperResponse response = new PaperResponse();
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

    private DigitalResponse createDigitalResponse(String jurorNumber) {
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

    private BureauJWTPayload buildBureauPayload() {
        return BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login(BUREAU_USER)
            .daysToExpire(89)
            .owner("400")
            .build();
    }

    private List<JurorPool> createJurorPoolList(String jurorNumber, String owner) {

        final List<JurorPool> jurorPoolList = new ArrayList<>();

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(1);
        jurorStatus.setStatusDesc("Responded");
        jurorStatus.setActive(true);

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("416");
        courtLocation.setName("TEST COURT");
        courtLocation.setLocCourtName("TEST COURT LONG NAME");

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("416230101");
        poolRequest.setCourtLocation(courtLocation);

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);
        juror.setFirstName("FIRSTNAME");
        juror.setLastName("LASTNAME");
        juror.setPostcode("M24 4GT");

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(owner);
        jurorPool.setStatus(jurorStatus);
        jurorPool.setPool(poolRequest);

        juror.setAssociatedPools(Set.of(jurorPool));
        jurorPool.setJuror(juror);

        jurorPoolList.add(jurorPool);

        return jurorPoolList;
    }
}