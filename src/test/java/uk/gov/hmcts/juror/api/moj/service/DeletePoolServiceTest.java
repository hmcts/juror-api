package uk.gov.hmcts.juror.api.moj.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.exception.PoolDeleteException;
import uk.gov.hmcts.juror.api.moj.exception.PoolRequestException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@RunWith(SpringRunner.class)
public class DeletePoolServiceTest {

    @Mock
    PoolRequestRepository poolRequestRepository;
    @Mock
    PoolHistoryRepository poolHistoryRepository;
    @Mock
    JurorPoolRepository jurorPoolRepository;
    @InjectMocks
    DeletePoolServiceImpl deletePoolService;

    @Test
    public void test_deletePool_poolNotFound() {
        String poolNumber = "415220101";

        Mockito.when(poolRequestRepository.findByPoolNumber(poolNumber))
            .thenReturn(Optional.empty());

        assertThatExceptionOfType(PoolRequestException.PoolRequestNotFound.class)
            .isThrownBy(() -> deletePoolService.deletePool(buildPayload("400", "1"), poolNumber));

        Mockito.verify(poolRequestRepository, Mockito.times(1)).findByPoolNumber(poolNumber);
        Mockito.verify(poolHistoryRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void test_deletePool_insufficientPermission() {
        String poolNumber = "415220101";

        assertThatExceptionOfType(PoolDeleteException.InsufficientPermission.class)
            .isThrownBy(() -> deletePoolService.deletePool(buildPayload("400", "99"), poolNumber));

        Mockito.verify(poolHistoryRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void test_deletePool_courtUserAndActivePool() {
        String poolNumber = "415220101";

        Mockito.when(poolRequestRepository.isActive(poolNumber)).thenReturn(true);

        assertThatExceptionOfType(PoolDeleteException.InsufficientPermission.class)
            .isThrownBy(() -> deletePoolService.deletePool(buildPayload("415", "1"), poolNumber));

        Mockito.verify(poolRequestRepository, Mockito.times(1)).isActive(poolNumber);
        Mockito.verify(poolHistoryRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void test_deletePool_poolHasMembers() {
        String poolNumber = "415220101";
        String locCode = "415";

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(locCode);
        courtLocation.setVotersLock(0);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(poolNumber);
        poolRequest.setCourtLocation(courtLocation);

        List<JurorPool> members = new ArrayList<>();
        members.add(new JurorPool());

        Mockito.when(poolRequestRepository.findByPoolNumber(poolNumber))
            .thenReturn(Optional.of(poolRequest));
        Mockito.doReturn(members).when(jurorPoolRepository).findByPoolPoolNumberAndIsActive(poolNumber, true);

        assertThatExceptionOfType(PoolDeleteException.PoolHasMembersException.class)
            .isThrownBy(() -> deletePoolService.deletePool(buildPayload("400", "1"), poolNumber));

        Mockito.verify(poolRequestRepository, Mockito.times(1)).findByPoolNumber(poolNumber);
        Mockito.verify(poolHistoryRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void test_deletePool_poolIsLocked() {
        String poolNumber = "415220101";
        String locCode = "415";

        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(locCode);
        courtLocation.setVotersLock(1);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(poolNumber);
        poolRequest.setCourtLocation(courtLocation);

        Mockito.when(poolRequestRepository.findByPoolNumber(poolNumber))
            .thenReturn(Optional.of(poolRequest));

        assertThatExceptionOfType(PoolDeleteException.PoolIsCurrentlyLocked.class)
            .isThrownBy(() -> deletePoolService.deletePool(buildPayload("400", "1"), poolNumber));

        Mockito.verify(poolHistoryRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    public void test_deletePool() {
        String poolNumber = "415220101";
        final BureauJwtPayload payload = buildPayload("400", "1");

        String locCode = "415";
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(locCode);
        courtLocation.setVotersLock(0);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(poolNumber);
        poolRequest.setCourtLocation(courtLocation);

        Mockito.when(poolRequestRepository.findByPoolNumber(poolNumber))
            .thenReturn(Optional.of(poolRequest));

        assertThat(poolNumber).isEqualTo(poolRequest.getPoolNumber());

        deletePoolService.deletePool(payload, poolRequest.getPoolNumber());

        Mockito.verify(poolRequestRepository, Mockito.times(1)).findByPoolNumber(poolNumber);
        Mockito.verify(poolRequestRepository, Mockito.times(1)).deletePoolRequestByPoolNumber(poolNumber);
        Mockito.verify(poolHistoryRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    public void test_deletePoolRequest() {
        String poolNumber = "101010101";
        final BureauJwtPayload payload = buildPayload("400", "1");

        String locCode = "415";
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(locCode);
        courtLocation.setVotersLock(0);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(poolNumber);
        poolRequest.setCourtLocation(courtLocation);
        poolRequest.setTotalNoRequired(10);
        poolRequest.setLastUpdate(LocalDateTime.now());

        Mockito.when(poolRequestRepository.findByPoolNumber(poolNumber))
            .thenReturn(Optional.of(poolRequest));

        deletePoolService.deletePool(payload, poolRequest.getPoolNumber());
        Mockito.verify(poolRequestRepository, Mockito.times(1)).deletePoolRequestByPoolNumber(poolNumber);
    }

    @Test
    public void test_deleteJurorPools() {
        String poolNumber = "415221001";
        final BureauJwtPayload payload = buildPayload("400", "1");

        String locCode = "415";
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(locCode);
        courtLocation.setVotersLock(0);

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(poolNumber);
        poolRequest.setCourtLocation(courtLocation);
        JurorPool jurorPool = createJurorPool("400", "415221001", false);

        Mockito.doReturn(Optional.of(poolRequest)).when(poolRequestRepository).findByPoolNumber(poolNumber);
        Mockito.doReturn(new ArrayList<JurorPool>()).when(jurorPoolRepository)
            .findByPoolPoolNumberAndIsActive(poolNumber, true);
        Mockito.doReturn(Collections.singletonList(jurorPool)).when(jurorPoolRepository)
            .findByPoolPoolNumberAndIsActive(poolNumber, false);

        deletePoolService.deletePool(payload, poolRequest.getPoolNumber());

        Mockito.verify(jurorPoolRepository, Mockito.times(1)).delete(jurorPool);
    }

    private JurorPool createJurorPool(String owner, String poolNumber, boolean isActive) {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("415");
        courtLocation.setLocCourtName("CHESTER");

        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(poolNumber);
        poolRequest.setCourtLocation(courtLocation);

        Juror juror = new Juror();
        juror.setJurorNumber("123456789");

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(owner);
        jurorPool.setPool(poolRequest);
        jurorPool.setIsActive(isActive);

        juror.setAssociatedPools(Set.of(jurorPool));
        jurorPool.setJuror(juror);

        return jurorPool;
    }

    private BureauJwtPayload buildPayload(String owner, String userLevel) {
        return BureauJwtPayload.builder()
            .userLevel(userLevel)
            .passwordWarning(false)
            .login("SOME_USER")
            .daysToExpire(89)
            .owner(owner)
            .build();
    }

}
