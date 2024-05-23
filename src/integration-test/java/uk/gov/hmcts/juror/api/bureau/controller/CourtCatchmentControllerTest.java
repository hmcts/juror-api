package uk.gov.hmcts.juror.api.bureau.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import uk.gov.hmcts.juror.api.bureau.controller.response.CourtCatchmentStatusDto;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;

import java.net.URI;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link CourtCatchmentControllerTest}.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CourtCatchmentControllerTest extends AbstractIntegrationTest {

    @Value("${jwt.secret.bureau}")
    private String bureauSecret;

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

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauResponseCourtCatchmentController.sql")
    public void courtCatchment_Changed() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("99")
            .login("ncrawford")
            .owner("400")
            .build());


        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        // assert db state before.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT ZIP FROM JUROR.POOL WHERE PART_NO = '209092530'",
            String.class)).isEqualTo("AB39RY");

        assertThat(jdbcTemplate.queryForObject("SELECT ZIP FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = "
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
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauResponseCourtCatchmentController.sql")
    public void courtCatchment_Unchanged() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("99")
            .login("ncrawford")
            .owner("400")
            .build());


        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        // assert db state.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT ZIP FROM JUROR.POOL WHERE PART_NO = '586856851'",
            String.class)).isEqualTo("CF62SW");

        assertThat(jdbcTemplate.queryForObject("SELECT ZIP FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = "
            + "'586856851'", String.class)).isEqualTo("CF86HA");


        URI uri = URI.create("/api/v1/bureau/juror/court/catchment/586856851");

        RequestEntity<CourtCatchmentStatusDto> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);

        ResponseEntity<CourtCatchmentStatusDto> exchange = template.exchange(requestEntity,
            CourtCatchmentStatusDto.class);
        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody().getCourtCatchmentStatus()).isEqualTo("Unchanged");

    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauResponseCourtCatchmentController_Unhappy.sql")
    public void courtCatchment_Loc_Code_Not_Found_Unhappy() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("99")
            .login("ncrawford")
            .owner("400")
            .build());

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        // assert db state.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT ZIP FROM JUROR.POOL WHERE PART_NO = '586856851'",
            String.class)).isEqualTo("CF62SW");

        assertThat(jdbcTemplate.queryForObject("SELECT ZIP FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = "
            + "'586856851'", String.class)).isEqualTo("CF86HA");


        URI uri = URI.create("/api/v1/bureau/juror/court/catchment/209092530");

        RequestEntity<CourtCatchmentStatusDto> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);

        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            new ParameterizedTypeReference<SpringBootErrorResponse>() {
            });

        assertThat(exchange.getBody().getMessage()).isEqualToIgnoringCase("loc_code not found");
        assertThat(exchange).describedAs("invalid").isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);


    }


}
