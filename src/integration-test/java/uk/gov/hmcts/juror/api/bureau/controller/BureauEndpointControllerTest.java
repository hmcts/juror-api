package uk.gov.hmcts.juror.api.bureau.controller;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Ignore;
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
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.SpringBootErrorResponse;
import uk.gov.hmcts.juror.api.bureau.controller.request.BureauResponseStatusUpdateDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauJurorDetailDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.BureauResponseSummaryWrapper;
import uk.gov.hmcts.juror.api.bureau.domain.ChangeLogType;
import uk.gov.hmcts.juror.api.bureau.exception.BureauOptimisticLockingException;
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

/**
 * Bureau endpoint controller integration tests.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings("PMD.TooManyMethods")
public class BureauEndpointControllerTest extends AbstractIntegrationTest {
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
    public void bureauAuthenticationEndpoint_unhappy_header1() {
        final String description = "Authentication header is not present";

        ResponseEntity<String> exchange = template.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
            URI.create("/api/v1/bureau/settings")), String.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).describedAs(description).isNotEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody()).describedAs(description).asString().isNotEmpty()
            .doesNotContain("Hello Bureau JWT!");
    }

    @Test
    public void bureauAuthenticationEndpoint_unhappy_header2() {
        final String description = "Authentication header is empty";

        httpHeaders.set(HttpHeaders.AUTHORIZATION, null);
        ResponseEntity<String> exchange = template.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
            URI.create("/api/v1/bureau/settings")), String.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).describedAs(description).isNotEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody()).describedAs(description).asString().isNotEmpty()
            .doesNotContain("Hello Bureau JWT!");
    }

    @Test
    public void bureauAuthenticationEndpoint_unhappy_header3() throws Exception {
        final String description = "Authentication header is invalid";

        final String publicJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("99")
            .login("testlogin")
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .build());

        final String[] jwtSections = publicJwt.split("\\.");
        final String invalidPublicJwt = String.join(".", jwtSections[0], "eyJhZG1pbiI6ICJ0cnVlIn0", jwtSections[2]);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, invalidPublicJwt);
        ResponseEntity<String> exchange = template.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET,
            URI.create("/api/v1/bureau/settings")), String.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).describedAs(description).isNotEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody()).describedAs(description).asString().isNotEmpty()
            .doesNotContain("Hello Bureau JWT!");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauRepository_findByJurorNumber.sql")
    public void retrieveBureauJurorDetailsById_WithValidJurorNumberAsParam_ReturnCorrectJurorDetails() {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("1")
            .login("testlogin")
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .build())
        );

        ResponseEntity<BureauJurorDetailDto> responseEntity = template.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, URI.create("/api/v1/bureau/juror/209092530")), BureauJurorDetailDto.class);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody()).isInstanceOf(BureauJurorDetailDto.class);
        assertThat(responseEntity.getBody()).extracting(
                "jurorNumber", "firstName", "lastName", "version", "useJurorPhoneDetails", "useJurorEmailDetails")
            .contains("209092530", "Jane", "CASTILLO", 555, true, true);
        assertThat(responseEntity.getBody().getChangeLog()).hasSize(1);
        assertThat(responseEntity.getBody().getChangeLog().get(0).getItems()).hasSize(2);
        assertThat(responseEntity.getBody().getPhoneLogs()).hasSize(1);

        final BureauJurorDetailDto.ChangeLogDto firstChangeLog = responseEntity.getBody().getChangeLog().get(0);
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(firstChangeLog)
            .isNotNull()
            .extracting("type", "notes")
            .containsExactly(ChangeLogType.JUROR_DETAILS.name(), "notes1")
        ;
        softly.assertThat(firstChangeLog.getItems().get(0))
            .isNotNull()
            .extracting("oldKeyName", "oldValue", "newKeyName", "newValue")
            .containsExactly("lastName", null, "lastName", "Castilio")
        ;
        softly.assertThat(firstChangeLog.getItems().get(1))
            .isNotNull()
            .extracting("oldKeyName", "oldValue", "newKeyName", "newValue")
            .containsExactly("firstName", null, "firstName", "Janey")
        ;
        softly.assertAll();

        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.contact_log", Integer.class))
            .as("There is a redundant phone log entry that is not from Juror Digital that is not "
                + "returned by the API").isEqualTo(2);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauRepository_findByJurorNumber.sql")
    public void filterBureauDetailsByStatus_WithValidCategoryFilter_ReturnsResponsesForStatusAndCount() {

        httpHeaders.set(HttpHeaders.AUTHORIZATION, mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("1")
            .login("testlogin")
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .build())
        );

        ResponseEntity<BureauResponseSummaryWrapper> response = template.exchange(new RequestEntity<Void>(httpHeaders,
                HttpMethod.GET, URI.create("/api/v1/bureau/responses?filterBy=todo")),
            BureauResponseSummaryWrapper.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().getResponses()).hasSize(3);
        assertThat(response.getBody().getTodoCount()).isEqualTo(3);
        assertThat(response.getBody().getRepliesPendingCount()).isEqualTo(4);
        assertThat(response.getBody().getCompletedCount()).isEqualTo(1);

    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauEndpointControllerTest.updateResponseStatus_happy.sql")
    public void updateResponseStatus_happy_closed() throws Exception {

        final LocalDateTime beforeTest = LocalDateTime.now();
        final String description = "Update juror response status happy path.";

        final URI uri = URI.create("/api/v1/bureau/status/644892530");

        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("99")
            .login("testlogin")
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_aud",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class))
            .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            1);

        final BureauResponseStatusUpdateDto dto = BureauResponseStatusUpdateDto.builder()
            .status(ProcessingStatus.CLOSED)
            .version(2)
            .build();

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<BureauResponseStatusUpdateDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(exchange.getBody()).isNullOrEmpty();

        // assert db state after merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertCompletedAtFix(beforeTest);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_aud",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class))
            .isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            2);

        // assert the change to DOB was applied and audited
        assertThat(jdbcTemplate.queryForObject("SELECT last_name FROM juror_mod.juror WHERE juror_number = '644892530'",
            String.class)).isEqualTo("DOE");

        assertThat(jdbcTemplate.queryForObject("SELECT NEW_PROCESSING_STATUS FROM juror_mod.juror_response_aud "
            + "WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo("CLOSED");
        assertThat(jdbcTemplate.queryForObject("SELECT OLD_PROCESSING_STATUS FROM juror_mod.juror_response_aud "
            + "WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo("TODO");
    }

    /**
     * Tests that {@link JurorResponse#completedAt} is set correctly when a response is closed.
     *
     * @param beforeTest the time the test started at (the completedAt value should be after this)
     * @since JDB-2139
     */
    private void assertCompletedAtFix(LocalDateTime beforeTest) {
        assertThat(jdbcTemplate.queryForObject("SELECT COMPLETED_AT FROM juror_mod.juror_response WHERE "
            + "JUROR_NUMBER='644892530'", LocalDateTime.class)).isNotNull().isAfter(beforeTest)
            .isBefore(LocalDateTime.now());
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauEndpointControllerTest.updateResponseStatus_unhappy_processingComplete.sql")
    @Ignore("Functionality removed.  Multiple updates are now allowed.  JDB-1895")
    public void updateResponseStatus_unhappy_processingAlreadyCompleted() throws Exception {
        fail("Functionality removed.  Multiple updates are now allowed.  JDB-1895");
        final String description = "Update juror response status unhappy path - processing previously completed.";

        final URI uri = URI.create("/api/v1/bureau/status/644892530");

        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("99")
            .login("testlogin")
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT VERSION FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            2);// initial version
        assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_COMPLETE FROM juror_mod.juror_response",
            String.class)).isEqualTo("Y");// processing complete
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_aud",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM juror_mod.juror_response "
            + "WHERE JUROR_NUMBER='644892530'", String.class)).isNull();

        final BureauResponseStatusUpdateDto dto = BureauResponseStatusUpdateDto.builder()
            .status(ProcessingStatus.AWAITING_COURT_REPLY)
            .version(2)
            .build();

        // expecting an error response with an optimistic locking error.
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<BureauResponseStatusUpdateDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);// custom response code from controller

        // assert db state after is the same
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT VERSION FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            2);// initial version
        assertThat(jdbcTemplate.queryForObject("SELECT PROCESSING_COMPLETE FROM juror_mod.juror_response",
            String.class)).isEqualTo("Y");// processing complete
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_aud",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM juror_mod.juror_response "
            + "WHERE JUROR_NUMBER='644892530'", String.class)).isNull();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauEndpointControllerTest.updateResponseStatus_happy.sql")
    public void updateResponseStatus_unhappy_optimisticLockingMismatch() throws Exception {
        final String description = "Update juror response status unhappy path - optimistic locking";

        final URI uri = URI.create("/api/v1/bureau/status/644892530");

        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("99")
            .login("testlogin")
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT VERSION FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            2);// initial version
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_aud",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            1);

        final BureauResponseStatusUpdateDto dto = BureauResponseStatusUpdateDto.builder()
            .status(ProcessingStatus.CLOSED)
            .version(1) // version number is older than the database current version
            .build();

        // expecting an error response with an optimistic locking error.
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<BureauResponseStatusUpdateDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);// custom response code from controller
        assertThat(exchange.getBody().getException()).isEqualTo(BureauOptimisticLockingException.class.getName());

        // assert db state after merge, entries should NOT be present!
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT VERSION FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            2);// unchanged initial version
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_aud",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            1);

        // assert the change to DOB was not applied
        assertThat(jdbcTemplate.queryForObject("SELECT last_name FROM juror_mod.juror WHERE juror_number = '644892530'",
            String.class)).isEqualTo("CASTILLO");// unchanged
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauEndpointControllerTest.updateResponseStatus_happy.sql")
    public void updateResponseStatus_happy_nonMergeStatusChange_awaitingJurorContact() throws Exception {

        final ProcessingStatus newProcessingStatus = ProcessingStatus.AWAITING_CONTACT;

        final String description = "Update juror response status happy path.";
        final URI uri = URI.create("/api/v1/bureau/status/644892530");

        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("99")
            .login("testlogin")
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .build());

        // assert db state before status change
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_aud",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            1);

        final BureauResponseStatusUpdateDto dto = BureauResponseStatusUpdateDto.builder()
            .status(newProcessingStatus)
            .version(2)
            .build();

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<BureauResponseStatusUpdateDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(exchange.getBody()).isNullOrEmpty();

        // assert db state after status change.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_aud",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            1);

        // assert the changes to pool were not applied
        assertThat(jdbcTemplate.queryForObject("SELECT last_name FROM juror_mod.juror WHERE juror_number = '644892530'",
            String.class)).isEqualTo("CASTILLO");

        // assert change to processing status was audited
        assertThat(jdbcTemplate.queryForObject("SELECT NEW_PROCESSING_STATUS FROM juror_mod.juror_response_aud "
            + "WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo(newProcessingStatus.toString());
        assertThat(jdbcTemplate.queryForObject("SELECT OLD_PROCESSING_STATUS FROM juror_mod.juror_response_aud "
            + "WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo("TODO");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauEndpointControllerTest.updateResponseStatus_happy.sql")
    public void updateResponseStatus_happy_nonMergeStatusChange_awaitingCourt() throws Exception {

        final ProcessingStatus newProcessingStatus = ProcessingStatus.AWAITING_COURT_REPLY;

        final String description = "Update juror response status happy path.";
        final URI uri = URI.create("/api/v1/bureau/status/644892530");

        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("99")
            .login("testlogin")
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .build());

        // assert db state before status change
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_aud",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            1);

        final BureauResponseStatusUpdateDto dto = BureauResponseStatusUpdateDto.builder()
            .status(newProcessingStatus)
            .version(2)
            .build();

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<BureauResponseStatusUpdateDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(exchange.getBody()).isNullOrEmpty();

        // assert db state after status change.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_aud",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            1);

        // assert the changes to pool were not applied
        assertThat(jdbcTemplate.queryForObject("SELECT last_name FROM juror_mod.juror WHERE juror_number = '644892530'",
            String.class)).isEqualTo("CASTILLO");

        // assert change to processing status was audited
        assertThat(jdbcTemplate.queryForObject("SELECT NEW_PROCESSING_STATUS FROM juror_mod.juror_response_aud "
            + "WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo(newProcessingStatus.toString());
        assertThat(jdbcTemplate.queryForObject("SELECT OLD_PROCESSING_STATUS FROM juror_mod.juror_response_aud "
            + "WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo("TODO");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauEndpointControllerTest.updateResponseStatus_happy.sql")
    public void updateResponseStatus_happy_nonMergeStatusChange_awaitingTranslation() throws Exception {

        final ProcessingStatus newProcessingStatus = ProcessingStatus.AWAITING_TRANSLATION;

        final String description = "Update juror response status happy path.";
        final URI uri = URI.create("/api/v1/bureau/status/644892530");

        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("99")
            .login("testlogin")
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .build());

        // assert db state before status change
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_aud",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            1);

        final BureauResponseStatusUpdateDto dto = BureauResponseStatusUpdateDto.builder()
            .status(newProcessingStatus)
            .version(2)
            .build();

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<BureauResponseStatusUpdateDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(exchange.getBody()).isNullOrEmpty();

        // assert db state after status change.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_aud",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            1);

        // assert the changes to pool were not applied
        assertThat(jdbcTemplate.queryForObject("SELECT last_name FROM juror_mod.juror WHERE juror_number = '644892530'",
            String.class)).isEqualTo("CASTILLO");

        // assert change to processing status was audited
        assertThat(jdbcTemplate.queryForObject("SELECT NEW_PROCESSING_STATUS FROM juror_mod.juror_response_aud "
            + "WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo(newProcessingStatus.toString());
        assertThat(jdbcTemplate.queryForObject("SELECT OLD_PROCESSING_STATUS FROM juror_mod.juror_response_aud "
            + "WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo("TODO");
    }

    @Test
    @Sql({"/db/truncate.sql",
        "/db/mod/truncate.sql",
        "/db/standing_data.sql",
        "/db/BureauEndpointControllerTest.updateResponseStatus_happy.sql"})
    public void updateResponseStatus_happy_nonMergeStatusChange_todo() throws Exception {

        final ProcessingStatus newProcessingStatus = ProcessingStatus.TODO;

        final String description = "Update juror response status happy path.";
        final URI uri = URI.create("/api/v1/bureau/status/644892530");

        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("99")
            .login("testlogin")
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .build());

        // assert db state before status change
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_aud",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class))
            .isEqualTo(1);

        final BureauResponseStatusUpdateDto dto = BureauResponseStatusUpdateDto.builder()
            .status(newProcessingStatus)
            .version(2)
            .build();

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<BureauResponseStatusUpdateDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(exchange.getBody()).isNullOrEmpty();

        // assert db state after status change.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_aud",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            1);

        // assert the changes to pool were not applied
        assertThat(jdbcTemplate.queryForObject("SELECT last_name FROM juror_mod.juror WHERE juror_number = '644892530'",
            String.class)).isEqualTo("CASTILLO");

        // assert change to processing status was audited
        assertThat(jdbcTemplate.queryForObject("SELECT NEW_PROCESSING_STATUS FROM juror_mod.juror_response_aud "
            + "WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo(newProcessingStatus.toString());
        assertThat(jdbcTemplate.queryForObject("SELECT OLD_PROCESSING_STATUS FROM juror_mod.juror_response_aud "
            + "WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo("TODO");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauEndpointControllerTest.updateResponseStatus_happy.sql")
    public void updateResponseStatus_happy_alsoUpdatesStaffAssignment() throws Exception {

        final ProcessingStatus newProcessingStatus = ProcessingStatus.AWAITING_CONTACT;

        final String description = "Update juror response status happy path.";
        final URI uri = URI.create("/api/v1/bureau/status/644892530");

        final String bureauJwt = mintBureauJwt(BureauJwtPayload.builder()
            .userLevel("99")
            .login("testlogin")
            .owner(JurorDigitalApplication.JUROR_OWNER)
            .build());

        // assert db state before status change
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_aud",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM juror_mod.juror_response "
            + "WHERE JUROR_NUMBER='644892530'", String.class)).isNull();

        final BureauResponseStatusUpdateDto dto = BureauResponseStatusUpdateDto.builder()
            .status(newProcessingStatus)
            .version(2)
            .build();

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<BureauResponseStatusUpdateDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<String> exchange = template.exchange(requestEntity, String.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(exchange.getBody()).isNullOrEmpty();

        // assert db state after status change.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_aud",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_reasonable_adjustment",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response_CJS_EMPLOYMENT",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_history", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_audit", Integer.class)).isEqualTo(
            1);

        // assert the changes to pool were not applied
        assertThat(jdbcTemplate.queryForObject("SELECT last_name FROM juror_mod.juror WHERE juror_number = '644892530'",
            String.class)).isEqualTo("CASTILLO");

        // assert change to processing status was audited
        assertThat(jdbcTemplate.queryForObject("SELECT NEW_PROCESSING_STATUS FROM juror_mod.juror_response_aud "
            + "WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo(newProcessingStatus.toString());
        assertThat(jdbcTemplate.queryForObject("SELECT OLD_PROCESSING_STATUS FROM juror_mod.juror_response_aud "
            + "WHERE JUROR_NUMBER = '644892530'", String.class)).isEqualTo("TODO");

        // assert staff assignment on response has changed and been audited
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM juror_mod.juror_response", String.class))
            .isEqualTo("testlogin");
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.user_juror_response_audit",
            Integer.class))
            .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT ASSIGNED_TO FROM juror_mod.user_juror_response_audit",
            String.class))
            .isEqualTo("testlogin");
    }
}
