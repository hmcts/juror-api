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
import uk.gov.hmcts.juror.api.moj.controller.reports.response.DigitalSummonsRepliesReportResponse;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.WeekendAttendanceReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.net.URI;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Attendance Reports Integration Tests at " + AttendanceReportsITest.URL_BASE)
class AttendanceReportsITest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    public static final String URL_BASE = "/api/v1/moj/reports";

    private HttpHeaders httpHeaders;

    @BeforeEach
    public void setUp() throws Exception {
        initHeaders();
    }

    private void initHeaders() {
        String bureauJwt = createJwt(
            "test_Bureau_standard",
            "400",
            UserType.BUREAU,
            Set.of(),
            "400"
        );

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }


    @Nested
    @DisplayName("Weekend Attendance Report Integration Tests")
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/mod/reports/WeekendAttendanceReportITest_typical.sql"
    })
    class WeekendAttendanceReportTests {

        @Test
        void weekendAttendanceReportsHappy() {

            ResponseEntity<WeekendAttendanceReportResponse> responseEntity =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(URL_BASE + "/weekend-attendance")),
                                      WeekendAttendanceReportResponse.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP OK response").isEqualTo(HttpStatus.OK);
            WeekendAttendanceReportResponse responseBody = responseEntity.getBody();
            assertThat(responseBody).isNotNull();


        }



        @Test
        void digitalSummonsRepliesReportsNoData() {

            ResponseEntity<DigitalSummonsRepliesReportResponse> responseEntity =
                    restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                                    URI.create(URL_BASE + "/digital-summons-replies-report/2025-10-01")),
                            DigitalSummonsRepliesReportResponse.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP OK response").isEqualTo(HttpStatus.OK);
            DigitalSummonsRepliesReportResponse responseBody = responseEntity.getBody();
            assertThat(responseBody).isNotNull();

            assertThat(responseBody.getHeadings()).isNotNull();
            assertThat(responseBody.getHeadings().size()).isEqualTo(3);

            assertThat(responseBody.getTableData()).isNotNull();
            assertThat(responseBody.getTableData().getHeadings()).isNotNull();
            assertThat(responseBody.getTableData().getHeadings().size()).isEqualTo(2);
            assertThat(responseBody.getTableData().getData()).isNotNull();
            assertThat(responseBody.getTableData().getData().size()).isEqualTo(0);

        }

    }

    private String createCourtJwt() {
        return createJwt(
            "test_court_standard",
            "415",
            UserType.COURT,
            Set.of(),
            "415"
        );
    }

}
