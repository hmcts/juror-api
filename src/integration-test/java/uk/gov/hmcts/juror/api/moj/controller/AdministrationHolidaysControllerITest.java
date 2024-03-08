package uk.gov.hmcts.juror.api.moj.controller;

import lombok.RequiredArgsConstructor;
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
import uk.gov.hmcts.juror.api.juror.domain.Holidays;
import uk.gov.hmcts.juror.api.juror.domain.HolidaysRepository;
import uk.gov.hmcts.juror.api.moj.controller.response.administration.HolidayDate;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + AdministrationHolidaysControllerITest.BASE_URL)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AdministrationHolidaysControllerITest extends AbstractIntegrationTest {
    public static final String BASE_URL = "/api/v1/moj/administration";

    private HttpHeaders httpHeaders;
    private final TestRestTemplate template;

    private final HolidaysRepository holidaysRepository;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Nested
    @DisplayName("GET  " + ViewBankHolidays.URL)
    @Sql(value = {"/db/administration/tearDownHolidays.sql",
        "/db/administration/createHolidays.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/db/administration/tearDownHolidays.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class ViewBankHolidays {
        public static final String URL = BASE_URL + "/bank-holidays";

        private ViewBankHolidays() {
    
        }

        @Nested
        @DisplayName("Positive")
        class Positive {

            Map<Integer, List<HolidayDate>> assertValid(Set<Role> roles) {
                final String jwt = createBureauJwt(COURT_USER, "415", UserType.COURT, roles, "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<Map<Integer, List<HolidayDate>>> response = template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(URL)),
                    new ParameterizedTypeReference<>() {
                    });
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                int year = LocalDate.now().getYear();
                assertThat(response.getBody()).isEqualTo(
                    Map.of(
                        year, List.of(
                            new HolidayDate(LocalDate.of(year, 1, 1), "Public holiday 1"),
                            new HolidayDate(LocalDate.of(year, 2, 1), "Public holiday 2")
                        ),
                        year + 1, List.of(
                            new HolidayDate(LocalDate.of(year + 1, 1, 1), "Public holiday 1 + 1"),
                            new HolidayDate(LocalDate.of(year + 1, 2, 1), "Public holiday 2 + 1")
                        ),
                        year + 2, List.of(
                            new HolidayDate(LocalDate.of(year + 2, 1, 1), "Public holiday 1 + 2"),
                            new HolidayDate(LocalDate.of(year + 2, 2, 1), "Public holiday 2 + 2")
                        )
                    )
                );
                return response.getBody();
            }

            @Test
            void typicalCourtUser() {
                assertValid(Set.of(Role.MANAGER));
            }

            @Test
            void typicalAdministrator() {
                assertValid(Set.of(Role.ADMINISTRATOR));
            }

        }

        @Nested
        @DisplayName("Negative")
        class Negative {


            private ResponseEntity<String> triggerInvalid(String owner, Set<Role> roles) {
                final String jwt = createBureauJwt(COURT_USER, owner, UserType.COURT, roles, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(URL)),
                    String.class);
            }

            @Test
            void unauthorisedNotManagerOrAdminUser() {
                assertForbiddenResponse(triggerInvalid("415", Set.of(Role.COURT_OFFICER)),
                    URL);
            }
        }
    }

    @Nested
    @DisplayName("GET  " + ViewNonSittingDays.URL)
    @Sql(value = {"/db/administration/tearDownHolidays.sql",
        "/db/administration/createHolidays.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/db/administration/tearDownHolidays.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class ViewNonSittingDays {
        public static final String URL = BASE_URL + "/non-sitting-days/{loc_code}";


        private String toUrl(String locCode) {
            return URL.replace("{loc_code}", locCode);
        }

        @Nested
        @DisplayName("Positive")
        class Positive {

            List<HolidayDate> assertValid(String locCode) {
                final String jwt = createBureauJwt(COURT_USER, locCode, UserType.COURT, Set.of(Role.MANAGER), locCode);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<List<HolidayDate>> response = template.exchange(
                    new RequestEntity<>(httpHeaders, GET,
                        URI.create(toUrl(locCode))),
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
                assertThat(assertValid("001"))
                    .isEqualTo(List.of(
                        )
                    );
            }

            @Test
            void noHolidays() {
                assertThat(assertValid("004"))
                    .isEqualTo(List.of());
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
                    "viewNonSittingDays.locCode: must match \"^\\d{3}$\"");
            }

            @Test
            void unauthorisedNotManagerUser() {
                assertForbiddenResponse(triggerInvalid("415", "415", Set.of(Role.COURT_OFFICER)),
                    toUrl("415"));
            }

            @Test
            void unauthorisedNotPartOfCourt() {
                assertForbiddenResponse(triggerInvalid("415", "416"),
                    toUrl("416"));
            }

            @Test
            void unauthorisedBureauUSer() {
                assertForbiddenResponse(triggerInvalid("400", "415", Set.of(Role.BUREAU_OFFICER)),
                    toUrl("415"));
            }
        }
    }

    @Nested
    @DisplayName("DELETE  " + DeleteNonSittingDays.URL)
    @Sql(value = {"/db/administration/tearDownHolidays.sql",
        "/db/administration/createHolidays.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/db/administration/tearDownHolidays.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class DeleteNonSittingDays {
        public static final String URL = BASE_URL + "/non-sitting-days/{loc_code}/{date}";


        private String toUrl(String locCode, LocalDate date) {
            return toUrl(locCode, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }

        private String toUrl(String locCode, String date) {
            return URL.replace("{loc_code}", locCode)
                .replace("{date}", date);
        }

        @Nested
        @DisplayName("Positive")
        class Positive {

            void assertValid(String locCode, LocalDate date) {
                final String jwt = createBureauJwt(COURT_USER, locCode, UserType.COURT, Set.of(Role.MANAGER), locCode);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<List<HolidayDate>> response = template.exchange(
                    new RequestEntity<>(httpHeaders, DELETE,
                        URI.create(toUrl(locCode, date))),
                    new ParameterizedTypeReference<>() {
                    });
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.ACCEPTED);

                holidaysRepository.findByCourtLocationLocCodeAndHolidayAndPublicHolidayIsFalse(locCode, date)
                    .ifPresent(holidays -> {
                        throw new AssertionError("Holiday should have been deleted");
                    });
            }

            @Test
            void typical() {
                assertValid("415", LocalDate.of(LocalDate.now().getYear(), 1, 1));
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {
            private static final String VALID_DATE = "2023-01-01";

            private ResponseEntity<String> triggerInvalid(String owner, String urlLocCode, String date) {
                return triggerInvalid(owner, urlLocCode, date, Set.of(Role.MANAGER));
            }

            private ResponseEntity<String> triggerInvalid(String owner, String urlLocCode, String date,
                                                          Set<Role> roles) {
                final String jwt = createBureauJwt(COURT_USER, owner, UserType.COURT, roles, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(httpHeaders, DELETE,
                        URI.create(toUrl(urlLocCode, date))),
                    String.class);
            }

            @Test
            void invalidLocCode() {
                assertInvalidPathParam(triggerInvalid("INVALID", "INVALID", VALID_DATE),
                    "deleteNonSittingDays.locCode: must match \"^\\d{3}$\"");
            }

            @Test
            void unauthorisedNotManagerUser() {
                assertForbiddenResponse(triggerInvalid("415", "415", VALID_DATE,
                    Set.of(Role.COURT_OFFICER)), toUrl("415", "2023-01-01"));
            }

            @Test
            void unauthorisedNotPartOfCourt() {
                assertForbiddenResponse(triggerInvalid("415", "416", VALID_DATE),
                    toUrl("416", VALID_DATE));
            }

            @Test
            void unauthorisedBureauUSer() {
                assertForbiddenResponse(triggerInvalid("400", "415", VALID_DATE, Set.of(Role.BUREAU_OFFICER)),
                    toUrl("415", VALID_DATE));
            }

            @Test
            void notFound() {
                assertNotFound(
                    triggerInvalid("415", "415", "2020-01-01"),
                    toUrl("415", "2020-01-01"),
                    "No non-sitting day found for 415 on 2020-01-01"
                );
            }
        }
    }

    @Nested
    @DisplayName("POST  " + AddNonSittingDays.URL)
    @Sql(value = {"/db/administration/tearDownHolidays.sql",
        "/db/administration/createHolidays.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = "/db/administration/tearDownHolidays.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class AddNonSittingDays {
        public static final String URL = BASE_URL + "/non-sitting-days/{loc_code}";


        private String toUrl(String locCode) {
            return URL.replace("{loc_code}", locCode);
        }

        static HolidayDate getValidPayload() {
            return new HolidayDate(LocalDate.of(2023, 5, 4),
                "Test: A new court holiday");
        }

        @Nested
        @DisplayName("Positive")
        class Positive {

            void assertValid(String locCode, HolidayDate holidayDate) {
                final String jwt = createBureauJwt(COURT_USER, locCode, UserType.COURT, Set.of(Role.MANAGER), locCode);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<Void> response = template.exchange(
                    new RequestEntity<>(holidayDate, httpHeaders, POST,
                        URI.create(toUrl(locCode))),
                    new ParameterizedTypeReference<>() {
                    });
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be accepted")
                    .isEqualTo(HttpStatus.ACCEPTED);
                assertThat(response.getBody()).isNull();

                Holidays holidays =
                    holidaysRepository.findByCourtLocationLocCodeAndHolidayAndPublicHolidayIsFalse(locCode,
                            holidayDate.getDate())
                        .orElseThrow(() -> new AssertionError("Holiday should have been added"));
                assertThat(holidays.getHoliday()).isEqualTo(holidayDate.getDate());
                assertThat(holidays.getPublicHoliday()).isFalse();
                assertThat(holidays.getDescription()).isEqualTo(holidayDate.getDescription());
            }

            @Test
            void typicalNonBankHolidayDay() {
                assertValid("415", new HolidayDate(LocalDate.of(2024, 5, 1),
                    "Test: A new court holiday 1"));
            }

            @Test
            void bankHolidayDay() {
                assertValid("416", new HolidayDate(LocalDate.of(LocalDate.now().getYear(), 1, 1),
                    "Test: A new court holiday 2"));
            }
        }

        @Nested
        @DisplayName("Negative")
        class Negative {

            private ResponseEntity<String> triggerInvalid(String owner, String urlLocCode, HolidayDate holidayDate) {
                return triggerInvalid(owner, urlLocCode, holidayDate, UserType.COURT, Set.of(Role.MANAGER));
            }

            private ResponseEntity<String> triggerInvalid(String owner, String urlLocCode, HolidayDate holidayDate,
                                                          UserType userType, Set<Role> roles) {
                final String jwt = createBureauJwt(COURT_USER, owner, userType, roles, owner);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return template.exchange(
                    new RequestEntity<>(holidayDate, httpHeaders, POST,
                        URI.create(toUrl(urlLocCode))),
                    String.class);
            }

            @Test
            void invalidLocCode() {
                assertInvalidPathParam(triggerInvalid("INVALID", "INVALID", getValidPayload()),
                    "addNonSittingDays.locCode: must match \"^\\d{3}$\"");
            }

            @Test
            void invalidPayload() {
                HolidayDate payload = getValidPayload();
                payload.setDate(null);
                assertInvalidPayload(triggerInvalid("415", "415", payload),
                    new RestResponseEntityExceptionHandler.FieldError("date", "must not be null"));
            }

            @Test
            void unauthorisedNotManagerUser() {
                assertForbiddenResponse(triggerInvalid("415", "415", getValidPayload(),
                        UserType.COURT, Set.of(Role.COURT_OFFICER)),
                    toUrl("415"));
            }

            @Test
            void unauthorisedNotPartOfCourt() {
                assertForbiddenResponse(triggerInvalid("415", "416", getValidPayload()),
                    toUrl("416"));
            }

            @Test
            void unauthorisedBureauUSer() {
                assertForbiddenResponse(triggerInvalid("400", "415", getValidPayload(),
                        UserType.BUREAU, Set.of(Role.BUREAU_OFFICER)),
                    toUrl("415"));
            }

            @Test
            void courtNotFound() {
                assertNotFound(
                    triggerInvalid("998", "998", getValidPayload()),
                    toUrl("998"),
                    "Court location not found"
                );
            }

            @Test
            void duplicateHoliday() {
                HolidayDate payload = getValidPayload();
                payload.setDate(LocalDate.of(LocalDate.now().getYear(), 1, 1));
                assertBusinessRuleViolation(
                    triggerInvalid("415", "415", payload),
                    "Non-sitting day already exists for 415 on " + payload.getDate().toString(),
                    MojException.BusinessRuleViolation.ErrorCode.DAY_ALREADY_EXISTS
                );
            }
        }
    }
}
