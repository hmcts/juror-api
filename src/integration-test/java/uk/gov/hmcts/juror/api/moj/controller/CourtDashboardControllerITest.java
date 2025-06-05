package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.TestUtils;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.courtdashboard.CourtAdminInfoDto;
import uk.gov.hmcts.juror.api.moj.controller.courtdashboard.CourtNotificationInfoDto;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpMethod.GET;

/**
 * Integration tests for the Court Dashboard controller.
 */
@SuppressWarnings("java:S5960")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({"/db/mod/truncate.sql", "/db/JurorExpenseControllerITest_expenseRates.sql", "/db/CourtDashboardController.sql"})
public class CourtDashboardControllerITest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private HttpHeaders httpHeaders;


    @Test
    public void courtNotificationsNonSjoHappy() {
        initHeadersCourt();
        ResponseEntity<CourtNotificationInfoDto> response = restTemplate.exchange(
            new RequestEntity<>(httpHeaders, GET,
                                URI.create("/api/v1/moj/court-dashboard/notifications/415")),
                                CourtNotificationInfoDto.class);

        assertThat(response.getStatusCode()).as("Expect the status to be OK").isEqualTo(HttpStatus.OK);

        CourtNotificationInfoDto responseBody = response.getBody();
        assertThat(responseBody).isNotNull();

        assertThat(responseBody.getOpenSummonsReplies())
            .as("Expect the openSummonsReplies to be 3")
            .isEqualTo(3);
        assertThat(responseBody.getPendingJurors())
            .as("Expect the pendingJurors to be 0")
            .isEqualTo(0);
    }

    @Test
    public void courtNotificationsSjoHappy() {
        initHeadersCourtSjo();
        ResponseEntity<CourtNotificationInfoDto> response = restTemplate.exchange(
            new RequestEntity<>(httpHeaders, GET,
                                URI.create("/api/v1/moj/court-dashboard/notifications/415")),
                                CourtNotificationInfoDto.class);

        assertThat(response.getStatusCode()).as("Expect the status to be OK").isEqualTo(HttpStatus.OK);

        CourtNotificationInfoDto responseBody = response.getBody();
        assertThat(responseBody).isNotNull();

        assertThat(responseBody.getOpenSummonsReplies())
            .as("Expect the openSummonsReplies to be 3")
            .isEqualTo(3);
        assertThat(responseBody.getPendingJurors())
            .as("Expect the pendingJurors to be 2")
            .isEqualTo(2);
    }

    @Test
    public void courtAdminInfoHappy() {
        initHeadersCourtSjo();
        ResponseEntity<CourtAdminInfoDto> response = restTemplate.exchange(
            new RequestEntity<>(httpHeaders, GET,
                                URI.create("/api/v1/moj/court-dashboard/admin/415")),
            CourtAdminInfoDto.class);

        assertThat(response.getStatusCode()).as("Expect the status to be OK").isEqualTo(HttpStatus.OK);

        CourtAdminInfoDto responseBody = response.getBody();
        assertThat(responseBody).isNotNull();

        assertThat(responseBody.getUnpaidAttendances())
            .as("Expect the unpaidAttendances to be 14")
            .isEqualTo(14);

        assertThat(responseBody.getOldestUnpaidAttendanceDate())
            .as("Expect the oldest Unpaid Attendance Date to be 2024-09-09")
            .isEqualTo("2024-09-09");

        long daysSinceOldest = LocalDate.now().toEpochDay() - LocalDate.of(2024,9,9).toEpochDay();
        assertThat(responseBody.getOldestUnpaidAttendanceDays())
            .as("Expect the oldest Unpaid Attendance Days to be " + daysSinceOldest)
            .isEqualTo(daysSinceOldest);

        assertThat(responseBody.getOldestUnpaidJurorNumber())
            .as("Expect the oldest Unpaid Juror Number to be 586856851")
            .isEqualTo("586856851");
        assertThat(responseBody.getUtilisationReportDate())
            .as("Expect the utilisation report date to be 2025-06-03T08:14:28")
            .isEqualTo("2025-06-03T08:14:28"); // This is a fixed date in the SQL script
        assertEquals(62.14, responseBody.getUtilisationPercentage(), 0.1, "Expect the utilisation value to be 62.14");
    }

    @Test
    public void judgesForCourtLocationsExceptionForBureauUser() {
        initHeadersBureau();

        ResponseEntity<MojException.Forbidden> response = restTemplate.exchange(new RequestEntity<>(httpHeaders, GET,
            URI.create("/api/v1/moj/trial/courtrooms/list")), MojException.Forbidden.class);

        assertThat(response.getStatusCode())
            .as("Expect the status to be forbidden.")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    private void initHeadersCourt() {
        BureauJwtPayload.Staff staff = createStaff("415", "MsCourt");
        httpHeaders = initialiseHeaders("COURT_USER", UserType.COURT,null,"415",staff);
    }

    private void initHeadersCourtSjo() {
        BureauJwtPayload.Staff staff = createStaff("415", "MsCourt");
        httpHeaders = initialiseHeaders("COURT_USER", UserType.COURT, Set.of(Role.SENIOR_JUROR_OFFICER), "415", staff);
    }

    private void initHeadersBureau() {
        BureauJwtPayload.Staff staff = createStaff("400", "MrBureau");
        httpHeaders = initialiseHeaders("BUREAU_USER", UserType.BUREAU,null,"400",staff);
    }

    private BureauJwtPayload.Staff createStaff(String owner, String staffName) {
        List<String> staffCourts = Collections.singletonList(owner);
        return TestUtils.staffBuilder(staffName, 1, staffCourts);
    }
}
