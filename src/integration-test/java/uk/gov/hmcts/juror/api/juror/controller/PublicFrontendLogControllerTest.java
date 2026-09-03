package uk.gov.hmcts.juror.api.juror.controller;

import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.TestUtil;
import uk.gov.hmcts.juror.api.testsupport.ContainerTest;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for log sink endpoint.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PublicFrontendLogControllerTest extends ContainerTest {
    @Value("${jwt.secret.hmac}")
    private String hmacSecret;

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
        httpHeaders.set(HttpHeaders.AUTHORIZATION, createHmacJwt());
        final URI uri = URI.create("/api/v1/auth/public/log");
        final String testLogMessage = "Hello world log message!";

        RequestEntity<String> requestEntity = new RequestEntity<>(testLogMessage, httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<Object> exchange = template.exchange(requestEntity, Object.class);
        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(exchange.getBody()).isNull();
    }

    @Test
    public void log_unhappy() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, createHmacJwt() + "invalid");
        final URI uri = URI.create("/api/v1/auth/public/log");
        final String testLogMessage = "Hello world log message!";

        RequestEntity<String> requestEntity = new RequestEntity<>(testLogMessage, httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<Object> exchange = template.exchange(requestEntity, Object.class);
        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isNotEqualTo(HttpStatus.NO_CONTENT);
    }

    private String createHmacJwt() {
        return TestUtil.mintHmacJwt(SignatureAlgorithm.HS256, hmacSecret,
            Instant.now().plus(100L * 365L, ChronoUnit.DAYS));
    }
}
