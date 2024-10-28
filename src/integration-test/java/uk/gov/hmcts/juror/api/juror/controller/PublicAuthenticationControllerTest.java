package uk.gov.hmcts.juror.api.juror.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.SpringBootErrorResponse;
import uk.gov.hmcts.juror.api.TestUtil;
import uk.gov.hmcts.juror.api.config.InvalidJwtAuthenticationException;
import uk.gov.hmcts.juror.api.juror.controller.PublicAuthenticationController.PublicAuthenticationRequestDto;
import uk.gov.hmcts.juror.api.juror.controller.PublicAuthenticationController.PublicAuthenticationResponseDto;
import uk.gov.hmcts.juror.api.juror.service.PublicAuthenticationServiceImpl;

import java.net.URI;
import java.util.Collections;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Public authentication Integration Tests.
 */
@SuppressWarnings("Duplicates")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PublicAuthenticationControllerTest extends AbstractIntegrationTest {
    private static final String HMAC_HEADER_VALID = "eyJhbGciOiJIUzI1NiJ9"
        + ".eyJleHAiOjM0NTIwMTk4MzM1MCwiaWF0IjoxNDg2NTY5MzEyMDQzfQ.XT6K5HDAxX57hg9eW3ZWqv57_p5lqptgBfJVreBQD9Y";
    private static final String HMAC_HEADER_INVALID = "eyJhbGciOiJIUzI1NiJ9"
        + ".eyJleHAiOjM0NTIwMTk4MzM1MSwiaWF0IjoxNDg2NTY5MzEyMDQzfQ.XT6K5HDAxX57hg9eW3ZWqv57_p5lqptgBfJVreBQD9Y";
    //payload (second section) has been modified

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private JdbcTemplate jdbcTemplate;

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
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicAuthenticationControllerTest.publicAuthenticationEndpoint_happy.sql")
    public void publicAuthenticationEndpointHappy() throws Exception {
        final String jurorNumber = "644892530";
        final String lastName = "Castillo";
        final String postcode = "AB3 9RY";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, HMAC_HEADER_VALID);
        URI uri = URI.create("/api/v1/auth/juror");

        PublicAuthenticationRequestDto requestDto = PublicAuthenticationRequestDto.builder()
            .jurorNumber(jurorNumber)
            .lastName(lastName)
            .postcode(postcode)
            .build();

        RequestEntity<PublicAuthenticationRequestDto> requestEntity = new RequestEntity<>(requestDto,
            httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<PublicAuthenticationResponseDto> exchange = template.exchange(requestEntity,
            new ParameterizedTypeReference<>() {
            });
        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        final String responseJson = TestUtil.parseToJsonString(exchange.getBody());
        assertThatJson(responseJson).node("jurorNumber").isStringEqualTo(jurorNumber);
        assertThatJson(responseJson).node("lastName").isStringEqualTo(lastName.toUpperCase());
        assertThatJson(responseJson).node("postcode").isStringEqualTo(postcode.toUpperCase());
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicAuthenticationControllerTest.publicAuthenticationEndpoint_happy.sql")
    public void publicAuthenticationEndpointHappyNoSpacePostcode() throws Exception {
        final String jurorNumber = "644892530";
        final String lastName = "Castillo";
        final String postcode = "AB39RY";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, HMAC_HEADER_VALID);
        URI uri = URI.create("/api/v1/auth/juror");

        PublicAuthenticationRequestDto requestDto = PublicAuthenticationRequestDto.builder()
            .jurorNumber(jurorNumber)
            .lastName(lastName)
            .postcode(postcode)
            .build();

        RequestEntity<PublicAuthenticationRequestDto> requestEntity = new RequestEntity<>(requestDto,
            httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<PublicAuthenticationResponseDto> exchange = template.exchange(requestEntity,
            new ParameterizedTypeReference<>() {});

        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        final String responseJson = TestUtil.parseToJsonString(exchange.getBody());
        assertThatJson(responseJson).node("jurorNumber").isStringEqualTo(jurorNumber);
        assertThatJson(responseJson).node("lastName").isStringEqualTo(lastName.toUpperCase());
        assertThatJson(responseJson).node("postcode").isStringEqualTo("AB3 9RY");// matches database value with space
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    public void publicAuthenticationEndpointUnhappyHeader1() throws Exception {
        final String description = "Authentication header is not present";

        final String jurorNumber = "644892530";
        final String lastName = "Castillo";
        final String postcode = "AB3 9RY";

        URI uri = URI.create("/api/v1/auth/juror");

        PublicAuthenticationRequestDto requestDto = PublicAuthenticationRequestDto.builder()
            .jurorNumber(jurorNumber)
            .lastName(lastName)
            .postcode(postcode)
            .build();

        RequestEntity<PublicAuthenticationRequestDto> requestEntity = new RequestEntity<>(requestDto,
            httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            new ParameterizedTypeReference<>() {});

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isNotEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody().getStatus()).isNotEqualTo(HttpStatus.OK.value())
            .isEqualTo(exchange.getStatusCode().value());
        assertThat(exchange.getBody().getException()).isEqualTo(InvalidJwtAuthenticationException.class.getName());
        assertThat(exchange.getBody().getMessage()).isEqualTo("Authentication header may not be empty!");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    public void publicAuthenticationEndpointUnhappyHeader2() throws Exception {
        final String description = "Authentication header is empty";

        final String jurorNumber = "644892530";
        final String lastName = "Castillo";
        final String postcode = "AB3 9RY";

        URI uri = URI.create("/api/v1/auth/juror");

        PublicAuthenticationRequestDto requestDto = PublicAuthenticationRequestDto.builder()
            .jurorNumber(jurorNumber)
            .lastName(lastName)
            .postcode(postcode)
            .build();

        httpHeaders.set(HttpHeaders.AUTHORIZATION, null);
        RequestEntity<PublicAuthenticationRequestDto> requestEntity = new RequestEntity<>(requestDto,
            httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            new ParameterizedTypeReference<SpringBootErrorResponse>() {
            });

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isNotEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody().getStatus()).isNotEqualTo(HttpStatus.OK.value())
            .isEqualTo(exchange.getStatusCode().value());
        assertThat(exchange.getBody().getException()).isEqualTo(InvalidJwtAuthenticationException.class.getName());
        assertThat(exchange.getBody().getMessage()).isEqualTo("Authentication header may not be empty!");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    public void publicAuthenticationEndpointUnhappyHeader3() throws Exception {
        final String description = "Authentication header is invalid";

        final String jurorNumber = "644892530";
        final String lastName = "Castillo";
        final String postcode = "AB3 9RY";

        URI uri = URI.create("/api/v1/auth/juror");

        PublicAuthenticationRequestDto requestDto = PublicAuthenticationRequestDto.builder()
            .jurorNumber(jurorNumber)
            .lastName(lastName)
            .postcode(postcode)
            .build();

        httpHeaders.set(HttpHeaders.AUTHORIZATION, HMAC_HEADER_INVALID);
        RequestEntity<PublicAuthenticationRequestDto> requestEntity = new RequestEntity<>(requestDto,
            httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            new ParameterizedTypeReference<>() {});

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isNotEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody().getStatus()).isNotEqualTo(HttpStatus.OK.value())
            .isEqualTo(exchange.getStatusCode().value());
        assertThat(exchange.getBody().getException()).isEqualTo(InvalidJwtAuthenticationException.class.getName());
        assertThat(exchange.getBody().getMessage()).isEqualTo("Failed to parse JWT");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicAuthenticationControllerTest.publicAuthenticationEndpoint_happy.sql")
    public void publicAuthenticationEndpoint_unhappy_invalidCredentials() throws Exception {
        final String description = "Invalid credentials";

        final String jurorNumber = "644892530";
        final String lastName = "Castilloo";// invalid, should be "Castillo"
        final String postcode = "AB3 9RY";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, HMAC_HEADER_VALID);
        URI uri = URI.create("/api/v1/auth/juror");

        PublicAuthenticationRequestDto requestDto = PublicAuthenticationRequestDto.builder()
            .jurorNumber(jurorNumber)
            .lastName(lastName)
            .postcode(postcode)
            .build();

        RequestEntity<PublicAuthenticationRequestDto> requestEntity = new RequestEntity<>(requestDto,
            httpHeaders, HttpMethod.POST, uri);


        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            new ParameterizedTypeReference<SpringBootErrorResponse>() {
            });

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getBody().getStatus()).isNotEqualTo(HttpStatus.OK.value())
            .isEqualTo(exchange.getStatusCode().value());
        assertThat(exchange.getBody().getException()).isEqualTo(
            PublicAuthenticationServiceImpl.InvalidJurorCredentialsException.class.getName());
        assertThat(exchange.getBody().getMessage()).isEqualTo("Invalid credentials");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicAuthenticationControllerTest.publicAuthenticationEndpoint_happy.sql")
    public void publicAuthenticationEndpoint_unhappy_invalidCredentialsLockout() throws Exception {
        final String description = "Invalid credentials";

        final String jurorNumber = "644892530";
        final String lastName = "Castilloo";// invalid, should be "Castillo"
        final String postcode = "AB3 9RY";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, HMAC_HEADER_VALID);
        URI uri = URI.create("/api/v1/auth/juror");

        PublicAuthenticationRequestDto requestDto = PublicAuthenticationRequestDto.builder()
            .jurorNumber(jurorNumber)
            .lastName(lastName)
            .postcode(postcode)
            .build();

        RequestEntity<PublicAuthenticationRequestDto> requestEntity = new RequestEntity<>(requestDto,
            httpHeaders, HttpMethod.POST, uri);

        // there is no login counter for any user.
        assertThat(jdbcTemplate.queryForObject("select sum(login_attempts) from juror_mod.juror",
            Integer.class)).isEqualTo(0);

        // failed login 1
        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            new ParameterizedTypeReference<>() {
            });

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getBody().getStatus()).isNotEqualTo(HttpStatus.OK.value())
            .isEqualTo(exchange.getStatusCode().value());
        assertThat(exchange.getBody().getException()).isEqualTo(
            PublicAuthenticationServiceImpl.InvalidJurorCredentialsException.class.getName());
        assertThat(exchange.getBody().getMessage()).isEqualTo("Invalid credentials");

        // check that the login counter not exists.
        assertThat(jdbcTemplate.queryForObject("select login_attempts from juror_mod.juror "
                + "where juror_number = '644892530'",
            Integer.class)).isEqualTo(1);

        // failed login 2
        exchange = template.exchange(requestEntity,
            new ParameterizedTypeReference<>() {
            });

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getBody().getStatus()).isNotEqualTo(HttpStatus.OK.value())
            .isEqualTo(exchange.getStatusCode().value());
        assertThat(exchange.getBody().getException()).isEqualTo(
            PublicAuthenticationServiceImpl.InvalidJurorCredentialsException.class.getName());
        assertThat(exchange.getBody().getMessage()).isEqualTo("Invalid credentials");

        // check that the login counter is still there.
        assertThat(jdbcTemplate.queryForObject("select login_attempts from juror_mod.juror "
                + "where juror_number = '644892530'",
            Integer.class)).isEqualTo(2);

        // failed login 3
        exchange = template.exchange(requestEntity,
            new ParameterizedTypeReference<>() {
            });

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getBody().getStatus()).isNotEqualTo(HttpStatus.OK.value())
            .isEqualTo(exchange.getStatusCode().value());
        assertThat(exchange.getBody().getException()).isEqualTo(
            PublicAuthenticationServiceImpl.InvalidJurorCredentialsException.class.getName());
        assertThat(exchange.getBody().getMessage()).isEqualTo("Invalid credentials");

        // account should now be locked
        // the login attempt counter was reset after the account was locked
        assertThat(jdbcTemplate.queryForObject("select login_attempts from juror_mod.juror "
                + "where juror_number = '644892530'",
            Integer.class)).isEqualTo(0);
        // the account is indeed locked
        assertThat(
            jdbcTemplate.queryForObject("select is_locked from juror_mod.juror where juror_number = '644892530'",
                Boolean.class)).isEqualTo(true);

        // valid login should result in lock error
        requestDto = PublicAuthenticationRequestDto.builder()
            .jurorNumber(jurorNumber)
            .lastName("Castillo") // correct credentials
            .postcode(postcode)
            .build();

        requestEntity = new RequestEntity<>(requestDto,
            httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<SpringBootErrorResponse> exchangeLocked = template.exchange(requestEntity,
            new ParameterizedTypeReference<>() {
            });

        assertThat(exchangeLocked).isNotNull();
        assertThat(exchangeLocked.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exchangeLocked.getBody().getStatus()).isNotEqualTo(HttpStatus.OK.value())
            .isEqualTo(exchangeLocked.getStatusCode().value());
        assertThat(exchangeLocked.getBody().getException()).isEqualTo(
            PublicAuthenticationServiceImpl.JurorAccountBlockedException.class.getName());
        assertThat(exchangeLocked.getBody().getMessage()).isEqualTo("Juror account is locked");

        // failed login count should have been cleared
        assertThat(jdbcTemplate.queryForObject("select sum(login_attempts) from juror_mod.juror",
            Integer.class)).isEqualTo(0);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicAuthenticationControllerTest.publicAuthenticationEndpoint_unhappy_alreadyResponded.sql")
    public void publicAuthenticationEndpoint_unhappy_alreadyResponded() throws Exception {
        final String description = "Already responded";

        final String jurorNumber = "644892530";
        final String lastName = "Castillo";
        final String postcode = "AB3 9RY";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, HMAC_HEADER_VALID);
        URI uri = URI.create("/api/v1/auth/juror");

        PublicAuthenticationRequestDto requestDto = PublicAuthenticationRequestDto.builder()
            .jurorNumber(jurorNumber)
            .lastName(lastName)
            .postcode(postcode)
            .build();

        RequestEntity<PublicAuthenticationRequestDto> requestEntity = new RequestEntity<>(requestDto,
            httpHeaders, HttpMethod.POST, uri);


        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            new ParameterizedTypeReference<>() {
            });

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exchange.getBody().getStatus()).isNotEqualTo(HttpStatus.OK.value())
            .isEqualTo(exchange.getStatusCode().value());
        assertThat(exchange.getBody().getException()).isEqualTo(
            PublicAuthenticationServiceImpl
                .JurorAlreadyRespondedException.class.getName());
        assertThat(exchange.getBody().getMessage()).isEqualTo("Juror already responded");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/PublicAuthenticationControllerTest.publicAuthenticationEndpoint_unhappy_courtDatePassed.sql")
    public void publicAuthenticationEndpoint_unhappy_courtDatePassed() throws Exception {
        final String description = "Court Date Passed";

        final String jurorNumber = "644892530";
        final String lastName = "Castillo";
        final String postcode = "AB3 9RY";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, HMAC_HEADER_VALID);
        URI uri = URI.create("/api/v1/auth/juror");

        PublicAuthenticationRequestDto requestDto = PublicAuthenticationRequestDto.builder()
            .jurorNumber(jurorNumber)
            .lastName(lastName)
            .postcode(postcode)
            .build();

        RequestEntity<PublicAuthenticationRequestDto> requestEntity = new RequestEntity<>(requestDto,
            httpHeaders, HttpMethod.POST, uri);


        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            new ParameterizedTypeReference<>() {
            });

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exchange.getBody().getStatus()).isNotEqualTo(HttpStatus.OK.value())
            .isEqualTo(exchange.getStatusCode().value());
        assertThat(exchange.getBody().getException()).isEqualTo(
            PublicAuthenticationServiceImpl.CourtDateLapsedException.class.getName());
        assertThat(exchange.getBody().getMessage()).isEqualTo("Not allowed. Court Date has already passed");
    }

}
