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
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.reports.response.WeekendAttendanceReportResponse;
import uk.gov.hmcts.juror.api.moj.domain.Permission;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
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

        Set<Permission> permissions = new HashSet<>();
        permissions.add(Permission.SUPER_USER);
        User user = User.builder()
            .username("Administrator")
            .permissions(permissions)
            .build();

        final BureauJwtPayload bureauJwtPayload = new BureauJwtPayload(user, UserType.ADMINISTRATOR, "400",
                                                                   Collections.singletonList(CourtLocation.builder()
                                                                                                 .locCode("400")
                                                                                                 .name("Bureau")
                                                                                                 .owner("400")
                                                                                                 .build()));

        String bureauJwt = mintBureauJwt(bureauJwtPayload);

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
        void weekendAttendanceReportBureauUserHappy() {

            ResponseEntity<WeekendAttendanceReportResponse> responseEntity =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                        URI.create(URL_BASE + "/weekend-attendance")),
                                      WeekendAttendanceReportResponse.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP OK response").isEqualTo(HttpStatus.OK);
            WeekendAttendanceReportResponse responseBody = responseEntity.getBody();
            assertThat(responseBody).isNotNull();

            // the result of the report is tested in detail in WeekendAttendanceReportITest as it depends
            // on the dates the report is run (number of weekends in the month etc)
        }

        @Test
        void weekendAttendanceReportCourtUserHappy() {
            //update the headers to use court user
            String courtJwt = createCourtJwt();
            httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);

            ResponseEntity<WeekendAttendanceReportResponse> responseEntity =
                restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                                                              URI.create(URL_BASE + "/weekend-attendance")),
                                      WeekendAttendanceReportResponse.class);

            assertThat(responseEntity.getStatusCode()).as("Expect HTTP OK response").isEqualTo(HttpStatus.OK);
            WeekendAttendanceReportResponse responseBody = responseEntity.getBody();
            assertThat(responseBody).isNotNull();

        }

        private String createCourtJwt() {
            Set<Permission> permissions = new HashSet<>();
            permissions.add(Permission.SUPER_USER);
            User user = User.builder()
                .username("Administrator")
                .permissions(permissions)
                .build();

            final BureauJwtPayload bureauJwtPayload = new BureauJwtPayload(user, UserType.ADMINISTRATOR, "415",
                                                                   Collections.singletonList(CourtLocation.builder()
                                                                                                 .locCode("415")
                                                                                                 .name("Chester")
                                                                                                 .owner("415")
                                                                                                 .build()));
            return mintBureauJwt(bureauJwtPayload);
        }
    }

}
