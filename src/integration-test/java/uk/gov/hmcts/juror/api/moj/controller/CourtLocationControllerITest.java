package uk.gov.hmcts.juror.api.moj.controller;

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
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtLocationDataDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtLocationListDto;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the API endpoints defined in {@link CourtLocationController}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CourtLocationControllerITest extends AbstractIntegrationTest {

    private static final String BUREAU_USER = "BUREAU_USER";


    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private CourtLocationRepository courtLocationRepository;

    private HttpHeaders httpHeaders;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        initHeaders();
    }

    private void initHeaders() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("BUREAU_USER")
            .daysToExpire(89)
            .owner("400")
            .build());

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    private String initCourtsJwt(String owner, List<String> courts) throws Exception {

        return mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("COURT_USER")
            .daysToExpire(89)
            .owner(owner)
            .staff(BureauJWTPayload.Staff.builder().courts(courts).build())
            .build());
    }

    @Test
    public void test_getCourtLocations_bureauUser() {
        ResponseEntity<CourtLocationListDto> responseEntity =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/court-location/all-court-locations")), CourtLocationListDto.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        CourtLocationListDto responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();

        Long courtLocationCount = courtLocationRepository.count();

        assertThat((long) responseBody.getData().size())
            .as("Expect the response body to contain a list of all Court Locations")
            .isEqualTo(courtLocationCount);
    }

    @Test
    public void test_getCourtLocations_courtUser() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Arrays.asList("415", "462", "767", "774")));
        ResponseEntity<CourtLocationListDto> responseEntity =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/court-location/all-court-locations")), CourtLocationListDto.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(HttpStatus.OK);

        CourtLocationListDto responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();

        Long courtLocationCount = courtLocationRepository.count();

        assertThat((long) responseBody.getData().size())
            .as("Expect the response body to contain a list of all Court Locations")
            .isEqualTo(courtLocationCount);
    }

    //Tests related to the operation: getAllCourtLocationsByPostcode()
    @Test
    @Sql("/db/CourtCatchmentAreaTestData.sql")
    public void test_getAllCourtLocationsByPostcode_happy() {
        //Invoke service.
        CourtLocationDataDto[] courtLocationsDto =
            templateExchangeAllCourtLocationsByPostcode("SE1", BUREAU_USER, "400", HttpStatus.OK);

        assertThat(courtLocationsDto).isNotNull();
        assertThat(courtLocationsDto).hasSize(4);
        assertThat(courtLocationsDto[0].getLocationCode()).isEqualTo("400");
        assertThat(courtLocationsDto[0].getLocationName()).isEqualTo("Jury Central Summoning Bureau");
        assertThat(courtLocationsDto[0].getAttendanceTime()).isNull();

        assertThat(courtLocationsDto[1].getLocationCode()).isEqualTo("428");
        assertThat(courtLocationsDto[1].getLocationName()).isEqualTo("Blackfriars");
        assertThat(courtLocationsDto[1].getAttendanceTime()).isNull();

        assertThat(courtLocationsDto[2].getLocationCode()).isEqualTo("440");
        assertThat(courtLocationsDto[2].getLocationName()).isEqualTo("Inner London Crown");
        assertThat(courtLocationsDto[2].getAttendanceTime()).isNull();

        assertThat(courtLocationsDto[3].getLocationCode()).isEqualTo("471");
        assertThat(courtLocationsDto[3].getLocationName().trim()).isEqualTo("Southwark");
        assertThat(courtLocationsDto[3].getAttendanceTime()).isNull();
    }

    @Test
    public void test_getAllCourtLocationsByPostcode_badRequestException() {
        //Invoke service.
        templateExchangeAllCourtLocationsByPostcode("SE1236LA", BUREAU_USER, "400", HttpStatus.BAD_REQUEST);
    }

    private CourtLocationDataDto[] templateExchangeAllCourtLocationsByPostcode(String postcode, String userType,
                                                                               String owner, HttpStatus httpStatus) {
        final URI uri =
            URI.create(String.format("/api/v1/moj/court-location/catchment-areas?postcode=%s", postcode));
        httpHeaders = initialiseHeaders("1", false, userType, 89, owner);

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        if (httpStatus.is2xxSuccessful()) {
            ResponseEntity<CourtLocationDataDto[]> response =
                restTemplate.exchange(requestEntity, CourtLocationDataDto[].class);
            assertThat(response.getStatusCode()).isEqualTo(httpStatus);
            return response.getBody();
        } else {
            ResponseEntity<String> response =
                restTemplate.exchange(requestEntity, String.class);
            assertThat(response.getStatusCode()).isEqualTo(httpStatus);
            return new CourtLocationDataDto[0];
        }
    }
}
