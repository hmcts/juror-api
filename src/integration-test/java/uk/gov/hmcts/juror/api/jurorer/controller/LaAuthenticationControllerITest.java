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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractControllerIntegrationTest;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.config.jurorer.JurorErJwtPayload;
import uk.gov.hmcts.juror.api.jurorer.domain.LaRoles;
import uk.gov.hmcts.juror.api.jurorer.domain.LocalAuthority;
import uk.gov.hmcts.juror.api.moj.domain.authentication.EmailDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.JwtDto;

import java.time.Clock;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + LaAuthenticationControllerITest.BASE_URL)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SuppressWarnings({"PMD.JUnitAssertionsShouldIncludeMessage", "PMD.JUnitTestsShouldIncludeAssert"}) // false positive
@Sql({"/db/jurorer/teardownUsers.sql", "/db/jurorer/createUsers.sql"})
public class LaAuthenticationControllerITest extends AbstractIntegrationTest {
    public static final String BASE_URL = "/api/v1/auth/juror-er";
    private static final String EMAIL_SUFFIX = "@localauthority1.council.uk";

    private final TestRestTemplate template;
    private HttpHeaders httpHeaders;

    @Value("${jwt.secret.er-portal}")
    private String erPortalSecret;

    private final Clock clock;

    @BeforeEach
    public void setUp() throws Exception {
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }


    @Nested
    @DisplayName("GET Juror ER JWT")
    class CreateJwt extends AbstractControllerIntegrationTest<EmailDto, JwtDto> {
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
        protected EmailDto getValidPayload() {
            return new EmailDto("test_user1" + EMAIL_SUFFIX);
        }


        @DisplayName("Positive")
        @Nested
        class Positive {

            private void responseValidator(JwtDto response,
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
                    .hasSize(8)
                    .containsEntry("username", expectedJwtClaims.getUsername())
                    .containsEntry("laCode", expectedJwtClaims.getLaCode())
                    .containsEntry("laName", expectedJwtClaims.getLaName())
                    .containsEntry("role", List.of(LaRoles.LA_USER.toString()));
            }

            @Test
            void primaryCourt() {
                testBuilder()
                    .payload(new EmailDto("test_user1" + EMAIL_SUFFIX))
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
            void userNotFound() {
                testBuilder()
                    .url(URL + "/001")
                    .payload(new EmailDto("not_found" + EMAIL_SUFFIX))
                    .triggerInvalid()
                    .assertNotFound("User not found");
            }

            @Test
            void userNotActive() {
                testBuilder()
                    .url(URL + "/004")
                    .payload(new EmailDto("test_user1@localauthority4.council.uk"))
                    .triggerInvalid()
                    .assertMojForbiddenResponse("User is not active");
            }

        }
    }


    @Nested
    @DisplayName("GET local authorities for user")
    class GetLocalAuthorities extends AbstractControllerIntegrationTest<EmailDto, List<LocalAuthority>> {
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
        protected EmailDto getValidPayload() {
            return new EmailDto("test_user1" + EMAIL_SUFFIX);
        }


        @DisplayName("Positive")
        @Nested
        class Positive {

            @Test
            void singleCourt() {
                testBuilder()
                    .payload(new EmailDto("test_user1" + EMAIL_SUFFIX))
                    .triggerValid()
                    .assertValid((controllerTest, response) -> assertThat(response)
                        .hasSize(1)
                        .extracting(LocalAuthority::getLaCode)
                        .containsExactly("001"));
            }

            @Test
            void twoCourts() {
                testBuilder()
                    .payload(new EmailDto("test_user3@localauthority2.council.uk"))
                    .triggerValid()
                    .assertValid((controllerTest, response) -> assertThat(response)
                        .hasSize(2)
                        .extracting(LocalAuthority::getLaCode)
                        .containsExactlyInAnyOrder("002", "003"));
            }

            @Test
            void twoCourtsOneActive() {
                testBuilder()
                    .payload(new EmailDto("test_user4@another.council.uk"))
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
                    .payload(new EmailDto("test_user1@localauthority4.council.uk"))
                    .triggerValid()
                    .assertValid((controllerTest, response) -> assertThat(response)
                        .hasSize(0));
            }

            @Test
            void userNotFound() {
                testBuilder()
                    .payload(new EmailDto("test_user1@somela.council.uk"))
                    .triggerInvalid()
                    .assertNotFound("User not found");
            }

        }
    }
}
