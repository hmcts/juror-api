package uk.gov.hmcts.juror.api.jurorer.controller;

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
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractControllerIntegrationTest;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.config.jurorer.JurorErJwtPayload;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaDevLoginDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaEmailDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaJwtDto;
import uk.gov.hmcts.juror.api.jurorer.controller.dto.LaObjectIdDto;
import uk.gov.hmcts.juror.api.jurorer.domain.LaRoles;
import uk.gov.hmcts.juror.api.jurorer.domain.LocalAuthority;
import uk.gov.hmcts.juror.api.moj.exception.RestResponseEntityExceptionHandler;

import java.time.Clock;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + LaAuthenticationControllerITest.BASE_URL)
@Sql({"/db/jurorer/teardownUsers.sql", "/db/jurorer/createUsers.sql"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SuppressWarnings("PMD.ExcessiveImports")
public class LaAuthenticationControllerITest extends AbstractIntegrationTest {
    public static final String BASE_URL = "/api/v1/auth/juror-er";

    @Autowired
    private TestRestTemplate template;

    @Value("${jwt.secret.er-portal}")
    private String erPortalSecret;

    private final Clock clock;

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    @BeforeEach
    public void setUp() throws Exception {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }


    @Nested
    @DisplayName("POST dev login")
    class DevLogin extends AbstractControllerIntegrationTest<LaDevLoginDto, LaObjectIdDto> {

        protected DevLogin() {
            super(POST, template, HttpStatus.OK);
        }

        @Override
        protected String getValidUrl() {
            return BASE_URL + "/dev-login";
        }

        @Override
        protected String getValidJwt() {
            return createHmacJwt();
        }

        @Override
        protected LaDevLoginDto getValidPayload() {
            return new LaDevLoginDto("test_user1@localauthority1.council.uk");
        }

        @Test
        void returnsObjectId() {
            testBuilder()
                .payload(new LaDevLoginDto("TEST_USER1@LOCALAUTHORITY1.COUNCIL.UK"))
                .triggerValid()
                .assertValid((controllerTest, response) ->
                    assertThat(response.getObjectId()).isEqualTo("11111111-1111-1111-1111-111111111111"));
        }

        @Test
        void unknownEmail() {
            testBuilder()
                .payload(new LaDevLoginDto("unknown@example.com"))
                .triggerInvalid()
                .assertNotFound("User object ID not found");
        }

        @Test
        void rejectsBureauJwt() {
            testBuilder()
                .jwt(createJwt("test_court_standard", "415"))
                .triggerInvalid()
                .assertInvalidJwtAuthenticationException();
        }

        @Test
        void missingEmail() {
            testBuilder()
                .payload(new LaDevLoginDto(null))
                .triggerInvalid()
                .assertInvalidPayload(new RestResponseEntityExceptionHandler.FieldError(
                    "email", "must not be blank"));
        }

        @Test
        void invalidEmail() {
            testBuilder()
                .payload(new LaDevLoginDto("invalid"))
                .triggerInvalid()
                .assertInvalidPayload(new RestResponseEntityExceptionHandler.FieldError(
                    "email", "must be a well-formed email address"));
        }
    }

    @Nested
    @DisplayName("GET Juror ER JWT")
    class CreateJwt extends AbstractControllerIntegrationTest<LaEmailDto, LaJwtDto> {
        private static final String URL = BASE_URL + "/jwt";

        protected CreateJwt() {
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
        protected LaEmailDto getValidPayload() {
            return new LaEmailDto("11111111-1111-1111-1111-111111111111");
        }


        @DisplayName("Positive")
        @Nested
        class Positive {

            private void responseValidator(LaJwtDto response,
                                           String username, JurorErJwtPayload expectedJwtClaims) {
                Jwt<JwsHeader, Claims> jwt = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(erPortalSecret)))
                    .build()
                    .parseSignedClaims(response.getJwt());

                JwsHeader header = jwt.getHeader();
                assertThat(header.isPayloadEncoded()).isTrue();
                assertThat(header.getAlgorithm()).isEqualTo("HS512");
                assertThat(header.getCompressionAlgorithm()).isNull();

                Claims claims = jwt.getPayload();

                assertThat(claims.getId()).isEqualTo(username);
                assertThat(claims.getIssuer()).isEqualTo("juror-api");
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
                    .hasSize(10)
                    .containsEntry("objectId", "11111111-1111-1111-1111-111111111111")
                    .containsEntry("username", expectedJwtClaims.getUsername())
                    .containsEntry("laCode", expectedJwtClaims.getLaCode())
                    .containsEntry("laName", expectedJwtClaims.getLaName())
                    .containsEntry("role", List.of(LaRoles.LA_USER.toString()))
                    .containsEntry("localAuthorities", List.of("001"));
            }

            private void responseValidatorLite(LaJwtDto response,
                                           String username, JurorErJwtPayload expectedJwtClaims) {
                Jwt<JwsHeader, Claims> jwt = Jwts.parser()
                    .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(erPortalSecret)))
                    .build()
                    .parseSignedClaims(response.getJwt());

                Claims claims = jwt.getPayload();

                assertThat(claims.getId()).isEqualTo(username);

                assertThat(claims)
                    .hasSize(10)
                    .containsEntry("objectId", "33333333-3333-3333-3333-333333333333")
                    .containsEntry("username", expectedJwtClaims.getUsername())
                    .containsEntry("laCode", expectedJwtClaims.getLaCode())
                    .containsEntry("laName", expectedJwtClaims.getLaName())
                    .containsEntry("role", List.of(LaRoles.LA_USER.toString()))
                    .containsEntry("localAuthorities", List.of("002", "003"));
            }

            @Test
            void primaryCourt() {
                testBuilder()
                    .payload(new LaEmailDto("11111111-1111-1111-1111-111111111111"))
                    .url(URL + "/001")
                    .triggerValid()
                    .assertValid((controllerTest, response) -> responseValidator(
                        response,
                        "test_user1@localauthority1.council.uk",
                        JurorErJwtPayload.builder()
                            .username("test_user1@localauthority1.council.uk")
                            .laCode("001")
                            .laName("West Oxfordshire")
                            .roles(List.of(LaRoles.LA_USER.toString()))
                            .localAuthorities(List.of("001"))
                            .build()
                    ));
            }

            @Test
            void multipleLas() {
                testBuilder()
                    .payload(new LaEmailDto("33333333-3333-3333-3333-333333333333"))
                    .url(URL + "/003")
                    .triggerValid()
                    .assertValid((controllerTest, response) -> responseValidatorLite(
                        response,
                        "test_user3@localauthority2.council.uk",
                        JurorErJwtPayload.builder()
                            .username("test_user3@localauthority2.council.uk")
                            .laCode("003")
                            .laName("Eastleigh")
                            .roles(List.of(LaRoles.LA_USER.toString()))
                            .localAuthorities(List.of("002", "003"))
                            .build()
                    ));
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
                    .assertInvalidJwtAuthenticationException();
            }

            @Test
            void missingObjectId() {
                testBuilder()
                    .url(BASE_URL + "/jwt/001")
                    .payload(new LaEmailDto(null))
                    .triggerInvalid()
                    .assertInvalidPayload(new RestResponseEntityExceptionHandler.FieldError(
                        "objectId", "must not be blank"));
            }

            @Test
            void localAuthorityNotAssigned() {
                testBuilder()
                    .url(BASE_URL + "/jwt/002")
                    .payload(new LaEmailDto("11111111-1111-1111-1111-111111111111"))
                    .triggerInvalid()
                    .assertNotFound("User not found");
            }

            @Test
            void userNotFound() {
                testBuilder()
                    .url(URL + "/001")
                    .payload(new LaEmailDto("99999999-9999-9999-9999-999999999999"))
                    .triggerInvalid()
                    .assertNotFound("User not found");
            }

            @Test
            void userNotActive() {
                testBuilder()
                    .url(URL + "/004")
                    .payload(new LaEmailDto("55555555-5555-5555-5555-555555555555"))
                    .triggerInvalid()
                    .assertMojForbiddenResponse("User is not active");
            }

            @Test
            @SuppressWarnings("PMD.UnitTestShouldIncludeAssert")
            void invalidLaCode() {
                testBuilder()
                    .url(URL + "/0T1")
                    .payload(new LaEmailDto("55555555-5555-5555-5555-555555555555"))
                    .triggerInvalid();
            }

        }
    }


    @Nested
    @DisplayName("GET local authorities for user")
    class GetLocalAuthorities extends AbstractControllerIntegrationTest<LaEmailDto, List<LocalAuthority>> {
        private static final String URL = BASE_URL + "/local-authorities";

        GetLocalAuthorities() {
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
        protected LaEmailDto getValidPayload() {
            return new LaEmailDto("11111111-1111-1111-1111-111111111111");
        }


        @DisplayName("Positive")
        @Nested
        class Positive {

            @Test
            void singleCourt() {
                testBuilder()
                    .payload(new LaEmailDto("11111111-1111-1111-1111-111111111111"))
                    .triggerValid()
                    .assertValid((controllerTest, response) -> assertThat(response)
                        .hasSize(1)
                        .extracting(LocalAuthority::getLaCode)
                        .containsExactly("001"));
            }

            @Test
            void twoCourts() {
                testBuilder()
                    .payload(new LaEmailDto("33333333-3333-3333-3333-333333333333"))
                    .triggerValid()
                    .assertValid((controllerTest, response) -> assertThat(response)
                        .hasSize(2)
                        .extracting(LocalAuthority::getLaCode)
                        .containsExactlyInAnyOrder("002", "003"));
            }

            @Test
            void twoCourtsOneActive() {
                testBuilder()
                    .payload(new LaEmailDto("44444444-4444-4444-4444-444444444444"))
                    .triggerValid()
                    .assertValid((controllerTest, response) -> assertThat(response)
                        .hasSize(1)
                        .extracting(LocalAuthority::getLaCode)
                        .containsExactlyInAnyOrder("003"));
            }

        }


        @DisplayName("Negative")
        @Nested
        class Negative {

            @Test
            void singleCourtInactive() {
                testBuilder()
                    .payload(new LaEmailDto("55555555-5555-5555-5555-555555555555"))
                    .triggerValid()
                    .assertValid((controllerTest, response) -> assertThat(response)
                        .hasSize(0));
            }

            @Test
            void missingObjectId() {
                testBuilder()
                    .payload(new LaEmailDto(null))
                    .triggerInvalid()
                    .assertInvalidPayload(new RestResponseEntityExceptionHandler.FieldError(
                        "objectId", "must not be blank"));
            }

            @Test
            void userNotFound() {
                testBuilder()
                    .payload(new LaEmailDto("99999999-9999-9999-9999-999999999999"))
                    .triggerInvalid()
                    .assertNotFound("User not found");
            }

        }
    }
}
