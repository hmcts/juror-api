package uk.gov.hmcts.juror.api.moj.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import uk.gov.hmcts.juror.api.moj.controller.request.CoronerPoolAddCitizenRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.CoronerPoolRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.NilPoolRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolAdditionalSummonsDto;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolCreateRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.SummonsFormRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.CoronerPoolItemDto;
import uk.gov.hmcts.juror.api.moj.controller.response.NilPoolResponseDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolCreatedMembersListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestItemDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PostcodesListDto;
import uk.gov.hmcts.juror.api.moj.controller.response.SummonsFormResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.CoronerPool;
import uk.gov.hmcts.juror.api.moj.domain.CoronerPoolDetail;
import uk.gov.hmcts.juror.api.moj.domain.VotersLocPostcodeTotals;
import uk.gov.hmcts.juror.api.moj.repository.BulkPrintDataRepository;
import uk.gov.hmcts.juror.api.moj.repository.ConfirmationLetterRepository;
import uk.gov.hmcts.juror.api.moj.repository.CoronerPoolDetailRepository;
import uk.gov.hmcts.juror.api.moj.repository.CoronerPoolRepository;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the API endpoints defined in CreatePoolController }.
 */
@RunWith(SpringRunner.class)
@SuppressWarnings({"PMD.LawOfDemeter", "PMD.TooManyMethods", "PMD.LinguisticNaming"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CreatePoolControllerITest extends AbstractIntegrationTest {

    @Value("${jwt.secret.bureau}")
    private String bureauSecret;

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private BulkPrintDataRepository bulkPrintDataRepository;
    @Autowired
    private ConfirmationLetterRepository confirmLetterRepository;
    @Autowired
    private CoronerPoolRepository coronerPoolRepository;
    @Autowired
    private CoronerPoolDetailRepository coronerPoolDetailRepository;

    private HttpHeaders httpHeaders;
    @Autowired
    private JurorPoolRepository jurorPoolRepository;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    /* Test to return pool request details where two court deferrals have been used */
    @Test
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_requestPoolDetails.sql"})
    public void getPoolRequestItem_happyPath() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .login("rprice")
            .staff(BureauJWTPayload.Staff.builder().name("Roxanne Price").active(1).rank(1).build())
            .daysToExpire(89)
            .owner("400")
            .build());

        final URI uri = URI.create("/api/v1/moj/pool-create/pool?poolNumber=415220110&owner=400");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<PoolRequestItemDto> response = template.exchange(requestEntity, PoolRequestItemDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();
        PoolRequestItemDto responseBody = response.getBody();

        assertThat(responseBody.getCourtName()).isEqualToIgnoringCase("THE CROWN COURT AT CHESTER");
        assertThat(responseBody.getPoolNumber()).isEqualToIgnoringCase("415220110");
        assertThat(responseBody.getLocCode()).isEqualToIgnoringCase("415");
        assertThat(responseBody.getNoRequested()).isEqualTo(10);
        assertThat(responseBody.getCourtSupplied()).isEqualTo(2);
    }

    @Test
    @Sql(statements = "delete from juror_mod.juror_pool")
    @Sql(statements = "delete from juror_mod.pool")
    public void getPoolRequestItem_poolRequestNotFound() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/pool-create/pool?poolNumber=415220110&owner=400");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<PoolRequestItemDto> response = template.exchange(requestEntity, PoolRequestItemDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_createPoolWithDeferral.sql"})
    public void getBureauDeferrals_deferralFound() throws Exception {

        String locationCode = "415";
        String attendanceDate = "2022-12-04";
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        final URI uri = URI.create("/api/v1/moj/pool-create/bureau-deferrals?locationCode="
            + locationCode + "&deferredTo=" + attendanceDate);

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<Long> response = template.exchange(requestEntity, Long.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
            .as("Expect one valid Bureau deferral for the location and date")
            .isEqualTo(1);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_createPoolWithDeferral.sql"})
    public void getBureauDeferrals_noDeferralFound() throws Exception {

        String locationCode = "419";
        String attendanceDate = "2022-12-09";
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        final URI uri = URI.create("/api/v1/moj/pool-create/bureau-deferrals?locationCode=" + locationCode
            + "&deferredTo=" + attendanceDate);

        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<Long> response = template.exchange(requestEntity, Long.class);

        assertThat(response.getStatusCode())
            .as("Expect the get request to be successful")
            .isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
            .as("Expect no Bureau deferrals for the location and date")
            .isEqualTo(0);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_loadVoters.sql",
        "/db/CreatePoolController_requestPoolDetails.sql"})
    public void getSummaryFormData_happyPath() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .login("rprice")
            .staff(BureauJWTPayload.Staff.builder().name("Roxanne Price").active(1).rank(1).build())
            .daysToExpire(89)
            .owner("400")
            .build());

        final SummonsFormRequestDto summonsFormRequest1 =
            new SummonsFormRequestDto("415220110", "415",
                LocalDateTime.of(2022, 10, 04, 9, 0, 0),
                140, LocalDate.of(2022, 10, 04));


        final URI uri = URI.create("/api/v1/moj/pool-create/summons-form");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<SummonsFormRequestDto> requestEntity = new RequestEntity<>(summonsFormRequest1, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<SummonsFormResponseDto> response = template.exchange(requestEntity,
            SummonsFormResponseDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();
        SummonsFormResponseDto responseBody = response.getBody();

        assertThat(responseBody.getBureauDeferrals()).isEqualTo(1);
        assertThat(responseBody.getNumberRequired()).isEqualTo(139);
        assertThat(responseBody.getCourtCatchmentSummaryItems()).isNotEmpty();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_loadVoters.sql",
        "/db/CreatePoolController_createPool.sql"})
    public void createPool_noBureauDeferrals() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .login("BUREAU_USER")
            .staff(BureauJWTPayload.Staff.builder().name("Bureau User").active(1).rank(1).build())
            .daysToExpire(89)
            .owner("400")
            .build());

        PoolCreateRequestDto poolCreateRequest = setUpPoolCreateRequestDto();

        final URI uri = URI.create("/api/v1/moj/pool-create/create-pool");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<PoolCreateRequestDto> requestEntity = new RequestEntity<>(poolCreateRequest, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @Sql({"/db/mod/truncate.sql",
        "/db/CreatePoolController_createPool.sql",
        "/db/CreatePoolController_loadVotersWithFlags.sql"})
    public void createPool_withDisqualifiedOnSelection() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .login("BUREAU_USER")
            .staff(BureauJWTPayload.Staff.builder().name("Bureau User").active(1).rank(1).build())
            .daysToExpire(89)
            .owner("400")
            .build());

        PoolCreateRequestDto poolCreateRequest = setUpPoolCreateRequestDto();

        final URI uri = URI.create("/api/v1/moj/pool-create/create-pool");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<PoolCreateRequestDto> requestEntity = new RequestEntity<>(poolCreateRequest, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        // now check the pool members contain disqualified jurors, when summoning 8 jurors, we will have 10 jurors
        // returned
        // two of the jurors will be disqualified on selection

        final URI uri2 = URI.create("/api/v1/moj/pool-create/members?poolNumber=415221201");

        RequestEntity<Void> requestEntity2 = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri2);
        ResponseEntity<PoolCreatedMembersListDto> response2 = template.exchange(requestEntity2,
            PoolCreatedMembersListDto.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response2.getBody()).isNotNull();
        List<PoolCreatedMembersListDto.JurorPoolDataDto> jurorPoolDataDto = response2.getBody().getData();
        assertThat(jurorPoolDataDto.size())
            .as("Expect there to be 10 jurors returned")
            .isEqualTo(10);

        int disqCount = jurorPoolDataDto.stream().mapToInt(juror ->
            juror.getStatus().equals("Disqualified") ? 1 : 0).sum();

        assertThat(disqCount).as("Expect there to be 2 disqualified jurors").isEqualTo(2);

    }

    @Test
    @Sql(statements = "DELETE FROM JUROR_MOD.BULK_PRINT_DATA")
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_loadVoters.sql",
        "/db/CreatePoolController_createPoolWithDeferral.sql"})
    public void createPool_withBureauDeferral() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .login("BUREAU_USER")
            .staff(BureauJWTPayload.Staff.builder().name("Bureau User").active(1).rank(1).build())
            .daysToExpire(89)
            .owner("400")
            .build());

        PoolCreateRequestDto poolCreateRequest = setUpPoolCreateRequestDto();
        //Update Bureau deferrals to use as there is one available
        poolCreateRequest.setBureauDeferrals(1);

        final URI uri = URI.create("/api/v1/moj/pool-create/create-pool");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<PoolCreateRequestDto> requestEntity = new RequestEntity<>(poolCreateRequest, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        // check that the deferred user has been added to print table
        long deferredJurorLetters = bulkPrintDataRepository.countByJurorNo("641500005");
        assertThat(deferredJurorLetters).isEqualTo(1);
        long letterCount = bulkPrintDataRepository.count();
        assertThat(letterCount).isEqualTo(17);
    }

    @Test
    @Sql(statements = "DELETE FROM JUROR_MOD.BULK_PRINT_DATA")
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_loadVoters.sql",
        "/db/CreatePoolController_createPoolWithDeferral.sql"})
    public void summonAdditionalCitizens() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .login("BUREAU_USER")
            .staff(BureauJWTPayload.Staff.builder().name("Bureau User").active(1).rank(1).build())
            .daysToExpire(89)
            .owner("400")
            .build());

        PoolCreateRequestDto poolCreateRequest = setUpPoolCreateRequestDto();
        //Update Bureau deferrals to use as there is one available
        poolCreateRequest.setBureauDeferrals(1);

        final URI uri = URI.create("/api/v1/moj/pool-create/create-pool");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<PoolCreateRequestDto> requestEntity = new RequestEntity<>(poolCreateRequest, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        PoolAdditionalSummonsDto poolAdditionalSummons = setUpPoolAdditionalSummonsDto();
        poolAdditionalSummons.setBureauDeferrals(0);

        final URI uri2 = URI.create("/api/v1/moj/pool-create/additional-summons");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<PoolAdditionalSummonsDto> requestEntity2 = new RequestEntity<>(poolAdditionalSummons, httpHeaders,
            HttpMethod.POST, uri2);
        ResponseEntity<String> response2 = template.exchange(requestEntity2, String.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        long letterCount = bulkPrintDataRepository.count();
        assertThat(letterCount).isEqualTo(19);
    }

    @Test
    public void summonAdditionalCitizens_courtUser() throws Exception {
        final String courtJwt = createBureauJwt("COURT_USER", "415");

        PoolAdditionalSummonsDto poolAdditionalSummons = setUpPoolAdditionalSummonsDto();

        final URI uri = URI.create("/api/v1/moj/pool-create/additional-summons");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, courtJwt);
        RequestEntity<PoolAdditionalSummonsDto> requestEntity = new RequestEntity<>(poolAdditionalSummons, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<String> response2 = template.exchange(requestEntity, String.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql(statements = "DELETE FROM JUROR_MOD.BULK_PRINT_DATA")
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_loadActivePool_and_loadSingleVoter.sql"})
    public void test_summonAdditionalCitizens_singleRecord() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");

        PoolAdditionalSummonsDto poolAdditionalSummons = setUpPoolAdditionalSummonsDto();

        poolAdditionalSummons.setCitizensToSummon(1);
        poolAdditionalSummons.setBureauDeferrals(0);

        final URI uri = URI.create("/api/v1/moj/pool-create/additional-summons");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<PoolAdditionalSummonsDto> requestEntity = new RequestEntity<>(poolAdditionalSummons, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<String> response2 = template.exchange(requestEntity, String.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        long letterCount = bulkPrintDataRepository.count();
        assertThat(letterCount).isEqualTo(1);
    }


    @Test
    @Sql(statements = "DELETE FROM JUROR_MOD.BULK_PRINT_DATA")
    @Sql(statements = "UPDATE JUROR.COURT_LOCATION SET VOTERS_LOCK = 0")
    @Sql({"/db/mod/truncate.sql",
        "/db/CreatePoolController_loadVoters.sql",
        "/db/CreatePoolController_createPoolWithDeferral.sql"})
    public void summonAdditionalCitizens_tooManyCitizens() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .login("BUREAU_USER")
            .staff(BureauJWTPayload.Staff.builder().name("Bureau User").active(1).rank(1).build())
            .daysToExpire(89)
            .owner("400")
            .build());

        PoolCreateRequestDto poolCreateRequest = setUpPoolCreateRequestDto();

        final URI uri = URI.create("/api/v1/moj/pool-create/create-pool");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<PoolCreateRequestDto> requestEntity = new RequestEntity<>(poolCreateRequest, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        PoolAdditionalSummonsDto poolAdditionalSummons = setUpPoolAdditionalSummonsDto();

        final URI uri2 = URI.create("/api/v1/moj/pool-create/additional-summons");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<PoolAdditionalSummonsDto> requestEntity2 = new RequestEntity<>(poolAdditionalSummons, httpHeaders,
            HttpMethod.POST, uri2);
        ResponseEntity<String> response2 = template.exchange(requestEntity2, String.class);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        long letterCount = bulkPrintDataRepository.count();
        assertThat(letterCount).isEqualTo(19);

        PoolAdditionalSummonsDto poolAdditionalSummons2 = setUpPoolAdditionalSummonsDto();
        poolAdditionalSummons2.setCitizensSummoned(10);
        /* update the citizens to summon to make it greater than yield for Chester (2.5 * number requested)
         * Already summoned 10 citizens, maximum allowed is 2.5 * 5 = 12.5 (12 citizens).
         */
        poolAdditionalSummons2.setCitizensToSummon(5);

        final URI uri3 = URI.create("/api/v1/moj/pool-create/additional-summons");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<PoolAdditionalSummonsDto> requestEntity3 = new RequestEntity<>(poolAdditionalSummons2,
            httpHeaders,
            HttpMethod.POST, uri3);
        ResponseEntity<String> response3 = template.exchange(requestEntity3, String.class);
        assertThat(response3.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    }

    private PoolAdditionalSummonsDto setUpPoolAdditionalSummonsDto() {
        PoolAdditionalSummonsDto poolAdditionalSummonsDto = new PoolAdditionalSummonsDto();
        poolAdditionalSummonsDto.setPoolNumber("415221201");
        poolAdditionalSummonsDto.setNoRequested(5);
        poolAdditionalSummonsDto.setCitizensSummoned(8);
        poolAdditionalSummonsDto.setCitizensToSummon(2);
        poolAdditionalSummonsDto.setCatchmentArea("415");
        poolAdditionalSummonsDto.setBureauDeferrals(1);
        List<String> postcodes = new ArrayList<>();
        postcodes.add("CH1");
        postcodes.add("CH2");
        postcodes.add("CH3");
        poolAdditionalSummonsDto.setPostcodes(postcodes);

        return poolAdditionalSummonsDto;
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_loadVoters.sql",
        "/db/CreatePoolController_requestPoolDetails.sql"})
    public void createPool_tooManyCitizens() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .login("BUREAU_USER")
            .staff(BureauJWTPayload.Staff.builder().name("Bureau User").active(1).rank(1).build())
            .daysToExpire(89)
            .owner("400")
            .build());

        PoolCreateRequestDto poolCreateRequest = setUpPoolCreateRequestDto();
        // update the citizens to summon a high number, above yield
        poolCreateRequest.setCitizensToSummon(1000);

        final URI uri = URI.create("/api/v1/moj/pool-create/create-pool");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<PoolCreateRequestDto> requestEntity = new RequestEntity<>(poolCreateRequest, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private PoolCreateRequestDto setUpPoolCreateRequestDto() {
        PoolCreateRequestDto poolCreateRequestDto = new PoolCreateRequestDto();
        poolCreateRequestDto.setPoolNumber("415221201");
        poolCreateRequestDto.setStartDate(LocalDate.of(2022, 12, 4));
        poolCreateRequestDto.setAttendTime(LocalDateTime.of(2022, 12, 04, 9, 0, 0));
        poolCreateRequestDto.setNoRequested(5);
        poolCreateRequestDto.setBureauDeferrals(0);
        poolCreateRequestDto.setNumberRequired(4);
        poolCreateRequestDto.setCitizensToSummon(8);
        poolCreateRequestDto.setCatchmentArea("415");
        List<String> postcodes = new ArrayList<>();
        postcodes.add("CH1");
        postcodes.add("CH2");
        postcodes.add("CH3");
        poolCreateRequestDto.setPostcodes(postcodes);

        return poolCreateRequestDto;
    }


    /* A Court user should not be able to create a pool */
    @Test
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_loadVoters.sql",
        "/db/CreatePoolController_requestPoolDetails.sql"})
    public void createPool_invalidUserType() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .login("COURT_USER")
            .staff(BureauJWTPayload.Staff.builder().name("Court User").active(1).rank(1).build())
            .daysToExpire(89)
            .owner("774")
            .build());

        final PoolCreateRequestDto poolCreateRequest =
            new PoolCreateRequestDto();

        final URI uri = URI.create("/api/v1/moj/pool-create/create-pool");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<PoolCreateRequestDto> requestEntity = new RequestEntity<>(poolCreateRequest, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<String> response = template.exchange(requestEntity, String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_getPoolMemberList.sql"})
    public void getPoolMembers_bureauUser_bureauOwnedPool() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .login("BUREAU_USER")
            .staff(BureauJWTPayload.Staff.builder().name("Bureau User").active(1).rank(1).build())
            .daysToExpire(89)
            .owner("400")
            .build());

        final URI uri = URI.create("/api/v1/moj/pool-create/members?poolNumber=415230101");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<PoolCreatedMembersListDto> response = template.exchange(requestEntity,
            PoolCreatedMembersListDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();
        List<PoolCreatedMembersListDto.JurorPoolDataDto> jurorPoolDataDto = response.getBody().getData();
        assertThat(jurorPoolDataDto.size()).isEqualTo(1);

        PoolCreatedMembersListDto.JurorPoolDataDto responseData = jurorPoolDataDto.get(0);
        assertThat(responseData.getJurorNumber()).isEqualToIgnoringCase("111111111");
        assertThat(responseData.getFirstName()).isEqualToIgnoringCase("TEST");
        assertThat(responseData.getLastName()).isEqualToIgnoringCase("ONE");
        assertThat(responseData.getPostcode()).isEqualToIgnoringCase("CH1 2AN");
        assertThat(responseData.getStatus()).isEqualTo("Summoned");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_getPoolMemberList.sql"})
    public void getPoolMembers_bureauUser_courtOwnedPool() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .login("BUREAU_USER")
            .staff(BureauJWTPayload.Staff.builder().name("Bureau User").active(1).rank(1).build())
            .daysToExpire(89)
            .owner("400")
            .build());

        final URI uri = URI.create("/api/v1/moj/pool-create/members?poolNumber=415230102");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<PoolCreatedMembersListDto> response = template.exchange(requestEntity,
            PoolCreatedMembersListDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();
        List<PoolCreatedMembersListDto.JurorPoolDataDto> jurorPoolDataDto = response.getBody().getData();
        assertThat(jurorPoolDataDto.isEmpty()).isTrue();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_getPoolMemberList.sql"})
    public void getPoolMembers_courtUser_bureauOwnedPool() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .login("COURT_USER")
            .staff(BureauJWTPayload.Staff.builder().name("Court User").active(1).rank(1).build())
            .daysToExpire(89)
            .owner("415")
            .build());

        final URI uri = URI.create("/api/v1/moj/pool-create/members?poolNumber=415230101");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<PoolCreatedMembersListDto> response = template.exchange(requestEntity,
            PoolCreatedMembersListDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();
        List<PoolCreatedMembersListDto.JurorPoolDataDto> jurorPoolDataDto = response.getBody().getData();
        assertThat(jurorPoolDataDto.size()).isEqualTo(1);

        PoolCreatedMembersListDto.JurorPoolDataDto responseData = jurorPoolDataDto.get(0);
        assertThat(responseData.getJurorNumber()).isEqualToIgnoringCase("333333333");
        assertThat(responseData.getFirstName()).isEqualToIgnoringCase("TEST");
        assertThat(responseData.getLastName()).isEqualToIgnoringCase("THREE");
        assertThat(responseData.getPostcode()).isEqualToIgnoringCase("CH1 2AN");
        assertThat(responseData.getStatus()).isEqualTo("Summoned");
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_getPoolMemberList.sql"})
    public void getPoolMembers_noPoolMembers() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .login("COURT_USER")
            .staff(BureauJWTPayload.Staff.builder().name("Court User").active(1).rank(1).build())
            .daysToExpire(89)
            .owner("415")
            .build());

        final URI uri = URI.create("/api/v1/moj/pool-create/members?poolNumber=415230102");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<PoolCreatedMembersListDto> response = template.exchange(requestEntity,
            PoolCreatedMembersListDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_loadVoters.sql"})
    public void getCourtCatchmentItems_happyPath() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/pool-create/postcodes?areaCode=415");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);

        ResponseEntity<PostcodesListDto> response = template.exchange(requestEntity, PostcodesListDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        List<VotersLocPostcodeTotals.CourtCatchmentSummaryItem> courtCatchmentSummaryItems =
            response.getBody().getCourtCatchmentSummaryItems();

        assertThat(courtCatchmentSummaryItems.size())
            .as("Expect 3 court catchment postcode area summary items to be returned")
            .isEqualTo(3);
        assertThat(courtCatchmentSummaryItems.stream().anyMatch(item ->
            "CH1".equals(item.getPostCodePart()) && item.getTotal() == 10))
            .as("3 of 13 voter records have already been selected, expect 10 to be remaining")
            .isTrue();
        assertThat(courtCatchmentSummaryItems.stream().anyMatch(item ->
            "CH2".equals(item.getPostCodePart()) && item.getTotal() == 4))
            .as("1 of 5 voter records have already been selected, expect 4 to be remaining")
            .isTrue();
        assertThat(courtCatchmentSummaryItems.stream().anyMatch(item ->
            "CH3".equals(item.getPostCodePart()) && item.getTotal() == 4))
            .as("1 of 5 voter records have already been selected, expect 4 to be remaining")
            .isTrue();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_loadVotersForCoroners.sql"})
    public void getCourtCatchmentItems_Coroners_NormalPool() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/pool-create/postcodes?areaCode=415");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);

        ResponseEntity<PostcodesListDto> response = template.exchange(requestEntity, PostcodesListDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        List<VotersLocPostcodeTotals.CourtCatchmentSummaryItem> courtCatchmentSummaryItems = response.getBody()
            .getCourtCatchmentSummaryItems();

        assertThat(courtCatchmentSummaryItems.size())
            .as("Expect 2 court catchment postcode area summary items to be returned")
            .isEqualTo(2);
        assertThat(courtCatchmentSummaryItems.stream().anyMatch(item ->
            "CH1".equals(item.getPostCodePart()) && item.getTotal() == 8))
            .as("expect 8 to be remaining as FLAGS are ignored for normal pools and one already selected")
            .isTrue();
        assertThat(courtCatchmentSummaryItems.stream().anyMatch(item ->
            "CH2".equals(item.getPostCodePart()) && item.getTotal() == 2))
            .as("expect 2 to be remaining as FLAGS are ignored for normal pools and one already selected")
            .isTrue();

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_loadVotersForCoroners.sql"})
    public void getCourtCatchmentItems_Coroners_happyPath() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/pool-create/postcodes?areaCode=415&isCoronersPool=true");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);

        ResponseEntity<PostcodesListDto> response = template.exchange(requestEntity, PostcodesListDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        List<VotersLocPostcodeTotals.CourtCatchmentSummaryItem> courtCatchmentSummaryItems = response.getBody()
            .getCourtCatchmentSummaryItems();

        assertThat(courtCatchmentSummaryItems.size())
            .as("Expect 2 court catchment postcode area summary items to be returned")
            .isEqualTo(2);
        assertThat(courtCatchmentSummaryItems.stream().anyMatch(item ->
            "CH1".equals(item.getPostCodePart()) && item.getTotal() == 7))
            .as("expect 7 to be remaining as 1 has FLAGS='Y' and one already selected")
            .isTrue();
        assertThat(courtCatchmentSummaryItems.stream().anyMatch(item ->
            "CH2".equals(item.getPostCodePart()) && item.getTotal() == 1))
            .as("expect 1 to be remaining as 1 has FLAGS='Y' and one already selected")
            .isTrue();
    }

    @Test
    @Sql(statements = "DELETE FROM JUROR_MOD.VOTERS")
    public void getCourtCatchmentItems_noCatchmentAreaItems() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/pool-create/postcodes?areaCode=415");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);

        ResponseEntity<PostcodesListDto> response = template.exchange(requestEntity, PostcodesListDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void getCourtCatchmentItems_invalidLocationCode() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/pool-create/postcodes?areaCode=100");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);

        ResponseEntity<PostcodesListDto> response = template.exchange(requestEntity, PostcodesListDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_initDeferrals.sql"})
    public void test_checkDeferralsForNilPool_courtUser() throws Exception {

        final NilPoolRequestDto nilPoolCheckRequest = setUpNilPoolCheckRequestDto();

        final URI uri = URI.create("/api/v1/moj/pool-create/nil-pool-check");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416")));
        RequestEntity<NilPoolRequestDto> requestEntity = new RequestEntity<>(nilPoolCheckRequest, httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<NilPoolResponseDto> response = template.exchange(requestEntity, NilPoolResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be successful")
            .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody().getDeferrals())
            .as("Expect there to be one deferral on this date (3/10/2022)")
            .isEqualTo(1);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_initDeferrals.sql"})
    public void test_checkNoDeferralsForNilPool_courtUser() throws Exception {

        final NilPoolRequestDto nilPoolCheckRequest = setUpNilPoolCheckRequestDto();
        //update the attendance date to not have any deferrals
        nilPoolCheckRequest.setAttendanceDate(LocalDate.of(2022, 12, 3));
        final URI uri = URI.create("/api/v1/moj/pool-create/nil-pool-check");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416")));
        RequestEntity<NilPoolRequestDto> requestEntity = new RequestEntity<>(nilPoolCheckRequest, httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<NilPoolResponseDto> response = template.exchange(requestEntity, NilPoolResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be successful")
            .isEqualTo(HttpStatus.OK);

        assertThat(response.getBody().getPoolNumber())
            .as("Expect there to be a pool number returned for creation")
            .isNotEmpty();
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_initDeferrals.sql"})
    public void test_checkDeferralsForNilPool_BadRequest_courtUser() throws Exception {

        final NilPoolRequestDto nilPoolCheckRequest = setUpNilPoolCheckRequestDto();
        //both location code and name should not be null
        nilPoolCheckRequest.setLocationName(null);
        nilPoolCheckRequest.setLocationCode(null);

        final URI uri = URI.create("/api/v1/moj/pool-create/nil-pool-check");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416")));
        RequestEntity<NilPoolRequestDto> requestEntity = new RequestEntity<>(nilPoolCheckRequest, httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<NilPoolResponseDto> response = template.exchange(requestEntity, NilPoolResponseDto.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be a bad request")
            .isEqualTo(HttpStatus.BAD_REQUEST);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/RequestPoolController_initDeferrals.sql"})
    public void test_checkDeferralsForNilPool_BureauUser() throws Exception {

        final NilPoolRequestDto nilPoolCheckRequest = setUpNilPoolCheckRequestDto();

        final URI uri = URI.create("/api/v1/moj/pool-create/nil-pool-check");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, createBureauJwt("BUREAU_USER", "400"));
        RequestEntity<NilPoolRequestDto> requestEntity = new RequestEntity<>(nilPoolCheckRequest, httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<String> response = template.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be forbidden")
            .isEqualTo(HttpStatus.FORBIDDEN);

    }

    private NilPoolRequestDto setUpNilPoolCheckRequestDto() {
        NilPoolRequestDto nilPoolRequestDto = new NilPoolRequestDto();
        nilPoolRequestDto.setLocationCode("416");
        nilPoolRequestDto.setPoolType("CRO");  // crown court
        nilPoolRequestDto.setAttendanceDate(LocalDate.of(2022, 10, 3));
        nilPoolRequestDto.setAttendanceTime(LocalTime.of(9, 30));

        return nilPoolRequestDto;
    }


    @Test
    @Sql(statements = "delete from juror_mod.pool_comments")
    @Sql(statements = "delete from juror_mod.pool_history")
    @Sql(statements = "delete from juror_mod.juror_pool")
    @Sql(statements = "delete from juror_mod.pool")
    public void test_createNilPool_CourtUser() throws Exception {

        final NilPoolRequestDto nilPoolCheckRequest = setUpNilPoolCreateRequestDto();

        final URI uri = URI.create("/api/v1/moj/pool-create/nil-pool-create");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416")));
        RequestEntity<NilPoolRequestDto> requestEntity = new RequestEntity<>(nilPoolCheckRequest, httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<String> response = template.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be successful")
            .isEqualTo(HttpStatus.CREATED);

    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_convertNilPool.sql"})
    public void test_convertNilPool_BureauUser() throws Exception {

        final NilPoolRequestDto nilPoolCheckRequest = setUpNilPoolCreateRequestDto();

        final URI uri = URI.create("/api/v1/moj/pool-create/nil-pool-check");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416")));
        RequestEntity<NilPoolRequestDto> requestEntity = new RequestEntity<>(nilPoolCheckRequest, httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<String> response = template.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be successful")
            .isEqualTo(HttpStatus.OK);

        final PoolRequestDto poolRequestDto = createPoolRequestDto();

        final URI uri2 = URI.create("/api/v1/moj/pool-create/nil-pool-convert");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, createBureauJwt("BUREAU_USER", "400"));
        RequestEntity<PoolRequestDto> requestEntity2 = new RequestEntity<>(poolRequestDto, httpHeaders,
            HttpMethod.PUT, uri2);

        ResponseEntity<String> response2 = template.exchange(requestEntity2, String.class);

        assertThat(response2.getStatusCode())
            .as("Expect the HTTP POST request to be successful")
            .isEqualTo(HttpStatus.OK);

    }

    @Test
    public void test_convertNilPool_CourtUser() throws Exception {

        final PoolRequestDto poolRequestDto = createPoolRequestDto();

        final URI uri = URI.create("/api/v1/moj/pool-create/nil-pool-convert");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416")));
        RequestEntity<PoolRequestDto> requestEntity = new RequestEntity<>(poolRequestDto, httpHeaders,
            HttpMethod.PUT, uri);

        ResponseEntity<String> response = template.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be forbidden")
            .isEqualTo(HttpStatus.FORBIDDEN);

    }

    private PoolRequestDto createPoolRequestDto() {
        PoolRequestDto poolRequestDto = new PoolRequestDto();
        poolRequestDto.setPoolNumber("416221201");
        poolRequestDto.setLocationCode("416");
        poolRequestDto.setAttendanceDate(LocalDate.of(2022, 12, 3));
        poolRequestDto.setNumberRequested(10);
        poolRequestDto.setPoolType("CRO");
        poolRequestDto.setAttendanceTime(LocalTime.of(9, 30));
        poolRequestDto.setDeferralsUsed(1);
        return poolRequestDto;

    }

    @Test
    public void test_createNilPool_BureauUser() throws Exception {

        final NilPoolRequestDto nilPoolCheckRequest = setUpNilPoolCreateRequestDto();

        final URI uri = URI.create("/api/v1/moj/pool-create/nil-pool-create");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, createBureauJwt("BUREAU_USER", "400"));
        RequestEntity<NilPoolRequestDto> requestEntity = new RequestEntity<>(nilPoolCheckRequest, httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<String> response = template.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be forbidden")
            .isEqualTo(HttpStatus.FORBIDDEN);

    }

    private NilPoolRequestDto setUpNilPoolCreateRequestDto() {
        NilPoolRequestDto nilPoolRequestDto = new NilPoolRequestDto();
        nilPoolRequestDto.setLocationName("CHICHESTER");
        nilPoolRequestDto.setPoolType("CRO");  // crown court
        nilPoolRequestDto.setAttendanceDate(LocalDate.of(2022, 12, 3));
        nilPoolRequestDto.setAttendanceTime(LocalTime.of(9, 30));
        nilPoolRequestDto.setLocationCode("416");
        nilPoolRequestDto.setPoolNumber("416221201");

        return nilPoolRequestDto;
    }

    @Sql(statements = "DELETE FROM JUROR_MOD.CORONER_POOL_DETAIL")
    @Sql(statements = "DELETE FROM JUROR_MOD.CORONER_POOL")
    @Test
    public void test_createCoronerPool_BureauUser() throws Exception {

        final CoronerPoolRequestDto coronerPoolRequestDto = setUpCoronerPoolCreateRequestDto();

        final URI uri = URI.create("/api/v1/moj/pool-create/create-coroner-pool");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, createBureauJwt("BUREAU_USER", "400"));
        RequestEntity<CoronerPoolRequestDto> requestEntity = new RequestEntity<>(coronerPoolRequestDto, httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<String> response = template.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be created")
            .isEqualTo(HttpStatus.CREATED);

        List<CoronerPool> coronerPools = coronerPoolRepository.findAll();
        assertThat(coronerPools.size()).as("Expect there to be 1 record in database")
            .isEqualTo(1);
        CoronerPool coronerPool = coronerPools.get(0);


        confirmCoronerPoolDataSavedSuccessfully(coronerPoolRequestDto, coronerPool);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_addCitizensForCoronerPool.sql"})
    public void addCitizensToCoronerPool_happy() throws Exception {

        // requested 100 citizens, adding 30
        final CoronerPoolAddCitizenRequestDto addCitizenRequestDto = setupCoronerPoolAddCitizensDto("923040001",
            12, 18);

        final URI uri = URI.create("/api/v1/moj/pool-create/add-citizens");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, createBureauJwt("BUREAU_USER", "400"));
        RequestEntity<CoronerPoolAddCitizenRequestDto> requestEntity = new RequestEntity<>(addCitizenRequestDto,
            httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<String> response = template.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be created")
            .isEqualTo(HttpStatus.CREATED);

        List<CoronerPoolDetail> coronerPoolDetailList = coronerPoolDetailRepository.findAllByPoolNumber("923040001");

        assertThat(coronerPoolDetailList.size())
            .as("Expect the coroner details table to have 30 entries")
            .isEqualTo(30);

    }


    @Test
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_addCitizensForCoronerPool.sql"})
    public void addCitizensToCoronerPool_tooManyCitizens() throws Exception {

        // total goes over 250
        final CoronerPoolAddCitizenRequestDto addCitizenRequestDto = setupCoronerPoolAddCitizensDto("923040002",
            200, 51);

        final URI uri = URI.create("/api/v1/moj/pool-create/add-citizens");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, createBureauJwt("BUREAU_USER", "400"));
        RequestEntity<CoronerPoolAddCitizenRequestDto> requestEntity = new RequestEntity<>(addCitizenRequestDto,
            httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<String> response = template.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP Response to be BAD_REQUEST")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_addMoreCitizensThanRequestedForCoronerPool.sql"})
    public void addCitizensToCoronerPool_addMoreCitizensThanRequested() throws Exception {

        // 30 requested, 12 already added and asking for 20 more
        final CoronerPoolAddCitizenRequestDto addCitizenRequestDto = setupCoronerPoolAddCitizensDto("923040002",
            10, 10);

        final URI uri = URI.create("/api/v1/moj/pool-create/add-citizens");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, createBureauJwt("BUREAU_USER", "400"));
        RequestEntity<CoronerPoolAddCitizenRequestDto> requestEntity = new RequestEntity<>(addCitizenRequestDto,
            httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<String> response = template.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP Response to be CREATED")
            .isEqualTo(HttpStatus.CREATED);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_addMoreCitizensThanRequestedForCoronerPool.sql"})
    public void addCitizensToCoronerPool_addMoreCitizensThanAllowed() throws Exception {

        // 30 requested, 12 already added and asking for 240 more (> 250 limit)
        final CoronerPoolAddCitizenRequestDto addCitizenRequestDto = setupCoronerPoolAddCitizensDto("923040002",
            120, 120);

        final URI uri = URI.create("/api/v1/moj/pool-create/add-citizens");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, createBureauJwt("BUREAU_USER", "400"));
        RequestEntity<CoronerPoolAddCitizenRequestDto> requestEntity = new RequestEntity<>(addCitizenRequestDto,
            httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<String> response = template.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP Response to be BAD_REQUEST")
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_createCoronerPool.sql"})
    public void getCoronerPoolDetails_happy() throws Exception {
        final String bureauJwt = createBureauJwt("BUREAU_USER", "400");
        final URI uri = URI.create("/api/v1/moj/pool-create/coroner-pool?poolNumber=923040001");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<CoronerPoolItemDto> response = template.exchange(requestEntity, CoronerPoolItemDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        CoronerPoolItemDto coronerPoolItemDto = response.getBody();

        confirmCoronerPoolRecordAsExpected(coronerPoolItemDto);
    }

    @Test
    public void test_createCoronerPool_courtUser() throws Exception {

        final CoronerPoolRequestDto coronerPoolRequestDto = setUpCoronerPoolCreateRequestDto();

        final URI uri = URI.create("/api/v1/moj/pool-create/create-coroner-pool");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416")));
        RequestEntity<CoronerPoolRequestDto> requestEntity = new RequestEntity<>(coronerPoolRequestDto, httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<String> response = template.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be forbidden")
            .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void getCoronerPoolDetails_courtUser() throws Exception {

        final URI uri = URI.create("/api/v1/moj/pool-create/coroner-pool?poolNumber=923040001");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416")));
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<CoronerPoolItemDto> response = template.exchange(requestEntity, CoronerPoolItemDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    public void addCitizensToCoronerPool_courtUser() throws Exception {

        final CoronerPoolAddCitizenRequestDto addCitizenRequestDto = setupCoronerPoolAddCitizensDto("923040001",
            10, 10);

        final URI uri = URI.create("/api/v1/moj/pool-create/add-citizens");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, initCourtsJwt("416", Collections.singletonList("416")));
        RequestEntity<CoronerPoolAddCitizenRequestDto> requestEntity = new RequestEntity<>(addCitizenRequestDto,
            httpHeaders,
            HttpMethod.POST, uri);

        ResponseEntity<String> response = template.exchange(requestEntity, String.class);

        assertThat(response.getStatusCode())
            .as("Expect the HTTP POST request to be forbidden")
            .isEqualTo(HttpStatus.FORBIDDEN);

    }

    private void confirmCoronerPoolRecordAsExpected(CoronerPoolItemDto coronerPoolItemDto) {
        assertThat(coronerPoolItemDto.getPoolNumber()).as("Expect Coroner pool number to be 923040001")
            .isEqualTo("923040001");
        assertThat(coronerPoolItemDto.getLocCode()).as("Expect Coroner pool locCode to be 415")
            .isEqualTo("415");
        assertThat(coronerPoolItemDto.getCourtName()).as("Expect Coroner pool court name to be CHESTER")
            .isEqualTo("CHESTER");
        assertThat(coronerPoolItemDto.getNoRequested()).as("Expect Coroner pool requested number to be 100")
            .isEqualTo(100);
        assertThat(coronerPoolItemDto.getName()).as("Expect Coroner pool requester name to be Coroners Name")
            .isEqualTo("Coroners Name");
        assertThat(coronerPoolItemDto.getEmailAddress())
            .as("Expect Coroner pool requester email to be emailof@coroner.gov.uk")
            .isEqualTo("emailof@coroner.gov.uk");
        assertThat(coronerPoolItemDto.getPhone()).as("Expect Coroner pool requester phone to be 0207 12341234")
            .isEqualTo("0207 12341234");
        assertThat(coronerPoolItemDto.getCoronerDetailsList().size())
            .as("Expect Coroner pool details list to contain 10 items")
            .isEqualTo(10);

        CoronerPoolItemDto.CoronerDetails coronerDetails =
            coronerPoolItemDto.getCoronerDetailsList().stream().filter(coroner ->
                coroner.getFirstName().equals("FNAMEEIGHT")
            ).findAny().get();
        assertThat(coronerDetails.getTitle()).as("The juror's title")
            .isNull();
        assertThat(coronerDetails.getFirstName()).as("The juror's first name")
            .isEqualTo("FNAMEEIGHT");
        assertThat(coronerDetails.getLastName()).as("The juror's last name")
            .isEqualTo("LNAMEEIGHT");
        assertThat(coronerDetails.getJurorNumber()).as("The juror's juror number")
            .isEqualTo("641500008");
        assertThat(coronerDetails.getAddressLineOne()).as("The juror's address line number one")
            .isEqualTo("8 STREET NAME");
        assertThat(coronerDetails.getAddressLineTwo()).as("The juror's address line number two")
            .isEqualTo("ANYTOWN");
        assertThat(coronerDetails.getAddressLineThree()).as("The juror's address line number three")
            .isEqualTo("ANOTHER LINE 3");
        assertThat(coronerDetails.getAddressLineFour()).as("The juror's address line number four")
            .isEqualTo("ANOTHER LINE 4");
        assertThat(coronerDetails.getAddressLineFive()).as("The juror's address line number five")
            .isEqualTo("ANOTHER LINE 5");
        assertThat(coronerDetails.getPostcode()).as("The juror's postcode")
            .isEqualTo("CH1 2AN");
    }

    private CoronerPoolAddCitizenRequestDto setupCoronerPoolAddCitizensDto(String poolNumber, int firstPostCode,
                                                                           int secondPostcode) {

        CoronerPoolAddCitizenRequestDto addCitizenRequestDto = new CoronerPoolAddCitizenRequestDto();

        addCitizenRequestDto.setPoolNumber(poolNumber);
        addCitizenRequestDto.setLocCode("415");

        List<CoronerPoolAddCitizenRequestDto.PostCodeAndNumbers> postCodeAndNumbersList = new ArrayList<>();
        CoronerPoolAddCitizenRequestDto.PostCodeAndNumbers postCodeAndNumbers =
            new CoronerPoolAddCitizenRequestDto.PostCodeAndNumbers("CH1", firstPostCode);
        CoronerPoolAddCitizenRequestDto.PostCodeAndNumbers postCodeAndNumbers2 =
            new CoronerPoolAddCitizenRequestDto.PostCodeAndNumbers("CH4", secondPostcode);
        postCodeAndNumbersList.add(postCodeAndNumbers);
        postCodeAndNumbersList.add(postCodeAndNumbers2);
        addCitizenRequestDto.setPostcodeAndNumbers(postCodeAndNumbersList);

        return addCitizenRequestDto;
    }

    private void confirmCoronerPoolDataSavedSuccessfully(CoronerPoolRequestDto coronerPoolRequestDto,
                                                         CoronerPool coronerPool) {

        assertThat(coronerPoolRequestDto.getNoRequested())
            .isEqualTo(coronerPool.getNumberRequested());

        assertThat(coronerPoolRequestDto.getLocationCode())
            .isEqualTo(coronerPool.getCourtLocation().getLocCode());

        assertThat(coronerPoolRequestDto.getRequestDate())
            .isEqualTo(coronerPool.getRequestDate());

        assertThat(coronerPoolRequestDto.getName())
            .isEqualTo(coronerPool.getName());

        assertThat(coronerPoolRequestDto.getEmailAddress())
            .isEqualTo(coronerPool.getEmail());

        if (coronerPoolRequestDto.getPhone() != null) {
            assertThat(coronerPoolRequestDto.getPhone())
                .isEqualTo(coronerPool.getPhoneNumber());
        }
    }

    private CoronerPoolRequestDto setUpCoronerPoolCreateRequestDto() {
        CoronerPoolRequestDto coronerPoolRequestDto = new CoronerPoolRequestDto();
        coronerPoolRequestDto.setLocationCode("415");
        coronerPoolRequestDto.setNoRequested(100);
        coronerPoolRequestDto.setRequestDate(LocalDate.now());
        coronerPoolRequestDto.setName("Coroners Name");
        coronerPoolRequestDto.setEmailAddress("emailof@coroner.gov.uk");
        coronerPoolRequestDto.setPhone("0207 12341234");

        return coronerPoolRequestDto;
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

}
