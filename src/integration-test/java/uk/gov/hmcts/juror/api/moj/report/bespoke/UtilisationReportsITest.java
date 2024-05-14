package uk.gov.hmcts.juror.api.moj.report.bespoke;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DailyUtilisationReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Controller: " + UtilisationReportsITest.DAILY_UTILISATION_REPORT_URL)
@Sql({
    "/db/truncate.sql",
    "/db/mod/truncate.sql",
    "/db/mod/reports/DailyUtilisationReportsITest_typical.sql",
})
// TODO use the new abstract test class
class UtilisationReportsITest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    public static final String URL_BASE = "/api/v1/moj/reports";

    public static final String DAILY_UTILISATION_REPORT_URL = URL_BASE + "/daily-utilisation";

    private HttpHeaders httpHeaders;

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        initHeaders();
    }

    private void initHeaders(Role... roles) {
        final String bureauJwt = createJwt(
            "test_court_standard",
            "415",
            UserType.COURT,
            Set.of(),
            "415"
        );

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Test
    void viewDailyUtilisationHappy() {

        ResponseEntity<DailyUtilisationReportResponse> responseEntity =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create(DAILY_UTILISATION_REPORT_URL + "/415?reportFromDate=2024-04-01&reportToDate=2024-05-13")),
                DailyUtilisationReportResponse.class);

        assertThat(responseEntity.getStatusCode()).as("Expect HTTP OK response").isEqualTo(HttpStatus.OK);

        DailyUtilisationReportResponse responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();

    }

}
