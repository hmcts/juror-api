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
import uk.gov.hmcts.juror.api.moj.controller.request.ViewExpenseRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseItemsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.BulkExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.BulkExpenseEntryDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.TotalExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.UnpaidExpenseSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.SortDirection;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.PaymentMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.repository.JurorRepository;
import uk.gov.hmcts.juror.api.moj.service.BulkService;
import uk.gov.hmcts.juror.api.moj.service.BulkServiceImpl;
import uk.gov.hmcts.juror.api.moj.service.expense.JurorExpenseServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
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
    class SetDefaultExpenses {

        @Test
        @DisplayName("Valid Request")
        @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
        void happyPathForSetDefaultExpenses() throws Exception {
            String jurorNumber = "111111111";

            BureauJWTPayload jwtPayload = TestUtils.createJwt(TestConstants.VALID_COURT_LOCATION, "COURT_USER");
            jwtPayload.setStaff(
                TestUtils.staffBuilder("Court User", 1, Collections.singletonList(TestConstants.VALID_COURT_LOCATION)));
            BureauJwtAuthentication mockPrincipal = mock(BureauJwtAuthentication.class);
            when(mockPrincipal.getPrincipal()).thenReturn(jwtPayload);

            Juror juror = new Juror();
            juror.setJurorNumber(jurorNumber);

            when(jurorRepository.findById(jurorNumber)).thenReturn(Optional.of(juror));

            mockMvc.perform(get(String.format(BASE_URL + "/default-summary/111111111"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .principal(mockPrincipal))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk());

        }
    }


    @Nested
    @DisplayName("GET " + BASE_URL)
    class GetBulkExpense {
        private static final String URL = BASE_URL;

        @Nested
        @DisplayName("Positive")
        class Positive {
            @Test
            @DisplayName("Single financial audit number")
            void singleFinancialAuditNumber() throws Exception {
                ViewExpenseRequest payload = new ViewExpenseRequest();
                payload.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
                payload.setIdentifier("F12345");


                BulkExpenseDto response = exampleResponse();
                when(jurorExpenseService.getBulkExpense(payload.getJurorNumber(), 12_345L)).thenReturn(
                    response);

                mockMvc.perform(get(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(List.of(payload))))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(content().json(TestUtils.asJsonString(List.of(response))));

                verify(jurorExpenseService, times(1)).getBulkExpense(payload.getJurorNumber(), 12_345L);
                verifyNoMoreInteractions(jurorExpenseService);
            }

            @Test
            @DisplayName("Single Pool number")
            void singlePoolNumber() throws Exception {
                ViewExpenseRequest payload = new ViewExpenseRequest();
                payload.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
                payload.setIdentifier(TestConstants.VALID_POOL_NUMBER);


                BulkExpenseDto response = exampleResponse();
                when(jurorExpenseService.getBulkDraftExpense(payload.getJurorNumber(), payload.getIdentifier()))
                    .thenReturn(response);

                mockMvc.perform(get(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(List.of(payload))))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(content().json(TestUtils.asJsonString(List.of(response))));

                verify(jurorExpenseService, times(1)).getBulkDraftExpense(payload.getJurorNumber(),
                    payload.getIdentifier());
                verifyNoMoreInteractions(jurorExpenseService);
            }

            @Test
            @DisplayName("Multiple pool and financial audit number")
            void multiplePoolAndFinancialAuditNumbers() throws Exception {
                ViewExpenseRequest payload1 = new ViewExpenseRequest();
                payload1.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
                payload1.setIdentifier("F12345");

                ViewExpenseRequest payload2 = new ViewExpenseRequest();
                payload2.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
                payload2.setIdentifier(TestConstants.VALID_POOL_NUMBER);

                ViewExpenseRequest payload3 = new ViewExpenseRequest();
                payload3.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
                payload3.setIdentifier("F123456789");

                BulkExpenseDto response = exampleResponse();

                when(jurorExpenseService.getBulkExpense(payload1.getJurorNumber(), 12_345L))
                    .thenReturn(response);

                when(
                    jurorExpenseService.getBulkDraftExpense(payload1.getJurorNumber(), TestConstants.VALID_POOL_NUMBER))
                    .thenReturn(response);

                when(jurorExpenseService.getBulkExpense(payload3.getJurorNumber(), 123_456_789L))
                    .thenReturn(response);

                mockMvc.perform(get(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(List.of(payload1, payload2, payload3))))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isOk())
                    .andExpect(content().json(TestUtils.asJsonString(List.of(response, response, response))));

                verify(jurorExpenseService, times(1)).getBulkExpense(payload1.getJurorNumber(), 12_345L);

                verify(jurorExpenseService, times(1)).getBulkDraftExpense(payload2.getJurorNumber(),
                    payload2.getIdentifier());
                verify(jurorExpenseService, times(1)).getBulkExpense(payload3.getJurorNumber(), 123_456_789L);

                verifyNoMoreInteractions(jurorExpenseService);
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            @Test
            @DisplayName("Invalid payload")
            void invalidPayload() throws Exception {
                ViewExpenseRequest payload = new ViewExpenseRequest();
                payload.setJurorNumber("INVALID");
                payload.setIdentifier("F12345");

                mockMvc.perform(get(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(List.of(payload))))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }

            @Test
            @DisplayName("Empty payload")
            void emptyPayload() throws Exception {
                mockMvc.perform(get(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(List.of())))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isBadRequest());
                verifyNoInteractions(jurorExpenseService);
            }

            @Test
            @DisplayName("Not Found")
            void notFound() throws Exception {
                ViewExpenseRequest payload = new ViewExpenseRequest();
                payload.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
                payload.setIdentifier(TestConstants.VALID_POOL_NUMBER);

                MojException.NotFound exception = new MojException.NotFound("No appearances found", null);
                when(jurorExpenseService.getBulkDraftExpense(payload.getJurorNumber(), payload.getIdentifier()))
                    .thenThrow(exception);

                mockMvc.perform(get(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(List.of(payload))))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isNotFound());

                verify(jurorExpenseService, times(1))
                    .getBulkDraftExpense(payload.getJurorNumber(), payload.getIdentifier());
                verifyNoMoreInteractions(jurorExpenseService);
            }

            @Test
            @DisplayName("Internal Server Error")
            void internalServerError() throws Exception {
                ViewExpenseRequest payload = new ViewExpenseRequest();
                payload.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
                payload.setIdentifier(TestConstants.VALID_POOL_NUMBER);

                MojException.InternalServerError exception = new MojException.InternalServerError("Invalid appearance"
                    + " stage type: CHECKED_IN", null);
                when(jurorExpenseService.getBulkDraftExpense(payload.getJurorNumber(), payload.getIdentifier()))
                    .thenThrow(exception);

                mockMvc.perform(get(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtils.asJsonString(List.of(payload))))
                    .andDo(MockMvcResultHandlers.print())
                    .andExpect(status().isInternalServerError());

                verify(jurorExpenseService, times(1))
                    .getBulkDraftExpense(payload.getJurorNumber(), payload.getIdentifier());
                verifyNoMoreInteractions(jurorExpenseService);

            }
        }

        private BulkExpenseDto exampleResponse() {
            return BulkExpenseDto.builder()
                .jurorNumber("641500020")
                .jurorVersion(null)//Always null when not approved
                .type(AppearanceStage.EXPENSE_EDITED)
                .mileage(34)
                .expenses(List.of(
                    BulkExpenseEntryDto.builder()
                        .appearanceDate(LocalDate.of(2023, 1, 14))
                        .attendanceType(AttendanceType.FULL_DAY)
                        .paymentMethod(PaymentMethod.BACS)
                        .originalValue(null)
                        .publicTransport(createBigDecimal(103.00))
                        .taxi(createBigDecimal(93.00))
                        .motorcycle(createBigDecimal(83.00))
                        .car(createBigDecimal(73.00))
                        .bicycle(createBigDecimal(63.00))
                        .parking(createBigDecimal(53.00))
                        .extraCare(createBigDecimal(43.00))
                        .other(createBigDecimal(33.00))
                        .lossOfEarnings(createBigDecimal(23.00))
                        .foodAndDrink(createBigDecimal(13.00))
                        .smartCard(createBigDecimal(28.00))
                        .build(),
                    BulkExpenseEntryDto.builder()
                        .appearanceDate(LocalDate.of(2023, 1, 15))
                        .attendanceType(AttendanceType.FULL_DAY)
                        .paymentMethod(PaymentMethod.BACS)
                        .originalValue(BulkExpenseEntryDto.builder()
                            .appearanceDate(LocalDate.of(2023, 1, 15))
                            .attendanceType(AttendanceType.FULL_DAY)
                            .paymentMethod(PaymentMethod.BACS)
                            .publicTransport(createBigDecimal(104))
                            .taxi(createBigDecimal(94.00))
                            .motorcycle(createBigDecimal(84.00))
                            .car(createBigDecimal(74.00))
                            .bicycle(createBigDecimal(64.00))
                            .parking(createBigDecimal(54.00))
                            .extraCare(createBigDecimal(44))
                            .other(createBigDecimal(34))
                            .lossOfEarnings(createBigDecimal(24))
                            .foodAndDrink(createBigDecimal(14))
                            .smartCard(createBigDecimal(29))
                            .build()
                        )
                        .publicTransport(createBigDecimal(134))
                        .taxi(createBigDecimal(98))
                        .motorcycle(createBigDecimal(95))
                        .car(createBigDecimal(74))
                        .bicycle(createBigDecimal(83.45))
                        .parking(createBigDecimal(67.00))
                        .extraCare(createBigDecimal(44.44))
                        .other(createBigDecimal(36.03))
                        .lossOfEarnings(createBigDecimal(24.01))
                        .foodAndDrink(createBigDecimal(17.93))
                        .smartCard(createBigDecimal(29))
                        .build(),
                    BulkExpenseEntryDto.builder()
                        .appearanceDate(LocalDate.of(2023, 1, 16))
                        .attendanceType(AttendanceType.FULL_DAY)
                        .paymentMethod(PaymentMethod.BACS)
                        .originalValue(null)
                        .publicTransport(createBigDecimal(105.00))
                        .taxi(createBigDecimal(95.00))
                        .motorcycle(createBigDecimal(85.00))
                        .car(createBigDecimal(75.00))
                        .bicycle(createBigDecimal(65.00))
                        .parking(createBigDecimal(55.00))
                        .extraCare(createBigDecimal(45.00))
                        .other(createBigDecimal(35.00))
                        .lossOfEarnings(createBigDecimal(25.00))
                        .foodAndDrink(createBigDecimal(15.00))
                        .smartCard(createBigDecimal(30.00))
                        .build()
                ))
                .totals(TotalExpenseDto.builder()
                    .totalAmount(createBigDecimal(1766.86))
                    .totalAmountPaidToDate(createBigDecimal(1683.00))
                    .balanceToPay(createBigDecimal(83.86))

                    .publicTransport(createBigDecimal(342.00))
                    .taxi(createBigDecimal(286.00))
                    .motorcycle(createBigDecimal(263.00))
                    .car(createBigDecimal(222.00))
                    .bicycle(createBigDecimal(211.45))
                    .parking(createBigDecimal(175.00))
                    .extraCare(createBigDecimal(132.44))
                    .other(createBigDecimal(104.03))
                    .lossOfEarnings(createBigDecimal(72.01))
                    .foodAndDrink(createBigDecimal(45.93))
                    .smartCard(createBigDecimal(87.00))
                    .build())
                .build();
        }

        private BigDecimal createBigDecimal(double value) {
            return new BigDecimal(String.format("%.2f", value));
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
}
