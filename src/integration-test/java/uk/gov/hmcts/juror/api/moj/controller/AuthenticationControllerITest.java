package uk.gov.hmcts.juror.api.moj.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractControllerIntegrationTest;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.config.InvalidJwtAuthenticationException;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.authentication.CourtDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.EmailDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.JwtDto;
import uk.gov.hmcts.juror.api.moj.enumeration.CourtType;

import java.time.Clock;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + AuthenticationControllerITest.BASE_URL)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Sql(value = {"/db/administration/teardownUsers.sql",
    "/db/administration/createUsers.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "/db/administration/teardownUsers.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SuppressWarnings({
    "PMD.JUnitTestsShouldIncludeAssert",
    "PMD.ExcessiveImports",
    "PMD.JUnitAssertionsShouldIncludeMessage"//False positive
})
public class AuthenticationControllerITest extends AbstractIntegrationTest {
    public static final String BASE_URL = "/api/v1/auth/moj";
    private static final String EMAIL_SUFFIX = "@email.gov.uk";

    private final TestRestTemplate template;
    private HttpHeaders httpHeaders;

    @Value("${jwt.secret.bureau}")
    private String bureauSecret;

    private final Clock clock;

    @BeforeEach
    public void setUp() throws Exception {
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Nested
    @DisplayName("GET (POST) " + ViewCourts.URL)
    class ViewCourts extends AbstractControllerIntegrationTest<EmailDto, List<CourtDto>> {
        private static final String URL = BASE_URL + "/courts";

        protected ViewCourts() {
            super(POST, template, HttpStatus.OK);
        }

        @Override
        protected String getValidUrl() {
            return URL;
        }

        @Override
        protected String getValidJwt() {
            return createHmacJwt();
        }

        @Override
        protected EmailDto getValidPayload() {
            return new EmailDto("test_court_standard" + EMAIL_SUFFIX);
        }

        @DisplayName("Positive")
        @Nested
        class Positive {

            @Test
            void noCourts() {
                testBuilder()
                    .payload(new EmailDto("test_court_no_courts" + EMAIL_SUFFIX))
                    .triggerValid()
                    .assertValidCollectionExactOrder();
            }

            @Test
            void primaryCourtOnly() {
                testBuilder()
                    .payload(new EmailDto("test_court_primary" + EMAIL_SUFFIX))
                    .triggerValid()
                    .assertValidCollectionExactOrder(CourtDto.builder()
                        .courtType(CourtType.MAIN)
                        .locCode("408")
                        .name("BRISTOL")
                        .build());
            }

            @Test
            void primaryWithSatellites() {
                testBuilder()
                    .payload(new EmailDto("test_court_standard" + EMAIL_SUFFIX))
                    .triggerValid()
                    .assertValidCollectionExactOrder(CourtDto.builder()
                            .courtType(CourtType.MAIN)
                            .locCode("415")
                            .name("CHESTER")
                            .build(),
                        CourtDto.builder()
                            .courtType(CourtType.SATELLITE)
                            .locCode("462")
                            .name("WARRINGTON")
                            .build(),
                        CourtDto.builder()
                            .courtType(CourtType.SATELLITE)
                            .locCode("767")
                            .name("KNUTSFORD")
                            .build());
            }
        }

        @DisplayName("Negative")
        @Nested
        class Negative {

            @Test
            void invalidJwtUsingBureauJwt() {
                testBuilder()
                    .jwt(createJwt("test_court_standard", "415"))
                    .triggerInvalid()
                    .assertInternalServerErrorViolation(InvalidJwtAuthenticationException.class,
                        "Failed to parse JWT");
            }

            @Test
            void userNotFound() {
                testBuilder()
                    .payload(new EmailDto("not_found" + EMAIL_SUFFIX))
                    .triggerInvalid()
                    .assertNotFound("User not found");
            }
        }
    }

    @Nested
    @DisplayName("GET (POST) " + ViewCourts.URL)
    class CreateJwt extends AbstractControllerIntegrationTest<EmailDto, JwtDto> {
        private static final String URL = BASE_URL + "/jwt/{loc_code}";

        protected CreateJwt() {
            super(POST, template, HttpStatus.OK);
        }

        @Override
        protected String getValidUrl() {
            return toUrl("415");
        }

        @Override
        protected String getValidJwt() {
            return createHmacJwt();
        }

        @Override
        protected EmailDto getValidPayload() {
            return new EmailDto("test_court_standard" + EMAIL_SUFFIX);
        }

        public String toUrl(String locCode) {
            return URL.replace("{loc_code}", locCode);
        }


        @DisplayName("Positive")
        @Nested
        class Positive {


            private void responseValidator(JwtDto response,
                                           String username, BureauJwtPayload expectedJwtClaims) {
                Jwt<JwsHeader, Claims> jwt = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(bureauSecret)))
                    .build()
                    .parseSignedClaims(response.getJwt());

                JwsHeader header = jwt.getHeader();
                assertThat(header.isPayloadEncoded()).isTrue();
                assertThat(header.getAlgorithm()).isEqualTo("HS512");
                assertThat(header.getCompressionAlgorithm()).isNull();

                Claims claims = jwt.getPayload();

                assertThat(claims.getId()).isEqualTo(username);
                assertThat(claims.getIssuer()).isEqualTo("juror");
                assertThat(claims.getSubject()).isNull();
                assertThat(claims.getExpiration())
                    //Must expire in future
                    .isInTheFuture()
                    //Expiry is less than 1 hour
                    .isBefore(new Date(clock.millis() + 3_700_000));
                assertThat(claims.getNotBefore()).isNull();
                assertThat(claims.getIssuedAt()).isBeforeOrEqualTo(new Date(clock.millis()));
                assertThat(claims.getIssuedAt()).isAfter(new Date(clock.millis() - 60_000));

                assertThat(claims)
                    .hasSize(14)
                    .containsEntry("owner", expectedJwtClaims.getOwner())
                    .containsEntry("email", expectedJwtClaims.getEmail())
                    .containsEntry("locCode", expectedJwtClaims.getLocCode())
                    .containsEntry("login", expectedJwtClaims.getLogin())
                    .containsEntry("userType", expectedJwtClaims.getUserType().name())
                    .containsEntry("activeUserType", expectedJwtClaims.getActiveUserType().name())
                    .containsEntry("staff", Map.of(
                        "name", expectedJwtClaims.getStaff().getName(),
                        "rank", expectedJwtClaims.getStaff().getRank(),
                        "active", expectedJwtClaims.getStaff().getActive(),
                        "courts", expectedJwtClaims.getStaff().getCourts()
                    ))
                    .containsEntry("roles", expectedJwtClaims.getRoles()
                        .stream().map(Enum::name).toList())
                    .containsEntry("permissions", List.of());
            }

            @Test
            void primaryCourt() {
                testBuilder()
                    .payload(new EmailDto("test_court_primary" + EMAIL_SUFFIX))
                    .url(toUrl("408"))
                    .triggerValid()
                    .assertValid((controllerTest, response) -> responseValidator(
                        response,
                        "test_court_primary",
                        BureauJwtPayload.builder()
                            .owner("408")
                            .email("test_court_primary" + EMAIL_SUFFIX)
                            .locCode("408")
                            .login("test_court_primary")
                            .userLevel("1")
                            .userType(UserType.COURT)
                            .activeUserType(UserType.COURT)
                            .roles(Set.of())
                            .staff(BureauJwtPayload.Staff.builder()
                                .name("Court Primary Only")
                                .rank(1)
                                .active(1)
                                .courts(List.of("408"))
                                .build())
                            .build()
                    ));
            }

            @Test
            void satelliteCourt() {
                testBuilder()
                    .url(toUrl("462"))
                    .payload(new EmailDto("test_court_standard" + EMAIL_SUFFIX))
                    .triggerValid()
                    .assertValid((controllerTest, response) -> responseValidator(
                        response,
                        "test_court_standard",
                        BureauJwtPayload.builder()
                            .owner("415")
                            .locCode("462")
                            .email("test_court_standard" + EMAIL_SUFFIX)
                            .login("test_court_standard")
                            .userLevel("1")
                            .userType(UserType.COURT)
                            .activeUserType(UserType.COURT)
                            .roles(List.of())
                            .staff(BureauJwtPayload.Staff.builder()
                                .rank(1)
                                .name("Court Standard")
                                .active(1)
                                .courts(List.of("415", "462", "767"))
                                .build())
                            .build()
                    ));
            }

            @Test
            void bureau() {
                testBuilder()
                    .url(toUrl("400"))
                    .payload(new EmailDto("test_bureau_standard" + EMAIL_SUFFIX))
                    .triggerValid()
                    .assertValid((controllerTest, response) -> responseValidator(
                        response,
                        "test_bureau_standard",
                        BureauJwtPayload.builder()
                            .owner("400")
                            .email("test_bureau_standard" + EMAIL_SUFFIX)
                            .locCode("400")
                            .login("test_bureau_standard")
                            .userLevel("0")
                            .userType(UserType.BUREAU)
                            .activeUserType(UserType.BUREAU)
                            .roles(Set.of())
                            .staff(BureauJwtPayload.Staff.builder()
                                .name("Bureau Standard")
                                .rank(0)
                                .active(1)
                                .courts(List.of("400"))
                                .build())
                            .build()));
            }

            @Test
            void adminBureau() {
                testBuilder()
                    .url(toUrl("400"))
                    .payload(new EmailDto("test_admin_standard" + EMAIL_SUFFIX))
                    .triggerValid()
                    .assertValid((controllerTest, response) -> responseValidator(
                        response,
                        "test_admin_standard",
                        BureauJwtPayload.builder()
                            .owner("400")
                            .locCode("400")
                            .email("test_admin_standard" + EMAIL_SUFFIX)
                            .login("test_admin_standard")
                            .userLevel("0")
                            .userType(UserType.ADMINISTRATOR)
                            .activeUserType(UserType.BUREAU)
                            .roles(List.of(Role.values()))
                            .staff(BureauJwtPayload.Staff.builder()
                                .rank(0)
                                .name("Admin Standard")
                                .active(1)
                                .courts(List.of("400"))
                                .build())
                            .build()));
            }

            @Test
            void adminCourt() {
                testBuilder()
                    .url(toUrl("415"))
                    .payload(new EmailDto("test_admin_standard" + EMAIL_SUFFIX))
                    .triggerValid()
                    .assertValid((controllerTest, response) -> responseValidator(
                        response,
                        "test_admin_standard",
                        BureauJwtPayload.builder()
                            .owner("415")
                            .locCode("415")
                            .email("test_admin_standard" + EMAIL_SUFFIX)
                            .login("test_admin_standard")
                            .userLevel("0")
                            .userType(UserType.ADMINISTRATOR)
                            .activeUserType(UserType.COURT)
                            .roles(List.of(Role.values()))
                            .staff(BureauJwtPayload.Staff.builder()
                                .rank(0)
                                .name("Admin Standard")
                                .active(1)
                                .courts(List.of("415", "462", "767"))
                                .build())
                            .build()));
            }

            @Test
            void adminAdmin() {
                testBuilder()
                    .url(toUrl("ADMIN"))
                    .payload(new EmailDto("test_admin_standard" + EMAIL_SUFFIX))
                    .triggerValid()
                    .assertValid((controllerTest, response) -> responseValidator(
                        response,
                        "test_admin_standard",
                        BureauJwtPayload.builder()
                            .owner("400")
                            .locCode("400")
                            .email("test_admin_standard" + EMAIL_SUFFIX)
                            .login("test_admin_standard")
                            .userLevel("0")
                            .userType(UserType.ADMINISTRATOR)
                            .activeUserType(UserType.ADMINISTRATOR)
                            .roles(List.of(Role.values()))
                            .staff(BureauJwtPayload.Staff.builder()
                                .rank(0)
                                .name("Admin Standard")
                                .active(1)
                                .courts(List.of("400"))
                                .build())
                            .build()));
            }


            @Test
            void withRoles() {
                testBuilder()
                    .url(toUrl("415"))
                    .payload(new EmailDto("test_court_sjo_mangr" + EMAIL_SUFFIX))
                    .triggerValid()
                    .assertValid((controllerTest, response) -> responseValidator(
                        response,
                        "test_court_sjo_mangr",
                        BureauJwtPayload.builder()
                            .owner("415")
                            .locCode("415")
                            .email("test_court_sjo_mangr" + EMAIL_SUFFIX)
                            .login("test_court_sjo_mangr")
                            .userLevel("9")
                            .userType(UserType.COURT)
                            .activeUserType(UserType.COURT)
                            .roles(List.of(Role.MANAGER, Role.SENIOR_JUROR_OFFICER))
                            .staff(BureauJwtPayload.Staff.builder()
                                .rank(9)
                                .name("Court SJO & Manager")
                                .active(1)
                                .courts(List.of("415", "462", "767"))
                                .build())
                            .build()));
            }
        }

        @DisplayName("Negative")
        @Nested
        class Negative {
            @Test
            void invalidLocationCode() {
                testBuilder()
                    .url(toUrl("INVALID"))
                    .triggerInvalid()
                    .assertInvalidPathParam("createJwt.locCode: must match \"^\\d{3}$|^ADMIN$\"");
            }

            @Test
            void invalidJwtUsingBureauJwt() {
                testBuilder()
                    .jwt(createJwt("test_court_standard", "415"))
                    .triggerInvalid()
                    .assertInvalidJwtAuthenticationException();
            }

            @Test
            void userNotFound() {
                testBuilder()
                    .payload(new EmailDto("not_found" + EMAIL_SUFFIX))
                    .triggerInvalid()
                    .assertNotFound("User not found");
            }

            @Test
            void userNotInCourt() {
                testBuilder()
                    .url(toUrl("414"))
                    .triggerInvalid()
                    .assertMojForbiddenResponse("User not part of court");
            }

            @Test
            void userNotActive() {
                testBuilder()
                    .payload(new EmailDto("test_court_inactive" + EMAIL_SUFFIX))
                    .triggerInvalid()
                    .assertMojForbiddenResponse("User is not active");
            }

            @Test
            void courtNotFound() {
                testBuilder()
                    .url(toUrl("666"))
                    .triggerInvalid()
                    .assertNotFound("Court not found");
            }
        }
    }
}
