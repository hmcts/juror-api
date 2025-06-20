package uk.gov.hmcts.juror.api.moj.controller;

import org.json.JSONObject;
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
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorManagementRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolEditRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorManagementResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.SummoningProgressResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.poolmanagement.AvailablePoolsInCourtLocationDto;
import uk.gov.hmcts.juror.api.moj.controller.response.poolmanagement.ReassignPoolMembersResultDto;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.HistoryCode;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.QPoolComment;
import uk.gov.hmcts.juror.api.moj.domain.QPoolHistory;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.PoolUtilisationDescription;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolCommentRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;
import uk.gov.hmcts.juror.api.moj.service.poolmanagement.JurorManagementConstants;
import uk.gov.hmcts.juror.api.moj.utils.DateUtils;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static uk.gov.hmcts.juror.api.TestUtil.getValuesInJsonObject;

/**
 * Integration tests for the API endpoints defined in {@link ManagePoolController}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings({
    "PMD.ExcessivePublicCount",
    "PMD.TooManyMethods",
    "PMD.ExcessiveImports",
    "PMD.CyclomaticComplexity"})
public class ManagePoolControllerITest extends AbstractIntegrationTest {
    private static final String URI_AVAILABLE_POOLS = "/api/v1/moj/manage-pool/available-pools/%s";
    private static final String URI_MANAGE_POOL_SUMMARY = "/api/v1/moj/manage-pool/summary?poolNumber=%s";
    private static final String COURT_USER = "COURT_USER";
    private static final String POOL_NUMBER_415221001 = "415221001";
    private static final String EXPECT_HTTP_RESPONSE_SUCCESSFUL = "Expect the HTTP GET request to be successful";
    private static final QPoolComment POOL_COMMENTS = QPoolComment.poolComment;
    public static final String EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR =
        "Expect property to be copied from source juror";
    public static final String EXPECT_PROPERTY_TO_BE_USE_A_DEFAULT_VALUE = "Expect property to be use a default value";
    public static final String POOL_NUMBER_415220401 = "415220401";
    public static final String POOL_NUMBER_416220502 = "416220502";

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private PoolRequestRepository poolRequestRepository;
    @Autowired
    private CourtLocationService courtLocationService;
    @Autowired
    private JurorPoolRepository jurorPoolRepository;
    @Autowired
    private PoolCommentRepository poolCommentRepository;
    @Autowired
    private PoolHistoryRepository poolHistoryRepository;
    @Autowired
    private JurorHistoryRepository jurorHistoryRepository;
    @Autowired
    private CourtLocationRepository courtLocationRepository;
    @Autowired
    private BulkPrintDataRepository bulkPrintDataRepository;
    private HttpHeaders httpHeaders;

    @Before
    public void setUp() throws Exception {
        initHeaders();
    }

    private void initHeaders(Role... roles) {
        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userType(UserType.BUREAU)
            .roles(Set.of(roles))
            .login("BUREAU_USER")
            .owner("400")
            .build());

        httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    private String initCourtsJwt(String owner, List<String> courts) {

        return mintBureauJwt(BureauJwtPayload.builder()
            .userType(UserType.COURT)
            .login(COURT_USER)
            .owner(owner)
            .staff(BureauJwtPayload.Staff.builder().courts(courts).build())
            .build());
    }

    @Test
    @Sql({"/db/mod/truncate.sql",
        "/db/ManagePoolController_initPool.sql",
        "/db/ManagePoolController_initCourtSupply.sql",
        "/db/ManagePoolController_initBureauSupply.sql"})
    @Sql(statements = "UPDATE JUROR_MOD.POOL SET NEW_REQUEST = 'N' WHERE POOL_NO = '415221001' AND OWNER = '400';")
    public void testGPoolStatisticsAllDataPresent() {
        ResponseEntity<PoolSummaryResponseDto> responseEntity =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create(String.format(URI_MANAGE_POOL_SUMMARY, POOL_NUMBER_415221001))),
                PoolSummaryResponseDto.class);

        assertThat(responseEntity.getStatusCode()).as(EXPECT_HTTP_RESPONSE_SUCCESSFUL).isEqualTo(HttpStatus.OK);

        PoolSummaryResponseDto responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();

        PoolSummaryResponseDto.PoolDetails poolDetails = responseBody.getPoolDetails();
        PoolSummaryResponseDto.BureauSummoning bureauSummoning = responseBody.getBureauSummoning();
        PoolSummaryResponseDto.PoolSummary poolSummary = responseBody.getPoolSummary();
        PoolSummaryResponseDto.AdditionalStatistics additionalStatistics = responseBody.getAdditionalStatistics();
        assertThat(poolDetails).isNotNull();
        assertThat(bureauSummoning).isNotNull();
        assertThat(poolSummary).isNotNull();
        assertThat(additionalStatistics).isNotNull();

        assertThat(poolDetails.getPoolNumber())
            .as("Pool number should be mapped from the POOL_NO value in the POOL_REQUEST view")
            .isEqualTo(POOL_NUMBER_415221001);
        assertThat(poolDetails.getCourtLocationCode())
            .as("Court location should be mapped from the COURT_LOCATION associated with the POOL_REQUEST record")
            .isEqualTo("415");
        assertThat(poolDetails.getCourtName())
            .as("Court name should be mapped from the COURT_LOCATION associated with the POOL_REQUEST record")
            .isEqualToIgnoringCase("CHESTER");
        assertThat(poolDetails.getPoolType()).isEqualTo("CROWN COURT");
        assertThat(poolDetails.getCourtStartDate())
            .as("Court start date should be mapped from the RETURN_DATE value in the POOL_REQUEST view")
            .isEqualToIgnoringCase("Monday 03 Oct 2022");
        assertThat(poolDetails.getAdditionalRequirements())
            .as("Additional requirements is only populated if the pool was requested as a special pool")
            .isNull();
        assertThat(poolDetails.getIsActive())
            .as("Is active should be true if any copy of this pool request has a NEW_REQUEST value equal to 'N'")
            .isTrue();
        assertThat(poolDetails.isNilPool())
            .as("Is nil pool should be mapped from the nil_pool value in the POOL_REQUEST view")
            .isFalse();
        assertThat(poolDetails.getCurrentOwner())
            .as("Current owner of the pool should be returned")
            .isEqualTo("400");
        assertThat(bureauSummoning.getTotalSummoned())
            .as("Total summoned should be mapped from the TOTAL_SUMMONED value in the POOL_STATS view and represents "
                + "the total number of bureau owned members in a pool (regardless of status)")
            .isEqualTo(12);
        assertThat(bureauSummoning.getConfirmedFromBureau())
            .as("Confirmed from bureau should be mapped from the AVAILABLE value in the POOL_STATS view and "
                + "represents the total number of bureau owned members in a pool wih a status of 'Responded'")
            .isEqualTo(2);
        assertThat(bureauSummoning.getRequestedFromBureau())
            .as("Requested from bureau should be mapped from the NO_REQUESTED value in the POOL_REQUEST view and "
                + "represents the number of jurors requested from the Bureau (total required - court supply)")
            .isEqualTo(9);
        assertThat(bureauSummoning.getUnavailable())
            .as("Unavailable should be mapped from the UNAVAILABLE value in the POOL_STATS view and represents the "
                + "number of bureau owned members in  a pool with a status that is NOT: 'Responded', 'Summoned', or "
                + "'Awaiting Info'")
            .isEqualTo(3);
        assertThat(bureauSummoning.getUnresolved())
            .as("Unresolved should be mapped from the UNRESOLVED value in the POOL_STATS view and represents the "
                + "number of bureau owned members in  a pool with a status of: 'Summoned' or 'Awaiting Info'")
            .isEqualTo(7);

        assertThat(poolSummary.getCurrentPoolSize())
            .as("Current pool size should be calculated as COURT_SUPPLY plus AVAILABLE ")
            .isEqualTo(3);
        assertThat(poolSummary.getRequiredPoolSize())
            .as("Required pool size should be mapped from TOTAL_NUMBER_REQUIRED value in the POOL table")
            .isEqualTo(10);

        assertThat(additionalStatistics.getCourtSupply())
            .as("Court Supply should be mapped from COURT_SUPPLY value in the POOL_STATS view and represent active, "
                + "court owned members in a pool")
            .isEqualTo(1);
    }

    @Test
    @Sql({"/db/mod/truncate.sql",
        "/db/ManagePoolController_initPool.sql",
        "/db/ManagePoolController_initCourtSupply.sql"})
    public void testGPoolStatisticsCourtSupplyOnly() {
        ResponseEntity<PoolSummaryResponseDto> responseEntity =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create(String.format(URI_MANAGE_POOL_SUMMARY, 415_221_001))), PoolSummaryResponseDto.class);

        assertThat(responseEntity.getStatusCode()).as(EXPECT_HTTP_RESPONSE_SUCCESSFUL).isEqualTo(HttpStatus.OK);

        PoolSummaryResponseDto responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();

        PoolSummaryResponseDto.PoolDetails poolDetails = responseBody.getPoolDetails();
        PoolSummaryResponseDto.BureauSummoning bureauSummoning = responseBody.getBureauSummoning();
        PoolSummaryResponseDto.PoolSummary poolSummary = responseBody.getPoolSummary();
        PoolSummaryResponseDto.AdditionalStatistics additionalStatistics = responseBody.getAdditionalStatistics();
        assertThat(poolDetails).isNotNull();
        assertThat(bureauSummoning).isNotNull();
        assertThat(poolSummary).isNotNull();
        assertThat(additionalStatistics).isNotNull();

        assertThat(poolDetails.getPoolNumber())
            .as("Pool number should be mapped from the POOL_NO value in the POOL_REQUEST view")
            .isEqualTo(POOL_NUMBER_415221001);
        assertThat(poolDetails.getCourtLocationCode())
            .as("Court location should be mapped from the COURT_LOCATION associated with the POOL_REQUEST record")
            .isEqualTo("415");
        assertThat(poolDetails.getCourtName())
            .as("Court name should be mapped from the COURT_LOCATION associated with the POOL_REQUEST record")
            .isEqualToIgnoringCase("CHESTER");
        assertThat(poolDetails.getPoolType()).isEqualTo("CROWN COURT");
        assertThat(poolDetails.getCourtStartDate())
            .as("Court start date should be mapped from the RETURN_DATE value in the POOL_REQUEST view")
            .isEqualToIgnoringCase("Monday 03 Oct 2022");
        assertThat(poolDetails.getAdditionalRequirements())
            .as("Additional requirements is only populated if the pool was requested as a special pool")
            .isNull();
        assertThat(poolDetails.getIsActive())
            .as("Is active should be false if no copies of this request has a NEW_REQUEST value equal to 'N'")
            .isFalse();
        assertThat(poolDetails.isNilPool())
            .as("Is nil pool should be mapped from the nil_pool value in the POOL_REQUEST view")
            .isFalse();
        assertThat(poolDetails.getCurrentOwner())
            .as("Current owner of the pool should be returned")
            .isEqualTo("400");
        assertThat(bureauSummoning.getTotalSummoned())
            .as("Total summoned should be mapped from the TOTAL_SUMMONED value in the POOL_STATS view and represents "
                + "the total number of bureau owned members in a pool (regardless of status)")
            .isEqualTo(0);
        assertThat(bureauSummoning.getConfirmedFromBureau())
            .as("Confirmed from bureau should be mapped from the AVAILABLE value in the POOL_STATS view and "
                + "represents the total number of bureau owned members in a pool wih a status of 'Responded'")
            .isEqualTo(0);
        assertThat(bureauSummoning.getRequestedFromBureau())
            .as("Requested from bureau should be mapped from the NO_REQUESTED value in the POOL_REQUEST view and "
                + "represents the number of jurors requested from the Bureau (total required - court supply)")
            .isEqualTo(9);
        assertThat(bureauSummoning.getUnavailable())
            .as("Unavailable should be mapped from the UNAVAILABLE value in the POOL_STATS view and represents the "
                + "number of bureau owned members in  a pool with a status that is NOT: 'Responded', 'Summoned', or "
                + "'Awaiting Info'")
            .isEqualTo(0);
        assertThat(bureauSummoning.getUnresolved())
            .as("Unresolved should be mapped from the UNRESOLVED value in the POOL_STATS view and represents the "
                + "number of bureau owned members in  a pool with a status of: 'Summoned' or 'Awaiting Info'")
            .isEqualTo(0);

        assertThat(poolSummary.getCurrentPoolSize())
            .as("Current pool size should be calculated as COURT_SUPPLY plus AVAILABLE ")
            .isEqualTo(1);
        assertThat(poolSummary.getRequiredPoolSize())
            .as("Required pool size should be mapped from TOTAL_NUMBER_REQUIRED value in the POOL_REQUEST_EXT table")
            .isEqualTo(10);

        assertThat(additionalStatistics.getCourtSupply())
            .as("Court Supply should be mapped from COURT_SUPPLY value in the POOL_STATS view and represent active, "
                + "court owned members in a pool")
            .isEqualTo(1);
    }

    /**
     * When the new Pool Summary changes first go-live, there will be existing Pools which do not have values stored
     * in the total_no_required column - this has accepted on the basis that there will only be an initial 8-9 week
     * period where these Pools will exist, after go-live, all future Pools will be created with an initialised
     * total_no_required value.
     */
    @Test
    @Sql({"/db/mod/truncate.sql",
        "/db/ManagePoolController_initPool.sql",
        "/db/ManagePoolController_initCourtSupply.sql",
        "/db/ManagePoolController_initBureauSupply.sql"})
    @Sql(statements = "UPDATE juror_mod.pool SET total_no_required = 0 WHERE pool_no = '415221001' AND owner = '400';")
    public void testGPoolStatisticsNoPoolRequestExt() {
        ResponseEntity<PoolSummaryResponseDto> responseEntity =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create(String.format(URI_MANAGE_POOL_SUMMARY, POOL_NUMBER_415221001))),
                PoolSummaryResponseDto.class);

        assertThat(responseEntity.getStatusCode()).as(EXPECT_HTTP_RESPONSE_SUCCESSFUL).isEqualTo(HttpStatus.OK);

        PoolSummaryResponseDto responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();

        PoolSummaryResponseDto.PoolDetails poolDetails = responseBody.getPoolDetails();
        PoolSummaryResponseDto.BureauSummoning bureauSummoning = responseBody.getBureauSummoning();
        PoolSummaryResponseDto.PoolSummary poolSummary = responseBody.getPoolSummary();
        PoolSummaryResponseDto.AdditionalStatistics additionalStatistics = responseBody.getAdditionalStatistics();
        assertThat(poolDetails).isNotNull();
        assertThat(bureauSummoning).isNotNull();
        assertThat(poolSummary).isNotNull();
        assertThat(additionalStatistics).isNotNull();

        assertThat(poolDetails.getPoolNumber())
            .as("Pool number should be mapped from the POOL_NO value in the POOL_REQUEST view")
            .isEqualTo(POOL_NUMBER_415221001);
        assertThat(poolDetails.getCourtLocationCode())
            .as("Court location should be mapped from the COURT_LOCATION associated with the POOL_REQUEST record")
            .isEqualTo("415");
        assertThat(poolDetails.getCourtName())
            .as("Court name should be mapped from the COURT_LOCATION associated with the POOL_REQUEST record")
            .isEqualToIgnoringCase("CHESTER");
        assertThat(poolDetails.getCourtStartDate())
            .as("Court start date should be mapped from the RETURN_DATE value in the POOL_REQUEST view")
            .isEqualToIgnoringCase("Monday 03 Oct 2022");
        assertThat(poolDetails.getAdditionalRequirements())
            .as("Additional requirements is only populated if the pool was requested as a special pool")
            .isNull();
        assertThat(poolDetails.getIsActive())
            .as("Is active should be false if no copies of this request has a NEW_REQUEST value equal to 'N'")
            .isFalse();
        assertThat(poolDetails.isNilPool())
            .as("Is nil pool should be mapped from the nil_pool value in the POOL_REQUEST view")
            .isFalse();
        assertThat(poolDetails.getCurrentOwner())
            .as("Current owner of the pool should be returned")
            .isEqualTo("400");
        assertThat(bureauSummoning.getTotalSummoned())
            .as("Total summoned should be mapped from the TOTAL_SUMMONED value"
                + " in the POOL_STATS view and represents "
                + "the total number of bureau owned members in a pool (regardless of status)")
            .isEqualTo(12);
        assertThat(bureauSummoning.getConfirmedFromBureau())
            .as("Confirmed from bureau should be mapped from the AVAILABLE value in the POOL_STATS view and "
                + "represents the total number of bureau owned members in a pool wih a status of 'Responded'")
            .isEqualTo(2);
        assertThat(bureauSummoning.getRequestedFromBureau())
            .as("Requested from bureau should be mapped from the NO_REQUESTED value in the POOL_REQUEST view and "
                + "represents the number of jurors requested from the Bureau (total required - court supply)")
            .isEqualTo(9);
        assertThat(bureauSummoning.getUnavailable())
            .as("Unavailable should be mapped from the UNAVAILABLE value in the POOL_STATS view and represents the "
                + "number of bureau owned members in  a pool with a status that is NOT: 'Responded', 'Summoned', or "
                + "'Awaiting Info'")
            .isEqualTo(3);
        assertThat(bureauSummoning.getUnresolved())
            .as("Unresolved should be mapped from the UNRESOLVED value in the POOL_STATS view and represents the "
                + "number of bureau owned members in  a pool with a status of: 'Summoned' or 'Awaiting Info'")
            .isEqualTo(7);

        assertThat(poolSummary.getCurrentPoolSize())
            .as("Current pool size should be calculated as COURT_SUPPLY plus AVAILABLE ")
            .isEqualTo(3);
        assertThat(poolSummary.getRequiredPoolSize())
            .as("Required pool size should be mapped from TOTAL_NUMBER_REQUIRED value in the POOL_REQUEST_EXT table")
            .isEqualTo(0);

        assertThat(additionalStatistics.getCourtSupply())
            .as("Court Supply should be mapped from COURT_SUPPLY value in the POOL_STATS view and represent active, "
                + "court owned members in a pool")
            .isEqualTo(1);
    }

    @Test
    public void testDeletePoolPoolNotFound() throws Exception {
        initHeaders(Role.MANAGER);

        String poolNumber = "415220101";

        ResponseEntity<?> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.DELETE,
            URI.create("/api/v1/moj/manage-pool/delete?poolNumber=" + poolNumber)), Object.class);

        assertThat(response.getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testDeletePoolInsufficientPermission() {
        initHeaders(Role.SENIOR_JUROR_OFFICER);
        String poolNumber = "415220101";

        ResponseEntity<?> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.DELETE,
            URI.create("/api/v1/moj/manage-pool/delete?poolNumber=" + poolNumber)), Object.class);

        assertThat(response.getStatusCode())
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolRequests.sql"})
    public void testDeletePoolPoolIsLocked() throws Exception {
        initHeaders(Role.MANAGER);
        String poolNumber = "457220607";

        Optional<PoolRequest> poolRequest = poolRequestRepository.findByPoolNumber(poolNumber);

        assertThat(poolRequest.isPresent())
            .isTrue();

        courtLocationService.getVotersLock(poolRequest.get().getCourtLocation().getLocCode());

        ResponseEntity<?> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.DELETE,
            URI.create("/api/v1/moj/manage-pool/delete?poolNumber=" + poolNumber)), Object.class);

        assertThat(response.getStatusCode())
            .isEqualTo(HttpStatus.LOCKED);

        courtLocationService.releaseVotersLock(poolRequest.get().getCourtLocation().getLocCode());
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolRequests.sql"})
    public void testDeletePoolCourtUserAndActivePool() throws Exception {
        initHeaders(Role.MANAGER);

        List<String> courts = new ArrayList<>();
        courts.add("415");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", courts));

        String poolNumber = "415220110";

        ResponseEntity<?> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.DELETE,
            URI.create("/api/v1/moj/manage-pool/delete?poolNumber=" + poolNumber)), Object.class);

        assertThat(response.getStatusCode())
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_deletePoolRequest.sql"})
    public void testDeletePool() throws Exception {
        initHeaders(Role.MANAGER);
        String poolNumber = "415220110";

        Optional<PoolRequest> poolRequestExists = poolRequestRepository.findByPoolNumber(poolNumber);
        assertThat(poolRequestExists).isPresent();

        ResponseEntity<?> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.DELETE,
            URI.create("/api/v1/moj/manage-pool/delete?poolNumber=" + poolNumber)), Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        Optional<PoolRequest> poolRequestDeleted = poolRequestRepository.findByPoolNumber(poolNumber);
        List<JurorPool> jurorPoolDeleted = jurorPoolRepository.findByPoolPoolNumberAndIsActive(poolNumber, false);

        assertThat(poolRequestDeleted)
            .as("Pool request should be deleted, expect a null value inside the Optional container")
            .isEmpty();
        assertThat(jurorPoolDeleted)
            .as("Inactive juror should be deleted along with it's corresponding pool request, expect list "
                + "to be empty")
            .isEmpty();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/EditPoolController_createPool.sql"})
    public void testEditPoolBureauUser() throws Exception {
        initHeaders(Role.MANAGER);
        String poolNumber = "415221201";

        //create a Pool request edit DTO
        PoolEditRequestDto poolEditRequestDto = getPoolEditRequestDto(poolNumber);

        RequestEntity<?> requestEntity = new RequestEntity<>(poolEditRequestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/edit-pool"));
        ResponseEntity<?> response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // check the values have changed as per request DTO
        Optional<PoolRequest> poolRequestEdited = poolRequestRepository.findByPoolNumber(poolNumber);
        assertThat(poolRequestEdited).isPresent();

        PoolRequest poolRequest = poolRequestEdited.get();
        assertThat(poolRequest.getNumberRequested()).as("Expect the Number Requested to be updated to 55")
            .isEqualTo(55);

        assertThat(poolCommentRepository.count(POOL_COMMENTS.pool.poolNumber.eq(poolNumber)))
            .as("Expect a record to be added to POOL_COMMENTS table")
            .isEqualTo(1);

        assertThat(poolHistoryRepository.count(QPoolHistory.poolHistory.historyCode.eq(HistoryCode.PREQ)))
            .as("Expect a record to be added to POOL_HISTORY with a History code of PREQ")
            .isEqualTo(1);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/EditPoolController_createPool.sql"})
    public void testEditPoolBureauUserNewRequest() throws Exception {
        initHeaders(Role.MANAGER);
        String poolNumber = "415221202";

        //create a Pool request edit DTO
        PoolEditRequestDto poolEditRequestDto = getPoolEditRequestDto(poolNumber);

        RequestEntity<?> requestEntity = new RequestEntity<>(poolEditRequestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/edit-pool"));
        ResponseEntity<?> response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // check the values have changed as per request DTO

        Optional<PoolRequest> poolRequestEdited = poolRequestRepository.findByPoolNumber(poolNumber);
        assertThat(poolRequestEdited).isPresent();
        assertThat(poolRequestEdited.get().getNewRequest()).isEqualTo('Y');

        PoolRequest poolRequest = poolRequestEdited.get();
        assertThat(poolRequest.getNumberRequested()).as("Expect the Number Requested to be updated to 55")
            .isEqualTo(55);

        assertThat(poolCommentRepository.count(POOL_COMMENTS.pool.poolNumber.eq(poolNumber)))
            .as("Expect a record to be added to POOL_COMMENTS table")
            .isEqualTo(1);

        assertThat(poolHistoryRepository.count(QPoolHistory.poolHistory.historyCode.eq(HistoryCode.PREQ)))
            .as("Expect a record to be added to POOL_HISTORY with a History code of PREQ")
            .isEqualTo(1);

    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/EditPoolController_createPool.sql"})
    public void testEditPoolNoRequestedSameBureauUser() throws Exception {
        initHeaders(Role.MANAGER);
        String poolNumber = "415221201";

        //create a Pool request edit DTO
        PoolEditRequestDto poolEditRequestDto = getPoolEditRequestDto(poolNumber);
        // keep number requested at current value
        poolEditRequestDto.setNoRequested(50);

        RequestEntity<?> requestEntity = new RequestEntity<>(poolEditRequestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/edit-pool"));
        ResponseEntity<?> response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // check the values have changed as per request DTO
        Optional<PoolRequest> poolRequestEdited = poolRequestRepository.findByPoolNumber(poolNumber);
        assertThat(poolRequestEdited).isPresent();

        PoolRequest poolRequest = poolRequestEdited.get();
        assertThat(poolRequest.getNumberRequested()).as("Expect the Number Requested to stay at 50")
            .isEqualTo(50);

        assertThat(poolCommentRepository.count(POOL_COMMENTS.pool.poolNumber.eq(poolNumber)))
            .as("Expect a record to be added to POOL_COMMENTS table")
            .isEqualTo(1);

        assertThat(poolHistoryRepository.count(QPoolHistory.poolHistory.historyCode.eq(HistoryCode.PREQ)))
            .as("Expect no record to be added to POOL_HISTORY")
            .isEqualTo(0);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/EditPoolController_createPool.sql"})
    public void testEditPoolAbovePoolCapacityBureauUser() throws Exception {
        initHeaders(Role.MANAGER);
        String poolNumber = "415221201";

        //create a Pool request edit DTO
        PoolEditRequestDto poolEditRequestDto = getPoolEditRequestDto(poolNumber);
        poolEditRequestDto.setNoRequested(100); // set this to be above the pool capacity

        RequestEntity<?> requestEntity = new RequestEntity<>(poolEditRequestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/edit-pool"));
        ResponseEntity<?> response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode()).as("Expect the response to be a bad request")
            .isEqualTo(HttpStatus.BAD_REQUEST);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/EditPoolController_createPool.sql"})
    public void testEditPoolCourtUser() throws Exception {
        initHeaders(Role.MANAGER);
        String poolNumber = "415221201";

        List<String> courts = new ArrayList<>();
        courts.add("415");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", courts));

        //create a Pool request edit DTO
        PoolEditRequestDto poolEditRequestDto = getPoolEditRequestDto(poolNumber);
        poolEditRequestDto.setNoRequested(null);
        poolEditRequestDto.setTotalRequired(70);

        RequestEntity<?> requestEntity = new RequestEntity<>(poolEditRequestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/edit-pool"));
        ResponseEntity<?> response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // check the values have changed as per request DTO
        Optional<PoolRequest> poolRequestOpt = poolRequestRepository.findById(poolNumber);
        assertThat(poolRequestOpt).isPresent();

        PoolRequest poolReq = poolRequestOpt.get();
        assertThat(poolReq.getTotalNoRequired()).as("Expect the Total Number Required to be updated to 70")
            .isEqualTo(70);

        assertThat(poolCommentRepository.count(POOL_COMMENTS.pool.poolNumber.eq(poolNumber)))
            .as("Expect a record to be added to POOL_COMMENTS table")
            .isEqualTo(1);

        assertThat(poolHistoryRepository.count(QPoolHistory.poolHistory.historyCode.eq(HistoryCode.PREQ)))
            .as("Expect a record to be added to POOL_HISTORY with a History code of PREQ")
            .isEqualTo(1);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/EditPoolController_createPool.sql"})
    public void testEditPoolTotalRequiredSameCourtUser() throws Exception {
        initHeaders(Role.MANAGER);
        String poolNumber = "415221201";

        List<String> courts = new ArrayList<>();
        courts.add("415");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", courts));

        //create a Pool request edit DTO
        PoolEditRequestDto poolEditRequestDto = getPoolEditRequestDto(poolNumber);
        poolEditRequestDto.setTotalRequired(60);

        RequestEntity<?> requestEntity = new RequestEntity<>(poolEditRequestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/edit-pool"));
        ResponseEntity<?> response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // check the values have changed as per request DTO
        Optional<PoolRequest> poolRequestOpt = poolRequestRepository.findById(poolNumber);
        assertThat(poolRequestOpt).isPresent();

        PoolRequest poolRequest = poolRequestOpt.get();
        assertThat(poolRequest.getTotalNoRequired()).as("Expect the Total Number Required to stay at 60")
            .isEqualTo(60);

        assertThat(poolCommentRepository.count(POOL_COMMENTS.pool.poolNumber.eq(poolNumber)))
            .as("Expect a record to be added to POOL_COMMENTS table")
            .isEqualTo(1);

        assertThat(poolHistoryRepository.count(QPoolHistory.poolHistory.historyCode.eq(HistoryCode.PREQ)))
            .as("Expect no record to be added to POOL_HISTORY")
            .isEqualTo(0);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/EditPoolController_createPool.sql"})
    public void testEditPoolTotalRequiredLowerThanRequestedCourtUser() throws Exception {
        initHeaders(Role.MANAGER);
        String poolNumber = "415221201";

        List<String> courts = new ArrayList<>();
        courts.add("415");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", courts));

        //create a Pool request edit DTO
        PoolEditRequestDto poolEditRequestDto = getPoolEditRequestDto(poolNumber);
        // setting total required lower than number requested from Bureau
        poolEditRequestDto.setTotalRequired(49);

        RequestEntity<?> requestEntity = new RequestEntity<>(poolEditRequestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/edit-pool"));
        ResponseEntity<?> response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testValidateJurorsForTransferHappyPath() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "415230701";
        String sourceCourtLocation = "415";
        String targetCourtLocation = "457";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333", "444444444");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(sourceCourtLocation,
            Collections.singletonList(sourceCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/movement/validation"));
        ResponseEntity<JurorManagementResponseDto> response = restTemplate.exchange(requestEntity,
            JurorManagementResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        JurorManagementResponseDto responseDto = response.getBody();
        assertThat(responseDto).isNotNull();

        assertThat(responseDto.getAvailableForMove().size())
            .as("Expect a single juror in the list to be available for transfer (passes validation)")
            .isEqualTo(1);
        assertThat(responseDto.getAvailableForMove().stream().allMatch("111111111"::equals)).isTrue();

        List<JurorManagementResponseDto.ValidationFailure> validationFailuresList =
            responseDto.getUnavailableForMove();

        JurorManagementResponseDto.ValidationFailure validationFailure1 = validationFailuresList.stream()
            .filter(failure -> "222222222".equals(failure.getJurorNumber()))
            .findFirst().orElse(null);
        assertThat(validationFailure1).isNotNull();
        assertThat(validationFailure1.getFailureReason())
            .isEqualToIgnoringCase(JurorManagementConstants.ABOVE_AGE_LIMIT_MESSAGE);
        assertThat(validationFailure1.getFirstName()).isEqualToIgnoringCase("FNAMETWOS");
        assertThat(validationFailure1.getLastName()).isEqualToIgnoringCase("LNAMETWOS");

        JurorManagementResponseDto.ValidationFailure validationFailure2 = validationFailuresList.stream()
            .filter(failure -> "333333333".equals(failure.getJurorNumber()))
            .findFirst().orElse(null);
        assertThat(validationFailure2).isNotNull();
        assertThat(validationFailure2.getFailureReason())
            .isEqualToIgnoringCase(String.format(JurorManagementConstants.INVALID_STATUS_MESSAGE, "Transferred"));
        assertThat(validationFailure2.getFirstName()).isEqualToIgnoringCase("FNAMETHREES");
        assertThat(validationFailure2.getLastName()).isEqualToIgnoringCase("LNAMETHREES");

        JurorManagementResponseDto.ValidationFailure validationFailure3 = validationFailuresList.stream()
            .filter(failure -> "444444444".equals(failure.getJurorNumber()))
            .findFirst().orElse(null);
        assertThat(validationFailure3).isNotNull();
        assertThat(validationFailure3.getFailureReason())
            .isEqualToIgnoringCase(JurorManagementConstants.NO_ACTIVE_RECORD_MESSAGE);
        assertThat(validationFailure3.getFirstName()).isEqualToIgnoringCase("");
        assertThat(validationFailure3.getLastName()).isEqualToIgnoringCase("");

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testValidateJurorsForTransferInvalidCourtLocation() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "415230701";
        String sourceCourtLocation = "012";
        String targetCourtLocation = "457";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333", "444444444");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(targetCourtLocation,
            Collections.singletonList(targetCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/movement/validation"));
        ResponseEntity<JurorManagementResponseDto> response = restTemplate.exchange(requestEntity,
            JurorManagementResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testValidateJurorsForTransferBadRequestInvalidPoolNumberTooShort() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "41523070";
        String sourceCourtLocation = "415";
        String targetCourtLocation = "457";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333", "444444444");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(targetCourtLocation,
            Collections.singletonList(targetCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/movement/validation"));
        ResponseEntity<JurorManagementResponseDto> response = restTemplate.exchange(requestEntity,
            JurorManagementResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testValidateJurorsForTransferBadRequestInvalidPoolNumberTooLong() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "4152307011";
        String sourceCourtLocation = "415";
        String targetCourtLocation = "457";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333", "444444444");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(targetCourtLocation,
            Collections.singletonList(targetCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/movement/validation"));
        ResponseEntity<JurorManagementResponseDto> response = restTemplate.exchange(requestEntity,
            JurorManagementResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testValidateJurorsForTransferBadRequestInvalidPoolNumberAlphaNumeric() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "4152307O1";
        String sourceCourtLocation = "415";
        String targetCourtLocation = "457";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333", "444444444");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(targetCourtLocation,
            Collections.singletonList(targetCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/movement/validation"));
        ResponseEntity<JurorManagementResponseDto> response = restTemplate.exchange(requestEntity,
            JurorManagementResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testValidateJurorsForTransferBadRequestInvalidSourceCourtLocationTooShort() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "415230701";
        String sourceCourtLocation = "41";
        String targetCourtLocation = "457";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333", "444444444");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(targetCourtLocation,
            Collections.singletonList(targetCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/movement/validation"));
        ResponseEntity<JurorManagementResponseDto> response = restTemplate.exchange(requestEntity,
            JurorManagementResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testValidateJurorsForTransferBadRequestInvalidSourceCourtLocationTooLong() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "415230701";
        String sourceCourtLocation = "4155";
        String targetCourtLocation = "457";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333", "444444444");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(targetCourtLocation,
            Collections.singletonList(targetCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/movement/validation"));
        ResponseEntity<JurorManagementResponseDto> response = restTemplate.exchange(requestEntity,
            JurorManagementResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testValidateJurorsForTransferBadRequestInvalidSourceCourtLocationAlphaNumeric() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "415230701";
        String sourceCourtLocation = "A15";
        String targetCourtLocation = "457";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333", "444444444");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(targetCourtLocation,
            Collections.singletonList(targetCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/movement/validation"));
        ResponseEntity<JurorManagementResponseDto> response = restTemplate.exchange(requestEntity,
            JurorManagementResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testValidateJurorsForTransferBadRequestInvalidTargetCourtLocationTooShort() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "415230701";
        String sourceCourtLocation = "415";
        String targetCourtLocation = "45";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333", "444444444");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(targetCourtLocation,
            Collections.singletonList(targetCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/movement/validation"));
        ResponseEntity<JurorManagementResponseDto> response = restTemplate.exchange(requestEntity,
            JurorManagementResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testValidateJurorsForTransferBadRequestInvalidTargetCourtLocationTooLong() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "415230701";
        String sourceCourtLocation = "415";
        String targetCourtLocation = "4577";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333", "444444444");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(targetCourtLocation,
            Collections.singletonList(targetCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/movement/validation"));
        ResponseEntity<JurorManagementResponseDto> response = restTemplate.exchange(requestEntity,
            JurorManagementResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testValidateJurorsForTransferBadRequestInvalidTargetCourtLocationAlphaNumeric() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "415230701";
        String sourceCourtLocation = "415";
        String targetCourtLocation = "A57";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333", "444444444");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(targetCourtLocation,
            Collections.singletonList(targetCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/movement/validation"));
        ResponseEntity<JurorManagementResponseDto> response = restTemplate.exchange(requestEntity,
            JurorManagementResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testValidateJurorsForTransferBadRequestInvalidJurorNumbersList() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "415230701";
        String sourceCourtLocation = "415";
        String targetCourtLocation = "457";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = new ArrayList<>();

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(targetCourtLocation,
            Collections.singletonList(targetCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/movement/validation"));
        ResponseEntity<JurorManagementResponseDto> response = restTemplate.exchange(requestEntity,
            JurorManagementResponseDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testTransferJurorsToNewCourtOneJurorFirstTransferHappyPath() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "415230701";
        String sourceCourtLocation = "415";
        String targetCourtLocation = "457";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(sourceCourtLocation,
            Collections.singletonList(sourceCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/transfer"));
        ResponseEntity<Integer> response = restTemplate.exchange(requestEntity, Integer.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        Integer responseDto = response.getBody();
        assertThat(responseDto).isNotNull();

        assertThat(responseDto)
            .as("Expect a single juror to have been transferred successfully")
            .isEqualTo(1);

        transferJurorPoolValidateNewlyCreatedPoolRequest(sourcePoolNumber, "457230702",
            targetCourtLocation, targetServiceStartDate);

        transferJurorPoolValidateSourceJurorPool("111111111", sourcePoolNumber);

        transferJurorPoolValidateNewlyCreatedJurorPool("111111111", sourcePoolNumber,
            "457230702", targetCourtLocation, targetServiceStartDate, COURT_USER);

        // verify no letters have been queued for bulk print
        List<BulkPrintData> bulkPrintData = bulkPrintDataRepository.findAll();
        assertThat(bulkPrintData.size()).isEqualTo(0);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testTransferJurorsToNewCourtMultipleJurorsSomeFail() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "415230701";
        String sourceCourtLocation = "415";
        String targetCourtLocation = "457";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Arrays.asList("111111111", "222222222", "333333333", "444444444", "555555555");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(sourceCourtLocation,
            Collections.singletonList(sourceCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/transfer"));
        ResponseEntity<Integer> response = restTemplate.exchange(requestEntity, Integer.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        Integer responseDto = response.getBody();

        assertThat(responseDto)
            .as("Expect two jurors to have been transferred successfully")
            .isEqualTo(2);

        transferJurorPoolValidateNewlyCreatedPoolRequest(sourcePoolNumber, "457230702",
            targetCourtLocation, targetServiceStartDate);

        for (String jurorNumber : Arrays.asList("111111111", "555555555")) {
            transferJurorPoolValidateSourceJurorPool(jurorNumber, sourcePoolNumber);

            transferJurorPoolValidateNewlyCreatedJurorPool(jurorNumber, sourcePoolNumber,
                "457230702", targetCourtLocation, targetServiceStartDate, COURT_USER);
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initTransferBackPoolMember.sql"})
    public void testTransferJurorsToNewCourtOneJurorTransferBackHappyPath() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "457230702";
        String sourceCourtLocation = "457";
        String targetCourtLocation = "415";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 24);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(sourceCourtLocation,
            Collections.singletonList(sourceCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/transfer"));
        ResponseEntity<Integer> response = restTemplate.exchange(requestEntity, Integer.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        Integer responseDto = response.getBody();

        assertThat(responseDto)
            .as("Expect a single juror to have been transferred successfully")
            .isEqualTo(1);

        transferJurorPoolValidateNewlyCreatedPoolRequest(sourcePoolNumber, "415230702",
            targetCourtLocation, targetServiceStartDate);

        transferJurorPoolValidateSourceJurorPool("111111111", sourcePoolNumber);

        transferJurorPoolValidateNewlyCreatedJurorPool("111111111", sourcePoolNumber,
            "415230702", targetCourtLocation, targetServiceStartDate, COURT_USER);

        transferJurorPoolValidateExistingTransferredJurorPool("111111111", "415230701");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testTransferJurorsToNewCourt_unhappyPathInvalidAccessBureauUser() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "415230701";
        String sourceCourtLocation = "415";
        String targetCourtLocation = "457";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/transfer"));
        assertThat(restTemplate.exchange(requestEntity, String.class).getStatusCode())
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testTransferJurorsToNewCourtUnhappyPathInvalidAccessCourtUser() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "415230701";
        String sourceCourtLocation = "415";
        String targetCourtLocation = "457";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(targetCourtLocation,
            Collections.singletonList(sourceCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/transfer"));
        assertThat(restTemplate.exchange(requestEntity, String.class).getStatusCode())
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testTransferJurorsToNewCourtUnhappyPathInvalidJurorNumberTooShort() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "415230701";
        String sourceCourtLocation = "415";
        String targetCourtLocation = "457";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("11111111");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(sourceCourtLocation,
            Collections.singletonList(sourceCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/transfer"));
        assertThat(restTemplate.exchange(requestEntity, String.class).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testTransferJurorsToNewCourtUnhappyPathInvalidJurorNumberTooLong() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "415230701";
        String sourceCourtLocation = "415";
        String targetCourtLocation = "457";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("1111111111");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(sourceCourtLocation,
            Collections.singletonList(sourceCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/transfer"));
        assertThat(restTemplate.exchange(requestEntity, String.class).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testTransferJurorsToNewCourtUnhappyPathInvalidJurorNumberAlphanumeric() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "415230701";
        String sourceCourtLocation = "415";
        String targetCourtLocation = "457";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("11111111l");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(sourceCourtLocation,
            Collections.singletonList(sourceCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/transfer"));
        assertThat(restTemplate.exchange(requestEntity, String.class).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testTransferJurorsToNewCourtUnhappyPath_sourcePoolNotFound() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "415230799";
        String sourceCourtLocation = "415";
        String targetCourtLocation = "457";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(sourceCourtLocation,
            Collections.singletonList(sourceCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/transfer"));
        assertThat(restTemplate.exchange(requestEntity, String.class).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testTransferJurorsToNewCourtUnhappyPathInvalidSourceCourt() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "415230701";
        String sourceCourtLocation = "415";
        String targetCourtLocation = "457";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(sourceCourtLocation,
            Collections.singletonList(sourceCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            "012", targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/transfer"));
        assertThat(restTemplate.exchange(requestEntity, String.class).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testTransferJurorsToNewCourtUnhappyPathInvalidTargetCourt() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "415230701";
        String sourceCourtLocation = "415";
        String targetCourtLocation = "012";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(sourceCourtLocation,
            Collections.singletonList(sourceCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/transfer"));
        assertThat(restTemplate.exchange(requestEntity, String.class).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testTransferJurorsToNewCourtUnhappyPathInvalidRequest_jurorListEmpty() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "415230701";
        String sourceCourtLocation = "415";
        String targetCourtLocation = "457";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = new ArrayList<>();

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(sourceCourtLocation,
            Collections.singletonList(sourceCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/transfer"));
        assertThat(restTemplate.exchange(requestEntity, String.class).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testTransferJurorsToNewCourtUnhappyPathInvalidRequest_sourcePoolNumber() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "41523070";
        String sourceCourtLocation = "415";
        String targetCourtLocation = "457";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(sourceCourtLocation,
            Collections.singletonList(sourceCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            sourceCourtLocation, targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/transfer"));
        assertThat(restTemplate.exchange(requestEntity, String.class).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testTransferJurorsToNewCourtUnhappyPathInvalidRequest_sourceCourtLocationCode() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "41523070";
        String sourceCourtLocation = "415";
        String targetCourtLocation = "457";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(sourceCourtLocation,
            Collections.singletonList(sourceCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            "4145", targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/transfer"));
        assertThat(restTemplate.exchange(requestEntity, String.class).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void testTransferJurorsToNewCourtUnhappyPathInvalidRequestTargetCourtLocationCode() throws Exception {
        initHeaders(Role.MANAGER);
        String sourcePoolNumber = "41523070";
        String sourceCourtLocation = "415";
        String targetCourtLocation = "4x7";
        LocalDate targetServiceStartDate = LocalDate.of(2023, 7, 17);
        List<String> jurorNumbers = Collections.singletonList("111111111");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(sourceCourtLocation,
            Collections.singletonList(sourceCourtLocation)));

        JurorManagementRequestDto requestDto = new JurorManagementRequestDto(sourcePoolNumber,
            "4145", targetCourtLocation, targetServiceStartDate, jurorNumbers);

        RequestEntity<JurorManagementRequestDto> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/transfer"));
        assertThat(restTemplate.exchange(requestEntity, String.class).getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    //Tests related to controller operation: getAvailablePoolsInCourtLocation
    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initAvailablePools_courtUser.sql"})
    public void availablePoolsInCourtLocationCourtUserHappy() {
        final URI uri = URI.create(String.format(URI_AVAILABLE_POOLS, "416"));
        httpHeaders = initialiseHeaders(COURT_USER, UserType.COURT, null, "416");

        ResponseEntity<AvailablePoolsInCourtLocationDto> responseEntity =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET, uri),
                AvailablePoolsInCourtLocationDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<AvailablePoolsInCourtLocationDto.AvailablePoolsDto> availablePoolsList =
            responseEntity.getBody().getAvailablePools();

        // this endpoint should return 4 available pools as it does not filter by court owner
        assertThat(availablePoolsList.size()).isEqualTo(4);

        // confirm the first pool in the list is the one for 2025-05-23 as it should sorted in ascending order
        assertThat(availablePoolsList.get(0).getPoolNumber()).isEqualTo(POOL_NUMBER_416220502);

        availablePoolsList.sort(Comparator.comparing(AvailablePoolsInCourtLocationDto
            .AvailablePoolsDto::getPoolNumber));

        verifyAvailablePool(availablePoolsList, LocalDate.now().plusDays(10), POOL_NUMBER_416220502, 0,
            PoolUtilisationDescription.CONFIRMED);
        verifyAvailablePool(availablePoolsList, LocalDate.now().plusDays(12), "416220503", 0,
            PoolUtilisationDescription.CONFIRMED);
        verifyAvailablePool(availablePoolsList, LocalDate.now().plusDays(12), "416220504", 0,
            PoolUtilisationDescription.CONFIRMED);
        verifyAvailablePool(availablePoolsList, LocalDate.now().plusDays(12), "416220505", 0,
            PoolUtilisationDescription.CONFIRMED);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initAvailablePools_courtUser.sql"})
    public void availablePoolsInCourtLocationCourtUserOwnerNotFoundException() {
        final URI uri = URI.create(String.format(URI_AVAILABLE_POOLS, "404"));
        httpHeaders = initialiseHeaders(COURT_USER, UserType.COURT, null, "505");

        ResponseEntity<String> responseEntity = restTemplate.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, uri), String.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        JSONObject exceptionDetails = getExceptionDetails(responseEntity);

        assertThat(exceptionDetails.getString("message")).isEqualTo("Juror record owner: 505 - "
            + "No records found for "
            + "the given owner");
        assertThat(exceptionDetails.getInt("status")).isEqualTo(404);

        List<String> availablePools = getValuesInJsonObject(exceptionDetails, "availablePools");
        assertThat(availablePools).isEmpty();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initAvailablePools.sql"})
    public void availablePoolsInCourtLocationBureauUserHappy() {
        ResponseEntity<AvailablePoolsInCourtLocationDto> responseEntity =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/manage-pool/available-pools/415?is-reassign=true")),
                AvailablePoolsInCourtLocationDto.class);

        assertThat(responseEntity.getStatusCode()).as(EXPECT_HTTP_RESPONSE_SUCCESSFUL).isEqualTo(HttpStatus.OK);

        AvailablePoolsInCourtLocationDto responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();

        List<AvailablePoolsInCourtLocationDto.AvailablePoolsDto> availablePoolsDtoList =
            responseBody.getAvailablePools();

        assertThat(availablePoolsDtoList).isNotNull();

        assertThat(availablePoolsDtoList.size())
            .as("There should be 3 available pools")
            .isEqualTo(3);

        // database has service start date 10 days in the future
        LocalDate serviceStartDate = LocalDate.now().plusDays(10);

        verifyAvailablePool(availablePoolsDtoList, serviceStartDate, POOL_NUMBER_415220401, 2,
            PoolUtilisationDescription.SURPLUS);
        verifyAvailablePool(availablePoolsDtoList, serviceStartDate, "415220502", 2,
            PoolUtilisationDescription.NEEDED);
        verifyAvailablePool(availablePoolsDtoList, serviceStartDate, "415220503", 4,
            PoolUtilisationDescription.NEEDED);
    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initAvailablePools_courtUser.sql"})
    public void availablePoolsInCourtLocationCourtOwnedCourtUserHappy() {
        httpHeaders = initialiseHeaders(COURT_USER, UserType.COURT, null, "416");
        ResponseEntity<AvailablePoolsInCourtLocationDto> responseEntity =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                    URI.create("/api/v1/moj/manage-pool/available-pools-court-owned/416")),
                AvailablePoolsInCourtLocationDto.class);

        assertThat(responseEntity.getStatusCode()).as(EXPECT_HTTP_RESPONSE_SUCCESSFUL).isEqualTo(HttpStatus.OK);

        AvailablePoolsInCourtLocationDto responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();

        List<AvailablePoolsInCourtLocationDto.AvailablePoolsDto> availablePoolsDtoList =
            responseBody.getAvailablePools();

        assertThat(availablePoolsDtoList).isNotNull();

        assertThat(availablePoolsDtoList.size())
            .as("There should be 1 available pool")
            .isEqualTo(1);

        // database has service start date 12 days in the future
        LocalDate serviceStartDate = LocalDate.now().plusDays(12);

        verifyAvailablePool(availablePoolsDtoList, serviceStartDate, "416220504", 0,
            PoolUtilisationDescription.CONFIRMED);
    }

    @Test
    public void availablePoolsInCourtLocationCourtOwnedBureauUserNoAccess() {
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders,
            HttpMethod.GET, URI.create("/api/v1/moj/manage-pool/available-pools-court-owned/416"));
        ResponseEntity<SummoningProgressResponseDto> response = restTemplate.exchange(requestEntity,
            SummoningProgressResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initAvailablePools.sql"})
    public void test_reassignJurorBureauUserHappy() throws Exception {

        final String jurorNumber = "555555553";
        final String targetPoolNumber = POOL_NUMBER_416220502;
        List<String> jurorNumbers = List.of(jurorNumber);

        //create a reassign jurors request DTO
        JurorManagementRequestDto requestDto = createJurorManagementRequestDto("415", POOL_NUMBER_415220401,
            "416", POOL_NUMBER_416220502, jurorNumbers, LocalDate.now());

        RequestEntity<?> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/reassign-jurors"));
        ResponseEntity<?> response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        validateReassignedJuror(jurorNumber, POOL_NUMBER_415220401, targetPoolNumber);

        CourtLocation targetCourt = courtLocationRepository.findById("416").orElse(null);
        assertThat(targetCourt).isNotNull();

        List<JurorHistory> historyEvents = jurorHistoryRepository.findByJurorNumberOrderById(jurorNumber);
        JurorHistory jurorHistory = historyEvents.stream().filter(hist ->
            hist.getHistoryCode().equals(HistoryCodeMod.REASSIGN_POOL_MEMBER)).findFirst().orElse(null);
        assertThat(jurorHistory).isNotNull();


        assertThat(jurorHistory.getOtherInformation()).isEqualTo(targetCourt.getNameWithLocCode());
        assertThat(jurorHistory.getOtherInformationRef()).isEqualTo(targetPoolNumber);

        // verify confirm letter has been queued for bulk print
        List<BulkPrintData> bulkPrintData = bulkPrintDataRepository.findAll();
        assertThat(bulkPrintData.size()).isEqualTo(1);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initAvailablePools.sql"})
    public void test_reassignJurorPreviouslyReassignedBureauUser() throws Exception {

        final String jurorNumber = "555555551"; // this juror was already reassigned from target pool
        List<String> jurorNumbers = List.of(jurorNumber);

        //create a reassign jurors request DTO
        JurorManagementRequestDto requestDto = createJurorManagementRequestDto("415", POOL_NUMBER_415220401,
            "416", POOL_NUMBER_416220502, jurorNumbers, LocalDate.now());

        RequestEntity<?> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/reassign-jurors"));
        ResponseEntity<?> response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        validateReassignedJuror(jurorNumber, POOL_NUMBER_415220401, POOL_NUMBER_416220502);

        // verify confirm letter has been queued for bulk print
        List<BulkPrintData> bulkPrintData = bulkPrintDataRepository.findAll();
        assertThat(bulkPrintData.size()).isEqualTo(1);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initAvailablePools.sql"})
    public void test_reassignMultipleJurorsBureauUserHappy() throws Exception {

        final List<String> jurorNumbers = Arrays.asList("555555551", "555555552", "555555553");

        //create a reassign jurors request DTO
        JurorManagementRequestDto requestDto = createJurorManagementRequestDto("415", POOL_NUMBER_415220401,
            "416", POOL_NUMBER_416220502, jurorNumbers, LocalDate.now());

        RequestEntity<?> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/reassign-jurors"));
        ResponseEntity<ReassignPoolMembersResultDto> response = restTemplate.exchange(requestEntity,
            ReassignPoolMembersResultDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        ReassignPoolMembersResultDto resultDto = response.getBody();

        assertThat(resultDto.getNumberReassigned()).isEqualTo(3);

        for (String jurorNumber : jurorNumbers) {
            validateReassignedJuror(jurorNumber, POOL_NUMBER_415220401, POOL_NUMBER_416220502);
        }

        // verify confirmation letters have been queued for bulk print
        List<BulkPrintData> bulkPrintData = bulkPrintDataRepository.findAll();
        assertThat(bulkPrintData.size()).isEqualTo(3);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initAvailablePools.sql"})
    public void test_reassignMultipleJurorsOneInvalidBureauUser() throws Exception {

        final List<String> jurorNumbers = Arrays.asList("555555551", "555555552", "555555555");
        final List<String> goodJurorNumbers = Arrays.asList("555555551", "555555552");

        //create a reassign jurors request DTO
        JurorManagementRequestDto requestDto = createJurorManagementRequestDto("415", POOL_NUMBER_415220401,
            "416", POOL_NUMBER_416220502, jurorNumbers, LocalDate.now());

        RequestEntity<?> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/reassign-jurors"));
        ResponseEntity<ReassignPoolMembersResultDto> response = restTemplate.exchange(requestEntity,
            ReassignPoolMembersResultDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        ReassignPoolMembersResultDto resultDto = response.getBody();

        assertThat(resultDto.getNumberReassigned()).isEqualTo(2);

        for (String jurorNumber : goodJurorNumbers) {
            validateReassignedJuror(jurorNumber, POOL_NUMBER_415220401, POOL_NUMBER_416220502);
        }

    }

    @Test
    public void test_reassignJurorNoJurorsBureauUser() throws Exception {

        List<String> jurorNumbers = new ArrayList<>();

        //create a reassign jurors request DTO
        JurorManagementRequestDto requestDto = createJurorManagementRequestDto("415", POOL_NUMBER_415220401,
            "416", POOL_NUMBER_416220502, jurorNumbers, LocalDate.now());

        RequestEntity<?> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/reassign-jurors"));
        ResponseEntity<?> response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initAvailablePools.sql"})
    public void test_reassignMultipleJurorsSameCourtBureauUserHappy() throws Exception {

        final List<String> jurorNumbers = Arrays.asList("555555551", "555555552");

        //create a reassign jurors request DTO
        JurorManagementRequestDto requestDto = createJurorManagementRequestDto("415", POOL_NUMBER_415220401,
            "415", "415220503", jurorNumbers, LocalDate.now());

        RequestEntity<?> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/reassign-jurors"));
        ResponseEntity<ReassignPoolMembersResultDto> response = restTemplate.exchange(requestEntity,
            ReassignPoolMembersResultDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        ReassignPoolMembersResultDto resultDto = response.getBody();

        assertThat(resultDto.getNumberReassigned()).isEqualTo(2);

        for (String jurorNumber : jurorNumbers) {
            validateReassignedJuror(jurorNumber, POOL_NUMBER_415220401, "415220503");
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initAvailablePools.sql"})
    public void testReassignJurorSameCourtPreviouslyReassignedBureauUser() throws Exception {

        final String jurorNumber = "555555561"; // this juror was already reassigned from target pool
        List<String> jurorNumbers = List.of(jurorNumber);

        //create a reassign jurors request DTO
        JurorManagementRequestDto requestDto = createJurorManagementRequestDto("416", "416220503",
            "416", POOL_NUMBER_416220502, jurorNumbers, LocalDate.now());

        RequestEntity<?> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/reassign-jurors"));
        ResponseEntity<?> response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        validateReassignedJuror(jurorNumber, "416220503", POOL_NUMBER_416220502);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initAvailablePools.sql"})
    public void testReassignJurorSameCourtPreviouslyReassignedAndReassignBackAgainBureauUser() throws Exception {

        final String jurorNumber = "555555561"; // this juror was already reassigned from target pool
        List<String> jurorNumbers = List.of(jurorNumber);

        //create a reassign jurors request DTO
        JurorManagementRequestDto requestDto = createJurorManagementRequestDto("416", "416220503",
            "416", POOL_NUMBER_416220502, jurorNumbers, LocalDate.now());

        RequestEntity<?> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/reassign-jurors"));
        ResponseEntity<?> response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        validateReassignedJuror(jurorNumber, "416220503", POOL_NUMBER_416220502);

        //create a reassign jurors request DTO
        JurorManagementRequestDto requestDto2 = createJurorManagementRequestDto("416", POOL_NUMBER_416220502,
            "416", "416220503", jurorNumbers, LocalDate.now());

        RequestEntity<?> requestEntity2 = new RequestEntity<>(requestDto2, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/reassign-jurors"));
        ResponseEntity<?> response2 = restTemplate.exchange(requestEntity2, String.class);

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

        validateReassignedJuror(jurorNumber, POOL_NUMBER_416220502, "416220503");
    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/reassignJurors.sql"})
    public void testReassignJurorCourtUserDifferentLocation() throws Exception {

        final String jurorNumber = "555555551";
        final String targetPoolNumber = "767220504";
        final String sourcePool = "415220504";
        final String owner = "415";
        List<String> jurorNumbers = List.of(jurorNumber);

        //create a reassign jurors request DTO
        JurorManagementRequestDto requestDto = createJurorManagementRequestDto("415", sourcePool,
            "767", "767220504", jurorNumbers, LocalDate.now());

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", List.of("415", "767")));

        RequestEntity<?> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/reassign-jurors"));
        ResponseEntity<?> response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        CourtLocation targetCourt = courtLocationRepository.findById("767").orElse(null);
        assertThat(targetCourt).isNotNull();

        executeInTransaction(() -> {
            // check the old record is now inactive
            Optional<JurorPool> oldJurorPoolOpt = jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumber(
                                        owner, jurorNumber, sourcePool);
            assertThat(oldJurorPoolOpt).isPresent();
            JurorPool oldJurorPool = oldJurorPoolOpt.get();
            assertThat(oldJurorPool).isNotNull();
            assertThat(oldJurorPool.getStatus().getStatus()).isEqualTo(8L);
            assertThat(oldJurorPool.getNextDate()).isNull();
            // check reassign date is set
            assertThat(oldJurorPool.getReassignDate()).isEqualTo(LocalDate.now());

            // check there is a new record created for reassigned juror
            Optional<JurorPool> newJurorPoolOpt = jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumber(
                                        owner, jurorNumber, targetPoolNumber);
            assertThat(newJurorPoolOpt).isPresent();
            JurorPool newJurorPool = newJurorPoolOpt.get();
            assertThat(newJurorPool).isNotNull();
            assertThat(newJurorPool.getStatus().getStatus()).isEqualTo(2L);
            assertThat(newJurorPool.getIsActive()).isTrue();

            List<JurorHistory> historyEvents = jurorHistoryRepository.findByJurorNumberOrderById(jurorNumber);
            JurorHistory jurorHistory = historyEvents.stream().filter(hist ->
                hist.getHistoryCode().equals(HistoryCodeMod.REASSIGN_POOL_MEMBER)).findFirst().orElse(null);
            assertThat(jurorHistory).isNotNull();

            assertThat(jurorHistory.getOtherInformation()).isEqualTo(targetCourt.getNameWithLocCode());
            assertThat(jurorHistory.getOtherInformationRef()).isEqualTo(targetPoolNumber);
        });

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/jurormanagement/reassignJurors.sql"})
    public void testReassignJurorCourtUserSameCourtLocation() throws Exception {

        final String jurorNumber = "555555551";
        final String targetPoolNumber = "415220505";
        final String sourcePool = "415220504";
        final String owner = "415";
        final String locCode = "415";
        List<String> jurorNumbers = List.of(jurorNumber);

        //create a reassign jurors request DTO
        JurorManagementRequestDto requestDto = createJurorManagementRequestDto(locCode, sourcePool,
            locCode, targetPoolNumber, jurorNumbers, LocalDate.now());

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt(owner, List.of("415", "767")));

        RequestEntity<?> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/reassign-jurors"));
        ResponseEntity<?> response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        CourtLocation targetCourt = courtLocationRepository.findById(locCode).orElse(null);
        assertThat(targetCourt).isNotNull();

        executeInTransaction(() -> {
            // check the old record is now inactive
            Optional<JurorPool> oldJurorPoolOpt = jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumber(
                owner, jurorNumber, sourcePool);
            assertThat(oldJurorPoolOpt).isPresent();
            JurorPool oldJurorPool = oldJurorPoolOpt.get();
            assertThat(oldJurorPool).isNotNull();
            assertThat(oldJurorPool.getStatus().getStatus()).isEqualTo(8L);
            assertThat(oldJurorPool.getNextDate()).isNull();
            // check reassign date is not set as its same court location
            assertThat(oldJurorPool.getReassignDate()).isNull();

            // check there is a new record created for reassigned juror
            Optional<JurorPool> newJurorPoolOpt = jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumber(
                owner, jurorNumber, targetPoolNumber);
            assertThat(newJurorPoolOpt).isPresent();
            JurorPool newJurorPool = newJurorPoolOpt.get();
            assertThat(newJurorPool).isNotNull();
            assertThat(newJurorPool.getStatus().getStatus()).isEqualTo(2L);
            assertThat(newJurorPool.getIsActive()).isTrue();

            List<JurorHistory> historyEvents = jurorHistoryRepository.findByJurorNumberOrderById(jurorNumber);
            JurorHistory jurorHistory = historyEvents.stream().filter(hist ->
                hist.getHistoryCode().equals(HistoryCodeMod.REASSIGN_POOL_MEMBER)).findFirst().orElse(null);
            assertThat(jurorHistory).isNotNull();

            assertThat(jurorHistory.getOtherInformation()).isEqualTo(targetCourt.getNameWithLocCode());
            assertThat(jurorHistory.getOtherInformationRef()).isEqualTo(targetPoolNumber);
        });

    }


    private JSONObject getExceptionDetails(ResponseEntity<String> responseEntity) {
        return new JSONObject(responseEntity.getBody());
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_getPoolMonitoringStats.sql"})
    public void testGetPoolMonitoringStatsBureauUserHappyPath() throws Exception {
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders,
            HttpMethod.GET, URI.create("/api/v1/moj/manage-pool/summoning-progress/415/CRO"));
        ResponseEntity<SummoningProgressResponseDto> response = restTemplate.exchange(requestEntity,
            SummoningProgressResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        SummoningProgressResponseDto responseDto = response.getBody();
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getStatsByWeek().size()).isEqualTo(8);
        LocalDate weekCommencingDate = DateUtils.getStartOfWeekFromDate(LocalDate.now());
        LocalDate eightWeeksCommencingDate = weekCommencingDate.plusWeeks(7);
        for (SummoningProgressResponseDto.WeekFilter week : responseDto.getStatsByWeek()) {
            assertThat(week.getStartOfWeek()).isBeforeOrEqualTo(eightWeeksCommencingDate)
                .isAfterOrEqualTo(weekCommencingDate);
            assertThat(week.getStats().size()).isLessThanOrEqualTo(5);
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_getPoolMonitoringStats.sql"})
    public void testGetPoolMonitoringStatsCourtUser() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", new ArrayList<>()));

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders,
            HttpMethod.GET, URI.create("/api/v1/moj/manage-pool/summoning-progress/415/CRO"));
        ResponseEntity<SummoningProgressResponseDto> response = restTemplate.exchange(requestEntity,
            SummoningProgressResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_getPoolMonitoringStats.sql"})
    public void testGetPoolMonitoringStats_wrongPoolTypeForCourtLocation() throws Exception {
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders,
            HttpMethod.GET, URI.create("/api/v1/moj/manage-pool/summoning-progress/415/CIV"));
        ResponseEntity<SummoningProgressResponseDto> response = restTemplate.exchange(requestEntity,
            SummoningProgressResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private void validateReassignedJuror(String jurorNumber, String sourcePool, String receivingPool) {
        executeInTransaction(() -> {
            // check the old record is now inactive
            JurorPool oldJurorPool =
                jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumber("400", jurorNumber,
                    sourcePool).get();
            assertThat(oldJurorPool).isNotNull();
            assertThat(oldJurorPool.getStatus().getStatus()).isEqualTo(8L);
            assertThat(oldJurorPool.getNextDate()).isNull();
            // check reassign date is not set as its a Bureau user
            assertThat(oldJurorPool.getReassignDate()).isNull();

            // check there is a new record created for reassigned juror
            JurorPool newJurorPool =
                jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumber("400", jurorNumber,
                    receivingPool).get();
            assertThat(newJurorPool).isNotNull();
            assertThat(newJurorPool.getStatus().getStatus()).isEqualTo(2L);

            assertThat(newJurorPool.getIsActive()).isTrue();

            verifyCopiedFields(oldJurorPool.getJuror(), newJurorPool.getJuror());

            // database should have service start date 10 days in the future
            LocalDate serviceStartDate = LocalDate.now().plusDays(10);
            assertThat(newJurorPool.getNextDate()).isEqualTo(serviceStartDate);

            // check there is a cert letter queued, should be one only
            Optional<BulkPrintData> confirmLetter = bulkPrintDataRepository.findLatestPendingLetterForJuror(jurorNumber,
                FormCode.ENG_CONFIRMATION.getCode());

            if (confirmLetter.isPresent()) {
                assertThat(confirmLetter.get().getJurorNo()).isEqualTo(jurorNumber);
            } else {
                fail("No confirmation letter found for juror: " + jurorNumber);
            }
        });
    }

    private static void verifyCopiedFields(Juror oldJuror, Juror newJuror) {
        // verify some of the crucial fields that should have been copied for juror
        assertThat(oldJuror.getFirstName()).isEqualTo(newJuror.getFirstName());
        assertThat(oldJuror.getLastName()).isEqualTo(newJuror.getLastName());
        assertThat(oldJuror.getPostcode()).isEqualTo(newJuror.getPostcode());
        assertThat(oldJuror.getAddressLine1()).isEqualTo(newJuror.getAddressLine1());

        if (oldJuror.getDateOfBirth() != null) {
            assertThat(oldJuror.getDateOfBirth()).isEqualTo(newJuror.getDateOfBirth());
        }
        if (oldJuror.getNotes() != null) {
            assertThat(oldJuror.getNotes()).isEqualTo(newJuror.getNotes());
        }
        if (oldJuror.getPoliceCheck() != null) {
            assertThat(oldJuror.getPoliceCheck()).isEqualTo(newJuror.getPoliceCheck());
        }

        assertThat(newJuror.getOpticRef()).isEqualTo(oldJuror.getOpticRef());
    }

    private void verifyAvailablePool(List<AvailablePoolsInCourtLocationDto.AvailablePoolsDto> availablePoolsDtoList,
                                     LocalDate serviceStartDate,
                                     final String poolNumber,
                                     final int utilisation,
                                     final PoolUtilisationDescription poolUtilisationDescription) {
        AvailablePoolsInCourtLocationDto.AvailablePoolsDto availablePool = availablePoolsDtoList.stream()
            .filter(pool -> pool.getPoolNumber().equals(poolNumber))
            .findFirst()
            .orElse(null);
        assertThat(availablePool)
            .as("Expect a valid active pool with pool number " + poolNumber + " to be returned as an available option")
            .isNotNull();
        assertThat(availablePool.getServiceStartDate())
            .as("Expect correct Pool Request service start date to be mapped in to the DTO")
            .isEqualTo(LocalDate.of(serviceStartDate.getYear(), serviceStartDate.getMonth(),
                serviceStartDate.getDayOfMonth()));
        assertThat(availablePool.getUtilisation())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(utilisation);
        assertThat(availablePool.getUtilisationDescription())
            .as("Expect Pool Utilisation stats to be calculated for the given pool request")
            .isEqualTo(poolUtilisationDescription);
    }

    private PoolEditRequestDto getPoolEditRequestDto(String poolNumber) {
        PoolEditRequestDto poolEditRequestDto = new PoolEditRequestDto();
        poolEditRequestDto.setPoolNumber(poolNumber);
        poolEditRequestDto.setNoRequested(55);
        poolEditRequestDto.setTotalRequired(null);
        poolEditRequestDto.setReasonForChange("Need a few more Jurors for the trials");

        return poolEditRequestDto;

    }

    private JurorManagementRequestDto createJurorManagementRequestDto(String sourceCourt, String sourcePool,
                                                                      String receivingCourt,
                                                                      String receivingPool, List<String> jurorNumbers,
                                                                      LocalDate serviceStartDate) {
        JurorManagementRequestDto requestDto = new JurorManagementRequestDto();

        requestDto.setSourcePoolNumber(sourcePool);
        requestDto.setSourceCourtLocCode(sourceCourt);
        requestDto.setReceivingPoolNumber(receivingPool);
        requestDto.setReceivingCourtLocCode(receivingCourt);
        requestDto.setJurorNumbers(jurorNumbers);
        requestDto.setServiceStartDate(serviceStartDate);

        return requestDto;
    }

    private void transferJurorPoolValidateNewlyCreatedPoolRequest(String sourcePoolNumber, String targetPoolNumber,
                                                                  String receivingCourtLocation,
                                                                  LocalDate targetServiceStartDate) {
        PoolRequest targetPoolRequest = poolRequestRepository
            .findByPoolNumber(targetPoolNumber)
            .orElse(null);

        PoolRequest sourcePoolRequest = poolRequestRepository
            .findByPoolNumber(sourcePoolNumber)
            .orElse(null);

        assertThat(targetPoolRequest)
            .as("New pool request should be created with a sequence number of 02 (suffix)")
            .isNotNull();
        assertThat(targetPoolRequest.getNumberRequested())
            .as("New pool request should have a Number Requested intentionally set to null to differentiate it form a"
                + " nil pool request")
            .isNull();
        assertThat(targetPoolRequest.getOwner())
            .as("New pool request should be owned by the receiving court location")
            .isEqualTo(receivingCourtLocation);
        assertThat(targetPoolRequest.getCourtLocation().getLocCode())
            .as("New pool request should be requested for the receiving court location")
            .isEqualTo(receivingCourtLocation);
        assertThat(targetPoolRequest.getNewRequest())
            .as("New pool request should use the default value for New Request")
            .isEqualTo('N');
        assertThat(targetPoolRequest.getReturnDate())
            .as("New pool request should use the target service start date as Return Date")
            .isEqualTo(targetServiceStartDate);
        assertThat(targetPoolRequest.getPoolType().getPoolType())
            .as("New pool request property should be mapped from source pool request")
            .isEqualTo(sourcePoolRequest.getPoolType().getPoolType());
    }

    private void transferJurorPoolValidateSourceJurorPool(String jurorNumber, String poolNumber) {
        JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumberAndIsActive(jurorNumber,
            poolNumber, true).get();

        assertThat(jurorPool.getStatus().getStatus())
            .as("Old juror should be set to Transferred (10)")
            .isEqualTo(10L);
        assertThat(jurorPool.getNextDate())
            .as("Old juror should be updated - NEXT DATE = Null")
            .isNull();
        assertThat(jurorPool.getTransferDate())
            .as("Old juror should be updated - Transferred Date = Today")
            .isEqualTo(LocalDate.now());
    }

    @SuppressWarnings("PMD.NcssCount")
    private void transferJurorPoolValidateNewlyCreatedJurorPool(String jurorNumber, String sourcePoolNumber,
                                                                String targetPoolNumber, String targetLocCode,
                                                                LocalDate targetStartDate, String currentUser) {
        executeInTransaction(() -> {
            JurorPool sourceJurorPool =
                jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumberAndIsActive(jurorNumber,
                    sourcePoolNumber, true).get();
            JurorPool targetJurorPool =
                jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumberAndIsActive(jurorNumber,
                    targetPoolNumber, true).get();

            // derived properties
            assertThat(targetJurorPool.getOwner())
                .as("Expect property to be derived for new juror record")
                .isEqualTo(targetLocCode);
            assertThat(targetJurorPool.getPoolNumber())
                .as("Expect property to be derived for new juror record")
                .isEqualTo(targetPoolNumber);
            assertThat(targetJurorPool.getNextDate())
                .as("Expect property to be derived for new juror record")
                .isEqualTo(targetStartDate);
            assertThat(targetJurorPool.getUserEdtq())
                .as("Expect property to be derived for new juror record")
                .isEqualTo(currentUser);
            assertThat(targetJurorPool.getCourt().getLocCode())
                .as("Expect property to be derived for new juror record")
                .isEqualTo(targetLocCode);

            // copied properties
            Juror targetJuror = targetJurorPool.getJuror();
            Juror sourceJuror = sourceJurorPool.getJuror();
            assertThat(targetJuror.getJurorNumber())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJurorPool.getJurorNumber());
            assertThat(targetJuror.getPollNumber())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getPollNumber());
            assertThat(targetJuror.getTitle())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getTitle());
            assertThat(targetJuror.getFirstName())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getFirstName());
            assertThat(targetJuror.getLastName())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getLastName());
            assertThat(targetJuror.getDateOfBirth().compareTo(sourceJuror.getDateOfBirth()))
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(0);
            assertThat(targetJuror.getAddressLine1())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getAddressLine1());
            assertThat(targetJuror.getAddressLine2())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getAddressLine2());
            assertThat(targetJuror.getAddressLine3())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getAddressLine3());
            assertThat(targetJuror.getAddressLine4())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getAddressLine4());
            assertThat(targetJuror.getAddressLine5())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getAddressLine5());
            assertThat(targetJuror.getPostcode())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getPostcode());
            assertThat(targetJuror.getPhoneNumber())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getPhoneNumber());
            assertThat(targetJuror.getAltPhoneNumber())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getAltPhoneNumber());
            assertThat(targetJuror.getWorkPhone())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getWorkPhone());
            assertThat(targetJuror.getWorkPhoneExtension())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getWorkPhoneExtension());
            assertThat(targetJurorPool.getDeferralDate())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(targetJurorPool.getDeferralDate());
            assertThat(targetJuror.isResponded())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.isResponded());
            assertThat(targetJuror.getExcusalDate())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getExcusalDate());
            assertThat(targetJuror.getExcusalCode())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getExcusalCode());
            assertThat(targetJuror.getExcusalRejected())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getExcusalRejected());
            assertThat(targetJuror.getDisqualifyDate())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getDisqualifyDate());
            assertThat(targetJuror.getDisqualifyCode())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getDisqualifyCode());
            assertThat(targetJuror.getNotes())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getNotes());
            assertThat(targetJurorPool.getNoAttendances())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJurorPool.getNoAttendances());
            assertThat(targetJurorPool.getIsActive())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJurorPool.getIsActive());
            assertThat(targetJuror.getNoDefPos())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getNoDefPos());
            assertThat(targetJuror.getPermanentlyDisqualify())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getPermanentlyDisqualify());
            assertThat(targetJuror.getReasonableAdjustmentCode())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getReasonableAdjustmentCode());
            assertThat(targetJuror.getReasonableAdjustmentMessage())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getReasonableAdjustmentMessage());
            assertThat(targetJuror.getSortCode())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getSortCode());
            assertThat(targetJuror.getBankAccountName())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getBankAccountName());
            assertThat(targetJuror.getBankAccountNumber())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getBankAccountNumber());
            assertThat(targetJuror.getBuildingSocietyRollNumber())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getBuildingSocietyRollNumber());
            assertThat(targetJurorPool.getWasDeferred())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJurorPool.getWasDeferred());
            assertThat(targetJurorPool.getIdChecked())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJurorPool.getIdChecked());
            assertThat(targetJurorPool.getPostpone())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJurorPool.getPostpone());
            assertThat(targetJuror.getWelsh())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getWelsh());
            assertThat(targetJuror.getPoliceCheck())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getPoliceCheck());
            assertThat(targetJuror.getEmail())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getEmail());
            assertThat(targetJuror.getContactPreference())
                .as(EXPECT_PROPERTY_TO_BE_COPIED_FROM_SOURCE_JUROR)
                .isEqualTo(sourceJuror.getContactPreference());

            assertThat(targetJuror.getOpticRef())
                .as("Expect optic reference to be copied from source juror")
                .isEqualTo(sourceJuror.getOpticRef());

            // default/not copied properties
            assertThat(targetJurorPool.getTimesSelected())
                .as(EXPECT_PROPERTY_TO_BE_USE_A_DEFAULT_VALUE)
                .isNull();
            assertThat(targetJurorPool.getLocation())
                .as(EXPECT_PROPERTY_TO_BE_USE_A_DEFAULT_VALUE)
                .isNull();
            assertThat(targetJurorPool.getStatus().getStatus())
                .as(EXPECT_PROPERTY_TO_BE_USE_A_DEFAULT_VALUE)
                .isEqualTo(2L);
            assertThat(targetJurorPool.getNoAttended())
                .as(EXPECT_PROPERTY_TO_BE_USE_A_DEFAULT_VALUE)
                .isNull();
            assertThat(targetJurorPool.getFailedToAttendCount())
                .as(EXPECT_PROPERTY_TO_BE_USE_A_DEFAULT_VALUE)
                .isNull();
            assertThat(targetJurorPool.getUnauthorisedAbsenceCount())
                .as(EXPECT_PROPERTY_TO_BE_USE_A_DEFAULT_VALUE)
                .isNull();
            assertThat(targetJurorPool.getEditTag())
                .as(EXPECT_PROPERTY_TO_BE_USE_A_DEFAULT_VALUE)
                .isNull();
            assertThat(targetJurorPool.isOnCall())
                .as(EXPECT_PROPERTY_TO_BE_USE_A_DEFAULT_VALUE)
                .isFalse();
            assertThat(targetJurorPool.getSmartCard())
                .as(EXPECT_PROPERTY_TO_BE_USE_A_DEFAULT_VALUE)
                .isNull();
            assertThat(targetJurorPool.getPaidCash())
                .as(EXPECT_PROPERTY_TO_BE_USE_A_DEFAULT_VALUE)
                .isNull();
            assertThat(targetJurorPool.getScanCode())
                .as(EXPECT_PROPERTY_TO_BE_USE_A_DEFAULT_VALUE)
                .isNull();
            assertThat(targetJuror.getSummonsFile())
                .as(EXPECT_PROPERTY_TO_BE_USE_A_DEFAULT_VALUE)
                .isNull();
            assertThat(targetJurorPool.getReminderSent())
                .as(EXPECT_PROPERTY_TO_BE_USE_A_DEFAULT_VALUE)
                .isNull();
            assertThat(targetJuror.getNotifications())
                .as(EXPECT_PROPERTY_TO_BE_USE_A_DEFAULT_VALUE)
                .isEqualTo(0);
            assertThat(targetJurorPool.getTransferDate())
                .as(EXPECT_PROPERTY_TO_BE_USE_A_DEFAULT_VALUE)
                .isNull();
        });
    }

    private void transferJurorPoolValidateExistingTransferredJurorPool(String jurorNumber, String poolNumber) {
        executeInTransaction(() -> {
            JurorPool jurorPool =
                jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumberAndIsActive(jurorNumber,
                    poolNumber, false).get();

            assertThat(jurorPool)
                .as("A juror record should exist in a now inactive and read only state")
                .isNotNull();
            assertThat(jurorPool.getStatus().getStatus())
                .as("Old juror should be set to Transferred (10)")
                .isEqualTo(10L);
        });
    }
}
