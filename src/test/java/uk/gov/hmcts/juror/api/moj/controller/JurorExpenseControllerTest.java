package uk.gov.hmcts.juror.api.moj.controller;

import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorNumberAndPoolNumberDto;
import uk.gov.hmcts.juror.api.moj.controller.request.RequestDefaultExpensesDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ApportionSmartCardRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ApproveExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.CalculateTotalExpenseRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.CombinedExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseItemsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseType;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.GetEnteredExpenseRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpense;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseFinancialLoss;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseFoodAndDrink;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseTime;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseTravel;
import uk.gov.hmcts.juror.api.moj.controller.response.DefaultExpenseResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.CombinedSimplifiedExpenseDetailDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.DailyExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.ExpenseCount;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.FinancialLossWarningTest;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.GetEnteredExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.PendingApprovalList;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.SimplifiedExpenseDetailDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.UnpaidExpenseSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.SortDirection;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.FoodDrinkClaimType;
import uk.gov.hmcts.juror.api.moj.enumeration.PayAttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.PaymentMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.service.BulkService;
import uk.gov.hmcts.juror.api.moj.service.BulkServiceImpl;
import uk.gov.hmcts.juror.api.moj.service.expense.JurorExpenseServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.juror.api.JurorDigitalApplication.PAGE_SIZE;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = JurorExpenseController.class,
    excludeAutoConfiguration = {SecurityAutoConfiguration.class})
@ContextConfiguration(classes = {JurorExpenseController.class, RestResponseEntityExceptionHandler.class,
    BulkServiceImpl.class})
@DisplayName("Controller: " + JurorExpenseControllerTest.BASE_URL)
@SuppressWarnings({"PMD.ExcessiveImports",
    "PMD.LawOfDemeter"})
class JurorExpenseControllerTest {
    public static final String BASE_URL = "/api/v1/moj/expenses";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BulkService bulkService;

    @MockBean
    private JurorExpenseServiceImpl jurorExpenseService;
    @MockBean
    private AppearanceRepository appearanceRepository;
    @MockBean
    private JurorRepository jurorRepository;

    @Nested
    @DisplayName("GET " + BASE_URL + "/unpaid-summary/{locCode}")
    class UnpaidExpensesForCourtLocation {

        @Test
        @DisplayName("Valid court user - with date range filter")
        void happyPathForValidCourtUserWithDates() throws Exception {
            BureauJwtPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
            jwtPayload.setStaff(TestUtils.staffBuilder("Court User", 1,
                Collections.singletonList(TestConstants.VALID_COURT_LOCATION)));
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

            BigDecimal totalUnapproved = BigDecimal.valueOf(65.95).setScale(2, RoundingMode.HALF_UP);
            UnpaidExpenseSummaryResponseDto responseItem =
                createUnpaidExpenseSummaryResponseDto("111111111", totalUnapproved);
            Sort sort = Sort.by("jurorNumber").ascending();

            int pageNumber = 0;
            Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE, sort);

            Mockito.doReturn(new PageImpl<>(Collections.singletonList(responseItem), pageable, 1))
                .when(jurorExpenseService)
                .getUnpaidExpensesForCourtLocation(TestConstants.VALID_COURT_LOCATION,
                    LocalDate.of(2023, 1, 5), LocalDate.of(2023, 1, 19),
                    pageNumber, "jurorNumber", SortDirection.ASC);


            mockMvc.perform(get(String.format(BASE_URL + "/unpaid-summary/"
                    + TestConstants.VALID_COURT_LOCATION + "?min_date=2023-01-05&max_date=2023-01-19&page_number=0"
                    + "&sort_by=jurorNumber&sort_order=ASC"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].juror_number", CoreMatchers.is(responseItem.getJurorNumber())))
                .andExpect(jsonPath("$.content[0].pool_number", CoreMatchers.is(responseItem.getPoolNumber())))
                .andExpect(jsonPath("$.content[0].first_name", CoreMatchers.is(responseItem.getFirstName())))
                .andExpect(jsonPath("$.content[0].last_name", CoreMatchers.is(responseItem.getLastName())))
                .andExpect(jsonPath("$.content[0].total_unapproved", CoreMatchers.is(totalUnapproved.doubleValue())));

            verify(jurorExpenseService, times(1))
                .getUnpaidExpensesForCourtLocation(TestConstants.VALID_COURT_LOCATION,
                    LocalDate.of(2023, 1, 5), LocalDate.of(2023, 1, 19),
                    0, "jurorNumber", SortDirection.ASC);
        }

        @Test
        @DisplayName("Valid court user - without date range filter")
        void happyPathForValidCourtUserWithoutDate() throws Exception {
            BureauJwtPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
            jwtPayload.setStaff(TestUtils.staffBuilder("Court User", 1,
                Collections.singletonList(TestConstants.VALID_COURT_LOCATION)));
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

            BigDecimal totalUnapproved = BigDecimal.valueOf(65.95).setScale(2, RoundingMode.HALF_UP);
            UnpaidExpenseSummaryResponseDto responseItem =
                createUnpaidExpenseSummaryResponseDto("111111111", totalUnapproved);
            Sort sort = Sort.by("jurorNumber").ascending();

            int pageNumber = 0;
            Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE, sort);

            Mockito.doReturn(new PageImpl<>(Collections.singletonList(responseItem), pageable, 1))
                .when(jurorExpenseService)
                .getUnpaidExpensesForCourtLocation(TestConstants.VALID_COURT_LOCATION, null, null,
                    pageNumber, "jurorNumber", SortDirection.ASC);


            mockMvc.perform(get(String.format(BASE_URL + "/unpaid"
                    + "-summary/" + TestConstants.VALID_COURT_LOCATION + "?page_number=0&sort_by=jurorNumber"
                    + "&sort_order=ASC"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].juror_number", CoreMatchers.is(responseItem.getJurorNumber())))
                .andExpect(jsonPath("$.content[0].pool_number", CoreMatchers.is(responseItem.getPoolNumber())))
                .andExpect(jsonPath("$.content[0].first_name", CoreMatchers.is(responseItem.getFirstName())))
                .andExpect(jsonPath("$.content[0].last_name", CoreMatchers.is(responseItem.getLastName())))
                .andExpect(jsonPath("$.content[0].total_unapproved", CoreMatchers.is(totalUnapproved.doubleValue())));

            verify(jurorExpenseService, times(1))
                .getUnpaidExpensesForCourtLocation(TestConstants.VALID_COURT_LOCATION, null, null,
                    0, "jurorNumber", SortDirection.ASC);
        }

        @Test
        @DisplayName("Invalid court location")
        void invalidCourtLocation() throws Exception {
            BureauJwtPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
            jwtPayload.setStaff(
                TestUtils.staffBuilder("Court User", 1, Collections.singletonList(TestConstants.VALID_COURT_LOCATION)));
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

            BigDecimal totalUnapproved = BigDecimal.valueOf(65.95).setScale(2, RoundingMode.HALF_UP);
            UnpaidExpenseSummaryResponseDto responseItem =
                createUnpaidExpenseSummaryResponseDto("111111111", totalUnapproved);
            Sort sort = Sort.by("jurorNumber").ascending();

            int pageNumber = 0;
            Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE, sort);

            Mockito.doReturn(new PageImpl<>(Collections.singletonList(responseItem), pageable, 1))
                .when(jurorExpenseService).getUnpaidExpensesForCourtLocation(TestConstants.VALID_COURT_LOCATION,
                    null, null, pageNumber, "jurorNumber", SortDirection.ASC);

            mockMvc.perform(get(String.format(BASE_URL + "/unpaid"
                    + "-summary/" + TestConstants.INVALID_COURT_LOCATION + "?page_number=0&sort_by=jurorNumber"
                    + "&sort_order=ASC"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

            verify(jurorExpenseService, Mockito.never())
                .getUnpaidExpensesForCourtLocation(TestConstants.VALID_COURT_LOCATION, null, null,
                    0, "jurorNumber", SortDirection.ASC);
        }

        @Test
        @DisplayName("Missing court location")
        void missingCourtLocation() throws Exception {
            BureauJwtPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
            jwtPayload.setStaff(
                TestUtils.staffBuilder("Court User", 1, Collections.singletonList(TestConstants.VALID_COURT_LOCATION)));
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

            BigDecimal totalUnapproved = BigDecimal.valueOf(65.95).setScale(2, RoundingMode.HALF_UP);
            UnpaidExpenseSummaryResponseDto responseItem =
                createUnpaidExpenseSummaryResponseDto("111111111", totalUnapproved);
            Sort sort = Sort.by("jurorNumber").ascending();

            int pageNumber = 0;
            Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE, sort);

            Mockito.doReturn(new PageImpl<>(Collections.singletonList(responseItem), pageable, 1))
                .when(jurorExpenseService).getUnpaidExpensesForCourtLocation(TestConstants.VALID_COURT_LOCATION,
                    null, null, pageNumber, "jurorNumber", SortDirection.ASC);

            mockMvc.perform(get(String.format(BASE_URL + "/unpaid"
                    + "-summary/" + "?page_number=0&sort_by=jurorNumber"
                    + "&sort_order=ASC"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

            verify(jurorExpenseService, Mockito.never())
                .getUnpaidExpensesForCourtLocation(TestConstants.VALID_COURT_LOCATION, null, null,
                    0, "jurorNumber", SortDirection.ASC);
        }

        @Test
        @DisplayName("Missing page number")
        void missingPageNumber() throws Exception {
            BureauJwtPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
            jwtPayload.setStaff(
                TestUtils.staffBuilder("Court User", 1, Collections.singletonList(TestConstants.VALID_COURT_LOCATION)));
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

            BigDecimal totalUnapproved = BigDecimal.valueOf(65.95).setScale(2, RoundingMode.HALF_UP);
            UnpaidExpenseSummaryResponseDto responseItem =
                createUnpaidExpenseSummaryResponseDto("111111111", totalUnapproved);
            Sort sort = Sort.by("jurorNumber").ascending();

            int pageNumber = 0;
            Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE, sort);

            Mockito.doReturn(new PageImpl<>(Collections.singletonList(responseItem), pageable, 1))
                .when(jurorExpenseService).getUnpaidExpensesForCourtLocation(TestConstants.VALID_COURT_LOCATION,
                    null, null, pageNumber, "jurorNumber", SortDirection.ASC);

            mockMvc.perform(get(String.format(BASE_URL + "/unpaid"
                    + "-summary/" + TestConstants.VALID_COURT_LOCATION + "?&sort_by=jurorNumber&sort_order=ASC"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

            verify(jurorExpenseService, Mockito.never())
                .getUnpaidExpensesForCourtLocation(TestConstants.VALID_COURT_LOCATION, null, null,
                    0, "jurorNumber", SortDirection.ASC);
        }

        @Test
        @DisplayName("Missing sort by")
        void missingSortBy() throws Exception {
            BureauJwtPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
            jwtPayload.setStaff(
                TestUtils.staffBuilder("Court User", 1, Collections.singletonList(TestConstants.VALID_COURT_LOCATION)));
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

            BigDecimal totalUnapproved = BigDecimal.valueOf(65.95).setScale(2, RoundingMode.HALF_UP);
            UnpaidExpenseSummaryResponseDto responseItem =
                createUnpaidExpenseSummaryResponseDto("111111111", totalUnapproved);
            Sort sort = Sort.by("jurorNumber").ascending();

            int pageNumber = 0;
            Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE, sort);

            Mockito.doReturn(new PageImpl<>(Collections.singletonList(responseItem), pageable, 1))
                .when(jurorExpenseService).getUnpaidExpensesForCourtLocation(TestConstants.VALID_COURT_LOCATION,
                    null, null, pageNumber, "jurorNumber", SortDirection.ASC);

            mockMvc.perform(get(String.format(BASE_URL + "/unpaid"
                    + "-summary/" + TestConstants.VALID_COURT_LOCATION + "?page_number=0&sort_order=ASC"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

            verify(jurorExpenseService, Mockito.never())
                .getUnpaidExpensesForCourtLocation(TestConstants.VALID_COURT_LOCATION, null, null,
                    0, "jurorNumber", SortDirection.ASC);
        }

        @Test
        @DisplayName("Missing sort order")
        void missingSortOrder() throws Exception {
            BureauJwtPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
            jwtPayload.setStaff(
                TestUtils.staffBuilder("Court User", 1, Collections.singletonList(TestConstants.VALID_COURT_LOCATION)));
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

            BigDecimal totalUnapproved = BigDecimal.valueOf(65.95).setScale(2, RoundingMode.HALF_UP);
            UnpaidExpenseSummaryResponseDto responseItem =
                createUnpaidExpenseSummaryResponseDto("111111111", totalUnapproved);
            Sort sort = Sort.by("jurorNumber").ascending();

            int pageNumber = 0;
            Pageable pageable = PageRequest.of(pageNumber, PAGE_SIZE, sort);

            Mockito.doReturn(new PageImpl<>(Collections.singletonList(responseItem), pageable, 1))
                .when(jurorExpenseService).getUnpaidExpensesForCourtLocation(TestConstants.VALID_COURT_LOCATION,
                    null, null, pageNumber, "jurorNumber", SortDirection.ASC);

            mockMvc.perform(get(String.format(BASE_URL + "/unpaid"
                    + "-summary/" + TestConstants.VALID_COURT_LOCATION + "?page_number=0&sort_by=jurorNumber"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isBadRequest());

            verify(jurorExpenseService, Mockito.never())
                .getUnpaidExpensesForCourtLocation(TestConstants.VALID_COURT_LOCATION, null, null,
                    0, "jurorNumber", SortDirection.ASC);
        }

        private UnpaidExpenseSummaryResponseDto createUnpaidExpenseSummaryResponseDto(String jurorNumber,
                                                                                      BigDecimal totalUnapproved) {
            return UnpaidExpenseSummaryResponseDto.builder()
                .jurorNumber(jurorNumber)
                .poolNumber("415230101")
                .firstName("Test")
                .lastName("Person")
                .totalUnapproved(totalUnapproved)
                .build();
        }
    }

    @Nested
    @DisplayName("GET " + BASE_URL)
    class GetDefaultExpenses {

        @Test
        @DisplayName("Valid Request")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void happyPathForGetDefaultExpenses() throws Exception {
            String jurorNumber = "111111111";

            BureauJwtPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
            jwtPayload.setStaff(
                TestUtils.staffBuilder("Court User", 1, Collections.singletonList(TestConstants.VALID_COURT_LOCATION)));
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

            DefaultExpenseResponseDto responseItem = new DefaultExpenseResponseDto();
            responseItem.setJurorNumber(jurorNumber);
            responseItem.setSmartCardNumber("12345678");
            responseItem.setFinancialLoss(new BigDecimal("20.0"));
            responseItem.setDistanceTraveledMiles(6);
            responseItem.setTravelTime(LocalTime.of(4, 30));

            Juror juror = new Juror();
            juror.setTravelTime(LocalTime.of(4, 30));
            juror.setMileage(6);
            juror.setSmartCardNumber("12345678");
            juror.setJurorNumber(jurorNumber);
            juror.setFinancialLoss(new BigDecimal("20.0"));

            when(jurorRepository.findById(jurorNumber)).thenReturn(Optional.of(juror));

            when(jurorExpenseService.getDefaultExpensesForJuror(jurorNumber)).thenReturn(responseItem);

            mockMvc.perform(get(BASE_URL + "/default-summary/111111111")
                    .contentType(MediaType.APPLICATION_JSON)
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(TestUtils.asJsonString(responseItem)));

            verify(jurorExpenseService, times(1)).getDefaultExpensesForJuror(jurorNumber);

        }
    }


    @Nested
    @DisplayName("POST " + BASE_URL + "/set-default-expenses")
    class SetDefaultExpenses {

        @Test
        @DisplayName("Happy Path - Set Default Expenses")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void happyPathForSetDefaultExpensesNotOverrideDraftExpenses() throws Exception {
            RequestDefaultExpensesDto payload = new RequestDefaultExpensesDto();
            payload.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
            payload.setTravelTime(LocalTime.of(4, 30));
            payload.setFinancialLoss(BigDecimal.ZERO);
            payload.setSmartCardNumber("12345678");
            payload.setDistanceTraveledMiles(5);
            payload.setOverwriteExistingDraftExpenses(false);

            Mockito.doNothing().when(jurorExpenseService).setDefaultExpensesForJuror(Mockito.any());

            mockMvc.perform(post(BASE_URL + "/set-default-expenses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

            verify(jurorExpenseService, times(1))
                .setDefaultExpensesForJuror(Mockito.any());
            verifyNoMoreInteractions(jurorExpenseService);
        }

        @Test
        @DisplayName("Happy Path - Set Default Expenses")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void happyPathForSetDefaultExpensesIsOverrideDraftExpenses() throws Exception {
            RequestDefaultExpensesDto payload = new RequestDefaultExpensesDto();
            payload.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
            payload.setTravelTime(LocalTime.of(4, 30));
            payload.setFinancialLoss(BigDecimal.ZERO);
            payload.setSmartCardNumber("12345678");
            payload.setDistanceTraveledMiles(5);
            payload.setOverwriteExistingDraftExpenses(true);

            Mockito.doNothing().when(jurorExpenseService).setDefaultExpensesForJuror(Mockito.any());

            mockMvc.perform(post(BASE_URL + "/set-default-expenses")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

            verify(jurorExpenseService, times(1))
                .setDefaultExpensesForJuror(payload);
            verifyNoMoreInteractions(jurorExpenseService);
        }
    }

    @Nested
    @DisplayName("POST " + BASE_URL + "/submit-for-approval")
    class SubmitForApproval {
        private static final String URL = BASE_URL + "/submit-for-approval";

        @Test
        @DisplayName("Happy path")
        void happyPath() throws Exception {
            ExpenseItemsDto payload = new ExpenseItemsDto();
            payload.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
            payload.setPoolNumber(TestConstants.VALID_POOL_NUMBER);
            payload.setAttendanceDates(List.of(LocalDate.of(2024, 1, 2)));

            Mockito.doNothing().when(jurorExpenseService).submitDraftExpensesForApproval(Mockito.any());

            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

            verify(jurorExpenseService, times(1))
                .submitDraftExpensesForApproval(Mockito.any());
            verifyNoMoreInteractions(jurorExpenseService);
        }

        @Test
        @DisplayName("Not Found Error")
        void appearanceRecordsNotFound() throws Exception {
            ExpenseItemsDto payload = new ExpenseItemsDto();
            payload.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
            payload.setPoolNumber(TestConstants.VALID_POOL_NUMBER);
            payload.setAttendanceDates(List.of(LocalDate.of(2024, 1, 2)));

            MojException.NotFound exception = new MojException.NotFound(String.format("No appearance records found for "
                    + "Juror Number: %s, Pool Number: %s and Attendance Dates provided",
                TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER), null);

            Mockito.doThrow(exception).when(jurorExpenseService).submitDraftExpensesForApproval(Mockito.any());

            mockMvc.perform(post(URL)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

            verify(jurorExpenseService, times(1))
                .submitDraftExpensesForApproval(Mockito.any());
            verifyNoMoreInteractions(jurorExpenseService);
        }
    }

    abstract class AbstractEnterDraftExpenseTest {

        private final String url;

        public AbstractEnterDraftExpenseTest(String url) {
            this.url = url;
        }


        protected static DailyExpenseFinancialLoss createDailyExpenseFinancialLoss(Double lossOfEarnings,
                                                                                   Double extraCareCost,
                                                                                   Double otherCost,
                                                                                   String otherCostDesc) {
            return DailyExpenseFinancialLoss.builder()
                .lossOfEarningsOrBenefits(doubleToBigDecimal(lossOfEarnings))
                .extraCareCost(doubleToBigDecimal(extraCareCost))
                .otherCosts(doubleToBigDecimal(otherCost))
                .otherCostsDescription(otherCostDesc)
                .build();
        }

        protected static DailyExpenseFoodAndDrink createDailyExpenseFoodAndDrink(FoodDrinkClaimType foodDrinkClaimType,
                                                                                 Double smartCardAmount) {
            return DailyExpenseFoodAndDrink.builder()
                .foodAndDrinkClaimType(foodDrinkClaimType)
                .smartCardAmount(doubleToBigDecimal(smartCardAmount))
                .build();
        }

        protected static BigDecimal doubleToBigDecimal(Double value) {
            return doubleToBigDecimal(value, 2);
        }

        protected static BigDecimal doubleToBigDecimal(Double value, int precision) {
            if (value == null) {
                return null;
            }
            return new BigDecimal(String.format("%." + precision + "f", value));
        }

        protected String toUrl(String jurorNumber) {
            return url.replace("{juror_number}", jurorNumber);
        }

        protected abstract DailyExpense getValidPayload();

        @Nested
        @DisplayName("Negative")
        class Negative {

            @Test
            void badPayload() throws Exception {
                DailyExpense payload = getValidPayload();
                payload.setPoolNumber("INVALID");
                mockMvc.perform(post(toUrl(TestConstants.VALID_JUROR_NUMBER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(payload)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }

            @Test
            void invalidJurorNumber() throws Exception {
                DailyExpense payload = getValidPayload();
                mockMvc.perform(post(toUrl("INVALID"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(payload)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            @Test
            void typical() throws Exception {
                DailyExpense payload = getValidPayload();

                DailyExpenseResponse response = new DailyExpenseResponse();
                response.setFinancialLossWarning(
                    FinancialLossWarningTest.getValidObject()
                );
                when(jurorExpenseService.updateDraftExpense(TestConstants.VALID_JUROR_NUMBER, payload))
                    .thenReturn(response);
                mockMvc.perform(post(toUrl(TestConstants.VALID_JUROR_NUMBER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(payload)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(content().json(TestUtils.asJsonString(response)));
                verify(jurorExpenseService, times(1)).updateDraftExpense(TestConstants.VALID_JUROR_NUMBER, payload);
            }
        }
    }

    @Nested
    @DisplayName("POST " + AttendedDayEnterDraftExpenseTest.URL)
    class AttendedDayEnterDraftExpenseTest extends AbstractEnterDraftExpenseTest {
        public static final String URL = BASE_URL + "/{juror_number}/draft/attended_day";

        AttendedDayEnterDraftExpenseTest() {
            super(URL);
        }

        protected static DailyExpense getTypicalPayload() {
            return DailyExpense.builder()
                .dateOfExpense(LocalDate.of(2023, 1, 5))
                .poolNumber("415230101")
                .payCash(false)
                .time(DailyExpenseTime.builder()
                    .travelTime(LocalTime.of(1, 2))
                    .payAttendance(PayAttendanceType.FULL_DAY)
                    .build())
                .financialLoss(
                    createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc")
                )
                .travel(
                    DailyExpenseTravel.builder()
                        .traveledByCar(true)
                        .jurorsTakenCar(null)
                        .milesTraveled(5)
                        .parking(doubleToBigDecimal(2.25))
                        .publicTransport(null)
                        .taxi(null)
                        .build()
                )
                .foodAndDrink(
                    createDailyExpenseFoodAndDrink(FoodDrinkClaimType.LESS_THAN_OR_EQUAL_TO_10_HOURS, 4.2)
                )
                .build();
        }

        @Override
        protected DailyExpense getValidPayload() {
            return getTypicalPayload();
        }
    }

    @Nested
    @DisplayName("POST " + NonAttendedDayEnterDraftExpenseTest.URL)
    class NonAttendedDayEnterDraftExpenseTest extends AbstractEnterDraftExpenseTest {
        public static final String URL = BASE_URL + "/{juror_number}/draft/non_attended_day";

        NonAttendedDayEnterDraftExpenseTest() {
            super(URL);
        }

        protected static DailyExpense getTypicalPayload() {
            return DailyExpense.builder()
                .dateOfExpense(LocalDate.of(2023, 1, 5))
                .poolNumber("415230101")
                .payCash(false)
                .time(DailyExpenseTime.builder()
                    .payAttendance(PayAttendanceType.FULL_DAY)
                    .build())
                .financialLoss(
                    createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc")
                )
                .build();
        }

        @Override
        protected DailyExpense getValidPayload() {
            return getTypicalPayload();
        }


        @Nested
        @DisplayName("Negative")
        class Negative extends AbstractEnterDraftExpenseTest.Negative {

            @Test
            @DisplayName("Attended day payload")
            void attendedDayPayload() throws Exception {
                DailyExpense payload = AttendedDayEnterDraftExpenseTest.getTypicalPayload();
                mockMvc.perform(post(toUrl(TestConstants.VALID_JUROR_NUMBER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(payload)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }
        }
    }

    @Nested
    @DisplayName("POST (get) " + GetEnteredExpenseDetails.URL)
    class GetEnteredExpenseDetails {
        public static final String URL = BASE_URL + "/entered";

        private GetEnteredExpenseDetails() {

        }

        @Nested
        @DisplayName("Negative")
        class Negative {

            @Test
            void badPayload() throws Exception {
                GetEnteredExpenseRequest request = GetEnteredExpenseRequest.builder()
                    .expenseDates(List.of(LocalDate.now()))
                    .poolNumber(TestConstants.INVALID_POOL_NUMBER)
                    .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                    .build();
                mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(request)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            @Test
            @DisplayName("typical")
            void typical() throws Exception {
                GetEnteredExpenseRequest request = GetEnteredExpenseRequest.builder()
                    .expenseDates(List.of(LocalDate.now(), LocalDate.now().plusDays(1)))
                    .poolNumber(TestConstants.VALID_POOL_NUMBER)
                    .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                    .build();
                when(jurorExpenseService
                    .getEnteredExpense(TestConstants.VALID_JUROR_NUMBER,
                        TestConstants.VALID_POOL_NUMBER,
                        request.getExpenseDates().get(0))
                ).thenReturn(GetEnteredExpenseResponse.builder()
                    .totalPaid(new BigDecimal("1.23"))
                    .build());
                when(jurorExpenseService
                    .getEnteredExpense(TestConstants.VALID_JUROR_NUMBER,
                        TestConstants.VALID_POOL_NUMBER,
                        request.getExpenseDates().get(1))
                ).thenReturn(GetEnteredExpenseResponse.builder()
                    .totalPaid(new BigDecimal("2.34"))
                    .build());

                mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(request)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", CoreMatchers.is(2)))
                    .andExpect(jsonPath("$.[0].total_paid", CoreMatchers.is(1.23)))
                    .andExpect(jsonPath("$.[1].total_paid", CoreMatchers.is(2.34)));

                verify(jurorExpenseService, times(1))
                    .getEnteredExpense(TestConstants.VALID_JUROR_NUMBER,
                        TestConstants.VALID_POOL_NUMBER,
                        request.getExpenseDates().get(0));
            }
        }
    }

    @Nested
    @DisplayName("POST " + ApproveExpenses.URL)
    class ApproveExpenses {
        public static final String URL = BASE_URL + "/approve";

        private ApproveExpenses() {

        }

        @Nested
        @DisplayName("Negative")
        class Negative {

            @Test
            void badPayload() throws Exception {
                ApproveExpenseDto request = ApproveExpenseDto.builder()
                    .jurorNumber(TestConstants.INVALID_JUROR_NUMBER)
                    .poolNumber(TestConstants.VALID_POOL_NUMBER)
                    .approvalType(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL)
                    .dateToRevisions(List.of(
                        ApproveExpenseDto.DateToRevision.builder()
                            .attendanceDate(LocalDate.now())
                            .version(1L).build()
                    ))
                    .build();
                mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(request)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            @Test
            @DisplayName("typical")
            void typical() throws Exception {
                ApproveExpenseDto request1 = ApproveExpenseDto.builder()
                    .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                    .poolNumber(TestConstants.VALID_POOL_NUMBER)
                    .approvalType(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL)
                    .cashPayment(false)
                    .dateToRevisions(List.of(
                        ApproveExpenseDto.DateToRevision.builder()
                            .attendanceDate(LocalDate.now())
                            .version(1L).build()
                    ))
                    .build();

                ApproveExpenseDto request2 = ApproveExpenseDto.builder()
                    .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                    .poolNumber(TestConstants.VALID_POOL_NUMBER)
                    .approvalType(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL)
                    .cashPayment(false)
                    .dateToRevisions(List.of(
                        ApproveExpenseDto.DateToRevision.builder()
                            .attendanceDate(LocalDate.now().plusDays(1))
                            .version(1L).build()
                    ))
                    .build();
                mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(List.of(request1, request2))))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk());

                verify(jurorExpenseService, times(1))
                    .approveExpenses(request1);
                verify(jurorExpenseService, times(1))
                    .approveExpenses(request2);
            }
        }
    }

    @Nested
    @DisplayName("POST (get) " + GetSimplifiedExpenseDetails.URL)
    class GetSimplifiedExpenseDetails {
        public static final String URL = BASE_URL + "/view/{type}/simplified";

        public String toUrl(String type) {
            return URL.replace("{type}", type);
        }

        private JurorNumberAndPoolNumberDto getValidPayload() {
            return JurorNumberAndPoolNumberDto.builder()
                .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                .poolNumber(TestConstants.VALID_POOL_NUMBER)
                .build();
        }


        @Nested
        @DisplayName("Negative")
        class Negative {

            @Test
            void invalidType() throws Exception {
                mockMvc.perform(post(toUrl("INVALID"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(getValidPayload())))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }

            @Test
            void invalidJurorNumber() throws Exception {
                JurorNumberAndPoolNumberDto request = getValidPayload();
                request.setJurorNumber(TestConstants.INVALID_JUROR_NUMBER);
                mockMvc.perform(post(toUrl(ExpenseType.FOR_APPROVAL.name()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(request)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }

            @Test
            void invalidPoolNumber() throws Exception {
                JurorNumberAndPoolNumberDto request = getValidPayload();
                request.setJurorNumber(TestConstants.INVALID_POOL_NUMBER);
                mockMvc.perform(post(toUrl(ExpenseType.FOR_APPROVAL.name()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(request)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            @Test
            @DisplayName("typical")
            void typical() throws Exception {
                JurorNumberAndPoolNumberDto request = getValidPayload();
                CombinedSimplifiedExpenseDetailDto combinedExpenseDetailsDto = new CombinedSimplifiedExpenseDetailDto();
                combinedExpenseDetailsDto.addSimplifiedExpenseDetailDto(
                    SimplifiedExpenseDetailDto.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 5))
                        .financialAuditNumber("F123")
                        .attendanceType(AttendanceType.FULL_DAY)
                        .financialLoss(new BigDecimal("249.00"))
                        .travel(new BigDecimal("228.97"))
                        .foodAndDrink(new BigDecimal("103.00"))
                        .smartcard(new BigDecimal("28.00"))
                        .totalDue(new BigDecimal("552.97"))
                        .totalPaid(new BigDecimal("0.00"))
                        .balanceToPay(new BigDecimal("552.97"))
                        .auditCreatedOn(LocalDateTime.of(2023, 1, 11, 9, 31, 1))
                        .build()
                );
                when(jurorExpenseService.getSimplifiedExpense(request, ExpenseType.FOR_APPROVAL))
                    .thenReturn(combinedExpenseDetailsDto);
                mockMvc.perform(post(toUrl(ExpenseType.FOR_APPROVAL.name()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(request)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.expense_details[0].attendance_date", CoreMatchers.is("2023-01-05")));

                verify(jurorExpenseService, times(1))
                    .getSimplifiedExpense(request, ExpenseType.FOR_APPROVAL);
            }
        }
    }

    @Nested
    @DisplayName("GET " + GetDraftExpenses.URL)
    class GetDraftExpenses {
        public static final String URL = BASE_URL + "/draft/{juror_number}/{pool_number}";

        public String toUrl(String jurorNumber, String poolNumber) {
            return URL.replace("{juror_number}", jurorNumber)
                .replace("{pool_number}", poolNumber);
        }


        @Nested
        @DisplayName("Negative")
        class Negative {

            @Test
            void invalidJurorNumber() throws Exception {
                mockMvc.perform(get(toUrl(TestConstants.INVALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }

            @Test
            void invalidPoolNumber() throws Exception {
                mockMvc.perform(get(toUrl(TestConstants.VALID_JUROR_NUMBER, TestConstants.INVALID_POOL_NUMBER)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            @Test
            @DisplayName("typical")
            void typical() throws Exception {

                CombinedExpenseDetailsDto<ExpenseDetailsDto> combinedExpenseDetailsDto =
                    new CombinedExpenseDetailsDto<>();
                combinedExpenseDetailsDto.addExpenseDetail(
                    ExpenseDetailsDto.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 5))
                        .attendanceType(AttendanceType.FULL_DAY)
                        .lossOfEarnings(new BigDecimal("90.00"))
                        .extraCare(new BigDecimal("70.00"))
                        .other(new BigDecimal("80.00"))
                        .publicTransport(new BigDecimal("10.00"))
                        .taxi(new BigDecimal("20.00"))
                        .motorcycle(new BigDecimal("30.00"))
                        .car(new BigDecimal("40.00"))
                        .bicycle(new BigDecimal("50.00"))
                        .parking(new BigDecimal("60.00"))
                        .foodAndDrink(new BigDecimal("100.00"))
                        .smartCard(new BigDecimal("25.00"))
                        .paymentMethod(PaymentMethod.BACS)
                        .build());

                when(jurorExpenseService.getDraftExpenses(TestConstants.VALID_JUROR_NUMBER,
                    TestConstants.VALID_POOL_NUMBER)).thenReturn(combinedExpenseDetailsDto);

                mockMvc.perform(get(toUrl(TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.expense_details[0].attendance_date", CoreMatchers.is("2023-01-05")));

                verify(jurorExpenseService, times(1))
                    .getDraftExpenses(TestConstants.VALID_JUROR_NUMBER,
                        TestConstants.VALID_POOL_NUMBER);
            }
        }
    }

    @Nested
    @DisplayName("GET (POST) " + GetExpenses.URL)
    class GetExpenses {
        public static final String URL = BASE_URL + "/{juror_number}/{pool_number}";

        public String toUrl(String jurorNumber, String poolNumber) {
            return URL.replace("{juror_number}", jurorNumber)
                .replace("{pool_number}", poolNumber);
        }

        static List<LocalDate> getValidPayload() {
            return List.of(
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2)

            );
        }

        @Nested
        @DisplayName("Negative")
        class Negative {

            @Test
            void invalidJurorNumber() throws Exception {
                mockMvc.perform(post(toUrl(TestConstants.INVALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(getValidPayload())))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }

            @Test
            void invalidPoolNumber() throws Exception {
                mockMvc.perform(post(toUrl(TestConstants.INVALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(getValidPayload())))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }

            @Test
            void invalidPayload() throws Exception {
                mockMvc.perform(post(toUrl(TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(List.of())))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            @Test
            @DisplayName("typical")
            void typical() throws Exception {

                CombinedExpenseDetailsDto<ExpenseDetailsDto> combinedExpenseDetailsDto =
                    new CombinedExpenseDetailsDto<>();
                combinedExpenseDetailsDto.addExpenseDetail(
                    ExpenseDetailsDto.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 5))
                        .attendanceType(AttendanceType.FULL_DAY)
                        .lossOfEarnings(new BigDecimal("90.00"))
                        .extraCare(new BigDecimal("70.00"))
                        .other(new BigDecimal("80.00"))
                        .publicTransport(new BigDecimal("10.00"))
                        .taxi(new BigDecimal("20.00"))
                        .motorcycle(new BigDecimal("30.00"))
                        .car(new BigDecimal("40.00"))
                        .bicycle(new BigDecimal("50.00"))
                        .parking(new BigDecimal("60.00"))
                        .foodAndDrink(new BigDecimal("100.00"))
                        .smartCard(new BigDecimal("25.00"))
                        .paymentMethod(PaymentMethod.BACS)
                        .build());

                List<LocalDate> payload = getValidPayload();
                when(jurorExpenseService.getExpenses(TestConstants.VALID_JUROR_NUMBER,
                    TestConstants.VALID_POOL_NUMBER, payload))
                    .thenReturn(combinedExpenseDetailsDto);

                mockMvc.perform(post(toUrl(TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(payload)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(TestUtils.asJsonString(combinedExpenseDetailsDto)));

                verify(jurorExpenseService, times(1))
                    .getExpenses(TestConstants.VALID_JUROR_NUMBER,
                        TestConstants.VALID_POOL_NUMBER, payload);
            }
        }
    }


    @Nested
    @DisplayName("GET " + GetExpensesForApproval.URL)
    class GetExpensesForApproval {
        public static final String URL = BASE_URL + "/approval/{loc_code}/{payment_method}";

        private String toUrl(String locCode, PaymentMethod paymentMethod, LocalDate from, LocalDate to) {
            return toUrl(locCode, paymentMethod.name(),
                from == null ? null : from.format(DateTimeFormatter.ISO_DATE),
                to == null ? null : to.format(DateTimeFormatter.ISO_DATE));
        }

        private String toUrl(String locCode, String paymentMethod, String from, String to) {
            String urlTmp = URL.replace("{loc_code}", locCode)
                .replace("{payment_method}", paymentMethod);
            StringBuilder builder = new StringBuilder(urlTmp);
            if (from != null) {
                builder.append("?from=").append(from);
            }
            if (to != null) {
                builder.append(from != null ? "&" : "?").append("to=").append(to);
            }
            return builder.toString();
        }


        @Nested
        @DisplayName("Negative")
        class Negative {

            @Test
            void invalidLocCode() throws Exception {
                mockMvc.perform(get(toUrl(TestConstants.INVALID_COURT_LOCATION, PaymentMethod.BACS,
                        null, null)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }

            @Test
            void invalidPaymentMethod() throws Exception {
                mockMvc.perform(get(toUrl(TestConstants.VALID_COURT_LOCATION, "INVALID",
                        null, null)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);

            }

            @Test
            void invalidFromDate() throws Exception {
                mockMvc.perform(get(toUrl(TestConstants.VALID_COURT_LOCATION, PaymentMethod.BACS.name(),
                        "INVALID", null)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);

            }

            @Test
            void invalidToDate() throws Exception {
                mockMvc.perform(get(toUrl(TestConstants.VALID_COURT_LOCATION, PaymentMethod.BACS.name(),
                        null, "INVALID")))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);

            }

        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            @Test
            void typicalWithoutDates() throws Exception {
                PendingApprovalList pendingApprovalList = new PendingApprovalList();
                pendingApprovalList.setTotalPendingBacs(2L);
                when(jurorExpenseService.getExpensesForApproval(TestConstants.VALID_COURT_LOCATION,
                    PaymentMethod.BACS, null, null)).thenReturn(pendingApprovalList);

                mockMvc.perform(get(toUrl(TestConstants.VALID_COURT_LOCATION, PaymentMethod.BACS.name(),
                        null, null)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total_pending_bacs", CoreMatchers.is(2)));

                verify(jurorExpenseService, times(1))
                    .getExpensesForApproval(TestConstants.VALID_COURT_LOCATION,
                        PaymentMethod.BACS, null, null);
            }

            @Test
            void typicalWithDates() throws Exception {
                PendingApprovalList pendingApprovalList = new PendingApprovalList();
                pendingApprovalList.setTotalPendingBacs(3L);
                when(jurorExpenseService.getExpensesForApproval(TestConstants.VALID_COURT_LOCATION,
                    PaymentMethod.BACS, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 2, 1))).thenReturn(
                    pendingApprovalList);

                mockMvc.perform(get(toUrl(TestConstants.VALID_COURT_LOCATION, PaymentMethod.BACS,
                        LocalDate.of(2023, 1, 1), LocalDate.of(2023, 2, 1))))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total_pending_bacs", CoreMatchers.is(3)));

                verify(jurorExpenseService, times(1))
                    .getExpensesForApproval(TestConstants.VALID_COURT_LOCATION,
                        PaymentMethod.BACS, LocalDate.of(2023, 1, 1), LocalDate.of(2023, 2, 1));
            }
        }
    }

    @Nested
    @DisplayName("GET " + GetCounts.URL)
    class GetCounts {
        public static final String URL = BASE_URL + "/counts/{juror_number}/{pool_number}";

        public String toUrl(String jurorNumber, String poolNumber) {
            return URL.replace("{juror_number}", jurorNumber)
                .replace("{pool_number}", poolNumber);
        }

        @Nested
        @DisplayName("Negative")
        class Negative {

            @Test
            void invalidJurorNumber() throws Exception {
                mockMvc.perform(get(toUrl(TestConstants.INVALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }

            @Test
            void invalidPoolNumber() throws Exception {
                mockMvc.perform(get(toUrl(TestConstants.VALID_JUROR_NUMBER, TestConstants.INVALID_POOL_NUMBER)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            @Test
            @DisplayName("typical")
            void typical() throws Exception {
                ExpenseCount expenseCount = ExpenseCount.builder()
                    .totalDraft(1)
                    .totalForApproval(2)
                    .totalApproved(3)
                    .totalForReapproval(4)
                    .build();

                when(jurorExpenseService.countExpenseTypes(TestConstants.VALID_JUROR_NUMBER,
                    TestConstants.VALID_POOL_NUMBER)).thenReturn(expenseCount);

                mockMvc.perform(get(toUrl(TestConstants.VALID_JUROR_NUMBER, TestConstants.VALID_POOL_NUMBER))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total_draft", CoreMatchers.is(1)))
                    .andExpect(jsonPath("$.total_for_approval", CoreMatchers.is(2)))
                    .andExpect(jsonPath("$.total_approved", CoreMatchers.is(3)))
                    .andExpect(jsonPath("$.total_for_reapproval", CoreMatchers.is(4)));

                verify(jurorExpenseService, times(1))
                    .countExpenseTypes(TestConstants.VALID_JUROR_NUMBER,
                        TestConstants.VALID_POOL_NUMBER);
            }
        }
    }

    @Nested
    @DisplayName("POST " + PostEditDailyExpense.URL)
    class PostEditDailyExpense {
        public static final String URL = BASE_URL + "/{juror_number}/edit/{type}";

        public String toUrl(String jurorNumber, String type) {
            return URL.replace("{juror_number}", jurorNumber)
                .replace("{type}", type);
        }


        @Nested
        @DisplayName("Negative")
        class Negative {
            @Test
            void invalidJurorNumber() throws Exception {
                mockMvc.perform(post(toUrl(TestConstants.INVALID_JUROR_NUMBER, ExpenseType.FOR_APPROVAL.name()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(List.of(mockDailyExpense(LocalDate.now())))))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }

            @Test
            void invalidType() throws Exception {
                mockMvc.perform(post(toUrl(TestConstants.VALID_JUROR_NUMBER, "INVALID"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(List.of(mockDailyExpense(LocalDate.now())))))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            @Test
            @DisplayName("typical")
            void typical() throws Exception {

                DailyExpense dailyExpense1 = mockDailyExpense(LocalDate.now().plusDays(1));
                DailyExpense dailyExpense2 = mockDailyExpense(LocalDate.now().plusDays(2));
                DailyExpense dailyExpense3 = mockDailyExpense(LocalDate.now().plusDays(3));


                mockMvc.perform(post(toUrl(TestConstants.VALID_JUROR_NUMBER, ExpenseType.APPROVED.name()))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(TestUtils.asJsonString(List.of(dailyExpense1, dailyExpense2, dailyExpense3))))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isAccepted());

                verify(jurorExpenseService, times(1))
                    .updateExpense(TestConstants.VALID_JUROR_NUMBER,
                        ExpenseType.APPROVED,
                        List.of(dailyExpense1, dailyExpense2, dailyExpense3));
            }
        }
    }

    private DailyExpense mockDailyExpense(LocalDate date) {
        return DailyExpense.builder()
            .dateOfExpense(date)
            .poolNumber("415230101")
            .payCash(false)
            .time(DailyExpenseTime.builder()
                .payAttendance(PayAttendanceType.FULL_DAY)
                .build())
            .financialLoss(
                DailyExpenseFinancialLoss.builder()
                    .lossOfEarningsOrBenefits(BigDecimal.ZERO)
                    .extraCareCost(BigDecimal.ZERO)
                    .otherCosts(BigDecimal.ZERO)
                    .otherCostsDescription("Misc")
                    .build()
            )
            .build();
    }


    @Nested
    @DisplayName("POST " + CalculateTotals.URL)
    class CalculateTotals {
        public static final String URL = BASE_URL + "/calculate/totals";

        private CalculateTotals() {

        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            @Test
            void invalidPayload() throws Exception {
                mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(CalculateTotalExpenseRequestDto.builder()
                            .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                            .expenseList(List.of(
                                DailyExpense.builder()
                                    .dateOfExpense(LocalDate.of(2023, 1, 17))
                                    .poolNumber(TestConstants.INVALID_POOL_NUMBER)
                                    .payCash(false)
                                    .time(DailyExpenseTime.builder()
                                        .payAttendance(PayAttendanceType.FULL_DAY)
                                        .build())
                                    .financialLoss(
                                        DailyExpenseFinancialLoss.builder().build()
                                    )
                                    .build()))
                            .build())))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            @Test
            @DisplayName("typical")
            void typical() throws Exception {

                CalculateTotalExpenseRequestDto requestDto = CalculateTotalExpenseRequestDto.builder()
                    .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                    .poolNumber(TestConstants.VALID_POOL_NUMBER)
                    .expenseList(List.of(
                        DailyExpense.builder()
                            .dateOfExpense(LocalDate.of(2023, 1, 17))
                            .payCash(false)
                            .time(DailyExpenseTime.builder()
                                .payAttendance(PayAttendanceType.FULL_DAY)
                                .build())
                            .financialLoss(
                                DailyExpenseFinancialLoss.builder().build()
                            )
                            .build()))
                    .build();

                mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(TestUtils.asJsonString(requestDto)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk());

                verify(jurorExpenseService, times(1))
                    .calculateTotals(requestDto);
            }
        }
    }

    @Nested
    @DisplayName("PATCH " + ApportionSmartCard.URL)
    class ApportionSmartCard {
        public static final String URL = BASE_URL + "/smartcard";


        private ApportionSmartCardRequest getValidPayload() {
            return ApportionSmartCardRequest.builder()
                .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                .poolNumber(TestConstants.VALID_POOL_NUMBER)
                .smartCardAmount(new BigDecimal("100.00"))
                .attendanceDates(List.of(LocalDate.of(2023, 1, 17)))
                .build();
        }

        @Nested
        @DisplayName("Positive")
        class Positive {

            @Test
            void typical() throws Exception {
                ApportionSmartCardRequest payload = getValidPayload();
                mockMvc.perform(patch(URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(TestUtils.asJsonString(payload)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isAccepted());

                verify(jurorExpenseService, times(1))
                    .apportionSmartCard(payload);
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            @Test
            void invalidPayload() throws Exception {
                ApportionSmartCardRequest payload = getValidPayload();
                payload.setJurorNumber(TestConstants.INVALID_JUROR_NUMBER);
                mockMvc.perform(patch(URL)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(TestUtils.asJsonString(payload)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }
        }
    }

}
