package uk.gov.hmcts.juror.api.moj.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.THistoryCodeRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings({
    "PMD.ExcessiveImports",
    "PMD.TooManyMethods"
})
class JurorHistoryServiceImplTest {
    private final JurorHistoryService jurorHistoryService;
    private final Clock clock;
    private final JurorHistoryRepository jurorHistoryRepository;
    private final THistoryCodeRepository tHistoryCodeRepository;

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    public JurorHistoryServiceImplTest() {
        this.clock = mock(Clock.class);
        this.jurorHistoryRepository = mock(JurorHistoryRepository.class);
        this.tHistoryCodeRepository = mock(THistoryCodeRepository.class);
        this.jurorHistoryService = new JurorHistoryServiceImpl(jurorHistoryRepository, tHistoryCodeRepository, clock);
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
        assertStandardValuesSystem(jurorPool, new JurorHistoryPartHistoryJurorHistoryExpectedValues(
            HistoryCodeMod.POLICE_CHECK_COMPLETE, "Passed"));
    }

    @Test
    void createPoliceCheckQualifyPartHistoryUnChecked() {
        JurorPool jurorPool = createJurorPool();
        jurorHistoryService.createPoliceCheckQualifyHistory(jurorPool, false);
        assertStandardValuesSystem(jurorPool, new JurorHistoryPartHistoryJurorHistoryExpectedValues(
            HistoryCodeMod.POLICE_CHECK_COMPLETE, "Unchecked - timed out"));
    }


    @Test
    void createPoliceCheckDisqualifyHistory() {
        JurorPool jurorPool = createJurorPool();
        jurorHistoryService.createPoliceCheckDisqualifyHistory(jurorPool);
        assertStandardValuesSystem(jurorPool,
            new JurorHistoryPartHistoryJurorHistoryExpectedValues(HistoryCodeMod.POLICE_CHECK_FAILED, "Failed"),
            new JurorHistoryPartHistoryJurorHistoryExpectedValues(HistoryCodeMod.DISQUALIFY_POOL_MEMBER,
                "Disqualify - E")
        );
    }

    @Test
    void createPoliceCheckInProgressHistory() {
        JurorPool jurorPool = createJurorPool();
        jurorHistoryService.createPoliceCheckInProgressHistory(jurorPool);
        assertStandardValuesSystem(jurorPool, new JurorHistoryPartHistoryJurorHistoryExpectedValues(
            HistoryCodeMod.POLICE_CHECK_REQUEST, "Check requested"));
    }

    @Test
    void createPoliceCheckInsufficientInformationHistory() {
        JurorPool jurorPool = createJurorPool();
        jurorHistoryService.createPoliceCheckInsufficientInformationHistory(jurorPool);
        assertStandardValuesSystem(jurorPool, new JurorHistoryPartHistoryJurorHistoryExpectedValues(
            HistoryCodeMod.INSUFFICIENT_INFORMATION, "Insufficient Information"));
    }

    @Test
    void createConfirmServiceHistory() {
        JurorPool jurorPool = createJurorPool();
        jurorHistoryService.createConfirmationLetterHistory(jurorPool, "Some Other Info");
        assertStandardValuesSystem(jurorPool, new JurorHistoryPartHistoryJurorHistoryExpectedValues(
            HistoryCodeMod.RESPONDED_LETTER, "Some Other Info"));
    }

    @Test
    void createWithdrawHistory() {
        JurorPool jurorPool = createJurorPool();
        jurorHistoryService.createWithdrawHistory(jurorPool, "Other Info");
        assertStandardValuesSystem(jurorPool, new JurorHistoryPartHistoryJurorHistoryExpectedValues(
            HistoryCodeMod.WITHDRAWAL_LETTER, "Other Info"));
    }

    @Test
    void createSendMessageHistory() {
        final String otherInfo = "Some other info";
        mockCurrentUser("someUserId1");
        jurorHistoryService.createSendMessageHistory(
            TestConstants.VALID_JUROR_NUMBER,
            TestConstants.VALID_POOL_NUMBER,
            "Some other info"
        );
        assertStandardValues(TestConstants.VALID_JUROR_NUMBER,
            TestConstants.VALID_POOL_NUMBER,
            "someUserId1",
            new JurorHistoryPartHistoryJurorHistoryExpectedValues(
                HistoryCodeMod.NOTIFY_MESSAGE_REQUESTED, otherInfo));
    }

    @Test
    void createEditBankAccountNumberHistory() {
        final String otherInfo = "Bank Acct No Changed";
        mockCurrentUser("someUserId1");
        jurorHistoryService.createEditBankAccountNumberHistory(TestConstants.VALID_JUROR_NUMBER);
        assertStandardValues(TestConstants.VALID_JUROR_NUMBER, null, "someUserId1",
            new JurorHistoryPartHistoryJurorHistoryExpectedValues(
                HistoryCodeMod.CHANGE_PERSONAL_DETAILS, otherInfo));
    }

    @Test
    void createEditBankSortCodeHistory() {
        final String otherInfo = "Bank Sort Code Changed";
        mockCurrentUser("someUserId1");
        jurorHistoryService.createEditBankSortCodeHistory(TestConstants.VALID_JUROR_NUMBER);
        assertStandardValues(TestConstants.VALID_JUROR_NUMBER, null, "someUserId1",
            new JurorHistoryPartHistoryJurorHistoryExpectedValues(
                HistoryCodeMod.CHANGE_PERSONAL_DETAILS, otherInfo));
    }

    @Test
    void createEditBankAccountNameHistory() {
        final String otherInfo = "Bank Account Name Changed";
        mockCurrentUser("someUserId1");
        jurorHistoryService.createEditBankAccountNameHistory(TestConstants.VALID_JUROR_NUMBER);
        assertStandardValues(TestConstants.VALID_JUROR_NUMBER, null, "someUserId1",
            new JurorHistoryPartHistoryJurorHistoryExpectedValues(
                HistoryCodeMod.CHANGE_PERSONAL_DETAILS, otherInfo));
    }


    @Test
    void typicalCreateCompleteServiceHistory() {
        JurorPool jurorPool = createJurorPool();
        jurorPool.getJuror().setCompletionDate(LocalDate.of(2023, 11, 24));
        mockCurrentUser("someUserId1");
        jurorHistoryService.createCompleteServiceHistory(jurorPool);
        assertStandardValues(jurorPool, "someUserId1", new JurorHistoryPartHistoryJurorHistoryExpectedValues(
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
                + "The juror record must contain a completion date for juror " + jurorPool.getJurorNumber(),
            exception.getMessage(),
            "Exception message must match");
    }

    @Test
    void typicalCreateUnCompleteServiceHistory() {
        JurorPool jurorPool = createJurorPool();
        jurorPool.getJuror().setCompletionDate(null);
        mockCurrentUser("someUserId1");
        jurorHistoryService.createUncompleteServiceHistory(jurorPool);
        assertStandardValues(jurorPool, "someUserId1", new JurorHistoryPartHistoryJurorHistoryExpectedValues(
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
        assertStandardValues(jurorPool, "someNewUser", new JurorHistoryPartHistoryJurorHistoryExpectedValues(
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
        assertStandardValues(jurorPool, "someNewUser", new JurorHistoryPartHistoryJurorHistoryExpectedValues(
            HistoryCodeMod.FAILED_TO_ATTEND, "FTA status removed"));
    }

    @Test
    void typicalCreateDeferredLetterHistory() {
        JurorPool jurorPool = createJurorPool();
        jurorPool.setDeferralCode("A");
        jurorPool.setDeferralDate(LocalDate.now(clock).plusMonths(3));

        JurorStatus jurorStatus = mock(JurorStatus.class);
        when(jurorStatus.getStatus()).thenReturn(IJurorStatus.DEFERRED);
        jurorPool.setStatus(jurorStatus);

        mockCurrentUser("someNewUser");
        jurorHistoryService.createDeferredLetterHistory(jurorPool);
        assertValuesAdditional(jurorPool, "someNewUser", jurorPool.getDeferralDate(), jurorPool.getDeferralCode(),
            new JurorHistoryPartHistoryJurorHistoryExpectedValues(HistoryCodeMod.DEFERRED_LETTER,
                "Deferral Letter Printed"));
    }

    @Test
    void negativeCreateDeferredLetterHistoryNoDeferredToDate() {
        JurorPool jurorPool = createJurorPool();
        jurorPool.setDeferralCode("A");

        JurorStatus jurorStatus = mock(JurorStatus.class);
        when(jurorStatus.getStatus()).thenReturn(IJurorStatus.DEFERRED);

        mockCurrentUser("someNewUser");
        MojException.InternalServerError exception = assertThrows(MojException.InternalServerError.class,
            () -> jurorHistoryService.createDeferredLetterHistory(jurorPool),
            "Exception must be thrown");
        assertEquals("A deferred juror_pool record should exist for the juror relating to the original pool they were "
                + "summoned to and deferred from",
            exception.getMessage(),
            "Exception message must match");
    }

    @Test
    void negativeCreateDeferredLetterHistoryNoDeferralCode() {
        JurorPool jurorPool = createJurorPool();
        jurorPool.setDeferralDate(LocalDate.now(clock).plusMonths(3));

        JurorStatus jurorStatus = mock(JurorStatus.class);
        when(jurorStatus.getStatus()).thenReturn(IJurorStatus.DEFERRED);

        mockCurrentUser("someNewUser");
        MojException.InternalServerError exception = assertThrows(MojException.InternalServerError.class,
            () -> jurorHistoryService.createDeferredLetterHistory(jurorPool),
            "Exception must be thrown");
        assertEquals("A deferred juror_pool record should exist for the juror relating to the original pool they were "
                + "summoned to and deferred from",
            exception.getMessage(),
            "Exception message must match");
    }

    @Test
    void negativeCreateDeferredLetterHistoryNoDeferralData() {
        JurorPool jurorPool = createJurorPool();

        JurorStatus jurorStatus = mock(JurorStatus.class);
        when(jurorStatus.getStatus()).thenReturn(IJurorStatus.DEFERRED);

        mockCurrentUser("someNewUser");
        MojException.InternalServerError exception = assertThrows(MojException.InternalServerError.class,
            () -> jurorHistoryService.createDeferredLetterHistory(jurorPool),
            "Exception must be thrown");
        assertEquals("A deferred juror_pool record should exist for the juror relating to the original pool they were "
                + "summoned to and deferred from",
            exception.getMessage(),
            "Exception message must match");
    }


    @Test
    void createExpenseForApprovalHistoryTypical() {
        mockCurrentUser("someUserId1");
        FinancialAuditDetails financialAuditDetails = mock(FinancialAuditDetails.class);
        Appearance appearance = mock(Appearance.class);
        when(appearance.getJurorNumber()).thenReturn(TestConstants.VALID_JUROR_NUMBER);
        when(appearance.getPoolNumber()).thenReturn(TestConstants.VALID_POOL_NUMBER);
        LocalDate attendanceDate = LocalDate.now(clock);
        when(appearance.getAttendanceDate()).thenReturn(attendanceDate);
        when(appearance.getTotalDue()).thenReturn(new BigDecimal("23.45"));
        String financialAuditId = "F" + RandomStringUtils.randomNumeric(9);
        when(financialAuditDetails.getFinancialAuditNumber()).thenReturn(financialAuditId);

        jurorHistoryService.createExpenseForApprovalHistory(
            financialAuditDetails,
            appearance);
        assertValuesAdditional(TestConstants.VALID_JUROR_NUMBER,
            TestConstants.VALID_POOL_NUMBER, "someUserId1",
            attendanceDate,
            financialAuditId,
            new JurorHistoryPartHistoryJurorHistoryExpectedValues(HistoryCodeMod.APPEARANCE_PAYMENTS,
                "£23.45"));
    }

    @Test
    void createExpenseEditHistoryTypical() {
        mockCurrentUser("someUserId1");
        FinancialAuditDetails financialAuditDetails = mock(FinancialAuditDetails.class);
        Appearance appearance = mock(Appearance.class);
        when(appearance.getJurorNumber()).thenReturn(TestConstants.VALID_JUROR_NUMBER);
        when(appearance.getPoolNumber()).thenReturn(TestConstants.VALID_POOL_NUMBER);
        LocalDate attendanceDate = LocalDate.now(clock);
        when(appearance.getAttendanceDate()).thenReturn(attendanceDate);
        when(appearance.getTotalDue()).thenReturn(new BigDecimal("23.45"));
        String financialAuditId = "F" + RandomStringUtils.randomNumeric(9);
        when(financialAuditDetails.getFinancialAuditNumber()).thenReturn(financialAuditId);

        jurorHistoryService.createExpenseEditHistory(
            financialAuditDetails,
            appearance);
        assertValuesAdditional(TestConstants.VALID_JUROR_NUMBER,
            TestConstants.VALID_POOL_NUMBER, "someUserId1",
            attendanceDate,
            financialAuditId,
            new JurorHistoryPartHistoryJurorHistoryExpectedValues(HistoryCodeMod.EDIT_PAYMENTS,
                "£23.45"));
    }

    @Test
    void createExpenseApproveCashTypical() {
        mockCurrentUser("someUserId1");
        FinancialAuditDetails financialAuditDetails = mock(FinancialAuditDetails.class);
        String financialAuditId = "F" + RandomStringUtils.randomNumeric(9);
        when(financialAuditDetails.getFinancialAuditNumber()).thenReturn(financialAuditId);
        LocalDate attendanceDate = LocalDate.now(clock);
        BigDecimal totalAmount = new BigDecimal("23.45");
        jurorHistoryService.createExpenseApproveCash(
            TestConstants.VALID_JUROR_NUMBER,
            financialAuditDetails,
            attendanceDate,
            totalAmount);
        assertValuesAdditional(TestConstants.VALID_JUROR_NUMBER,
            null, "someUserId1",
            attendanceDate,
            financialAuditId,
            new JurorHistoryPartHistoryJurorHistoryExpectedValues(HistoryCodeMod.CASH_PAYMENT_APPROVAL,
                "£23.45"));
    }

    @Test
    void createExpenseApproveBacsTypical() {
        mockCurrentUser("someUserId1");
        FinancialAuditDetails financialAuditDetails = mock(FinancialAuditDetails.class);
        String financialAuditId = "F" + RandomStringUtils.randomNumeric(9);
        when(financialAuditDetails.getFinancialAuditNumber()).thenReturn(financialAuditId);
        LocalDate attendanceDate = LocalDate.now(clock);
        BigDecimal totalAmount = new BigDecimal("23.45");
        jurorHistoryService.createExpenseApproveBacs(
            TestConstants.VALID_JUROR_NUMBER,
            financialAuditDetails,
            attendanceDate,
            totalAmount);
        assertValuesAdditional(TestConstants.VALID_JUROR_NUMBER,
            null, "someUserId1",
            attendanceDate,
            financialAuditId,
            new JurorHistoryPartHistoryJurorHistoryExpectedValues(HistoryCodeMod.ARAMIS_EXPENSES_FILE_CREATED,
                "£23.45"));
    }

    @Test
    void createSummonsReminderLetterHistory() {
        JurorPool jurorPool = createJurorPool();
        jurorPool.setIsActive(true);

        JurorStatus jurorStatus = mock(JurorStatus.class);
        when(jurorStatus.getStatus()).thenReturn(IJurorStatus.RESPONDED);
        jurorPool.setStatus(jurorStatus);

        jurorHistoryService.createSummonsReminderLetterHistory(jurorPool);
        assertValuesAdditional(jurorPool, "SYSTEM", null, null,
            new JurorHistoryPartHistoryJurorHistoryExpectedValues(HistoryCodeMod.NON_RESPONDED_LETTER,
                "Reminder letter printed"));
    }

    @Test
    void createJuryAttendanceHistory() {
        TestUtils.setUpMockAuthentication("415", "TEST_USER", "1", List.of("415"));
        JurorPool jurorPool = createJurorPool();
        jurorPool.setIsActive(true);

        JurorStatus jurorStatus = mock(JurorStatus.class);
        when(jurorStatus.getStatus()).thenReturn(IJurorStatus.JUROR);
        jurorPool.setStatus(jurorStatus);

        String attendanceAuditNumber = "J00000001";
        jurorHistoryService.createJuryAttendanceHistory(jurorPool, attendanceAuditNumber);
        assertValuesAdditional(jurorPool, "TEST_USER", null, null,
            new JurorHistoryPartHistoryJurorHistoryExpectedValues(HistoryCodeMod.JURY_ATTENDANCE,
                attendanceAuditNumber));
    }

    @Test
    void createPoolAttendanceHistory() {
        TestUtils.setUpMockAuthentication("415", "TEST_USER", "1", List.of("415"));
        JurorPool jurorPool = createJurorPool();
        jurorPool.setIsActive(true);

        JurorStatus jurorStatus = mock(JurorStatus.class);
        when(jurorStatus.getStatus()).thenReturn(IJurorStatus.JUROR);
        jurorPool.setStatus(jurorStatus);

        String attendanceAuditNumber = "P00000001";
        jurorHistoryService.createPoolAttendanceHistory(jurorPool, attendanceAuditNumber);
        assertValuesAdditional(jurorPool, "TEST_USER", null, null,
            new JurorHistoryPartHistoryJurorHistoryExpectedValues(HistoryCodeMod.POOL_ATTENDANCE,
                attendanceAuditNumber));
    }

    private void assertValuesAdditional(JurorPool jurorPool, String userId,
                                        LocalDate additionalDateInfo, String additionalReferenceInfo,
                                        JurorHistoryPartHistoryJurorHistoryExpectedValues... expectedValues) {
        assertValuesAdditional(jurorPool.getJurorNumber(), jurorPool.getPoolNumber(), userId, additionalDateInfo,
            additionalReferenceInfo,
            expectedValues);
    }

    private void assertValuesAdditional(String jurorNmber, String poolNumber, String userId,
                                        LocalDate additionalDateInfo, String additionalReferenceInfo,
                                        JurorHistoryPartHistoryJurorHistoryExpectedValues... expectedValues) {

        ArgumentCaptor<JurorHistory> jurorHistoryArgumentCaptor = ArgumentCaptor.forClass(JurorHistory.class);

        verify(jurorHistoryRepository, times(expectedValues.length)).save(jurorHistoryArgumentCaptor.capture());

        Iterator<JurorHistory> jurorHistoryValues = jurorHistoryArgumentCaptor.getAllValues().iterator();
        for (JurorHistoryPartHistoryJurorHistoryExpectedValues expectedValue : expectedValues) {
            JurorHistory jurorHistory = jurorHistoryValues.next();
            assertEquals(jurorNmber, jurorHistory.getJurorNumber(),
                "Juror Number must match");
            assertEquals(poolNumber, jurorHistory.getPoolNumber(),
                "Pool Number must match");
            assertEquals(userId, jurorHistory.getCreatedBy(),
                "User Id must match");
            assertEquals(expectedValue.historyCode, jurorHistory.getHistoryCode(),
                "History Code must match");
            assertEquals(expectedValue.info, jurorHistory.getOtherInformation(),
                "Info must match");
            assertEquals(LocalDateTime.now(clock), jurorHistory.getDateCreated(),
                "Date Part must match");
            assertEquals(additionalDateInfo, jurorHistory.getOtherInformationDate(),
                "Other Date Info must match");
            assertEquals(additionalReferenceInfo, jurorHistory.getOtherInformationRef(),
                "Other Reference Info must match");
        }

    }

    private void mockCurrentUser(String userId) {
        securityUtilMockedStatic = Mockito.mockStatic(SecurityUtil.class);
        securityUtilMockedStatic.when(SecurityUtil::getActiveLogin)
            .thenReturn(userId);
    }


    private void assertStandardValuesSystem(JurorPool jurorPool,
                                            JurorHistoryPartHistoryJurorHistoryExpectedValues... expectedValues) {
        assertStandardValues(jurorPool.getJurorNumber(), jurorPool.getPoolNumber(), "SYSTEM", expectedValues);
    }

    private void assertStandardValues(JurorPool jurorPool,
                                      String userId,
                                      JurorHistoryPartHistoryJurorHistoryExpectedValues... expectedValues) {
        assertStandardValues(jurorPool.getJurorNumber(), jurorPool.getPoolNumber(), userId, expectedValues);

    }

    private void assertStandardValues(String jurorNumber,
                                      String poolNumber,
                                      String userId,
                                      JurorHistoryPartHistoryJurorHistoryExpectedValues... expectedValues) {
        ArgumentCaptor<JurorHistory> jurorHistoryArgumentCaptor = ArgumentCaptor.forClass(JurorHistory.class);

        verify(jurorHistoryRepository, times(expectedValues.length)).save(jurorHistoryArgumentCaptor.capture());

        Iterator<JurorHistory> jurorHistoryValues = jurorHistoryArgumentCaptor.getAllValues().iterator();
        for (JurorHistoryPartHistoryJurorHistoryExpectedValues expectedValue : expectedValues) {
            JurorHistory jurorHistory = jurorHistoryValues.next();
            assertEquals(jurorNumber, jurorHistory.getJurorNumber(),
                "Juror Number must match");
            assertEquals(poolNumber, jurorHistory.getPoolNumber(),
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
