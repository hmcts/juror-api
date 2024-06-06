package uk.gov.hmcts.juror.api.bureau.controller;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauBacklogCountData;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;

import java.net.URI;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link BureauBacklogCountControllerTest}.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BureauBacklogCountControllerTest extends AbstractIntegrationTest {

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
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauBacklogCountService_BacklogCount.sql")
    public void bureauBacklogCount_happy() {
        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("99")
            .login("ncrawford")
            .owner("400")
            .build());


        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        // assert db state before.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE where "
            + "PROCESSING_STATUS = 'TODO' and STAFF_LOGIN IS NULL ", Integer.class)).isEqualTo(7);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE where "
                + "PROCESSING_STATUS = 'TODO' and STAFF_LOGIN IS NULL and URGENT = 'Y'",
            Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE where "
            + "PROCESSING_STATUS != 'TODO' ", Integer.class)).isEqualTo(1);


        URI uri = URI.create("/api/v1/bureau/backlog/count");

        RequestEntity<BureauBacklogCountData> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);

        ResponseEntity<BureauBacklogCountData> exchange = template.exchange(requestEntity,
            BureauBacklogCountData.class);
        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody().getNonUrgent()).isEqualTo(4);
        assertThat(exchange.getBody().getUrgent()).isEqualTo(3);
    }


}
