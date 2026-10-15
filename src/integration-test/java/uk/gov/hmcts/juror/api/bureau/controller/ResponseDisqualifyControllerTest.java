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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseDisqualifyController.DisqualifyCodeDto;
import uk.gov.hmcts.juror.api.bureau.controller.ResponseDisqualifyController.DisqualifyReasonsDto;
import uk.gov.hmcts.juror.api.moj.enumeration.DisqualifyCode;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for {@link ResponseDisqualifyController}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "feature-flags.flags.digital-by-default=true")
public class ResponseDisqualifyControllerTest extends AbstractIntegrationTest {

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
    public void testGDisqualifyReasons_happy() {
        final String loginName = "testlogin";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, createJwtBureau(loginName));

        ResponseEntity<DisqualifyReasonsDto> responseEntity = template.exchange(new RequestEntity<>(httpHeaders,
            HttpMethod.GET, URI.create("/api/v1/bureau/juror/disqualify")), DisqualifyReasonsDto.class);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode().value()).isEqualTo(HttpStatus.OK.value());
        assertThat(responseEntity.getBody()).isNotNull();

        List<DisqualifyCodeDto> data = responseEntity.getBody().getData();
        assertThat(data.size()).isGreaterThan(0);

        // JDB-1458: unable to assert actual list of reasons as they may change, but we know 'E' shouldn't be in it
        assertThat(data)
            .as("Ensure Disqualify code is not E")
            .filteredOn("disqualifyCode", DisqualifyCode.E.getCode())
            .isEmpty();
    }
}
