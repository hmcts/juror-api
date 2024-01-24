package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolHistoryListDto;
import uk.gov.hmcts.juror.api.moj.repository.PoolHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

/**
 * Integration tests for the Pool History controller.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PoolHistoryControllerITest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private PoolRequestRepository poolRequestRepository;
    @Autowired
    private PoolHistoryRepository poolHistoryRepository;


    private HttpHeaders httpHeaders;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        initHeaders();

    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/PoolHistoryController_createInitialPoolHistories.sql"})
    public void getPoolHistory_Happy_Path() {
        ResponseEntity<PoolHistoryListDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, GET,
                URI.create("/api/v1/moj/pool-history/111111111")), PoolHistoryListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(OK);

        PoolHistoryListDto poolHistoryListDto = response.getBody();

        assertThat(poolHistoryListDto).isNotNull();
        assertThat(poolHistoryListDto.getData().size()).isEqualTo(2);
    }


    @Test
    @Sql({"/db/mod/truncate.sql","/db/PoolHistoryController_createInitialPoolHistories.sql"})
    public void getPoolHistory_BureauUser_PoolRequestDoesNotExist() {

        ResponseEntity<PoolHistoryListDto> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, GET,
                URI.create("/api/v1/moj/pool-history/123456789")), PoolHistoryListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP PUT request to be NOT_FOUND")
            .isEqualTo(NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/PoolHistoryController_createInitialPoolHistories.sql"})
    public void getPoolHistory_CourtUser_NoAccess() throws Exception {
        String poolNumber = "111111111";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Collections.singletonList("415")));

        ResponseEntity<PoolHistoryListDto> response =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, GET,
                URI.create("/api/v1/moj/pool-history/" + poolNumber)), PoolHistoryListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP GET request to be FORBIDDEN")
            .isEqualTo(FORBIDDEN);
    }

    private void initHeaders() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("BUREAU_USER")
            .daysToExpire(89)
            .owner("400")
            .build());

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    private String initCourtsJwt(String owner, List<String> courts) throws Exception {

        return mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("COURT_USER")
            .daysToExpire(89)
            .owner(owner)
            .staff(BureauJWTPayload.Staff.builder().courts(courts).build())
            .build());
    }
}
