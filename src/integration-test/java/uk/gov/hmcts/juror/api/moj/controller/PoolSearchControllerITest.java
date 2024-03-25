package uk.gov.hmcts.juror.api.moj.controller;

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
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolSearchRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestSearchListDto;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the API endpoints defined in {@link PoolSearchController}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PoolSearchControllerITest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private HttpHeaders httpHeaders;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        initHeaders();
    }

    private void initHeaders() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
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

        return mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("COURT_USER")
            .daysToExpire(89)
            .owner(owner)
            .staff(BureauJwtPayload.Staff.builder().courts(courts).build())
            .build());
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchPoolRequests_bureauUser() {
        PoolSearchRequestDto request = createRequest(null, "417");

        ResponseEntity<PoolRequestSearchListDto> responseEntity =
            restTemplate.exchange(new RequestEntity<>(request, httpHeaders, HttpMethod.POST,
                URI.create("/api/v1/moj/pool-search")), PoolRequestSearchListDto.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expect the HTTP POST request (GET With Body) to be successful. "
                + "Because we want this POST request to transport a GET request semantic, "
                + "a '200' response code is expected instead of a '201'")
            .isEqualTo(HttpStatus.OK);

        PoolRequestSearchListDto responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getResultsCount())
            .as("Expect the response body to contain a total count value of 7")
            .isEqualTo(7);
        assertThat(responseBody.getData().size())
            .as("Expect the response body to contain all 7 data items")
            .isEqualTo(7);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchPoolRequests_courtUser_withAccess() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("417", Collections.singletonList("417")));
        PoolSearchRequestDto request = createRequest(null, "417");

        ResponseEntity<PoolRequestSearchListDto> responseEntity =
            restTemplate.exchange(new RequestEntity<PoolSearchRequestDto>(request, httpHeaders, HttpMethod.POST,
                URI.create("/api/v1/moj/pool-search")), PoolRequestSearchListDto.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expect the HTTP POST request (GET With Body) to be successful. "
                + "Because we want this POST request to transport a GET request semantic, "
                + "a '200' response code is expected instead of a '201'")
            .isEqualTo(HttpStatus.OK);

        PoolRequestSearchListDto responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getResultsCount())
            .as("Expect the response body to contain a total count value of 7")
            .isEqualTo(7);
        assertThat(responseBody.getData().size())
            .as("Expect the response body to contain all 7 data items")
            .isEqualTo(7);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchPoolRequests_courtUser_withoutAccess() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Arrays.asList("415", "416")));
        PoolSearchRequestDto request = createRequest(null, "417");

        ResponseEntity<PoolRequestSearchListDto> responseEntity =
            restTemplate.exchange(new RequestEntity<PoolSearchRequestDto>(request, httpHeaders, HttpMethod.POST,
                URI.create("/api/v1/moj/pool-search")), PoolRequestSearchListDto.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expect the HTTP POST request (GET With Body) to be unsuccessful")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchPoolRequests_bureauUser_invalidSearchCriteria() throws Exception {
        PoolSearchRequestDto request = createRequest(null, null);

        ResponseEntity<PoolRequestSearchListDto> responseEntity =
            restTemplate.exchange(new RequestEntity<PoolSearchRequestDto>(request, httpHeaders, HttpMethod.POST,
                URI.create("/api/v1/moj/pool-search")), PoolRequestSearchListDto.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expect the HTTP POST request (GET With Body) to be unsuccessful")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchPoolRequests_courtUser_noResults() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("417", Collections.singletonList("417")));
        PoolSearchRequestDto request = createRequest("417221201", "417");

        ResponseEntity<PoolRequestSearchListDto> responseEntity =
            restTemplate.exchange(new RequestEntity<PoolSearchRequestDto>(request, httpHeaders, HttpMethod.POST,
                URI.create("/api/v1/moj/pool-search")), PoolRequestSearchListDto.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expect the HTTP POST request (GET With Body) to be successful. "
                + "Because we want this POST request to transport a GET request semantic, "
                + "a '200' response code is expected instead of a '201'")
            .isEqualTo(HttpStatus.OK);

        PoolRequestSearchListDto responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getResultsCount())
            .as("Expect the response body to contain a total count value of 0")
            .isEqualTo(0);
        assertThat(responseBody.getData().isEmpty())
            .as("Expect the response body to contain an empty array of data items")
            .isTrue();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchPoolRequests_invalidPoolNumber_tooShort() throws Exception {
        PoolSearchRequestDto request = createRequest("41", null);

        ResponseEntity<PoolRequestSearchListDto> responseEntity =
            restTemplate.exchange(new RequestEntity<PoolSearchRequestDto>(request, httpHeaders, HttpMethod.POST,
                URI.create("/api/v1/moj/pool-search")), PoolRequestSearchListDto.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expect the HTTP POST request (GET With Body) to be unsuccessful")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
    public void test_searchPoolRequests_invalidPoolNumber_tooLong() throws Exception {
        PoolSearchRequestDto request = createRequest("4172212010", null);

        ResponseEntity<PoolRequestSearchListDto> responseEntity =
            restTemplate.exchange(new RequestEntity<PoolSearchRequestDto>(request, httpHeaders, HttpMethod.POST,
                URI.create("/api/v1/moj/pool-search")), PoolRequestSearchListDto.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expect the HTTP POST request (GET With Body) to be unsuccessful")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private PoolSearchRequestDto createRequest(String poolNumber, String locCode) {
        PoolSearchRequestDto poolSearchRequestDto = new PoolSearchRequestDto();
        poolSearchRequestDto.setOffset(0);
        poolSearchRequestDto.setPoolNumber(poolNumber);
        poolSearchRequestDto.setLocCode(locCode);
        return poolSearchRequestDto;
    }

}