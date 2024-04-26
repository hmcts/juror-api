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
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.JudgeDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.JudgeListDto;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the Judge controller.
 */
@SuppressWarnings("java:S5960")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({"/db/mod/truncate.sql", "/db/trial/JudgeDetails.sql"})
public class JudgeControllerITest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private HttpHeaders httpHeaders;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        initHeadersCourt();
    }

    @Test
    public void getJudgesForCourtLocationsHappy() {
        ResponseEntity<JudgeListDto> responseEntity =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/trial/judge/list")), JudgeListDto.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expect the status to be OK.")
            .isEqualTo(HttpStatus.OK);

        JudgeListDto responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();

        assertThat(Objects.requireNonNull(responseBody).getJudges().size())
            .as("Expect the response body to contain details of 3 judges")
            .isEqualTo(3);

        List<JudgeDto> judges = responseBody.getJudges();
        assertThat(judges)
            .as("Expect JudgeDto to contain 3 judge codes")
            .extracting(JudgeDto::getCode)
            .containsExactlyInAnyOrder("DRED", "JUDD", "LAWW");

        assertThat(judges)
            .as("Expect JudgeDto to contain 3 judge descriptions")
            .extracting(JudgeDto::getDescription)
            .containsExactlyInAnyOrder("DREDD", "LAWSON", "HIGHCOURT");
    }

    @Test
    public void getJudgesForCourtLocationsExceptionForBureauUser() {
        initHeadersBureau();

        ResponseEntity<JudgeListDto> responseEntity =
            restTemplate.exchange(new RequestEntity<>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/trial/judge/list")), JudgeListDto.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expect the status to be forbidden.")
            .isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(Objects.requireNonNull(responseEntity.getBody()).getJudges())
            .as("Expect the response body to contain no judge details")
            .isNull();
    }
    
    private void initHeadersCourt() {
        httpHeaders =
            initialiseHeaders("COURT_USER", UserType.COURT,null,"435");
    }

    private void initHeadersBureau() {
        httpHeaders =
            initialiseHeaders("BUREAU_USER", UserType.BUREAU,null,"400");
    }
}
