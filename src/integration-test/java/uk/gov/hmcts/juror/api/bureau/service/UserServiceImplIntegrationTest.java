package uk.gov.hmcts.juror.api.bureau.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.Repeat;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffDetailDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffDto;
import uk.gov.hmcts.juror.api.bureau.controller.response.StaffListDto;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponseRepository;

import java.util.Comparator;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link UserServiceImpl}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserServiceImplIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private JurorResponseRepository jurorResponseRepository;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffRepositoryTest_findByActiveOrderByNameAsc.sql")
    public void getAll() {
        final StaffListDto allStaff = userService.getAll();
        assertThat(allStaff.getData().getActiveStaff()).hasSize(6)
            .isSortedAccordingTo(Comparator.comparing(StaffDto::getName)).allMatch(StaffDto::isActive)
            .extracting("name")
            .containsOnly("Joanna Powers", "Todd Sanchez", "Grant Beck", "Roxanne Price", "Preston Brewer",
                "Amelia Copeland");
        assertThat(allStaff.getData().getInactiveStaff()).hasSize(3)
            .isSortedAccordingTo(Comparator.comparing(StaffDto::getName)).allMatch(staffDto -> !staffDto.isActive())
            .extracting("name").containsOnly("Joan Phillips", "Paul Brooks", "Shawn Rogers");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffRepositoryTest_findByActiveOrderByNameAsc.sql")
    public void getOne_happyPath() throws Exception {
        final StaffDetailDto dto = userService.getOne("jpowers");
        assertThat(dto).isNotNull();
        assertThat(dto.getData().getName()).isEqualTo("Joanna Powers");
    }

    @Test(expected = UserService.NoMatchForLoginException.class)
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffRepositoryTest_findByActiveOrderByNameAsc.sql")
    public void getOne_errorPath_noMatch() throws Exception {
        userService.getOne("nresult");
    }

    @Test
    @Repeat(10)
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffRepositoryTest_assignUrgentResponse_happy.sql")
    public void assignUrgentResponse_happy() {
        final String _JUROR_NUMBER = "123251234";//urgent juror

        // load existing juror response
        final JurorResponse jurorResponse = jurorResponseRepository.findByJurorNumber(_JUROR_NUMBER);

        //assert initial db state
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(7);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users WHERE active = true",
            Integer.class)).isEqualTo(6);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " + "JUROR_NUMBER = '" + _JUROR_NUMBER + "'",
            String.class)).isNullOrEmpty();
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_ASSIGNMENT_DATE FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " + "JUROR_NUMBER = '" + _JUROR_NUMBER
                + "'", Date.class)).isNull();
        assertThat(jdbcTemplate.queryForObject(
            "SELECT URGENT FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = " + "'" + _JUROR_NUMBER + "'",
            String.class)).isEqualTo("Y");
        assertThat(jdbcTemplate.queryForObject(
            "SELECT SUPER_URGENT FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " + "JUROR_NUMBER = '" + _JUROR_NUMBER + "'",
            String.class)).isEqualTo("N");

        userService.assignUrgentResponse(jurorResponse);

        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(7);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users WHERE active = true",
            Integer.class)).isEqualTo(6);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " + "JUROR_NUMBER = '" + _JUROR_NUMBER + "'",
            String.class)).describedAs("Eligible staff member has been assigned to response " + _JUROR_NUMBER)
            .isIn("TSANCHEZ", "JPOWERS", "RPRICE").describedAs("Inactive staff member").isNotEqualTo("GBECK")
            .describedAs("Not assigned to court").isNotEqualTo("PBREWER").describedAs("Has no courts assigned")
            .isNotEqualTo("ACOPELAND");
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_ASSIGNMENT_DATE FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " + "JUROR_NUMBER = '" + _JUROR_NUMBER
                + "'", Date.class)).describedAs("Is today").isToday();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(1);
    }

    @Test
    @Repeat(10)
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/StaffRepositoryTest_assignUrgentResponse_happy.sql")
    public void assignSuperUrgentResponse_happy() {
        final String _JUROR_NUMBER = "209092530";//super urgent juror

        // load existing juror response
        final JurorResponse jurorResponse = jurorResponseRepository.findByJurorNumber(_JUROR_NUMBER);

        //assert initial db state
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(0);
        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(7);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users WHERE active = true",
            Integer.class)).isEqualTo(6);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " + "JUROR_NUMBER = '" + _JUROR_NUMBER + "'",
            String.class)).isNullOrEmpty();
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_ASSIGNMENT_DATE FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " + "JUROR_NUMBER = '" + _JUROR_NUMBER
                + "'", Date.class)).isNull();
        assertThat(jdbcTemplate.queryForObject(
            "SELECT URGENT FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE JUROR_NUMBER = " + "'" + _JUROR_NUMBER + "'",
            String.class)).isEqualTo("N");
        assertThat(jdbcTemplate.queryForObject(
            "SELECT SUPER_URGENT FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " + "JUROR_NUMBER = '" + _JUROR_NUMBER + "'",
            String.class)).isEqualTo("Y");

        userService.assignUrgentResponse(jurorResponse);

        assertThat(
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM JUROR_DIGITAL.JUROR_RESPONSE", Integer.class)).isEqualTo(
            2);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(7);
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users WHERE active = true",
            Integer.class)).isEqualTo(6);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_LOGIN FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " + "JUROR_NUMBER = '" + _JUROR_NUMBER + "'",
            String.class)).describedAs("Eligible staff member has been assigned to response " + _JUROR_NUMBER)
            .isIn("TSANCHEZ", "JPOWERS", "RPRICE").describedAs("Inactive staff member").isNotEqualTo("GBECK")
            .describedAs("Not assigned to court").isNotEqualTo("PBREWER").describedAs("Has no courts assigned")
            .isNotEqualTo("ACOPELAND");
        assertThat(jdbcTemplate.queryForObject(
            "SELECT STAFF_ASSIGNMENT_DATE FROM JUROR_DIGITAL.JUROR_RESPONSE WHERE " + "JUROR_NUMBER = '" + _JUROR_NUMBER
                + "'", Date.class)).describedAs("Is today").isToday();
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM JUROR_DIGITAL.STAFF_JUROR_RESPONSE_AUDIT",
            Integer.class)).isEqualTo(1);
    }
}

