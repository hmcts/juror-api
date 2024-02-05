package uk.gov.hmcts.juror.api.moj.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.DefaultExpenseSummaryDto;
import uk.gov.hmcts.juror.api.moj.controller.request.ViewExpenseRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseItemsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.GetEnteredExpenseRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpense;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseApplyToAllDays;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseFinancialLoss;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseFoodAndDrink;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseTime;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.draft.DailyExpenseTravel;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.BulkExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.BulkExpenseEntryDto;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.DailyExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.FinancialLossWarning;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.GetEnteredExpenseResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.expense.TotalExpenseDto;
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.AppearanceId;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.FoodDrinkClaimType;
import uk.gov.hmcts.juror.api.moj.enumeration.PayAttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.PaymentMethod;
import uk.gov.hmcts.juror.api.moj.enumeration.TravelMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.utils.CustomPageImpl;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + JurorExpenseControllerITest.BASE_URL)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings({"PMD.LawOfDemeter", "PMD.ExcessiveImports"})
class JurorExpenseControllerITest extends AbstractIntegrationTest {

    private static final String PAGINATION_PAGE_NO = "&page_number=0";
    private static final String PAGINATION_SORT_BY = "&sort_by=totalUnapproved&sort_order=DESC";
    private static final String MAX_DATE = "&max_date=";
    private static final String COURT_USER = "COURT_USER";
    private static final String BUREAU_USER = "BUREAU_USER";
    private static final String MIN_DATE = "?min_date=";
    private static final String URL_UNPAID_SUMMARY = "/api/v1/moj/expenses/unpaid-summary/";
    private static final String URL_DEFAULT_SUMMARY = "/api/v1/moj/expenses/default-summary/";
    public static final String BASE_URL = "/api/v1/moj/expenses";

    private final TestRestTemplate template;
    private final AppearanceRepository appearanceRepository;
    private final CourtLocationRepository courtLocationRepository;

    private HttpHeaders httpHeaders;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Nested
    @DisplayName("GET /api/v1/moj/expenses/unpaid-summary/{locCode}")
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_setUp.sql"})
    class GetUnpaidExpenses {
        private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        @Test
        @DisplayName("Valid court user - first page of results")
        void happyPathNoDateRangeFirstPage() throws Exception {
            final String courtLocation = "415";
            final String jwt = createBureauJwt(COURT_USER, courtLocation);
            final URI uri = URI.create(URL_UNPAID_SUMMARY + courtLocation + "?page_number=0&sort_by"
                + "=totalUnapproved&sort_order=DESC");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            ResponseEntity<CustomPageImpl<Void>> response = template.exchange(new RequestEntity<>(httpHeaders, GET,
                uri), new ParameterizedTypeReference<>() {
            });
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            CustomPageImpl<Void> responseBody = response.getBody();
            assertNotNull(responseBody, "Response must be present");

            assertThat(responseBody.getTotalPages()).isEqualTo(2);
            assertThat(responseBody.getTotalElements()).isEqualTo(26);
            assertThat(responseBody.getContent().size()).isEqualTo(25);
        }

        @Test
        @DisplayName("Valid court user - last page of results")
        void happyPathNoDateRangeLastPage() throws Exception {
            final String courtLocation = "415";
            final String jwt = createBureauJwt(COURT_USER, courtLocation);
            final URI uri = URI.create(URL_UNPAID_SUMMARY + courtLocation + "?page_number=1&sort_by"
                + "=totalUnapproved&sort_order=DESC");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            ResponseEntity<CustomPageImpl<Void>> response = template.exchange(new RequestEntity<>(httpHeaders, GET,
                uri), new ParameterizedTypeReference<>() {
            });
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            CustomPageImpl<Void> responseBody = response.getBody();
            assertNotNull(responseBody, "Response must be present");

            assertThat(responseBody.getTotalPages()).isEqualTo(2);
            assertThat(responseBody.getTotalElements()).isEqualTo(26);
            assertThat(responseBody.getContent().size()).isEqualTo(1);
        }

        @Test
        @DisplayName("Valid court user - filter by date range")
        void happyPathWithDateRange() throws Exception {
            final String courtLocation = "415";
            final String jwt = createBureauJwt(COURT_USER, courtLocation);
            final LocalDate minDate = LocalDate.of(2023, 1, 5);
            final LocalDate maxDate = LocalDate.of(2023, 1, 10);
            final URI uri = URI.create(URL_UNPAID_SUMMARY + courtLocation + MIN_DATE
                + dateFormatter.format(minDate) + MAX_DATE + dateFormatter.format(maxDate) + PAGINATION_PAGE_NO
                + PAGINATION_SORT_BY);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            ResponseEntity<CustomPageImpl<Void>> response = template.exchange(new RequestEntity<>(httpHeaders, GET,
                uri), new ParameterizedTypeReference<>() {
            });
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            CustomPageImpl<Void> responseBody = response.getBody();
            assertNotNull(responseBody, "Response must be present");

            assertThat(responseBody.getTotalPages()).isEqualTo(1);
            assertThat(responseBody.getTotalElements()).isEqualTo(11);
            assertThat(responseBody.getContent().size()).isEqualTo(11);
        }

        @Test
        @DisplayName("403 Forbidden - Invalid user")
        void invalidUser() throws Exception {
            final String courtLocation = "415";
            final String jwt = createBureauJwt(COURT_USER, "400");
            final LocalDate minDate = LocalDate.of(2023, 1, 5);
            final LocalDate maxDate = LocalDate.of(2023, 1, 10);
            final URI uri = URI.create(URL_UNPAID_SUMMARY + courtLocation + MIN_DATE
                + dateFormatter.format(minDate) + MAX_DATE + dateFormatter.format(maxDate) + PAGINATION_PAGE_NO
                + PAGINATION_SORT_BY);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            RequestEntity<Void> request = new RequestEntity<>(httpHeaders, GET, uri);
            ResponseEntity<Object> response = template.exchange(request, Object.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        @DisplayName("400 Bad Request - Missing Parameter")
        void missingParameter() throws Exception {
            final String courtLocation = "415";
            final String jwt = createBureauJwt(COURT_USER, "400");
            final LocalDate minDate = LocalDate.of(2023, 1, 5);
            final LocalDate maxDate = LocalDate.of(2023, 1, 10);
            final URI uri = URI.create(URL_UNPAID_SUMMARY + courtLocation + MIN_DATE
                + dateFormatter.format(minDate) + MAX_DATE + dateFormatter.format(maxDate)
                + PAGINATION_SORT_BY);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            RequestEntity<Void> request = new RequestEntity<>(httpHeaders, GET, uri);
            ResponseEntity<Object> response = template.exchange(request, Object.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("404 Not Found - Missing Court Location")
        void invalidUrl() throws Exception {
            final String jwt = createBureauJwt(COURT_USER, "400");
            final LocalDate minDate = LocalDate.of(2023, 1, 5);
            final LocalDate maxDate = LocalDate.of(2023, 1, 10);
            final URI uri = URI.create(URL_UNPAID_SUMMARY + MIN_DATE + dateFormatter.format(minDate)
                + MAX_DATE + dateFormatter.format(maxDate) + PAGINATION_PAGE_NO + PAGINATION_SORT_BY);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            RequestEntity<Void> request = new RequestEntity<>(httpHeaders, GET, uri);
            ResponseEntity<Object> response = template.exchange(request, Object.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }


        private String createBureauJwt(String login, String owner) throws Exception {
            return mintBureauJwt(BureauJWTPayload.builder()
                .userLevel("1")
                .login(login)
                .staff(BureauJWTPayload.Staff.builder()
                    .name("Test User")
                    .active(1)
                    .rank(1)
                    .build())
                .daysToExpire(89)
                .owner(owner).staff(BureauJWTPayload.Staff.builder()
                    .courts(Collections.singletonList(owner))
                    .build())
                .build());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/moj/expenses/default-summary")
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_setUp_default_expenses.sql"})
    class SetDefaultExpenses {

        @Test
        @DisplayName("200 Ok - Happy Path")
        void setDefaultExpensesHappyPath() throws Exception {
            final String jurorNumber = "641500020";
            final String courtLocation = "415";
            final String jwt = createBureauJwt(COURT_USER, courtLocation);
            final URI uri = URI.create(URL_DEFAULT_SUMMARY + jurorNumber);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            DefaultExpenseSummaryDto dto = new DefaultExpenseSummaryDto();
            dto.setJurorNumber(jurorNumber);
            dto.setSmartCardNumber("12345678");
            dto.setTotalSmartCardSpend(40.0);
            dto.setFinancialLoss(0.0);
            dto.setDistanceTraveledMiles(6);
            dto.setTravelTime(LocalTime.of(4, 30));

            RequestEntity<DefaultExpenseSummaryDto> request = new RequestEntity<>(
                DefaultExpenseSummaryDto.builder()
                    .jurorNumber(jurorNumber)
                    .totalSmartCardSpend(40.0)
                    .travelTime(LocalTime.of(4, 30))
                    .distanceTraveledMiles(6)
                    .financialLoss(0.0)
                    .build(), httpHeaders,
                GET, uri);
            ResponseEntity<DefaultExpenseSummaryDto> response =
                template.exchange(request, DefaultExpenseSummaryDto.class);

            DefaultExpenseSummaryDto responseBody = response.getBody();
            assertNotNull(responseBody, "Response must be present");

        }

        @Test
        @DisplayName("404 Not Found - Missing Juror Number")
        void invalidUrl() throws Exception {
            final String jwt = createBureauJwt(COURT_USER, "400");
            final URI uri = URI.create(URL_DEFAULT_SUMMARY);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            RequestEntity<Void> request = new RequestEntity<>(httpHeaders, GET, uri);
            ResponseEntity<Object> response = template.exchange(request, Object.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/moj/expenses/{juror_number}/{identifier}")
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_bulkExpenseSetUp.sql"})
    class GetBulkExpense {
        private static final URI VIEW_EXPENSE_URI = URI.create("/api/v1/moj/expenses");

        private BigDecimal createBigDecimal(double value) {
            return new BigDecimal(String.format("%.2f", value));
        }

        @Nested
        @DisplayName("Negative")
        @SuppressWarnings("PMD.TooManyMethods")
        class Negative {
            private ResponseEntity<String> triggerInvalid(ViewExpenseRequest request) throws Exception {
                return triggerInvalid(List.of(request));
            }

            private ResponseEntity<String> triggerInvalid(List<ViewExpenseRequest> request) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(request, httpHeaders, GET, VIEW_EXPENSE_URI),
                    String.class);
            }

            @Test
            @DisplayName("No appearances")
            @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
            void noAppearances() throws Exception {
                ViewExpenseRequest request = createRequest("641500021", "415230101");
                validateNotFound(triggerInvalid(request), VIEW_EXPENSE_URI.getPath(), "No appearances found");
            }

            @Test
            @DisplayName("Invalid appearances stage")
            @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
            void invalidAppearanceStage() throws Exception {
                ViewExpenseRequest request = createRequest("641500022", "415230101");
                validateInternalServerErrorViolation(triggerInvalid(request), VIEW_EXPENSE_URI.getPath(),
                    "Invalid appearance stage type: APPEARANCE_CONFIRMED");
            }

            @Test
            @DisplayName("Invalid juror number")
            @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
            void invalidJurorNumber() throws Exception {
                ViewExpenseRequest request = createRequest("64150002", "415230101");
                validateInvalidPathParam(triggerInvalid(request),
                    "getBulkExpense.request[0].jurorNumber: must match \"^\\d{9}$\"");
            }

            @Test
            @DisplayName("Invalid pool number")
            @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
            void invalidPoolNumber() throws Exception {
                ViewExpenseRequest request = createRequest("641500020", "41523010");
                validateInvalidPathParam(triggerInvalid(request),
                    "getBulkExpense.request[0].identifier: must match \"^F\\d+$|^\\d{9}$\"");
            }

            @Test
            @DisplayName("Invalid Audit number")
            @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
            void invalidAuditNumber() throws Exception {
                ViewExpenseRequest request = createRequest("641500020", "FABC");
                validateInvalidPathParam(triggerInvalid(request),
                    "getBulkExpense.request[0].identifier: must match \"^F\\d+$|^\\d{9}$\"");
            }

            @Test
            @DisplayName("Unauthorised - none court user")
            @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
            void unauthorisedNoneCourtUser() throws Exception {
                List<ViewExpenseRequest> request = List.of(createRequest("641500020", "415230101"));
                final String jwt = createBureauJwt(COURT_USER, "400");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                validateForbiddenResponse(template.exchange(
                        new RequestEntity<>(request, httpHeaders, GET, VIEW_EXPENSE_URI), String.class),
                    VIEW_EXPENSE_URI.getPath());
            }

            @Test
            @DisplayName("Empty request body")
            @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
            void emptyRequest() throws Exception {
                validateInvalidPathParam(triggerInvalid(List.of()),
                    "getBulkExpense.request: size must be between 1 and 20");
            }

            @Test
            @DisplayName("Request body with null")
            @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
            void requestWithNullItem() throws Exception {
                List<ViewExpenseRequest> request = new ArrayList<>();
                request.add(null);
                validateInvalidPathParam(triggerInvalid(request),
                    "getBulkExpense.request[0].<list element>: must not be null");
            }

            @Test
            @DisplayName("Request has too many items")
            @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
            void tooManyItems() throws Exception {
                List<ViewExpenseRequest> request = new ArrayList<>();
                final int maxRequests = 20;
                for (int index = 0; index < maxRequests + 1; index++) {
                    request.add(ViewExpenseRequest.builder()
                        .jurorNumber(String.valueOf(100_000_000 + index))
                        .identifier("123456789")
                        .build());
                }

                validateInvalidPathParam(triggerInvalid(request),
                    "getBulkExpense.request: size must be between 1 and 20");
            }
        }

        private ViewExpenseRequest createRequest(String jurorNumber, String identifier) {
            return ViewExpenseRequest
                .builder()
                .jurorNumber(jurorNumber)
                .identifier(identifier)
                .build();
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            private ResponseEntity<BulkExpenseDto[]> triggerValid(List<ViewExpenseRequest> request) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<BulkExpenseDto[]> response = template.exchange(
                    new RequestEntity<>(request, httpHeaders, GET, VIEW_EXPENSE_URI), BulkExpenseDto[].class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                return response;
            }

            private ResponseEntity<BulkExpenseDto[]> triggerValid(String jurorNumber,
                                                                  String identifier) throws Exception {
                return triggerValid(List.of(ViewExpenseRequest.builder()
                    .jurorNumber(jurorNumber)
                    .identifier(identifier)
                    .build()));
            }


            @Test
            @DisplayName("Get Draft Expenses (Pool Number)")
            @SuppressWarnings("PMD.LinguisticNaming")
            void getDraftExpensesTest() throws Exception {
                ResponseEntity<BulkExpenseDto[]> response = triggerValid("641500020", "415230101");
                BulkExpenseDto[] responseBody = response.getBody();
                assertNotNull(responseBody, "Response must be present");
                BulkExpenseDto expected = getDraftExpensesExpected();
                assertArrayEquals(new BulkExpenseDto[]{expected}, responseBody,
                    "Expect the response body to match the expected value");
            }

            @Test
            @DisplayName("Positive - Get For Approval Expenses (Audit Number)")
            @SuppressWarnings("PMD.LinguisticNaming")
            void getForApprovalExpensesTest() throws Exception {
                ResponseEntity<BulkExpenseDto[]> response = triggerValid("641500020", "F123");
                BulkExpenseDto[] responseBody = response.getBody();
                assertNotNull(responseBody, "Response must be present");
                BulkExpenseDto expected = getForApprovalExpensesExpected();

                assertArrayEquals(new BulkExpenseDto[]{expected}, responseBody,
                    "Expect the response body to match the expected value");
            }


            @Test
            @DisplayName("Positive - Get Approved Expenses (Audit Number)")
            @SuppressWarnings("PMD.LinguisticNaming")
            void getApprovedExpensesTest() throws Exception {
                ResponseEntity<BulkExpenseDto[]> response = triggerValid("641500020", "F321");
                BulkExpenseDto[] responseBody = response.getBody();
                assertNotNull(responseBody, "Response must be present");
                BulkExpenseDto expected = getApprovedExpensesExpected();
                assertArrayEquals(new BulkExpenseDto[]{expected}, responseBody,
                    "Expect the response body to match the expected value");
            }

            @Test
            @DisplayName("Positive - Get Edited Expenses (Audit Number)")
            @SuppressWarnings("PMD.LinguisticNaming")
            void getEditedExpensesTest() throws Exception {
                ResponseEntity<BulkExpenseDto[]> response = triggerValid("641500020", "F12345");
                BulkExpenseDto[] responseBody = response.getBody();
                assertNotNull(responseBody, "Response must be present");
                BulkExpenseDto expected = getEditedExpensesExpected();
                assertArrayEquals(new BulkExpenseDto[]{expected}, responseBody,
                    "Expect the response body to match the expected value");
            }

            @Test
            @DisplayName("Positive - Get Expenses Multiple")
            @SuppressWarnings("PMD.LinguisticNaming")
            void getMultipleExpensesTest() throws Exception {
                ResponseEntity<BulkExpenseDto[]> response =
                    triggerValid(List.of(
                            //Draft
                            ViewExpenseRequest.builder()
                                .jurorNumber("641500020")
                                .identifier("415230101")
                                .build(),
                            //For approval
                            ViewExpenseRequest.builder()
                                .jurorNumber("641500020")
                                .identifier("F123")
                                .build(),
                            //Approved
                            ViewExpenseRequest.builder()
                                .jurorNumber("641500020")
                                .identifier("F321")
                                .build(),
                            //Edited
                            ViewExpenseRequest.builder()
                                .jurorNumber("641500020")
                                .identifier("F12345")
                                .build()
                        )
                    );

                BulkExpenseDto[] responseBody = response.getBody();
                assertNotNull(responseBody, "Response must be present");
                assertArrayEquals(new BulkExpenseDto[]{
                        getDraftExpensesExpected(),
                        getForApprovalExpensesExpected(),
                        getApprovedExpensesExpected(),
                        getEditedExpensesExpected()
                    }, responseBody,
                    "Expect the response body to match the expected value");
            }

            private BulkExpenseDto getDraftExpensesExpected() {
                return BulkExpenseDto.builder()
                    .jurorNumber("641500020")
                    .jurorVersion(null)//Always null unless approved
                    .submittedOn(null)
                    .submittedBy(null)
                    .approvedOn(null)
                    .approvedBy(null)
                    .type(AppearanceStage.EXPENSE_ENTERED)
                    .mileage(2)
                    .expenses(List.of(
                        BulkExpenseEntryDto.builder()
                            .appearanceDate(LocalDate.of(2023, 1, 5))
                            .attendanceType(AttendanceType.FULL_DAY)
                            .paymentMethod(PaymentMethod.BACS)
                            .originalValue(null)
                            .publicTransport(createBigDecimal(10.00))
                            .taxi(createBigDecimal(20.00))
                            .motorcycle(createBigDecimal(30.00))
                            .car(createBigDecimal(40.00))
                            .bicycle(createBigDecimal(50.00))
                            .parking(createBigDecimal(60.00))
                            .extraCare(createBigDecimal(70.00))
                            .other(createBigDecimal(80.00))
                            .lossOfEarnings(createBigDecimal(90.00))
                            .foodAndDrink(createBigDecimal(100.00))
                            .smartCard(createBigDecimal(25.00))
                            .build(),
                        BulkExpenseEntryDto.builder()
                            .appearanceDate(LocalDate.of(2023, 1, 6))
                            .attendanceType(AttendanceType.FULL_DAY)
                            .paymentMethod(PaymentMethod.CASH)
                            .originalValue(null)
                            .publicTransport(createBigDecimal(11.00))
                            .taxi(createBigDecimal(21.00))
                            .motorcycle(createBigDecimal(31.00))
                            .car(createBigDecimal(41.00))
                            .bicycle(createBigDecimal(51.00))
                            .parking(createBigDecimal(61.00))
                            .extraCare(createBigDecimal(71.00))
                            .other(createBigDecimal(81.00))
                            .lossOfEarnings(createBigDecimal(91.00))
                            .foodAndDrink(createBigDecimal(101.00))
                            .smartCard(createBigDecimal(26.00))
                            .build(),
                        BulkExpenseEntryDto.builder()
                            .appearanceDate(LocalDate.of(2023, 1, 7))
                            .attendanceType(AttendanceType.HALF_DAY)
                            .paymentMethod(PaymentMethod.BACS)
                            .originalValue(null)
                            .publicTransport(createBigDecimal(12.00))
                            .taxi(createBigDecimal(22.00))
                            .motorcycle(createBigDecimal(32.00))
                            .car(createBigDecimal(42.00))
                            .bicycle(createBigDecimal(52.00))
                            .parking(createBigDecimal(62.00))
                            .extraCare(createBigDecimal(72.00))
                            .other(createBigDecimal(82.00))
                            .lossOfEarnings(createBigDecimal(92.00))
                            .foodAndDrink(createBigDecimal(102.00))
                            .smartCard(createBigDecimal(27.00))
                            .build()
                    ))
                    .totals(TotalExpenseDto.builder()
                        .totalAmount(createBigDecimal(1602.00))
                        .totalAmountPaidToDate(createBigDecimal(0.00))
                        .balanceToPay(createBigDecimal(1602.00))
                        .totalDays(3)

                        .publicTransport(createBigDecimal(33.00))
                        .taxi(createBigDecimal(63.00))
                        .motorcycle(createBigDecimal(93.00))
                        .car(createBigDecimal(123.00))
                        .bicycle(createBigDecimal(153.00))
                        .parking(createBigDecimal(183.00))
                        .extraCare(createBigDecimal(213.00))
                        .other(createBigDecimal(243.00))
                        .lossOfEarnings(createBigDecimal(273.00))
                        .foodAndDrink(createBigDecimal(303.00))
                        .smartCard(createBigDecimal(78.00))
                        .build())
                    .build();
            }

            private BulkExpenseDto getForApprovalExpensesExpected() {
                return BulkExpenseDto.builder()
                    .jurorNumber("641500020")
                    .jurorVersion(null)//Always null draft/for approval
                    .approvedBy(null)
                    .approvedOn(null)
                    .submittedBy("smcintyre")
                    .submittedOn(LocalDateTime.of(2024, 1, 9, 10, 0, 0))
                    .type(AppearanceStage.EXPENSE_ENTERED)
                    .mileage(2)
                    .expenses(List.of(
                        BulkExpenseEntryDto.builder()
                            .appearanceDate(LocalDate.of(2023, 1, 8))
                            .attendanceType(AttendanceType.FULL_DAY)
                            .paymentMethod(PaymentMethod.BACS)
                            .originalValue(null)
                            .publicTransport(createBigDecimal(13.97))
                            .taxi(createBigDecimal(23.00))
                            .motorcycle(createBigDecimal(33.00))
                            .car(createBigDecimal(43.00))
                            .bicycle(createBigDecimal(53.00))
                            .parking(createBigDecimal(63.00))
                            .extraCare(createBigDecimal(73.00))
                            .other(createBigDecimal(83.00))
                            .lossOfEarnings(createBigDecimal(93.00))
                            .foodAndDrink(createBigDecimal(103.00))
                            .smartCard(createBigDecimal(28.00))
                            .build(),
                        BulkExpenseEntryDto.builder()
                            .appearanceDate(LocalDate.of(2023, 1, 9))
                            .attendanceType(AttendanceType.FULL_DAY)
                            .paymentMethod(PaymentMethod.BACS)
                            .originalValue(null)
                            .publicTransport(createBigDecimal(14.01))
                            .taxi(createBigDecimal(24.00))
                            .motorcycle(createBigDecimal(34.00))
                            .car(createBigDecimal(44.00))
                            .bicycle(createBigDecimal(54.00))
                            .parking(createBigDecimal(64.00))
                            .extraCare(createBigDecimal(74.00))
                            .other(createBigDecimal(84.00))
                            .lossOfEarnings(createBigDecimal(94.00))
                            .foodAndDrink(createBigDecimal(104.00))
                            .smartCard(createBigDecimal(29.00))
                            .build(),
                        BulkExpenseEntryDto.builder()
                            .appearanceDate(LocalDate.of(2023, 1, 10))
                            .attendanceType(AttendanceType.FULL_DAY)
                            .paymentMethod(PaymentMethod.BACS)
                            .originalValue(null)
                            .publicTransport(createBigDecimal(15.00))
                            .taxi(createBigDecimal(25.00))
                            .motorcycle(createBigDecimal(35.00))
                            .car(createBigDecimal(45.00))
                            .bicycle(createBigDecimal(55.00))
                            .parking(createBigDecimal(65.00))
                            .extraCare(createBigDecimal(75.00))
                            .other(createBigDecimal(85.00))
                            .lossOfEarnings(createBigDecimal(95.00))
                            .foodAndDrink(createBigDecimal(105.00))
                            .smartCard(createBigDecimal(30.00))
                            .build()
                    ))
                    .totals(TotalExpenseDto.builder()
                        .totalAmount(createBigDecimal(1683.98))
                        .totalAmountPaidToDate(createBigDecimal(0.00))
                        .balanceToPay(createBigDecimal(1683.98))
                        .totalDays(3)

                        .publicTransport(createBigDecimal(42.98))
                        .taxi(createBigDecimal(72.00))
                        .motorcycle(createBigDecimal(102.00))
                        .car(createBigDecimal(132.00))
                        .bicycle(createBigDecimal(162.00))
                        .parking(createBigDecimal(192.00))
                        .extraCare(createBigDecimal(222.00))
                        .other(createBigDecimal(252.00))
                        .lossOfEarnings(createBigDecimal(282.00))
                        .foodAndDrink(createBigDecimal(312.00))
                        .smartCard(createBigDecimal(87))
                        .build())
                    .build();
            }

            private BulkExpenseDto getApprovedExpensesExpected() {
                return BulkExpenseDto.builder()
                    .jurorNumber("641500020")
                    .jurorVersion(3L)
                    .approvedBy("alineweaver")
                    .approvedOn(LocalDateTime.of(2024, 1, 10, 12, 0, 0))
                    .submittedBy("sbell")
                    .submittedOn(LocalDateTime.of(2024, 1, 9, 11, 11, 11))
                    .type(AppearanceStage.EXPENSE_AUTHORISED)
                    .mileage(2)
                    .expenses(List.of(
                        BulkExpenseEntryDto.builder()
                            .appearanceDate(LocalDate.of(2023, 1, 11))
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
                            .appearanceDate(LocalDate.of(2023, 1, 12))
                            .attendanceType(AttendanceType.FULL_DAY)
                            .paymentMethod(PaymentMethod.BACS)
                            .originalValue(null)
                            .publicTransport(createBigDecimal(104.00))
                            .taxi(createBigDecimal(94.00))
                            .motorcycle(createBigDecimal(84.00))
                            .car(createBigDecimal(74.00))
                            .bicycle(createBigDecimal(64.00))
                            .parking(createBigDecimal(54.00))
                            .extraCare(createBigDecimal(44.00))
                            .other(createBigDecimal(34.00))
                            .lossOfEarnings(createBigDecimal(24.00))
                            .foodAndDrink(createBigDecimal(14.00))
                            .smartCard(createBigDecimal(29.00))
                            .build(),
                        BulkExpenseEntryDto.builder()
                            .appearanceDate(LocalDate.of(2023, 1, 13))
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
                        .totalAmount(createBigDecimal(1683.00))
                        .totalAmountPaidToDate(createBigDecimal(1683.00))
                        .balanceToPay(createBigDecimal(0.00))
                        .totalDays(3)

                        .publicTransport(createBigDecimal(312.00))
                        .taxi(createBigDecimal(282.00))
                        .motorcycle(createBigDecimal(252.00))
                        .car(createBigDecimal(222.00))
                        .bicycle(createBigDecimal(192.00))
                        .parking(createBigDecimal(162.00))
                        .extraCare(createBigDecimal(132.00))
                        .other(createBigDecimal(102.00))
                        .lossOfEarnings(createBigDecimal(72.00))
                        .foodAndDrink(createBigDecimal(42.00))
                        .smartCard(createBigDecimal(87.00))
                        .build())
                    .build();
            }

            private BulkExpenseDto getEditedExpensesExpected() {
                return BulkExpenseDto.builder()
                    .jurorNumber("641500020")
                    .jurorVersion(null)//Always null when not approved
                    .approvedBy("alineweaver")
                    .approvedOn(LocalDateTime.of(2024, 1, 10, 12, 0, 1))
                    .submittedBy("sbell")
                    .submittedOn(LocalDateTime.of(2024, 1, 9, 12, 12, 12))
                    .type(AppearanceStage.EXPENSE_EDITED)
                    .mileage(2)
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
                        .totalDays(3)

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
        }
    }

    @SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
    abstract class AbstractDraftDailyExpense {
        private final String url;
        private final String methodName;

        AbstractDraftDailyExpense(String url, String methodName) {
            this.url = url;
            this.methodName = methodName;
        }

        protected DailyExpenseTravel createDailyExpenseTravel(TravelMethod travelMethod,
                                                              Integer jurorsTaken,
                                                              Integer milesTraveled,
                                                              Double parking,
                                                              Double publicTransport,
                                                              Double taxi) {
            DailyExpenseTravel.DailyExpenseTravelBuilder builder = DailyExpenseTravel.builder()
                .milesTraveled(milesTraveled)
                .parking(doubleToBigDecimal(parking))
                .publicTransport(doubleToBigDecimal(publicTransport))
                .taxi(doubleToBigDecimal(taxi));

            if (TravelMethod.CAR.equals(travelMethod)) {
                builder.traveledByCar(true)
                    .jurorsTakenCar(jurorsTaken);
            } else if (TravelMethod.MOTERCYCLE.equals(travelMethod)) {
                builder.traveledByMotorcycle(true)
                    .jurorsTakenMotorcycle(jurorsTaken);
            } else if (TravelMethod.BICYCLE.equals(travelMethod)) {
                builder.traveledByBicycle(true);
            }

            return builder.build();
        }

        protected DailyExpenseFinancialLoss createDailyExpenseFinancialLoss(Double lossOfEarnings,
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

        protected DailyExpenseFoodAndDrink createDailyExpenseFoodAndDrink(FoodDrinkClaimType foodDrinkClaimType,
                                                                          Double smartCardAmount) {
            return DailyExpenseFoodAndDrink.builder()
                .foodAndDrinkClaimType(foodDrinkClaimType)
                .smartCardAmount(doubleToBigDecimal(smartCardAmount))
                .build();
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

        protected Appearance getAppearance(String jurorNumber, LocalDate date, String locCode) {
            Optional<CourtLocation> courtLocationOptional = courtLocationRepository.findByLocCode(locCode);
            if (courtLocationOptional.isEmpty()) {
                fail("Failed to find court location");
            }
            Optional<Appearance> appearanceOptional = appearanceRepository.findById(
                new AppearanceId(jurorNumber, date, courtLocationOptional.get()));
            if (appearanceOptional.isEmpty()) {
                fail("Failed to find appearance");
            }
            return appearanceOptional.get();
        }


        protected String toUrl(String jurorNumber) {
            return url.replace("{juror_number}", jurorNumber);
        }

        @DisplayName("Negative")
        @Nested
        class Negative {

            protected ResponseEntity<String> triggerInvalid(String jurorNumber, DailyExpense request) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(request, httpHeaders, POST,
                        URI.create(toUrl(jurorNumber))),
                    String.class);
            }

            @Test
            @SuppressWarnings({
                "PMD.JUnitTestsShouldIncludeAssert" //False positive
            })
            void invalidJurorNumber() throws Exception {
                final String jurorNumber = "INVALID";
                DailyExpense request = DailyExpense.builder()
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

                ResponseEntity<String> response = triggerInvalid(jurorNumber, request);
                validateInvalidPathParam(response,
                    methodName + ".jurorNumber: must match \"^\\d{9}$\"");
            }

            @Test
            @SuppressWarnings({
                "PMD.JUnitTestsShouldIncludeAssert" //False positive
            })
            void noAttendancesFound() throws Exception {
                final String jurorNumber = "123456789";
                DailyExpense request = DailyExpense.builder()
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

                ResponseEntity<String> response = triggerInvalid(jurorNumber, request);
                validateNotFound(response, toUrl(jurorNumber),
                    "No draft appearance record found for juror: 123456789 on day: 2023-01-05");
            }
        }

        @DisplayName("Positive")
        @Nested
        class Positive {

            protected ResponseEntity<DailyExpenseResponse> triggerValid(String jurorNumber,
                                                                        DailyExpense request) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<DailyExpenseResponse> response = template.exchange(
                    new RequestEntity<>(request, httpHeaders, POST,
                        URI.create(toUrl(jurorNumber))),
                    DailyExpenseResponse.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                return response;
            }

        }
    }

    @Nested
    @DisplayName("POST " + PostDraftAttendedDayDailyExpense.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_draftExpenseSetUp.sql"})
    class PostDraftAttendedDayDailyExpense extends AbstractDraftDailyExpense {

        public static final String URL = BASE_URL + "/{juror_number}/draft/attended_day";

        PostDraftAttendedDayDailyExpense() {
            super(URL, "postDraftAttendedDayDailyExpense");
        }

        @Nested
        @DisplayName("Negative")
        class Negative extends AbstractDraftDailyExpense.Negative {

            @Test
            @SuppressWarnings({
                "PMD.JUnitTestsShouldIncludeAssert" //False positive
            })
            void negativeExpenseTotal() throws Exception {
                final String jurorNumber = "641500021";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 5))
                    .poolNumber("415230101")
                    .payCash(false)
                    .time(DailyExpenseTime.builder()
                        .travelTime(LocalTime.of(1, 2))
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .foodAndDrink(
                        createDailyExpenseFoodAndDrink(FoodDrinkClaimType.LESS_THAN_1O_HOURS, 10.72)
                    )
                    .build();
                validateBusinessRuleViolation(triggerInvalid(jurorNumber, request),
                    "Total expenses cannot be less than £0. For Day "
                        + "2023-01-05", MojException.BusinessRuleViolation.ErrorCode.EXPENSES_CANNOT_BE_LESS_THAN_ZERO);
            }

            @Test
            @SuppressWarnings({
                "PMD.JUnitTestsShouldIncludeAssert" //False positive
            })
            void applyToAllWith0Expense() throws Exception {
                final String jurorNumber = "641500022";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 5))
                    .poolNumber("415230101")
                    .payCash(false)
                    .time(DailyExpenseTime.builder()
                        .travelTime(LocalTime.of(1, 2))
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .financialLoss(createDailyExpenseFinancialLoss(0.00, 5.00, 5.00, "Desc"))
                    .applyToAllDays(List.of(DailyExpenseApplyToAllDays.OTHER_COSTS))
                    .build();
                validateBusinessRuleViolation(triggerInvalid(jurorNumber, request),
                    "Total expenses cannot be less than £0. For Day "
                        + "2023-01-06", MojException.BusinessRuleViolation.ErrorCode.EXPENSES_CANNOT_BE_LESS_THAN_ZERO);
            }
        }

        @Nested
        @DisplayName("Positive")
        class Positive extends AbstractDraftDailyExpense.Positive {

            @Test
            void typical() throws Exception {
                final String jurorNumber = "641500021";
                DailyExpense request = DailyExpense.builder()
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
                        createDailyExpenseTravel(TravelMethod.CAR, null, 5, 2.25, null, null)
                    )
                    .foodAndDrink(
                        createDailyExpenseFoodAndDrink(FoodDrinkClaimType.LESS_THAN_1O_HOURS, 4.2)
                    )
                    .build();

                ResponseEntity<DailyExpenseResponse> response = triggerValid(jurorNumber, request);
                assertThat(response).isNotNull();
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getFinancialLossWarning()).isNull();

                Appearance appearance = getAppearance(jurorNumber, request.getDateOfExpense(), "415");

                assertThat(appearance).isNotNull();
                assertThat(appearance.getPayAttendanceType()).isEqualTo(PayAttendanceType.FULL_DAY);
                assertThat(appearance.getPayCash()).isEqualTo(false);
                assertThat(appearance.getTravelTime()).isEqualTo(LocalTime.of(1, 2));

                //Financial Loss
                assertThat(appearance.getLossOfEarningsDue()).isEqualTo(doubleToBigDecimal(25.01));
                assertThat(appearance.getChildcareDue()).isEqualTo(doubleToBigDecimal(10.00));
                assertThat(appearance.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(5.00));
                assertThat(appearance.getMiscDescription()).isEqualTo("Desc");
                //Travel
                assertThat(appearance.getTraveledByCar()).isTrue();
                assertThat(appearance.getJurorsTakenCar()).isNull();
                assertThat(appearance.getCarDue()).isEqualTo(doubleToBigDecimal(1.57));


                assertThat(appearance.getMilesTraveled()).isEqualTo(5);
                assertThat(appearance.getParkingDue()).isEqualTo(doubleToBigDecimal(2.25));
                assertThat(appearance.getPublicTransportDue()).isNull();
                assertThat(appearance.getHiredVehicleDue()).isNull();

                // Substance
                assertThat(appearance.getFoodAndDrinkClaimType()).isEqualTo(FoodDrinkClaimType.LESS_THAN_1O_HOURS);
                assertThat(appearance.getSmartCardAmountDue()).isEqualTo(doubleToBigDecimal(4.2));
                assertThat(appearance.getSubsistenceDue()).isEqualTo(doubleToBigDecimal(5.71));


                assertThat(appearance.getTotalDue()).isEqualTo(doubleToBigDecimal(45.34));

            }


            @Test
            void financialLossExceeded() throws Exception {
                final String jurorNumber = "641500021";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 5))
                    .poolNumber("415230101")
                    .payCash(true)
                    .time(DailyExpenseTime.builder()
                        .travelTime(LocalTime.of(2, 2))
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(45.01, 20.00, 5.00, "Desc 3")
                    )
                    .travel(
                        createDailyExpenseTravel(TravelMethod.MOTERCYCLE, 1, 7, 2.25, 3.2, 1.23)
                    )
                    .foodAndDrink(
                        createDailyExpenseFoodAndDrink(FoodDrinkClaimType.MORE_THAN_10_HOURS, 3.2)
                    )
                    .build();

                ResponseEntity<DailyExpenseResponse> response = triggerValid(jurorNumber, request);
                assertThat(response).isNotNull();
                assertThat(response.getBody()).isNotNull();

                FinancialLossWarning financialLossWarning = response.getBody().getFinancialLossWarning();
                assertThat(financialLossWarning.getDate()).isEqualTo(request.getDateOfExpense());
                assertThat(financialLossWarning.getJurorsLoss()).isEqualTo(doubleToBigDecimal(70.01));
                assertThat(financialLossWarning.getLimit()).isEqualTo(doubleToBigDecimal(64.95, 5));
                assertThat(financialLossWarning.getAttendanceType()).isEqualTo(PayAttendanceType.FULL_DAY);
                assertThat(financialLossWarning.getMessage()).isEqualTo(
                    "The amount you entered will automatically be recalculated to limit the juror's loss to £64.95"
                );
                assertThat(financialLossWarning.getIsLongTrialDay()).isEqualTo(false);


                Appearance appearance = getAppearance(jurorNumber, request.getDateOfExpense(), "415");

                assertThat(appearance).isNotNull();
                assertThat(appearance.getPayAttendanceType()).isEqualTo(PayAttendanceType.FULL_DAY);
                assertThat(appearance.getPayCash()).isEqualTo(true);
                assertThat(appearance.getTravelTime()).isEqualTo(LocalTime.of(2, 2));

                //Financial Loss
                assertThat(appearance.getLossOfEarningsDue()).isEqualTo(doubleToBigDecimal(45.01));
                assertThat(appearance.getChildcareDue()).isEqualTo(doubleToBigDecimal(19.94));
                assertThat(appearance.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(0.00));
                assertThat(appearance.getMiscDescription()).isEqualTo("Desc 3");
                //Travel
                assertThat(appearance.getTraveledByMotorcycle()).isTrue();
                assertThat(appearance.getJurorsTakenMotorcycle()).isEqualTo(1);
                assertThat(appearance.getMotorcycleDue()).isEqualTo(doubleToBigDecimal(2.268));

                assertThat(appearance.getMilesTraveled()).isEqualTo(7);
                assertThat(appearance.getParkingDue()).isEqualTo(doubleToBigDecimal(2.25));
                assertThat(appearance.getPublicTransportDue()).isEqualTo(doubleToBigDecimal(3.2));
                assertThat(appearance.getHiredVehicleDue()).isEqualTo(doubleToBigDecimal(1.23));

                // Substance
                assertThat(appearance.getFoodAndDrinkClaimType()).isEqualTo(FoodDrinkClaimType.MORE_THAN_10_HOURS);
                assertThat(appearance.getSmartCardAmountDue()).isEqualTo(doubleToBigDecimal(3.2));
                assertThat(appearance.getSubsistenceDue()).isEqualTo(doubleToBigDecimal(12.17));

                assertThat(appearance.getTotalDue()).isEqualTo(doubleToBigDecimal(82.87));
            }

            @Test
            void applyToAllFinancialLossExceeded() throws Exception {
                final String jurorNumber = "641500021";
                final String poolNumber = "415230101";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 5))
                    .poolNumber(poolNumber)
                    .payCash(true)
                    .time(DailyExpenseTime.builder()
                        .travelTime(LocalTime.of(2, 2))
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(1.01, 50.00, 35.00, "Desc 3")
                    )
                    .applyToAllDays(List.of(DailyExpenseApplyToAllDays.OTHER_COSTS,
                        DailyExpenseApplyToAllDays.EXTRA_CARE_COSTS,
                        DailyExpenseApplyToAllDays.LOSS_OF_EARNINGS))
                    .build();

                ResponseEntity<DailyExpenseResponse> response = triggerValid(jurorNumber, request);
                assertThat(response).isNotNull();
                assertThat(response.getBody()).isNotNull();

                FinancialLossWarning financialLossWarning = response.getBody().getFinancialLossWarning();
                assertThat(financialLossWarning.getDate()).isEqualTo(request.getDateOfExpense());
                assertThat(financialLossWarning.getJurorsLoss()).isEqualTo(doubleToBigDecimal(86.01));
                assertThat(financialLossWarning.getLimit()).isEqualTo(doubleToBigDecimal(64.95, 5));
                assertThat(financialLossWarning.getAttendanceType()).isEqualTo(PayAttendanceType.FULL_DAY);
                assertThat(financialLossWarning.getMessage()).isEqualTo(
                    "The amount you entered will automatically be recalculated to limit the juror's loss to £64.95"
                );
                assertThat(financialLossWarning.getIsLongTrialDay()).isEqualTo(false);


                Appearance appearance = getAppearance(jurorNumber, request.getDateOfExpense(), "415");

                assertThat(appearance).isNotNull();
                assertThat(appearance.getPayAttendanceType()).isEqualTo(PayAttendanceType.FULL_DAY);
                assertThat(appearance.getPayCash()).isEqualTo(true);
                assertThat(appearance.getTravelTime()).isEqualTo(LocalTime.of(2, 2));

                //Financial Loss
                assertThat(appearance.getLossOfEarningsDue()).isEqualTo(doubleToBigDecimal(1.01));
                assertThat(appearance.getChildcareDue()).isEqualTo(doubleToBigDecimal(50.00));
                assertThat(appearance.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(13.94));
                assertThat(appearance.getMiscDescription()).isEqualTo("Desc 3");

                assertThat(appearance.getTotalDue()).isEqualTo(doubleToBigDecimal(64.95));


                List<Appearance> appearances =
                    appearanceRepository.findByJurorNumberAndPoolNumberAndIsDraftExpenseTrue(jurorNumber, poolNumber);

                assertThat(appearances).size().isEqualTo(3);
                appearances.forEach(appearance1 -> {
                    if (!appearance1.getAttendanceDate().equals(request.getDateOfExpense())) {
                        if (PayAttendanceType.HALF_DAY.equals(appearance1.getPayAttendanceType())) {
                            assertThat(appearance1.getLossOfEarningsDue()).isEqualTo(doubleToBigDecimal(1.01));
                            assertThat(appearance1.getChildcareDue()).isEqualTo(doubleToBigDecimal(31.46));
                            assertThat(appearance1.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(0.00));
                            assertThat(appearance1.getMiscDescription()).isEqualTo("Desc 3");
                            assertThat(appearance1.getTotalDue()).isEqualTo(doubleToBigDecimal(32.47));
                        } else if (PayAttendanceType.FULL_DAY.equals(appearance1.getPayAttendanceType())) {
                            assertThat(appearance1.getLossOfEarningsDue()).isEqualTo(doubleToBigDecimal(1.01));
                            assertThat(appearance1.getChildcareDue()).isEqualTo(doubleToBigDecimal(50.00));
                            assertThat(appearance1.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(13.94));
                            assertThat(appearance1.getMiscDescription()).isEqualTo("Desc 3");
                            assertThat(appearance1.getTotalDue()).isEqualTo(doubleToBigDecimal(64.95));
                        } else {
                            fail("Not handled");
                        }
                    }
                });
            }

            @Test
            void applyToAllDaysMultipleIncludingTravelAndNonAttendedDays() throws Exception {
                final String jurorNumber = "641500021";
                final String poolNumber = "415230101";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 5))
                    .poolNumber(poolNumber)
                    .payCash(true)
                    .time(DailyExpenseTime.builder()
                        .travelTime(LocalTime.of(2, 2))
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .travel(createDailyExpenseTravel(TravelMethod.CAR, 2, 6, 2.4, 0.0, 0.0))
                    .financialLoss(
                        createDailyExpenseFinancialLoss(0.00, 0.00, 15.00, "Desc 3")
                    )
                    .applyToAllDays(List.of(DailyExpenseApplyToAllDays.OTHER_COSTS,
                        DailyExpenseApplyToAllDays.TRAVEL_COSTS))
                    .build();

                ResponseEntity<DailyExpenseResponse> response = triggerValid(jurorNumber, request);
                assertThat(response).isNotNull();
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getFinancialLossWarning()).isNull();

                Appearance appearance = getAppearance(jurorNumber, request.getDateOfExpense(), "415");

                assertThat(appearance).isNotNull();
                assertThat(appearance.getPayAttendanceType()).isEqualTo(PayAttendanceType.FULL_DAY);
                assertThat(appearance.getPayCash()).isEqualTo(true);
                assertThat(appearance.getTravelTime()).isEqualTo(LocalTime.of(2, 2));

                //Financial Loss
                assertThat(appearance.getLossOfEarningsDue()).isEqualTo(doubleToBigDecimal(0.00));
                assertThat(appearance.getChildcareDue()).isEqualTo(doubleToBigDecimal(0.00));
                assertThat(appearance.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(15.00));
                assertThat(appearance.getMiscDescription()).isEqualTo("Desc 3");

                //Travel
                assertThat(appearance.getTraveledByCar()).isTrue();
                assertThat(appearance.getJurorsTakenCar()).isEqualTo(2);
                assertThat(appearance.getCarDue()).isEqualTo(doubleToBigDecimal(2.388));


                assertThat(appearance.getMilesTraveled()).isEqualTo(6);
                assertThat(appearance.getParkingDue()).isEqualTo(doubleToBigDecimal(2.4));
                assertThat(appearance.getPublicTransportDue()).isEqualTo(doubleToBigDecimal(0.0));
                assertThat(appearance.getHiredVehicleDue()).isEqualTo(doubleToBigDecimal(0.0));

                assertThat(appearance.getTotalDue()).isEqualTo(doubleToBigDecimal(19.788));


                List<Appearance> appearances =
                    appearanceRepository.findByJurorNumberAndPoolNumberAndIsDraftExpenseTrue(jurorNumber, poolNumber);

                assertThat(appearances).size().isEqualTo(3);

                AtomicBoolean hasNonAttendanceDay = new AtomicBoolean(false);
                appearances.forEach(appearance1 -> {
                    if (!appearance1.getAttendanceDate().equals(request.getDateOfExpense())) {
                        if (AttendanceType.NON_ATTENDANCE.equals(appearance1.getAttendanceType())) {
                            hasNonAttendanceDay.set(true);
                            assertThat(appearance1.getLossOfEarningsDue()).isEqualTo(doubleToBigDecimal(0.00));
                            assertThat(appearance1.getChildcareDue()).isEqualTo(doubleToBigDecimal(0.00));
                            assertThat(appearance1.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(15.00));
                            assertThat(appearance1.getMiscDescription()).isEqualTo("Desc 3");
                            assertThat(appearance1.getTotalDue()).isEqualTo(doubleToBigDecimal(15.00));
                        } else {
                            assertThat(appearance1.getLossOfEarningsDue()).isEqualTo(doubleToBigDecimal(0.00));
                            assertThat(appearance1.getChildcareDue()).isEqualTo(doubleToBigDecimal(0.00));
                            assertThat(appearance1.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(15.00));
                            assertThat(appearance1.getMiscDescription()).isEqualTo("Desc 3");
                            assertThat(appearance1.getTotalDue()).isEqualTo(doubleToBigDecimal(19.788));
                        }
                    }
                });
                assertThat(hasNonAttendanceDay.get()).isTrue();

            }
        }

    }

    @Nested
    @DisplayName("POST " + PostDraftNonAttendedDayDailyExpense.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_draftExpenseSetUp.sql"})
    class PostDraftNonAttendedDayDailyExpense extends AbstractDraftDailyExpense {
        public static final String URL = BASE_URL + "/{juror_number}/draft/non_attended_day";

        PostDraftNonAttendedDayDailyExpense() {
            super(URL, "postDraftNonAttendedDayDailyExpense");
        }

        @Nested
        @DisplayName("Negative")
        class Negative extends AbstractDraftDailyExpense.Negative {

            @Test
            @SuppressWarnings({
                "PMD.JUnitTestsShouldIncludeAssert" //False positive
            })
            void hasTravelExpenses() throws Exception {
                final String jurorNumber = "641500021";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 5))
                    .poolNumber("415230101")
                    .payCash(false)
                    .time(DailyExpenseTime.builder()
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .travel(DailyExpenseTravel.builder().build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc")
                    )
                    .build();

                ResponseEntity<String> response = triggerInvalid(jurorNumber, request);

                validateInvalidPayload(response,
                    new RestResponseEntityExceptionHandler.FieldError("travel", "must be null"));
            }

            @Test
            @SuppressWarnings({
                "PMD.JUnitTestsShouldIncludeAssert" //False positive
            })
            void hasFoodAndDrinkExpenses() throws Exception {
                final String jurorNumber = "641500021";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 5))
                    .poolNumber("415230101")
                    .payCash(false)
                    .time(DailyExpenseTime.builder()
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .foodAndDrink(DailyExpenseFoodAndDrink.builder()
                        .foodAndDrinkClaimType(FoodDrinkClaimType.NONE)
                        .build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc")
                    )
                    .build();

                ResponseEntity<String> response = triggerInvalid(jurorNumber, request);
                validateInvalidPayload(response,
                    new RestResponseEntityExceptionHandler.FieldError("foodAndDrink", "must be null"));
            }

            @Test
            @SuppressWarnings({
                "PMD.JUnitTestsShouldIncludeAssert" //False positive
            })
            void applyToAllHasTravel() throws Exception {
                final String jurorNumber = "641500021";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 5))
                    .poolNumber("415230101")
                    .payCash(false)
                    .time(DailyExpenseTime.builder()
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .applyToAllDays(List.of(DailyExpenseApplyToAllDays.TRAVEL_COSTS))
                    .financialLoss(
                        createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc")
                    )
                    .build();

                ResponseEntity<String> response = triggerInvalid(jurorNumber, request);

                validateInvalidPayload(response,
                    new RestResponseEntityExceptionHandler.FieldError("applyToAllDays[0]",
                        "Non Attendance day can only apply to all for [EXTRA_CARE_COSTS, OTHER_COSTS, PAY_CASH]"));
            }

            @Test
            @SuppressWarnings({
                "PMD.JUnitTestsShouldIncludeAssert" //False positive
            })
            void hasTotalTravelTime() throws Exception {
                final String jurorNumber = "641500021";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 5))
                    .poolNumber("415230101")
                    .payCash(false)
                    .time(DailyExpenseTime.builder()
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .travelTime(LocalTime.of(1, 1))
                        .build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc")
                    )
                    .build();

                ResponseEntity<String> response = triggerInvalid(jurorNumber, request);

                validateInvalidPayload(response,
                    new RestResponseEntityExceptionHandler.FieldError("time.travelTime", "must be null"));
            }
        }

        @Nested
        @DisplayName("Positive")
        class Positive extends AbstractDraftDailyExpense.Positive {
            @Test
            void typical() throws Exception {
                final String jurorNumber = "641500021";
                DailyExpense request = DailyExpense.builder()
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

                ResponseEntity<DailyExpenseResponse> response = triggerValid(jurorNumber, request);
                assertThat(response).isNotNull();
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getFinancialLossWarning()).isNull();

                Appearance appearance = getAppearance(jurorNumber, request.getDateOfExpense(), "415");

                assertThat(appearance).isNotNull();
                assertThat(appearance.getPayAttendanceType()).isEqualTo(PayAttendanceType.FULL_DAY);
                assertThat(appearance.getLossOfEarningsDue()).isEqualTo(doubleToBigDecimal(25.01));
                assertThat(appearance.getChildcareDue()).isEqualTo(doubleToBigDecimal(10.00));
                assertThat(appearance.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(5.00));
                assertThat(appearance.getMiscDescription()).isEqualTo("Desc");
                assertThat(appearance.getPayCash()).isEqualTo(false);

                assertThat(appearance.getTotalDue()).isEqualTo(doubleToBigDecimal(40.01));
            }

            @Test
            void financialLossLimitApplied() throws Exception {
                final String jurorNumber = "641500021";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 6))
                    .poolNumber("415230101")
                    .payCash(false)
                    .time(DailyExpenseTime.builder()
                        .payAttendance(PayAttendanceType.HALF_DAY)
                        .build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(25.00, 10.00, 5.00, "Desc 2")
                    )
                    .build();

                ResponseEntity<DailyExpenseResponse> response = triggerValid(jurorNumber, request);
                assertThat(response).isNotNull();
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getFinancialLossWarning()).isNotNull();

                FinancialLossWarning financialLossWarning = response.getBody().getFinancialLossWarning();
                assertThat(financialLossWarning.getDate()).isEqualTo(request.getDateOfExpense());
                assertThat(financialLossWarning.getJurorsLoss()).isEqualTo(doubleToBigDecimal(40.00));
                assertThat(financialLossWarning.getLimit()).isEqualTo(doubleToBigDecimal(32.47, 5));
                assertThat(financialLossWarning.getAttendanceType()).isEqualTo(PayAttendanceType.HALF_DAY);
                assertThat(financialLossWarning.getMessage()).isEqualTo(
                    "The amount you entered will automatically be recalculated to limit the juror's loss to £32.47"
                );
                assertThat(financialLossWarning.getIsLongTrialDay()).isEqualTo(false);

                Appearance appearance = getAppearance(jurorNumber, request.getDateOfExpense(), "415");

                assertThat(appearance).isNotNull();
                assertThat(appearance.getPayAttendanceType()).isEqualTo(PayAttendanceType.HALF_DAY);
                assertThat(appearance.getLossOfEarningsDue()).isEqualTo(doubleToBigDecimal(25.00));
                assertThat(appearance.getChildcareDue()).isEqualTo(doubleToBigDecimal(7.47));
                assertThat(appearance.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(0.00));
                assertThat(appearance.getMiscDescription()).isEqualTo("Desc 2");
                assertThat(appearance.getPayCash()).isEqualTo(false);

                assertThat(appearance.getTotalDue()).isEqualTo(doubleToBigDecimal(32.47));

            }

            @Test
            void zero() throws Exception {
                final String jurorNumber = "641500021";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 5))
                    .poolNumber("415230101")
                    .payCash(false)
                    .time(DailyExpenseTime.builder()
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(0.0, 0.00, 0.00, null)
                    )
                    .build();

                ResponseEntity<DailyExpenseResponse> response = triggerValid(jurorNumber, request);
                assertThat(response).isNotNull();
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getFinancialLossWarning()).isNull();

                Appearance appearance = getAppearance(jurorNumber, request.getDateOfExpense(), "415");

                assertThat(appearance).isNotNull();
                assertThat(appearance.getPayAttendanceType()).isEqualTo(PayAttendanceType.FULL_DAY);
                assertThat(appearance.getLossOfEarningsDue()).isEqualTo(doubleToBigDecimal(0.00));
                assertThat(appearance.getChildcareDue()).isEqualTo(doubleToBigDecimal(0.00));
                assertThat(appearance.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(0.00));
                assertThat(appearance.getMiscDescription()).isNull();
                assertThat(appearance.getPayCash()).isEqualTo(false);

                assertThat(appearance.getTotalDue()).isEqualTo(doubleToBigDecimal(0.00));
            }

            @Test
            void applyToAllDaysSingle() throws Exception {
                final String jurorNumber = "641500021";
                final String poolNumber = "415230101";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 5))
                    .poolNumber("415230101")
                    .payCash(false)
                    .time(DailyExpenseTime.builder()
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc")
                    )
                    .applyToAllDays(List.of(DailyExpenseApplyToAllDays.OTHER_COSTS))
                    .build();

                ResponseEntity<DailyExpenseResponse> response = triggerValid(jurorNumber, request);
                assertThat(response).isNotNull();
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getFinancialLossWarning()).isNull();

                Appearance appearance = getAppearance(jurorNumber, request.getDateOfExpense(), "415");

                assertThat(appearance).isNotNull();
                assertThat(appearance.getPayAttendanceType()).isEqualTo(PayAttendanceType.FULL_DAY);
                assertThat(appearance.getLossOfEarningsDue()).isEqualTo(doubleToBigDecimal(25.01));
                assertThat(appearance.getChildcareDue()).isEqualTo(doubleToBigDecimal(10.00));
                assertThat(appearance.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(5.00));
                assertThat(appearance.getMiscDescription()).isEqualTo("Desc");
                assertThat(appearance.getTotalDue()).isEqualTo(doubleToBigDecimal(40.01));
                assertThat(appearance.getPayCash()).isEqualTo(false);


                List<Appearance> appearances =
                    appearanceRepository.findByJurorNumberAndPoolNumberAndIsDraftExpenseTrue(jurorNumber, poolNumber);

                assertThat(appearances).size().isEqualTo(3);
                appearances.forEach(appearance1 -> {
                    if (!appearance1.getAttendanceDate().equals(request.getDateOfExpense())) {
                        assertThat(appearance1.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(5.00));
                        assertThat(appearance1.getMiscDescription()).isEqualTo("Desc");
                        //Ensures nothing else is set
                        assertThat(appearance1.getTotalDue()).isEqualTo(doubleToBigDecimal(5.00));
                    }
                });
            }

            @Test
            void applyToAllDaysMultiple() throws Exception {
                final String jurorNumber = "641500021";
                final String poolNumber = "415230101";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 5))
                    .poolNumber("415230101")
                    .payCash(true)
                    .time(DailyExpenseTime.builder()
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc 2")
                    )
                    .applyToAllDays(List.of(DailyExpenseApplyToAllDays.OTHER_COSTS,
                        DailyExpenseApplyToAllDays.EXTRA_CARE_COSTS, DailyExpenseApplyToAllDays.PAY_CASH))
                    .build();

                ResponseEntity<DailyExpenseResponse> response = triggerValid(jurorNumber, request);
                assertThat(response).isNotNull();
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getFinancialLossWarning()).isNull();

                Appearance appearance = getAppearance(jurorNumber, request.getDateOfExpense(), "415");

                assertThat(appearance).isNotNull();
                assertThat(appearance.getPayAttendanceType()).isEqualTo(PayAttendanceType.FULL_DAY);
                assertThat(appearance.getLossOfEarningsDue()).isEqualTo(doubleToBigDecimal(25.01));
                assertThat(appearance.getChildcareDue()).isEqualTo(doubleToBigDecimal(10.00));
                assertThat(appearance.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(5.00));
                assertThat(appearance.getMiscDescription()).isEqualTo("Desc 2");
                assertThat(appearance.getTotalDue()).isEqualTo(doubleToBigDecimal(40.01));
                assertThat(appearance.getPayCash()).isEqualTo(true);


                List<Appearance> appearances =
                    appearanceRepository.findByJurorNumberAndPoolNumberAndIsDraftExpenseTrue(jurorNumber, poolNumber);

                assertThat(appearances).size().isEqualTo(3);
                appearances.forEach(appearance1 -> {
                    if (!appearance1.getAttendanceDate().equals(request.getDateOfExpense())) {
                        assertThat(appearance1.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(5.00));
                        assertThat(appearance.getChildcareDue()).isEqualTo(doubleToBigDecimal(10.00));
                        assertThat(appearance1.getMiscDescription()).isEqualTo("Desc 2");
                        //Ensures nothing else is set
                        assertThat(appearance1.getTotalDue()).isEqualTo(doubleToBigDecimal(15.00));
                        assertThat(appearance.getPayCash()).isEqualTo(true);

                    }
                });
            }
        }

    }


    @Nested
    @DisplayName("GET " + GetEnteredExpenseDetails.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_getEnteredExpenseDetails.sql"})
    class GetEnteredExpenseDetails {
        public static final String URL = BASE_URL + "/entered";


        private GetEnteredExpenseRequest buildRequest(LocalDate date) {
            return GetEnteredExpenseRequest.builder()
                .dateOfExpense(date)
                .jurorNumber("641500020")
                .poolNumber("415230101")
                .build();
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            private ResponseEntity<GetEnteredExpenseResponse> triggerValid(
                GetEnteredExpenseRequest request) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<GetEnteredExpenseResponse> response = template.exchange(
                    new RequestEntity<>(request, httpHeaders, POST, URI.create(URL)),
                    GetEnteredExpenseResponse.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                return response;
            }

            private void validateTime(GetEnteredExpenseResponse.DailyExpenseTimeEntered time, LocalTime timeAtCourt,
                                      PayAttendanceType payAttendanceType, LocalTime travelTime) {
                assertThat(time).isNotNull();
                assertThat(time.getTimeSpentAtCourt()).isEqualTo(timeAtCourt);
                assertThat(time.getPayAttendance()).isEqualTo(payAttendanceType);
                assertThat(time.getTravelTime()).isEqualTo(travelTime);
            }

            private void validateFinancialLoss(DailyExpenseFinancialLoss financialLoss, BigDecimal lossOfEarnings,
                                               BigDecimal extraCare, BigDecimal other, String otherDescription) {
                assertThat(financialLoss).isNotNull();
                assertThat(financialLoss.getLossOfEarningsOrBenefits()).isEqualTo(lossOfEarnings);
                assertThat(financialLoss.getExtraCareCost()).isEqualTo(extraCare);
                assertThat(financialLoss.getOtherCosts()).isEqualTo(other);
                assertThat(financialLoss.getOtherCostsDescription()).isEqualTo(otherDescription);
            }

            private void validateTravel(DailyExpenseTravel travel,
                                        Boolean travelByCar, Integer jurorsByCar,
                                        Boolean travelByMotorcycle, Integer jurorsByMotorcycle,
                                        Boolean travelByBike,
                                        Integer milesTraveled, BigDecimal parking,
                                        BigDecimal publicTransport, BigDecimal taxi) {
                assertThat(travel).isNotNull();
                assertThat(travel.getTraveledByCar()).isEqualTo(travelByCar);
                assertThat(travel.getJurorsTakenCar()).isEqualTo(jurorsByCar);
                assertThat(travel.getTraveledByMotorcycle()).isEqualTo(travelByMotorcycle);
                assertThat(travel.getJurorsTakenMotorcycle()).isEqualTo(jurorsByMotorcycle);
                assertThat(travel.getTraveledByBicycle()).isEqualTo(travelByBike);
                assertThat(travel.getMilesTraveled()).isEqualTo(milesTraveled);
                assertThat(travel.getParking()).isEqualTo(parking);
                assertThat(travel.getPublicTransport()).isEqualTo(publicTransport);
                assertThat(travel.getTaxi()).isEqualTo(taxi);
            }

            private void validateFoodAndDrink(DailyExpenseFoodAndDrink foodAndDrink,
                                              FoodDrinkClaimType foodDrinkClaimType,
                                              BigDecimal smartCardAmount) {
                assertThat(foodAndDrink).isNotNull();
                assertThat(foodAndDrink.getFoodAndDrinkClaimType()).isEqualTo(foodDrinkClaimType);
                assertThat(foodAndDrink.getSmartCardAmount()).isEqualTo(smartCardAmount);
            }

            @Test
            void positiveDraftExpense() throws Exception {
                LocalDate dateOfExpense = LocalDate.of(2023, 1, 5);
                GetEnteredExpenseRequest request = buildRequest(dateOfExpense);

                ResponseEntity<GetEnteredExpenseResponse> responseEntity = triggerValid(request);
                assertThat(responseEntity).isNotNull();
                assertThat(responseEntity.getBody()).isNotNull();
                GetEnteredExpenseResponse response = responseEntity.getBody();
                assertThat(response.getDateOfExpense()).isEqualTo(dateOfExpense);
                assertThat(response.getStage()).isEqualTo(AppearanceStage.EXPENSE_ENTERED);
                assertThat(response.getTotalDue()).isEqualTo(new BigDecimal("525.00"));
                assertThat(response.getTotalPaid()).isEqualTo(new BigDecimal("0.00"));
                assertThat(response.getPayCash()).isEqualTo(true);

                validateTime(response.getTime(),
                    LocalTime.of(6, 30),
                    PayAttendanceType.FULL_DAY,
                    LocalTime.of(0, 40)
                );
                validateFinancialLoss(response.getFinancialLoss(),
                    new BigDecimal("90.00"),
                    new BigDecimal("70.00"),
                    new BigDecimal("80.00"),
                    "Desc 1");
                validateTravel(response.getTravel(),
                    true,
                    1,
                    true,
                    2,
                    true,
                    4,
                    new BigDecimal("60.00"),
                    new BigDecimal("10.00"),
                    new BigDecimal("20.00")
                );
                validateFoodAndDrink(response.getFoodAndDrink(),
                    FoodDrinkClaimType.LESS_THAN_1O_HOURS,
                    new BigDecimal("25.00"));
            }


            @Test
            void positiveForApprovalExpense() throws Exception {
                LocalDate dateOfExpense = LocalDate.of(2023, 1, 8);
                GetEnteredExpenseRequest request = buildRequest(dateOfExpense);

                ResponseEntity<GetEnteredExpenseResponse> responseEntity = triggerValid(request);
                assertThat(responseEntity).isNotNull();
                assertThat(responseEntity.getBody()).isNotNull();
                GetEnteredExpenseResponse response = responseEntity.getBody();
                assertThat(response.getDateOfExpense()).isEqualTo(dateOfExpense);
                assertThat(response.getStage()).isEqualTo(AppearanceStage.EXPENSE_ENTERED);
                assertThat(response.getTotalDue()).isEqualTo(new BigDecimal("552.97"));
                assertThat(response.getTotalPaid()).isEqualTo(new BigDecimal("0.00"));
                assertThat(response.getPayCash()).isEqualTo(false);

                validateTime(response.getTime(),
                    LocalTime.of(4, 0),
                    PayAttendanceType.HALF_DAY,
                    LocalTime.of(0, 40)
                );
                validateFinancialLoss(response.getFinancialLoss(),
                    new BigDecimal("93.00"),
                    new BigDecimal("73.00"),
                    new BigDecimal("83.00"),
                    "Desc 3");
                validateTravel(response.getTravel(),
                    null,
                    null,
                    false,
                    null,
                    true,
                    6,
                    new BigDecimal("63.00"),
                    new BigDecimal("13.97"),
                    new BigDecimal("23.00")
                );
                validateFoodAndDrink(response.getFoodAndDrink(),
                    FoodDrinkClaimType.NONE,
                    new BigDecimal("28.00"));
            }

            @Test
            void positiveApprovedExpense() throws Exception {
                LocalDate dateOfExpense = LocalDate.of(2023, 1, 11);
                GetEnteredExpenseRequest request = buildRequest(dateOfExpense);

                ResponseEntity<GetEnteredExpenseResponse> responseEntity = triggerValid(request);
                assertThat(responseEntity).isNotNull();
                assertThat(responseEntity.getBody()).isNotNull();
                GetEnteredExpenseResponse response = responseEntity.getBody();
                assertThat(response.getDateOfExpense()).isEqualTo(dateOfExpense);
                assertThat(response.getStage()).isEqualTo(AppearanceStage.EXPENSE_AUTHORISED);
                assertThat(response.getTotalDue()).isEqualTo(new BigDecimal("551.48"));
                assertThat(response.getTotalPaid()).isEqualTo(new BigDecimal("541.48"));
                assertThat(response.getPayCash()).isEqualTo(false);

                validateTime(response.getTime(),
                    LocalTime.of(6, 25),
                    PayAttendanceType.FULL_DAY,
                    LocalTime.of(1, 43)
                );
                validateFinancialLoss(response.getFinancialLoss(),
                    new BigDecimal("23.00"),
                    new BigDecimal("43.00"),
                    new BigDecimal("33.00"),
                    "Desc 4");
                validateTravel(response.getTravel(),
                    true,
                    3,
                    null,
                    null,
                    null,
                    9,
                    new BigDecimal("53.00"),
                    new BigDecimal("103.00"),
                    new BigDecimal("93.00")
                );
                validateFoodAndDrink(response.getFoodAndDrink(),
                    FoodDrinkClaimType.MORE_THAN_10_HOURS,
                    new BigDecimal("28.52"));
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            private ResponseEntity<String> triggerInvalid(GetEnteredExpenseRequest request) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(request, httpHeaders, POST, URI.create(URL)),
                    String.class);
            }

            @Test
            void invalidPayload() throws Exception {
                validateInvalidPayload(triggerInvalid(GetEnteredExpenseRequest.builder()
                        .jurorNumber("INVALID")
                        .poolNumber(TestConstants.VALID_POOL_NUMBER)
                        .dateOfExpense(LocalDate.now())
                        .build()),
                    new RestResponseEntityExceptionHandler.FieldError("jurorNumber",
                        "must match \"^\\d{9}$\""));
            }

            @Test
            void appearanceNotFound() throws Exception {
                LocalDate dateOfExpense = LocalDate.of(2024, 1, 11);
                GetEnteredExpenseRequest request = buildRequest(dateOfExpense);
                validateNotFound(triggerInvalid(request),
                    URL, "No appearance record found for juror: 641500020 on day: 2024-01-11");
            }

            @Test
            void unauthorisedBureauUser() throws Exception {
                LocalDate dateOfExpense = LocalDate.of(2024, 1, 11);
                GetEnteredExpenseRequest request = buildRequest(dateOfExpense);
                final String jwt = createBureauJwt(BUREAU_USER, "400");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                validateForbiddenResponse(template.exchange(
                        new RequestEntity<>(request, httpHeaders, POST, URI.create(URL)), String.class),
                    URL);
            }
        }
    }


    @Nested
    @DisplayName("POST /api/v1/moj/expenses/submit-for-approval")
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_submitForApprovalSetUp.sql"})
    class SubmitForApproval {

        final URI uri = URI.create("/api/v1/moj/expenses/submit-for-approval");

        @Test
        @DisplayName("Happy path")
        @SneakyThrows
        void happyPath() {
            final String courtLocation = "415";
            final String jwt = createBureauJwt(COURT_USER, courtLocation);

            final String jurorNumber = "641500020";
            final String poolNumber = "415230101";

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            ExpenseItemsDto payload = new ExpenseItemsDto();
            payload.setJurorNumber(jurorNumber);
            payload.setPoolNumber(poolNumber);
            List<LocalDate> appearanceDates = List.of(LocalDate.of(2024, 1, 2),
                LocalDate.of(2024, 1, 3));
            payload.setAttendanceDates(appearanceDates);

            RequestEntity<ExpenseItemsDto> request = new RequestEntity<>(payload, httpHeaders, POST, uri);
            ResponseEntity<Void> response = template.exchange(request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            List<Appearance> appearances =
                appearanceRepository.findAllByJurorNumberAndPoolNumber(jurorNumber, poolNumber);

            for (Appearance appearance :
                appearances.stream().filter(app -> appearanceDates.contains(app.getAttendanceDate())).toList()) {
                verifyExpenseSubmittedForApproval(appearance);
            }

            for (Appearance appearance :
                appearances.stream().filter(app -> !appearanceDates.contains(app.getAttendanceDate())).toList()) {
                verifyExpenseStillInDraft(appearance);
            }
        }

        @Test
        @DisplayName("No appearance records found")
        @SneakyThrows
        void notFoundError() {
            final String courtLocation = "415";
            final String jwt = createBureauJwt(COURT_USER, courtLocation);

            final String jurorNumber = "641500020";
            final String poolNumber = "415230101";

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            ExpenseItemsDto payload = new ExpenseItemsDto();
            payload.setJurorNumber(jurorNumber);
            payload.setPoolNumber(poolNumber);
            List<LocalDate> appearanceDates = List.of(LocalDate.of(2024, 1, 1));
            payload.setAttendanceDates(appearanceDates);

            RequestEntity<ExpenseItemsDto> request = new RequestEntity<>(payload, httpHeaders, POST, uri);
            ResponseEntity<Void> response = template.exchange(request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Bad Request - Invalid juror number")
        @SneakyThrows
        void invalidJurorNumber() {
            final String courtLocation = "415";
            final String jwt = createBureauJwt(COURT_USER, courtLocation);

            final String jurorNumber = "6415000200";
            final String poolNumber = "415230101";

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            ExpenseItemsDto payload = new ExpenseItemsDto();
            payload.setJurorNumber(jurorNumber);
            payload.setPoolNumber(poolNumber);
            List<LocalDate> appearanceDates = List.of(LocalDate.of(2024, 1, 1));
            payload.setAttendanceDates(appearanceDates);

            RequestEntity<ExpenseItemsDto> request = new RequestEntity<>(payload, httpHeaders, POST, uri);
            ResponseEntity<Void> response = template.exchange(request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Bad Request - Invalid pool number")
        @SneakyThrows
        void invalidPoolNumber() {
            final String courtLocation = "415";
            final String jwt = createBureauJwt(COURT_USER, courtLocation);

            final String jurorNumber = "641500020";
            final String poolNumber = "4152301010";

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            ExpenseItemsDto payload = new ExpenseItemsDto();
            payload.setJurorNumber(jurorNumber);
            payload.setPoolNumber(poolNumber);
            List<LocalDate> appearanceDates = List.of(LocalDate.of(2024, 1, 1));
            payload.setAttendanceDates(appearanceDates);

            RequestEntity<ExpenseItemsDto> request = new RequestEntity<>(payload, httpHeaders, POST, uri);
            ResponseEntity<Void> response = template.exchange(request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Bad Request - Empty attendance date list")
        @SneakyThrows
        void invalidAttendanceDates() {
            final String courtLocation = "415";
            final String jwt = createBureauJwt(COURT_USER, courtLocation);

            final String jurorNumber = "641500020";
            final String poolNumber = "415230101";

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            ExpenseItemsDto payload = new ExpenseItemsDto();
            payload.setJurorNumber(jurorNumber);
            payload.setPoolNumber(poolNumber);
            payload.setAttendanceDates(new ArrayList<>());

            RequestEntity<ExpenseItemsDto> request = new RequestEntity<>(payload, httpHeaders, POST, uri);
            ResponseEntity<Void> response = template.exchange(request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Forbidden Error - Invalid Bureau user")
        @SneakyThrows
        void bureauUser() {
            final String bureauLocCode = "400";
            final String jwt = createBureauJwt(BUREAU_USER, bureauLocCode);

            final String jurorNumber = "641500020";
            final String poolNumber = "415230101";

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            ExpenseItemsDto payload = new ExpenseItemsDto();
            payload.setJurorNumber(jurorNumber);
            payload.setPoolNumber(poolNumber);
            List<LocalDate> appearanceDates = List.of(LocalDate.of(2024, 1, 2));
            payload.setAttendanceDates(appearanceDates);

            RequestEntity<ExpenseItemsDto> request = new RequestEntity<>(payload, httpHeaders, POST, uri);
            ResponseEntity<Void> response = template.exchange(request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        private void verifyExpenseSubmittedForApproval(Appearance appearance) {
            assertThat(appearance.getFinancialAuditDetails())
                .as("Financial Audit Details object should be created/associated")
                .isNotNull();
            assertThat(appearance.getFinancialAuditDetails().getSubmittedOn())
                .as("Financial Audit Details object should be submitted today")
                .isEqualToIgnoringHours(LocalDateTime.now());
            assertThat(appearance.getFinancialAuditDetails().getSubmittedBy().getUsername())
                .as("Financial Audit Details object should be submitted by the current user")
                .isEqualToIgnoringCase("COURT_USER");

            assertThat(appearance.getAppearanceStage())
                .as("Appearance stage should remain unchanged (still entered)")
                .isEqualTo(AppearanceStage.EXPENSE_ENTERED);
            assertThat(appearance.getIsDraftExpense())
                .as("Is draft expense flag should be updated to false (expense no longer draft)")
                .isFalse();
        }

        private void verifyExpenseStillInDraft(Appearance appearance) {
            assertThat(appearance.getFinancialAuditDetails())
                .as("Financial Audit Details object should not be created/associated")
                .isNull();

            assertThat(appearance.getAppearanceStage())
                .as("Appearance stage should remain unchanged (still entered)")
                .isEqualTo(AppearanceStage.EXPENSE_ENTERED);
            assertThat(appearance.getIsDraftExpense())
                .as("Is draft expense flag should remain unchanged (expense still in draft)")
                .isTrue();
        }

    }
}
