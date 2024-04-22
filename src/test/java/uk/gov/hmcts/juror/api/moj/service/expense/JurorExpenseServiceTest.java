package uk.gov.hmcts.juror.api.moj.service.expense;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.bureau.service.UserService;
import uk.gov.hmcts.juror.api.juror.domain.ApplicationSettings;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.RequestDefaultExpensesDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ApportionSmartCardRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ApproveExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.CalculateTotalExpenseRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.CombinedExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseType;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpense;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseApplyToAllDays;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseFinancialLoss;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseFoodAndDrink;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseTime;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseTravel;
import uk.gov.hmcts.juror.api.moj.controller.response.DefaultExpenseResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.CombinedSimplifiedExpenseDetailDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.DailyExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.ExpenseCount;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.ExpenseDetailsForTotals;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.FinancialLossWarning;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.GetEnteredExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.PendingApproval;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.PendingApprovalList;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.SimplifiedExpenseDetailDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.UnpaidExpenseSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.ExpenseRates;
import uk.gov.hmcts.juror.api.moj.domain.ExpenseRatesDto;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorExpenseTotals;
import uk.gov.hmcts.juror.api.moj.domain.PaymentData;
import uk.gov.hmcts.juror.api.moj.domain.SortDirection;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.FoodDrinkClaimType;
import uk.gov.hmcts.juror.api.moj.enumeration.PayAttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.PaymentMethod;
import uk.gov.hmcts.juror.api.moj.enumeration.TravelMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.ExpenseRatesRepository;
import uk.gov.hmcts.juror.api.moj.repository.FinancialAuditDetailsRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorExpenseTotalsRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.repository.PaymentDataRepository;
import uk.gov.hmcts.juror.api.moj.service.ApplicationSettingService;
import uk.gov.hmcts.juror.api.moj.service.FinancialAuditService;
import uk.gov.hmcts.juror.api.moj.service.JurorHistoryService;
import uk.gov.hmcts.juror.api.moj.service.ValidationService;
import uk.gov.hmcts.juror.api.moj.utils.JurorUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.CAN_NOT_APPROVE_MORE_THAN_LIMIT;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.CAN_NOT_APPROVE_OWN_EDIT;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.DATA_OUT_OF_DATE;
import static uk.gov.hmcts.juror.api.moj.exception.MojException.BusinessRuleViolation.ErrorCode.EXPENSES_CANNOT_BE_LESS_THAN_ZERO;

@ExtendWith(SpringExtension.class)
@SuppressWarnings({"PMD.ExcessiveImports", "PMD.LawOfDemeter", "PMD.CouplingBetweenObjects", "PMD.NcssCount",
    "PMD.TooManyMethods", "unchecked"})
class JurorExpenseServiceTest {

    @Mock
    private JurorExpenseTotalsRepository jurorExpenseTotalsRepository;
    @Mock
    private JurorRepository jurorRepository;
    @Mock
    private FinancialAuditDetailsRepository financialAuditDetailsRepository;
    @Mock
    private AppearanceRepository appearanceRepository;
    @Mock
    private FinancialAuditService financialAuditService;
    @Mock
    private JurorHistoryService jurorHistoryService;
    @Mock
    private UserService userService;
    @Mock
    private ValidationService validationService;
    @Mock
    private PaymentDataRepository paymentDataRepository;

    @Mock
    private ApplicationSettingService applicationSettingService;
    @Mock
    private ExpenseRatesRepository expenseRatesRepository;

    @Mock
    private EntityManager entityManager;

    private MockedStatic<SecurityUtil> securityUtilMockedStatic;
    private MockedStatic<JurorUtils> jurorUtilsMockedStatic;

    @InjectMocks
    private JurorExpenseServiceImpl jurorExpenseService;

    @BeforeEach
    void beforeEach() {
        jurorExpenseService = spy(jurorExpenseService);
    }


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
            assertEquals(5, jurorExpenseService.getJurorDefaultMileage(TestConstants.VALID_JUROR_NUMBER),
                "Should return the mileage from the juror");
            verify(juror, times(1)).getMileage();
            verify(jurorRepository, times(1)).findByJurorNumber(TestConstants.VALID_JUROR_NUMBER);
            verifyNoMoreInteractions(juror, jurorRepository);
        }

        @Test
        void notFound() {
            MojException.NotFound exception =
                assertThrows(MojException.NotFound.class,
                    () -> jurorExpenseService.getJurorDefaultMileage(TestConstants.VALID_JUROR_NUMBER),
                    "Should throw an exception when juror cannot be found.");
            assertEquals("Juror not found: " + TestConstants.VALID_JUROR_NUMBER, exception.getMessage(),
                "Message should match");
            assertNull(exception.getCause(), "There should be no cause");
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
            List<Appearance> appearances = List.of(mockAppearanceWithStage(AppearanceStage.EXPENSE_AUTHORISED),
                mockAppearanceWithStage(AppearanceStage.EXPENSE_EDITED),
                mockAppearanceWithStage(AppearanceStage.EXPENSE_AUTHORISED));
            assertEquals(AppearanceStage.EXPENSE_EDITED, jurorExpenseService.getAppearanceStage(appearances),
                "When all appearances are either EXPENSE_EDITED or one other stage then EXPENSE_EDITED should be "
                    + "returned");
        }

        @Test
        void negativeTwoTypeOfStagesNoneEdit() {
            List<Appearance> appearances = List.of(mockAppearanceWithStage(AppearanceStage.EXPENSE_ENTERED),
                mockAppearanceWithStage(AppearanceStage.EXPENSE_AUTHORISED),
                mockAppearanceWithStage(AppearanceStage.EXPENSE_AUTHORISED));

            MojException.InternalServerError exception = assertThrows(MojException.InternalServerError.class,
                () -> jurorExpenseService.getAppearanceStage(appearances),
                "Expect an exception when multiple stage exist and they are not EXPENSE_EDITED");

            assertEquals("Appearances must be of the same type  (Unless EXPENSE_EDITED)", exception.getMessage(),
                "Message should match");
            assertNull(exception.getCause(), "Should have no cause");
        }

        @Test
        void negativeThreeTypeOfStagesOneEdit() {
            List<Appearance> appearances = List.of(mockAppearanceWithStage(AppearanceStage.EXPENSE_ENTERED),
                mockAppearanceWithStage(AppearanceStage.EXPENSE_AUTHORISED),
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

    @Nested
    @DisplayName("DefaultExpenseResponseDto getDefaultExpensesForJuror")
    class GetDefaultExpenses {

        @Test
        @DisplayName("Successfully retrieve default values and set")
        void positiveGetDefaultExpensesHappyPath() {

            Juror juror = new Juror();
            juror.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
            juror.setFinancialLoss(BigDecimal.valueOf(0.0));
            juror.setTravelTime(LocalTime.of(4, 30));
            juror.setMileage(5);
            juror.setSmartCardNumber("12345678");
            juror.setClaimingSubsistenceAllowance(true);

            when(jurorRepository.findById(TestConstants.VALID_JUROR_NUMBER)).thenReturn(Optional.of(juror));

            DefaultExpenseResponseDto responseDto =
                jurorExpenseService.getDefaultExpensesForJuror(TestConstants.VALID_JUROR_NUMBER);

            assertThat(responseDto).isNotNull();
            assertThat(responseDto.getFinancialLoss()).isEqualTo(juror.getFinancialLoss());
            assertThat(responseDto.getDistanceTraveledMiles()).isEqualTo(juror.getMileage());
            assertThat(responseDto.getSmartCardNumber()).isEqualTo(juror.getSmartCardNumber());
            assertThat(responseDto.getTravelTime()).isEqualTo(LocalTime.of(4, 30));
            assertThat(responseDto.isClaimingSubsistenceAllowance()).isTrue();

            verify(jurorRepository, times(1)).findById(TestConstants.VALID_JUROR_NUMBER);

        }

        @Test
        @DisplayName("404 Juror Number Not Found")
        void jurorNotFound() {
            assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
                jurorExpenseService.getDefaultExpensesForJuror("123456789"));
        }
    }

    @Nested
    @DisplayName("RequestDefaultExpensesDto setDefaultExpenses")
    class SetDefaultExpenses {
        @BeforeEach
        void mockCurrentUser() {
            jurorUtilsMockedStatic = mockStatic(JurorUtils.class);
            securityUtilMockedStatic = mockStatic(SecurityUtil.class);
            securityUtilMockedStatic.when(SecurityUtil::getActiveOwner)
                .thenReturn(TestConstants.VALID_COURT_LOCATION);
        }

        @AfterEach
        void afterEach() {
            if (jurorUtilsMockedStatic != null) {
                jurorUtilsMockedStatic.close();
            }
            if (securityUtilMockedStatic != null) {
                securityUtilMockedStatic.close();
            }
        }

        @Test
        @DisplayName("Successfully set default values without overriding appearance")
        void setDefaultExpensesHappyPathNotOverrideDraftExpenses() {
            RequestDefaultExpensesDto dto = new RequestDefaultExpensesDto();
            dto.setTravelTime(LocalTime.of(4, 30));
            dto.setFinancialLoss(new BigDecimal("123.321"));
            dto.setSmartCardNumber("12345678");
            dto.setDistanceTraveledMiles(5);
            dto.setOverwriteExistingDraftExpenses(false);
            dto.setHasFoodAndDrink(true);

            Juror juror = mock(Juror.class);

            jurorUtilsMockedStatic.when(() -> JurorUtils.getActiveJurorRecord(jurorRepository,
                    TestConstants.VALID_JUROR_NUMBER))
                .thenReturn(juror);

            jurorExpenseService.setDefaultExpensesForJuror(TestConstants.VALID_JUROR_NUMBER, dto);

            verify(juror, times(1)).setMileage(5);
            verify(juror, times(1)).setFinancialLoss(new BigDecimal("123.321"));
            verify(juror, times(1)).setSmartCardNumber("12345678");
            verify(juror, times(1)).setTravelTime(LocalTime.of(4, 30));
            verify(juror, times(1)).setClaimingSubsistenceAllowance(true);
            verifyNoMoreInteractions(juror);

            verify(jurorRepository, times(1)).save(juror);
            verify(jurorExpenseService, never()).applyDefaultExpenses(any());
            securityUtilMockedStatic.verify(SecurityUtil::getActiveOwner, times(1));
            jurorUtilsMockedStatic.verify(
                () -> JurorUtils.getActiveJurorRecord(jurorRepository, TestConstants.VALID_JUROR_NUMBER),
                times(1));
        }

        @Test
        @DisplayName("Successfully set default values with overriding appearance")
        @SuppressWarnings("LineLength")
        void setDefaultExpensesHappyPathOverrideDraftExpenses() {
            RequestDefaultExpensesDto dto = new RequestDefaultExpensesDto();
            dto.setTravelTime(LocalTime.of(4, 30));
            dto.setFinancialLoss(new BigDecimal("123.321"));
            dto.setSmartCardNumber("12345678");
            dto.setDistanceTraveledMiles(5);
            dto.setOverwriteExistingDraftExpenses(true);
            dto.setHasFoodAndDrink(true);

            Juror juror = mock(Juror.class);

            jurorUtilsMockedStatic.when(() -> JurorUtils.getActiveJurorRecord(jurorRepository,
                    TestConstants.VALID_JUROR_NUMBER))
                .thenReturn(juror);
            doNothing().when(jurorExpenseService).applyDefaultExpenses(any());

            List<Appearance> appearances = List.of(
                mock(Appearance.class),
                mock(Appearance.class),
                mock(Appearance.class)
            );

            doReturn(appearances).when(appearanceRepository)
                .findAllByJurorNumberAndAppearanceStageInAndCourtLocationOwnerAndIsDraftExpenseTrueOrderByAttendanceDateDesc(
                    any(), any(), any());

            jurorExpenseService.setDefaultExpensesForJuror(TestConstants.VALID_JUROR_NUMBER, dto);

            verify(juror, times(1)).setMileage(5);
            verify(juror, times(1)).setFinancialLoss(new BigDecimal("123.321"));
            verify(juror, times(1)).setSmartCardNumber("12345678");
            verify(juror, times(1)).setTravelTime(LocalTime.of(4, 30));
            verify(juror, times(1)).setClaimingSubsistenceAllowance(true);
            verifyNoMoreInteractions(juror);

            verify(jurorRepository, times(1)).save(juror);
            verify(jurorExpenseService, times(1)).applyDefaultExpenses(any());
            verify(appearanceRepository, times(1))
                .findAllByJurorNumberAndAppearanceStageInAndCourtLocationOwnerAndIsDraftExpenseTrueOrderByAttendanceDateDesc(
                    TestConstants.VALID_JUROR_NUMBER,
                    Set.of(AppearanceStage.EXPENSE_ENTERED),
                    TestConstants.VALID_COURT_LOCATION);
            securityUtilMockedStatic.verify(SecurityUtil::getActiveOwner, times(1));
            jurorUtilsMockedStatic.verify(
                () -> JurorUtils.getActiveJurorRecord(jurorRepository, TestConstants.VALID_JUROR_NUMBER),
                times(1));
        }
    }


    @Nested
    @DisplayName("submitDraftExpensesForApproval(ExpenseItemsDto dto)")
    class SubmitForApproval {

        @Test
        @DisplayName("Successfully submit one expense for approval")
        void singleExpenseHappyPath() {

            final Appearance appearanceToSubmit = buildTestAppearance(TestConstants.VALID_JUROR_NUMBER,
                LocalDate.of(2024, 1, 1));
            doNothing().when(jurorExpenseService).saveAppearancesWithExpenseRateIdUpdate(anyCollection());

            Juror juror = new Juror();
            juror.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
            juror.setBankAccountNumber("12345678");
            juror.setSortCode("123456");
            doReturn(Optional.of(juror)).when(jurorRepository).findById(TestConstants.VALID_JUROR_NUMBER);

            CourtLocation courtLocation = mock(CourtLocation.class);
            appearanceToSubmit.setCourtLocation(courtLocation);
            doReturn(TestConstants.VALID_COURT_LOCATION).when(courtLocation).getLocCode();
            FinancialAuditDetails financialAuditDetails = mock(FinancialAuditDetails.class);
            doReturn(financialAuditDetails).when(financialAuditService).createFinancialAuditDetail(
                any(), any(), any(), any()
            );


            List<LocalDate> attendanceDates = List.of(LocalDate.of(2024, 1, 1));


            doReturn(List.of(appearanceToSubmit)).when(appearanceRepository)
                .findAllByCourtLocationLocCodeAndJurorNumberAndAttendanceDateIn(
                    TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER,
                    attendanceDates);

            jurorExpenseService.submitDraftExpensesForApproval(
                TestConstants.VALID_COURT_LOCATION,
                TestConstants.VALID_JUROR_NUMBER,
                attendanceDates);

            verify(appearanceRepository, times(1))
                .findAllByCourtLocationLocCodeAndJurorNumberAndAttendanceDateIn(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER, attendanceDates);
            ArgumentCaptor<List<Appearance>> appearanceArgumentCaptor = ArgumentCaptor.forClass(List.class);

            verify(jurorExpenseService, times(1)).saveAppearancesWithExpenseRateIdUpdate(
                appearanceArgumentCaptor.capture());

            verify(financialAuditService, times(1))
                .createFinancialAuditDetail(TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_COURT_LOCATION,
                    FinancialAuditDetails.Type.FOR_APPROVAL,
                    List.of(appearanceToSubmit)
                );

            List<Appearance> updatedAppearances = appearanceArgumentCaptor.getValue();
            assertThat(updatedAppearances).hasSize(1);
            Appearance updatedAppearance = updatedAppearances.get(0);
            assertThat(updatedAppearance.isDraftExpense())
                .as("Expect the is_draft_expense flag to be updated to false")
                .isFalse();
            verify(jurorHistoryService, times(1))
                .createExpenseForApprovalHistory(financialAuditDetails, updatedAppearance);
        }

        @Test
        @DisplayName("Successfully submit two expenses for approval")
        void multipleExpenseHappyPath() {
            final Appearance appearanceToSubmit1 = buildTestAppearance(TestConstants.VALID_JUROR_NUMBER,
                LocalDate.of(2024, 1, 1));
            final Appearance appearanceToSubmit2 = buildTestAppearance(TestConstants.VALID_JUROR_NUMBER,
                LocalDate.of(2024, 1, 2));

            Juror juror = new Juror();
            juror.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
            juror.setBankAccountNumber("12345678");
            juror.setSortCode("123456");
            doReturn(Optional.of(juror)).when(jurorRepository).findById(TestConstants.VALID_JUROR_NUMBER);

            doNothing().when(jurorExpenseService).saveAppearancesWithExpenseRateIdUpdate(anyCollection());

            CourtLocation courtLocation = mock(CourtLocation.class);
            appearanceToSubmit1.setCourtLocation(courtLocation);
            doReturn(TestConstants.VALID_COURT_LOCATION).when(courtLocation).getLocCode();
            FinancialAuditDetails financialAuditDetails = mock(FinancialAuditDetails.class);
            doReturn(financialAuditDetails).when(financialAuditService).createFinancialAuditDetail(
                any(), any(), any(), any()
            );

            List<LocalDate> attendanceDates = List.of(LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 1, 2));
            doReturn(List.of(appearanceToSubmit1, appearanceToSubmit2))
                .when(appearanceRepository)
                .findAllByCourtLocationLocCodeAndJurorNumberAndAttendanceDateIn(
                    TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER,
                    attendanceDates);

            ArgumentCaptor<List<Appearance>> appearanceArgumentCaptor = ArgumentCaptor.forClass(List.class);
            jurorExpenseService.submitDraftExpensesForApproval(
                TestConstants.VALID_COURT_LOCATION,
                TestConstants.VALID_JUROR_NUMBER,
                attendanceDates);

            verify(appearanceRepository, times(1))
                .findAllByCourtLocationLocCodeAndJurorNumberAndAttendanceDateIn(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER, attendanceDates);
            verify(jurorExpenseService, times(1))
                .saveAppearancesWithExpenseRateIdUpdate(appearanceArgumentCaptor.capture());
            verify(financialAuditService, times(1))
                .createFinancialAuditDetail(TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_COURT_LOCATION,
                    FinancialAuditDetails.Type.FOR_APPROVAL,
                    List.of(appearanceToSubmit1, appearanceToSubmit2)
                );

            List<Appearance> updatedAppearances = appearanceArgumentCaptor.getValue();
            for (Appearance updatedAppearance : updatedAppearances) {
                assertThat(updatedAppearance.isDraftExpense())
                    .as("Expect the is_draft_expense flag to be updated to false")
                    .isFalse();
                verify(jurorHistoryService, times(1))
                    .createExpenseForApprovalHistory(financialAuditDetails, updatedAppearance);
            }
        }

        @Test
        @DisplayName("Appearance records not found")
        void appearanceRecordsNotFound() {
            List<LocalDate> attendanceDates = List.of(LocalDate.of(2024, 1, 1));

            doReturn(new ArrayList<Appearance>()).when(appearanceRepository)
                .findAllByCourtLocationLocCodeAndJurorNumberAndAttendanceDateIn(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER, attendanceDates);

            assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
                jurorExpenseService.submitDraftExpensesForApproval(
                    TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER,
                    attendanceDates));

            verify(appearanceRepository, times(1))
                .findAllByCourtLocationLocCodeAndJurorNumberAndAttendanceDateIn(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER, attendanceDates);
            verify(appearanceRepository, never())
                .save(Mockito.any());
            verify(financialAuditDetailsRepository, never()).save(Mockito.any());
        }

        private Appearance buildTestAppearance(String jurorNumber, LocalDate attendanceDate) {
            return Appearance.builder()
                .jurorNumber(jurorNumber)
                .attendanceDate(attendanceDate)
                .attendanceType(AttendanceType.FULL_DAY)
                .appearanceStage(AppearanceStage.EXPENSE_ENTERED)
                .isDraftExpense(true)
                .build();
        }

    }

    @Nested
    @DisplayName("Appearance getDraftAppearance(String jurorNumber, String poolNumber, LocalDate attendanceDate)")
    class GetDraftAppearance {
        @Test
        void positiveFound() {
            final String jurorNumber = TestConstants.VALID_JUROR_NUMBER;
            final LocalDate attendanceDate = LocalDate.now();
            final Appearance appearance = mock(Appearance.class);
            final Optional<Appearance> appearanceOptional = Optional.of(appearance);

            when(appearanceRepository.findByCourtLocationLocCodeAndJurorNumberAndAttendanceDateAndIsDraftExpense(
                TestConstants.VALID_COURT_LOCATION, jurorNumber, attendanceDate, true))
                .thenReturn(appearanceOptional);

            assertThat(
                jurorExpenseService.getDraftAppearance(TestConstants.VALID_COURT_LOCATION, jurorNumber, attendanceDate))
                .isEqualTo(appearance);

            verify(appearanceRepository, times(1))
                .findByCourtLocationLocCodeAndJurorNumberAndAttendanceDateAndIsDraftExpense(
                    TestConstants.VALID_COURT_LOCATION, jurorNumber, attendanceDate, true);
        }

        @Test
        void negativeNotFound() {
            final String jurorNumber = TestConstants.VALID_JUROR_NUMBER;
            final LocalDate attendanceDate = LocalDate.now();
            final Optional<Appearance> appearanceOptional = Optional.empty();

            when(appearanceRepository.findByCourtLocationLocCodeAndJurorNumberAndAttendanceDateAndIsDraftExpense(
                TestConstants.VALID_COURT_LOCATION, jurorNumber, attendanceDate, true))
                .thenReturn(appearanceOptional);

            MojException.NotFound exception = assertThrows(MojException.NotFound.class,
                () -> jurorExpenseService.getDraftAppearance(TestConstants.VALID_COURT_LOCATION, jurorNumber,
                    attendanceDate),
                "Exception should be thrown when appearance not found");
            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage()).isEqualTo(
                "No draft appearance record found for juror: "
                    + jurorNumber + " on day: "
                    + attendanceDate
            );

            verify(appearanceRepository, times(1))
                .findByCourtLocationLocCodeAndJurorNumberAndAttendanceDateAndIsDraftExpense(
                    TestConstants.VALID_COURT_LOCATION, jurorNumber, attendanceDate, true);
        }
    }

    @Nested
    @DisplayName("boolean isAttendanceDay(Appearance appearance)")
    class IsAttendanceDay {
        @ParameterizedTest(name = "Is Attendance Day: {0}")
        @EnumSource(value = AttendanceType.class, mode = EnumSource.Mode.EXCLUDE,
            names = {"NON_ATTENDANCE", "NON_ATTENDANCE_LONG_TRIAL"})
        void positiveIsAttendanceDay(AttendanceType attendanceType) {
            Appearance appearance = mock(Appearance.class);
            when(appearance.getAttendanceType()).thenReturn(attendanceType);
            assertThat(jurorExpenseService.isAttendanceDay(appearance)).isTrue();
            verify(appearance, times(2))
                .getAttendanceType();
        }

        @Test
        void positiveIsNotAttendanceDay() {
            Appearance appearance = mock(Appearance.class);
            when(appearance.getAttendanceType()).thenReturn(AttendanceType.NON_ATTENDANCE);
            assertThat(jurorExpenseService.isAttendanceDay(appearance)).isFalse();
            verify(appearance, times(1))
                .getAttendanceType();
        }

        @Test
        void positiveIsNotAttendanceDayLongTrial() {
            Appearance appearance = mock(Appearance.class);
            when(appearance.getAttendanceType()).thenReturn(AttendanceType.NON_ATTENDANCE_LONG_TRIAL);
            assertThat(jurorExpenseService.isAttendanceDay(appearance)).isFalse();
            verify(appearance, times(2))
                .getAttendanceType();
        }
    }

    @Nested
    @DisplayName("DailyExpenseResponse updateDraftExpenseInternal(Appearance appearance,"
        + " DailyExpense request)")
    class UpdateExpenseInternal {

        @Test
        void positiveTypicalAttendedDay() {
            DailyExpenseFinancialLoss financialLoss = mock(DailyExpenseFinancialLoss.class);
            DailyExpenseTime time = mock(DailyExpenseTime.class);
            DailyExpenseTravel travel = mock(DailyExpenseTravel.class);
            DailyExpenseFoodAndDrink food = mock(DailyExpenseFoodAndDrink.class);

            DailyExpense dailyExpense = spy(DailyExpense.builder()
                .paymentMethod(PaymentMethod.CASH)
                .financialLoss(financialLoss)
                .time(time)
                .travel(travel)
                .foodAndDrink(food)
                .build());

            LocalTime localTime = LocalTime.now();
            doReturn(localTime).when(time).getTravelTime();

            Appearance appearance = mock(Appearance.class);

            doNothing().when(jurorExpenseService).updateDraftTimeExpense(appearance, time);

            doReturn(null).when(jurorExpenseService)
                .updateDraftFinancialLossExpense(appearance, financialLoss);

            doReturn(true).when(jurorExpenseService).isAttendanceDay(appearance);
            doNothing().when(jurorExpenseService).updateDraftTravelExpense(appearance, travel);
            doNothing().when(jurorExpenseService).updateDraftFoodAndDrinkExpense(appearance, food);
            doNothing().when(jurorExpenseService).validateExpense(appearance);

            DailyExpenseResponse response = jurorExpenseService
                .updateExpenseInternal(appearance, dailyExpense);

            assertThat(response).isNotNull();
            assertThat(response.getFinancialLossWarning()).isNull();

            verify(appearance, times(1)).setPayCash(true);
            verify(jurorExpenseService, times(1))
                .updateDraftTimeExpense(appearance, time);
            verify(jurorExpenseService, times(1))
                .updateDraftFinancialLossExpense(appearance, financialLoss);
            verify(appearance, times(1)).setTravelTime(localTime);
            verify(jurorExpenseService, times(1))
                .updateDraftTravelExpense(appearance, travel);
            verify(jurorExpenseService, times(1))
                .updateDraftFoodAndDrinkExpense(appearance, food);
            verify(jurorExpenseService, times(1))
                .validateExpense(appearance);
            verify(jurorExpenseService, times(1))
                .isAttendanceDay(appearance);
            verify(jurorExpenseService, times(1))
                .updateExpenseInternal(appearance, dailyExpense);
            verify(appearanceRepository, never()).save(appearance);


            verify(dailyExpense, times(1)).getPaymentMethod();
            verify(dailyExpense, times(1)).getFinancialLoss();
            verify(dailyExpense, times(1)).getTime();
            verify(dailyExpense, times(1)).getTravel();
            verify(dailyExpense, times(1)).getFoodAndDrink();


            verify(time, times(1)).getTravelTime();
            verify(time, times(1)).getPayAttendance();

            verifyNoMoreInteractions(jurorExpenseService, appearance, financialLoss, time, travel, food, dailyExpense);
        }

        @Test
        void positiveTypicalNonAttendedDay() {
            DailyExpenseFinancialLoss financialLoss = mock(DailyExpenseFinancialLoss.class);
            DailyExpenseTime time = mock(DailyExpenseTime.class);
            DailyExpenseTravel travel = mock(DailyExpenseTravel.class);
            DailyExpenseFoodAndDrink food = mock(DailyExpenseFoodAndDrink.class);

            DailyExpense dailyExpense = spy(DailyExpense.builder()
                .paymentMethod(PaymentMethod.CASH)
                .financialLoss(financialLoss)
                .time(time)
                .travel(travel)
                .foodAndDrink(food)
                .build());

            LocalTime localTime = LocalTime.now();
            doReturn(localTime).when(time).getTravelTime();

            Appearance appearance = mock(Appearance.class);

            doNothing().when(jurorExpenseService).updateDraftTimeExpense(appearance, time);

            doReturn(null).when(jurorExpenseService)
                .updateDraftFinancialLossExpense(appearance, financialLoss);

            doReturn(false).when(jurorExpenseService).isAttendanceDay(appearance);
            doNothing().when(jurorExpenseService).validateExpense(appearance);

            DailyExpenseResponse response = jurorExpenseService
                .updateExpenseInternal(appearance, dailyExpense);

            assertThat(response).isNotNull();
            assertThat(response.getFinancialLossWarning()).isNull();

            verify(appearance, times(1)).setPayCash(true);
            verify(jurorExpenseService, times(1))
                .updateDraftTimeExpense(appearance, time);
            verify(jurorExpenseService, times(1))
                .updateDraftFinancialLossExpense(appearance, financialLoss);
            verify(jurorExpenseService, times(1))
                .validateExpense(appearance);
            verify(jurorExpenseService, times(1))
                .isAttendanceDay(appearance);
            verify(jurorExpenseService, times(1))
                .updateExpenseInternal(appearance, dailyExpense);
            verify(appearanceRepository, never()).save(appearance);


            verify(dailyExpense, times(1)).getPaymentMethod();
            verify(dailyExpense, times(1)).getFinancialLoss();
            verify(dailyExpense, times(1)).getTime();
            verify(time, times(1)).getPayAttendance();
            verifyNoMoreInteractions(jurorExpenseService, appearance, financialLoss, time, travel, food, dailyExpense);
        }

        @Test
        void positiveNullTime() {
            DailyExpenseFinancialLoss financialLoss = mock(DailyExpenseFinancialLoss.class);
            DailyExpenseTravel travel = mock(DailyExpenseTravel.class);
            DailyExpenseFoodAndDrink food = mock(DailyExpenseFoodAndDrink.class);

            DailyExpense dailyExpense = spy(DailyExpense.builder()
                .paymentMethod(PaymentMethod.CASH)
                .financialLoss(financialLoss)
                .time(null)
                .travel(travel)
                .foodAndDrink(food)
                .build());

            Appearance appearance = mock(Appearance.class);

            doNothing().when(jurorExpenseService).updateDraftTimeExpense(appearance, null);

            doReturn(null).when(jurorExpenseService)
                .updateDraftFinancialLossExpense(appearance, financialLoss);

            doReturn(false).when(jurorExpenseService).isAttendanceDay(appearance);
            doNothing().when(jurorExpenseService).validateExpense(appearance);

            DailyExpenseResponse response = jurorExpenseService
                .updateExpenseInternal(appearance, dailyExpense);

            assertThat(response).isNotNull();
            assertThat(response.getFinancialLossWarning()).isNull();

            verify(appearance, times(1)).setPayCash(true);
            verify(jurorExpenseService, times(1))
                .updateDraftTimeExpense(appearance, null);
            verify(jurorExpenseService, times(1))
                .updateDraftFinancialLossExpense(appearance, financialLoss);
            verify(jurorExpenseService, times(1))
                .validateExpense(appearance);
            verify(jurorExpenseService, times(1))
                .isAttendanceDay(appearance);
            verify(jurorExpenseService, times(1))
                .updateExpenseInternal(appearance, dailyExpense);
            verify(appearanceRepository, never()).save(appearance);


            verify(dailyExpense, times(1)).getPaymentMethod();
            verify(dailyExpense, times(1)).getFinancialLoss();
            verify(dailyExpense, times(1)).getTime();
            verifyNoMoreInteractions(jurorExpenseService, appearance, financialLoss, travel, food, dailyExpense);
        }

        @Test
        void positiveNullPayAttendance() {
            DailyExpenseFinancialLoss financialLoss = mock(DailyExpenseFinancialLoss.class);
            DailyExpenseTime time = mock(DailyExpenseTime.class);
            DailyExpenseTravel travel = mock(DailyExpenseTravel.class);
            DailyExpenseFoodAndDrink food = mock(DailyExpenseFoodAndDrink.class);

            DailyExpense dailyExpense = spy(DailyExpense.builder()
                .paymentMethod(PaymentMethod.CASH)
                .financialLoss(financialLoss)
                .time(time)
                .travel(travel)
                .foodAndDrink(food)
                .build());

            LocalTime localTime = LocalTime.now();
            doReturn(localTime).when(time).getTravelTime();
            doReturn(null).when(time).getPayAttendance();
            Appearance appearance = mock(Appearance.class);

            doNothing().when(jurorExpenseService).updateDraftTimeExpense(appearance, time);

            doReturn(null).when(jurorExpenseService)
                .updateDraftFinancialLossExpense(appearance, financialLoss);

            doReturn(false).when(jurorExpenseService).isAttendanceDay(appearance);
            doNothing().when(jurorExpenseService).validateExpense(appearance);

            DailyExpenseResponse response = jurorExpenseService
                .updateExpenseInternal(appearance, dailyExpense);

            assertThat(response).isNotNull();
            assertThat(response.getFinancialLossWarning()).isNull();

            verify(appearance, times(1)).setPayCash(true);
            verify(jurorExpenseService, times(1))
                .updateDraftTimeExpense(appearance, time);
            verify(jurorExpenseService, times(1))
                .updateDraftFinancialLossExpense(appearance, financialLoss);
            verify(jurorExpenseService, times(1))
                .validateExpense(appearance);
            verify(jurorExpenseService, times(1))
                .isAttendanceDay(appearance);
            verify(jurorExpenseService, times(1))
                .updateExpenseInternal(appearance, dailyExpense);
            verify(appearanceRepository, never()).save(appearance);


            verify(dailyExpense, times(1)).getPaymentMethod();
            verify(dailyExpense, times(1)).getFinancialLoss();
            verify(dailyExpense, times(1)).getTime();
            verify(time, times(1)).getPayAttendance();
            verifyNoMoreInteractions(jurorExpenseService, appearance, financialLoss, time, travel, food, dailyExpense);
        }

        @Test
        void positiveWithFinancialLoss() {
            DailyExpenseFinancialLoss financialLoss = mock(DailyExpenseFinancialLoss.class);
            DailyExpenseTime time = mock(DailyExpenseTime.class);
            DailyExpenseTravel travel = mock(DailyExpenseTravel.class);
            DailyExpenseFoodAndDrink food = mock(DailyExpenseFoodAndDrink.class);

            DailyExpense dailyExpense = spy(DailyExpense.builder()
                .paymentMethod(PaymentMethod.CASH)
                .financialLoss(financialLoss)
                .time(time)
                .travel(travel)
                .foodAndDrink(food)
                .build());

            LocalTime localTime = LocalTime.now();
            doReturn(localTime).when(time).getTravelTime();
            doReturn(PayAttendanceType.FULL_DAY).when(time).getPayAttendance();
            Appearance appearance = mock(Appearance.class);

            doNothing().when(jurorExpenseService).updateDraftTimeExpense(appearance, time);

            FinancialLossWarning financialLossWarning = mock(FinancialLossWarning.class);
            doReturn(financialLossWarning).when(jurorExpenseService)
                .updateDraftFinancialLossExpense(appearance, financialLoss);

            doReturn(false).when(jurorExpenseService).isAttendanceDay(appearance);
            doNothing().when(jurorExpenseService).validateExpense(appearance);

            DailyExpenseResponse response = jurorExpenseService
                .updateExpenseInternal(appearance, dailyExpense);

            assertThat(response).isNotNull();
            assertThat(response.getFinancialLossWarning()).isEqualTo(financialLossWarning);

            verify(appearance, times(1)).setPayCash(true);
            verify(jurorExpenseService, times(1))
                .updateDraftTimeExpense(appearance, time);
            verify(jurorExpenseService, times(1))
                .updateDraftFinancialLossExpense(appearance, financialLoss);
            verify(jurorExpenseService, times(1))
                .validateExpense(appearance);
            verify(jurorExpenseService, times(1))
                .isAttendanceDay(appearance);
            verify(jurorExpenseService, times(1))
                .updateExpenseInternal(appearance, dailyExpense);
            verify(appearanceRepository, never()).save(appearance);


            verify(dailyExpense, times(1)).getPaymentMethod();
            verify(dailyExpense, times(2)).getFinancialLoss();
            verify(dailyExpense, times(1)).getTime();
            verify(time, times(1)).getPayAttendance();
            verifyNoMoreInteractions(jurorExpenseService, appearance, financialLoss, time, travel, food, dailyExpense);
        }

        @Test
        void positiveValidateFinancialLossLimitDueToPayAttendanceChange() {
            DailyExpenseTime time = mock(DailyExpenseTime.class);
            DailyExpenseTravel travel = mock(DailyExpenseTravel.class);
            DailyExpenseFoodAndDrink food = mock(DailyExpenseFoodAndDrink.class);

            DailyExpense dailyExpense = spy(DailyExpense.builder()
                .paymentMethod(PaymentMethod.CASH)
                .financialLoss(null)
                .time(time)
                .travel(travel)
                .foodAndDrink(food)
                .build());

            LocalTime localTime = LocalTime.now();
            doReturn(localTime).when(time).getTravelTime();
            doReturn(PayAttendanceType.FULL_DAY).when(time).getPayAttendance();
            Appearance appearance = mock(Appearance.class);
            doNothing().when(jurorExpenseService).updateDraftTimeExpense(appearance, time);

            doReturn(null).when(jurorExpenseService)
                .updateDraftFinancialLossExpense(appearance, null);

            doReturn(false).when(jurorExpenseService).isAttendanceDay(appearance);
            doNothing().when(jurorExpenseService).validateExpense(appearance);
            doReturn(null).when(jurorExpenseService).validateAndUpdateFinancialLossExpenseLimit(appearance);

            DailyExpenseResponse response = jurorExpenseService
                .updateExpenseInternal(appearance, dailyExpense);

            assertThat(response).isNotNull();
            assertThat(response.getFinancialLossWarning()).isNull();

            verify(appearance, times(1)).setPayCash(true);
            verify(jurorExpenseService, times(1))
                .updateDraftTimeExpense(appearance, time);
            verify(jurorExpenseService, times(1))
                .updateDraftFinancialLossExpense(appearance, null);
            verify(jurorExpenseService, times(1))
                .validateExpense(appearance);
            verify(jurorExpenseService, times(1))
                .isAttendanceDay(appearance);
            verify(jurorExpenseService, times(1))
                .updateExpenseInternal(appearance, dailyExpense);
            verify(jurorExpenseService, times(1))
                .validateAndUpdateFinancialLossExpenseLimit(appearance);
            verify(appearanceRepository, never()).save(appearance);


            verify(dailyExpense, times(1)).getPaymentMethod();
            verify(dailyExpense, times(2)).getFinancialLoss();
            verify(dailyExpense, times(1)).getTime();
            verify(time, times(1)).getPayAttendance();
            verifyNoMoreInteractions(jurorExpenseService, appearance, time, travel, food, dailyExpense);
        }
    }

    @Nested
    @DisplayName("DailyExpenseResponse updateDraftExpense(String jurorNumber,"
        + " DailyExpense request)")
    class UpdateDraftExpense {
        @Test
        void positiveNullApplyToAll() {
            final String jurorNumber = TestConstants.VALID_JUROR_NUMBER;
            final LocalDate dateOfExpense = LocalDate.now();
            DailyExpense dailyExpense = mock(DailyExpense.class);
            doReturn(dateOfExpense).when(dailyExpense).getDateOfExpense();
            doReturn(null).when(dailyExpense).getApplyToAllDays();
            Appearance appearance = mock(Appearance.class);

            doReturn(appearance).when(jurorExpenseService).getDraftAppearance(
                TestConstants.VALID_COURT_LOCATION, jurorNumber, dateOfExpense);
            doReturn(AttendanceType.NON_ATTENDANCE).when(appearance).getAttendanceType();
            DailyExpenseResponse responseMock = mock(DailyExpenseResponse.class);


            doReturn(responseMock).when(jurorExpenseService).updateExpenseInternal(appearance, dailyExpense);


            DailyExpenseResponse response = jurorExpenseService.updateDraftExpense(
                TestConstants.VALID_COURT_LOCATION,
                jurorNumber, dailyExpense);

            assertThat(response).isNotNull();
            assertThat(response).isEqualTo(responseMock);

            verify(dailyExpense, times(1)).getDateOfExpense();
            verify(dailyExpense, times(1)).getApplyToAllDays();

            verify(appearance, times(1)).getAttendanceType();

            verify(jurorExpenseService, times(1)).getDraftAppearance(
                TestConstants.VALID_COURT_LOCATION, jurorNumber, dateOfExpense);
            verify(jurorExpenseService, times(1)).updateExpenseInternal(appearance, dailyExpense);
            verify(jurorExpenseService, times(1)).updateDraftExpense(
                TestConstants.VALID_COURT_LOCATION, jurorNumber, dailyExpense);

            verify(validationService, times(1))
                .validate(dailyExpense, DailyExpense.NonAttendanceDay.class);
            verify(jurorExpenseService, times(1)).isAttendanceDay(appearance);
            verifyNoMoreInteractions(appearance, dailyExpense, jurorExpenseService, validationService);
        }

        @Test
        @DisplayName("Apply to all is not triggered if an empty value is used. Current day should get updated with "
            + "all values but not all other days")
        void positiveEmptyApplyToAll() {
            final String jurorNumber = TestConstants.VALID_JUROR_NUMBER;
            final LocalDate dateOfExpense = LocalDate.now();
            DailyExpense dailyExpense = mock(DailyExpense.class);
            doReturn(dateOfExpense).when(dailyExpense).getDateOfExpense();
            doReturn(List.of()).when(dailyExpense).getApplyToAllDays();
            Appearance appearance = mock(Appearance.class);
            doReturn(AttendanceType.FULL_DAY).when(appearance).getAttendanceType();
            doReturn(appearance).when(jurorExpenseService).getDraftAppearance(TestConstants.VALID_COURT_LOCATION,
                jurorNumber, dateOfExpense);
            DailyExpenseResponse responseMock = mock(DailyExpenseResponse.class);

            doReturn(responseMock).when(jurorExpenseService).updateExpenseInternal(appearance, dailyExpense);

            DailyExpenseResponse response = jurorExpenseService.updateDraftExpense(
                TestConstants.VALID_COURT_LOCATION, jurorNumber, dailyExpense);

            assertThat(response).isNotNull();
            assertThat(response.getFinancialLossWarning()).isEqualTo(responseMock.getFinancialLossWarning());

            verify(dailyExpense, times(1)).getDateOfExpense();
            verify(dailyExpense, times(2)).getApplyToAllDays();

            verify(appearance, times(2)).getAttendanceType();
            verify(jurorExpenseService, times(1)).getDraftAppearance(TestConstants.VALID_COURT_LOCATION,
                jurorNumber, dateOfExpense);
            verify(jurorExpenseService, times(1)).updateExpenseInternal(appearance, dailyExpense);
            verify(jurorExpenseService, times(1)).updateDraftExpense(TestConstants.VALID_COURT_LOCATION,
                jurorNumber, dailyExpense);
            verify(validationService, times(1))
                .validate(dailyExpense, DailyExpense.AttendanceDay.class);
            verify(jurorExpenseService, times(1)).isAttendanceDay(appearance);
            verifyNoMoreInteractions(appearance, dailyExpense, jurorExpenseService, validationService);
        }

        @Test
        void positiveHasApplyToAll() {
            final String jurorNumber = TestConstants.VALID_JUROR_NUMBER;
            final String poolNumber = TestConstants.VALID_POOL_NUMBER;
            final LocalDate dateOfExpense = LocalDate.now();
            DailyExpense dailyExpense = mock(DailyExpense.class);
            doReturn(dateOfExpense).when(dailyExpense).getDateOfExpense();
            doReturn(List.of(DailyExpenseApplyToAllDays.TRAVEL_COSTS, DailyExpenseApplyToAllDays.OTHER_COSTS)).when(
                dailyExpense).getApplyToAllDays();

            List<Appearance> appearances = List.of(mock(Appearance.class), mock(Appearance.class),
                mock(Appearance.class));

            doReturn(appearances).when(appearanceRepository)
                .findByCourtLocationLocCodeAndJurorNumberAndIsDraftExpenseTrue(TestConstants.VALID_COURT_LOCATION,
                    jurorNumber);
            doNothing().when(jurorExpenseService).applyToAll(appearances, dailyExpense);
            Appearance appearance = mock(Appearance.class);

            doReturn(appearance).when(jurorExpenseService).getDraftAppearance(jurorNumber, poolNumber, dateOfExpense);
            DailyExpenseResponse responseMock = mock(DailyExpenseResponse.class);

            doReturn(responseMock).when(jurorExpenseService).updateExpenseInternal(appearance, dailyExpense);

            DailyExpenseResponse response =
                jurorExpenseService.updateDraftExpense(TestConstants.VALID_COURT_LOCATION, jurorNumber, dailyExpense);

            assertThat(response).isNotNull();
            assertThat(response.getFinancialLossWarning()).isEqualTo(responseMock.getFinancialLossWarning());

            verify(dailyExpense, times(2)).getApplyToAllDays();


            verify(jurorExpenseService, times(1)).updateDraftExpense(TestConstants.VALID_COURT_LOCATION, jurorNumber,
                dailyExpense);
            verify(jurorExpenseService, times(1)).applyToAll(appearances, dailyExpense);
            verifyNoMoreInteractions(appearance, dailyExpense, jurorExpenseService, validationService);
        }
    }

    @Nested
    @DisplayName("void applyToAll(List<Appearance> appearances, DailyExpense request)")
    class ApplyToAll {

        @Test
        void positiveTravelCost() {
            DailyExpense dailyExpense = mock(DailyExpense.class);
            DailyExpenseTravel travel = mock(DailyExpenseTravel.class);
            doReturn(travel).when(dailyExpense).getTravel();
            doReturn(List.of(DailyExpenseApplyToAllDays.TRAVEL_COSTS)).when(dailyExpense).getApplyToAllDays();
            doReturn(null).when(dailyExpense).getFinancialLoss();
            Appearance attendedDay1 = mock(Appearance.class);
            Appearance attendedDay2 = mock(Appearance.class);
            Appearance nonAttendedDay1 = mock(Appearance.class);

            doReturn(true).when(jurorExpenseService).isAttendanceDay(attendedDay1);
            doReturn(true).when(jurorExpenseService).isAttendanceDay(attendedDay2);
            doReturn(false).when(jurorExpenseService).isAttendanceDay(nonAttendedDay1);

            doNothing().when(jurorExpenseService).updateDraftTravelExpense(any(), any());
            doNothing().when(jurorExpenseService).validateExpense(any());
            doNothing().when(jurorExpenseService).saveAppearancesWithExpenseRateIdUpdate(anyCollection());

            List<Appearance> appearances = List.of(attendedDay1, nonAttendedDay1, attendedDay2);
            jurorExpenseService.applyToAll(appearances, dailyExpense);


            verify(jurorExpenseService, times(1)).applyToAll(appearances, dailyExpense);
            verify(jurorExpenseService, times(1)).updateDraftTravelExpense(attendedDay1, travel);
            verify(jurorExpenseService, times(1)).updateDraftTravelExpense(attendedDay2, travel);
            verify(jurorExpenseService, times(1)).isAttendanceDay(attendedDay1);
            verify(jurorExpenseService, times(1)).isAttendanceDay(attendedDay2);
            verify(jurorExpenseService, times(1)).isAttendanceDay(nonAttendedDay1);


            verify(jurorExpenseService, times(1)).validateExpense(attendedDay1);
            verify(jurorExpenseService, times(1)).validateExpense(attendedDay2);
            verify(jurorExpenseService, times(1)).validateExpense(nonAttendedDay1);

            verify(jurorExpenseService, times(1)).saveAppearancesWithExpenseRateIdUpdate(appearances);

            verify(dailyExpense, times(2)).getTravel();
            verify(dailyExpense, times(1)).getFinancialLoss();
            verify(dailyExpense, times(1)).getApplyToAllDays();


            verifyNoMoreInteractions(dailyExpense, travel, attendedDay1, attendedDay2, nonAttendedDay1,
                jurorExpenseService);
        }


        @Test
        void positivePayCash() {
            DailyExpense dailyExpense = mock(DailyExpense.class);
            doReturn(PaymentMethod.CASH).when(dailyExpense).getPaymentMethod();
            doReturn(List.of(DailyExpenseApplyToAllDays.PAY_CASH)).when(dailyExpense).getApplyToAllDays();
            doReturn(null).when(dailyExpense).getFinancialLoss();
            Appearance attendedDay1 = mock(Appearance.class);
            Appearance attendedDay2 = mock(Appearance.class);
            Appearance nonAttendedDay1 = mock(Appearance.class);

            doNothing().when(jurorExpenseService).validateExpense(any());
            doNothing().when(jurorExpenseService).saveAppearancesWithExpenseRateIdUpdate(any());

            List<Appearance> appearances = List.of(attendedDay1, nonAttendedDay1, attendedDay2);
            jurorExpenseService.applyToAll(appearances, dailyExpense);


            verify(jurorExpenseService, times(1)).applyToAll(appearances, dailyExpense);

            verify(attendedDay1, times(1)).setPayCash(true);
            verify(attendedDay2, times(1)).setPayCash(true);
            verify(nonAttendedDay1, times(1)).setPayCash(true);


            verify(jurorExpenseService, times(1)).validateExpense(attendedDay1);
            verify(jurorExpenseService, times(1)).validateExpense(attendedDay2);
            verify(jurorExpenseService, times(1)).validateExpense(nonAttendedDay1);

            verify(jurorExpenseService, times(1)).saveAppearancesWithExpenseRateIdUpdate(appearances);

            verify(dailyExpense, times(1)).getFinancialLoss();
            verify(dailyExpense, times(3)).getPaymentMethod();
            verify(dailyExpense, times(1)).getApplyToAllDays();


            verifyNoMoreInteractions(dailyExpense, attendedDay1, attendedDay2, nonAttendedDay1,
                jurorExpenseService);
        }

        @Test
        void positiveFinancialLoss() {
            DailyExpense dailyExpense = mock(DailyExpense.class);
            DailyExpenseFinancialLoss financialLoss = mock(DailyExpenseFinancialLoss.class);
            BigDecimal lossOfEarnings = new BigDecimal("1.23");
            doReturn(lossOfEarnings).when(financialLoss).getLossOfEarningsOrBenefits();
            doReturn(List.of(DailyExpenseApplyToAllDays.LOSS_OF_EARNINGS)).when(dailyExpense).getApplyToAllDays();
            doReturn(financialLoss).when(dailyExpense).getFinancialLoss();
            Appearance attendedDay1 = mock(Appearance.class);
            Appearance attendedDay2 = mock(Appearance.class);
            Appearance nonAttendedDay1 = mock(Appearance.class);

            doNothing().when(jurorExpenseService).validateExpense(any());
            doNothing().when(jurorExpenseService).saveAppearancesWithExpenseRateIdUpdate(any());
            doReturn(null).when(jurorExpenseService).validateAndUpdateFinancialLossExpenseLimit(any());

            List<Appearance> appearances = List.of(attendedDay1, nonAttendedDay1, attendedDay2);
            jurorExpenseService.applyToAll(appearances, dailyExpense);


            verify(jurorExpenseService, times(1)).applyToAll(appearances, dailyExpense);


            verify(jurorExpenseService, times(1)).validateExpense(attendedDay1);
            verify(jurorExpenseService, times(1)).validateExpense(attendedDay2);
            verify(jurorExpenseService, times(1)).validateExpense(nonAttendedDay1);

            verify(jurorExpenseService, times(1)).validateAndUpdateFinancialLossExpenseLimit(attendedDay1);
            verify(jurorExpenseService, times(1)).validateAndUpdateFinancialLossExpenseLimit(attendedDay2);
            verify(jurorExpenseService, times(1)).validateAndUpdateFinancialLossExpenseLimit(nonAttendedDay1);

            verify(jurorExpenseService, times(1)).saveAppearancesWithExpenseRateIdUpdate(appearances);

            verify(dailyExpense, times(1)).getFinancialLoss();
            verify(dailyExpense, times(1)).getApplyToAllDays();
            verify(financialLoss, times(3)).getLossOfEarningsOrBenefits();

            verify(attendedDay1, times(1)).setLossOfEarningsDue(lossOfEarnings);
            verify(attendedDay2, times(1)).setLossOfEarningsDue(lossOfEarnings);
            verify(nonAttendedDay1, times(1)).setLossOfEarningsDue(lossOfEarnings);

            verifyNoMoreInteractions(dailyExpense, financialLoss, attendedDay1, attendedDay2, nonAttendedDay1,
                jurorExpenseService);
        }

        @Test
        void positiveExtraCareCost() {
            DailyExpense dailyExpense = mock(DailyExpense.class);
            DailyExpenseFinancialLoss financialLoss = mock(DailyExpenseFinancialLoss.class);
            BigDecimal extraCareCost = new BigDecimal("1.23");
            doReturn(extraCareCost).when(financialLoss).getExtraCareCost();
            doReturn(List.of(DailyExpenseApplyToAllDays.EXTRA_CARE_COSTS)).when(dailyExpense).getApplyToAllDays();
            doReturn(financialLoss).when(dailyExpense).getFinancialLoss();
            Appearance attendedDay1 = mock(Appearance.class);
            Appearance attendedDay2 = mock(Appearance.class);
            Appearance nonAttendedDay1 = mock(Appearance.class);

            doNothing().when(jurorExpenseService).validateExpense(any());
            doNothing().when(jurorExpenseService).saveAppearancesWithExpenseRateIdUpdate(any());
            doReturn(null).when(jurorExpenseService).validateAndUpdateFinancialLossExpenseLimit(any());

            List<Appearance> appearances = List.of(attendedDay1, nonAttendedDay1, attendedDay2);
            jurorExpenseService.applyToAll(appearances, dailyExpense);


            verify(jurorExpenseService, times(1)).applyToAll(appearances, dailyExpense);


            verify(jurorExpenseService, times(1)).validateExpense(attendedDay1);
            verify(jurorExpenseService, times(1)).validateExpense(attendedDay2);
            verify(jurorExpenseService, times(1)).validateExpense(nonAttendedDay1);

            verify(jurorExpenseService, times(1)).validateAndUpdateFinancialLossExpenseLimit(attendedDay1);
            verify(jurorExpenseService, times(1)).validateAndUpdateFinancialLossExpenseLimit(attendedDay2);
            verify(jurorExpenseService, times(1)).validateAndUpdateFinancialLossExpenseLimit(nonAttendedDay1);

            verify(jurorExpenseService, times(1)).saveAppearancesWithExpenseRateIdUpdate(appearances);

            verify(dailyExpense, times(1)).getFinancialLoss();
            verify(dailyExpense, times(1)).getApplyToAllDays();
            verify(financialLoss, times(3)).getExtraCareCost();

            verify(attendedDay1, times(1)).setChildcareDue(extraCareCost);
            verify(attendedDay2, times(1)).setChildcareDue(extraCareCost);
            verify(nonAttendedDay1, times(1)).setChildcareDue(extraCareCost);

            verifyNoMoreInteractions(dailyExpense, financialLoss, attendedDay1, attendedDay2, nonAttendedDay1,
                jurorExpenseService);
        }

        @Test
        void positiveOtherCost() {
            DailyExpense dailyExpense = mock(DailyExpense.class);
            DailyExpenseFinancialLoss financialLoss = mock(DailyExpenseFinancialLoss.class);
            BigDecimal otherCost = new BigDecimal("1.23");
            String otherDesc = "Other desc";
            doReturn(otherCost).when(financialLoss).getOtherCosts();
            doReturn(otherDesc).when(financialLoss).getOtherCostsDescription();
            doReturn(List.of(DailyExpenseApplyToAllDays.OTHER_COSTS)).when(dailyExpense).getApplyToAllDays();
            doReturn(financialLoss).when(dailyExpense).getFinancialLoss();
            Appearance attendedDay1 = mock(Appearance.class);
            Appearance attendedDay2 = mock(Appearance.class);
            Appearance nonAttendedDay1 = mock(Appearance.class);

            doNothing().when(jurorExpenseService).validateExpense(any());
            doNothing().when(jurorExpenseService).saveAppearancesWithExpenseRateIdUpdate(any());
            doReturn(null).when(jurorExpenseService).validateAndUpdateFinancialLossExpenseLimit(any());

            List<Appearance> appearances = List.of(attendedDay1, nonAttendedDay1, attendedDay2);
            jurorExpenseService.applyToAll(appearances, dailyExpense);


            verify(jurorExpenseService, times(1)).applyToAll(appearances, dailyExpense);


            verify(jurorExpenseService, times(1)).validateExpense(attendedDay1);
            verify(jurorExpenseService, times(1)).validateExpense(attendedDay2);
            verify(jurorExpenseService, times(1)).validateExpense(nonAttendedDay1);

            verify(jurorExpenseService, times(1)).validateAndUpdateFinancialLossExpenseLimit(attendedDay1);
            verify(jurorExpenseService, times(1)).validateAndUpdateFinancialLossExpenseLimit(attendedDay2);
            verify(jurorExpenseService, times(1)).validateAndUpdateFinancialLossExpenseLimit(nonAttendedDay1);

            verify(jurorExpenseService, times(1)).saveAppearancesWithExpenseRateIdUpdate(appearances);

            verify(dailyExpense, times(1)).getFinancialLoss();
            verify(dailyExpense, times(1)).getApplyToAllDays();
            verify(financialLoss, times(3)).getOtherCosts();
            verify(financialLoss, times(3)).getOtherCostsDescription();

            verify(attendedDay1, times(1)).setMiscAmountDue(otherCost);
            verify(attendedDay2, times(1)).setMiscAmountDue(otherCost);
            verify(nonAttendedDay1, times(1)).setMiscAmountDue(otherCost);
            verify(attendedDay1, times(1)).setMiscDescription(otherDesc);
            verify(attendedDay2, times(1)).setMiscDescription(otherDesc);
            verify(nonAttendedDay1, times(1)).setMiscDescription(otherDesc);

            verifyNoMoreInteractions(dailyExpense, financialLoss, attendedDay1, attendedDay2, nonAttendedDay1,
                jurorExpenseService);
        }

        @Test
        void positiveAllTypes() {
            DailyExpense dailyExpense = mock(DailyExpense.class);
            DailyExpenseTravel travel = mock(DailyExpenseTravel.class);
            doReturn(travel).when(dailyExpense).getTravel();
            doReturn(PaymentMethod.BACS).when(dailyExpense).getPaymentMethod();
            DailyExpenseFinancialLoss financialLoss = mock(DailyExpenseFinancialLoss.class);
            BigDecimal lossOfEarnings = new BigDecimal("1.23");
            doReturn(lossOfEarnings).when(financialLoss).getLossOfEarningsOrBenefits();
            BigDecimal otherCost = new BigDecimal("1.23");
            String otherDesc = "Other desc";
            doReturn(otherCost).when(financialLoss).getOtherCosts();
            doReturn(otherDesc).when(financialLoss).getOtherCostsDescription();

            BigDecimal extraCareCost = new BigDecimal("2.33");
            doReturn(extraCareCost).when(financialLoss).getExtraCareCost();

            doReturn(List.of(
                DailyExpenseApplyToAllDays.TRAVEL_COSTS,
                DailyExpenseApplyToAllDays.PAY_CASH,
                DailyExpenseApplyToAllDays.LOSS_OF_EARNINGS,
                DailyExpenseApplyToAllDays.EXTRA_CARE_COSTS,
                DailyExpenseApplyToAllDays.OTHER_COSTS
            )).when(dailyExpense).getApplyToAllDays();

            doReturn(financialLoss).when(dailyExpense).getFinancialLoss();
            Appearance attendedDay1 = mock(Appearance.class);
            Appearance attendedDay2 = mock(Appearance.class);
            Appearance nonAttendedDay1 = mock(Appearance.class);

            doReturn(true).when(jurorExpenseService).isAttendanceDay(attendedDay1);
            doReturn(true).when(jurorExpenseService).isAttendanceDay(attendedDay2);
            doReturn(false).when(jurorExpenseService).isAttendanceDay(nonAttendedDay1);

            doNothing().when(jurorExpenseService).updateDraftTravelExpense(any(), any());
            doNothing().when(jurorExpenseService).validateExpense(any());
            doNothing().when(jurorExpenseService).saveAppearancesWithExpenseRateIdUpdate(any());
            doReturn(null).when(jurorExpenseService).validateAndUpdateFinancialLossExpenseLimit(any());

            List<Appearance> appearances = List.of(attendedDay1, nonAttendedDay1, attendedDay2);
            jurorExpenseService.applyToAll(appearances, dailyExpense);


            verify(jurorExpenseService, times(1)).applyToAll(appearances, dailyExpense);


            verify(jurorExpenseService, times(1)).validateExpense(attendedDay1);
            verify(jurorExpenseService, times(1)).validateExpense(attendedDay2);
            verify(jurorExpenseService, times(1)).validateExpense(nonAttendedDay1);

            verify(jurorExpenseService, times(1)).validateAndUpdateFinancialLossExpenseLimit(attendedDay1);
            verify(jurorExpenseService, times(1)).validateAndUpdateFinancialLossExpenseLimit(attendedDay2);
            verify(jurorExpenseService, times(1)).validateAndUpdateFinancialLossExpenseLimit(nonAttendedDay1);

            verify(jurorExpenseService, times(1)).updateDraftTravelExpense(attendedDay1, travel);
            verify(jurorExpenseService, times(1)).updateDraftTravelExpense(attendedDay2, travel);
            verify(jurorExpenseService, times(1)).isAttendanceDay(attendedDay1);
            verify(jurorExpenseService, times(1)).isAttendanceDay(attendedDay2);
            verify(jurorExpenseService, times(1)).isAttendanceDay(nonAttendedDay1);

            verify(jurorExpenseService, times(1)).saveAppearancesWithExpenseRateIdUpdate(appearances);

            verify(dailyExpense, times(5)).getFinancialLoss();
            verify(dailyExpense, times(1)).getApplyToAllDays();
            verify(dailyExpense, times(3)).getPaymentMethod();
            verify(financialLoss, times(3)).getOtherCosts();
            verify(financialLoss, times(3)).getOtherCosts();
            verify(financialLoss, times(3)).getOtherCostsDescription();
            verify(financialLoss, times(3)).getExtraCareCost();
            verify(financialLoss, times(3)).getLossOfEarningsOrBenefits();
            verify(dailyExpense, times(2)).getTravel();

            verify(attendedDay1, times(1)).setMiscAmountDue(otherCost);
            verify(attendedDay2, times(1)).setMiscAmountDue(otherCost);
            verify(nonAttendedDay1, times(1)).setMiscAmountDue(otherCost);
            verify(attendedDay1, times(1)).setMiscDescription(otherDesc);
            verify(attendedDay2, times(1)).setMiscDescription(otherDesc);
            verify(nonAttendedDay1, times(1)).setMiscDescription(otherDesc);
            verify(attendedDay1, times(1)).setChildcareDue(extraCareCost);
            verify(attendedDay2, times(1)).setChildcareDue(extraCareCost);
            verify(nonAttendedDay1, times(1)).setChildcareDue(extraCareCost);

            verify(attendedDay1, times(1)).setLossOfEarningsDue(lossOfEarnings);
            verify(attendedDay2, times(1)).setLossOfEarningsDue(lossOfEarnings);
            verify(nonAttendedDay1, times(1)).setLossOfEarningsDue(lossOfEarnings);

            verify(attendedDay1, times(1)).setPayCash(false);
            verify(attendedDay2, times(1)).setPayCash(false);
            verify(nonAttendedDay1, times(1)).setPayCash(false);


            verifyNoMoreInteractions(dailyExpense, financialLoss, attendedDay1, attendedDay2, nonAttendedDay1,
                jurorExpenseService);
        }
    }

    @Nested
    @DisplayName("void updateDraftTimeExpense(Appearance appearance, DailyExpenseTime time)")
    class UpdateDraftTimeExpense {
        @Test
        void positiveTypical() {
            PayAttendanceType payAttendanceType = PayAttendanceType.FULL_DAY;
            Appearance appearance = mock(Appearance.class);
            DailyExpenseTime dailyExpenseTime = spy(
                DailyExpenseTime.builder()
                    .payAttendance(payAttendanceType)
                    .build()
            );


            jurorExpenseService.updateDraftTimeExpense(appearance, dailyExpenseTime);

            verify(appearance, times(1))
                .setPayAttendanceType(payAttendanceType);

            verify(dailyExpenseTime, times(1))
                .getPayAttendance();

            verifyNoMoreInteractions(appearance, dailyExpenseTime);
        }

        @Test
        void positiveNullTime() {
            Appearance appearance = mock(Appearance.class);
            jurorExpenseService.updateDraftTimeExpense(appearance, null);
            verifyNoInteractions(appearance);
        }
    }

    @Nested
    @DisplayName("void checkTotalExpensesAreNotLessThan0(Appearance appearance)")
    class CheckTotalExpensesAreNotLessThan0 {
        @Test
        void positivePass() {
            Appearance appearance = mock(Appearance.class);
            when(appearance.getTotalDue()).thenReturn(BigDecimal.ZERO);
            assertDoesNotThrow(() -> jurorExpenseService.checkTotalExpensesAreNotLessThan0(appearance),
                "Should not throw an exception when total expense is greater than 0");
            verify(appearance, times(1)).getTotalDue();
            verifyNoMoreInteractions(appearance);
        }

        @Test
        void negativeFail() {
            LocalDate date = LocalDate.now();
            Appearance appearance = mock(Appearance.class);
            when(appearance.getAttendanceDate()).thenReturn(date);
            when(appearance.getTotalDue()).thenReturn(new BigDecimal("-0.01"));

            MojException.BusinessRuleViolation exception =
                assertThrows(MojException.BusinessRuleViolation.class,
                    () -> jurorExpenseService.checkTotalExpensesAreNotLessThan0(appearance),
                    "Exception should be thrown when expense is less that 0"
                );

            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage()).isEqualTo("Total expenses cannot be less than £0. For Day " + date);
            assertThat(exception.getErrorCode()).isEqualTo(EXPENSES_CANNOT_BE_LESS_THAN_ZERO);


            verify(appearance, times(1)).getTotalDue();
            verify(appearance, times(1)).getAttendanceDate();
            verifyNoMoreInteractions(appearance);
        }
    }

    @Nested
    @DisplayName("boolean validateTotalExpense(Appearance appearance)")
    class ValidateTotalExpense {
        @Test
        void positivePassGreaterThan0() {
            Appearance appearance = mock(Appearance.class);
            when(appearance.getTotalDue()).thenReturn(new BigDecimal("1.00"));
            assertThat(jurorExpenseService.validateTotalExpense(appearance))
                .isTrue();
            verify(appearance, times(1)).getTotalDue();
            verifyNoMoreInteractions(appearance);
        }

        @Test
        void positivePassEqualTo0() {
            Appearance appearance = mock(Appearance.class);
            when(appearance.getTotalDue()).thenReturn(BigDecimal.ZERO);
            assertThat(jurorExpenseService.validateTotalExpense(appearance))
                .isTrue();
            verify(appearance, times(1)).getTotalDue();
            verifyNoMoreInteractions(appearance);
        }

        @Test
        void positiveFailLessThan0() {
            Appearance appearance = mock(Appearance.class);
            when(appearance.getTotalDue()).thenReturn(new BigDecimal("-0.01"));
            assertThat(jurorExpenseService.validateTotalExpense(appearance))
                .isFalse();
            verify(appearance, times(1)).getTotalDue();
            verifyNoMoreInteractions(appearance);
        }
    }

    @Nested
    @DisplayName("FinancialLossWarning validateAndUpdateFinancialLossExpenseLimit(Appearance appearance)")
    class ValidateAndUpdateFinancialLossExpenseLimit {
        private final LocalDate date = LocalDate.now();
        private final BigDecimal halfDayStandardLimit = new BigDecimal("10.00");
        private final BigDecimal halfDayLongLimit = new BigDecimal("15.00");
        private final BigDecimal fullDayStandardLimit = new BigDecimal("20.00");
        private final BigDecimal fullDayLongLimit = new BigDecimal("25.00");
        private ExpenseRates expenseRates;

        @BeforeEach
        void beforeEach() {
            this.expenseRates = mock(ExpenseRates.class);
            doReturn(halfDayStandardLimit).when(expenseRates).getLimitFinancialLossHalfDay();
            doReturn(halfDayLongLimit).when(expenseRates).getLimitFinancialLossHalfDayLongTrial();
            doReturn(fullDayStandardLimit).when(expenseRates).getLimitFinancialLossFullDay();
            doReturn(fullDayLongLimit).when(expenseRates).getLimitFinancialLossFullDayLongTrial();
            doReturn(expenseRates).when(jurorExpenseService).getCurrentExpenseRates(false);
        }

        private Appearance createAppearanceMock(Double lossOfEarnings, Double extraCareCost,
                                                Double effectiveOtherCost,
                                                PayAttendanceType payAttendanceType, boolean longTrial) {
            Appearance appearance = mock(Appearance.class);
            doReturn(doubleToBigDecimal(lossOfEarnings)).when(appearance).getLossOfEarningsDue();
            doReturn(doubleToBigDecimal(extraCareCost)).when(appearance).getChildcareDue();
            doReturn(doubleToBigDecimal(effectiveOtherCost)).when(appearance).getMiscAmountDue();
            doReturn(payAttendanceType).when(appearance).getPayAttendanceType();
            doReturn(longTrial).when(appearance).isLongTrialDay();
            doReturn(date).when(appearance).getAttendanceDate();
            return appearance;
        }

        protected BigDecimal doubleToBigDecimal(Double value) {
            return doubleToBigDecimal(value, 2);
        }

        protected BigDecimal doubleToBigDecimal(Double value, int precision) {
            if (value == null) {
                return null;
            }
            return new BigDecimal(String.format("%." + precision + "f", value));
        }

        @Test
        void positiveHalfDayStandardTrialTypical() {
            PayAttendanceType payAttendanceType = PayAttendanceType.HALF_DAY;
            boolean isLongTrial = false;
            Appearance appearance = createAppearanceMock(60.00, null, null,
                payAttendanceType, isLongTrial);

            FinancialLossWarning financialLossWarning =
                jurorExpenseService.validateAndUpdateFinancialLossExpenseLimit(appearance);
            assertThat(financialLossWarning).isNotNull();
            assertThat(financialLossWarning.getDate()).isEqualTo(this.date);
            assertThat(financialLossWarning.getJurorsLoss()).isEqualTo(new BigDecimal("60.00"));
            assertThat(financialLossWarning.getLimit()).isEqualTo(this.halfDayStandardLimit);
            assertThat(financialLossWarning.getAttendanceType()).isEqualTo(payAttendanceType);
            assertThat(financialLossWarning.getIsLongTrialDay()).isEqualTo(isLongTrial);
            assertThat(financialLossWarning.getMessage()).isEqualTo("The amount you entered will automatically be "
                + "recalculated to limit the juror's loss to £10.00");

            verify(appearance, times(1)).setLossOfEarningsDue(new BigDecimal("10.00"));
            verify(appearance, times(1)).setChildcareDue(BigDecimal.ZERO);
            verify(appearance, times(1)).setMiscAmountDue(BigDecimal.ZERO);
            verify(jurorExpenseService, times(1)).getCurrentExpenseRates(false);

        }

        @Test
        void positiveHalfDayLongTrialTypical() {
            PayAttendanceType payAttendanceType = PayAttendanceType.HALF_DAY;
            boolean isLongTrial = true;
            Appearance appearance = createAppearanceMock(60.00, null, null,
                payAttendanceType, isLongTrial);

            FinancialLossWarning financialLossWarning =
                jurorExpenseService.validateAndUpdateFinancialLossExpenseLimit(appearance);
            assertThat(financialLossWarning).isNotNull();
            assertThat(financialLossWarning.getDate()).isEqualTo(this.date);
            assertThat(financialLossWarning.getJurorsLoss()).isEqualTo(new BigDecimal("60.00"));
            assertThat(financialLossWarning.getLimit()).isEqualTo(this.halfDayLongLimit);
            assertThat(financialLossWarning.getAttendanceType()).isEqualTo(payAttendanceType);
            assertThat(financialLossWarning.getIsLongTrialDay()).isEqualTo(isLongTrial);
            assertThat(financialLossWarning.getMessage()).isEqualTo("The amount you entered will automatically be "
                + "recalculated to limit the juror's loss to £15.00");

            verify(appearance, times(1)).setLossOfEarningsDue(new BigDecimal("15.00"));
            verify(appearance, times(1)).setChildcareDue(BigDecimal.ZERO);
            verify(appearance, times(1)).setMiscAmountDue(BigDecimal.ZERO);
            verify(jurorExpenseService, times(1)).getCurrentExpenseRates(false);
        }

        @Test
        void positiveFullDayStandardTrialTypical() {
            PayAttendanceType payAttendanceType = PayAttendanceType.FULL_DAY;
            boolean isLongTrial = false;
            Appearance appearance = createAppearanceMock(60.00, null, null,
                payAttendanceType, isLongTrial);

            FinancialLossWarning financialLossWarning =
                jurorExpenseService.validateAndUpdateFinancialLossExpenseLimit(appearance);
            assertThat(financialLossWarning).isNotNull();
            assertThat(financialLossWarning.getDate()).isEqualTo(this.date);
            assertThat(financialLossWarning.getJurorsLoss()).isEqualTo(new BigDecimal("60.00"));
            assertThat(financialLossWarning.getLimit()).isEqualTo(this.fullDayStandardLimit);
            assertThat(financialLossWarning.getAttendanceType()).isEqualTo(payAttendanceType);
            assertThat(financialLossWarning.getIsLongTrialDay()).isEqualTo(isLongTrial);
            assertThat(financialLossWarning.getMessage()).isEqualTo("The amount you entered will automatically be "
                + "recalculated to limit the juror's loss to £20.00");

            verify(appearance, times(1)).setLossOfEarningsDue(new BigDecimal("20.00"));
            verify(appearance, times(1)).setChildcareDue(BigDecimal.ZERO);
            verify(appearance, times(1)).setMiscAmountDue(BigDecimal.ZERO);
            verify(jurorExpenseService, times(1)).getCurrentExpenseRates(false);
        }

        @Test
        void positiveFullDayLongTrialTypical() {
            PayAttendanceType payAttendanceType = PayAttendanceType.FULL_DAY;
            boolean isLongTrial = true;
            Appearance appearance = createAppearanceMock(60.00, null, null,
                payAttendanceType, isLongTrial);

            FinancialLossWarning financialLossWarning =
                jurorExpenseService.validateAndUpdateFinancialLossExpenseLimit(appearance);
            assertThat(financialLossWarning).isNotNull();
            assertThat(financialLossWarning.getDate()).isEqualTo(this.date);
            assertThat(financialLossWarning.getJurorsLoss()).isEqualTo(new BigDecimal("60.00"));
            assertThat(financialLossWarning.getLimit()).isEqualTo(this.fullDayLongLimit);
            assertThat(financialLossWarning.getAttendanceType()).isEqualTo(payAttendanceType);
            assertThat(financialLossWarning.getIsLongTrialDay()).isEqualTo(isLongTrial);
            assertThat(financialLossWarning.getMessage()).isEqualTo("The amount you entered will automatically be "
                + "recalculated to limit the juror's loss to £25.00");

            verify(appearance, times(1)).setLossOfEarningsDue(new BigDecimal("25.00"));
            verify(appearance, times(1)).setChildcareDue(BigDecimal.ZERO);
            verify(appearance, times(1)).setMiscAmountDue(BigDecimal.ZERO);
            verify(jurorExpenseService, times(1)).getCurrentExpenseRates(false);
        }

        @Test
        void positiveEqualToLimit() {
            PayAttendanceType payAttendanceType = PayAttendanceType.FULL_DAY;
            boolean isLongTrial = true;
            Appearance appearance = createAppearanceMock(12.00, 5.00, 8.00,
                payAttendanceType, isLongTrial);

            FinancialLossWarning financialLossWarning =
                jurorExpenseService.validateAndUpdateFinancialLossExpenseLimit(appearance);
            assertThat(financialLossWarning).isNull();

            verify(appearance, times(1)).getPayAttendanceType();
            verify(appearance, times(1)).isLongTrialDay();
            verify(appearance, times(1)).getLossOfEarningsDue();
            verify(appearance, times(1)).getChildcareDue();
            verify(appearance, times(1)).getMiscAmountDue();
            verifyNoMoreInteractions(appearance);
            verify(jurorExpenseService, times(1)).getCurrentExpenseRates(false);
        }

        @Test
        void positiveGreaterThanLimitOtherReduced() {
            PayAttendanceType payAttendanceType = PayAttendanceType.FULL_DAY;
            boolean isLongTrial = true;
            Appearance appearance = createAppearanceMock(14.00, 5.00, 8.00,
                payAttendanceType, isLongTrial);

            FinancialLossWarning financialLossWarning =
                jurorExpenseService.validateAndUpdateFinancialLossExpenseLimit(appearance);
            assertThat(financialLossWarning).isNotNull();
            assertThat(financialLossWarning.getDate()).isEqualTo(this.date);
            assertThat(financialLossWarning.getJurorsLoss()).isEqualTo(new BigDecimal("27.00"));
            assertThat(financialLossWarning.getLimit()).isEqualTo(this.fullDayLongLimit);
            assertThat(financialLossWarning.getAttendanceType()).isEqualTo(payAttendanceType);
            assertThat(financialLossWarning.getIsLongTrialDay()).isEqualTo(isLongTrial);
            assertThat(financialLossWarning.getMessage()).isEqualTo("The amount you entered will automatically be "
                + "recalculated to limit the juror's loss to £25.00");

            verify(appearance, times(1)).setLossOfEarningsDue(new BigDecimal("14.00"));
            verify(appearance, times(1)).setChildcareDue(new BigDecimal("5.00"));
            verify(appearance, times(1)).setMiscAmountDue(new BigDecimal("6.00"));
            verify(jurorExpenseService, times(1)).getCurrentExpenseRates(false);
        }

        @Test
        void positiveGreaterThanLimitExtraCareReduced() {
            PayAttendanceType payAttendanceType = PayAttendanceType.FULL_DAY;
            boolean isLongTrial = true;
            Appearance appearance = createAppearanceMock(22.00, 5.00, 8.00,
                payAttendanceType, isLongTrial);

            FinancialLossWarning financialLossWarning =
                jurorExpenseService.validateAndUpdateFinancialLossExpenseLimit(appearance);
            assertThat(financialLossWarning).isNotNull();
            assertThat(financialLossWarning.getDate()).isEqualTo(this.date);
            assertThat(financialLossWarning.getJurorsLoss()).isEqualTo(new BigDecimal("35.00"));
            assertThat(financialLossWarning.getLimit()).isEqualTo(this.fullDayLongLimit);
            assertThat(financialLossWarning.getAttendanceType()).isEqualTo(payAttendanceType);
            assertThat(financialLossWarning.getIsLongTrialDay()).isEqualTo(isLongTrial);
            assertThat(financialLossWarning.getMessage()).isEqualTo("The amount you entered will automatically be "
                + "recalculated to limit the juror's loss to £25.00");

            verify(appearance, times(1)).setLossOfEarningsDue(new BigDecimal("22.00"));
            verify(appearance, times(1)).setChildcareDue(new BigDecimal("3.00"));
            verify(appearance, times(1)).setMiscAmountDue(BigDecimal.ZERO);
            verify(jurorExpenseService, times(1)).getCurrentExpenseRates(false);
        }

        @Test
        void positiveGreaterThanLimitLossOfEarningsReduced() {
            PayAttendanceType payAttendanceType = PayAttendanceType.FULL_DAY;
            boolean isLongTrial = true;
            Appearance appearance = createAppearanceMock(32.00, 5.00, 8.00,
                payAttendanceType, isLongTrial);

            FinancialLossWarning financialLossWarning =
                jurorExpenseService.validateAndUpdateFinancialLossExpenseLimit(appearance);
            assertThat(financialLossWarning).isNotNull();
            assertThat(financialLossWarning.getDate()).isEqualTo(this.date);
            assertThat(financialLossWarning.getJurorsLoss()).isEqualTo(new BigDecimal("45.00"));
            assertThat(financialLossWarning.getLimit()).isEqualTo(this.fullDayLongLimit);
            assertThat(financialLossWarning.getAttendanceType()).isEqualTo(payAttendanceType);
            assertThat(financialLossWarning.getIsLongTrialDay()).isEqualTo(isLongTrial);
            assertThat(financialLossWarning.getMessage()).isEqualTo("The amount you entered will automatically be "
                + "recalculated to limit the juror's loss to £25.00");

            verify(appearance, times(1)).setLossOfEarningsDue(new BigDecimal("25.00"));
            verify(appearance, times(1)).setChildcareDue(BigDecimal.ZERO);
            verify(appearance, times(1)).setMiscAmountDue(BigDecimal.ZERO);
            verify(jurorExpenseService, times(1)).getCurrentExpenseRates(false);
        }
    }

    @Nested
    @DisplayName("FinancialLossWarning updateDraftFinancialLossExpense(Appearance appearance,"
        + " DailyExpenseFinancialLoss financialLoss)")
    class UpdateDraftFinancialLossExpense {

        @Test
        void positiveTypical() {
            final BigDecimal lossOfEarnings = new BigDecimal("1.3");
            final BigDecimal extraCare = new BigDecimal("2.2");
            final BigDecimal otherCost = new BigDecimal("3.1");
            final String otherDesc = "Test Desc";

            DailyExpenseFinancialLoss financialLoss = spy(DailyExpenseFinancialLoss.builder()
                .lossOfEarningsOrBenefits(lossOfEarnings)
                .extraCareCost(extraCare)
                .otherCosts(otherCost)
                .otherCostsDescription(otherDesc)
                .build());

            Appearance appearance = mock(Appearance.class);

            doReturn(null).when(jurorExpenseService)
                .validateAndUpdateFinancialLossExpenseLimit(appearance);
            assertThat(jurorExpenseService.updateDraftFinancialLossExpense(appearance, financialLoss))
                .isNull();

            verify(appearance, times(1)).setLossOfEarningsDue(lossOfEarnings);
            verify(appearance, times(1)).setChildcareDue(extraCare);
            verify(appearance, times(1)).setMiscAmountDue(otherCost);
            verify(appearance, times(1)).setMiscDescription(otherDesc);

            verify(financialLoss, times(1)).getLossOfEarningsOrBenefits();
            verify(financialLoss, times(1)).getExtraCareCost();
            verify(financialLoss, times(1)).getOtherCosts();
            verify(financialLoss, times(1)).getOtherCostsDescription();

            verifyNoMoreInteractions(financialLoss, appearance);
        }

        @Test
        void positiveTypicalWithWarning() {
            final BigDecimal lossOfEarnings = new BigDecimal("2.3");
            final BigDecimal extraCare = new BigDecimal("3.2");
            final BigDecimal otherCost = new BigDecimal("4.1");
            final String otherDesc = "Test Desc 2";

            DailyExpenseFinancialLoss financialLoss = spy(DailyExpenseFinancialLoss.builder()
                .lossOfEarningsOrBenefits(lossOfEarnings)
                .extraCareCost(extraCare)
                .otherCosts(otherCost)
                .otherCostsDescription(otherDesc)
                .build());

            Appearance appearance = mock(Appearance.class);
            FinancialLossWarning warning = mock(FinancialLossWarning.class);

            doReturn(warning).when(jurorExpenseService)
                .validateAndUpdateFinancialLossExpenseLimit(appearance);
            assertThat(jurorExpenseService.updateDraftFinancialLossExpense(appearance, financialLoss))
                .isEqualTo(warning);

            verify(appearance, times(1)).setLossOfEarningsDue(lossOfEarnings);
            verify(appearance, times(1)).setChildcareDue(extraCare);
            verify(appearance, times(1)).setMiscAmountDue(otherCost);
            verify(appearance, times(1)).setMiscDescription(otherDesc);

            verify(financialLoss, times(1)).getLossOfEarningsOrBenefits();
            verify(financialLoss, times(1)).getExtraCareCost();
            verify(financialLoss, times(1)).getOtherCosts();
            verify(financialLoss, times(1)).getOtherCostsDescription();

            verifyNoMoreInteractions(financialLoss, appearance);

        }

        @Test
        void positiveNullFinancialLoss() {
            Appearance appearance = mock(Appearance.class);
            jurorExpenseService.updateDraftFinancialLossExpense(appearance, null);
            verifyNoMoreInteractions(appearance);
        }
    }

    @Nested
    @DisplayName("void updateDraftTravelExpense(Appearance appearance, DailyExpenseTravel travel)")
    class UpdateDraftTravelExpense {
        private final BigDecimal travelCost = new BigDecimal("1.2");
        private final BigDecimal parking = new BigDecimal("1.1");
        private final BigDecimal publicTransport = new BigDecimal("2.1");
        private final BigDecimal hiredVehicle = new BigDecimal("3.1");
        private Appearance appearance;
        private ExpenseRates expenseRates;
        private DailyExpenseTravel travel;

        void setupStandard(TravelMethod travelMethod) {
            this.appearance = spy(new Appearance());
            this.expenseRates = mock(ExpenseRates.class);
            doReturn(expenseRates).when(jurorExpenseService).getCurrentExpenseRates(false);

            DailyExpenseTravel.DailyExpenseTravelBuilder builder = DailyExpenseTravel.builder()
                .milesTraveled(3)
                .parking(parking)
                .publicTransport(publicTransport)
                .taxi(hiredVehicle);

            if (TravelMethod.CAR.equals(travelMethod)) {
                builder.traveledByCar(true)
                    .jurorsTakenCar(2);
                when(expenseRates.getCarMileageRatePerMile2OrMorePassengers())
                    .thenReturn(travelCost);
            } else if (TravelMethod.MOTERCYCLE.equals(travelMethod)) {
                builder.traveledByMotorcycle(true)
                    .jurorsTakenMotorcycle(2);
                when(expenseRates.getMotorcycleMileageRatePerMile1Passengers())
                    .thenReturn(travelCost);
            } else if (TravelMethod.BICYCLE.equals(travelMethod)) {
                builder.traveledByBicycle(true);
                when(expenseRates.getBikeRate())
                    .thenReturn(travelCost);
            }
            this.travel = spy(builder.build());
        }

        void verifyStandard(TravelMethod travelMethod) {
            verify(jurorExpenseService, times(1)).getCurrentExpenseRates(false);
            verify(appearance, times(1)).setMilesTraveled(3);
            verify(appearance, times(1)).setParkingDue(parking);
            verify(appearance, times(1)).setPublicTransportDue(publicTransport);
            verify(appearance, times(1)).setHiredVehicleDue(hiredVehicle);


            verify(appearance, times(1)).getTraveledByCar();
            verify(appearance, times(1)).getJurorsTakenCar();
            verify(appearance, times(1)).getTraveledByMotorcycle();
            verify(appearance, times(1)).getJurorsTakenMotorcycle();
            verify(appearance, times(1)).getTraveledByBicycle();

            verify(travel, times(1)).getMilesTraveled();
            verify(travel, times(1)).getParking();
            verify(travel, times(1)).getPublicTransport();
            verify(travel, times(1)).getTaxi();


            if (TravelMethod.CAR.equals(travelMethod)) {
                verify(expenseRates, times(1)).getCarMileageRatePerMile2OrMorePassengers();
            } else if (TravelMethod.MOTERCYCLE.equals(travelMethod)) {
                verify(expenseRates, times(1)).getMotorcycleMileageRatePerMile1Passengers();
            } else if (TravelMethod.BICYCLE.equals(travelMethod)) {
                verify(expenseRates, times(1)).getBikeRate();
            }

            verifyNoMoreInteractions(appearance, expenseRates, travel);
        }

        @Test
        void positiveTypicalCar() {
            TravelMethod travelMethod = TravelMethod.CAR;
            setupStandard(travelMethod);
            jurorExpenseService.updateDraftTravelExpense(appearance, travel);

            verify(appearance, times(1)).setCarDue(new BigDecimal("3.6"));
            verify(appearance, times(1)).setTraveledByCar(true);
            verify(appearance, times(1)).setJurorsTakenCar(2);
            verify(travel, times(1)).getTraveledByCar();
            verify(travel, times(1)).getJurorsTakenCar();

            verify(appearance, times(1)).setMotorcycleDue(null);
            verify(appearance, times(1)).setTraveledByMotorcycle(null);
            verify(appearance, times(1)).setJurorsTakenMotorcycle(null);
            verify(travel, times(1)).getTraveledByMotorcycle();
            verify(travel, times(1)).getJurorsTakenMotorcycle();

            verify(appearance, times(1)).setBicycleDue(null);
            verify(appearance, times(1)).setTraveledByBicycle(null);
            verify(travel, times(1)).getTraveledByBicycle();

            verifyStandard(travelMethod);
        }

        @Test
        void positiveTypicalMotorCycle() {
            TravelMethod travelMethod = TravelMethod.MOTERCYCLE;
            setupStandard(travelMethod);
            jurorExpenseService.updateDraftTravelExpense(appearance, travel);

            verify(appearance, times(1)).setCarDue(null);
            verify(appearance, times(1)).setTraveledByCar(null);
            verify(appearance, times(1)).setJurorsTakenCar(null);
            verify(travel, times(1)).getTraveledByCar();
            verify(travel, times(1)).getJurorsTakenCar();

            verify(appearance, times(1)).setMotorcycleDue(new BigDecimal("3.6"));
            verify(appearance, times(1)).setTraveledByMotorcycle(true);
            verify(appearance, times(1)).setJurorsTakenMotorcycle(2);
            verify(travel, times(1)).getTraveledByMotorcycle();
            verify(travel, times(1)).getJurorsTakenMotorcycle();

            verify(appearance, times(1)).setBicycleDue(null);
            verify(appearance, times(1)).setTraveledByBicycle(null);
            verify(travel, times(1)).getTraveledByBicycle();
            verifyStandard(travelMethod);
        }

        @Test
        void positiveTypicalBike() {
            TravelMethod travelMethod = TravelMethod.BICYCLE;
            setupStandard(travelMethod);
            jurorExpenseService.updateDraftTravelExpense(appearance, travel);

            verify(appearance, times(1)).setCarDue(null);
            verify(appearance, times(1)).setTraveledByCar(null);
            verify(appearance, times(1)).setJurorsTakenCar(null);
            verify(travel, times(1)).getTraveledByCar();
            verify(travel, times(1)).getJurorsTakenCar();

            verify(appearance, times(1)).setMotorcycleDue(null);
            verify(appearance, times(1)).setTraveledByMotorcycle(null);
            verify(appearance, times(1)).setJurorsTakenMotorcycle(null);
            verify(travel, times(1)).getTraveledByMotorcycle();
            verify(travel, times(1)).getJurorsTakenMotorcycle();

            verify(appearance, times(1)).setBicycleDue(new BigDecimal("3.6"));
            verify(appearance, times(1)).setTraveledByBicycle(true);
            verify(travel, times(1)).getTraveledByBicycle();
            verifyStandard(travelMethod);
        }

        @Test
        void positiveNullTravel() {
            Appearance appearance = mock(Appearance.class);
            jurorExpenseService.updateDraftTravelExpense(appearance, null);
            verifyNoMoreInteractions(appearance);
        }

        @Test
        void positiveNullMilesTraveled() {
            this.appearance = spy(new Appearance());
            this.expenseRates = mock(ExpenseRates.class);
            doReturn(expenseRates).when(jurorExpenseService).getCurrentExpenseRates(false);
            when(expenseRates.getCarMileageRatePerMile2OrMorePassengers()).thenReturn(travelCost);

            this.travel = spy(DailyExpenseTravel.builder()
                .traveledByCar(true)
                .jurorsTakenCar(2)
                .milesTraveled(null)
                .parking(parking)
                .publicTransport(publicTransport)
                .taxi(hiredVehicle)
                .build());

            jurorExpenseService.updateDraftTravelExpense(appearance, travel);
            verify(appearance, times(1)).setCarDue(new BigDecimal("0.0"));
            verify(appearance, times(1)).setMotorcycleDue(null);
            verify(appearance, times(1)).setBicycleDue(null);

            verify(appearance, times(1)).setTraveledByCar(true);
            verify(appearance, times(1)).setJurorsTakenCar(2);
            verify(appearance, times(1)).setTraveledByMotorcycle(null);
            verify(appearance, times(1)).setJurorsTakenMotorcycle(null);
            verify(appearance, times(1)).setTraveledByBicycle(null);

            verify(appearance, times(1)).setMilesTraveled(null);
            verify(appearance, times(1)).setParkingDue(parking);
            verify(appearance, times(1)).setPublicTransportDue(publicTransport);
            verify(appearance, times(1)).setHiredVehicleDue(hiredVehicle);

            verify(appearance, times(1)).getJurorsTakenCar();
            verify(appearance, times(1)).getTraveledByCar();
            verify(appearance, times(1)).getJurorsTakenMotorcycle();
            verify(appearance, times(1)).getTraveledByMotorcycle();
            verify(appearance, times(1)).getTraveledByBicycle();


            verify(travel, times(1)).getTraveledByCar();
            verify(travel, times(1)).getJurorsTakenCar();
            verify(travel, times(1)).getTraveledByMotorcycle();
            verify(travel, times(1)).getJurorsTakenMotorcycle();
            verify(travel, times(1)).getTraveledByBicycle();

            verify(travel, times(1)).getMilesTraveled();
            verify(travel, times(1)).getParking();
            verify(travel, times(1)).getPublicTransport();
            verify(travel, times(1)).getTaxi();

            verify(expenseRates, times(1)).getCarMileageRatePerMile2OrMorePassengers();
            verifyNoMoreInteractions(appearance, expenseRates, travel);
        }
    }

    @Nested
    @DisplayName("void updateDraftFoodAndDrinkExpense(Appearance appearance, DailyExpenseFoodAndDrink foodAndDrink)")
    class UpdateDraftFoodAndDrinkExpense {
        @Test
        void positiveNullFoodAndDrink() {
            Appearance appearance = mock(Appearance.class);
            jurorExpenseService.updateDraftFoodAndDrinkExpense(appearance, null);
            verifyNoMoreInteractions(appearance);
        }

        @Test
        void positiveTypical() {
            BigDecimal rate = new BigDecimal("12.22");
            ExpenseRates expenseRates = mock(ExpenseRates.class);
            Appearance appearance = mock(Appearance.class);
            doReturn(expenseRates).when(jurorExpenseService).getCurrentExpenseRates(false);
            FoodDrinkClaimType foodDrinkClaimType = mock(FoodDrinkClaimType.class);
            when(foodDrinkClaimType.getRate(expenseRates)).thenReturn(rate);


            BigDecimal smartCardRate = new BigDecimal("3.33");
            DailyExpenseFoodAndDrink foodAndDrink = spy(DailyExpenseFoodAndDrink.builder()
                .foodAndDrinkClaimType(foodDrinkClaimType)
                .smartCardAmount(smartCardRate)
                .build());

            jurorExpenseService.updateDraftFoodAndDrinkExpense(appearance, foodAndDrink);


            verify(appearance, times(1)).setSubsistenceDue(rate);
            verify(appearance, times(1)).setSmartCardAmountDue(smartCardRate);
            verify(appearance, times(1)).setFoodAndDrinkClaimType(foodDrinkClaimType);

            verify(foodAndDrink, times(1)).getSmartCardAmount();
            verify(foodAndDrink, times(1)).getFoodAndDrinkClaimType();

            verify(foodDrinkClaimType, times(1)).getRate(expenseRates);

            verifyNoMoreInteractions(expenseRates, foodAndDrink, appearance);
        }
    }

    @Test
    void positiveGetTimeFromAppearance() {
        Appearance appearance = mock(Appearance.class);

        LocalTime timeSpentAtCourt = LocalTime.now();
        when(appearance.getTimeSpentAtCourt()).thenReturn(timeSpentAtCourt);

        PayAttendanceType payAttendanceType = PayAttendanceType.FULL_DAY;
        when(appearance.getPayAttendanceType()).thenReturn(payAttendanceType);

        LocalTime travelTime = LocalTime.now().plusHours(1);
        when(appearance.getTravelTime()).thenReturn(travelTime);

        GetEnteredExpenseResponse.DailyExpenseTimeEntered data =
            jurorExpenseService.getTimeFromAppearance(appearance);

        assertThat(data).isNotNull();
        assertThat(data.getTimeSpentAtCourt()).isEqualTo(timeSpentAtCourt);
        assertThat(data.getPayAttendance()).isEqualTo(payAttendanceType);
        assertThat(data.getTravelTime()).isEqualTo(travelTime);

        verify(appearance, times(1)).getTimeSpentAtCourt();
        verify(appearance, times(1)).getPayAttendanceType();
        verify(appearance, times(1)).getTravelTime();
        verifyNoMoreInteractions(appearance);
    }

    @Test
    void positiveGetFinancialLossFromAppearance() {
        Appearance appearance = mock(Appearance.class);
        BigDecimal lossOfEarningsDue = new BigDecimal("1.11");
        when(appearance.getLossOfEarningsDue()).thenReturn(lossOfEarningsDue);

        BigDecimal childcareDue = new BigDecimal("2.22");
        when(appearance.getChildcareDue()).thenReturn(childcareDue);

        BigDecimal otherCosts = new BigDecimal("3.33");
        when(appearance.getMiscAmountDue()).thenReturn(otherCosts);

        String otherDescription = "Other Desc";
        when(appearance.getMiscDescription()).thenReturn(otherDescription);

        DailyExpenseFinancialLoss financialLoss = jurorExpenseService.getFinancialLossFromAppearance(appearance);

        assertThat(financialLoss).isNotNull();
        assertThat(financialLoss.getLossOfEarningsOrBenefits()).isEqualTo(lossOfEarningsDue);
        assertThat(financialLoss.getExtraCareCost()).isEqualTo(childcareDue);
        assertThat(financialLoss.getOtherCosts()).isEqualTo(otherCosts);
        assertThat(financialLoss.getOtherCostsDescription()).isEqualTo(otherDescription);

        verify(appearance, times(1)).getLossOfEarningsDue();
        verify(appearance, times(1)).getChildcareDue();
        verify(appearance, times(1)).getMiscAmountDue();
        verify(appearance, times(1)).getMiscDescription();

        verifyNoMoreInteractions(appearance);
    }

    @Test
    void positiveGetTravelFromAppearance() {
        Appearance appearance = mock(Appearance.class);
        Boolean travelByCar = true;
        when(appearance.getTraveledByCar()).thenReturn(travelByCar);
        int jurorsTakenByCar = 1;
        when(appearance.getJurorsTakenCar()).thenReturn(jurorsTakenByCar);
        Boolean travelByMotorcycle = false;
        when(appearance.getTraveledByMotorcycle()).thenReturn(travelByMotorcycle);
        int jurorsTakenByMotorcycle = 2;
        when(appearance.getJurorsTakenMotorcycle()).thenReturn(jurorsTakenByMotorcycle);
        boolean travelByBike = true;
        when(appearance.getTraveledByBicycle()).thenReturn(travelByBike);
        int milesTraveled = 6;
        when(appearance.getMilesTraveled()).thenReturn(milesTraveled);
        BigDecimal parking = new BigDecimal("1.12");
        when(appearance.getParkingDue()).thenReturn(parking);
        BigDecimal publicTransport = new BigDecimal("2.12");
        when(appearance.getPublicTransportDue()).thenReturn(publicTransport);
        BigDecimal taxi = new BigDecimal("3.12");
        when(appearance.getHiredVehicleDue()).thenReturn(taxi);

        DailyExpenseTravel travel = jurorExpenseService.getTravelFromAppearance(appearance);

        assertThat(travel).isNotNull();
        assertThat(travel.getTraveledByCar()).isEqualTo(travelByCar);
        assertThat(travel.getJurorsTakenCar()).isEqualTo(jurorsTakenByCar);
        assertThat(travel.getTraveledByMotorcycle()).isEqualTo(travelByMotorcycle);
        assertThat(travel.getJurorsTakenMotorcycle()).isEqualTo(jurorsTakenByMotorcycle);
        assertThat(travel.getTraveledByBicycle()).isEqualTo(travelByBike);
        assertThat(travel.getMilesTraveled()).isEqualTo(milesTraveled);
        assertThat(travel.getParking()).isEqualTo(parking);
        assertThat(travel.getPublicTransport()).isEqualTo(publicTransport);
        assertThat(travel.getTaxi()).isEqualTo(taxi);


        verify(appearance, times(1)).getTraveledByCar();
        verify(appearance, times(1)).getJurorsTakenCar();
        verify(appearance, times(1)).getTraveledByMotorcycle();
        verify(appearance, times(1)).getJurorsTakenMotorcycle();
        verify(appearance, times(1)).getTraveledByBicycle();
        verify(appearance, times(1)).getMilesTraveled();
        verify(appearance, times(1)).getParkingDue();
        verify(appearance, times(1)).getPublicTransportDue();
        verify(appearance, times(1)).getHiredVehicleDue();

        verifyNoMoreInteractions(appearance);
    }

    @Test
    void positiveGetFoodAndDrinkFromAppearance() {
        Appearance appearance = mock(Appearance.class);
        FoodDrinkClaimType drinkClaimType = FoodDrinkClaimType.LESS_THAN_OR_EQUAL_TO_10_HOURS;
        when(appearance.getFoodAndDrinkClaimType()).thenReturn(drinkClaimType);
        BigDecimal smartCard = new BigDecimal("2.21");
        when(appearance.getSmartCardAmountDue()).thenReturn(smartCard);

        DailyExpenseFoodAndDrink foodAndDrink = jurorExpenseService.getFoodAndDrinkFromAppearance(appearance);

        assertThat(foodAndDrink).isNotNull();
        assertThat(foodAndDrink.getFoodAndDrinkClaimType()).isEqualTo(drinkClaimType);
        assertThat(foodAndDrink.getSmartCardAmount()).isEqualTo(smartCard);

        verify(appearance, times(1)).getFoodAndDrinkClaimType();
        verify(appearance, times(1)).getSmartCardAmountDue();

        verifyNoMoreInteractions(appearance);
    }

    @Test
    void positiveGetEnteredExpense() {
        LocalDate date = LocalDate.now();
        Appearance appearance = mock(Appearance.class);
        doReturn(appearance).when(jurorExpenseService).getAppearance(
            TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER, date
        );
        when(appearance.getAttendanceDate()).thenReturn(date);
        AppearanceStage stage = AppearanceStage.EXPENSE_ENTERED;
        when(appearance.getAppearanceStage()).thenReturn(stage);
        BigDecimal totalDue = new BigDecimal("12.2");
        when(appearance.getTotalDue()).thenReturn(totalDue);
        BigDecimal totalPaid = new BigDecimal("12.2");
        when(appearance.getTotalPaid()).thenReturn(totalPaid);
        Boolean payCash = true;
        when(appearance.isPayCash()).thenReturn(payCash);
        when(appearance.getNonAttendanceDay()).thenReturn(true);

        GetEnteredExpenseResponse.DailyExpenseTimeEntered time =
            mock(GetEnteredExpenseResponse.DailyExpenseTimeEntered.class);
        doReturn(time).when(jurorExpenseService).getTimeFromAppearance(appearance);

        DailyExpenseFinancialLoss financialLoss = mock(DailyExpenseFinancialLoss.class);
        doReturn(financialLoss).when(jurorExpenseService).getFinancialLossFromAppearance(appearance);

        DailyExpenseTravel travel = mock(DailyExpenseTravel.class);
        doReturn(travel).when(jurorExpenseService).getTravelFromAppearance(appearance);

        DailyExpenseFoodAndDrink foodAndDrink = mock(DailyExpenseFoodAndDrink.class);
        doReturn(foodAndDrink).when(jurorExpenseService).getFoodAndDrinkFromAppearance(appearance);

        GetEnteredExpenseResponse data = jurorExpenseService.getEnteredExpense(
            TestConstants.VALID_JUROR_NUMBER,
            TestConstants.VALID_POOL_NUMBER,
            date
        );

        assertThat(data).isNotNull();
        assertThat(data.getDateOfExpense()).isEqualTo(date);
        assertThat(data.getStage()).isEqualTo(stage);
        assertThat(data.getTotalDue()).isEqualTo(totalDue);
        assertThat(data.getTotalPaid()).isEqualTo(totalPaid);
        assertThat(data.getPayCash()).isEqualTo(payCash);
        assertThat(data.getTime()).isEqualTo(time);
        assertThat(data.getFinancialLoss()).isEqualTo(financialLoss);
        assertThat(data.getTravel()).isEqualTo(travel);
        assertThat(data.getFoodAndDrink()).isEqualTo(foodAndDrink);
        assertThat(data.getNoneAttendanceDay()).isEqualTo(true);

        verify(jurorExpenseService, times(1))
            .getEnteredExpense(TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER,
                date);
        verify(jurorExpenseService, times(1))
            .getAppearance(TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER,
                date);
        verify(appearance, times(1)).getAttendanceDate();
        verify(appearance, times(1)).getAppearanceStage();
        verify(appearance, times(1)).getTotalDue();
        verify(appearance, times(1)).getTotalPaid();
        verify(appearance, times(1)).isPayCash();
        verify(appearance, times(1)).getNonAttendanceDay();
        verify(jurorExpenseService, times(1)).getTimeFromAppearance(appearance);
        verify(jurorExpenseService, times(1)).getFinancialLossFromAppearance(appearance);
        verify(jurorExpenseService, times(1)).getTravelFromAppearance(appearance);
        verify(jurorExpenseService, times(1)).getFoodAndDrinkFromAppearance(appearance);

        verifyNoMoreInteractions(appearance);
    }

    @Test
    void positiveGetAppearance() {
        LocalDate date = LocalDate.now();
        Appearance appearance = mock(Appearance.class);
        doReturn(Optional.of(appearance)).when(appearanceRepository)
            .findByCourtLocationLocCodeAndJurorNumberAndAttendanceDate(
                TestConstants.VALID_COURT_LOCATION,
                TestConstants.VALID_JUROR_NUMBER,
                date);

        assertThat(jurorExpenseService.getAppearance(
            TestConstants.VALID_COURT_LOCATION,
            TestConstants.VALID_JUROR_NUMBER,
            date
        )).isEqualTo(appearance);

        verify(appearanceRepository, times(1))
            .findByCourtLocationLocCodeAndJurorNumberAndAttendanceDate(
                TestConstants.VALID_COURT_LOCATION,
                TestConstants.VALID_JUROR_NUMBER,
                date);
        verifyNoMoreInteractions(appearanceRepository);
    }

    @Test
    void negativeGetAppearanceNotFound() {
        LocalDate date = LocalDate.now();
        doReturn(Optional.empty()).when(appearanceRepository)
            .findByCourtLocationLocCodeAndJurorNumberAndAttendanceDate(
                TestConstants.VALID_COURT_LOCATION,
                TestConstants.VALID_JUROR_NUMBER,
                date);

        MojException.NotFound exception = assertThrows(MojException.NotFound.class,
            () -> jurorExpenseService.getAppearance(TestConstants.VALID_COURT_LOCATION,
                TestConstants.VALID_JUROR_NUMBER,
                date),
            "Should throw exception when appearance is not found");

        assertThat(exception).isNotNull();
        assertThat(exception.getCause()).isNull();
        assertThat(exception.getMessage())
            .isEqualTo("No appearance record found for juror: 123456789 on day: " + date);

        verify(appearanceRepository, times(1))
            .findByCourtLocationLocCodeAndJurorNumberAndAttendanceDate(
                TestConstants.VALID_COURT_LOCATION,
                TestConstants.VALID_JUROR_NUMBER,
                date);
        verifyNoMoreInteractions(appearanceRepository);
    }


    @Nested
    @DisplayName("public void approveExpenses(ApproveExpenseDto dto)")
    class ApproveExpenses {

        @BeforeEach
        void beforeEach() {
            securityUtilMockedStatic = mockStatic(SecurityUtil.class);
            securityUtilMockedStatic.when(SecurityUtil::getActiveOwner)
                .thenReturn(TestConstants.VALID_COURT_LOCATION);
        }

        @AfterEach
        void afterEach() {
            if (securityUtilMockedStatic != null) {
                securityUtilMockedStatic.close();
            }
        }

        private User mockUser(String username) {
            securityUtilMockedStatic.when(SecurityUtil::getActiveLogin)
                .thenReturn(username);
            User user = mock(User.class);
            doReturn(user).when(userService).findByUsername(username);
            doReturn(TestConstants.VALID_COURT_LOCATION).when(user).getOwner();
            return user;
        }

        private Appearance mockAppearance(CourtLocation courtLocation, boolean isCash) {
            Appearance appearance = mock(Appearance.class);
            doReturn(courtLocation).when(appearance).getCourtLocation();
            doReturn(isCash).when(appearance).isPayCash();
            return appearance;
        }

        @Test
        void positiveTypicalBacs() {
            User user = mockUser("testUser");
            when(user.getApprovalLimit()).thenReturn(new BigDecimal("1000.00"));
            ApproveExpenseDto.ApprovalType approvalType = mock(ApproveExpenseDto.ApprovalType.class);
            ApproveExpenseDto approveExpenseDto = ApproveExpenseDto.builder()
                .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                .approvalType(approvalType)
                .dateToRevisions(List.of(
                    ApproveExpenseDto.DateToRevision.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 1))
                        .version(1L)
                        .build(),
                    ApproveExpenseDto.DateToRevision.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 2))
                        .version(2L)
                        .build()
                ))
                .build();

            CourtLocation courtLocation = mock(CourtLocation.class);
            doReturn(TestConstants.VALID_COURT_LOCATION).when(courtLocation).getOwner();
            doReturn(TestConstants.VALID_COURT_LOCATION).when(courtLocation).getLocCode();
            Appearance appearance1 = mockAppearance(courtLocation, false);
            Appearance appearance2 = mockAppearance(courtLocation, true);
            Appearance appearance3 = mockAppearance(courtLocation, false);
            doReturn(true).when(approvalType).isApplicable(appearance1);
            doReturn(new BigDecimal("1.01")).when(appearance1).getTotalChanged();
            doReturn(true).when(approvalType).isApplicable(appearance2);
            doReturn(new BigDecimal("2.01")).when(appearance2).getTotalChanged();
            doReturn(true).when(approvalType).isApplicable(appearance3);
            doReturn(new BigDecimal("3.01")).when(appearance3).getTotalChanged();
            doReturn(FinancialAuditDetails.Type.APPROVED_BACS).when(approvalType).toApproveType(anyBoolean());
            doReturn(LocalDate.of(2023, 1, 2)).when(appearance1).getAttendanceDate();
            doReturn(LocalDate.of(2023, 1, 1)).when(appearance3).getAttendanceDate();

            List<Appearance> appearances = List.of(appearance1, appearance2, appearance3);

            doReturn(appearances).when(appearanceRepository)
                .findAllByCourtLocationLocCodeAndJurorNumber(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER);

            doReturn(true).when(jurorExpenseService)
                .validateAppearanceVersionNumber(List.of(appearance1, appearance3),
                    approveExpenseDto.getDateToRevisions());

            doReturn(true).when(jurorExpenseService)
                .validateUserCanApprove(List.of(appearance1, appearance3));

            FinancialAuditDetails financialAuditDetails = mock(FinancialAuditDetails.class);
            doReturn(financialAuditDetails).when(financialAuditService)
                .createFinancialAuditDetail(TestConstants.VALID_JUROR_NUMBER,
                    TestConstants.VALID_COURT_LOCATION,
                    FinancialAuditDetails.Type.APPROVED_BACS,
                    List.of(appearance1, appearance3));

            PaymentData paymentData = mock(PaymentData.class);
            doReturn(paymentData).when(jurorExpenseService).createPaymentData(
                TestConstants.VALID_JUROR_NUMBER,
                courtLocation,
                List.of(appearance1, appearance3)
            );

            doNothing().when(jurorExpenseService).approveAppearance(any());
            doNothing().when(jurorExpenseService).saveAppearancesWithExpenseRateIdUpdate(any());
            jurorExpenseService.approveExpenses(
                TestConstants.VALID_COURT_LOCATION,
                PaymentMethod.BACS,
                approveExpenseDto);


            verify(appearanceRepository, times(1))
                .findAllByCourtLocationLocCodeAndJurorNumber(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER);
            verify(jurorExpenseService, times(1))
                .validateAppearanceVersionNumber(List.of(appearance1, appearance3),
                    approveExpenseDto.getDateToRevisions());

            verify(financialAuditService, times(1))
                .createFinancialAuditDetail(TestConstants.VALID_JUROR_NUMBER,
                    TestConstants.VALID_COURT_LOCATION,
                    FinancialAuditDetails.Type.APPROVED_BACS,
                    List.of(appearance1, appearance3));
            verify(jurorExpenseService, times(1))
                .saveAppearancesWithExpenseRateIdUpdate(List.of(appearance1, appearance3));
            verify(jurorHistoryService, times(1))
                .createExpenseApproveBacs(TestConstants.VALID_JUROR_NUMBER,
                    financialAuditDetails,
                    LocalDate.of(2023, 1, 2),
                    new BigDecimal("4.02"));

            verify(paymentDataRepository, times(1))
                .save(paymentData);
            verify(jurorExpenseService, times(1)).createPaymentData(
                TestConstants.VALID_JUROR_NUMBER,
                courtLocation,
                List.of(appearance1, appearance3)
            );
            verify(jurorExpenseService, times(1))
                .approveAppearance(appearance1);
            verify(jurorExpenseService, times(1))
                .approveAppearance(appearance3);
        }

        @Test
        void positiveTypicalCash() {
            User user = mockUser("testUser");
            when(user.getApprovalLimit()).thenReturn(new BigDecimal("1000.00"));
            ApproveExpenseDto.ApprovalType approvalType = mock(ApproveExpenseDto.ApprovalType.class);
            ApproveExpenseDto approveExpenseDto = ApproveExpenseDto.builder()
                .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                .approvalType(approvalType)
                .dateToRevisions(List.of(
                    ApproveExpenseDto.DateToRevision.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 1))
                        .version(1L)
                        .build(),
                    ApproveExpenseDto.DateToRevision.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 2))
                        .version(2L)
                        .build()
                ))
                .build();

            CourtLocation courtLocation = mock(CourtLocation.class);
            doReturn(TestConstants.VALID_COURT_LOCATION).when(courtLocation).getOwner();
            doReturn(TestConstants.VALID_COURT_LOCATION).when(courtLocation).getLocCode();
            Appearance appearance1 = mockAppearance(courtLocation, true);
            Appearance appearance2 = mockAppearance(courtLocation, false);
            Appearance appearance3 = mockAppearance(courtLocation, true);
            doReturn(true).when(approvalType).isApplicable(appearance1);
            doReturn(new BigDecimal("1.01")).when(appearance1).getTotalChanged();
            doReturn(true).when(approvalType).isApplicable(appearance2);
            doReturn(new BigDecimal("2.01")).when(appearance2).getTotalChanged();
            doReturn(true).when(approvalType).isApplicable(appearance3);
            doReturn(new BigDecimal("3.01")).when(appearance3).getTotalChanged();
            doReturn(FinancialAuditDetails.Type.APPROVED_CASH).when(approvalType).toApproveType(anyBoolean());
            doReturn(LocalDate.of(2023, 1, 2)).when(appearance1).getAttendanceDate();
            doReturn(LocalDate.of(2023, 1, 1)).when(appearance3).getAttendanceDate();

            List<Appearance> appearances = List.of(appearance1, appearance2, appearance3);

            doReturn(appearances).when(appearanceRepository)
                .findAllByCourtLocationLocCodeAndJurorNumber(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER);

            doReturn(true).when(jurorExpenseService)
                .validateAppearanceVersionNumber(List.of(appearance1, appearance3),
                    approveExpenseDto.getDateToRevisions());

            doReturn(true).when(jurorExpenseService)
                .validateUserCanApprove(List.of(appearance1, appearance3));

            FinancialAuditDetails financialAuditDetails = mock(FinancialAuditDetails.class);
            doReturn(financialAuditDetails).when(financialAuditService)
                .createFinancialAuditDetail(TestConstants.VALID_JUROR_NUMBER,
                    TestConstants.VALID_COURT_LOCATION,
                    FinancialAuditDetails.Type.APPROVED_CASH,
                    List.of(appearance1, appearance3));
            doNothing().when(jurorExpenseService).approveAppearance(any());
            doNothing().when(jurorExpenseService).saveAppearancesWithExpenseRateIdUpdate(any());

            jurorExpenseService.approveExpenses(
                TestConstants.VALID_COURT_LOCATION,
                PaymentMethod.CASH,
                approveExpenseDto);


            verify(appearanceRepository, times(1))
                .findAllByCourtLocationLocCodeAndJurorNumber(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER);
            verify(jurorExpenseService, times(1))
                .validateAppearanceVersionNumber(List.of(appearance1, appearance3),
                    approveExpenseDto.getDateToRevisions());

            verify(financialAuditService, times(1))
                .createFinancialAuditDetail(TestConstants.VALID_JUROR_NUMBER,
                    TestConstants.VALID_COURT_LOCATION,
                    FinancialAuditDetails.Type.APPROVED_CASH,
                    List.of(appearance1, appearance3));
            verify(jurorExpenseService, times(1))
                .saveAppearancesWithExpenseRateIdUpdate(List.of(appearance1, appearance3));
            verify(jurorHistoryService, times(1))
                .createExpenseApproveCash(TestConstants.VALID_JUROR_NUMBER,
                    financialAuditDetails,
                    LocalDate.of(2023, 1, 2),
                    new BigDecimal("4.02"));
            verify(jurorExpenseService, times(1))
                .approveAppearance(appearance1);
            verify(jurorExpenseService, times(1))
                .approveAppearance(appearance3);
        }

        @Test
        void negativeNoAppearances() {
            ApproveExpenseDto.ApprovalType approvalType = mock(ApproveExpenseDto.ApprovalType.class);
            doReturn("EXAMPLE_APPROVAL_TYPE").when(approvalType).name();
            ApproveExpenseDto approveExpenseDto = ApproveExpenseDto.builder()
                .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                .approvalType(approvalType)
                .dateToRevisions(List.of(
                    ApproveExpenseDto.DateToRevision.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 1))
                        .version(1L)
                        .build(),
                    ApproveExpenseDto.DateToRevision.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 2))
                        .version(2L)
                        .build()
                ))
                .build();
            doReturn(List.of()).when(appearanceRepository)
                .findAllByCourtLocationLocCodeAndJurorNumber(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER);

            MojException.NotFound exception =
                assertThrows(MojException.NotFound.class,
                    () -> jurorExpenseService.approveExpenses(
                        TestConstants.VALID_COURT_LOCATION,
                        PaymentMethod.CASH,
                        approveExpenseDto),
                    "Expect exception to be thrown when no appearances are found");

            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage())
                .isEqualTo(
                    "No appearance records found for Loc code: 415, Juror Number: 123456789 and approval type "
                        + "EXAMPLE_APPROVAL_TYPE");

            verifyNoInteractions(financialAuditDetailsRepository);
            verify(appearanceRepository, never())
                .saveAll(any());
        }

        @Test
        void negativeAppearancesFoundButWrongOwner() {
            ApproveExpenseDto.ApprovalType approvalType = mock(ApproveExpenseDto.ApprovalType.class);
            doReturn("EXAMPLE_APPROVAL_TYPE").when(approvalType).name();
            ApproveExpenseDto approveExpenseDto = ApproveExpenseDto.builder()
                .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                .approvalType(approvalType)
                .dateToRevisions(List.of(
                    ApproveExpenseDto.DateToRevision.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 1))
                        .version(1L)
                        .build(),
                    ApproveExpenseDto.DateToRevision.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 2))
                        .version(2L)
                        .build()
                ))
                .build();

            CourtLocation courtLocation = mock(CourtLocation.class);
            doReturn("414").when(courtLocation).getLocCode();
            doReturn("414").when(courtLocation).getOwner();
            Appearance appearance1 = mockAppearance(courtLocation, false);
            Appearance appearance2 = mockAppearance(courtLocation, false);
            Appearance appearance3 = mockAppearance(courtLocation, false);
            doReturn(true).when(approvalType).isApplicable(appearance1);
            doReturn(true).when(approvalType).isApplicable(appearance2);
            doReturn(true).when(approvalType).isApplicable(appearance3);
            List<Appearance> appearances = List.of(appearance1, appearance2, appearance3);

            doReturn(appearances).when(appearanceRepository)
                .findAllByCourtLocationLocCodeAndJurorNumber(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER);
            MojException.NotFound exception =
                assertThrows(MojException.NotFound.class,
                    () -> jurorExpenseService.approveExpenses(
                        TestConstants.VALID_COURT_LOCATION,
                        PaymentMethod.CASH,
                        approveExpenseDto),
                    "Expect exception to be thrown when no appearances are found");

            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage())
                .isEqualTo(
                    "No appearance records found for Loc code: 415, Juror Number: 123456789 and approval type "
                        + "EXAMPLE_APPROVAL_TYPE");

            verifyNoInteractions(financialAuditDetailsRepository);
            verify(appearanceRepository, never())
                .saveAll(any());
        }

        @Test
        void negativeAppearancesFoundButWrongCashType() {
            ApproveExpenseDto.ApprovalType approvalType = mock(ApproveExpenseDto.ApprovalType.class);
            doReturn("EXAMPLE_APPROVAL_TYPE").when(approvalType).name();
            ApproveExpenseDto approveExpenseDto = ApproveExpenseDto.builder()
                .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                .approvalType(approvalType)
                .dateToRevisions(List.of(
                    ApproveExpenseDto.DateToRevision.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 1))
                        .version(1L)
                        .build(),
                    ApproveExpenseDto.DateToRevision.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 2))
                        .version(2L)
                        .build()
                ))
                .build();

            CourtLocation courtLocation = mock(CourtLocation.class);
            doReturn(TestConstants.VALID_COURT_LOCATION).when(courtLocation).getOwner();
            doReturn(TestConstants.VALID_COURT_LOCATION).when(courtLocation).getLocCode();
            Appearance appearance1 = mockAppearance(courtLocation, false);
            Appearance appearance2 = mockAppearance(courtLocation, false);
            Appearance appearance3 = mockAppearance(courtLocation, false);
            doReturn(true).when(approvalType).isApplicable(appearance1);
            doReturn(true).when(approvalType).isApplicable(appearance2);
            doReturn(true).when(approvalType).isApplicable(appearance3);
            List<Appearance> appearances = List.of(appearance1, appearance2, appearance3);

            doReturn(appearances).when(appearanceRepository)
                .findAllByCourtLocationLocCodeAndJurorNumber(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER);
            MojException.NotFound exception =
                assertThrows(MojException.NotFound.class,
                    () -> jurorExpenseService.approveExpenses(
                        TestConstants.VALID_COURT_LOCATION,
                        PaymentMethod.CASH,
                        approveExpenseDto),
                    "Expect exception to be thrown when no appearances are found");

            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage())
                .isEqualTo(
                    "No appearance records found for Loc code: 415, Juror Number: 123456789 and approval type "
                        + "EXAMPLE_APPROVAL_TYPE");

            verifyNoInteractions(financialAuditDetailsRepository);
            verify(appearanceRepository, never())
                .saveAll(any());

        }

        @Test
        void negativeAppearancesFoundButNotApplicable() {
            ApproveExpenseDto.ApprovalType approvalType = mock(ApproveExpenseDto.ApprovalType.class);
            doReturn("EXAMPLE_APPROVAL_TYPE").when(approvalType).name();
            ApproveExpenseDto approveExpenseDto = ApproveExpenseDto.builder()
                .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                .approvalType(approvalType)
                .dateToRevisions(List.of(
                    ApproveExpenseDto.DateToRevision.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 1))
                        .version(1L)
                        .build(),
                    ApproveExpenseDto.DateToRevision.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 2))
                        .version(2L)
                        .build()
                ))
                .build();

            CourtLocation courtLocation = mock(CourtLocation.class);
            doReturn(TestConstants.VALID_COURT_LOCATION).when(courtLocation).getOwner();
            doReturn(TestConstants.VALID_COURT_LOCATION).when(courtLocation).getLocCode();
            Appearance appearance1 = mockAppearance(courtLocation, false);
            Appearance appearance2 = mockAppearance(courtLocation, false);
            Appearance appearance3 = mockAppearance(courtLocation, false);
            doReturn(false).when(approvalType).isApplicable(appearance1);
            doReturn(false).when(approvalType).isApplicable(appearance2);
            doReturn(false).when(approvalType).isApplicable(appearance3);
            List<Appearance> appearances = List.of(appearance1, appearance2, appearance3);

            doReturn(appearances).when(appearanceRepository)
                .findAllByCourtLocationLocCodeAndJurorNumber(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER);
            MojException.NotFound exception =
                assertThrows(MojException.NotFound.class,
                    () -> jurorExpenseService.approveExpenses(
                        TestConstants.VALID_COURT_LOCATION,
                        PaymentMethod.BACS,
                        approveExpenseDto),
                    "Expect exception to be thrown when no appearances are found");

            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage())
                .isEqualTo(
                    "No appearance records found for Loc code: 415, Juror Number: 123456789 and approval type "
                        + "EXAMPLE_APPROVAL_TYPE");

            verifyNoInteractions(financialAuditDetailsRepository);
            verify(appearanceRepository, never())
                .saveAll(any());

        }

        @Test
        void negativeExpenseTotalAboveUsersLimit() {
            User user = mockUser("testUser2");
            when(user.getApprovalLimit()).thenReturn(new BigDecimal("123.45"));
            ApproveExpenseDto.ApprovalType approvalType = mock(ApproveExpenseDto.ApprovalType.class);
            ApproveExpenseDto approveExpenseDto = ApproveExpenseDto.builder()
                .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                .approvalType(approvalType)
                .dateToRevisions(List.of(
                    ApproveExpenseDto.DateToRevision.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 1))
                        .version(1L)
                        .build(),
                    ApproveExpenseDto.DateToRevision.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 2))
                        .version(2L)
                        .build()
                ))
                .build();

            CourtLocation courtLocation = mock(CourtLocation.class);
            doReturn(TestConstants.VALID_COURT_LOCATION).when(courtLocation).getOwner();
            doReturn(TestConstants.VALID_COURT_LOCATION).when(courtLocation).getLocCode();
            Appearance appearance1 = mockAppearance(courtLocation, false);
            Appearance appearance2 = mockAppearance(courtLocation, false);
            Appearance appearance3 = mockAppearance(courtLocation, false);
            doReturn(true).when(approvalType).isApplicable(appearance1);
            doReturn(false).when(approvalType).isApplicable(appearance2);
            doReturn(true).when(approvalType).isApplicable(appearance3);

            doReturn(new BigDecimal("103.45")).when(appearance1).getTotalChanged();
            doReturn(new BigDecimal("20.01")).when(appearance3).getTotalChanged();

            List<Appearance> appearances = List.of(appearance1, appearance2, appearance3);

            doReturn(appearances).when(appearanceRepository)
                .findAllByCourtLocationLocCodeAndJurorNumber(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER);

            doReturn(true).when(jurorExpenseService)
                .validateAppearanceVersionNumber(List.of(appearance1, appearance3),
                    approveExpenseDto.getDateToRevisions());

            doReturn(true).when(jurorExpenseService)
                .validateUserCanApprove(List.of(appearance1, appearance3));


            MojException.BusinessRuleViolation exception =
                assertThrows(MojException.BusinessRuleViolation.class,
                    () -> jurorExpenseService.approveExpenses(
                        TestConstants.VALID_COURT_LOCATION,
                        PaymentMethod.BACS,
                        approveExpenseDto),
                    "Expect exception to be thrown when user tries to approve his own expense");

            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getErrorCode()).isEqualTo(CAN_NOT_APPROVE_MORE_THAN_LIMIT);
            assertThat(exception.getMessage()).isEqualTo("User cannot approve expenses over £123.45");

            verifyNoInteractions(financialAuditDetailsRepository);
            verify(appearanceRepository, never())
                .saveAll(any());
        }

        @Test
        void negativeDataOutOfDate() {
            ApproveExpenseDto.ApprovalType approvalType = mock(ApproveExpenseDto.ApprovalType.class);
            ApproveExpenseDto approveExpenseDto = ApproveExpenseDto.builder()
                .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                .approvalType(approvalType)
                .dateToRevisions(List.of(
                    ApproveExpenseDto.DateToRevision.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 1))
                        .version(1L)
                        .build(),
                    ApproveExpenseDto.DateToRevision.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 2))
                        .version(2L)
                        .build()
                ))
                .build();

            CourtLocation courtLocation = mock(CourtLocation.class);
            doReturn(TestConstants.VALID_COURT_LOCATION).when(courtLocation).getOwner();
            doReturn(TestConstants.VALID_COURT_LOCATION).when(courtLocation).getLocCode();
            Appearance appearance1 = mockAppearance(courtLocation, false);
            Appearance appearance2 = mockAppearance(courtLocation, false);
            Appearance appearance3 = mockAppearance(courtLocation, false);
            doReturn(true).when(approvalType).isApplicable(appearance1);
            doReturn(false).when(approvalType).isApplicable(appearance2);
            doReturn(true).when(approvalType).isApplicable(appearance3);

            List<Appearance> appearances = List.of(appearance1, appearance2, appearance3);

            doReturn(appearances).when(appearanceRepository)
                .findAllByCourtLocationLocCodeAndJurorNumber(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER);

            doReturn(false).when(jurorExpenseService)
                .validateAppearanceVersionNumber(List.of(appearance1, appearance3),
                    approveExpenseDto.getDateToRevisions());


            MojException.BusinessRuleViolation exception =
                assertThrows(MojException.BusinessRuleViolation.class,
                    () -> jurorExpenseService.approveExpenses(
                        TestConstants.VALID_COURT_LOCATION,
                        PaymentMethod.BACS,
                        approveExpenseDto),
                    "Expect exception to be thrown when user tries to approve his own expense");

            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getErrorCode()).isEqualTo(DATA_OUT_OF_DATE);
            assertThat(exception.getMessage()).isEqualTo("Revisions do not align");

            verifyNoInteractions(financialAuditDetailsRepository);
            verify(appearanceRepository, never())
                .saveAll(any());

        }


        @Test
        void negativeCanNotApproveOwnEdit() {
            ApproveExpenseDto.ApprovalType approvalType = mock(ApproveExpenseDto.ApprovalType.class);
            ApproveExpenseDto approveExpenseDto = ApproveExpenseDto.builder()
                .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                .approvalType(approvalType)
                .dateToRevisions(List.of(
                    ApproveExpenseDto.DateToRevision.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 1))
                        .version(1L)
                        .build(),
                    ApproveExpenseDto.DateToRevision.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 2))
                        .version(2L)
                        .build()
                ))
                .build();

            CourtLocation courtLocation = mock(CourtLocation.class);
            doReturn(TestConstants.VALID_COURT_LOCATION).when(courtLocation).getOwner();
            doReturn(TestConstants.VALID_COURT_LOCATION).when(courtLocation).getLocCode();
            Appearance appearance1 = mockAppearance(courtLocation, false);
            Appearance appearance2 = mockAppearance(courtLocation, false);
            Appearance appearance3 = mockAppearance(courtLocation, false);
            doReturn(true).when(approvalType).isApplicable(appearance1);
            doReturn(false).when(approvalType).isApplicable(appearance2);
            doReturn(true).when(approvalType).isApplicable(appearance3);

            List<Appearance> appearances = List.of(appearance1, appearance2, appearance3);

            doReturn(appearances).when(appearanceRepository)
                .findAllByCourtLocationLocCodeAndJurorNumber(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER);

            doReturn(true).when(jurorExpenseService)
                .validateAppearanceVersionNumber(List.of(appearance1, appearance3),
                    approveExpenseDto.getDateToRevisions());

            doReturn(false).when(jurorExpenseService)
                .validateUserCanApprove(List.of(appearance1, appearance3));


            MojException.BusinessRuleViolation exception =
                assertThrows(MojException.BusinessRuleViolation.class,
                    () -> jurorExpenseService.approveExpenses(
                        TestConstants.VALID_COURT_LOCATION,
                        PaymentMethod.BACS,
                        approveExpenseDto),
                    "Expect exception to be thrown when user tries to approve his own expense");

            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getErrorCode()).isEqualTo(CAN_NOT_APPROVE_OWN_EDIT);
            assertThat(exception.getMessage()).isEqualTo("User cannot approve an expense they have edited");

            verifyNoInteractions(financialAuditDetailsRepository);
            verify(appearanceRepository, never())
                .saveAll(any());
        }

    }

    @Nested
    @DisplayName("Juror getJuror(String jurorNumber)")
    class GetJuror {

        @Test
        void positiveJurorFound() {
            Juror juror = mock(Juror.class);
            doReturn(juror).when(jurorRepository).findByJurorNumber(TestConstants.VALID_JUROR_NUMBER);
            assertThat(jurorExpenseService.getJuror(TestConstants.VALID_JUROR_NUMBER)).isEqualTo(juror);
        }

        @Test
        void negativeJurorNotFound() {
            doReturn(null).when(jurorRepository).findByJurorNumber(TestConstants.VALID_JUROR_NUMBER);
            MojException.NotFound exception = assertThrows(MojException.NotFound.class,
                () -> jurorExpenseService.getJuror(TestConstants.VALID_JUROR_NUMBER),
                "Should throw exception when juror is not found");
            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage()).isEqualTo("Juror not found: 123456789");
        }
    }

    @Nested
    @DisplayName("void approveAppearance(Appearance appearance)")
    class ApproveAppearance {
        @Test
        void positiveTypical() {
            Appearance appearance = new Appearance();
            appearance.setPublicTransportDue(new BigDecimal("1.01"));
            appearance.setPublicTransportPaid(new BigDecimal("0.01"));
            appearance.setHiredVehicleDue(new BigDecimal("2.01"));
            appearance.setHiredVehiclePaid(new BigDecimal("0.02"));
            appearance.setMotorcycleDue(new BigDecimal("3.01"));
            appearance.setMotorcyclePaid(new BigDecimal("0.01"));
            appearance.setCarDue(new BigDecimal("4.01"));
            appearance.setCarPaid(new BigDecimal("0.01"));
            appearance.setBicycleDue(new BigDecimal("5.01"));
            appearance.setBicyclePaid(new BigDecimal("0.01"));
            appearance.setParkingDue(new BigDecimal("6.01"));
            appearance.setParkingPaid(new BigDecimal("0.01"));
            appearance.setChildcareDue(new BigDecimal("7.01"));
            appearance.setChildcarePaid(new BigDecimal("0.01"));
            appearance.setMiscAmountDue(new BigDecimal("8.01"));
            appearance.setMiscAmountPaid(new BigDecimal("0.01"));
            appearance.setLossOfEarningsDue(new BigDecimal("9.01"));
            appearance.setLossOfEarningsPaid(new BigDecimal("0.01"));
            appearance.setSubsistenceDue(new BigDecimal("10.01"));
            appearance.setSubsistencePaid(new BigDecimal("0.01"));
            appearance.setSmartCardAmountDue(new BigDecimal("11.01"));
            appearance.setSmartCardAmountPaid(new BigDecimal("0.01"));

            jurorExpenseService.approveAppearance(appearance);

            assertThat(appearance.getAppearanceStage()).isEqualTo(AppearanceStage.EXPENSE_AUTHORISED);
            assertThat(appearance.getPublicTransportPaid()).isEqualTo(new BigDecimal("1.01"));
            assertThat(appearance.getHiredVehiclePaid()).isEqualTo(new BigDecimal("2.01"));
            assertThat(appearance.getMotorcyclePaid()).isEqualTo(new BigDecimal("3.01"));
            assertThat(appearance.getCarPaid()).isEqualTo(new BigDecimal("4.01"));
            assertThat(appearance.getBicyclePaid()).isEqualTo(new BigDecimal("5.01"));
            assertThat(appearance.getParkingPaid()).isEqualTo(new BigDecimal("6.01"));
            assertThat(appearance.getChildcarePaid()).isEqualTo(new BigDecimal("7.01"));
            assertThat(appearance.getMiscAmountPaid()).isEqualTo(new BigDecimal("8.01"));
            assertThat(appearance.getLossOfEarningsPaid()).isEqualTo(new BigDecimal("9.01"));
            assertThat(appearance.getSubsistencePaid()).isEqualTo(new BigDecimal("10.01"));
            assertThat(appearance.getSmartCardAmountPaid()).isEqualTo(new BigDecimal("11.01"));
        }
    }

    @Nested
    @DisplayName("PaymentData createPaymentData(String jurorNumber, CourtLocation courtLocation,\n"
        + "                                  List<Appearance> appearances)")
    class CreatePaymentData {
        @Test
        void positiveTypical() {
            Appearance appearance1 = mock(Appearance.class);
            doReturn(new BigDecimal("1.01")).when(appearance1).getTravelTotalChanged();
            doReturn(new BigDecimal("2.01")).when(appearance1).getSubsistenceTotalChanged();
            doReturn(new BigDecimal("3.01")).when(appearance1).getFinancialLossTotalChanged();
            Appearance appearance2 = mock(Appearance.class);
            doReturn(new BigDecimal("4.01")).when(appearance2).getTravelTotalChanged();
            doReturn(new BigDecimal("5.01")).when(appearance2).getSubsistenceTotalChanged();
            doReturn(new BigDecimal("6.01")).when(appearance2).getFinancialLossTotalChanged();

            Juror juror = mock(Juror.class);
            doReturn(juror).when(jurorExpenseService).getJuror(TestConstants.VALID_JUROR_NUMBER);
            CourtLocation courtLocation = mock(CourtLocation.class);

            when(juror.getSortCode()).thenReturn("112233");
            when(juror.getBankAccountName()).thenReturn("bankAccountName");
            when(juror.getBankAccountNumber()).thenReturn("123456789");
            when(juror.getSortCode()).thenReturn("112233");
            when(juror.getBuildingSocietyRollNumber()).thenReturn("buildSocNum");
            when(juror.getAddressLine1()).thenReturn("address1");
            when(juror.getAddressLine2()).thenReturn("address2");
            when(juror.getAddressLine3()).thenReturn("address3");
            when(juror.getAddressLine4()).thenReturn("address4");
            when(juror.getAddressLine5()).thenReturn("address5");
            when(juror.getPostcode()).thenReturn("postCode12");

            when(applicationSettingService.getAppSetting(ApplicationSettings.Setting.PAYMENT_AUTH_CODE))
                .thenReturn(Optional.of(new ApplicationSettings(ApplicationSettings.Setting.PAYMENT_AUTH_CODE,
                    "AuthCode321")));

            when(juror.getName()).thenReturn("Jurorname asd");
            when(courtLocation.getCostCentre()).thenReturn("costCenter312");
            PaymentData paymentData = jurorExpenseService.createPaymentData(
                TestConstants.VALID_JUROR_NUMBER,
                courtLocation,
                List.of(appearance1, appearance2)
            );

            assertThat(paymentData.getCourtLocation()).isEqualTo(courtLocation);
            assertThat(paymentData.getCreationDate()).isEqualToIgnoringHours(LocalDateTime.now());
            assertThat(paymentData.getExpenseTotal()).isEqualTo(new BigDecimal("21.06"));
            assertThat(paymentData.getJurorNumber()).isEqualTo(TestConstants.VALID_JUROR_NUMBER);
            assertThat(paymentData.getBankSortCode()).isEqualTo("112233");
            assertThat(paymentData.getBankAccountName()).isEqualTo("bankAccountName");
            assertThat(paymentData.getBankAccountNumber()).isEqualTo("123456789");
            assertThat(paymentData.getBuildingSocietyNumber()).isEqualTo("buildSocNum");
            assertThat(paymentData.getAddressLine1()).isEqualTo("address1");
            assertThat(paymentData.getAddressLine2()).isEqualTo("address2");
            assertThat(paymentData.getAddressLine3()).isEqualTo("address3");
            assertThat(paymentData.getAddressLine4()).isEqualTo("address4");
            assertThat(paymentData.getAddressLine5()).isEqualTo("address5");
            assertThat(paymentData.getPostcode()).isEqualTo("postCode12");
            assertThat(paymentData.getAuthCode()).isEqualTo("AuthCode321");
            assertThat(paymentData.getJurorName()).isEqualTo("Jurorname asd");
            assertThat(paymentData.getLocCostCentre()).isEqualTo("costCenter312");
            assertThat(paymentData.getTravelTotal()).isEqualTo(new BigDecimal("5.02"));
            assertThat(paymentData.getSubsistenceTotal()).isEqualTo(new BigDecimal("7.02"));
            assertThat(paymentData.getFinancialLossTotal()).isEqualTo(new BigDecimal("9.02"));
            assertThat(paymentData.getExpenseFileName()).isNull();
            assertThat(paymentData.isExtracted()).isFalse();
        }

        @Test
        void negativeAuthCodeNotFound() {
            Appearance appearance1 = mock(Appearance.class);
            doReturn(new BigDecimal("1.01")).when(appearance1).getTravelTotalChanged();
            doReturn(new BigDecimal("2.01")).when(appearance1).getSubsistenceTotalChanged();
            doReturn(new BigDecimal("3.01")).when(appearance1).getFinancialLossTotalChanged();
            Juror juror = mock(Juror.class);
            doReturn(juror).when(jurorExpenseService).getJuror(TestConstants.VALID_JUROR_NUMBER);
            CourtLocation courtLocation = mock(CourtLocation.class);

            when(applicationSettingService.getAppSetting(ApplicationSettings.Setting.PAYMENT_AUTH_CODE))
                .thenReturn(Optional.empty());
            MojException.InternalServerError exception = assertThrows(MojException.InternalServerError.class,
                () -> jurorExpenseService.createPaymentData(
                    TestConstants.VALID_JUROR_NUMBER,
                    courtLocation,
                    List.of(appearance1)
                ),
                "Expect exception to be thrown when no auth code is found");
            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage()).isEqualTo("Payment Auth Code not found in application settings");
        }
    }

    @Nested
    @DisplayName("private boolean validateUserCanApprove(List<Appearance> appearances)")
    class ValidateUserCanApprove {
        private static final String USER_NAME = "exampleUser";

        @BeforeEach
        void beforeEach() {
            securityUtilMockedStatic = mockStatic(SecurityUtil.class);
            securityUtilMockedStatic.when(SecurityUtil::getActiveLogin)
                .thenReturn(USER_NAME);
        }

        @AfterEach
        void afterEach() {
            if (securityUtilMockedStatic != null) {
                securityUtilMockedStatic.close();
            }
        }

        @Test
        void positiveTypicalTrue() {
            List<Appearance> appearances = List.of(mock(Appearance.class), mock(Appearance.class),
                mock(Appearance.class));
            FinancialAuditDetails financialAuditDetails1 = mock(FinancialAuditDetails.class);
            doReturn(mockUser("UserName1")).when(financialAuditDetails1).getCreatedBy();
            doReturn(List.of(financialAuditDetails1)).when(financialAuditService)
                .getFinancialAuditDetails(appearances.get(0));

            FinancialAuditDetails financialAuditDetails2 = mock(FinancialAuditDetails.class);
            doReturn(mockUser("UserName2")).when(financialAuditDetails2).getCreatedBy();
            doReturn(List.of(financialAuditDetails2)).when(financialAuditService)
                .getFinancialAuditDetails(appearances.get(1));

            FinancialAuditDetails financialAuditDetails3 = mock(FinancialAuditDetails.class);
            doReturn(mockUser("UserName3")).when(financialAuditDetails3).getCreatedBy();
            doReturn(List.of(financialAuditDetails1, financialAuditDetails2, financialAuditDetails3)).when(
                financialAuditService).getFinancialAuditDetails(appearances.get(2));

            assertThat(jurorExpenseService.validateUserCanApprove(appearances)).isTrue();

            verify(financialAuditService, times(1))
                .getFinancialAuditDetails(appearances.get(0));
            verify(financialAuditService, times(1))
                .getFinancialAuditDetails(appearances.get(1));
            verify(financialAuditService, times(1))
                .getFinancialAuditDetails(appearances.get(2));
            verifyNoMoreInteractions(financialAuditService);
        }

        @Test
        void positiveTypicalFalse() {
            List<Appearance> appearances = List.of(mock(Appearance.class), mock(Appearance.class),
                mock(Appearance.class));
            FinancialAuditDetails financialAuditDetails1 = mock(FinancialAuditDetails.class);
            doReturn(mockUser("UserName1")).when(financialAuditDetails1).getCreatedBy();
            doReturn(List.of(financialAuditDetails1)).when(financialAuditService)
                .getFinancialAuditDetails(appearances.get(0));

            FinancialAuditDetails financialAuditDetails2 = mock(FinancialAuditDetails.class);
            doReturn(mockUser(USER_NAME)).when(financialAuditDetails2).getCreatedBy();
            doReturn(List.of(financialAuditDetails2)).when(financialAuditService)
                .getFinancialAuditDetails(appearances.get(1));

            FinancialAuditDetails financialAuditDetails3 = mock(FinancialAuditDetails.class);
            doReturn(mockUser("UserName3")).when(financialAuditDetails3).getCreatedBy();
            doReturn(List.of(financialAuditDetails1, financialAuditDetails2, financialAuditDetails3)).when(
                financialAuditService).getFinancialAuditDetails(appearances.get(2));

            assertThat(jurorExpenseService.validateUserCanApprove(appearances)).isFalse();

            verify(financialAuditService, times(1))
                .getFinancialAuditDetails(appearances.get(0));
            verify(financialAuditService, times(1))
                .getFinancialAuditDetails(appearances.get(1));
            verify(financialAuditService, times(1))
                .getFinancialAuditDetails(appearances.get(2));
            verifyNoMoreInteractions(financialAuditService);
        }

        private Object mockUser(String username) {
            User user = mock(User.class);
            doReturn(username).when(user).getUsername();
            return user;
        }

    }

    @Nested
    @DisplayName("boolean validateAppearanceVersionNumber(List<Appearance> appearances,\n"
        + "                                            List<ApproveExpenseDto.DateToRevision> dateToRevision) {\n"
        + "    ")
    class ValidateAppearanceVersionNumber {
        private Appearance mockAppearance(long version, LocalDate attendanceDate) {
            Appearance appearance = mock(Appearance.class);
            doReturn(version).when(appearance).getVersion();
            doReturn(attendanceDate).when(appearance).getAttendanceDate();
            return appearance;
        }

        private ApproveExpenseDto.DateToRevision mockDateToRevision(long version, LocalDate attendanceDate) {
            return ApproveExpenseDto.DateToRevision.builder()
                .version(version)
                .attendanceDate(attendanceDate)
                .build();
        }

        @Test
        void positiveTypicalTrue() {
            Appearance appearance1 = mockAppearance(1L, LocalDate.of(2023, 1, 1));
            ApproveExpenseDto.DateToRevision dateToRevision1 = mockDateToRevision(1L, LocalDate.of(2023, 1, 1));
            Appearance appearance2 = mockAppearance(4L, LocalDate.of(2023, 1, 2));
            ApproveExpenseDto.DateToRevision dateToRevision2 = mockDateToRevision(4L, LocalDate.of(2023, 1, 2));
            Appearance appearance3 = mockAppearance(89L, LocalDate.of(2023, 1, 3));
            ApproveExpenseDto.DateToRevision dateToRevision3 = mockDateToRevision(89L, LocalDate.of(2023, 1, 3));

            assertThat(jurorExpenseService.validateAppearanceVersionNumber(
                List.of(appearance1, appearance2, appearance3),
                List.of(dateToRevision1, dateToRevision2, dateToRevision3)))
                .isTrue();
        }


        @Test
        void positiveTypicalFalse() {
            Appearance appearance1 = mockAppearance(1L, LocalDate.of(2023, 1, 1));
            ApproveExpenseDto.DateToRevision dateToRevision1 = mockDateToRevision(1L, LocalDate.of(2023, 1, 1));
            Appearance appearance2 = mockAppearance(4L, LocalDate.of(2023, 1, 2));
            ApproveExpenseDto.DateToRevision dateToRevision2 = mockDateToRevision(4L, LocalDate.of(2023, 1, 2));
            Appearance appearance3 = mockAppearance(88L, LocalDate.of(2023, 1, 3));
            ApproveExpenseDto.DateToRevision dateToRevision3 = mockDateToRevision(89L, LocalDate.of(2023, 1, 3));

            assertThat(jurorExpenseService.validateAppearanceVersionNumber(
                List.of(appearance1, appearance2, appearance3),
                List.of(dateToRevision1, dateToRevision2, dateToRevision3)))
                .isFalse();
        }

        @Test
        void positiveTypicalSizeMismatch() {
            assertThat(jurorExpenseService.validateAppearanceVersionNumber(
                List.of(mock(Appearance.class)),
                List.of())).isFalse();

            assertThat(jurorExpenseService.validateAppearanceVersionNumber(
                List.of(),
                List.of(mock(ApproveExpenseDto.DateToRevision.class)))).isFalse();

            assertThat(jurorExpenseService.validateAppearanceVersionNumber(
                List.of(mock(Appearance.class)),
                List.of(mock(ApproveExpenseDto.DateToRevision.class),
                    mock(ApproveExpenseDto.DateToRevision.class)))).isFalse();
        }
    }

    @Nested
    @DisplayName("public CombinedSimplifiedExpenseDetailDto getSimplifiedExpense(JurorNumberAndPoolNumberDto request,\n"
        + "                                                                   ExpenseType type)")
    class GetSimplifiedExpense {
        @BeforeEach
        void mockCurrentUser() {
            securityUtilMockedStatic = mockStatic(SecurityUtil.class);
            securityUtilMockedStatic.when(SecurityUtil::getActiveOwner)
                .thenReturn(TestConstants.VALID_COURT_LOCATION);
        }

        @AfterEach
        void afterEach() {
            if (securityUtilMockedStatic != null) {
                securityUtilMockedStatic.close();
            }
        }

        @Test
        @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
        void positiveTypical() {
            CourtLocation courtLocation = mock(CourtLocation.class);
            when(courtLocation.getOwner()).thenReturn(TestConstants.VALID_COURT_LOCATION);
            Appearance appearance1 = mock(Appearance.class);
            when(appearance1.getCourtLocation()).thenReturn(courtLocation);
            when(appearance1.getAppearanceStage()).thenReturn(AppearanceStage.EXPENSE_ENTERED);
            when(appearance1.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 3));
            SimplifiedExpenseDetailDto simplifiedExpenseDetailDto1 =
                createSimplifiedExpenseDetailDto(LocalDate.of(2023, 1, 3));
            doReturn(simplifiedExpenseDetailDto1).when(jurorExpenseService)
                .mapCombinedSimplifiedExpenseDetailDto(appearance1);

            Appearance appearance2 = mock(Appearance.class);
            when(appearance2.getCourtLocation()).thenReturn(courtLocation);
            when(appearance2.getAppearanceStage()).thenReturn(AppearanceStage.EXPENSE_ENTERED);
            when(appearance2.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 2));
            SimplifiedExpenseDetailDto simplifiedExpenseDetailDto2 =
                createSimplifiedExpenseDetailDto(LocalDate.of(2023, 1, 1));
            doReturn(simplifiedExpenseDetailDto2).when(jurorExpenseService)
                .mapCombinedSimplifiedExpenseDetailDto(appearance2);


            Appearance appearance3 = mock(Appearance.class);
            when(appearance3.getCourtLocation()).thenReturn(courtLocation);
            when(appearance3.getAppearanceStage()).thenReturn(AppearanceStage.EXPENSE_ENTERED);
            when(appearance3.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 1));
            SimplifiedExpenseDetailDto simplifiedExpenseDetailDto3 =
                createSimplifiedExpenseDetailDto(LocalDate.of(2023, 1, 2));
            doReturn(simplifiedExpenseDetailDto3).when(jurorExpenseService)
                .mapCombinedSimplifiedExpenseDetailDto(appearance3);

            ExpenseType expenseType = mock(ExpenseType.class);
            doReturn(true).when(expenseType).isApplicable(appearance1);
            doReturn(false).when(expenseType).isApplicable(appearance2);
            doReturn(true).when(expenseType).isApplicable(appearance3);

            when(appearanceRepository.findAllByCourtLocationLocCodeAndJurorNumber(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER
            )).thenReturn(List.of(appearance1, appearance2, appearance3));

            CombinedSimplifiedExpenseDetailDto data = jurorExpenseService.getSimplifiedExpense(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, expenseType
            );
            assertThat(data).isNotNull();
            assertThat(data.getExpenseDetails())
                .containsExactlyInAnyOrder(simplifiedExpenseDetailDto3, simplifiedExpenseDetailDto1);

        }

        @Test
        void positiveEmptyAppearances() {

            ExpenseType expenseType = mock(ExpenseType.class);
            doReturn("TEST_EXPENSE_TYPE").when(expenseType).name();

            when(appearanceRepository.findAllByCourtLocationLocCodeAndJurorNumber(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER
            )).thenReturn(List.of());

            CombinedSimplifiedExpenseDetailDto result = jurorExpenseService.getSimplifiedExpense(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, expenseType
            );
            assertThat(result).isNotNull();
            assertThat(result.getExpenseDetails()).hasSize(0);

            verify(appearanceRepository, times(1))
                .findAllByCourtLocationLocCodeAndJurorNumber(
                    TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER
                );
        }

        @Test
        void positiveAppearancesNotApplicable() {
            CourtLocation courtLocation = mock(CourtLocation.class);
            when(courtLocation.getOwner()).thenReturn("414");
            Appearance appearance1 = mock(Appearance.class);
            when(appearance1.getCourtLocation()).thenReturn(courtLocation);
            when(appearance1.getAppearanceStage()).thenReturn(AppearanceStage.EXPENSE_ENTERED);
            when(appearance1.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 3));
            SimplifiedExpenseDetailDto simplifiedExpenseDetailDto1 =
                createSimplifiedExpenseDetailDto(LocalDate.of(2023, 1, 3));
            doReturn(simplifiedExpenseDetailDto1).when(jurorExpenseService)
                .mapCombinedSimplifiedExpenseDetailDto(appearance1);

            Appearance appearance2 = mock(Appearance.class);
            when(appearance2.getCourtLocation()).thenReturn(courtLocation);
            when(appearance2.getAppearanceStage()).thenReturn(AppearanceStage.EXPENSE_ENTERED);
            when(appearance2.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 1));
            SimplifiedExpenseDetailDto simplifiedExpenseDetailDto2 =
                createSimplifiedExpenseDetailDto(LocalDate.of(2023, 1, 1));
            doReturn(simplifiedExpenseDetailDto2).when(jurorExpenseService)
                .mapCombinedSimplifiedExpenseDetailDto(appearance2);


            Appearance appearance3 = mock(Appearance.class);
            when(appearance3.getCourtLocation()).thenReturn(courtLocation);
            when(appearance3.getAppearanceStage()).thenReturn(AppearanceStage.EXPENSE_ENTERED);
            when(appearance3.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 2));
            SimplifiedExpenseDetailDto simplifiedExpenseDetailDto3 =
                createSimplifiedExpenseDetailDto(LocalDate.of(2023, 1, 2));
            doReturn(simplifiedExpenseDetailDto3).when(jurorExpenseService)
                .mapCombinedSimplifiedExpenseDetailDto(appearance3);

            ExpenseType expenseType = mock(ExpenseType.class);
            doReturn("TEST_EXPENSE_TYPE").when(expenseType).name();
            doReturn(false).when(expenseType).isApplicable(appearance1);
            doReturn(false).when(expenseType).isApplicable(appearance2);
            doReturn(false).when(expenseType).isApplicable(appearance3);

            when(appearanceRepository.findAllByCourtLocationLocCodeAndJurorNumber(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER
            )).thenReturn(List.of(appearance1, appearance2, appearance3));

            CombinedSimplifiedExpenseDetailDto result = jurorExpenseService.getSimplifiedExpense(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, expenseType
            );
            assertThat(result).isNotNull();
            assertThat(result.getExpenseDetails()).hasSize(0);

            verify(appearanceRepository, times(1))
                .findAllByCourtLocationLocCodeAndJurorNumber(
                    TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER
                );
        }

        @Test
        void negativeForbiddenWrongOwner() {
            CourtLocation courtLocation = mock(CourtLocation.class);
            when(courtLocation.getOwner()).thenReturn("414");
            Appearance appearance1 = mock(Appearance.class);
            when(appearance1.getCourtLocation()).thenReturn(courtLocation);
            when(appearance1.getAppearanceStage()).thenReturn(AppearanceStage.EXPENSE_ENTERED);
            when(appearance1.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 3));
            SimplifiedExpenseDetailDto simplifiedExpenseDetailDto1 =
                createSimplifiedExpenseDetailDto(LocalDate.of(2023, 1, 3));
            doReturn(simplifiedExpenseDetailDto1).when(jurorExpenseService)
                .mapCombinedSimplifiedExpenseDetailDto(appearance1);

            Appearance appearance2 = mock(Appearance.class);
            when(appearance2.getCourtLocation()).thenReturn(courtLocation);
            when(appearance2.getAppearanceStage()).thenReturn(AppearanceStage.EXPENSE_ENTERED);
            when(appearance2.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 1));
            SimplifiedExpenseDetailDto simplifiedExpenseDetailDto2 =
                createSimplifiedExpenseDetailDto(LocalDate.of(2023, 1, 1));
            doReturn(simplifiedExpenseDetailDto2).when(jurorExpenseService)
                .mapCombinedSimplifiedExpenseDetailDto(appearance2);


            Appearance appearance3 = mock(Appearance.class);
            when(appearance3.getCourtLocation()).thenReturn(courtLocation);
            when(appearance3.getAppearanceStage()).thenReturn(AppearanceStage.EXPENSE_ENTERED);
            when(appearance3.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 2));
            SimplifiedExpenseDetailDto simplifiedExpenseDetailDto3 =
                createSimplifiedExpenseDetailDto(LocalDate.of(2023, 1, 2));
            doReturn(simplifiedExpenseDetailDto3).when(jurorExpenseService)
                .mapCombinedSimplifiedExpenseDetailDto(appearance3);

            ExpenseType expenseType = mock(ExpenseType.class);
            doReturn(true).when(expenseType).isApplicable(appearance1);
            doReturn(true).when(expenseType).isApplicable(appearance2);
            doReturn(true).when(expenseType).isApplicable(appearance3);

            when(appearanceRepository.findAllByCourtLocationLocCodeAndJurorNumber(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER
            )).thenReturn(List.of(appearance1, appearance2, appearance3));

            MojException.Forbidden exception = assertThrows(MojException.Forbidden.class,
                () -> jurorExpenseService.getSimplifiedExpense(
                    TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, expenseType
                ),
                "Should throw exception when juror tries to access a pool they do not have access to");
            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage())
                .isEqualTo("User cannot access this juror pool");
        }

        private SimplifiedExpenseDetailDto createSimplifiedExpenseDetailDto(LocalDate date) {
            return SimplifiedExpenseDetailDto.builder()
                .attendanceDate(date)
                .financialLoss(new BigDecimal("1.1"))
                .travel(new BigDecimal("2.2"))
                .foodAndDrink(new BigDecimal("3.3"))
                .smartcard(new BigDecimal("4.4"))
                .totalDue(new BigDecimal("5.5"))
                .totalPaid(new BigDecimal("6.6"))
                .balanceToPay(new BigDecimal("7.7"))
                .build();
        }


    }

    @Nested
    @DisplayName("private SimplifiedExpenseDetailDto mapCombinedSimplifiedExpenseDetailDto(Appearance appearance)")
    class MapCombinedSimplifiedExpenseDetailDto {
        @Test
        void positiveTypical() {
            FinancialAuditDetails financialAuditDetails = new FinancialAuditDetails();
            financialAuditDetails.setId(123L);
            financialAuditDetails.setCreatedOn(LocalDateTime.now());
            Appearance appearance = mock(Appearance.class);
            when(appearance.getFinancialAuditDetails()).thenReturn(financialAuditDetails);
            when(appearance.getAttendanceDate()).thenReturn(LocalDate.now());
            when(appearance.getAttendanceType()).thenReturn(AttendanceType.FULL_DAY);
            when(appearance.getTotalFinancialLossDue()).thenReturn(new BigDecimal("12.2"));
            when(appearance.getTotalTravelDue()).thenReturn(new BigDecimal("12.3"));
            when(appearance.getSubsistenceDue()).thenReturn(new BigDecimal("12.4"));
            when(appearance.getSmartCardAmountDue()).thenReturn(new BigDecimal("12.5"));
            when(appearance.getTotalDue()).thenReturn(new BigDecimal("12.6"));
            when(appearance.getTotalPaid()).thenReturn(new BigDecimal("12.7"));
            when(appearance.getBalanceToPay()).thenReturn(new BigDecimal("12.8"));

            SimplifiedExpenseDetailDto data = jurorExpenseService.mapCombinedSimplifiedExpenseDetailDto(appearance);

            assertThat(data).isNotNull();
            assertThat(data.getAttendanceDate()).isEqualTo(appearance.getAttendanceDate());
            assertThat(data.getFinancialAuditNumber()).isEqualTo("F123");
            assertThat(data.getAttendanceType()).isEqualTo(AttendanceType.FULL_DAY);
            assertThat(data.getFinancialLoss()).isEqualTo(new BigDecimal("12.2"));
            assertThat(data.getTravel()).isEqualTo(new BigDecimal("12.3"));
            assertThat(data.getFoodAndDrink()).isEqualTo(new BigDecimal("12.4"));
            assertThat(data.getSmartcard()).isEqualTo(new BigDecimal("12.5"));
            assertThat(data.getTotalDue()).isEqualTo(new BigDecimal("12.6"));
            assertThat(data.getTotalPaid()).isEqualTo(new BigDecimal("12.7"));
            assertThat(data.getBalanceToPay()).isEqualTo(new BigDecimal("12.8"));
            assertThat(data.getAuditCreatedOn()).isEqualTo(financialAuditDetails.getCreatedOn());
        }
    }

    @Nested
    @DisplayName(
        "public CombinedExpenseDetailsDto<ExpenseDetailsDto> getExpenses(String jurorNumber, String poolNumber, "
            + "List<LocalDate> dates)")
    class GetExpensesByDates {

        @Test
        void positiveTypical() {
            List<LocalDate> localDates = List.of(mock(LocalDate.class), mock(LocalDate.class), mock(LocalDate.class));

            List<Appearance> appearances = List.of(mock(Appearance.class), mock(Appearance.class));
            when(appearanceRepository.findAllByCourtLocationLocCodeAndJurorNumberAndAttendanceDateIn(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, localDates))
                .thenReturn(appearances);
            CombinedExpenseDetailsDto<ExpenseDetailsDto> result = mock(CombinedExpenseDetailsDto.class);
            doReturn(result).when(jurorExpenseService).getExpenses(appearances);
            when(result.getExpenseDetails()).thenReturn(List.of(mock(ExpenseDetailsDto.class),
                mock(ExpenseDetailsDto.class), mock(ExpenseDetailsDto.class)));

            assertThat(jurorExpenseService.getExpenses(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, localDates))
                .isEqualTo(result);

            verify(appearanceRepository, times(1))
                .findAllByCourtLocationLocCodeAndJurorNumberAndAttendanceDateIn(
                    TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, localDates);
            verify(jurorExpenseService, times(1)).getExpenses(appearances);
        }

        @Test
        void negativeWrongSize() {
            List<LocalDate> localDates = List.of(mock(LocalDate.class), mock(LocalDate.class), mock(LocalDate.class));

            List<Appearance> appearances = List.of(mock(Appearance.class), mock(Appearance.class));
            when(appearanceRepository.findAllByCourtLocationLocCodeAndJurorNumberAndAttendanceDateIn(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, localDates))
                .thenReturn(appearances);
            CombinedExpenseDetailsDto<ExpenseDetailsDto> result = mock(CombinedExpenseDetailsDto.class);
            doReturn(result).when(jurorExpenseService).getExpenses(appearances);
            when(result.getExpenseDetails()).thenReturn(
                List.of(mock(ExpenseDetailsDto.class), mock(ExpenseDetailsDto.class)));

            MojException.NotFound exception = assertThrows(MojException.NotFound.class,
                () -> jurorExpenseService.getExpenses(
                    TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, localDates),
                "Should throw exception when dates size is not equal to 3");
            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage()).isEqualTo("Not all dates found");
        }
    }

    @Nested
    @DisplayName("CombinedExpenseDetailsDto<ExpenseDetailsDto> getExpenses(List<Appearance> appearances)")
    class GetExpensesList {
        @BeforeEach
        void mockCurrentUser() {
            securityUtilMockedStatic = mockStatic(SecurityUtil.class);
            securityUtilMockedStatic.when(SecurityUtil::getActiveOwner)
                .thenReturn(TestConstants.VALID_COURT_LOCATION);
        }

        @AfterEach
        void afterEach() {
            if (securityUtilMockedStatic != null) {
                securityUtilMockedStatic.close();
            }
        }

        @Test
        void positiveTypical() {
            CourtLocation courtLocation = mock(CourtLocation.class);
            when(courtLocation.getOwner()).thenReturn(TestConstants.VALID_COURT_LOCATION);
            Appearance appearance1 = mock(Appearance.class);
            when(appearance1.getCourtLocation()).thenReturn(courtLocation);
            when(appearance1.getAppearanceStage()).thenReturn(AppearanceStage.EXPENSE_ENTERED);
            when(appearance1.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 3));
            ExpenseDetailsDto expenseDetailDto1 = createExpenseDetailsDto(LocalDate.of(2023, 1, 3));
            doReturn(expenseDetailDto1).when(jurorExpenseService).mapAppearanceToExpenseDetailsDto(appearance1);

            Appearance appearance2 = mock(Appearance.class);
            when(appearance2.getCourtLocation()).thenReturn(courtLocation);
            when(appearance2.getAppearanceStage()).thenReturn(AppearanceStage.EXPENSE_ENTERED);
            when(appearance2.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 1));
            ExpenseDetailsDto expenseDetailDto2 = createExpenseDetailsDto(LocalDate.of(2023, 1, 1));
            doReturn(expenseDetailDto2).when(jurorExpenseService).mapAppearanceToExpenseDetailsDto(appearance2);


            Appearance appearance3 = mock(Appearance.class);
            when(appearance3.getCourtLocation()).thenReturn(courtLocation);
            when(appearance3.getAppearanceStage()).thenReturn(AppearanceStage.EXPENSE_ENTERED);
            when(appearance3.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 2));
            ExpenseDetailsDto expenseDetailDto3 = createExpenseDetailsDto(LocalDate.of(2023, 1, 2));
            doReturn(expenseDetailDto3).when(jurorExpenseService).mapAppearanceToExpenseDetailsDto(appearance3);


            CombinedExpenseDetailsDto<ExpenseDetailsDto> result = jurorExpenseService.getExpenses(
                List.of(appearance1, appearance2, appearance3)
            );
            assertThat(result).isNotNull();
            assertThat(result.getExpenseDetails()).hasSize(3);
            assertThat(result.getExpenseDetails()).containsExactly(
                expenseDetailDto1, expenseDetailDto2, expenseDetailDto3);
        }

        @Test
        void positiveNotFound() {
            CombinedExpenseDetailsDto<ExpenseDetailsDto> result = jurorExpenseService.getExpenses(
                List.of());
            assertThat(result).isNotNull();
            assertThat(result.getExpenseDetails()).hasSize(0);
        }

        @Test
        void negativeWrongOwner() {
            CourtLocation courtLocation = mock(CourtLocation.class);
            when(courtLocation.getOwner()).thenReturn("414");
            Appearance appearance1 = mock(Appearance.class);
            when(appearance1.getCourtLocation()).thenReturn(courtLocation);
            when(appearance1.getAppearanceStage()).thenReturn(AppearanceStage.EXPENSE_ENTERED);
            when(appearance1.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 3));

            Appearance appearance2 = mock(Appearance.class);
            when(appearance2.getCourtLocation()).thenReturn(courtLocation);
            when(appearance2.getAppearanceStage()).thenReturn(AppearanceStage.EXPENSE_ENTERED);
            when(appearance2.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 1));

            Appearance appearance3 = mock(Appearance.class);
            when(appearance3.getCourtLocation()).thenReturn(courtLocation);
            when(appearance3.getAppearanceStage()).thenReturn(AppearanceStage.EXPENSE_ENTERED);
            when(appearance3.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 2));

            MojException.Forbidden exception = assertThrows(MojException.Forbidden.class,
                () -> jurorExpenseService.getExpenses(List.of(appearance1, appearance2, appearance3)),
                "Should throw exception when juror tries to access a pool they do not have access to");
            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage())
                .isEqualTo("User cannot access this juror pool");
        }

        private ExpenseDetailsDto createExpenseDetailsDto(LocalDate date) {
            return ExpenseDetailsDto.builder()
                .attendanceDate(date)
                .attendanceType(AttendanceType.FULL_DAY)
                .lossOfEarnings(new BigDecimal("91.00"))
                .extraCare(new BigDecimal("71.00"))
                .other(new BigDecimal("81.00"))
                .publicTransport(new BigDecimal("11.00"))
                .taxi(new BigDecimal("21.00"))
                .motorcycle(new BigDecimal("31.00"))
                .car(new BigDecimal("41.00"))
                .bicycle(new BigDecimal("51.00"))
                .parking(new BigDecimal("61.00"))
                .foodAndDrink(new BigDecimal("101.00"))
                .smartCard(new BigDecimal("26.00"))
                .paymentMethod(PaymentMethod.CASH)
                .build();
        }
    }

    @Nested
    @DisplayName("public CombinedExpenseDetailsDto<ExpenseDetailsDto> getDraftExpenses(String jurorNumber, String "
        + "poolNumber)")
    class GetDraftExpenses {

        @Test
        @SuppressWarnings("LineLength")
        void positiveTypical() {
            Appearance appearance1 = mock(Appearance.class);
            when(appearance1.getAppearanceStage()).thenReturn(AppearanceStage.EXPENSE_ENTERED);
            when(appearance1.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 3));
            ExpenseDetailsDto expenseDetailDto1 = createExpenseDetailsDto(LocalDate.of(2023, 1, 3));
            doReturn(expenseDetailDto1).when(jurorExpenseService).mapAppearanceToExpenseDetailsDto(appearance1);

            Appearance appearance2 = mock(Appearance.class);
            when(appearance2.getAppearanceStage()).thenReturn(AppearanceStage.EXPENSE_ENTERED);
            when(appearance2.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 1));
            ExpenseDetailsDto expenseDetailDto2 = createExpenseDetailsDto(LocalDate.of(2023, 1, 1));
            doReturn(expenseDetailDto2).when(jurorExpenseService).mapAppearanceToExpenseDetailsDto(appearance2);


            Appearance appearance3 = mock(Appearance.class);
            when(appearance3.getAppearanceStage()).thenReturn(AppearanceStage.EXPENSE_ENTERED);
            when(appearance3.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 2));
            ExpenseDetailsDto expenseDetailDto3 = createExpenseDetailsDto(LocalDate.of(2023, 1, 2));
            doReturn(expenseDetailDto3).when(jurorExpenseService).mapAppearanceToExpenseDetailsDto(appearance3);

            when(
                appearanceRepository.findAllByCourtLocationLocCodeAndJurorNumberAndAppearanceStageAndIsDraftExpenseTrueOrderByAttendanceDate(
                    TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER,
                    AppearanceStage.EXPENSE_ENTERED
                )).thenReturn(List.of(appearance2, appearance3, appearance1));

            CombinedExpenseDetailsDto<ExpenseDetailsDto> combinedExpenseDetailsDto = new CombinedExpenseDetailsDto<>();
            doReturn(combinedExpenseDetailsDto).when(jurorExpenseService).getExpenses(any());

            CombinedExpenseDetailsDto<ExpenseDetailsDto> result = jurorExpenseService.getDraftExpenses(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER
            );
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(combinedExpenseDetailsDto);

            verify(appearanceRepository, times(1))
                .findAllByCourtLocationLocCodeAndJurorNumberAndAppearanceStageAndIsDraftExpenseTrueOrderByAttendanceDate(
                    TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER,
                    AppearanceStage.EXPENSE_ENTERED);
            verify(jurorExpenseService, times(1))
                .getExpenses(List.of(appearance2, appearance3, appearance1));

        }


        private ExpenseDetailsDto createExpenseDetailsDto(LocalDate date) {
            return ExpenseDetailsDto.builder()
                .attendanceDate(date)
                .attendanceType(AttendanceType.FULL_DAY)
                .lossOfEarnings(new BigDecimal("91.00"))
                .extraCare(new BigDecimal("71.00"))
                .other(new BigDecimal("81.00"))
                .publicTransport(new BigDecimal("11.00"))
                .taxi(new BigDecimal("21.00"))
                .motorcycle(new BigDecimal("31.00"))
                .car(new BigDecimal("41.00"))
                .bicycle(new BigDecimal("51.00"))
                .parking(new BigDecimal("61.00"))
                .foodAndDrink(new BigDecimal("101.00"))
                .smartCard(new BigDecimal("26.00"))
                .paymentMethod(PaymentMethod.CASH)
                .build();
        }
    }

    @Nested
    @DisplayName("ExpenseDetailsDto mapAppearanceToExpenseDetailsDto(Appearance appearance) ")
    class MapAppearanceToExpenseDetailsDto {
        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        void positiveTypical(boolean isCash) {
            Appearance appearance = new Appearance();
            appearance.setAttendanceDate(LocalDate.now());
            appearance.setAttendanceType(AttendanceType.FULL_DAY);
            appearance.setLossOfEarningsDue(new BigDecimal("1.11"));
            appearance.setChildcareDue(new BigDecimal("2.22"));
            appearance.setMiscAmountDue(new BigDecimal("3.33"));
            appearance.setPublicTransportDue(new BigDecimal("4.44"));
            appearance.setHiredVehicleDue(new BigDecimal("5.55"));
            appearance.setMotorcycleDue(new BigDecimal("6.66"));
            appearance.setCarDue(new BigDecimal("7.77"));
            appearance.setBicycleDue(new BigDecimal("8.88"));
            appearance.setParkingDue(new BigDecimal("9.99"));
            appearance.setSubsistenceDue(new BigDecimal("10.10"));
            appearance.setSmartCardAmountDue(new BigDecimal("11.11"));
            appearance.setPayCash(isCash);

            ExpenseDetailsDto expenseDetailsDto =
                jurorExpenseService.mapAppearanceToExpenseDetailsDto(appearance);
            assertThat(expenseDetailsDto).isNotNull();
            assertThat(expenseDetailsDto.getAttendanceDate()).isEqualTo(appearance.getAttendanceDate());
            assertThat(expenseDetailsDto.getAttendanceType()).isEqualTo(appearance.getAttendanceType());
            assertThat(expenseDetailsDto.getLossOfEarnings()).isEqualTo(appearance.getLossOfEarningsDue());
            assertThat(expenseDetailsDto.getExtraCare()).isEqualTo(appearance.getChildcareDue());
            assertThat(expenseDetailsDto.getOther()).isEqualTo(appearance.getMiscAmountDue());
            assertThat(expenseDetailsDto.getPublicTransport()).isEqualTo(appearance.getPublicTransportDue());
            assertThat(expenseDetailsDto.getTaxi()).isEqualTo(appearance.getHiredVehicleDue());
            assertThat(expenseDetailsDto.getMotorcycle()).isEqualTo(appearance.getMotorcycleDue());
            assertThat(expenseDetailsDto.getCar()).isEqualTo(appearance.getCarDue());
            assertThat(expenseDetailsDto.getBicycle()).isEqualTo(appearance.getBicycleDue());
            assertThat(expenseDetailsDto.getParking()).isEqualTo(appearance.getParkingDue());
            assertThat(expenseDetailsDto.getFoodAndDrink()).isEqualTo(appearance.getSubsistenceDue());
            assertThat(expenseDetailsDto.getSmartCard()).isEqualTo(appearance.getSmartCardAmountDue());
            if (isCash) {
                assertThat(expenseDetailsDto.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
            } else {
                assertThat(expenseDetailsDto.getPaymentMethod()).isEqualTo(PaymentMethod.BACS);
            }
        }
    }

    @Nested
    @DisplayName("void checkDueIsMoreThanPaid(Appearance appearance)")
    class CheckDueIsMoreThanPaid {

        @Test
        void positiveTrue() {
            Appearance appearance = mock(Appearance.class);
            doReturn(Map.of()).when(appearance).getExpensesWhereDueIsLessThenPaid();

            assertDoesNotThrow(() -> jurorExpenseService.checkDueIsMoreThanPaid(appearance),
                "Should not throw exception when due is more then paid");
        }

        @Test
        void positiveFalse() {
            Appearance appearance = mock(Appearance.class);
            Map<String, Object> errors = Map.of("some field", true);
            doReturn(errors).when(appearance).getExpensesWhereDueIsLessThenPaid();

            MojException.BusinessRuleViolation exception = assertThrows(MojException.BusinessRuleViolation.class,
                () -> jurorExpenseService.checkDueIsMoreThanPaid(appearance),
                "Should throw exception when due is less then paid");

            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage()).isEqualTo("Updated expense values cannot be less than the paid amount");
            assertThat(exception.getMetaData()).isEqualTo(errors);
            assertThat(exception.getErrorCode()).isEqualTo(
                MojException.BusinessRuleViolation.ErrorCode.EXPENSE_VALUES_REDUCED_LESS_THAN_PAID);
        }
    }

    @Nested
    @DisplayName("public void updateExpense(String jurorNumber, ExpenseType type, List<DailyExpense> request)")
    class UpdateExpense {

        @Test
        void positiveTypical() {
            CourtLocation courtLocation = mock(CourtLocation.class);
            when(courtLocation.getLocCode()).thenReturn(TestConstants.VALID_COURT_LOCATION);

            DailyExpense dailyExpense1 = mock(DailyExpense.class);
            doReturn(LocalDate.of(2023, 1, 1)).when(dailyExpense1).getDateOfExpense();
            Appearance appearance1 = mock(Appearance.class);
            doReturn(appearance1).when(jurorExpenseService)
                .getAppearance(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER,
                    LocalDate.of(2023, 1, 1));
            doReturn(courtLocation).when(appearance1).getCourtLocation();

            DailyExpense dailyExpense2 = mock(DailyExpense.class);
            doReturn(LocalDate.of(2023, 1, 2)).when(dailyExpense2).getDateOfExpense();
            Appearance appearance2 = mock(Appearance.class);
            doReturn(appearance2).when(jurorExpenseService)
                .getAppearance(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER,
                    LocalDate.of(2023, 1, 2));
            doReturn(courtLocation).when(appearance2).getCourtLocation();

            DailyExpense dailyExpense3 = mock(DailyExpense.class);
            doReturn(LocalDate.of(2023, 1, 3)).when(dailyExpense3).getDateOfExpense();
            Appearance appearance3 = mock(Appearance.class);
            doReturn(appearance3).when(jurorExpenseService)
                .getAppearance(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER,
                    LocalDate.of(2023, 1, 3));
            doReturn(courtLocation).when(appearance3).getCourtLocation();

            ExpenseType expenseType = mock(ExpenseType.class);

            doReturn(true).when(expenseType).isApplicable(appearance1);
            doReturn(true).when(expenseType).isApplicable(appearance2);
            doReturn(true).when(expenseType).isApplicable(appearance3);
            doReturn(FinancialAuditDetails.Type.FOR_APPROVAL_EDIT).when(expenseType).toEditType();

            doReturn(null).when(jurorExpenseService).updateExpenseInternal(any(), any());

            FinancialAuditDetails financialAuditDetails = mock(FinancialAuditDetails.class);
            doReturn(financialAuditDetails).when(financialAuditService)
                .createFinancialAuditDetail(any(), any(), any(), any());

            jurorExpenseService.updateExpense(
                TestConstants.VALID_COURT_LOCATION,
                TestConstants.VALID_JUROR_NUMBER,
                expenseType,
                List.of(dailyExpense1, dailyExpense2, dailyExpense3)
            );

            verify(financialAuditService, times(1))
                .createFinancialAuditDetail(TestConstants.VALID_JUROR_NUMBER,
                    TestConstants.VALID_COURT_LOCATION,
                    FinancialAuditDetails.Type.FOR_APPROVAL_EDIT,
                    List.of(appearance1, appearance2, appearance3));

            verify(jurorExpenseService, times(1))
                .updateExpenseInternal(appearance1, dailyExpense1);
            verify(jurorExpenseService, times(1))
                .updateExpenseInternal(appearance2, dailyExpense2);
            verify(jurorExpenseService, times(1))
                .updateExpenseInternal(appearance3, dailyExpense3);

            verify(jurorHistoryService, times(1))
                .createExpenseEditHistory(financialAuditDetails, appearance1);
            verify(jurorHistoryService, times(1))
                .createExpenseEditHistory(financialAuditDetails, appearance2);
            verify(jurorHistoryService, times(1))
                .createExpenseEditHistory(financialAuditDetails, appearance3);
        }

        @Test
        void negativeTypeNotApplicable() {
            DailyExpense dailyExpense1 = mock(DailyExpense.class);
            doReturn(LocalDate.of(2023, 1, 1)).when(dailyExpense1).getDateOfExpense();
            Appearance appearance1 = mock(Appearance.class);
            doReturn(appearance1).when(jurorExpenseService)
                .getAppearance(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER,
                    LocalDate.of(2023, 1, 1));

            ExpenseType expenseType = mock(ExpenseType.class);
            doReturn("TEST_TYPE").when(expenseType).name();
            doReturn(false).when(expenseType).isApplicable(appearance1);


            MojException.BusinessRuleViolation exception = assertThrows(MojException.BusinessRuleViolation.class,
                () -> jurorExpenseService.updateExpense(
                    TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER,
                    expenseType,
                    List.of(dailyExpense1)
                ),
                "Should throw exception when expense type is not applicable");

            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage()).isEqualTo("Expense for this day is not of type: TEST_TYPE");
            assertThat(exception.getErrorCode()).isEqualTo(
                MojException.BusinessRuleViolation.ErrorCode.WRONG_EXPENSE_TYPE);
        }
    }

    @Nested
    @DisplayName("public CombinedExpenseDetailsDto<ExpenseDetailsForTotals> calculateTotals"
        + "(CalculateTotalExpenseRequestDto dto)")
    class CalculateTotals {
        @Test
        void positiveTypical() {
            Appearance appearance1 = mock(Appearance.class);
            when(appearance1.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 1));
            DailyExpense dailyExpense1 = mock(DailyExpense.class);
            when(dailyExpense1.getDateOfExpense()).thenReturn(LocalDate.of(2023, 1, 1));
            when(appearance1.getTotalPaid()).thenReturn(new BigDecimal("1.11"));

            Appearance appearance2 = mock(Appearance.class);
            when(appearance2.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 2));
            DailyExpense dailyExpense2 = mock(DailyExpense.class);
            when(dailyExpense2.getDateOfExpense()).thenReturn(LocalDate.of(2023, 1, 2));
            when(dailyExpense2.shouldPullFromDatabase()).thenReturn(true);
            when(appearance2.getTotalPaid()).thenReturn(new BigDecimal("2.22"));

            Appearance appearance3 = mock(Appearance.class);
            when(appearance3.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 3));
            DailyExpense dailyExpense3 = mock(DailyExpense.class);
            when(dailyExpense3.getDateOfExpense()).thenReturn(LocalDate.of(2023, 1, 3));
            when(appearance3.getTotalPaid()).thenReturn(new BigDecimal("3.33"));

            CalculateTotalExpenseRequestDto request = CalculateTotalExpenseRequestDto.builder()
                .expenseList(List.of(dailyExpense1, dailyExpense2, dailyExpense3))
                .build();


            doReturn(appearance1).when(jurorExpenseService)
                .getAppearance(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER,
                    LocalDate.of(2023, 1, 1));
            doReturn(appearance2).when(jurorExpenseService)
                .getAppearance(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER,
                    LocalDate.of(2023, 1, 2));
            doReturn(appearance3).when(jurorExpenseService)
                .getAppearance(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER,
                    LocalDate.of(2023, 1, 3));

            doReturn(new DailyExpenseResponse()).when(jurorExpenseService)
                .updateExpenseInternal(appearance1, dailyExpense1);


            doReturn(new DailyExpenseResponse()).when(jurorExpenseService)
                .updateExpenseInternal(appearance3, dailyExpense3);

            CombinedExpenseDetailsDto<ExpenseDetailsForTotals> result =
                jurorExpenseService.calculateTotals(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER, request);

            assertThat(result).isNotNull();
            assertThat(result.getExpenseDetails()).contains(
                ExpenseDetailsForTotals.builder()
                    .attendanceDate(LocalDate.of(2023, 1, 1))
                    .paymentMethod(PaymentMethod.BACS)
                    .totalPaid(new BigDecimal("1.11")).build(),
                ExpenseDetailsForTotals.builder()
                    .attendanceDate(LocalDate.of(2023, 1, 2))
                    .paymentMethod(PaymentMethod.BACS)
                    .totalPaid(new BigDecimal("2.22")).build(),
                ExpenseDetailsForTotals.builder()
                    .attendanceDate(LocalDate.of(2023, 1, 3))
                    .paymentMethod(PaymentMethod.BACS)
                    .totalPaid(new BigDecimal("3.33")).build());

            verify(jurorExpenseService, times(1))
                .getAppearance(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER,
                    LocalDate.of(2023, 1, 1));
            verify(jurorExpenseService, times(1))
                .getAppearance(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER,
                    LocalDate.of(2023, 1, 2));
            verify(jurorExpenseService, times(1))
                .getAppearance(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER,
                    LocalDate.of(2023, 1, 3));

            verify(jurorExpenseService, times(1))
                .updateExpenseInternal(appearance1, dailyExpense1);
            verify(jurorExpenseService, never())
                .updateExpenseInternal(appearance2, dailyExpense2);
            verify(jurorExpenseService, times(1))
                .updateExpenseInternal(appearance3, dailyExpense3);


            verify(entityManager, times(1)).detach(appearance1);
            verify(entityManager, times(1)).detach(appearance2);
            verify(entityManager, times(1)).detach(appearance3);
        }

        @Test
        void positiveTypicalApportionExpense() {
            Appearance appearance1 = mock(Appearance.class);
            when(appearance1.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 1));
            DailyExpense dailyExpense1 = mock(DailyExpense.class);
            when(dailyExpense1.getDateOfExpense()).thenReturn(LocalDate.of(2023, 1, 1));
            when(appearance1.getTotalPaid()).thenReturn(new BigDecimal("1.11"));

            Appearance appearance2 = mock(Appearance.class);
            when(appearance2.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 2));
            DailyExpense dailyExpense2 = mock(DailyExpense.class);
            when(dailyExpense2.getDateOfExpense()).thenReturn(LocalDate.of(2023, 1, 2));
            when(dailyExpense2.shouldPullFromDatabase()).thenReturn(true);
            when(appearance2.getTotalPaid()).thenReturn(new BigDecimal("2.22"));

            Appearance appearance3 = mock(Appearance.class);
            when(appearance3.getAttendanceDate()).thenReturn(LocalDate.of(2023, 1, 3));
            DailyExpense dailyExpense3 = mock(DailyExpense.class);
            when(dailyExpense3.getDateOfExpense()).thenReturn(LocalDate.of(2023, 1, 3));
            when(appearance3.getTotalPaid()).thenReturn(new BigDecimal("3.33"));

            CalculateTotalExpenseRequestDto request = CalculateTotalExpenseRequestDto.builder()
                .expenseList(List.of(dailyExpense1, dailyExpense2, dailyExpense3))
                .build();


            doReturn(appearance1).when(jurorExpenseService)
                .getAppearance(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER,
                    LocalDate.of(2023, 1, 1));
            doReturn(appearance2).when(jurorExpenseService)
                .getAppearance(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER,
                    LocalDate.of(2023, 1, 2));
            doReturn(appearance3).when(jurorExpenseService)
                .getAppearance(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER,
                    LocalDate.of(2023, 1, 3));

            doReturn(new DailyExpenseResponse(new FinancialLossWarning())).when(jurorExpenseService)
                .updateExpenseInternal(appearance1, dailyExpense1);


            doReturn(new DailyExpenseResponse()).when(jurorExpenseService)
                .updateExpenseInternal(appearance3, dailyExpense3);

            CombinedExpenseDetailsDto<ExpenseDetailsForTotals> result =
                jurorExpenseService.calculateTotals(
                    TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER,
                    request);

            assertThat(result).isNotNull();
            assertThat(result.getExpenseDetails()).contains(
                ExpenseDetailsForTotals.builder()
                    .attendanceDate(LocalDate.of(2023, 1, 1))
                    .paymentMethod(PaymentMethod.BACS)
                    .financialLossApportionedApplied(true)
                    .totalPaid(new BigDecimal("1.11")).build(),
                ExpenseDetailsForTotals.builder()
                    .attendanceDate(LocalDate.of(2023, 1, 2))
                    .paymentMethod(PaymentMethod.BACS)
                    .totalPaid(new BigDecimal("2.22")).build(),
                ExpenseDetailsForTotals.builder()
                    .attendanceDate(LocalDate.of(2023, 1, 3))
                    .paymentMethod(PaymentMethod.BACS)
                    .totalPaid(new BigDecimal("3.33")).build());

            verify(jurorExpenseService, times(1))
                .getAppearance(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER,
                    LocalDate.of(2023, 1, 1));
            verify(jurorExpenseService, times(1))
                .getAppearance(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER,
                    LocalDate.of(2023, 1, 2));
            verify(jurorExpenseService, times(1))
                .getAppearance(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER,
                    LocalDate.of(2023, 1, 3));

            verify(jurorExpenseService, times(1))
                .updateExpenseInternal(appearance1, dailyExpense1);
            verify(jurorExpenseService, never())
                .updateExpenseInternal(appearance2, dailyExpense2);
            verify(jurorExpenseService, times(1))
                .updateExpenseInternal(appearance3, dailyExpense3);


            verify(entityManager, times(1)).detach(appearance1);
            verify(entityManager, times(1)).detach(appearance2);
            verify(entityManager, times(1)).detach(appearance3);
        }

    }

    @Nested
    @DisplayName("public ExpenseCount countExpenseTypes(String jurorNumber, String poolNumber)")
    class CountExpenseTypes {

        private static final CourtLocation COURT_LOCATION;

        static {
            COURT_LOCATION = mock(CourtLocation.class);
            when(COURT_LOCATION.getOwner()).thenReturn(TestConstants.VALID_COURT_LOCATION);
        }

        @BeforeEach
        void mockCurrentUser() {
            securityUtilMockedStatic = mockStatic(SecurityUtil.class);
            securityUtilMockedStatic.when(SecurityUtil::getActiveOwner)
                .thenReturn(TestConstants.VALID_COURT_LOCATION);
        }

        @AfterEach
        void afterEach() {
            if (securityUtilMockedStatic != null) {
                securityUtilMockedStatic.close();
            }
        }

        private Appearance mockAppearance(boolean isDraft, AppearanceStage stage) {
            Appearance appearance = mock(Appearance.class);
            when(appearance.getAppearanceStage()).thenReturn(stage);
            when(appearance.isDraftExpense()).thenReturn(isDraft);
            when(appearance.getCourtLocation()).thenReturn(COURT_LOCATION);
            return appearance;
        }

        @Test
        void positiveTypical() {
            List<Appearance> appearances = List.of(
                mockAppearance(true, AppearanceStage.EXPENSE_ENTERED),
                mockAppearance(true, AppearanceStage.EXPENSE_ENTERED),
                mockAppearance(true, AppearanceStage.EXPENSE_ENTERED),
                mockAppearance(false, AppearanceStage.EXPENSE_ENTERED),
                mockAppearance(false, AppearanceStage.EXPENSE_ENTERED),
                mockAppearance(false, AppearanceStage.EXPENSE_ENTERED),
                mockAppearance(false, AppearanceStage.EXPENSE_ENTERED),
                mockAppearance(false, AppearanceStage.EXPENSE_AUTHORISED),
                mockAppearance(false, AppearanceStage.EXPENSE_AUTHORISED),
                mockAppearance(false, AppearanceStage.EXPENSE_EDITED),

                mockAppearance(false, AppearanceStage.CHECKED_IN),
                mockAppearance(false, AppearanceStage.CHECKED_OUT),
                mockAppearance(true, AppearanceStage.CHECKED_IN),
                mockAppearance(true, AppearanceStage.CHECKED_OUT)
            );
            when(appearanceRepository.findAllByCourtLocationLocCodeAndJurorNumber(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER
            )).thenReturn(appearances);

            assertThat(jurorExpenseService.countExpenseTypes(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER
            )).isEqualTo(
                new ExpenseCount(3, 4, 1, 2));
        }

        @Test
        void positiveNotFound() {
            when(appearanceRepository.findAllByJurorNumberAndPoolNumber(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER
            )).thenReturn(List.of());

            assertThat(jurorExpenseService.countExpenseTypes(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER
            )).isEqualTo(
                new ExpenseCount(0, 0, 0, 0));
        }

        @Test
        void negativeWrongOwner() {
            List<Appearance> appearances = List.of(
                mockAppearance(true, AppearanceStage.EXPENSE_ENTERED),
                mockAppearance(true, AppearanceStage.EXPENSE_ENTERED),
                mockAppearance(true, AppearanceStage.EXPENSE_ENTERED),
                mockAppearance(false, AppearanceStage.EXPENSE_ENTERED),
                mockAppearance(false, AppearanceStage.EXPENSE_ENTERED)
            );
            when(appearanceRepository.findAllByCourtLocationLocCodeAndJurorNumber(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER
            )).thenReturn(appearances);
            securityUtilMockedStatic.when(SecurityUtil::getActiveOwner)
                .thenReturn("414");

            MojException.Forbidden exception = assertThrows(MojException.Forbidden.class,
                () -> jurorExpenseService.countExpenseTypes(
                    TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER
                ),
                "Should throw exception user tries to access a pool they do not have access too");
            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage())
                .isEqualTo("User cannot access this juror pool");
        }

    }

    @Nested
    @DisplayName("public List<PendingApproval> getExpensesForApproval(String locCode, PaymentMethod paymentMethod,\n"
        + "                                                        LocalDate fromInclusive, LocalDate toInclusive)")
    class GetExpensesForApproval {
        private PendingApproval mockPendingApproval(String validJurorNumber, String validPoolNumber) {
            PendingApproval pendingApproval = mock(PendingApproval.class);
            doReturn(validJurorNumber).when(pendingApproval).getJurorNumber();
            doReturn(validPoolNumber).when(pendingApproval).getPoolNumber();
            return pendingApproval;
        }

        @Test
        void positiveTypicalCash() {
            PaymentMethod paymentMethod = PaymentMethod.CASH;

            List<Appearance> forApprovedAppearances = List.of(
                mock(Appearance.class),
                mock(Appearance.class),
                mock(Appearance.class),
                mock(Appearance.class)
            );
            List<Appearance> forReApprovedAppearances = List.of(
                mock(Appearance.class),
                mock(Appearance.class)
            );

            doReturn(forApprovedAppearances).when(
                appearanceRepository).findAllByCourtLocationLocCodeAndAppearanceStageAndPayCashAndIsDraftExpenseFalse(
                TestConstants.VALID_COURT_LOCATION,
                AppearanceStage.EXPENSE_ENTERED,
                true);
            doReturn(forReApprovedAppearances).when(
                appearanceRepository).findAllByCourtLocationLocCodeAndAppearanceStageAndPayCashAndIsDraftExpenseFalse(
                TestConstants.VALID_COURT_LOCATION,
                AppearanceStage.EXPENSE_EDITED,
                true);


            doReturn(5L).when(
                appearanceRepository).countPendingApproval(
                TestConstants.VALID_COURT_LOCATION,
                true);


            doReturn(2L).when(
                appearanceRepository).countPendingApproval(
                TestConstants.VALID_COURT_LOCATION,
                false);

            LocalDate from = LocalDate.of(2023, 1, 1);
            LocalDate to = LocalDate.of(2023, 1, 5);

            PendingApproval pendingApproval1 = mockPendingApproval(TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER);
            PendingApproval pendingApproval2 = mockPendingApproval(TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER_2);
            PendingApproval pendingApproval3 = mockPendingApproval(TestConstants.VALID_JUROR_NUMBER_2,
                TestConstants.VALID_POOL_NUMBER);

            doReturn(List.of(pendingApproval1, pendingApproval2, pendingApproval3)).when(jurorExpenseService)
                .mapAppearancesToPendingApproval(
                    forApprovedAppearances, false, from, to);

            PendingApproval pendingApproval4 = mockPendingApproval(TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER);
            PendingApproval pendingApproval5 = mockPendingApproval(TestConstants.VALID_JUROR_NUMBER_2,
                TestConstants.VALID_POOL_NUMBER_2);
            doReturn(List.of(pendingApproval4, pendingApproval5)).when(jurorExpenseService)
                .mapAppearancesToPendingApproval(
                    forReApprovedAppearances, true, from, to);

            PendingApprovalList pendingApprovalList = jurorExpenseService.getExpensesForApproval(
                TestConstants.VALID_COURT_LOCATION, paymentMethod,
                from, to
            );
            assertThat(pendingApprovalList.getPendingApproval()).containsExactly(
                pendingApproval5, pendingApproval3, pendingApproval2, pendingApproval1, pendingApproval4
            );
            assertThat(pendingApprovalList.getTotalPendingCash())
                .isEqualTo(5L);
            assertThat(pendingApprovalList.getTotalPendingBacs())
                .isEqualTo(2L);

            verify(appearanceRepository,
                times(1)).findAllByCourtLocationLocCodeAndAppearanceStageAndPayCashAndIsDraftExpenseFalse(
                TestConstants.VALID_COURT_LOCATION,
                AppearanceStage.EXPENSE_ENTERED,
                true);
            verify(appearanceRepository,
                times(1)).findAllByCourtLocationLocCodeAndAppearanceStageAndPayCashAndIsDraftExpenseFalse(
                TestConstants.VALID_COURT_LOCATION,
                AppearanceStage.EXPENSE_EDITED,
                true);
            verify(jurorExpenseService, times(1)).mapAppearancesToPendingApproval(
                forApprovedAppearances, false, from, to);
            verify(jurorExpenseService, times(1)).mapAppearancesToPendingApproval(
                forReApprovedAppearances, true, from, to);
        }


    }

    @Nested
    @DisplayName("private List<PendingApproval> mapAppearancesToPendingApproval(List<Appearance> appearances,\n"
        + "                                                                  boolean isReapproval,\n"
        + "                                                                  LocalDate fromInclusive, LocalDate "
        + "toInclusive)")
    class MapAppearancesToPendingApproval {

        private Appearance mockAppearance(
            String jurorNumber,
            String poolNumber,
            LocalDate date) {
            Appearance appearance = mock(Appearance.class);
            doReturn(date).when(appearance).getAttendanceDate();
            doReturn(jurorNumber).when(appearance).getJurorNumber();
            doReturn(poolNumber).when(appearance).getPoolNumber();
            return appearance;
        }

        @Test
        void positiveTypicalJurorPoolMapping() {
            Appearance juror1Pool1Appearance1 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 1, 1));
            Appearance juror1Pool1Appearance2 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 1, 2));
            Appearance juror1Pool1Appearance3 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 1, 3));

            Appearance juror2Pool1Appearance1 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 1, 1));
            Appearance juror2Pool1Appearance2 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 1, 2));

            Appearance juror2Pool2Appearance1 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER_2, LocalDate.of(2023, 1, 1));
            Appearance juror2Pool2Appearance2 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER_2, LocalDate.of(2023, 1, 2));


            PendingApproval juror1Pool1PendingApproval = mock(PendingApproval.class);
            PendingApproval juror2Pool1PendingApproval = mock(PendingApproval.class);
            PendingApproval juror2Pool2PendingApproval = mock(PendingApproval.class);

            doReturn(juror1Pool1PendingApproval).when(jurorExpenseService)
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror1Pool1Appearance1, juror1Pool1Appearance2, juror1Pool1Appearance3), false);
            doReturn(juror2Pool1PendingApproval).when(jurorExpenseService)
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror2Pool1Appearance1, juror2Pool1Appearance2), false);
            doReturn(juror2Pool2PendingApproval).when(jurorExpenseService)
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror2Pool2Appearance1, juror2Pool2Appearance2), false);


            assertThat(jurorExpenseService.mapAppearancesToPendingApproval(
                List.of(
                    juror1Pool1Appearance1, juror1Pool1Appearance2, juror1Pool1Appearance3,
                    juror2Pool1Appearance1, juror2Pool1Appearance2,
                    juror2Pool2Appearance1, juror2Pool2Appearance2
                ), false, null, null
            )).containsExactlyInAnyOrder(juror1Pool1PendingApproval, juror2Pool1PendingApproval,
                juror2Pool2PendingApproval);

            verify(jurorExpenseService, times(1))
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror1Pool1Appearance1, juror1Pool1Appearance2, juror1Pool1Appearance3), false);
            verify(jurorExpenseService, times(1))
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror2Pool1Appearance1, juror2Pool1Appearance2), false);
            verify(jurorExpenseService, times(1))
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror2Pool2Appearance1, juror2Pool2Appearance2), false);
        }

        @Test
        @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
        void positiveTypicalFromDateEquals() {
            Appearance juror1Pool1Appearance1 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 1, 1));
            Appearance juror1Pool1Appearance2 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 1, 2));
            Appearance juror1Pool1Appearance3 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 1, 3));

            Appearance juror2Pool1Appearance1 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 2, 1));
            Appearance juror2Pool1Appearance2 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 2, 2));

            Appearance juror2Pool2Appearance1 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER_2, LocalDate.of(2023, 3, 1));
            Appearance juror2Pool2Appearance2 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER_2, LocalDate.of(2023, 3, 2));


            PendingApproval juror1Pool1PendingApproval = mock(PendingApproval.class);
            PendingApproval juror2Pool1PendingApproval = mock(PendingApproval.class);
            PendingApproval juror2Pool2PendingApproval = mock(PendingApproval.class);

            doReturn(juror1Pool1PendingApproval).when(jurorExpenseService)
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror1Pool1Appearance1, juror1Pool1Appearance2, juror1Pool1Appearance3), false);
            doReturn(juror2Pool1PendingApproval).when(jurorExpenseService)
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror2Pool1Appearance1, juror2Pool1Appearance2), false);
            doReturn(juror2Pool2PendingApproval).when(jurorExpenseService)
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror2Pool2Appearance1, juror2Pool2Appearance2), false);


            assertThat(jurorExpenseService.mapAppearancesToPendingApproval(
                List.of(
                    juror1Pool1Appearance1, juror1Pool1Appearance2, juror1Pool1Appearance3,
                    juror2Pool1Appearance1, juror2Pool1Appearance2,
                    juror2Pool2Appearance1, juror2Pool2Appearance2
                ), false, LocalDate.of(2023, 2, 1), null
            )).containsExactlyInAnyOrder(juror2Pool1PendingApproval, juror2Pool2PendingApproval);

            verify(jurorExpenseService, times(1))
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror2Pool1Appearance1, juror2Pool1Appearance2), false);
            verify(jurorExpenseService, times(1))
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror2Pool2Appearance1, juror2Pool2Appearance2), false);
        }

        @Test
        @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
        void positiveTypicalFromDateIsAfter() {
            Appearance juror1Pool1Appearance1 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 1, 1));
            Appearance juror1Pool1Appearance2 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 1, 2));
            Appearance juror1Pool1Appearance3 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 1, 3));

            Appearance juror2Pool1Appearance1 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 2, 1));
            Appearance juror2Pool1Appearance2 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 2, 2));

            Appearance juror2Pool2Appearance1 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER_2, LocalDate.of(2023, 3, 1));
            Appearance juror2Pool2Appearance2 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER_2, LocalDate.of(2023, 3, 2));


            PendingApproval juror1Pool1PendingApproval = mock(PendingApproval.class);
            PendingApproval juror2Pool1PendingApproval = mock(PendingApproval.class);
            PendingApproval juror2Pool2PendingApproval = mock(PendingApproval.class);

            doReturn(juror1Pool1PendingApproval).when(jurorExpenseService)
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror1Pool1Appearance1, juror1Pool1Appearance2, juror1Pool1Appearance3), false);
            doReturn(juror2Pool1PendingApproval).when(jurorExpenseService)
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror2Pool1Appearance1, juror2Pool1Appearance2), false);
            doReturn(juror2Pool2PendingApproval).when(jurorExpenseService)
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror2Pool2Appearance1, juror2Pool2Appearance2), false);


            assertThat(jurorExpenseService.mapAppearancesToPendingApproval(
                List.of(
                    juror1Pool1Appearance1, juror1Pool1Appearance2, juror1Pool1Appearance3,
                    juror2Pool1Appearance1, juror2Pool1Appearance2,
                    juror2Pool2Appearance1, juror2Pool2Appearance2
                ), false, LocalDate.of(2023, 1, 25), null
            )).containsExactlyInAnyOrder(juror2Pool1PendingApproval, juror2Pool2PendingApproval);

            verify(jurorExpenseService, times(1))
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror2Pool1Appearance1, juror2Pool1Appearance2), false);
            verify(jurorExpenseService, times(1))
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror2Pool2Appearance1, juror2Pool2Appearance2), false);
        }

        @Test
        @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
        void positiveTypicalToDateEquals() {
            Appearance juror1Pool1Appearance1 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 1, 1));
            Appearance juror1Pool1Appearance2 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 1, 2));
            Appearance juror1Pool1Appearance3 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 1, 3));

            Appearance juror2Pool1Appearance1 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 2, 1));
            Appearance juror2Pool1Appearance2 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 2, 2));

            Appearance juror2Pool2Appearance1 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER_2, LocalDate.of(2023, 3, 1));
            Appearance juror2Pool2Appearance2 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER_2, LocalDate.of(2023, 3, 2));


            PendingApproval juror1Pool1PendingApproval = mock(PendingApproval.class);
            PendingApproval juror2Pool1PendingApproval = mock(PendingApproval.class);
            PendingApproval juror2Pool2PendingApproval = mock(PendingApproval.class);

            doReturn(juror1Pool1PendingApproval).when(jurorExpenseService)
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror1Pool1Appearance1, juror1Pool1Appearance2, juror1Pool1Appearance3), false);
            doReturn(juror2Pool1PendingApproval).when(jurorExpenseService)
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror2Pool1Appearance1, juror2Pool1Appearance2), false);
            doReturn(juror2Pool2PendingApproval).when(jurorExpenseService)
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror2Pool2Appearance1, juror2Pool2Appearance2), false);


            assertThat(jurorExpenseService.mapAppearancesToPendingApproval(
                List.of(
                    juror1Pool1Appearance1, juror1Pool1Appearance2, juror1Pool1Appearance3,
                    juror2Pool1Appearance1, juror2Pool1Appearance2,
                    juror2Pool2Appearance1, juror2Pool2Appearance2
                ), false, null, LocalDate.of(2023, 2, 1)
            )).containsExactlyInAnyOrder(juror1Pool1PendingApproval, juror2Pool1PendingApproval);

            verify(jurorExpenseService, times(1))
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror1Pool1Appearance1, juror1Pool1Appearance2, juror1Pool1Appearance3), false);
            verify(jurorExpenseService, times(1))
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror2Pool1Appearance1, juror2Pool1Appearance2), false);
        }

        @Test
        @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
        void positiveTypicalToDateIsBefore() {
            Appearance juror1Pool1Appearance1 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 1, 1));
            Appearance juror1Pool1Appearance2 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 1, 2));
            Appearance juror1Pool1Appearance3 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 1, 3));

            Appearance juror2Pool1Appearance1 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 2, 1));
            Appearance juror2Pool1Appearance2 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 2, 2));

            Appearance juror2Pool2Appearance1 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER_2, LocalDate.of(2023, 3, 1));
            Appearance juror2Pool2Appearance2 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER_2, LocalDate.of(2023, 3, 2));


            PendingApproval juror1Pool1PendingApproval = mock(PendingApproval.class);
            PendingApproval juror2Pool1PendingApproval = mock(PendingApproval.class);
            PendingApproval juror2Pool2PendingApproval = mock(PendingApproval.class);

            doReturn(juror1Pool1PendingApproval).when(jurorExpenseService)
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror1Pool1Appearance1, juror1Pool1Appearance2, juror1Pool1Appearance3), false);
            doReturn(juror2Pool1PendingApproval).when(jurorExpenseService)
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror2Pool1Appearance1, juror2Pool1Appearance2), false);
            doReturn(juror2Pool2PendingApproval).when(jurorExpenseService)
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror2Pool2Appearance1, juror2Pool2Appearance2), false);


            assertThat(jurorExpenseService.mapAppearancesToPendingApproval(
                List.of(
                    juror1Pool1Appearance1, juror1Pool1Appearance2, juror1Pool1Appearance3,
                    juror2Pool1Appearance1, juror2Pool1Appearance2,
                    juror2Pool2Appearance1, juror2Pool2Appearance2
                ), false, null, LocalDate.of(2023, 2, 28)
            )).containsExactlyInAnyOrder(juror1Pool1PendingApproval, juror2Pool1PendingApproval);

            verify(jurorExpenseService, times(1))
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror1Pool1Appearance1, juror1Pool1Appearance2, juror1Pool1Appearance3), false);
            verify(jurorExpenseService, times(1))
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror2Pool1Appearance1, juror2Pool1Appearance2), false);

        }

        @Test
        void positiveTypicalBothDates() {
            Appearance juror1Pool1Appearance1 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 1, 1));
            Appearance juror1Pool1Appearance2 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 1, 2));
            Appearance juror1Pool1Appearance3 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 1, 3));

            Appearance juror2Pool1Appearance1 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 2, 1));
            Appearance juror2Pool1Appearance2 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER, LocalDate.of(2023, 2, 2));

            Appearance juror2Pool2Appearance1 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER_2, LocalDate.of(2023, 3, 1));
            Appearance juror2Pool2Appearance2 = mockAppearance(
                TestConstants.VALID_JUROR_NUMBER_2, TestConstants.VALID_POOL_NUMBER_2, LocalDate.of(2023, 3, 2));


            PendingApproval juror1Pool1PendingApproval = mock(PendingApproval.class);
            PendingApproval juror2Pool1PendingApproval = mock(PendingApproval.class);
            PendingApproval juror2Pool2PendingApproval = mock(PendingApproval.class);

            doReturn(juror1Pool1PendingApproval).when(jurorExpenseService)
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror1Pool1Appearance1, juror1Pool1Appearance2, juror1Pool1Appearance3), false);
            doReturn(juror2Pool1PendingApproval).when(jurorExpenseService)
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror2Pool1Appearance1, juror2Pool1Appearance2), false);
            doReturn(juror2Pool2PendingApproval).when(jurorExpenseService)
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror2Pool2Appearance1, juror2Pool2Appearance2), false);


            assertThat(jurorExpenseService.mapAppearancesToPendingApproval(
                List.of(
                    juror1Pool1Appearance1, juror1Pool1Appearance2, juror1Pool1Appearance3,
                    juror2Pool1Appearance1, juror2Pool1Appearance2,
                    juror2Pool2Appearance1, juror2Pool2Appearance2
                ), false, LocalDate.of(2023, 1, 29), LocalDate.of(2023, 2, 28)
            )).containsExactlyInAnyOrder(juror2Pool1PendingApproval);

            verify(jurorExpenseService, times(1))
                .mapAppearancesToPendingApprovalSinglePool(
                    List.of(juror2Pool1Appearance1, juror2Pool1Appearance2), false);
        }
    }

    @Nested
    @DisplayName("private PendingApproval mapAppearancesToPendingApprovalSinglePool(List<Appearance> appearances,\n"
        + "                                                                      boolean isReapproval)")
    class MapAppearancesToPendingApprovalSinglePool {

        private Appearance mockAppearance(LocalDate date, BigDecimal totalChanged, Long version) {
            Appearance appearance = mock(Appearance.class);
            doReturn(date).when(appearance).getAttendanceDate();
            doReturn(totalChanged).when(appearance).getTotalChanged();
            doReturn(version).when(appearance).getVersion();
            return appearance;
        }

        @Test
        void positiveTypicalApproval() {
            Appearance appearance1 = mockAppearance(LocalDate.of(2023, 1, 1), new BigDecimal("2.2"), 3L);
            when(appearance1.getJurorNumber()).thenReturn(TestConstants.VALID_JUROR_NUMBER);
            when(appearance1.getPoolNumber()).thenReturn(TestConstants.VALID_POOL_NUMBER);
            Appearance appearance2 = mockAppearance(LocalDate.of(2023, 1, 3), new BigDecimal("4.2"), 1L);
            Appearance appearance3 = mockAppearance(LocalDate.of(2023, 1, 2), new BigDecimal("3.2"), 8L);

            Juror juror = mock(Juror.class);
            when(juror.getFirstName()).thenReturn("John");
            when(juror.getLastName()).thenReturn("Doe");
            doReturn(juror).when(jurorExpenseService).getJuror(TestConstants.VALID_JUROR_NUMBER);
            doReturn(true).when(jurorExpenseService).validateUserCanApprove(
                List.of(appearance1, appearance2, appearance3)
            );

            assertThat(jurorExpenseService.mapAppearancesToPendingApprovalSinglePool(
                List.of(appearance1, appearance2, appearance3), false))
                .isEqualTo(PendingApproval.builder()
                    .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                    .poolNumber(TestConstants.VALID_POOL_NUMBER)
                    .firstName("John")
                    .lastName("Doe")
                    .amountDue(new BigDecimal("9.6"))
                    .expenseType(ExpenseType.FOR_APPROVAL)
                    .canApprove(true)
                    .dateToRevisions(
                        List.of(
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 1))
                                .version(3L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 3))
                                .version(1L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 2))
                                .version(8L)
                                .build()
                        )
                    )
                    .build());

            verify(jurorExpenseService, times(1)).getJuror(TestConstants.VALID_JUROR_NUMBER);
            verify(jurorExpenseService, times(1)).validateUserCanApprove(
                List.of(appearance1, appearance2, appearance3)
            );

        }

        @Test
        void positiveTypicalReApproval() {
            Appearance appearance1 = mockAppearance(LocalDate.of(2023, 1, 4), new BigDecimal("3.2"), 3L);
            when(appearance1.getJurorNumber()).thenReturn(TestConstants.VALID_JUROR_NUMBER);
            when(appearance1.getPoolNumber()).thenReturn(TestConstants.VALID_POOL_NUMBER);
            Appearance appearance2 = mockAppearance(LocalDate.of(2023, 1, 5), new BigDecimal("4.2"), 1L);
            Appearance appearance3 = mockAppearance(LocalDate.of(2023, 1, 6), new BigDecimal("5.2"), 3L);

            Juror juror = mock(Juror.class);
            when(juror.getFirstName()).thenReturn("John2");
            when(juror.getLastName()).thenReturn("Doe2");
            doReturn(juror).when(jurorExpenseService).getJuror(TestConstants.VALID_JUROR_NUMBER);
            doReturn(false).when(jurorExpenseService).validateUserCanApprove(
                List.of(appearance1, appearance2, appearance3)
            );

            assertThat(jurorExpenseService.mapAppearancesToPendingApprovalSinglePool(
                List.of(appearance1, appearance2, appearance3), true))
                .isEqualTo(PendingApproval.builder()
                    .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                    .poolNumber(TestConstants.VALID_POOL_NUMBER)
                    .firstName("John2")
                    .lastName("Doe2")
                    .amountDue(new BigDecimal("12.6"))
                    .expenseType(ExpenseType.FOR_REAPPROVAL)
                    .canApprove(false)
                    .dateToRevisions(
                        List.of(
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 4))
                                .version(3L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 5))
                                .version(1L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 6))
                                .version(3L)
                                .build()
                        )
                    )
                    .build());

            verify(jurorExpenseService, times(1)).getJuror(TestConstants.VALID_JUROR_NUMBER);
            verify(jurorExpenseService, times(1)).validateUserCanApprove(
                List.of(appearance1, appearance2, appearance3)
            );

        }
    }

    @Nested
    @DisplayName("PayAttendanceType calculatePayAttendanceType(LocalTime totalTimeForDay)")
    class CalculatePayAttendanceType {
        private Appearance mockAppearance(boolean isFullDay) {
            Appearance appearance = mock(Appearance.class);
            doReturn(isFullDay).when(appearance).isFullDay();
            return appearance;
        }

        @Test
        void positiveHalfDay() {
            assertThat(jurorExpenseService.calculatePayAttendanceType(mockAppearance(false)))
                .isEqualTo(PayAttendanceType.HALF_DAY);
        }


        @Test
        void positiveFullDay() {
            assertThat(jurorExpenseService.calculatePayAttendanceType(mockAppearance(true)))
                .isEqualTo(PayAttendanceType.FULL_DAY);
        }
    }

    @Nested
    @DisplayName(" public void applyDefaultExpenses(Appearance appearance, Juror juror)")
    class ApplyDefaultExpenses {
        @Test
        void positiveLongDayClaimingSubsistenceAllowance() {
            PayAttendanceType payAttendanceType = PayAttendanceType.FULL_DAY;
            LocalTime effectiveTime = LocalTime.of(10, 0, 1);
            doReturn(payAttendanceType).when(jurorExpenseService)
                .calculatePayAttendanceType(any());
            doNothing().when(jurorExpenseService)
                .updateMilesTraveledAndTravelDue(any(), any());
            doNothing().when(jurorExpenseService)
                .updateFoodDrinkClaimType(any(), any());
            doReturn(null).when(jurorExpenseService)
                .validateAndUpdateFinancialLossExpenseLimit(any());
            doReturn(true).when(jurorExpenseService)
                .isAttendanceDay(any());

            Juror juror = mock(Juror.class);
            doReturn(5).when(juror).getMileage();
            LocalTime travelTime = LocalTime.of(1, 2, 3);
            doReturn(travelTime).when(juror).getTravelTime();
            doReturn(true).when(juror).isClaimingSubsistenceAllowance();
            doReturn(null).when(juror).getFinancialLoss();

            Appearance appearance = mock(Appearance.class);
            doReturn(AttendanceType.FULL_DAY).when(appearance).getAttendanceType();
            doReturn(effectiveTime).when(appearance).getEffectiveTime();


            jurorExpenseService.applyDefaultExpenses(appearance, juror);

            verify(jurorExpenseService, times(1))
                .calculatePayAttendanceType(appearance);
            verify(jurorExpenseService, times(1))
                .updateMilesTraveledAndTravelDue(appearance, 5);
            verify(jurorExpenseService, times(1))
                .updateFoodDrinkClaimType(appearance, FoodDrinkClaimType.MORE_THAN_10_HOURS);
            verify(jurorExpenseService, times(1))
                .validateAndUpdateFinancialLossExpenseLimit(appearance);
            verify(jurorExpenseService, times(1))
                .isAttendanceDay(appearance);

            verify(appearance, times(1)).getAttendanceType();
            verify(appearance, times(1)).getEffectiveTime();
            verify(appearance, times(1)).setTravelTime(travelTime);
            verify(appearance, times(1)).setLossOfEarningsDue(null);
            verify(appearance, times(1)).setPayAttendanceType(payAttendanceType);
            verify(juror, times(1)).getMileage();
            verify(juror, times(1)).getTravelTime();
            verify(juror, times(1)).isClaimingSubsistenceAllowance();
            verify(juror, times(1)).getFinancialLoss();

            verify(jurorExpenseService, times(1)).applyDefaultExpenses(
                appearance, juror
            );//Required for noMoreInteractions
            verifyNoMoreInteractions(juror, appearance, jurorExpenseService);
        }

        @Test
        void positiveStandardDayclaimingSubsistenceAllowance() {
            PayAttendanceType payAttendanceType = PayAttendanceType.FULL_DAY;
            LocalTime effectiveTime = LocalTime.of(10, 0, 0);
            doReturn(payAttendanceType).when(jurorExpenseService)
                .calculatePayAttendanceType(any());
            doNothing().when(jurorExpenseService)
                .updateMilesTraveledAndTravelDue(any(), any());
            doNothing().when(jurorExpenseService)
                .updateFoodDrinkClaimType(any(), any());
            doReturn(null).when(jurorExpenseService)
                .validateAndUpdateFinancialLossExpenseLimit(any());
            doReturn(true).when(jurorExpenseService)
                .isAttendanceDay(any());

            Juror juror = mock(Juror.class);
            doReturn(5).when(juror).getMileage();
            LocalTime travelTime = LocalTime.of(1, 2, 3);
            doReturn(travelTime).when(juror).getTravelTime();
            doReturn(true).when(juror).isClaimingSubsistenceAllowance();
            doReturn(null).when(juror).getFinancialLoss();

            Appearance appearance = mock(Appearance.class);
            doReturn(AttendanceType.FULL_DAY).when(appearance).getAttendanceType();
            doReturn(effectiveTime).when(appearance).getEffectiveTime();

            jurorExpenseService.applyDefaultExpenses(appearance, juror);

            verify(jurorExpenseService, times(1))
                .calculatePayAttendanceType(appearance);
            verify(jurorExpenseService, times(1))
                .updateMilesTraveledAndTravelDue(appearance, 5);
            verify(jurorExpenseService, times(1))
                .updateFoodDrinkClaimType(appearance, FoodDrinkClaimType.LESS_THAN_OR_EQUAL_TO_10_HOURS);
            verify(jurorExpenseService, times(1))
                .validateAndUpdateFinancialLossExpenseLimit(appearance);
            verify(jurorExpenseService, times(1))
                .isAttendanceDay(appearance);

            verify(appearance, times(1)).getAttendanceType();
            verify(appearance, times(1)).getEffectiveTime();
            verify(appearance, times(1)).setTravelTime(travelTime);
            verify(appearance, times(1)).setLossOfEarningsDue(null);
            verify(appearance, times(1)).setPayAttendanceType(payAttendanceType);
            verify(juror, times(1)).getMileage();
            verify(juror, times(1)).getTravelTime();
            verify(juror, times(1)).isClaimingSubsistenceAllowance();
            verify(juror, times(1)).getFinancialLoss();

            verify(jurorExpenseService, times(1)).applyDefaultExpenses(
                appearance, juror
            );//Required for noMoreInteractions
            verifyNoMoreInteractions(juror, appearance, jurorExpenseService);
        }


        @Test
        void positiveNonAttendanceDayClaimingSubsistenceAllowance() {
            PayAttendanceType payAttendanceType = PayAttendanceType.FULL_DAY;
            LocalTime effectiveTime = LocalTime.of(10, 0, 0);
            doReturn(payAttendanceType).when(jurorExpenseService)
                .calculatePayAttendanceType(any());
            doNothing().when(jurorExpenseService)
                .updateMilesTraveledAndTravelDue(any(), any());
            doNothing().when(jurorExpenseService)
                .updateFoodDrinkClaimType(any(), any());
            doReturn(null).when(jurorExpenseService)
                .validateAndUpdateFinancialLossExpenseLimit(any());
            doReturn(false).when(jurorExpenseService)
                .isAttendanceDay(any());

            Juror juror = mock(Juror.class);
            doReturn(5).when(juror).getMileage();
            LocalTime travelTime = LocalTime.of(1, 2, 3);
            doReturn(travelTime).when(juror).getTravelTime();
            doReturn(true).when(juror).isClaimingSubsistenceAllowance();
            doReturn(null).when(juror).getFinancialLoss();

            Appearance appearance = mock(Appearance.class);
            doReturn(AttendanceType.NON_ATTENDANCE).when(appearance).getAttendanceType();
            doReturn(effectiveTime).when(appearance).getEffectiveTime();


            jurorExpenseService.applyDefaultExpenses(appearance, juror);

            verify(jurorExpenseService, times(1))
                .calculatePayAttendanceType(appearance);
            verify(jurorExpenseService, times(1))
                .updateMilesTraveledAndTravelDue(appearance, 5);
            verify(jurorExpenseService, times(1))
                .updateFoodDrinkClaimType(appearance, FoodDrinkClaimType.NONE);
            verify(jurorExpenseService, times(1))
                .validateAndUpdateFinancialLossExpenseLimit(appearance);
            verify(jurorExpenseService, times(1))
                .isAttendanceDay(appearance);

            verify(appearance, times(1)).getAttendanceType();
            verify(appearance, times(1)).getEffectiveTime();
            verify(appearance, times(1)).setTravelTime(travelTime);
            verify(appearance, times(1)).setLossOfEarningsDue(null);
            verify(appearance, times(1)).setPayAttendanceType(payAttendanceType);
            verify(juror, times(1)).getMileage();
            verify(juror, times(1)).getTravelTime();
            verify(juror, never()).isClaimingSubsistenceAllowance();
            verify(juror, times(1)).getFinancialLoss();

            verify(jurorExpenseService, times(1)).applyDefaultExpenses(
                appearance, juror
            );//Required for noMoreInteractions
            verifyNoMoreInteractions(juror, appearance, jurorExpenseService);
        }

        @Test
        void positiveNotClaimingSubsistenceAllowance() {
            PayAttendanceType payAttendanceType = PayAttendanceType.FULL_DAY;
            LocalTime effectiveTime = LocalTime.of(10, 0, 0);
            doReturn(payAttendanceType).when(jurorExpenseService)
                .calculatePayAttendanceType(any());
            doNothing().when(jurorExpenseService)
                .updateMilesTraveledAndTravelDue(any(), any());
            doNothing().when(jurorExpenseService)
                .updateFoodDrinkClaimType(any(), any());
            doReturn(null).when(jurorExpenseService)
                .validateAndUpdateFinancialLossExpenseLimit(any());
            doReturn(true).when(jurorExpenseService)
                .isAttendanceDay(any());

            Juror juror = mock(Juror.class);
            doReturn(5).when(juror).getMileage();
            LocalTime travelTime = LocalTime.of(1, 2, 3);
            doReturn(travelTime).when(juror).getTravelTime();
            doReturn(false).when(juror).isClaimingSubsistenceAllowance();
            doReturn(null).when(juror).getFinancialLoss();

            Appearance appearance = mock(Appearance.class);
            doReturn(effectiveTime).when(appearance).getEffectiveTime();
            doReturn(AttendanceType.FULL_DAY).when(appearance).getAttendanceType();
            doReturn(payAttendanceType).when(appearance).getPayAttendanceType();

            jurorExpenseService.applyDefaultExpenses(appearance, juror);

            verify(jurorExpenseService, times(1))
                .calculatePayAttendanceType(appearance);
            verify(jurorExpenseService, times(1))
                .updateMilesTraveledAndTravelDue(appearance, 5);
            verify(jurorExpenseService, times(1))
                .updateFoodDrinkClaimType(appearance, FoodDrinkClaimType.NONE);
            verify(jurorExpenseService, times(1))
                .validateAndUpdateFinancialLossExpenseLimit(appearance);
            verify(jurorExpenseService, times(1))
                .isAttendanceDay(appearance);

            verify(appearance, times(1)).getAttendanceType();
            verify(appearance, times(1)).getEffectiveTime();
            verify(appearance, times(1)).setTravelTime(travelTime);
            verify(appearance, times(1)).setLossOfEarningsDue(null);
            verify(appearance, times(1)).setPayAttendanceType(payAttendanceType);
            verify(juror, times(1)).getMileage();
            verify(juror, times(1)).getTravelTime();
            verify(juror, times(1)).isClaimingSubsistenceAllowance();
            verify(juror, times(1)).getFinancialLoss();

            verify(jurorExpenseService, times(1)).applyDefaultExpenses(
                appearance, juror
            );//Required for noMoreInteractions
            verifyNoMoreInteractions(juror, appearance, jurorExpenseService);
        }

        @Test
        void positiveHasFinancialLossPayAttendanceFullDay() {
            PayAttendanceType payAttendanceType = PayAttendanceType.FULL_DAY;
            LocalTime effectiveTime = LocalTime.of(10, 0, 0);
            doReturn(payAttendanceType).when(jurorExpenseService)
                .calculatePayAttendanceType(any());
            doNothing().when(jurorExpenseService)
                .updateMilesTraveledAndTravelDue(any(), any());
            doNothing().when(jurorExpenseService)
                .updateFoodDrinkClaimType(any(), any());
            doReturn(null).when(jurorExpenseService)
                .validateAndUpdateFinancialLossExpenseLimit(any());
            doReturn(true).when(jurorExpenseService)
                .isAttendanceDay(any());

            Juror juror = mock(Juror.class);
            doReturn(5).when(juror).getMileage();
            LocalTime travelTime = LocalTime.of(1, 2, 3);
            doReturn(travelTime).when(juror).getTravelTime();
            doReturn(false).when(juror).isClaimingSubsistenceAllowance();
            doReturn(new BigDecimal("50.00")).when(juror).getFinancialLoss();

            Appearance appearance = mock(Appearance.class);
            doReturn(effectiveTime).when(appearance).getEffectiveTime();
            doReturn(AttendanceType.FULL_DAY).when(appearance).getAttendanceType();
            doReturn(payAttendanceType).when(appearance).getPayAttendanceType();


            jurorExpenseService.applyDefaultExpenses(appearance, juror);

            verify(jurorExpenseService, times(1))
                .calculatePayAttendanceType(appearance);
            verify(jurorExpenseService, times(1))
                .updateMilesTraveledAndTravelDue(appearance, 5);
            verify(jurorExpenseService, times(1))
                .updateFoodDrinkClaimType(appearance, FoodDrinkClaimType.NONE);
            verify(jurorExpenseService, times(1))
                .validateAndUpdateFinancialLossExpenseLimit(appearance);
            verify(jurorExpenseService, times(1))
                .isAttendanceDay(appearance);

            verify(appearance, times(1)).getAttendanceType();
            verify(appearance, times(1)).getEffectiveTime();
            verify(appearance, times(1)).setTravelTime(travelTime);
            verify(appearance, times(1)).setLossOfEarningsDue(new BigDecimal("50.00"));
            verify(appearance, times(1)).setPayAttendanceType(payAttendanceType);
            verify(appearance, times(1)).getPayAttendanceType();
            verify(juror, times(1)).getMileage();
            verify(juror, times(1)).getTravelTime();
            verify(juror, times(1)).isClaimingSubsistenceAllowance();
            verify(juror, times(2)).getFinancialLoss();

            verify(jurorExpenseService, times(1)).applyDefaultExpenses(
                appearance, juror
            );//Required for noMoreInteractions
            verifyNoMoreInteractions(juror, appearance, jurorExpenseService);
        }

        @Test
        void positiveHasFinancialLossPayAttendanceHalfDay() {
            PayAttendanceType payAttendanceType = PayAttendanceType.HALF_DAY;
            LocalTime effectiveTime = LocalTime.of(10, 0, 0);
            doReturn(payAttendanceType).when(jurorExpenseService)
                .calculatePayAttendanceType(any());
            doNothing().when(jurorExpenseService)
                .updateMilesTraveledAndTravelDue(any(), any());
            doNothing().when(jurorExpenseService)
                .updateFoodDrinkClaimType(any(), any());
            doReturn(null).when(jurorExpenseService)
                .validateAndUpdateFinancialLossExpenseLimit(any());
            doReturn(true).when(jurorExpenseService)
                .isAttendanceDay(any());

            Juror juror = mock(Juror.class);
            doReturn(5).when(juror).getMileage();
            LocalTime travelTime = LocalTime.of(1, 2, 3);
            doReturn(travelTime).when(juror).getTravelTime();
            doReturn(false).when(juror).isClaimingSubsistenceAllowance();
            doReturn(new BigDecimal("50.00")).when(juror).getFinancialLoss();

            Appearance appearance = mock(Appearance.class);
            doReturn(AttendanceType.HALF_DAY).when(appearance).getAttendanceType();
            doReturn(effectiveTime).when(appearance).getEffectiveTime();
            doReturn(payAttendanceType).when(appearance).getPayAttendanceType();


            jurorExpenseService.applyDefaultExpenses(appearance, juror);

            verify(jurorExpenseService, times(1))
                .calculatePayAttendanceType(appearance);
            verify(jurorExpenseService, times(1))
                .updateMilesTraveledAndTravelDue(appearance, 5);
            verify(jurorExpenseService, times(1))
                .updateFoodDrinkClaimType(appearance, FoodDrinkClaimType.NONE);
            verify(jurorExpenseService, times(1))
                .validateAndUpdateFinancialLossExpenseLimit(appearance);
            verify(jurorExpenseService, times(1))
                .isAttendanceDay(appearance);

            verify(appearance, times(1)).getAttendanceType();
            verify(appearance, times(1)).getEffectiveTime();
            verify(appearance, times(1)).setTravelTime(travelTime);
            verify(appearance, times(1)).setLossOfEarningsDue(new BigDecimal("25.00"));
            verify(appearance, times(1)).setPayAttendanceType(payAttendanceType);
            verify(appearance, times(1)).getPayAttendanceType();
            verify(juror, times(1)).getMileage();
            verify(juror, times(1)).getTravelTime();
            verify(juror, times(1)).isClaimingSubsistenceAllowance();
            verify(juror, times(2)).getFinancialLoss();

            verify(jurorExpenseService, times(1)).applyDefaultExpenses(
                appearance, juror
            );//Required for noMoreInteractions
            verifyNoMoreInteractions(juror, appearance, jurorExpenseService);
        }
    }

    @Nested
    @DisplayName("public void applyDefaultExpenses(List<Appearance> appearances)")
    class ApplyDefaultExpensesList {
        @Test
        void positiveTypical() {
            Juror juror = mock(Juror.class);
            doReturn(TestConstants.VALID_JUROR_NUMBER).when(juror).getJurorNumber();
            doReturn(juror).when(jurorExpenseService).getJuror(TestConstants.VALID_JUROR_NUMBER);

            doNothing().when(jurorExpenseService).saveAppearancesWithExpenseRateIdUpdate(anyCollection());
            doNothing().when(jurorExpenseService).applyDefaultExpenses(any(), any());
            Appearance appearance1 = mockAppearance(AttendanceType.FULL_DAY);
            Appearance appearance2 = mockAppearance(AttendanceType.FULL_DAY);
            Appearance appearance3 = mockAppearance(AttendanceType.FULL_DAY);
            jurorExpenseService.applyDefaultExpenses(
                List.of(appearance1, appearance2, appearance3));

            verify(jurorExpenseService, times(1))
                .applyDefaultExpenses(appearance1, juror);
            verify(jurorExpenseService, times(1))
                .applyDefaultExpenses(appearance2, juror);
            verify(jurorExpenseService, times(1))
                .applyDefaultExpenses(appearance3, juror);
            verify(jurorExpenseService, times(1)).saveAppearancesWithExpenseRateIdUpdate(
                List.of(appearance1, appearance2, appearance3)
            );
        }

        @Test
        void positiveNullAppearances() {
            jurorExpenseService.applyDefaultExpenses(null);
            verify(jurorExpenseService, never())
                .applyDefaultExpenses(any(), any());
            verify(appearanceRepository, never()).saveAll(any());
        }


        @Test
        void positiveEmptyAppearances() {
            jurorExpenseService.applyDefaultExpenses(List.of());
            verify(jurorExpenseService, never())
                .applyDefaultExpenses(any(), any());
            verify(appearanceRepository, never()).saveAll(any());
        }

        @Test
        void positiveAbsentAppearance() {
            Juror juror = mock(Juror.class);
            doReturn(TestConstants.VALID_JUROR_NUMBER).when(juror).getJurorNumber();
            doReturn(juror).when(jurorExpenseService).getJuror(TestConstants.VALID_JUROR_NUMBER);

            doNothing().when(jurorExpenseService).saveAppearancesWithExpenseRateIdUpdate(anyCollection());
            doNothing().when(jurorExpenseService).applyDefaultExpenses(any(), any());
            Appearance appearance1 = mockAppearance(AttendanceType.FULL_DAY);
            Appearance appearance2 = mockAppearance(AttendanceType.ABSENT);
            jurorExpenseService.applyDefaultExpenses(
                List.of(appearance1, appearance2));

            verify(jurorExpenseService, times(1))
                .applyDefaultExpenses(appearance1, juror);
            verify(jurorExpenseService, times(1))
                .applyDefaultExpenses(appearance2, juror);
            verify(jurorExpenseService, times(1)).saveAppearancesWithExpenseRateIdUpdate(
                List.of(appearance1)
            );
        }

        private Appearance mockAppearance(AttendanceType attendanceType) {
            Appearance appearance = mock(Appearance.class);
            doReturn(TestConstants.VALID_JUROR_NUMBER).when(appearance).getJurorNumber();
            doReturn(AppearanceStage.EXPENSE_ENTERED).when(appearance).getAppearanceStage();
            doReturn(attendanceType).when(appearance).getAttendanceType();
            doReturn(true).when(appearance).isDraftExpense();
            return appearance;
        }
    }

    @Nested
    @DisplayName("public boolean isLongTrialDay(String jurorNumber, String poolNumber, LocalDate localDate)")
    class IsLongTrialDay {
        @ParameterizedTest
        @ValueSource(ints = {
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9
        })
        void positiveFalse(int offset) {
            assertTest(offset, false);
        }

        @ParameterizedTest
        @ValueSource(ints = {
            10, 11, 12, 13, 14, 15
        })
        void positiveTrue(int offset) {
            assertTest(offset, true);
        }

        private void assertTest(int offset, boolean expectedValue) {
            final LocalDate baseDay = LocalDate.of(2023, 1, 1);
            LocalDate searchDate = baseDay.plusDays(offset);

            List<Appearance> appearances = new ArrayList<>();
            for (int i = 0; i < 15; i++) {
                Appearance appearance = mock(Appearance.class);
                doReturn(baseDay.plusDays(i)).when(appearance).getAttendanceDate();
                appearances.add(appearance);
            }
            when(appearanceRepository.findAllByCourtLocationLocCodeAndJurorNumber(
                TestConstants.VALID_COURT_LOCATION,
                TestConstants.VALID_JUROR_NUMBER
            )).thenReturn(appearances);

            assertThat(jurorExpenseService.isLongTrialDay(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, searchDate))
                .isEqualTo(expectedValue);

            verify(appearanceRepository, times(
                1)).findAllByCourtLocationLocCodeAndJurorNumber(
                TestConstants.VALID_COURT_LOCATION,
                TestConstants.VALID_JUROR_NUMBER
            );
        }
    }

    @Test
    void positiveValidateExpenseTest() {
        Appearance appearance = mock(Appearance.class);
        doNothing().when(jurorExpenseService).checkTotalExpensesAreNotLessThan0(appearance);
        doNothing().when(jurorExpenseService).checkDueIsMoreThanPaid(appearance);
        jurorExpenseService.validateExpense(appearance);
        verify(jurorExpenseService, times(1)).checkTotalExpensesAreNotLessThan0(appearance);
        verify(jurorExpenseService, times(1)).checkDueIsMoreThanPaid(appearance);
        verify(jurorExpenseService, times(1)).validateExpense(appearance);
        verifyNoMoreInteractions(jurorExpenseService);
    }

    @Nested
    @DisplayName("public void apportionSmartCard(ApportionSmartCardRequest dto)")
    class ApportionSmartCard {

        @BeforeEach
        void mockCurrentUser() {
            securityUtilMockedStatic = mockStatic(SecurityUtil.class);
            securityUtilMockedStatic.when(SecurityUtil::getActiveOwner)
                .thenReturn(TestConstants.VALID_COURT_LOCATION);
        }

        @AfterEach
        void afterEach() {
            if (securityUtilMockedStatic != null) {
                securityUtilMockedStatic.close();
            }
        }


        private Appearance mockAppearance(CourtLocation courtLocation, boolean isDraft) {
            Appearance appearance = mock(Appearance.class);
            doReturn(courtLocation).when(appearance).getCourtLocation();
            doReturn(isDraft).when(appearance).isDraftExpense();
            return appearance;
        }

        @Test
        void positiveTypical() {
            List<LocalDate> attendanceDates = List.of(
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 1, 2),
                LocalDate.of(2023, 1, 3)
            );
            final ApportionSmartCardRequest dto = ApportionSmartCardRequest.builder()
                .smartCardAmount(new BigDecimal("90.00"))
                .attendanceDates(
                    attendanceDates
                )
                .build();
            CourtLocation courtLocation = mock(CourtLocation.class);
            doReturn(TestConstants.VALID_COURT_LOCATION).when(courtLocation).getOwner();

            Appearance appearance1 = mockAppearance(courtLocation, true);
            Appearance appearance2 = mockAppearance(courtLocation, true);
            Appearance appearance3 = mockAppearance(courtLocation, true);
            List<Appearance> appearances = List.of(
                appearance1, appearance2, appearance3
            );
            doReturn(appearances).when(jurorExpenseService).getAppearances(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, attendanceDates);
            doNothing().when(jurorExpenseService).validateExpense(any());
            doNothing().when(jurorExpenseService).saveAppearancesWithExpenseRateIdUpdate(anyCollection());

            jurorExpenseService.apportionSmartCard(TestConstants.VALID_COURT_LOCATION,
                TestConstants.VALID_JUROR_NUMBER, dto);

            verify(jurorExpenseService, times(1)).getAppearances(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, attendanceDates
            );
            verify(jurorExpenseService, times(1)).validateExpense(appearance1);
            verify(jurorExpenseService, times(1)).validateExpense(appearance2);
            verify(jurorExpenseService, times(1)).validateExpense(appearance3);

            verify(appearance1, times(1)).setSmartCardAmountDue(new BigDecimal("30.00"));
            verify(appearance2, times(1)).setSmartCardAmountDue(new BigDecimal("30.00"));
            verify(appearance3, times(1)).setSmartCardAmountDue(new BigDecimal("30.00"));

            verify(jurorExpenseService, times(1)).saveAppearancesWithExpenseRateIdUpdate(appearances);
        }

        @Test
        void positiveTypicalWithRoundingOffset() {
            List<LocalDate> attendanceDates = List.of(
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 1, 2),
                LocalDate.of(2023, 1, 3)
            );
            final ApportionSmartCardRequest dto = ApportionSmartCardRequest.builder()
                .smartCardAmount(new BigDecimal("100.00"))
                .attendanceDates(
                    attendanceDates
                )
                .build();
            CourtLocation courtLocation = mock(CourtLocation.class);
            doReturn(TestConstants.VALID_COURT_LOCATION).when(courtLocation).getOwner();

            Appearance appearance1 = mockAppearance(courtLocation, true);
            Appearance appearance2 = mockAppearance(courtLocation, true);
            Appearance appearance3 = mockAppearance(courtLocation, true);
            List<Appearance> appearances = List.of(
                appearance1, appearance2, appearance3
            );
            doReturn(appearances).when(jurorExpenseService).getAppearances(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, attendanceDates);
            doNothing().when(jurorExpenseService).validateExpense(any());
            doNothing().when(jurorExpenseService).saveAppearancesWithExpenseRateIdUpdate(anyCollection());

            jurorExpenseService.apportionSmartCard(TestConstants.VALID_COURT_LOCATION,
                TestConstants.VALID_JUROR_NUMBER, dto);

            verify(jurorExpenseService, times(1)).getAppearances(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, attendanceDates);
            verify(jurorExpenseService, times(1)).validateExpense(appearance1);
            verify(jurorExpenseService, times(1)).validateExpense(appearance2);
            verify(jurorExpenseService, times(1)).validateExpense(appearance3);

            verify(appearance1, times(1)).setSmartCardAmountDue(new BigDecimal("33.33"));
            verify(appearance2, times(1)).setSmartCardAmountDue(new BigDecimal("33.33"));
            verify(appearance3, times(1)).setSmartCardAmountDue(new BigDecimal("33.34"));

            verify(jurorExpenseService, times(1)).saveAppearancesWithExpenseRateIdUpdate(appearances);

        }

        @Test
        void negativeWrongOwner() {
            List<LocalDate> attendanceDates = List.of(
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 1, 2),
                LocalDate.of(2023, 1, 3)
            );
            ApportionSmartCardRequest dto = ApportionSmartCardRequest.builder()
                .smartCardAmount(new BigDecimal("100.00"))
                .attendanceDates(
                    attendanceDates
                )
                .build();
            CourtLocation courtLocation = mock(CourtLocation.class);
            doReturn(TestConstants.VALID_COURT_LOCATION).when(courtLocation).getOwner();

            Appearance appearance1 = mockAppearance(courtLocation, true);
            Appearance appearance2 = mockAppearance(courtLocation, true);
            Appearance appearance3 = mockAppearance(courtLocation, true);
            List<Appearance> appearances = List.of(
                appearance1, appearance2, appearance3
            );
            doReturn(appearances).when(jurorExpenseService).getAppearances(TestConstants.VALID_COURT_LOCATION,
                TestConstants.VALID_JUROR_NUMBER, attendanceDates);

            securityUtilMockedStatic.when(SecurityUtil::getActiveOwner)
                .thenReturn("414");

            MojException.Forbidden exception = assertThrows(MojException.Forbidden.class,
                () -> jurorExpenseService.apportionSmartCard(
                    TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, dto),
                "Should throw exception when juror tries to access a pool they do not have access to");
            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage())
                .isEqualTo("User cannot access this juror pool");

        }

        @Test
        void negativeEditingNonDraftDays() {
            List<LocalDate> attendanceDates = List.of(
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 1, 2),
                LocalDate.of(2023, 1, 3)
            );
            ApportionSmartCardRequest dto = ApportionSmartCardRequest.builder()
                .smartCardAmount(new BigDecimal("100.00"))
                .attendanceDates(
                    attendanceDates
                )
                .build();
            CourtLocation courtLocation = mock(CourtLocation.class);
            doReturn(TestConstants.VALID_COURT_LOCATION).when(courtLocation).getOwner();

            Appearance appearance1 = mockAppearance(courtLocation, true);
            Appearance appearance2 = mockAppearance(courtLocation, false);
            Appearance appearance3 = mockAppearance(courtLocation, true);
            List<Appearance> appearances = List.of(
                appearance1, appearance2, appearance3
            );
            doReturn(appearances).when(jurorExpenseService).getAppearances(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, attendanceDates);

            MojException.BusinessRuleViolation exception = assertThrows(MojException.BusinessRuleViolation.class,
                () -> jurorExpenseService.apportionSmartCard(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER, dto),
                "Should throw exception when juror tries apportion non draft expenses");
            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getErrorCode()).isEqualTo(
                MojException.BusinessRuleViolation.ErrorCode.APPORTION_SMART_CARD_NON_DRAFT_DAYS);
            assertThat(exception.getMessage())
                .isEqualTo("Can not apportion smart card for non-draft days");
        }
    }

    @Nested
    @DisplayName("List<Appearance> getAppearances(ExpenseItemsDto dto)")
    class GetAppearances {

        private Appearance mockAppearance(LocalDate attendanceDate) {
            Appearance appearance = mock(Appearance.class);
            doReturn(attendanceDate).when(appearance).getAttendanceDate();
            return appearance;
        }

        @Test
        void positiveTypical() {
            List<LocalDate> attendanceDates = List.of(
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 1, 2),
                LocalDate.of(2023, 1, 3)
            );

            List<Appearance> appearances = new ArrayList<>(List.of(
                mockAppearance(LocalDate.of(2023, 1, 1)),
                mockAppearance(LocalDate.of(2023, 1, 2)),
                mockAppearance(LocalDate.of(2023, 1, 3))
            ));
            doReturn(appearances).when(appearanceRepository)
                .findAllByCourtLocationLocCodeAndJurorNumberAndAttendanceDateIn(
                    TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, attendanceDates);

            assertThat(jurorExpenseService.getAppearances(TestConstants.VALID_COURT_LOCATION,
                TestConstants.VALID_JUROR_NUMBER, attendanceDates))
                .isEqualTo(appearances);

            verify(appearanceRepository, times(1)).findAllByCourtLocationLocCodeAndJurorNumberAndAttendanceDateIn(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, attendanceDates
            );
        }

        @Test
        void negativeIsEmpty() {
            List<LocalDate> attendanceDates = List.of(
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 1, 2),
                LocalDate.of(2023, 1, 3)
            );

            doReturn(List.of()).when(appearanceRepository)
                .findAllByCourtLocationLocCodeAndJurorNumberAndAttendanceDateIn(
                    TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, attendanceDates);


            MojException.NotFound exception = assertThrows(MojException.NotFound.class,
                () -> jurorExpenseService.getAppearances(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER, attendanceDates),
                "Expect exception to be thrown when no appearances are found"
            );
            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage()).isEqualTo(
                "One or more appearance records not found for Loc code: 415, Juror Number: 123456789 and Attendance "
                    + "Dates provided");


            verify(appearanceRepository, times(1)).findAllByCourtLocationLocCodeAndJurorNumberAndAttendanceDateIn(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, attendanceDates
            );
        }

        @Test
        void negativeSizeDoNoMatch() {
            List<LocalDate> attendanceDates = List.of(
                LocalDate.of(2023, 1, 1),
                LocalDate.of(2023, 1, 2),
                LocalDate.of(2023, 1, 3)
            );
            doReturn(new ArrayList<>(List.of(
                mockAppearance(LocalDate.of(2023, 1, 1)),
                mockAppearance(LocalDate.of(2023, 1, 2))
            ))).when(appearanceRepository).findAllByCourtLocationLocCodeAndJurorNumberAndAttendanceDateIn(
                TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, attendanceDates
            );


            MojException.NotFound exception = assertThrows(MojException.NotFound.class,
                () -> jurorExpenseService.getAppearances(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER, attendanceDates),
                "Expect exception to be thrown when no appearances are found"
            );
            assertThat(exception).isNotNull();
            assertThat(exception.getCause()).isNull();
            assertThat(exception.getMessage()).isEqualTo(
                "One or more appearance records not found for Loc code: 415, Juror Number: 123456789 and Attendance "
                    + "Dates provided");


            verify(appearanceRepository, times(1))
                .findAllByCourtLocationLocCodeAndJurorNumberAndAttendanceDateIn(
                    TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, attendanceDates
                );
        }
    }

    @Test
    void positiveUpdateExpenseRates() {
        ExpenseRatesDto expenseRatesDto = ExpenseRatesDto.builder()
            .carMileageRatePerMile0Passengers(new BigDecimal("1.01"))
            .carMileageRatePerMile1Passengers(new BigDecimal("1.02"))
            .carMileageRatePerMile2OrMorePassengers(new BigDecimal("1.03"))
            .motorcycleMileageRatePerMile0Passengers(new BigDecimal("1.04"))
            .motorcycleMileageRatePerMile1Passengers(new BigDecimal("1.05"))
            .bikeRate(new BigDecimal("1.06"))
            .limitFinancialLossHalfDay(new BigDecimal("1.07"))
            .limitFinancialLossFullDay(new BigDecimal("1.08"))
            .limitFinancialLossHalfDayLongTrial(new BigDecimal("1.09"))
            .limitFinancialLossFullDayLongTrial(new BigDecimal("1.10"))
            .subsistenceRateStandard(new BigDecimal("1.11"))
            .subsistenceRateLongDay(new BigDecimal("1.12"))
            .build();

        ArgumentCaptor<ExpenseRates> expenseRatesArgumentCaptor = ArgumentCaptor.forClass(ExpenseRates.class);
        jurorExpenseService.updateExpenseRates(expenseRatesDto);

        verify(expenseRatesRepository, times(1)).save(expenseRatesArgumentCaptor.capture());

        ExpenseRates expenseRates = expenseRatesArgumentCaptor.getValue();
        assertThat(expenseRates.getCarMileageRatePerMile0Passengers()).isEqualTo(new BigDecimal("1.01"));
        assertThat(expenseRates.getCarMileageRatePerMile1Passengers()).isEqualTo(new BigDecimal("1.02"));
        assertThat(expenseRates.getCarMileageRatePerMile2OrMorePassengers()).isEqualTo(new BigDecimal("1.03"));
        assertThat(expenseRates.getMotorcycleMileageRatePerMile0Passengers()).isEqualTo(new BigDecimal("1.04"));
        assertThat(expenseRates.getMotorcycleMileageRatePerMile1Passengers()).isEqualTo(new BigDecimal("1.05"));
        assertThat(expenseRates.getBikeRate()).isEqualTo(new BigDecimal("1.06"));
        assertThat(expenseRates.getLimitFinancialLossHalfDay()).isEqualTo(new BigDecimal("1.07"));
        assertThat(expenseRates.getLimitFinancialLossFullDay()).isEqualTo(new BigDecimal("1.08"));
        assertThat(expenseRates.getLimitFinancialLossHalfDayLongTrial()).isEqualTo(new BigDecimal("1.09"));
        assertThat(expenseRates.getLimitFinancialLossFullDayLongTrial()).isEqualTo(new BigDecimal("1.10"));
        assertThat(expenseRates.getSubsistenceRateStandard()).isEqualTo(new BigDecimal("1.11"));
        assertThat(expenseRates.getSubsistenceRateLongDay()).isEqualTo(new BigDecimal("1.12"));
        assertThat(expenseRates.getRatesEffectiveFrom()).isNotNull();

    }

    @Nested
    @DisplayName("public ExpenseRates getCurrentExpenseRates()")
    class GetCurrentExpenseRates {

        ExpenseRates getExpenseRates() {
            return ExpenseRates.builder()
                .carMileageRatePerMile0Passengers(new BigDecimal("1.01"))
                .carMileageRatePerMile1Passengers(new BigDecimal("1.02"))
                .carMileageRatePerMile2OrMorePassengers(new BigDecimal("1.03"))
                .motorcycleMileageRatePerMile0Passengers(new BigDecimal("1.04"))
                .motorcycleMileageRatePerMile1Passengers(new BigDecimal("1.05"))
                .bikeRate(new BigDecimal("1.06"))
                .limitFinancialLossHalfDay(new BigDecimal("1.07"))
                .limitFinancialLossFullDay(new BigDecimal("1.08"))
                .limitFinancialLossHalfDayLongTrial(new BigDecimal("1.09"))
                .limitFinancialLossFullDayLongTrial(new BigDecimal("1.10"))
                .subsistenceRateStandard(new BigDecimal("1.11"))
                .subsistenceRateLongDay(new BigDecimal("1.12"))
                .build();
        }

        @Test
        void positiveAll() {
            ExpenseRates expenseRates = getExpenseRates();
            when(expenseRatesRepository.getCurrentRates()).thenReturn(expenseRates);

            assertThat(jurorExpenseService.getCurrentExpenseRates(false))
                .isEqualTo(expenseRates);
            verify(expenseRatesRepository, times(1)).getCurrentRates();
        }

        @Test
        void positiveOnlyFinancialLoss() {
            ExpenseRates expenseRates = getExpenseRates();
            when(expenseRatesRepository.getCurrentRates()).thenReturn(expenseRates);

            assertThat(jurorExpenseService.getCurrentExpenseRates(true))
                .isEqualTo(ExpenseRates.builder()
                    .limitFinancialLossHalfDay(new BigDecimal("1.07"))
                    .limitFinancialLossFullDay(new BigDecimal("1.08"))
                    .limitFinancialLossHalfDayLongTrial(new BigDecimal("1.09"))
                    .limitFinancialLossFullDayLongTrial(new BigDecimal("1.10"))
                    .build());
            verify(expenseRatesRepository, times(1)).getCurrentRates();
        }
    }
}

