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
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauOfficerAllocatedData;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauOfficerAllocatedResponses;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link BureauBacklogCountControllerTest}.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BureauOfficerAllocatedRepliesControllerTest extends AbstractIntegrationTest {

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
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauOfficerAllocateRepliesService_BacklogData.sql")
    public void bureauAllocationReplies_happy() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userType(UserType.BUREAU)
            .roles(Set.of(Role.MANAGER))
            .login("ncrawford")
            .owner("400")
            .build());


        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        // assert db state before.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE where "
            + "PROCESSING_STATUS = 'TODO' and STAFF_LOGIN IS NULL ", Integer.class)).isEqualTo(7);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE where "
                + "PROCESSING_STATUS = 'TODO' and STAFF_LOGIN IS NULL and URGENT = 'Y' AND SUPER_URGENT='N' ",
            Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE where "
                + "PROCESSING_STATUS = 'TODO' and STAFF_LOGIN IS NULL and (URGENT = 'Y' OR SUPER_URGENT='Y') ",
            Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE where "
                + "PROCESSING_STATUS = 'TODO' and STAFF_LOGIN IS NULL and URGENT = 'N' and  SUPER_URGENT='Y' ",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.JUROR_RESPONSE where "
            + "PROCESSING_STATUS != 'TODO' ", Integer.class)).isEqualTo(2);


        URI uri = URI.create("/api/v1/bureau/allocate/replies");

        RequestEntity<BureauOfficerAllocatedResponses> requestEntity = new RequestEntity<>(httpHeaders,
            HttpMethod.GET, uri);

        ResponseEntity<BureauOfficerAllocatedResponses> exchange = template.exchange(requestEntity,
            BureauOfficerAllocatedResponses.class);


        assertThat(exchange).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody().getBureauBacklogCount().getNonUrgent()).isEqualTo(4);
        assertThat(exchange.getBody().getBureauBacklogCount().getUrgent()).isEqualTo(3);

        assertThat(exchange.getBody().getData().size()).isEqualTo(6);

        List<BureauOfficerAllocatedData> carneson =
            exchange.getBody().getData().stream().filter(r -> "carneson".equals(r.getLogin()))
                .toList();
        assertThat(carneson.size()).isEqualTo(1);
        assertThat(carneson.get(0).getName()).isEqualToIgnoringCase("Chad Arneson");
        assertThat(carneson.get(0).getAllReplies()).isEqualTo(8);
        assertThat(carneson.get(0).getUrgent()).isEqualTo(6);
        assertThat(carneson.get(0).getNonUrgent()).isEqualTo(2);

        List<BureauOfficerAllocatedData> mruby =
            exchange.getBody().getData().stream().filter(r -> "mruby".equals(r.getLogin()))
                .toList();
        assertThat(mruby.size()).isEqualTo(1);
        assertThat(mruby.get(0).getName()).isEqualToIgnoringCase("Martin Ruby");
        assertThat(mruby.get(0).getAllReplies()).isEqualTo(4);
        assertThat(mruby.get(0).getUrgent()).isEqualTo(2);
        assertThat(mruby.get(0).getNonUrgent()).isEqualTo(2);

        exchange.getBody().getData()
            .stream()
            .filter(r -> !("carneson".equals(r.getLogin()) || "mruby".equals(r.getLogin())))
            .forEach(bureauOfficerAllocatedData -> {
                assertThat(bureauOfficerAllocatedData.getAllReplies()).isEqualTo(0);
                assertThat(bureauOfficerAllocatedData.getUrgent()).isEqualTo(0);
                assertThat(bureauOfficerAllocatedData.getNonUrgent()).isEqualTo(0);
            });
    }
}
