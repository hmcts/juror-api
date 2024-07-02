package uk.gov.hmcts.juror.api.bureau.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.SpringBootErrorResponse;
import uk.gov.hmcts.juror.api.config.InvalidJwtAuthenticationException;

import java.net.URI;
import java.util.Collections;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Application settings endpoint controller integration tests.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApplicationSettingsControllerIntegrationTest extends AbstractIntegrationTest {
    private static final String HMAC_HEADER_VALID = "eyJhbGciOiJIUzI1NiJ9"
        + ".eyJleHAiOjM0NTIwMTk4MzM1MCwiaWF0IjoxNDg2NTY5MzEyMDQzfQ.XT6K5HDAxX57hg9eW3ZWqv57_p5lqptgBfJVreBQD9Y";
    private static final String HMAC_HEADER_INVALID = "eyJhbGciOiJIUzI1NiJ9"
        + ".eyJleHAiOjM0NTIwMTk4MzM1MSwiaWF0IjoxNDg2NTY5MzEyMDQzfQ.XT6K5HDAxX57hg9eW3ZWqv57_p5lqptgBfJVreBQD9Y";
    //payload (second section) has been modified

    @Autowired
    private TestRestTemplate template;

    private HttpHeaders httpHeaders;

    @Before
    public void setUp() throws Exception {
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    public void applicationSettings_happy() {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, HMAC_HEADER_VALID);

        ResponseEntity<String> responseEntity = template.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
            URI.create("/api/v1/auth/settings")), String.class);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThatJson(responseEntity.getBody())
            .node("data")
            .isArray();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/app_settings.sql")
    public void applicationSettings_unhappy() {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, HMAC_HEADER_INVALID);

        ResponseEntity<SpringBootErrorResponse> responseEntity =
            template.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/auth/settings")), SpringBootErrorResponse.class);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getBody().getException()).isEqualTo(
            InvalidJwtAuthenticationException.class.getCanonicalName());
    }
}