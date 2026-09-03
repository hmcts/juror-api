package uk.gov.hmcts.juror.api.bureau.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
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
        httpHeaders.set(HttpHeaders.AUTHORIZATION, createHmacJwt());

        ResponseEntity<String> responseEntity = template.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
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
        httpHeaders.set(HttpHeaders.AUTHORIZATION, createHmacJwt() + "invalid");

        ResponseEntity<SpringBootErrorResponse> responseEntity =
            template.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/auth/settings")), SpringBootErrorResponse.class);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getBody().getException()).isEqualTo(
            InvalidJwtAuthenticationException.class.getCanonicalName());
    }
}
