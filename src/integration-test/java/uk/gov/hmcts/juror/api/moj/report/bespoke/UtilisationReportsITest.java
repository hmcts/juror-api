package uk.gov.hmcts.juror.api.moj.report.bespoke;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Utilisation Reports Integration Tests at " + UtilisationReportsITest.URL_BASE)
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

    @Nested
    @DisplayName("Daily Utilisation Report Integration Tests")
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/mod/reports/DailyUtilisationReportsITest_typical.sql"
    })
    class DailyUtilisationReportTests {

        @Test
        void viewDailyUtilisationHappy() {

            ResponseEntity<DailyUtilisationReportResponse> responseEntity =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(DAILY_UTILISATION_REPORT_URL
                            + "/415?reportFromDate=2024-04-20&reportToDate=2024-05-13")),
                    DailyUtilisationReportResponse.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP OK response").isEqualTo(HttpStatus.OK);
            DailyUtilisationReportResponse responseBody = responseEntity.getBody();
            assertThat(responseBody).isNotNull();

            // validate the table data
            DailyUtilisationReportResponse.TableData tableData = responseBody.getTableData();
            assertThat(tableData).isNotNull();
            assertThat(tableData.getHeadings()).isNotNull();
            assertThat(tableData.getHeadings()).hasSize(6);

            // validate the week data
            assertThat(tableData.getWeeks()).isNotNull();
            assertThat(tableData.getWeeks()).hasSize(4);

            // validate days within the week
            DailyUtilisationReportResponse.TableData.Week week = tableData.getWeeks().get(2);
            assertThat(week).isNotNull();
            assertThat(week.getDays().size()).isEqualTo(7);

            // validate a day within the week
            DailyUtilisationReportResponse.TableData.Week.Day day = week.getDays().get(3);

            assertThat(day).isNotNull();
            assertThat(day.getDate()).isEqualTo(LocalDate.parse("2024-05-09", DateTimeFormatter.ISO_LOCAL_DATE));
            assertThat(day.getJurorWorkingDays()).isEqualTo(21);
            assertThat(day.getSittingDays()).isEqualTo(12);
            assertThat(day.getAttendanceDays()).isEqualTo(13);
            assertThat(day.getNonAttendanceDays()).isEqualTo(8);
            assertThat(Math.round(day.getUtilisation())).isEqualTo(Math.round(57.14));

            // validate the weekly totals for the week
            assertThat(week.getWeeklyTotalJurorWorkingDays()).isEqualTo(98);
            assertThat(week.getWeeklyTotalSittingDays()).isEqualTo(52);
            assertThat(week.getWeeklyTotalAttendanceDays()).isEqualTo(56);
            assertThat(week.getWeeklyTotalNonAttendanceDays()).isEqualTo(42);
            assertThat(Math.round(week.getWeeklyTotalUtilisation())).isEqualTo(Math.round(53.06));

            // validate the overall totals
            assertThat(tableData.getOverallTotalJurorWorkingDays()).isEqualTo(221);
            assertThat(tableData.getOverallTotalSittingDays()).isEqualTo(52);
            assertThat(tableData.getOverallTotalAttendanceDays()).isEqualTo(68);
            assertThat(tableData.getOverallTotalNonAttendanceDays()).isEqualTo(153);
            assertThat(Math.round(tableData.getOverallTotalUtilisation())).isEqualTo(Math.round(23.52));

        }

        @Test
        void viewDailyUtilisationInvalidDateRange() {

            ResponseEntity<DailyUtilisationReportResponse> responseEntity =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(DAILY_UTILISATION_REPORT_URL
                            + "/415?reportFromDate=2024-05-20&reportToDate=2024-04-13")),
                    DailyUtilisationReportResponse.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP BAD_REQUEST response")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void viewDailyUtilisationInvalidUserType() {

            final String bureauJwt = createBureauJwt();

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ResponseEntity<DailyUtilisationReportResponse> responseEntity =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(DAILY_UTILISATION_REPORT_URL
                            + "/415?reportFromDate=2024-04-20&reportToDate=2024-05-13")),
                    DailyUtilisationReportResponse.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP FORBIDDEN response")
                .isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    private String createBureauJwt() {
        final String bureauJwt = createJwt(
            "test_Bureau_standard",
            "400",
            UserType.BUREAU,
            Set.of(),
            "400"
        );
        return bureauJwt;
    }

}
