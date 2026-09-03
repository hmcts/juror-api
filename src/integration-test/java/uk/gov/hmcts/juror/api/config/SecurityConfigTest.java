package uk.gov.hmcts.juror.api.config;

import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
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

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class SecurityConfigTest extends ContainerTest {
    @Value("${jwt.secret.hmac}")
    private String hmacSecret;

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
        httpHeaders.set(HttpHeaders.AUTHORIZATION, createHmacJwt());
        ResponseEntity<String> exchange = testRestTemplate.exchange(new RequestEntity<>(httpHeaders,
            HttpMethod.GET, URI.create("/health")), String.class);
        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThatJson(exchange.getBody()).node("status").isStringEqualTo("UP");
    }

    @Test
    @Ignore("Enable when actuator endpoint is secured.")
    public void hmacLogin_healthEndpoint_unhappy_no_token() throws Exception {
        ResponseEntity<String> exchange = testRestTemplate.exchange(new RequestEntity<>(httpHeaders,
            HttpMethod.GET, URI.create("/health")), String.class);
        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isNotEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody()).asString().isNotEmpty();
    }

    private String createHmacJwt() {
        return TestUtil.mintHmacJwt(SignatureAlgorithm.HS256, hmacSecret,
            Instant.now().plus(100L * 365L, ChronoUnit.DAYS));
    }
}
