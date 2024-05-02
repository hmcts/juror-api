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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.SpringBootErrorResponse;
import uk.gov.hmcts.juror.api.bureau.controller.request.AssignmentsMultiRequestDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.MultipleStaffAssignmentDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.ReassignResponsesDto;
import uk.gov.hmcts.juror.api.bureau.controller.request.StaffAssignmentRequestDto;
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
import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.domain.Role;
import uk.gov.hmcts.juror.api.moj.domain.UserType;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

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
            .build(),
        StaffDto.builder()
            .login("tsanchez").name("Todd Sanchez")
            .isActive(true).isTeamLeader(false)
            .team(TEAM_2).version(0)
            .build(),
        StaffDto.builder()
            .login("gbeck").name("Grant Beck")
            .isActive(true).isTeamLeader(false)
            .team(TEAM_3).version(0)
            .build(),
        StaffDto.builder()
            .login("rprice").name("Roxanne Price")
            .isActive(true).isTeamLeader(true)
            .team(TEAM_1).version(0)
            .build(),
        StaffDto.builder()
            .login("pbrewer").name("Preston Brewer")
            .isActive(true).isTeamLeader(true)
            .team(TEAM_2).version(0)
            .build(),
        StaffDto.builder()
            .login("acopeland").name("Amelia Copeland")
            .isActive(true).isTeamLeader(true)
            .team(TEAM_3).version(0)
            .build()
    );

    private static final List<StaffDto> INACTIVE_STAFF = Arrays.asList(
        StaffDto.builder()
            .login("jphillips")
            .name("Joan Phillips")
            .isActive(false).isTeamLeader(false)
            .team(TEAM_1).version(0)
            .build(),
        StaffDto.builder()
            .login("srogers")
            .name("Shawn Rogers")
            .isActive(false).isTeamLeader(false)
            .team(TEAM_2).version(0)
            .build(),
        StaffDto.builder()
            .login("pbrooks")
            .name("Paul Brooks")
            .isActive(false).isTeamLeader(false)
            .team(TEAM_3).version(0)
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
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffRepositoryTest_findByActiveOrderByNameAsc.sql")
    public void getAll_happyPath() throws Exception {
        final String bureauJwt = mintBureauJwt(
            BureauJwtPayload.builder()
                .userType(UserType.BUREAU)
                .roles(Collections.singleton(Role.MANAGER))
                .login("rprice")
                .staff(BureauJwtPayload.Staff.builder().name("Roxanne Price").active(1).build())
                .owner("400").build());

        final URI uri = URI.create("/api/v1/bureau/staff");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<StaffListDto> response = template.exchange(requestEntity, StaffListDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isNotNull();
        assertThat(response.getBody().getData().getActiveStaff()).isNotNull().hasSize(6)
            .isSortedAccordingTo(Comparator.comparing(StaffDto::getName)).containsAll(ACTIVE_STAFF);
        assertThat(response.getBody().getData().getInactiveStaff()).isNotNull().hasSize(3)
            .isSortedAccordingTo(Comparator.comparing(StaffDto::getName)).containsAll(INACTIVE_STAFF);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffRepositoryTest_findByActiveOrderByNameAsc.sql")
    public void getAll_unhappyPath_notATeamLeader() throws Exception {
        final String bureauJwt = mintBureauJwt(
            BureauJwtPayload.builder()
                .userType(UserType.BUREAU)
                .login("jpowers")
                .staff(BureauJwtPayload.Staff.builder().name("Joanna Powers").active(1).build())
                .owner("400").build());

        final URI uri = URI.create("/api/v1/bureau/staff");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<StaffListDto> response = template.exchange(requestEntity, StaffListDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffRepositoryTest_findByActiveOrderByNameAsc.sql")
    public void getOne_happyPath() throws Exception {
        final String bureauJwt = mintBureauJwt(
            BureauJwtPayload.builder()
                .userType(UserType.BUREAU)
                .roles(Set.of(Role.MANAGER))
                .login("rprice")
                .staff(BureauJwtPayload.Staff.builder().name("Roxanne Price").active(1).build())
                .owner("400").build());

        final URI uri = URI.create("/api/v1/bureau/staff/jpowers");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<StaffDetailDto> response = template.exchange(requestEntity, StaffDetailDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        assertThat(response.getBody().getData().getName()).isEqualTo("Joanna Powers");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffRepositoryTest_findByActiveOrderByNameAsc.sql")
    public void getOne_errorPath_noResult() throws Exception {
        final String bureauJwt = mintBureauJwt(
            BureauJwtPayload.builder()
                .userType(UserType.BUREAU)
                .roles(Set.of(Role.MANAGER))
                .login("rprice")
                .staff(BureauJwtPayload.Staff.builder().name("Roxanne Price").active(1).build())
                .owner("400").build());

        final URI uri = URI.create("/api/v1/bureau/staff/nresult");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<StaffDetailDto> response = template.exchange(requestEntity, StaffDetailDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffRepositoryTest_findByActiveOrderByNameAsc.sql")
    public void getOne_errorPath_notATeamLeader() throws Exception {
        final String bureauJwt = mintBureauJwt(
            BureauJwtPayload.builder().userLevel("99").login("jpowers")
                .staff(BureauJwtPayload.Staff.builder().name("Joanna Powers").active(1).rank(0).build())
                .owner("400").build());

        final URI uri = URI.create("/api/v1/bureau/staff");

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<Void> requestEntity = new RequestEntity<>(httpHeaders, HttpMethod.GET, uri);
        ResponseEntity<StaffDetailDto> response = template.exchange(requestEntity, StaffDetailDto.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_happy.sql")
    @Test
    public void changeStaffAssignment_happy() throws Exception {
        final String description = "Update staff assignment happy path";

        final URI uri = URI.create("/api/v1/bureau/staff/assign");

        final String bureauJwt = mintBureauJwt(
            BureauJwtPayload.builder().userLevel("99").login("smcbob")
                .owner("400").build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            0);// initial version
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo("jmcbob");

        final StaffAssignmentRequestDto dto =
            StaffAssignmentRequestDto.builder().responseJurorNumber("644892530").assignTo("smcbob")
                .version(0)// matches DB
                .build();

        // expecting happy response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<StaffAssignmentRequestDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<StaffAssignmentResponseDto> exchange =
            template.exchange(requestEntity, StaffAssignmentResponseDto.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody().getAssignmentDate()).isToday();
        assertThat(exchange.getBody()).extracting(StaffAssignmentResponseDto::getJurorResponse,
                StaffAssignmentResponseDto::getAssignedBy, StaffAssignmentResponseDto::getAssignedTo)
            .containsExactly("644892530", "smcbob", "smcbob");

        // assert the changes to DOB were applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            1);// incremented versions
        //assert the assignment has changed!
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo("smcbob");
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_happy.sql")
    @Test
    public void changeStaffAssignment_toBacklog_happy() throws Exception {
        final String description = "Update staff assignment happy path";

        final URI uri = URI.create("/api/v1/bureau/staff/assign");

        final String bureauJwt = mintBureauJwt(
            BureauJwtPayload.builder().userLevel("99").login("smcbob")
                .owner("400").build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            0);// initial version
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo("jmcbob");

        final StaffAssignmentRequestDto dto =
            StaffAssignmentRequestDto.builder().responseJurorNumber("644892530").assignTo(null).version(0)// matches DB
                .build();

        // expecting happy response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<StaffAssignmentRequestDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<StaffAssignmentResponseDto> exchange =
            template.exchange(requestEntity, StaffAssignmentResponseDto.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody().getAssignmentDate()).isToday();
        assertThat(exchange.getBody()).extracting(StaffAssignmentResponseDto::getJurorResponse,
                StaffAssignmentResponseDto::getAssignedBy, StaffAssignmentResponseDto::getAssignedTo)
            .containsExactly("644892530", "smcbob", null);

        // assert the changes to DOB were applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            1);// incremented versions
        //assert the assignment has changed!
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo(null);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_unhappy_badStatus.sql")
    @Test
    public void changeStaffAssignment_toBacklog_unhappy_badStatus_awaitingJuror() throws Exception {
        final String description = "Update staff assignment unhappy path, awaiting-juror";

        final URI uri = URI.create("/api/v1/bureau/staff/assign");

        final String bureauJwt = mintBureauJwt(
            BureauJwtPayload.builder().userLevel("99").login("smcbob")
                .owner("400").build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            0);// initial version
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo("jmcbob");

        final StaffAssignmentRequestDto dto =
            StaffAssignmentRequestDto.builder().responseJurorNumber("644892530").assignTo(null).version(0)// matches DB
                .build();

        // expecting unhappy response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<StaffAssignmentRequestDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<StaffAssignmentResponseDto> exchange =
            template.exchange(requestEntity, StaffAssignmentResponseDto.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // assert no changes to db were applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            0);// no incremented version
        //assert the assignment has not changed!
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo("jmcbob");
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_unhappy_badStatus.sql")
    @Test
    public void changeStaffAssignment_toBacklog_unhappy_badStatus_awaitingCourt() throws Exception {
        final String description = "Update staff assignment unhappy path, awaiting-court";

        final URI uri = URI.create("/api/v1/bureau/staff/assign");

        final String bureauJwt = mintBureauJwt(
            BureauJwtPayload.builder().userLevel("99").login("smcbob")
                .owner("400").build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            0);// initial version
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo("jmcbob");

        final StaffAssignmentRequestDto dto =
            StaffAssignmentRequestDto.builder().responseJurorNumber("586856851").assignTo(null).version(0)// matches DB
                .build();

        // expecting unhappy response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<StaffAssignmentRequestDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<StaffAssignmentResponseDto> exchange =
            template.exchange(requestEntity, StaffAssignmentResponseDto.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // assert no changes to db were applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            0);// no incremented version
        //assert the assignment has not changed!
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo("jmcbob");
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_unhappy_badStatus.sql")
    @Test
    public void changeStaffAssignment_toBacklog_unhappy_badStatus_awaitingTranslation() throws Exception {
        final String description = "Update staff assignment unhappy path, awaiting-translation";

        final URI uri = URI.create("/api/v1/bureau/staff/assign");

        final String bureauJwt = mintBureauJwt(
            BureauJwtPayload.builder().userLevel("99").login("smcbob")
                .owner("400").build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            0);// initial version
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo("jmcbob");

        final StaffAssignmentRequestDto dto =
            StaffAssignmentRequestDto.builder().responseJurorNumber("586856852").assignTo(null).version(0)// matches DB
                .build();

        // expecting unhappy response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<StaffAssignmentRequestDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<StaffAssignmentResponseDto> exchange =
            template.exchange(requestEntity, StaffAssignmentResponseDto.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // assert no changes to db were applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            0);// no incremented version
        //assert the assignment has not changed!
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo("jmcbob");
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_unhappy_urgent.sql")
    @Test
    public void changeStaffAssignment_toBacklog_unhappy_urgent() throws Exception {
        final String description = "Update staff assignment unhappy path, urgent";

        final URI uri = URI.create("/api/v1/bureau/staff/assign");

        final String bureauJwt = mintBureauJwt(
            BureauJwtPayload.builder().userLevel("99").login("smcbob")
                .owner("400").build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            0);// initial version
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo("jmcbob");

        final StaffAssignmentRequestDto dto =
            StaffAssignmentRequestDto.builder().responseJurorNumber("644892530").assignTo(null).version(0)// matches DB
                .build();

        // expecting unhappy response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<StaffAssignmentRequestDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<StaffAssignmentResponseDto> exchange =
            template.exchange(requestEntity, StaffAssignmentResponseDto.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // assert no changes to db were applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            0);// no incremented version
        //assert the assignment has not changed!
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo("jmcbob");
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_unhappy_urgent.sql")
    @Test
    public void changeStaffAssignment_toBacklog_unhappy_superUrgent() throws Exception {
        final String description = "Update staff assignment unhappy path, super-urgent";

        final URI uri = URI.create("/api/v1/bureau/staff/assign");

        final String bureauJwt = mintBureauJwt(
            BureauJwtPayload.builder().userLevel("99").login("smcbob")
                .owner("400").build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            0);// initial version
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '586856851'",
            String.class)).isEqualTo("jmcbob");

        final StaffAssignmentRequestDto dto =
            StaffAssignmentRequestDto.builder().responseJurorNumber("586856851").assignTo(null).version(0)// matches DB
                .build();

        // expecting unhappy response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<StaffAssignmentRequestDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<StaffAssignmentResponseDto> exchange =
            template.exchange(requestEntity, StaffAssignmentResponseDto.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // assert no changes to db were applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            0);// no incremented version
        //assert the assignment has not changed!
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '586856851'",
            String.class)).isEqualTo("jmcbob");
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_happy.sql")
    @Test
    public void changeStaffAssignment_toBacklog_unhappy_notTeamLeader() throws Exception {
        final String description = "Update staff assignment unhappy path, not team leader";

        final URI uri = URI.create("/api/v1/bureau/staff/assign");

        final String bureauJwt = mintBureauJwt(
            BureauJwtPayload.builder().userLevel("99").login("jmcbob")
                .owner("400").build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            0);// initial version
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo("jmcbob");

        final StaffAssignmentRequestDto dto =
            StaffAssignmentRequestDto.builder().responseJurorNumber("586856852").assignTo(null).version(0)// matches DB
                .build();

        // expecting unhappy response
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<StaffAssignmentRequestDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<StaffAssignmentResponseDto> exchange =
            template.exchange(requestEntity, StaffAssignmentResponseDto.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // assert no changes to db were applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            0);// no incremented version
        //assert the assignment has not changed!
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo("jmcbob");
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_unhappy_optimisticLockingMismatch.sql")
    @Test
    public void changeStaffAssignment_unhappy_optimisticLockingMismatch() throws Exception {
        final String description = "Update staff assignment unhappy path - optimistic locking";

        final URI uri = URI.create("/api/v1/bureau/staff/assign");

        final String bureauJwt = mintBureauJwt(
            BureauJwtPayload.builder().userLevel("99").login("smcbob")
                .owner("400").build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            2);// initial version
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo("jmcbob");

        final StaffAssignmentRequestDto dto =
            StaffAssignmentRequestDto.builder().responseJurorNumber("644892530").assignTo("smcbob")
                .version(0)// does not match DB
                .build();

        // expecting an error response with an optimistic locking error.
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<StaffAssignmentRequestDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<SpringBootErrorResponse> exchange =
            template.exchange(requestEntity, SpringBootErrorResponse.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);// custom response code from controller
        assertThat(exchange.getBody().getException()).isEqualTo(BureauOptimisticLockingException.class.getName());

        // assert the change to DOB was not applied
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT VERSION FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            2);// initial version
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo("jmcbob");
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffRepositoryTest.staffRepository_happy_findActiveStaffMembers.sql")
    @Test
    public void activeStaffRoster_happy() throws Exception {
        final String description = "Active staff roster happy path";

        final URI uri = URI.create("/api/v1/bureau/staff/roster");

        final String bureauJwt = mintBureauJwt(
            BureauJwtPayload.builder().userLevel("99").login("smcbob")
                .owner("400").build());

        final int expectedStaffIncludingAutoUser = 6;

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(
            expectedStaffIncludingAutoUser);

        // expecting happy response with inactive staff filtered out
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        final ResponseEntity<StaffRosterResponseDto> exchange =
            template.exchange(new RequestEntity<Void>(httpHeaders, HttpMethod.GET, uri), StaffRosterResponseDto.class);

        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(exchange.getBody().getData()).describedAs(
                "Dto contains the active staff data alphabetically ordered by name without"
                    + JurorDigitalApplication.AUTO_USER).hasSize(4)
            .extracting(StaffRosterResponseDto.StaffDto::getName, StaffRosterResponseDto.StaffDto::getLogin)
            .containsExactly(tuple("Alison Active", "AACTIVE5"), tuple("Andy Active", "AACTIVE123"),
                tuple("Bobbie McActive", "MCBBOBBIE"), tuple("Xavier Activez", "ACTIVEX"))
            .doesNotContain(tuple("Joe Inactive", "JINACTIVE1"), tuple("Auto Processing User", "AUTO"));

        // assert db state
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(
            expectedStaffIncludingAutoUser);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauStaffControllerTest_getStaffAssignments.sql")
    @Test
    public void getStaffAssignments_happy() throws Exception {
        final String description = "Get multiple staff assignments, happy path";

        final URI uri = URI.create("/api/v1/bureau/staff/assignments-multi");

        final String bureauJwt = mintBureauJwt(
            BureauJwtPayload.builder().userLevel("99").login("smcbob")
                .owner("400").build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(6);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(6);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            0);// initial versions all zero

        List<String> jurorNumbers = new ArrayList<>();
        jurorNumbers.add("111111000");
        jurorNumbers.add("111111001");
        jurorNumbers.add("111111002");
        jurorNumbers.add("999999998"); // doesn't exist
        jurorNumbers.add("999999999"); // doesn't exist

        final AssignmentsMultiRequestDto dto = new AssignmentsMultiRequestDto(jurorNumbers);

        // expect response of type 200 for success
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<AssignmentsMultiRequestDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
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
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_happy.sql")
    @Test
    public void changeMultipleAssignments_happy() throws Exception {
        final String description = "Update multiple staff assignments happy path";

        final URI uri = URI.create("/api/v1/bureau/staff/assign-multi");

        final String bureauJwt = mintBureauJwt(
            BureauJwtPayload.builder().userLevel("99").login("smcbob")
                .owner("400").build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            0);// initial versions all zero
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo("jmcbob");
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '586856851'",
            String.class)).isNullOrEmpty();

        final MultipleStaffAssignmentDto dto = MultipleStaffAssignmentDto.builder().assignTo("smcbob").responses(
            Arrays.asList(
                MultipleStaffAssignmentDto.ResponseMetadata.builder().responseJurorNumber("644892530").version(0)
                    .build(),
                MultipleStaffAssignmentDto.ResponseMetadata.builder().responseJurorNumber("586856851").version(0)
                    .build())).build();

        // expect void response of type 204 for success
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<MultipleStaffAssignmentDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<OperationFailureListDto> exchange =
            template.exchange(requestEntity, OperationFailureListDto.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(exchange.getBody().getFailureDtos()).isNotNull().hasSize(0);

        // assert changes to db
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            2);// incremented versions
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo("smcbob");
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '586856851'",
            String.class)).isEqualTo("smcbob");
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_unhappy_badStatus.sql")
    @Test
    public void changeMultipleAssignments_unhappy_toBacklog_badStatus() throws Exception {
        final String description =
            "Update multiple staff assignments unhappy path, as at least one response is not TODO";

        final URI uri = URI.create("/api/v1/bureau/staff/assign-multi");

        final String bureauJwt = mintBureauJwt(
            BureauJwtPayload.builder().userLevel("99").login("smcbob")
                .owner("400").build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            0);// initial versions all zero
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '586856853'",
            String.class)).isEqualTo("jmcbob");
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '586856852'",
            String.class)).isEqualTo("jmcbob");

        final MultipleStaffAssignmentDto dto = MultipleStaffAssignmentDto.builder().assignTo(null).responses(
            Arrays.asList(
                MultipleStaffAssignmentDto.ResponseMetadata.builder().responseJurorNumber("586856853") // status: TO-DO
                    .version(0).build(), MultipleStaffAssignmentDto.ResponseMetadata.builder()
                    .responseJurorNumber("586856852") // status: AWAITING_TRANSLATION, will throw exception
                    .version(0).build())).build();

        // expect accepted response as one will assign and one won't
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<MultipleStaffAssignmentDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<SpringBootErrorResponse> exchange =
            template.exchange(requestEntity, SpringBootErrorResponse.class);
        assertThat(exchange).describedAs(description).isNotNull();
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(exchange.getBody()).isNotNull();

        // assert that there were some changes to db
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(1);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            1);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '586856853'",
            String.class)).isNull();
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '586856852'",
            String.class)).isEqualTo("jmcbob");
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffManagementControllerTest.changeStaffAssignment_unhappy_urgent.sql")
    @Test
    public void changeMultipleAssignments_unhappy_toBacklog_urgents() throws Exception {
        final String description = "Update multiple staff assignments unhappy path, as at least one response is Urgent";

        final URI uri = URI.create("/api/v1/bureau/staff/assign-multi");

        final String bureauJwt = mintBureauJwt(
            BureauJwtPayload.builder().userLevel("99").login("smcbob")
                .owner("400").build());

        // assert db state before merge.
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            0);// initial versions all zero
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '586856853'",
            String.class)).isEqualTo("jmcbob");
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '586856852'",
            String.class)).isEqualTo("jmcbob");

        final MultipleStaffAssignmentDto dto = MultipleStaffAssignmentDto.builder().assignTo(null).responses(
            Arrays.asList(MultipleStaffAssignmentDto.ResponseMetadata.builder()
                .responseJurorNumber("644892530") // Urgent, will throw exception
                .version(0).build(), MultipleStaffAssignmentDto.ResponseMetadata.builder()
                .responseJurorNumber("586856851") // Super-Urgent, will throw exception
                .version(0).build(), MultipleStaffAssignmentDto.ResponseMetadata.builder()
                .responseJurorNumber("586856852") // will be processed successfully
                .version(0).build(), MultipleStaffAssignmentDto.ResponseMetadata.builder()
                .responseJurorNumber("586856853") // will be processed successfully
                .version(0).build())).build();

        // expect accepted response with a StaffAssignmentException
        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<MultipleStaffAssignmentDto> requestEntity =
            new RequestEntity<>(dto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<OperationFailureListDto> exchange =
            template.exchange(requestEntity, OperationFailureListDto.class);
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
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror", Integer.class)).isEqualTo(4);
        assertThat(
            jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.juror_response", Integer.class)).isEqualTo(4);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.staff_juror_response_audit",
            Integer.class)).isEqualTo(2);
        assertThat(
            jdbcTemplate.queryForObject("SELECT SUM(VERSION) FROM juror_mod.juror_response", Integer.class)).isEqualTo(
            2);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '644892530'",
            String.class)).isEqualTo("jmcbob");
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '586856851'",
            String.class)).isEqualTo("jmcbob");
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '586856852'",
            String.class)).isNull();
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_login FROM juror_mod.juror_response WHERE JUROR_NUMBER = '586856853'",
            String.class)).isNull();
    }


    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauResponseController_reassign.sql")
    public void reassignStaffMembersResponses_happyPath() {
        final String bureauJwt = mintBureauJwt(
            BureauJwtPayload.builder()
                .userType(UserType.BUREAU)
                .roles(Set.of(Role.MANAGER))
                .login("kfry")
                .owner("400").staff(
                    BureauJwtPayload.Staff.builder().active(1).name("kfry").courts(Collections.singletonList(
                            "123"))
                        .build()).build());

        final String staffToDeactivate = "ncrawford";
        final String staffToReassignUrgentsTo = "kfry";
        final String staffToReassignPendingsTo = "lrees";
        final URI uri = URI.create("/api/v1/bureau/responses/reassign");

        ReassignResponsesDto reassignDto =
            ReassignResponsesDto.builder().staffToDeactivate(staffToDeactivate).urgentsLogin(staffToReassignUrgentsTo)
                .pendingLogin(staffToReassignPendingsTo).todoLogin(null) // assign to-do's to backlog
                .build();

        // make database assertions before making request
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM juror_mod.juror_response WHERE STAFF_login='" + staffToDeactivate + "'",
            Integer.class)).as("Officer should have assigned responses").isEqualTo(9);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<ReassignResponsesDto> requestEntity =
            new RequestEntity<>(reassignDto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);
        softly.assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.OK);

        // assertions after request is carried out
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM juror_mod.juror_response WHERE STAFF_login='" + staffToDeactivate
                    + "' AND NOT PROCESSING_STATUS='CLOSED'", Integer.class))
            .as("Officer should have no assigned responses").isEqualTo(0);
        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM juror_mod.juror_response WHERE STAFF_login='" + staffToDeactivate
                    + "' AND PROCESSING_STATUS='CLOSED'", Integer.class))
            .as("Officer should still have closed responses that are unaffected").isEqualTo(2);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM juror_mod.juror_response WHERE STAFF_login='" + staffToReassignUrgentsTo + "'",
            Integer.class)).as("Urgents should be re-assigned to specified user").isEqualTo(2);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM juror_mod.juror_response WHERE STAFF_login='" + staffToReassignPendingsTo + "'",
            Integer.class)).as("Pending responses should be re-assigned to specified user").isEqualTo(4);
        softly.assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM juror_mod.juror_response WHERE STAFF_login IS NULL",
                Integer.class)).as("Todo responses should be re-assigned to specified user (backlog)").isEqualTo(2);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauResponseController_reassign.sql")
    public void reassignStaffMembersResponses_unhappyPath_sendUrgentsToBacklog() throws Exception {
        final String bureauJwt = mintBureauJwt(
            BureauJwtPayload.builder()
                .userType(UserType.BUREAU)
                .roles(Set.of(Role.MANAGER))
                .owner("400").staff(
                    BureauJwtPayload.Staff.builder().active(1).name("kfry").courts(Collections.singletonList(
                            "123"))
                        .build()).build());

        final String staffToDeactivate = "ncrawford";
        final String staffToReassignPendingsTo = "lrees";
        final URI uri = URI.create("/api/v1/bureau/responses/reassign");

        ReassignResponsesDto reassignDto =
            ReassignResponsesDto.builder().staffToDeactivate(staffToDeactivate).urgentsLogin(null)
                .pendingLogin(staffToReassignPendingsTo).todoLogin(null) // assign to-do's to backlog
                .build();

        // make database assertions before making request
        final SoftAssertions softly = new SoftAssertions();
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM juror_mod.juror_response WHERE staff_login='" + staffToDeactivate + "'",
            Integer.class)).as("Officer should have assigned responses").isEqualTo(9);

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<ReassignResponsesDto> requestEntity =
            new RequestEntity<>(reassignDto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);
        softly.assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // assertions after request is carried out
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM juror_mod.juror_response WHERE STAFF_login='" + staffToDeactivate + "'",
            Integer.class)).as("Officer should still have assigned responses").isEqualTo(9);


        softly.assertThat(jdbcTemplate.queryForObject(
                "SELECT active FROM juror_mod.users WHERE username='" + staffToDeactivate + "'", Boolean.class))
            .as("Staff member should not be deactivated").isEqualTo(true);
        softly.assertThat(jdbcTemplate.queryForObject(
            "SELECT count(*) FROM juror_mod.staff_juror_response_audit WHERE staff_login='" + staffToDeactivate
                + "'", Integer.class)).as("There should be no audit entry for the staff member").isEqualTo(0);
        softly.assertAll();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/mod/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/BureauResponseController_reassign.sql")
    public void reassignStaffMembersResponses_unhappyPath_cantFindStaff() {
        final String bureauJwt = mintBureauJwt(
            BureauJwtPayload.builder()
                .userType(UserType.BUREAU)
                .roles(Set.of(Role.MANAGER))
                .login("kfry")
                .owner("400")
                .staff(
                    BureauJwtPayload.Staff.builder()
                        .active(1)
                        .name("kfry").courts(Collections.singletonList("123"))
                        .build()).build());

        final String staffToReassignFrom = "NON-EXISTENT-USER";
        final String staffToReassignPendingsTo = "lrees";
        final URI uri = URI.create("/api/v1/bureau/responses/reassign");

        ReassignResponsesDto reassignDto =
            ReassignResponsesDto.builder().staffToDeactivate(staffToReassignFrom).urgentsLogin(null)
                .pendingLogin(staffToReassignPendingsTo).todoLogin(null) // assign to-do's to backlog
                .build();

        httpHeaders.set(HttpHeaders.AUTHORIZATION, bureauJwt);
        RequestEntity<ReassignResponsesDto> requestEntity =
            new RequestEntity<>(reassignDto, httpHeaders, HttpMethod.POST, uri);
        ResponseEntity<Void> exchange = template.exchange(requestEntity, Void.class);
        assertThat(exchange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
