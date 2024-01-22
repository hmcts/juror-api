package uk.gov.hmcts.juror.api.bureau.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.bureau.scheduler.BureauBatchScheduler;

import java.net.URI;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Integration test for {@link JurorCommsCronBatchController}.
*/

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JurorCommsCronBatchControllerTest extends AbstractIntegrationTest {
    private static final String HMAC_HEADER_VALID = "eyJhbGciOiJIUzI1NiJ9" +
        ".eyJleHAiOjM0NTIwMTk4MzM1MCwiaWF0IjoxNDg2NTY5MzEyMDQzfQ.XT6K5HDAxX57hg9eW3ZWqv57_p5lqptgBfJVreBQD9Y";

    private static final String[] TYPES = {"letterComms"};
    private HttpHeaders httpHeaders;

    @Autowired
    private TestRestTemplate template;


    @MockBean
    BureauBatchScheduler bureauBatchScheduler;

    @Autowired
    JurorCommsCronBatchController jurorCommsCronBatchController;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        httpHeaders.set(HttpHeaders.AUTHORIZATION, HMAC_HEADER_VALID);
    }

    @Test
    public void jurorCommsCronBatch_happy() throws Exception {
        final URI uri = URI.create("/api/v1/bureau/cron?types=" + TYPES);

        RequestEntity<Object> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<Object> response = jurorCommsCronBatchController.callBureauBatchScheduler(TYPES, null, null);

        verify(bureauBatchScheduler, times(1)).processBatchJobServices(TYPES, null, null);

        assertThat(response).isNotNull();
    }
}
