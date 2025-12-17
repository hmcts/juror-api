package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.IncompleteServiceReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.OverdueUtilisationReportResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.managementdashboard.WeekendAttendanceReportResponseDto;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;

/**
 * Integration tests for the Management Dashboard controller.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ManagementDashboardControllerITest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private HttpHeaders httpHeaders;

    @BeforeEach
    void setUp() {
        httpHeaders = new HttpHeaders();
        final BureauJwtPayload bureauJwtPayload = TestUtils.getJwtPayloadSuperUser("415", "Chester");

        final String bureauJwt = mintBureauJwt(bureauJwtPayload);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/mod/ManagementDashboardOverdueUtilITest_typical.sql"})
    void overdueUtilisationReportHappy() {

        ResponseEntity<OverdueUtilisationReportResponseDto> response = restTemplate.exchange(
            new RequestEntity<>(httpHeaders, GET,
                                URI.create("/api/v1/moj/management-dashboard/overdue-utilisation")),
                OverdueUtilisationReportResponseDto.class);

        assertThat(response.getStatusCode()).as("Expect the status to be OK").isEqualTo(HttpStatus.OK);

        OverdueUtilisationReportResponseDto responseBody = response.getBody();
        assertThat(responseBody).isNotNull(); // no actual data to test against

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/mod/ManagementDashboardIncompleteServiceReportITest_typical.sql"})
    void incompleteServiceReportHappy() {

        ResponseEntity<IncompleteServiceReportResponseDto> response = restTemplate.exchange(
            new RequestEntity<>(httpHeaders, GET,
                                URI.create("/api/v1/moj/management-dashboard/incomplete-service")),
            IncompleteServiceReportResponseDto.class);

        assertThat(response.getStatusCode()).as("Expect the status to be OK").isEqualTo(HttpStatus.OK);

        IncompleteServiceReportResponseDto responseBody = response.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getRecords()).isNotNull();
        assertThat(responseBody.getRecords().size()).isEqualTo(2);

        IncompleteServiceReportResponseDto.IncompleteServiceRecord incompleteServiceRecord =
                                                                                responseBody.getRecords().get(0);
        assertThat(incompleteServiceRecord.getCourt()).isEqualTo("CHESTER (415)");
        assertThat(incompleteServiceRecord.getNumberOfIncompleteServices()).isEqualTo(11);

        incompleteServiceRecord =
                responseBody.getRecords().get(1);
        assertThat(incompleteServiceRecord.getCourt()).isEqualTo("IPSWICH (426)");
        assertThat(incompleteServiceRecord.getNumberOfIncompleteServices()).isEqualTo(10);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/mod/reports/WeekendAttendanceReportITest_typical.sql"})
    void weekendAttendanceReportHappy() {

        ResponseEntity<WeekendAttendanceReportResponseDto> response = restTemplate.exchange(
                new RequestEntity<>(httpHeaders, GET,
                        URI.create("/api/v1/moj/management-dashboard/weekend-attendance")),
                WeekendAttendanceReportResponseDto.class);

        assertThat(response.getStatusCode()).as("Expect the status to be OK").isEqualTo(HttpStatus.OK);

        WeekendAttendanceReportResponseDto responseBody = response.getBody();
        assertThat(responseBody).isNull();

    }

}
