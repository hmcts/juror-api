package uk.gov.hmcts.juror.api.moj.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.controller.request.MarkAsDeceasedDto;
import uk.gov.hmcts.juror.api.moj.domain.ContactCode;
import uk.gov.hmcts.juror.api.moj.domain.ContactEnquiryCode;
import uk.gov.hmcts.juror.api.moj.domain.ContactEnquiryType;
import uk.gov.hmcts.juror.api.moj.domain.IContactCode;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.ContactCodeRepository;
import uk.gov.hmcts.juror.api.moj.repository.ContactEnquiryTypeRepository;
import uk.gov.hmcts.juror.api.moj.repository.ContactLogRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseCommonRepositoryMod;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeceasedResponseServiceTest {
    @Mock
    private JurorHistoryRepository jurorHistoryRepository;
    @Mock
    private ContactCodeRepository contactCodeRepository;
    @Mock
    private JurorRepository jurorRepository;
    @Mock
    private JurorPoolRepository jurorPoolRepository;
    @Mock
    private JurorResponseCommonRepositoryMod jurorResponseCommonRepositoryMod;
    @Mock
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    @Mock
    private JurorDigitalResponseRepositoryMod jurorDigitalResponseRepository;
    @Mock
    private ContactEnquiryTypeRepository contactEnquiryTypeRepository;
    @Mock
    private ContactLogRepository contactLogRepository;

    public static final String UNABLE_TO_ATTEND = "UA";

    @InjectMocks
    private DeceasedResponseServiceImpl deceasedResponseService;


    @BeforeEach
    void beforeEach() {
        jurorHistoryRepository = mock(JurorHistoryRepository.class);
        contactCodeRepository = mock(ContactCodeRepository.class);
        jurorRepository = mock(JurorRepository.class);
        jurorPoolRepository = mock(JurorPoolRepository.class);
        jurorResponseCommonRepositoryMod = mock(JurorResponseCommonRepositoryMod.class);
        jurorPaperResponseRepository = mock(JurorPaperResponseRepositoryMod.class);
        jurorDigitalResponseRepository = mock(JurorDigitalResponseRepositoryMod.class);
        contactEnquiryTypeRepository = mock(ContactEnquiryTypeRepository.class);
        contactLogRepository = mock(ContactLogRepository.class);

        this.deceasedResponseService = new DeceasedResponseServiceImpl(
            contactCodeRepository,
            jurorHistoryRepository,
            jurorPoolRepository,
            jurorRepository,
            jurorPaperResponseRepository,
            jurorDigitalResponseRepository,
            jurorResponseCommonRepositoryMod,
            contactLogRepository
        );
    }

    @Test
    void jurorRecordDoesNotExist() {
        final String owner = "400";
        String jurorNumber = "123456789";

        MarkAsDeceasedDto markAsDeceasedDto = new MarkAsDeceasedDto();

        markAsDeceasedDto.setJurorNumber(jurorNumber);
        markAsDeceasedDto.setDeceasedComment("Brother phoned in to say juror is deceased.");
        markAsDeceasedDto.setPaperResponseExists(false);

        List<JurorPool> jurorPools = new ArrayList<>();

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);
        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(
            () -> deceasedResponseService.markAsDeceased(buildPayload(owner), markAsDeceasedDto));

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);
        verify(jurorPoolRepository, never()).save(any());
    }

    @Test
    void jurorRecordIsNotOwned() {
        final String owner = "415";
        String jurorNumber = "123456789";

        MarkAsDeceasedDto markAsDeceasedDto = new MarkAsDeceasedDto();

        markAsDeceasedDto.setJurorNumber(jurorNumber);
        markAsDeceasedDto.setDeceasedComment("Brother phoned in to say juror is deceased.");
        markAsDeceasedDto.setPaperResponseExists(false);

        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createValidJurorPool(jurorNumber, "400"));

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(
            () -> deceasedResponseService.markAsDeceased(buildPayload(owner), markAsDeceasedDto));

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        verify(jurorPoolRepository, never()).save(any());
    }

    @Test
    void markJurorAsDeceasedPaperResponseExistsFlagTrue() {
        String owner = "400";
        String jurorNumber = "222222225";

        MarkAsDeceasedDto markAsDeceasedDto = new MarkAsDeceasedDto();

        markAsDeceasedDto.setJurorNumber(jurorNumber);
        markAsDeceasedDto.setDeceasedComment("Brother phoned in to say juror is deceased.");
        markAsDeceasedDto.setPaperResponseExists(true);

        JurorPool jurorPool = createValidJurorPool(jurorNumber, owner);

        List<JurorPool> jurorPools = new ArrayList<>();

        jurorPools.add(jurorPool);

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);

        ContactEnquiryType enquiryType = new ContactEnquiryType(ContactEnquiryCode.valueOf(UNABLE_TO_ATTEND),
            "description");

        Optional<ContactEnquiryType> enquiryTypeOpt = Optional.of(enquiryType);

        doReturn(enquiryTypeOpt).when(contactEnquiryTypeRepository)
            .findById(ContactEnquiryCode.valueOf(UNABLE_TO_ATTEND));

        ContactCode contactCode = new ContactCode(
            IContactCode.UNABLE_TO_ATTEND.getCode(),
            IContactCode.UNABLE_TO_ATTEND.getDescription());
        when(contactCodeRepository.findById(
            IContactCode.UNABLE_TO_ATTEND.getCode())).thenReturn(Optional.of(contactCode));

        deceasedResponseService.markAsDeceased(buildPayload(owner), markAsDeceasedDto);

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), anyBoolean());
        verify(jurorPoolRepository, times(1)).save(any());
        verify(contactCodeRepository, times(1)).findById(any());
        verify(jurorPaperResponseRepository, times(1)).save(any());
        verify(contactLogRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void markJurorAsDeceasedPaperResponseExists() {
        String owner = "400";
        String jurorNumber = "222222225";

        MarkAsDeceasedDto markAsDeceasedDto = new MarkAsDeceasedDto();

        markAsDeceasedDto.setJurorNumber(jurorNumber);
        markAsDeceasedDto.setDeceasedComment("Brother phoned in to say juror is deceased.");
        markAsDeceasedDto.setPaperResponseExists(false); // this is the default value

        JurorPool jurorPool = createValidJurorPool(jurorNumber, owner);
        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(jurorPool);

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);

        ContactEnquiryType enquiryType = new ContactEnquiryType(ContactEnquiryCode.valueOf(UNABLE_TO_ATTEND),
            "description");

        Optional<ContactEnquiryType> enquiryTypeOpt = Optional.of(enquiryType);

        doReturn(enquiryTypeOpt).when(contactEnquiryTypeRepository)
            .findById(ContactEnquiryCode.valueOf(UNABLE_TO_ATTEND));

        ContactCode contactCode = new ContactCode(
            IContactCode.UNABLE_TO_ATTEND.getCode(),
            IContactCode.UNABLE_TO_ATTEND.getDescription());
        when(contactCodeRepository.findById(
            IContactCode.UNABLE_TO_ATTEND.getCode())).thenReturn(Optional.of(contactCode));

        doReturn(null).when(jurorDigitalResponseRepository)
            .findByJurorNumber(jurorNumber);

        PaperResponse jurorResponse = new PaperResponse();
        jurorResponse.setJurorNumber(jurorNumber);

        doReturn(jurorResponse).when(jurorResponseCommonRepositoryMod)
            .findByJurorNumber(jurorNumber);

        doReturn(null).when(jurorPaperResponseRepository)
            .save(any(PaperResponse.class));

        deceasedResponseService.markAsDeceased(buildPayload(owner), markAsDeceasedDto);

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), anyBoolean());
        verify(jurorPoolRepository, times(1)).save(any());
        verify(jurorResponseCommonRepositoryMod, times(1))
            .findByJurorNumber(jurorNumber);

        final ArgumentCaptor<PaperResponse> PaperResponseCaptor = ArgumentCaptor.forClass(PaperResponse.class);

        verify(jurorPaperResponseRepository, times(1))
            .save(PaperResponseCaptor.capture());

        PaperResponse paperResponse = PaperResponseCaptor.getValue();
        assertThat(paperResponse.getJurorNumber()).isEqualTo(jurorNumber);
        assertThat(paperResponse.getProcessingStatus()).isEqualTo(ProcessingStatus.CLOSED);
        assertThat(paperResponse.getProcessingComplete()).isTrue();
        assertThat(paperResponse.getCompletedAt()).isNotNull();

        final ArgumentCaptor<Juror> jurorArgumentCaptor = ArgumentCaptor.forClass(Juror.class);

        verify(jurorRepository, times(1))
            .save(jurorArgumentCaptor.capture());

        Juror juror = jurorArgumentCaptor.getValue();
        assertThat(juror.getJurorNumber()).isEqualTo(jurorNumber);
        assertThat(juror.getExcusalCode()).isEqualTo("D");
        assertThat(juror.getExcusalDate()).isEqualTo(LocalDate.now(ZoneId.systemDefault()));
        assertThat(juror.isResponded()).isTrue();

        verify(contactCodeRepository, times(1)).findById(any());
        verify(contactLogRepository, times(1)).saveAndFlush(any());
    }

    @Test
    void markJurorAsDeceasedDigitalResponseExists() {
        String owner = "400";
        String jurorNumber = "222222225";

        MarkAsDeceasedDto markAsDeceasedDto = new MarkAsDeceasedDto();

        markAsDeceasedDto.setJurorNumber(jurorNumber);
        markAsDeceasedDto.setDeceasedComment("Brother phoned in to say juror is deceased.");
        markAsDeceasedDto.setPaperResponseExists(false); // this is the default value

        JurorPool jurorPool = createValidJurorPool(jurorNumber, owner);
        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(jurorPool);

        doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);

        ContactEnquiryType enquiryType = new ContactEnquiryType(ContactEnquiryCode.valueOf(UNABLE_TO_ATTEND),
            "description");

        Optional<ContactEnquiryType> enquiryTypeOpt = Optional.of(enquiryType);

        doReturn(enquiryTypeOpt).when(contactEnquiryTypeRepository)
            .findById(ContactEnquiryCode.valueOf(UNABLE_TO_ATTEND));

        ContactCode contactCode = new ContactCode(
            IContactCode.UNABLE_TO_ATTEND.getCode(),
            IContactCode.UNABLE_TO_ATTEND.getDescription());
        when(contactCodeRepository.findById(
            IContactCode.UNABLE_TO_ATTEND.getCode())).thenReturn(Optional.of(contactCode));
        
        DigitalResponse jurorResponse = new DigitalResponse();
        jurorResponse.setJurorNumber(jurorNumber);

        doReturn(jurorResponse).when(jurorResponseCommonRepositoryMod)
            .findByJurorNumber(jurorNumber);
        doReturn(null).when(jurorDigitalResponseRepository)
            .save(any(DigitalResponse.class));

        deceasedResponseService.markAsDeceased(buildPayload(owner), markAsDeceasedDto);

        verify(jurorPoolRepository, times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(any(), anyBoolean());
        verify(jurorPoolRepository, times(1)).save(any());
        verify(jurorResponseCommonRepositoryMod, times(1))
            .findByJurorNumber(jurorNumber);

        verify(jurorDigitalResponseRepository, times(1))
            .save(any(DigitalResponse.class));

        final ArgumentCaptor<DigitalResponse> digitalResponseCaptor = ArgumentCaptor.forClass(DigitalResponse.class);

        verify(jurorDigitalResponseRepository, times(1))
            .save(digitalResponseCaptor.capture());

        DigitalResponse digitalResponse = digitalResponseCaptor.getValue();
        assertThat(digitalResponse.getJurorNumber()).isEqualTo(jurorNumber);
        assertThat(digitalResponse.getProcessingStatus()).isEqualTo(ProcessingStatus.CLOSED);
        assertThat(digitalResponse.getProcessingComplete()).isTrue();
        assertThat(digitalResponse.getCompletedAt()).isNotNull();

        final ArgumentCaptor<Juror> jurorArgumentCaptor = ArgumentCaptor.forClass(Juror.class);

        verify(jurorRepository, times(1))
            .save(jurorArgumentCaptor.capture());

        Juror juror = jurorArgumentCaptor.getValue();
        assertThat(juror.getJurorNumber()).isEqualTo(jurorNumber);
        assertThat(juror.getExcusalCode()).isEqualTo("D");
        assertThat(juror.getExcusalDate()).isEqualTo(LocalDate.now(ZoneId.systemDefault()));
        assertThat(juror.isResponded()).isTrue();

        verify(jurorPaperResponseRepository, times(0))
            .findByJurorNumber(anyString());
        verify(contactCodeRepository, times(1)).findById(any());
        verify(contactLogRepository, times(1)).saveAndFlush(any());
    }

    private BureauJwtPayload buildPayload(String owner) {
        return BureauJwtPayload.builder()
            .userLevel("99")
            .login("BUREAU_USER")
            .owner(owner)
            .build();
    }

    private JurorPool createValidJurorPool(String jurorNumber, String owner) {

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("415");
        courtLocation.setLocCourtName("CHESTER");

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("415230101");
        poolRequest.setCourtLocation(courtLocation);

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);
        juror.setTitle("jurorPoolTitle");
        juror.setFirstName("jurorPool1");
        juror.setLastName("jurorPool1L");
        juror.setPostcode("M24 4GT");
        juror.setAddressLine1("549 STREET NAME");
        juror.setAddressLine2("ANYTOWN");
        juror.setAddressLine3("ANYCOUNTRY");
        juror.setAddressLine4("");
        juror.setAddressLine5("");

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

}
