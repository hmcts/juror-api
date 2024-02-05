package uk.gov.hmcts.juror.api.moj.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("PMD.TooManyMethods")
class JurorHistoryServiceImplTest {
    private final JurorHistoryService jurorHistoryService;
    private final Clock clock;
    private final JurorHistoryRepository jurorHistoryRepository;
    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    public JurorHistoryServiceImplTest() {
        this.clock = mock(Clock.class);
        this.jurorHistoryRepository = mock(JurorHistoryRepository.class);
        this.jurorHistoryService = new JurorHistoryServiceImpl(jurorHistoryRepository, clock);
    }

    @BeforeEach
    void beforeEach() {
        when(clock.instant())
            .thenReturn(Instant.now());

        when(clock.getZone())
            .thenReturn(ZoneId.systemDefault());
    }

    @AfterEach
    void afterEach() {
        if (securityUtilMockedStatic != null) {
            securityUtilMockedStatic.close();
        }
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
        jurorHistoryService.createPoliceCheckQualifyHistory(jurorPool, true);
        verifyStandardValuesSystem(jurorPool, new JurorHistoryPartHistoryJurorHistoryExpectedValues(
            HistoryCodeMod.POLICE_CHECK_COMPLETE, "Passed"));
    }

    @Test
    void createPoliceCheckQualifyPartHistoryUnChecked() {
        JurorPool jurorPool = createJurorPool();
        jurorHistoryService.createPoliceCheckQualifyHistory(jurorPool, false);
        verifyStandardValuesSystem(jurorPool, new JurorHistoryPartHistoryJurorHistoryExpectedValues(
            HistoryCodeMod.POLICE_CHECK_COMPLETE, "Unchecked - timed out"));
    }

    @Test
    void createPoliceCheckDisqualifyHistory() {
        JurorPool jurorPool = createJurorPool();
        jurorHistoryService.createPoliceCheckDisqualifyHistory(jurorPool);
        verifyStandardValuesSystem(jurorPool,
            new JurorHistoryPartHistoryJurorHistoryExpectedValues(HistoryCodeMod.POLICE_CHECK_FAILED, "Failed"),
            new JurorHistoryPartHistoryJurorHistoryExpectedValues(HistoryCodeMod.DISQUALIFY_POOL_MEMBER,
                "Disqualify - E")
        );
    }

    @Test
    void createPoliceCheckInProgressHistory() {
        JurorPool jurorPool = createJurorPool();
        jurorHistoryService.createPoliceCheckInProgressHistory(jurorPool);
        verifyStandardValuesSystem(jurorPool, new JurorHistoryPartHistoryJurorHistoryExpectedValues(
            HistoryCodeMod.POLICE_CHECK_REQUEST, "Check requested"));
    }

    @Test
    void createPoliceCheckInsufficientInformationHistory() {
        JurorPool jurorPool = createJurorPool();
        jurorHistoryService.createPoliceCheckInsufficientInformationHistory(jurorPool);
        verifyStandardValuesSystem(jurorPool, new JurorHistoryPartHistoryJurorHistoryExpectedValues(
            HistoryCodeMod.INSUFFICIENT_INFORMATION, "Insufficient Information"));
    }


    @Test
    void typicalCreateCompleteServiceHistory() {
        JurorPool jurorPool = createJurorPool();
        jurorPool.getJuror().setCompletionDate(LocalDate.of(2023, 11, 24));
        mockCurrentUser("someUserId1");
        jurorHistoryService.createCompleteServiceHistory(jurorPool);
        verifyStandardValues(jurorPool, "someUserId1", new JurorHistoryPartHistoryJurorHistoryExpectedValues(
            HistoryCodeMod.COMPLETE_SERVICE, "Completed service on 24/11/2023"));
    }

    @Test
    void negativeNoCompletedDateCreateCompleteServiceHistory() {
        JurorPool jurorPool = createJurorPool();
        jurorPool.getJuror().setCompletionDate(null);
        mockCurrentUser("someUserId1");
        MojException.InternalServerError exception = assertThrows(MojException.InternalServerError.class,
            () -> jurorHistoryService.createCompleteServiceHistory(jurorPool),
            "Exception must be thrown");
        assertEquals("To create a complete service history entry. "
                + "The juror record must contain a completion date for juror " + jurorPool.getJurorNumber(), exception.getMessage(),
            "Exception message must match");
    }

    @Test
    void typicalCreateUnCompleteServiceHistory() {
        JurorPool jurorPool = createJurorPool();
        jurorPool.getJuror().setCompletionDate(null);
        mockCurrentUser("someUserId1");
        jurorHistoryService.createUncompleteServiceHistory(jurorPool);
        verifyStandardValues(jurorPool, "someUserId1", new JurorHistoryPartHistoryJurorHistoryExpectedValues(
            HistoryCodeMod.COMPLETE_SERVICE, "Completion date removed"));
    }

    @Test
    void negativeCompletionDateUnCompletedDateCreateCompleteServiceHistory() {
        JurorPool jurorPool = createJurorPool();
        jurorPool.getJuror().setCompletionDate(LocalDate.now());
        mockCurrentUser("someUserId1");
        MojException.InternalServerError exception = assertThrows(MojException.InternalServerError.class,
            () -> jurorHistoryService.createUncompleteServiceHistory(jurorPool),
            "Exception must be thrown");
        assertEquals("To uncomplete a service history entry. "
                + "The juror record must not contain a completion date for juror "
                + jurorPool.getJurorNumber(), exception.getMessage(),
            "Exception message must match");
    }

    @Test
    void typicalCreateFailedToAttendHistory() {
        JurorPool jurorPool = createJurorPool();

        JurorStatus jurorStatus = mock(JurorStatus.class);
        when(jurorStatus.getStatus()).thenReturn(IJurorStatus.FAILED_TO_ATTEND);
        jurorPool.setStatus(jurorStatus);

        mockCurrentUser("someNewUser");
        jurorHistoryService.createFailedToAttendHistory(jurorPool);
        verifyStandardValues(jurorPool, "someNewUser", new JurorHistoryPartHistoryJurorHistoryExpectedValues(
            HistoryCodeMod.FAILED_TO_ATTEND, "FTA after responding"));
    }

    @Test
    void negativeWrongStatusCreateFailedToAttendHistory() {
        JurorPool jurorPool = createJurorPool();

        JurorStatus jurorStatus = mock(JurorStatus.class);
        when(jurorStatus.getStatus()).thenReturn(IJurorStatus.FAILED_TO_ATTEND);

        mockCurrentUser("someNewUser");
        MojException.InternalServerError exception = assertThrows(MojException.InternalServerError.class,
            () -> jurorHistoryService.createFailedToAttendHistory(jurorPool),
            "Exception must be thrown");
        assertEquals("To create a failed to attend history entry. "
                + "The juror pool must have the status of failed to attend",
            exception.getMessage(),
            "Exception message must match");
    }

    @Test
    void typicalCreateUndoFailedToAttendHistory() {
        JurorPool jurorPool = createJurorPool();

        JurorStatus jurorStatus = mock(JurorStatus.class);
        when(jurorStatus.getStatus()).thenReturn(IJurorStatus.RESPONDED);
        jurorPool.setStatus(jurorStatus);

        mockCurrentUser("someNewUser");
        jurorHistoryService.createUndoFailedToAttendHistory(jurorPool);
        verifyStandardValues(jurorPool, "someNewUser", new JurorHistoryPartHistoryJurorHistoryExpectedValues(
            HistoryCodeMod.FAILED_TO_ATTEND, "FTA status removed"));
    }

    private void mockCurrentUser(String userId) {
        securityUtilMockedStatic = Mockito.mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getActiveLogin)
            .thenReturn(userId);
    }


    private void verifyStandardValuesSystem(JurorPool jurorPool,
                                            JurorHistoryPartHistoryJurorHistoryExpectedValues... expectedValues) {
        verifyStandardValues(jurorPool, "SYSTEM", expectedValues);
    }

    private void verifyStandardValues(JurorPool jurorPool,
                                      String userId,
                                      JurorHistoryPartHistoryJurorHistoryExpectedValues... expectedValues) {
        ArgumentCaptor<JurorHistory> jurorHistoryArgumentCaptor = ArgumentCaptor.forClass(JurorHistory.class);

        verify(jurorHistoryRepository, times(expectedValues.length)).save(jurorHistoryArgumentCaptor.capture());

        Iterator<JurorHistory> jurorHistoryValues = jurorHistoryArgumentCaptor.getAllValues().iterator();
        for (JurorHistoryPartHistoryJurorHistoryExpectedValues expectedValue : expectedValues) {
            JurorHistory jurorHistory = jurorHistoryValues.next();
            assertEquals(jurorPool.getJuror().getJurorNumber(), jurorHistory.getJurorNumber(),
                "Juror Number must match");
            assertEquals(jurorPool.getPoolNumber(), jurorHistory.getPoolNumber(),
                "Pool Number must match");
            assertEquals(userId, jurorHistory.getCreatedBy(),
                "User Id must match");
            assertEquals(expectedValue.historyCode, jurorHistory.getHistoryCode(),
                "History Code must match");
            assertEquals(expectedValue.info, jurorHistory.getOtherInformation(),
                "Info must match");
            assertEquals(LocalDateTime.now(clock), jurorHistory.getDateCreated(),
                "Date Part must match");
        }
    }

    private record JurorHistoryPartHistoryJurorHistoryExpectedValues(HistoryCodeMod historyCode, String info) {
    }
}
