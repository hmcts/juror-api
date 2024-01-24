package uk.gov.hmcts.juror.api.moj.service;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@RunWith(SpringRunner.class)
public class UndeliverableResponseServiceTest {

    @Mock
    private JurorHistoryRepository jurorHistoryRepository;
    @Mock
    private JurorPoolRepository jurorPoolRepository;

    @InjectMocks
    UndeliverableResponseServiceImpl undeliverableResponseService;


    @Test
    public void test_jurorRecordDoesNotExist() {
        String owner = "400";
        String jurorNumber = "111111111";

        List<JurorPool> members = new ArrayList<>();

        Mockito.doReturn(members).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(
            () -> undeliverableResponseService.markAsUndeliverable(buildPayload(owner), jurorNumber));

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.verify(jurorPoolRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(jurorHistoryRepository, Mockito.never()).save(Mockito.any());
    }


    @Test
    public void test_jurorRecordIsNotOwned() {
        String owner = "415";
        String jurorNumber = "123456789";

        List<JurorPool> members = new ArrayList<>();
        members.add(createValidJurorPool(jurorNumber, "400"));

        Mockito.doReturn(members).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(
            () -> undeliverableResponseService.markAsUndeliverable(buildPayload(owner), jurorNumber));

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);
        Mockito.verify(jurorPoolRepository, Mockito.never()).save(Mockito.any());
        Mockito.verify(jurorHistoryRepository, Mockito.never()).save(Mockito.any());

    }

    @Test
    public void test_markJurorAsUndeliverable() {
        String owner = "400";
        String jurorNumber = "222222225";

        JurorPool jurorPool = createValidJurorPool(jurorNumber, owner);
        List<JurorPool> members = new ArrayList<>();
        members.add(jurorPool);

        Mockito.doReturn(members).when(jurorPoolRepository)
            .findByJurorJurorNumberAndIsActive(jurorNumber, true);

        undeliverableResponseService.markAsUndeliverable(buildPayload(owner), jurorNumber);

        Mockito.verify(jurorPoolRepository, Mockito.times(1))
            .findByJurorJurorNumberAndIsActive(Mockito.any(), Mockito.anyBoolean());
        Mockito.verify(jurorPoolRepository, Mockito.times(1)).save(Mockito.any());
        Mockito.verify(jurorHistoryRepository, Mockito.times(1)).save(Mockito.any());

    }

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
