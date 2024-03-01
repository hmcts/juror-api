package uk.gov.hmcts.juror.api.moj.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.HistoryCode;
import uk.gov.hmcts.juror.api.moj.domain.PoolHistory;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.PoolType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.PoolHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


@RunWith(SpringRunner.class)
public class PoolHistoryServiceTest {

    @Mock
    private PoolHistoryRepository poolHistoryRepository;

    @Mock
    private PoolRequestRepository poolRequestRepository;

    @InjectMocks
    PoolHistoryServiceImpl poolHistoryService;

    @Test
    public void test_getPoolHistory_happyPath() {
        String owner = "400";
        String poolNumber = "111111111";

        PoolRequest poolRequest = createValidPoolRequest();

        List<PoolHistory> poolHistory = createPoolHistoryList();

        Mockito.doReturn(Optional.of(poolRequest)).when(poolRequestRepository).findById(Mockito.any());
        Mockito.doReturn(poolHistory).when(poolHistoryRepository)
            .findPoolHistorySincePoolCreated(Mockito.any());

        poolHistoryService.getPoolHistoryListData(buildPayload(owner), poolNumber);

        Mockito.verify(poolRequestRepository, Mockito.times(1))
            .findById(Mockito.any());
        Mockito.verify(poolHistoryRepository, Mockito.times(1))
            .findPoolHistorySincePoolCreated(poolNumber);

    }

    @Test
    public void test_checkDatesBeforeDateCreated() {
        final String owner = "400";
        final String poolNumber = "111111111";

        PoolRequest poolRequest = createValidPoolRequest();
        poolRequest.setDateCreated(LocalDateTime.now());

        Mockito.doReturn(Optional.of(poolRequest)).when(poolRequestRepository).findById(Mockito.any());
        Mockito.doReturn(new ArrayList<>()).when(poolHistoryRepository)
            .findPoolHistorySincePoolCreated(Mockito.any());

        assertThat(poolHistoryService.getPoolHistoryListData(buildPayload(owner), poolNumber).getData()).isEmpty();

        Mockito.verify(poolRequestRepository, Mockito.times(1))
            .findById(Mockito.any());
        Mockito.verify(poolHistoryRepository, Mockito.times(1))
            .findPoolHistorySincePoolCreated(poolNumber);
    }

    @Test
    public void test_checkPoolRequestNotFound() {
        String owner = "400";
        String poolNumber = "123456789";

        Mockito.doReturn(Optional.empty()).when(poolRequestRepository).findById(Mockito.any());
        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(
            () -> poolHistoryService.getPoolHistoryListData(buildPayload(owner), poolNumber));

        Mockito.verify(poolRequestRepository, Mockito.times(1)).findById(Mockito.any());
        Mockito.verify(poolHistoryRepository, Mockito.never())
            .findPoolHistorySincePoolCreated(poolNumber);

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

    private PoolRequest createValidPoolRequest() {
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber("111111111");
        poolRequest.setOwner("415");
        poolRequest.setAttendTime(LocalDateTime.of(2022, 10, 3, 11, 30));
        poolRequest.setNumberRequested(150);
        poolRequest.setPoolType(new PoolType("CRO", "Crown Court"));
        poolRequest.setAdditionalSummons(0);
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode("415");
        courtLocation.setLocCourtName("Test Court Location Name");
        courtLocation.setYield(BigDecimal.valueOf(2));
        poolRequest.setCourtLocation(courtLocation);
        poolRequest.setDateCreated(LocalDateTime.now().plusMonths(5));
        return poolRequest;
    }

    private List<PoolHistory> createPoolHistoryList() {
        List<PoolHistory> poolHistoryList = new ArrayList<>();

        PoolHistory poolHistoryPedt = new PoolHistory("400", LocalDateTime.now(), HistoryCode.PEDT,
            "BUREAU_USER", "150 (New Pool Request)"
        );
        PoolHistory poolHistoryPhdi = new PoolHistory("400", LocalDateTime.now(), HistoryCode.PHDI,
            "BUREAU_USER", "150 (New Pool Request)"
        );

        poolHistoryList.add(poolHistoryPedt);
        poolHistoryList.add(poolHistoryPhdi);

        return poolHistoryList;
    }


}
