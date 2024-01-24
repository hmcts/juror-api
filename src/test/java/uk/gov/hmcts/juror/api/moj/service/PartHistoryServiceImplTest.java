package uk.gov.hmcts.juror.api.moj.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import uk.gov.hmcts.juror.api.bureau.domain.PartHist;
import uk.gov.hmcts.juror.api.bureau.domain.PartHistRepository;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class PartHistoryServiceImplTest {
    private final PartHistoryService partHistoryService;
    private final Clock clock;
    private final PartHistRepository partHistRepository;

    public PartHistoryServiceImplTest() {
        this.clock = mock(Clock.class);
        this.partHistRepository = mock(PartHistRepository.class);
        this.partHistoryService = new PartHistoryServiceImpl(partHistRepository, clock);
    }

    @BeforeEach
    void beforeEach() {
        Mockito.when(clock.instant())
            .thenReturn(Instant.now());

        Mockito.when(clock.getZone())
            .thenReturn(ZoneId.systemDefault());
    }

    private JurorPool createJurorPool() {
        CourtLocation courtLocation = new CourtLocation();
        courtLocation.setLocCode(RandomStringUtils.randomAlphabetic(3));
        PoolRequest poolRequest = new PoolRequest();
        poolRequest.setPoolNumber(RandomStringUtils.randomNumeric(9));
        poolRequest.setCourtLocation(courtLocation);
        Juror juror = new Juror();
        juror.setJurorNumber(RandomStringUtils.randomNumeric(9));

        JurorPool jurorPool = new JurorPool();
        jurorPool.setOwner(RandomStringUtils.randomAlphabetic(3));
        jurorPool.setPool(poolRequest);
        jurorPool.setStatus(new JurorStatus());

        juror.setAssociatedPools(Set.of(jurorPool));
        jurorPool.setJuror(juror);
        return jurorPool;
    }

    @Test
    void createPoliceCheckQualifyPartHistoryChecked() {
        JurorPool jurorPool = createJurorPool();
        partHistoryService.createPoliceCheckQualifyPartHistory(jurorPool, true);
        verifyStandardValues(jurorPool, new PartHistoryExpectedValues("POLG", "Passed"));
    }

    @Test
    void createPoliceCheckQualifyPartHistoryUnChecked() {
        JurorPool jurorPool = createJurorPool();
        partHistoryService.createPoliceCheckQualifyPartHistory(jurorPool, false);
        verifyStandardValues(jurorPool, new PartHistoryExpectedValues("POLG", "Unchecked - timed out"));
    }

    @Test
    void createPoliceCheckDisqualifyPartHistory() {
        JurorPool jurorPool = createJurorPool();
        partHistoryService.createPoliceCheckDisqualifyPartHistory(jurorPool);
        verifyStandardValues(jurorPool,
            new PartHistoryExpectedValues("POLF", "Failed"),
            new PartHistoryExpectedValues("PDIS", "Disqualify - E")
        );
    }

    @Test
    void createPoliceCheckInProgressPartHistory() {
        JurorPool jurorPool = createJurorPool();
        partHistoryService.createPoliceCheckInProgressPartHistory(jurorPool);
        verifyStandardValues(jurorPool, new PartHistoryExpectedValues("POLE", "Check requested"));
    }

    @Test
    void createPoliceCheckInsufficientInformationPartHistory() {
        JurorPool jurorPool = createJurorPool();
        partHistoryService.createPoliceCheckInsufficientInformationPartHistory(jurorPool);
        verifyStandardValues(jurorPool, new PartHistoryExpectedValues("POLI", "Insufficient Information"));
    }

    private void verifyStandardValues(JurorPool jurorPool,
                                      PartHistoryExpectedValues... expectedValues) {
        ArgumentCaptor<PartHist> partHistArgumentCaptor = ArgumentCaptor.forClass(PartHist.class);

        verify(partHistRepository, times(expectedValues.length)).save(partHistArgumentCaptor.capture());

        Iterator<PartHist> partHistoryValues = partHistArgumentCaptor.getAllValues().iterator();
        for (PartHistoryExpectedValues expectedValue : expectedValues) {
            PartHist partHist = partHistoryValues.next();
            assertEquals(jurorPool.getOwner(), partHist.getOwner(),
                "Owner must match");
            assertEquals(jurorPool.getPoolNumber(), partHist.getPoolNumber(),
                "Pool Number must match");
            assertEquals(jurorPool.getJuror().getJurorNumber(), partHist.getJurorNumber(),
                "Juror Number must match");
            assertEquals(jurorPool.getPoolNumber(), partHist.getPoolNumber(),
                "Pool Number must match");
            assertEquals("SYSTEM", partHist.getUserId(),
                "User Id must match");
            assertEquals(expectedValue.historyCode, partHist.getHistoryCode(),
                "History Code must match");
            assertEquals(expectedValue.info, partHist.getInfo(),
                "Info must match");
            assertEquals(Date.from(clock.instant()), partHist.getDatePart(),
                "Date Part must match");
        }
    }

    private record PartHistoryExpectedValues(String historyCode, String info) {
    }
}
