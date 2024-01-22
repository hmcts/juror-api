package uk.gov.hmcts.juror.api.moj.service.expense;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.DefaultExpenseSummaryDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseItemsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.BulkExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.BulkExpenseEntryDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.TotalExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.UnpaidExpenseSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.AppearanceId;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorExpenseTotals;
import uk.gov.hmcts.juror.api.moj.domain.SortDirection;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.FinancialAuditDetailsRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorExpenseTotalsRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage.EXPENSE_AUTHORISED;
import static uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage.EXPENSE_ENTERED;

@ExtendWith(SpringExtension.class)
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.LawOfDemeter"})
public class JurorExpenseServiceTest {

    @Mock
    private JurorExpenseTotalsRepository jurorExpenseTotalsRepository;
    @Mock
    private JurorRepository jurorRepository;
    @Mock
    private FinancialAuditDetailsRepository financialAuditDetailsRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AppearanceRepository appearanceRepository;

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;

    @InjectMocks
    private JurorExpenseServiceImpl jurorExpenseService;


    @Nested
    @DisplayName("public Page<UnpaidExpenseSummaryResponseDto> getUnpaidExpensesForCourtLocation")
    class GetUnpaidExpensesForCourtLocation {

        @BeforeEach
        void mockCurrentUser() {
            securityUtilMockedStatic = mockStatic(SecurityUtil.class);
            securityUtilMockedStatic.when(SecurityUtil::getActiveLogin).thenReturn("CURRENT_USER");
        }

        @AfterEach
        void afterEach() {
            if (securityUtilMockedStatic != null) {
                securityUtilMockedStatic.close();
            }
        }


        @Test
        @DisplayName("Happy path - No date range filter applied")
        void happyPathNoDateRange() {
            String courtLocation = "415";
            SortDirection sortDirection = SortDirection.ASC;
            String sortBy = "jurorNumber";
            int pageNumber = 1;

            LocalDate minDate = null;
            LocalDate maxDate = null;

            doReturn(2L).when(jurorExpenseTotalsRepository)
                .countByCourtLocationCodeAndTotalUnapprovedGreaterThan(anyString(), anyFloat());
            doReturn(createJurorExpenseTotals(courtLocation)).when(jurorExpenseTotalsRepository)
                .findUnpaidByCourtLocationCode(anyString(), any(Pageable.class));

            Page<UnpaidExpenseSummaryResponseDto> responseDtoPage =
                jurorExpenseService.getUnpaidExpensesForCourtLocation(courtLocation, minDate, maxDate, pageNumber,
                    sortBy, sortDirection);

            assertThat(responseDtoPage).isNotEmpty();
            List<UnpaidExpenseSummaryResponseDto> responseDtoList = responseDtoPage.getContent();
            assertThat(responseDtoList).isNotEmpty();
            assertThat(responseDtoList.size()).isEqualTo(2);

            UnpaidExpenseSummaryResponseDto firstResult = responseDtoList.get(0);
            assertThat(firstResult.getJurorNumber()).isEqualToIgnoringCase("111111111");
            assertThat(firstResult.getPoolNumber()).isEqualToIgnoringCase("999999999");
            assertThat(firstResult.getFirstName()).isEqualToIgnoringCase("Test");
            assertThat(firstResult.getLastName()).isEqualToIgnoringCase("Person");
            assertThat(firstResult.getTotalUnapproved().compareTo(BigDecimal.valueOf(50.67))).isEqualTo(0);

            UnpaidExpenseSummaryResponseDto secondResult = responseDtoList.get(1);
            assertThat(secondResult.getJurorNumber()).isEqualToIgnoringCase("222222222");
            assertThat(secondResult.getPoolNumber()).isEqualToIgnoringCase("888888888");
            assertThat(secondResult.getFirstName()).isEqualToIgnoringCase("John");
            assertThat(secondResult.getLastName()).isEqualToIgnoringCase("Smith");
            assertThat(secondResult.getTotalUnapproved().compareTo(BigDecimal.valueOf(76.05))).isEqualTo(0);

            verify(jurorExpenseTotalsRepository, never())
                .countUnpaidByCourtLocationCodeAndAppearanceDate(anyString(), any(LocalDate.class),
                    any(LocalDate.class));
            verify(jurorExpenseTotalsRepository, never())
                .findUnpaidByCourtLocationCodeAndAppearanceDate(anyString(), any(LocalDate.class),
                    any(LocalDate.class), any(Pageable.class));

            verify(jurorExpenseTotalsRepository, times(1))
                .countByCourtLocationCodeAndTotalUnapprovedGreaterThan(anyString(), anyFloat());
            verify(jurorExpenseTotalsRepository, times(1))
                .findUnpaidByCourtLocationCode(anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("Happy path - Date range filter applied")
        void happyPathWithDateRange() {
            String courtLocation = "415";
            SortDirection sortDirection = SortDirection.DESC;
            String sortBy = "poolNumber";
            int pageNumber = 1;

            LocalDate minDate = LocalDate.now().minusDays(14);
            LocalDate maxDate = LocalDate.now();

            doReturn(2L).when(jurorExpenseTotalsRepository)
                .countUnpaidByCourtLocationCodeAndAppearanceDate(anyString(), any(LocalDate.class),
                    any(LocalDate.class));
            doReturn(createJurorExpenseTotals(courtLocation)).when(jurorExpenseTotalsRepository)
                .findUnpaidByCourtLocationCodeAndAppearanceDate(anyString(), any(LocalDate.class),
                    any(LocalDate.class), any(Pageable.class));

            Page<UnpaidExpenseSummaryResponseDto> responseDtoPage =
                jurorExpenseService.getUnpaidExpensesForCourtLocation(courtLocation, minDate, maxDate, pageNumber,
                    sortBy, sortDirection);

            assertThat(responseDtoPage).isNotEmpty();
            List<UnpaidExpenseSummaryResponseDto> responseDtoList = responseDtoPage.getContent();
            assertThat(responseDtoList).isNotEmpty();
            assertThat(responseDtoList.size()).isEqualTo(2);

            UnpaidExpenseSummaryResponseDto firstResult = responseDtoList.get(0);
            assertThat(firstResult.getJurorNumber()).isEqualToIgnoringCase("111111111");
            assertThat(firstResult.getPoolNumber()).isEqualToIgnoringCase("999999999");
            assertThat(firstResult.getFirstName()).isEqualToIgnoringCase("Test");
            assertThat(firstResult.getLastName()).isEqualToIgnoringCase("Person");
            assertThat(firstResult.getTotalUnapproved().compareTo(BigDecimal.valueOf(50.67))).isEqualTo(0);

            UnpaidExpenseSummaryResponseDto secondResult = responseDtoList.get(1);
            assertThat(secondResult.getJurorNumber()).isEqualToIgnoringCase("222222222");
            assertThat(secondResult.getPoolNumber()).isEqualToIgnoringCase("888888888");
            assertThat(secondResult.getFirstName()).isEqualToIgnoringCase("John");
            assertThat(secondResult.getLastName()).isEqualToIgnoringCase("Smith");
            assertThat(secondResult.getTotalUnapproved().compareTo(BigDecimal.valueOf(76.05))).isEqualTo(0);

            verify(jurorExpenseTotalsRepository, times(1))
                .countUnpaidByCourtLocationCodeAndAppearanceDate(anyString(), any(LocalDate.class),
                    any(LocalDate.class));
            verify(jurorExpenseTotalsRepository, times(1))
                .findUnpaidByCourtLocationCodeAndAppearanceDate(anyString(), any(LocalDate.class),
                    any(LocalDate.class), any(Pageable.class));

            verify(jurorExpenseTotalsRepository, never())
                .countByCourtLocationCodeAndTotalUnapprovedGreaterThan(anyString(), anyFloat());
            verify(jurorExpenseTotalsRepository, never())
                .findUnpaidByCourtLocationCode(anyString(), any(Pageable.class));
        }

        @Test
        @DisplayName("403 Forbidden - User with incorrect permissions")
        void invalidUser() {
            securityUtilMockedStatic.when(() -> SecurityUtil.validateCourtLocationPermitted(anyString()))
                .thenThrow(
                    new MojException.Forbidden("Current user does not have permissions to " + "Court Location", null));

            String courtLocation = "415";
            SortDirection sortDirection = SortDirection.DESC;
            String sortBy = "poolNumber";
            int pageNumber = 1;

            LocalDate minDate = LocalDate.now().minusDays(14);
            LocalDate maxDate = LocalDate.now();

            assertThatExceptionOfType(MojException.Forbidden.class).isThrownBy(
                () -> jurorExpenseService.getUnpaidExpensesForCourtLocation(courtLocation, minDate, maxDate, pageNumber,
                    sortBy, sortDirection));


            verify(jurorExpenseTotalsRepository, never())
                .countUnpaidByCourtLocationCodeAndAppearanceDate(anyString(), any(LocalDate.class),
                    any(LocalDate.class));
            verify(jurorExpenseTotalsRepository, never())
                .findUnpaidByCourtLocationCodeAndAppearanceDate(anyString(), any(LocalDate.class),
                    any(LocalDate.class), any(Pageable.class));

            verify(jurorExpenseTotalsRepository, never())
                .countByCourtLocationCodeAndTotalUnapprovedGreaterThan(anyString(), anyFloat());
            verify(jurorExpenseTotalsRepository, never())
                .findUnpaidByCourtLocationCode(anyString(), any(Pageable.class));

        }

        private List<JurorExpenseTotals> createJurorExpenseTotals(String locCode) {
            JurorExpenseTotals jurorExpenseTotals1 =
                JurorExpenseTotals.builder().courtLocationCode(locCode).jurorNumber("111111111").poolNumber("999999999")
                    .firstName("Test").lastName("Person").totalUnapproved(BigDecimal.valueOf(50.67)).build();

            JurorExpenseTotals jurorExpenseTotals2 =
                JurorExpenseTotals.builder().courtLocationCode(locCode).jurorNumber("222222222").poolNumber("888888888")
                    .firstName("John").lastName("Smith").totalUnapproved(BigDecimal.valueOf(76.05)).build();

            return Arrays.asList(jurorExpenseTotals1, jurorExpenseTotals2);
        }
    }

    @Nested
    @DisplayName("Integer getJurorDefaultMileage(String jurorNumber)")
    class GetJurorDefaultMileage {
        @Test
        void typical() {
            Juror juror = mock(Juror.class);
            when(juror.getMileage()).thenReturn(5);
            when(jurorRepository.findByJurorNumber(TestConstants.VALID_JUROR_NUMBER)).thenReturn(juror);
            assertEquals(5, jurorExpenseService.getJurorDefaultMileage(TestConstants.VALID_JUROR_NUMBER));
            verify(juror, times(1)).getMileage();
            verify(jurorRepository, times(1)).findByJurorNumber(TestConstants.VALID_JUROR_NUMBER);
            verifyNoMoreInteractions(juror, jurorRepository);
        }

        @Test
        void notFound() {
            MojException.NotFound exception =
                assertThrows(MojException.NotFound.class,
                    () -> jurorExpenseService.getJurorDefaultMileage(TestConstants.VALID_JUROR_NUMBER),
                    "Should throw an exception when juror can not be found.");
            assertEquals("Juror not found: " + TestConstants.VALID_JUROR_NUMBER, exception.getMessage(),
                "Message should match");
            assertNull(exception.getCause(), "There should be no cause");
        }
    }

    @Nested
    @DisplayName("public BulkExpenseDto getBulkDraftExpense(String jurorNumber, String poolNumber)")
    class GetBulkDraftExpense {
        @Test
        void typical() {
            BulkExpenseDto bulkExpenseDto = new BulkExpenseDto();
            jurorExpenseService = spy(jurorExpenseService);
            doReturn(bulkExpenseDto).when(jurorExpenseService).getBulkExpense(any(), any());

            Appearance draftAppearance1 = mockAppearanceDraft(true);
            Appearance draftAppearance2 = mockAppearanceDraft(true);
            Appearance draftAppearance3 = mockAppearanceDraft(true);
            Appearance noneDraftAppearance = mockAppearanceDraft(false);

            List<Appearance> appearanceList =
                List.of(draftAppearance1, draftAppearance2, noneDraftAppearance, draftAppearance3);

            when(appearanceRepository.findAllByJurorNumberAndPoolNumber(TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER)).thenReturn(appearanceList);

            assertNull(bulkExpenseDto.getJurorNumber(), "Juror number should be null before test");

            assertEquals(bulkExpenseDto, jurorExpenseService.getBulkDraftExpense(TestConstants.VALID_JUROR_NUMBER,
                    TestConstants.VALID_POOL_NUMBER),
                "Returned bulkExpenseDto should match expected");

            assertNull(bulkExpenseDto.getJurorNumber(),
                "Juror number null at this stage as it is set by another method");

            verify(appearanceRepository, times(1)).findAllByJurorNumberAndPoolNumber(TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER);

            verify(jurorExpenseService, times(1)).getBulkExpense(
                TestConstants.VALID_JUROR_NUMBER,
                List.of(draftAppearance1, draftAppearance2, draftAppearance3));
            verify(jurorExpenseService, times(1)).getBulkDraftExpense(TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER);
            verifyNoMoreInteractions(appearanceRepository, jurorExpenseService);
        }
    }

    @Nested
    @DisplayName("public BulkExpenseDto getBulkExpense(String jurorNumber, long financialAuditNumber)")
    class GetBulkExpense {
        @Test
        void typical() {
            final long financialAuditNumber = 123L;
            BulkExpenseDto bulkExpenseDto = new BulkExpenseDto();
            jurorExpenseService = spy(jurorExpenseService);
            doReturn(bulkExpenseDto).when(jurorExpenseService).getBulkExpense(any(), any());

            Appearance noneDraftAppearance1 = mockAppearanceDraft(false);
            Appearance noneDraftAppearance2 = mockAppearanceDraft(false);
            Appearance noneDraftAppearance3 = mockAppearanceDraft(false);
            Appearance draftAppearance = mockAppearanceDraft(true);

            List<Appearance> appearanceList =
                List.of(noneDraftAppearance1, noneDraftAppearance2, draftAppearance, noneDraftAppearance3);

            when(appearanceRepository.findAllByJurorNumberAndFinancialAuditDetailsId(TestConstants.VALID_JUROR_NUMBER,
                financialAuditNumber)).thenReturn(appearanceList);


            assertNull(bulkExpenseDto.getJurorNumber(), "Juror number should be null before test");
            assertEquals(bulkExpenseDto,
                jurorExpenseService.getBulkExpense(TestConstants.VALID_JUROR_NUMBER, financialAuditNumber),
                "Returned bulkExpenseDto should match expected");
            assertNull(bulkExpenseDto.getJurorNumber(),
                "Juror number null at this stage as it is set by another method");

            verify(appearanceRepository, times(1)).findAllByJurorNumberAndFinancialAuditDetailsId(
                TestConstants.VALID_JUROR_NUMBER, financialAuditNumber);

            verify(jurorExpenseService, times(1)).getBulkExpense(
                TestConstants.VALID_JUROR_NUMBER,
                List.of(noneDraftAppearance1, noneDraftAppearance2, noneDraftAppearance3));
            verify(jurorExpenseService, times(1)).getBulkExpense(TestConstants.VALID_JUROR_NUMBER,
                financialAuditNumber);
            verifyNoMoreInteractions(appearanceRepository, jurorExpenseService);
        }
    }

    @Nested
    @DisplayName("private BulkExpenseDto getBulkExpense(List<Appearance> appearances)")
    class GetBulkExpenseAppearances {
        @Test
        void negativeNoAppearances() {
            MojException.NotFound exception =
                assertThrows(MojException.NotFound.class,
                    () -> jurorExpenseService.getBulkExpense(TestConstants.VALID_JUROR_NUMBER, List.of()),
                    "Should throw an exception when no appearances are given");
            assertEquals("No appearances found", exception.getMessage(),
                "Message should match");
            assertNull(exception.getCause(), "There should be no cause");
        }

        @ParameterizedTest
        @EnumSource(value = AppearanceStage.class,
            mode = EnumSource.Mode.EXCLUDE,
            names = {"EXPENSE_EDITED", "EXPENSE_ENTERED", "EXPENSE_AUTHORISED"})
        void negativeIncorrectAppearanceStage(AppearanceStage invalidStage) {
            jurorExpenseService = spy(jurorExpenseService);
            List<Appearance> appearances = List.of(
                mockAppearanceWithStage(invalidStage)
            );
            when(jurorExpenseService.getAppearanceStage(appearances)).thenReturn(invalidStage);

            MojException.InternalServerError exception =
                assertThrows(MojException.InternalServerError.class,
                    () -> jurorExpenseService.getBulkExpense(TestConstants.VALID_JUROR_NUMBER, appearances),
                    "Should throw an exception when an invalid appearance Stage is given");

            assertEquals("Invalid appearance stage type: " + invalidStage, exception.getMessage(),
                "Message should match");
            assertNull(exception.getCause(), "There should be no cause");
            verify(jurorExpenseService, times(1)).getAppearanceStage(appearances);
        }

        @SuppressWarnings({"checkstyle:FallThrough", "checkstyle:MissingSwitchDefault"})
        @ParameterizedTest
        @EnumSource(value = AppearanceStage.class,
            mode = EnumSource.Mode.INCLUDE,
            names = {"EXPENSE_AUTHORISED", "EXPENSE_EDITED", "EXPENSE_ENTERED"})
        void positiveTypical(AppearanceStage stage) {
            User approvedByUser = mock(User.class);
            when(approvedByUser.getUsername()).thenReturn("Approving User");
            User submittedByUser = mock(User.class);
            when(submittedByUser.getUsername()).thenReturn("Submitted User");


            FinancialAuditDetails financialAuditDetails = new FinancialAuditDetails();

            if (AppearanceStage.EXPENSE_AUTHORISED.equals(stage) || EXPENSE_ENTERED.equals(stage)) {
                if (AppearanceStage.EXPENSE_AUTHORISED.equals(stage)) {
                    financialAuditDetails.setApprovedOn(LocalDateTime.now().minusDays(1));
                    financialAuditDetails.setApprovedBy(approvedByUser);
                    financialAuditDetails.setJurorRevisionWhenApproved(321L);
                }
                financialAuditDetails.setSubmittedOn(LocalDateTime.now().minusDays(2));
                financialAuditDetails.setSubmittedBy(submittedByUser);

            }
            jurorExpenseService = spy(jurorExpenseService);

            Appearance appearance1 = mockAppearanceWithStage(stage);
            when(appearance1.getFinancialAuditDetails()).thenReturn(
                mockFinancialAuditDetails(123L, financialAuditDetails));
            Appearance appearance2 = mockAppearanceWithStage(stage);
            when(appearance2.getFinancialAuditDetails()).thenReturn(
                mockFinancialAuditDetails(321L, financialAuditDetails));
            Appearance appearance3 = mockAppearanceWithStage(stage);
            when(appearance3.getFinancialAuditDetails()).thenReturn(
                mockFinancialAuditDetails(124L, financialAuditDetails));
            List<Appearance> appearances = List.of(appearance1, appearance2, appearance3);

            doReturn(stage).when(jurorExpenseService).getAppearanceStage(appearances);
            doReturn(5).when(jurorExpenseService).getJurorDefaultMileage(TestConstants.VALID_JUROR_NUMBER);
            List<BulkExpenseEntryDto> bulkExpenseEntryDtoList = List.of(
                mockBulkExpenseEntryDto(LocalDate.of(2023, 10, 1)),
                mockBulkExpenseEntryDto(LocalDate.of(2023, 10, 1)),
                mockBulkExpenseEntryDto(LocalDate.of(2023, 10, 1))
            );

            doReturn(bulkExpenseEntryDtoList).when(jurorExpenseService)
                .getBulkExpenseDtoEntities(appearances);
            TotalExpenseDto totalExpenseDto = mock(TotalExpenseDto.class);

            doReturn(totalExpenseDto).when(jurorExpenseService)
                .calculateBulkExpenseTotals(bulkExpenseEntryDtoList, appearances);

            BulkExpenseDto bulkExpenseDto = jurorExpenseService.getBulkExpense(TestConstants.VALID_JUROR_NUMBER,
                appearances);

            if (EXPENSE_AUTHORISED.equals(stage)) {
                assertEquals(321L, bulkExpenseDto.getJurorVersion(),
                    "Juror version should be the highest number from all appearances");
            } else {
                assertNull(bulkExpenseDto.getJurorVersion(),
                    "Juror version should be null unless stage is AUTHORISED");
            }

            assertEquals(bulkExpenseEntryDtoList, bulkExpenseDto.getExpenses(),
                "Expenses should match the calculated values");
            assertEquals(totalExpenseDto, bulkExpenseDto.getTotals(),
                "Totals should match calculated total");
            assertEquals(5, bulkExpenseDto.getMileage(),
                "Mileage should match juror default mileage");
            assertEquals(TestConstants.VALID_JUROR_NUMBER, bulkExpenseDto.getJurorNumber(),
                "Juror Number should match provided juror");
            assertEquals(stage, bulkExpenseDto.getType(),
                "Stage should match calculated stage");


            User nullUserNameUser = mock(User.class);
            when(nullUserNameUser.getUsername()).thenReturn(null);
            assertEquals(
                Optional.ofNullable(financialAuditDetails.getApprovedBy()).orElse(nullUserNameUser).getUsername(),
                bulkExpenseDto.getApprovedBy(),
                "Should match");
            assertEquals(financialAuditDetails.getApprovedOn(), bulkExpenseDto.getApprovedOn(),
                "Should match");
            assertEquals(
                Optional.ofNullable(financialAuditDetails.getSubmittedBy()).orElse(nullUserNameUser).getUsername(),
                bulkExpenseDto.getSubmittedBy(),
                "Should match");
            assertEquals(financialAuditDetails.getSubmittedOn(), bulkExpenseDto.getSubmittedOn(),
                "Should match");
            assertEquals(financialAuditDetails.getJurorRevisionWhenApproved(), bulkExpenseDto.getJurorVersion(),
                "Should match");


            verify(jurorExpenseService, times(1)).getAppearanceStage(appearances);
            verify(jurorExpenseService, times(1)).getBulkExpenseDtoEntities(appearances);
            verify(jurorExpenseService, times(1)).getBulkExpenseDtoEntities(appearances);
            verify(jurorExpenseService, times(1)).calculateBulkExpenseTotals(bulkExpenseEntryDtoList, appearances);
            verify(jurorExpenseService, times(1)).getJurorDefaultMileage(TestConstants.VALID_JUROR_NUMBER);
            verify(appearances.get(0), times(1)).getFinancialAuditDetails();
            appearances.forEach(Mockito::verifyNoMoreInteractions);
        }

        @Test
        void positiveDraftNoFinancialDetail() {
            AppearanceStage stage = EXPENSE_ENTERED;

            jurorExpenseService = spy(jurorExpenseService);
            Appearance appearance1 = mockAppearanceWithStage(stage);
            when(appearance1.getFinancialAuditDetails()).thenReturn(null);
            Appearance appearance2 = mockAppearanceWithStage(stage);
            when(appearance2.getFinancialAuditDetails()).thenReturn(null);
            Appearance appearance3 = mockAppearanceWithStage(stage);
            when(appearance3.getFinancialAuditDetails()).thenReturn(null);
            List<Appearance> appearances = List.of(appearance1, appearance2, appearance3);

            doReturn(stage).when(jurorExpenseService).getAppearanceStage(appearances);

            List<BulkExpenseEntryDto> bulkExpenseEntryDtoList = List.of(
                mockBulkExpenseEntryDto(LocalDate.of(2023, 10, 1)),
                mockBulkExpenseEntryDto(LocalDate.of(2023, 10, 1)),
                mockBulkExpenseEntryDto(LocalDate.of(2023, 10, 1))
            );

            doReturn(bulkExpenseEntryDtoList).when(jurorExpenseService)
                .getBulkExpenseDtoEntities(appearances);
            TotalExpenseDto totalExpenseDto = mock(TotalExpenseDto.class);

            doReturn(totalExpenseDto).when(jurorExpenseService)
                .calculateBulkExpenseTotals(bulkExpenseEntryDtoList, appearances);
            doReturn(4).when(jurorExpenseService)
                .getJurorDefaultMileage(TestConstants.VALID_JUROR_NUMBER);

            BulkExpenseDto bulkExpenseDto = jurorExpenseService.getBulkExpense(TestConstants.VALID_JUROR_NUMBER,
                appearances);
            assertNull(bulkExpenseDto.getJurorVersion(),
                "Juror version should be null unless stage is AUTHORISED");

            assertEquals(bulkExpenseEntryDtoList, bulkExpenseDto.getExpenses(),
                "Expenses should match the calculated values");
            assertEquals(totalExpenseDto, bulkExpenseDto.getTotals(),
                "Totals should match calculated total");
            assertEquals(4, bulkExpenseDto.getMileage(),
                "Mileage should match juror default mileage");
            assertEquals(TestConstants.VALID_JUROR_NUMBER, bulkExpenseDto.getJurorNumber(),
                "Juror Number should match provided juror");
            assertEquals(stage, bulkExpenseDto.getType(),
                "Stage should match calculated stage");

            assertNull(
                bulkExpenseDto.getApprovedBy(),
                "Should be null");
            assertNull(bulkExpenseDto.getApprovedOn(),
                "Should be null");
            assertNull(bulkExpenseDto.getSubmittedBy(),
                "Should be null");
            assertNull(bulkExpenseDto.getSubmittedOn(),
                "Should be null");
            assertNull(bulkExpenseDto.getJurorVersion(),
                "Should be null");


            verify(jurorExpenseService, times(1)).getAppearanceStage(appearances);
            verify(jurorExpenseService, times(1)).getBulkExpenseDtoEntities(appearances);
            verify(jurorExpenseService, times(1)).getBulkExpenseDtoEntities(appearances);
            verify(jurorExpenseService, times(1)).calculateBulkExpenseTotals(bulkExpenseEntryDtoList, appearances);

            verify(appearances.get(0), times(1)).getFinancialAuditDetails();
            appearances.forEach(Mockito::verifyNoMoreInteractions);
        }

        @Test
        void positiveFinancialDetailsButNulLSubmittedAndApprovedBy() {
            User approvedByUser = mock(User.class);
            when(approvedByUser.getUsername()).thenReturn("Approving User");
            User submittedByUser = mock(User.class);
            when(submittedByUser.getUsername()).thenReturn("Submitted User");

            AppearanceStage stage = EXPENSE_ENTERED;
            FinancialAuditDetails financialAuditDetails = new FinancialAuditDetails();
            jurorExpenseService = spy(jurorExpenseService);
            Appearance appearance1 = mockAppearanceWithStage(stage);
            when(appearance1.getFinancialAuditDetails()).thenReturn(
                mockFinancialAuditDetails(123L, financialAuditDetails));
            Appearance appearance2 = mockAppearanceWithStage(stage);
            when(appearance2.getFinancialAuditDetails()).thenReturn(
                mockFinancialAuditDetails(321L, financialAuditDetails));
            Appearance appearance3 = mockAppearanceWithStage(stage);
            when(appearance3.getFinancialAuditDetails()).thenReturn(
                mockFinancialAuditDetails(124L, financialAuditDetails));
            List<Appearance> appearances = List.of(appearance1, appearance2, appearance3);

            doReturn(stage).when(jurorExpenseService).getAppearanceStage(appearances);

            List<BulkExpenseEntryDto> bulkExpenseEntryDtoList = List.of(
                mockBulkExpenseEntryDto(LocalDate.of(2023, 10, 1)),
                mockBulkExpenseEntryDto(LocalDate.of(2023, 10, 1)),
                mockBulkExpenseEntryDto(LocalDate.of(2023, 10, 1))
            );

            doReturn(bulkExpenseEntryDtoList).when(jurorExpenseService)
                .getBulkExpenseDtoEntities(appearances);
            TotalExpenseDto totalExpenseDto = mock(TotalExpenseDto.class);

            doReturn(totalExpenseDto).when(jurorExpenseService)
                .calculateBulkExpenseTotals(bulkExpenseEntryDtoList, appearances);
            doReturn(10).when(jurorExpenseService).getJurorDefaultMileage(TestConstants.VALID_JUROR_NUMBER);
            BulkExpenseDto bulkExpenseDto = jurorExpenseService.getBulkExpense(TestConstants.VALID_JUROR_NUMBER,
                appearances);

            assertNull(bulkExpenseDto.getJurorVersion(),
                "Juror version should be null unless stage is AUTHORISED");

            assertEquals(bulkExpenseEntryDtoList, bulkExpenseDto.getExpenses(),
                "Expenses should match the calculated values");
            assertEquals(totalExpenseDto, bulkExpenseDto.getTotals(),
                "Totals should match calculated total");
            assertEquals(10, bulkExpenseDto.getMileage(),
                "Mileage should match juror default mileage");
            assertEquals(TestConstants.VALID_JUROR_NUMBER, bulkExpenseDto.getJurorNumber(),
                "Juror Number should match provided juror");
            assertEquals(stage, bulkExpenseDto.getType(),
                "Stage should match calculated stage");

            assertNull(
                bulkExpenseDto.getApprovedBy(),
                "Should be null");
            assertNull(bulkExpenseDto.getApprovedOn(),
                "Should be null");
            assertNull(bulkExpenseDto.getSubmittedBy(),
                "Should be null");
            assertNull(bulkExpenseDto.getSubmittedOn(),
                "Should be null");
            assertNull(bulkExpenseDto.getJurorVersion(),
                "Should be null");


            verify(jurorExpenseService, times(1)).getAppearanceStage(appearances);
            verify(jurorExpenseService, times(1)).getBulkExpenseDtoEntities(appearances);
            verify(jurorExpenseService, times(1)).getBulkExpenseDtoEntities(appearances);
            verify(jurorExpenseService, times(1)).getJurorDefaultMileage(TestConstants.VALID_JUROR_NUMBER);
            verify(jurorExpenseService, times(1)).calculateBulkExpenseTotals(bulkExpenseEntryDtoList, appearances);

            verify(appearances.get(0), times(1)).getFinancialAuditDetails();
            appearances.forEach(Mockito::verifyNoMoreInteractions);
        }
    }

    @Nested
    @DisplayName("private List<BulkExpenseEntryDto> getBulkExpenseDtoEntities(List<Appearance> appearances)")
    class GetBulkExpenseDtoEntities {
        private MockedStatic<BulkExpenseEntryDto> bulkExpenseEntryDtoMockedStatic;


        @BeforeEach
        void beforeEach() {
            bulkExpenseEntryDtoMockedStatic =
                mockStatic(BulkExpenseEntryDto.class);
        }

        @AfterEach
        void afterEach() {
            if (bulkExpenseEntryDtoMockedStatic != null) {
                bulkExpenseEntryDtoMockedStatic.close();
            }
        }

        @Test
        @SuppressWarnings("unchecked")
        void positiveTypical() {
            jurorExpenseService = spy(jurorExpenseService);

            Appearance appearanceAuthorised1 =
                mockAppearanceWithStage(EXPENSE_AUTHORISED);
            BulkExpenseEntryDto bulkExpenseEntityDtoAuthorised1 =
                mockBulkExpenseEntryDto(LocalDate.of(2023, 10, 1));
            Appearance appearanceAuthorised2 =
                mockAppearanceWithStage(EXPENSE_AUTHORISED);
            BulkExpenseEntryDto bulkExpenseEntityDtoAuthorised2 =
                mockBulkExpenseEntryDto(LocalDate.of(2023, 10, 3));
            Appearance appearanceAuthorised3 =
                mockAppearanceWithStage(EXPENSE_AUTHORISED);
            BulkExpenseEntryDto bulkExpenseEntityDtoAuthorised3 =
                mockBulkExpenseEntryDto(LocalDate.of(2023, 10, 4));

            Appearance appearanceEdited =
                mockAppearanceWithStage(AppearanceStage.EXPENSE_EDITED);
            BulkExpenseEntryDto bulkExpenseEntityDtoEdited =
                mockBulkExpenseEntryDto(LocalDate.of(2023, 10, 4));
            Appearance appearanceEntered =
                mockAppearanceWithStage(EXPENSE_ENTERED);
            BulkExpenseEntryDto bulkExpenseEntityDtoEntered =
                mockBulkExpenseEntryDto(LocalDate.of(2023, 10, 2));

            List<Appearance> appearanceList = List.of(
                appearanceAuthorised1,
                appearanceEdited,
                appearanceAuthorised2,
                appearanceEntered
            );

            bulkExpenseEntryDtoMockedStatic.when(() -> BulkExpenseEntryDto.fromAppearance(appearanceAuthorised1))
                .thenReturn(bulkExpenseEntityDtoAuthorised1);
            bulkExpenseEntryDtoMockedStatic.when(() -> BulkExpenseEntryDto.fromAppearance(appearanceAuthorised2))
                .thenReturn(bulkExpenseEntityDtoAuthorised2);
            bulkExpenseEntryDtoMockedStatic.when(() -> BulkExpenseEntryDto.fromAppearance(appearanceAuthorised3))
                .thenReturn(bulkExpenseEntityDtoAuthorised3);
            bulkExpenseEntryDtoMockedStatic.when(() -> BulkExpenseEntryDto.fromAppearance(appearanceEntered))
                .thenReturn(bulkExpenseEntityDtoEntered);
            bulkExpenseEntryDtoMockedStatic.when(() -> BulkExpenseEntryDto.fromAppearance(appearanceEdited))
                .thenReturn(bulkExpenseEntityDtoEdited);

            Revision<Long, Appearance> editedRevision = mock(Revision.class);
            when(editedRevision.getEntity()).thenReturn(appearanceAuthorised3);
            doReturn(editedRevision).when(jurorExpenseService)
                .getLastAuditForAppearanceWhereStage(appearanceEdited, EXPENSE_AUTHORISED);

            List<BulkExpenseEntryDto> bulkExpenseEntryDtoList =
                jurorExpenseService.getBulkExpenseDtoEntities(appearanceList);

            assertEquals(List.of(
                    bulkExpenseEntityDtoAuthorised1,
                    bulkExpenseEntityDtoEntered,
                    bulkExpenseEntityDtoAuthorised2,
                    bulkExpenseEntityDtoEdited
                ),
                bulkExpenseEntryDtoList,
                "Returned list should match expected and should be sorted by date");

            verify(bulkExpenseEntityDtoEdited, times(1)).setOriginalValue(bulkExpenseEntityDtoAuthorised3);
            verify(bulkExpenseEntityDtoAuthorised1, never()).setOriginalValue(any());
            verify(bulkExpenseEntityDtoAuthorised2, never()).setOriginalValue(any());
            verify(bulkExpenseEntityDtoEntered, never()).setOriginalValue(any());
        }
    }

    @Nested
    @DisplayName("private TotalExpenseDto calculateBulkExpenseTotals(AppearanceStage stage, List<BulkExpenseEntryDto> "
        + "expenseDtos, List<Appearance> appearances)")
    class CalculateBulkExpenseTotals {

        BulkExpenseEntryDto mockBulkExpenseEntryDto(BigDecimal multiplier) {
            BulkExpenseEntryDto bulkExpenseEntryDto = spy(new BulkExpenseEntryDto());
            when(bulkExpenseEntryDto.getPublicTransport()).thenReturn(new BigDecimal("1.00").multiply(multiplier));
            when(bulkExpenseEntryDto.getTaxi()).thenReturn(new BigDecimal("2.00").multiply(multiplier));
            when(bulkExpenseEntryDto.getMotorcycle()).thenReturn(new BigDecimal("3.00").multiply(multiplier));
            when(bulkExpenseEntryDto.getCar()).thenReturn(new BigDecimal("4.00").multiply(multiplier));
            when(bulkExpenseEntryDto.getBicycle()).thenReturn(new BigDecimal("5.00").multiply(multiplier));
            when(bulkExpenseEntryDto.getParking()).thenReturn(new BigDecimal("6.00").multiply(multiplier));
            when(bulkExpenseEntryDto.getFoodAndDrink()).thenReturn(new BigDecimal("7.00").multiply(multiplier));
            when(bulkExpenseEntryDto.getLossOfEarnings()).thenReturn(new BigDecimal("8.00").multiply(multiplier));
            when(bulkExpenseEntryDto.getExtraCare()).thenReturn(new BigDecimal("9.00").multiply(multiplier));
            when(bulkExpenseEntryDto.getOther()).thenReturn(new BigDecimal("10.00").multiply(multiplier));
            when(bulkExpenseEntryDto.getSmartCard()).thenReturn(new BigDecimal("11.00").multiply(multiplier));
            return bulkExpenseEntryDto;
        }

        Appearance mockAppearance(BigDecimal totalPaid, BigDecimal totalDue) {
            Appearance appearance = mock(Appearance.class);
            when(appearance.getTotalPaid()).thenReturn(totalPaid);
            when(appearance.getTotalDue()).thenReturn(totalDue);
            return appearance;
        }

        @Test
        void positiveTotalExpenseDtoAppearances() {
            List<BulkExpenseEntryDto> bulkExpenseEntryDtos = List.of(mockBulkExpenseEntryDto(new BigDecimal("1.00")),
                mockBulkExpenseEntryDto(new BigDecimal("2.00")), mockBulkExpenseEntryDto(new BigDecimal("3.00")));
            List<Appearance> appearances = List.of(mockAppearance(new BigDecimal("0.00"), new BigDecimal("100.00")),
                mockAppearance(new BigDecimal("100.00"), new BigDecimal("0.00")),
                mockAppearance(new BigDecimal("10.00"), new BigDecimal("70.00")));


            TotalExpenseDto totalExpenseDto =
                jurorExpenseService.calculateBulkExpenseTotals(bulkExpenseEntryDtos, appearances);

            assertEquals(3, totalExpenseDto.getTotalDays(), "Total days should match number of appearances");

            assertEquals(new BigDecimal("280.00"), totalExpenseDto.getTotalAmount(),
                "Total amount should match total of all paid &  due values");

            assertEquals(new BigDecimal("110.00"), totalExpenseDto.getTotalAmountPaidToDate(),
                "Total amount paid should match total of total paid");

            assertEquals(new BigDecimal("170.00"), totalExpenseDto.getBalanceToPay(),
                "Balance To Pay should match total expense minus total paid to date");


            assertEquals(new BigDecimal("6.0000"), totalExpenseDto.getPublicTransport(),
                "Total should match total of all expenses");
            assertEquals(new BigDecimal("12.0000"), totalExpenseDto.getTaxi(),
                "Total should match total of all expenses");
            assertEquals(new BigDecimal("18.0000"), totalExpenseDto.getMotorcycle(),
                "Total should match total of all expenses");
            assertEquals(new BigDecimal("24.0000"), totalExpenseDto.getCar(),
                "Total should match total of all expenses");
            assertEquals(new BigDecimal("30.0000"), totalExpenseDto.getBicycle(),
                "Total should match total of all expenses");
            assertEquals(new BigDecimal("36.0000"), totalExpenseDto.getParking(),
                "Total should match total of all expenses");
            assertEquals(new BigDecimal("42.0000"), totalExpenseDto.getFoodAndDrink(),
                "Total should match total of all expenses");
            assertEquals(new BigDecimal("48.0000"), totalExpenseDto.getLossOfEarnings(),
                "Total should match total of all expenses");
            assertEquals(new BigDecimal("54.0000"), totalExpenseDto.getExtraCare(),
                "Total should match total of all expenses");
            assertEquals(new BigDecimal("60.0000"), totalExpenseDto.getOther(),
                "Total should match total of all expenses");
            assertEquals(new BigDecimal("66.0000"), totalExpenseDto.getSmartCard(),
                "Total should match total of all expenses");
            assertEquals(new BigDecimal("264.0000"), totalExpenseDto.getTotal(),
                "Total should match total of all expenses");

            appearances.forEach(appearance -> {
                verify(appearance, times(1)).getTotalDue();
                verify(appearance, times(1)).getTotalPaid();
                verifyNoMoreInteractions(appearance);
            });

            bulkExpenseEntryDtos.forEach(bulkExpenseEntryDto -> {
                verify(bulkExpenseEntryDto, times(1)).getPublicTransport();
                verify(bulkExpenseEntryDto, times(1)).getTaxi();
                verify(bulkExpenseEntryDto, times(1)).getMotorcycle();
                verify(bulkExpenseEntryDto, times(1)).getCar();
                verify(bulkExpenseEntryDto, times(1)).getBicycle();
                verify(bulkExpenseEntryDto, times(1)).getParking();
                verify(bulkExpenseEntryDto, times(1)).getFoodAndDrink();
                verify(bulkExpenseEntryDto, times(1)).getLossOfEarnings();
                verify(bulkExpenseEntryDto, times(1)).getExtraCare();
                verify(bulkExpenseEntryDto, times(1)).getOther();
                verify(bulkExpenseEntryDto, times(1)).getSmartCard();
                verifyNoMoreInteractions(bulkExpenseEntryDto);
            });
        }
    }

    @Nested
    @DisplayName("private Revision<Long, Appearance> getLastAuditForAppearanceWhereStage(Appearance appearance,\n"
        + "                                                                           AppearanceStage appearanceStage)")
    class GetLastAuditForAppearanceWhereStage {


        @SuppressWarnings("unchecked")
        private Revision<Long, Appearance> createAppearanceRevision(Long revisionNumber, Instant instant,
                                                                    AppearanceStage stage) {
            Appearance appearance = mockAppearanceWithStage(stage);

            Revision<Long, Appearance> revision = mock(Revision.class);
            when(revision.getEntity()).thenReturn(appearance);
            when(revision.getRequiredRevisionNumber()).thenReturn(revisionNumber);
            when(revision.getRequiredRevisionInstant()).thenReturn(instant);
            return revision;
        }

        @Test
        void positiveLastAuditFound() {
            LocalDate attendanceDate = LocalDate.now();
            CourtLocation courtLocation = mock(CourtLocation.class);
            Appearance appearance = mock(Appearance.class);
            when(appearance.getJurorNumber()).thenReturn(TestConstants.VALID_JUROR_NUMBER);
            when(appearance.getAttendanceDate()).thenReturn(attendanceDate);
            when(appearance.getCourtLocation()).thenReturn(courtLocation);

            Revisions<Long, Appearance> appearanceRevisions = Revisions.of(
                List.of(createAppearanceRevision(3L, Instant.ofEpochSecond(3), EXPENSE_AUTHORISED),
                    createAppearanceRevision(2L, Instant.ofEpochSecond(2), EXPENSE_AUTHORISED),
                    createAppearanceRevision(8L, Instant.ofEpochSecond(1), EXPENSE_AUTHORISED),
                    createAppearanceRevision(7L, Instant.ofEpochSecond(7), EXPENSE_AUTHORISED),
                    createAppearanceRevision(4L, Instant.ofEpochSecond(4), EXPENSE_AUTHORISED),
                    createAppearanceRevision(5L, Instant.ofEpochSecond(5), EXPENSE_AUTHORISED),
                    createAppearanceRevision(6L, Instant.ofEpochSecond(6), EXPENSE_AUTHORISED),
                    createAppearanceRevision(8L, Instant.ofEpochSecond(8), AppearanceStage.EXPENSE_EDITED),
                    createAppearanceRevision(9L, Instant.ofEpochSecond(9), AppearanceStage.EXPENSE_EDITED),
                    createAppearanceRevision(10L, Instant.ofEpochSecond(10), AppearanceStage.EXPENSE_EDITED)));


            when(appearanceRepository.findRevisions(
                new AppearanceId(TestConstants.VALID_JUROR_NUMBER, attendanceDate, courtLocation))).thenReturn(
                appearanceRevisions);

            Revision<Long, Appearance> foundRevision =
                jurorExpenseService.getLastAuditForAppearanceWhereStage(appearance, EXPENSE_AUTHORISED);


            assertEquals(7L, foundRevision.getRequiredRevisionNumber(),
                "Should return the newest revision of the requested stage");


            verify(appearanceRepository, times(1)).findRevisions(
                new AppearanceId(TestConstants.VALID_JUROR_NUMBER, attendanceDate, courtLocation));
            verifyNoMoreInteractions(appearanceRepository);
        }

        @Test
        void negativeLastAuditNotFound() {
            LocalDate attendanceDate = LocalDate.now();
            CourtLocation courtLocation = mock(CourtLocation.class);
            Appearance appearance = mock(Appearance.class);
            when(appearance.getJurorNumber()).thenReturn(TestConstants.VALID_JUROR_NUMBER);
            when(appearance.getAttendanceDate()).thenReturn(attendanceDate);
            when(appearance.getCourtLocation()).thenReturn(courtLocation);
            when(appearance.getIdString()).thenReturn("SOME_ID_STRING");

            Revisions<Long, Appearance> appearanceRevisions = Revisions.of(
                List.of(createAppearanceRevision(3L, Instant.ofEpochSecond(3), EXPENSE_AUTHORISED),
                    createAppearanceRevision(2L, Instant.ofEpochSecond(2), EXPENSE_AUTHORISED),
                    createAppearanceRevision(8L, Instant.ofEpochSecond(1), EXPENSE_AUTHORISED),
                    createAppearanceRevision(7L, Instant.ofEpochSecond(7), EXPENSE_AUTHORISED),
                    createAppearanceRevision(4L, Instant.ofEpochSecond(4), EXPENSE_AUTHORISED),
                    createAppearanceRevision(5L, Instant.ofEpochSecond(5), EXPENSE_AUTHORISED),
                    createAppearanceRevision(6L, Instant.ofEpochSecond(6), EXPENSE_AUTHORISED),
                    createAppearanceRevision(8L, Instant.ofEpochSecond(8), AppearanceStage.EXPENSE_EDITED),
                    createAppearanceRevision(9L, Instant.ofEpochSecond(9), AppearanceStage.EXPENSE_EDITED),
                    createAppearanceRevision(10L, Instant.ofEpochSecond(10), AppearanceStage.EXPENSE_EDITED)));


            when(appearanceRepository.findRevisions(
                new AppearanceId(TestConstants.VALID_JUROR_NUMBER, attendanceDate, courtLocation))).thenReturn(
                appearanceRevisions);

            MojException.NotFound exception = assertThrows(MojException.NotFound.class,
                () -> jurorExpenseService.getLastAuditForAppearanceWhereStage(appearance, AppearanceStage.CHECKED_OUT),
                "An exception should be thrown if no revisions can be found that meet the criteria");

            assertEquals("No appearance history found with stage: CHECKED_OUT for appearance: SOME_ID_STRING",
                exception.getMessage(), "Message should match");
            assertNull(exception.getCause(), "There should be no cause for this exception");

            verify(appearanceRepository, times(1)).findRevisions(
                new AppearanceId(TestConstants.VALID_JUROR_NUMBER, attendanceDate, courtLocation));
            verifyNoMoreInteractions(appearanceRepository);
        }

    }


    @Nested
    @DisplayName("private AppearanceStage getAppearanceStage(List<Appearance> appearances)")
    class GetAppearanceStage {

        @ParameterizedTest
        @EnumSource(value = AppearanceStage.class, mode = EnumSource.Mode.INCLUDE, names = {"EXPENSE_AUTHORISED",
            "EXPENSE_EDITED", "EXPENSE_ENTERED"})
        void positiveAllStagesMatch(AppearanceStage stage) {
            List<Appearance> appearances =
                List.of(mockAppearanceWithStage(stage), mockAppearanceWithStage(stage), mockAppearanceWithStage(stage));
            assertEquals(stage, jurorExpenseService.getAppearanceStage(appearances),
                "When all appearances are the same stage said stage should be returned");
        }

        @Test
        void positive2TypesOfStageOneEdited() {
            List<Appearance> appearances = List.of(mockAppearanceWithStage(EXPENSE_AUTHORISED),
                mockAppearanceWithStage(AppearanceStage.EXPENSE_EDITED),
                mockAppearanceWithStage(EXPENSE_AUTHORISED));
            assertEquals(AppearanceStage.EXPENSE_EDITED, jurorExpenseService.getAppearanceStage(appearances),
                "When all appearances are either EXPENSE_EDITED or one other stage then EXPENSE_EDITED should be "
                    + "returned");
        }

        @Test
        void negativeTwoTypeOfStagesNoneEdit() {
            List<Appearance> appearances = List.of(mockAppearanceWithStage(EXPENSE_ENTERED),
                mockAppearanceWithStage(EXPENSE_AUTHORISED),
                mockAppearanceWithStage(EXPENSE_AUTHORISED));

            MojException.InternalServerError exception = assertThrows(MojException.InternalServerError.class,
                () -> jurorExpenseService.getAppearanceStage(appearances),
                "Expect an exception when multiple stage exist and they are not EXPENSE_EDITED");

            assertEquals("Appearances must be of the same type  (Unless EXPENSE_EDITED)", exception.getMessage(),
                "Message should match");
            assertNull(exception.getCause(), "Should have no cause");
        }

        @Test
        void negativeThreeTypeOfStagesOneEdit() {
            List<Appearance> appearances = List.of(mockAppearanceWithStage(EXPENSE_ENTERED),
                mockAppearanceWithStage(EXPENSE_AUTHORISED),
                mockAppearanceWithStage(AppearanceStage.EXPENSE_EDITED));

            MojException.InternalServerError exception = assertThrows(MojException.InternalServerError.class,
                () -> jurorExpenseService.getAppearanceStage(appearances),
                "Expect an exception when multiple stage exist and they are not EXPENSE_EDITED");

            assertEquals("Appearances must be of the same type  (Unless EXPENSE_EDITED)", exception.getMessage(),
                "Message should match");
            assertNull(exception.getCause(), "Should have no cause");
        }
    }


    private Appearance mockAppearanceWithStage(AppearanceStage stage) {
        Appearance appearance = mock(Appearance.class);
        when(appearance.getAppearanceStage()).thenReturn(stage);
        return appearance;
    }

    private Appearance mockAppearanceDraft(boolean isDraft) {
        Appearance appearance = mock(Appearance.class);
        when(appearance.getIsDraftExpense()).thenReturn(isDraft);
        return appearance;
    }

    private BulkExpenseEntryDto mockBulkExpenseEntryDto(LocalDate localDate) {
        BulkExpenseEntryDto bulkExpenseEntryDto = mock(BulkExpenseEntryDto.class);
        when(bulkExpenseEntryDto.getAppearanceDate()).thenReturn(localDate);
        return bulkExpenseEntryDto;
    }

    private FinancialAuditDetails mockFinancialAuditDetails(long id, FinancialAuditDetails base) {
        FinancialAuditDetails financialAuditDetails = new FinancialAuditDetails();
        financialAuditDetails.setSubmittedBy(base.getSubmittedBy());
        financialAuditDetails.setSubmittedOn(base.getSubmittedOn());
        financialAuditDetails.setApprovedBy(base.getApprovedBy());
        financialAuditDetails.setApprovedOn(base.getApprovedOn());
        financialAuditDetails.setJurorRevisionWhenApproved(base.getJurorRevisionWhenApproved());
        financialAuditDetails.setId(id);
        return financialAuditDetails;
    }

    @Nested
    @DisplayName("DefaultExpenseSummaryDto setDefaultExpensesForJuror")
    class SetDefaultExpenses {
        @BeforeEach
        void mockCurrentUser() {
            securityUtilMockedStatic = mockStatic(SecurityUtil.class);
            securityUtilMockedStatic.when(SecurityUtil::getActiveLogin)
                .thenReturn("CURRENT_USER");
        }

        @AfterEach
        void afterEach() {
            if (securityUtilMockedStatic != null) {
                securityUtilMockedStatic.close();
            }
        }

        @Test
        @DisplayName("Successfully retrieve default values and set")
        void setDefaultExpensesHappyPath() {
            String jurorNumber = "111111111";

            Juror juror = new Juror();
            juror.setJurorNumber(jurorNumber);

            DefaultExpenseSummaryDto dto = new DefaultExpenseSummaryDto();
            dto.setJurorNumber(jurorNumber);
            dto.setSmartCardNumber("12345678");
            dto.setTotalSmartCardSpend(40.0);
            dto.setFinancialLoss(0.0);
            dto.setDistanceTraveledMiles(6);
            dto.setTravelTime(LocalTime.of(4, 30));

            when(jurorRepository.findById(jurorNumber)).thenReturn(Optional.of(juror));

            jurorExpenseService.setDefaultExpensesForJuror(dto);

            assertThat(juror.getAmountSpent()).isEqualTo(dto.getTotalSmartCardSpend());
            assertThat(juror.getFinancialLoss()).isEqualTo(dto.getFinancialLoss());
            assertThat(juror.getTravelTime()).isEqualTo(4.5);
            assertThat(juror.getMileage()).isEqualTo(dto.getDistanceTraveledMiles());
            assertThat(juror.getSmartCard()).isEqualTo(dto.getSmartCardNumber());

            verify(jurorRepository, times(1)).save(juror);
            verify(jurorRepository, times(1)).findById(jurorNumber);
        }

        @Test
        @DisplayName("404 Juror Number Not Found")
        void jurorNotFound() {
            String jurorNumber = "111111111";

            Juror juror = new Juror();

            DefaultExpenseSummaryDto dto = new DefaultExpenseSummaryDto();

            when(jurorRepository.findById(jurorNumber)).thenReturn(Optional.of(juror));

            assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
                jurorExpenseService.setDefaultExpensesForJuror(dto));

            verify(jurorRepository, never()).save(juror);
            verify(jurorRepository, never()).findById(jurorNumber);
        }

    }

    @Nested
    @DisplayName("submitDraftExpensesForApproval(ExpenseItemsDto dto)")
    class SubmitForApproval {

        @BeforeEach
        void mockCurrentUser() {
            String username = "CURRENT_USER";
            securityUtilMockedStatic = mockStatic(SecurityUtil.class);
            securityUtilMockedStatic.when(SecurityUtil::getActiveLogin)
                .thenReturn(username);

            doReturn(null).when(financialAuditDetailsRepository).save(Mockito.any());
            doReturn(null).when(appearanceRepository).save(Mockito.any());
            doReturn(User.builder().owner("415").active(true).username(username).build())
                .when(userRepository).findByUsername(username);
        }

        @AfterEach
        void afterEach() {
            if (securityUtilMockedStatic != null) {
                securityUtilMockedStatic.close();
            }
        }

        @Test
        @DisplayName("Successfully submit one expense for approval")
        void singleExpenseHappyPath() {
            String jurorNumber = "641512345";
            String poolNumber = "415123456";

            Appearance appearanceToSubmit = buildTestAppearance(jurorNumber, poolNumber,
                LocalDate.of(2024, 1, 1));
            Appearance appearanceInDraft = buildTestAppearance(jurorNumber, poolNumber,
                LocalDate.of(2024, 1, 2));

            doReturn(List.of(appearanceToSubmit, appearanceInDraft)).when(appearanceRepository)
                .findAllByJurorNumberAndPoolNumber(jurorNumber, poolNumber);

            ExpenseItemsDto expenseItemsDto = new ExpenseItemsDto(jurorNumber, poolNumber,
                List.of(LocalDate.of(2024, 1, 1)));

            ArgumentCaptor<Appearance> appearanceArgumentCaptor = ArgumentCaptor.forClass(Appearance.class);
            jurorExpenseService.submitDraftExpensesForApproval(expenseItemsDto);

            verify(appearanceRepository, times(1))
                .findAllByJurorNumberAndPoolNumber(jurorNumber, poolNumber);
            verify(appearanceRepository, times(1))
                .save(appearanceArgumentCaptor.capture());
            verify(financialAuditDetailsRepository, times(1)).save(Mockito.any());

            Appearance updatedAppearance = appearanceArgumentCaptor.getValue();
            assertThat(updatedAppearance.getIsDraftExpense())
                .as("Expect the is_draft_expense flag to be updated to false")
                .isFalse();
            assertThat(updatedAppearance.getFinancialAuditDetails())
                .as("Expect the financial audit details object to be created")
                .isNotNull();
        }

        @Test
        @DisplayName("Successfully submit two expenses for approval")
        void multipleExpenseHappyPath() {
            String jurorNumber = "641512345";
            String poolNumber = "415123456";

            Appearance appearanceToSubmit1 = buildTestAppearance(jurorNumber, poolNumber,
                LocalDate.of(2024, 1, 1));
            Appearance appearanceToSubmit2 = buildTestAppearance(jurorNumber, poolNumber,
                LocalDate.of(2024, 1, 2));
            Appearance appearanceInDraft = buildTestAppearance(jurorNumber, poolNumber,
                LocalDate.of(2024, 1, 3));

            doReturn(List.of(appearanceToSubmit1, appearanceToSubmit2, appearanceInDraft))
                .when(appearanceRepository).findAllByJurorNumberAndPoolNumber(jurorNumber, poolNumber);

            ExpenseItemsDto expenseItemsDto = new ExpenseItemsDto(jurorNumber, poolNumber,
                List.of(LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 1, 2)));

            ArgumentCaptor<Appearance> appearanceArgumentCaptor = ArgumentCaptor.forClass(Appearance.class);
            jurorExpenseService.submitDraftExpensesForApproval(expenseItemsDto);

            verify(appearanceRepository, times(1))
                .findAllByJurorNumberAndPoolNumber(jurorNumber, poolNumber);
            verify(appearanceRepository, times(2))
                .save(appearanceArgumentCaptor.capture());
            verify(financialAuditDetailsRepository, times(1)).save(Mockito.any());

            List<Appearance> updatedAppearances = appearanceArgumentCaptor.getAllValues();
            for (Appearance updatedAppearance : updatedAppearances) {
                assertThat(updatedAppearance.getIsDraftExpense())
                    .as("Expect the is_draft_expense flag to be updated to false")
                    .isFalse();
                assertThat(updatedAppearance.getFinancialAuditDetails())
                    .as("Expect the financial audit details object to be created")
                    .isNotNull();
            }
        }

        @Test
        @DisplayName("Appearance records not found")
        void appearanceRecordsNotFound() {
            String jurorNumber = "641512345";
            String poolNumber = "415123456";

            doReturn(new ArrayList<Appearance>()).when(appearanceRepository)
                .findAllByJurorNumberAndPoolNumber(jurorNumber, poolNumber);

            ExpenseItemsDto expenseItemsDto = new ExpenseItemsDto(jurorNumber, poolNumber,
                List.of(LocalDate.of(2024, 1, 1)));

            assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
                jurorExpenseService.submitDraftExpensesForApproval(expenseItemsDto));

            verify(appearanceRepository, times(1))
                .findAllByJurorNumberAndPoolNumber(jurorNumber, poolNumber);
            verify(appearanceRepository, never())
                .save(Mockito.any());
            verify(financialAuditDetailsRepository, never()).save(Mockito.any());
        }

        private Appearance buildTestAppearance(String jurorNumber, String poolNumber, LocalDate attendanceDate) {
            return Appearance.builder()
                .jurorNumber(jurorNumber)
                .poolNumber(poolNumber)
                .attendanceDate(attendanceDate)
                .attendanceType(AttendanceType.FULL_DAY)
                .appearanceStage(EXPENSE_ENTERED)
                .isDraftExpense(true)
                .build();
        }

    }
}
