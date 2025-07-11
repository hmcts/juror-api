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
import uk.gov.hmcts.juror.api.moj.controller.courtdashboard.CourtAttendanceInfoDto;
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
            .as("Expect the oldest Unpaid Attendance Date to be 2024-09-08")
            .isEqualTo("2024-09-08");

        long daysSinceOldest = LocalDate.now().toEpochDay() - LocalDate.of(2024,9,8).toEpochDay();
        assertThat(responseBody.getOldestUnpaidAttendanceDays())
            .as("Expect the oldest Unpaid Attendance Days to be " + daysSinceOldest)
            .isEqualTo(daysSinceOldest);

        assertThat(responseBody.getOldestUnpaidJurorNumber())
            .as("Expect the oldest Unpaid Juror Number to be 472008411")
            .isEqualTo("472008411");
        assertThat(responseBody.getUtilisationReportDate())
            .as("Expect the utilisation report date to be 2025-06-03T08:14:28")
            .isEqualTo("2025-06-03T08:14:28"); // This is a fixed date in the SQL script
        assertEquals(62.14, responseBody.getUtilisationPercentage(), 0.1, "Expect the utilisation value to be 62.14");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/CourtDashboardAttendanceITest.sql"})
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage") // false positive
    public void courtAttendanceNonSjoHappy() {
        initHeadersCourt();
        ResponseEntity<CourtAttendanceInfoDto> response = restTemplate.exchange(
            new RequestEntity<>(httpHeaders, GET,
                                URI.create("/api/v1/moj/court-dashboard/attendance/415")),
            CourtAttendanceInfoDto.class);

        assertThat(response.getStatusCode()).as("Expect the status to be OK").isEqualTo(HttpStatus.OK);

        CourtAttendanceInfoDto responseBody = response.getBody();
        assertThat(responseBody).isNotNull();

        CourtAttendanceInfoDto.AttendanceStatsToday statsToday = responseBody.getAttendanceStatsToday();

        assertThat(statsToday.getExpected())
            .as("Expect the total expected in today to be 7")
            .isEqualTo(8);
        assertThat(statsToday.getCheckedIn())
            .as("Expect the total checked in today to be 2")
            .isEqualTo(2);
        assertThat(statsToday.getCheckedOut())
            .as("Expect the total checked out today to be 1")
            .isEqualTo(1);
        assertThat(statsToday.getOnTrials())
            .as("Expect the total on trials today to be 2")
            .isEqualTo(2);
        assertThat(statsToday.getNotCheckedIn())
            .as("Expect the total not checked in today to be 2")
            .isEqualTo(2);
        // one juror is also absent

        CourtAttendanceInfoDto.AttendanceStatsLastSevenDays statsLastSevenDays = responseBody.getAttendanceStatsLastSevenDays();
        assertThat(statsLastSevenDays.getExpected())
            .as("Expect the total attendances in the last 7 days to be between 25 and 35"
                + "depending on the weekends and public holidays as one juror is on a trial and works last 3 days")
            .isBetween(25, 35);
        assertThat(statsLastSevenDays.getAttended())
            .as("Expect the total attended in the last 7 days to be 15")
            .isEqualTo(15);
        assertThat(statsLastSevenDays.getOnTrials())
            .as("Expect the total on trials in the last 7 days to be 2")
            .isEqualTo(2);
        assertThat(statsLastSevenDays.getAbsent())
            .as("Expect the total absent in the last 7 days to be 1")
            .isEqualTo(1);

        assertThat(responseBody.getTotalDueToAttend())
            .as("Expect the total due to attend to be 5")
            .isEqualTo(6);
        assertThat(responseBody.getReasonableAdjustments())
            .as("Expect the total reasonable adjustments to be 1")
            .isEqualTo(1);
        assertThat(responseBody.getUnconfirmedAttendances())
            .as("Expect the total unconfirmed attendances to be 8")
            .isEqualTo(8);
    }

    @Test
    public void courtDashboardExceptionForBureauUser() {
        initHeadersBureau();

        ResponseEntity<MojException.Forbidden> response = restTemplate.exchange(new RequestEntity<>(httpHeaders, GET,
                URI.create("/api/v1/moj/court-dashboard/attendance/415")), MojException.Forbidden.class);

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
