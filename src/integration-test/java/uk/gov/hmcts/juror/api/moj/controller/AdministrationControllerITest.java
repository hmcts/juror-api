package uk.gov.hmcts.juror.api.moj.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtRates;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.CodeDescriptionResponse;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.CourtDetailsReduced;
import uk.gov.hmcts.juror.api.moj.domain.Address;
import uk.gov.hmcts.juror.api.moj.domain.CodeType;
import uk.gov.hmcts.juror.api.moj.domain.CourtDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.ExpenseRates;
import uk.gov.hmcts.juror.api.moj.domain.ExpenseRatesDto;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UpdateCourtDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.enumeration.CourtType;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.ExpenseRatesRepository;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.util.AssertionErrors.fail;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + AdministrationControllerITest.BASE_URL)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings("PMD.ExcessiveImports")
public class AdministrationControllerITest extends AbstractIntegrationTest {
    public static final String BASE_URL = "/api/v1/moj/administration";

    private HttpHeaders httpHeaders;
    private final TestRestTemplate template;

    private final CourtLocationRepository courtLocationRepository;

    private final ExpenseRatesRepository expenseRatesRepository;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Nested
    @DisplayName("GET  " + ViewCodeAndDescriptions.URL)
    class ViewCodeAndDescriptions {
        public static final String URL = BASE_URL + "/codes/{code_type}";

        private String toUrl(CodeType codeType) {
            return toUrl(codeType.name());
        }

        private String toUrl(String codeType) {
            return URL.replace("{code_type}", codeType);
        }

        @Nested
        @DisplayName("Positive")
        class Positive {

            void assertValid(CodeType codeType, CodeDescriptionResponse... expectedCodes) {
                final String jwt = createBureauJwt(COURT_USER, "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<List<CodeDescriptionResponse>> response = template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(toUrl(codeType))),
                    new ParameterizedTypeReference<>() {
                    });
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();

                assertThat(response.getBody()).containsExactly(expectedCodes);
            }

            @Test
            @DisplayName("Disqualification Codes")
            void disqualifiedCodes() {
                assertValid(CodeType.DISQUALIFIED,
                    new CodeDescriptionResponse("A", "Less Than Eighteen Years of Age or Over 75", null),
                    new CodeDescriptionResponse("B", "On Bail", null),
                    new CodeDescriptionResponse("C", "Has Been Convicted of an Offence", null),
                    new CodeDescriptionResponse("D", "JUDICIAL DISQUALIFICATION", null),
                    new CodeDescriptionResponse("E", "Electronic Police Check Failure", null),
                    new CodeDescriptionResponse("M", "Suffering From a Mental Disorder", null),
                    new CodeDescriptionResponse("R", "Not Resident for the Appropriate Period", null));
            }

            @Test
            @DisplayName("Juror Status")
            void jurorStatusCodes() {
                assertValid(CodeType.JUROR_STATUS,
                    new CodeDescriptionResponse("0", "Pool", true),
                    new CodeDescriptionResponse("1", "Summoned", true),
                    new CodeDescriptionResponse("2", "Responded", true),
                    new CodeDescriptionResponse("3", "Panel", true),
                    new CodeDescriptionResponse("4", "Juror", true),
                    new CodeDescriptionResponse("5", "Excused", true),
                    new CodeDescriptionResponse("6", "Disqualified", true),
                    new CodeDescriptionResponse("7", "Deferred", true),
                    new CodeDescriptionResponse("8", "Reassigned", true),
                    new CodeDescriptionResponse("9", "Undeliverable", true),
                    new CodeDescriptionResponse("10", "Transferred", true),
                    new CodeDescriptionResponse("11", "Awaiting Info", true),
                    new CodeDescriptionResponse("12", "FailedToAttend", true),
                    new CodeDescriptionResponse("13", "Completed", true));
            }

            @Test
            @DisplayName("Trial Type")
            void trailTypeCodes() {
                assertValid(CodeType.TRIAL_TYPE,
                    new CodeDescriptionResponse("CIV", "Civil", null),
                    new CodeDescriptionResponse("CRI", "Criminal", null));
            }

            @Test
            @DisplayName("ID Check codes")
            void idCheckCodes() {
                assertValid(CodeType.ID_CHECK,
                    new CodeDescriptionResponse("A", "Bank Statement", null),
                    new CodeDescriptionResponse("B", "Birth Certificate", null),
                    new CodeDescriptionResponse("C", "Credit Card", null),
                    new CodeDescriptionResponse("D", "Drivers Licence", null),
                    new CodeDescriptionResponse("E", "EU Nat ID Card", null),
                    new CodeDescriptionResponse("F", "Bus Pass", null),
                    new CodeDescriptionResponse("H", "Home Office Doc", null),
                    new CodeDescriptionResponse("I", "Company ID", null),
                    new CodeDescriptionResponse("L", "Cheque Bk, Crd 3Stts", null),
                    new CodeDescriptionResponse("M", "Medical Card", null),
                    new CodeDescriptionResponse("N", "None", null),
                    new CodeDescriptionResponse("O", "Other", null),
                    new CodeDescriptionResponse("P", "Passport", null),
                    new CodeDescriptionResponse("S", "Nat Insurance Card", null),
                    new CodeDescriptionResponse("T", "Travel Card", null),
                    new CodeDescriptionResponse("U", "Utility Bill", null),
                    new CodeDescriptionResponse("V", "Bank or Visa card", null),
                    new CodeDescriptionResponse("W", "Work Permit", null),
                    new CodeDescriptionResponse("X", "DSS ID", null));
            }

            @Test
            @DisplayName("Excusal and deferral codes")
            void excusalAndDeferralCodes() {
                assertValid(CodeType.EXCUSAL_AND_DEFERRAL,
                    new CodeDescriptionResponse("A", "MOVED FROM AREA", null),
                    new CodeDescriptionResponse("B", "STUDENT", null),
                    new CodeDescriptionResponse("C", "CHILD CARE", null),
                    new CodeDescriptionResponse("CE", "CJS employee (unable to transfer)", null),
                    new CodeDescriptionResponse("D", "DECEASED", null),
                    new CodeDescriptionResponse("DC", "Deferred by court - too many jurors ", null),
                    new CodeDescriptionResponse("F", "THE FORCES", null),
                    new CodeDescriptionResponse("G", "FINANCIAL HARDSHIP", null),
                    new CodeDescriptionResponse("I", "ILL", null),
                    new CodeDescriptionResponse("J", "EXCUSE BY BUREAU, TOO MANY JURORS", null),
                    new CodeDescriptionResponse("K", "CRIMINAL RECORD", null),
                    new CodeDescriptionResponse("L", "LANGUAGE DIFFICULTIES", null),
                    new CodeDescriptionResponse("M", "MEDICAL", null),
                    new CodeDescriptionResponse("N", "MENTAL HEALTH", null),
                    new CodeDescriptionResponse("O", "OTHER", null),
                    new CodeDescriptionResponse("P", "POSTPONEMENT OF SERVICE", null),
                    new CodeDescriptionResponse("PE", "Personal engagement", null),
                    new CodeDescriptionResponse("R", "RELIGIOUS REASONS", null),
                    new CodeDescriptionResponse("S", "RECENTLY SERVED", null),
                    new CodeDescriptionResponse("T", "TRAVELLING DIFFICULTIES", null),
                    new CodeDescriptionResponse("W", "WORK RELATED", null),
                    new CodeDescriptionResponse("X", "CARER", null),
                    new CodeDescriptionResponse("Y", "HOLIDAY", null),
                    new CodeDescriptionResponse("Z", "BEREAVEMENT", null));
            }

            @Test
            @DisplayName("Phone Log codes")
            void phoneLogCodes() {
                assertValid(CodeType.PHONE_LOG,
                    new CodeDescriptionResponse("AP", "Appeal", null),
                    new CodeDescriptionResponse("CA", "Change of address", null),
                    new CodeDescriptionResponse("CN", "Change of name", null),
                    new CodeDescriptionResponse("CP", "Car parking/Taxi/Travel to court", null),
                    new CodeDescriptionResponse("CS", "Chasing Expense payment - SSCL", null),
                    new CodeDescriptionResponse("DE", "Discuss Deferral", null),
                    new CodeDescriptionResponse("DS", "Difficulty completing the reply to summons", null),
                    new CodeDescriptionResponse("EL", "Discuss Excusal", null),
                    new CodeDescriptionResponse("EQ", "Eligibility query", null),
                    new CodeDescriptionResponse("ER", "Discuss the reason for being released early", null),
                    new CodeDescriptionResponse("EX", "Expense Enquiry", null),
                    new CodeDescriptionResponse("FA", "Check what facilities are available at court", null),
                    new CodeDescriptionResponse("GE", "General", null),
                    new CodeDescriptionResponse("GL", "Going to be late", null),
                    new CodeDescriptionResponse("IA", "Issues accessing digital service", null),
                    new CodeDescriptionResponse("IN", "Phone Inquiry", null),
                    new CodeDescriptionResponse("LR", "Late response to summons", null),
                    new CodeDescriptionResponse("LS", "Times and Length of service", null),
                    new CodeDescriptionResponse("MD", "Medical", null),
                    new CodeDescriptionResponse("ME", "Medical", null),
                    new CodeDescriptionResponse("NS", "No show", null),
                    new CodeDescriptionResponse("OU", "Outgoing Phone Call", null),
                    new CodeDescriptionResponse("PA", "Parking inquiry", null),
                    new CodeDescriptionResponse("PE", "Requesting an update on a previous enquiry", null),
                    new CodeDescriptionResponse("PV", "Pre-Court Visit", null),
                    new CodeDescriptionResponse("RA", "Reasonable Adjustments", null),
                    new CodeDescriptionResponse("RC", "Relocate to another court", null),
                    new CodeDescriptionResponse("RD", "Request Deferral", null),
                    new CodeDescriptionResponse("RE", "Request Excusal", null),
                    new CodeDescriptionResponse("SD", "Check sentence hearing date/result", null),
                    new CodeDescriptionResponse("SI", "Sickness", null),
                    new CodeDescriptionResponse("TR", "Travel inquiry", null),
                    new CodeDescriptionResponse("UA", "Unable to attend", null),
                    new CodeDescriptionResponse("VI", "Visit", null));
            }

            @Test
            @DisplayName("Reasonable Adjustment codes")
            void reasonableAdjustmentCodes() {
                assertValid(CodeType.REASONABLE_ADJUSTMENTS,
                    new CodeDescriptionResponse(" ", "NONE", null),
                    new CodeDescriptionResponse("A", "RELIGIOUS REASONS", null),
                    new CodeDescriptionResponse("C", "CARING RESPONSIBILITIES", null),
                    new CodeDescriptionResponse("D", "ALLERGIES", null),
                    new CodeDescriptionResponse("E", "EPILEPSY", null),
                    new CodeDescriptionResponse("H", "HEARING LOSS", null),
                    new CodeDescriptionResponse("I", "DIABETIC", null),
                    new CodeDescriptionResponse("J", "CJS EMPLOYEE", null),
                    new CodeDescriptionResponse("L", "LIMITED MOBILITY", null),
                    new CodeDescriptionResponse("M", "MULTIPLE", null),
                    new CodeDescriptionResponse("N", "NONE", null),
                    new CodeDescriptionResponse("O", "OTHER", null),
                    new CodeDescriptionResponse("P", "PREGNANCY/BREASTFEEDING", null),
                    new CodeDescriptionResponse("R", "READING", null),
                    new CodeDescriptionResponse("T", "TRAVELLING DIFFICULTIES", null),
                    new CodeDescriptionResponse("U", "MEDICATION", null),
                    new CodeDescriptionResponse("V", "VISUAL IMPAIRMENT", null),
                    new CodeDescriptionResponse("W", "WHEEL CHAIR ACCESS", null));
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {

            private ResponseEntity<String> triggerInvalid(String codeType, String owner) {
                final String jwt = createBureauJwt(COURT_USER, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(toUrl(codeType))),
                    String.class);
            }

            @Test
            void invalidCodeType() throws JsonProcessingException {
                assertInvalidPathParam(triggerInvalid("INVALID", "415"),
                    "INVALID is the incorrect data type or is not in the expected format (code_type)");
            }

            @Test
            @Disabled("Pending new authentication rules for admin users")
            void unauthorisedNotAdminUser() {
                fail("TODO");
            }
        }
    }

    @Nested
    @DisplayName("GET  " + ViewCourtDetails.URL)
    @Sql(value = {"/db/administration/tearDownCourts.sql",
        "/db/administration/createCourts.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/db/administration/tearDownCourts.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class ViewCourtDetails {
        public static final String URL = BASE_URL + "/courts/{loc_code}";


        private String toUrl(String locCode) {
            return URL.replace("{loc_code}", locCode);
        }

        @Nested
        @DisplayName("Positive")
        class Positive {

            CourtDetailsDto assertValid(String locCode) {
                final String jwt = createBureauJwt(COURT_USER, locCode, UserType.COURT, Set.of(Role.MANAGER), locCode);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<CourtDetailsDto> response = template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(toUrl(locCode))),
                    CourtDetailsDto.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                return response.getBody();
            }

            @Test
            void englishCourt() {
                assertThat(assertValid("001"))
                    .isEqualTo(CourtDetailsDto.builder()
                        .isWelsh(false)
                        .courtCode("001")
                        .englishCourtName("COURT1")
                        .englishAddress(
                            Address.builder()
                                .addressLine1("COURT1 ADDRESS1")
                                .addressLine2("COURT1 ADDRESS2")
                                .addressLine3("COURT1 ADDRESS3")
                                .addressLine4("COURT1 ADDRESS4")
                                .addressLine5("COURT1 ADDRESS5")
                                .postcode("AB1 2CD")
                                .build())
                        .welshCourtName(null)
                        .welshAddress(null)
                        .mainPhone("0123456789")
                        .attendanceTime(LocalTime.of(9, 0))
                        .costCentre("CSTCNR1")
                        .signature("COURT1 SIGNATURE")
                        .assemblyRoom("ROOM1")
                        .build()
                    );
            }

            @Test
            void welshCourt() {
                assertThat(assertValid("002"))
                    .isEqualTo(CourtDetailsDto.builder()
                        .isWelsh(true)
                        .courtCode("002")
                        .englishCourtName("COURT2")
                        .englishAddress(
                            Address.builder()
                                .addressLine1("COURT2 ADDRESS1")
                                .addressLine2("COURT2 ADDRESS2")
                                .addressLine3("COURT2 ADDRESS3")
                                .addressLine4("COURT2 ADDRESS4")
                                .addressLine5("COURT2 ADDRESS5")
                                .postcode("AB2 3CD")
                                .build())
                        .welshCourtName("WELSH_COURT2")
                        .welshAddress(Address.builder()
                            .addressLine1("WELSH_COURT2 ADDRESS1")
                            .addressLine2("WELSH_COURT2 ADDRESS2")
                            .addressLine3("WELSH_COURT2 ADDRESS3")
                            .addressLine4("WELSH_COURT2 ADDRESS4")
                            .addressLine5("WELSH_COURT2 ADDRESS5")
                            .postcode(null)
                            .build())
                        .mainPhone("0123458888")
                        .attendanceTime(LocalTime.of(9, 15))
                        .costCentre("CSTCNR2")
                        .signature("COURT2 SIGNATURE")
                        .assemblyRoom("ROOM3")
                        .build()
                    );
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {

            private ResponseEntity<String> triggerInvalid(String owner, String urlLocCode) {
                return triggerInvalid(owner, urlLocCode, Set.of(Role.MANAGER));
            }

            private ResponseEntity<String> triggerInvalid(String owner, String urlLocCode, Set<Role> roles) {
                final String jwt = createBureauJwt(COURT_USER, owner, UserType.COURT, roles, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(toUrl(urlLocCode))),
                    String.class);
            }

            @Test
            void invalidCodeType() {
                assertInvalidPathParam(triggerInvalid("INVALID", "INVALID"),
                    "viewCourtDetails.locCode: must match \"^\\d{3}$\"");
            }

            @Test
            void unauthorisedNotPartOfCourt() {
                assertForbiddenResponse(triggerInvalid("415", "416"),
                    toUrl("416"));
            }

            @Test
            void courtNotFound() {
                assertNotFound(triggerInvalid("901", "901"),
                    toUrl("901"), "Court not found");
            }
        }
    }


    @Nested
    @DisplayName("PUT  " + UpdateCourtRates.URL)
    @Sql(value = {"/db/administration/tearDownCourts.sql",
        "/db/administration/createCourts.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/db/administration/tearDownCourts.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class UpdateCourtRates {
        public static final String URL = BASE_URL + "/courts/{loc_code}/rates";


        private String toUrl(String locCode) {
            return URL.replace("{loc_code}", locCode);
        }

        static CourtRates getValidPayload() {
            return CourtRates.builder()
                .taxiSoftLimit(new BigDecimal("10.00000"))
                .publicTransportSoftLimit(new BigDecimal("5.00000"))
                .build();
        }

        @Nested
        @DisplayName("Positive")
        class Positive {

            void assertValid(String locCode, CourtRates courtRates) {
                final String jwt = createBureauJwt(COURT_USER, locCode, UserType.COURT, Set.of(Role.MANAGER), locCode);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<Void> response = template.exchange(
                    new RequestEntity<>(courtRates, httpHeaders, PUT,
                        URI.create(toUrl(locCode))),
                    Void.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be accepted")
                    .isEqualTo(HttpStatus.ACCEPTED);
                assertThat(response.getBody()).isNull();

                CourtLocation courtLocation =
                    courtLocationRepository.findById(locCode).orElseThrow(() -> new AssertionError("Court not found"));
                assertThat(courtLocation.getTaxiSoftLimit()).isEqualTo(courtRates.getTaxiSoftLimit());
                assertThat(courtLocation.getPublicTransportSoftLimit()).isEqualTo(
                    courtRates.getPublicTransportSoftLimit());
            }

            @Test
            void typical() {
                assertValid("001", CourtRates.builder()
                    .taxiSoftLimit(new BigDecimal("10.00000"))
                    .publicTransportSoftLimit(new BigDecimal("5.00000"))
                    .build());
            }

            @Test
            void nullTaxiLimit() {
                assertValid("002", CourtRates.builder()
                    .taxiSoftLimit(null)
                    .publicTransportSoftLimit(new BigDecimal("5.00000"))
                    .build());
            }

            @Test
            void nullPublicTransportLimit() {
                assertValid("001", CourtRates.builder()
                    .taxiSoftLimit(new BigDecimal("10.00000"))
                    .publicTransportSoftLimit(null)
                    .build());
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {

            private ResponseEntity<String> triggerInvalid(String owner, String urlLocCode, CourtRates courtRates) {
                return triggerInvalid(owner, urlLocCode, courtRates, UserType.COURT, Set.of(Role.MANAGER));
            }

            private ResponseEntity<String> triggerInvalid(String owner, String urlLocCode, CourtRates courtRates,
                                                          UserType userType, Set<Role> roles) {
                final String jwt = createBureauJwt(COURT_USER, owner, userType, roles, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(courtRates, httpHeaders, PUT,
                        URI.create(toUrl(urlLocCode))),
                    String.class);
            }

            @Test
            void invalidLocCode() {
                assertInvalidPathParam(triggerInvalid("INVALID", "INVALID", getValidPayload()),
                    "updateCourtRates.courtCode: must match \"^\\d{3}$\"");
            }

            @Test
            void unauthorisedNotManagerUser() {
                assertForbiddenResponse(triggerInvalid("415", "415", getValidPayload(),
                        UserType.COURT, Set.of()),
                    toUrl("415"));
            }

            @Test
            void unauthorisedNotPartOfCourt() {
                assertForbiddenResponse(triggerInvalid("415", "416", getValidPayload()),
                    toUrl("416"));
            }

            @Test
            void unauthorisedIsBureau() {
                assertForbiddenResponse(triggerInvalid("400", "416", getValidPayload(),
                        UserType.BUREAU, Set.of()),
                    toUrl("416"));
            }

            @Test
            void courtNotFound() {
                assertNotFound(triggerInvalid("901", "901", getValidPayload()),
                    toUrl("901"), "Court not found");
            }
        }
    }

    @Nested
    @DisplayName("PUT  " + UpdateCourtDetails.URL)
    @Sql(value = {"/db/administration/tearDownCourts.sql",
        "/db/administration/createCourts.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/db/administration/tearDownCourts.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class UpdateCourtDetails {
        public static final String URL = BASE_URL + "/courts/{loc_code}";

        private String toUrl(String locCode) {
            return URL.replace("{loc_code}", locCode);
        }

        private UpdateCourtDetailsDto getValidPayload() {
            return UpdateCourtDetailsDto.builder()
                .mainPhoneNumber("0123456789")
                .defaultAttendanceTime(LocalTime.of(9, 0))
                .assemblyRoomId(999_992L)
                .costCentre("NWCSTCNR1")
                .signature("New COURT1 SIGNATURE")
                .build();
        }

        @Nested
        @DisplayName("Positive")
        class Positive {

            void assertValid(String locCode, UpdateCourtDetailsDto request, UserType userType, Set<Role> roles) {
                final String jwt = createBureauJwt(COURT_USER, locCode, userType, roles, locCode);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<CourtDetailsDto> response = template.exchange(
                    new RequestEntity<>(request, httpHeaders, PUT,
                        URI.create(toUrl(locCode))),
                    CourtDetailsDto.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.ACCEPTED);
                assertThat(response.getBody()).isNull();
            }

            @Test
            void typicalCourt() {
                assertValid("001", getValidPayload(), UserType.COURT, Set.of(Role.MANAGER));
                CourtLocation courtLocation = courtLocationRepository.findByLocCode("001")
                    .orElseThrow(() -> new AssertionError("Court not found"));
                assertThat(courtLocation.getLocPhone()).isEqualTo("0123456789");
                assertThat(courtLocation.getCourtAttendTime()).isEqualTo(LocalTime.of(9, 0));
                assertThat(courtLocation.getAssemblyRoom().getId()).isEqualTo(999_992L);
                assertThat(courtLocation.getCostCentre()).isEqualTo("NWCSTCNR1");
                assertThat(courtLocation.getSignatory()).isEqualTo("New COURT1 SIGNATURE");
            }

            @Test
            void typicalAdministrator() {
                assertValid("001", getValidPayload(), UserType.ADMINISTRATOR, Set.of());
                CourtLocation courtLocation = courtLocationRepository.findByLocCode("001")
                    .orElseThrow(() -> new AssertionError("Court not found"));
                assertThat(courtLocation.getLocPhone()).isEqualTo("0123456789");
                assertThat(courtLocation.getCourtAttendTime()).isEqualTo(LocalTime.of(9, 0));
                assertThat(courtLocation.getAssemblyRoom().getId()).isEqualTo(999_992L);
                assertThat(courtLocation.getCostCentre()).isEqualTo("NWCSTCNR1");
                assertThat(courtLocation.getSignatory()).isEqualTo("New COURT1 SIGNATURE");
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {

            private ResponseEntity<String> triggerInvalid(String owner, String urlLocCode,
                                                          UpdateCourtDetailsDto request) {
                return triggerInvalid(owner, urlLocCode, request, UserType.COURT, Set.of());
            }

            private ResponseEntity<String> triggerInvalid(String owner, String urlLocCode,
                                                          UpdateCourtDetailsDto request,
                                                          UserType userType, Set<Role> roles) {
                final String jwt = createBureauJwt(COURT_USER, owner, userType, roles, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(request, httpHeaders, PUT,
                        URI.create(toUrl(urlLocCode))),
                    String.class);
            }

            @Test
            void invalidCodeType() {
                assertInvalidPathParam(triggerInvalid("INVALID", "INVALID", getValidPayload()),
                    "updateCourtDetails.locCode: must match \"^\\d{3}$\"");
            }

            @Test
            void unauthorisedNotPartOfCourt() {
                assertForbiddenResponse(triggerInvalid("001", "004", getValidPayload()),
                    toUrl("004"));
            }

            @Test
            void unauthorisedBureauOfficer() {
                assertForbiddenResponse(triggerInvalid("400", "415", getValidPayload(),
                        UserType.BUREAU,Set.of()),
                    toUrl("415"));
            }

            @Test
            void courtNotFound() {
                assertNotFound(triggerInvalid("901", "901", getValidPayload()),
                    toUrl("901"), "Court not found");
            }

            @Test
            void invalidPayload() {
                UpdateCourtDetailsDto invalidPayload = getValidPayload();
                invalidPayload.setCostCentre(null);
                assertInvalidPayload(triggerInvalid("001", "001", invalidPayload),
                    new RestResponseEntityExceptionHandler.FieldError("costCentre", "must not be blank"));
            }
        }
    }

    public static ExpenseRatesDto getBaseExpenseRates() {
        return ExpenseRatesDto.builder()
            .carMileageRatePerMile0Passengers(new BigDecimal("0.31400"))
            .carMileageRatePerMile1Passengers(new BigDecimal("0.35600"))
            .carMileageRatePerMile2OrMorePassengers(new BigDecimal("0.39800"))
            .motorcycleMileageRatePerMile0Passengers(new BigDecimal("0.31400"))
            .motorcycleMileageRatePerMile1Passengers(new BigDecimal("0.32400"))
            .bikeRate(new BigDecimal("0.09600"))
            .limitFinancialLossHalfDay(new BigDecimal("32.47000"))
            .limitFinancialLossFullDay(new BigDecimal("64.95000"))
            .limitFinancialLossHalfDayLongTrial(new BigDecimal("64.95000"))
            .limitFinancialLossFullDayLongTrial(new BigDecimal("129.91000"))
            .subsistenceRateStandard(new BigDecimal("5.71000"))
            .subsistenceRateLongDay(new BigDecimal("12.17000"))
            .build();
    }


    @Nested
    @DisplayName("GET  " + ViewAllCourtsDetails.URL)
    @Sql(value = {"/db/administration/tearDownCourts.sql",
        "/db/administration/createCourts.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/db/administration/tearDownCourts.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class ViewAllCourtsDetails {
        public static final String URL = BASE_URL + "/courts";

        private ViewAllCourtsDetails() {

        }

        @Nested
        @DisplayName("Positive")
        class Positive {

            List<CourtDetailsReduced> assertValid() {
                final String jwt = createBureauJwt(COURT_USER, "415", UserType.ADMINISTRATOR,
                    Set.of(), "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<List<CourtDetailsReduced>> response = template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(URL)),
                    new ParameterizedTypeReference<>() {
                    });
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isNotNull();
                return response.getBody();
            }

            @Test
            void typical() {
                List<CourtDetailsReduced> response = assertValid();
                //Filter response to only the courts we add in this test
                List<CourtDetailsReduced> resultFiltered = response.stream()
                    .filter(c -> c.getLocCode().startsWith("00"))
                    .filter(c -> !"000".equals(c.getLocCode()))
                    .toList();
                assertThat(resultFiltered).containsExactlyInAnyOrder(
                    CourtDetailsReduced.builder()
                        .locCode("001")
                        .courtName("COURT1")
                        .courtType(CourtType.MAIN)
                        .build(),
                    CourtDetailsReduced.builder()
                        .locCode("002")
                        .courtName("COURT2")
                        .courtType(CourtType.SATELLITE)
                        .build(),
                    CourtDetailsReduced.builder()
                        .locCode("003")
                        .courtName("COURT3")
                        .courtType(CourtType.MAIN)
                        .build(),
                    CourtDetailsReduced.builder()
                        .locCode("004")
                        .courtName("COURT4")
                        .courtType(CourtType.SATELLITE)
                        .build(),
                    CourtDetailsReduced.builder()
                        .locCode("005")
                        .courtName("COURT5")
                        .courtType(CourtType.MAIN)
                        .build()
                );
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {


            private ResponseEntity<String> triggerInvalid(UserType userType, Set<Role> roles) {
                return triggerInvalid(userType, roles, "415");
            }

            private ResponseEntity<String> triggerInvalid(UserType userType, Set<Role> roles, String owner) {
                final String jwt = createBureauJwt(COURT_USER, owner, userType, roles, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(URL)),
                    String.class);
            }

            @Test
            void unauthorisedIsBureau() {
                assertForbiddenResponse(triggerInvalid(UserType.BUREAU, Set.of(), "400"),
                    URL);
            }

            @ParameterizedTest
            @EnumSource(value = UserType.class, names = {"ADMINISTRATOR"}, mode = EnumSource.Mode.EXCLUDE)
            void unauthorisedNotAdministratorUserType(UserType userType) {
                assertForbiddenResponse(triggerInvalid(userType, Set.of()),
                    URL);
            }
        }
    }


    @Nested
    @DisplayName("GET  " + ViewExpenseDetails.URL)
    @Sql({"/db/mod/truncate.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class ViewExpenseDetails {
        public static final String URL = BASE_URL + "/expenses/rates";

        private ViewExpenseDetails() {

        }

        @Nested
        @DisplayName("Positive")
        class Positive {

            @Test
            void typical() {
                final String jwt = createBureauJwt(COURT_USER, TestConstants.VALID_COURT_LOCATION,
                    UserType.ADMINISTRATOR, Set.of(),
                    TestConstants.VALID_COURT_LOCATION);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<ExpenseRatesDto> response = template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(URL)),
                    ExpenseRatesDto.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                assertThat(response.getBody()).isEqualTo(
                    getBaseExpenseRates()
                );
            }


        }

        @Nested
        @DisplayName("Negative")
        class Negative {


            private ResponseEntity<String> triggerInvalid(UserType userType, Set<Role> roles) {
                final String jwt = createBureauJwt(COURT_USER, TestConstants.VALID_COURT_LOCATION, userType, roles,
                    TestConstants.VALID_COURT_LOCATION);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(URL)),
                    String.class);
            }

            @Test
            void unauthorisedNotAdminUser() {
                assertForbiddenResponse(triggerInvalid(UserType.COURT, Set.of()), URL);
            }
        }
    }

    @Nested
    @DisplayName("PUT  " + UpdateExpenseDetails.URL)
    @Sql({"/db/mod/truncate.sql",
        "/db/JurorExpenseControllerITest_expenseRates.sql"})
    class UpdateExpenseDetails {
        public static final String URL = BASE_URL + "/expenses/rates";


        private ExpenseRatesDto getValidPayload() {
            return ExpenseRatesDto.builder()
                .carMileageRatePerMile0Passengers(new BigDecimal("1.01000"))
                .carMileageRatePerMile1Passengers(new BigDecimal("2.02000"))
                .carMileageRatePerMile2OrMorePassengers(new BigDecimal("3.03000"))
                .motorcycleMileageRatePerMile0Passengers(new BigDecimal("4.04000"))
                .motorcycleMileageRatePerMile1Passengers(new BigDecimal("5.05000"))
                .bikeRate(new BigDecimal("6.06000"))
                .limitFinancialLossHalfDay(new BigDecimal("7.07000"))
                .limitFinancialLossFullDay(new BigDecimal("8.08000"))
                .limitFinancialLossHalfDayLongTrial(new BigDecimal("9.09000"))
                .limitFinancialLossFullDayLongTrial(new BigDecimal("10.10000"))
                .subsistenceRateStandard(new BigDecimal("11.11000"))
                .subsistenceRateLongDay(new BigDecimal("12.12000"))
                .build();
        }

        @Nested
        @DisplayName("Positive")
        class Positive {

            @Test
            void typical() {
                final String jwt = createBureauJwt(COURT_USER,
                    TestConstants.VALID_COURT_LOCATION,
                    UserType.ADMINISTRATOR,
                    Set.of());

                ExpenseRatesDto request = getValidPayload();
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<Void> response = template.exchange(
                    new RequestEntity<>(request, httpHeaders, PUT,
                        URI.create(URL)),
                    Void.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.ACCEPTED);
                assertThat(response.getBody()).isNull();

                List<ExpenseRates> expenseRates = expenseRatesRepository.findAll();
                assertThat(expenseRates).hasSize(3);
                assertThat(expenseRates.get(0)).isEqualTo(
                    ExpenseRates.builder()
                        .id(999_998)
                        .carMileageRatePerMile0Passengers(new BigDecimal("0.10000"))
                        .carMileageRatePerMile1Passengers(new BigDecimal("0.10000"))
                        .carMileageRatePerMile2OrMorePassengers(new BigDecimal("0.10000"))
                        .motorcycleMileageRatePerMile0Passengers(new BigDecimal("0.10000"))
                        .motorcycleMileageRatePerMile1Passengers(new BigDecimal("0.10000"))
                        .bikeRate(new BigDecimal("0.10000"))
                        .limitFinancialLossHalfDay(new BigDecimal("0.10000"))
                        .limitFinancialLossFullDay(new BigDecimal("0.10000"))
                        .limitFinancialLossHalfDayLongTrial(new BigDecimal("0.10000"))
                        .limitFinancialLossFullDayLongTrial(new BigDecimal("0.10000"))
                        .subsistenceRateStandard(new BigDecimal("0.10000"))
                        .subsistenceRateLongDay(new BigDecimal("0.10000"))
                        .ratesEffectiveFrom(LocalDate.now().minusDays(1))
                        .build());
                assertThat(expenseRates.get(1)).isEqualTo(
                    ExpenseRates.builder()
                        .id(999_999)
                        .carMileageRatePerMile0Passengers(new BigDecimal("0.31400"))
                        .carMileageRatePerMile1Passengers(new BigDecimal("0.35600"))
                        .carMileageRatePerMile2OrMorePassengers(new BigDecimal("0.39800"))
                        .motorcycleMileageRatePerMile0Passengers(new BigDecimal("0.31400"))
                        .motorcycleMileageRatePerMile1Passengers(new BigDecimal("0.32400"))
                        .bikeRate(new BigDecimal("0.09600"))
                        .limitFinancialLossHalfDay(new BigDecimal("32.47000"))
                        .limitFinancialLossFullDay(new BigDecimal("64.95000"))
                        .limitFinancialLossHalfDayLongTrial(new BigDecimal("64.95000"))
                        .limitFinancialLossFullDayLongTrial(new BigDecimal("129.91000"))
                        .subsistenceRateStandard(new BigDecimal("5.71000"))
                        .subsistenceRateLongDay(new BigDecimal("12.17000"))
                        .ratesEffectiveFrom(LocalDate.now())
                        .build());

                ExpenseRates addedExpenseRates = expenseRates.get(2);

                assertThat(addedExpenseRates.getCarMileageRatePerMile0Passengers()).isEqualTo(
                    request.getCarMileageRatePerMile0Passengers());
                assertThat(addedExpenseRates.getCarMileageRatePerMile1Passengers()).isEqualTo(
                    request.getCarMileageRatePerMile1Passengers());
                assertThat(addedExpenseRates.getCarMileageRatePerMile2OrMorePassengers()).isEqualTo(
                    request.getCarMileageRatePerMile2OrMorePassengers());
                assertThat(addedExpenseRates.getMotorcycleMileageRatePerMile0Passengers()).isEqualTo(
                    request.getMotorcycleMileageRatePerMile0Passengers());
                assertThat(addedExpenseRates.getMotorcycleMileageRatePerMile1Passengers()).isEqualTo(
                    request.getMotorcycleMileageRatePerMile1Passengers());
                assertThat(addedExpenseRates.getBikeRate()).isEqualTo(request.getBikeRate());
                assertThat(addedExpenseRates.getLimitFinancialLossHalfDay()).isEqualTo(
                    request.getLimitFinancialLossHalfDay());
                assertThat(addedExpenseRates.getLimitFinancialLossFullDay()).isEqualTo(
                    request.getLimitFinancialLossFullDay());
                assertThat(addedExpenseRates.getLimitFinancialLossHalfDayLongTrial()).isEqualTo(
                    request.getLimitFinancialLossHalfDayLongTrial());
                assertThat(addedExpenseRates.getLimitFinancialLossFullDayLongTrial()).isEqualTo(
                    request.getLimitFinancialLossFullDayLongTrial());
                assertThat(addedExpenseRates.getSubsistenceRateStandard()).isEqualTo(
                    request.getSubsistenceRateStandard());
                assertThat(addedExpenseRates.getSubsistenceRateLongDay()).isEqualTo(
                    request.getSubsistenceRateLongDay());
                assertThat(addedExpenseRates.getRatesEffectiveFrom()).isToday();
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {

            private ResponseEntity<String> triggerInvalid(UserType userType, Set<Role> roles,
                                                          ExpenseRatesDto payload) {
                return triggerInvalid(TestConstants.VALID_COURT_LOCATION, userType, roles, payload);
            }

            private ResponseEntity<String> triggerInvalid(String owner, UserType userType, Set<Role> roles,
                                                          ExpenseRatesDto payload) {
                final String jwt = createBureauJwt(COURT_USER, owner, userType, roles,
                    TestConstants.VALID_COURT_LOCATION);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(payload, httpHeaders, PUT,
                        URI.create(URL)),
                    String.class);
            }

            @Test
            void unauthorisedNotAdminUser() {
                assertForbiddenResponse(triggerInvalid(UserType.COURT, Set.of(), getValidPayload()),
                    URL);
            }


            @Test
            void unauthorisedIsBureauUser() {
                assertForbiddenResponse(
                    triggerInvalid("400", UserType.BUREAU, Set.of(), getValidPayload()),
                    URL);
            }

            @Test
            void invalidPayload() {
                ExpenseRatesDto invalidPayload = getValidPayload();
                invalidPayload.setBikeRate(null);
                assertInvalidPayload(
                    triggerInvalid(UserType.ADMINISTRATOR, Set.of(), invalidPayload),
                    new RestResponseEntityExceptionHandler.FieldError("bikeRate", "must not be null"));
            }
        }
    }
}
