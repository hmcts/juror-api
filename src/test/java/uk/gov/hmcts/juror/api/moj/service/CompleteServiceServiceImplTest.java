package uk.gov.hmcts.juror.api.moj.service;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.moj.controller.request.CompleteServiceJurorNumberListDto;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CompleteServiceValidationResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorStatusValidationResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.JurorStatus;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorStatusRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@DisplayName("CompleteServiceServiceImpl")
@SuppressWarnings("PMD.ExcessiveImports")
class CompleteServiceServiceImplTest {

    private JurorPoolRepository jurorPoolRepository;
    private JurorStatusRepository jurorStatusRepository;
    private JurorHistoryService jurorHistoryService;
    private JurorRepository jurorRepository;
    private CompleteServiceServiceImpl completeServiceService;

    @BeforeEach
    void beforeEach() {
        this.jurorPoolRepository = mock(JurorPoolRepository.class);
        this.jurorStatusRepository = mock(JurorStatusRepository.class);
        this.jurorHistoryService = mock(JurorHistoryService.class);
        this.jurorRepository = mock(JurorRepository.class);
        this.completeServiceService = new CompleteServiceServiceImpl(
            jurorPoolRepository, jurorStatusRepository,
            jurorRepository, jurorHistoryService);
    }

    @Nested
    @DisplayName("completeService")
    @SuppressWarnings("PMD.TooManyMethods")
    class CompleteService {

        private JurorStatus completedStatus;

        @BeforeEach
        void beforeEach() {
            completedStatus = new JurorStatus();
            when(jurorStatusRepository.findById(IJurorStatus.COMPLETED))
                .thenReturn(Optional.ofNullable(completedStatus));
        }


        void assertInvalidStatus(int poolStatus) {
            LocalDate localDate = LocalDate.of(2023, 11, 22);

            final String firstName = RandomStringUtils.randomAlphabetic(20);
            final String lastName = RandomStringUtils.randomAlphabetic(20);

            Juror juror = mock(Juror.class);
            JurorPool jurorPool = mock(JurorPool.class);

            createJurorPoolMock(TestConstants.VALID_POOL_NUMBER, poolStatus, TestConstants.VALID_JUROR_NUMBER,
                firstName, lastName, jurorPool, juror);

            CompleteServiceJurorNumberListDto completeServiceJurorNumberListDto =
                createCompleteServiceJurorNumberListDto(localDate, TestConstants.VALID_JUROR_NUMBER);

            MojException.BusinessRuleViolation exception = assertThrows(MojException.BusinessRuleViolation.class,
                () -> completeServiceService
                    .completeService(TestConstants.VALID_POOL_NUMBER, completeServiceJurorNumberListDto),
                "Expected exception to be thrown when juror pool not in resolved status");

            assertEquals("Unable to complete the service for the following juror number(s) due to "
                    + "invalid state: " + TestConstants.VALID_JUROR_NUMBER, exception.getMessage(),
                "Expected exception message to be " + "Unable to complete the service for the following "
                    + "juror number(s) due to invalid state: " + TestConstants.VALID_JUROR_NUMBER);


            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndPoolPoolNumber(TestConstants.VALID_JUROR_NUMBER,
                    TestConstants.VALID_POOL_NUMBER);

            verify(jurorPool, times(1)).getStatus();


            verifyNoMoreInteractions(jurorPoolRepository, jurorPoolRepository, jurorRepository, jurorHistoryService,
                juror, jurorPool);
        }

        void assertValidStatus(int poolStatus) {
            LocalDate localDate = LocalDate.of(2023, 11, 22);

            final String firstName = RandomStringUtils.randomAlphabetic(20);
            final String lastName = RandomStringUtils.randomAlphabetic(20);

            Juror juror = mock(Juror.class);
            JurorPool jurorPool = mock(JurorPool.class);

            createJurorPoolMock(TestConstants.VALID_POOL_NUMBER, poolStatus, TestConstants.VALID_JUROR_NUMBER,
                firstName, lastName, jurorPool, juror);

            CompleteServiceJurorNumberListDto completeServiceJurorNumberListDto =
                createCompleteServiceJurorNumberListDto(localDate, TestConstants.VALID_JUROR_NUMBER);

            completeServiceService
                .completeService(TestConstants.VALID_POOL_NUMBER, completeServiceJurorNumberListDto);

            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndPoolPoolNumber(TestConstants.VALID_JUROR_NUMBER,
                    TestConstants.VALID_POOL_NUMBER);

            validateCompleteWasSuccess(jurorPool, juror, localDate);

            verifyNoMoreInteractions(jurorPoolRepository, jurorPoolRepository, jurorRepository, jurorHistoryService);

        }

        @Test
        void positiveTypicalSingle() {
            assertValidStatus(IJurorStatus.RESPONDED);
        }

        @Test
        void positiveTypicalMultiple() {

            final String firstName1 = RandomStringUtils.randomAlphabetic(20);
            final String lastName1 = RandomStringUtils.randomAlphabetic(20);
            final String firstName2 = RandomStringUtils.randomAlphabetic(20);
            final String lastName2 = RandomStringUtils.randomAlphabetic(20);
            final String firstName3 = RandomStringUtils.randomAlphabetic(20);
            final String lastName3 = RandomStringUtils.randomAlphabetic(20);

            final int poolStatus = IJurorStatus.RESPONDED;


            Juror juror1 = mock(Juror.class);
            JurorPool jurorPool1 = mock(JurorPool.class);
            Juror juror2 = mock(Juror.class);
            JurorPool jurorPool2 = mock(JurorPool.class);
            Juror juror3 = mock(Juror.class);
            JurorPool jurorPool3 = mock(JurorPool.class);

            createJurorPoolMock(TestConstants.VALID_POOL_NUMBER, poolStatus, "123456789",
                firstName1, lastName1, jurorPool1, juror1);
            createJurorPoolMock(TestConstants.VALID_POOL_NUMBER, poolStatus, "123456788",
                firstName2, lastName2, jurorPool2, juror2);
            createJurorPoolMock(TestConstants.VALID_POOL_NUMBER, poolStatus, "123456787",
                firstName3, lastName3, jurorPool3, juror3);

            LocalDate localDate = LocalDate.of(2023, 11, 22);
            CompleteServiceJurorNumberListDto completeServiceJurorNumberListDto =
                createCompleteServiceJurorNumberListDto(localDate, "123456789", "123456788", "123456787");

            completeServiceService
                .completeService(TestConstants.VALID_POOL_NUMBER, completeServiceJurorNumberListDto);


            validateCompleteWasSuccess(jurorPool1, juror1, localDate);
            validateCompleteWasSuccess(jurorPool2, juror2, localDate);
            validateCompleteWasSuccess(jurorPool3, juror3, localDate);


            verifyNoMoreInteractions(jurorPoolRepository, jurorPoolRepository, jurorRepository, jurorHistoryService);
        }

        private void validateCompleteWasSuccess(JurorPool jurorPool, Juror juror, LocalDate completionDate) {

            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndPoolPoolNumber(juror.getJurorNumber(),
                    TestConstants.VALID_POOL_NUMBER);
            verify(juror, times(1)).getJurorNumber(); // Required for verifyNoMoreInteractions

            verify(jurorHistoryService, times(1))
                .createCompleteServiceHistory(jurorPool);
            verify(jurorPool, times(1)).getJuror();
            verify(jurorPool, times(1)).getStatus();
            verify(jurorPool, times(1)).setStatus(completedStatus);
            verify(jurorPool, never()).setIsActive(anyBoolean());
            verify(jurorPool, times(1)).setOnCall(false);

            verify(juror, times(1))
                .setCompletionDate(completionDate);
            verify(jurorRepository, times(1)).save(juror);
            verify(jurorPoolRepository, times(1)).save(jurorPool);
            verifyNoMoreInteractions(juror, jurorPool);
        }

        @Test
        void negativeNotInTransferredStatus() {
            assertInvalidStatus(IJurorStatus.TRANSFERRED);
        }

        @Test
        void negativeNotInFailedToAttendStatus() {
            assertInvalidStatus(IJurorStatus.FAILED_TO_ATTEND);
        }

        @Test
        void negativeNotInSummonedStatus() {
            assertInvalidStatus(IJurorStatus.SUMMONED);
        }

        @Test
        void samplePositiveCompletedStatus() {
            assertValidStatus(IJurorStatus.COMPLETED);
        }

        @Test
        void samplePositiveExcusedStatus() {
            assertValidStatus(IJurorStatus.EXCUSED);
        }

        @Test
        void negativeNotFound() {
            LocalDate localDate = LocalDate.of(2023, 11, 22);
            when(jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER))
                .thenReturn(null);

            CompleteServiceJurorNumberListDto completeServiceJurorNumberListDto =
                createCompleteServiceJurorNumberListDto(localDate, TestConstants.VALID_JUROR_NUMBER);

            MojException.NotFound exception = assertThrows(MojException.NotFound.class,
                () -> completeServiceService
                    .completeService(TestConstants.VALID_POOL_NUMBER, completeServiceJurorNumberListDto),
                "Expected exception to be thrown when juror pool not found");

            assertEquals("Juror number " + TestConstants.VALID_JUROR_NUMBER + " not found in pool "
                    + TestConstants.VALID_POOL_NUMBER,
                exception.getMessage(),
                "Expected exception message to be " + "Juror number " + TestConstants.VALID_JUROR_NUMBER
                    + " not found in pool " + TestConstants.VALID_POOL_NUMBER);


            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndPoolPoolNumber(TestConstants.VALID_JUROR_NUMBER,
                    TestConstants.VALID_POOL_NUMBER);

            verifyNoMoreInteractions(jurorPoolRepository, jurorPoolRepository, jurorRepository, jurorHistoryService);
        }
    }

    @Nested
    @DisplayName("completeDismissalService")
    class CompleteDismissalService {

        private JurorStatus completedStatus;

        @BeforeEach
        void beforeEach() {
            completedStatus = new JurorStatus();
            when(jurorStatusRepository.findById(IJurorStatus.COMPLETED))
                .thenReturn(Optional.ofNullable(completedStatus));
        }

        @Test
        void positiveTypicalSingle() {
            LocalDate localDate = LocalDate.now();

            final String firstName = RandomStringUtils.randomAlphabetic(20);
            final String lastName = RandomStringUtils.randomAlphabetic(20);
            final int poolStatus = IJurorStatus.RESPONDED;
            Juror juror = mock(Juror.class);
            JurorPool jurorPool = mock(JurorPool.class);

            CompleteServiceJurorNumberListDto completeServiceJurorNumberListDto =
                createCompleteServiceJurorNumberListDto(localDate, TestConstants.VALID_JUROR_NUMBER);

            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(poolStatus);

            createMockJurorAndPool(firstName, lastName, juror, jurorPool, jurorStatus,
                TestConstants.VALID_JUROR_NUMBER);

            completeServiceService
                .completeDismissedJurorsService(completeServiceJurorNumberListDto);

            validateCompleteDismissal(localDate, juror, jurorPool, jurorStatus);
            verifyNoMoreInteractions(jurorPoolRepository, jurorPoolRepository, jurorRepository, jurorHistoryService);
        }

        private void createMockJurorAndPool(String firstName, String lastName, Juror juror, JurorPool jurorPool,
                                            JurorStatus jurorStatus, String jurorNumber) {
            when(juror.getJurorNumber()).thenReturn(jurorNumber);
            when(juror.getFirstName()).thenReturn(firstName);
            when(juror.getLastName()).thenReturn(lastName);

            when(jurorPool.getJuror()).thenReturn(juror);

            when(jurorPoolRepository.findByJurorJurorNumberAndStatusAndIsActive(jurorNumber,
                jurorStatus, true)).thenReturn(jurorPool);

            when(jurorStatusRepository.findById(IJurorStatus.RESPONDED))
                .thenReturn(Optional.ofNullable(jurorStatus));
        }

        private void validateCompleteDismissal(LocalDate localDate, Juror juror, JurorPool jurorPool,
                                               JurorStatus jurorStatus) {
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndStatusAndIsActive(TestConstants.VALID_JUROR_NUMBER,
                    jurorStatus, true);

            verify(jurorHistoryService, times(1))
                .createCompleteServiceHistory(jurorPool);
            verify(jurorPool, times(1)).getJuror();
            verify(jurorPool, times(1)).setStatus(completedStatus);
            verify(jurorPool, times(1)).setNextDate(null);
            verify(jurorPool, times(1)).setOnCall(false);
            verify(jurorPool, never()).setIsActive(anyBoolean());

            verify(juror, times(1))
                .setCompletionDate(localDate);
            verify(jurorRepository, times(1)).save(juror);
            verify(jurorPoolRepository, times(1)).save(jurorPool);
            verifyNoMoreInteractions(juror, jurorPool);

        }

        @Test
        @SuppressWarnings({
            "PMD.JUnitTestsShouldIncludeAssert"//False positive
        })
        void positiveTypicalMultiple() {

            final String firstName1 = RandomStringUtils.randomAlphabetic(20);
            final String lastName1 = RandomStringUtils.randomAlphabetic(20);
            final String firstName2 = RandomStringUtils.randomAlphabetic(20);
            final String lastName2 = RandomStringUtils.randomAlphabetic(20);
            final String firstName3 = RandomStringUtils.randomAlphabetic(20);
            final String lastName3 = RandomStringUtils.randomAlphabetic(20);
            final int poolStatus = IJurorStatus.RESPONDED;

            Juror juror1 = mock(Juror.class);
            JurorPool jurorPool1 = mock(JurorPool.class);

            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(poolStatus);

            createMockJurorAndPool(firstName1, lastName1, juror1, jurorPool1, jurorStatus, "123456789");

            Juror juror2 = mock(Juror.class);
            JurorPool jurorPool2 = mock(JurorPool.class);
            createMockJurorAndPool(firstName2, lastName2, juror2, jurorPool2, jurorStatus, "123456788");

            Juror juror3 = mock(Juror.class);
            JurorPool jurorPool3 = mock(JurorPool.class);
            createMockJurorAndPool(firstName3, lastName3, juror3, jurorPool3, jurorStatus, "123456787");

            LocalDate localDate = LocalDate.now();
            CompleteServiceJurorNumberListDto completeServiceJurorNumberListDto =
                createCompleteServiceJurorNumberListDto(localDate, "123456789", "123456788", "123456787");

            completeServiceService.completeDismissedJurorsService(completeServiceJurorNumberListDto);
            validateCompleteDismissal(localDate, juror1, jurorPool1, jurorStatus);
            validateCompleteDismissal(localDate, juror2, jurorPool2, jurorStatus);
            validateCompleteDismissal(localDate, juror3, jurorPool3, jurorStatus);
        }

        @Test
        void negativeNotFound() {
            JurorStatus jurorStatus = new JurorStatus();
            jurorStatus.setStatus(IJurorStatus.RESPONDED);
            when(jurorPoolRepository.findByJurorJurorNumberAndStatusAndIsActive(TestConstants.VALID_JUROR_NUMBER,
                jurorStatus, true))
                .thenReturn(null);

            when(jurorStatusRepository.findById(IJurorStatus.RESPONDED))
                .thenReturn(Optional.ofNullable(jurorStatus));
            LocalDate localDate = LocalDate.of(2023, 11, 22);
            CompleteServiceJurorNumberListDto completeServiceJurorNumberListDto =
                createCompleteServiceJurorNumberListDto(localDate, TestConstants.VALID_JUROR_NUMBER);

            MojException.NotFound exception = assertThrows(MojException.NotFound.class,
                () -> completeServiceService
                    .completeDismissedJurorsService(completeServiceJurorNumberListDto),
                "Expected exception to be thrown when juror pool not found");

            assertEquals("Juror number " + TestConstants.VALID_JUROR_NUMBER + " not found in database",
                exception.getMessage(),
                "Expected exception message to be " + "Juror number " + TestConstants.VALID_JUROR_NUMBER
                    + " not found in database");

            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndStatusAndIsActive(TestConstants.VALID_JUROR_NUMBER,
                    jurorStatus, true);

            verifyNoMoreInteractions(jurorPoolRepository, jurorPoolRepository, jurorRepository, jurorHistoryService);
        }
    }

    @Nested
    @DisplayName("validateCanCompleteService")
    class ValidateCanCompleteService {

        @Test
        void positiveValidateCanCompleteServiceSingleRequest() {
            final String firstName = RandomStringUtils.randomAlphabetic(20);
            final String lastName = RandomStringUtils.randomAlphabetic(20);
            final int poolStatus = IJurorStatus.RESPONDED;

            createJurorPoolMock(TestConstants.VALID_POOL_NUMBER, poolStatus, TestConstants.VALID_JUROR_NUMBER,
                firstName, lastName);
            JurorNumberListDto jurorNumberListDto = createJurorNumberListDto(TestConstants.VALID_JUROR_NUMBER);

            CompleteServiceValidationResponseDto completeServiceValidationResponseDto = completeServiceService
                .validateCanCompleteService(TestConstants.VALID_POOL_NUMBER, jurorNumberListDto);

            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndPoolPoolNumber(TestConstants.VALID_JUROR_NUMBER,
                    TestConstants.VALID_POOL_NUMBER);
            assertEquals(0, completeServiceValidationResponseDto.getInvalidNotResponded().size(),
                "Expected 0 invalid juror number");
            assertEquals(1, completeServiceValidationResponseDto.getValid().size(),
                "Expected 1 valid juror number");
            JurorStatusValidationResponseDto validJuror = completeServiceValidationResponseDto.getValid().get(0);

            validateJurorStatusValidationResponseDto(validJuror,
                TestConstants.VALID_JUROR_NUMBER,
                firstName,
                lastName,
                poolStatus);

            verifyNoMoreInteractions(jurorPoolRepository);
        }

        @Test
        void positiveValidateCanCompleteServiceMultipleAllValid() {
            final String firstName1 = RandomStringUtils.randomAlphabetic(20);
            final String lastName1 = RandomStringUtils.randomAlphabetic(20);
            final String firstName2 = RandomStringUtils.randomAlphabetic(20);
            final String lastName2 = RandomStringUtils.randomAlphabetic(20);
            final String firstName3 = RandomStringUtils.randomAlphabetic(20);
            final String lastName3 = RandomStringUtils.randomAlphabetic(20);
            final int poolStatus = IJurorStatus.RESPONDED;


            createJurorPoolMock(TestConstants.VALID_POOL_NUMBER, poolStatus, "123456789",
                firstName1, lastName1);
            createJurorPoolMock(TestConstants.VALID_POOL_NUMBER, poolStatus, "123456788",
                firstName2, lastName2);
            createJurorPoolMock(TestConstants.VALID_POOL_NUMBER, poolStatus, "123456787",
                firstName3, lastName3);

            JurorNumberListDto jurorNumberListDto = createJurorNumberListDto("123456789", "123456788", "123456787");

            CompleteServiceValidationResponseDto completeServiceValidationResponseDto = completeServiceService
                .validateCanCompleteService(TestConstants.VALID_POOL_NUMBER, jurorNumberListDto);

            assertEquals(0, completeServiceValidationResponseDto.getInvalidNotResponded().size(),
                "Expected 0 invalid juror number");
            assertEquals(3, completeServiceValidationResponseDto.getValid().size(),
                "Expected 3 valid juror number");

            validateJurorStatusValidationResponseDto(completeServiceValidationResponseDto.getValid().get(0),
                "123456789",
                firstName1,
                lastName1,
                poolStatus);
            validateJurorStatusValidationResponseDto(completeServiceValidationResponseDto.getValid().get(1),
                "123456788",
                firstName2,
                lastName2,
                poolStatus);
            validateJurorStatusValidationResponseDto(completeServiceValidationResponseDto.getValid().get(2),
                "123456787",
                firstName3,
                lastName3,
                poolStatus);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndPoolPoolNumber("123456789", TestConstants.VALID_POOL_NUMBER);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndPoolPoolNumber("123456788", TestConstants.VALID_POOL_NUMBER);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndPoolPoolNumber("123456787", TestConstants.VALID_POOL_NUMBER);

            verifyNoMoreInteractions(jurorPoolRepository);
        }

        @Test
        void positiveValidateCanCompleteServiceMultipleAllInvalid() {
            final String firstName1 = RandomStringUtils.randomAlphabetic(20);
            final String lastName1 = RandomStringUtils.randomAlphabetic(20);
            final int poolStatus1 = IJurorStatus.TRANSFERRED;

            final String firstName2 = RandomStringUtils.randomAlphabetic(20);
            final String lastName2 = RandomStringUtils.randomAlphabetic(20);
            final int poolStatus2 = IJurorStatus.FAILED_TO_ATTEND;

            final String firstName3 = RandomStringUtils.randomAlphabetic(20);
            final String lastName3 = RandomStringUtils.randomAlphabetic(20);
            final int poolStatus3 = IJurorStatus.SUMMONED;


            createJurorPoolMock(TestConstants.VALID_POOL_NUMBER, poolStatus1, "123456789",
                firstName1, lastName1);
            createJurorPoolMock(TestConstants.VALID_POOL_NUMBER, poolStatus2, "123456788",
                firstName2, lastName2);
            createJurorPoolMock(TestConstants.VALID_POOL_NUMBER, poolStatus3, "123456787",
                firstName3, lastName3);

            JurorNumberListDto jurorNumberListDto = createJurorNumberListDto("123456789", "123456788", "123456787");

            CompleteServiceValidationResponseDto completeServiceValidationResponseDto = completeServiceService
                .validateCanCompleteService(TestConstants.VALID_POOL_NUMBER, jurorNumberListDto);

            assertEquals(0, completeServiceValidationResponseDto.getValid().size(),
                "Expected 0 valid juror number");
            assertEquals(3, completeServiceValidationResponseDto.getInvalidNotResponded().size(),
                "Expected 3 invalid juror number");

            validateJurorStatusValidationResponseDto(
                completeServiceValidationResponseDto.getInvalidNotResponded().get(0),
                "123456789",
                firstName1,
                lastName1,
                poolStatus1);
            validateJurorStatusValidationResponseDto(
                completeServiceValidationResponseDto.getInvalidNotResponded().get(1),
                "123456788",
                firstName2,
                lastName2,
                poolStatus2);
            validateJurorStatusValidationResponseDto(
                completeServiceValidationResponseDto.getInvalidNotResponded().get(2),
                "123456787",
                firstName3,
                lastName3,
                poolStatus3);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndPoolPoolNumber("123456789", TestConstants.VALID_POOL_NUMBER);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndPoolPoolNumber("123456788", TestConstants.VALID_POOL_NUMBER);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndPoolPoolNumber("123456787", TestConstants.VALID_POOL_NUMBER);

            verifyNoMoreInteractions(jurorPoolRepository);
        }

        @Test
        void positiveValidateCanCompleteServiceMultipleMixValidAndInvalid() {
            final String poolNumber = "012345678";
            final String firstName1 = RandomStringUtils.randomAlphabetic(20);
            final String lastName1 = RandomStringUtils.randomAlphabetic(20);
            final int poolStatus1 = IJurorStatus.SUMMONED;

            final String firstName2 = RandomStringUtils.randomAlphabetic(20);
            final String lastName2 = RandomStringUtils.randomAlphabetic(20);
            final int poolStatus2 = IJurorStatus.RESPONDED;

            final String firstName3 = RandomStringUtils.randomAlphabetic(20);
            final String lastName3 = RandomStringUtils.randomAlphabetic(20);
            final int poolStatus3 = IJurorStatus.TRANSFERRED;


            createJurorPoolMock(poolNumber, poolStatus1, "123456789",
                firstName1, lastName1);
            createJurorPoolMock(poolNumber, poolStatus2, "123456788",
                firstName2, lastName2);
            createJurorPoolMock(poolNumber, poolStatus3, "123456787",
                firstName3, lastName3);

            JurorNumberListDto jurorNumberListDto = createJurorNumberListDto("123456789", "123456788", "123456787");

            CompleteServiceValidationResponseDto completeServiceValidationResponseDto = completeServiceService
                .validateCanCompleteService(poolNumber, jurorNumberListDto);

            assertEquals(1, completeServiceValidationResponseDto.getValid().size(),
                "Expected 1 valid juror number");
            assertEquals(2, completeServiceValidationResponseDto.getInvalidNotResponded().size(),
                "Expected 2 invalid juror number");

            validateJurorStatusValidationResponseDto(
                completeServiceValidationResponseDto.getInvalidNotResponded().get(0),
                "123456789",
                firstName1,
                lastName1,
                poolStatus1);
            validateJurorStatusValidationResponseDto(completeServiceValidationResponseDto.getValid().get(0),
                "123456788",
                firstName2,
                lastName2,
                poolStatus2);
            validateJurorStatusValidationResponseDto(
                completeServiceValidationResponseDto.getInvalidNotResponded().get(1),
                "123456787",
                firstName3,
                lastName3,
                poolStatus3);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndPoolPoolNumber("123456789", poolNumber);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndPoolPoolNumber("123456788", poolNumber);
            verify(jurorPoolRepository, times(1))
                .findByJurorJurorNumberAndPoolPoolNumber("123456787", poolNumber);

            verifyNoMoreInteractions(jurorPoolRepository);
        }

        @Test
        void negativeValidateCanCompleteServiceJurorPoolNotFound() {

            when(jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER))
                .thenReturn(null);

            JurorNumberListDto jurorNumberListDto = createJurorNumberListDto(TestConstants.VALID_JUROR_NUMBER);

            MojException.NotFound notFoundException =
                assertThrows(MojException.NotFound.class, () -> completeServiceService
                        .validateCanCompleteService(TestConstants.VALID_POOL_NUMBER, jurorNumberListDto),
                    "Expected exception to be thrown when juror pool not found");

            assertEquals("Juror number " + TestConstants.VALID_JUROR_NUMBER + " not found in pool "
                    + TestConstants.VALID_POOL_NUMBER,
                notFoundException.getMessage(),
                "Expected exception message to be " + "Juror number " + TestConstants.VALID_JUROR_NUMBER
                    + " not found in pool " + TestConstants.VALID_POOL_NUMBER);
        }
    }


    @Nested
    @DisplayName("public void uncompleteJurorsService(String jurorNumber, String poolNumber)")
    class UncompleteJurorsService {
        @Test
        void positiveTypical() {
            JurorPool jurorPool = mock(JurorPool.class);
            Juror juror = mock(Juror.class);
            when(jurorPool.getJuror()).thenReturn(juror);
            JurorStatus completeStatus = mock(JurorStatus.class);
            JurorStatus respondedStatus = mock(JurorStatus.class);

            when(jurorStatusRepository.findById(IJurorStatus.COMPLETED))
                .thenReturn(Optional.of(completeStatus));

            when(jurorStatusRepository.findById(IJurorStatus.RESPONDED))
                .thenReturn(Optional.of(respondedStatus));

            doReturn(jurorPool)
                .when(jurorPoolRepository)
                .findByJurorJurorNumberAndPoolPoolNumberAndStatus(TestConstants.VALID_JUROR_NUMBER,
                    TestConstants.VALID_POOL_NUMBER, completeStatus);


            completeServiceService.uncompleteJurorsService(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER);


            verify(jurorStatusRepository, times(1)).findById(IJurorStatus.COMPLETED);
            verify(jurorStatusRepository, times(1)).findById(IJurorStatus.RESPONDED);

            verify(jurorPool, times(1)).setStatus(respondedStatus);
            verify(jurorPool, times(1)).getJuror();
            verify(juror, times(1)).setCompletionDate(null);

            verify(jurorHistoryService, times(1)).createUncompleteServiceHistory(jurorPool);
            verify(jurorRepository, times(1)).save(juror);
            verify(jurorPoolRepository, times(1)).save(jurorPool);


            verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndPoolPoolNumberAndStatus(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER, completeStatus);


            verifyNoMoreInteractions(jurorStatusRepository,
                jurorHistoryService, jurorRepository, jurorPoolRepository,
                jurorPool, completeStatus, respondedStatus, juror);
        }

        @Test
        void negativeNullPools() {
            JurorStatus completeStatus = mock(JurorStatus.class);

            when(jurorStatusRepository.findById(IJurorStatus.COMPLETED))
                .thenReturn(Optional.of(completeStatus));

            doReturn(null)
                .when(jurorPoolRepository)
                .findByJurorJurorNumberAndPoolPoolNumberAndStatus(TestConstants.VALID_JUROR_NUMBER,
                    TestConstants.VALID_POOL_NUMBER, completeStatus);


            MojException.NotFound exception = assertThrows(MojException.NotFound.class,
                () -> completeServiceService.uncompleteJurorsService(
                    TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER),
                "Exception should be thrown");

            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage()).isEqualTo(
                "No complete juror pool found for Juror number " + TestConstants.VALID_JUROR_NUMBER);


            verify(jurorStatusRepository, times(1)).findById(IJurorStatus.COMPLETED);
            verify(jurorPoolRepository, times(1)).findByJurorJurorNumberAndPoolPoolNumberAndStatus(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER, completeStatus);


            verifyNoMoreInteractions(jurorStatusRepository,
                jurorHistoryService, jurorRepository, jurorPoolRepository, completeStatus);

        }
    }

    private void createJurorPoolMock(String jurorPoolNumber, int jurorPoolStatus, String jurorNumber,
                                     String firstName, String lastName) {
        createJurorPoolMock(jurorPoolNumber, jurorPoolStatus, jurorNumber, firstName, lastName,
            mock(JurorPool.class), mock(Juror.class));
    }

    private void createJurorPoolMock(String jurorPoolNumber, int jurorPoolStatus, String jurorNumber,
                                     String firstName, String lastName, JurorPool jurorPool, Juror juror) {
        JurorStatus jurorStatus = new JurorStatus();
        jurorStatus.setStatus(jurorPoolStatus);


        when(juror.getJurorNumber()).thenReturn(jurorNumber);
        when(juror.getFirstName()).thenReturn(firstName);
        when(juror.getLastName()).thenReturn(lastName);

        when(jurorPool.getJuror()).thenReturn(juror);
        when(jurorPool.getStatus()).thenReturn(jurorStatus);

        when(jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(jurorNumber, jurorPoolNumber))
            .thenReturn(jurorPool);
    }

    private void validateJurorStatusValidationResponseDto(JurorStatusValidationResponseDto validJuror,
                                                          String jurorNumber, String firstName, String lastName,
                                                          int status) {
        assertEquals(jurorNumber, validJuror.getJurorNumber(),
            "Expected valid juror number to be " + jurorNumber);
        assertEquals(firstName, validJuror.getFirstName(),
            "Expected valid juror first name to be " + firstName);
        assertEquals(lastName, validJuror.getLastName(),
            "Expected valid juror last name to be " + lastName);
        assertEquals(status, validJuror.getStatus(),
            "Expected valid juror status to be " + status);
    }

    private JurorNumberListDto createJurorNumberListDto(String... jurorNumbers) {
        JurorNumberListDto jurorNumberListDto = new JurorNumberListDto();
        jurorNumberListDto.setJurorNumbers(Arrays.stream(jurorNumbers).toList());
        return jurorNumberListDto;
    }

    private CompleteServiceJurorNumberListDto createCompleteServiceJurorNumberListDto(LocalDate completionDate,
                                                                                      String... jurorNumbers) {
        CompleteServiceJurorNumberListDto jurorNumberListDto = new CompleteServiceJurorNumberListDto();
        jurorNumberListDto.setJurorNumbers(Arrays.stream(jurorNumbers).toList());
        jurorNumberListDto.setCompletionDate(completionDate);
        return jurorNumberListDto;
    }
}
