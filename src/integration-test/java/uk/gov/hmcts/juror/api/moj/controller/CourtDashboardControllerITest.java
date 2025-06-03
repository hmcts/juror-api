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
import uk.gov.hmcts.juror.api.moj.controller.courtdashboard.CourtNotificationListDto;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;

/**
 * Integration tests for the Court Dashboard controller.
 */
@SuppressWarnings("java:S5960")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql({"/db/mod/truncate.sql","/db/CourtDashboardController.sql"})
public class CourtDashboardControllerITest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private HttpHeaders httpHeaders;


    @Test
    public void courtNotificationsNonSjoHappy() {
        initHeadersCourt();
        ResponseEntity<CourtNotificationListDto> response = restTemplate.exchange(
            new RequestEntity<>(httpHeaders, GET,
                                URI.create("/api/v1/moj/court-dashboard/notifications/415")),
                                CourtNotificationListDto.class);

        assertThat(response.getStatusCode()).as("Expect the status to be OK").isEqualTo(HttpStatus.OK);

        CourtNotificationListDto responseBody = response.getBody();
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
        ResponseEntity<CourtNotificationListDto> response = restTemplate.exchange(
            new RequestEntity<>(httpHeaders, GET,
                                URI.create("/api/v1/moj/court-dashboard/notifications/415")),
                                CourtNotificationListDto.class);

        assertThat(response.getStatusCode()).as("Expect the status to be OK").isEqualTo(HttpStatus.OK);

        CourtNotificationListDto responseBody = response.getBody();
        assertThat(responseBody).isNotNull();

        assertThat(responseBody.getOpenSummonsReplies())
            .as("Expect the openSummonsReplies to be 3")
            .isEqualTo(3);
        assertThat(responseBody.getPendingJurors())
            .as("Expect the pendingJurors to be 2")
            .isEqualTo(2);
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
