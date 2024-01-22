package uk.gov.hmcts.juror.api.config;

import org.junit.Before;
import org.junit.Ignore;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.util.Collections;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SecurityConfigTest {
    private static final String HMAC_HEADER_VALID = "eyJhbGciOiJIUzI1NiJ9"
        + ".eyJleHAiOjM0NTIwMTk4MzM1MCwiaWF0IjoxNDg2NTY5MzEyMDQzfQ.XT6K5HDAxX57hg9eW3ZWqv57_p5lqptgBfJVreBQD9Y";

    @SuppressWarnings("SpringJavaAutowiredMembersInspection")
    @Autowired
    private TestRestTemplate testRestTemplate;

    private HttpHeaders httpHeaders;

    @Before
    public void setUp() throws Exception {
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Test
    public void hmacLogin_healthEndpoint_happy() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, HMAC_HEADER_VALID);
        ResponseEntity<String> exchange = testRestTemplate.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create("/health")), String.class);
        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThatJson(exchange.getBody()).node("status").isStringEqualTo("UP");
    }

    @Test
    @Ignore("Enable when actuator endpoint is secured.")
    public void hmacLogin_healthEndpoint_unhappy_no_token() throws Exception {
        ResponseEntity<String> exchange = testRestTemplate.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create("/health")), String.class);
        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isNotEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody()).asString().isNotEmpty();
    }
}
