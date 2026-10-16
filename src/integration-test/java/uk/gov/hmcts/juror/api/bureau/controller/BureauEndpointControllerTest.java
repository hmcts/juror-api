package uk.gov.hmcts.juror.api.bureau.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
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
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseSummaryWrapper;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;

import java.net.URI;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Bureau endpoint controller integration tests.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BureauEndpointControllerTest extends AbstractIntegrationTest {
    @Autowired
    private TestRestTemplate template;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private HttpHeaders httpHeaders;

    @Value("${jwt.secret.bureau}")
    private String bureauSecret;

    @Before
    public void setUp() throws Exception {
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Test
    public void bureauAuthenticationEndpoint_unhappy_header1() {
        final String description = "Authentication header is not present";

        ResponseEntity<String> exchange = template.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
            URI.create("/api/v1/bureau/settings")), String.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).describedAs(description).isNotEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody()).describedAs(description).asString().isNotEmpty()
            .doesNotContain("Hello Bureau JWT!");
    }

    @Test
    public void bureauAuthenticationEndpoint_unhappy_header2() {
        final String description = "Authentication header is empty";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, null);
        ResponseEntity<String> exchange = template.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
            URI.create("/api/v1/bureau/settings")), String.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).describedAs(description).isNotEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody()).describedAs(description).asString().isNotEmpty()
            .doesNotContain("Hello Bureau JWT!");
    }

    @Test
    public void bureauAuthenticationEndpoint_unhappy_header3() throws Exception {
        final String description = "Authentication header is invalid";

        final String publicJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("99")
            .login("testlogin")
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .build());

        final String[] jwtSections = publicJwt.split("\\.");
        final String invalidPublicJwt = String.join(".", jwtSections[0], "eyJhZG1pbiI6ICJ0cnVlIn0", jwtSections[2]);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, invalidPublicJwt);
        ResponseEntity<String> exchange = template.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
            URI.create("/api/v1/bureau/settings")), String.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).describedAs(description).isNotEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody()).describedAs(description).asString().isNotEmpty()
            .doesNotContain("Hello Bureau JWT!");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauRepository_findByJurorNumber.sql")
    public void filterBureauDetailsByStatus_WithValidCategoryFilter_ReturnsResponsesForStatusAndCount() {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("1")
            .login("testlogin")
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .build())
        );

        ResponseEntity<BureauResponseSummaryWrapper> response = template.exchange(new RequestEntity<>(httpHeaders,
                HttpMethod.GET, URI.create("/api/v1/bureau/responses?filterBy=todo")),
            BureauResponseSummaryWrapper.class);
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody().getResponses()).hasSize(3);
        assertThat(response.getBody().getTodoCount()).isEqualTo(3);
        assertThat(response.getBody().getRepliesPendingCount()).isEqualTo(4);
        assertThat(response.getBody().getCompletedCount()).isEqualTo(1);

    }
}
