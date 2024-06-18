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
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(SpringExtension.class)
class UndeliverableResponseServiceTest {

    @Mock
    private JurorHistoryService jurorHistoryService;
    @Mock
    private JurorPoolRepository jurorPoolRepository;

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

        List<JurorPool> members = new ArrayList<>();

        Mockito.doReturn(members).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(
            () -> undeliverableResponseService.markAsUndeliverable(List.of(jurorNumber)));

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.verify(jurorPoolRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(jurorHistoryService, Mockito.never()).createUndeliveredSummonsHistory(Mockito.any());
    }


    @Test
    void negativeJurorRecordIsNotOwned() {
        String jurorNumber = "123456789";

        List<JurorPool> members = new ArrayList<>();
        members.add(createValidJurorPool(jurorNumber, "415"));

        Mockito.doReturn(members).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(
            () -> undeliverableResponseService.markAsUndeliverable(List.of(jurorNumber)));

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.verify(jurorPoolRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(jurorHistoryService, Mockito.never()).createUndeliveredSummonsHistory(Mockito.any());

    }

    @Test
    void positiveMarkJurorAsUndeliverable() {
        String jurorNumber = "222222225";

        JurorPool jurorPool = createValidJurorPool(jurorNumber, OWNER);
        List<JurorPool> members = new ArrayList<>();
        members.add(jurorPool);

        Mockito.doReturn(members).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        undeliverableResponseService.markAsUndeliverable(List.of(jurorNumber));

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActive(Mockito.any(), Mockito.anyBoolean());
        Mockito.verify(jurorPoolRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(jurorHistoryService, Mockito.times(1)).createUndeliveredSummonsHistory(jurorPool);
    }

    @Test
    void positiveMarkJurorAsUndeliverableMultiple() {
        String jurorNumber1 = "222222225";
        String jurorNumber2 = "222222226";

        JurorPool jurorPool1 = createValidJurorPool(jurorNumber1, OWNER);
        JurorPool jurorPool2 = createValidJurorPool(jurorNumber2, OWNER);
        List<JurorPool> members1 = new ArrayList<>();
        members1.add(jurorPool1);

        List<JurorPool> members2 = new ArrayList<>();
        members2.add(jurorPool2);

        Mockito.doReturn(members1).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber1, true);
        Mockito.doReturn(members2).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber2, true);

        undeliverableResponseService.markAsUndeliverable(List.of(jurorNumber1, jurorNumber2));

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActive(jurorNumber1, true);
        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActive(jurorNumber2, true);
        Mockito.verify(jurorPoolRepository, Mockito.times(1)).save(jurorPool1);
        Mockito.verify(jurorPoolRepository, Mockito.times(1)).save(jurorPool2);
        Mockito.verify(jurorHistoryService, Mockito.times(1))
            .createUndeliveredSummonsHistory(jurorPool1);
        Mockito.verify(jurorHistoryService, Mockito.times(1))
            .createUndeliveredSummonsHistory(jurorPool2);
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
