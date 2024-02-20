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
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.moj.controller.request.RequestDefaultExpensesDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseItemsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.GetEnteredExpenseRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpense;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseFinancialLoss;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseFoodAndDrink;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseTime;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseTravel;
import uk.gov.hmcts.juror.api.moj.controller.response.DefaultExpenseResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.DailyExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.FinancialLossWarningTest;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.GetEnteredExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.UnpaidExpenseSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.SortDirection;
import uk.gov.hmcts.juror.api.moj.enumeration.FoodDrinkClaimType;
import uk.gov.hmcts.juror.api.moj.enumeration.PayAttendanceType;
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
import java.time.LocalTime;
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
@SuppressWarnings({"PMD.ExcessiveImports"})
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
            BureauJWTPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
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
            BureauJWTPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
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
            BureauJWTPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
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
            BureauJWTPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
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
            BureauJWTPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
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
            BureauJWTPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
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
            BureauJWTPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
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

            BureauJWTPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
            jwtPayload.setStaff(
                TestUtils.staffBuilder("Court User", 1, Collections.singletonList(TestConstants.VALID_COURT_LOCATION)));
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

            DefaultExpenseResponseDto responseItem = new DefaultExpenseResponseDto();
            responseItem.setJurorNumber(jurorNumber);
            responseItem.setSmartCardNumber("12345678");
            responseItem.setTotalSmartCardSpend(BigDecimal.valueOf(40.0));
            responseItem.setFinancialLoss(new BigDecimal("20.0"));
            responseItem.setDistanceTraveledMiles(6);
            responseItem.setTravelTime(LocalTime.of(4, 30));

            Juror juror = new Juror();
            juror.setTravelTime(LocalTime.of(4, 30));
            juror.setMileage(6);
            juror.setSmartCard("12345678");
            juror.setAmountSpent(BigDecimal.valueOf(40.0));
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
    @DisplayName("SET " + BASE_URL)
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
            payload.setTotalSmartCardSpend(BigDecimal.valueOf(10.0));
            payload.setDistanceTraveledMiles(5);
            payload.setOverwriteExistingDraftExpenses(false);

            Mockito.doNothing().when(jurorExpenseService).setDefaultExpensesForJuror(Mockito.any());

            mockMvc.perform(post(BASE_URL + "/set-default-expenses/123456789")
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
            payload.setTotalSmartCardSpend(BigDecimal.valueOf(10.0));
            payload.setDistanceTraveledMiles(5);
            payload.setOverwriteExistingDraftExpenses(true);

            Mockito.doNothing().when(jurorExpenseService).setDefaultExpensesForJuror(Mockito.any());

            mockMvc.perform(post(BASE_URL + "/set-default-expenses/123456789")
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
                    createDailyExpenseFoodAndDrink(FoodDrinkClaimType.LESS_THAN_1O_HOURS, 4.2)
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

        @Nested
        @DisplayName("Negative")
        class Negative {

            @Test
            void badPayload() throws Exception {
                GetEnteredExpenseRequest request = GetEnteredExpenseRequest.builder()
                    .dateOfExpense(LocalDate.now())
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
                    .dateOfExpense(LocalDate.now())
                    .poolNumber(TestConstants.VALID_POOL_NUMBER)
                    .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                    .build();
                GetEnteredExpenseResponse response = GetEnteredExpenseResponse.builder()
                    .totalPaid(new BigDecimal("1.23"))
                    .build();
                when(jurorExpenseService
                    .getEnteredExpense(TestConstants.VALID_JUROR_NUMBER,
                        TestConstants.VALID_POOL_NUMBER,
                        request.getDateOfExpense())
                ).thenReturn(response);

                mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(request)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total_paid", CoreMatchers.is(1.23)));

                verify(jurorExpenseService, times(1))
                    .getEnteredExpense(TestConstants.VALID_JUROR_NUMBER,
                        TestConstants.VALID_POOL_NUMBER,
                        request.getDateOfExpense());
            }
        }
    }
}
