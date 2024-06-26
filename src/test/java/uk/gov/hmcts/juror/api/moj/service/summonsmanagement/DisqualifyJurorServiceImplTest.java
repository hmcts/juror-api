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
import uk.gov.hmcts.juror.api.juror.domain.DisqualificationLetter;
import uk.gov.hmcts.juror.api.juror.domain.DisqualificationLetterRepository;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.controller.request.summonsmanagement.DisqualifyJurorDto;
import uk.gov.hmcts.juror.api.moj.controller.response.summonsmanagement.DisqualifyReasonsDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseAuditMod;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.enumeration.DisqualifyCodeEnum;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
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
import java.util.Optional;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static uk.gov.hmcts.juror.api.moj.domain.IJurorStatus.DISQUALIFIED;

@RunWith(SpringRunner.class)
public class DisqualifyJurorServiceImplTest {

    @Mock
    private JurorPoolRepository jurorPoolRepository;
    @Mock
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    @Mock
    private JurorDigitalResponseRepositoryMod jurorDigitalResponseRepository;
    @Mock
    private JurorResponseAuditRepositoryMod jurorResponseAuditRepository;
    @Mock
    private JurorHistoryService jurorHistoryService;
    @Mock
    private DisqualificationLetterRepository disqualificationLetterRepository;
    @Mock
    private AssignOnUpdateServiceMod assignOnUpdateService;
    @Mock
    private SummonsReplyMergeService summonsReplyMergeService;
    @Mock
    private PrintDataService printDataService;


    @InjectMocks
    private DisqualifyJurorServiceImpl disqualifyJurorService;

    private static final String BUREAU_USER = "BUREAU_USER";
    private static final String JUROR_123456789 = "123456789";

    // Tests related to service method: getDisqualifyReasons()
    @Test
    public void getDisqualifyReasons_court_happy() {
        BureauJwtPayload courtPayload = buildBureauPayload();
        courtPayload.setOwner("411");
        DisqualifyReasonsDto disqualifyReasonsExpect = getDisqualifyReasons();

        DisqualifyReasonsDto disqualifyReasonsActual = disqualifyJurorService.getDisqualifyReasons(courtPayload);

        assertThat(disqualifyReasonsActual).usingRecursiveComparison().isEqualTo(disqualifyReasonsExpect);
        verify(jurorPoolRepository, never()).save(any());
        verify(jurorPaperResponseRepository, never()).findById(any());
        verify(jurorDigitalResponseRepository, never()).findByJurorNumber(any());
        verify(jurorResponseAuditRepository, never()).save(any());
        verifyNoInteractions(jurorHistoryService);
        verify(disqualificationLetterRepository, never()).save(any());
        verify(assignOnUpdateService, never()).assignToCurrentLogin(any(), any());

        verify(summonsReplyMergeService, never()).mergeDigitalResponse(any(), any());
        verify(summonsReplyMergeService, never()).mergePaperResponse(any(), any());
    }

    @Test
    public void getDisqualifyReasons_bureau_happy() {
        BureauJwtPayload courtPayload = buildBureauPayload();
        DisqualifyReasonsDto disqualifyReasonsExpect = getDisqualifyReasons();

        DisqualifyReasonsDto disqualifyReasonsActual = disqualifyJurorService.getDisqualifyReasons(courtPayload);

        assertThat(disqualifyReasonsActual).usingRecursiveComparison().isEqualTo(disqualifyReasonsExpect);
        verify(jurorPoolRepository, never()).save(any());
        verify(jurorPaperResponseRepository, never()).findById(any());
        verify(jurorDigitalResponseRepository, never()).findByJurorNumber(any());
        verify(jurorResponseAuditRepository, never()).save(any());
        verifyNoInteractions(jurorHistoryService);
        verify(disqualificationLetterRepository, never()).save(any());
        verify(assignOnUpdateService, never()).assignToCurrentLogin(any(), any());

        verify(summonsReplyMergeService, never()).mergeDigitalResponse(any(), any());
        verify(summonsReplyMergeService, never()).mergePaperResponse(any(), any());
    }

    @Test
    public void getDisqualifyReasons_manualVerification_happy() {
        BureauJwtPayload courtPayload = buildBureauPayload();
        courtPayload.setOwner("411");

        DisqualifyReasonsDto disqualifyReasonsActual = disqualifyJurorService.getDisqualifyReasons(courtPayload);
        List<DisqualifyReasonsDto.DisqualifyReasons> disqualifyReasonsListActual =
            disqualifyReasonsActual.getDisqualifyReasons();

        assertThat(disqualifyReasonsListActual.get(0).getCode()).isEqualTo("A");
        assertThat(disqualifyReasonsListActual.get(0).getDescription()).isEqualTo("Age");
        assertThat(disqualifyReasonsListActual.get(0).getHeritageCode()).isEqualTo("A");
        assertThat(disqualifyReasonsListActual.get(0).getHeritageDescription())
            .isEqualTo("Less Than Eighteen Years of Age or Over 75");

        assertThat(disqualifyReasonsListActual.get(1).getCode()).isEqualTo("B");
        assertThat(disqualifyReasonsListActual.get(1).getDescription()).isEqualTo("Bail");
        assertThat(disqualifyReasonsListActual.get(1).getHeritageCode()).isEqualTo("B");
        assertThat(disqualifyReasonsListActual.get(1).getHeritageDescription()).isEqualTo("On Bail");

        assertThat(disqualifyReasonsListActual.get(2).getCode()).isEqualTo("C");
        assertThat(disqualifyReasonsListActual.get(2).getDescription()).isEqualTo("Conviction");
        assertThat(disqualifyReasonsListActual.get(2).getHeritageCode()).isEqualTo("C");
        assertThat(disqualifyReasonsListActual.get(2).getHeritageDescription())
            .isEqualTo("Has Been Convicted of an Offence");

        assertThat(disqualifyReasonsListActual.get(3).getCode()).isEqualTo("D");
        assertThat(disqualifyReasonsListActual.get(3).getDescription()).isEqualTo("Judicial Disqualification");
        assertThat(disqualifyReasonsListActual.get(3).getHeritageCode()).isEqualTo("D");
        assertThat(disqualifyReasonsListActual.get(3).getHeritageDescription())
            .isEqualTo("Judicial Disqualification");

        assertThat(disqualifyReasonsListActual.get(4).getCode()).isEqualTo("E");
        assertThat(disqualifyReasonsListActual.get(4).getDescription())
            .isEqualTo("Electronic Police Check Failure");
        assertThat(disqualifyReasonsListActual.get(4).getHeritageCode()).isEqualTo("E");
        assertThat(disqualifyReasonsListActual.get(4).getHeritageDescription())
            .isEqualTo("Electronic Police Check Failure");

        assertThat(disqualifyReasonsListActual.get(5).getCode()).isEqualTo("J");
        assertThat(disqualifyReasonsListActual.get(5).getDescription())
            .isEqualTo("Involved in Justice Administration or a Member of the Clergy");
        assertThat(disqualifyReasonsListActual.get(5).getHeritageCode()).isEqualTo("J");
        assertThat(disqualifyReasonsListActual.get(5).getHeritageDescription())
            .isEqualTo("Involved in Justice Administration or a Member of the Clergy");

        assertThat(disqualifyReasonsListActual.get(6).getCode()).isEqualTo("M");
        assertThat(disqualifyReasonsListActual.get(6).getDescription())
            .isEqualTo("Suffering From a Mental Disorder");
        assertThat(disqualifyReasonsListActual.get(6).getHeritageCode()).isEqualTo("M");
        assertThat(disqualifyReasonsListActual.get(6)
            .getHeritageDescription()).isEqualTo("Suffering From a Mental Disorder");

        assertThat(disqualifyReasonsListActual.get(7).getCode()).isEqualTo("N");
        assertThat(disqualifyReasonsListActual.get(7).getDescription()).isEqualTo("Mental Capacity Act");
        assertThat(disqualifyReasonsListActual.get(7).getHeritageCode()).isEqualTo("M");
        assertThat(disqualifyReasonsListActual.get(7)
            .getHeritageDescription()).isEqualTo("Suffering From a Mental Disorder");

        assertThat(disqualifyReasonsListActual.get(8).getCode()).isEqualTo("O");
        assertThat(disqualifyReasonsListActual.get(8).getDescription()).isEqualTo("Mental Health Act");
        assertThat(disqualifyReasonsListActual.get(8).getHeritageCode()).isEqualTo("M");
        assertThat(disqualifyReasonsListActual.get(8).getHeritageDescription())
            .isEqualTo("Suffering From a Mental Disorder");

        assertThat(disqualifyReasonsListActual.get(9).getCode()).isEqualTo("R");
        assertThat(disqualifyReasonsListActual.get(9).getDescription()).isEqualTo("Residency");
        assertThat(disqualifyReasonsListActual.get(9).getHeritageCode()).isEqualTo("R");
        assertThat(disqualifyReasonsListActual.get(9).getHeritageDescription())
            .isEqualTo("Not Resident for the Appropriate Period");

        verify(jurorPoolRepository, never()).save(any());
        verify(jurorPaperResponseRepository, never()).findById(any());
        verify(jurorDigitalResponseRepository, never()).findByJurorNumber(any());
        verify(jurorResponseAuditRepository, never()).save(any());
        verifyNoInteractions(jurorHistoryService);
        verify(disqualificationLetterRepository, never()).save(any());
        verify(assignOnUpdateService, never()).assignToCurrentLogin(any(), any());

        verify(summonsReplyMergeService, never()).mergeDigitalResponse(any(), any());
        verify(summonsReplyMergeService, never()).mergePaperResponse(any(), any());
    }

    // Tests related to service method: disqualifyJuror()
    @Test
    public void disqualifyJuror_bureau_paper_happy() {
        String jurorNumber = JUROR_123456789;
        final ArgumentCaptor<String> jurorNumberCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Boolean> isActiveCaptor = ArgumentCaptor.forClass(Boolean.class);
        final ArgumentCaptor<PaperResponse> jurorPaperResponseEntityCaptor =
            ArgumentCaptor.forClass(PaperResponse.class);
        final ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<JurorPool> jurorPoolEntityCaptor = ArgumentCaptor.forClass(JurorPool.class);

        BureauJwtPayload courtPayload = buildBureauPayload();
        final DisqualifyJurorDto disqualifyJurorDto = createDisqualifyJurorDtoPaperB();
        List<JurorPool> jurorPoolList = createJurorPoolList(jurorNumber, courtPayload.getOwner());
        PaperResponse paperResponse = createPaperResponse(jurorNumber);

        //Paper specific
        doReturn(paperResponse).when(jurorPaperResponseRepository).findByJurorNumber(anyString());
        doNothing().when(summonsReplyMergeService).mergePaperResponse(any(PaperResponse.class), anyString());

        //Common - Paper and Digital
        doReturn(jurorPoolList).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
        doReturn(null).when(jurorPoolRepository).save(any(JurorPool.class));
        doReturn(null).when(disqualificationLetterRepository).save(any(DisqualificationLetter.class));

        disqualifyJurorService.disqualifyJuror(jurorNumber, disqualifyJurorDto, courtPayload);

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumberCaptor.capture(),
                isActiveCaptor.capture());
        assertThat(jurorNumberCaptor.getValue()).isEqualTo(jurorNumber);
        assertThat(isActiveCaptor.getValue()).isEqualTo(Boolean.TRUE);

        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(jurorNumberCaptor.capture());
        assertThat(jurorNumberCaptor.getValue()).isEqualTo(jurorNumber);

        verify(summonsReplyMergeService, times(1))
            .mergePaperResponse(jurorPaperResponseEntityCaptor.capture(),
                userCaptor.capture());
        assertThat(jurorPaperResponseEntityCaptor.getValue().getProcessingStatus()).isEqualTo(ProcessingStatus.CLOSED);
        assertThat(userCaptor.getValue()).isEqualTo(BUREAU_USER);

        verify(jurorPoolRepository, times(1)).save(jurorPoolEntityCaptor.capture());
        JurorPool capturedJurorPool = jurorPoolEntityCaptor.getValue();
        Juror capturedJuror = capturedJurorPool.getJuror();
        assertThat(capturedJuror.isResponded()).isEqualTo(Boolean.TRUE);
        assertThat(capturedJuror.getDisqualifyDate()).isNotNull();
        assertThat(capturedJurorPool.getUserEdtq()).isEqualTo(BUREAU_USER);
        assertThat(capturedJurorPool.getNextDate()).isNull();
        assertThat(capturedJurorPool.getStatus().getStatus()).isEqualTo(DISQUALIFIED);

        verify(jurorHistoryService).createDisqualifyHistory(jurorPoolList.get(0),"B");

        // TODO - verify the printDataServiceArgumentCaptor and approach to letters for disqualification

        //Services or repository methods specific to Digital response
        verify(jurorDigitalResponseRepository, never()).findByJurorNumber(anyString());
        verify(assignOnUpdateService, never()).assignToCurrentLogin(any(DigitalResponse.class),
            anyString());
        verify(summonsReplyMergeService, never()).mergeDigitalResponse(any(DigitalResponse.class), anyString());
        verify(jurorResponseAuditRepository, never()).save(any(JurorResponseAuditMod.class));
    }

    @Test
    public void disqualifyJuror_bureau_digital_happy() {
        String jurorNumber = JUROR_123456789;
        final ArgumentCaptor<String> jurorNumberCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<String> userCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Boolean> isActiveCaptor = ArgumentCaptor.forClass(Boolean.class);
        final ArgumentCaptor<DigitalResponse> jurorDigitalResponseEntityCaptor =
            ArgumentCaptor.forClass(DigitalResponse.class);
        final ArgumentCaptor<JurorPool> jurorPoolEntityCaptor = ArgumentCaptor.forClass(JurorPool.class);
        //      final ArgumentCaptor<PrintDataService> printDataServiceArgumentCaptor =
        //      ArgumentCaptor.forClass(PrintDataService.class);

        BureauJwtPayload courtPayload = buildBureauPayload();
        final DisqualifyJurorDto disqualifyJurorDto = createDisqualifyJurorDtoDigitalN();
        List<JurorPool> jurorPoolList = createJurorPoolList(jurorNumber, courtPayload.getOwner());
        DigitalResponse digitalResponse = createDigitalResponse(jurorNumber);

        //Digital Specific
        doReturn(digitalResponse).when(jurorDigitalResponseRepository).findByJurorNumber(anyString());
        doNothing().when(assignOnUpdateService).assignToCurrentLogin(any(DigitalResponse.class), anyString());
        //DIGITAL
        doNothing().when(summonsReplyMergeService).mergeDigitalResponse(any(DigitalResponse.class), anyString());

        //Common - Paper and Digital
        doReturn(jurorPoolList).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
        doReturn(null).when(jurorPoolRepository).save(any(JurorPool.class));
        doReturn(null).when(disqualificationLetterRepository).save(any(DisqualificationLetter.class));

        disqualifyJurorService.disqualifyJuror(jurorNumber, disqualifyJurorDto, courtPayload);

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumberCaptor.capture(),
                isActiveCaptor.capture());
        assertThat(jurorNumberCaptor.getValue()).isEqualTo(jurorNumber);
        assertThat(isActiveCaptor.getValue()).isEqualTo(Boolean.TRUE);

        verify(jurorDigitalResponseRepository, times(1))
            .findByJurorNumber(jurorNumberCaptor.capture());
        assertThat(jurorNumberCaptor.getValue()).isEqualTo(jurorNumber);

        verify(assignOnUpdateService, times(1))
            .assignToCurrentLogin(any(DigitalResponse.class), userCaptor.capture());
        assertThat(userCaptor.getValue()).isEqualTo(BUREAU_USER);

        verify(summonsReplyMergeService, times(1)).mergeDigitalResponse(
            jurorDigitalResponseEntityCaptor.capture(),
            userCaptor.capture());
        assertThat(jurorDigitalResponseEntityCaptor.getValue().getProcessingStatus()).isEqualTo(
            ProcessingStatus.CLOSED);
        assertThat(userCaptor.getValue()).isEqualTo(BUREAU_USER);

        verify(jurorPoolRepository, times(1)).save(jurorPoolEntityCaptor.capture());
        JurorPool capturedJurorPool = jurorPoolEntityCaptor.getValue();
        Juror capturedJuror = capturedJurorPool.getJuror();
        assertThat(capturedJuror.isResponded()).isEqualTo(Boolean.TRUE);
        assertThat(capturedJuror.getDisqualifyDate()).isNotNull();
        assertThat(capturedJurorPool.getUserEdtq()).isEqualTo(BUREAU_USER);
        assertThat(capturedJurorPool.getNextDate()).isNull();
        assertThat(capturedJurorPool.getStatus().getStatus()).isEqualTo(DISQUALIFIED);

        verify(jurorHistoryService).createDisqualifyHistory(jurorPoolList.get(0),"N");
        // TODO - verify the printDataServiceArgumentCaptor and approach to letters for disqualification
        //Services or repository methods specific to Paper response
        verify(jurorPaperResponseRepository, never()).findById(anyString());
        verify(summonsReplyMergeService, never()).mergePaperResponse(any(PaperResponse.class), anyString());
    }

    @Test
    public void disqualifyJuror_noActivePoolRecord() {
        BureauJwtPayload courtPayload = buildBureauPayload();
        DisqualifyJurorDto disqualifyJurorDto = createDisqualifyJurorDtoDigitalN();

        doReturn(new ArrayList<JurorPool>()).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(anyString(), anyBoolean());

        Assertions.assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            disqualifyJurorService.disqualifyJuror(JUROR_123456789, disqualifyJurorDto, courtPayload));

        //Digital related
        verify(jurorDigitalResponseRepository, never()).findByJurorNumber(anyString());
        verify(assignOnUpdateService, never()).assignToCurrentLogin(any(DigitalResponse.class), anyString());
        verify(summonsReplyMergeService, never()).mergeDigitalResponse(any(DigitalResponse.class), anyString());
        verify(jurorResponseAuditRepository, never()).save(any(JurorResponseAuditMod.class));

        //Paper related
        verify(jurorPaperResponseRepository, never()).findById(anyString());
        verify(summonsReplyMergeService, never()).mergePaperResponse(any(PaperResponse.class), anyString());

        //Common
        verify(disqualificationLetterRepository, never()).save(any(DisqualificationLetter.class));
        verifyNoInteractions(jurorHistoryService);
        verify(jurorPoolRepository, never()).save(any(JurorPool.class));
        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
    }

    @Test
    public void disqualifyJuror_noPaperResponse() {
        BureauJwtPayload courtPayload = buildBureauPayload();
        DisqualifyJurorDto disqualifyJurorDto = createDisqualifyJurorDtoPaperB();
        List<JurorPool> jurorPoolList = createJurorPoolList(JUROR_123456789, courtPayload.getOwner());

        doReturn(jurorPoolList).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
        doReturn(Optional.empty()).when(jurorPaperResponseRepository).findById(anyString());

        Assertions.assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            disqualifyJurorService.disqualifyJuror(JUROR_123456789, disqualifyJurorDto, courtPayload));

        //Digital related
        verify(jurorDigitalResponseRepository, never()).findByJurorNumber(anyString());
        verify(assignOnUpdateService, never()).assignToCurrentLogin(any(DigitalResponse.class), anyString());
        verify(summonsReplyMergeService, never()).mergeDigitalResponse(any(DigitalResponse.class), anyString());
        verify(jurorResponseAuditRepository, never()).save(any(JurorResponseAuditMod.class));

        //Paper related
        verify(jurorPaperResponseRepository, times(1)).findByJurorNumber(anyString());
        verify(summonsReplyMergeService, never()).mergePaperResponse(any(PaperResponse.class), anyString());

        //Common
        verify(disqualificationLetterRepository, never()).save(any(DisqualificationLetter.class));
        verifyNoInteractions(jurorHistoryService);
        verify(jurorPoolRepository, never()).save(any(JurorPool.class));
        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
    }

    @Test
    public void disqualifyJuror_noDigitalResponse() {
        BureauJwtPayload courtPayload = buildBureauPayload();
        DisqualifyJurorDto disqualifyJurorDto = createDisqualifyJurorDtoDigitalN();
        List<JurorPool> jurorPoolList = createJurorPoolList(JUROR_123456789, courtPayload.getOwner());

        doReturn(jurorPoolList).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
        doReturn(null).when(jurorDigitalResponseRepository).findByJurorNumber(anyString());

        Assertions.assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            disqualifyJurorService.disqualifyJuror(JUROR_123456789, disqualifyJurorDto, courtPayload));

        //Digital related
        verify(jurorDigitalResponseRepository, times(1)).findByJurorNumber(anyString());
        verify(assignOnUpdateService, never()).assignToCurrentLogin(any(DigitalResponse.class), anyString());
        verify(summonsReplyMergeService, never()).mergeDigitalResponse(any(DigitalResponse.class), anyString());
        verify(jurorResponseAuditRepository, never()).save(any(JurorResponseAuditMod.class));

        //Paper related
        verify(jurorPaperResponseRepository, never()).findById(anyString());
        verify(summonsReplyMergeService, never()).mergePaperResponse(any(PaperResponse.class), anyString());

        //Common
        verify(disqualificationLetterRepository, never()).save(any(DisqualificationLetter.class));
        verifyNoInteractions(jurorHistoryService);
        verify(jurorPoolRepository, never()).save(any(JurorPool.class));
        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
    }

    @Test
    public void disqualifyJuror_existingProcessingStatusIsComplete() {
        BureauJwtPayload courtPayload = buildBureauPayload();
        final DisqualifyJurorDto disqualifyJurorDto = createDisqualifyJurorDtoDigitalN();
        List<JurorPool> jurorPoolList = createJurorPoolList(JUROR_123456789, courtPayload.getOwner());

        DigitalResponse digitalResponse = createDigitalResponse(JUROR_123456789);
        digitalResponse.setProcessingComplete(Boolean.TRUE);

        doReturn(jurorPoolList).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
        doReturn(digitalResponse).when(jurorDigitalResponseRepository).findByJurorNumber(anyString());

        disqualifyJurorService.disqualifyJuror(JUROR_123456789, disqualifyJurorDto, courtPayload);

        // expect no further processing of response but still update pool and history

        //Digital related
        verify(jurorDigitalResponseRepository, times(1)).findByJurorNumber(anyString());
        verify(assignOnUpdateService, never()).assignToCurrentLogin(any(DigitalResponse.class), anyString());
        verify(summonsReplyMergeService, never()).mergeDigitalResponse(any(DigitalResponse.class), anyString());
        verify(jurorResponseAuditRepository, never()).save(any(JurorResponseAuditMod.class));

        //Paper related
        verify(jurorPaperResponseRepository, never()).findById(anyString());
        verify(summonsReplyMergeService, never()).mergePaperResponse(any(PaperResponse.class), anyString());

        //Common
        // TODO add verification for printDataServiceArgumentCaptor
        verify(jurorHistoryService).createDisqualifyHistory(jurorPoolList.get(0),"N");
        verify(jurorPoolRepository, times(1)).save(any(JurorPool.class));
        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(anyString(), anyBoolean());
    }

    private DisqualifyJurorDto createDisqualifyJurorDtoPaperB() {
        return DisqualifyJurorDto.builder()
            .code(DisqualifyCodeEnum.B)
            .replyMethod(ReplyMethod.PAPER)
            .build();
    }

    private DisqualifyJurorDto createDisqualifyJurorDtoDigitalN() {
        return DisqualifyJurorDto.builder()
            .code(DisqualifyCodeEnum.N)
            .replyMethod(ReplyMethod.DIGITAL)
            .build();
    }

    private PaperResponse createPaperResponse(String jurorNumber) {
        PaperResponse response = new PaperResponse();
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
        response.setProcessingStatus(ProcessingStatus.TODO);

        return response;
    }

    private DigitalResponse createDigitalResponse(String jurorNumber) {
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

        response.setProcessingStatus(ProcessingStatus.TODO);

        return response;
    }

    private BureauJwtPayload buildBureauPayload() {
        return BureauJwtPayload.builder()
            .userLevel("99")
            .login(BUREAU_USER)
            .owner("400")
            .build();
    }

    private DisqualifyReasonsDto getDisqualifyReasons() {
        List<DisqualifyReasonsDto.DisqualifyReasons> disqualifyReasons = new ArrayList<>();

        for (DisqualifyCodeEnum disqualifyCodeEnum : DisqualifyCodeEnum.values()) {
            DisqualifyReasonsDto.DisqualifyReasons disqualifyReason = DisqualifyReasonsDto.DisqualifyReasons
                .builder()
                .code(disqualifyCodeEnum.getCode())
                .description(disqualifyCodeEnum.getDescription())
                .heritageCode(disqualifyCodeEnum.getHeritageCode())
                .heritageDescription(disqualifyCodeEnum.getHeritageDescription())
                .build();
            disqualifyReasons.add(disqualifyReason);
        }

        return DisqualifyReasonsDto.builder().disqualifyReasons(disqualifyReasons).build();
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