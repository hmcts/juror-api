package uk.gov.hmcts.juror.api.juror.controller;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for log sink endpoint.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PublicFrontendLogControllerTest {
    private static final String HMAC_HEADER_VALID = "eyJhbGciOiJIUzI1NiJ9" +
        ".eyJleHAiOjM0NTIwMTk4MzM1MCwiaWF0IjoxNDg2NTY5MzEyMDQzfQ.XT6K5HDAxX57hg9eW3ZWqv57_p5lqptgBfJVreBQD9Y";
    private static final String HMAC_HEADER_INVALID = "eyJhbGciOiJIUzI1NiJ9" +
        ".eyJleHAiOjM0NTIwMTk4MzM1MSwiaWF0IjoxNDg2NTY5MzEyMDQzfQ.XT6K5HDAxX57hg9eW3ZWqv57_p5lqptgBfJVreBQD9Y";
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
    public void log_happy() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, HMAC_HEADER_VALID);
        final URI uri = URI.create("/api/v1/auth/public/log");
        final String TEST_LOG_MESSAGE = "Hello world log message!";

        RequestEntity<String> requestEntity = new RequestEntity<>(TEST_LOG_MESSAGE, httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<Object> exchange = template.exchange(requestEntity, Object.class);
        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(exchange.getBody()).isNull();
    }

    @Test
    public void log_unhappy() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, HMAC_HEADER_INVALID);
        final URI uri = URI.create("/api/v1/auth/public/log");
        final String TEST_LOG_MESSAGE = "Hello world log message!";

        RequestEntity<String> requestEntity = new RequestEntity<>(TEST_LOG_MESSAGE, httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<Object> exchange = template.exchange(requestEntity, Object.class);
        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isNotEqualTo(HttpStatus.NO_CONTENT);
    }
}