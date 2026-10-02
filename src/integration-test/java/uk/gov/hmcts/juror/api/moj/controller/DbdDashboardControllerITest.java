package uk.gov.hmcts.juror.api.moj.controller;

import com.jayway.jsonpath.JsonPath;
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
import static org.assertj.core.api.Assertions.within;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({"/db/mod/truncate.sql", "/db/DbdDashboardControllerITest.sql"})
@SuppressWarnings("PMD.UseUnderscoresInNumericLiterals")
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
        assertThatJson(response.getBody()).node("court_groups[0].period_a.locations[0].total_responses").isAbsent();
        assertJsonFloat(response.getBody(),
                        "$.court_groups[0].period_a.locations[0].response_rate_percent", 60.869565f);
        assertJsonFloat(response.getBody(),
                        "$.court_groups[0].period_a.locations[0].digital_responses_percent", 57.142857f);
        assertThatJson(response.getBody()).node("court_groups[0].period_a.locations[0].online_response_times")
            .isObject();
        assertThatJson(response.getBody()).node("court_groups[0].period_a.locations[0].paper_response_times")
            .isObject();
        assertThatJson(response.getBody()).node("court_groups[0].period_a.locations[0].response_times_percent")
            .isObject();
        assertJsonFloat(response.getBody(),
                        "$.court_groups[0].period_a.locations[0].response_times_percent.within7_days_percent",
                        57.142857f);
        assertJsonFloat(response.getBody(),
                        "$.court_groups[0].period_a.locations[0].response_times_percent.within14_days_percent",
                        42.857143f);
        assertThatJson(response.getBody()).node("court_groups[0].period_a.locations[0].age_group_breakdown")
            .isObject();
        assertJsonFloat(response.getBody(),
                        "$.court_groups[0].period_a.locations[0].age_group_breakdown_percent['18-24']",
                        34.782608f);
        assertJsonFloat(response.getBody(),
                        "$.court_groups[0].period_a.locations[0].age_group_breakdown_percent['25-34']",
                        26.086956f);
        assertJsonFloat(response.getBody(),
                        "$.court_groups[0].period_a.locations[0].age_group_breakdown_percent['35-44']",
                        39.130436f);
        assertJsonNull(response.getBody(), "$.court_groups[0].period_b");
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
        assertJsonNull(response.getBody(), "$.court_groups[0].period_a.locations[0].location_code");
        assertThatJson(response.getBody()).node("court_groups[0].period_a.locations[0].total_responses").isAbsent();
        assertThatJson(response.getBody()).node("court_groups[0].period_b.locations[0].total_responses").isAbsent();
        assertJsonFloat(response.getBody(),
                        "$.court_groups[0].period_b.locations[0].response_rate_percent", 50f);
        assertJsonFloat(response.getBody(),
                        "$.court_groups[0].period_b.locations[0].digital_responses_percent", 66.666664f);
        assertJsonFloat(response.getBody(),
                        "$.court_groups[0].period_b.locations[0].response_times_percent.within7_days_percent",
                        66.666664f);
        assertJsonFloat(response.getBody(),
                        "$.court_groups[0].period_b.locations[0].response_times_percent.within21_days_percent",
                        33.333332f);
        assertJsonFloat(response.getBody(),
                        "$.court_groups[0].period_b.locations[0].age_group_breakdown_percent['35-44']", 50f);
    }

    private ResponseEntity<String> postStatistics(Map<String, Object> request) {
        return restTemplate.exchange(
            new RequestEntity<>(request, httpHeaders, HttpMethod.POST, STATISTICS_URI),
            String.class
        );
    }

    private void assertJsonFloat(String responseBody, String path, float expected) {
        Number value = JsonPath.read(responseBody, path);
        assertThat(value.floatValue()).isCloseTo(expected, within(0.0001f));
    }

    private void assertJsonNull(String responseBody, String path) {
        Object value = JsonPath.read(responseBody, path);
        assertThat(value).isNull();
    }
}
