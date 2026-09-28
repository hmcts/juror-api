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
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({"/db/mod/truncate.sql", "/db/DbdDashboardControllerITest.sql"})
public class DbdDashboardControllerITest extends AbstractIntegrationTest {

    private static final URI STATISTICS_URI = URI.create("/api/v1/moj/dbd-dashboard/statistics");

    @Autowired
    private TestRestTemplate restTemplate;

    private HttpHeaders httpHeaders;

    @Before
    public void setUp() {
        httpHeaders = initialiseHeaders("BUREAU_USER", UserType.BUREAU, null, "400");
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    @Test
    public void statisticsSinglePeriodHappy() {
        ResponseEntity<String> response = postStatistics(Map.of(
            "courtGroups", List.of(Map.of(
                "groupName", "Pilot courts",
                "groupLocations", List.of(415)
            )),
            "dateRangeA", Map.of(
                "startDate", "20260101",
                "endDate", "20260131"
            ),
            "sumGroups", false
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThatJson(response.getBody()).node("court_groups[0].group_name").isEqualTo("Pilot courts");
        assertThatJson(response.getBody()).node("court_groups[0].period_a.locations[0].location_code").isEqualTo(415);
        assertThatJson(response.getBody()).node("court_groups[0].period_a.locations[0].online_response_total")
            .isEqualTo(8);
        assertThatJson(response.getBody()).node("court_groups[0].period_a.locations[0].paper_response_total")
            .isEqualTo(6);
        assertThatJson(response.getBody()).node("court_groups[0].period_a.locations[0].not_responded_total")
            .isEqualTo(9);
        assertThatJson(response.getBody()).node("court_groups[0].period_a.locations[0].total_responses")
            .isEqualTo(14);
        assertThatJson(response.getBody()).node("court_groups[0].period_a.locations[0].response_rate_percent")
            .isEqualTo(61);
        assertThatJson(response.getBody()).node("court_groups[0].period_a.locations[0].digital_responses_percent")
            .isEqualTo(57);
        assertThatJson(response.getBody()).node("court_groups[0].period_a.locations[0].online_response_times")
            .isObject();
        assertThatJson(response.getBody()).node("court_groups[0].period_a.locations[0].paper_response_times")
            .isObject();
        assertThatJson(response.getBody()).node("court_groups[0].period_a.locations[0].age_group_breakdown")
            .isObject();
        assertThatJson(response.getBody()).node("court_groups[0].period_b").isAbsent();
    }

    @Test
    public void statisticsWithComparisonPeriodHappy() {
        ResponseEntity<String> response = postStatistics(Map.of(
            "courtGroups", List.of(Map.of(
                "groupName", "Pilot courts",
                "groupLocations", List.of(415)
            )),
            "dateRangeA", Map.of(
                "startDate", "20260101",
                "endDate", "20260131"
            ),
            "dateRangeB", Map.of(
                "startDate", "20260201",
                "endDate", "20260228"
            ),
            "sumGroups", true
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThatJson(response.getBody()).node("court_groups[0].period_a.locations[0].location_code").isAbsent();
        assertThatJson(response.getBody()).node("court_groups[0].period_a.locations[0].total_responses")
            .isEqualTo(14);
        assertThatJson(response.getBody()).node("court_groups[0].period_b.locations[0].total_responses")
            .isEqualTo(6);
        assertThatJson(response.getBody()).node("court_groups[0].period_b.locations[0].response_rate_percent")
            .isEqualTo(50);
        assertThatJson(response.getBody()).node("court_groups[0].period_b.locations[0].digital_responses_percent")
            .isEqualTo(67);
    }

    private ResponseEntity<String> postStatistics(Map<String, Object> request) {
        return restTemplate.exchange(
            new RequestEntity<>(request, httpHeaders, HttpMethod.POST, STATISTICS_URI),
            String.class
        );
    }
}
