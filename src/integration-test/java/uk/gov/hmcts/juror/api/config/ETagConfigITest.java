package uk.gov.hmcts.juror.api.config;

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
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestItemDto;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;

import java.net.URI;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the eTag Header.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ETagConfigITest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private PoolRequestRepository poolRequestRepository;

    private HttpHeaders httpHeaders;

    private final URI uri = URI.create("/api/v1/moj/pool-create/pool?poolNumber=415220110&owner=400");

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON_UTF8));
        String bureauJwt = createJwt("rprice", "400");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/CreatePoolController_requestPoolDetails.sql"})
    public void etagHeaderHappy() throws Exception {

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<PoolRequestItemDto> response = template.exchange(requestEntity, PoolRequestItemDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getHeaders().get("ETag").get(0)).isNotNull();
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/CreatePoolController_requestPoolDetails.sql"})
    public void etagHeaderResourceNotModified() throws Exception {

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<PoolRequestItemDto> response = template.exchange(requestEntity, PoolRequestItemDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        String etagValue = response.getHeaders().get("ETag").get(0);
        assertThat(etagValue).isNotNull();

        httpHeaders.set("If-None-Match", etagValue);

        requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        response = template.exchange(requestEntity, PoolRequestItemDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_MODIFIED);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @Sql({"/db/mod/truncate.sql","/db/CreatePoolController_requestPoolDetails.sql"})
    public void etagHeaderResourceModified() throws Exception {

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<PoolRequestItemDto> response = template.exchange(requestEntity, PoolRequestItemDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        String etagValue = response.getHeaders().get("ETag").get(0);
        assertThat(etagValue).isNotNull();

        PoolRequest poolRequest = RepositoryUtils.retrieveFromDatabase("415220110", poolRequestRepository);
        poolRequest.setReturnDate(poolRequest.getReturnDate().plusDays(1));
        poolRequestRepository.save(poolRequest);

        httpHeaders.set("If-None-Match", etagValue);
        requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        response = template.exchange(requestEntity, PoolRequestItemDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

}
