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
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
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
import uk.gov.hmcts.juror.api.moj.domain.Appearance;
import uk.gov.hmcts.juror.api.moj.domain.AppearanceId;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetailsAppearances;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.PaymentData;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.AttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.FoodDrinkClaimType;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.PayAttendanceType;
import uk.gov.hmcts.juror.api.moj.enumeration.PaymentMethod;
import uk.gov.hmcts.juror.api.moj.enumeration.TravelMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.repository.AppearanceRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.FinancialAuditDetailsAppearancesRepository;
import uk.gov.hmcts.juror.api.moj.repository.FinancialAuditDetailsRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.PaymentDataRepository;
import uk.gov.hmcts.juror.api.utils.CustomPageImpl;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
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

    private static final String URL_SET_DEFAULT_EXPENSES = "/api/v1/moj/expenses/set-default-expenses";
    public static final String BASE_URL = "/api/v1/moj/expenses";

    private final TestRestTemplate template;
    private final AppearanceRepository appearanceRepository;
    private final FinancialAuditDetailsRepository financialAuditDetailsRepository;
    private final FinancialAuditDetailsAppearancesRepository financialAuditDetailsAppearancesRepository;
    private final CourtLocationRepository courtLocationRepository;
    private final JurorHistoryRepository jurorHistoryRepository;
    private final PaymentDataRepository paymentDataRepository;

    private HttpHeaders httpHeaders;

    @Autowired
    private Clock clock;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    private void assertJurorHistory(String jurorNumber, HistoryCodeMod historyCodeMod, String courtUser,
                                    String otherInfo, String poolNumber, LocalDate date, String reference) {
        assertJurorHistory(jurorNumber, historyCodeMod, courtUser, otherInfo, poolNumber, date, reference, 1, 0);
    }

    private void assertJurorHistory(String jurorNumber, HistoryCodeMod historyCodeMod, String courtUser,
                                    String otherInfo, String poolNumber, LocalDate date, String reference,
                                    int expectedSize, int index) {
        List<JurorHistory> jurorHistory = jurorHistoryRepository.findByJurorNumber(jurorNumber);
        assertThat(jurorHistory).hasSize(expectedSize);
        JurorHistory history = jurorHistory.get(index);
        assertThat(history.getJurorNumber()).isEqualTo(jurorNumber);
        assertThat(history.getPoolNumber()).isEqualTo(poolNumber);
        assertThat(history.getDateCreated()).isNotNull();
        assertThat(history.getHistoryCode()).isEqualTo(historyCodeMod);
        assertThat(history.getCreatedBy()).isEqualTo(courtUser);
        assertThat(history.getOtherInformation()).isEqualTo(otherInfo);
        assertThat(history.getOtherInformationDate()).isEqualTo(date);
        assertThat(history.getOtherInformationRef()).isEqualTo(reference);
    }

    private void assertFinancialAuditDetailsAppearances(
        FinancialAuditDetailsAppearances financialAuditDetailsAppearance, long id,
        LocalDate attendanceDate,
        long appearanceVersion) {
        assertThat(financialAuditDetailsAppearance).isNotNull();
        assertThat(financialAuditDetailsAppearance.getFinancialAuditId()).isEqualTo(id);
        assertThat(financialAuditDetailsAppearance.getPoolNumber()).isEqualTo(ApproveExpenses.POOL_NUMBER);
        assertThat(financialAuditDetailsAppearance.getAttendanceDate()).isEqualTo(attendanceDate);
        assertThat(financialAuditDetailsAppearance.getAppearanceVersion()).isEqualTo(appearanceVersion);
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

            ResponseEntity<CustomPageImpl<Void>> response =
                template.exchange(new RequestEntity<>(httpHeaders, GET, uri),
                    new ParameterizedTypeReference<>() {
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

            ResponseEntity<CustomPageImpl<Void>> response =
                template.exchange(new RequestEntity<>(httpHeaders, GET, uri),
                    new ParameterizedTypeReference<>() {
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

            ResponseEntity<CustomPageImpl<Void>> response =
                template.exchange(new RequestEntity<>(httpHeaders, GET, uri),
                    new ParameterizedTypeReference<>() {
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
            return mintBureauJwt(BureauJwtPayload.builder()
                .userLevel("1")
                .login(login)
                .staff(BureauJwtPayload.Staff.builder()
                    .name("Test User")
                    .active(1)
                    .rank(1)
                    .build())
                .daysToExpire(89)
                .owner(owner).staff(BureauJwtPayload.Staff.builder()
                    .courts(Collections.singletonList(owner))
                    .build())
                .build());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/moj/expenses/default-summary")
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_setUp_default_expenses.sql"})
    class GetDefaultExpenses {

        @Test
        @DisplayName("200 Ok - Happy Path")
        void retrieveDefaultExpensesHappyPath() throws Exception {
            final String jurorNumber = "641500020";
            final String courtLocation = "415";
            final String jwt = createBureauJwt(COURT_USER, courtLocation);
            final URI uri = URI.create(URL_DEFAULT_SUMMARY + jurorNumber);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            DefaultExpenseResponseDto dto = new DefaultExpenseResponseDto();
            dto.setJurorNumber(jurorNumber);
            dto.setSmartCardNumber("12345678");
            dto.setFinancialLoss(BigDecimal.valueOf(0.0));
            dto.setDistanceTraveledMiles(6);
            dto.setTravelTime(LocalTime.of(4, 30));

            RequestEntity<DefaultExpenseResponseDto> request = new RequestEntity<>(httpHeaders, GET, uri);
            ResponseEntity<DefaultExpenseResponseDto> response =
                template.exchange(request, DefaultExpenseResponseDto.class);

            assertThat(response.getBody()).isEqualTo(
                DefaultExpenseResponseDto.builder()
                    .jurorNumber(jurorNumber)
                    .smartCardNumber("12345678")
                    .financialLoss(new BigDecimal("39.12"))
                    .distanceTraveledMiles(6)
                    .travelTime(LocalTime.of(4, 30))
                    .claimingSubsistenceAllowance(true)
                    .build()

            );
        }

        @Test
        @DisplayName("404 Not Found - Missing Juror Number")
        void invalidUrl() throws Exception {
            final String jwt = createBureauJwt(COURT_USER, "400");
            final URI uri = URI.create(URL_DEFAULT_SUMMARY);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            RequestEntity<DefaultExpenseResponseDto> request = new RequestEntity<>(httpHeaders, GET, uri);
            ResponseEntity<DefaultExpenseResponseDto> response = template.exchange(request,
                DefaultExpenseResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("POST /api/v1/moj/expenses/set-default-expenses")
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_setUp_default_expenses.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class SetDefaultExpenses {

        private BigDecimal createBigDecimal(double value) {
            return new BigDecimal(String.format("%.2f", value));
        }

        @Test
        @DisplayName("200 Ok - Happy Path Not Override Draft Expenses")
        void setDefaultExpensesHappyPathNotOverride() throws Exception {
            String jurorNumber = "641500020";
            final String courtLocation = "415";
            final String jwt = createBureauJwt(COURT_USER, courtLocation);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            RequestDefaultExpensesDto payload = new RequestDefaultExpensesDto();
            payload.setJurorNumber(jurorNumber);
            payload.setSmartCardNumber("123456789");
            payload.setFinancialLoss(createBigDecimal(0.00));
            payload.setTravelTime(LocalTime.of(0, 40));
            payload.setDistanceTraveledMiles(2);
            payload.setOverwriteExistingDraftExpenses(false);

            RequestEntity<RequestDefaultExpensesDto> request = new RequestEntity<>(payload, httpHeaders, POST,
                URI.create(URL_SET_DEFAULT_EXPENSES));
            ResponseEntity<Void> response = template.exchange(request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("200 Ok - Happy Path Override Draft Expenses")
        void setDefaultExpensesHappyPathIsOverride() throws Exception {
            String jurorNumber = "641500020";
            final String courtLocation = "415";
            final String jwt = createBureauJwt(COURT_USER, courtLocation);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            RequestDefaultExpensesDto payload = new RequestDefaultExpensesDto();
            payload.setJurorNumber(jurorNumber);
            payload.setSmartCardNumber("123456789");
            payload.setFinancialLoss(createBigDecimal(0.00));
            payload.setTravelTime(LocalTime.of(0, 40));
            payload.setDistanceTraveledMiles(2);
            payload.setOverwriteExistingDraftExpenses(true);

            RequestEntity<RequestDefaultExpensesDto> request = new RequestEntity<>(payload, httpHeaders, POST,
                URI.create(URL_SET_DEFAULT_EXPENSES));
            ResponseEntity<Void> response = template.exchange(request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

    }

    @SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
    abstract class AbstractDraftDailyExpense {
        protected final String url;
        protected final String methodName;

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
                assertInvalidPathParam(response,
                    methodName + ".jurorNumber: must match \"^\\d{9}$\"");
            }

            @Test
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
                assertNotFound(response, toUrl(jurorNumber),
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
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_draftExpenseSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class PostDraftAttendedDayDailyExpense extends AbstractDraftDailyExpense {

        public static final String URL = BASE_URL + "/{juror_number}/draft/attended_day";

        PostDraftAttendedDayDailyExpense() {
            super(URL, "postDraftAttendedDayDailyExpense");
        }

        @Nested
        @DisplayName("Negative")
        class Negative extends AbstractDraftDailyExpense.Negative {

            @Test
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
                        createDailyExpenseFoodAndDrink(FoodDrinkClaimType.LESS_THAN_OR_EQUAL_TO_10_HOURS, 10.72)
                    )
                    .build();
                assertBusinessRuleViolation(triggerInvalid(jurorNumber, request),
                    "Total expenses cannot be less than £0. For Day "
                        + "2023-01-05", MojException.BusinessRuleViolation.ErrorCode.EXPENSES_CANNOT_BE_LESS_THAN_ZERO);
            }

            @Test
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
                assertBusinessRuleViolation(triggerInvalid(jurorNumber, request),
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
                        createDailyExpenseFoodAndDrink(FoodDrinkClaimType.LESS_THAN_OR_EQUAL_TO_10_HOURS, 4.2)
                    )
                    .build();

                ResponseEntity<DailyExpenseResponse> response = triggerValid(jurorNumber, request);
                assertThat(response).isNotNull();
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getFinancialLossWarning()).isNull();

                Appearance appearance = getAppearance(jurorNumber, request.getDateOfExpense(), "415");

                assertThat(appearance).isNotNull();
                assertThat(appearance.getPayAttendanceType()).isEqualTo(PayAttendanceType.FULL_DAY);
                assertThat(appearance.isPayCash()).isEqualTo(false);
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

                // Subsistence
                assertThat(appearance.getFoodAndDrinkClaimType())
                    .isEqualTo(FoodDrinkClaimType.LESS_THAN_OR_EQUAL_TO_10_HOURS);
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
                assertThat(appearance.isPayCash()).isEqualTo(true);
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

                // Subsistence
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

                List<Appearance> appearances =
                    appearanceRepository.findByJurorNumberAndPoolNumberAndIsDraftExpenseTrue(jurorNumber, poolNumber);

                assertThat(appearances).size().isEqualTo(4);
                appearances.forEach(appearance1 -> {
                    if (PayAttendanceType.HALF_DAY.equals(appearance1.getPayAttendanceType())) {
                        assertThat(appearance1.getLossOfEarningsDue()).isEqualTo(doubleToBigDecimal(1.01));
                        assertThat(appearance1.getChildcareDue()).isEqualTo(doubleToBigDecimal(31.46));
                        assertThat(appearance1.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(0.00));
                        assertThat(appearance1.getMiscDescription()).isEqualTo("Desc 3");
                        assertThat(appearance1.getTotalDue()).isEqualTo(doubleToBigDecimal(32.47));
                    } else if (PayAttendanceType.FULL_DAY.equals(appearance1.getPayAttendanceType())) {
                        assertThat(appearance1.getLossOfEarningsDue()).isEqualTo(doubleToBigDecimal(1.01));
                        assertThat(appearance1.getChildcareDue()).isEqualTo(doubleToBigDecimal(50.00));
                        assertThat(appearance1.getMiscDescription()).isEqualTo("Desc 3");
                        if (AttendanceType.NON_ATTENDANCE_LONG_TRIAL.equals(appearance1.getAttendanceType())) {
                            assertThat(appearance1.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(35.00));
                            assertThat(appearance1.getTotalDue()).isEqualTo(doubleToBigDecimal(86.01));
                        } else {
                            assertThat(appearance1.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(13.94));
                            assertThat(appearance1.getTotalDue()).isEqualTo(doubleToBigDecimal(64.95));
                        }
                    } else {
                        fail("Not handled");
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

                List<Appearance> appearances =
                    appearanceRepository.findByJurorNumberAndPoolNumberAndIsDraftExpenseTrue(jurorNumber, poolNumber);

                assertThat(appearances).size().isEqualTo(4);

                AtomicBoolean hasNonAttendanceDay = new AtomicBoolean(false);
                appearances.forEach(appearance1 -> {
                    if (AttendanceType.NON_ATTENDANCE.equals(appearance1.getAttendanceType())
                        || AttendanceType.NON_ATTENDANCE_LONG_TRIAL.equals(appearance1.getAttendanceType())) {
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
                });
                assertThat(hasNonAttendanceDay.get()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("POST " + PostDraftNonAttendedDayDailyExpense.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_draftExpenseSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class PostDraftNonAttendedDayDailyExpense extends AbstractDraftDailyExpense {
        public static final String URL = BASE_URL + "/{juror_number}/draft/non_attended_day";

        PostDraftNonAttendedDayDailyExpense() {
            super(URL, "postDraftNonAttendedDayDailyExpense");
        }

        @Nested
        @DisplayName("Negative")
        class Negative extends AbstractDraftDailyExpense.Negative {

            @Test
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

                assertInvalidPayload(response,
                    new RestResponseEntityExceptionHandler.FieldError("travel", "must be null"));
            }

            @Test
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
                assertInvalidPayload(response,
                    new RestResponseEntityExceptionHandler.FieldError("foodAndDrink", "must be null"));
            }

            @Test
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

                assertInvalidPayload(response,
                    new RestResponseEntityExceptionHandler.FieldError("applyToAllDays[0]",
                        "Non Attendance day can only apply to all for [EXTRA_CARE_COSTS, OTHER_COSTS, PAY_CASH]"));
            }

            @Test
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

                assertInvalidPayload(response,
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
                assertThat(appearance.isPayCash()).isEqualTo(false);

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
                assertThat(appearance.isPayCash()).isEqualTo(false);

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
                assertThat(appearance.isPayCash()).isEqualTo(false);

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
                        createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc"))
                    .applyToAllDays(List.of(DailyExpenseApplyToAllDays.OTHER_COSTS))
                    .build();

                ResponseEntity<DailyExpenseResponse> response = triggerValid(jurorNumber, request);
                assertThat(response).isNotNull();
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getFinancialLossWarning()).isNull();

                List<Appearance> appearances =
                    appearanceRepository.findByJurorNumberAndPoolNumberAndIsDraftExpenseTrue(jurorNumber, poolNumber);

                assertThat(appearances).size().isEqualTo(4);
                appearances.forEach(appearance1 -> {
                    assertThat(appearance1.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(5.00));
                    assertThat(appearance1.getMiscDescription()).isEqualTo("Desc");
                    assertThat(appearance1.getTotalDue()).isEqualTo(doubleToBigDecimal(5.00));
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
                    .financialLoss(createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc 2"))
                    .applyToAllDays(List.of(DailyExpenseApplyToAllDays.OTHER_COSTS,
                        DailyExpenseApplyToAllDays.EXTRA_CARE_COSTS,
                        DailyExpenseApplyToAllDays.PAY_CASH))
                    .build();

                ResponseEntity<DailyExpenseResponse> response = triggerValid(jurorNumber, request);
                assertThat(response).isNotNull();
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getFinancialLossWarning()).isNull();

                List<Appearance> appearances =
                    appearanceRepository.findByJurorNumberAndPoolNumberAndIsDraftExpenseTrue(jurorNumber, poolNumber);

                // ensure apply all is updated for all applicable expenses and to all days
                assertThat(appearances).size().isEqualTo(4);
                appearances.forEach(appearance1 -> {
                    assertThat(appearance1.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(5.00));
                    assertThat(appearance1.getChildcareDue()).isEqualTo(doubleToBigDecimal(10.00));
                    assertThat(appearance1.getMiscDescription()).isEqualTo("Desc 2");
                    assertThat(appearance1.getTotalDue()).isEqualTo(doubleToBigDecimal(15.00));
                    assertThat(appearance1.isPayCash()).isEqualTo(true);
                });
            }
        }
    }


    @Nested
    @DisplayName("GET " + GetEnteredExpenseDetails.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_getEnteredExpenseDetails.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class GetEnteredExpenseDetails {
        public static final String URL = BASE_URL + "/entered";


        private GetEnteredExpenseRequest buildRequest(LocalDate date) {
            return GetEnteredExpenseRequest.builder()
                .expenseDates(List.of(date))
                .jurorNumber("641500020")
                .poolNumber("415230101")
                .build();
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            private ResponseEntity<List<GetEnteredExpenseResponse>> triggerValid(
                GetEnteredExpenseRequest request) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<List<GetEnteredExpenseResponse>> response = template.exchange(
                    new RequestEntity<>(request, httpHeaders, POST, URI.create(URL)),
                    new ParameterizedTypeReference<>() {
                    });
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

                ResponseEntity<List<GetEnteredExpenseResponse>> responseEntity = triggerValid(request);
                assertThat(responseEntity).isNotNull();
                assertThat(responseEntity.getBody()).isNotNull();
                List<GetEnteredExpenseResponse> responseList = responseEntity.getBody();
                assertThat(responseList).hasSize(1);
                GetEnteredExpenseResponse response = responseList.get(0);
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
                    FoodDrinkClaimType.LESS_THAN_OR_EQUAL_TO_10_HOURS,
                    new BigDecimal("25.00"));
            }


            @Test
            void positiveForApprovalExpense() throws Exception {
                LocalDate dateOfExpense = LocalDate.of(2023, 1, 8);
                GetEnteredExpenseRequest request = buildRequest(dateOfExpense);

                ResponseEntity<List<GetEnteredExpenseResponse>> responseEntity = triggerValid(request);
                assertThat(responseEntity).isNotNull();
                assertThat(responseEntity.getBody()).isNotNull();
                List<GetEnteredExpenseResponse> responseList = responseEntity.getBody();
                assertThat(responseList).hasSize(1);
                GetEnteredExpenseResponse response = responseList.get(0);
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

                ResponseEntity<List<GetEnteredExpenseResponse>> responseEntity = triggerValid(request);
                assertThat(responseEntity).isNotNull();
                assertThat(responseEntity.getBody()).isNotNull();
                List<GetEnteredExpenseResponse> responseList = responseEntity.getBody();
                assertThat(responseList).hasSize(1);
                GetEnteredExpenseResponse response = responseList.get(0);
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
                assertInvalidPayload(triggerInvalid(GetEnteredExpenseRequest.builder()
                        .jurorNumber("INVALID")
                        .poolNumber(TestConstants.VALID_POOL_NUMBER)
                        .expenseDates(List.of(LocalDate.now()))
                        .build()),
                    new RestResponseEntityExceptionHandler.FieldError("jurorNumber",
                        "must match \"^\\d{9}$\""));
            }

            @Test
            void appearanceNotFound() throws Exception {
                LocalDate dateOfExpense = LocalDate.of(2024, 1, 11);
                GetEnteredExpenseRequest request = buildRequest(dateOfExpense);
                assertNotFound(triggerInvalid(request),
                    URL, "No appearance record found for juror: 641500020 on day: 2024-01-11");
            }

            @Test
            void unauthorisedBureauUser() throws Exception {
                LocalDate dateOfExpense = LocalDate.of(2024, 1, 11);
                GetEnteredExpenseRequest request = buildRequest(dateOfExpense);
                final String jwt = createBureauJwt(BUREAU_USER, "400");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                assertForbiddenResponse(template.exchange(
                        new RequestEntity<>(request, httpHeaders, POST, URI.create(URL)), String.class),
                    URL);
            }
        }
    }


    @Nested
    @DisplayName("POST /api/v1/moj/expenses/submit-for-approval")
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_submitForApprovalSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
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
            assertThat(appearance.getFinancialAuditDetails().getCreatedOn())
                .as("Financial Audit Details object should be submitted today")
                .isEqualToIgnoringHours(LocalDateTime.now(clock));
            assertThat(appearance.getFinancialAuditDetails().getCreatedBy().getUsername())
                .as("Financial Audit Details object should be submitted by the current user")
                .isEqualToIgnoringCase("COURT_USER");
            assertThat(appearance.getFinancialAuditDetails().getType())
                .as("Financial Audit Details object should be to type FOR_APPROVAL")
                .isEqualTo(FinancialAuditDetails.Type.FOR_APPROVAL);

            assertThat(appearance.getFinancialAuditDetails().getCourtLocationRevision())
                .as("Financial Audit Details object should have the correct court revision")
                .isEqualTo(0);

            assertThat(appearance.getFinancialAuditDetails().getJurorRevision())
                .as("Financial Audit Details object should have the correct juror revision")
                .isEqualTo(1);

            assertThat(appearance.getAppearanceStage())
                .as("Appearance stage should remain unchanged (still entered)")
                .isEqualTo(AppearanceStage.EXPENSE_ENTERED);
            assertThat(appearance.isDraftExpense())
                .as("Is draft expense flag should be updated to false (expense no longer draft)")
                .isFalse();
            assertThat(appearance.getExpenseRates()).isNotNull();
            assertThat(appearance.getExpenseRates().getId()).isEqualTo(999_999);
        }

        private void verifyExpenseStillInDraft(Appearance appearance) {
            assertThat(appearance.getFinancialAuditDetails())
                .as("Financial Audit Details object should not be created/associated")
                .isNull();

            assertThat(appearance.getAppearanceStage())
                .as("Appearance stage should remain unchanged (still entered)")
                .isEqualTo(AppearanceStage.EXPENSE_ENTERED);
            assertThat(appearance.isDraftExpense())
                .as("Is draft expense flag should remain unchanged (expense still in draft)")
                .isTrue();
        }
    }

    @Nested
    @DisplayName("GET " + GetEnteredExpenseDetails.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_simplifiedExpenseSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class GetSimplifiedExpenseDetails {
        public static final String URL = BASE_URL + "/view/{type}/simplified";

        public static final String JUROR_NUMBER = "641500020";
        public static final String JUROR_NUMBER_NO_APPEARANCES = "641500024";
        public static final String POOL_NUMBER = "415230101";

        public String toUrl(ExpenseType expenseType) {
            return toUrl(expenseType.name());
        }

        public String toUrl(String expenseType) {
            return URL.replace("{type}", expenseType);
        }

        public URI toUri(ExpenseType expenseType) {
            return toUri(expenseType.name());
        }

        public URI toUri(String expenseType) {
            return URI.create(toUrl(expenseType));
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            protected ResponseEntity<CombinedSimplifiedExpenseDetailDto> triggerValid(
                ExpenseType expenseType,
                JurorNumberAndPoolNumberDto request) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<CombinedSimplifiedExpenseDetailDto> response = template.exchange(
                    new RequestEntity<>(request, httpHeaders, POST,
                        toUri(expenseType)),
                    CombinedSimplifiedExpenseDetailDto.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                assertThat(response.getBody())
                    .as("Expect no body")
                    .isNotNull();
                return response;
            }

            @Test
            void typicalForApproval() throws Exception {
                ResponseEntity<CombinedSimplifiedExpenseDetailDto> response = triggerValid(ExpenseType.FOR_APPROVAL,
                    new JurorNumberAndPoolNumberDto(JUROR_NUMBER, POOL_NUMBER));

                CombinedSimplifiedExpenseDetailDto body = response.getBody();
                assertThat(body.getExpenseDetails()).hasSize(3);
                assertThat(body.getExpenseDetails().get(0)).isEqualTo(
                    SimplifiedExpenseDetailDto.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 8))
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
                assertThat(body.getExpenseDetails().get(1)).isEqualTo(
                    SimplifiedExpenseDetailDto.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 9))
                        .financialAuditNumber("F123")
                        .attendanceType(AttendanceType.FULL_DAY)
                        .financialLoss(new BigDecimal("252.00"))
                        .travel(new BigDecimal("234.01"))
                        .foodAndDrink(new BigDecimal("104.00"))
                        .smartcard(new BigDecimal("29.00"))
                        .totalDue(new BigDecimal("561.01"))
                        .totalPaid(new BigDecimal("0.00"))
                        .balanceToPay(new BigDecimal("561.01"))
                        .auditCreatedOn(LocalDateTime.of(2023, 1, 11, 9, 31, 1))
                        .build()
                );
                assertThat(body.getExpenseDetails().get(2)).isEqualTo(
                    SimplifiedExpenseDetailDto.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 10))
                        .financialAuditNumber("F123")
                        .attendanceType(AttendanceType.FULL_DAY)
                        .financialLoss(new BigDecimal("255.00"))
                        .travel(new BigDecimal("240.00"))
                        .foodAndDrink(new BigDecimal("105.00"))
                        .smartcard(new BigDecimal("30.00"))
                        .totalDue(new BigDecimal("570.00"))
                        .totalPaid(new BigDecimal("0.00"))
                        .balanceToPay(new BigDecimal("570.00"))
                        .auditCreatedOn(LocalDateTime.of(2023, 1, 11, 9, 31, 1))
                        .build()
                );

                CombinedSimplifiedExpenseDetailDto.Total total = body.getTotal();
                assertThat(total).isEqualTo(
                    CombinedSimplifiedExpenseDetailDto.Total.builder()
                        .totalAttendances(3)
                        .financialLoss(new BigDecimal("756.00"))
                        .travel(new BigDecimal("702.98"))
                        .foodAndDrink(new BigDecimal("312.00"))
                        .smartcard(new BigDecimal("87.00"))
                        .totalDue(new BigDecimal("1683.98"))
                        .totalPaid(new BigDecimal("0.00"))
                        .balanceToPay(new BigDecimal("1683.98"))
                        .build());
            }

            @Test
            void typicalApproval() throws Exception {
                ResponseEntity<CombinedSimplifiedExpenseDetailDto> response = triggerValid(ExpenseType.APPROVED,
                    new JurorNumberAndPoolNumberDto(JUROR_NUMBER, POOL_NUMBER));

                CombinedSimplifiedExpenseDetailDto body = response.getBody();
                assertThat(body.getExpenseDetails()).hasSize(3);
                assertThat(body.getExpenseDetails().get(0)).isEqualTo(
                    SimplifiedExpenseDetailDto.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 11))
                        .financialAuditNumber("F321")
                        .attendanceType(AttendanceType.FULL_DAY)
                        .financialLoss(new BigDecimal("99.00"))
                        .travel(new BigDecimal("468.00"))
                        .foodAndDrink(new BigDecimal("13.00"))
                        .smartcard(new BigDecimal("28.00"))
                        .totalDue(new BigDecimal("552.00"))
                        .totalPaid(new BigDecimal("552.00"))
                        .balanceToPay(new BigDecimal("0.00"))
                        .auditCreatedOn(LocalDateTime.of(2023, 1, 12, 9, 32, 2))
                        .build()
                );
                assertThat(body.getExpenseDetails().get(1)).isEqualTo(
                    SimplifiedExpenseDetailDto.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 12))
                        .financialAuditNumber("F3")
                        .attendanceType(AttendanceType.FULL_DAY)
                        .financialLoss(new BigDecimal("102.00"))
                        .travel(new BigDecimal("474.00"))
                        .foodAndDrink(new BigDecimal("14.00"))
                        .smartcard(new BigDecimal("29.00"))
                        .totalDue(new BigDecimal("561.00"))
                        .totalPaid(new BigDecimal("561.00"))
                        .balanceToPay(new BigDecimal("0.00"))
                        .auditCreatedOn(LocalDateTime.of(2023, 1, 13, 9, 33, 3))
                        .build()
                );
                assertThat(body.getExpenseDetails().get(2)).isEqualTo(
                    SimplifiedExpenseDetailDto.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 13))
                        .financialAuditNumber("F321")
                        .attendanceType(AttendanceType.FULL_DAY)
                        .financialLoss(new BigDecimal("105.00"))
                        .travel(new BigDecimal("480.00"))
                        .foodAndDrink(new BigDecimal("15.00"))
                        .smartcard(new BigDecimal("30.00"))
                        .totalDue(new BigDecimal("570.00"))
                        .totalPaid(new BigDecimal("570.00"))
                        .balanceToPay(new BigDecimal("0.00"))
                        .auditCreatedOn(LocalDateTime.of(2023, 1, 12, 9, 32, 2))
                        .build()
                );

                CombinedSimplifiedExpenseDetailDto.Total total = body.getTotal();
                assertThat(total).isEqualTo(
                    CombinedSimplifiedExpenseDetailDto.Total.builder()
                        .totalAttendances(3)
                        .financialLoss(new BigDecimal("306.00"))
                        .travel(new BigDecimal("1422.00"))
                        .foodAndDrink(new BigDecimal("42.00"))
                        .smartcard(new BigDecimal("87.00"))
                        .totalDue(new BigDecimal("1683.00"))
                        .totalPaid(new BigDecimal("1683.00"))
                        .balanceToPay(new BigDecimal("0.00"))
                        .build());
            }

            @Test
            void typicalforReapproval() throws Exception {
                ResponseEntity<CombinedSimplifiedExpenseDetailDto> response = triggerValid(ExpenseType.FOR_REAPPROVAL,
                    new JurorNumberAndPoolNumberDto(JUROR_NUMBER, POOL_NUMBER));

                CombinedSimplifiedExpenseDetailDto body = response.getBody();
                assertThat(body.getExpenseDetails()).hasSize(3);
                assertThat(body.getExpenseDetails().get(0)).isEqualTo(
                    SimplifiedExpenseDetailDto.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 14))
                        .financialAuditNumber("F12345")
                        .attendanceType(AttendanceType.FULL_DAY)
                        .financialLoss(new BigDecimal("129.00"))
                        .travel(new BigDecimal("528.00"))
                        .foodAndDrink(new BigDecimal("23.00"))
                        .smartcard(new BigDecimal("18.00"))
                        .totalDue(new BigDecimal("662.00"))
                        .totalPaid(new BigDecimal("552.00"))
                        .balanceToPay(new BigDecimal("110.00"))
                        .auditCreatedOn(LocalDateTime.of(2023, 1, 14, 9, 34, 4))
                        .build()
                );
                assertThat(body.getExpenseDetails().get(1)).isEqualTo(
                    SimplifiedExpenseDetailDto.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 15))
                        .financialAuditNumber("F12345")
                        .attendanceType(AttendanceType.FULL_DAY)
                        .financialLoss(new BigDecimal("132.00"))
                        .travel(new BigDecimal("534.00"))
                        .foodAndDrink(new BigDecimal("24.00"))
                        .smartcard(new BigDecimal("19.00"))
                        .totalDue(new BigDecimal("671.00"))
                        .totalPaid(new BigDecimal("561.00"))
                        .balanceToPay(new BigDecimal("110.00"))
                        .auditCreatedOn(LocalDateTime.of(2023, 1, 14, 9, 34, 4))
                        .build()
                );
                assertThat(body.getExpenseDetails().get(2)).isEqualTo(
                    SimplifiedExpenseDetailDto.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 16))
                        .financialAuditNumber("F12345")
                        .attendanceType(AttendanceType.HALF_DAY)
                        .financialLoss(new BigDecimal("135.00"))
                        .travel(new BigDecimal("550.00"))
                        .foodAndDrink(new BigDecimal("25.00"))
                        .smartcard(new BigDecimal("0.00"))
                        .totalDue(new BigDecimal("710.00"))
                        .totalPaid(new BigDecimal("570.00"))
                        .balanceToPay(new BigDecimal("140.00"))
                        .auditCreatedOn(LocalDateTime.of(2023, 1, 14, 9, 34, 4))
                        .build()
                );

                CombinedSimplifiedExpenseDetailDto.Total total = body.getTotal();
                assertThat(total).isEqualTo(
                    CombinedSimplifiedExpenseDetailDto.Total.builder()
                        .totalAttendances(3)
                        .financialLoss(new BigDecimal("396.00"))
                        .travel(new BigDecimal("1612.00"))
                        .foodAndDrink(new BigDecimal("72.00"))
                        .smartcard(new BigDecimal("37.00"))
                        .totalDue(new BigDecimal("2043.00"))
                        .totalPaid(new BigDecimal("1683.00"))
                        .balanceToPay(new BigDecimal("360.00"))
                        .build());
            }

            @Test
            void appearancesNotFoundApproved() throws Exception {
                ResponseEntity<CombinedSimplifiedExpenseDetailDto> response = triggerValid(ExpenseType.APPROVED,
                    new JurorNumberAndPoolNumberDto(JUROR_NUMBER_NO_APPEARANCES, POOL_NUMBER));

                CombinedSimplifiedExpenseDetailDto body = response.getBody();
                assertThat(body.getExpenseDetails()).hasSize(0);

                CombinedSimplifiedExpenseDetailDto.Total total = body.getTotal();
                assertThat(total).isEqualTo(
                    CombinedSimplifiedExpenseDetailDto.Total.builder()
                        .totalAttendances(0)
                        .financialLoss(BigDecimal.ZERO)
                        .travel(BigDecimal.ZERO)
                        .foodAndDrink(BigDecimal.ZERO)
                        .smartcard(BigDecimal.ZERO)
                        .totalDue(BigDecimal.ZERO)
                        .totalPaid(BigDecimal.ZERO)
                        .balanceToPay(BigDecimal.ZERO)
                        .build());
            }

            @Test
            void appearancesNotFoundForApproval() throws Exception {
                ResponseEntity<CombinedSimplifiedExpenseDetailDto> response = triggerValid(ExpenseType.FOR_APPROVAL,
                    new JurorNumberAndPoolNumberDto(JUROR_NUMBER_NO_APPEARANCES, POOL_NUMBER));

                CombinedSimplifiedExpenseDetailDto body = response.getBody();
                assertThat(body.getExpenseDetails()).hasSize(0);

                CombinedSimplifiedExpenseDetailDto.Total total = body.getTotal();
                assertThat(total).isEqualTo(
                    CombinedSimplifiedExpenseDetailDto.Total.builder()
                        .totalAttendances(0)
                        .financialLoss(BigDecimal.ZERO)
                        .travel(BigDecimal.ZERO)
                        .foodAndDrink(BigDecimal.ZERO)
                        .smartcard(BigDecimal.ZERO)
                        .totalDue(BigDecimal.ZERO)
                        .totalPaid(BigDecimal.ZERO)
                        .balanceToPay(BigDecimal.ZERO)
                        .build());
            }

            @Test
            void appearancesNotFoundForReapproval() throws Exception {
                ResponseEntity<CombinedSimplifiedExpenseDetailDto> response = triggerValid(ExpenseType.FOR_REAPPROVAL,
                    new JurorNumberAndPoolNumberDto(JUROR_NUMBER_NO_APPEARANCES, POOL_NUMBER));

                CombinedSimplifiedExpenseDetailDto body = response.getBody();
                assertThat(body.getExpenseDetails()).hasSize(0);

                CombinedSimplifiedExpenseDetailDto.Total total = body.getTotal();
                assertThat(total).isEqualTo(
                    CombinedSimplifiedExpenseDetailDto.Total.builder()
                        .totalAttendances(0)
                        .financialLoss(BigDecimal.ZERO)
                        .travel(BigDecimal.ZERO)
                        .foodAndDrink(BigDecimal.ZERO)
                        .smartcard(BigDecimal.ZERO)
                        .totalDue(BigDecimal.ZERO)
                        .totalPaid(BigDecimal.ZERO)
                        .balanceToPay(BigDecimal.ZERO)
                        .build());
            }

            @Test
            void appearancesFoundButNotApplicable() throws Exception {
                ResponseEntity<CombinedSimplifiedExpenseDetailDto> response = triggerValid(ExpenseType.FOR_APPROVAL,
                    new JurorNumberAndPoolNumberDto("641500021", POOL_NUMBER));

                CombinedSimplifiedExpenseDetailDto body = response.getBody();
                assertThat(body.getExpenseDetails()).hasSize(0);

                CombinedSimplifiedExpenseDetailDto.Total total = body.getTotal();
                assertThat(total).isEqualTo(
                    CombinedSimplifiedExpenseDetailDto.Total.builder()
                        .totalAttendances(0)
                        .financialLoss(BigDecimal.ZERO)
                        .travel(BigDecimal.ZERO)
                        .foodAndDrink(BigDecimal.ZERO)
                        .smartcard(BigDecimal.ZERO)
                        .totalDue(BigDecimal.ZERO)
                        .totalPaid(BigDecimal.ZERO)
                        .balanceToPay(BigDecimal.ZERO)
                        .build());
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            protected ResponseEntity<String> triggerInvalid(String type,
                                                            JurorNumberAndPoolNumberDto request,
                                                            String owner) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(request, httpHeaders, POST,
                        toUri(type)),
                    String.class);
            }

            @Test
            void forbiddenIsBureauUser() throws Exception {
                final String type = ExpenseType.FOR_APPROVAL.name();
                assertForbiddenResponse(triggerInvalid(type,
                        new JurorNumberAndPoolNumberDto("641500021", "415230101"), "400"),
                    toUrl(type));
            }

            @Test
            void forbiddenOwnerDoNotMatch() throws Exception {
                final String type = ExpenseType.FOR_APPROVAL.name();
                assertMojForbiddenResponse(triggerInvalid(type,
                        new JurorNumberAndPoolNumberDto(JUROR_NUMBER, POOL_NUMBER),
                        "414"), toUrl(type),
                    "User cannot access this juror pool");
            }
        }
    }


    @Nested
    @DisplayName("GET " + GetDraftExpenses.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_draftExpenseSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class GetDraftExpenses {
        public static final String URL = BASE_URL + "/draft/{juror_number}/{pool_number}";

        public static final String JUROR_NUMBER = "641500020";
        public static final String JUROR_NUMBER_NO_APPEARANCES = "641500024";
        public static final String POOL_NUMBER = "415230101";

        public String toUrl(String jurorNumber, String poolNumber) {
            return URL.replace("{juror_number}", jurorNumber)
                .replace("{pool_number}", poolNumber);
        }

        public URI toUri(String jurorNumber, String poolNumber) {
            return URI.create(toUrl(jurorNumber, poolNumber));
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            protected ResponseEntity<CombinedExpenseDetailsDto<ExpenseDetailsDto>> triggerValid(
                String jurorNumber, String poolNumber) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<CombinedExpenseDetailsDto<ExpenseDetailsDto>> response = template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        toUri(jurorNumber, poolNumber)),
                    new ParameterizedTypeReference<>() {
                    });


                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                assertThat(response.getBody())
                    .as("Expect no body")
                    .isNotNull();
                return response;
            }

            @Test
            void typical() throws Exception {
                ResponseEntity<CombinedExpenseDetailsDto<ExpenseDetailsDto>> response =
                    triggerValid(JUROR_NUMBER, POOL_NUMBER);
                CombinedExpenseDetailsDto<ExpenseDetailsDto> body = response.getBody();
                assertThat(body.getExpenseDetails()).hasSize(3);
                assertThat(body.getExpenseDetails().get(0)).isEqualTo(
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
                        .build()
                );
                assertThat(body.getExpenseDetails().get(1)).isEqualTo(
                    ExpenseDetailsDto.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 6))
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
                        .build()
                );
                assertThat(body.getExpenseDetails().get(2)).isEqualTo(
                    ExpenseDetailsDto.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 7))
                        .attendanceType(AttendanceType.HALF_DAY)
                        .lossOfEarnings(new BigDecimal("92.00"))
                        .extraCare(new BigDecimal("72.00"))
                        .other(new BigDecimal("82.00"))
                        .publicTransport(new BigDecimal("12.00"))
                        .taxi(new BigDecimal("22.00"))
                        .motorcycle(new BigDecimal("32.00"))
                        .car(new BigDecimal("42.00"))
                        .bicycle(new BigDecimal("52.00"))
                        .parking(new BigDecimal("62.00"))
                        .foodAndDrink(new BigDecimal("102.00"))
                        .smartCard(new BigDecimal("27.00"))
                        .paymentMethod(PaymentMethod.BACS)
                        .build()
                );
                CombinedExpenseDetailsDto.Total total = body.getTotal();
                assertThat(total).isEqualTo(
                    CombinedExpenseDetailsDto.Total.builder()
                        .totalDays(3)
                        .lossOfEarnings(new BigDecimal("273.00"))
                        .extraCare(new BigDecimal("213.00"))
                        .other(new BigDecimal("243.00"))
                        .publicTransport(new BigDecimal("33.00"))
                        .taxi(new BigDecimal("63.00"))
                        .motorcycle(new BigDecimal("93.00"))
                        .car(new BigDecimal("123.00"))
                        .bicycle(new BigDecimal("153.00"))
                        .parking(new BigDecimal("183.00"))
                        .foodAndDrink(new BigDecimal("303.00"))
                        .smartCard(new BigDecimal("78.00"))
                        .build()
                );
            }

            @Test
            void appearancesNotFound() throws Exception {
                ResponseEntity<CombinedExpenseDetailsDto<ExpenseDetailsDto>> response =
                    triggerValid(JUROR_NUMBER_NO_APPEARANCES, POOL_NUMBER);
                CombinedExpenseDetailsDto<ExpenseDetailsDto> body = response.getBody();

                assertThat(body.getExpenseDetails()).hasSize(0);

                CombinedExpenseDetailsDto.Total total = body.getTotal();
                assertThat(total).isEqualTo(
                    CombinedExpenseDetailsDto.Total.builder()
                        .totalDays(0)
                        .lossOfEarnings(BigDecimal.ZERO)
                        .extraCare(BigDecimal.ZERO)
                        .other(BigDecimal.ZERO)
                        .publicTransport(BigDecimal.ZERO)
                        .taxi(BigDecimal.ZERO)
                        .motorcycle(BigDecimal.ZERO)
                        .car(BigDecimal.ZERO)
                        .bicycle(BigDecimal.ZERO)
                        .parking(BigDecimal.ZERO)
                        .foodAndDrink(BigDecimal.ZERO)
                        .smartCard(BigDecimal.ZERO)
                        .build()
                );
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            protected ResponseEntity<String> triggerInvalid(String jurorNumber,
                                                            String poolNumber, String owner) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        toUri(jurorNumber, poolNumber)),
                    String.class);
            }

            @Test
            void canNotAccessJurorPool() throws Exception {
                assertMojForbiddenResponse(triggerInvalid(JUROR_NUMBER, POOL_NUMBER, "414"),
                    toUrl(JUROR_NUMBER, POOL_NUMBER),
                    "User cannot access this juror pool");
            }

            @Test
            void isBureauUser() throws Exception {
                assertForbiddenResponse(triggerInvalid(JUROR_NUMBER, POOL_NUMBER, "400"),
                    toUrl(JUROR_NUMBER, POOL_NUMBER));
            }
        }
    }

    @Nested
    @DisplayName("GET (POST) " + GetExpenses.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_draftExpenseSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class GetExpenses {
        public static final String URL = BASE_URL + "/{juror_number}/{pool_number}";

        public static final String JUROR_NUMBER = "641500020";
        public static final String POOL_NUMBER = "415230101";

        public String toUrl(String jurorNumber, String poolNumber) {
            return URL.replace("{juror_number}", jurorNumber)
                .replace("{pool_number}", poolNumber);
        }

        public URI toUri(String jurorNumber, String poolNumber) {
            return URI.create(toUrl(jurorNumber, poolNumber));
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            protected ResponseEntity<CombinedExpenseDetailsDto<ExpenseDetailsDto>> triggerValid(
                String jurorNumber, String poolNumber, List<LocalDate> payload) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<CombinedExpenseDetailsDto<ExpenseDetailsDto>> response = template.exchange(
                    new RequestEntity<>(payload, httpHeaders, POST,
                        toUri(jurorNumber, poolNumber)),
                    new ParameterizedTypeReference<>() {
                    });

                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                assertThat(response.getBody())
                    .as("Expect no body")
                    .isNotNull();
                return response;
            }

            @Test
            void typical() throws Exception {
                ResponseEntity<CombinedExpenseDetailsDto<ExpenseDetailsDto>> response =
                    triggerValid(JUROR_NUMBER, POOL_NUMBER,
                        List.of(
                            LocalDate.of(2023, 1, 5),
                            LocalDate.of(2023, 1, 8),
                            LocalDate.of(2023, 1, 14)
                        ));
                CombinedExpenseDetailsDto<ExpenseDetailsDto> body = response.getBody();
                assertThat(body.getExpenseDetails()).hasSize(3);
                assertThat(body.getExpenseDetails().get(0)).isEqualTo(
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
                        .build()
                );
                assertThat(body.getExpenseDetails().get(1)).isEqualTo(
                    ExpenseDetailsDto.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 8))
                        .attendanceType(AttendanceType.FULL_DAY)
                        .lossOfEarnings(new BigDecimal("93.00"))
                        .extraCare(new BigDecimal("73.00"))
                        .other(new BigDecimal("83.00"))
                        .publicTransport(new BigDecimal("13.97"))
                        .taxi(new BigDecimal("23.00"))
                        .motorcycle(new BigDecimal("33.00"))
                        .car(new BigDecimal("43.00"))
                        .bicycle(new BigDecimal("53.00"))
                        .parking(new BigDecimal("63.00"))
                        .foodAndDrink(new BigDecimal("103.00"))
                        .smartCard(new BigDecimal("28.00"))
                        .paymentMethod(PaymentMethod.BACS)
                        .build()
                );
                assertThat(body.getExpenseDetails().get(2)).isEqualTo(
                    ExpenseDetailsDto.builder()
                        .attendanceDate(LocalDate.of(2023, 1, 14))
                        .attendanceType(AttendanceType.FULL_DAY)
                        .lossOfEarnings(new BigDecimal("23.00"))
                        .extraCare(new BigDecimal("43.00"))
                        .other(new BigDecimal("33.00"))
                        .publicTransport(new BigDecimal("103.00"))
                        .taxi(new BigDecimal("93.00"))
                        .motorcycle(new BigDecimal("83.00"))
                        .car(new BigDecimal("73.00"))
                        .bicycle(new BigDecimal("63.00"))
                        .parking(new BigDecimal("53.00"))
                        .foodAndDrink(new BigDecimal("13.00"))
                        .smartCard(new BigDecimal("28.00"))
                        .paymentMethod(PaymentMethod.BACS)
                        .build()
                );
                CombinedExpenseDetailsDto.Total total = body.getTotal();
                assertThat(total).isEqualTo(
                    CombinedExpenseDetailsDto.Total.builder()
                        .totalDays(3)
                        .lossOfEarnings(new BigDecimal("206.00"))
                        .extraCare(new BigDecimal("186.00"))
                        .other(new BigDecimal("196.00"))
                        .publicTransport(new BigDecimal("126.97"))
                        .taxi(new BigDecimal("136.00"))
                        .motorcycle(new BigDecimal("146.00"))
                        .car(new BigDecimal("156.00"))
                        .bicycle(new BigDecimal("166.00"))
                        .parking(new BigDecimal("176.00"))
                        .foodAndDrink(new BigDecimal("216.00"))
                        .smartCard(new BigDecimal("81.00"))
                        .build()
                );
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            protected ResponseEntity<String> triggerInvalid(String jurorNumber,
                                                            String poolNumber, String owner,
                                                            List<LocalDate> payload) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(payload, httpHeaders, POST,
                        toUri(jurorNumber, poolNumber)),
                    String.class);
            }

            @Test
            void canNotAccessJurorPool() throws Exception {
                assertMojForbiddenResponse(triggerInvalid(JUROR_NUMBER, POOL_NUMBER, "414", List.of(
                        LocalDate.of(2023, 1, 5)
                    )),
                    toUrl(JUROR_NUMBER, POOL_NUMBER),
                    "User cannot access this juror pool");
            }

            @Test
            void isBureauUser() throws Exception {
                assertForbiddenResponse(triggerInvalid(JUROR_NUMBER, POOL_NUMBER, "400", List.of(
                        LocalDate.of(2023, 1, 5)
                    )),
                    toUrl(JUROR_NUMBER, POOL_NUMBER));
            }


            @Test
            void oneOrMoreDatesNotFound() throws Exception {
                assertNotFound(triggerInvalid(JUROR_NUMBER, POOL_NUMBER, "415", List.of(
                        LocalDate.of(2023, 1, 5),
                        LocalDate.of(2020, 1, 8))),
                    toUrl(JUROR_NUMBER, POOL_NUMBER),
                    "Not all dates found");
            }
        }
    }


    @Nested
    @DisplayName("POST " + ApproveExpenses.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_approveExpenseSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class ApproveExpenses {
        public static final String URL = BASE_URL + "/approve";

        private static final String JUROR_NUMBER = "641500020";
        private static final String POOL_NUMBER = "415230101";

        protected ApproveExpenses() {

        }

        @Nested
        class Positive {
            protected ResponseEntity<String> triggerValid(ApproveExpenseDto... expenseDto) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415", UserType.COURT, Set.of(Role.MANAGER), "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<String> response = template.exchange(
                    new RequestEntity<>(List.of(expenseDto), httpHeaders, POST,
                        URI.create(URL)),
                    String.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                assertThat(response.getBody())
                    .as("Expect no body")
                    .isEqualTo(null);
                return response;
            }


            private long assertFinancialAuditDetailsApproved(FinancialAuditDetails financialAuditDetail,
                                                             LocalDateTime createdOn, boolean isCash) {
                assertThat(financialAuditDetail.getJurorRevision()).isEqualTo(1L);
                assertThat(financialAuditDetail.getJurorNumber()).isEqualTo(JUROR_NUMBER);
                assertThat(financialAuditDetail.getCourtLocationRevision()).isEqualTo(6L);
                assertThat(financialAuditDetail.getLocCode()).isEqualTo("415");
                assertThat(financialAuditDetail.getType()).isEqualTo(isCash
                    ? FinancialAuditDetails.Type.APPROVED_CASH
                    : FinancialAuditDetails.Type.APPROVED_BACS);
                assertThat(financialAuditDetail.getCreatedBy().getUsername()).isEqualTo("COURT_USER");
                assertThat(financialAuditDetail.getCreatedOn()).isEqualToIgnoringHours(createdOn);
                return financialAuditDetail.getId();
            }

            @Test
            void typicalApprovalBacs() throws Exception {
                ApproveExpenseDto approveExpenseDto = ApproveExpenseDto.builder()
                    .jurorNumber(JUROR_NUMBER)
                    .poolNumber("415230101")
                    .approvalType(ApproveExpenseDto.ApprovalType.FOR_APPROVAL)
                    .cashPayment(false)
                    .dateToRevisions(
                        List.of(
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 8))
                                .version(1L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 9))
                                .version(2L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 10))
                                .version(1L)
                                .build()
                        )
                    )
                    .build();
                triggerValid(approveExpenseDto);

                List<FinancialAuditDetails> financialAuditDetails = new ArrayList<>();
                financialAuditDetailsRepository.findAll().forEach(financialAuditDetails::add);
                assertThat(financialAuditDetails).hasSize(1);

                List<FinancialAuditDetailsAppearances> financialAuditDetailsAppearances = new ArrayList<>();
                financialAuditDetailsAppearancesRepository.findAll().forEach(financialAuditDetailsAppearances::add);
                financialAuditDetailsAppearances.sort(
                    Comparator.comparing(FinancialAuditDetailsAppearances::getAttendanceDate));
                assertThat(financialAuditDetailsAppearances).hasSize(3);
                long id = assertFinancialAuditDetailsApproved(financialAuditDetails.get(0),
                    LocalDateTime.now(), false);
                assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearances.get(0),
                    id, LocalDate.of(2023, 1, 8), 2);
                assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearances.get(1),
                    id, LocalDate.of(2023, 1, 9), 3);
                assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearances.get(2),
                    id, LocalDate.of(2023, 1, 10), 2);

                assertApproved(
                    appearanceRepository.findByJurorNumberAndAttendanceDate(JUROR_NUMBER, LocalDate.of(2023, 1, 8)));
                assertApproved(
                    appearanceRepository.findByJurorNumberAndAttendanceDate(JUROR_NUMBER, LocalDate.of(2023, 1, 9)));
                assertApproved(
                    appearanceRepository.findByJurorNumberAndAttendanceDate(JUROR_NUMBER, LocalDate.of(2023, 1, 10)));
                assertJurorHistory(JUROR_NUMBER, HistoryCodeMod.ARAMIS_EXPENSES_FILE_CREATED, "COURT_USER", "£1,683.98",
                    approveExpenseDto.getPoolNumber(), LocalDate.of(2023, 1, 10), "F" + id);
                assertPaymentData(JUROR_NUMBER, new BigDecimal("1683.98"), new BigDecimal("702.98"),
                    new BigDecimal("225.00"), new BigDecimal("756.00"));
            }

            @Test
            void typicalApprovalCash() throws Exception {
                ApproveExpenseDto approveExpenseDto = ApproveExpenseDto.builder()
                    .jurorNumber(JUROR_NUMBER)
                    .poolNumber("415230101")
                    .approvalType(ApproveExpenseDto.ApprovalType.FOR_APPROVAL)
                    .cashPayment(true)
                    .dateToRevisions(
                        List.of(
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 2, 8))
                                .version(1L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 2, 9))
                                .version(2L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 2, 10))
                                .version(1L)
                                .build()
                        )
                    )
                    .build();
                triggerValid(approveExpenseDto);

                List<FinancialAuditDetails> financialAuditDetails = new ArrayList<>();
                financialAuditDetailsRepository.findAll().forEach(financialAuditDetails::add);
                assertThat(financialAuditDetails).hasSize(1);

                List<FinancialAuditDetailsAppearances> financialAuditDetailsAppearances = new ArrayList<>();
                financialAuditDetailsAppearancesRepository.findAll().forEach(financialAuditDetailsAppearances::add);
                financialAuditDetailsAppearances.sort(
                    Comparator.comparing(FinancialAuditDetailsAppearances::getAttendanceDate));
                assertThat(financialAuditDetailsAppearances).hasSize(3);
                long id = assertFinancialAuditDetailsApproved(financialAuditDetails.get(0),
                    LocalDateTime.now(), true);
                assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearances.get(0),
                    id, LocalDate.of(2023, 2, 8), 2);
                assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearances.get(1),
                    id, LocalDate.of(2023, 2, 9), 3);
                assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearances.get(2),
                    id, LocalDate.of(2023, 2, 10), 2);

                assertApproved(
                    appearanceRepository.findByJurorNumberAndAttendanceDate(JUROR_NUMBER, LocalDate.of(2023, 2, 8)));
                assertApproved(
                    appearanceRepository.findByJurorNumberAndAttendanceDate(JUROR_NUMBER, LocalDate.of(2023, 2, 9)));
                assertApproved(
                    appearanceRepository.findByJurorNumberAndAttendanceDate(JUROR_NUMBER, LocalDate.of(2023, 2, 10)));
                assertJurorHistory(JUROR_NUMBER, HistoryCodeMod.CASH_PAYMENT_APPROVAL, "COURT_USER", "£1,683.98",
                    approveExpenseDto.getPoolNumber(), LocalDate.of(2023, 2, 10), "F" + id);
                List<PaymentData> paymentDataList = paymentDataRepository.findByJurorNumber(JUROR_NUMBER);
                assertThat(paymentDataList).hasSize(0);
            }

            @Test
            void typicalReApproved() throws Exception {
                ApproveExpenseDto approveExpenseDto = ApproveExpenseDto.builder()
                    .jurorNumber(JUROR_NUMBER)
                    .poolNumber("415230101")
                    .approvalType(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL)
                    .cashPayment(false)
                    .dateToRevisions(
                        List.of(
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 14))
                                .version(1L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 15))
                                .version(1L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 16))
                                .version(1L)
                                .build()
                        )
                    )
                    .build();
                triggerValid(approveExpenseDto);

                List<FinancialAuditDetails> financialAuditDetails = new ArrayList<>();
                financialAuditDetailsRepository.findAll().forEach(financialAuditDetails::add);
                assertThat(financialAuditDetails).hasSize(1);


                List<FinancialAuditDetailsAppearances> financialAuditDetailsAppearances = new ArrayList<>();
                financialAuditDetailsAppearancesRepository.findAll().forEach(financialAuditDetailsAppearances::add);
                financialAuditDetailsAppearances.sort(
                    Comparator.comparing(FinancialAuditDetailsAppearances::getAttendanceDate));
                assertThat(financialAuditDetailsAppearances).hasSize(3);
                long id = assertFinancialAuditDetailsApproved(financialAuditDetails.get(0),
                    LocalDateTime.now(), false);
                assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearances.get(0),
                    id, LocalDate.of(2023, 1, 14), 2);
                assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearances.get(1),
                    id, LocalDate.of(2023, 1, 15), 2);
                assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearances.get(2),
                    id, LocalDate.of(2023, 1, 16), 2);

                assertApproved(
                    appearanceRepository.findByJurorNumberAndAttendanceDate(JUROR_NUMBER, LocalDate.of(2023, 1, 14)));
                assertApproved(
                    appearanceRepository.findByJurorNumberAndAttendanceDate(JUROR_NUMBER, LocalDate.of(2023, 1, 15)));
                assertApproved(
                    appearanceRepository.findByJurorNumberAndAttendanceDate(JUROR_NUMBER, LocalDate.of(2023, 1, 16)));

                assertJurorHistory(JUROR_NUMBER, HistoryCodeMod.ARAMIS_EXPENSES_FILE_CREATED, "COURT_USER", "£407.00",
                    approveExpenseDto.getPoolNumber(), LocalDate.of(2023, 1, 16), "F" + id);
                assertPaymentData(JUROR_NUMBER, new BigDecimal("407.00"), new BigDecimal("260.00"),
                    new BigDecimal("57.00"), new BigDecimal("90.00"));
            }

            private void assertApproved(Appearance appearance) {
                assertThat(appearance).isNotNull();
                assertThat(appearance.getAppearanceStage())
                    .isEqualTo(AppearanceStage.EXPENSE_AUTHORISED);
            }

            private void assertPaymentData(String jurorNumber, BigDecimal totalExpense,
                                           BigDecimal travel, BigDecimal subsistence,
                                           BigDecimal financialLoss) {
                List<PaymentData> paymentDataList = paymentDataRepository.findByJurorNumber(jurorNumber);
                assertThat(paymentDataList).hasSize(1);
                PaymentData paymentData = paymentDataList.get(0);
                assertThat(paymentData.getJurorNumber()).isEqualTo(jurorNumber);
                assertThat(paymentData.getExpenseTotal()).isEqualTo(totalExpense);
                assertThat(paymentData.getFinancialLossTotal()).isEqualTo(financialLoss);
                assertThat(paymentData.getTravelTotal()).isEqualTo(travel);
                assertThat(paymentData.getSubsistenceTotal()).isEqualTo(subsistence);


            }

        }

        @Nested
        class Negative {
            protected ResponseEntity<String> triggerInvalid(String owner,
                                                            ApproveExpenseDto... expenseDto) throws Exception {
                return triggerInvalid(owner, COURT_USER, UserType.COURT, Set.of(Role.MANAGER), expenseDto);
            }

            protected ResponseEntity<String> triggerInvalid(String owner, String username,
                                                            UserType userType, Set<Role> roles,
                                                            ApproveExpenseDto... expenseDto) throws Exception {
                final String jwt = createBureauJwt(username, owner, userType, roles, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(List.of(expenseDto), httpHeaders, POST,
                        URI.create(URL)),
                    String.class);
            }

            @Test
            void negativeAppearancesNotFound() throws Exception {
                final String jurorNumber = "641500021";
                assertNotFound(triggerInvalid("415", ApproveExpenseDto.builder()
                        .jurorNumber(jurorNumber)
                        .poolNumber("415230101")
                        .cashPayment(true)
                        .approvalType(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL)
                        .dateToRevisions(
                            List.of(
                                ApproveExpenseDto.DateToRevision.builder()
                                    .attendanceDate(LocalDate.of(2023, 1, 14))
                                    .version(1L)
                                    .build()
                            )
                        ).build()), URL,
                    "No appearance records found for Juror Number: 641500021, Pool Number: 415230101 and approval "
                        + "type FOR_REAPPROVAL");
            }

            @Test
            void negativeOutOfDataData() throws Exception {
                assertBusinessRuleViolation(triggerInvalid("415", ApproveExpenseDto.builder()
                        .jurorNumber(JUROR_NUMBER)
                        .poolNumber("415230101")
                        .approvalType(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL)
                        .cashPayment(false)
                        .dateToRevisions(
                            List.of(
                                ApproveExpenseDto.DateToRevision.builder()
                                    .attendanceDate(LocalDate.of(2023, 1, 14))
                                    .version(2L)
                                    .build(),
                                ApproveExpenseDto.DateToRevision.builder()
                                    .attendanceDate(LocalDate.of(2023, 1, 15))
                                    .version(1L)
                                    .build(),
                                ApproveExpenseDto.DateToRevision.builder()
                                    .attendanceDate(LocalDate.of(2023, 1, 16))
                                    .version(1L)
                                    .build()
                            )
                        )
                        .build()), "Revisions do not align",
                    MojException.BusinessRuleViolation.ErrorCode.DATA_OUT_OF_DATE);
            }

            @Test
            void negativeMissingVersion() throws Exception {
                assertBusinessRuleViolation(triggerInvalid("415", ApproveExpenseDto.builder()
                        .jurorNumber(JUROR_NUMBER)
                        .poolNumber("415230101")
                        .approvalType(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL)
                        .cashPayment(false)
                        .dateToRevisions(
                            List.of(
                                ApproveExpenseDto.DateToRevision.builder()
                                    .attendanceDate(LocalDate.of(2023, 1, 14))
                                    .version(1L)
                                    .build(),
                                ApproveExpenseDto.DateToRevision.builder()
                                    .attendanceDate(LocalDate.of(2023, 1, 15))
                                    .version(1L)
                                    .build()
                            )
                        )
                        .build()), "Revisions do not align",
                    MojException.BusinessRuleViolation.ErrorCode.DATA_OUT_OF_DATE);
            }

            @Test
            @Sql(value = {"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_approveExpenseSetUp.sql"},
                statements = {
                    "INSERT INTO juror_mod.financial_audit_details (id, juror_revision, court_location_revision, "
                        + "type, created_by, created_on) VALUES (3, 1, 6, 'FOR_APPROVAL', 'COURT_USER',"
                        + " '2023-01-01 00:00:00')",
                    "INSERT INTO juror_mod.financial_audit_details_appearances (financial_audit_id, juror_number, "
                        + "attendance_date,loc_code,appearance_version ) VALUES "
                        + "(3, '641500020', '2023-01-14', '415', 1)"
                })
            void negativeUserCanNotApprove() throws Exception {
                assertBusinessRuleViolation(triggerInvalid("415", ApproveExpenseDto.builder()
                        .jurorNumber(JUROR_NUMBER)
                        .poolNumber("415230101")
                        .approvalType(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL)
                        .cashPayment(false)
                        .dateToRevisions(
                            List.of(
                                ApproveExpenseDto.DateToRevision.builder()
                                    .attendanceDate(LocalDate.of(2023, 1, 14))
                                    .version(1L)
                                    .build(),
                                ApproveExpenseDto.DateToRevision.builder()
                                    .attendanceDate(LocalDate.of(2023, 1, 15))
                                    .version(1L)
                                    .build(),
                                ApproveExpenseDto.DateToRevision.builder()
                                    .attendanceDate(LocalDate.of(2023, 1, 16))
                                    .version(1L)
                                    .build()
                            )
                        ).build()), "User cannot approve an expense they have edited",
                    MojException.BusinessRuleViolation.ErrorCode.CAN_NOT_APPROVE_OWN_EDIT);
            }

            @Test
            void negativeCanNotApproveMoreThan() throws Exception {
                assertBusinessRuleViolation(triggerInvalid("415", "COURT_USER2",
                        UserType.COURT, Set.of(Role.MANAGER),
                        ApproveExpenseDto.builder()
                            .jurorNumber(JUROR_NUMBER)
                            .poolNumber("415230101")
                            .approvalType(ApproveExpenseDto.ApprovalType.FOR_APPROVAL)
                            .cashPayment(false)
                            .dateToRevisions(
                                List.of(
                                    ApproveExpenseDto.DateToRevision.builder()
                                        .attendanceDate(LocalDate.of(2023, 1, 8))
                                        .version(1L)
                                        .build(),
                                    ApproveExpenseDto.DateToRevision.builder()
                                        .attendanceDate(LocalDate.of(2023, 1, 9))
                                        .version(2L)
                                        .build(),
                                    ApproveExpenseDto.DateToRevision.builder()
                                        .attendanceDate(LocalDate.of(2023, 1, 10))
                                        .version(1L)
                                        .build()
                                )
                            )
                            .build()), "User cannot approve expenses over £100.00",
                    MojException.BusinessRuleViolation.ErrorCode.CAN_NOT_APPROVE_MORE_THAN_LIMIT);
            }

            @Test
            void negativeUnauthorizedIsBureau() throws Exception {
                assertForbiddenResponse(
                    triggerInvalid("400", ApproveExpenseDto.builder()
                        .jurorNumber(JUROR_NUMBER)
                        .poolNumber("415230101")
                        .approvalType(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL)
                        .cashPayment(true)
                        .dateToRevisions(
                            List.of(
                                ApproveExpenseDto.DateToRevision.builder()
                                    .attendanceDate(LocalDate.of(2023, 1, 14))
                                    .version(1L)
                                    .build(),
                                ApproveExpenseDto.DateToRevision.builder()
                                    .attendanceDate(LocalDate.of(2023, 1, 15))
                                    .version(1L)
                                    .build(),
                                ApproveExpenseDto.DateToRevision.builder()
                                    .attendanceDate(LocalDate.of(2023, 1, 16))
                                    .version(1L)
                                    .build()
                            )
                        )
                        .build()), URL);
            }

            @Test
            void negativeUnauthorizedNotManager() throws Exception {
                assertForbiddenResponse(
                    triggerInvalid("415",
                        COURT_USER, UserType.COURT, Set.of(),
                        ApproveExpenseDto.builder()
                            .jurorNumber(JUROR_NUMBER)
                            .poolNumber("415230101")
                            .approvalType(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL)
                            .cashPayment(true)
                            .dateToRevisions(
                                List.of(
                                    ApproveExpenseDto.DateToRevision.builder()
                                        .attendanceDate(LocalDate.of(2023, 1, 14))
                                        .version(1L)
                                        .build(),
                                    ApproveExpenseDto.DateToRevision.builder()
                                        .attendanceDate(LocalDate.of(2023, 1, 15))
                                        .version(1L)
                                        .build(),
                                    ApproveExpenseDto.DateToRevision.builder()
                                        .attendanceDate(LocalDate.of(2023, 1, 16))
                                        .version(1L)
                                        .build()
                                )
                            )
                            .build()), URL);
            }
        }
    }

    @Nested
    @DisplayName("POST " + PostEditAttendedDayDailyExpense.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_editExpenseSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class PostEditAttendedDayDailyExpense extends AbstractDraftDailyExpense {

        public static final String URL = BASE_URL + "/{juror_number}/edit/{type}";

        PostEditAttendedDayDailyExpense() {
            super(URL, "postEditDailyExpense");
        }


        protected String toUrl(String jurorNumber, ExpenseType type) {
            return toUrl(jurorNumber, type.name());
        }

        protected String toUrl(String jurorNumber, String type) {
            return URL.replace("{juror_number}", jurorNumber)
                .replace("{type}", type);
        }

        @DisplayName("Negative")
        @Nested
        class Negative {

            protected ResponseEntity<String> triggerInvalid(String jurorNumber,
                                                            String expenseType,
                                                            DailyExpense... request) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(request, httpHeaders, POST,
                        URI.create(toUrl(jurorNumber, expenseType))),
                    String.class);
            }

            @Test
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

                ResponseEntity<String> response = triggerInvalid(jurorNumber, ExpenseType.APPROVED.name(), request);
                assertInvalidPathParam(response,
                    methodName + ".jurorNumber: must match \"^\\d{9}$\"");
            }

            @Test
            void invalidExpenseType() throws Exception {
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

                ResponseEntity<String> response = triggerInvalid(TestConstants.VALID_JUROR_NUMBER,
                    "INVALID", request);
                assertInvalidPathParam(response,
                    "INVALID is the incorrect data type or is not in the expected format (type)");
            }

            @Test
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

                ResponseEntity<String> response = triggerInvalid(jurorNumber, ExpenseType.FOR_REAPPROVAL.name(),
                    request);
                assertNotFound(response, toUrl(jurorNumber, ExpenseType.FOR_REAPPROVAL),
                    "No appearance record found for juror: 123456789 on day: 2023-01-05");
            }

            @Test
            void wrongExpenseType() throws Exception {
                final String jurorNumber = "641500020";
                final String poolNumber = "415230101";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 8))
                    .poolNumber(poolNumber)
                    .payCash(false)
                    .time(DailyExpenseTime.builder()
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc")
                    )
                    .build();

                ResponseEntity<String> response = triggerInvalid(jurorNumber, ExpenseType.APPROVED.name(),
                    request);
                assertBusinessRuleViolation(response,
                    "Expense for this day is not of type: APPROVED",
                    MojException.BusinessRuleViolation.ErrorCode.WRONG_EXPENSE_TYPE);
            }

            @Test
            void lessThenAmountAlreadyPaid() throws Exception {
                final String jurorNumber = "641500020";
                final String poolNumber = "415230101";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 11))
                    .poolNumber(poolNumber)
                    .payCash(false)
                    .time(DailyExpenseTime.builder()
                        .travelTime(LocalTime.of(1, 2))
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(0.01, 10.00, 5.00, "Desc")
                    )
                    .build();
                assertBusinessRuleViolation(triggerInvalid(jurorNumber, ExpenseType.APPROVED.name(), request),
                    "Updated expense values cannot be less than the paid amount",
                    MojException.BusinessRuleViolation.ErrorCode.EXPENSE_VALUES_REDUCED_LESS_THAN_PAID);
            }
        }


        @Nested
        @DisplayName("Positive")
        class Positive {
            protected ResponseEntity<Void> triggerValid(String jurorNumber,
                                                        ExpenseType expenseType,
                                                        DailyExpense... request) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<Void> response = template.exchange(
                    new RequestEntity<>(request, httpHeaders, POST,
                        URI.create(toUrl(jurorNumber, expenseType))),
                    Void.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.ACCEPTED);
                return response;
            }

            @Test
            void typicalForApproved() throws Exception {
                final String jurorNumber = "641500020";
                final String poolNumber = "415230101";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 8))
                    .poolNumber(poolNumber)
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
                        createDailyExpenseFoodAndDrink(FoodDrinkClaimType.LESS_THAN_OR_EQUAL_TO_10_HOURS, 4.2)
                    )
                    .build();

                DailyExpense request2 = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 9))
                    .poolNumber(poolNumber)
                    .payCash(false)
                    .time(DailyExpenseTime.builder()
                        .travelTime(LocalTime.of(1, 2))
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(15.01, 30.00, 1.00, "Desc")
                    )
                    .travel(
                        createDailyExpenseTravel(TravelMethod.CAR, 1, 3, 2.25, 1.2, 3.2)
                    )
                    .foodAndDrink(
                        createDailyExpenseFoodAndDrink(FoodDrinkClaimType.MORE_THAN_10_HOURS, 4.2)
                    )
                    .build();
                triggerValid(jurorNumber, ExpenseType.FOR_APPROVAL, request, request2);

                Appearance appearance = getAppearance(jurorNumber, request.getDateOfExpense(), "415");
                assertThat(appearance).isNotNull();
                assertThat(appearance.getPayAttendanceType()).isEqualTo(PayAttendanceType.FULL_DAY);
                assertThat(appearance.isPayCash()).isEqualTo(false);
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

                // Subsistence
                assertThat(appearance.getFoodAndDrinkClaimType())
                    .isEqualTo(FoodDrinkClaimType.LESS_THAN_OR_EQUAL_TO_10_HOURS);
                assertThat(appearance.getSmartCardAmountDue()).isEqualTo(doubleToBigDecimal(4.2));
                assertThat(appearance.getSubsistenceDue()).isEqualTo(doubleToBigDecimal(5.71));


                assertThat(appearance.getTotalDue()).isEqualTo(doubleToBigDecimal(45.34));

                Appearance appearance2 = getAppearance(jurorNumber, request2.getDateOfExpense(), "415");

                assertThat(appearance2).isNotNull();
                assertThat(appearance2.getPayAttendanceType()).isEqualTo(PayAttendanceType.FULL_DAY);
                assertThat(appearance2.isPayCash()).isEqualTo(false);
                assertThat(appearance2.getTravelTime()).isEqualTo(LocalTime.of(1, 2));

                //Financial Loss
                assertThat(appearance2.getLossOfEarningsDue()).isEqualTo(doubleToBigDecimal(15.01));
                assertThat(appearance2.getChildcareDue()).isEqualTo(doubleToBigDecimal(30.00));
                assertThat(appearance2.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(1.00));
                assertThat(appearance2.getMiscDescription()).isEqualTo("Desc");
                //Travel
                assertThat(appearance2.getTraveledByCar()).isTrue();
                assertThat(appearance2.getJurorsTakenCar()).isEqualTo(1);
                assertThat(appearance2.getCarDue()).isEqualTo(doubleToBigDecimal(1.07));


                assertThat(appearance2.getMilesTraveled()).isEqualTo(3);
                assertThat(appearance2.getParkingDue()).isEqualTo(doubleToBigDecimal(2.25));
                assertThat(appearance2.getPublicTransportDue()).isEqualTo(doubleToBigDecimal(1.2));
                assertThat(appearance2.getHiredVehicleDue()).isEqualTo(doubleToBigDecimal(3.2));

                // Subsistence
                assertThat(appearance2.getFoodAndDrinkClaimType()).isEqualTo(FoodDrinkClaimType.MORE_THAN_10_HOURS);
                assertThat(appearance2.getSmartCardAmountDue()).isEqualTo(doubleToBigDecimal(4.2));
                assertThat(appearance2.getSubsistenceDue()).isEqualTo(doubleToBigDecimal(12.17));

                assertThat(appearance2.getTotalDue()).isEqualTo(doubleToBigDecimal(61.70));
                assertJurorHistory(jurorNumber, HistoryCodeMod.EDIT_PAYMENTS, "COURT_USER", "£45.34",
                    poolNumber, LocalDate.of(2023, 1, 8), "F2", 2, 0);
                assertJurorHistory(jurorNumber, HistoryCodeMod.EDIT_PAYMENTS, "COURT_USER", "£61.70",
                    poolNumber, LocalDate.of(2023, 1, 9), "F2", 2, 1);


                List<FinancialAuditDetailsAppearances> financialAuditDetailsAppearances = new ArrayList<>();
                financialAuditDetailsAppearancesRepository.findAll().forEach(financialAuditDetailsAppearances::add);
                financialAuditDetailsAppearances.sort(
                    Comparator.comparing(FinancialAuditDetailsAppearances::getAttendanceDate));
                assertThat(financialAuditDetailsAppearances).hasSize(2);
                assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearances.get(0),
                    2, LocalDate.of(2023, 1, 8), 3);

                assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearances.get(1),
                    2, LocalDate.of(2023, 1, 9), 3);
            }


            @Test
            void typicalApproved() throws Exception {
                final String jurorNumber = "641500020";
                final String poolNumber = "415230101";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 11))
                    .poolNumber(poolNumber)
                    .payCash(false)
                    .time(DailyExpenseTime.builder()
                        .travelTime(LocalTime.of(1, 2))
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(12.00, 7.00, 8.93, "Desc")
                    )
                    .travel(
                        createDailyExpenseTravel(TravelMethod.CAR, null, 5, 15.02, 11.00, 5.00)
                    )
                    .foodAndDrink(
                        createDailyExpenseFoodAndDrink(FoodDrinkClaimType.LESS_THAN_OR_EQUAL_TO_10_HOURS, 4.2)
                    )
                    .build();

                triggerValid(jurorNumber, ExpenseType.APPROVED, request);

                Appearance appearance = getAppearance(jurorNumber, request.getDateOfExpense(), "415");
                assertThat(appearance).isNotNull();
                assertThat(appearance.getPayAttendanceType()).isEqualTo(PayAttendanceType.FULL_DAY);
                assertThat(appearance.isPayCash()).isEqualTo(false);
                assertThat(appearance.getTravelTime()).isEqualTo(LocalTime.of(1, 2));

                //Financial Loss
                assertThat(appearance.getLossOfEarningsDue()).isEqualTo(doubleToBigDecimal(12.00));
                assertThat(appearance.getChildcareDue()).isEqualTo(doubleToBigDecimal(7.00));
                assertThat(appearance.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(8.93));
                assertThat(appearance.getMiscDescription()).isEqualTo("Desc");
                //Travel
                assertThat(appearance.getTraveledByCar()).isTrue();
                assertThat(appearance.getJurorsTakenCar()).isNull();
                assertThat(appearance.getCarDue()).isEqualTo(doubleToBigDecimal(1.57));


                assertThat(appearance.getMilesTraveled()).isEqualTo(5);
                assertThat(appearance.getParkingDue()).isEqualTo(doubleToBigDecimal(15.02));
                assertThat(appearance.getPublicTransportDue()).isEqualTo(doubleToBigDecimal(11.00));
                assertThat(appearance.getHiredVehicleDue()).isEqualTo(doubleToBigDecimal(5.00));

                // Subsistence
                assertThat(appearance.getFoodAndDrinkClaimType())
                    .isEqualTo(FoodDrinkClaimType.LESS_THAN_OR_EQUAL_TO_10_HOURS);
                assertThat(appearance.getSmartCardAmountDue()).isEqualTo(doubleToBigDecimal(4.2));
                assertThat(appearance.getSubsistenceDue()).isEqualTo(doubleToBigDecimal(5.71));


                assertThat(appearance.getTotalDue()).isEqualTo(doubleToBigDecimal(62.03));


                List<FinancialAuditDetailsAppearances> financialAuditDetailsAppearances = new ArrayList<>();
                financialAuditDetailsAppearancesRepository.findAll().forEach(financialAuditDetailsAppearances::add);
                assertThat(financialAuditDetailsAppearances).hasSize(1);
                assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearances.get(0),
                    2, LocalDate.of(2023, 1, 11), 2);
                assertJurorHistory(jurorNumber, HistoryCodeMod.EDIT_PAYMENTS, "COURT_USER", "£62.03",
                    poolNumber, LocalDate.of(2023, 1, 11), "F2", 1, 0);
            }

            @Test
            void typicalReApproved() throws Exception {
                final String jurorNumber = "641500020";
                final String poolNumber = "415230101";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 15))
                    .poolNumber(poolNumber)
                    .payCash(false)
                    .time(DailyExpenseTime.builder()
                        .travelTime(LocalTime.of(2, 2))
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(12.10, 7.10, 9.93, "Desc 2")
                    )
                    .travel(
                        createDailyExpenseTravel(TravelMethod.CAR, null, 6, 16.02, 12.00, 6.00)
                    )
                    .foodAndDrink(
                        createDailyExpenseFoodAndDrink(FoodDrinkClaimType.LESS_THAN_OR_EQUAL_TO_10_HOURS, 5.2)
                    )
                    .build();

                triggerValid(jurorNumber, ExpenseType.FOR_REAPPROVAL, request);

                Appearance appearance = getAppearance(jurorNumber, request.getDateOfExpense(), "415");
                assertThat(appearance).isNotNull();
                assertThat(appearance.getPayAttendanceType()).isEqualTo(PayAttendanceType.FULL_DAY);
                assertThat(appearance.isPayCash()).isEqualTo(false);
                assertThat(appearance.getTravelTime()).isEqualTo(LocalTime.of(2, 2));

                //Financial Loss
                assertThat(appearance.getLossOfEarningsDue()).isEqualTo(doubleToBigDecimal(12.10));
                assertThat(appearance.getChildcareDue()).isEqualTo(doubleToBigDecimal(7.10));
                assertThat(appearance.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(9.93));
                assertThat(appearance.getMiscDescription()).isEqualTo("Desc 2");
                //Travel
                assertThat(appearance.getTraveledByCar()).isTrue();
                assertThat(appearance.getJurorsTakenCar()).isNull();
                assertThat(appearance.getCarDue()).isEqualTo(doubleToBigDecimal(1.88));


                assertThat(appearance.getMilesTraveled()).isEqualTo(6);
                assertThat(appearance.getParkingDue()).isEqualTo(doubleToBigDecimal(16.02));
                assertThat(appearance.getPublicTransportDue()).isEqualTo(doubleToBigDecimal(12.00));
                assertThat(appearance.getHiredVehicleDue()).isEqualTo(doubleToBigDecimal(6.00));

                // Subsistence
                assertThat(appearance.getFoodAndDrinkClaimType())
                    .isEqualTo(FoodDrinkClaimType.LESS_THAN_OR_EQUAL_TO_10_HOURS);
                assertThat(appearance.getSmartCardAmountDue()).isEqualTo(doubleToBigDecimal(5.2));
                assertThat(appearance.getSubsistenceDue()).isEqualTo(doubleToBigDecimal(5.71));


                assertThat(appearance.getTotalDue()).isEqualTo(doubleToBigDecimal(65.54));


                List<FinancialAuditDetailsAppearances> financialAuditDetailsAppearances = new ArrayList<>();
                financialAuditDetailsAppearancesRepository.findAll().forEach(financialAuditDetailsAppearances::add);
                assertThat(financialAuditDetailsAppearances).hasSize(1);
                assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearances.get(0),
                    2, LocalDate.of(2023, 1, 15), 2);
                assertJurorHistory(jurorNumber, HistoryCodeMod.EDIT_PAYMENTS, "COURT_USER", "£65.54",
                    poolNumber, LocalDate.of(2023, 1, 15), "F2", 1, 0);
            }
        }
    }

    @Nested
    @DisplayName("POST " + CalculateTotals.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_calculateTotalExpenseSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class CalculateTotals extends AbstractDraftDailyExpense {
        public static final String URL = BASE_URL + "/calculate/totals";

        private static final String JUROR_NUMBER = "641500020";
        private static final String POOL_NUMBER = "415230101";

        CalculateTotals() {
            super(URL, "calculateTotalExpense");
        }

        @Nested
        class Positive {
            protected CombinedExpenseDetailsDto<ExpenseDetailsForTotals> triggerValid(
                CalculateTotalExpenseRequestDto request) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<CombinedExpenseDetailsDto<ExpenseDetailsForTotals>> response = template.exchange(
                    new RequestEntity<>(request, httpHeaders, POST,
                        URI.create(URL)),
                    new ParameterizedTypeReference<>() {
                    });
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                assertThat(response.getBody())
                    .as("Expect no body")
                    .isNotNull();
                return response.getBody();
            }

            @Test
            void typicalAttendanceDay() throws Exception {
                CalculateTotalExpenseRequestDto request = CalculateTotalExpenseRequestDto.builder()
                    .jurorNumber(JUROR_NUMBER)
                    .poolNumber(POOL_NUMBER)
                    .expenseList(List.of(
                        DailyExpense.builder()
                            .dateOfExpense(LocalDate.of(2023, 1, 5))
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
                                createDailyExpenseFoodAndDrink(FoodDrinkClaimType.LESS_THAN_OR_EQUAL_TO_10_HOURS, 4.2)
                            )
                            .build(),
                        DailyExpense.builder()
                            .dateOfExpense(LocalDate.of(2023, 1, 11))
                            .payCash(false)
                            .time(DailyExpenseTime.builder()
                                .travelTime(LocalTime.of(1, 2))
                                .payAttendance(PayAttendanceType.FULL_DAY)
                                .build())
                            .financialLoss(
                                createDailyExpenseFinancialLoss(15.01, 6.00, 6.20, "Desc")
                            )
                            .travel(
                                createDailyExpenseTravel(TravelMethod.CAR, 3, 15, 13.25, 12.1, 9.4)
                            )
                            .foodAndDrink(
                                createDailyExpenseFoodAndDrink(FoodDrinkClaimType.MORE_THAN_10_HOURS, 4.1)
                            )
                            .build()
                    ))
                    .build();

                CombinedExpenseDetailsDto<ExpenseDetailsForTotals> response = triggerValid(request);
                assertThat(response).isNotNull();
                assertThat(response.getExpenseDetails()).hasSize(2);
                assertThat(response.getExpenseDetails().get(0)).isEqualTo(
                    ExpenseDetailsForTotals.builder()
                        .financialLossApportionedApplied(false)
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .totalDue(new BigDecimal("45.34000"))
                        .totalPaid(new BigDecimal("0.00"))
                        .attendanceDate(LocalDate.of(2023, 1, 5))
                        .attendanceType(AttendanceType.FULL_DAY)
                        .paymentMethod(PaymentMethod.BACS)
                        .lossOfEarnings(new BigDecimal("25.01"))
                        .extraCare(new BigDecimal("10.00"))
                        .other(new BigDecimal("5.00"))
                        .publicTransport(BigDecimal.ZERO)
                        .taxi(BigDecimal.ZERO)
                        .motorcycle(BigDecimal.ZERO)
                        .car(new BigDecimal("1.57000"))
                        .bicycle(BigDecimal.ZERO)
                        .parking(new BigDecimal("2.25"))
                        .foodAndDrink(new BigDecimal("5.71000"))
                        .smartCard(new BigDecimal("4.20"))
                        .build());
                assertThat(response.getExpenseDetails().get(1)).isEqualTo(
                    ExpenseDetailsForTotals.builder()
                        .financialLossApportionedApplied(false)
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .totalDue(new BigDecimal("76.00000"))
                        .totalPaid(new BigDecimal("8.00"))
                        .attendanceDate(LocalDate.of(2023, 1, 11))
                        .attendanceType(AttendanceType.FULL_DAY)
                        .paymentMethod(PaymentMethod.BACS)
                        .lossOfEarnings(new BigDecimal("15.01"))
                        .extraCare(new BigDecimal("6.00"))
                        .other(new BigDecimal("6.20"))
                        .publicTransport(new BigDecimal("12.10"))
                        .taxi(new BigDecimal("9.40"))
                        .motorcycle(BigDecimal.ZERO)
                        .car(new BigDecimal("5.97000"))
                        .bicycle(BigDecimal.ZERO)
                        .parking(new BigDecimal("13.25"))
                        .foodAndDrink(new BigDecimal("12.17000"))
                        .smartCard(new BigDecimal("4.10"))
                        .build());
                assertThat(response.getTotal()).isEqualTo(
                    CombinedExpenseDetailsDto.Total.builder()
                        .totalDays(2)
                        .lossOfEarnings(new BigDecimal("40.02"))
                        .extraCare(new BigDecimal("16.00"))
                        .other(new BigDecimal("11.20"))
                        .publicTransport(new BigDecimal("12.10"))
                        .taxi(new BigDecimal("9.40"))
                        .motorcycle(BigDecimal.ZERO)
                        .car(new BigDecimal("7.54000"))
                        .bicycle(BigDecimal.ZERO)
                        .parking(new BigDecimal("15.50"))
                        .foodAndDrink(new BigDecimal("17.88000"))
                        .smartCard(new BigDecimal("8.30"))
                        .build());
            }

            @Test
            void typicalNonAttendanceDay() throws Exception {
                CalculateTotalExpenseRequestDto request = CalculateTotalExpenseRequestDto.builder()
                    .jurorNumber(JUROR_NUMBER)
                    .poolNumber(POOL_NUMBER)
                    .expenseList(List.of(
                        DailyExpense.builder()
                            .dateOfExpense(LocalDate.of(2023, 1, 17))
                            .payCash(false)
                            .time(DailyExpenseTime.builder()
                                .payAttendance(PayAttendanceType.FULL_DAY)
                                .build())
                            .financialLoss(
                                createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc")
                            )
                            .build()
                    ))
                    .build();

                CombinedExpenseDetailsDto<ExpenseDetailsForTotals> response = triggerValid(request);
                assertThat(response).isNotNull();
                assertThat(response.getExpenseDetails()).hasSize(1);
                assertThat(response.getExpenseDetails().get(0)).isEqualTo(
                    ExpenseDetailsForTotals.builder()
                        .financialLossApportionedApplied(false)
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .totalDue(new BigDecimal("33.01"))
                        .totalPaid(new BigDecimal("8.00"))
                        .attendanceDate(LocalDate.of(2023, 1, 17))
                        .attendanceType(AttendanceType.FULL_DAY)
                        .paymentMethod(PaymentMethod.BACS)
                        .lossOfEarnings(new BigDecimal("25.01"))
                        .extraCare(new BigDecimal("10.00"))
                        .other(new BigDecimal("5.00"))
                        .publicTransport(new BigDecimal("1.00"))
                        .taxi(new BigDecimal("5.00"))
                        .motorcycle(new BigDecimal("0.00"))
                        .car(new BigDecimal("1.00"))
                        .bicycle(new BigDecimal("0.00"))
                        .parking(new BigDecimal("12.00"))
                        .foodAndDrink(new BigDecimal("3.00"))
                        .smartCard(new BigDecimal("29.00"))
                        .build());
                assertThat(response.getTotal()).isEqualTo(
                    CombinedExpenseDetailsDto.Total.builder()
                        .totalDays(1)
                        .lossOfEarnings(new BigDecimal("25.01"))
                        .extraCare(new BigDecimal("10.00"))
                        .other(new BigDecimal("5.00"))
                        .publicTransport(new BigDecimal("1.00"))
                        .taxi(new BigDecimal("5.00"))
                        .motorcycle(new BigDecimal("0.00"))
                        .car(new BigDecimal("1.00"))
                        .bicycle(new BigDecimal("0.00"))
                        .parking(new BigDecimal("12.00"))
                        .foodAndDrink(new BigDecimal("3.00"))
                        .smartCard(new BigDecimal("29.00"))
                        .build());
            }

            @Test
            void typicalFinancialLossApportioned() throws Exception {
                CalculateTotalExpenseRequestDto request = CalculateTotalExpenseRequestDto.builder()
                    .jurorNumber(JUROR_NUMBER)
                    .poolNumber(POOL_NUMBER)
                    .expenseList(List.of(
                        DailyExpense.builder()
                            .dateOfExpense(LocalDate.of(2023, 1, 5))
                            .payCash(false)
                            .time(DailyExpenseTime.builder()
                                .travelTime(LocalTime.of(1, 2))
                                .payAttendance(PayAttendanceType.FULL_DAY)
                                .build())
                            .financialLoss(
                                createDailyExpenseFinancialLoss(50.12, 16.30, 5.10, "Desc")
                            )
                            .travel(
                                createDailyExpenseTravel(TravelMethod.CAR, null, 5, 2.25, null, null)
                            )
                            .foodAndDrink(
                                createDailyExpenseFoodAndDrink(FoodDrinkClaimType.LESS_THAN_OR_EQUAL_TO_10_HOURS, 4.2)
                            )
                            .build()
                    ))
                    .build();

                CombinedExpenseDetailsDto<ExpenseDetailsForTotals> response = triggerValid(request);
                assertThat(response).isNotNull();
                assertThat(response.getExpenseDetails()).hasSize(1);
                assertThat(response.getExpenseDetails().get(0)).isEqualTo(
                    ExpenseDetailsForTotals.builder()
                        .financialLossApportionedApplied(true)
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .totalDue(new BigDecimal("70.28000"))
                        .totalPaid(new BigDecimal("0.00"))
                        .attendanceDate(LocalDate.of(2023, 1, 5))
                        .attendanceType(AttendanceType.FULL_DAY)
                        .paymentMethod(PaymentMethod.BACS)
                        .lossOfEarnings(new BigDecimal("50.12"))
                        .extraCare(new BigDecimal("14.83000"))
                        .other(BigDecimal.ZERO)
                        .publicTransport(BigDecimal.ZERO)
                        .taxi(BigDecimal.ZERO)
                        .motorcycle(BigDecimal.ZERO)
                        .car(new BigDecimal("1.57000"))
                        .bicycle(BigDecimal.ZERO)
                        .parking(new BigDecimal("2.25"))
                        .foodAndDrink(new BigDecimal("5.71000"))
                        .smartCard(new BigDecimal("4.20"))
                        .build());
                assertThat(response.getTotal()).isEqualTo(
                    CombinedExpenseDetailsDto.Total.builder()
                        .totalDays(1)
                        .lossOfEarnings(new BigDecimal("50.12"))
                        .extraCare(new BigDecimal("14.83000"))
                        .other(BigDecimal.ZERO)
                        .publicTransport(BigDecimal.ZERO)
                        .taxi(BigDecimal.ZERO)
                        .motorcycle(BigDecimal.ZERO)
                        .car(new BigDecimal("1.57000"))
                        .bicycle(BigDecimal.ZERO)
                        .parking(new BigDecimal("2.25"))
                        .foodAndDrink(new BigDecimal("5.71000"))
                        .smartCard(new BigDecimal("4.20"))
                        .build());
            }

            @Test
            void typicalWithDbData() throws Exception {
                CalculateTotalExpenseRequestDto request = CalculateTotalExpenseRequestDto.builder()
                    .jurorNumber(JUROR_NUMBER)
                    .poolNumber(POOL_NUMBER)
                    .expenseList(List.of(
                        DailyExpense.builder()
                            .dateOfExpense(LocalDate.of(2023, 1, 5))
                            .build(),
                        DailyExpense.builder()
                            .dateOfExpense(LocalDate.of(2023, 1, 11))
                            .payCash(false)
                            .time(DailyExpenseTime.builder()
                                .travelTime(LocalTime.of(1, 2))
                                .payAttendance(PayAttendanceType.FULL_DAY)
                                .build())
                            .financialLoss(
                                createDailyExpenseFinancialLoss(15.01, 6.00, 6.20, "Desc")
                            )
                            .travel(
                                createDailyExpenseTravel(TravelMethod.CAR, 3, 15, 13.25, 12.1, 9.4)
                            )
                            .foodAndDrink(
                                createDailyExpenseFoodAndDrink(FoodDrinkClaimType.MORE_THAN_10_HOURS, 4.1)
                            )
                            .build()
                    ))
                    .build();

                CombinedExpenseDetailsDto<ExpenseDetailsForTotals> response = triggerValid(request);
                assertThat(response).isNotNull();
                assertThat(response.getExpenseDetails()).hasSize(2);
                assertThat(response.getExpenseDetails().get(0)).isEqualTo(
                    ExpenseDetailsForTotals.builder()
                        .financialLossApportionedApplied(false)
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .totalDue(new BigDecimal("525.00"))
                        .totalPaid(new BigDecimal("0.00"))
                        .attendanceDate(LocalDate.of(2023, 1, 5))
                        .attendanceType(AttendanceType.FULL_DAY)
                        .paymentMethod(PaymentMethod.BACS)
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
                        .build());
                assertThat(response.getExpenseDetails().get(1)).isEqualTo(
                    ExpenseDetailsForTotals.builder()
                        .financialLossApportionedApplied(false)
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .totalDue(new BigDecimal("76.00000"))
                        .totalPaid(new BigDecimal("8.00"))
                        .attendanceDate(LocalDate.of(2023, 1, 11))
                        .attendanceType(AttendanceType.FULL_DAY)
                        .paymentMethod(PaymentMethod.BACS)
                        .lossOfEarnings(new BigDecimal("15.01"))
                        .extraCare(new BigDecimal("6.00"))
                        .other(new BigDecimal("6.20"))
                        .publicTransport(new BigDecimal("12.10"))
                        .taxi(new BigDecimal("9.40"))
                        .motorcycle(BigDecimal.ZERO)
                        .car(new BigDecimal("5.97000"))
                        .bicycle(BigDecimal.ZERO)
                        .parking(new BigDecimal("13.25"))
                        .foodAndDrink(new BigDecimal("12.17000"))
                        .smartCard(new BigDecimal("4.10"))
                        .build());
                CombinedExpenseDetailsDto.Total total = response.getTotal();
                total.setHasTotals(true);//Have to set this to true so we can view the totals
                assertThat(total).isEqualTo(
                    CombinedExpenseDetailsDto.Total.builder()
                        .hasTotals(true)
                        .totalDays(2)
                        .lossOfEarnings(new BigDecimal("105.01"))
                        .extraCare(new BigDecimal("76.00"))
                        .other(new BigDecimal("86.20"))
                        .publicTransport(new BigDecimal("22.10"))
                        .taxi(new BigDecimal("29.40"))
                        .motorcycle(new BigDecimal("30.00"))
                        .car(new BigDecimal("45.97000"))
                        .bicycle(new BigDecimal("50.00"))
                        .parking(new BigDecimal("73.25"))
                        .foodAndDrink(new BigDecimal("112.17000"))
                        .smartCard(new BigDecimal("29.10"))
                        .totalDue(new BigDecimal("601.00000"))
                        .totalPaid(new BigDecimal("8.00"))
                        .build());
            }
        }

        @Nested
        class Negative {
            protected ResponseEntity<String> triggerInvalid(String owner,
                                                            CalculateTotalExpenseRequestDto request) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(request, httpHeaders, POST,
                        URI.create(URL)),
                    String.class);
            }

            @Test
            void badPayload() throws Exception {
                assertInvalidPayload(triggerInvalid("415", CalculateTotalExpenseRequestDto.builder()
                        .jurorNumber(null)
                        .poolNumber(POOL_NUMBER)
                        .expenseList(List.of(
                            DailyExpense.builder()
                                .dateOfExpense(LocalDate.of(2023, 1, 17))
                                .payCash(false)
                                .time(DailyExpenseTime.builder()
                                    .payAttendance(PayAttendanceType.FULL_DAY)
                                    .build())
                                .financialLoss(
                                    createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc")
                                )
                                .build()
                        ))
                        .build()),
                    new RestResponseEntityExceptionHandler.FieldError("jurorNumber", "must not be blank"));
            }

            @Test
            void forbiddenIsBureauUser() throws Exception {
                assertForbiddenResponse(triggerInvalid("400", CalculateTotalExpenseRequestDto.builder()
                    .jurorNumber(JUROR_NUMBER)
                    .poolNumber(POOL_NUMBER)
                    .expenseList(List.of(
                        DailyExpense.builder()
                            .dateOfExpense(LocalDate.of(2023, 1, 17))
                            .payCash(false)
                            .time(DailyExpenseTime.builder()
                                .payAttendance(PayAttendanceType.FULL_DAY)
                                .build())
                            .financialLoss(
                                createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc")
                            )
                            .build()
                    ))
                    .build()), URL);
            }

            @Test
            void expenseNotFound() throws Exception {
                assertNotFound(triggerInvalid("415", CalculateTotalExpenseRequestDto.builder()
                    .jurorNumber(JUROR_NUMBER)
                    .poolNumber(POOL_NUMBER)
                    .expenseList(List.of(
                        DailyExpense.builder()
                            .dateOfExpense(LocalDate.of(2024, 1, 17))
                            .payCash(false)
                            .time(DailyExpenseTime.builder()
                                .payAttendance(PayAttendanceType.FULL_DAY)
                                .build())
                            .financialLoss(
                                createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc")
                            )
                            .build()
                    ))
                    .build()), URL, "No appearance record found for juror: 641500020 on day: 2024-01-17");
            }

            @Test
            void expensesLessThanZero() throws Exception {
                assertBusinessRuleViolation(triggerInvalid("415", CalculateTotalExpenseRequestDto.builder()
                        .jurorNumber(JUROR_NUMBER)
                        .poolNumber(POOL_NUMBER)
                        .expenseList(List.of(
                            DailyExpense.builder()
                                .dateOfExpense(LocalDate.of(2023, 1, 5))
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
                                    createDailyExpenseFoodAndDrink(FoodDrinkClaimType.LESS_THAN_OR_EQUAL_TO_10_HOURS,
                                        1000.00)
                                )
                                .build()
                        ))
                        .build()), "Total expenses cannot be less than £0. For Day 2023-01-05",
                    MojException.BusinessRuleViolation.ErrorCode.EXPENSES_CANNOT_BE_LESS_THAN_ZERO);
            }

            @Test
            void dueIsLessThanPaid() throws Exception {
                assertBusinessRuleViolation(triggerInvalid("415", CalculateTotalExpenseRequestDto.builder()
                        .jurorNumber(JUROR_NUMBER)
                        .poolNumber(POOL_NUMBER)
                        .expenseList(List.of(
                            DailyExpense.builder()
                                .dateOfExpense(LocalDate.of(2023, 1, 11))
                                .payCash(false)
                                .time(DailyExpenseTime.builder()
                                    .travelTime(LocalTime.of(1, 2))
                                    .payAttendance(PayAttendanceType.FULL_DAY)
                                    .build())
                                .financialLoss(
                                    createDailyExpenseFinancialLoss(15.01, 6.00, 0.20, "Desc")
                                )
                                .travel(
                                    createDailyExpenseTravel(TravelMethod.CAR, 3, 15, 13.25, 12.1, 9.4)
                                )
                                .foodAndDrink(
                                    createDailyExpenseFoodAndDrink(FoodDrinkClaimType.MORE_THAN_10_HOURS, 4.1)
                                )
                                .build()
                        ))
                        .build()), "Updated expense values cannot be less than the paid amount",
                    MojException.BusinessRuleViolation.ErrorCode.EXPENSE_VALUES_REDUCED_LESS_THAN_PAID);
            }
        }
    }


    @Nested
    @DisplayName("GET /api/v1/moj/expenses/submit-for-approval")
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_draftExpenseSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class GetCounts {

        public static final String URL = BASE_URL + "/counts/{juror_number}/{pool_number}";

        private String toUrl(String jurorNumber, String poolNumber) {
            return URL.replace("{juror_number}", jurorNumber)
                .replace("{pool_number}", poolNumber);
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            protected ExpenseCount triggerValid(String jurorNumber,
                                                String poolNumber) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<ExpenseCount> response = template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(toUrl(jurorNumber, poolNumber))),
                    ExpenseCount.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                assertThat(response.getBody())
                    .as("Expect no body")
                    .isNotNull();
                return response.getBody();
            }

            @Test
            void typical() throws Exception {
                ExpenseCount expenseCount = triggerValid("641500020", "415230101");
                assertThat(expenseCount).isEqualTo(ExpenseCount.builder()
                    .totalDraft(3)
                    .totalForApproval(3)
                    .totalApproved(5)
                    .totalForReapproval(1)
                    .build());
            }

            @Test
            void notAppearances() throws Exception {
                ExpenseCount expenseCount = triggerValid("641500029", "415230101");
                assertThat(expenseCount).isEqualTo(ExpenseCount.builder()
                    .totalDraft(0)
                    .totalForApproval(0)
                    .totalApproved(0)
                    .totalForReapproval(0)
                    .build());
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            protected ResponseEntity<String> triggerInvalid(String jurorNumber,
                                                            String poolNumber, String owner) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(toUrl(jurorNumber, poolNumber))),
                    String.class);
            }

            @Test
            void canNotAccessJurorPool() throws Exception {
                assertMojForbiddenResponse(triggerInvalid("641500020", "415230101", "414"),
                    toUrl("641500020", "415230101"),
                    "User cannot access this juror pool");
            }

            @Test
            void isBureauUser() throws Exception {
                assertForbiddenResponse(triggerInvalid("641500020", "415230101", "400"),
                    toUrl("641500020", "415230101"));
            }

            @Test
            void invalidJurorNumber() throws Exception {
                assertInvalidPathParam(triggerInvalid("INVALID", "415230101", "415"),
                    "getCounts.jurorNumber: must match \"^\\d{9}$\"");
            }

            @Test
            void invalidPoolNumber() throws Exception {
                assertInvalidPathParam(triggerInvalid("641500020", "INVALID", "415"),
                    "getCounts.poolNumber: must match \"^\\d{9}$\"");
            }
        }
    }

    @Nested
    @DisplayName("GET " + GetExpensesForApproval.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_getApproveExpenseSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class GetExpensesForApproval {
        public static final String URL = BASE_URL + "/approval/{loc_code}/{payment_method}";

        private String toUrl(String locCode, PaymentMethod paymentMethod, LocalDate from, LocalDate to) {
            return toUrl(locCode, paymentMethod.name(),
                from == null
                    ? null
                    : from.format(DateTimeFormatter.ISO_DATE),
                to == null
                    ? null
                    : to.format(DateTimeFormatter.ISO_DATE));
        }

        private String toUrl(String locCode, String paymentMethod, String from, String to) {
            String urlTmp = URL.replace("{loc_code}", locCode)
                .replace("{payment_method}", paymentMethod);
            if (from != null) {
                urlTmp += "?from=" + from;
            }
            if (to != null) {
                urlTmp += (from != null
                    ? "&"
                    : "?") + "to=" + to;
            }
            return urlTmp;
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            protected PendingApprovalList triggerValid(String locCode,
                                                       LocalDate from,
                                                       LocalDate to,
                                                       PaymentMethod paymentMethod) throws Exception {
                return triggerValid(COURT_USER, locCode, from, to, paymentMethod);
            }

            protected PendingApprovalList triggerValid(String username,
                                                       String locCode,
                                                       LocalDate from,
                                                       LocalDate to,
                                                       PaymentMethod paymentMethod) throws Exception {
                final String jwt = createBureauJwt(username, locCode);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<PendingApprovalList> response = template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(toUrl(locCode, paymentMethod, from, to))),
                    PendingApprovalList.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                assertThat(response.getBody())
                    .as("Expect no body")
                    .isNotNull();
                return response.getBody();
            }

            @Test
            void typicalCash() throws Exception {
                PendingApprovalList pendingApprovals = triggerValid("415", null, null, PaymentMethod.CASH);
                assertThat(pendingApprovals.getTotalPendingCash()).isEqualTo(2L);
                assertThat(pendingApprovals.getTotalPendingBacs()).isEqualTo(3L);

                assertThat(pendingApprovals.getPendingApproval()).as("Verify pendingApprovals").containsExactly(
                    PendingApproval.builder()
                        .jurorNumber("641500020")
                        .poolNumber("415230101")
                        .firstName("Fnametwozero")
                        .lastName("Lnametwozero")
                        .amountDue(new BigDecimal("1683.98"))
                        .expenseType(ExpenseType.FOR_APPROVAL)
                        .canApprove(true)
                        .dateToRevisions(List.of(
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 2, 8))
                                .version(1L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 2, 9))
                                .version(2L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 2, 10))
                                .version(1L)
                                .build()
                        ))
                        .build(),
                    PendingApproval.builder()
                        .jurorNumber("641500021")
                        .poolNumber("415230101")
                        .firstName("Fnametwoone")
                        .lastName("Lnametwoone")
                        .amountDue(new BigDecimal("1134.01"))
                        .expenseType(ExpenseType.FOR_APPROVAL)
                        .canApprove(true)
                        .dateToRevisions(List.of(
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 2, 1))
                                .version(1L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 2, 2))
                                .version(2L)
                                .build()
                        ))
                        .build()
                );
            }

            @Test
            void typicalBacs() throws Exception {
                PendingApprovalList pendingApprovals = triggerValid("415", null, null, PaymentMethod.BACS);
                assertThat(pendingApprovals.getTotalPendingCash()).isEqualTo(2L);
                assertThat(pendingApprovals.getTotalPendingBacs()).isEqualTo(3L);

                assertThat(pendingApprovals.getPendingApproval()).as("Verify pendingApprovals").containsExactly(
                    PendingApproval.builder()
                        .jurorNumber("641500020")
                        .poolNumber("415230101")
                        .firstName("Fnametwozero")
                        .lastName("Lnametwozero")
                        .amountDue(new BigDecimal("1683.98"))
                        .expenseType(ExpenseType.FOR_APPROVAL)
                        .canApprove(true)
                        .dateToRevisions(List.of(
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 8))
                                .version(1L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 9))
                                .version(2L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 10))
                                .version(1L)
                                .build()
                        ))
                        .build(),
                    PendingApproval.builder()
                        .jurorNumber("641500020")
                        .poolNumber("415230101")
                        .firstName("Fnametwozero")
                        .lastName("Lnametwozero")
                        .amountDue(new BigDecimal("407.00"))
                        .expenseType(ExpenseType.FOR_REAPPROVAL)
                        .canApprove(true)
                        .dateToRevisions(List.of(
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 14))
                                .version(1L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 15))
                                .version(1L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 16))
                                .version(1L)
                                .build()
                        ))
                        .build(),
                    PendingApproval.builder()
                        .jurorNumber("641500021")
                        .poolNumber("415230101")
                        .firstName("Fnametwoone")
                        .lastName("Lnametwoone")
                        .amountDue(new BigDecimal("1687.98"))
                        .expenseType(ExpenseType.FOR_APPROVAL)
                        .canApprove(true)
                        .dateToRevisions(List.of(
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 8))
                                .version(1L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 9))
                                .version(2L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 10))
                                .version(1L)
                                .build()
                        ))
                        .build()
                );
            }

            @Test
            void typicalFromDateFilter() throws Exception {
                PendingApprovalList pendingApprovals =
                    triggerValid("415", LocalDate.of(2023, 1, 14), null, PaymentMethod.BACS);
                assertThat(pendingApprovals.getTotalPendingCash()).isEqualTo(2L);
                assertThat(pendingApprovals.getTotalPendingBacs()).isEqualTo(3L);

                assertThat(pendingApprovals.getPendingApproval()).as("Verify pendingApprovals").containsExactly(
                    PendingApproval.builder()
                        .jurorNumber("641500020")
                        .poolNumber("415230101")
                        .firstName("Fnametwozero")
                        .lastName("Lnametwozero")
                        .amountDue(new BigDecimal("407.00"))
                        .expenseType(ExpenseType.FOR_REAPPROVAL)
                        .canApprove(true)
                        .dateToRevisions(List.of(
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 14))
                                .version(1L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 15))
                                .version(1L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 16))
                                .version(1L)
                                .build()
                        ))
                        .build());
            }

            @Test
            void typicalToDateFilter() throws Exception {
                PendingApprovalList pendingApprovals =
                    triggerValid("415", null, LocalDate.of(2023, 1, 9), PaymentMethod.BACS);
                assertThat(pendingApprovals.getTotalPendingCash()).isEqualTo(2L);
                assertThat(pendingApprovals.getTotalPendingBacs()).isEqualTo(3L);

                assertThat(pendingApprovals.getPendingApproval()).as("Verify pendingApprovals").containsExactly(
                    PendingApproval.builder()
                        .jurorNumber("641500020")
                        .poolNumber("415230101")
                        .firstName("Fnametwozero")
                        .lastName("Lnametwozero")
                        .amountDue(new BigDecimal("1683.98"))
                        .expenseType(ExpenseType.FOR_APPROVAL)
                        .canApprove(true)
                        .dateToRevisions(List.of(
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 8))
                                .version(1L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 9))
                                .version(2L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 10))
                                .version(1L)
                                .build()
                        ))
                        .build(),
                    PendingApproval.builder()
                        .jurorNumber("641500021")
                        .poolNumber("415230101")
                        .firstName("Fnametwoone")
                        .lastName("Lnametwoone")
                        .amountDue(new BigDecimal("1687.98"))
                        .expenseType(ExpenseType.FOR_APPROVAL)
                        .canApprove(true)
                        .dateToRevisions(List.of(
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 8))
                                .version(1L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 9))
                                .version(2L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 10))
                                .version(1L)
                                .build()
                        ))
                        .build());
            }

            @Test
            void typicalBothFromAndToFilter() throws Exception {
                PendingApprovalList pendingApprovals =
                    triggerValid("415", LocalDate.of(2023, 1, 9), LocalDate.of(2023, 1, 10), PaymentMethod.BACS);
                assertThat(pendingApprovals.getTotalPendingCash()).isEqualTo(2L);
                assertThat(pendingApprovals.getTotalPendingBacs()).isEqualTo(3L);

                assertThat(pendingApprovals.getPendingApproval()).as("Verify pendingApprovals").containsExactly(
                    PendingApproval.builder()
                        .jurorNumber("641500020")
                        .poolNumber("415230101")
                        .firstName("Fnametwozero")
                        .lastName("Lnametwozero")
                        .amountDue(new BigDecimal("1683.98"))
                        .expenseType(ExpenseType.FOR_APPROVAL)
                        .canApprove(true)
                        .dateToRevisions(List.of(
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 8))
                                .version(1L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 9))
                                .version(2L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 10))
                                .version(1L)
                                .build()
                        ))
                        .build(),
                    PendingApproval.builder()
                        .jurorNumber("641500021")
                        .poolNumber("415230101")
                        .firstName("Fnametwoone")
                        .lastName("Lnametwoone")
                        .amountDue(new BigDecimal("1687.98"))
                        .expenseType(ExpenseType.FOR_APPROVAL)
                        .canApprove(true)
                        .dateToRevisions(List.of(
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 8))
                                .version(1L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 9))
                                .version(2L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 10))
                                .version(1L)
                                .build()
                        ))
                        .build());

            }

            @Test
            void canNotApprove() throws Exception {
                PendingApprovalList pendingApprovals =
                    triggerValid("COURT_USER2", "415", LocalDate.of(2023, 1, 9), LocalDate.of(2023, 1, 10),
                        PaymentMethod.BACS);
                assertThat(pendingApprovals.getTotalPendingCash()).isEqualTo(2L);
                assertThat(pendingApprovals.getTotalPendingBacs()).isEqualTo(3L);

                assertThat(pendingApprovals.getPendingApproval()).containsExactly(
                    PendingApproval.builder()
                        .jurorNumber("641500020")
                        .poolNumber("415230101")
                        .firstName("Fnametwozero")
                        .lastName("Lnametwozero")
                        .amountDue(new BigDecimal("1683.98"))
                        .expenseType(ExpenseType.FOR_APPROVAL)
                        .canApprove(true)
                        .dateToRevisions(List.of(
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 8))
                                .version(1L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 9))
                                .version(2L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 10))
                                .version(1L)
                                .build()
                        ))
                        .build(),
                    PendingApproval.builder()
                        .jurorNumber("641500021")
                        .poolNumber("415230101")
                        .firstName("Fnametwoone")
                        .lastName("Lnametwoone")
                        .amountDue(new BigDecimal("1687.98"))
                        .expenseType(ExpenseType.FOR_APPROVAL)
                        .canApprove(false)
                        .dateToRevisions(List.of(
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 8))
                                .version(1L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 9))
                                .version(2L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 10))
                                .version(1L)
                                .build()
                        ))
                        .build()
                );

            }

            @Test
            void notFound() throws Exception {
                assertThat(triggerValid("414", null, null, PaymentMethod.BACS)).isEqualTo(
                    PendingApprovalList.builder()
                        .totalPendingBacs(0L)
                        .totalPendingCash(0L)
                        .pendingApproval(Collections.emptyList())
                        .build());
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            protected ResponseEntity<String> triggerInvalid(String from,
                                                            String to,
                                                            String paymentMethod) throws Exception {
                return triggerInvalid(TestConstants.VALID_COURT_LOCATION,
                    TestConstants.VALID_COURT_LOCATION,
                    from,
                    to,
                    paymentMethod
                );
            }

            protected ResponseEntity<String> triggerInvalid(String owner,
                                                            String locCode,
                                                            String from,
                                                            String to,
                                                            String paymentMethod) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(toUrl(locCode, paymentMethod, from, to))),
                    String.class);
            }

            @Test
            void invalidFrom() throws Exception {
                assertInvalidPathParam(triggerInvalid("INVALID", null, PaymentMethod.BACS.name()),
                    "INVALID is the incorrect data type or is not in the expected format (from)");
            }

            @Test
            void invalidTo() throws Exception {
                assertInvalidPathParam(triggerInvalid(null, "INVALID", PaymentMethod.BACS.name()),
                    "INVALID is the incorrect data type or is not in the expected format (to)");
            }

            @Test
            void invalidLocCode() throws Exception {
                assertInvalidPathParam(triggerInvalid("INVALID", "INVALID", null, null, PaymentMethod.BACS.name()),
                    "getExpensesForApproval.locCode: must match \"^\\d{3}$\"");
            }

            @Test
            void invalidPaymentType() throws Exception {
                assertInvalidPathParam(triggerInvalid(null, null, "INVALID"),
                    "INVALID is the incorrect data type or is not in the expected format (payment_method)");
            }

            @Test
            void unauthorizedLocCodeNotPartOfUser() throws Exception {
                assertForbiddenResponse(triggerInvalid("415", "414", null, null, PaymentMethod.BACS.name()),
                    toUrl("414", PaymentMethod.BACS.name(), null, null));
            }
        }
    }

    @Nested
    @DisplayName("PATCH " + ApportionSmartCard.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_draftExpenseSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class ApportionSmartCard {
        public static final String URL = BASE_URL + "/smartcard";

        private static final String JUROR_NUMBER = "641500020";
        private static final String POOL_NUMBER = "415230101";

        private static final List<LocalDate> ATTENDANCE_DATES = List.of(
            LocalDate.of(2023, 1, 5),
            LocalDate.of(2023, 1, 6),
            LocalDate.of(2023, 1, 7)
        );

        protected ApportionSmartCardRequest getValidPayload(BigDecimal smartCardAmount) {
            return ApportionSmartCardRequest.builder()
                .jurorNumber(JUROR_NUMBER)
                .poolNumber(POOL_NUMBER)
                .smartCardAmount(smartCardAmount)
                .attendanceDates(ATTENDANCE_DATES)
                .build();
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            protected ResponseEntity<Void> triggerValid(ApportionSmartCardRequest request) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<Void> response = template.exchange(
                    new RequestEntity<>(request, httpHeaders, PATCH,
                        URI.create(URL)),
                    Void.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.ACCEPTED);
                return response;
            }

            private void assertAppearance(LocalDate localDate,
                                          BigDecimal expectedSmartCardAmount) {
                Appearance appearance = appearanceRepository.findByJurorNumberAndPoolNumberAndAttendanceDate(
                    ApportionSmartCard.JUROR_NUMBER, ApportionSmartCard.POOL_NUMBER, localDate).orElseThrow();
                assertThat(appearance.getSmartCardAmountDue()).isEqualTo(expectedSmartCardAmount);
            }

            @Test
            void typical() throws Exception {
                triggerValid(getValidPayload(new BigDecimal("90.00")));

                assertAppearance(ATTENDANCE_DATES.get(0),
                    new BigDecimal("30.00"));
                assertAppearance(ATTENDANCE_DATES.get(1),
                    new BigDecimal("30.00"));
                assertAppearance(ATTENDANCE_DATES.get(2),
                    new BigDecimal("30.00"));
            }


            @Test
            void typicalWithRoundingErrorsAccountedFor() throws Exception {
                triggerValid(getValidPayload(new BigDecimal("100.00")));

                assertAppearance(ATTENDANCE_DATES.get(0),
                    new BigDecimal("33.33"));
                assertAppearance(ATTENDANCE_DATES.get(1),
                    new BigDecimal("33.33"));
                assertAppearance(ATTENDANCE_DATES.get(2),
                    new BigDecimal("33.34"));
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            protected ResponseEntity<String> triggerInvalid(ApportionSmartCardRequest request,
                                                            String owner) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(request, httpHeaders, PATCH,
                        URI.create(URL)),
                    String.class);
            }

            @Test
            void invalidPayload() throws Exception {
                assertInvalidPayload(triggerInvalid(getValidPayload(new BigDecimal("-0.01")), "415"),
                    new RestResponseEntityExceptionHandler.FieldError("smartCardAmount",
                        "must be greater than or equal to 0"));
            }

            @Test
            void forbiddenIsBureauUser() throws Exception {
                assertForbiddenResponse(triggerInvalid(getValidPayload(new BigDecimal("90.00")), "400"),
                    URL);
            }

            @Test
            void forbiddenNotPartOfPoolCourt() throws Exception {
                assertMojForbiddenResponse(triggerInvalid(getValidPayload(new BigDecimal("90.00")), "414"),
                    URL, "User cannot access this juror pool");
            }

            @Test
            void jurorNotFound() throws Exception {
                ApportionSmartCardRequest payload = getValidPayload(new BigDecimal("90.00"));
                payload.setJurorNumber(TestConstants.VALID_JUROR_NUMBER);
                assertNotFound(triggerInvalid(payload, "415"), URL,
                    "One or more appearance records not found for Juror Number: 123456789, Pool Number: 415230101 and "
                        + "Attendance Dates provided");
            }

            @Test
            void negativeExpenseAmount() throws Exception {
                assertBusinessRuleViolation(triggerInvalid(getValidPayload(new BigDecimal("900000.00")), "415"),
                    "Total expenses cannot be less than £0. For Day 2023-01-05",
                    MojException.BusinessRuleViolation.ErrorCode.EXPENSES_CANNOT_BE_LESS_THAN_ZERO);
            }

            @Test
            void negativeNonDraftDays() throws Exception {
                ApportionSmartCardRequest payload = getValidPayload(new BigDecimal("90.00"));
                payload.setAttendanceDates(List.of(LocalDate.of(2023, 1, 11)));
                assertBusinessRuleViolation(triggerInvalid(payload, "415"),
                    "Can not apportion smart card for non-draft days",
                    MojException.BusinessRuleViolation.ErrorCode.APPORTION_SMART_CARD_NON_DRAFT_DAYS);
            }
        }
    }
}
