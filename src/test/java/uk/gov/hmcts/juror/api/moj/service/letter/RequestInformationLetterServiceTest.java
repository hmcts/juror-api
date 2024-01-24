package uk.gov.hmcts.juror.api.moj.service.letter;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.moj.controller.request.AdditionalInformationDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.domain.letter.RequestLetter;
import uk.gov.hmcts.juror.api.moj.enumeration.ReplyMethod;
import uk.gov.hmcts.juror.api.moj.enumeration.letter.MissingInformation;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.letter.RequestLetterRepository;
import uk.gov.hmcts.juror.api.moj.service.SummonsReplyStatusUpdateService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@RunWith(SpringRunner.class)
public class RequestInformationLetterServiceTest {

    @Mock
    private RequestLetterRepository requestLetterRepository;
    @Mock
    private SummonsReplyStatusUpdateService summonsReplyStatusUpdateService;
    @Mock
    private JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    @Mock
    private JurorPoolRepository jurorPoolRepository;
    @Mock
    private JurorDigitalResponseRepositoryMod jurorResponseRepository;

    @InjectMocks
    RequestInformationLetterServiceImpl requestInformationLetterService;


    // testing scenario when Digital response is valid and updated to AWAITING_CONTACT processing state
    @Test
    public void queueRequestLetter_BureauUser_DigitalResponse_HappyPath() {
        String owner = "400";
        String jurorNumber = "123456789";

        DigitalResponse jurorResponse = new DigitalResponse();
        jurorResponse.setProcessingStatus(ProcessingStatus.TODO);

        final BureauJWTPayload payload = TestUtils.createJwt(owner, "BUREAU_USER");

        AdditionalInformationDto additionalInformationDto = getAdditionalInformationDto(jurorNumber);
        additionalInformationDto.setReplyMethod(ReplyMethod.DIGITAL);

        JurorPool jurorPool = createJurorPool(jurorNumber, owner);
        jurorPool.getJuror().setWelsh(true);

        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(jurorPool);

        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);
        Mockito.doNothing().when(summonsReplyStatusUpdateService)
            .updateJurorResponseStatus(jurorNumber, ProcessingStatus.AWAITING_CONTACT, payload);

        requestInformationLetterService.requestInformation(payload, additionalInformationDto);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);
        Mockito.verify(requestLetterRepository, Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(requestLetterRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(summonsReplyStatusUpdateService, Mockito.times(1))
            .updateDigitalJurorResponseStatus(jurorNumber, ProcessingStatus.AWAITING_CONTACT, payload);
    }

    // testing scenario when paper response is missing a signature (manual letter required instead)
    @Test
    public void queueRequestLetter_BureauUser_PaperResponse_SignatureMissing() {
        String owner = "400";
        String jurorNumber = "123456789";

        PaperResponse jurorPaperResponse = new PaperResponse();
        jurorPaperResponse.setProcessingStatus(ProcessingStatus.TODO);

        BureauJWTPayload payload = TestUtils.createJwt(owner, "BUREAU_USER");

        AdditionalInformationDto additionalInformationDto = new AdditionalInformationDto(jurorNumber, ReplyMethod.PAPER,
            Collections.singletonList(MissingInformation.SIGNATURE));

        Mockito.doReturn(Collections.singletonList(createJurorPool(jurorNumber, owner))).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        Assertions.assertThatExceptionOfType(MojException.BadRequest.class).isThrownBy(() -> {
            requestInformationLetterService.requestInformation(payload, additionalInformationDto);
        });

        Mockito.verify(jurorPoolRepository, Mockito.never())
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.verify(requestLetterRepository, Mockito.never()).findById(Mockito.any());
        Mockito.verify(requestLetterRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(summonsReplyStatusUpdateService, Mockito.never())
            .updateJurorResponseStatus(jurorNumber, ProcessingStatus.AWAITING_CONTACT, payload);
    }

    // testing scenario when paper response is missing a signature (manual letter required instead)
    @Test
    public void queueRequestLetter_BureauUser_PaperResponse_Welsh() {
        final ArgumentCaptor<RequestLetter> requestLetterArgumentCaptor = ArgumentCaptor.forClass(RequestLetter.class);
        String owner = "400";
        String jurorNumber = "123456789";

        PaperResponse jurorPaperResponse = new PaperResponse();
        jurorPaperResponse.setProcessingStatus(ProcessingStatus.TODO);

        final BureauJWTPayload payload = TestUtils.createJwt(owner, "BUREAU_USER");

        final AdditionalInformationDto additionalInformationDto = getAdditionalInformationDto(jurorNumber);

        JurorPool jurorPool = createJurorPool(jurorNumber, owner);
        jurorPool.getJuror().setWelsh(true);

        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(jurorPool);

        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);
        Mockito.doNothing().when(summonsReplyStatusUpdateService)
            .updateDigitalJurorResponseStatus(jurorNumber, ProcessingStatus.AWAITING_CONTACT, payload);

        requestInformationLetterService.requestInformation(payload, additionalInformationDto);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);
        Mockito.verify(requestLetterRepository, Mockito.times(1)).findById(Mockito.any());

        Mockito.verify(requestLetterRepository, Mockito.times(1)).save(requestLetterArgumentCaptor.capture());
        RequestLetter requestLetter = requestLetterArgumentCaptor.getValue();
        Assertions.assertThat(requestLetter.getRequiredInformation())
            .isEqualTo(
                MissingInformation.buildMissingInformationString(additionalInformationDto.getMissingInformation(),
                    true));

        Mockito.verify(summonsReplyStatusUpdateService, Mockito.times(1))
            .updateJurorResponseStatus(jurorNumber, ProcessingStatus.AWAITING_CONTACT, payload);
    }

    // testing scenario when associated juror record does not exist
    @Test
    public void queueRequestLetter_BureauUser_JurorRecordDoesNotExist() {
        String owner = "400";
        String jurorNumber = "123456789";

        PaperResponse jurorPaperResponse = new PaperResponse();
        jurorPaperResponse.setProcessingStatus(ProcessingStatus.AWAITING_CONTACT);

        // an empty list of pool members required for test
        List<JurorPool> jurorPools = new ArrayList<>();

        BureauJWTPayload payload = TestUtils.createJwt(owner, "BUREAU_USER");

        AdditionalInformationDto additionalInformationDto = getAdditionalInformationDto(jurorNumber);

        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);

        assertThatExceptionOfType(MojException.NotFound.class)
            .isThrownBy(() -> requestInformationLetterService.requestInformation(payload, additionalInformationDto));

        Mockito.verify(summonsReplyStatusUpdateService, Mockito.never())
            .updateJurorResponseStatus(jurorNumber, ProcessingStatus.AWAITING_CONTACT, payload);
        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);
        Mockito.verify(requestLetterRepository, Mockito.never()).findById(Mockito.any());
        Mockito.verify(requestLetterRepository, Mockito.never()).save(Mockito.any());
    }

    // testing scenario when associated juror record is not owned by user
    @Test
    public void queueRequestLetter_BureauUser_JurorRecordNotOwned() {
        final String owner = "400";
        String jurorNumber = "123456789";

        PaperResponse jurorPaperResponse = new PaperResponse();
        jurorPaperResponse.setProcessingStatus(ProcessingStatus.AWAITING_CONTACT);

        // an empty list of pool members required for test
        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner("415");

        juror.setAssociatedPools(Set.of(jurorPool));
        jurorPool.setJuror(juror);

        List<JurorPool> jurorPools = new ArrayList<>();
        jurorPools.add(jurorPool);

        BureauJWTPayload payload = TestUtils.createJwt(owner, "BUREAU_USER");

        AdditionalInformationDto additionalInformationDto = getAdditionalInformationDto(jurorNumber);

        Mockito.doReturn(jurorPools).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);
        Mockito.doNothing().when(summonsReplyStatusUpdateService)
            .updateJurorResponseStatus(jurorNumber, ProcessingStatus.AWAITING_CONTACT, payload);

        Assertions.assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(() ->
            requestInformationLetterService.requestInformation(payload, additionalInformationDto));

        Mockito.verify(summonsReplyStatusUpdateService, Mockito.never())
            .updateJurorResponseStatus(jurorNumber, ProcessingStatus.AWAITING_CONTACT, payload);
        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActiveOrderByPoolReturnDateDesc(jurorNumber, true);
        Mockito.verify(requestLetterRepository, Mockito.never()).findById(Mockito.any());
        Mockito.verify(requestLetterRepository, Mockito.never()).save(Mockito.any());
    }

    private AdditionalInformationDto getAdditionalInformationDto(String jurorNumber) {

        List<MissingInformation> missingInformationList =
            Arrays.asList(MissingInformation.BAIL, MissingInformation.CONVICTIONS);

        return AdditionalInformationDto.builder()
            .jurorNumber(jurorNumber)
            .replyMethod(ReplyMethod.PAPER)
            .missingInformation(missingInformationList)
            .build();
    }

    private JurorPool createJurorPool(String jurorNumber, String owner) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCourtName("CHESTER");
        courtLocation.setLocCode("415");

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setCourtLocation(courtLocation);
        poolRequest.setPoolNumber("415230101");

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(owner);
        jurorPool.setPool(poolRequest);
        jurorPool.setJuror(juror);

        juror.setAssociatedPools(Set.of(jurorPool));

        return jurorPool;
    }

}
