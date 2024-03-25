package uk.gov.hmcts.juror.api.bureau.controller;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.bureau.controller.request.AutoAssignRequest;
import uk.gov.hmcts.juror.api.bureau.controller.request.JurorResponseSearchRequest;
import uk.gov.hmcts.juror.api.bureau.controller.response.AutoAssignResponse;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseOverviewDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseSummaryWrapper;
import uk.gov.hmcts.juror.api.bureau.controller.response.JurorResponseSearchResults;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.juror.api.bureau.service.JurorResponseSearchServiceImplTest.assertResponsesSortedCorrectly;

/**
 * Integration tests for the API endpoints defined in {@link BureauResponsesController}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings("PMD.LawOfDemeter")
public class BureauResponsesControllerTest extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate template;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private HttpHeaders httpHeaders;

    @Value("${jwt.secret.bureau}")
    private String bureauSecret;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }


    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauServiceImpl_getByProcessingStatus.sql"
    })
    public void getTodo() throws Exception {

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("ncrawford")
            .daysToExpire(89)
            .owner("400")
            .build());

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        ResponseEntity<BureauResponseSummaryWrapper> responseEntity =
            template.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET, URI.create("/api/v1/bureau"
                + "/responses/todo")), BureauResponseSummaryWrapper.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        final BureauResponseSummaryWrapper wrapper = responseEntity.getBody();
        assertThat(wrapper.getResponses()).isNotNull().hasSize(3);
        assertThat(wrapper.getTodoCount()).isEqualTo(3);
        assertThat(wrapper.getRepliesPendingCount()).isEqualTo(4);
        assertThat(wrapper.getCompletedCount()).isEqualTo(1);
        assertThat(wrapper.getResponses()).extracting("jurorNumber").contains("209092530", "586856851", "487498307");
        assertThat(wrapper.getResponses()).extracting("processingStatus").containsOnly("TODO");
        assertThat(wrapper.getResponses()).extracting("assignedStaffMember").extracting("login").containsOnly(
            "ncrawford");
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauServiceImpl_getByProcessingStatus.sql"
    })
    public void getRepliesPending() throws Exception {

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("ncrawford")
            .daysToExpire(89)
            .owner("400")
            .build());

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        ResponseEntity<BureauResponseSummaryWrapper> responseEntity =
            template.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET, URI.create("/api/v1/bureau"
                + "/responses/pending")), BureauResponseSummaryWrapper.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        final BureauResponseSummaryWrapper wrapper = responseEntity.getBody();
        assertThat(wrapper.getResponses()).isNotNull().hasSize(4);
        assertThat(wrapper.getTodoCount()).isEqualTo(3);
        assertThat(wrapper.getRepliesPendingCount()).isEqualTo(4);
        assertThat(wrapper.getCompletedCount()).isEqualTo(1);
        assertThat(wrapper.getResponses()).extracting("jurorNumber").contains("472008411", "845814425", "275852838",
            "811923115");
        assertThat(wrapper.getResponses()).extracting("processingStatus").containsOnly("AWAITING_CONTACT",
            "AWAITING_TRANSLATION", "AWAITING_COURT_REPLY");
        assertThat(wrapper.getResponses()).extracting("assignedStaffMember").extracting("login").containsOnly(
            "ncrawford");
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauServiceImplTest_completedToday.sql"
    })
    public void getCompletedToday() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("ncrawford")
            .daysToExpire(89)
            .owner("400")
            .build());

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        ResponseEntity<BureauResponseSummaryWrapper> responseEntity =
            template.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET, URI.create("/api/v1/bureau"
                + "/responses/completedToday")), BureauResponseSummaryWrapper.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        final BureauResponseSummaryWrapper wrapper = responseEntity.getBody();

        assertThat(wrapper.getResponses()).isNotNull().hasSize(1);
        assertThat(wrapper.getTodoCount()).isEqualTo(3);
        assertThat(wrapper.getRepliesPendingCount()).isEqualTo(4);
        assertThat(wrapper.getCompletedCount()).isEqualTo(1);
        assertThat(wrapper.getResponses()).extracting("jurorNumber").contains("827761086");
        assertThat(wrapper.getResponses()).extracting("processingStatus").containsOnly("CLOSED");
        assertThat(wrapper.getResponses()).extracting("assignedStaffMember").extracting("login").containsOnly(
            "ncrawford");
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauServiceImpl_getByProcessingStatus.sql"
    })
    public void getOverview() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("ncrawford")
            .daysToExpire(89)
            .owner("400")
            .build());

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        ResponseEntity<BureauResponseOverviewDto> responseEntity =
            template.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET, URI.create("/api/v1/bureau"
                + "/responses/overview/ncrawford")), BureauResponseOverviewDto.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        final BureauResponseOverviewDto overviewDto = responseEntity.getBody();

        assertThat(overviewDto.getUrgentsCount()).isEqualTo(2);
        assertThat(overviewDto.getPendingCount()).isEqualTo(3);
        assertThat(overviewDto.getTodoCount()).isEqualTo(2);
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/JurorResponseSearchServiceImpl_searchForResponses_bureauOfficer.sql"
    })
    public void searchForResponses_jurorNumberOnly_errorPath_notANumber() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("ncrawford")
            .daysToExpire(89)
            .owner("400")
            .build());

        final URI uri = URI.create("/api/v1/bureau/responses/search");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<JurorResponseSearchRequest> requestEntity =
            new RequestEntity<>(JurorResponseSearchRequest.builder().jurorNumber("Not a number").build(), httpHeaders,
                HttpMethod.POST, uri);
        ResponseEntity<JurorResponseSearchResults> response = template.exchange(requestEntity,
            JurorResponseSearchResults.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/JurorResponseSearchServiceImpl_searchForResponses_bureauOfficer.sql"
    })
    public void searchForResponses_lastNameOnly_errorPath_blank() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("ncrawford")
            .daysToExpire(89)
            .owner("400")
            .build());

        final URI uri = URI.create("/api/v1/bureau/responses/search");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<JurorResponseSearchRequest> requestEntity =
            new RequestEntity<>(JurorResponseSearchRequest.builder().lastName("   ").build(), httpHeaders,
                HttpMethod.POST, uri);
        ResponseEntity<JurorResponseSearchResults> response = template.exchange(requestEntity,
            JurorResponseSearchResults.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/JurorResponseSearchServiceImpl_searchForResponses_bureauOfficer.sql"
    })
    public void searchForResponses_postcodeOnly_errorPath_blank() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("ncrawford")
            .daysToExpire(89)
            .owner("400")
            .build());

        final URI uri = URI.create("/api/v1/bureau/responses/search");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<JurorResponseSearchRequest> requestEntity =
            new RequestEntity<>(JurorResponseSearchRequest.builder().postCode("   ").build(), httpHeaders,
                HttpMethod.POST, uri);
        ResponseEntity<JurorResponseSearchResults> response = template.exchange(requestEntity,
            JurorResponseSearchResults.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/JurorResponseSearchServiceImpl_searchForResponses_bureauOfficer.sql"
    })
    public void searchForResponses_poolNumber_errorPath_notANumber() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("ncrawford")
            .daysToExpire(89)
            .owner("400")
            .build());

        final URI uri = URI.create("/api/v1/bureau/responses/search");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<JurorResponseSearchRequest> requestEntity =
            new RequestEntity<>(JurorResponseSearchRequest.builder().poolNumber("Not a number").build(), httpHeaders,
                HttpMethod.POST, uri);
        ResponseEntity<JurorResponseSearchResults> response = template.exchange(requestEntity,
            JurorResponseSearchResults.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/JurorResponseSearchServiceImpl_searchForResponses_bureauOfficer.sql"
    })
    public void searchForResponses_lastNameOnly() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("ncrawford")
            .daysToExpire(89)
            .owner("400")
            .build());

        final URI uri = URI.create("/api/v1/bureau/responses/search");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<JurorResponseSearchRequest> requestEntity =
            new RequestEntity<>(JurorResponseSearchRequest.builder().lastName("Charleston").build(), httpHeaders,
                HttpMethod.POST, uri);
        ResponseEntity<JurorResponseSearchResults> response = template.exchange(requestEntity,
            JurorResponseSearchResults.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getResponses()).isNotNull().hasSize(1);
        // Only surnames _beginning_ with the search string should be found
        assertThat(response.getBody().getResponses()).extracting("lastName").contains("Charleston");
        assertThat(response.getBody().getResponses()).extracting("lastName").doesNotContainNull();

        assertResponsesSortedCorrectly(response.getBody());
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/JurorResponseSearchServiceImpl_searchForResponses_teamLeader.sql"
    })
    public void searchForResponses_teamLeaderHasHigherLimit() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("ksalazar")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder().rank(1).active(1).name("Kris Salazar").build())
            .build());

        final URI uri = URI.create("/api/v1/bureau/responses/search");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        // EVERY surname in the test data contains the letter e (for precisely this reason!) so this search will
        // match more than 250 rows
        RequestEntity<JurorResponseSearchRequest> requestEntity =
            new RequestEntity<>(JurorResponseSearchRequest.builder().lastName("e").build(), httpHeaders,
                HttpMethod.POST, uri);
        ResponseEntity<JurorResponseSearchResults> response = template.exchange(requestEntity,
            JurorResponseSearchResults.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody().getResponses()).hasSize(0);
        assertThat(response.getBody().getMeta().getMax()).isEqualTo(250);
        assertThat(response.getBody().getMeta().getTotal()).isEqualTo(0);
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauResponsesController_autoAssignPost.sql"
    })
    public void autoAssign_post_happyPath() throws Exception {

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("ksalazar")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder().rank(1).active(1).name("Kris Salazar").build())
            .build());

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        final URI uri = URI.create("/api/v1/bureau/responses/autoassign");

        RequestEntity<AutoAssignRequest> request = new RequestEntity<>(AutoAssignRequest.builder()
            .data(Arrays.asList(
                AutoAssignRequest.StaffCapacity.builder().capacity(30).login("carneson").build(),
                AutoAssignRequest.StaffCapacity.builder().capacity(30).login("sgomez").build(),
                AutoAssignRequest.StaffCapacity.builder().capacity(59).login("mruby").build()
            )).build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> response = template.exchange(request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauResponsesController_autoAssignPost.sql"
    })
    public void autoAssign_post_alternatePath_insufficientCapacity() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("ksalazar")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder().rank(1).active(1).name("Kris Salazar").build())
            .build());

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        final URI uri = URI.create("/api/v1/bureau/responses/autoassign");

        RequestEntity<AutoAssignRequest> request = new RequestEntity<>(AutoAssignRequest.builder()
            .data(Arrays.asList(
                AutoAssignRequest.StaffCapacity.builder().capacity(30).login("carneson").build(),
                AutoAssignRequest.StaffCapacity.builder().capacity(30).login("sgomez").build(),
                AutoAssignRequest.StaffCapacity.builder().capacity(30).login("mruby").build()
            )).build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> response = template.exchange(request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauResponsesController_autoAssignPost.sql"
    })
    public void autoAssign_post_errorPath_staffMemberDoesNotExist() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("ksalazar")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder().rank(1).active(1).name("Kris Salazar").build())
            .build());

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        final URI uri = URI.create("/api/v1/bureau/responses/autoassign");

        RequestEntity<AutoAssignRequest> request = new RequestEntity<>(AutoAssignRequest.builder()
            .data(Arrays.asList(
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("idonotexist").build(),
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("sgomez").build(),
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("mruby").build()
            )).build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> response = template.exchange(request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauResponsesController_autoAssignPost.sql"
    })
    public void autoAssign_post_errorPath_staffMemberIsTeamLeader() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("ksalazar")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder().rank(1).active(1).name("Kris Salazar").build())
            .build());

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        final URI uri = URI.create("/api/v1/bureau/responses/autoassign");

        RequestEntity<AutoAssignRequest> request = new RequestEntity<>(AutoAssignRequest.builder()
            .data(Arrays.asList(
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("tgarrett").build(),
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("sgomez").build(),
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("mruby").build()
            )).build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> response = template.exchange(request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauResponsesController_autoAssignPost.sql"
    })
    public void autoAssign_post_errorPath_userIsNotTeamLeader() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("carneson")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder().rank(0).active(1).name("Chad Arneson").build())
            .build());

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        final URI uri = URI.create("/api/v1/bureau/responses/autoassign");

        RequestEntity<AutoAssignRequest> request = new RequestEntity<>(AutoAssignRequest.builder()
            .data(Arrays.asList(
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("sgomez").build(),
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("mruby").build()
            )).build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> response = template.exchange(request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauResponsesController_autoAssignPost.sql"
    })
    public void autoAssign_post_errorPath_duplicateCapacityValue() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("ksalazar")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder().rank(1).active(1).name("Kris Salazar").build())
            .build());

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        final URI uri = URI.create("/api/v1/bureau/responses/autoassign");

        RequestEntity<AutoAssignRequest> request = new RequestEntity<>(AutoAssignRequest.builder()
            .data(Arrays.asList(
                AutoAssignRequest.StaffCapacity.builder().capacity(70).login("sgomez").build(),
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("sgomez").build(),
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("mruby").build()
            )).build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> response = template.exchange(request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    /**
     * The backend should return a 404 when the request specifies a capacity value for a non-existent bureau officer.
     *
     * @throws Exception if the test falls over
     */
    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauResponsesController_autoAssignPost.sql"
    })
    public void autoAssign_post_errorPath_bureauOfficerDoesNotExist() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("ksalazar")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder().rank(1).active(1).name("Kris Salazar").build())
            .build());

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        final URI uri = URI.create("/api/v1/bureau/responses/autoassign");

        RequestEntity<AutoAssignRequest> request = new RequestEntity<>(AutoAssignRequest.builder()
            .data(Collections.singletonList(
                AutoAssignRequest.StaffCapacity.builder().capacity(70).login("idonotexist").build()
            )).build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> response = template.exchange(request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    /**
     * The backend should return a 403 when the request specifies a capacity value for an inactive bureau officer.
     *
     * @throws Exception if the test falls over
     */
    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauResponsesController_autoAssignPost.sql"
    })
    public void autoAssign_post_errorPath_bureauOfficerInactive() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("ksalazar")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder().rank(1).active(1).name("Kris Salazar").build())
            .build());

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        final URI uri = URI.create("/api/v1/bureau/responses/autoassign");

        RequestEntity<AutoAssignRequest> request = new RequestEntity<>(AutoAssignRequest.builder()
            .data(Collections.singletonList(
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("dpeters").build()
            )).build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> response = template.exchange(request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    /**
     * The backend should return a 403 when the request specifies a capacity value for a team leader.
     *
     * @throws Exception if the test falls over
     */
    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauResponsesController_autoAssignPost.sql"
    })
    public void autoAssign_post_errorPath_capacityForTeamLeader() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("ksalazar")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder().rank(1).active(1).name("Kris Salazar").build())
            .build());

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        final URI uri = URI.create("/api/v1/bureau/responses/autoassign");

        RequestEntity<AutoAssignRequest> request = new RequestEntity<>(AutoAssignRequest.builder()
            .data(Collections.singletonList(
                AutoAssignRequest.StaffCapacity.builder().capacity(60).login("tgarrett").build()
            )).build(), httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> response = template.exchange(request, Void.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauResponsesController_autoAssignGet.sql"
    })
    @Sql(statements = "INSERT INTO juror_mod.app_setting (SETTING,VALUE) "
        + "VALUES ('AUTO_ASSIGNMENT_DEFAULT_CAPACITY', '50');")
    public void autoAssign_get_happyPath() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("jbrown1")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder().rank(1).active(1).name("Jared Brown").build())
            .build());

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        final URI uri = URI.create("/api/v1/bureau/responses/autoassign");

        RequestEntity<Void> request = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<AutoAssignResponse> response = template.exchange(request, AutoAssignResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(3).containsExactlyInAnyOrder(
            AutoAssignResponse.StaffCapacityResponse.responseBuilder().login("smcintyre").name("Stephanie Mcintyre")
                .capacity(50).urgents(2L).allocation(52L).incompletes(2L).build(),
            AutoAssignResponse.StaffCapacityResponse.responseBuilder().login("sbell").name("Sandra Bell").capacity(50)
                .urgents(1L).allocation(51L).incompletes(6L).build(),
            AutoAssignResponse.StaffCapacityResponse.responseBuilder().login("alineweaver").name("Albert Lineweaver")
                .capacity(50).urgents(3L).allocation(53L).incompletes(9L).build()
        );

        assertThat(response.getBody().getMeta()).isNotNull();
        assertThat(response.getBody().getMeta().getBacklogSize()).isEqualTo(1L);
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauResponsesController_autoAssignGet.sql"
    })
    public void autoAssign_get_alternatePath_noSettingInDatabase() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("jbrown1")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder().rank(1).active(1).name("Jared Brown").build())
            .build());

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        final URI uri = URI.create("/api/v1/bureau/responses/autoassign");

        RequestEntity<Void> request = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<AutoAssignResponse> response = template.exchange(request, AutoAssignResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getData()).hasSize(3).containsExactlyInAnyOrder(
            AutoAssignResponse.StaffCapacityResponse.responseBuilder().login("smcintyre").name("Stephanie Mcintyre")
                .capacity(60).urgents(2L).allocation(62L).incompletes(2L).build(),
            AutoAssignResponse.StaffCapacityResponse.responseBuilder().login("sbell").name("Sandra Bell").capacity(60)
                .urgents(1L).allocation(61L).incompletes(6L).build(),
            AutoAssignResponse.StaffCapacityResponse.responseBuilder().login("alineweaver").name("Albert Lineweaver")
                .capacity(60).urgents(3L).allocation(63L).incompletes(9L).build()
        );
    }

    @Test
    @Sql({
        "/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauResponsesController_autoAssignGet.sql"
    })
    public void autoAssign_get_errorPath_userIsNotTeamLeader() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("sbell")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder().rank(0).active(1).name("Sandra Bell").build())
            .build());

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);

        final URI uri = URI.create("/api/v1/bureau/responses/autoassign");

        RequestEntity<Void> request = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<AutoAssignResponse> response = template.exchange(request, AutoAssignResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNull();
    }
}
