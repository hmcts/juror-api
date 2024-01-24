package uk.gov.hmcts.juror.api.bureau.controller;

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

import java.net.URI;
import java.util.Collections;

import static net.javacrumbs.jsonunit.fluent.JsonFluentAssert.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for authentication endpoint on bureau.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BureauAuthenticationControllerTest extends AbstractIntegrationTest {
    private static final String HMAC_HEADER_VALID = "eyJhbGciOiJIUzI1NiJ9"
        + ".eyJleHAiOjM0NTIwMTk4MzM1MCwiaWF0IjoxNDg2NTY5MzEyMDQzfQ.XT6K5HDAxX57hg9eW3ZWqv57_p5lqptgBfJVreBQD9Y";
    private static final String HMAC_HEADER_INVALID = "eyJhbGciOiJIUzI1NiJ9"
        + ".eyJleHAiOjM0NTIwMTk4MzM1MSwiaWF0IjoxNDg2NTY5MzEyMDQzfQ.XT6K5HDAxX57hg9eW3ZWqv57_p5lqptgBfJVreBQD9Y";
    //payload (second section) has been modified

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestRestTemplate template;
    private HttpHeaders httpHeaders;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));


    }

    @Sql({
        "/db/mod/truncate.sql",
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauAuthenticationControllerTest.bureauAuthenticationEndpoint_happy.sql"
    })
    @Test
    public void bureauAuthenticationEndpoint_happy_multi_court() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, HMAC_HEADER_VALID);
        URI uri = URI.create("/api/v1/auth/bureau");

        BureauAuthenticationController.BureauAuthenticationRequestDto requestDto =
            BureauAuthenticationController.BureauAuthenticationRequestDto.builder()
                .userId("username")
                .password("password")
                .build();

        RequestEntity<BureauAuthenticationController.BureauAuthenticationRequestDto> requestEntity =
            new RequestEntity<>(requestDto,
                httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<BureauAuthenticationController.BureauAuthenticationResponseDto> exchange =
            template.exchange(requestEntity,
                new ParameterizedTypeReference<>() {
                });

        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody()).isInstanceOf(
            BureauAuthenticationController.BureauAuthenticationResponseDto.class);
        assertThat(exchange.getBody().getStaff().getCourts())
            .hasSize(3)
            .containsExactlyInAnyOrder("462", "767", "415");

        final String responseJson = TestUtil.parseToJsonString(exchange.getBody());
        assertThatJson(responseJson).node("login").isStringEqualTo("username");
        assertThatJson(responseJson).node("userLevel").isStringEqualTo("3");
        assertThatJson(responseJson).node("daysToExpire").isAbsent();
        assertThatJson(responseJson).node("passwordWarning").isAbsent();
        assertThatJson(responseJson).node("staff").isPresent();
    }

    @Sql({
        "/db/mod/truncate.sql",
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauAuthenticationControllerTest.bureauAuthenticationEndpoint_happy.sql"
    })
    @Test
    public void bureauAuthenticationEndpoint_happy_single_court() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, HMAC_HEADER_VALID);
        URI uri = URI.create("/api/v1/auth/bureau");

        BureauAuthenticationController.BureauAuthenticationRequestDto requestDto =
            BureauAuthenticationController.BureauAuthenticationRequestDto.builder()
                .userId("username4")
                .password("password")
                .build();

        RequestEntity<BureauAuthenticationController.BureauAuthenticationRequestDto> requestEntity =
            new RequestEntity<>(requestDto,
                httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<BureauAuthenticationController.BureauAuthenticationResponseDto> exchange =
            template.exchange(requestEntity,
                new ParameterizedTypeReference<>() {
                });

        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody()).isInstanceOf(
            BureauAuthenticationController.BureauAuthenticationResponseDto.class);
        assertThat(exchange.getBody().getStaff().getCourts())
            .hasSize(1)
            .containsExactlyInAnyOrder("400");

        final String responseJson = TestUtil.parseToJsonString(exchange.getBody());
        assertThatJson(responseJson).node("login").isStringEqualTo("username4");
        assertThatJson(responseJson).node("userLevel").isStringEqualTo("1");
        assertThatJson(responseJson).node("daysToExpire").isAbsent();
        assertThatJson(responseJson).node("passwordWarning").isAbsent();
        assertThatJson(responseJson).node("staff").isPresent();
    }

    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauAuthenticationControllerTest.bureauAuthenticationEndpoint_happy.sql"
    })
    @Test
    public void bureauAuthenticationEndpoint_happy_passwordWarning() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, HMAC_HEADER_VALID);
        URI uri = URI.create("/api/v1/auth/bureau");

        BureauAuthenticationController.BureauAuthenticationRequestDto requestDto =
            BureauAuthenticationController.BureauAuthenticationRequestDto.builder()
                .userId("username2")
                .password("password")
                .build();

        RequestEntity<BureauAuthenticationController.BureauAuthenticationRequestDto> requestEntity =
            new RequestEntity<>(requestDto,
                httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<BureauAuthenticationController.BureauAuthenticationResponseDto> exchange =
            template.exchange(requestEntity,
                new ParameterizedTypeReference<BureauAuthenticationController.BureauAuthenticationResponseDto>() {
                });

        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody()).isInstanceOf(
            BureauAuthenticationController.BureauAuthenticationResponseDto.class);
        final String responseJson = TestUtil.parseToJsonString(exchange.getBody());
        assertThatJson(responseJson).node("login").isStringEqualTo("username2");
        assertThatJson(responseJson).node("userLevel").isStringEqualTo("3");
        assertThatJson(responseJson).node("daysToExpire").isEqualTo(5);
        assertThatJson(responseJson).node("passwordWarning").isEqualTo(true);
    }

    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql"
    })
    @Test
    public void bureauAuthenticationEndpoint_unhappy_header1() throws Exception {
        final String description = "Authentication header is not present";

        ResponseEntity<String> exchange = template.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.POST,
            URI.create("/api/v1/auth/bureau")), String.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).describedAs(description).isNotEqualTo(HttpStatus.OK);
    }

    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql"
    })
    @Test
    public void bureauAuthenticationEndpoint_unhappy_header2() throws Exception {
        final String description = "Authentication header is empty";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, null);
        ResponseEntity<String> exchange = template.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.POST,
            URI.create("/api/v1/auth/bureau")), String.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).describedAs(description).isNotEqualTo(HttpStatus.OK);
    }

    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql"
    })
    @Test
    public void bureauAuthenticationEndpoint_unhappy_header3() throws Exception {
        final String description = "Authentication header is invalid";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, HMAC_HEADER_INVALID);
        ResponseEntity<String> exchange = template.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.POST,
            URI.create("/api/v1/auth/bureau")), String.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).describedAs(description).isNotEqualTo(HttpStatus.OK);
    }

    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauAuthenticationControllerTest.bureauAuthenticationEndpoint_happy.sql"
    })
    @Test
    public void bureauAuthenticationEndpoint_happy_passwordExpired() throws Exception {
        final String description = "Password expired";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, HMAC_HEADER_VALID);
        URI uri = URI.create("/api/v1/auth/bureau");

        BureauAuthenticationController.BureauAuthenticationRequestDto requestDto =
            BureauAuthenticationController.BureauAuthenticationRequestDto.builder()
                .userId("username3")
                .password("password")
                .build();

        RequestEntity<BureauAuthenticationController.BureauAuthenticationRequestDto> requestEntity =
            new RequestEntity<>(requestDto,
                httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            new ParameterizedTypeReference<SpringBootErrorResponse>() {
            });

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getBody().getStatus()).isNotEqualTo(HttpStatus.OK.value())
            .isEqualTo(exchange.getStatusCode().value());
        assertThat(exchange.getBody().getException()).isEqualTo(
            uk.gov.hmcts.juror.api.bureau.service.BureauAuthenticationServiceImpl
                .BureauPasswordExpiredException.class.getName());
        assertThat(exchange.getBody().getMessage()).isEqualTo("Password expired");
    }

    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauAuthenticationControllerTest.bureauAuthenticationEndpoint_happy.sql"
    })
    @Test
    public void bureauAuthenticationEndpoint_unhappy_invalidCredentials() throws Exception {
        final String description = "Invalid credentials";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, HMAC_HEADER_VALID);
        URI uri = URI.create("/api/v1/auth/bureau");

        BureauAuthenticationController.BureauAuthenticationRequestDto requestDto =
            BureauAuthenticationController.BureauAuthenticationRequestDto.builder()
                .userId("foo")
                .password("bar")
                .build();

        RequestEntity<BureauAuthenticationController.BureauAuthenticationRequestDto> requestEntity =
            new RequestEntity<>(requestDto,
                httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            new ParameterizedTypeReference<>() {
            });

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exchange.getBody().getStatus()).isNotEqualTo(HttpStatus.OK.value())
            .isEqualTo(exchange.getStatusCode().value());
        assertThat(exchange.getBody().getException()).isEqualTo(
            uk.gov.hmcts.juror.api.bureau.service.BureauAuthenticationServiceImpl
                .InvalidBureauCredentialsException.class.getName());
        assertThat(exchange.getBody().getMessage()).isEqualTo("Bad credentials");
    }


    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauAuthenticationControllerTest.bureauAuthenticationEndpoint_happy.sql"
    })
    @Test
    public void bureauAuthenticationEndpoint_unhappy_invalidCredentialsLockout() {
        final String description = "Invalid credentials";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, HMAC_HEADER_VALID);
        URI uri = URI.create("/api/v1/auth/bureau");

        BureauAuthenticationController.BureauAuthenticationRequestDto requestDto =
            BureauAuthenticationController.BureauAuthenticationRequestDto.builder()
                .userId("username")
                .password("bar")
                .build();

        RequestEntity<BureauAuthenticationController.BureauAuthenticationRequestDto> requestEntity =
            new RequestEntity<>(requestDto,
                httpHeaders, HttpMethod.POST, uri);

        // there is no login counter for any user.
        assertThat(
            jdbcTemplate.queryForObject("select failed_login_attempts from juror_mod.users where username= 'username'",
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
            uk.gov.hmcts.juror.api.bureau.service.BureauAuthenticationServiceImpl
                .InvalidBureauCredentialsException.class.getName());
        assertThat(exchange.getBody().getMessage()).isEqualTo("Invalid credentials");


        // check that the login counter not exists.
        assertThat(
            jdbcTemplate.queryForObject("select failed_login_attempts from juror_mod.users where username= 'username'",
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
            uk.gov.hmcts.juror.api.bureau.service.BureauAuthenticationServiceImpl
                .InvalidBureauCredentialsException.class.getName());
        assertThat(exchange.getBody().getMessage()).isEqualTo("Invalid credentials");

        // check that the login counter is still there.
        assertThat(
            jdbcTemplate.queryForObject("select failed_login_attempts from juror_mod.users where username= 'username'",
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
            uk.gov.hmcts.juror.api.bureau.service.BureauAuthenticationServiceImpl
                .InvalidBureauCredentialsException.class.getName());
        assertThat(exchange.getBody().getMessage()).isEqualTo("Invalid credentials");

        // account should now be locked
        // the login attempt counter was reset after the account was locked
        assertThat(
            jdbcTemplate.queryForObject("select failed_login_attempts from juror_mod.users where username= 'username'",
                Integer.class)).isEqualTo(0);
        // the account is indeed locked
        assertThat(
            jdbcTemplate.queryForObject("select login_enabled_yn from juror_mod.users where username = 'username'",
                Boolean.class)).isEqualTo(false);

        // valid login should result in lock error
        requestDto = BureauAuthenticationController.BureauAuthenticationRequestDto.builder()
            .userId("username")
            .password("password")
            .build();

        requestEntity = new RequestEntity<>(requestDto, httpHeaders, HttpMethod.POST, uri);

        ResponseEntity<SpringBootErrorResponse> exchangeLocked = template.exchange(requestEntity,
            new ParameterizedTypeReference<>() {
            });

        assertThat(exchangeLocked).isNotNull();
        assertThat(exchangeLocked.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exchangeLocked.getBody().getStatus()).isNotEqualTo(HttpStatus.OK.value())
            .isEqualTo(exchangeLocked.getStatusCode().value());
        assertThat(exchangeLocked.getBody().getException()).isEqualTo(
            uk.gov.hmcts.juror.api.bureau.service.BureauAuthenticationServiceImpl
                .BureauAccountLockedException.class.getName());
        assertThat(exchangeLocked.getBody().getMessage()).isEqualTo("Bureau account is locked");
    }
}