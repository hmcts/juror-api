package uk.gov.hmcts.juror.api.bureau.controller;

import org.assertj.core.api.SoftAssertions;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.SpringBootErrorResponse;
import uk.gov.hmcts.juror.api.bureau.controller.request.AssignmentsMultiRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.MultipleStaffAssignmentDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.ReassignResponsesDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.StaffAssignmentRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.StaffMemberCrudRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.StaffMemberCrudResponseDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.AssignmentsListDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.AssignmentsListDto.AssignmentListDataDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.OperationFailureListDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffAssignmentResponseDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffDetailDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffListDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffRosterResponseDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.TeamDto;
import uk.gov.hmcts.juror.api.bureau.exception.BureauOptimisticLockingException;
import uk.gov.hmcts.juror.api.bureau.service.StaffMemberCrudException;
import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;

/**
 * Integration tests for the API endpoints defined in {@link BureauStaffController}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@SuppressWarnings("PMD.LawOfDemeter")
public class BureauStaffControllerTest extends AbstractIntegrationTest {

    private static final TeamDto TEAM_1 = TeamDto.builder().id(1L).name("London & Wales").version(0).build();
    private static final TeamDto TEAM_2 =
        TeamDto.builder().id(2L).name("South East, North East & North West").version(0).build();
    private static final TeamDto TEAM_3 = TeamDto.builder().id(3L).name("Midlands & South West").version(0).build();

    private static final List<StaffDto> ACTIVE_STAFF = Arrays.asList(
        StaffDto.builder()
            .login("jpowers").name("Joanna Powers")
            .isActive(true).isTeamLeader(false)
            .team(TEAM_1).version(0)
            .court1("400")
            .build(),
        StaffDto.builder()
            .login("tsanchez").name("Todd Sanchez")
            .isActive(true).isTeamLeader(false)
            .team(TEAM_2).version(0)
            .court1("400")
            .build(),
        StaffDto.builder()
            .login("gbeck").name("Grant Beck")
            .isActive(true).isTeamLeader(false)
            .team(TEAM_3).version(0)
            .court1("400")
            .build(),
        StaffDto.builder()
            .login("rprice").name("Roxanne Price")
            .isActive(true).isTeamLeader(true)
            .team(TEAM_1).version(0)
            .court1("415")
            .court2("462")
            .court3("767")
            .build(),
        StaffDto.builder()
            .login("pbrewer").name("Preston Brewer")
            .isActive(true).isTeamLeader(true)
            .team(TEAM_2).version(0)
            .court1("400")
            .build(),
        StaffDto.builder()
            .login("acopeland").name("Amelia Copeland")
            .isActive(true).isTeamLeader(true)
            .team(TEAM_3).version(0)
            .court1("400")
            .build()
    );

    private static final List<StaffDto> INACTIVE_STAFF = Arrays.asList(
        StaffDto.builder()
            .login("jphillips")
            .name("Joan Phillips")
            .isActive(false).isTeamLeader(false)
            .team(TEAM_1).version(0)
            .court1("400")
            .build(),
        StaffDto.builder()
            .login("srogers")
            .name("Shawn Rogers")
            .isActive(false).isTeamLeader(false)
            .team(TEAM_2).version(0)
            .court1("400")
            .build(),
        StaffDto.builder()
            .login("pbrooks")
            .name("Paul Brooks")
            .isActive(false).isTeamLeader(false)
            .team(TEAM_3).version(0)
            .court1("400")
            .build()
    );

    @Value("${jwt.secret.bureau}")
    private String bureauSecret;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TestRestTemplate template;

    private HttpHeaders httpHeaders;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffRepositoryTest_findByActiveOrderByNameAsc.sql")
    public void getAll_happyPath() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("rprice")
            .staff(BureauJWTPayload.Staff.builder().name("Roxanne Price").active(1).rank(1).build())
            .daysToExpire(89)
            .owner("400")
            .build());

        final URI uri = URI.create("/api/v1/bureau/staff");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<StaffListDto> response = template.exchange(requestEntity, StaffListDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getActiveStaff()).isNotNull().hasSize(6)
            .isSortedAccordingTo(Comparator.comparing(StaffDto::getName))
            .containsAll(ACTIVE_STAFF);
        assertThat(response.getBody().getData().getInactiveStaff()).isNotNull().hasSize(3)
            .isSortedAccordingTo(Comparator.comparing(StaffDto::getName))
            .containsAll(INACTIVE_STAFF);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffRepositoryTest_findByActiveOrderByNameAsc.sql")
    public void getAll_unhappyPath_notATeamLeader() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("jpowers")
            .staff(BureauJWTPayload.Staff.builder().name("Joanna Powers").active(1).rank(0).build())
            .daysToExpire(89)
            .owner("400")
            .build());

        final URI uri = URI.create("/api/v1/bureau/staff");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<StaffListDto> response = template.exchange(requestEntity, StaffListDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffRepositoryTest_findByActiveOrderByNameAsc.sql")
    public void getOne_happyPath() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("rprice")
            .staff(BureauJWTPayload.Staff.builder().name("Roxanne Price").active(1).rank(1).build())
            .daysToExpire(89)
            .owner("400")
            .build());

        final URI uri = URI.create("/api/v1/bureau/staff/jpowers");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<StaffDetailDto> response = template.exchange(requestEntity, StaffDetailDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody().getData().getName()).isEqualTo("Joanna Powers");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffRepositoryTest_findByActiveOrderByNameAsc.sql")
    public void getOne_errorPath_noResult() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("rprice")
            .staff(BureauJWTPayload.Staff.builder().name("Roxanne Price").active(1).rank(1).build())
            .daysToExpire(89)
            .owner("400")
            .build());

        final URI uri = URI.create("/api/v1/bureau/staff/nresult");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<StaffDetailDto> response = template.exchange(requestEntity, StaffDetailDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffRepositoryTest_findByActiveOrderByNameAsc.sql")
    public void getOne_errorPath_notATeamLeader() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("jpowers")
            .staff(BureauJWTPayload.Staff.builder().name("Joanna Powers").active(1).rank(0).build())
            .daysToExpire(89)
            .owner("400")
            .build());

        final URI uri = URI.create("/api/v1/bureau/staff");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<StaffDetailDto> response = template.exchange(requestEntity, StaffDetailDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_happy.sql")
    @Test
    public void changeStaffAssignment_happy() throws Exception {
        final String description = "Update staff assignment happy path";

        final URI uri = URI.create("/api/v1/bureau/staff/assign");

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("smcbob")
            .daysToExpire(89)
            .owner("400")
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(0);// initial version
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '644892530'", String.class)).isEqualTo("jmcbob");

        final StaffAssignmentRequestDto dto = StaffAssignmentRequestDto.builder()
            .responseJurorNumber("644892530")
            .assignTo("smcbob")
            .version(0)// matches DB
            .build();

        // expecting happy response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<StaffAssignmentRequestDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<StaffAssignmentResponseDto> exchange = template.exchange(requestEntity,
            StaffAssignmentResponseDto.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody().getAssignmentDate()).isToday();
        assertThat(exchange.getBody()).extracting(StaffAssignmentResponseDto::getJurorResponse,
                StaffAssignmentResponseDto::getAssignedBy, StaffAssignmentResponseDto::getAssignedTo)
            .containsExactly("644892530", "smcbob", "smcbob");

        // assert the changes to DOB were applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(1);// incremented versions
        //assert the assignment has changed!
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '644892530'", String.class)).isEqualTo("smcbob");
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_happy.sql")
    @Test
    public void changeStaffAssignment_toBacklog_happy() throws Exception {
        final String description = "Update staff assignment happy path";

        final URI uri = URI.create("/api/v1/bureau/staff/assign");

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("smcbob")
            .daysToExpire(89)
            .owner("400")
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(0);// initial version
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '644892530'", String.class)).isEqualTo("jmcbob");

        final StaffAssignmentRequestDto dto = StaffAssignmentRequestDto.builder()
            .responseJurorNumber("644892530")
            .assignTo(null)
            .version(0)// matches DB
            .build();

        // expecting happy response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<StaffAssignmentRequestDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<StaffAssignmentResponseDto> exchange = template.exchange(requestEntity,
            StaffAssignmentResponseDto.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody().getAssignmentDate()).isToday();
        assertThat(exchange.getBody()).extracting(StaffAssignmentResponseDto::getJurorResponse,
                StaffAssignmentResponseDto::getAssignedBy, StaffAssignmentResponseDto::getAssignedTo)
            .containsExactly("644892530", "smcbob", null);

        // assert the changes to DOB were applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(1);// incremented versions
        //assert the assignment has changed!
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '644892530'", String.class)).isEqualTo(null);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_unhappy_badStatus.sql")
    @Test
    public void changeStaffAssignment_toBacklog_unhappy_badStatus_awaitingJuror() throws Exception {
        final String description = "Update staff assignment unhappy path, awaiting-juror";

        final URI uri = URI.create("/api/v1/bureau/staff/assign");

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("smcbob")
            .daysToExpire(89)
            .owner("400")
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(0);// initial version
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '644892530'", String.class)).isEqualTo("jmcbob");

        final StaffAssignmentRequestDto dto = StaffAssignmentRequestDto.builder()
            .responseJurorNumber("644892530")
            .assignTo(null)
            .version(0)// matches DB
            .build();

        // expecting unhappy response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<StaffAssignmentRequestDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<StaffAssignmentResponseDto> exchange = template.exchange(requestEntity,
            StaffAssignmentResponseDto.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // assert no changes to db were applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(0);// no incremented version
        //assert the assignment has not changed!
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '644892530'", String.class)).isEqualTo("jmcbob");
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_unhappy_badStatus.sql")
    @Test
    public void changeStaffAssignment_toBacklog_unhappy_badStatus_awaitingCourt() throws Exception {
        final String description = "Update staff assignment unhappy path, awaiting-court";

        final URI uri = URI.create("/api/v1/bureau/staff/assign");

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("smcbob")
            .daysToExpire(89)
            .owner("400")
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(0);// initial version
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '644892530'", String.class)).isEqualTo("jmcbob");

        final StaffAssignmentRequestDto dto = StaffAssignmentRequestDto.builder()
            .responseJurorNumber("586856851")
            .assignTo(null)
            .version(0)// matches DB
            .build();

        // expecting unhappy response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<StaffAssignmentRequestDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<StaffAssignmentResponseDto> exchange = template.exchange(requestEntity,
            StaffAssignmentResponseDto.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // assert no changes to db were applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(0);// no incremented version
        //assert the assignment has not changed!
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '644892530'", String.class)).isEqualTo("jmcbob");
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_unhappy_badStatus.sql")
    @Test
    public void changeStaffAssignment_toBacklog_unhappy_badStatus_awaitingTranslation() throws Exception {
        final String description = "Update staff assignment unhappy path, awaiting-translation";

        final URI uri = URI.create("/api/v1/bureau/staff/assign");

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("smcbob")
            .daysToExpire(89)
            .owner("400")
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(0);// initial version
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '644892530'", String.class)).isEqualTo("jmcbob");

        final StaffAssignmentRequestDto dto = StaffAssignmentRequestDto.builder()
            .responseJurorNumber("586856852")
            .assignTo(null)
            .version(0)// matches DB
            .build();

        // expecting unhappy response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<StaffAssignmentRequestDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<StaffAssignmentResponseDto> exchange = template.exchange(requestEntity,
            StaffAssignmentResponseDto.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // assert no changes to db were applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(0);// no incremented version
        //assert the assignment has not changed!
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '644892530'", String.class)).isEqualTo("jmcbob");
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_unhappy_urgent.sql")
    @Test
    public void changeStaffAssignment_toBacklog_unhappy_urgent() throws Exception {
        final String description = "Update staff assignment unhappy path, urgent";

        final URI uri = URI.create("/api/v1/bureau/staff/assign");

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("smcbob")
            .daysToExpire(89)
            .owner("400")
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(0);// initial version
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '644892530'", String.class)).isEqualTo("jmcbob");

        final StaffAssignmentRequestDto dto = StaffAssignmentRequestDto.builder()
            .responseJurorNumber("644892530")
            .assignTo(null)
            .version(0)// matches DB
            .build();

        // expecting unhappy response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<StaffAssignmentRequestDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<StaffAssignmentResponseDto> exchange = template.exchange(requestEntity,
            StaffAssignmentResponseDto.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // assert no changes to db were applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(0);// no incremented version
        //assert the assignment has not changed!
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '644892530'", String.class)).isEqualTo("jmcbob");
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_unhappy_urgent.sql")
    @Test
    public void changeStaffAssignment_toBacklog_unhappy_superUrgent() throws Exception {
        final String description = "Update staff assignment unhappy path, super-urgent";

        final URI uri = URI.create("/api/v1/bureau/staff/assign");

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("smcbob")
            .daysToExpire(89)
            .owner("400")
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(0);// initial version
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '586856851'", String.class)).isEqualTo("jmcbob");

        final StaffAssignmentRequestDto dto = StaffAssignmentRequestDto.builder()
            .responseJurorNumber("586856851")
            .assignTo(null)
            .version(0)// matches DB
            .build();

        // expecting unhappy response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<StaffAssignmentRequestDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<StaffAssignmentResponseDto> exchange = template.exchange(requestEntity,
            StaffAssignmentResponseDto.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // assert no changes to db were applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(0);// no incremented version
        //assert the assignment has not changed!
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '586856851'", String.class)).isEqualTo("jmcbob");
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_happy.sql")
    @Test
    public void changeStaffAssignment_toBacklog_unhappy_notTeamLeader() throws Exception {
        final String description = "Update staff assignment unhappy path, not team leader";

        final URI uri = URI.create("/api/v1/bureau/staff/assign");

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("jmcbob")
            .daysToExpire(89)
            .owner("400")
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(0);// initial version
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '644892530'", String.class)).isEqualTo("jmcbob");

        final StaffAssignmentRequestDto dto = StaffAssignmentRequestDto.builder()
            .responseJurorNumber("586856852")
            .assignTo(null)
            .version(0)// matches DB
            .build();

        // expecting unhappy response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<StaffAssignmentRequestDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<StaffAssignmentResponseDto> exchange = template.exchange(requestEntity,
            StaffAssignmentResponseDto.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // assert no changes to db were applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(0);// no incremented version
        //assert the assignment has not changed!
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '644892530'", String.class)).isEqualTo("jmcbob");
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_unhappy_optimisticLockingMismatch.sql")
    @Test
    public void changeStaffAssignment_unhappy_optimisticLockingMismatch() throws Exception {
        final String description = "Update staff assignment unhappy path - optimistic locking";

        final URI uri = URI.create("/api/v1/bureau/staff/assign");

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("smcbob")
            .daysToExpire(89)
            .owner("400")
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(2);// initial version
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '644892530'", String.class)).isEqualTo("jmcbob");

        final StaffAssignmentRequestDto dto = StaffAssignmentRequestDto.builder()
            .responseJurorNumber("644892530")
            .assignTo("smcbob")
            .version(0)// does not match DB
            .build();

        // expecting an error response with an optimistic locking error.
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<StaffAssignmentRequestDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);// custom response code from controller
        assertThat(exchange.getBody().getException()).isEqualTo(BureauOptimisticLockingException.class.getName());

        // assert the change to DOB was not applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT VERSION FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            2);// initial version
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '644892530'", String.class)).isEqualTo("jmcbob");
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffRepositoryTest.staffRepository_happy_findActiveStaffMembers.sql")
    @Test
    public void activeStaffRoster_happy() throws Exception {
        final String description = "Active staff roster happy path";

        final URI uri = URI.create("/api/v1/bureau/staff/roster");

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("smcbob")
            .daysToExpire(89)
            .owner("400")
            .build());

        final int EXPECTED_STAFF_INCLUDING_AUTO_USER = 6;

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class))
            .isEqualTo(EXPECTED_STAFF_INCLUDING_AUTO_USER);

        // expecting happy response with inactive staff filtered out
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final ResponseEntity<StaffRosterResponseDto> exchange = template.exchange(new RequestEntity<Void>(httpHeaders,
            HttpMethod.GET, uri), StaffRosterResponseDto.class);

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody().getData())
            .describedAs("Dto contains the active staff data alphabetically ordered by name without"
                + JurorDigitalApplication.AUTO_USER)
            .hasSize(4)
            .extracting(StaffRosterResponseDto.StaffDto::getName, StaffRosterResponseDto.StaffDto::getLogin)
            .containsExactly(
                tuple("Alison Active", "AACTIVE5"),
                tuple("Andy Active", "AACTIVE123"),
                tuple("Bobbie McActive", "MCBBOBBIE"),
                tuple("Xavier Activez", "ACTIVEX")
            )
            .doesNotContain(tuple("Joe Inactive", "JINACTIVE1"),
                tuple("Auto Processing User", "AUTO")
            )
        ;

        // assert db state
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class))
            .isEqualTo(EXPECTED_STAFF_INCLUDING_AUTO_USER);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauStaffControllerTest_getStaffAssignments.sql")
    @Test
    public void getStaffAssignments_happy() throws Exception {
        final String description = "Get multiple staff assignments, happy path";

        final URI uri = URI.create("/api/v1/bureau/staff/assignments-multi");

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("smcbob")
            .daysToExpire(89)
            .owner("400")
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(6);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            6);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(0);// initial versions all zero

        List<String> jurorNumbers = new ArrayList<>();
        jurorNumbers.add("111111000");
        jurorNumbers.add("111111001");
        jurorNumbers.add("111111002");
        jurorNumbers.add("999999998"); // doesn't exist
        jurorNumbers.add("999999999"); // doesn't exist

        final AssignmentsMultiRequestDto dto = new AssignmentsMultiRequestDto(jurorNumbers);

        // expect response of type 200 for success
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<AssignmentsMultiRequestDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<AssignmentsListDto> exchange = template.exchange(requestEntity, AssignmentsListDto.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);

        List<AssignmentListDataDto> responseData = exchange.getBody().getData();
        assertThat(responseData.size()).isEqualTo(3);

        // assert data is returned correctly
        assertThat(responseData.get(0).getAssignedTo()).isNull();
        assertThat(responseData.get(0).getJurorNumber()).isEqualTo("111111000");
        assertThat(responseData.get(0).getVersion()).isEqualTo(0);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_happy.sql")
    @Test
    public void changeMultipleAssignments_happy() throws Exception {
        final String description = "Update multiple staff assignments happy path";

        final URI uri = URI.create("/api/v1/bureau/staff/assign-multi");

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("smcbob")
            .daysToExpire(89)
            .owner("400")
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(0);// initial versions all zero
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '644892530'", String.class)).isEqualTo("jmcbob");
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '586856851'", String.class)).isNullOrEmpty();

        final MultipleStaffAssignmentDto dto = MultipleStaffAssignmentDto.builder()
            .assignTo("smcbob")
            .responses(Arrays.asList(
                MultipleStaffAssignmentDto.ResponseMetadata.builder()
                    .responseJurorNumber("644892530")
                    .version(0)
                    .build(),
                MultipleStaffAssignmentDto.ResponseMetadata.builder()
                    .responseJurorNumber("586856851")
                    .version(0)
                    .build()))
            .build();

        // expect void response of type 204 for success
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<MultipleStaffAssignmentDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<OperationFailureListDto> exchange = template.exchange(requestEntity,
            OperationFailureListDto.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(exchange.getBody().getFailureDtos()).isNotNull().hasSize(0);

        // assert changes to db
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(2);// incremented versions
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '644892530'", String.class)).isEqualTo("smcbob");
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '586856851'", String.class)).isEqualTo("smcbob");
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_unhappy_badStatus.sql")
    @Test
    public void changeMultipleAssignments_unhappy_toBacklog_badStatus() throws Exception {
        final String description = "Update multiple staff assignments unhappy path, as at least one response is not " +
            "TODO";

        final URI uri = URI.create("/api/v1/bureau/staff/assign-multi");

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("smcbob")
            .daysToExpire(89)
            .owner("400")
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(0);// initial versions all zero
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '586856853'", String.class)).isEqualTo("jmcbob");
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '586856852'", String.class)).isEqualTo("jmcbob");

        final MultipleStaffAssignmentDto dto = MultipleStaffAssignmentDto.builder()
            .assignTo(null)
            .responses(Arrays.asList(
                MultipleStaffAssignmentDto.ResponseMetadata.builder()
                    .responseJurorNumber("586856853") // status: TO-DO
                    .version(0)
                    .build(),
                MultipleStaffAssignmentDto.ResponseMetadata.builder()
                    .responseJurorNumber("586856852") // status: AWAITING_TRANSLATION, will throw exception
                    .version(0)
                    .build()))
            .build();

        // expect accepted response as one will assign and one won't
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<MultipleStaffAssignmentDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(exchange.getBody()).isNotNull();

        // assert that there were some changes to db
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '586856853'", String.class)).isNull();
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '586856852'", String.class)).isEqualTo("jmcbob");
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_unhappy_urgent.sql")
    @Test
    public void changeMultipleAssignments_unhappy_toBacklog_urgents() throws Exception {
        final String description = "Update multiple staff assignments unhappy path, as at least one response is Urgent";

        final URI uri = URI.create("/api/v1/bureau/staff/assign-multi");

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("smcbob")
            .daysToExpire(89)
            .owner("400")
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(0);// initial versions all zero
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '586856853'", String.class)).isEqualTo("jmcbob");
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '586856852'", String.class)).isEqualTo("jmcbob");

        final MultipleStaffAssignmentDto dto = MultipleStaffAssignmentDto.builder()
            .assignTo(null)
            .responses(Arrays.asList(
                MultipleStaffAssignmentDto.ResponseMetadata.builder()
                    .responseJurorNumber("644892530") // Urgent, will throw exception
                    .version(0)
                    .build(),
                MultipleStaffAssignmentDto.ResponseMetadata.builder()
                    .responseJurorNumber("586856851") // Super-Urgent, will throw exception
                    .version(0)
                    .build(),
                MultipleStaffAssignmentDto.ResponseMetadata.builder()
                    .responseJurorNumber("586856852") // will be processed successfully
                    .version(0)
                    .build(),
                MultipleStaffAssignmentDto.ResponseMetadata.builder()
                    .responseJurorNumber("586856853") // will be processed successfully
                    .version(0)
                    .build()))
            .build();

        // expect accepted response with a StaffAssignmentException
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<MultipleStaffAssignmentDto> requestEntity = new RequestEntity<>(dto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<OperationFailureListDto> exchange = template.exchange(requestEntity,
            OperationFailureListDto.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        // assert body of response has correct details
        OperationFailureListDto failureListDto = exchange.getBody();
        assertThat(failureListDto.getFailureDtos().size()).isEqualTo(2);
        assertThat(failureListDto.getFailureDtos().get(0).getJurorNumber()).isEqualTo("644892530");
        assertThat(failureListDto.getFailureDtos().get(0).getReason()).isEqualToIgnoringCase("URGENT");
        assertThat(failureListDto.getFailureDtos().get(1).getJurorNumber()).isEqualTo("586856851");
        assertThat(failureListDto.getFailureDtos().get(1).getReason()).isEqualToIgnoringCase("SUPER_URGENT");

        // assert that there were some changes to db
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR.POOL", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM JUROR_DIGITAL.JUROR_RESPONSE",
            Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '644892530'", String.class)).isEqualTo("jmcbob");
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '586856851'", String.class)).isEqualTo("jmcbob");
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '586856852'", String.class)).isNull();
        assertThat(jdbcTemplate.queryForObject("SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " +
            "JUROR_NUMBER = '586856853'", String.class)).isNull();
    }


    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauResponseController_reassign.sql")
    public void reassignStaffMembersResponses_happyPath() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("kfry")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name("kfry")
                .courts(Collections.singletonList("123"))
                .build())
            .build());

        final String staffToDeactivate = "ncrawford";
        final String staffToReassignUrgentsTo = "kfry";
        final String staffToReassignPendingsTo = "lrees";
        final URI uri = URI.create("/api/v1/bureau/responses/reassign");

        ReassignResponsesDto reassignDto = ReassignResponsesDto.builder()
            .staffToDeactivate(staffToDeactivate)
            .urgentsLogin(staffToReassignUrgentsTo)
            .pendingLogin(staffToReassignPendingsTo)
            .todoLogin(null) // assign to-do's to backlog
            .build();

        // make database assertions before making request
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE " +
                "WHERE STAFF_LOGIN='" + staffToDeactivate + "'", Integer.class))
            .as("Officer should have assigned responses")
            .isEqualTo(9);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<ReassignResponsesDto> requestEntity = new RequestEntity<>(reassignDto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);
        softly.assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);

        // assertions after request is carried out
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE " +
                "WHERE STAFF_LOGIN='" + staffToDeactivate + "' AND NOT PROCESSING_STATUS='CLOSED'", Integer.class))
            .as("Officer should have no assigned responses")
            .isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE " +
                "WHERE STAFF_LOGIN='" + staffToDeactivate + "' AND PROCESSING_STATUS='CLOSED'", Integer.class))
            .as("Officer should still have closed responses that are unaffected")
            .isEqualTo(2);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE " +
                "WHERE STAFF_LOGIN='" + staffToReassignUrgentsTo + "'", Integer.class))
            .as("Urgents should be re-assigned to specified user")
            .isEqualTo(2);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE " +
                "WHERE STAFF_LOGIN='" + staffToReassignPendingsTo + "'", Integer.class))
            .as("Pending responses should be re-assigned to specified user")
            .isEqualTo(4);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE " +
                "WHERE STAFF_LOGIN IS NULL", Integer.class))
            .as("Todo responses should be re-assigned to specified user (backlog)")
            .isEqualTo(2);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauResponseController_reassign.sql")
    public void reassignStaffMembersResponses_unhappyPath_sendUrgentsToBacklog() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("kfry")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name("kfry")
                .courts(Collections.singletonList("123"))
                .build())
            .build());

        final String staffToDeactivate = "ncrawford";
        final String staffToReassignPendingsTo = "lrees";
        final URI uri = URI.create("/api/v1/bureau/responses/reassign");

        ReassignResponsesDto reassignDto = ReassignResponsesDto.builder()
            .staffToDeactivate(staffToDeactivate)
            .urgentsLogin(null)
            .pendingLogin(staffToReassignPendingsTo)
            .todoLogin(null) // assign to-do's to backlog
            .build();

        // make database assertions before making request
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE " +
                "WHERE STAFF_LOGIN='" + staffToDeactivate + "'", Integer.class))
            .as("Officer should have assigned responses")
            .isEqualTo(9);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<ReassignResponsesDto> requestEntity = new RequestEntity<>(reassignDto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);
        softly.assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // assertions after request is carried out
        softly.assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE " +
                "WHERE STAFF_LOGIN='" + staffToDeactivate + "'", Integer.class))
            .as("Officer should still have assigned responses")
            .isEqualTo(9);

        
        softly.assertThat(jdbcTemplate.queryForObject("SELECT active FROM juror_mod.users " +
                "WHERE username='" + staffToDeactivate + "'", Boolean.class))
            .as("Staff member should not be deactivated")
            .isEqualTo(true);
        softly.assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT " +
                "WHERE LOGIN='" + staffToDeactivate + "'", Integer.class))
            .as("There should be no audit entry for the staff member")
            .isEqualTo(0);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauResponseController_reassign.sql")
    public void reassignStaffMembersResponses_unhappyPath_cantFindStaff() throws Exception {
        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("99")
            .passwordWarning(false)
            .login("kfry")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name("kfry")
                .courts(Collections.singletonList("123"))
                .build())
            .build());

        final String staffToReassignFrom = "NON-EXISTENT-USER";
        final String staffToReassignPendingsTo = "lrees";
        final URI uri = URI.create("/api/v1/bureau/responses/reassign");

        ReassignResponsesDto reassignDto = ReassignResponsesDto.builder()
            .staffToDeactivate(staffToReassignFrom)
            .urgentsLogin(null)
            .pendingLogin(staffToReassignPendingsTo)
            .todoLogin(null) // assign to-do's to backlog
            .build();

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<ReassignResponsesDto> requestEntity = new RequestEntity<>(reassignDto, httpHeaders,
            HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/StaffManagementControllerTest_createStaffMember_happy.sql"
    })
    @Test
    public void createStaffMember_happy_court() throws Exception {
        final String description = "Create new staff member happy path";

        final URI uri = URI.create("/api/v1/bureau/staff");

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("5")
            .passwordWarning(false)
            .login("EXISTING1")
            .daysToExpire(89)
            .owner("415")//Note court owner
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name("EXISTING1")
                .courts(Collections.singletonList("123"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(0);

        final String _LOGIN = "NNEWBIE123";
        final String _NAME = "Newguy McNewbie";
        final boolean _TEAM_LEADER = true;
        final boolean _INACTIVE = false;
        final long _TEAM_ID = 3L;
        final StaffMemberCrudRequestDto postDto = new StaffMemberCrudRequestDto(_LOGIN, _NAME, _TEAM_LEADER,
            _INACTIVE, _TEAM_ID, null);//NOTE: null version

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final RequestEntity<StaffMemberCrudRequestDto> requestEntity = new RequestEntity<>(postDto, httpHeaders,
            HttpMethod.POST, uri);
        final ResponseEntity<StaffMemberCrudResponseDto> exchange = template.exchange(requestEntity, StaffMemberCrudResponseDto.class);

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(exchange.getBody())
            .describedAs("Dto contains the staff member just created")
            .extracting(StaffMemberCrudResponseDto::getLogin,
                StaffMemberCrudResponseDto::getName,
                StaffMemberCrudResponseDto::isTeamLeader,
                StaffMemberCrudResponseDto::isActive,
                StaffMemberCrudResponseDto::getTeam,
                StaffMemberCrudResponseDto::getCourt1,
                StaffMemberCrudResponseDto::getCourt2,
                StaffMemberCrudResponseDto::getCourt3,
                StaffMemberCrudResponseDto::getCourt4,
                StaffMemberCrudResponseDto::getCourt5,
                StaffMemberCrudResponseDto::getCourt6,
                StaffMemberCrudResponseDto::getCourt7,
                StaffMemberCrudResponseDto::getCourt8,
                StaffMemberCrudResponseDto::getCourt9,
                StaffMemberCrudResponseDto::getCourt10,
                StaffMemberCrudResponseDto::getVersion
            )
            .containsExactly(
                _LOGIN, _NAME, _TEAM_LEADER, _INACTIVE, _TEAM_ID, "415", "462", "767",  null, null,
                null, null, null, null, null, 0// NOTE: version set to 0 by hibernate
            )
        ;

        // assert the DB change was applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(1);
    }
    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/StaffManagementControllerTest_createStaffMember_happy.sql"
    })
    @Test
    public void createStaffMember_happy() throws Exception {
        final String description = "Create new staff member happy path";

        final URI uri = URI.create("/api/v1/bureau/staff");

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("5")
            .passwordWarning(false)
            .login("EXISTING1")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name("EXISTING1")
                .courts(Collections.singletonList("123"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(0);

        final String _LOGIN = "NNEWBIE123";
        final String _NAME = "Newguy McNewbie";
        final boolean _TEAM_LEADER = true;
        final boolean _INACTIVE = false;
        final long _TEAM_ID = 3L;
        final String _COURT_1 = "400";
        final StaffMemberCrudRequestDto postDto = new StaffMemberCrudRequestDto(_LOGIN, _NAME, _TEAM_LEADER,
            _INACTIVE, _TEAM_ID, null);//NOTE: null version

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final RequestEntity<StaffMemberCrudRequestDto> requestEntity = new RequestEntity<>(postDto, httpHeaders,
            HttpMethod.POST, uri);
        final ResponseEntity<StaffMemberCrudResponseDto> exchange = template.exchange(requestEntity, StaffMemberCrudResponseDto.class);

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(exchange.getBody())
            .describedAs("Dto contains the staff member just created")
            .extracting(StaffMemberCrudResponseDto::getLogin,
                StaffMemberCrudResponseDto::getName,
                StaffMemberCrudResponseDto::isTeamLeader,
                StaffMemberCrudResponseDto::isActive,
                StaffMemberCrudResponseDto::getTeam,
                StaffMemberCrudResponseDto::getCourt1,
                StaffMemberCrudResponseDto::getCourt2,
                StaffMemberCrudResponseDto::getCourt3,
                StaffMemberCrudResponseDto::getCourt4,
                StaffMemberCrudResponseDto::getCourt5,
                StaffMemberCrudResponseDto::getCourt6,
                StaffMemberCrudResponseDto::getCourt7,
                StaffMemberCrudResponseDto::getCourt8,
                StaffMemberCrudResponseDto::getCourt9,
                StaffMemberCrudResponseDto::getCourt10,
                StaffMemberCrudResponseDto::getVersion
            )
            .containsExactly(
                _LOGIN, _NAME, _TEAM_LEADER, _INACTIVE, _TEAM_ID, _COURT_1, null, null, null, null,
                null, null, null, null, null, 0// NOTE: version set to 0 by hibernate
            )
        ;

        // assert the DB change was applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(1);
    }

    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/StaffManagementControllerTest_createStaffMember_happy.sql"
    })
    @Test
    public void createStaffMember_unhappy_not_a_team_leader() throws Exception {
        final String description = "Create new staff member unhappy path - not a team leader";

        final URI uri = URI.create("/api/v1/bureau/staff");

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login("EXISTING2")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(0)//NOT A TEAM LEADER
                .name("EXISTING2")
                .courts(Collections.singletonList("123"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(0);

        final String _LOGIN = "NNEWBIE123";
        final String _NAME = "Newguy McNewbie";
        final boolean _TEAM_LEADER = true;
        final boolean _INACTIVE = false;
        final long _TEAM_ID = 3L;
        final String _COURT_1 = "400";
        final StaffMemberCrudRequestDto  postDto = new StaffMemberCrudRequestDto(_LOGIN, _NAME, _TEAM_LEADER,
            _INACTIVE, _TEAM_ID, null);//NOTE: null version

        // expecting an error response with unauthorized.
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final RequestEntity<StaffMemberCrudRequestDto> requestEntity = new RequestEntity<>(postDto, httpHeaders,
            HttpMethod.POST, uri);
        final ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exchange.getBody().getException()).contains(AccessDeniedException.class.getName());

        // assert the change to DB was not applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(0);
    }

    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/StaffManagementControllerTest_createStaffMember_happy.sql"
    })
    @Test
    public void createStaffMember_unhappy_staff_username_already_exists() throws Exception {
        final String description = "Create new staff member unhappy path - staff username already in use";

        final URI uri = URI.create("/api/v1/bureau/staff");

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("5")
            .passwordWarning(false)
            .login("EXISTING1")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name("EXISTING1")
                .courts(Collections.singletonList("123"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(0);

        final String _LOGIN = "EXISTING2";
        final String _NAME = "Newguy McNewbie";
        final boolean _TEAM_LEADER = true;
        final boolean _INACTIVE = false;
        final long _TEAM_ID = 3L;
        final String _COURT_1 = "400";
        final StaffMemberCrudRequestDto  postDto = new StaffMemberCrudRequestDto(_LOGIN, _NAME, _TEAM_LEADER,
            _INACTIVE, _TEAM_ID, null);//NOTE: null version

        // expecting an error response with unauthorized.
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final RequestEntity<StaffMemberCrudRequestDto> requestEntity = new RequestEntity<>(postDto, httpHeaders,
            HttpMethod.POST, uri);
        final ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exchange.getBody().getException()).contains(StaffMemberCrudException.class.getName());
        assertThat(exchange.getBody().getMessage()).contains("Juror username EXISTING2 has already been allocated to " +
            "Alison Active");

        // assert the change to DB was not applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(0);
    }

    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/StaffManagementControllerTest_createStaffMember_happy.sql"
    })
    @Test
    public void createStaffMember_unhappy_existing_invalid_team() throws Exception {
        final String description = "Create new staff member unhappy path - invalid team";

        final URI uri = URI.create("/api/v1/bureau/staff");

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("5")
            .passwordWarning(false)
            .login("EXISTING1")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name("EXISTING1")
                .courts(Collections.singletonList("123"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(0);

        final String _LOGIN = "NNEWBIE123";
        final String _NAME = "Newguy McNewbie";
        final boolean _TEAM_LEADER = true;
        final boolean _INACTIVE = false;
        final long _INVALID_TEAM_ID = 999999L;
        final String _COURT_1 = "400";
        final StaffMemberCrudRequestDto  postDto = new StaffMemberCrudRequestDto(_LOGIN, _NAME, _TEAM_LEADER,
            _INACTIVE, _INVALID_TEAM_ID, null);//NOTE: null version

        // expecting an error response with unauthorized.
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final RequestEntity<StaffMemberCrudRequestDto> requestEntity = new RequestEntity<>(postDto, httpHeaders,
            HttpMethod.POST, uri);
        final ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exchange.getBody().getException()).contains(StaffMemberCrudException.class.getName());
        assertThat(exchange.getBody().getMessage()).contains("Invalid team id: 999999");

        // assert the change to DB was not applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(0);
    }

    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/StaffManagementControllerTest_createStaffMember_happy.sql"
    })
    @Test
    public void createStaffMember_unhappy_null_login() throws Exception {
        final String description = "Create new staff member unhappy path - null login";

        final URI uri = URI.create("/api/v1/bureau/staff");

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("5")
            .passwordWarning(false)
            .login("EXISTING1")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name("EXISTING1")
                .courts(Collections.singletonList("123"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(0);

        final String _LOGIN = null;
        final String _NAME = "Newguy McNewbie";
        final boolean _TEAM_LEADER = true;
        final boolean _INACTIVE = false;
        final long _TEAM_ID = 3L;
        final String _COURT_1 = "400";
        final StaffMemberCrudRequestDto  postDto = new StaffMemberCrudRequestDto(_LOGIN, _NAME, _TEAM_LEADER,
            _INACTIVE, _TEAM_ID, null);//NOTE: null version

        // expecting an error response with bad request.
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final RequestEntity<StaffMemberCrudRequestDto> requestEntity = new RequestEntity<>(postDto, httpHeaders,
            HttpMethod.POST, uri);
        final ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(exchange.getBody().getException()).contains(MethodArgumentNotValidException.class.getName());
        assertThat(exchange.getBody().getMessage()).contains("Validation failed for object='staffMemberCrudRequestDto'. " +
            "Error count: 1");

        // assert the change to DB was not applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(0);
    }
    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/StaffManagementControllerTest_createStaffMember_happy.sql"
    })
    @Test
    public void updateStaffMember_happy_court() throws Exception {
        final String description = "Update new staff member happy path";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("5")
            .passwordWarning(false)
            .login("EXISTING1")
            .daysToExpire(89)
            .owner("415")
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name("EXISTING1")
                .courts(Collections.singletonList("123"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users s WHERE username = " +
            "'EXISTING2'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT s.name FROM juror_mod.users s WHERE s.username = 'EXISTING2'"
            , String.class)).isEqualTo("Alison Active");
        assertThat(jdbcTemplate.queryForObject("SELECT s.active FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Boolean.class)).isEqualTo(false);
        assertThat(jdbcTemplate.queryForObject("SELECT s.team_id FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(0);

        final String _LOGIN = "EXISTING2";
        final String _NAME = "Alison Active";
        final boolean _TEAM_LEADER = false;
        final boolean _ACTIVE = true;
        final long _TEAM_ID = 3L;
        final StaffMemberCrudRequestDto postDto = new StaffMemberCrudRequestDto(_LOGIN, _NAME, _TEAM_LEADER,
            _ACTIVE, _TEAM_ID, 0);//NOTE: existing version

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/staff/" + _LOGIN);
        final RequestEntity<StaffMemberCrudRequestDto> requestEntity = new RequestEntity<>(postDto, httpHeaders,
            HttpMethod.PUT, uri);
        final ResponseEntity<StaffMemberCrudResponseDto> exchange = template.exchange(requestEntity, StaffMemberCrudResponseDto.class);

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(exchange.getBody())
            .describedAs("Dto contains the staff member just updated")
            .extracting(StaffMemberCrudResponseDto::getLogin,
                StaffMemberCrudResponseDto::getName,
                StaffMemberCrudResponseDto::isTeamLeader,
                StaffMemberCrudResponseDto::isActive,
                StaffMemberCrudResponseDto::getTeam,
                StaffMemberCrudResponseDto::getCourt1,
                StaffMemberCrudResponseDto::getCourt2,
                StaffMemberCrudResponseDto::getCourt3,
                StaffMemberCrudResponseDto::getCourt4,
                StaffMemberCrudResponseDto::getCourt5,
                StaffMemberCrudResponseDto::getCourt6,
                StaffMemberCrudResponseDto::getCourt7,
                StaffMemberCrudResponseDto::getCourt8,
                StaffMemberCrudResponseDto::getCourt9,
                StaffMemberCrudResponseDto::getCourt10,
                StaffMemberCrudResponseDto::getVersion
            )
            .containsExactly(
                _LOGIN, _NAME, _TEAM_LEADER, _ACTIVE, _TEAM_ID, "400", null,null, null, null,
                null, null, null, null, null, 1// NOTE: version incremented by hibernate
                    //NOTE: owner not updated this may need revisiting following user management stories
            )
        ;

        // assert the DB change was applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users s WHERE username = " +
            "'EXISTING2'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT s.name FROM juror_mod.users s WHERE s.username = 'EXISTING2'"
            , String.class)).isEqualTo(_NAME);
        assertThat(jdbcTemplate.queryForObject("SELECT s.active FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Boolean.class)).isEqualTo(_ACTIVE);
        assertThat(jdbcTemplate.queryForObject("SELECT s.team_id FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Integer.class)).isEqualTo((int) _TEAM_ID);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(1);
    }
    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/StaffManagementControllerTest_createStaffMember_happy.sql"
    })
    @Test
    public void updateStaffMember_happy() throws Exception {
        final String description = "Update new staff member happy path";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("5")
            .passwordWarning(false)
            .login("EXISTING1")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name("EXISTING1")
                .courts(Collections.singletonList("123"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users s WHERE username = " +
            "'EXISTING2'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT s.name FROM juror_mod.users s WHERE s.username = 'EXISTING2'"
            , String.class)).isEqualTo("Alison Active");
        assertThat(jdbcTemplate.queryForObject("SELECT s.active FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Boolean.class)).isEqualTo(false);
        assertThat(jdbcTemplate.queryForObject("SELECT s.team_id FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(0);

        final String _LOGIN = "EXISTING2";
        final String _NAME = "Alison Active";
        final boolean _TEAM_LEADER = false;
        final boolean _ACTIVE = true;
        final long _TEAM_ID = 3L;
        final String _COURT_1 = "400";
        final StaffMemberCrudRequestDto postDto = new StaffMemberCrudRequestDto(_LOGIN, _NAME, _TEAM_LEADER,
            _ACTIVE, _TEAM_ID, 0);//NOTE: existing version

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/staff/" + _LOGIN);
        final RequestEntity<StaffMemberCrudRequestDto> requestEntity = new RequestEntity<>(postDto, httpHeaders,
            HttpMethod.PUT, uri);
        final ResponseEntity<StaffMemberCrudResponseDto> exchange = template.exchange(requestEntity, StaffMemberCrudResponseDto.class);

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(exchange.getBody())
            .describedAs("Dto contains the staff member just updated")
            .extracting(StaffMemberCrudResponseDto::getLogin,
                StaffMemberCrudResponseDto::getName,
                StaffMemberCrudResponseDto::isTeamLeader,
                StaffMemberCrudResponseDto::isActive,
                StaffMemberCrudResponseDto::getTeam,
                StaffMemberCrudResponseDto::getCourt1,
                StaffMemberCrudResponseDto::getCourt2,
                StaffMemberCrudResponseDto::getCourt3,
                StaffMemberCrudResponseDto::getCourt4,
                StaffMemberCrudResponseDto::getCourt5,
                StaffMemberCrudResponseDto::getCourt6,
                StaffMemberCrudResponseDto::getCourt7,
                StaffMemberCrudResponseDto::getCourt8,
                StaffMemberCrudResponseDto::getCourt9,
                StaffMemberCrudResponseDto::getCourt10,
                StaffMemberCrudResponseDto::getVersion
            )
            .containsExactly(
                _LOGIN, _NAME, _TEAM_LEADER, _ACTIVE, _TEAM_ID, _COURT_1, null, null, null, null,
                null, null, null, null, null, 1// NOTE: version incremented by hibernate
            )
        ;

        // assert the DB change was applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users s WHERE username = " +
            "'EXISTING2'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT s.name FROM juror_mod.users s WHERE s.username = 'EXISTING2'"
            , String.class)).isEqualTo(_NAME);
        assertThat(jdbcTemplate.queryForObject("SELECT s.active FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Boolean.class)).isEqualTo(_ACTIVE);
        assertThat(jdbcTemplate.queryForObject("SELECT s.team_id FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Integer.class)).isEqualTo((int) _TEAM_ID);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(1);
    }

    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/StaffManagementControllerTest_createStaffMember_happy.sql"
    })
    @Test
    public void updateStaffMember_unhappy_not_a_team_leader() throws Exception {
        final String description = "Update staff member unhappy path - not a team leader";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login("EXISTING2")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(0)//NOT A TEAM LEADER
                .name("EXISTING2")
                .courts(Collections.singletonList("123"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users s WHERE username = " +
            "'EXISTING2'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT s.name FROM juror_mod.users s WHERE s.username = 'EXISTING2'"
            , String.class)).isEqualTo("Alison Active");
        assertThat(jdbcTemplate.queryForObject("SELECT s.active FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Boolean.class)).isEqualTo(false);
        assertThat(jdbcTemplate.queryForObject("SELECT s.team_id FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT s.version FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(0);

        final String _LOGIN = "EXISTING2";
        final String _NAME = "Alison Active";
        final boolean _TEAM_LEADER = false;
        final boolean _ACTIVE = true;
        final long _TEAM_ID = 3L;
        final String _COURT_1 = "400";
        final StaffMemberCrudRequestDto  postDto = new StaffMemberCrudRequestDto(_LOGIN, _NAME, _TEAM_LEADER,
            _ACTIVE, _TEAM_ID,0);//NOTE: existing version

        // expecting an error response with unauthorized.
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/staff/" + _LOGIN);
        final RequestEntity<StaffMemberCrudRequestDto> requestEntity = new RequestEntity<>(postDto, httpHeaders,
            HttpMethod.PUT, uri);
        final ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(exchange.getBody().getException()).contains(AccessDeniedException.class.getName());

        // assert the change to DB was not applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users s WHERE username = " +
            "'EXISTING2'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT s.name FROM juror_mod.users s WHERE s.username = 'EXISTING2'"
            , String.class)).isEqualTo("Alison Active");
        assertThat(jdbcTemplate.queryForObject("SELECT s.active FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Boolean.class)).isEqualTo(false);
        assertThat(jdbcTemplate.queryForObject("SELECT s.team_id FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT s.version FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(0);
    }

    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/StaffManagementControllerTest_createStaffMember_happy.sql"
    })
    @Test
    public void updateStaffMember_unhappy_user_does_not_exist() throws Exception {
        final String description = "Update staff member unhappy path - user does not exist";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("5")
            .passwordWarning(false)
            .login("EXISTING1")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name("EXISTING1")
                .courts(Collections.singletonList("123"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(0);

        final String _LOGIN = "NONEXISTANT";
        final String _NAME = "Notina Database";
        final boolean _TEAM_LEADER = false;
        final boolean _ACTIVE = true;
        final long _TEAM_ID = 3L;
        final String _COURT_1 = "400";
        final StaffMemberCrudRequestDto  postDto = new StaffMemberCrudRequestDto(_LOGIN, _NAME, _TEAM_LEADER,
            _ACTIVE, _TEAM_ID, 0);//NOTE: existing version

        // expecting an error response with unauthorized.
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/staff/" + _LOGIN);
        final RequestEntity<StaffMemberCrudRequestDto> requestEntity = new RequestEntity<>(postDto, httpHeaders,
            HttpMethod.PUT, uri);
        final ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exchange.getBody().getException()).contains(StaffMemberCrudException.class.getName());
        assertThat(exchange.getBody().getMessage()).contains("Juror username " + _LOGIN + "does not exist");

        // assert the change to DB was not applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(0);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest_createStaffMember_unhappy_optimisticLocking.sql")
    @Test
    public void updateStaffMember_unhappy_optimisticLockFailure() throws Exception {
        final String description = "Update staff member unhappy path - optimistic locking fail";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("1")
            .passwordWarning(false)
            .login("EXISTING2")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)// TEAM LEADER
                .name("EXISTING2")
                .courts(Collections.singletonList("123"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users s WHERE username = " +
            "'EXISTING2'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT s.name FROM juror_mod.users s WHERE s.username = 'EXISTING2'"
            , String.class)).isEqualTo("Alison Active");
        assertThat(jdbcTemplate.queryForObject("SELECT s.active FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Boolean.class)).isEqualTo(false);
        assertThat(jdbcTemplate.queryForObject("SELECT s.team_id FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT s.version FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(0);

        final String _LOGIN = "EXISTING2";
        final String _NAME = "Alison Active";
        final boolean _TEAM_LEADER = false;
        final boolean _ACTIVE = true;
        final long _TEAM_ID = 3L;
        final String _COURT_1 = "400";
        final StaffMemberCrudRequestDto  postDto = new StaffMemberCrudRequestDto(_LOGIN, _NAME, _TEAM_LEADER,
            _ACTIVE, _TEAM_ID, 1);//NOTE: behind existing version

        // expecting an error response with unauthorized.
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/staff/" + _LOGIN);
        final RequestEntity<StaffMemberCrudRequestDto> requestEntity = new RequestEntity<>(postDto, httpHeaders,
            HttpMethod.PUT, uri);
        final ResponseEntity<SpringBootErrorResponse> exchange = template.exchange(requestEntity,
            SpringBootErrorResponse.class);

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(exchange.getBody().getException()).contains(BureauOptimisticLockingException.class.getName());

        // assert the change to DB was not applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users s WHERE username = " +
            "'EXISTING2'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT s.name FROM juror_mod.users s WHERE s.username = 'EXISTING2'"
            , String.class)).isEqualTo("Alison Active");
        assertThat(jdbcTemplate.queryForObject("SELECT s.active FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Boolean.class)).isEqualTo(false);
        assertThat(jdbcTemplate.queryForObject("SELECT s.team_id FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT s.version FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(0);
    }

    @Sql({
        "/db/truncate.sql",
        "/db/standing_data.sql",
        "/db/StaffManagementControllerTest_createStaffMember_happy.sql"
    })
    @Test
    public void updateStaffMember_unhappy_login_shouldnt_be_validated() throws Exception {
        final String description = "Update new staff member unhappy path - login id shouldn't be validated";

        final String bureauJwt = mintBureauJwt(BureauJWTPayload.builder()
            .userLevel("5")
            .passwordWarning(false)
            .login("EXISTING1")
            .daysToExpire(89)
            .owner("400")
            .staff(BureauJWTPayload.Staff.builder()
                .active(1)
                .rank(1)
                .name("EXISTING1")
                .courts(Collections.singletonList("123"))
                .build())
            .build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users s WHERE username = " +
            "'EXISTING2'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT s.name FROM juror_mod.users s WHERE s.username = 'EXISTING2'"
            , String.class)).isEqualTo("Alison Active");
        assertThat(jdbcTemplate.queryForObject("SELECT s.active FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Boolean.class)).isEqualTo(false);
        assertThat(jdbcTemplate.queryForObject("SELECT s.team_id FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(0);

        final String _NULL_LOGIN = null;
        final String _LOGIN = "EXISTING2";
        final String _NAME = "Alison Active";
        final boolean _TEAM_LEADER = false;
        final boolean _ACTIVE = true;
        final long _TEAM_ID = 3L;
        final String _COURT_1 = "400";
        final StaffMemberCrudRequestDto  postDto = new StaffMemberCrudRequestDto(_NULL_LOGIN, _NAME, _TEAM_LEADER,
            _ACTIVE, _TEAM_ID, 0);//NOTE: existing version

        // expecting an accepted response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final URI uri = URI.create("/api/v1/bureau/staff/" + _LOGIN);
        final RequestEntity<StaffMemberCrudRequestDto> requestEntity = new RequestEntity<>(postDto, httpHeaders,
            HttpMethod.PUT, uri);
        final ResponseEntity<StaffMemberCrudResponseDto> exchange = template.exchange(requestEntity, StaffMemberCrudResponseDto.class);

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(exchange.getBody())
            .describedAs("Dto contains the staff member just updated")
            .extracting(StaffMemberCrudResponseDto::getLogin,
                StaffMemberCrudResponseDto::getName,
                StaffMemberCrudResponseDto::isTeamLeader,
                StaffMemberCrudResponseDto::isActive,
                StaffMemberCrudResponseDto::getTeam,
                StaffMemberCrudResponseDto::getCourt1,
                StaffMemberCrudResponseDto::getCourt2,
                StaffMemberCrudResponseDto::getCourt3,
                StaffMemberCrudResponseDto::getCourt4,
                StaffMemberCrudResponseDto::getCourt5,
                StaffMemberCrudResponseDto::getCourt6,
                StaffMemberCrudResponseDto::getCourt7,
                StaffMemberCrudResponseDto::getCourt8,
                StaffMemberCrudResponseDto::getCourt9,
                StaffMemberCrudResponseDto::getCourt10,
                StaffMemberCrudResponseDto::getVersion
            )
            .containsExactly(
                _LOGIN, _NAME, _TEAM_LEADER, _ACTIVE, _TEAM_ID, _COURT_1, null, null, null, null,
                null, null, null, null, null, 1// NOTE: version incremented by hibernate
            )
        ;

        // assert the DB change was applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.TEAM", Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users s WHERE username = " +
            "'EXISTING2'", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT s.name FROM juror_mod.users s WHERE s.username = 'EXISTING2'"
            , String.class)).isEqualTo(_NAME);
        assertThat(jdbcTemplate.queryForObject("SELECT s.active FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Boolean.class)).isEqualTo(_ACTIVE);
        assertThat(jdbcTemplate.queryForObject("SELECT s.team_id FROM juror_mod.users s WHERE s.username = " +
            "'EXISTING2'", Integer.class)).isEqualTo((int) _TEAM_ID);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_AUDIT", Integer.class)).isEqualTo(1);
    }
}
