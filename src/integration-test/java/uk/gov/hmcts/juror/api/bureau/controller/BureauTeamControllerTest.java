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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.bureau.controller.response.TeamDto;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link BureauTeamControllerTest}.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BureauTeamControllerTest extends AbstractIntegrationTest {

    @Value("${jwt.secret.bureau}")
    private String bureauSecret;

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
    @Sql("/db/BureauTeamServicesTest_findAllTeams.sql")
    public void getAllTeams_happyPath() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("rprice")
            .staff(BureauJWTPayload.Staff.builder().name("Roxanne Price").active(1).rank(1).build())
            .daysToExpire(89)
            .owner("400")
            .build());

        final URI uri = URI.create("/api/v1/bureau/team");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        ParameterizedTypeReference<List<TeamDto>> typeRef =
            new ParameterizedTypeReference<List<TeamDto>>() {
            };

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<List<TeamDto>> response = template.exchange(requestEntity, typeRef);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isInstanceOf(ArrayList.class);
        assertThat(response.getBody().get(0).getId()).isEqualTo(1);
        assertThat(response.getBody().get(0).getName()).isEqualTo("LONDON & WALES");
        assertThat(response.getBody().get(1).getId()).isEqualTo(2);
        assertThat(response.getBody().get(1).getName()).isEqualTo("MIDLANDS");

    }

}
