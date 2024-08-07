package uk.gov.hmcts.juror.api.moj.service.summonsmanagement;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseAuditMod;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.service.AssignOnUpdateServiceMod;
import uk.gov.hmcts.juror.api.moj.service.JurorHistoryService;
import uk.gov.hmcts.juror.api.moj.service.PrintDataService;
import uk.gov.hmcts.juror.api.moj.service.SummonsReplyMergeService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SuppressWarnings("PMD.TooManyMethods")
public class DisqualifyJurorDueToAgeServiceImplTest {
    private static final String BUREAU_USER = "400";
    private static final String JUROR_NUMBER = "123456789";

    @Mock
    private JurorPoolRepository jurorPoolRepository;

    @Mock
    private JurorHistoryService jurorHistoryService;

    @Mock
    private SummonsReplyMergeService summonsReplyMergeService;

    @Mock
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepository;

    @Mock
    private JurorDigitalResponseRepositoryMod jurorDigitalResponseRepository;

    @Mock
    private JurorResponseAuditRepositoryMod jurorResponseAuditRepository;

    @Mock
    private AssignOnUpdateServiceMod assignOnUpdateService;

    @Mock
    private PrintDataService printDataService;

    @InjectMocks
    private DisqualifyJurorServiceImpl disqualifyJurorServiceImpl;


    @Test
    public void disqualifyDueToAge_bureau_partialPaperResponsePresent_happyPath() {
        final ArgumentCaptor<String> jurorNumberCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<PaperResponse> jurorPaperResponseEntityCaptor =
            ArgumentCaptor.forClass(PaperResponse.class);
        final ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<JurorPool> jurorPoolEntityCaptor = ArgumentCaptor.forClass(JurorPool.class);

        BureauJwtPayload courtPayload = buildBureauPayload();
        List<JurorPool> jurorPoolList = createJurorPoolList(courtPayload.getOwner());
        PaperResponse paperResponse = createPaperResponse();

        when(jurorDigitalResponseRepository.findByJurorNumber(JUROR_NUMBER)).thenReturn(null);
        when(jurorPaperResponseRepository.findByJurorNumber(JUROR_NUMBER)).thenReturn(paperResponse);
        doNothing().when(summonsReplyMergeService).mergePaperResponse(any(PaperResponse.class), anyString());

        doReturn(jurorPoolList).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
        doReturn(null).when(jurorPoolRepository).save(any(JurorPool.class));

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
        verifyJurorPoolRepository(jurorPoolEntityCaptor);

        //verification of the JurorHistoryRepository invocation
        verify(jurorHistoryService).createDisqualifyHistory(jurorPoolList.get(0),"A");

        //verification of the DisqualificationLetterRepository invocation
        // TODO - verify the printDataServiceArgumentCaptor and approach to letters for disqualification

        //verify that the below services are never invoked
        verify(assignOnUpdateService, never()).assignToCurrentLogin(any(DigitalResponse.class),
            anyString());
        verify(summonsReplyMergeService, never()).mergeDigitalResponse(any(DigitalResponse.class), anyString());
        assertThat(paperResponse.getProcessingStatus()).isEqualTo(ProcessingStatus.CLOSED);
    }


    @Test
    public void disqualifyDueToAge_bureau_partialDigitalResponsePresent_happyPath() {
        final ArgumentCaptor<String> jurorNumberCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<DigitalResponse> jurorDigitalResponseEntityCaptor =
            ArgumentCaptor.forClass(DigitalResponse.class);
        final ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<JurorPool> jurorPoolEntityCaptor = ArgumentCaptor.forClass(JurorPool.class);
        final ArgumentCaptor<JurorResponseAuditMod> jurorResponseAuditArgumentCaptor =
            ArgumentCaptor.forClass(JurorResponseAuditMod.class);

        DigitalResponse digitalResponse = createDigitalResponse();
        digitalResponse.setProcessingComplete(false);
        digitalResponse.setCompletedAt(null);
        digitalResponse.setProcessingStatus(jurorResponseAuditRepository, ProcessingStatus.TODO);

        when(jurorDigitalResponseRepository.findByJurorNumber(JUROR_NUMBER)).thenReturn(digitalResponse);
        when(jurorPaperResponseRepository.findByJurorNumber(JUROR_NUMBER)).thenReturn(null);

        BureauJwtPayload courtPayload = buildBureauPayload();
        List<JurorPool> jurorPoolList = createJurorPoolList(courtPayload.getOwner());

        doReturn(jurorPoolList).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
        doReturn(null).when(jurorPoolRepository).save(any(JurorPool.class));

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
        verifyJurorPoolRepository(jurorPoolEntityCaptor);

        //verification of the JurorHistoryRepository activity

        verify(jurorHistoryService).createDisqualifyHistory(jurorPoolList.get(0),"A");

        //verification of the DisqualificationLetterRepository
        // TODO - verify the printDataServiceArgumentCaptor and approach to letters for disqualification

        //verification of the JurorResponseAuditRepository
        verify(jurorResponseAuditRepository, times(3))
            .save(jurorResponseAuditArgumentCaptor.capture());
        verify(jurorResponseAuditRepository, times(3)).save(any(JurorResponseAuditMod.class));
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
        final ArgumentCaptor<JurorResponseAuditMod> jurorResponseAuditArgumentCaptor =
            ArgumentCaptor.forClass(JurorResponseAuditMod.class);

        BureauJwtPayload courtPayload = buildBureauPayload();
        List<JurorPool> jurorPoolList = createJurorPoolList(courtPayload.getOwner());

        when(jurorDigitalResponseRepository.findByJurorNumber(JUROR_NUMBER)).thenReturn(null);
        when(jurorPaperResponseRepository.findByJurorNumber(JUROR_NUMBER)).thenReturn(null);

        doReturn(jurorPoolList).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
        doReturn(null).when(jurorPoolRepository).save(any(JurorPool.class));

        doAnswer(invocation -> invocation.getArguments()[0])
            .when(jurorPaperResponseRepository).save(any(PaperResponse.class));

        disqualifyJurorServiceImpl.disqualifyJurorDueToAgeOutOfRange(JUROR_NUMBER, courtPayload);

        //verification of the JurorDigitalResponseRepository
        verify(jurorDigitalResponseRepository, times(1))
            .findByJurorNumber(jurorNumberCaptor.capture());
        assertThat(jurorNumberCaptor.getValue()).isEqualTo(JUROR_NUMBER);
        verify(jurorDigitalResponseRepository, times(0)).save(any());

        // verification of the JurorPaperResponseRepository
        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumberCaptor.capture());
        assertThat(jurorNumberCaptor.getValue()).isEqualTo(JUROR_NUMBER);
        verify(jurorPaperResponseRepository, times(2)).save(any());

        //verification of the SummonsReplyMergeService invocation
        verifySummonsReplyMergeService_Paper(jurorPaperResponseEntityCaptor, userCaptor);

        //verification of the JurorPoolRepository activity
        verifyJurorPoolRepository(jurorPoolEntityCaptor);

        //verification of the JurorHistoryRepository activity
        verify(jurorHistoryService).createDisqualifyHistory(jurorPoolList.get(0),"A");

        //verification of the DisqualificationLetterRepository invocation
        // TODO - verify the printDataServiceArgumentCaptor and approach to letters for disqualification
    }

    @Test
    public void disqualifyJurorDueToAge_bureau_completedDigitalResponse() {
        DigitalResponse digitalResponse = createDigitalResponse();
        digitalResponse.setProcessingComplete(true);
        digitalResponse.setCompletedAt(LocalDateTime.now());
        digitalResponse.setProcessingStatus(jurorResponseAuditRepository, ProcessingStatus.CLOSED);

        when(jurorDigitalResponseRepository.findByJurorNumber(JUROR_NUMBER)).thenReturn(digitalResponse);
        when(jurorPaperResponseRepository.findByJurorNumber(JUROR_NUMBER)).thenReturn(null);

        BureauJwtPayload courtPayload = buildBureauPayload();
        List<JurorPool> jurorPoolList = createJurorPoolList(courtPayload.getOwner());

        doReturn(jurorPoolList).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
        doReturn(null).when(jurorPoolRepository).save(any(JurorPool.class));

        Assertions.assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
            disqualifyJurorServiceImpl.disqualifyJurorDueToAgeOutOfRange(JUROR_NUMBER, courtPayload));

        //Digital related
        verify(jurorDigitalResponseRepository, times(1)).findByJurorNumber(anyString());
        verify(assignOnUpdateService, never()).assignToCurrentLogin(any(DigitalResponse.class), anyString());
        verify(summonsReplyMergeService, never()).mergeDigitalResponse(any(DigitalResponse.class), anyString());
        assertThat(digitalResponse.getProcessingStatus()).isEqualTo(ProcessingStatus.CLOSED);

        //Paper related
        verify(jurorPaperResponseRepository, never()).findById(anyString());
        verify(summonsReplyMergeService, never()).mergePaperResponse(any(PaperResponse.class), anyString());

        //Common
        verifyNoInteractions(printDataService);
        verifyNoInteractions(jurorHistoryService);
        verify(jurorPoolRepository, never()).save(any(JurorPool.class));
        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
    }

    @Test
    public void disqualifyJurorDueToAge_bureau_completedPaperResponse() {
        PaperResponse paperResponse = createPaperResponse();
        paperResponse.setProcessingComplete(true);
        paperResponse.setCompletedAt(LocalDateTime.now());
        paperResponse.setProcessingStatus(jurorResponseAuditRepository, ProcessingStatus.CLOSED);

        when(jurorPaperResponseRepository.findByJurorNumber(JUROR_NUMBER)).thenReturn(paperResponse);
        when(jurorDigitalResponseRepository.findByJurorNumber(JUROR_NUMBER)).thenReturn(null);

        BureauJwtPayload courtPayload = buildBureauPayload();
        List<JurorPool> jurorPoolList = createJurorPoolList(courtPayload.getOwner());

        doReturn(jurorPoolList).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
        doReturn(null).when(jurorPoolRepository).save(any(JurorPool.class));

        Assertions.assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() ->
            disqualifyJurorServiceImpl.disqualifyJurorDueToAgeOutOfRange(JUROR_NUMBER, courtPayload));

        //Paper response-related
        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(anyString());
        verify(summonsReplyMergeService, never()).mergePaperResponse(any(PaperResponse.class), anyString());

        //Digital response-related
        verify(jurorDigitalResponseRepository, never()).findById(anyString());
        verify(assignOnUpdateService, never()).assignToCurrentLogin(any(DigitalResponse.class), anyString());
        verify(summonsReplyMergeService, never()).mergeDigitalResponse(any(DigitalResponse.class), anyString());

        assertThat(paperResponse.getProcessingStatus()).isEqualTo(ProcessingStatus.CLOSED);
        //Common
        verifyNoInteractions(printDataService);
        verifyNoInteractions(jurorHistoryService);
        verify(jurorPoolRepository, never()).save(any(JurorPool.class));
        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
    }

    @Test
    public void disqualifyJurorDueToAge_noActivePoolRecord() {
        BureauJwtPayload courtPayload = buildBureauPayload();

        doReturn(new ArrayList<JurorPool>()).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(anyString(), anyBoolean());

        Assertions.assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            disqualifyJurorServiceImpl.disqualifyJurorDueToAgeOutOfRange(JUROR_NUMBER, courtPayload));

        //Digital related
        verify(jurorDigitalResponseRepository, never()).findByJurorNumber(anyString());
        verify(assignOnUpdateService, never()).assignToCurrentLogin(any(DigitalResponse.class), anyString());
        verify(summonsReplyMergeService, never()).mergeDigitalResponse(any(DigitalResponse.class), anyString());
        verify(jurorResponseAuditRepository, never()).save(any(JurorResponseAuditMod.class));

        //Paper related
        verify(jurorPaperResponseRepository, never()).findById(anyString());
        verify(summonsReplyMergeService, never()).mergePaperResponse(any(PaperResponse.class), anyString());

        //Common
        verifyNoInteractions(printDataService);
        verifyNoInteractions(jurorHistoryService);
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

    private void verifyJurorPoolRepository(final ArgumentCaptor<JurorPool> jurorPoolEntityCaptor) {
        verify(jurorPoolRepository, times(1)).save(jurorPoolEntityCaptor.capture());
        JurorPool capturedJurorPool = jurorPoolEntityCaptor.getValue();
        Juror capturedJuror = capturedJurorPool.getJuror();
        assertThat(capturedJuror.isResponded()).isEqualTo(true);
        assertThat(capturedJuror.getDisqualifyDate()).isNotNull();
        assertThat(capturedJurorPool.getUserEdtq()).isEqualTo(DisqualifyJurorDueToAgeServiceImplTest.BUREAU_USER);
        assertThat(capturedJurorPool.getNextDate()).isNull();
        assertThat(capturedJurorPool.getStatus().getStatus()).isEqualTo(
            uk.gov.hmcts.juror.api.moj.domain.IJurorStatus.DISQUALIFIED);
    }

    private PaperResponse createPaperResponse() {
        PaperResponse response = new PaperResponse();
        response.setJurorNumber(DisqualifyJurorDueToAgeServiceImplTest.JUROR_NUMBER);
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

    private DigitalResponse createDigitalResponse() {
        DigitalResponse response = new DigitalResponse();

        response.setJurorNumber(DisqualifyJurorDueToAgeServiceImplTest.JUROR_NUMBER);
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

    private BureauJwtPayload buildBureauPayload() {
        return BureauJwtPayload.builder()
            .userLevel("99")
            .login(BUREAU_USER)
            .owner("400")
            .build();
    }

    private List<JurorPool> createJurorPoolList(String owner) {

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
        juror.setJurorNumber(DisqualifyJurorDueToAgeServiceImplTest.JUROR_NUMBER);
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