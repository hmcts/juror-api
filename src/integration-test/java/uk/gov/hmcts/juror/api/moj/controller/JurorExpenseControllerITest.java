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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.TestConstants;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.RequestDefaultExpensesDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ApportionSmartCardRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ApproveExpenseDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.CalculateTotalExpenseRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.CombinedExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.DateDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseTotal;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.ExpenseType;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.GetEnteredExpenseRequest;
import uk.gov.hmcts.juror.api.moj.controller.request.expense.UnpaidExpenseSummaryRequestDto;
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
import uk.gov.hmcts.juror.api.moj.domain.AppearanceId;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetails;
import uk.gov.hmcts.juror.api.moj.domain.FinancialAuditDetailsAppearances;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.PaymentData;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
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
import static org.springframework.http.HttpMethod.PUT;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + JurorExpenseControllerITest.BASE_URL)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings({"PMD.TooManyMethods", "PMD.ExcessiveImports"})
class JurorExpenseControllerITest extends AbstractIntegrationTest {

    public static final String JUROR_NUMBER = "641500020";
    public static final String COURT_LOCATION = "415";
    public static final String JUROR_NUMBER_NO_APPEARANCES = "641500024";
    public static final String POOL_NUMBER = "415230101";
    private static final String COURT_USER = "COURT_USER";
    private static final String BUREAU_USER = "BUREAU_USER";

    public static final String BASE_URL = "/api/v1/moj/expenses/{loc_code}";
    private static final String URL_UNPAID_SUMMARY = BASE_URL + "/unpaid-summary";


    private final TestRestTemplate template;
    private final AppearanceRepository appearanceRepository;
    private final FinancialAuditDetailsRepository financialAuditDetailsRepository;
    private final FinancialAuditDetailsAppearancesRepository financialAuditDetailsAppearancesRepository;
    private final CourtLocationRepository courtLocationRepository;
    private final JurorHistoryRepository jurorHistoryRepository;
    private final PaymentDataRepository paymentDataRepository;
    private final PlatformTransactionManager transactionManager;
    private TransactionTemplate transactionTemplate;

    private HttpHeaders httpHeaders;

    @Autowired
    private Clock clock;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        transactionTemplate = new TransactionTemplate(transactionManager);
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
        assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearance, id, attendanceDate,
            appearanceVersion, null);
    }

    private void assertFinancialAuditDetailsAppearances(
        FinancialAuditDetailsAppearances financialAuditDetailsAppearance, long id,
        LocalDate attendanceDate,
        long appearanceVersion,
        Long lastApprovedAuditNumber) {
        assertThat(financialAuditDetailsAppearance).isNotNull();
        assertThat(financialAuditDetailsAppearance.getFinancialAuditId()).isEqualTo(id);
        assertThat(financialAuditDetailsAppearance.getAttendanceDate()).isEqualTo(attendanceDate);
        assertThat(financialAuditDetailsAppearance.getAppearanceVersion()).isEqualTo(appearanceVersion);
        assertThat(financialAuditDetailsAppearance.getLastApprovedFAudit()).isEqualTo(lastApprovedAuditNumber);
    }

    @Nested
    @DisplayName("POST " + URL_UNPAID_SUMMARY)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_setUp.sql"})
    class GetUnpaidExpenses {
        public String toUrl(String courtLocation) {
            return URL_UNPAID_SUMMARY.replace("{loc_code}", courtLocation);
        }

        @Test
        @DisplayName("Valid court user - first page of results")
        void happyPathNoDateRangeFirstPage() throws Exception {
            final String courtLocation = COURT_LOCATION;
            final String jwt = createJwt(COURT_USER, courtLocation);
            final URI uri = URI.create(toUrl(courtLocation));

            UnpaidExpenseSummaryRequestDto requestDto =
                UnpaidExpenseSummaryRequestDto.builder()
                    .pageNumber(1)
                    .pageLimit(25)
                    .sortField(UnpaidExpenseSummaryRequestDto.SortField.TOTAL_IN_DRAFT)
                    .sortMethod(SortMethod.DESC)
                    .build();

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            ResponseEntity<PaginatedList<UnpaidExpenseSummaryResponseDto>> response =
                template.exchange(new RequestEntity<>(requestDto, httpHeaders, POST, uri),
                    new ParameterizedTypeReference<>() {
                    });

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            PaginatedList<UnpaidExpenseSummaryResponseDto> responseBody = response.getBody();
            assertNotNull(responseBody, "Response must be present");

            assertThat(responseBody.getTotalPages()).isEqualTo(2);
            assertThat(responseBody.getTotalItems()).isEqualTo(26);
            assertThat(responseBody.getData().size()).isEqualTo(25);
        }

        @Test
        @DisplayName("Valid court user - last page of results")
        void happyPathNoDateRangeLastPage() {
            final String courtLocation = COURT_LOCATION;
            final String jwt = createJwt(COURT_USER, courtLocation);
            final URI uri = URI.create(toUrl(courtLocation));

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            UnpaidExpenseSummaryRequestDto requestDto =
                UnpaidExpenseSummaryRequestDto.builder()
                    .pageNumber(2)
                    .pageLimit(25)
                    .sortField(UnpaidExpenseSummaryRequestDto.SortField.TOTAL_IN_DRAFT)
                    .sortMethod(SortMethod.DESC)
                    .build();

            ResponseEntity<PaginatedList<UnpaidExpenseSummaryResponseDto>> response =
                template.exchange(new RequestEntity<>(requestDto, httpHeaders, POST, uri),
                    new ParameterizedTypeReference<>() {
                    });
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            PaginatedList<UnpaidExpenseSummaryResponseDto> responseBody = response.getBody();
            assertNotNull(responseBody, "Response must be present");

            assertThat(responseBody.getTotalPages()).isEqualTo(2);
            assertThat(responseBody.getTotalItems()).isEqualTo(26);
            assertThat(responseBody.getData().size()).isEqualTo(1);
        }

        @Test
        @DisplayName("Valid court user - filter by date range")
        void happyPathWithDateRange() {
            final String courtLocation = COURT_LOCATION;
            final String jwt = createJwt(COURT_USER, courtLocation);
            final LocalDate minDate = LocalDate.of(2023, 1, 5);
            final LocalDate maxDate = LocalDate.of(2023, 1, 10);
            final URI uri = URI.create(toUrl(courtLocation));

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            UnpaidExpenseSummaryRequestDto requestDto =
                UnpaidExpenseSummaryRequestDto.builder()
                    .pageNumber(1)
                    .pageLimit(25)
                    .from(minDate)
                    .to(maxDate)
                    .sortField(UnpaidExpenseSummaryRequestDto.SortField.TOTAL_IN_DRAFT)
                    .sortMethod(SortMethod.DESC)
                    .build();

            ResponseEntity<PaginatedList<UnpaidExpenseSummaryResponseDto>> response =
                template.exchange(new RequestEntity<>(requestDto, httpHeaders, POST, uri),
                    new ParameterizedTypeReference<>() {
                    });
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            PaginatedList<UnpaidExpenseSummaryResponseDto> responseBody = response.getBody();
            assertNotNull(responseBody, "Response must be present");

            assertThat(responseBody.getTotalPages()).isEqualTo(1);
            assertThat(responseBody.getTotalItems()).isEqualTo(10);
            assertThat(responseBody.getData().size()).isEqualTo(10);
        }

        @Test
        @DisplayName("403 Forbidden - Invalid user")
        void invalidUser() throws Exception {
            final String jwt = createJwtBureau(COURT_USER);
            final LocalDate minDate = LocalDate.of(2023, 1, 5);
            final LocalDate maxDate = LocalDate.of(2023, 1, 10);
            final URI uri = URI.create(toUrl(COURT_LOCATION));

            UnpaidExpenseSummaryRequestDto requestDto =
                UnpaidExpenseSummaryRequestDto.builder()
                    .pageNumber(1)
                    .pageLimit(25)
                    .from(minDate)
                    .to(maxDate)
                    .sortField(UnpaidExpenseSummaryRequestDto.SortField.TOTAL_IN_DRAFT)
                    .sortMethod(SortMethod.DESC)
                    .build();
            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            RequestEntity<UnpaidExpenseSummaryRequestDto> request =
                new RequestEntity<>(requestDto, httpHeaders, POST, uri);
            ResponseEntity<Object> response = template.exchange(request, Object.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    @Nested
    @DisplayName("GET " + GetDefaultExpenses.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_setUp_default_expenses.sql"})
    class GetDefaultExpenses {

        public static final String URL = BASE_URL + "/{juror_number}/default-expenses";

        public String toUrl(String courtLocation, String jurorNumber) {
            return URL.replace("{loc_code}", courtLocation)
                .replace("{juror_number}", jurorNumber);
        }

        @Test
        @DisplayName("200 Ok - Happy Path")
        void retrieveDefaultExpensesHappyPath() throws Exception {
            final String jwt = createJwt(COURT_USER, COURT_LOCATION);
            final URI uri = URI.create(toUrl(COURT_LOCATION, JUROR_NUMBER));

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            DefaultExpenseResponseDto dto = new DefaultExpenseResponseDto();
            dto.setSmartCardNumber("12345678");
            dto.setFinancialLoss(BigDecimal.valueOf(0.0));
            dto.setDistanceTraveledMiles(6);
            dto.setTravelTime(LocalTime.of(4, 30));

            RequestEntity<DefaultExpenseResponseDto> request = new RequestEntity<>(httpHeaders, GET, uri);
            ResponseEntity<DefaultExpenseResponseDto> response =
                template.exchange(request, DefaultExpenseResponseDto.class);

            assertThat(response.getBody()).isEqualTo(
                DefaultExpenseResponseDto.builder()
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
            final String jwt = createJwt(COURT_USER, COURT_LOCATION);
            final URI uri = URI.create(toUrl(COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER));

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            RequestEntity<DefaultExpenseResponseDto> request = new RequestEntity<>(httpHeaders, GET, uri);
            ResponseEntity<DefaultExpenseResponseDto> response = template.exchange(request,
                DefaultExpenseResponseDto.class);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("POST " + SetDefaultExpenses.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_setUp_default_expenses.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class SetDefaultExpenses {

        public static final String URL = BASE_URL + "/{juror_number}/default-expenses";

        private BigDecimal createBigDecimal(double value) {
            return new BigDecimal(String.format("%.2f", value));
        }

        public String toUrl(String courtLocation, String jurorNumber) {
            return URL.replace("{loc_code}", courtLocation)
                .replace("{juror_number}", jurorNumber);
        }

        @Test
        @DisplayName("Positive Update default expenses")
        void setDefaultExpensesHappy() {
            final String jwt = createJwt(COURT_USER, COURT_LOCATION);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            RequestDefaultExpensesDto payload = new RequestDefaultExpensesDto();
            payload.setSmartCardNumber("123456789");
            payload.setFinancialLoss(createBigDecimal(2.00));
            payload.setTravelTime(LocalTime.of(0, 40));
            payload.setDistanceTraveledMiles(2);
            payload.setOverwriteExistingDraftExpenses(false);

            RequestEntity<RequestDefaultExpensesDto> request = new RequestEntity<>(payload, httpHeaders, POST,
                URI.create(toUrl(COURT_LOCATION, JUROR_NUMBER)));
            ResponseEntity<Void> response = template.exchange(request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        @DisplayName("Negative - Not found")
        void setDefaultExpensesNotFound() {
            final String jwt = createJwt(COURT_USER, COURT_LOCATION);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            RequestDefaultExpensesDto payload = new RequestDefaultExpensesDto();
            payload.setSmartCardNumber("123456789");
            payload.setFinancialLoss(createBigDecimal(1.03));
            payload.setTravelTime(LocalTime.of(0, 40));
            payload.setDistanceTraveledMiles(2);
            payload.setOverwriteExistingDraftExpenses(true);

            RequestEntity<RequestDefaultExpensesDto> request = new RequestEntity<>(payload, httpHeaders, POST,
                URI.create(toUrl(COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER)));
            ResponseEntity<Void> response = template.exchange(request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

    }

    @SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
    abstract class AbstractDraftDailyExpense {
        public static final String URL = BASE_URL + "/{juror_number}/DRAFT/edit";
        public static final String METHOD_NAME = "postEditDailyExpense";

        public String toUrl(String locCode, String jurorNumber) {
            return URL
                .replace("{loc_code}", locCode)
                .replace("{juror_number}", jurorNumber);
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


        @DisplayName("Negative")
        @Nested
        class Negative {


            protected ResponseEntity<String> triggerInvalid(String jurorNumber, DailyExpense request) throws
                Exception {
                final String jwt = createJwt(COURT_USER, COURT_LOCATION);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(List.of(request), httpHeaders, PUT,
                        URI.create(toUrl(COURT_LOCATION, jurorNumber))),
                    String.class);
            }

            @Test
            void invalidJurorNumber() throws Exception {
                final String jurorNumber = "INVALID";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 5))
                    .paymentMethod(PaymentMethod.BACS)
                    .time(DailyExpenseTime.builder()
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc")
                    )
                    .build();

                ResponseEntity<String> response = triggerInvalid(jurorNumber, request);
                assertInvalidPathParam(response,
                    METHOD_NAME + ".jurorNumber: must match \"^\\d{9}$\"");
            }

            @Test
            void noAttendancesFound() throws Exception {
                final String jurorNumber = "123456789";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 5))
                    .paymentMethod(PaymentMethod.BACS)
                    .time(DailyExpenseTime.builder()
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc")
                    )
                    .build();

                ResponseEntity<String> response = triggerInvalid(jurorNumber, request);
                assertNotFound(response, toUrl(COURT_LOCATION, jurorNumber),
                    "No draft appearance record found for juror: 123456789 on day: 2023-01-05");
            }
        }

        @DisplayName("Positive")
        @Nested
        class Positive {

            protected ResponseEntity<DailyExpenseResponse[]> triggerValid(String jurorNumber,
                                                                          DailyExpense request) throws Exception {
                final String jwt = createJwt(COURT_USER, COURT_LOCATION);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<DailyExpenseResponse[]> response = template.exchange(
                    new RequestEntity<>(List.of(request), httpHeaders, PUT,
                        URI.create(toUrl(COURT_LOCATION, jurorNumber))),
                    DailyExpenseResponse[].class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                return response;
            }
        }
    }

    @Nested
    @DisplayName("POST Draft Attended Day" + AbstractDraftDailyExpense.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_draftExpenseSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class PostDraftAttendedDayDailyExpense extends AbstractDraftDailyExpense {

        @Nested
        @DisplayName("Negative")
        class Negative extends AbstractDraftDailyExpense.Negative {

            @Test
            void negativeExpenseTotal() throws Exception {
                final String jurorNumber = "641500021";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 5))
                    .paymentMethod(PaymentMethod.BACS)
                    .time(DailyExpenseTime.builder()
                        .travelTime(LocalTime.of(1, 2))
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .foodAndDrink(
                        createDailyExpenseFoodAndDrink(FoodDrinkClaimType.LESS_THAN_OR_EQUAL_TO_10_HOURS, 10.72)
                    )
                    .build();
                assertBusinessRuleViolation(triggerInvalid(jurorNumber, request),
                    "Total expenses cannot be less than £0. For Day 2023-01-05",
                    MojException.BusinessRuleViolation.ErrorCode.EXPENSES_CANNOT_BE_LESS_THAN_ZERO);
            }

            @Test
            void applyToAllWith0Expense() throws Exception {
                final String jurorNumber = "641500022";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 5))
                    .paymentMethod(PaymentMethod.BACS)
                    .time(DailyExpenseTime.builder()
                        .travelTime(LocalTime.of(1, 2))
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .financialLoss(createDailyExpenseFinancialLoss(0.00, 5.00, 5.00, "Desc"))
                    .applyToAllDays(List.of(DailyExpenseApplyToAllDays.OTHER_COSTS))
                    .build();
                assertBusinessRuleViolation(triggerInvalid(jurorNumber, request),
                    "Total expenses cannot be less than £0. For Day 2023-01-06",
                    MojException.BusinessRuleViolation.ErrorCode.EXPENSES_CANNOT_BE_LESS_THAN_ZERO);
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
                    .paymentMethod(PaymentMethod.BACS)
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

                ResponseEntity<DailyExpenseResponse[]> response = triggerValid(jurorNumber, request);
                assertThat(response).isNotNull();
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody()[0].getFinancialLossWarning()).isNull();

                Appearance appearance = getAppearance(jurorNumber, request.getDateOfExpense(), COURT_LOCATION);

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
                    .paymentMethod(PaymentMethod.CASH)
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

                ResponseEntity<DailyExpenseResponse[]> response = triggerValid(jurorNumber, request);
                assertThat(response).isNotNull();
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody()).hasSize(1);

                FinancialLossWarning financialLossWarning = response.getBody()[0].getFinancialLossWarning();
                assertThat(financialLossWarning.getDate()).isEqualTo(request.getDateOfExpense());
                assertThat(financialLossWarning.getJurorsLoss()).isEqualTo(doubleToBigDecimal(70.01));
                assertThat(financialLossWarning.getLimit()).isEqualTo(doubleToBigDecimal(64.95, 5));
                assertThat(financialLossWarning.getAttendanceType()).isEqualTo(PayAttendanceType.FULL_DAY);
                assertThat(financialLossWarning.getMessage()).isEqualTo(
                    "The amount you entered will automatically be recalculated to limit the juror's loss to £64.95"
                );
                assertThat(financialLossWarning.getIsLongTrialDay()).isEqualTo(false);


                Appearance appearance = getAppearance(jurorNumber, request.getDateOfExpense(), COURT_LOCATION);

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
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 5))
                    .paymentMethod(PaymentMethod.CASH)
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

                ResponseEntity<DailyExpenseResponse[]> response = triggerValid(jurorNumber, request);
                assertThat(response).isNotNull();
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody()[0].getFinancialLossWarning()).isNull();

                List<Appearance> appearances =
                    appearanceRepository.findByCourtLocationLocCodeAndJurorNumberAndIsDraftExpenseTrue(
                        COURT_LOCATION, jurorNumber);

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
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 5))
                    .paymentMethod(PaymentMethod.CASH)
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

                ResponseEntity<DailyExpenseResponse[]> response = triggerValid(jurorNumber, request);
                assertThat(response).isNotNull();
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody()[0].getFinancialLossWarning()).isNull();

                List<Appearance> appearances =
                    appearanceRepository.findByCourtLocationLocCodeAndJurorNumberAndIsDraftExpenseTrue(
                        COURT_LOCATION, jurorNumber);

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
    @DisplayName("POST Draft Non-Attended Day" + AbstractDraftDailyExpense.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_draftExpenseSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class PostDraftNonAttendedDayDailyExpense extends AbstractDraftDailyExpense {

        @Nested
        @DisplayName("Negative")
        class Negative extends AbstractDraftDailyExpense.Negative {

            @Test
            void hasTravelExpenses() throws Exception {
                final String jurorNumber = "641500021";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 7))
                    .paymentMethod(PaymentMethod.BACS)
                    .time(DailyExpenseTime.builder()
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .travel(DailyExpenseTravel.builder().build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc")
                    )
                    .build();

                ResponseEntity<String> response = triggerInvalid(jurorNumber, request);
                assertInvalidPathParam(response, "travel: must be null");
            }

            @Test
            void hasFoodAndDrinkExpenses() throws Exception {
                final String jurorNumber = "641500021";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 7))
                    .paymentMethod(PaymentMethod.BACS)
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
                assertInvalidPathParam(response, "foodAndDrink: must be null");
            }


            @Test
            void hasTotalTravelTime() throws Exception {
                final String jurorNumber = "641500021";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 7))
                    .paymentMethod(PaymentMethod.BACS)
                    .time(DailyExpenseTime.builder()
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .travelTime(LocalTime.of(1, 1))
                        .build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc")
                    )
                    .build();

                ResponseEntity<String> response = triggerInvalid(jurorNumber, request);

                assertInvalidPathParam(response, "time.travelTime: must be null");
            }
        }

        @Nested
        @DisplayName("Positive")
        class Positive extends AbstractDraftDailyExpense.Positive {
            @Test
            void typical() throws Exception {
                final String jurorNumber = "641500021";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 7))
                    .paymentMethod(PaymentMethod.BACS)
                    .time(DailyExpenseTime.builder()
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc")
                    )
                    .build();

                ResponseEntity<DailyExpenseResponse[]> response = triggerValid(jurorNumber, request);
                assertThat(response).isNotNull();
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody()[0].getFinancialLossWarning()).isNull();

                Appearance appearance = getAppearance(jurorNumber, request.getDateOfExpense(), COURT_LOCATION);

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
                    .dateOfExpense(LocalDate.of(2023, 1, 7))
                    .paymentMethod(PaymentMethod.BACS)
                    .time(DailyExpenseTime.builder()
                        .payAttendance(PayAttendanceType.HALF_DAY)
                        .build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(75.00, 10.00, 5.00, "Desc 2")
                    )
                    .build();

                ResponseEntity<DailyExpenseResponse[]> response = triggerValid(jurorNumber, request);
                assertThat(response).isNotNull();
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody()).hasSize(1);

                FinancialLossWarning financialLossWarning = response.getBody()[0].getFinancialLossWarning();
                assertThat(financialLossWarning).isNotNull();
                assertThat(financialLossWarning.getDate()).isEqualTo(request.getDateOfExpense());
                assertThat(financialLossWarning.getJurorsLoss()).isEqualTo(doubleToBigDecimal(90.00));
                assertThat(financialLossWarning.getLimit()).isEqualTo(doubleToBigDecimal(64.95, 5));
                assertThat(financialLossWarning.getAttendanceType()).isEqualTo(PayAttendanceType.FULL_DAY);
                assertThat(financialLossWarning.getMessage()).isEqualTo(
                    "The amount you entered will automatically be recalculated to limit the juror's loss to £64.95"
                );
                assertThat(financialLossWarning.getIsLongTrialDay()).isEqualTo(false);

                Appearance appearance = getAppearance(jurorNumber, request.getDateOfExpense(), COURT_LOCATION);

                assertThat(appearance).isNotNull();
                assertThat(appearance.getPayAttendanceType()).isEqualTo(PayAttendanceType.FULL_DAY);
                assertThat(appearance.getLossOfEarningsDue()).isEqualTo(doubleToBigDecimal(64.95));
                assertThat(appearance.getChildcareDue()).isEqualTo(doubleToBigDecimal(0.00));
                assertThat(appearance.getMiscAmountDue()).isEqualTo(doubleToBigDecimal(0.00));
                assertThat(appearance.getMiscDescription()).isEqualTo("Desc 2");
                assertThat(appearance.isPayCash()).isEqualTo(false);

                assertThat(appearance.getTotalDue()).isEqualTo(doubleToBigDecimal(64.95));

            }

            @Test
            void zero() throws Exception {
                final String jurorNumber = "641500021";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 7))
                    .paymentMethod(PaymentMethod.BACS)
                    .time(DailyExpenseTime.builder()
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(0.0, 0.00, 0.00, null)
                    )
                    .build();

                ResponseEntity<DailyExpenseResponse[]> response = triggerValid(jurorNumber, request);
                assertThat(response).isNotNull();
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody()[0].getFinancialLossWarning()).isNull();

                Appearance appearance = getAppearance(jurorNumber, request.getDateOfExpense(), COURT_LOCATION);

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
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 7))
                    .paymentMethod(PaymentMethod.BACS)
                    .time(DailyExpenseTime.builder()
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc"))
                    .applyToAllDays(List.of(DailyExpenseApplyToAllDays.OTHER_COSTS))
                    .build();

                ResponseEntity<DailyExpenseResponse[]> response = triggerValid(jurorNumber, request);
                assertThat(response).isNotNull();
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody()[0].getFinancialLossWarning()).isNull();

                List<Appearance> appearances =
                    appearanceRepository.findByCourtLocationLocCodeAndJurorNumberAndIsDraftExpenseTrue(
                        COURT_LOCATION, jurorNumber);

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
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 7))
                    .paymentMethod(PaymentMethod.CASH)
                    .time(DailyExpenseTime.builder()
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .financialLoss(createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc 2"))
                    .applyToAllDays(List.of(DailyExpenseApplyToAllDays.OTHER_COSTS,
                        DailyExpenseApplyToAllDays.EXTRA_CARE_COSTS,
                        DailyExpenseApplyToAllDays.PAY_CASH))
                    .build();

                ResponseEntity<DailyExpenseResponse[]> response = triggerValid(jurorNumber, request);
                assertThat(response).isNotNull();
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody()).hasSize(1);
                assertThat(response.getBody()[0].getFinancialLossWarning()).isNull();

                List<Appearance> appearances =
                    appearanceRepository.findByCourtLocationLocCodeAndJurorNumberAndIsDraftExpenseTrue(
                        COURT_LOCATION, jurorNumber);

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
        public static final String URL = BASE_URL + "/{juror_number}/entered";

        public String toUrl(String locCode, String jurorNumber) {
            return URL
                .replace("{loc_code}", locCode)
                .replace("{juror_number}", jurorNumber);
        }

        private GetEnteredExpenseRequest buildRequest(LocalDate date) {
            return GetEnteredExpenseRequest.builder()
                .expenseDates(List.of(date))
                .build();
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            private ResponseEntity<List<GetEnteredExpenseResponse>> triggerValid(
                GetEnteredExpenseRequest request) {
                final String jwt = createJwt(COURT_USER, COURT_LOCATION);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);


                ResponseEntity<List<GetEnteredExpenseResponse>> response = template.exchange(
                    new RequestEntity<>(request, httpHeaders, POST,
                        URI.create(toUrl(COURT_LOCATION, JUROR_NUMBER))),
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

            @SuppressWarnings("PMD.ExcessiveParameterList")
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
            void positiveDraftExpense() {
                LocalDate dateOfExpense = LocalDate.of(2023, 1, 5);
                GetEnteredExpenseRequest request = buildRequest(dateOfExpense);

                ResponseEntity<List<GetEnteredExpenseResponse>> responseEntity = triggerValid(request);
                assertThat(responseEntity).as("Response Entity").isNotNull();
                assertThat(responseEntity.getBody()).isNotNull();
                List<GetEnteredExpenseResponse> responseList = responseEntity.getBody();
                assertThat(responseList).hasSize(1);
                GetEnteredExpenseResponse response = responseList.get(0);
                assertThat(response.getDateOfExpense()).isEqualTo(dateOfExpense);
                assertThat(response.getStage()).isEqualTo(AppearanceStage.EXPENSE_ENTERED);
                assertThat(response.getTotalDue()).isEqualTo(new BigDecimal("525.00"));
                assertThat(response.getTotalPaid()).isEqualTo(new BigDecimal("0.00"));
                assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);

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
                assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.BACS);

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
                assertThat(response.getPaymentMethod()).isEqualTo(PaymentMethod.BACS);

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
            private ResponseEntity<String> triggerInvalid(
                String jurorNumber,
                GetEnteredExpenseRequest request) {
                final String jwt = createJwt(COURT_USER, COURT_LOCATION);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(request, httpHeaders, POST, URI.create(
                        toUrl(COURT_LOCATION, jurorNumber)
                    )),
                    String.class);
            }

            @Test
            void invalidPayload() throws Exception {
                assertInvalidPathParam(triggerInvalid(
                        TestConstants.INVALID_JUROR_NUMBER,
                        GetEnteredExpenseRequest.builder()
                            .expenseDates(List.of(LocalDate.now()))
                            .build()),
                    "getEnteredExpenseDetails.jurorNumber: must match \"^\\d{9}$\"");
            }

            @Test
            void appearanceNotFound() {
                LocalDate dateOfExpense = LocalDate.of(2024, 1, 11);
                GetEnteredExpenseRequest request = buildRequest(dateOfExpense);
                assertNotFound(triggerInvalid(JUROR_NUMBER, request),
                    toUrl(COURT_LOCATION, JUROR_NUMBER), "No appearance record found for juror: "
                        + JUROR_NUMBER + " on day: 2024-01-11");
            }

            @Test
            void unauthorisedBureauUser() {
                LocalDate dateOfExpense = LocalDate.of(2024, 1, 11);
                GetEnteredExpenseRequest request = buildRequest(dateOfExpense);
                final String jwt = createJwtBureau(BUREAU_USER);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                assertForbiddenResponse(template.exchange(
                        new RequestEntity<>(request, httpHeaders, POST, URI.create(
                            toUrl(COURT_LOCATION, JUROR_NUMBER))), String.class),
                    toUrl(COURT_LOCATION, JUROR_NUMBER));
            }
        }
    }


    @Nested
    @DisplayName("POST /api/v1/moj/expenses/submit-for-approval")
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_submitForApprovalSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class SubmitForApproval {

        public static final String URL = BASE_URL + "/{juror_number}/submit-for-approval";

        public String toUrl(String locCode, String jurorNumber) {
            return URL
                .replace("{loc_code}", locCode)
                .replace("{juror_number}", jurorNumber);
        }

        @Test
        @DisplayName("Positive Typical")
        @SneakyThrows
        void positiveTypical() {
            final String jwt = createJwt(COURT_USER, COURT_LOCATION);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            DateDto payload = new DateDto();
            List<LocalDate> appearanceDates = List.of(LocalDate.of(2024, 1, 2),
                LocalDate.of(2024, 1, 3));
            payload.setDates(appearanceDates);

            RequestEntity<DateDto> request = new RequestEntity<>(payload, httpHeaders, POST,
                URI.create(toUrl(COURT_LOCATION, JUROR_NUMBER)));
            ResponseEntity<Void> response = template.exchange(request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            List<Appearance> appearances =
                transactionTemplate.execute(status -> appearanceRepository
                    .findAllByCourtLocationLocCodeAndJurorNumber(COURT_LOCATION, JUROR_NUMBER));


            List<Appearance> modifiedAppearances =
                appearances.stream().filter(app -> appearanceDates.contains(app.getAttendanceDate())).toList();
            assertThat(modifiedAppearances).hasSize(2);
            verifyExpenseSubmittedForApproval(modifiedAppearances.get(0), 2);
            verifyExpenseSubmittedForApproval(modifiedAppearances.get(1), 3);

            List<Appearance> unmodifiedAppearances =
                appearances.stream().filter(app -> !appearanceDates.contains(app.getAttendanceDate())).toList();
            assertThat(unmodifiedAppearances).hasSize(3);
            for (Appearance appearance : unmodifiedAppearances) {
                verifyExpenseStillInDraft(appearance);
            }

        }

        @Test
        @DisplayName("Positive Typical - Non Attendance")
        @SneakyThrows
        void positiveTypicalNonAttendance() {
            final String jwt = createJwt(COURT_USER, COURT_LOCATION);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            DateDto payload = new DateDto();
            List<LocalDate> appearanceDates = List.of(LocalDate.of(2024, 4, 9));
            payload.setDates(appearanceDates);

            RequestEntity<DateDto> request = new RequestEntity<>(payload, httpHeaders, POST,
                URI.create(toUrl(COURT_LOCATION, JUROR_NUMBER)));
            ResponseEntity<Void> response = template.exchange(request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            List<Appearance> appearances =
                transactionTemplate.execute(
                    status -> appearanceRepository.findAllByCourtLocationLocCodeAndJurorNumber(COURT_LOCATION,
                        JUROR_NUMBER));

            List<Appearance> modifiedAppearances =
                appearances.stream().filter(app -> appearanceDates.contains(app.getAttendanceDate())).toList();
            assertThat(modifiedAppearances).hasSize(1);
            verifyExpenseSubmittedForApproval(modifiedAppearances.get(0), 2);

            List<Appearance> unmodifiedAppearances =
                appearances.stream().filter(app -> !appearanceDates.contains(app.getAttendanceDate())).toList();
            assertThat(unmodifiedAppearances).hasSize(4);
            for (Appearance appearance : unmodifiedAppearances) {
                verifyExpenseStillInDraft(appearance);
            }
        }

        @Test
        @DisplayName("No appearance records found")
        @SneakyThrows
        void notFoundError() {
            final String jwt = createJwt(COURT_USER, COURT_LOCATION);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            DateDto payload = new DateDto();
            List<LocalDate> appearanceDates = List.of(LocalDate.of(2024, 1, 1));
            payload.setDates(appearanceDates);

            RequestEntity<DateDto> request = new RequestEntity<>(payload, httpHeaders, POST,
                URI.create(toUrl(COURT_LOCATION, JUROR_NUMBER)));
            ResponseEntity<Void> response = template.exchange(request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @DisplayName("Bad Request - Invalid juror number")
        @SneakyThrows
        void invalidJurorNumber() {
            final String jwt = createJwt(COURT_USER, COURT_LOCATION);


            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            DateDto payload = new DateDto();
            List<LocalDate> appearanceDates = List.of(LocalDate.of(2024, 1, 1));
            payload.setDates(appearanceDates);

            RequestEntity<DateDto> request = new RequestEntity<>(payload, httpHeaders, POST,
                URI.create(toUrl(COURT_LOCATION, "INVALID")));
            ResponseEntity<Void> response = template.exchange(request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Bad Request - Invalid Loc Code")
        @SneakyThrows
        void invalidLocCode() {
            final String jwt = createJwt(COURT_USER, "INVALID");

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            DateDto payload = new DateDto();
            List<LocalDate> appearanceDates = List.of(LocalDate.of(2024, 1, 1));
            payload.setDates(appearanceDates);

            RequestEntity<DateDto> request = new RequestEntity<>(payload, httpHeaders, POST,
                URI.create(toUrl("INVALID", JUROR_NUMBER)));
            ResponseEntity<Void> response = template.exchange(request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Business Validation Rule - INVALID_APPEARANCES_STATUS")
        @SneakyThrows
        void invalidAppearanceStatus() {
            final String jwt = createJwt(COURT_USER, COURT_LOCATION);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            DateDto payload = new DateDto();
            List<LocalDate> appearanceDates = List.of(LocalDate.of(2024, 1, 5));
            payload.setDates(appearanceDates);

            RequestEntity<DateDto> request = new RequestEntity<>(payload, httpHeaders, POST,
                URI.create(toUrl(COURT_LOCATION, JUROR_NUMBER)));
            ResponseEntity<String> response = template.exchange(request, String.class);

            assertBusinessRuleViolation(response,
                "All appearances must be in draft and have stage EXPENSE_ENTERED",
                MojException.BusinessRuleViolation.ErrorCode.INVALID_APPEARANCES_STATUS);

        }

        @Test
        @DisplayName("Bad Request - Empty attendance date list")
        @SneakyThrows
        void invalidAttendanceDates() {
            final String jwt = createJwt(COURT_USER, COURT_LOCATION);


            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            DateDto payload = new DateDto();
            payload.setDates(new ArrayList<>());

            RequestEntity<DateDto> request = new RequestEntity<>(payload, httpHeaders, POST,
                URI.create(toUrl(COURT_LOCATION, JUROR_NUMBER)));
            ResponseEntity<Void> response = template.exchange(request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("Forbidden Error - Invalid Bureau user")
        @SneakyThrows
        void bureauUser() {
            final String jwt = createJwtBureau(BUREAU_USER);

            httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);

            DateDto payload = new DateDto();
            List<LocalDate> appearanceDates = List.of(LocalDate.of(2024, 1, 2));
            payload.setDates(appearanceDates);

            RequestEntity<DateDto> request = new RequestEntity<>(payload, httpHeaders, POST,
                URI.create(toUrl("400", JUROR_NUMBER)));
            ResponseEntity<Void> response = template.exchange(request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        private void verifyExpenseSubmittedForApproval(Appearance appearance, long version) {
            assertThat(appearance.getFinancialAudit())
                .as("Financial Audit Details object should be created/associated")
                .isNotNull();
            FinancialAuditDetails financialAuditDetails =
                transactionTemplate.execute(status -> financialAuditDetailsRepository.findById(
                    new FinancialAuditDetails.IdClass(appearance.getFinancialAudit(), appearance.getLocCode())
                ).orElseThrow());

            assertThat(financialAuditDetails.getCreatedOn())
                .as("Financial Audit Details object should be submitted today")
                .isEqualToIgnoringHours(LocalDateTime.now(clock));
            assertThat(financialAuditDetails.getCreatedBy().getUsername())
                .as("Financial Audit Details object should be submitted by the current user")
                .isEqualToIgnoringCase("COURT_USER");
            assertThat(financialAuditDetails.getType())
                .as("Financial Audit Details object should be to type FOR_APPROVAL")
                .isEqualTo(FinancialAuditDetails.Type.FOR_APPROVAL);

            assertThat(financialAuditDetails.getCourtLocationRevision())
                .as("Financial Audit Details object should have the correct court revision")
                .isEqualTo(0);

            assertThat(financialAuditDetails.getJurorRevision())
                .as("Financial Audit Details object should have the correct juror revision")
                .isEqualTo(1);

            assertThat(appearance.getAppearanceStage())
                .as("Appearance stage should remain unchanged (still entered)")
                .isEqualTo(AppearanceStage.EXPENSE_ENTERED);

            assertThat(appearance.getVersion())
                .as("Appearance version should be incremented")
                .isEqualTo(version);
            assertThat(appearance.isDraftExpense())
                .as("Is draft expense flag should be updated to false (expense no longer draft)")
                .isFalse();
            assertThat(appearance.getExpenseRates()).isNotNull();
            assertThat(appearance.getExpenseRates().getId()).isEqualTo(999_999);
        }

        private void verifyExpenseStillInDraft(Appearance appearance) {
            assertThat(appearance.getFinancialAudit())
                .as("Financial Audit Details object should not be created/associated")
                .isNull();

            assertThat(appearance.getAppearanceStage())
                .as("Appearance stage should remain unchanged (still entered)")
                .isEqualTo(AppearanceStage.EXPENSE_ENTERED);
            if (appearance.getAttendanceDate().equals(LocalDate.of(2024, 1, 5))) {
                assertThat(appearance.isDraftExpense())
                    .as("Is draft expense flag should remain unchanged")
                    .isFalse();
            } else {
                assertThat(appearance.isDraftExpense())
                    .as("Is draft expense flag should remain unchanged (expense still in draft)")
                    .isTrue();
            }
        }
    }

    @Nested
    @DisplayName("GET " + GetEnteredExpenseDetails.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_simplifiedExpenseSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class GetSimplifiedExpenseDetails {
        public static final String URL = BASE_URL + "/{juror_number}/{type}/view/simplified";


        public String toUrl(String jurorNumber, ExpenseType expenseType) {
            return toUrl(jurorNumber, expenseType.name());
        }

        public String toUrl(String jurorNumber, String expenseType) {
            return toUrl(COURT_LOCATION, jurorNumber, expenseType);
        }

        public String toUrl(String locCode, String jurorNumber, String expenseType) {
            return URL
                .replace("{loc_code}", locCode)
                .replace("{juror_number}", jurorNumber)
                .replace("{type}", expenseType);
        }

        public URI toUri(String jurorNumber, String expenseType) {
            return URI.create(toUrl(jurorNumber, expenseType));
        }

        public URI toUri(String owner, String jurorNumber, String expenseType) {
            return URI.create(toUrl(owner, jurorNumber, expenseType));
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            protected ResponseEntity<CombinedSimplifiedExpenseDetailDto> triggerValid(
                String jurorNumber,
                ExpenseType expenseType) throws Exception {
                final String jwt = createJwt(COURT_USER, COURT_LOCATION);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<CombinedSimplifiedExpenseDetailDto> response = template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        toUri(jurorNumber, expenseType.name())),
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
                ResponseEntity<CombinedSimplifiedExpenseDetailDto> response = triggerValid(
                    JUROR_NUMBER,
                    ExpenseType.FOR_APPROVAL);

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
                ResponseEntity<CombinedSimplifiedExpenseDetailDto> response = triggerValid(
                    JUROR_NUMBER,
                    ExpenseType.APPROVED);

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
                ResponseEntity<CombinedSimplifiedExpenseDetailDto> response = triggerValid(
                    JUROR_NUMBER,
                    ExpenseType.FOR_REAPPROVAL);

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
                ResponseEntity<CombinedSimplifiedExpenseDetailDto> response = triggerValid(
                    JUROR_NUMBER_NO_APPEARANCES, ExpenseType.APPROVED);

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
                ResponseEntity<CombinedSimplifiedExpenseDetailDto> response =
                    triggerValid(JUROR_NUMBER_NO_APPEARANCES,
                        ExpenseType.FOR_APPROVAL);

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
                ResponseEntity<CombinedSimplifiedExpenseDetailDto> response = triggerValid(
                    JUROR_NUMBER_NO_APPEARANCES,
                    ExpenseType.FOR_REAPPROVAL);

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
                ResponseEntity<CombinedSimplifiedExpenseDetailDto> response = triggerValid(
                    "641500021",
                    ExpenseType.FOR_APPROVAL);

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
            protected ResponseEntity<String> triggerInvalid(
                String jurorNumber,
                String type,
                String owner) throws Exception {
                final String jwt = createJwt(COURT_USER, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        toUri(owner, jurorNumber, type)),
                    String.class);
            }

            @Test
            void forbiddenIsBureauUser() throws Exception {
                final String type = ExpenseType.FOR_APPROVAL.name();
                assertForbiddenResponse(triggerInvalid("641500021", type, "400"),
                    toUrl("400", "641500021", type));
            }
        }
    }


    @Nested
    @DisplayName("GET " + GetDraftExpenses.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_draftExpenseSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class GetDraftExpenses {
        public static final String URL = BASE_URL + "/{juror_number}/DRAFT/view";

        public String toUrl(String locCode, String jurorNumber) {
            return URL
                .replace("{loc_code}", locCode)
                .replace("{juror_number}", jurorNumber);
        }

        public URI toUri(String locCode, String jurorNumber) {
            return URI.create(toUrl(locCode, jurorNumber));
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            protected ResponseEntity<CombinedExpenseDetailsDto<ExpenseDetailsDto>> triggerValid(
                String locCode, String jurorNumber) throws Exception {
                final String jwt = createJwt(COURT_USER, locCode);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<CombinedExpenseDetailsDto<ExpenseDetailsDto>> response = template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        toUri(locCode, jurorNumber)),
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
                    triggerValid(COURT_LOCATION, JUROR_NUMBER);
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
                ExpenseTotal total = body.getTotal();
                assertThat(total).isEqualTo(
                    ExpenseTotal.builder()
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
                    triggerValid(COURT_LOCATION, JUROR_NUMBER_NO_APPEARANCES);
                CombinedExpenseDetailsDto<ExpenseDetailsDto> body = response.getBody();

                assertThat(body.getExpenseDetails()).hasSize(0);

                ExpenseTotal total = body.getTotal();
                assertThat(total).isEqualTo(
                    ExpenseTotal.builder()
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
            protected ResponseEntity<String> triggerInvalid(String locCode,
                                                            String jurorNumber,
                                                            String owner) throws Exception {
                final String jwt = createJwt(COURT_USER, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        toUri(locCode, jurorNumber)),
                    String.class);
            }

            @Test
            void canNotAccessJurorPool() throws Exception {
                assertForbiddenResponse(triggerInvalid("415", JUROR_NUMBER, "414"),
                    toUrl("415", JUROR_NUMBER));
            }

            @Test
            void isBureauUser() throws Exception {
                assertForbiddenResponse(triggerInvalid("400", JUROR_NUMBER, "400"),
                    toUrl("400", JUROR_NUMBER));
            }
        }
    }

    @Nested
    @DisplayName("GET (POST) " + GetExpenses.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_draftExpenseSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class GetExpenses {
        public static final String URL = BASE_URL + "/{juror_number}/view";

        public String toUrl(String locCode, String jurorNumber) {
            return URL
                .replace("{loc_code}", locCode)
                .replace("{juror_number}", jurorNumber);
        }

        public URI toUri(String locCode, String jurorNumber) {
            return URI.create(toUrl(locCode, jurorNumber));
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            protected ResponseEntity<CombinedExpenseDetailsDto<ExpenseDetailsDto>> triggerValid(
                String jurorNumber, List<LocalDate> payload) throws Exception {
                final String jwt = createJwt(COURT_USER, COURT_LOCATION);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<CombinedExpenseDetailsDto<ExpenseDetailsDto>> response = template.exchange(
                    new RequestEntity<>(payload, httpHeaders, POST,
                        toUri(COURT_LOCATION, jurorNumber)),
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
                    triggerValid(JUROR_NUMBER,
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
                ExpenseTotal total = body.getTotal();
                assertThat(total).isEqualTo(
                    ExpenseTotal.builder()
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
            protected ResponseEntity<String> triggerInvalid(String locCode,
                                                            String jurorNumber,
                                                            String owner,
                                                            List<LocalDate> payload) throws Exception {
                final String jwt = createJwt(COURT_USER, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(payload, httpHeaders, POST,
                        toUri(locCode, jurorNumber)),
                    String.class);
            }

            @Test
            void canNotAccessJurorPool() throws Exception {
                assertForbiddenResponse(triggerInvalid(COURT_LOCATION, JUROR_NUMBER, "414", List.of(
                        LocalDate.of(2023, 1, 5)
                    )),
                    toUrl(COURT_LOCATION, JUROR_NUMBER));
            }

            @Test
            void isBureauUser() throws Exception {
                assertForbiddenResponse(triggerInvalid("400", JUROR_NUMBER, "400", List.of(
                        LocalDate.of(2023, 1, 5)
                    )),
                    toUrl("400", JUROR_NUMBER));
            }

            @Test
            void oneOrMoreDatesNotFound() throws Exception {
                assertNotFound(triggerInvalid(COURT_LOCATION, JUROR_NUMBER, COURT_LOCATION, List.of(
                        LocalDate.of(2023, 1, 5),
                        LocalDate.of(2020, 1, 8))),
                    toUrl(COURT_LOCATION, JUROR_NUMBER),
                    "Not all dates found");
            }
        }
    }


    @Nested
    @DisplayName("POST " + ApproveExpenses.URL)
    @Sql({"/db/mod/truncate.sql",
        "/db/truncate.sql",
        "/db/JurorExpenseControllerITest_approveExpenseSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class ApproveExpenses {
        public static final String URL = BASE_URL + "/{payment_method}/approve";

        public String toUrl(String locCode, String paymentMethod) {
            return URL
                .replace("{loc_code}", locCode)
                .replace("{payment_method}", paymentMethod);
        }

        public String toUrl(String locCode, PaymentMethod paymentMethod) {
            return toUrl(locCode, paymentMethod.name());
        }

        @Nested
        class Positive {
            protected ResponseEntity<String> triggerValid(
                PaymentMethod paymentMethod,
                ApproveExpenseDto... expenseDto) throws Exception {
                final String jwt = createJwt(COURT_USER, Set.of(Role.MANAGER), COURT_LOCATION, COURT_LOCATION);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<String> response = template.exchange(
                    new RequestEntity<>(List.of(expenseDto), httpHeaders, POST,
                        URI.create(toUrl(COURT_LOCATION, paymentMethod))),
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
                                                             LocalDateTime createdOn, FinancialAuditDetails.Type
                                                                 type) {
                assertThat(financialAuditDetail.getJurorRevision()).isEqualTo(1L);
                assertThat(financialAuditDetail.getJurorNumber()).isEqualTo(JUROR_NUMBER);
                assertThat(financialAuditDetail.getCourtLocationRevision()).isEqualTo(6L);
                assertThat(financialAuditDetail.getLocCode()).isEqualTo(COURT_LOCATION);
                assertThat(financialAuditDetail.getType()).isEqualTo(type);
                assertThat(financialAuditDetail.getCreatedBy().getUsername()).isEqualTo("COURT_USER");
                assertThat(financialAuditDetail.getCreatedOn()).isEqualToIgnoringHours(createdOn);
                return financialAuditDetail.getId();
            }

            @Test
            void typicalApprovalBacs() throws Exception {
                ApproveExpenseDto approveExpenseDto = ApproveExpenseDto.builder()
                    .jurorNumber(JUROR_NUMBER)
                    .approvalType(ApproveExpenseDto.ApprovalType.FOR_APPROVAL)
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
                triggerValid(PaymentMethod.BACS, approveExpenseDto);

                List<FinancialAuditDetails> financialAuditDetails = new ArrayList<>();
                financialAuditDetailsRepository.findAll().forEach(financialAuditDetails::add);
                assertThat(financialAuditDetails).hasSize(1);

                List<FinancialAuditDetailsAppearances> financialAuditDetailsAppearances = new ArrayList<>();
                financialAuditDetailsAppearancesRepository.findAll().forEach(financialAuditDetailsAppearances::add);
                financialAuditDetailsAppearances.sort(
                    Comparator.comparing(FinancialAuditDetailsAppearances::getAttendanceDate));
                assertThat(financialAuditDetailsAppearances).hasSize(3);
                long id = assertFinancialAuditDetailsApproved(financialAuditDetails.get(0),
                    LocalDateTime.now(), FinancialAuditDetails.Type.APPROVED_BACS);
                assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearances.get(0),
                    id, LocalDate.of(2023, 1, 8), 2);
                assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearances.get(1),
                    id, LocalDate.of(2023, 1, 9), 4);
                assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearances.get(2),
                    id, LocalDate.of(2023, 1, 10), 3);

                assertApproved(
                    appearanceRepository.findByJurorNumberAndAttendanceDate(JUROR_NUMBER, LocalDate.of(2023, 1, 8)));
                assertApproved(
                    appearanceRepository.findByJurorNumberAndAttendanceDate(JUROR_NUMBER, LocalDate.of(2023, 1, 9)));
                assertApproved(
                    appearanceRepository.findByJurorNumberAndAttendanceDate(JUROR_NUMBER, LocalDate.of(2023, 1, 10)));
                assertJurorHistory(JUROR_NUMBER, HistoryCodeMod.ARAMIS_EXPENSES_FILE_CREATED, "COURT_USER",
                    "£1,683.98", null, LocalDate.of(2023, 1, 10), "F" + id);
                assertPaymentData(JUROR_NUMBER, new BigDecimal("1683.98"), new BigDecimal("702.98"),
                    new BigDecimal("225.00"), new BigDecimal("756.00"));

            }

            @Test
            void typicalApprovalCash() throws Exception {
                ApproveExpenseDto approveExpenseDto = ApproveExpenseDto.builder()
                    .jurorNumber(JUROR_NUMBER)
                    .approvalType(ApproveExpenseDto.ApprovalType.FOR_APPROVAL)
                    .dateToRevisions(
                        List.of(
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 2, 8))
                                .version(1L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 2, 9))
                                .version(1L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 2, 10))
                                .version(1L)
                                .build()
                        )
                    )
                    .build();
                triggerValid(PaymentMethod.CASH, approveExpenseDto);

                List<FinancialAuditDetails> financialAuditDetails = new ArrayList<>();
                financialAuditDetailsRepository.findAll().forEach(financialAuditDetails::add);
                assertThat(financialAuditDetails).hasSize(1);

                List<FinancialAuditDetailsAppearances> financialAuditDetailsAppearances = new ArrayList<>();
                financialAuditDetailsAppearancesRepository.findAll().forEach(financialAuditDetailsAppearances::add);
                financialAuditDetailsAppearances.sort(
                    Comparator.comparing(FinancialAuditDetailsAppearances::getAttendanceDate));
                assertThat(financialAuditDetailsAppearances).hasSize(3);
                long id = assertFinancialAuditDetailsApproved(financialAuditDetails.get(0),
                    LocalDateTime.now(), FinancialAuditDetails.Type.APPROVED_CASH);
                assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearances.get(0),
                    id, LocalDate.of(2023, 2, 8), 2);
                assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearances.get(1),
                    id, LocalDate.of(2023, 2, 9), 3);
                assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearances.get(2),
                    id, LocalDate.of(2023, 2, 10), 3);

                assertApproved(
                    appearanceRepository.findByJurorNumberAndAttendanceDate(JUROR_NUMBER, LocalDate.of(2023, 2, 8)));
                assertApproved(
                    appearanceRepository.findByJurorNumberAndAttendanceDate(JUROR_NUMBER, LocalDate.of(2023, 2, 9)));
                assertApproved(
                    appearanceRepository.findByJurorNumberAndAttendanceDate(JUROR_NUMBER, LocalDate.of(2023, 2, 10)));
                assertJurorHistory(JUROR_NUMBER, HistoryCodeMod.CASH_PAYMENT_APPROVAL, "COURT_USER", "£1,683.98",
                    null, LocalDate.of(2023, 2, 10), "F" + id);
                List<PaymentData> paymentDataList = paymentDataRepository.findByJurorNumber(JUROR_NUMBER);
                assertThat(paymentDataList).hasSize(0);
            }

            @Test
            @Sql({
                "/db/mod/truncate.sql",
                "/db/truncate.sql",
                "/db/JurorExpenseControllerITest_approveExpenseSetUp.sql",
                "/db/JurorExpenseControllerITest_expenseRates.sql",
                "/db/JurorExpenseControllerITest_ApproveExpensesSupport.sql"
            }
            )
            void typicalReApproved() throws Exception {
                ApproveExpenseDto approveExpenseDto = ApproveExpenseDto.builder()
                    .jurorNumber(JUROR_NUMBER)
                    .approvalType(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL)
                    .dateToRevisions(
                        List.of(
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 14))
                                .version(2L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 15))
                                .version(2L)
                                .build(),
                            ApproveExpenseDto.DateToRevision.builder()
                                .attendanceDate(LocalDate.of(2023, 1, 16))
                                .version(2L)
                                .build()
                        )
                    )
                    .build();
                triggerValid(PaymentMethod.BACS, approveExpenseDto);

                List<FinancialAuditDetails> financialAuditDetails = new ArrayList<>();
                financialAuditDetailsRepository.findAll().forEach(financialAuditDetails1 -> {
                    if (financialAuditDetails1.getId() == 12_344
                        || financialAuditDetails1.getId() == 12_345) {
                        return;
                    }
                    financialAuditDetails.add(financialAuditDetails1);
                });
                assertThat(financialAuditDetails).hasSize(1);


                List<FinancialAuditDetailsAppearances> financialAuditDetailsAppearances = new ArrayList<>();
                financialAuditDetailsAppearancesRepository.findAll()
                    .forEach(financialAuditDetailsAppearances1 -> {
                        if (financialAuditDetailsAppearances1.getFinancialAuditId() == 12_344
                            || financialAuditDetailsAppearances1.getFinancialAuditId() == 12_345) {
                            return;
                        }
                        financialAuditDetailsAppearances.add(financialAuditDetailsAppearances1);
                    });

                financialAuditDetailsAppearances.sort(
                    Comparator.comparing(FinancialAuditDetailsAppearances::getAttendanceDate));
                assertThat(financialAuditDetailsAppearances).hasSize(3);
                long id = assertFinancialAuditDetailsApproved(financialAuditDetails.get(0),
                    LocalDateTime.now(), FinancialAuditDetails.Type.REAPPROVED_BACS);
                assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearances.get(0),
                    id, LocalDate.of(2023, 1, 14), 4, 12_344L);
                assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearances.get(1),
                    id, LocalDate.of(2023, 1, 15), 4, 12_344L);
                assertFinancialAuditDetailsAppearances(financialAuditDetailsAppearances.get(2),
                    id, LocalDate.of(2023, 1, 16), 4, 12_344L);

                assertApproved(
                    appearanceRepository.findByJurorNumberAndAttendanceDate(JUROR_NUMBER,
                        LocalDate.of(2023, 1, 14)));
                assertApproved(
                    appearanceRepository.findByJurorNumberAndAttendanceDate(JUROR_NUMBER,
                        LocalDate.of(2023, 1, 15)));
                assertApproved(
                    appearanceRepository.findByJurorNumberAndAttendanceDate(JUROR_NUMBER,
                        LocalDate.of(2023, 1, 16)));

                assertJurorHistory(JUROR_NUMBER, HistoryCodeMod.ARAMIS_EXPENSES_FILE_CREATED, "COURT_USER",
                    "£407.00",
                    null, LocalDate.of(2023, 1, 16), "F" + id);
                assertPaymentData(JUROR_NUMBER, new BigDecimal("407.00"), new BigDecimal("260.00"),
                    new BigDecimal("57.00"), new BigDecimal("90.00"));

            }

            private void assertApproved(Optional<Appearance> appearanceOpt) {
                assertThat(appearanceOpt.isPresent()).isTrue();
                Appearance appearance = appearanceOpt.get();
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
                                                            String locCode,
                                                            String paymentMethod,
                                                            ApproveExpenseDto... expenseDto) throws Exception {
                return triggerInvalid(owner, locCode, paymentMethod,
                    COURT_USER, "400".equals(owner) ? UserType.BUREAU : UserType.COURT,
                    Set.of(Role.MANAGER), expenseDto);
            }

            protected ResponseEntity<String> triggerInvalid(String owner,
                                                            String locCode,
                                                            String paymentMethod,
                                                            String username,
                                                            UserType userType, Set<Role> roles,
                                                            ApproveExpenseDto... expenseDto) throws Exception {
                final String jwt = createJwt(username, owner, userType, roles, owner);


                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(List.of(expenseDto), httpHeaders, POST,
                        URI.create(toUrl(locCode, paymentMethod))),
                    String.class);
            }

            @Test
            void negativeAppearancesNotFound() throws Exception {
                final String jurorNumber = "641500021";
                assertNotFound(triggerInvalid(COURT_LOCATION, COURT_LOCATION,
                        PaymentMethod.CASH.name(),
                        ApproveExpenseDto.builder()
                            .jurorNumber(jurorNumber)
                            .approvalType(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL)
                            .dateToRevisions(
                                List.of(
                                    ApproveExpenseDto.DateToRevision.builder()
                                        .attendanceDate(LocalDate.of(2023, 1, 14))
                                        .version(1L)
                                        .build()
                                )
                            ).build()), toUrl(COURT_LOCATION, PaymentMethod.CASH),
                    "No appearance records found for Loc code: 415, Juror Number: 641500021 and approval type "
                        + "FOR_REAPPROVAL");
            }

            @Test
            void negativeOutOfDataData() throws Exception {
                assertBusinessRuleViolation(triggerInvalid(COURT_LOCATION,
                        COURT_LOCATION, PaymentMethod.BACS.name(),
                        ApproveExpenseDto.builder()
                            .jurorNumber(JUROR_NUMBER)
                            .approvalType(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL)
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
                assertBusinessRuleViolation(triggerInvalid(COURT_LOCATION,
                        COURT_LOCATION,
                        PaymentMethod.BACS.name(),
                        ApproveExpenseDto.builder()
                            .jurorNumber(JUROR_NUMBER)
                            .approvalType(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL)
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
                    "INSERT INTO juror_mod.financial_audit_details ("
                        + "id, juror_revision, court_location_revision, type, created_by, created_on, "
                        + "juror_number, loc_code) VALUES ("
                        + "12345, 1, 6, 'FOR_APPROVAL', 'COURT_USER', '2023-01-01 00:00:00','641500020','415')",
                    "INSERT INTO juror_mod.financial_audit_details_appearances ("
                        + "financial_audit_id, attendance_date,appearance_version, loc_code) VALUES "
                        + "(12345, '2023-01-14', 2,'415')"
                })
            void negativeUserCanNotApprove() throws Exception {
                assertBusinessRuleViolation(triggerInvalid(COURT_LOCATION,
                        COURT_LOCATION,
                        PaymentMethod.BACS.name(),
                        ApproveExpenseDto.builder()
                            .jurorNumber(JUROR_NUMBER)
                            .approvalType(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL)
                            .dateToRevisions(
                                List.of(
                                    ApproveExpenseDto.DateToRevision.builder()
                                        .attendanceDate(LocalDate.of(2023, 1, 14))
                                        .version(2L)
                                        .build(),
                                    ApproveExpenseDto.DateToRevision.builder()
                                        .attendanceDate(LocalDate.of(2023, 1, 15))
                                        .version(2L)
                                        .build(),
                                    ApproveExpenseDto.DateToRevision.builder()
                                        .attendanceDate(LocalDate.of(2023, 1, 16))
                                        .version(2L)
                                        .build()
                                )
                            ).build()), "User cannot approve an expense they have edited",
                    MojException.BusinessRuleViolation.ErrorCode.CAN_NOT_APPROVE_OWN_EDIT);
            }

            @Test
            void negativeCanNotApproveMoreThan() throws Exception {
                assertBusinessRuleViolation(triggerInvalid(COURT_LOCATION,
                        COURT_LOCATION,
                        PaymentMethod.BACS.name(),
                        "COURT_USER2",
                        UserType.COURT, Set.of(Role.MANAGER),
                        ApproveExpenseDto.builder()
                            .jurorNumber(JUROR_NUMBER)
                            .approvalType(ApproveExpenseDto.ApprovalType.FOR_APPROVAL)
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
                    triggerInvalid("400",
                        "400",
                        PaymentMethod.BACS.name(),
                        ApproveExpenseDto.builder()
                            .jurorNumber(JUROR_NUMBER)
                            .approvalType(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL)
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
                            .build()), toUrl("400", PaymentMethod.BACS));
            }

            @Test
            void negativeUnauthorizedNotManager() throws Exception {
                assertForbiddenResponse(
                    triggerInvalid(COURT_LOCATION,
                        COURT_LOCATION,
                        PaymentMethod.CASH.name(),
                        COURT_USER, UserType.COURT, Set.of(),
                        ApproveExpenseDto.builder()
                            .jurorNumber(JUROR_NUMBER)
                            .approvalType(ApproveExpenseDto.ApprovalType.FOR_REAPPROVAL)
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
                            .build()), toUrl(COURT_LOCATION, PaymentMethod.CASH));
            }
        }
    }

    @Nested
    @DisplayName("POST " + PostEditAttendedDayDailyExpense.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_editExpenseSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class PostEditAttendedDayDailyExpense extends AbstractDraftDailyExpense {

        public static final String URL = BASE_URL + "/{juror_number}/{type}/edit";


        protected String toUrl(String jurorNumber, ExpenseType type) {
            return toUrl(jurorNumber, type.name());
        }

        @Override
        public String toUrl(String jurorNumber, String type) {
            return URL
                .replace("{loc_code}", COURT_LOCATION)
                .replace("{juror_number}", jurorNumber)
                .replace("{type}", type);
        }

        @DisplayName("Negative")
        @Nested
        class Negative {

            protected ResponseEntity<String> triggerInvalid(String jurorNumber,
                                                            String expenseType,
                                                            DailyExpense... request) throws Exception {
                final String jwt = createJwt(COURT_USER, COURT_LOCATION);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(request, httpHeaders, PUT,
                        URI.create(toUrl(jurorNumber, expenseType))),
                    String.class);
            }

            @Test
            void invalidJurorNumber() throws Exception {
                final String jurorNumber = "INVALID";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 5))
                    .paymentMethod(PaymentMethod.BACS)
                    .time(DailyExpenseTime.builder()
                        .payAttendance(PayAttendanceType.FULL_DAY)
                        .build())
                    .financialLoss(
                        createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc")
                    )
                    .build();

                ResponseEntity<String> response = triggerInvalid(jurorNumber, ExpenseType.APPROVED.name(), request);
                assertInvalidPathParam(response,
                    "postEditDailyExpense.jurorNumber: must match \"^\\d{9}$\"");
            }

            @Test
            void invalidExpenseType() throws Exception {
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 5))
                    .paymentMethod(PaymentMethod.BACS)
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
                    .paymentMethod(PaymentMethod.BACS)
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
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 8))
                    .paymentMethod(PaymentMethod.BACS)
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
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 11))
                    .paymentMethod(PaymentMethod.BACS)
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
                final String jwt = createJwt(COURT_USER, COURT_LOCATION);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<Void> response = template.exchange(
                    new RequestEntity<>(request, httpHeaders, PUT,
                        URI.create(toUrl(jurorNumber, expenseType))),
                    Void.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                return response;
            }

            @Test
            void typicalForApproved() throws Exception {
                final String jurorNumber = "641500020";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 8))
                    .paymentMethod(PaymentMethod.BACS)
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
                    .paymentMethod(PaymentMethod.BACS)
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

                Appearance appearance = getAppearance(jurorNumber, request.getDateOfExpense(), COURT_LOCATION);
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

                Appearance appearance2 = getAppearance(jurorNumber, request2.getDateOfExpense(), COURT_LOCATION);

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
                    "415230101", LocalDate.of(2023, 1, 8), "F2", 2, 0);
                assertJurorHistory(jurorNumber, HistoryCodeMod.EDIT_PAYMENTS, "COURT_USER", "£61.70",
                    "415230101", LocalDate.of(2023, 1, 9), "F2", 2, 1);


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
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 11))
                    .paymentMethod(PaymentMethod.BACS)
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

                Appearance appearance = getAppearance(jurorNumber, request.getDateOfExpense(), COURT_LOCATION);
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
                    "415230101", LocalDate.of(2023, 1, 11), "F2", 1, 0);
            }

            @Test
            void typicalReApproved() throws Exception {
                final String jurorNumber = "641500020";
                DailyExpense request = DailyExpense.builder()
                    .dateOfExpense(LocalDate.of(2023, 1, 15))
                    .paymentMethod(PaymentMethod.BACS)
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

                Appearance appearance = getAppearance(jurorNumber, request.getDateOfExpense(), COURT_LOCATION);
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
                    "415230101", LocalDate.of(2023, 1, 15), "F2", 1, 0);
            }
        }
    }

    @Nested
    @DisplayName("POST " + CalculateTotals.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_calculateTotalExpenseSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class CalculateTotals extends AbstractDraftDailyExpense {
        public static final String URL = BASE_URL + "/{juror_number}/calculate/totals";

        private static final String JUROR_NUMBER = "641500020";

        public String toUrl(String jurorNumber) {
            return toUrl(COURT_LOCATION, jurorNumber);
        }

        @Override
        public String toUrl(String locCode, String jurorNumber) {
            return URL
                .replace("{loc_code}", locCode)
                .replace("{juror_number}", jurorNumber);
        }

        @Nested
        class Positive {

            protected CombinedExpenseDetailsDto<ExpenseDetailsForTotals> triggerValid(
                String jurorNumber,
                CalculateTotalExpenseRequestDto request) throws Exception {
                final String jwt = createJwt(COURT_USER, COURT_LOCATION);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<CombinedExpenseDetailsDto<ExpenseDetailsForTotals>> response = template.exchange(
                    new RequestEntity<>(request, httpHeaders, POST,
                        URI.create(toUrl(jurorNumber))),
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
                    .expenseList(List.of(
                        DailyExpense.builder()
                            .dateOfExpense(LocalDate.of(2023, 1, 5))
                            .paymentMethod(PaymentMethod.BACS)
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
                            .paymentMethod(PaymentMethod.BACS)
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

                CombinedExpenseDetailsDto<ExpenseDetailsForTotals> response = triggerValid(JUROR_NUMBER, request);
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
                    ExpenseTotal.builder()
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
                        .totalPaid(new BigDecimal("8.00"))
                        .totalDue(new BigDecimal("121.34000"))
                        .build());
            }

            @Test
            void typicalNonAttendanceDay() throws Exception {
                CalculateTotalExpenseRequestDto request = CalculateTotalExpenseRequestDto.builder()
                    .expenseList(List.of(
                        DailyExpense.builder()
                            .dateOfExpense(LocalDate.of(2023, 1, 17))
                            .paymentMethod(PaymentMethod.BACS)
                            .time(DailyExpenseTime.builder()
                                .payAttendance(PayAttendanceType.FULL_DAY)
                                .build())
                            .financialLoss(
                                createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc")
                            )
                            .build()
                    ))
                    .build();

                CombinedExpenseDetailsDto<ExpenseDetailsForTotals> response = triggerValid(JUROR_NUMBER, request);
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
                    ExpenseTotal.builder()
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
                        .totalPaid(new BigDecimal("8.00"))
                        .totalDue(new BigDecimal("33.01"))
                        .build());
            }

            @Test
            void typicalFinancialLossApportioned() throws Exception {
                CalculateTotalExpenseRequestDto request = CalculateTotalExpenseRequestDto.builder()
                    .expenseList(List.of(
                        DailyExpense.builder()
                            .dateOfExpense(LocalDate.of(2023, 1, 5))
                            .paymentMethod(PaymentMethod.BACS)
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

                CombinedExpenseDetailsDto<ExpenseDetailsForTotals> response = triggerValid(JUROR_NUMBER, request);
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
                    ExpenseTotal.builder()
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
                        .totalPaid(new BigDecimal("0.00"))
                        .totalDue(new BigDecimal("70.28000"))
                        .build());
            }

            @Test
            void typicalWithDbData() throws Exception {
                CalculateTotalExpenseRequestDto request = CalculateTotalExpenseRequestDto.builder()
                    .expenseList(List.of(
                        DailyExpense.builder()
                            .dateOfExpense(LocalDate.of(2023, 1, 5))
                            .build(),
                        DailyExpense.builder()
                            .dateOfExpense(LocalDate.of(2023, 1, 11))
                            .paymentMethod(PaymentMethod.BACS)
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

                CombinedExpenseDetailsDto<ExpenseDetailsForTotals> response = triggerValid(JUROR_NUMBER, request);
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
                ExpenseTotal total = response.getTotal();
                assertThat(total).isEqualTo(
                    ExpenseTotal.builder()
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
                                                            String locCode,
                                                            String jurorNumber,
                                                            CalculateTotalExpenseRequestDto request) throws
                Exception {
                final String jwt = createJwt(COURT_USER, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(request, httpHeaders, POST,
                        URI.create(toUrl(locCode, jurorNumber))),
                    String.class);
            }

            @Test
            void badPayload() throws Exception {
                assertInvalidPathParam(triggerInvalid(COURT_LOCATION, COURT_LOCATION, JUROR_NUMBER,
                        CalculateTotalExpenseRequestDto.builder()
                            .expenseList(List.of(
                                DailyExpense.builder()
                                    .dateOfExpense(LocalDate.of(2023, 1, 17))
                                    .paymentMethod(PaymentMethod.BACS)
                                    .time(DailyExpenseTime.builder()
                                        .payAttendance(PayAttendanceType.FULL_DAY)
                                        .build())
                                    .financialLoss(
                                        createDailyExpenseFinancialLoss(-0.1, 10.00, 5.00, "Desc")
                                    )
                                    .build()
                            ))
                            .build()),
                    "calculateTotals.dto.expenseList[0].financialLoss.lossOfEarningsOrBenefits: must be greater than "
                        + "or equal to 0");
            }

            @Test
            void forbiddenIsBureauUser() throws Exception {
                assertForbiddenResponse(triggerInvalid("400", "400", JUROR_NUMBER,
                    CalculateTotalExpenseRequestDto.builder()
                        .expenseList(List.of(
                            DailyExpense.builder()
                                .dateOfExpense(LocalDate.of(2023, 1, 17))
                                .paymentMethod(PaymentMethod.BACS)
                                .time(DailyExpenseTime.builder()
                                    .payAttendance(PayAttendanceType.FULL_DAY)
                                    .build())
                                .financialLoss(
                                    createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc")
                                )
                                .build()
                        ))
                        .build()), toUrl("400", JUROR_NUMBER));
            }

            @Test
            void expenseNotFound() throws Exception {
                assertNotFound(triggerInvalid(COURT_LOCATION, COURT_LOCATION, JUROR_NUMBER,
                        CalculateTotalExpenseRequestDto.builder()
                            .expenseList(List.of(
                                DailyExpense.builder()
                                    .dateOfExpense(LocalDate.of(2024, 1, 17))
                                    .paymentMethod(PaymentMethod.BACS)
                                    .time(DailyExpenseTime.builder()
                                        .payAttendance(PayAttendanceType.FULL_DAY)
                                        .build())
                                    .financialLoss(
                                        createDailyExpenseFinancialLoss(25.01, 10.00, 5.00, "Desc")
                                    )
                                    .build()
                            ))
                            .build()), toUrl(COURT_LOCATION, JUROR_NUMBER),
                    "No appearance record found for juror: 641500020 on day: 2024-01-17");
            }

            @Test
            @SuppressWarnings("LineLength")
            void expensesLessThanZero() throws Exception {
                assertBusinessRuleViolation(triggerInvalid(COURT_LOCATION, COURT_LOCATION, JUROR_NUMBER,
                        CalculateTotalExpenseRequestDto.builder()
                            .expenseList(List.of(
                                DailyExpense.builder()
                                    .dateOfExpense(LocalDate.of(2023, 1, 5))
                                    .paymentMethod(PaymentMethod.BACS)
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
                assertBusinessRuleViolation(triggerInvalid(COURT_LOCATION, COURT_LOCATION, JUROR_NUMBER,
                        CalculateTotalExpenseRequestDto.builder()
                            .expenseList(List.of(
                                DailyExpense.builder()
                                    .dateOfExpense(LocalDate.of(2023, 1, 11))
                                    .paymentMethod(PaymentMethod.BACS)
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

        public static final String URL = BASE_URL + "/{juror_number}/counts";

        private String toUrl(String locCode, String jurorNumber) {
            return URL
                .replace("{loc_code}", locCode)
                .replace("{juror_number}", jurorNumber);
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            protected ExpenseCount triggerValid(String locCode,
                                                String jurorNumber) throws Exception {
                final String jwt = createJwt(COURT_USER, COURT_LOCATION);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<ExpenseCount> response = template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(toUrl(locCode, jurorNumber))),
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
                ExpenseCount expenseCount = triggerValid(COURT_LOCATION, "641500020");
                assertThat(expenseCount).isEqualTo(ExpenseCount.builder()
                    .totalDraft(3)
                    .totalForApproval(3)
                    .totalApproved(5)
                    .totalForReapproval(1)
                    .build());
            }


            @Test
            void notAppearances() throws Exception {
                ExpenseCount expenseCount = triggerValid(COURT_LOCATION, "641500029");
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
            protected ResponseEntity<String> triggerInvalid(String locCode,
                                                            String jurorNumber,
                                                            String owner) throws Exception {
                final String jwt = createJwt(COURT_USER, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(toUrl(locCode, jurorNumber))),
                    String.class);
            }

            @Test
            void canNotAccessJurorPool() throws Exception {
                assertForbiddenResponse(triggerInvalid(COURT_LOCATION, "641500020", "414"),
                    toUrl(COURT_LOCATION, "641500020"));
            }

            @Test
            void isBureauUser() throws Exception {
                assertForbiddenResponse(triggerInvalid("400", "641500020", "400"),
                    toUrl("400", "641500020"));
            }

            @Test
            void invalidJurorNumber() throws Exception {
                assertInvalidPathParam(triggerInvalid(COURT_LOCATION, "INVALID", COURT_LOCATION),
                    "getCounts.jurorNumber: must match \"^\\d{9}$\"");
            }

            @Test
            void invalidLocCode() throws Exception {
                assertInvalidPathParam(triggerInvalid("INVALID", "641500020", "INVALID"),
                    "getCounts.locCode: must match \"^\\d{3}$\"");
            }
        }
    }

    @Nested
    @DisplayName("GET " + GetExpensesForApproval.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_getApproveExpenseSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class GetExpensesForApproval {
        public static final String URL = BASE_URL + "/{payment_method}/pending-approval";

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
            StringBuilder builder = new StringBuilder(URL.replace("{loc_code}", locCode)
                .replace("{payment_method}", paymentMethod));

            if (from != null) {
                builder.append("?from=")
                    .append(from);
            }
            if (to != null) {
                builder.append(from != null ? '&' : '?')
                    .append("to=")
                    .append(to);
            }
            return builder.toString();
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
                final String jwt = createJwt(username, locCode);
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
            @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
            void typicalCash() throws Exception {
                PendingApprovalList pendingApprovals = triggerValid(COURT_LOCATION, null, null, PaymentMethod.CASH);
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
                PendingApprovalList pendingApprovals = triggerValid(COURT_LOCATION, null, null, PaymentMethod.BACS);
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
                    triggerValid(COURT_LOCATION, LocalDate.of(2023, 1, 14), null, PaymentMethod.BACS);
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
            @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
            void typicalToDateFilter() throws Exception {
                PendingApprovalList pendingApprovals =
                    triggerValid(COURT_LOCATION, null, LocalDate.of(2023, 1, 9), PaymentMethod.BACS);
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
            @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
            void typicalBothFromAndToFilter() throws Exception {
                PendingApprovalList pendingApprovals =
                    triggerValid(COURT_LOCATION, LocalDate.of(2023, 1, 9), LocalDate.of(2023, 1, 10), PaymentMethod
                        .BACS);
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
            @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")
            void canNotApprove() throws Exception {
                PendingApprovalList pendingApprovals =
                    triggerValid("COURT_USER2", COURT_LOCATION, LocalDate.of(2023, 1, 9), LocalDate.of(2023, 1, 10),
                        PaymentMethod.BACS);
                assertThat(pendingApprovals.getTotalPendingCash()).as("Total Pending Cash").isEqualTo(2L);
                assertThat(pendingApprovals.getTotalPendingBacs()).as("Total Pending Bacs").isEqualTo(3L);

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
                final String jwt = createJwt(COURT_USER, owner);
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
                assertForbiddenResponse(triggerInvalid(COURT_LOCATION, "414", null, null, PaymentMethod.BACS.name()),
                    toUrl("414", PaymentMethod.BACS.name(), null, null));
            }
        }
    }

    @Nested
    @DisplayName("PATCH " + ApportionSmartCard.URL)
    @Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_draftExpenseSetUp.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class ApportionSmartCard {
        public static final String URL = BASE_URL + "/{juror_number}/smartcard";

        private static final String JUROR_NUMBER = "641500020";


        private static final List<LocalDate> ATTENDANCE_DATES = List.of(
            LocalDate.of(2023, 1, 5),
            LocalDate.of(2023, 1, 6),
            LocalDate.of(2023, 1, 7)
        );

        public String toUrl(String locCode, String jurorNumber) {
            return URL
                .replace("{loc_code}", locCode)
                .replace("{juror_number}", jurorNumber);
        }

        protected ApportionSmartCardRequest getValidPayload(BigDecimal smartCardAmount) {
            return ApportionSmartCardRequest.builder()
                .smartCardAmount(smartCardAmount)
                .dates(ATTENDANCE_DATES)
                .build();
        }

        @Nested
        @DisplayName("Positive")
        class Positive {
            protected ResponseEntity<Void> triggerValid(
                String jurorNumber,
                ApportionSmartCardRequest request) throws Exception {
                final String jwt = createJwt(COURT_USER, COURT_LOCATION);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<Void> response = template.exchange(
                    new RequestEntity<>(request, httpHeaders, PATCH,
                        URI.create(toUrl(COURT_LOCATION, jurorNumber))),
                    Void.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.ACCEPTED);
                return response;
            }

            private void assertAppearance(LocalDate localDate,
                                          BigDecimal expectedSmartCardAmount) {
                Appearance appearance = appearanceRepository
                    .findByCourtLocationLocCodeAndJurorNumberAndAttendanceDate(
                        COURT_LOCATION, ApportionSmartCard.JUROR_NUMBER, localDate).orElseThrow();
                assertThat(appearance.getSmartCardAmountDue()).isEqualTo(expectedSmartCardAmount);
            }

            @Test
            void typical() throws Exception {
                triggerValid(JUROR_NUMBER, getValidPayload(new BigDecimal("90.00")));

                assertAppearance(ATTENDANCE_DATES.get(0),
                    new BigDecimal("30.00"));
                assertAppearance(ATTENDANCE_DATES.get(1),
                    new BigDecimal("30.00"));
                assertAppearance(ATTENDANCE_DATES.get(2),
                    new BigDecimal("30.00"));
            }


            @Test
            void typicalWithRoundingErrorsAccountedFor() throws Exception {
                triggerValid(JUROR_NUMBER, getValidPayload(new BigDecimal("100.00")));

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
            protected ResponseEntity<String> triggerInvalid(
                String locCode,
                String jurorNumber,
                ApportionSmartCardRequest request,
                String owner) throws Exception {
                final String jwt = createJwt(COURT_USER, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(request, httpHeaders, PATCH,
                        URI.create(toUrl(locCode, jurorNumber))),
                    String.class);
            }

            @Test
            void invalidPayload() throws Exception {
                assertInvalidPayload(triggerInvalid(
                        COURT_LOCATION, JUROR_NUMBER, getValidPayload(new BigDecimal("-0.01")), COURT_LOCATION),
                    new RestResponseEntityExceptionHandler.FieldError("smartCardAmount",
                        "must be greater than or equal to 0"));
            }

            @Test
            void forbiddenIsBureauUser() throws Exception {
                assertForbiddenResponse(triggerInvalid(
                        COURT_LOCATION, JUROR_NUMBER, getValidPayload(new BigDecimal("90.00")), "400"),
                    toUrl(COURT_LOCATION, JUROR_NUMBER));
            }

            @Test
            void forbiddenNotPartOfPoolCourt() throws Exception {
                assertForbiddenResponse(triggerInvalid(
                        COURT_LOCATION, JUROR_NUMBER, getValidPayload(new BigDecimal("90.00")), "414"),
                    toUrl(COURT_LOCATION, JUROR_NUMBER));
            }

            @Test
            void jurorNotFound() throws Exception {
                assertNotFound(triggerInvalid(COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER,
                        getValidPayload(new BigDecimal("90.00")), COURT_LOCATION),
                    toUrl(COURT_LOCATION, TestConstants.VALID_JUROR_NUMBER),
                    "One or more appearance records not found for Loc code: 415, "
                        + "Juror Number: 123456789 and Attendance Dates provided");
            }

            @Test
            void negativeExpenseAmount() throws Exception {
                assertBusinessRuleViolation(triggerInvalid(COURT_LOCATION, JUROR_NUMBER,
                        getValidPayload(new BigDecimal("900000.00")),
                        COURT_LOCATION),
                    "Total expenses cannot be less than £0. For Day 2023-01-05",
                    MojException.BusinessRuleViolation.ErrorCode.EXPENSES_CANNOT_BE_LESS_THAN_ZERO);
            }

            @Test
            void negativeNonDraftDays() throws Exception {
                ApportionSmartCardRequest payload = getValidPayload(new BigDecimal("90.00"));
                payload.setDates(List.of(LocalDate.of(2023, 1, 11)));
                assertBusinessRuleViolation(triggerInvalid(
                        COURT_LOCATION, JUROR_NUMBER, payload, COURT_LOCATION),
                    "Can not apportion smart card for non-draft days",
                    MojException.BusinessRuleViolation.ErrorCode.APPORTION_SMART_CARD_NON_DRAFT_DAYS);
            }
        }
    }

}
