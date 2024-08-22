package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtLocationDataDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CourtLocationListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolNumbersListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestActiveListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestDataDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolsAtCourtLocationListDto;
import uk.gov.hmcts.juror.api.moj.domain.DayType;
import uk.gov.hmcts.juror.api.moj.domain.HistoryCode;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.QPoolHistory;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.repository.ConfirmationLetterRepository;
import uk.gov.hmcts.juror.api.moj.repository.CurrentlyDeferredRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.utils.RepositoryUtils;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;

/**
 * Integration tests for the API endpoints defined in {@link RequestPoolController}.
 */
@RunWith(SpringRunner.class)
@SuppressWarnings({"PMD.ExcessivePublicCount", "PMD.TooManyMethods", "PMD.ExcessiveImports"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RequestPoolControllerITest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private PoolRequestRepository poolRequestRepository;
    @Autowired
    private JurorPoolRepository jurorPoolRepository;
    @Autowired
    private PoolHistoryRepository poolHistoryRepository;
    @Autowired
    private JurorHistoryRepository jurorHistoryRepository;
    @Autowired
    private ConfirmationLetterRepository confirmLetterRepository;

    private HttpHeaders httpHeaders;
    @Autowired
    private CurrentlyDeferredRepository currentlyDeferredRepository;

    @Before
    public void setUp() throws Exception {
        initHeaders();
    }

    private void initHeaders() throws Exception {
        final String bureauJwt = getBureauJwt();

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    private String initCourtsJwt(String owner, List<String> courts) throws Exception {

        return mintBureauJwt(BureauJwtPayload.builder()
            .userType(UserType.COURT)
            .login("COURT_USER")
            .owner(owner)
            .staff(BureauJwtPayload.Staff.builder().courts(courts).build())
            .build());
    }

    @Test
    public void test_getCourtLocations_bureauUser() {
        ResponseEntity<CourtLocationListDto> responseEntity =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/pool-request/court-locations")), CourtLocationListDto.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(OK);

        CourtLocationListDto responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getData().isEmpty())
            .as("Expect the response body to contain a list of Court Locations (not empty)")
            .isFalse();

        CourtLocationDataDto dataItem = responseBody.getData().get(0);
        assertThat(dataItem.getLocationCode().isEmpty())
            .as("Expect the location code to be populated in the list of data items")
            .isFalse();
        assertThat(dataItem.getLocationName().isEmpty())
            .as("Expect the location name to be populated in the list of data items")
            .isFalse();
        assertThat(dataItem.getAttendanceTime().isEmpty())
            .as("Expect the attendance time to be populated in the list of data items")
            .isFalse();
        assertThat(dataItem.getOwner().isEmpty())
            .as("Expect the owner to be populated in the list of data items")
            .isFalse();
    }

    @Test
    public void test_getCourtLocations_courtUser() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("799", Collections.singletonList("799")));
        ResponseEntity<CourtLocationListDto> responseEntity =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/pool-request/court-locations")), CourtLocationListDto.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(OK);

        CourtLocationListDto responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getData().size())
            .as("Expect the response body to contain a single Court Location in the returned list")
            .isEqualTo(1);

        CourtLocationDataDto dataItem = responseBody.getData().get(0);
        assertThat(dataItem.getLocationCode())
            .as("Expect the location code to be populated in the list of data items")
            .isEqualTo("799");
        assertThat(dataItem.getLocationName())
            .as("Expect the location name to be populated in the list of data items")
            .isEqualTo("HOVE");
        assertThat(dataItem.getAttendanceTime())
            .as("Expect the attendance time to be populated in the list of data items")
            .isEqualTo("09:00");
        assertThat(dataItem.getOwner())
            .as("Expect the owner to be populated in the list of data items")
            .isEqualTo("799");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_initPoolRequests.sql"})
    public void test_getPoolRequests_withoutLocCodeParam_bureauUser() {
        ResponseEntity<PaginatedList<PoolRequestDataDto>> responseEntity =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/pool-request/pools-requested"
                        + "?pageNumber=1&pageLimit=25&sortBy=RETURN_DATE&sortOrder=ASC")),
                new ParameterizedTypeReference<>() {});

        assertThat(responseEntity.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(OK);

        PaginatedList<PoolRequestDataDto> responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getData().size())
            .as("Expect the response body to contain 2 Pool Requests owned by the Bureau")
            .isEqualTo(25);
        assertThat(responseBody.getTotalItems())
            .as("The total number of entries available in the table")
            .isEqualTo(31);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_initPoolRequests.sql"})
    public void test_getPoolRequests_withoutLocCodeParam_bureauUser_page2() {
        ResponseEntity<PaginatedList<PoolRequestDataDto>> responseEntity =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/pool-request/pools-requested"
                        + "?pageNumber=2&pageLimit=25&sortBy=RETURN_DATE&sortOrder=ASC")),
                new ParameterizedTypeReference<>() {});

        assertThat(responseEntity.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(OK);

        PaginatedList<PoolRequestDataDto> responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getData().size())
            .as("Expect the response body to contain 2 Pool Requests owned by the Bureau")
            .isEqualTo(6);
        assertThat(responseBody.getTotalItems())
            .as("The total number of entries available in the table")
            .isEqualTo(31);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_initPoolRequests.sql"})
    public void test_getPoolRequests_withoutLocCodeParam_courtUser() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, getSatelliteCourtJwt("457","457", "479"));

        ResponseEntity<PaginatedList<PoolRequestDataDto>> responseEntity =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/pool-request/pools-requested"
                        + "?pageNumber=1&pageLimit=25&sortBy=RETURN_DATE&sortOrder=ASC")),
                new ParameterizedTypeReference<>() {});

        assertThat(responseEntity.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(OK);

        PaginatedList<PoolRequestDataDto> responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getData().size())
            .as("Expect the response body to contain 2 Pool Request, filtered on user authority")
            .isEqualTo(2);
        assertThat(responseBody.getTotalItems())
            .as("The total number of entries available in the table")
            .isEqualTo(2);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_initPoolRequests.sql"})
    public void test_getPoolRequests_createdWithLocCodeParam() {
        ResponseEntity<PoolRequestActiveListDto> responseEntity =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/pool-request/pools-active?locCode=415&tab=bureau&offset=0&sortBy"
                    + "=poolNumber&sortOrder=asc")), PoolRequestActiveListDto.class);

        assertThat(responseEntity.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(OK);

        PoolRequestActiveListDto responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getData().size())
            .as("Expect the response body to contain 2 Pool Requests")
            .isEqualTo(2);

        PoolRequestActiveListDto.PoolRequestActiveDataDto data = responseBody.getData().get(1);
        assertThat(data.getCourtName())
            .as("Court Name should be populated from the Court Location table using the LOC_CODE foreign key")
            .isEqualTo("CHESTER");
        assertThat(data.getPoolNumber())
            .as("Pool Number should be mapped from the Pool Request view")
            .isEqualTo("415220110");
        assertThat(data.getPoolType())
            .as("Pool Type should be mapped from the Pool Request view")
            .isEqualTo("CROWN COURT");
        assertThat(data.getRequestedFromBureau())
            .as("Number Requested should be mapped from the Pool Request view")
            .isEqualTo(10);
        assertThat(data.getAttendanceDate())
            .as("Attendance Date should be mapped from the RETURN_DATE column in the Pool Request view")
            .hasDayOfMonth(20).hasMonth(Month.JANUARY).hasYear(2022);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_initPoolRequests.sql"})
    public void test_getPoolRequests_requestedWithLocCodeParam_noPoolsExist() {
        ResponseEntity<PaginatedList<PoolRequestDataDto>> responseEntity =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/pool-request/pools-requested"
                    + "?locCode=414&pageNumber=1&pageLimit=25&sortBy=RETURN_DATE&sortOrder=ASC")),
                new ParameterizedTypeReference<>() {});

        assertThat(responseEntity.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(OK);

        PaginatedList<PoolRequestDataDto> responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getData().isEmpty())
            .as("Expect the response body to be an empty list")
            .isTrue();
        assertThat(responseBody.getTotalItems())
            .as("The total number of entries available in the table")
            .isEqualTo(0);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_initPoolRequests.sql"})
    public void test_getPoolRequests_requestedWithLocCodeParam_poolsExist() {
        ResponseEntity<PaginatedList<PoolRequestDataDto>> responseEntity =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/pool-request/pools-requested"
                    + "?locCode=415&pageNumber=1&pageLimit=25&sortBy=RETURN_DATE&sortOrder=ASC")),
                new ParameterizedTypeReference<>() {});

        assertThat(responseEntity.getStatusCode())
            .as("Expect the HTTP GET request to be successful")
            .isEqualTo(OK);

        PaginatedList<PoolRequestDataDto> responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getData().size())
            .as("Expect the response body to be an empty list")
            .isEqualTo(9);
    }

    private PoolRequestDto createValidPoolRequestDto() {
        PoolRequestDto poolRequestDto = new PoolRequestDto();
        poolRequestDto.setPoolNumber("123456789");
        poolRequestDto.setLocationCode("799");
        poolRequestDto.setAttendanceDate(LocalDate.of(2022, 1, 8));
        poolRequestDto.setNumberRequested(10);
        poolRequestDto.setPoolType("CRO");
        poolRequestDto.setAttendanceTime(LocalTime.of(10, 12));
        poolRequestDto.setDeferralsUsed(0);
        poolRequestDto.setCourtOnly(false);
        return poolRequestDto;
    }

    private PoolRequestDto createValidCourtOnlyPoolRequestDto() {
        PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        poolRequestDto.setNumberRequested(0);
        poolRequestDto.setAttendanceTime(null);
        poolRequestDto.setCourtOnly(true);
        return poolRequestDto;
    }

    @Test
    @Sql("/db/mod/truncate.sql")
    public void test_requestNewPoolFromBureau_validRequest_bureauUser() {

        HttpEntity<PoolRequestDto> entity = new HttpEntity<>(createValidPoolRequestDto(), httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(URI.create("/api/v1/moj/pool-request/new-pool"),
            HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode())
            .as("All fields in the request should satisfy the validation requirements, "
                + "expect a new Pool Request to be created successfullyRequest")
            .isEqualTo(HttpStatus.CREATED);

        PoolRequest poolRequest = RepositoryUtils.retrieveFromDatabase("123456789", poolRequestRepository);
        PoolRequestDto requestedPoolData = entity.getBody();

        assertThat(requestedPoolData).isNotNull();

        assertThat(poolRequest.getOwner())
            .as("Created Pool Request owner should be the Bureau")
            .isEqualTo("400");
        assertThat(poolRequest.getPoolNumber())
            .as("Created Pool Request pool number should be mapped from the request DTO")
            .isEqualTo(requestedPoolData.getPoolNumber());
        assertThat(poolRequest.getCourtLocation().getLocCode())
            .as("Created Pool Request location code should be mapped from the request DTO")
            .isEqualTo(requestedPoolData.getLocationCode());
        LocalDate expectedAttendanceDate = requestedPoolData.getAttendanceDate();
        assertThat(poolRequest.getReturnDate())
            .as("Created Pool Request return date should be mapped from the request DTO")
            .hasYear(expectedAttendanceDate.getYear()).hasMonth(expectedAttendanceDate.getMonth())
            .hasDayOfMonth(expectedAttendanceDate.getDayOfMonth());
        assertThat(poolRequest.getNumberRequested())
            .as("Created Pool Request number requested should be calculated from the request DTO number "
                + "requested - deferrals used")
            .isEqualTo(requestedPoolData.getNumberRequested() - requestedPoolData.getDeferralsUsed());
        assertThat(poolRequest.getPoolType().getPoolType())
            .as("Created Pool Request pool type should be mapped from the request DTO")
            .isEqualTo(requestedPoolData.getPoolType());
        LocalTime expectedAttendTime = requestedPoolData.getAttendanceTime();
        assertThat(poolRequest.getAttendTime())
            .as("Created Pool Request attend time should be mapped from the request DTO")
            .hasYear(expectedAttendanceDate.getYear()).hasMonth(expectedAttendanceDate.getMonth())
            .hasDayOfMonth(expectedAttendanceDate.getDayOfMonth())
            .hasHour(expectedAttendTime.getHour()).hasMinute(expectedAttendTime.getMinute());
        assertThat(poolRequest.getNewRequest())
            .as("Pools requested by the bureau should have a default NEW_REQUEST value of 'Y'")
            .isEqualTo('Y');
        assertThat(poolRequest.getTotalNoRequired())
            .as("Created Pool Request should have a Total Required value of 10")
            .isEqualTo(requestedPoolData.getNumberRequested());
        assertThat(poolRequest.isNilPool())
            .as("Created Pool Request should have a Nil Pool value of false")
            .isFalse();
    }

    @Test
    @Sql("/db/mod/truncate.sql")
    public void test_requestNewPoolFromBureau_validRequest_courtsUser() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("457", Arrays.asList("457", "799")));
        HttpEntity<PoolRequestDto> entity = new HttpEntity<>(createValidPoolRequestDto(), httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
            URI.create("/api/v1/moj/pool-request/new-pool"),
            HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode())
            .as("All fields in the request should satisfy the validation requirements, "
                + "expect a new Pool Request to be created successfullyRequest")
            .isEqualTo(HttpStatus.CREATED);

        PoolRequest poolRequest = RepositoryUtils.retrieveFromDatabase("123456789", poolRequestRepository);
        PoolRequestDto requestedPoolData = entity.getBody();

        assertThat(requestedPoolData).isNotNull();

        assertThat(poolRequest.getOwner())
            .as("Created Pool Request owner should be the Bureau")
            .isEqualTo("400");
        assertThat(poolRequest.getPoolNumber())
            .as("Created Pool Request pool number should be mapped from the request DTO")
            .isEqualTo(requestedPoolData.getPoolNumber());
        assertThat(poolRequest.getCourtLocation().getLocCode())
            .as("Created Pool Request location code should be mapped from the request DTO")
            .isEqualTo(requestedPoolData.getLocationCode());
        LocalDate expectedAttendanceDate = requestedPoolData.getAttendanceDate();
        assertThat(poolRequest.getReturnDate())
            .as("Created Pool Request return date should be mapped from the request DTO")
            .hasYear(expectedAttendanceDate.getYear()).hasMonth(expectedAttendanceDate.getMonth())
            .hasDayOfMonth(expectedAttendanceDate.getDayOfMonth());
        assertThat(poolRequest.getNumberRequested())
            .as("Created Pool Request number requested should be calculated from the request DTO number "
                + "requested - deferrals used")
            .isEqualTo(requestedPoolData.getNumberRequested() - requestedPoolData.getDeferralsUsed());
        assertThat(poolRequest.getPoolType().getPoolType())
            .as("Created Pool Request pool type should be mapped from the request DTO")
            .isEqualTo(requestedPoolData.getPoolType());
        LocalTime expectedAttendTime = requestedPoolData.getAttendanceTime();
        assertThat(poolRequest.getAttendTime())
            .as("Created Pool Request attend time should be mapped from the request DTO")
            .hasYear(expectedAttendanceDate.getYear()).hasMonth(expectedAttendanceDate.getMonth())
            .hasDayOfMonth(expectedAttendanceDate.getDayOfMonth())
            .hasHour(expectedAttendTime.getHour()).hasMinute(expectedAttendTime.getMinute());
        assertThat(poolRequest.isNilPool())
            .as("Created Pool Request should have a Nil Pool value of false")
            .isFalse();
    }

    @Test
    @Sql("/db/mod/truncate.sql")
    public void test_requestNewPoolFromBureau_satelliteCourtOwner() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Arrays.asList("415", "767")));
        PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        poolRequestDto.setLocationCode("767");
        HttpEntity<PoolRequestDto> entity = new HttpEntity<>(poolRequestDto, httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
            URI.create("/api/v1/moj/pool-request/new-pool"),
            HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode())
            .as("All fields in the request should satisfy the validation requirements, "
                + "expect a new Pool Request to be created successfully")
            .isEqualTo(HttpStatus.CREATED);

        PoolRequest poolRequest = RepositoryUtils.retrieveFromDatabase("123456789", poolRequestRepository);
        PoolRequestDto requestedPoolData = entity.getBody();

        assertThat(requestedPoolData).isNotNull();

        assertThat(poolRequest.getOwner())
            .as("Created Pool Request owner should be the Bureau")
            .isEqualTo("400");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_initDeferrals.sql"})
    public void test_requestNewPoolFromBureau_satelliteCourtDeferrals() throws Exception {
        String ownerCourt = "415";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(ownerCourt, Arrays.asList(ownerCourt, "767")));
        PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        poolRequestDto.setDeferralsUsed(1);
        poolRequestDto.setPoolNumber("767221001");
        poolRequestDto.setLocationCode("767");
        poolRequestDto.setAttendanceDate(LocalDate.of(2022, 10, 3));
        HttpEntity<PoolRequestDto> entity = new HttpEntity<>(poolRequestDto, httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
            URI.create("/api/v1/moj/pool-request/new-pool"),
            HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode())
            .as("All fields in the request should satisfy the validation requirements, expect a new Pool "
                + "Request to be created successfully")
            .isEqualTo(HttpStatus.CREATED);

        PoolRequest newlyRequestedPool = RepositoryUtils.retrieveFromDatabase("767221001",
            poolRequestRepository);
        PoolRequestDto requestedPoolData = entity.getBody();

        assertThat(requestedPoolData).isNotNull();

        assertThat(newlyRequestedPool.getNumberRequested())
            .as("Created Pool Request number requested should be calculated from the request DTO number "
                + "requested - deferrals used")
            .isEqualTo(requestedPoolData.getNumberRequested() - requestedPoolData.getDeferralsUsed());

        assertThat(currentlyDeferredRepository.count())
            .as("Expect 1 records to be deleted from DEFER_DBF, leaving 6 remaining records")
            .isEqualTo(6);

        Map<String, String> oldJurorPoolIdMap = new ConcurrentHashMap<>();
        oldJurorPoolIdMap.put("444444444", "767220401");
        assertJurorPoolsInactive(oldJurorPoolIdMap);

        assertThat(jurorPoolRepository.count())
            .as("Expect 1 new records to be added to the POOL_MEMBER view")
            .isEqualTo(8);

        Map<String, String> newJurorPoolIdMap = new ConcurrentHashMap<>();
        newJurorPoolIdMap.put("444444444", "767221001");
        assertNewJurorPoolCreated(newlyRequestedPool, newJurorPoolIdMap);

        assertThat(poolHistoryRepository.count(QPoolHistory.poolHistory.historyCode.eq(HistoryCode.PDEF)))
            .as("Expect no records to be added to POOL_HISTORY with a History code of PDEF (court deferrals "
                + "are not currently recorded in POOL_HISTORY")
            .isEqualTo(0);
        assertThat(poolHistoryRepository.count(QPoolHistory.poolHistory.historyCode.eq(HistoryCode.PREQ)))
            .as("Expect one record to be added to POOL_HISTORY with a History code of PREQ (indicating the total "
                + "number required has been set/updated")
            .isEqualTo(1);

        assertThat(jurorHistoryRepository.count())
            .as("Expect 1 record to be added to juror_history, one for each deferral used in this newly requested pool")
            .isEqualTo(1);
        Map<String, String> newJurorHistoryMap = new ConcurrentHashMap<>();
        newJurorHistoryMap.put("Owner", ownerCourt);
        newJurorHistoryMap.put("PoolNumber", newlyRequestedPool.getPoolNumber());
        newJurorHistoryMap.put("HistoryCode", "PDEF");
        newJurorHistoryMap.put("Info", "Added to New Pool");
        newJurorHistoryMap.put("UserId", "COURT_USER");
        assertJurorHistoryCreated(jurorHistoryRepository.findAll(), newJurorHistoryMap,
            Collections.singletonList("444444444"));
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_initDeferrals.sql"})
    public void test_requestNewPoolFromBureau_withCourtDeferrals() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Arrays.asList("415", "416")));
        String ownerCourt = "415";
        PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        poolRequestDto.setDeferralsUsed(3);
        poolRequestDto.setPoolNumber("415221001");
        poolRequestDto.setLocationCode(ownerCourt);
        poolRequestDto.setAttendanceDate(LocalDate.of(2022, 10, 3));
        HttpEntity<PoolRequestDto> entity = new HttpEntity<>(poolRequestDto, httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
            URI.create("/api/v1/moj/pool-request/new-pool"),
            HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode())
            .as("All fields in the request should satisfy the validation requirements, "
                + "expect a new Pool Request to be created successfullyRequest")
            .isEqualTo(HttpStatus.CREATED);

        PoolRequest newlyRequestedPool = RepositoryUtils.retrieveFromDatabase("415221001",
            poolRequestRepository);
        PoolRequestDto requestedPoolData = entity.getBody();

        assertThat(requestedPoolData).isNotNull();

        assertThat(newlyRequestedPool.getNumberRequested())
            .as("Created Pool Request number requested should be calculated from the request DTO number "
                + "requested - deferrals used")
            .isEqualTo(requestedPoolData.getNumberRequested() - requestedPoolData.getDeferralsUsed());

        assertThat(currentlyDeferredRepository.count())
            .as("Expect 3 records to be deleted from DEFER_DBF, leaving 4 remaining records")
            .isEqualTo(4);

        Map<String, String> oldJurorPoolIdMap = new ConcurrentHashMap<>();
        oldJurorPoolIdMap.put("111111111", "415220401");
        oldJurorPoolIdMap.put("222222222", "415220401");
        oldJurorPoolIdMap.put("333333333", "415220502");
        assertJurorPoolsInactive(oldJurorPoolIdMap);

        assertThat(jurorPoolRepository.count())
            .as("Expect 3 new records to be added to the POOL_MEMBER view")
            .isEqualTo(10);

        Map<String, String> newJurorPoolIdMap = new ConcurrentHashMap<>();
        newJurorPoolIdMap.put("111111111", "415221001");
        newJurorPoolIdMap.put("222222222", "415221001");
        newJurorPoolIdMap.put("333333333", "415221001");
        assertNewJurorPoolCreated(newlyRequestedPool, newJurorPoolIdMap);

        assertThat(poolHistoryRepository.count(QPoolHistory.poolHistory.historyCode.eq(HistoryCode.PDEF)))
            .as("Expect no records to be added to POOL_HISTORY with a History code of PDEF (court deferrals"
                + "are not currently recorded in POOL_HISTORY")
            .isEqualTo(0);
        assertThat(poolHistoryRepository.count(QPoolHistory.poolHistory.historyCode.eq(HistoryCode.PREQ)))
            .as("Expect one record to be added to POOL_HISTORY with a History code of PREQ (indicating the total "
                + "number required has been set/updated")
            .isEqualTo(1);

        assertThat(jurorHistoryRepository.count())
            .as("Expect 3 records to be added to juror_history, one for each deferral used in this newly requested "
                + "pool")
            .isEqualTo(3);
        Map<String, String> newJurorHistoryMap = new ConcurrentHashMap<>();
        newJurorHistoryMap.put("Owner", ownerCourt);
        newJurorHistoryMap.put("PoolNumber", newlyRequestedPool.getPoolNumber());
        newJurorHistoryMap.put("HistoryCode", "PDEF");
        newJurorHistoryMap.put("Info", "Added to New Pool");
        newJurorHistoryMap.put("UserId", "COURT_USER");
        assertJurorHistoryCreated(jurorHistoryRepository.findAll(), newJurorHistoryMap,
            Arrays.asList("111111111", "222222222", "333333333"));

        assertThat(newlyRequestedPool.getTotalNoRequired())
            .as("Total Number Required should be mapped from the Pool Request Dto (Number Requested)")
            .isEqualTo(poolRequestDto.getNumberRequested());
        assertThat(newlyRequestedPool.isNilPool())
            .as("Created Pool Request should have a Nil Pool value of false")
            .isFalse();
    }

    @Test
    @Sql("/db/mod/truncate.sql")
    public void test_requestNewCourtOnlyPool_validRequest_courtsUser() throws Exception {
        String courtOwner = "457";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(courtOwner, Arrays.asList(courtOwner, "799")));
        HttpEntity<PoolRequestDto> entity = new HttpEntity<>(createValidCourtOnlyPoolRequestDto(), httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
            URI.create("/api/v1/moj/pool-request/new-pool"),
            HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode())
            .as("All fields in the request should satisfy the validation requirements, "
                + "expect a new Pool Request to be created successfully")
            .isEqualTo(HttpStatus.CREATED);

        PoolRequest poolRequest = RepositoryUtils.retrieveFromDatabase("123456789", poolRequestRepository);
        PoolRequestDto requestedPoolData = entity.getBody();

        assertThat(requestedPoolData).isNotNull();

        assertThat(poolRequest.getOwner())
            .as("Created Pool Request owner should be the Court")
            .isEqualTo(courtOwner);
        assertThat(poolRequest.getPoolNumber())
            .as("Created Pool Request pool number should be mapped from the request DTO")
            .isEqualTo(requestedPoolData.getPoolNumber());
        assertThat(poolRequest.getCourtLocation().getLocCode())
            .as("Created Pool Request location code should be mapped from the request DTO")
            .isEqualTo(requestedPoolData.getLocationCode());
        LocalDate expectedAttendanceDate = requestedPoolData.getAttendanceDate();
        assertThat(poolRequest.getReturnDate())
            .as("Created Pool Request return date should be mapped from the request DTO")
            .isEqualTo(expectedAttendanceDate);
        assertThat(poolRequest.getNumberRequested())
            .as("Created Pool Request number requested should be null for court-use only pools")
            .isNull();
        assertThat(poolRequest.getPoolType().getPoolType())
            .as("Created Pool Request pool type should be mapped from the request DTO")
            .isEqualTo(requestedPoolData.getPoolType());
        assertThat(poolRequest.getAttendTime())
            .as("Created Pool Request attend time should be null for court-use only pools")
            .isNull();
        assertThat(poolRequest.isNilPool())
            .as("Created Pool Request should have a Nil Pool value of false")
            .isFalse();
    }

    @Test
    @Sql("/db/mod/truncate.sql")
    public void test_requestNewCourtOnlyPool_invalidBureauUser() {
        HttpEntity<PoolRequestDto> entity = new HttpEntity<>(createValidCourtOnlyPoolRequestDto(), httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
            URI.create("/api/v1/moj/pool-request/new-pool"),
            HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be unsuccessful")
            .isEqualTo(FORBIDDEN);

        assertThatExceptionOfType(MojException.NotFound.class).isThrownBy(() ->
            RepositoryUtils.retrieveFromDatabase("123456789", poolRequestRepository));
    }

    private void assertJurorPoolsInactive(Map<String, String> jurorPoolIdMap) {
        for (String jurorNumber : jurorPoolIdMap.keySet()) {
            JurorPool oldDeferredJuror =
                jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumberAndIsActive(jurorNumber,
                    jurorPoolIdMap.get(jurorNumber), false).get();
            assertThat(oldDeferredJuror.getIsActive())
                .as("Expect the old deferred juror record to be updated to inactive")
                .isFalse();
        }
    }

    private void assertNewJurorPoolCreated(PoolRequest newPool, Map<String, String> jurorPoolIdMap) {
        for (String jurorNumber : jurorPoolIdMap.keySet()) {
            JurorPool newJurorPool = jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumberAndIsActive(jurorNumber,
                jurorPoolIdMap.get(jurorNumber), true).get();
            Juror newJurorRecord = newJurorPool.getJuror();
            assertThat(newJurorPool.getWasDeferred())
                .as("Expect the new juror record to be created as was_deferred")
                .isTrue();
            assertThat(newJurorPool.getIsActive())
                .as("Expect the new juror record to be created as active")
                .isTrue();
            assertThat(newJurorPool.getStatus().getStatus())
                .as("Expect the new juror record to be created with a status of Responded")
                .isEqualTo(2L);
            assertThat(newJurorPool.getDeferralDate())
                .as("Expect the new juror record to be created with a null deferral date")
                .isNull();
            assertThat(newJurorRecord.getExcusalDate())
                .as("Expect the new juror record to be created with a null excusal date")
                .isNull();
            assertThat(newJurorRecord.getExcusalCode())
                .as("Expect the new juror record to be created with a null excusal code")
                .isNull();
            assertThat(newJurorPool.getUserEdtq())
                .as("Expect the new juror record to be created with the current user login recorded")
                .isEqualTo("COURT_USER");
            LocalDate returnDate = newPool.getReturnDate();
            assertThat(newJurorPool.getNextDate())
                .as("Expect the new juror record to be created with a next date matching the new pool's return date")
                .hasYear(returnDate.getYear()).hasMonth(returnDate.getMonth())
                .hasDayOfMonth(returnDate.getDayOfMonth());
            assertThat(newJurorRecord.getExcusalRejected())
                .as("Expect the new juror record to be created with a null excusal rejected value")
                .isNull();
            assertThat(newJurorRecord.getDisqualifyDate())
                .as("Expect the new juror record to be created with a null disqualify date value")
                .isNull();
            assertThat(newJurorRecord.getDisqualifyCode())
                .as("Expect the new juror record to be created with a null disqualify date value")
                .isNull();
            assertThat(newJurorPool.getPoolSequence())
                .as("Expect the new juror record to be created with a valid pool member sequence number")
                .isNotNull();
        }
    }

    private void assertJurorHistoryCreated(Iterable<JurorHistory> jurorHistRecords, Map<String, String> jurorHistMap,
                                           List<String> jurorNumbers) {
        for (JurorHistory record : jurorHistRecords) {
            assertThat(jurorNumbers)
                .as("Expect a juror_history record to be created for each court deferral used in this newly requested"
                    + " pool")
                .contains(record.getJurorNumber());
            assertThat(record.getPoolNumber())
                .as("Expect a juror_history record to be created with the same Pool Number as the newly requested pool")
                .isEqualTo(jurorHistMap.get("PoolNumber"));
            assertThat(record.getHistoryCode().getCode())
                .as("Expect a juror_history record to be created with History Code of 'PDEF'")
                .isEqualTo(jurorHistMap.get("HistoryCode"));
            assertThat(record.getOtherInformation())
                .as("Expect a juror_history record to be created with the hard coded description 'Added to New Pool'")
                .isEqualTo(jurorHistMap.get("Info"));
            assertThat(record.getCreatedBy())
                .as("Expect a juror_history record to be created with the current user's login as the User ID")
                .isEqualTo(jurorHistMap.get("UserId"));
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_duplicatePoolRequests.sql"})
    public void test_requestNewPoolFromBureau_duplicatePool() {
        PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        HttpEntity<PoolRequestDto> entity = new HttpEntity<>(poolRequestDto, httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
            URI.create("/api/v1/moj/pool-request/new-pool"),
            HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode())
            .as("Pool number and owner combination already exists and should fail validation")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql("/db/RequestPoolController_initInvalidTests.sql")
    public void test_requestNewPoolFromBureau_invalidPoolNumber_tooLong() {
        PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        poolRequestDto.setPoolNumber("1234567890");
        HttpEntity<PoolRequestDto> entity = new HttpEntity<>(poolRequestDto, httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
            URI.create("/api/v1/moj/pool-request/new-pool"),
            HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode())
            .as("Pool number exceeds the maximum length and should fail validation")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql("/db/RequestPoolController_initInvalidTests.sql")
    public void test_requestNewPoolFromBureau_invalidPoolNumber_null() {
        PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        poolRequestDto.setPoolNumber(null);
        HttpEntity<PoolRequestDto> entity = new HttpEntity<>(poolRequestDto, httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
            URI.create("/api/v1/moj/pool-request/new-pool"),
            HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode())
            .as("Pool number is mandatory and should not be null")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql("/db/RequestPoolController_initInvalidTests.sql")
    public void test_requestNewPoolFromBureau_invalidPoolNumber_notNumeric() {
        PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        poolRequestDto.setPoolNumber("ABCDEFGHI");
        HttpEntity<PoolRequestDto> entity = new HttpEntity<>(poolRequestDto, httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
            URI.create("/api/v1/moj/pool-request/new-pool"),
            HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode())
            .as("Pool number should contain numeric characters only")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql("/db/RequestPoolController_initInvalidTests.sql")
    public void test_requestNewPoolFromBureau_invalidCourtLocation_doesNotExist() {
        PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        poolRequestDto.setLocationCode("012");
        HttpEntity<PoolRequestDto> entity = new HttpEntity<>(poolRequestDto, httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
            URI.create("/api/v1/moj/pool-request/new-pool"),
            HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode())
            .as("Court Location code does not exist in the database")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql("/db/mod/truncate.sql")
    public void test_requestNewPoolFromBureau_invalidCourtLocation_tooShort() {
        PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        poolRequestDto.setLocationCode("12");
        HttpEntity<PoolRequestDto> entity = new HttpEntity<>(poolRequestDto, httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
            URI.create("/api/v1/moj/pool-request/new-pool"),
            HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode())
            .as("Court Location must be a minimum of 3 digits")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql("/db/RequestPoolController_initInvalidTests.sql")
    public void test_requestNewPoolFromBureau_invalidCourtLocation_tooLong() {
        PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        poolRequestDto.setLocationCode("1234");
        HttpEntity<PoolRequestDto> entity = new HttpEntity<>(poolRequestDto, httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
            URI.create("/api/v1/moj/pool-request/new-pool"),
            HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode())
            .as("Court Location must be a maximum of 3 digits")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql("/db/RequestPoolController_initInvalidTests.sql")
    public void test_requestNewPoolFromBureau_invalidCourtLocation_null() {
        PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        poolRequestDto.setLocationCode(null);
        HttpEntity<PoolRequestDto> entity = new HttpEntity<>(poolRequestDto, httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
            URI.create("/api/v1/moj/pool-request/new-pool"),
            HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode())
            .as("Court Location is mandatory and should not be null")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql("/db/RequestPoolController_initInvalidTests.sql")
    public void test_requestNewPoolFromBureau_invalidAttendanceDate_null() {
        PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        poolRequestDto.setAttendanceDate(null);
        HttpEntity<PoolRequestDto> entity = new HttpEntity<>(poolRequestDto, httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
            URI.create("/api/v1/moj/pool-request/new-pool"),
            HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode())
            .as("Attendance Date is mandatory and should not be null")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql("/db/RequestPoolController_initInvalidTests.sql")
    public void test_requestNewPoolFromBureau_invalidNoRequested_underMin() {
        PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        poolRequestDto.setNumberRequested(-1);
        HttpEntity<PoolRequestDto> entity = new HttpEntity<>(poolRequestDto, httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
            URI.create("/api/v1/moj/pool-request/new-pool"),
            HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode())
            .as("Number Requested must be a positive value between 0 and 3000 (inclusive)")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql("/db/RequestPoolController_initInvalidTests.sql")
    public void test_requestNewPoolFromBureau_invalidNoRequested_overMax() {
        PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        poolRequestDto.setNumberRequested(3001);
        HttpEntity<PoolRequestDto> entity = new HttpEntity<>(poolRequestDto, httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
            URI.create("/api/v1/moj/pool-request/new-pool"),
            HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode())
            .as("Number Requested must be a positive value between 0 and 3000 (inclusive)")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql("/db/RequestPoolController_initInvalidTests.sql")
    public void test_requestNewPoolFromBureau_invalidPoolType_doesNotExist() {
        PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        poolRequestDto.setPoolType("123");
        HttpEntity<PoolRequestDto> entity = new HttpEntity<>(poolRequestDto, httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
            URI.create("/api/v1/moj/pool-request/new-pool"),
            HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode())
            .as("Pool Type does not exist in the database")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql("/db/mod/truncate.sql")
    public void test_requestNewPoolFromBureau_invalidPoolType_tooLong() {
        PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        poolRequestDto.setPoolType("1234");
        HttpEntity<PoolRequestDto> entity = new HttpEntity<>(poolRequestDto, httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
            URI.create("/api/v1/moj/pool-request/new-pool"),
            HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode())
            .as("Pool Type must be exactly 3 characters in length")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql("/db/RequestPoolController_initInvalidTests.sql")
    public void test_requestNewPoolFromBureau_invalidPoolType_tooShort() {
        PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        poolRequestDto.setPoolType("12");
        HttpEntity<PoolRequestDto> entity = new HttpEntity<>(poolRequestDto, httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
            URI.create("/api/v1/moj/pool-request/new-pool"),
            HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode())
            .as("Pool Type must be exactly 3 characters in length")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql("/db/RequestPoolController_initInvalidTests.sql")
    public void test_requestNewPoolFromBureau_invalidDeferralsUsed_belowMin() {
        PoolRequestDto poolRequestDto = createValidPoolRequestDto();
        poolRequestDto.setDeferralsUsed(-1);
        HttpEntity<PoolRequestDto> entity = new HttpEntity<>(poolRequestDto, httpHeaders);

        ResponseEntity<String> response = restTemplate.exchange(
            URI.create("/api/v1/moj/pool-request/new-pool"),
            HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode())
            .as("Deferrals used must be a positive integer greater than or equal to 0")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_initDeferrals.sql"})
    public void test_courtDeferrals_multipleDeferralsExist() {
        String locationCode = "415";
        String attendanceDate = "2022-10-03";
        ResponseEntity<Integer> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/pool-request/deferrals?locationCode=" + locationCode
                    + "&deferredTo=" + attendanceDate)),
            Integer.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);
        assertThat(response.getBody())
            .as("Expect three valid court deferrals for the correct court location and date")
            .isEqualTo(3);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_initDeferrals.sql"})
    public void test_courtDeferrals_oneDeferralsExists() {
        String locationCode = "416";
        String attendanceDate = "2022-10-03";
        ResponseEntity<Integer> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/pool-request/deferrals?locationCode=" + locationCode
                    + "&deferredTo=" + attendanceDate)),
            Integer.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);
        assertThat(response.getBody())
            .as("Expect only one valid court deferrals for the court location and date provided")
            .isEqualTo(1);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_initDeferrals.sql"})
    public void test_courtDeferrals_noDeferralsExists() {
        String locationCode = "416";
        String attendanceDate = "2022-10-04";
        ResponseEntity<Integer> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/pool-request/deferrals?locationCode=" + locationCode
                    + "&deferredTo=" + attendanceDate)),
            Integer.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);
        assertThat(response.getBody())
            .as("Expect no valid court deferrals for the court location and date provided")
            .isEqualTo(0);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_initDeferrals.sql"})
    public void test_courtDeferrals_invalidDateFormat() {
        String locationCode = "415";
        String attendanceDate = "12/10/2022";
        ResponseEntity<String> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/pool-request/deferrals?locationCode=" + locationCode
                    + "&deferredTo=" + attendanceDate)),
            String.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be unsuccessful and return a Http Status of 400 (bad request)")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_initHolidays.sql"})
    public void test_checkDate_businessDay() {
        String locationCode = "415";
        String attendanceDate = "2022-10-03";
        ResponseEntity<DayType> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/pool-request/day-type?locationCode=" + locationCode
                    + "&attendanceDate=" + attendanceDate)),
            DayType.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);
        assertThat(response.getBody())
            .as("Expect the requested attendance date to be a valid business day")
            .isEqualTo(DayType.BUSINESS_DAY);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_initHolidays.sql"})
    public void test_checkDate_weekend() {
        String locationCode = "415";
        String attendanceDate = "2022-10-02";
        ResponseEntity<DayType> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/pool-request/day-type?locationCode=" + locationCode
                    + "&attendanceDate=" + attendanceDate)),
            DayType.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);
        assertThat(response.getBody())
            .as("Expect the requested attendance date to be on a Sunday (Weekend)")
            .isEqualTo(DayType.WEEKEND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_initHolidays.sql"})
    public void test_checkDate_holiday() {
        String locationCode = "416";
        String attendanceDate = "2022-10-03";
        ResponseEntity<DayType> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/pool-request/day-type?locationCode=" + locationCode
                    + "&attendanceDate=" + attendanceDate)),
            DayType.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);
        assertThat(response.getBody())
            .as("Expect the requested attendance date to match a holiday record")
            .isEqualTo(DayType.HOLIDAY);
    }

    // TODO write test to check id is generated - entityleveltesting

    @Test
    public void test_checkDate_invalidDateFormat() {
        String locationCode = "416";
        String attendanceDate = "2022/10/03";
        ResponseEntity<String> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/pool-request/day-type?locationCode=" + locationCode
                    + "&attendanceDate=" + attendanceDate)),
            String.class);

        assertThat(response.getStatusCode())
            .as("Expect to get a bad response due to date format")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_checkDate_courtLocationDoesNotExist() {
        String locationCode = "012";
        String attendanceDate = "2022-10-03";
        ResponseEntity<String> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/pool-request/day-type?locationCode=" + locationCode
                    + "&attendanceDate=" + attendanceDate)),
            String.class);

        assertThat(response.getStatusCode())
            .as("Expect to get a bad response due to court location")
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void test_checkDate_courtLocationTooShort() {
        String locationCode = "41";
        String attendanceDate = "2022-10-03";
        ResponseEntity<String> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/pool-request/day-type?locationCode=" + locationCode
                    + "&attendanceDate=" + attendanceDate)),
            String.class);

        assertThat(response.getStatusCode())
            .as("Expect to get a bad response due to court location")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql("/db/mod/truncate.sql")
    public void test_generatePoolNumber_firstSequence() {
        String locationCode = "415";
        String attendanceDate = "2022-10-03";
        ResponseEntity<String> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/pool-request/generate-pool-number?locationCode=" + locationCode
                    + "&attendanceDate=" + attendanceDate)),
            String.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);
        assertThat(response.getBody())
            .as("Expect the generated Pool Number to have the default sequence start suffix (01)")
            .isEqualTo("415221001");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_generatePoolNumbers.sql"})
    public void test_generatePoolNumber_nextSequence() {
        String locationCode = "415";
        String attendanceDate = "2022-10-31";
        ResponseEntity<String> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/pool-request/generate-pool-number?locationCode=" + locationCode
                    + "&attendanceDate=" + attendanceDate)),
            String.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);
        assertThat(response.getBody())
            .as("Expect the generated Pool Number to increment the sequence number of the highest current "
                + "sequence number by 1")
            .isEqualTo("415221002");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_generatePoolNumbers.sql"})
    public void test_generatePoolNumber_invalidSequence() {
        String locationCode = "457";
        String attendanceDate = "2022-06-27";
        ResponseEntity<String> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/pool-request/generate-pool-number?locationCode=" + locationCode
                    + "&attendanceDate=" + attendanceDate)),
            String.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);
        assertThat(response.getBody())
            .as("Expect an error - no Pool Number to be generated")
            .isNull();
    }

    @Test
    public void test_generatePoolNumber_invalidDateFormat() {
        String locationCode = "415";
        String attendanceDate = "2022/10/03";
        ResponseEntity<String> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create("/api/v1/moj/pool-request/generate-pool-number?locationCode=" + locationCode
                    + "&attendanceDate=" + attendanceDate)),
            String.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_initPoolRequests.sql"})
    public void test_getPoolNumbers() {
        String poolNumberPrefix = "4152201";
        ResponseEntity<PoolNumbersListDto> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders,
                HttpMethod.GET,
                URI.create("/api/v1/moj/pool-request/pool-numbers?poolNumberPrefix=" + poolNumberPrefix)),
            PoolNumbersListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);

        PoolNumbersListDto responseBody = response.getBody();

        assertThat(responseBody)
            .as("Expect the body to not be null")
            .isNotNull();
        assertThat(responseBody.getData().size())
            .as("Expect the pool numbers list to have 3 entries")
            .isEqualTo(3);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_initPoolRequests.sql"})
    public void test_getPoolNumbers_noPoolsExist() {
        String poolNumberPrefix = "4142301";
        ResponseEntity<PoolNumbersListDto> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders,
                HttpMethod.GET,
                URI.create("/api/v1/moj/pool-request/pool-numbers?poolNumberPrefix=" + poolNumberPrefix)),
            PoolNumbersListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);

        PoolNumbersListDto responseBody = response.getBody();

        assertThat(responseBody)
            .as("Expect the body to not be null")
            .isNotNull();
        assertThat(responseBody.getData().size())
            .as("Expect the pool numbers list to have 0 entries")
            .isEqualTo(0);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_activePoolsBureau.sql"})
    public void test_getActivePools_AtBureauByBureauUser() {

        String tab = "bureau";
        int offset = 0;
        String sortBy = "poolNumber";
        String sortOrder = "asc";

        String requestUrl = "/api/v1/moj/pool-request/pools-active?tab=" + tab
            + "&offset=" + offset + "&sortBy=" + sortBy + "&sortOrder=" + sortOrder;

        ResponseEntity<PoolRequestActiveListDto> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create(requestUrl)), PoolRequestActiveListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);

        PoolRequestActiveListDto responseBody = response.getBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getTotalSize())
            .as("Expect the active pools list to have 29 entries in total")
            .isEqualTo(29);

        List<PoolRequestActiveListDto.PoolRequestActiveDataDto> poolRequestActiveDataDtos = responseBody.getData();
        assertThat(poolRequestActiveDataDtos.size())
            .as("Expect the active pools list page to have 25 entries in total")
            .isEqualTo(25);

        // get first element in the page
        PoolRequestActiveListDto.PoolRequestActiveDataDto poolRequestActiveDataDto = poolRequestActiveDataDtos.get(0);

        assertThat(poolRequestActiveDataDto.getPoolNumber())
            .as("Expect the number to be equal to 415221201")
            .isEqualTo("415221201");
        assertThat(poolRequestActiveDataDto.getCourtName())
            .as("Expect the court name to be equal to CHESTER")
            .isEqualTo("CHESTER");
        assertThat(poolRequestActiveDataDto.getConfirmedFromBureau())
            .as("Expect the confirmed jurors to be equal to 2")
            .isEqualTo(2);
        assertThat(poolRequestActiveDataDto.getRequestedFromBureau())
            .as("Expect the requested jurors to be equal to 1")
            .isEqualTo(50);
        assertThat(poolRequestActiveDataDto.getPoolType())
            .as("Expect the court type to be equal to CROWN COURT")
            .isEqualTo("CROWN COURT");
        assertThat(poolRequestActiveDataDto.getAttendanceDate())
            .as("Expect the service start date to be equal 4/12/2022")
            .isEqualTo(LocalDate.of(2022, 12, 4));
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_activePoolsBureau.sql"})
    public void test_getActivePools_AtBureauByBureauUser_secondPage() {

        String tab = "bureau";
        int offset = 1;
        String sortBy = "poolNumber";
        String sortOrder = "asc";

        String requestUrl = "/api/v1/moj/pool-request/pools-active?tab=" + tab + "&offset=" + offset + "&sortBy="
            + sortBy + "&sortOrder=" + sortOrder;

        ResponseEntity<PoolRequestActiveListDto> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create(requestUrl)), PoolRequestActiveListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);

        PoolRequestActiveListDto responseBody = response.getBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getTotalSize())
            .as("Expect the active pools list to have 4 entries in total")
            .isEqualTo(29);

        List<PoolRequestActiveListDto.PoolRequestActiveDataDto> poolRequestActiveDataDtos = responseBody.getData();
        assertThat(poolRequestActiveDataDtos.size())
            .as("Expect the active pools list page to have 4 entries in total")
            .isEqualTo(4);

        // get first element in the page
        PoolRequestActiveListDto.PoolRequestActiveDataDto poolRequestActiveDataDto = poolRequestActiveDataDtos.get(0);

        assertThat(poolRequestActiveDataDto.getPoolNumber())
            .as("Expect the number to be equal to 415221234")
            .isEqualTo("415221234");
        assertThat(poolRequestActiveDataDto.getCourtName())
            .as("Expect the court name to be equal to CHESTER")
            .isEqualTo("CHESTER");
        assertThat(poolRequestActiveDataDto.getConfirmedFromBureau())
            .as("Expect the confirmed jurors to be equal to 0")
            .isEqualTo(0);
        assertThat(poolRequestActiveDataDto.getRequestedFromBureau())
            .as("Expect the requested jurors to be equal to 50")
            .isEqualTo(50);
        assertThat(poolRequestActiveDataDto.getPoolType())
            .as("Expect the court type to be equal to CROWN COURT")
            .isEqualTo("CROWN COURT");
        assertThat(poolRequestActiveDataDto.getAttendanceDate())
            .as("Expect the service start date to be equal 4/12/2022")
            .isEqualTo(LocalDate.of(2022, 12, 4));
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_activePoolsBureau.sql"})
    public void test_getActivePools_AtBureauByBureauUser_descending() {

        String tab = "bureau";
        int offset = 0;
        String sortBy = "poolNumber";
        String sortOrder = "desc";

        String requestUrl = "/api/v1/moj/pool-request/pools-active?tab=" + tab + "&offset=" + offset
            + "&sortBy=" + sortBy + "&sortOrder=" + sortOrder;

        ResponseEntity<PoolRequestActiveListDto> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create(requestUrl)), PoolRequestActiveListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);

        PoolRequestActiveListDto responseBody = response.getBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getTotalSize())
            .as("Expect the active pools list to have 4 entries in total")
            .isEqualTo(29);

        List<PoolRequestActiveListDto.PoolRequestActiveDataDto> poolRequestActiveDataDtos = responseBody.getData();
        assertThat(poolRequestActiveDataDtos.size())
            .as("Expect the active pools list page to have 25 entries in total")
            .isEqualTo(25);

        // get first element in the page
        PoolRequestActiveListDto.PoolRequestActiveDataDto poolRequestActiveDataDto = poolRequestActiveDataDtos.get(0);

        assertThat(poolRequestActiveDataDto.getPoolNumber())
            .as("Expect the number to be equal to 462221207")
            .isEqualTo("462221207");
        assertThat(poolRequestActiveDataDto.getCourtName())
            .as("Expect the court name to be equal to WARRINGTON")
            .isEqualTo("WARRINGTON");
        assertThat(poolRequestActiveDataDto.getConfirmedFromBureau())
            .as("Expect the confirmed jurors to be equal to 0")
            .isEqualTo(0);
        assertThat(poolRequestActiveDataDto.getRequestedFromBureau())
            .as("Expect the requested jurors to be equal to 1")
            .isEqualTo(50);
        assertThat(poolRequestActiveDataDto.getPoolType())
            .as("Expect the court type to be equal to CROWN COURT")
            .isEqualTo("CROWN COURT");
        assertThat(poolRequestActiveDataDto.getAttendanceDate())
            .as("Expect the service start date to be equal 4/11/2022")
            .isEqualTo(LocalDate.of(2022, 11, 4));
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_activePoolsBureau.sql"})
    public void test_getActivePools_AtBureauByBureauUser_CourtFilter() {

        String tab = "bureau";
        int offset = 0;
        String sortBy = "poolNumber";
        String sortOrder = "asc";
        String locCode = "416";

        String requestUrl = "/api/v1/moj/pool-request/pools-active?tab=" + tab + "&locCode=" + locCode
            + "&offset=" + offset + "&sortBy=" + sortBy + "&sortOrder=" + sortOrder;

        ResponseEntity<PoolRequestActiveListDto> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create(requestUrl)), PoolRequestActiveListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);

        PoolRequestActiveListDto responseBody = response.getBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getTotalSize())
            .as("Expect the active pools list to have 1 entry in total")
            .isEqualTo(1);

        List<PoolRequestActiveListDto.PoolRequestActiveDataDto> poolRequestActiveDataDtos = responseBody.getData();
        assertThat(poolRequestActiveDataDtos.size())
            .as("Expect the active pools list page to have 1 entry in total")
            .isEqualTo(1);

        // get first element in the page
        PoolRequestActiveListDto.PoolRequestActiveDataDto poolRequestActiveDataDto = poolRequestActiveDataDtos.get(0);

        assertThat(poolRequestActiveDataDto.getPoolNumber())
            .as("Expect the number to be equal to 416221203")
            .isEqualTo("416221203");
        assertThat(poolRequestActiveDataDto.getCourtName())
            .as("Expect the court name to be equal to LEWES SITTING AT CHICHESTER")
            .isEqualTo("LEWES SITTING AT CHICHESTER");
        assertThat(poolRequestActiveDataDto.getConfirmedFromBureau())
            .as("Expect the confirmed jurors to be equal to 1")
            .isEqualTo(1);
        assertThat(poolRequestActiveDataDto.getRequestedFromBureau())
            .as("Expect the requested jurors to be equal to 1")
            .isEqualTo(59);
        assertThat(poolRequestActiveDataDto.getPoolType())
            .as("Expect the court type to be equal to CROWN COURT")
            .isEqualTo("CROWN COURT");
        assertThat(poolRequestActiveDataDto.getAttendanceDate())
            .as("Expect the service start date to be equal 4/12/2022")
            .isEqualTo(LocalDate.of(2022, 12, 4));
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_activePoolsBureau.sql"})
    public void test_getActivePools_AtBureauByCourtUser_WithMultipleCourts() throws Exception {

        String tab = "bureau";
        int offset = 0;
        String sortBy = "poolNumber";
        String sortOrder = "asc";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Arrays.asList("415", "462", "774", "767")));

        String requestUrl = "/api/v1/moj/pool-request/pools-active?tab=" + tab + "&offset=" + offset + "&sortBy="
            + sortBy + "&sortOrder=" + sortOrder;

        ResponseEntity<PoolRequestActiveListDto> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create(requestUrl)), PoolRequestActiveListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);

        PoolRequestActiveListDto responseBody = response.getBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getTotalSize())
            .as("Expect the active pools list to have 28 entries in total")
            .isEqualTo(28);

        List<PoolRequestActiveListDto.PoolRequestActiveDataDto> poolRequestActiveDataDtos = responseBody.getData();
        assertThat(poolRequestActiveDataDtos.size())
            .as("Expect the active pools list page to have 25 entries in total")
            .isEqualTo(25);

        // get first element in the page
        PoolRequestActiveListDto.PoolRequestActiveDataDto poolRequestActiveDataDto = poolRequestActiveDataDtos.get(0);

        assertThat(poolRequestActiveDataDto.getPoolNumber())
            .as("Expect the number to be equal to 415221201")
            .isEqualTo("415221201");
        assertThat(poolRequestActiveDataDto.getCourtName())
            .as("Expect the court name to be equal to CHESTER")
            .isEqualTo("CHESTER");
        assertThat(poolRequestActiveDataDto.getConfirmedFromBureau())
            .as("Expect the confirmed jurors to be equal to 2")
            .isEqualTo(2);
        assertThat(poolRequestActiveDataDto.getRequestedFromBureau())
            .as("Expect the requested jurors to be equal to 1")
            .isEqualTo(50);
        assertThat(poolRequestActiveDataDto.getPoolType())
            .as("Expect the court type to be equal to CROWN COURT")
            .isEqualTo("CROWN COURT");
        assertThat(poolRequestActiveDataDto.getAttendanceDate())
            .as("Expect the service start date to be equal 4/12/2022")
            .isEqualTo(LocalDate.of(2022, 12, 4));
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_activePoolsBureau.sql"})
    public void test_getActivePools_AtBureauByCourtUser_WithMultipleCourts_CourtFilter() throws Exception {

        String tab = "bureau";
        int offset = 0;
        String sortBy = "poolNumber";
        String sortOrder = "asc";
        String locCode = "462";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Arrays.asList("415", "462", "774", "767")));

        String requestUrl = "/api/v1/moj/pool-request/pools-active?tab=" + tab + "&locCode=" + locCode + "&offset="
            + offset + "&sortBy=" + sortBy + "&sortOrder=" + sortOrder;

        ResponseEntity<PoolRequestActiveListDto> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create(requestUrl)), PoolRequestActiveListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);

        PoolRequestActiveListDto responseBody = response.getBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getTotalSize())
            .as("Expect the active pools list to have 1 entry in total")
            .isEqualTo(1);

        List<PoolRequestActiveListDto.PoolRequestActiveDataDto> poolRequestActiveDataDtos = responseBody.getData();
        assertThat(poolRequestActiveDataDtos.size())
            .as("Expect the active pools list page to have 1 entry in total")
            .isEqualTo(1);

        // get first element in the page
        PoolRequestActiveListDto.PoolRequestActiveDataDto poolRequestActiveDataDto = poolRequestActiveDataDtos.get(0);

        assertThat(poolRequestActiveDataDto.getPoolNumber())
            .as("Expect the number to be equal to 462221207")
            .isEqualTo("462221207");
        assertThat(poolRequestActiveDataDto.getCourtName())
            .as("Expect the court name to be equal to WARRINGTON")
            .isEqualTo("WARRINGTON");
        assertThat(poolRequestActiveDataDto.getConfirmedFromBureau())
            .as("Expect the confirmed jurors to be equal to 0")
            .isEqualTo(0);
        assertThat(poolRequestActiveDataDto.getRequestedFromBureau())
            .as("Expect the requested jurors to be equal to 1")
            .isEqualTo(50);
        assertThat(poolRequestActiveDataDto.getPoolType())
            .as("Expect the court type to be equal to CROWN COURT")
            .isEqualTo("CROWN COURT");
        assertThat(poolRequestActiveDataDto.getAttendanceDate())
            .as("Expect the service start date to be equal 4/11/2022")
            .isEqualTo(LocalDate.of(2022, 11, 4));
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_activePoolsCourt.sql"})
    public void test_getActivePools_AtCourtByBureauUser() {

        String tab = "court";
        int offset = 0;
        String sortBy = "poolNumber";
        String sortOrder = "asc";

        String requestUrl = "/api/v1/moj/pool-request/pools-active?tab=" + tab + "&offset=" + offset
            + "&sortBy=" + sortBy + "&sortOrder=" + sortOrder;

        ResponseEntity<PoolRequestActiveListDto> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create(requestUrl)), PoolRequestActiveListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);

        PoolRequestActiveListDto responseBody = response.getBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getTotalSize())
            .as("Expect the active pools list to have 28 entries in total")
            .isEqualTo(28);

        List<PoolRequestActiveListDto.PoolRequestActiveDataDto> poolRequestActiveDataDtos = responseBody.getData();
        assertThat(poolRequestActiveDataDtos.size())
            .as("Expect the active pools list page to have 25 entries in total")
            .isEqualTo(25);

        // get first element in the page
        PoolRequestActiveListDto.PoolRequestActiveDataDto poolRequestActiveDataDto = poolRequestActiveDataDtos.get(0);

        assertThat(poolRequestActiveDataDto.getPoolNumber())
            .as("Expect the number to be equal to 415221306")
            .isEqualTo("415221306");
        assertThat(poolRequestActiveDataDto.getCourtName())
            .as("Expect the court name to be equal to CHESTER")
            .isEqualTo("CHESTER");
        assertThat(poolRequestActiveDataDto.getPoolCapacity())
            .as("Expect the pool capacity to be equal to 60")
            .isEqualTo(60);
        assertThat(poolRequestActiveDataDto.getJurorsInPool())
            .as("Expect the jurors in pool to be equal to 9")
            .isEqualTo(9);
        assertThat(poolRequestActiveDataDto.getPoolType())
            .as("Expect the court type to be equal to CROWN COURT")
            .isEqualTo("CROWN COURT");
        assertThat(poolRequestActiveDataDto.getAttendanceDate())
            .as("Expect the service start date to be equal today's date minus 10 days")
            .isEqualTo(LocalDate.now().minusDays(10));
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_activePoolsCourt.sql"})
    public void test_getActivePools_AtCourtByBureauUser_secondPage() {

        String tab = "court";
        int offset = 1;
        String sortBy = "poolNumber";
        String sortOrder = "asc";

        String requestUrl = "/api/v1/moj/pool-request/pools-active?tab=" + tab + "&offset=" + offset
            + "&sortBy=" + sortBy + "&sortOrder=" + sortOrder;

        ResponseEntity<PoolRequestActiveListDto> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create(requestUrl)), PoolRequestActiveListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);

        PoolRequestActiveListDto responseBody = response.getBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getTotalSize())
            .as("Expect the active pools list to have 28 entries in total")
            .isEqualTo(28);

        List<PoolRequestActiveListDto.PoolRequestActiveDataDto> poolRequestActiveDataDtos = responseBody.getData();
        assertThat(poolRequestActiveDataDtos.size())
            .as("Expect the active pools list page to have 3 entries in total")
            .isEqualTo(3);

        // get first element in the page
        PoolRequestActiveListDto.PoolRequestActiveDataDto poolRequestActiveDataDto = poolRequestActiveDataDtos.get(0);

        assertThat(poolRequestActiveDataDto.getPoolNumber())
            .as("Expect the number to be equal to 415221331")
            .isEqualTo("415221331");
        assertThat(poolRequestActiveDataDto.getCourtName())
            .as("Expect the court name to be equal to CHESTER")
            .isEqualTo("CHESTER");
        assertThat(poolRequestActiveDataDto.getPoolCapacity())
            .as("Expect the pool capacity to be equal to 0")
            .isEqualTo(0);
        assertThat(poolRequestActiveDataDto.getJurorsInPool())
            .as("Expect the jurors in pool to be equal to 0")
            .isEqualTo(0);
        assertThat(poolRequestActiveDataDto.getPoolType())
            .as("Expect the court type to be equal to CROWN COURT")
            .isEqualTo("CROWN COURT");
        assertThat(poolRequestActiveDataDto.getAttendanceDate())
            .as("Expect the service start date to be equal today's date plus 1 week")
            .isEqualTo(LocalDate.now().plusWeeks(1));
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_activePoolsCourt.sql"})
    public void test_getActivePools_AtCourtByBureauUser_CourtFilter() {

        String tab = "court";
        int offset = 0;
        String sortBy = "poolNumber";
        String sortOrder = "asc";
        String locCode = "415";

        String requestUrl = "/api/v1/moj/pool-request/pools-active?tab=" + tab + "&locCode=" + locCode
            + "&offset=" + offset + "&sortBy=" + sortBy + "&sortOrder=" + sortOrder;

        ResponseEntity<PoolRequestActiveListDto> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create(requestUrl)), PoolRequestActiveListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);

        PoolRequestActiveListDto responseBody = response.getBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getTotalSize())
            .as("Expect the active pools list to have 26 entries in total")
            .isEqualTo(26);

        List<PoolRequestActiveListDto.PoolRequestActiveDataDto> poolRequestActiveDataDtos = responseBody.getData();
        assertThat(poolRequestActiveDataDtos.size())
            .as("Expect the active pools list page to have 25 entries in total")
            .isEqualTo(25);

        // get first element in the page
        PoolRequestActiveListDto.PoolRequestActiveDataDto poolRequestActiveDataDto = poolRequestActiveDataDtos.get(0);

        assertThat(poolRequestActiveDataDto.getPoolNumber())
            .as("Expect the number to be equal to 415221306")
            .isEqualTo("415221306");
        assertThat(poolRequestActiveDataDto.getCourtName())
            .as("Expect the court name to be equal to CHESTER")
            .isEqualTo("CHESTER");
        assertThat(poolRequestActiveDataDto.getPoolCapacity())
            .as("Expect the pool capacity to be equal to 60")
            .isEqualTo(60);
        assertThat(poolRequestActiveDataDto.getJurorsInPool())
            .as("Expect the jurors in pool to be equal to 9")
            .isEqualTo(9);
        assertThat(poolRequestActiveDataDto.getPoolType())
            .as("Expect the court type to be equal to CROWN COURT")
            .isEqualTo("CROWN COURT");
        assertThat(poolRequestActiveDataDto.getAttendanceDate())
            .as("Expect the service start date to be equal today's date minus 10 days")
            .isEqualTo(LocalDate.now().minusDays(10));
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_activePoolsCourt.sql"})
    public void test_getActivePools_AtCourtByCourtUser_MultipleCourts() throws Exception {

        String tab = "court";
        int offset = 0;
        String sortBy = "poolNumber";
        String sortOrder = "asc";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Arrays.asList("415", "462", "774", "767")));

        String requestUrl = "/api/v1/moj/pool-request/pools-active?tab=" + tab + "&offset=" + offset
            + "&sortBy=" + sortBy + "&sortOrder=" + sortOrder;

        ResponseEntity<PoolRequestActiveListDto> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create(requestUrl)), PoolRequestActiveListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);

        PoolRequestActiveListDto responseBody = response.getBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getTotalSize())
            .as("Expect the active pools list to have 27 entries in total")
            .isEqualTo(27);

        List<PoolRequestActiveListDto.PoolRequestActiveDataDto> poolRequestActiveDataDtos = responseBody.getData();
        assertThat(poolRequestActiveDataDtos.size())
            .as("Expect the active pools list page to have 25 entries in total")
            .isEqualTo(25);

        // get first element in the page
        PoolRequestActiveListDto.PoolRequestActiveDataDto poolRequestActiveDataDto = poolRequestActiveDataDtos.get(0);

        assertThat(poolRequestActiveDataDto.getPoolNumber())
            .as("Expect the number to be equal to 415221306")
            .isEqualTo("415221306");
        assertThat(poolRequestActiveDataDto.getCourtName())
            .as("Expect the court name to be equal to CHESTER")
            .isEqualTo("CHESTER");
        assertThat(poolRequestActiveDataDto.getPoolCapacity())
            .as("Expect the pool capacity to be equal to 60")
            .isEqualTo(60);
        assertThat(poolRequestActiveDataDto.getJurorsInPool())
            .as("Expect the jurors in pool to be equal to 9")
            .isEqualTo(9);
        assertThat(poolRequestActiveDataDto.getPoolType())
            .as("Expect the court type to be equal to CROWN COURT")
            .isEqualTo("CROWN COURT");
        assertThat(poolRequestActiveDataDto.getAttendanceDate())
            .as("Expect the service start date to be equal today's date minus 10 days")
            .isEqualTo(LocalDate.now().minusDays(10));
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_activePoolsCourt.sql"})
    public void test_getActivePools_AtCourtByCourtUser_MultipleCourts_SecondPage() throws Exception {

        String tab = "court";
        int offset = 1;
        String sortBy = "poolNumber";
        String sortOrder = "asc";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Arrays.asList("415", "462", "774", "767")));

        String requestUrl = "/api/v1/moj/pool-request/pools-active?tab=" + tab + "&offset=" + offset
            + "&sortBy=" + sortBy + "&sortOrder=" + sortOrder;

        ResponseEntity<PoolRequestActiveListDto> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create(requestUrl)), PoolRequestActiveListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);

        PoolRequestActiveListDto responseBody = response.getBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getTotalSize())
            .as("Expect the active pools list to have 27 entries in total")
            .isEqualTo(27);

        List<PoolRequestActiveListDto.PoolRequestActiveDataDto> poolRequestActiveDataDtos = responseBody.getData();
        assertThat(poolRequestActiveDataDtos.size())
            .as("Expect the active pools list page to have 2 entries in total")
            .isEqualTo(2);

        // get first element in the page
        PoolRequestActiveListDto.PoolRequestActiveDataDto poolRequestActiveDataDto = poolRequestActiveDataDtos.get(0);

        assertThat(poolRequestActiveDataDto.getPoolNumber())
            .as("Expect the number to be equal to 415221331")
            .isEqualTo("415221331");
        assertThat(poolRequestActiveDataDto.getCourtName())
            .as("Expect the court name to be equal to CHESTER")
            .isEqualTo("CHESTER");
        assertThat(poolRequestActiveDataDto.getPoolCapacity())
            .as("Expect the pool capacity to be equal to 0")
            .isEqualTo(0);
        assertThat(poolRequestActiveDataDto.getJurorsInPool())
            .as("Expect the jurors in pool to be equal to 0")
            .isEqualTo(0);
        assertThat(poolRequestActiveDataDto.getPoolType())
            .as("Expect the court type to be equal to CROWN COURT")
            .isEqualTo("CROWN COURT");
        assertThat(poolRequestActiveDataDto.getAttendanceDate())
            .as("Expect the service start date to be equal today's date plus 1 week")
            .isEqualTo(LocalDate.now().plusWeeks(1));
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_activePoolsCourt.sql"})
    public void test_getActivePools_AtCourtByCourtUser_MultipleCourts_desc() throws Exception {

        String tab = "court";
        int offset = 0;
        String sortBy = "poolNumber";
        String sortOrder = "desc";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", Arrays.asList("415", "462", "774", "767")));

        String requestUrl = "/api/v1/moj/pool-request/pools-active?tab=" + tab + "&offset=" + offset
            + "&sortBy=" + sortBy + "&sortOrder=" + sortOrder;

        ResponseEntity<PoolRequestActiveListDto> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create(requestUrl)), PoolRequestActiveListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);

        PoolRequestActiveListDto responseBody = response.getBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getTotalSize())
            .as("Expect the active pools list to have 27 entries in total")
            .isEqualTo(27);

        List<PoolRequestActiveListDto.PoolRequestActiveDataDto> poolRequestActiveDataDtos = responseBody.getData();
        assertThat(poolRequestActiveDataDtos.size())
            .as("Expect the active pools list page to have 25 entries in total")
            .isEqualTo(25);

        // get first element in the page
        PoolRequestActiveListDto.PoolRequestActiveDataDto poolRequestActiveDataDto = poolRequestActiveDataDtos.get(0);

        assertThat(poolRequestActiveDataDto.getPoolNumber())
            .as("Expect the number to be equal to 767221206")
            .isEqualTo("767221206");
        assertThat(poolRequestActiveDataDto.getCourtName())
            .as("Expect the court name to be equal to KNUTSFORD")
            .isEqualTo("KNUTSFORD");
        assertThat(poolRequestActiveDataDto.getPoolCapacity())
            .as("Expect the pool capacity to be equal to 60")
            .isEqualTo(60);
        assertThat(poolRequestActiveDataDto.getJurorsInPool())
            .as("Expect the jurors in pool to be equal to 2")
            .isEqualTo(2);
        assertThat(poolRequestActiveDataDto.getPoolType())
            .as("Expect the court type to be equal to CROWN COURT")
            .isEqualTo("CROWN COURT");
        assertThat(poolRequestActiveDataDto.getAttendanceDate())
            .as("Expect the service start date to be equal today's date minus 10 days")
            .isEqualTo(LocalDate.now().minusDays(10));
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_activePoolsCourt.sql"})
    public void test_getActivePools_AtCourtByCourtUser_OneCourt() throws Exception {

        String tab = "court";
        int offset = 0;
        String sortBy = "poolNumber";
        String sortOrder = "asc";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", List.of("416")));

        String requestUrl = "/api/v1/moj/pool-request/pools-active?tab=" + tab + "&offset=" + offset
            + "&sortBy=" + sortBy + "&sortOrder=" + sortOrder;

        ResponseEntity<PoolRequestActiveListDto> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create(requestUrl)), PoolRequestActiveListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);

        PoolRequestActiveListDto responseBody = response.getBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getTotalSize())
            .as("Expect the active pools list to have 2 entry in total")
            .isEqualTo(2);

        List<PoolRequestActiveListDto.PoolRequestActiveDataDto> poolRequestActiveDataDtos = responseBody.getData();
        assertThat(poolRequestActiveDataDtos.size())
            .as("Expect the active pools list page to have entry in total")
            .isEqualTo(2);

        // get first element in the page
        PoolRequestActiveListDto.PoolRequestActiveDataDto poolRequestActiveDataDto = poolRequestActiveDataDtos.get(0);

        assertThat(poolRequestActiveDataDto.getPoolNumber())
            .as("Expect the number to be equal to 416221336")
            .isEqualTo("416221336");
        assertThat(poolRequestActiveDataDto.getCourtName())
            .as("Expect the court name to be equal to LEWES SITTING AT CHICHESTER")
            .isEqualTo("LEWES SITTING AT CHICHESTER");
        assertThat(poolRequestActiveDataDto.getPoolCapacity())
            .as("Expect the pool capacity to be equal to 60")
            .isEqualTo(50);
        assertThat(poolRequestActiveDataDto.getJurorsInPool())
            .as("Expect the jurors in pool to be equal to 1")
            .isEqualTo(1);
        assertThat(poolRequestActiveDataDto.getPoolType())
            .as("Expect the court type to be equal to CROWN COURT")
            .isEqualTo("CROWN COURT");
        assertThat(poolRequestActiveDataDto.getAttendanceDate())
            .as("Expect the service start date to be equal today's date minus 9 days")
            .isEqualTo(LocalDate.now().minusDays(9));
    }

    @Test
    @Sql("/db/mod/truncate.sql")
    public void test_getActivePools_BureauNoResult() {

        String tab = "bureau";
        int offset = 0;
        String sortBy = "poolNumber";
        String sortOrder = "asc";
        String locCode = "415";

        String requestUrl = "/api/v1/moj/pool-request/pools-active?tab=" + tab + "&locCode=" + locCode
            + "&offset=" + offset + "&sortBy=" + sortBy + "&sortOrder=" + sortOrder;

        ResponseEntity<PoolRequestActiveListDto> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create(requestUrl)), PoolRequestActiveListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);

        PoolRequestActiveListDto responseBody = response.getBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getTotalSize())
            .as("Expect the active pools list to have no entries")
            .isEqualTo(0);
    }

    @Test
    @Sql("/db/mod/truncate.sql")
    public void test_getActivePools_CourtNoResult() throws Exception {

        String tab = "bureau";
        int offset = 0;
        String sortBy = "poolNumber";
        String sortOrder = "asc";
        String locCode = "415";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415",
            Arrays.asList("415", "462", "774", "767")));

        String requestUrl = "/api/v1/moj/pool-request/pools-active?tab=" + tab + "&locCode=" + locCode
            + "&offset=" + offset + "&sortBy=" + sortBy + "&sortOrder=" + sortOrder;

        ResponseEntity<PoolRequestActiveListDto> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create(requestUrl)), PoolRequestActiveListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);

        PoolRequestActiveListDto responseBody = response.getBody();

        assertThat(responseBody).isNotNull();
        assertThat(responseBody.getTotalSize())
            .as("Expect the active pools list to have no entries")
            .isEqualTo(0);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_poolsAtCourtLocation.sql"})
    public void testGetPoolsAtCourtLocationByCourtUserSingle() throws Exception {

        String locCode = "417";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("417", List.of("417")));

        String requestUrl = "/api/v1/moj/pool-request/pools-at-court?locCode=" + locCode;

        ResponseEntity<PoolsAtCourtLocationListDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders,
                HttpMethod.GET, URI.create(requestUrl)), PoolsAtCourtLocationListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);

        PoolsAtCourtLocationListDto poolsAtCourtLocationListDto = response.getBody();
        assertThat(poolsAtCourtLocationListDto).isNotNull();
        List<PoolsAtCourtLocationListDto.PoolsAtCourtLocationDataDto> poolsAtCourtLocationDataDtos
            = poolsAtCourtLocationListDto.getData();
        assertThat(poolsAtCourtLocationDataDtos.size())
            .as("Expect the active pools list to have one entry")
            .isEqualTo(1);

        PoolsAtCourtLocationListDto.PoolsAtCourtLocationDataDto poolsAtCourtLocationDataDto
            = poolsAtCourtLocationDataDtos.get(0);
        assertThat(poolsAtCourtLocationDataDto.getPoolNumber()).as("Expect the pool number to be 417230101")
            .isEqualTo("417230101");
        assertThat(poolsAtCourtLocationDataDto.getJurorsInAttendance())
            .as("Expect there to be 2 jurors in attendance")
            .isEqualTo(2);
        assertThat(poolsAtCourtLocationDataDto.getJurorsOnCall()).as("Expect there to be 1 juror on call")
            .isEqualTo(1);
        assertThat(poolsAtCourtLocationDataDto.getOtherJurors()).as("Expect there to be 2 other jurors")
            .isEqualTo(2);
        assertThat(poolsAtCourtLocationDataDto.getTotalJurors()).as("Expect there to be 5 jurors in total")
            .isEqualTo(5);
        assertThat(poolsAtCourtLocationDataDto.getPoolType()).as("Expect the pool type to be CROWN COURT")
            .isEqualTo("CRO");
        assertThat(poolsAtCourtLocationDataDto.getJurorsOnTrials()).as("Jurors on trial").isEqualTo(7);
        LocalDate serviceStartDate = LocalDate.now().minusDays(10);
        assertThat(poolsAtCourtLocationDataDto.getServiceStartDate()).as("Expect the pool start date to be "
                + serviceStartDate)
            .isEqualTo(serviceStartDate);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_poolsAtCourtLocation.sql"})
    public void testGetPoolsAtCourtLocationByCourtUserMultiple() throws Exception {

        String locCode = "418";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("418", List.of("418")));

        String requestUrl = "/api/v1/moj/pool-request/pools-at-court?locCode=" + locCode;

        ResponseEntity<PoolsAtCourtLocationListDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders,
                HttpMethod.GET, URI.create(requestUrl)), PoolsAtCourtLocationListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);

        PoolsAtCourtLocationListDto poolsAtCourtLocationListDto = response.getBody();
        assertThat(poolsAtCourtLocationListDto).isNotNull();
        List<PoolsAtCourtLocationListDto.PoolsAtCourtLocationDataDto> poolsAtCourtLocationDataDtosUnsorted
            = poolsAtCourtLocationListDto.getData();
        assertThat(poolsAtCourtLocationDataDtosUnsorted.size())
            .as("Expect the active pools list to have three entries")
            .isEqualTo(3);

        // sort the list of pools by pool number or else the order is not guaranteed
        List<PoolsAtCourtLocationListDto.PoolsAtCourtLocationDataDto> poolsAtCourtLocationDataDtos =
            poolsAtCourtLocationDataDtosUnsorted.stream().sorted(Comparator.comparing(PoolsAtCourtLocationListDto
                .PoolsAtCourtLocationDataDto::getPoolNumber)).collect(Collectors.toList());

        PoolsAtCourtLocationListDto.PoolsAtCourtLocationDataDto poolsAtCourtLocationDataDto
            = poolsAtCourtLocationDataDtos.get(0);
        LocalDate serviceStartDate = LocalDate.now().minusDays(10);

        assertThat(poolsAtCourtLocationDataDto.getPoolNumber()).as("Expect the pool number to be 418230101")
            .isEqualTo("418230101");
        validatePoolData(poolsAtCourtLocationDataDto, serviceStartDate);
        assertThat(poolsAtCourtLocationDataDto.getJurorsOnTrials()).as("Expect there to be 0 juror(s) in trials")
            .isEqualTo(0);

        poolsAtCourtLocationDataDto
            = poolsAtCourtLocationDataDtos.get(1);
        assertThat(poolsAtCourtLocationDataDto.getPoolNumber()).as("Expect the pool number to be 418230102")
            .isEqualTo("418230102");
        validatePoolData(poolsAtCourtLocationDataDto, serviceStartDate);
        assertThat(poolsAtCourtLocationDataDto.getJurorsOnTrials()).as("Expect there to be 1 juror(s) in trials")
            .isEqualTo(1);


        poolsAtCourtLocationDataDto
            = poolsAtCourtLocationDataDtos.get(2);
        assertThat(poolsAtCourtLocationDataDto.getPoolNumber()).as("Expect the pool number to be 418230103")
            .isEqualTo("418230103");
        validatePoolData(poolsAtCourtLocationDataDto, serviceStartDate);
        assertThat(poolsAtCourtLocationDataDto.getJurorsOnTrials()).as("Expect there to be 1 juror(s) in trials")
            .isEqualTo(1);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_poolsAtCourtLocation.sql"})
    public void testGetPoolsAtCourtLocationByCourtOneOnCallOnlyNoneInAttendance() throws Exception {

        String locCode = "419";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("419", List.of("419")));
        String requestUrl = "/api/v1/moj/pool-request/pools-at-court?locCode=" + locCode;

        ResponseEntity<PoolsAtCourtLocationListDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders,
                HttpMethod.GET, URI.create(requestUrl)), PoolsAtCourtLocationListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);

        PoolsAtCourtLocationListDto poolsAtCourtLocationListDto = response.getBody();
        assertThat(poolsAtCourtLocationListDto).isNotNull();
        List<PoolsAtCourtLocationListDto.PoolsAtCourtLocationDataDto> poolsAtCourtLocationDataDtosUnsorted
            = poolsAtCourtLocationListDto.getData();
        assertThat(poolsAtCourtLocationDataDtosUnsorted.size())
            .as("Expect the active pools list to have 1 entry")
            .isEqualTo(1);

        PoolsAtCourtLocationListDto.PoolsAtCourtLocationDataDto poolsAtCourtLocationDataDto
            = poolsAtCourtLocationDataDtosUnsorted.get(0);

        assertThat(poolsAtCourtLocationDataDto.getPoolNumber()).as("Expect the pool number to be 419230101")
            .isEqualTo("419230101");

        assertThat(poolsAtCourtLocationDataDto.getJurorsInAttendance())
            .as("Expect there to be 0 juror in attendance")
            .isEqualTo(0);
        assertThat(poolsAtCourtLocationDataDto.getJurorsOnCall()).as("Expect there to be 1 juror on call")
            .isEqualTo(1);

    }

    private static void validatePoolData(
        PoolsAtCourtLocationListDto.PoolsAtCourtLocationDataDto poolsAtCourtLocationDataDto,
        LocalDate serviceStartDate) {
        assertThat(poolsAtCourtLocationDataDto.getJurorsInAttendance())
            .as("Expect there to be 1 juror in attendance")
            .isEqualTo(1);
        assertThat(poolsAtCourtLocationDataDto.getJurorsOnCall()).as("Expect there to be 0 juror on call")
            .isEqualTo(0);
        assertThat(poolsAtCourtLocationDataDto.getOtherJurors()).as("Expect there to be 0 other jurors")
            .isEqualTo(0);
        assertThat(poolsAtCourtLocationDataDto.getPoolType()).as("Expect the pool type to be CROWN COURT")
            .isEqualTo("CRO");
        assertThat(poolsAtCourtLocationDataDto.getServiceStartDate()).as("Expect the pool start date to be "
                + serviceStartDate)
            .isEqualTo(serviceStartDate);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_poolsAtCourtLocation.sql"})
    public void testGetPoolsAtCourtLocationByCourtUserNone() throws Exception {

        String locCode = "420";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("420", List.of("420")));
        String requestUrl = "/api/v1/moj/pool-request/pools-at-court?locCode=" + locCode;

        ResponseEntity<PoolsAtCourtLocationListDto> response =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders,
                HttpMethod.GET, URI.create(requestUrl)), PoolsAtCourtLocationListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(OK);

        PoolsAtCourtLocationListDto poolsAtCourtLocationListDto = response.getBody();
        assertThat(poolsAtCourtLocationListDto).isNotNull();

        assertThat(poolsAtCourtLocationListDto.getData())
            .as("Expect the active pools list to be empty")
            .isEmpty();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_poolsAtCourtLocation.sql"})
    public void testGetPoolsAtCourtLocationByBureauUserForbidden() throws Exception {

        String locCode = "419";
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("400", List.of("419")));
        String requestUrl = "/api/v1/moj/pool-request/pools-at-court?locCode=" + locCode;

        ResponseEntity<PoolsAtCourtLocationListDto> response = restTemplate.exchange(new RequestEntity<Void>(
            httpHeaders, HttpMethod.GET, URI.create(requestUrl)), PoolsAtCourtLocationListDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be forbidden")
            .isEqualTo(FORBIDDEN);
    }
}
