package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.CoronerPoolFilterRequestQuery;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolSearchRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestSearchListDto;
import uk.gov.hmcts.juror.api.moj.domain.FilterCoronerPool;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;

import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the API endpoints defined in {@link PoolSearchController}.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PoolSearchControllerITest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private HttpHeaders httpHeaders;

    @Override
    @BeforeEach
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

    @Nested
    class PoolRequestSearchTests {

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initPoolRequests.sql"})
        void searchPoolRequestsBureauUser() {
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
        void searchPoolRequestsCourtUserWithAccess() throws Exception {
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
        void searchPoolRequestsCourtUserWithoutAccess() throws Exception {
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
        void searchPoolRequestsBureauUserInvalidSearchCriteria() throws Exception {
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
        void searchPoolRequestsCourtUserNoResults() throws Exception {
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
        void searchPoolRequestsInvalidPoolNumberTooShort() throws Exception {
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
        void searchPoolRequestsInvalidPoolNumberTooLong() throws Exception {
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

    @Nested
    class CoronerPoolRequestSearchTests {

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initCoronerPoolRequests.sql"})
        void searchCoronerPoolRequestsHappy() {
            CoronerPoolFilterRequestQuery request = CoronerPoolFilterRequestQuery.builder()
                .poolNumber("923040001")
                .locationCode("415")
                .pageNumber(1)
                .pageLimit(10)
                .sortMethod(SortMethod.ASC)
                .sortField(CoronerPoolFilterRequestQuery.SortField.POOL_NUMBER)
                .build();

            ResponseEntity<PaginatedList<FilterCoronerPool>> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, HttpMethod.POST,
                    URI.create("/api/v1/moj/pool-search/coroner-pools")), new ParameterizedTypeReference<>() {});

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request (GET With Body) to be successful")
                .isEqualTo(HttpStatus.OK);

            assertThat(response.getBody()).isNotNull();
            PaginatedList<FilterCoronerPool> responseBody = response.getBody();

            assertThat(responseBody.getTotalItems()).as("Expect the response body to contain a total count value of 1")
                .isEqualTo(1);

            List<FilterCoronerPool> data = responseBody.getData();
            assertThat(data.size()).as("Expect the response body to contain all 1 data items").isEqualTo(1);
            FilterCoronerPool pool = data.get(0);
            assertThat(pool.getPoolNumber()).as("Expect the response body to contain the correct pool number")
                .isEqualTo("923040001");
            assertThat(pool.getCourtName()).as("Expect the response body to contain the correct court name")
                .isEqualTo("THE CROWN COURT AT CHESTER");
            assertThat(pool.getRequestedDate()).as("Expect the response body to contain the correct requested date")
                .isEqualTo(LocalDate.now().minusDays(10));
            assertThat(pool.getRequestedBy()).as("Expect the response body to contain the correct requested by")
                .isEqualTo("First Name");

        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initCoronerPoolRequests.sql"})
        void searchCoronerPoolRequestsBureauUserNoResults() {
            CoronerPoolFilterRequestQuery request = CoronerPoolFilterRequestQuery.builder()
                .poolNumber("111111111")
                .locationCode("555")
                .pageNumber(1)
                .pageLimit(10)
                .sortMethod(SortMethod.ASC)
                .sortField(CoronerPoolFilterRequestQuery.SortField.POOL_NUMBER)
                .build();

            ResponseEntity<PaginatedList<FilterCoronerPool>> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, HttpMethod.POST,
                    URI.create("/api/v1/moj/pool-search/coroner-pools")), new ParameterizedTypeReference<>() {});

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request (GET With Body) to be successful")
                .isEqualTo(HttpStatus.OK);

            assertThat(response.getBody()).isNotNull();
            PaginatedList<FilterCoronerPool> responseBody = response.getBody();

            assertThat(responseBody.getTotalItems()).as("Expect the response body to contain a no data")
                .isEqualTo(0);

        }

        @Test
        @Sql({"/db/mod/truncate.sql", "/db/PoolRequestSearchService_initCoronerPoolRequests.sql"})
        void searchCoronerPoolRequestsBureauUserInvalidSearchCriteria() {
            // Invalid search criteria
            CoronerPoolFilterRequestQuery request = CoronerPoolFilterRequestQuery.builder()
                .poolNumber("12345678910")
                .pageNumber(1)
                .pageLimit(10)
                .sortMethod(SortMethod.ASC)
                .sortField(CoronerPoolFilterRequestQuery.SortField.POOL_NUMBER)
                .build();

            ResponseEntity<PaginatedList<FilterCoronerPool>> response =
                restTemplate.exchange(new RequestEntity<>(request, httpHeaders, HttpMethod.POST,
                    URI.create("/api/v1/moj/pool-search/coroner-pools")), new ParameterizedTypeReference<>() {});

            assertThat(response.getStatusCode())
                .as("Expect the HTTP POST request (GET With Body) to be BAD_REQUEST")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

}