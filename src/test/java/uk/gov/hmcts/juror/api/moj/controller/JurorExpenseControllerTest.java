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
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtAuthentication;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.RequestDefaultExpensesDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ApportionSmartCardRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ApproveExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.AttendanceDates;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.CalculateTotalExpenseRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.CombinedExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseType;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.GetEnteredExpenseRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpense;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseFinancialLoss;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseTime;
import uk.gov.hmcts.juror.api.moj.controller.response.DefaultExpenseResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.CombinedSimplifiedExpenseDetailDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.ExpenseCount;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.GetEnteredExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.PendingApprovalList;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.SimplifiedExpenseDetailDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.UnpaidExpenseSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.SortDirection;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    public static final String BASE_URL = "/api/v1/moj/expenses/{loc_code}";

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
    @DisplayName("GET " + UnpaidExpensesForCourtLocation.URL)
    class UnpaidExpensesForCourtLocation {

        public static final String URL = BASE_URL + "/unpaid-summary";

        public String toUrl(String locCode) {
            return URL.replace("{loc_code}", locCode);
        }

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


            mockMvc.perform(
                    get(String.format(toUrl(TestConstants.VALID_COURT_LOCATION) + "?min_date=2023-01-05&max_date=2023"
                        + "-01-19"
                        + "&page_number=0"
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


            mockMvc.perform(
                    get(String.format(toUrl(TestConstants.VALID_COURT_LOCATION) + "?page_number=0&sort_by=jurorNumber"
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

            mockMvc.perform(get(String.format(toUrl(TestConstants.INVALID_COURT_LOCATION) + "?page_number=0&sort_by"
                    + "=jurorNumber"
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

            mockMvc.perform(
                    get(String.format(toUrl(TestConstants.VALID_COURT_LOCATION) + "?&sort_by=jurorNumber&sort_order"
                        + "=ASC"))
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

            mockMvc.perform(
                    get(String.format(toUrl(TestConstants.VALID_COURT_LOCATION) + "?page_number=0&sort_order=ASC"))
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

            mockMvc.perform(
                    get(String.format(toUrl(TestConstants.VALID_COURT_LOCATION) + "?page_number=0&sort_by=jurorNumber"))
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
    @DisplayName("GET " + GetDefaultExpenses.URL)
    class GetDefaultExpenses {

        public static final String URL = BASE_URL + "/{juror_number}/default-expenses";

        public String toUrl(String locCode, String jurorNumber) {
            return URL.replace("{loc_code}", locCode)
                .replace("{juror_number}", jurorNumber);
        }

        @Test
        @DisplayName("Valid Request")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void happyPathForGetDefaultExpenses() throws Exception {
            final String jurorNumber = "111111111";

            BureauJwtPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
            jwtPayload.setStaff(
                TestUtils.staffBuilder("Court User", 1, Collections.singletonList(TestConstants.VALID_COURT_LOCATION)));
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

            DefaultExpenseResponseDto responseItem = new DefaultExpenseResponseDto();
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

            when(jurorExpenseService.getDefaultExpensesForJuror(jurorNumber)).thenReturn(responseItem);

            mockMvc.perform(get(
                    toUrl(TestConstants.VALID_COURT_LOCATION, "111111111"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(content().json(TestUtils.asJsonString(responseItem)));

            verify(jurorExpenseService, times(1)).getDefaultExpensesForJuror(jurorNumber);

        }
    }


    @Nested
    @DisplayName("POST " + SetDefaultExpenses.URL)
    class SetDefaultExpenses {

        public static final String URL = BASE_URL + "/{juror_number}/default-expenses";

        public String toUrl(String locCode, String jurorNumber) {
            return URL.replace("{loc_code}", locCode)
                .replace("{juror_number}", jurorNumber);
        }

        @Test
        @DisplayName("Happy Path - Set Default Expenses")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void happyPathForSetDefaultExpensesNotOverrideDraftExpenses() throws Exception {
            RequestDefaultExpensesDto payload = new RequestDefaultExpensesDto();
            payload.setTravelTime(LocalTime.of(4, 30));
            payload.setFinancialLoss(BigDecimal.ZERO);
            payload.setSmartCardNumber("12345678");
            payload.setDistanceTraveledMiles(5);
            payload.setOverwriteExistingDraftExpenses(false);


            mockMvc.perform(post(toUrl(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

            verify(jurorExpenseService, times(1))
                .setDefaultExpensesForJuror(
                    TestConstants.VALID_JUROR_NUMBER,
                    payload
                );
            verifyNoMoreInteractions(jurorExpenseService);
        }
    }

    @Nested
    @DisplayName("POST " + SubmitForApproval.URL)
    class SubmitForApproval {
        private static final String URL = BASE_URL + "/{juror_number}/submit-for-approval";

        public String toUrl(String locCode, String jurorNumber) {
            return URL.replace("{loc_code}", locCode)
                .replace("{juror_number}", jurorNumber);
        }

        @Test
        @DisplayName("Happy path")
        void happyPath() throws Exception {
            AttendanceDates payload = new AttendanceDates();
            payload.setAttendanceDates(List.of(LocalDate.of(2024, 1, 2)));

            Mockito.doNothing().when(jurorExpenseService).submitDraftExpensesForApproval(any(), any(), any());

            mockMvc.perform(post(toUrl(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

            verify(jurorExpenseService, times(1))
                .submitDraftExpensesForApproval(
                    eq(TestConstants.VALID_COURT_LOCATION),
                    eq(TestConstants.VALID_JUROR_NUMBER),
                    eq(payload.getAttendanceDates())
                );
            verifyNoMoreInteractions(jurorExpenseService);
        }

        @Test
        @DisplayName("Not Found Error")
        void appearanceRecordsNotFound() throws Exception {
            AttendanceDates payload = new AttendanceDates();
            payload.setAttendanceDates(List.of(LocalDate.of(2024, 1, 2)));

            MojException.NotFound exception = new MojException.NotFound(String.format("No appearance records found for "
                    + "Juror Number: %s, Pool Number: %s and Attendance Dates provided",
                TestConstants.VALID_JUROR_NUMBER,
                TestConstants.VALID_POOL_NUMBER), null);

            Mockito.doThrow(exception).when(jurorExpenseService).submitDraftExpensesForApproval(any(), any(), any());

            mockMvc.perform(post(toUrl(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER))
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtils.asJsonString(payload)))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isNotFound());

            verify(jurorExpenseService, times(1))
                .submitDraftExpensesForApproval(
                    eq(TestConstants.VALID_COURT_LOCATION),
                    eq(TestConstants.VALID_JUROR_NUMBER),
                    eq(payload.getAttendanceDates())
                );
            verifyNoMoreInteractions(jurorExpenseService);
        }
    }

    @Nested
    @DisplayName("POST (get) " + GetEnteredExpenseDetails.URL)
    class GetEnteredExpenseDetails {
        public static final String URL = BASE_URL + "/{juror_number}/entered";

        public String toUrl(String locCode, String jurorNumber) {
            return URL.replace("{loc_code}", locCode)
                .replace("{juror_number}", jurorNumber);
        }

        @Nested
        @DisplayName("Negative")
        class Negative {

            @Test
            void badPayload() throws Exception {
                GetEnteredExpenseRequest request = GetEnteredExpenseRequest.builder()
                    .expenseDates(List.of(LocalDate.now()))
                    .build();
                mockMvc.perform(post(toUrl(TestConstants.VALID_COURT_LOCATION, TestConstants.INVALID_JUROR_NUMBER))
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
                    .build();
                when(jurorExpenseService
                    .getEnteredExpense(TestConstants.VALID_COURT_LOCATION,
                        TestConstants.VALID_JUROR_NUMBER,
                        request.getExpenseDates().get(0))
                ).thenReturn(GetEnteredExpenseResponse.builder()
                    .totalPaid(new BigDecimal("1.23"))
                    .build());
                when(jurorExpenseService
                    .getEnteredExpense(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER,
                        request.getExpenseDates().get(1))
                ).thenReturn(GetEnteredExpenseResponse.builder()
                    .totalPaid(new BigDecimal("2.34"))
                    .build());

                mockMvc.perform(post(toUrl(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(request)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()", CoreMatchers.is(2)))
                    .andExpect(jsonPath("$.[0].total_paid", CoreMatchers.is(1.23)))
                    .andExpect(jsonPath("$.[1].total_paid", CoreMatchers.is(2.34)));

                verify(jurorExpenseService, times(1))
                    .getEnteredExpense(TestConstants.VALID_COURT_LOCATION,
                        TestConstants.VALID_JUROR_NUMBER,
                        request.getExpenseDates().get(0));
            }
        }
    }

    @Nested
    @DisplayName("POST " + ApproveExpenses.URL)
    class ApproveExpenses {
        public static final String URL = BASE_URL + "/{payment_method}/approve";

        private ApproveExpenses() {

        }

        public String toUrl(String locCode, PaymentMethod paymentMethod) {
            return URL.replace("{loc_code}", locCode)
                .replace("{payment_method}", paymentMethod.name());
        }

        @Nested
        @DisplayName("Negative")
        class Negative {

            @Test
            void badPayload() throws Exception {
                ApproveExpenseDto request = ApproveExpenseDto.builder()
                    .jurorNumber(TestConstants.INVALID_JUROR_NUMBER)
                    .approvalType(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL)
                    .dateToRevisions(List.of(
                        ApproveExpenseDto.DateToRevision.builder()
                            .attendanceDate(LocalDate.now())
                            .version(1L).build()
                    ))
                    .build();
                mockMvc.perform(post(toUrl(TestConstants.VALID_COURT_LOCATION, PaymentMethod.CASH))
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
                    .approvalType(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL)
                    .dateToRevisions(List.of(
                        ApproveExpenseDto.DateToRevision.builder()
                            .attendanceDate(LocalDate.now())
                            .version(1L).build()
                    ))
                    .build();

                ApproveExpenseDto request2 = ApproveExpenseDto.builder()
                    .jurorNumber(TestConstants.VALID_JUROR_NUMBER)
                    .approvalType(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL)
                    .dateToRevisions(List.of(
                        ApproveExpenseDto.DateToRevision.builder()
                            .attendanceDate(LocalDate.now().plusDays(1))
                            .version(1L).build()
                    ))
                    .build();
                mockMvc.perform(post(toUrl(TestConstants.VALID_COURT_LOCATION, PaymentMethod.BACS))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(List.of(request1, request2))))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk());

                verify(jurorExpenseService, times(1))
                    .approveExpenses(TestConstants.VALID_COURT_LOCATION, PaymentMethod.BACS, request1);
                verify(jurorExpenseService, times(1))
                    .approveExpenses(TestConstants.VALID_COURT_LOCATION, PaymentMethod.BACS, request2);
            }
        }
    }

    @Nested
    @DisplayName("GET " + GetSimplifiedExpenseDetails.URL)
    class GetSimplifiedExpenseDetails {
        public static final String URL = BASE_URL + "/{juror_number}/{type}/view/simplified";

        public String toUrl(String locCode, String jurorNumber, ExpenseType expenseType) {
            return toUrl(locCode, jurorNumber, expenseType.name());
        }

        public String toUrl(String locCode, String jurorNumber, String expenseType) {
            return URL.replace("{loc_code}", locCode)
                .replace("{juror_number}", jurorNumber)
                .replace("{type}", expenseType);
        }


        @Nested
        @DisplayName("Negative")
        class Negative {

            @Test
            void invalidType() throws Exception {
                mockMvc.perform(get(toUrl(TestConstants.VALID_COURT_LOCATION,
                        TestConstants.VALID_JUROR_NUMBER, "INVALID")))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }

            @Test
            void invalidJurorNumber() throws Exception {
                mockMvc.perform(get(toUrl(TestConstants.VALID_COURT_LOCATION,
                        TestConstants.INVALID_JUROR_NUMBER, ExpenseType.FOR_APPROVAL)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }

            @Test
            void invalidPoolNumber() throws Exception {
                mockMvc.perform(get(toUrl(TestConstants.INVALID_COURT_LOCATION,
                        TestConstants.VALID_JUROR_NUMBER, ExpenseType.FOR_APPROVAL)))
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
                when(jurorExpenseService.getSimplifiedExpense(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER,
                    ExpenseType.FOR_APPROVAL))
                    .thenReturn(combinedExpenseDetailsDto);

                mockMvc.perform(get(toUrl(TestConstants.VALID_COURT_LOCATION,
                        TestConstants.VALID_JUROR_NUMBER, ExpenseType.FOR_APPROVAL.name())))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.expense_details[0].attendance_date", CoreMatchers.is("2023-01-05")));

                verify(jurorExpenseService, times(1))
                    .getSimplifiedExpense(TestConstants.VALID_COURT_LOCATION,
                        TestConstants.VALID_JUROR_NUMBER,
                        ExpenseType.FOR_APPROVAL);
            }
        }
    }

    @Nested
    @DisplayName("GET " + GetDraftExpenses.URL)
    class GetDraftExpenses {
        public static final String URL = BASE_URL + "/{juror_number}/DRAFT/view";


        public String toUrl(String locCode, String jurorNumber) {
            return URL.replace("{loc_code}", locCode)
                .replace("{juror_number}", jurorNumber);
        }

        @Nested
        @DisplayName("Negative")
        class Negative {

            @Test
            void invalidJurorNumber() throws Exception {
                mockMvc.perform(get(toUrl(TestConstants.VALID_COURT_LOCATION, TestConstants.INVALID_JUROR_NUMBER)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }

            @Test
            void invalidLocCode() throws Exception {
                mockMvc.perform(get(toUrl(TestConstants.INVALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER)))
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

                when(jurorExpenseService.getDraftExpenses(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER)).thenReturn(combinedExpenseDetailsDto);

                mockMvc.perform(get(toUrl(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.expense_details[0].attendance_date", CoreMatchers.is("2023-01-05")));

                verify(jurorExpenseService, times(1))
                    .getDraftExpenses(TestConstants.VALID_COURT_LOCATION,
                        TestConstants.VALID_JUROR_NUMBER);
            }
        }
    }

    @Nested
    @DisplayName("GET (POST) " + GetExpenses.URL)
    class GetExpenses {
        public static final String URL = BASE_URL + "/{juror_number}/view";

        public String toUrl(String locCode, String jurorNumber) {
            return URL.replace("{loc_code}", locCode)
                .replace("{juror_number}", jurorNumber);
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
                mockMvc.perform(post(toUrl(TestConstants.VALID_COURT_LOCATION, TestConstants.INVALID_JUROR_NUMBER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(getValidPayload())))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }

            @Test
            void invalidLocCode() throws Exception {
                mockMvc.perform(post(toUrl(TestConstants.INVALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(getValidPayload())))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }

            @Test
            void invalidPayload() throws Exception {
                mockMvc.perform(post(toUrl(TestConstants.INVALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER))
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
                when(jurorExpenseService.getExpenses(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER, payload))
                    .thenReturn(combinedExpenseDetailsDto);

                mockMvc.perform(post(toUrl(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(payload)))
                    .andExpect(status().isOk())
                    .andExpect(content().json(TestUtils.asJsonString(combinedExpenseDetailsDto)));

                verify(jurorExpenseService, times(1))
                    .getExpenses(TestConstants.VALID_COURT_LOCATION,
                        TestConstants.VALID_JUROR_NUMBER, payload);
            }
        }
    }


    @Nested
    @DisplayName("GET " + GetExpensesForApproval.URL)
    class GetExpensesForApproval {
        public static final String URL = BASE_URL + "/{payment_method}/pending-approval";

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
        public static final String URL = BASE_URL + "/{juror_number}/counts";

        public String toUrl(String locCode, String jurorNumber) {
            return URL.replace("{loc_code}", locCode)
                .replace("{juror_number}", jurorNumber);
        }

        @Nested
        @DisplayName("Negative")
        class Negative {

            @Test
            void invalidJurorNumber() throws Exception {
                mockMvc.perform(get(toUrl(TestConstants.VALID_COURT_LOCATION, TestConstants.INVALID_JUROR_NUMBER)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }

            @Test
            void invalidLocCode() throws Exception {
                mockMvc.perform(get(toUrl(TestConstants.INVALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER)))
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

                when(jurorExpenseService.countExpenseTypes(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_JUROR_NUMBER)).thenReturn(expenseCount);

                mockMvc.perform(get(toUrl(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER))
                        .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.total_draft", CoreMatchers.is(1)))
                    .andExpect(jsonPath("$.total_for_approval", CoreMatchers.is(2)))
                    .andExpect(jsonPath("$.total_approved", CoreMatchers.is(3)))
                    .andExpect(jsonPath("$.total_for_reapproval", CoreMatchers.is(4)));

                verify(jurorExpenseService, times(1))
                    .countExpenseTypes(TestConstants.VALID_COURT_LOCATION,
                        TestConstants.VALID_JUROR_NUMBER);
            }
        }
    }

    @Nested
    @DisplayName("PUT " + PostEditDailyExpense.URL)
    class PostEditDailyExpense {
        public static final String URL = BASE_URL + "/{juror_number}/{type}/edit";

        public String toUrl(String locCode, String jurorNumber, String type) {
            return URL
                .replace("{loc_code}", locCode)
                .replace("{juror_number}", jurorNumber)
                .replace("{type}", type);
        }


        @Nested
        @DisplayName("Negative")
        class Negative {
            @Test
            void invalidJurorNumber() throws Exception {
                mockMvc.perform(put(toUrl(TestConstants.VALID_COURT_LOCATION, TestConstants.INVALID_JUROR_NUMBER,
                        ExpenseType.FOR_APPROVAL.name()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(List.of(mockDailyExpense(LocalDate.now())))))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }

            @Test
            void invalidType() throws Exception {
                mockMvc.perform(
                        put(toUrl(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER, "INVALID"))
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


                mockMvc.perform(put(toUrl(TestConstants.VALID_COURT_LOCATION,
                        TestConstants.VALID_JUROR_NUMBER, ExpenseType.APPROVED.name()))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(TestUtils.asJsonString(List.of(dailyExpense1, dailyExpense2, dailyExpense3))))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk());

                verify(jurorExpenseService, times(1))
                    .updateExpense(
                        TestConstants.VALID_COURT_LOCATION,
                        TestConstants.VALID_JUROR_NUMBER,
                        ExpenseType.APPROVED,
                        List.of(dailyExpense1, dailyExpense2, dailyExpense3));
            }

            @Test
            @DisplayName("typical - Draft")
            void typicalDraft() throws Exception {

                DailyExpense dailyExpense1 = mockDailyExpense(LocalDate.now().plusDays(1));

                mockMvc.perform(put(toUrl(TestConstants.VALID_COURT_LOCATION,
                        TestConstants.VALID_JUROR_NUMBER, ExpenseType.DRAFT.name()))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(TestUtils.asJsonString(List.of(dailyExpense1))))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk());

                verify(jurorExpenseService, times(1))
                    .updateDraftExpense(
                        TestConstants.VALID_COURT_LOCATION,
                        TestConstants.VALID_JUROR_NUMBER,
                        dailyExpense1);
            }
        }
    }

    private DailyExpense mockDailyExpense(LocalDate date) {
        return DailyExpense.builder()
            .dateOfExpense(date)
            .paymentMethod(PaymentMethod.BACS)
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
        public static final String URL = BASE_URL + "/{juror_number}/calculate/totals";

        private CalculateTotals() {

        }

        public String toUrl(String locCode, String jurorNumber) {
            return URL.replace("{loc_code}", locCode)
                .replace("{juror_number}", jurorNumber);
        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            @Test
            void invalidPayload() throws Exception {
                mockMvc.perform(post(toUrl(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(CalculateTotalExpenseRequestDto.builder()
                            .expenseList(List.of(
                                DailyExpense.builder()
                                    .dateOfExpense(LocalDate.of(2023, 1, 17))
                                    .paymentMethod(PaymentMethod.CASH)
                                    .time(DailyExpenseTime.builder()
                                        .payAttendance(PayAttendanceType.FULL_DAY)
                                        .build())
                                    .financialLoss(
                                        DailyExpenseFinancialLoss.builder().otherCosts(new BigDecimal("-1")).build()
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
                    .expenseList(List.of(
                        DailyExpense.builder()
                            .dateOfExpense(LocalDate.of(2023, 1, 17))
                            .paymentMethod(PaymentMethod.CASH)
                            .time(DailyExpenseTime.builder()
                                .payAttendance(PayAttendanceType.FULL_DAY)
                                .build())
                            .financialLoss(
                                DailyExpenseFinancialLoss.builder().build()
                            )
                            .build()))
                    .build();

                mockMvc.perform(post(toUrl(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(TestUtils.asJsonString(requestDto)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk());

                verify(jurorExpenseService, times(1))
                    .calculateTotals(
                        TestConstants.VALID_COURT_LOCATION,
                        TestConstants.VALID_JUROR_NUMBER,
                        requestDto);
            }
        }
    }

    @Nested
    @DisplayName("PATCH " + ApportionSmartCard.URL)
    class ApportionSmartCard {
        public static final String URL = BASE_URL + "/{juror_number}/smartcard";

        public String toUrl(String locCode, String jurorNumber) {
            return URL.replace("{loc_code}", locCode)
                .replace("{juror_number}", jurorNumber);
        }

        private ApportionSmartCardRequest getValidPayload() {
            return ApportionSmartCardRequest.builder()
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
                mockMvc.perform(patch(toUrl(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(TestUtils.asJsonString(payload)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isAccepted());

                verify(jurorExpenseService, times(1))
                    .apportionSmartCard(
                        TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER,
                        payload);
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            @Test
            void invalidPayload() throws Exception {
                ApportionSmartCardRequest payload = getValidPayload();
                payload.setSmartCardAmount(new BigDecimal("-1"));
                mockMvc.perform(patch(toUrl(TestConstants.VALID_COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER))
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(TestUtils.asJsonString(payload)))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }
        }
    }

}
