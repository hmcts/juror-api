package uk.gov.hmcts.juror.api.moj.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.MarkAsDeceasedDto;
import uk.gov.hmcts.juror.api.moj.domain.ContactCode;
import uk.gov.hmcts.juror.api.moj.domain.ContactEnquiryCode;
import uk.gov.hmcts.juror.api.moj.domain.ContactEnquiryType;
import uk.gov.hmcts.juror.api.moj.domain.IContactCode;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.ContactCodeRepository;
import uk.gov.hmcts.juror.api.moj.repository.ContactEnquiryTypeRepository;
import uk.gov.hmcts.juror.api.moj.repository.ContactLogRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@RunWith(SpringRunner.class)
public class DeceasedResponseServiceTest {
    @Mock
    private JurorHistoryRepository jurorHistoryRepository;
    @Mock
    private ContactCodeRepository contactCodeRepository;
    @Mock
    private JurorRepository jurorRepository;
    @Mock
    private JurorPoolRepository jurorPoolRepository;
    @Mock
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    @Mock
    private ContactEnquiryTypeRepository contactEnquiryTypeRepository;
    @Mock
    private ContactLogRepository contactLogRepository;

    public static final String UNABLE_TO_ATTEND = "UA";


    @InjectMocks
    private DeceasedResponseServiceImpl deceasedResponseService;


    @Test
    public void test_jurorRecordDoesNotExist() {
        final String owner = "400";
        String jurorNumber = "123456789";

        MarkAsDeceasedDto markAsDeceasedDto = new MarkAsDeceasedDto();

        markAsDeceasedDto.setJurorNumber(jurorNumber);
        markAsDeceasedDto.setDeceasedComment("Brother phoned in to say juror is deceased.");
        markAsDeceasedDto.setPaperResponseExists(false);

        List<JurorPool> jurorPools = new ArrayList<>();

        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);
        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(
            () -> deceasedResponseService.markAsDeceased(buildPayload(owner), markAsDeceasedDto));

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);
        Mockito.verify(jurorPoolRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void test_jurorRecordIsNotOwned() {
        final String owner = "415";
        String jurorNumber = "123456789";

        MarkAsDeceasedDto markAsDeceasedDto = new MarkAsDeceasedDto();

        markAsDeceasedDto.setJurorNumber(jurorNumber);
        markAsDeceasedDto.setDeceasedComment("Brother phoned in to say juror is deceased.");
        markAsDeceasedDto.setPaperResponseExists(false);

        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(createValidJurorPool(jurorNumber, "400"));

        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(
            () -> deceasedResponseService.markAsDeceased(buildPayload(owner), markAsDeceasedDto));

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.verify(jurorPoolRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void test_markJurorAsDeceased_paperResponseExists() {
        String owner = "400";
        String jurorNumber = "222222225";

        MarkAsDeceasedDto markAsDeceasedDto = new MarkAsDeceasedDto();

        markAsDeceasedDto.setJurorNumber(jurorNumber);
        markAsDeceasedDto.setDeceasedComment("Brother phoned in to say juror is deceased.");
        markAsDeceasedDto.setPaperResponseExists(true);

        JurorPool jurorPool = createValidJurorPool(jurorNumber, owner);

        List<JurorPool> jurorPools = new ArrayList<>();

        jurorPools.add(jurorPool);

        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);

        ContactEnquiryType enquiryType = new ContactEnquiryType(ContactEnquiryCode.valueOf(UNABLE_TO_ATTEND),
            "description");

        Optional<ContactEnquiryType> enquiryTypeOpt = Optional.of(enquiryType);

        Mockito.doReturn(enquiryTypeOpt).when(contactEnquiryTypeRepository)
            .findById(ContactEnquiryCode.valueOf(UNABLE_TO_ATTEND));

        ContactCode contactCode = new ContactCode(
            IContactCode.UNABLE_TO_ATTEND.getCode(),
            IContactCode.UNABLE_TO_ATTEND.getDescription());
        Mockito.when(contactCodeRepository.findById(
            IContactCode.UNABLE_TO_ATTEND.getCode())).thenReturn(Optional.of(contactCode));

        deceasedResponseService.markAsDeceased(buildPayload(owner), markAsDeceasedDto);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(Mockito.any(), Mockito.anyBoolean());
        Mockito.verify(jurorPoolRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(contactCodeRepository, Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(contactLogRepository, Mockito.times(1)).saveAndFlush(Mockito.any());
    }

    @Test
    public void test_markJurorAsDeceased_paperResponseDoesNotExists() {
        String owner = "400";
        String jurorNumber = "222222225";

        MarkAsDeceasedDto markAsDeceasedDto = new MarkAsDeceasedDto();

        markAsDeceasedDto.setJurorNumber(jurorNumber);
        markAsDeceasedDto.setDeceasedComment("Brother phoned in to say juror is deceased.");
        markAsDeceasedDto.setPaperResponseExists(false);

        JurorPool jurorPool = createValidJurorPool(jurorNumber, owner);

        List<JurorPool> jurorPools = new ArrayList<>();

        jurorPools.add(jurorPool);

        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);

        ContactEnquiryType enquiryType = new ContactEnquiryType(ContactEnquiryCode.valueOf(UNABLE_TO_ATTEND),
            "description");

        Optional<ContactEnquiryType> enquiryTypeOpt = Optional.of(enquiryType);

        Mockito.doReturn(enquiryTypeOpt).when(contactEnquiryTypeRepository)
            .findById(ContactEnquiryCode.valueOf(UNABLE_TO_ATTEND));

        ContactCode contactCode = new ContactCode(
            IContactCode.UNABLE_TO_ATTEND.getCode(),
            IContactCode.UNABLE_TO_ATTEND.getDescription());
        Mockito.when(contactCodeRepository.findById(
            IContactCode.UNABLE_TO_ATTEND.getCode())).thenReturn(Optional.of(contactCode));

        deceasedResponseService.markAsDeceased(buildPayload(owner), markAsDeceasedDto);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(Mockito.any(), Mockito.anyBoolean());
        Mockito.verify(jurorPoolRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(contactCodeRepository, Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(contactLogRepository, Mockito.times(1)).saveAndFlush(Mockito.any());
    }


    //add positive tests for deceased excusal (with and without minimal paper response)

    private BureauJWTPayload buildPayload(String owner) {
        return BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("BUREAU_USER")
            .daysToExpire(89)
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
