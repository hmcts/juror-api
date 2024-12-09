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
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DailyUtilisationReportJurorsResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DailyUtilisationReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.MonthlyUtilisationReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.repository.UtilisationStatsRepository;

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
    @Autowired
    private UtilisationStatsRepository utilisationStatsRepository;
    public static final String URL_BASE = "/api/v1/moj/reports";
    public static final String DAILY_UTILISATION_REPORT_URL = URL_BASE + "/daily-utilisation";
    public static final String DAILY_UTILISATION_JURORS_URL = URL_BASE + "/daily-utilisation-jurors";
    public static final String GENERATE_MONTHLY_UTILISATION_REPORT_URL = URL_BASE + "/generate-monthly-utilisation";
    public static final String VIEW_MONTHLY_UTILISATION_REPORT_URL = URL_BASE + "/view-monthly-utilisation";
    public static final String GET_MONTHLY_UTILISATION_REPORT_URL = URL_BASE + "/monthly-utilisation-reports";

    private HttpHeaders httpHeaders;

    @BeforeEach
    public void setUp() throws Exception {
        initHeaders();
    }

    private void initHeaders() {
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

            assertThat(responseEntity.getBody()).isNotNull();
            DailyUtilisationReportResponse responseBody = responseEntity.getBody();

            // validate the table data
            assertThat(responseBody.getTableData()).isNotNull();
            DailyUtilisationReportResponse.TableData tableData = responseBody.getTableData();
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
            assertThat(day.getSittingDays()).isEqualTo(16);
            assertThat(day.getAttendanceDays()).isEqualTo(17);
            assertThat(day.getNonAttendanceDays()).isEqualTo(4);
            assertThat(Math.round(day.getUtilisation())).isEqualTo(Math.round(76.19));

            // validate the weekly totals for the week
            assertThat(week.getWeeklyTotalJurorWorkingDays()).isEqualTo(98);
            assertThat(week.getWeeklyTotalSittingDays()).isEqualTo(68);
            assertThat(week.getWeeklyTotalAttendanceDays()).isEqualTo(72);
            assertThat(week.getWeeklyTotalNonAttendanceDays()).isEqualTo(26);
            assertThat(Math.round(week.getWeeklyTotalUtilisation())).isEqualTo(Math.round(69.39));

            // validate the overall totals
            assertThat(tableData.getOverallTotalJurorWorkingDays()).isEqualTo(221);
            assertThat(tableData.getOverallTotalSittingDays()).isEqualTo(104);
            assertThat(tableData.getOverallTotalAttendanceDays()).isEqualTo(120);
            assertThat(tableData.getOverallTotalNonAttendanceDays()).isEqualTo(101);
            assertThat(Math.round(tableData.getOverallTotalUtilisation())).isEqualTo(Math.round(47.06));

        }

        @Test
        void viewDailyUtilisationReportFromAfterReportToDate() {
            ResponseEntity<DailyUtilisationReportResponse> responseEntity =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(DAILY_UTILISATION_REPORT_URL
                            + "/415?reportFromDate=2024-05-20&reportToDate=2024-04-13")),
                    DailyUtilisationReportResponse.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP BAD_REQUEST response")
                .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void viewDailyUtilisationReportInvalidDateRange() {
            // reportFromDate is more than 31 before reportToDate
            ResponseEntity<DailyUtilisationReportResponse> responseEntity =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(DAILY_UTILISATION_REPORT_URL
                            + "/415?reportFromDate=2024-04-20&reportToDate=2024-05-23")),
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


    @Nested
    @DisplayName("Daily Utilisation Jurors Integration Tests")
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/mod/reports/DailyUtilisationReportsITest_typical.sql"
    })
    class DailyUtilisationJurorsTests {

        @Test
        void viewDailyUtilisationJurorsHappy() {

            ResponseEntity<DailyUtilisationReportJurorsResponse> responseEntity =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(DAILY_UTILISATION_JURORS_URL
                            + "/415?reportDate=2024-05-09")),
                    DailyUtilisationReportJurorsResponse.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP OK response").isEqualTo(HttpStatus.OK);
            DailyUtilisationReportJurorsResponse responseBody = responseEntity.getBody();
            assertThat(responseBody).isNotNull();

            // validate the table data
            DailyUtilisationReportJurorsResponse.TableData tableData = responseBody.getTableData();
            assertThat(tableData).isNotNull();
            assertThat(tableData.getHeadings()).isNotNull();
            assertThat(tableData.getHeadings()).hasSize(5);

            // validate the jurors
            assertThat(tableData.getJurors()).isNotNull();
            assertThat(tableData.getJurors()).hasSize(21);

            // validate a juror
            DailyUtilisationReportJurorsResponse.TableData.Juror juror = tableData.getJurors().get(0);
            assertThat(juror).isNotNull();
            assertThat(juror.getJuror()).isEqualTo("415000001");
            assertThat(juror.getJurorWorkingDay()).isEqualTo(1);
            assertThat(juror.getSittingDay()).isEqualTo(1);
            assertThat(juror.getAttendanceDay()).isEqualTo(1);
            assertThat(juror.getNonAttendanceDay()).isEqualTo(0);

            // validate the totals
            assertThat(tableData.getTotalJurorWorkingDays()).isEqualTo(21);
            assertThat(tableData.getTotalSittingDays()).isEqualTo(16);
            assertThat(tableData.getTotalAttendanceDays()).isEqualTo(17);
            assertThat(tableData.getTotalNonAttendanceDays()).isEqualTo(4);

        }

        @Test
        void viewDailyUtilisationJurorsNoUtilisation() {
            ResponseEntity<DailyUtilisationReportJurorsResponse> responseEntity =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(DAILY_UTILISATION_JURORS_URL
                            + "/415?reportDate=2024-03-01")),
                    DailyUtilisationReportJurorsResponse.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP OK response").isEqualTo(HttpStatus.OK);
            DailyUtilisationReportJurorsResponse responseBody = responseEntity.getBody();
            assertThat(responseBody).isNotNull();

            // validate the table data
            DailyUtilisationReportJurorsResponse.TableData tableData = responseBody.getTableData();
            assertThat(tableData).isNotNull();
            assertThat(tableData.getHeadings()).isNotNull();
            assertThat(tableData.getHeadings()).hasSize(5);

            // validate the jurors
            assertThat(tableData.getJurors()).isNotNull();
            assertThat(tableData.getJurors()).hasSize(0);
        }

        @Test
        void viewDailyUtilisationJurorsInvalidUserType() {

            final String bureauJwt = createBureauJwt();

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ResponseEntity<DailyUtilisationReportJurorsResponse> responseEntity =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(DAILY_UTILISATION_JURORS_URL
                            + "/415?reportDate=2024-05-09")),
                    DailyUtilisationReportJurorsResponse.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP FORBIDDEN response")
                .isEqualTo(HttpStatus.FORBIDDEN);

        }

    }

    @Nested
    @DisplayName("Generate Monthly Utilisation Report Integration Tests")
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/mod/reports/DailyUtilisationReportsITest_typical.sql"
    })
    class GenerateMonthlyUtilisationReportTests {

        @Test
        void generateMonthlyUtilisationReportHappy() {

            ResponseEntity<MonthlyUtilisationReportResponse> responseEntity =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(GENERATE_MONTHLY_UTILISATION_REPORT_URL
                            + "/415?reportDate=2024-05-01")),
                    MonthlyUtilisationReportResponse.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP OK response").isEqualTo(HttpStatus.OK);
            MonthlyUtilisationReportResponse responseBody = responseEntity.getBody();
            assertThat(responseBody).isNotNull();

            // validate the table data
            MonthlyUtilisationReportResponse.TableData tableData = responseBody.getTableData();
            assertThat(tableData).isNotNull();
            assertThat(tableData.getHeadings()).isNotNull();
            assertThat(tableData.getHeadings()).hasSize(6);

            // validate the month data
            assertThat(tableData.getMonths()).isNotNull();
            assertThat(tableData.getMonths().size()).isEqualTo(1);

            MonthlyUtilisationReportResponse.TableData.Month month = tableData.getMonths().get(0);
            assertThat(month.getMonth()).isEqualTo("May 2024");
            assertThat(month.getJurorWorkingDays()).isEqualTo(457);
            assertThat(month.getSittingDays()).isEqualTo(260);
            assertThat(month.getAttendanceDays()).isEqualTo(264);
            assertThat(month.getNonAttendanceDays()).isEqualTo(193);
            assertThat(Math.round(month.getUtilisation())).isEqualTo(Math.round(56.89));

            // validate the totals - should be as per the month
            assertThat(tableData.getTotalJurorWorkingDays()).isEqualTo(457);
            assertThat(tableData.getTotalSittingDays()).isEqualTo(260);
            assertThat(tableData.getTotalAttendanceDays()).isEqualTo(264);
            assertThat(tableData.getTotalNonAttendanceDays()).isEqualTo(193);
            assertThat(Math.round(tableData.getTotalUtilisation())).isEqualTo(Math.round(56.89));

            LocalDate may2024 = LocalDate.parse("2024-05-01");

            // verify the report has been saved
            assertThat(utilisationStatsRepository.findByMonthStartBetweenAndLocCode(may2024,
                may2024, "415").size()).isEqualTo(1);

        }

        @Test
        void generateMonthlyUtilisationEarlierReportIsOverwritten() {

            LocalDate may2024 = LocalDate.parse("2024-05-01");

            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create(GENERATE_MONTHLY_UTILISATION_REPORT_URL
                        + "/415?reportDate=2024-05-01")),
                MonthlyUtilisationReportResponse.class);

            // verify the report has been saved first time
            assertThat(utilisationStatsRepository.findByMonthStartBetweenAndLocCode(may2024,
                may2024, "415").size()).isEqualTo(1);

            // generate the same report again
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create(GENERATE_MONTHLY_UTILISATION_REPORT_URL
                        + "/415?reportDate=2024-05-01")),
                MonthlyUtilisationReportResponse.class);

            // verify the report has been saved over the previous one
            assertThat(utilisationStatsRepository.findByMonthStartBetweenAndLocCode(may2024,
                may2024, "415").size()).isEqualTo(1);
        }

        @Test
        void generateMonthlyUtilisationJurorsInvalidUserType() {

            final String bureauJwt = createBureauJwt();

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ResponseEntity<MonthlyUtilisationReportResponse> responseEntity =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(GENERATE_MONTHLY_UTILISATION_REPORT_URL
                            + "/415?reportDate=2024-05-01")),
                    MonthlyUtilisationReportResponse.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP FORBIDDEN response")
                .isEqualTo(HttpStatus.FORBIDDEN);

        }

    }


    @Nested
    @DisplayName("View Monthly Utilisation Report Integration Tests")
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/mod/reports/MonthlyUtilisationReportsITest_typical.sql"
    })
    class ViewMonthlyUtilisationReportTests {

        @Test
        void viewMonthlyUtilisationReportHappy() {

            ResponseEntity<MonthlyUtilisationReportResponse> responseEntity =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(VIEW_MONTHLY_UTILISATION_REPORT_URL
                            + "/415?reportDate=2024-05-01")),
                    MonthlyUtilisationReportResponse.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP OK response").isEqualTo(HttpStatus.OK);
            MonthlyUtilisationReportResponse responseBody = responseEntity.getBody();
            assertThat(responseBody).isNotNull();

            // validate the table data
            MonthlyUtilisationReportResponse.TableData tableData = responseBody.getTableData();
            assertThat(tableData).isNotNull();
            assertThat(tableData.getHeadings()).isNotNull();
            assertThat(tableData.getHeadings()).hasSize(6);

            // validate the month data
            assertThat(tableData.getMonths()).isNotNull();
            assertThat(tableData.getMonths().size()).isEqualTo(1);

            MonthlyUtilisationReportResponse.TableData.Month month = tableData.getMonths().get(0);
            assertThat(month.getMonth()).isEqualTo("May 2024");
            assertThat(month.getJurorWorkingDays()).isEqualTo(455);
            assertThat(month.getSittingDays()).isEqualTo(55);
            assertThat(month.getAttendanceDays()).isEqualTo(55);
            assertThat(month.getNonAttendanceDays()).isEqualTo(400);
            assertThat(Math.round(month.getUtilisation())).isEqualTo(Math.round(12.09));

            // validate the totals - should be as per the month
            assertThat(tableData.getTotalJurorWorkingDays()).isEqualTo(455);
            assertThat(tableData.getTotalSittingDays()).isEqualTo(55);
            assertThat(tableData.getTotalAttendanceDays()).isEqualTo(55);
            assertThat(tableData.getTotalNonAttendanceDays()).isEqualTo(400);
            assertThat(Math.round(tableData.getTotalUtilisation())).isEqualTo(Math.round(12.09));

        }

        @Test
        void viewMonthlyUtilisationReportPreviousMonths() {

            ResponseEntity<MonthlyUtilisationReportResponse> responseEntity =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(VIEW_MONTHLY_UTILISATION_REPORT_URL
                            + "/415?reportDate=2024-05-01&previousMonths=true")),
                    MonthlyUtilisationReportResponse.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP OK response").isEqualTo(HttpStatus.OK);
            MonthlyUtilisationReportResponse responseBody = responseEntity.getBody();
            assertThat(responseBody).isNotNull();

            // validate the table data
            MonthlyUtilisationReportResponse.TableData tableData = responseBody.getTableData();
            assertThat(tableData).isNotNull();
            assertThat(tableData.getHeadings()).isNotNull();
            assertThat(tableData.getHeadings()).hasSize(6);

            // validate the month data
            assertThat(tableData.getMonths()).isNotNull();
            assertThat(tableData.getMonths().size()).isEqualTo(3);

            MonthlyUtilisationReportResponse.TableData.Month month = tableData.getMonths().get(0);
            assertThat(month.getMonth()).isEqualTo("March 2024");
            assertThat(month.getJurorWorkingDays()).isEqualTo(453);
            assertThat(month.getSittingDays()).isEqualTo(53);
            assertThat(month.getAttendanceDays()).isEqualTo(53);
            assertThat(month.getNonAttendanceDays()).isEqualTo(400);
            assertThat(Math.round(month.getUtilisation())).isEqualTo(Math.round(11.70));

            month = tableData.getMonths().get(1);
            assertThat(month.getMonth()).isEqualTo("April 2024");
            assertThat(month.getJurorWorkingDays()).isEqualTo(454);
            assertThat(month.getSittingDays()).isEqualTo(54);
            assertThat(month.getAttendanceDays()).isEqualTo(54);
            assertThat(month.getNonAttendanceDays()).isEqualTo(400);
            assertThat(Math.round(month.getUtilisation())).isEqualTo(Math.round(11.90));

            month = tableData.getMonths().get(2);
            assertThat(month.getMonth()).isEqualTo("May 2024");
            assertThat(month.getJurorWorkingDays()).isEqualTo(455);
            assertThat(month.getSittingDays()).isEqualTo(55);
            assertThat(month.getAttendanceDays()).isEqualTo(55);
            assertThat(month.getNonAttendanceDays()).isEqualTo(400);
            assertThat(Math.round(month.getUtilisation())).isEqualTo(Math.round(12.08));

            // validate the totals - should be as per the month
            assertThat(tableData.getTotalJurorWorkingDays()).isEqualTo(1362);
            assertThat(tableData.getTotalSittingDays()).isEqualTo(162);
            assertThat(tableData.getTotalAttendanceDays()).isEqualTo(162);
            assertThat(tableData.getTotalNonAttendanceDays()).isEqualTo(1200);
            assertThat(Math.round(tableData.getTotalUtilisation())).isEqualTo(Math.round(11.90));

        }

        @Test
        void viewMonthlyUtilisationInvalidUserType() {

            final String bureauJwt = createBureauJwt();

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ResponseEntity<MonthlyUtilisationReportResponse> responseEntity =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(VIEW_MONTHLY_UTILISATION_REPORT_URL
                            + "/415?reportDate=2024-05-01")),
                    MonthlyUtilisationReportResponse.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP FORBIDDEN response")
                .isEqualTo(HttpStatus.FORBIDDEN);

        }

    }

    @Nested
    @DisplayName("Get Monthly Utilisation Report Integration Tests")
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/mod/reports/MonthlyUtilisationReportsITest_typical.sql"
    })
    class GetMonthlyUtilisationReportTests {

        @Test
        void monthlyUtilisationReportsHappy() {

            ResponseEntity<String> responseEntity =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(GET_MONTHLY_UTILISATION_REPORT_URL + "/415")),
                    String.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP OK response").isEqualTo(HttpStatus.OK);
            String responseBody = responseEntity.getBody();
            assertThat(responseBody).isNotNull();

            assertThat(responseBody).isEqualTo("June 2024,May 2024,April 2024,March 2024,February 2024,"
                + "January 2024,December 2023,November 2023,October 2023,September 2023,August 2023,July 2023,");

        }

        @Test
        void monthlyUtilisationReportsInvalidUserType() {

            final String bureauJwt = createBureauJwt();

            httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

            ResponseEntity<MonthlyUtilisationReportResponse> responseEntity =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(GET_MONTHLY_UTILISATION_REPORT_URL + "/415")),
                    MonthlyUtilisationReportResponse.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP FORBIDDEN response")
                .isEqualTo(HttpStatus.FORBIDDEN);
        }

    }

    private String createBureauJwt() {
        return createJwt(
            "test_Bureau_standard",
            "400",
            UserType.BUREAU,
            Set.of(),
            "400"
        );
    }

}
