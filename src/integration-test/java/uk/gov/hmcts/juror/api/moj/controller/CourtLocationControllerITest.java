package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtLocationDataDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtLocationListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtRates;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;

/**
 * Integration tests for the API endpoints defined in {@link CourtLocationController}.
 */
@SuppressWarnings("PMD.LawOfDemeter")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CourtLocationControllerITest extends AbstractIntegrationTest {

    private static final String BASE_URL = "/api/v1/moj/court-location";

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private CourtLocationRepository courtLocationRepository;

    private HttpHeaders httpHeaders;

    @Override
    @BeforeEach
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
    void testGetCourtLocationsBureauUser() {
        ResponseEntity<CourtLocationListDto> responseEntity =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, GET,
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
    void testGetCourtLocationsCourtUser() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Arrays.asList("415", "462", "767", "774")));
        ResponseEntity<CourtLocationListDto> responseEntity =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, GET,
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
    void testGetAllCourtLocationsByPostcodeHappy() {
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
    void testGetAllCourtLocationsByPostcodeBadRequestException() {
        //Invoke service.
        templateExchangeAllCourtLocationsByPostcode("SE1236LA", BUREAU_USER, "400", HttpStatus.BAD_REQUEST);
    }

    private CourtLocationDataDto[] templateExchangeAllCourtLocationsByPostcode(String postcode, String userType,
                                                                               String owner, HttpStatus httpStatus) {
        final URI uri =
            URI.create(String.format("/api/v1/moj/court-location/catchment-areas?postcode=%s", postcode));
        httpHeaders = initialiseHeaders("1", false, userType, 89, owner);

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, GET, uri);
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


    @Nested
    @DisplayName("GET " + GetCourtRates.URL)
    @Sql({"/db/mod/truncate.sql", "/db/CourtLocationControllerITest_getCourtRates.sql"})
    @Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
        statements = {"delete from juror_mod.court_location where loc_code in ('001')"})
    class GetCourtRates {
        public static final String URL = BASE_URL + "/{loc_code}/rates";

        private String toUrl(String locCode) {
            return URL
                .replace("{loc_code}", locCode);
        }

        @DisplayName("Positive")
        @Nested
        class Positive {
            protected ResponseEntity<CourtRates> triggerValid(String locCode) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415", locCode);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<CourtRates> response = restTemplate.exchange(
                    new RequestEntity<>(null, httpHeaders, GET,
                        URI.create(toUrl(locCode))),
                    CourtRates.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                return response;
            }


            @Test
            void typical() throws Exception {
                ResponseEntity<CourtRates> response = triggerValid("001");

                CourtRates rates = response.getBody();
                assertThat(rates).isNotNull();
                assertThat(rates.getPublicTransportSoftLimit()).isEqualTo(new BigDecimal("13.01300"));
                assertThat(rates.getTaxiSoftLimit()).isEqualTo(new BigDecimal("14.01400"));
            }
        }

        @DisplayName("Negative")
        @Nested
        class Negative {
            protected ResponseEntity<String> triggerInvalid(String locCode) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415", locCode);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return restTemplate.exchange(
                    new RequestEntity<>(null, httpHeaders, GET,
                        URI.create(toUrl(locCode))),
                    String.class);
            }

            @Test
            void courtNotFound() throws Exception {
                assertNotFound(
                    triggerInvalid("004"),
                    toUrl("004"),
                    "Court location not found");
            }

            @Test
            void unauthorisedNotPartOfCourt() throws Exception {
                String url = toUrl("001");
                final String jwt = createBureauJwt(COURT_USER, "415", "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                assertForbiddenResponse(restTemplate.exchange(
                    new RequestEntity<>(null, httpHeaders, GET,
                        URI.create(url)),
                    String.class), url);
            }

            @Test
            void invalidLocCode() throws Exception {
                assertInvalidPathParam(
                    triggerInvalid("INVALID"),
                    "getCourtRates.locCode: size must be between 3 and 3");
            }
        }
    }
}
