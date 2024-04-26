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
import uk.gov.hmcts.juror.api.moj.domain.HistoryCode;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorHistory;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.QPoolComment;
import uk.gov.hmcts.juror.api.moj.domain.QPoolHistory;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;
import uk.gov.hmcts.juror.api.moj.domain.letter.CertLetter;
import uk.gov.hmcts.juror.api.moj.domain.letter.ConfirmationLetter;
import uk.gov.hmcts.juror.api.moj.domain.letter.LetterId;
import uk.gov.hmcts.juror.api.moj.enumeration.HistoryCodeMod;
import uk.gov.hmcts.juror.api.moj.enumeration.PoolUtilisationDescription;
import uk.gov.hmcts.juror.api.moj.repository.ConfirmationLetterRepository;
import uk.gov.hmcts.juror.api.moj.repository.CourtLocationRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolCommentRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolHistoryRepository;
import uk.gov.hmcts.juror.api.moj.repository.PoolRequestRepository;
import uk.gov.hmcts.juror.api.moj.repository.letter.CertLetterRepository;
import uk.gov.hmcts.juror.api.moj.service.CourtLocationService;
import uk.gov.hmcts.juror.api.moj.service.PoolStatisticsService;
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
import static uk.gov.hmcts.juror.api.TestUtil.getValuesInJsonObject;

/**
 * Integration tests for the API endpoints defined in {@link ManagePoolController}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings({"PMD.LawOfDemeter", "PMD.TooManyMethods", "PMD.ExcessiveImports"})
public class ManagePoolControllerITest extends AbstractIntegrationTest {
    private static final String URI_AVAILABLE_POOLS = "/api/v1/moj/manage-pool/available-pools/%s";
    private static final String URI_MANAGE_POOL_SUMMARY = "/api/v1/moj/manage-pool/summary?poolNumber=%s";
    private static final String COURT_USER = "COURT_USER";
    private static final String POOL_NUMBER_415221001 = "415221001";
    private static final String EXPECT_HTTP_RESPONSE_SUCCESSFUL = "Expect the HTTP GET request to be successful";
    private static final QPoolComment POOL_COMMENTS = QPoolComment.poolComment;

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private PoolStatisticsService poolStatisticsService;
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
    private CertLetterRepository certLetterRepository;
    @Autowired
    private JurorHistoryRepository jurorHistoryRepository;
    @Autowired
    private CourtLocationRepository courtLocationRepository;

    private HttpHeaders httpHeaders;
    @Autowired
    private ConfirmationLetterRepository confirmationLetterRepository;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
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
    public void getPoolStatistics_allDataPresent() {
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
    public void getPoolStatistics_courtSupplyOnly() {
        ResponseEntity<PoolSummaryResponseDto> responseEntity =
            restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
                URI.create(String.format(URI_MANAGE_POOL_SUMMARY, 415221001))), PoolSummaryResponseDto.class);

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
    public void getPoolStatistics_noPoolRequestExt() {
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
    public void test_deletePool_poolNotFound() throws Exception {
        initHeaders(Role.MANAGER);

        String poolNumber = "415220101";

        ResponseEntity<?> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.DELETE,
            URI.create("/api/v1/moj/manage-pool/delete?poolNumber=" + poolNumber)), Object.class);

        assertThat(response.getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void test_deletePool_insufficientPermission() {
        initHeaders(Role.SENIOR_JUROR_OFFICER);
        String poolNumber = "415220101";

        ResponseEntity<?> response = restTemplate.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.DELETE,
            URI.create("/api/v1/moj/manage-pool/delete?poolNumber=" + poolNumber)), Object.class);

        assertThat(response.getStatusCode())
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolRequests.sql"})
    public void test_deletePool_poolIsLocked() throws Exception {
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
    public void test_deletePool_courtUserAndActivePool() throws Exception {
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
    public void test_deletePool() throws Exception {
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
    public void test_editPoolBureauUser() throws Exception {
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
    public void test_editPool_NoRequestedSame_BureauUser() throws Exception {
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
    public void test_editPoolAbovePoolCapacity_BureauUser() throws Exception {
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
    @Sql(statements = "DELETE FROM JUROR.POOL_COMMENTS")
    public void test_editPoolCourtUser() throws Exception {
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
    public void test_editPool_TotalRequiredSame_CourtUser() throws Exception {
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
    @Sql(statements = "DELETE FROM JUROR.POOL_COMMENTS")
    @Sql(statements = "DELETE FROM JUROR_MOD.POOL_HISTORY")
    public void test_editPool_TotalRequiredLowerThanRequested_CourtUser() throws Exception {
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
    public void test_validateJurorsForTransfer_happyPath() throws Exception {
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
        assertThat(responseDto.getAvailableForMove().stream().allMatch(jurorNumber ->
            jurorNumber.equalsIgnoreCase("111111111"))).isTrue();

        List<JurorManagementResponseDto.ValidationFailure> validationFailuresList =
            responseDto.getUnavailableForMove();

        JurorManagementResponseDto.ValidationFailure validationFailure1 = validationFailuresList.stream()
            .filter(failure -> failure.getJurorNumber().equalsIgnoreCase("222222222"))
            .findFirst().orElse(null);
        assertThat(validationFailure1).isNotNull();
        assertThat(validationFailure1.getFailureReason())
            .isEqualToIgnoringCase(JurorManagementConstants.ABOVE_AGE_LIMIT_MESSAGE);
        assertThat(validationFailure1.getFirstName()).isEqualToIgnoringCase("FNAMETWOS");
        assertThat(validationFailure1.getLastName()).isEqualToIgnoringCase("LNAMETWOS");

        JurorManagementResponseDto.ValidationFailure validationFailure2 = validationFailuresList.stream()
            .filter(failure -> failure.getJurorNumber().equalsIgnoreCase("333333333"))
            .findFirst().orElse(null);
        assertThat(validationFailure2).isNotNull();
        assertThat(validationFailure2.getFailureReason())
            .isEqualToIgnoringCase(String.format(JurorManagementConstants.INVALID_STATUS_MESSAGE, "Transferred"));
        assertThat(validationFailure2.getFirstName()).isEqualToIgnoringCase("FNAMETHREES");
        assertThat(validationFailure2.getLastName()).isEqualToIgnoringCase("LNAMETHREES");

        JurorManagementResponseDto.ValidationFailure validationFailure3 = validationFailuresList.stream()
            .filter(failure -> failure.getJurorNumber().equalsIgnoreCase("444444444"))
            .findFirst().orElse(null);
        assertThat(validationFailure3).isNotNull();
        assertThat(validationFailure3.getFailureReason())
            .isEqualToIgnoringCase(JurorManagementConstants.NO_ACTIVE_RECORD_MESSAGE);
        assertThat(validationFailure3.getFirstName()).isEqualToIgnoringCase("");
        assertThat(validationFailure3.getLastName()).isEqualToIgnoringCase("");

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void test_validateJurorsForTransfer_invalidCourtLocation() throws Exception {
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
    public void test_validateJurorsForTransfer_badRequest_invalidPoolNumber_tooShort() throws Exception {
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
    public void test_validateJurorsForTransfer_badRequest_invalidPoolNumber_tooLong() throws Exception {
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
    public void test_validateJurorsForTransfer_badRequest_invalidPoolNumber_alphaNumeric() throws Exception {
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
    public void test_validateJurorsForTransfer_badRequest_invalidSourceCourtLocation_tooShort() throws Exception {
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
    public void test_validateJurorsForTransfer_badRequest_invalidSourceCourtLocation_tooLong() throws Exception {
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
    public void test_validateJurorsForTransfer_badRequest_invalidSourceCourtLocation_alphaNumeric() throws Exception {
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
    public void test_validateJurorsForTransfer_badRequest_invalidTargetCourtLocation_tooShort() throws Exception {
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
    public void test_validateJurorsForTransfer_badRequest_invalidTargetCourtLocation_tooLong() throws Exception {
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
    public void test_validateJurorsForTransfer_badRequest_invalidTargetCourtLocation_alphaNumeric() throws Exception {
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
    public void test_validateJurorsForTransfer_badRequest_invalidJurorNumbersList() throws Exception {
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
    public void test_transferJurorsToNewCourt_oneJuror_firstTransfer_happyPath() throws Exception {
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
            targetCourtLocation, targetServiceStartDate, 1);

        transferJurorPoolValidateSourceJurorPool("111111111", sourcePoolNumber);

        transferJurorPoolValidateNewlyCreatedJurorPool("111111111", sourcePoolNumber,
            "457230702", targetCourtLocation, targetServiceStartDate, COURT_USER);

        long letterCount = certLetterRepository.count();

        assertThat(letterCount)
            .as("Only a single Certificate of Attendance Letters should exist")
            .isEqualTo(1);
        validateCertificateOfAttendanceLetter(sourceCourtLocation, "111111111");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void test_transferJurorsToNewCourt_multipleJurors_someFail() throws Exception {
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
            targetCourtLocation, targetServiceStartDate, 2);

        for (String jurorNumber : Arrays.asList("111111111", "555555555")) {
            transferJurorPoolValidateSourceJurorPool(jurorNumber, sourcePoolNumber);

            transferJurorPoolValidateNewlyCreatedJurorPool(jurorNumber, sourcePoolNumber,
                "457230702", targetCourtLocation, targetServiceStartDate, COURT_USER);
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initTransferBackPoolMember.sql"})
    public void test_transferJurorsToNewCourt_oneJuror_transferBack_happyPath() throws Exception {
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
            targetCourtLocation, targetServiceStartDate, 1);

        transferJurorPoolValidateSourceJurorPool("111111111", sourcePoolNumber);

        transferJurorPoolValidateNewlyCreatedJurorPool("111111111", sourcePoolNumber,
            "415230702", targetCourtLocation, targetServiceStartDate, COURT_USER);

        transferJurorPoolValidateExistingTransferredJurorPool("111111111", "415230701");

        long letterCount = certLetterRepository.count();

        assertThat(letterCount)
            .as("2 Certificate of Attendance Letters should exist")
            .isEqualTo(2);
        validateCertificateOfAttendanceLetter(targetCourtLocation, "111111111");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initPoolMembersForTransfer.sql"})
    public void test_transferJurorsToNewCourt_unhappyPath_invalidAccess_bureauUser() throws Exception {
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
    public void test_transferJurorsToNewCourt_unhappyPath_invalidAccess_courtUser() throws Exception {
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
    public void test_transferJurorsToNewCourt_unhappyPath_invalidJurorNumber_tooShort() throws Exception {
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
    public void test_transferJurorsToNewCourt_unhappyPath_invalidJurorNumber_tooLong() throws Exception {
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
    public void test_transferJurorsToNewCourt_unhappyPath_invalidJurorNumber_alphanumeric() throws Exception {
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
    public void test_transferJurorsToNewCourt_unhappyPath_sourcePoolNotFound() throws Exception {
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
    public void test_transferJurorsToNewCourt_unhappyPath_invalidSourceCourt() throws Exception {
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
    public void test_transferJurorsToNewCourt_unhappyPath_invalidTargetCourt() throws Exception {
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
    public void test_transferJurorsToNewCourt_unhappyPath_invalidRequest_jurorListEmpty() throws Exception {
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
    public void test_transferJurorsToNewCourt_unhappyPath_invalidRequest_sourcePoolNumber() throws Exception {
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
    public void test_transferJurorsToNewCourt_unhappyPath_invalidRequest_sourceCourtLocationCode() throws Exception {
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
    public void test_transferJurorsToNewCourt_unhappyPath_invalidRequest_targetCourtLocationCode() throws Exception {
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
    public void availablePoolsInCourtLocationCourtUserHappy() throws NullPointerException {
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

        availablePoolsList.sort(Comparator.comparing(AvailablePoolsInCourtLocationDto
            .AvailablePoolsDto::getPoolNumber));

        verifyAvailablePool(availablePoolsList, LocalDate.now().plusDays(10), "416220502", 0,
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
    public void availablePoolsInCourtLocationCourtUserOwnerNotFoundException() throws NullPointerException {
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
                URI.create("/api/v1/moj/manage-pool/available-pools/415")), AvailablePoolsInCourtLocationDto.class);

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

        verifyAvailablePool(availablePoolsDtoList, serviceStartDate, "415220401", 2,
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
    public void test_reassignJuror_BureauUser_happy() throws Exception {

        final String jurorNumber = "555555553";
        final String bureauOwner = "400";
        final String targetPoolNumber = "416220502";
        List<String> jurorNumbers = List.of(jurorNumber);

        //create a reassign jurors request DTO
        JurorManagementRequestDto requestDto = createJurorManagementRequestDto("415", "415220401",
            "416", "416220502", jurorNumbers, LocalDate.now());

        RequestEntity<?> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/reassign-jurors"));
        ResponseEntity<?> response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        validateReassignedJuror(jurorNumber, "415220401", targetPoolNumber);

        CourtLocation targetCourt = courtLocationRepository.findById("416").orElse(null);
        assertThat(targetCourt).isNotNull();

        List<JurorHistory> historyEvents = jurorHistoryRepository.findByJurorNumber(jurorNumber);
        JurorHistory jurorHistory = historyEvents.stream().filter(hist ->
            hist.getHistoryCode().equals(HistoryCodeMod.REASSIGN_POOL_MEMBER)).findFirst().orElse(null);
        assertThat(jurorHistory).isNotNull();

        String expectedHistoryInfo = "To " + targetPoolNumber + " " + targetCourt.getName();
        assertThat(jurorHistory.getOtherInformation()).isEqualTo(expectedHistoryInfo);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initAvailablePools.sql"})
    public void test_reassignJurorPreviouslyReassigned_BureauUser() throws Exception {

        final String jurorNumber = "555555551"; // this juror was already reassigned from target pool
        List<String> jurorNumbers = List.of(jurorNumber);

        //create a reassign jurors request DTO
        JurorManagementRequestDto requestDto = createJurorManagementRequestDto("415", "415220401",
            "416", "416220502", jurorNumbers, LocalDate.now());

        RequestEntity<?> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/reassign-jurors"));
        ResponseEntity<?> response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        validateReassignedJuror(jurorNumber, "415220401", "416220502");

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initAvailablePools.sql"})
    public void test_reassignMultipleJurors_BureauUser_happy() throws Exception {

        final List<String> jurorNumbers = Arrays.asList("555555551", "555555552", "555555553");

        //create a reassign jurors request DTO
        JurorManagementRequestDto requestDto = createJurorManagementRequestDto("415", "415220401",
            "416", "416220502", jurorNumbers, LocalDate.now());

        RequestEntity<?> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/reassign-jurors"));
        ResponseEntity<ReassignPoolMembersResultDto> response = restTemplate.exchange(requestEntity,
            ReassignPoolMembersResultDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        ReassignPoolMembersResultDto resultDto = response.getBody();

        assertThat(resultDto.getNumberReassigned()).isEqualTo(3);

        for (String jurorNumber : jurorNumbers) {
            validateReassignedJuror(jurorNumber, "415220401", "416220502");
        }

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initAvailablePools.sql"})
    public void test_reassignMultipleJurors_OneInvalid_BureauUser() throws Exception {

        final List<String> jurorNumbers = Arrays.asList("555555551", "555555552", "555555555");
        final List<String> goodJurorNumbers = Arrays.asList("555555551", "555555552");

        //create a reassign jurors request DTO
        JurorManagementRequestDto requestDto = createJurorManagementRequestDto("415", "415220401",
            "416", "416220502", jurorNumbers, LocalDate.now());

        RequestEntity<?> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/reassign-jurors"));
        ResponseEntity<ReassignPoolMembersResultDto> response = restTemplate.exchange(requestEntity,
            ReassignPoolMembersResultDto.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        ReassignPoolMembersResultDto resultDto = response.getBody();

        assertThat(resultDto.getNumberReassigned()).isEqualTo(2);

        for (String jurorNumber : goodJurorNumbers) {
            validateReassignedJuror(jurorNumber, "415220401", "416220502");
        }

    }

    @Test
    public void test_reassignJuror_NoJurors_BureauUser() throws Exception {

        List<String> jurorNumbers = new ArrayList<>();

        //create a reassign jurors request DTO
        JurorManagementRequestDto requestDto = createJurorManagementRequestDto("415", "415220401",
            "416", "416220502", jurorNumbers, LocalDate.now());

        RequestEntity<?> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/reassign-jurors"));
        ResponseEntity<?> response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initAvailablePools.sql"})
    public void test_reassignMultipleJurorsSameCourt_BureauUser_happy() throws Exception {

        final List<String> jurorNumbers = Arrays.asList("555555551", "555555552");

        //create a reassign jurors request DTO
        JurorManagementRequestDto requestDto = createJurorManagementRequestDto("415", "415220401",
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
            validateReassignedJuror(jurorNumber, "415220401", "415220503");
        }
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initAvailablePools.sql"})
    public void test_reassignJurorSameCourtPreviouslyReassigned_BureauUser() throws Exception {

        final String jurorNumber = "555555561"; // this juror was already reassigned from target pool
        List<String> jurorNumbers = List.of(jurorNumber);

        //create a reassign jurors request DTO
        JurorManagementRequestDto requestDto = createJurorManagementRequestDto("416", "416220503",
            "416", "416220502", jurorNumbers, LocalDate.now());

        RequestEntity<?> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/reassign-jurors"));
        ResponseEntity<?> response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        validateReassignedJuror(jurorNumber, "416220503", "416220502");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_initAvailablePools.sql"})
    public void test_reassignJurorSameCourtPreviouslyReassignedAndReassignBackAgain_BureauUser() throws Exception {

        final String jurorNumber = "555555561"; // this juror was already reassigned from target pool
        List<String> jurorNumbers = List.of(jurorNumber);

        //create a reassign jurors request DTO
        JurorManagementRequestDto requestDto = createJurorManagementRequestDto("416", "416220503",
            "416", "416220502", jurorNumbers, LocalDate.now());

        RequestEntity<?> requestEntity = new RequestEntity<>(requestDto, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/reassign-jurors"));
        ResponseEntity<?> response = restTemplate.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        validateReassignedJuror(jurorNumber, "416220503", "416220502");

        //create a reassign jurors request DTO
        JurorManagementRequestDto requestDto2 = createJurorManagementRequestDto("416", "416220502",
            "416", "416220503", jurorNumbers, LocalDate.now());

        RequestEntity<?> requestEntity2 = new RequestEntity<>(requestDto2, httpHeaders,
            HttpMethod.PUT, URI.create("/api/v1/moj/manage-pool/reassign-jurors"));
        ResponseEntity<?> response2 = restTemplate.exchange(requestEntity2, String.class);

        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

        validateReassignedJuror(jurorNumber, "416220502", "416220503");
    }

    private JSONObject getExceptionDetails(ResponseEntity<String> responseEntity) {
        return new JSONObject(responseEntity.getBody());
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_getPoolMonitoringStats.sql"})
    public void test_getPoolMonitoringStats_bureauUser_happyPath() throws Exception {
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
    public void test_getPoolMonitoringStats_courtUser() throws Exception {
        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("415", new ArrayList<>()));

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders,
            HttpMethod.GET, URI.create("/api/v1/moj/manage-pool/summoning-progress/415/CRO"));
        ResponseEntity<SummoningProgressResponseDto> response = restTemplate.exchange(requestEntity,
            SummoningProgressResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/ManagePoolController_getPoolMonitoringStats.sql"})
    public void test_getPoolMonitoringStats_wrongPoolTypeForCourtLocation() throws Exception {
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders,
            HttpMethod.GET, URI.create("/api/v1/moj/manage-pool/summoning-progress/415/CIV"));
        ResponseEntity<SummoningProgressResponseDto> response = restTemplate.exchange(requestEntity,
            SummoningProgressResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private void validateReassignedJuror(String jurorNumber, String sourcePool, String receivingPool) {
        // check the old record is now inactive
        JurorPool oldJurorPool = jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumber("400", jurorNumber,
            sourcePool).get();
        assertThat(oldJurorPool).isNotNull();
        assertThat(oldJurorPool.getStatus().getStatus()).isEqualTo(8L);
        assertThat(oldJurorPool.getNextDate()).isNull();

        // check there is a new record created for reassigned juror
        JurorPool newJurorPool = jurorPoolRepository.findByOwnerAndJurorJurorNumberAndPoolPoolNumber("400", jurorNumber,
            receivingPool).get();
        assertThat(newJurorPool).isNotNull();
        assertThat(newJurorPool.getStatus().getStatus()).isEqualTo(2L);

        assertThat(newJurorPool.getIsActive()).isTrue();

        verifyCopiedFields(oldJurorPool.getJuror(), newJurorPool.getJuror());

        // database should have service start date 10 days in the future
        LocalDate serviceStartDate = LocalDate.now().plusDays(10);
        assertThat(newJurorPool.getNextDate()).isEqualTo(serviceStartDate);

        LetterId letterId = new LetterId("400", jurorNumber);
        // check there is a cert letter queued, should be one only
        Optional<ConfirmationLetter> confirmLetter = confirmationLetterRepository.findById(letterId);

        confirmLetter.ifPresent(letter -> {
            assertThat(letter.getJurorNumber()).isEqualTo(jurorNumber);
            assertThat(letter.getOwner()).isEqualTo("400");
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

    private void validateCertificateOfAttendanceLetter(String owner, String jurorNumber) {
        CertLetter letter = certLetterRepository.findById(new LetterId(owner, jurorNumber)).orElse(null);
        assertThat(letter)
            .as("Certificate of attendance letter should exist for the given juror number and owner")
            .isNotNull();
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
                                                                  LocalDate targetServiceStartDate, int poolTotal) {
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

    private void transferJurorPoolValidateNewlyCreatedJurorPool(String jurorNumber, String sourcePoolNumber,
                                                                String targetPoolNumber, String targetLocCode,
                                                                LocalDate targetStartDate, String currentUser) {
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
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJurorPool.getJurorNumber());
        assertThat(targetJuror.getPollNumber())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getPollNumber());
        assertThat(targetJuror.getTitle())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getTitle());
        assertThat(targetJuror.getFirstName())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getFirstName());
        assertThat(targetJuror.getLastName())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getLastName());
        assertThat(targetJuror.getDateOfBirth().compareTo(sourceJuror.getDateOfBirth()))
            .as("Expect property to be copied from source juror")
            .isEqualTo(0);
        assertThat(targetJuror.getAddressLine1())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getAddressLine1());
        assertThat(targetJuror.getAddressLine2())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getAddressLine2());
        assertThat(targetJuror.getAddressLine3())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getAddressLine3());
        assertThat(targetJuror.getAddressLine4())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getAddressLine4());
        assertThat(targetJuror.getAddressLine5())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getAddressLine5());
        assertThat(targetJuror.getPostcode())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getPostcode());
        assertThat(targetJuror.getPhoneNumber())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getPhoneNumber());
        assertThat(targetJuror.getAltPhoneNumber())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getAltPhoneNumber());
        assertThat(targetJuror.getWorkPhone())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getWorkPhone());
        assertThat(targetJuror.getWorkPhoneExtension())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getWorkPhoneExtension());
        assertThat(targetJurorPool.getDeferralDate())
            .as("Expect property to be copied from source juror")
            .isEqualTo(targetJurorPool.getDeferralDate());
        assertThat(targetJuror.isResponded())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.isResponded());
        assertThat(targetJuror.getExcusalDate())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getExcusalDate());
        assertThat(targetJuror.getExcusalCode())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getExcusalCode());
        assertThat(targetJuror.getExcusalRejected())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getExcusalRejected());
        assertThat(targetJuror.getDisqualifyDate())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getDisqualifyDate());
        assertThat(targetJuror.getDisqualifyCode())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getDisqualifyCode());
        assertThat(targetJuror.getNotes())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getNotes());
        assertThat(targetJurorPool.getNoAttendances())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJurorPool.getNoAttendances());
        assertThat(targetJurorPool.getIsActive())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJurorPool.getIsActive());
        assertThat(targetJuror.getNoDefPos())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getNoDefPos());
        assertThat(targetJuror.getPermanentlyDisqualify())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getPermanentlyDisqualify());
        assertThat(targetJuror.getReasonableAdjustmentCode())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getReasonableAdjustmentCode());
        assertThat(targetJuror.getReasonableAdjustmentMessage())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getReasonableAdjustmentMessage());
        assertThat(targetJuror.getSortCode())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getSortCode());
        assertThat(targetJuror.getBankAccountName())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getBankAccountName());
        assertThat(targetJuror.getBankAccountNumber())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getBankAccountNumber());
        assertThat(targetJuror.getBuildingSocietyRollNumber())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getBuildingSocietyRollNumber());
        assertThat(targetJurorPool.getWasDeferred())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJurorPool.getWasDeferred());
        assertThat(targetJurorPool.getIdChecked())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJurorPool.getIdChecked());
        assertThat(targetJurorPool.getPostpone())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJurorPool.getPostpone());
        assertThat(targetJuror.getWelsh())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getWelsh());
        assertThat(targetJuror.getPoliceCheck())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getPoliceCheck());
        assertThat(targetJuror.getEmail())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getEmail());
        assertThat(targetJuror.getContactPreference())
            .as("Expect property to be copied from source juror")
            .isEqualTo(sourceJuror.getContactPreference());

        assertThat(targetJuror.getOpticRef())
            .as("Expect optic reference to be copied from source juror")
            .isEqualTo(sourceJuror.getOpticRef());

        // default/not copied properties
        assertThat(targetJurorPool.getTimesSelected())
            .as("Expect property to be use a default value")
            .isNull();
        assertThat(targetJurorPool.getLocation())
            .as("Expect property to be use a default value")
            .isNull();
        assertThat(targetJurorPool.getStatus().getStatus())
            .as("Expect property to be use a default value")
            .isEqualTo(2L);
        assertThat(targetJurorPool.getNoAttended())
            .as("Expect property to be use a default value")
            .isNull();
        assertThat(targetJurorPool.getFailedToAttendCount())
            .as("Expect property to be use a default value")
            .isNull();
        assertThat(targetJurorPool.getUnauthorisedAbsenceCount())
            .as("Expect property to be use a default value")
            .isNull();
        assertThat(targetJurorPool.getEditTag())
            .as("Expect property to be use a default value")
            .isNull();
        assertThat(targetJurorPool.getOnCall())
            .as("Expect property to be use a default value")
            .isFalse();
        assertThat(targetJurorPool.getSmartCard())
            .as("Expect property to be use a default value")
            .isNull();
        assertThat(targetJurorPool.getPaidCash())
            .as("Expect property to be use a default value")
            .isNull();
        assertThat(targetJurorPool.getScanCode())
            .as("Expect property to be use a default value")
            .isNull();
        assertThat(targetJuror.getSummonsFile())
            .as("Expect property to be use a default value")
            .isNull();
        assertThat(targetJurorPool.getReminderSent())
            .as("Expect property to be use a default value")
            .isNull();
        assertThat(targetJuror.getNotifications())
            .as("Expect property to be use a default value")
            .isEqualTo(0);
        assertThat(targetJurorPool.getTransferDate())
            .as("Expect property to be use a default value")
            .isNull();
    }

    private void transferJurorPoolValidateExistingTransferredJurorPool(String jurorNumber, String poolNumber) {
        JurorPool jurorPool =
            jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumberAndIsActive(jurorNumber,
                poolNumber, false).get();

        assertThat(jurorPool)
            .as("A juror record should exist in a now inactive and read only state")
            .isNotNull();
        assertThat(jurorPool.getStatus().getStatus())
            .as("Old juror should be set to Transferred (10)")
            .isEqualTo(10L);
    }

}
