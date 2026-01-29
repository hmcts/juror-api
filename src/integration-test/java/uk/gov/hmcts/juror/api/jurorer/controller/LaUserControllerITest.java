package uk.gov.hmcts.juror.api.jurorer.controller;

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
import uk.gov.hmcts.juror.api.moj.domain.authentication.EmailDto;
import uk.gov.hmcts.juror.api.moj.domain.authentication.JwtDto;

import java.time.Clock;
import java.util.Collections;

import static org.springframework.http.HttpMethod.POST;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + LaUserControllerITest.BASE_URL)
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Sql(value = {"/db/jurorer/teardownUsers.sql", "/db/jurorer/createUsers.sql"})
@SuppressWarnings({
    "PMD.JUnitTestsShouldIncludeAssert",
    "PMD.ExcessiveImports",
    "PMD.JUnitAssertionsShouldIncludeMessage"//False positive
})
public class LaUserControllerITest extends AbstractIntegrationTest {
    public static final String BASE_URL = "/api/v1/auth/juror-er/users";

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
    @DisplayName("GET Juror ER users")
    class CreateJwt extends AbstractControllerIntegrationTest<EmailDto, JwtDto> {
        private static final String URL = BASE_URL + "/001";

        protected CreateJwt() {
            super(POST, template, HttpStatus.OK);
        }

        @Override
        protected String getValidUrl() {
            return URL;
        }

        @Override
        protected String getValidJwt() {

            // create a valid juror ER JWT payload


            return mintJurorErJwt();
        }

        //dont need a payload builder as only one field
        @Override
        protected EmailDto getValidPayload() {
            return new EmailDto("test_user1");
        }

        @DisplayName("Positive")
        @Nested
        class Positive {


            @Test
            void primaryCourt() {
                testBuilder()
                    .payload(new EmailDto("test_user1"))
                    .url(URL)
                    .triggerValid()
                    .assertValid();
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
                    .payload(new EmailDto("not_found"))
                    .triggerInvalid()
                    .assertNotFound("User not found");
            }

            @Test
            void userNotActive() {
                testBuilder()
                    .payload(new EmailDto("test_court_inactive"))
                    .triggerInvalid()
                    .assertMojForbiddenResponse("User is not active");
            }

        }
    }
}
