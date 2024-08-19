package uk.gov.hmcts.juror.api.bureau.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.SpringBootErrorResponse;
import uk.gov.hmcts.juror.api.bureau.controller.response.CourtCatchmentStatusDto;

import java.net.URI;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link CourtCatchmentControllerTest}.
 */

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CourtCatchmentControllerTest extends AbstractIntegrationTest {


    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestRestTemplate template;

    private HttpHeaders httpHeaders;

    @BeforeEach
    void setUp() throws Exception {
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Test
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauResponseCourtCatchmentController.sql")
    void courtCatchmentChanged() throws Exception {
        final String bureauJwt = createJwtBureau("ncrawford");


        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        // assert db state before.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class))
            .isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_pool", Integer.class))
            .isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT postcode FROM juror_mod.juror WHERE juror_number = '209092530'",
            String.class)).isEqualTo("AB39RY");

        assertThat(jdbcTemplate.queryForObject("SELECT postcode FROM juror_mod.JUROR_RESPONSE WHERE JUROR_NUMBER = "
            + "'209092530'", String.class)).isEqualTo("RG16HA");


        URI uri = URI.create("/api/v1/bureau/juror/court/catchment/209092530");

        RequestEntity<CourtCatchmentStatusDto> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);

        ResponseEntity<CourtCatchmentStatusDto> exchange = template.exchange(requestEntity,
            CourtCatchmentStatusDto.class);
        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody().getCourtCatchmentStatus()).isEqualTo("Changed");

    }

    @Test
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauResponseCourtCatchmentController.sql")
    void courtCatchmentUnchanged() throws Exception {
        final String bureauJwt = createJwtBureau("ncrawford");


        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        // assert db state.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_pool", Integer.class)).isEqualTo(
            4);
        assertThat(jdbcTemplate.queryForObject("SELECT postcode FROM juror_mod.juror WHERE juror_number = '586856851'",
            String.class)).isEqualTo("CF62SW");

        assertThat(jdbcTemplate.queryForObject("SELECT postcode FROM juror_mod.juror_response "
            + "WHERE JUROR_NUMBER = '586856851'", String.class)).isEqualTo("CF86HA");


        URI uri = URI.create("/api/v1/bureau/juror/court/catchment/586856851");

        RequestEntity<CourtCatchmentStatusDto> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);

        ResponseEntity<CourtCatchmentStatusDto> exchange = template.exchange(requestEntity,
            CourtCatchmentStatusDto.class);
        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody().getCourtCatchmentStatus()).isEqualTo("Unchanged");

    }

    @Test
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauResponseCourtCatchmentController_Unhappy.sql")
    void courtCatchmentLocCodeNotFoundUnhappy() throws Exception {
        final String bureauJwt = createJwtBureau("ncrawford");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        // assert db state.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class))
            .isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_pool", Integer.class))
            .isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT postcode FROM juror_mod.juror WHERE juror_number = '586856851'",
            String.class)).isEqualTo("CF62SW");

        assertThat(jdbcTemplate.queryForObject("SELECT postcode FROM juror_mod.juror_response WHERE JUROR_NUMBER = "
            + "'586856851'", String.class)).isEqualTo("CF86HA");


        URI uri = URI.create("/api/v1/bureau/juror/court/catchment/209092530");

        RequestEntity<CourtCatchmentStatusDto> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);

        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            new ParameterizedTypeReference<>() {
            });

        assertThat(exchange.getBody().getMessage()).isEqualToIgnoringCase("loc_code not found");
        assertThat(exchange).describedAs("invalid").isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);


    }


}
