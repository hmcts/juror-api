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
import org.springframework.http.HttpMethod;
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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;

/**
 * Integration tests for the API endpoints defined in {@link CourtLocationController}.
 */
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
    void testGetCourtLocationsCourtUser() throws Exception {
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


    @Nested
    @DisplayName("GET " + GetCourtRates.URL)
    @Sql({"/db/mod/truncate.sql", "/db/CourtLocationControllerITest_getCourtRates.sql"})
    class GetCourtRates {
        public static final String URL = BASE_URL + "/{loc_code}/{date}/rates";

        private String toUrl(String locCode, LocalDate date) {
            return toUrl(locCode, date.toString());
        }

        private String toUrl(String locCode, String date) {
            return URL
                .replace("{loc_code}", locCode)
                .replace("{date}", date);
        }

        @DisplayName("Positive")
        @Nested
        class Positive {
            protected ResponseEntity<CourtRates> triggerValid(String locCode, LocalDate date) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415", locCode);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                ResponseEntity<CourtRates> response = restTemplate.exchange(
                    new RequestEntity<>(null, httpHeaders, GET,
                        URI.create(toUrl(locCode, date))),
                    CourtRates.class);
                assertThat(response.getStatusCode())
                    .as("Expect the HTTP GET request to be successful")
                    .isEqualTo(HttpStatus.OK);
                return response;
            }


            @Test
            void effectiveFromHistoricalDate() throws Exception {
                ResponseEntity<CourtRates> response = triggerValid("001", LocalDate.of(2023, 1, 5));

                CourtRates rates = response.getBody();
                assertThat(rates).isNotNull();
                assertThat(rates.getCarRate0Passengers()).isEqualTo(new BigDecimal("1.01000"));
                assertThat(rates.getCarRate1Passenger()).isEqualTo(new BigDecimal("2.02000"));
                assertThat(rates.getCarRate2OrMorePassenger()).isEqualTo(new BigDecimal("3.03000"));
                assertThat(rates.getMotorcycleRate0Passenger()).isEqualTo(new BigDecimal("4.04000"));
                assertThat(rates.getMotorcycleRate1OrMorePassenger()).isEqualTo(new BigDecimal("5.05000"));
                assertThat(rates.getBicycleRate0OrMorePassenger()).isEqualTo(new BigDecimal("6.06000"));
                assertThat(rates.getFinancialLossHalfDayLimit()).isEqualTo(new BigDecimal("7.07000"));
                assertThat(rates.getFinancialLossFullDayLimit()).isEqualTo(new BigDecimal("8.08000"));
                assertThat(rates.getFinancialLossHalfDayLongTrialLimit()).isEqualTo(new BigDecimal("9.09000"));
                assertThat(rates.getFinancialLossFullDayLongTrialLimit()).isEqualTo(new BigDecimal("10.01000"));
                assertThat(rates.getSubstanceRateStandard()).isEqualTo(new BigDecimal("11.01100"));
                assertThat(rates.getSubstanceRateLongDay()).isEqualTo(new BigDecimal("12.01200"));
                assertThat(rates.getPublicTransportSoftLimit()).isEqualTo(new BigDecimal("13.01300"));
            }

            @Test
            void effectiveFromFutureData() throws Exception {
                ResponseEntity<CourtRates> response = triggerValid("002", LocalDate.of(2023, 1, 5));

                CourtRates rates = response.getBody();
                assertThat(rates).isNotNull();
                assertThat(rates.getCarRate0Passengers()).isEqualTo(new BigDecimal("2.01000"));
                assertThat(rates.getCarRate1Passenger()).isEqualTo(new BigDecimal("2.02000"));
                assertThat(rates.getCarRate2OrMorePassenger()).isEqualTo(new BigDecimal("3.03000"));
                assertThat(rates.getMotorcycleRate0Passenger()).isEqualTo(new BigDecimal("4.04000"));
                assertThat(rates.getMotorcycleRate1OrMorePassenger()).isEqualTo(new BigDecimal("5.05000"));
                assertThat(rates.getBicycleRate0OrMorePassenger()).isEqualTo(new BigDecimal("6.06000"));
                assertThat(rates.getFinancialLossHalfDayLimit()).isEqualTo(new BigDecimal("7.07000"));
                assertThat(rates.getFinancialLossFullDayLimit()).isEqualTo(new BigDecimal("8.08000"));
                assertThat(rates.getFinancialLossHalfDayLongTrialLimit()).isEqualTo(new BigDecimal("9.09000"));
                assertThat(rates.getFinancialLossFullDayLongTrialLimit()).isEqualTo(new BigDecimal("10.01000"));
                assertThat(rates.getSubstanceRateStandard()).isEqualTo(new BigDecimal("11.01100"));
                assertThat(rates.getSubstanceRateLongDay()).isEqualTo(new BigDecimal("12.01200"));
                assertThat(rates.getPublicTransportSoftLimit()).isEqualTo(new BigDecimal("13.01300"));


                ResponseEntity<CourtRates> response2 = triggerValid("002", LocalDate.of(2023, 5, 6));

                CourtRates rates2 = response2.getBody();
                assertThat(rates2).isNotNull();
                assertThat(rates2.getCarRate0Passengers()).isEqualTo(new BigDecimal("3.01000"));
                assertThat(rates2.getCarRate1Passenger()).isEqualTo(new BigDecimal("2.02000"));
                assertThat(rates2.getCarRate2OrMorePassenger()).isEqualTo(new BigDecimal("3.03000"));
                assertThat(rates2.getMotorcycleRate0Passenger()).isEqualTo(new BigDecimal("4.04000"));
                assertThat(rates2.getMotorcycleRate1OrMorePassenger()).isEqualTo(new BigDecimal("5.05000"));
                assertThat(rates2.getBicycleRate0OrMorePassenger()).isEqualTo(new BigDecimal("6.06000"));
                assertThat(rates2.getFinancialLossHalfDayLimit()).isEqualTo(new BigDecimal("7.07000"));
                assertThat(rates2.getFinancialLossFullDayLimit()).isEqualTo(new BigDecimal("8.08000"));
                assertThat(rates2.getFinancialLossHalfDayLongTrialLimit()).isEqualTo(new BigDecimal("9.09000"));
                assertThat(rates2.getFinancialLossFullDayLongTrialLimit()).isEqualTo(new BigDecimal("10.01000"));
                assertThat(rates2.getSubstanceRateStandard()).isEqualTo(new BigDecimal("11.01100"));
                assertThat(rates2.getSubstanceRateLongDay()).isEqualTo(new BigDecimal("12.01200"));
                assertThat(rates2.getPublicTransportSoftLimit()).isEqualTo(new BigDecimal("13.01300"));
            }

            @Test
            void effectiveFromFutureDataNested() throws Exception {
                ResponseEntity<CourtRates> response = triggerValid("003", LocalDate.of(2023, 1, 5));

                CourtRates rates = response.getBody();
                assertThat(rates).isNotNull();
                assertThat(rates.getCarRate0Passengers()).isEqualTo(new BigDecimal("1.01000"));
                assertThat(rates.getCarRate1Passenger()).isEqualTo(new BigDecimal("2.02000"));
                assertThat(rates.getCarRate2OrMorePassenger()).isEqualTo(new BigDecimal("3.03000"));
                assertThat(rates.getMotorcycleRate0Passenger()).isEqualTo(new BigDecimal("4.04000"));
                assertThat(rates.getMotorcycleRate1OrMorePassenger()).isEqualTo(new BigDecimal("5.05000"));
                assertThat(rates.getBicycleRate0OrMorePassenger()).isEqualTo(new BigDecimal("6.06000"));
                assertThat(rates.getFinancialLossHalfDayLimit()).isEqualTo(new BigDecimal("7.07000"));
                assertThat(rates.getFinancialLossFullDayLimit()).isEqualTo(new BigDecimal("8.08000"));
                assertThat(rates.getFinancialLossHalfDayLongTrialLimit()).isEqualTo(new BigDecimal("9.09000"));
                assertThat(rates.getFinancialLossFullDayLongTrialLimit()).isEqualTo(new BigDecimal("10.01000"));
                assertThat(rates.getSubstanceRateStandard()).isEqualTo(new BigDecimal("11.01100"));
                assertThat(rates.getSubstanceRateLongDay()).isEqualTo(new BigDecimal("12.01200"));
                assertThat(rates.getPublicTransportSoftLimit()).isEqualTo(new BigDecimal("13.01300"));


                ResponseEntity<CourtRates> response2 = triggerValid("003", LocalDate.of(2023, 6, 6));

                CourtRates rates2 = response2.getBody();
                assertThat(rates2).isNotNull();
                assertThat(rates2.getCarRate0Passengers()).isEqualTo(new BigDecimal("5.01000"));
                assertThat(rates2.getCarRate1Passenger()).isEqualTo(new BigDecimal("5.02000"));
                assertThat(rates2.getCarRate2OrMorePassenger()).isEqualTo(new BigDecimal("5.03000"));
                assertThat(rates2.getMotorcycleRate0Passenger()).isEqualTo(new BigDecimal("5.04000"));
                assertThat(rates2.getMotorcycleRate1OrMorePassenger()).isEqualTo(new BigDecimal("5.05000"));
                assertThat(rates2.getBicycleRate0OrMorePassenger()).isEqualTo(new BigDecimal("5.06000"));
                assertThat(rates2.getFinancialLossHalfDayLimit()).isEqualTo(new BigDecimal("5.07000"));
                assertThat(rates2.getFinancialLossFullDayLimit()).isEqualTo(new BigDecimal("5.08000"));
                assertThat(rates2.getFinancialLossHalfDayLongTrialLimit()).isEqualTo(new BigDecimal("5.09000"));
                assertThat(rates2.getFinancialLossFullDayLongTrialLimit()).isEqualTo(new BigDecimal("5.01000"));
                assertThat(rates2.getSubstanceRateStandard()).isEqualTo(new BigDecimal("5.01100"));
                assertThat(rates2.getSubstanceRateLongDay()).isEqualTo(new BigDecimal("5.01200"));
                assertThat(rates2.getPublicTransportSoftLimit()).isEqualTo(new BigDecimal("5.01300"));
            }
        }

        @DisplayName("Negative")
        @Nested
        class Negative {
            protected ResponseEntity<String> triggerInvalid(String locCode, LocalDate date) throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415", locCode);
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                return restTemplate.exchange(
                    new RequestEntity<>(null, httpHeaders, GET,
                        URI.create(toUrl(locCode, date))),
                    String.class);
            }

            @Test
            void courtNotFound() throws Exception {
                validateNotFound(
                    triggerInvalid("004", LocalDate.of(2023, 1, 1)),
                    toUrl("004", LocalDate.of(2023, 1, 1)),
                    "No court location rates are active on date: 2023-01-01 for court 004");
            }

            @Test
            void effectiveFromRatesNotFound() throws Exception {
                validateNotFound(
                    triggerInvalid("103", LocalDate.of(2024, 1, 1)),
                    toUrl("103", LocalDate.of(2024, 1, 1)),
                    "No court location rates are active on date: 2024-01-01 for court 103");

            }

            @Test
            void unauthorisedNotPartOfCourt() throws Exception {
                String url = toUrl("001", LocalDate.now());
                final String jwt = createBureauJwt(COURT_USER, "415", "415");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                validateForbiddenResponse(restTemplate.exchange(
                    new RequestEntity<>(null, httpHeaders, GET,
                        URI.create(url)),
                    String.class), url);
            }

            @Test
            void invalidLocCode() throws Exception {
                validateInvalidPathParam(
                    triggerInvalid("INVALID", LocalDate.of(2024, 1, 1)),
                    "getCourtRates.locCode: size must be between 3 and 3");
            }

            @Test
            void invalidDate() throws Exception {
                final String jwt = createBureauJwt(COURT_USER, "415", "001");
                httpHeaders.set(HttpHeaders.AUTHORIZATION, jwt);
                validateInvalidPathParam(restTemplate.exchange(
                        new RequestEntity<>(null, httpHeaders, GET,
                            URI.create(toUrl("001", "INVALID"))),
                        String.class),
                    "INVALID is the incorrect data type or is not in the expected format (date)");

            }
        }
    }
}
