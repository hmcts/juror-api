package uk.gov.hmcts.juror.api.moj.service;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.PaperResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.ReplyType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorDigitalResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorPaperResponseRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseAuditRepositoryMod;
import uk.gov.hmcts.juror.api.moj.repository.jurorresponse.JurorResponseCommonRepositoryMod;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(SpringExtension.class)
class UndeliverableResponseServiceTest {

    @Mock
    private JurorHistoryService jurorHistoryService;
    @Mock
    private JurorPoolService jurorPoolService;
    @Mock
    private  JurorPaperResponseRepositoryMod jurorPaperResponseRepository;
    @Mock
    private  JurorDigitalResponseRepositoryMod jurorDigitalResponseRepository;
    @Mock
    private  JurorResponseCommonRepositoryMod jurorResponseCommonRepositoryMod;
    @Mock
    private JurorResponseAuditRepositoryMod jurorResponseAuditRepository;

    @InjectMocks
    private UndeliverableResponseServiceImpl undeliverableResponseService;

    private static final String LOGIN = "BUREAU_USER";
    private static final String OWNER = "400";

    @BeforeEach
    void beforeEach() {
        TestUtils.mockSecurityUtil(
            BureauJwtPayload.builder()
                .owner(OWNER)
                .login(LOGIN)
                .build()
        );
    }

    @AfterEach
    void afterAll() {
        TestUtils.afterAll();
    }

    @Test
    void negativeJurorRecordDoesNotExist() {
        String jurorNumber = "111111111";

        Mockito.doThrow(MojException.NotFound.class).when(jurorPoolService)
            .getJurorPoolFromUser(jurorNumber);
        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(
            () -> undeliverableResponseService.markAsUndeliverable(List.of(jurorNumber)));

        Mockito.verify(jurorPoolService, Mockito.times(1))
            .getJurorPoolFromUser(jurorNumber);
        Mockito.verify(jurorPoolService, Mockito.never()).save(Mockito.any());
        Mockito.verify(jurorHistoryService, Mockito.never()).createUndeliveredSummonsHistory(Mockito.any());
    }


    @Test
    void negativeJurorRecordIsNotOwned() {
        String jurorNumber = "123456789";


        Mockito.doReturn(createValidJurorPool(jurorNumber, "415")).when(jurorPoolService)
            .getJurorPoolFromUser(jurorNumber);

        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(
            () -> undeliverableResponseService.markAsUndeliverable(List.of(jurorNumber)));

        Mockito.verify(jurorPoolService, Mockito.times(1))
            .getJurorPoolFromUser(jurorNumber);
        Mockito.verify(jurorPoolService, Mockito.never()).save(Mockito.any());
        Mockito.verify(jurorHistoryService, Mockito.never()).createUndeliveredSummonsHistory(Mockito.any());

    }

    @Test
    void positiveMarkJurorAsUndeliverable() {
        String jurorNumber = "222222225";

        JurorPool jurorPool = createValidJurorPool(jurorNumber, OWNER);
        Mockito.doReturn(jurorPool).when(jurorPoolService)
            .getJurorPoolFromUser(jurorNumber);

        undeliverableResponseService.markAsUndeliverable(List.of(jurorNumber));

        Mockito.verify(jurorPoolService, Mockito.times(1))
            .getJurorPoolFromUser(jurorNumber);
        Mockito.verify(jurorPoolService, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(jurorHistoryService, Mockito.times(1)).createUndeliveredSummonsHistory(jurorPool);
    }

    @Test
    void positiveMarkJurorAsUndeliverableMultiple() {
        String jurorNumber1 = "222222225";
        String jurorNumber2 = "222222226";

        JurorPool jurorPool1 = createValidJurorPool(jurorNumber1, OWNER);
        JurorPool jurorPool2 = createValidJurorPool(jurorNumber2, OWNER);

        Mockito.doReturn(jurorPool1).when(jurorPoolService)
            .getJurorPoolFromUser(jurorNumber1);
        Mockito.doReturn(jurorPool2).when(jurorPoolService)
            .getJurorPoolFromUser(jurorNumber2);

        undeliverableResponseService.markAsUndeliverable(List.of(jurorNumber1, jurorNumber2));

        Mockito.verify(jurorPoolService, Mockito.times(1))
            .getJurorPoolFromUser(jurorNumber1);
        Mockito.verify(jurorPoolService, Mockito.times(1))
            .getJurorPoolFromUser(jurorNumber2);
        Mockito.verify(jurorPoolService, Mockito.times(1)).save(jurorPool1);
        Mockito.verify(jurorPoolService, Mockito.times(1)).save(jurorPool2);


        Mockito.verify(jurorHistoryService, Mockito.times(1))
            .createUndeliveredSummonsHistory(jurorPool1);
        Mockito.verify(jurorHistoryService, Mockito.times(1))
            .createUndeliveredSummonsHistory(jurorPool2);
    }


    @Test
    void markAsUndeliverableUpdatesResponseToClosed() {
        String jurorNumber = "333333333";
        JurorPool jurorPool = createValidJurorPool(jurorNumber, OWNER);
        AbstractJurorResponse jurorResponse = new DigitalResponse();
        jurorResponse.setReplyType(new ReplyType("Digital", null));

        Mockito.doReturn(jurorPool).when(jurorPoolService).getJurorPoolFromUser(jurorNumber);
        Mockito.doReturn(jurorResponse).when(jurorResponseCommonRepositoryMod).findByJurorNumber(jurorNumber);

        undeliverableResponseService.markAsUndeliverable(List.of(jurorNumber));

        Mockito.verify(jurorResponseCommonRepositoryMod, Mockito.times(1)).findByJurorNumber(jurorNumber);
        Mockito.verify(jurorDigitalResponseRepository, Mockito.times(1)).save((DigitalResponse) jurorResponse);
        Mockito.verify(jurorPaperResponseRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void markAsUndeliverableWithNoResponse() {
        String jurorNumber = "444444444";
        JurorPool jurorPool = createValidJurorPool(jurorNumber, OWNER);

        Mockito.doReturn(jurorPool).when(jurorPoolService).getJurorPoolFromUser(jurorNumber);
        Mockito.doReturn(null).when(jurorResponseCommonRepositoryMod).findByJurorNumber(jurorNumber);

        undeliverableResponseService.markAsUndeliverable(List.of(jurorNumber));

        Mockito.verify(jurorResponseCommonRepositoryMod, Mockito.times(1)).findByJurorNumber(jurorNumber);
        Mockito.verify(jurorDigitalResponseRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(jurorPaperResponseRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    void markAsUndeliverableUpdatesPaperResponseToClosed() {
        String jurorNumber = "555555555";
        JurorPool jurorPool = createValidJurorPool(jurorNumber, OWNER);
        AbstractJurorResponse jurorResponse = new PaperResponse();
        jurorResponse.setReplyType(new ReplyType("Paper", null));

        Mockito.doReturn(jurorPool).when(jurorPoolService).getJurorPoolFromUser(jurorNumber);
        Mockito.doReturn(jurorResponse).when(jurorResponseCommonRepositoryMod).findByJurorNumber(jurorNumber);

        undeliverableResponseService.markAsUndeliverable(List.of(jurorNumber));

        Mockito.verify(jurorResponseCommonRepositoryMod, Mockito.times(1)).findByJurorNumber(jurorNumber);
        Mockito.verify(jurorPaperResponseRepository, Mockito.times(1)).save((PaperResponse) jurorResponse);
        Mockito.verify(jurorDigitalResponseRepository, Mockito.never()).save(Mockito.any());
    }


    private JurorPool createValidJurorPool(String jurorNumber, String owner) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCourtName("CHESTER");
        courtLocation.setLocCode("415");

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("415230101");
        poolRequest.setCourtLocation(courtLocation);

        Juror juror = new Juror();
        juror.setJurorNumber(jurorNumber);
        juror.setFirstName("jurorPool1");
        juror.setLastName("jurorPool1L");
        juror.setPostcode("M24 4GT");
        juror.setTitle("jurorPoolTitle");

        juror.setAddressLine1("549 STREET NAME");
        juror.setAddressLine2("ANYTOWN");
        juror.setAddressLine3("ANYCOUNTRY");
        juror.setAddressLine4("");
        juror.setAddressLine5("");

        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus((9));
        jurorStatus.setStatusDesc("Responded");
        jurorStatus.setActive(true);

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(owner);
        jurorPool.setUserEdtq("BUREAU_USER");
        jurorPool.setStatus(jurorStatus);
        jurorPool.setNextDate(null);
        jurorPool.setPool(poolRequest);

        juror.setAssociatedPools(Set.of(jurorPool));
        jurorPool.setJuror(juror);

        return jurorPool;
    }


}
