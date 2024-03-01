package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.Before;
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
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.CourtroomsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.trial.CourtroomsListDto;
import uk.gov.hmcts.juror.api.moj.exception.MojException;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;

/**
 * Integration tests for the Courtroom controller.
 */
@SuppressWarnings("java:S5960")
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql("/db/trial/Courtroom.sql")
public class CourtroomControllerITest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private HttpHeaders httpHeaders;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void listOfCourtroomsForLocationHappy() {
        initHeadersCourt();
        ResponseEntity<CourtroomsListDto[]> response = restTemplate.exchange(new RequestEntity<>(httpHeaders, GET,
            URI.create("/api/v1/moj/trial/courtrooms/list")), CourtroomsListDto[].class);

        assertThat(response.getStatusCode()).as("Expect the status to be OK").isEqualTo(HttpStatus.OK);

        CourtroomsListDto[] responseBody = response.getBody();
        assertThat(responseBody).isNotNull();

        CourtroomsListDto courtDetails = Arrays.stream(responseBody).findFirst().get();
        assertThat(courtDetails).isNotNull();
        assertThat(Objects.requireNonNull(courtDetails).getCourtLocation())
            .as("Expect the response to contain the location of the court")
            .isEqualTo("STOKE-ON-TRENT");

        List<CourtroomsDto> courtrooms = courtDetails.getCourtRooms();
        assertThat(Objects.requireNonNull(courtrooms).size())
            .as("Expect the response to return courtrooms of the given court")
            .isEqualTo(9);

        assertThat(courtrooms)
            .as("Expect owner to be the same for all courtrooms")
            .extracting(CourtroomsDto::getOwner)
            .containsExactly("456", "456", "456", "456", "456", "456", "456", "456", "456");
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
        BureauJWTPayload.Staff staff = createStaff("456", "MsCourt");

        httpHeaders = initialiseHeaders("99", false, "COURT_USER", 89,
            "456", staff);
    }

    private void initHeadersBureau() {
        BureauJWTPayload.Staff staff = createStaff("400", "MrBureau");

        httpHeaders = initialiseHeaders("99", false, "BUREAU_USER", 89,
            "400", staff);
    }

    private BureauJWTPayload.Staff createStaff(String owner, String staffName) {
        List<String> staffCourts = Collections.singletonList(owner);
        return TestUtils.staffBuilder(staffName, 1, staffCourts);
    }
}
