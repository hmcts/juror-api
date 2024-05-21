package uk.gov.hmcts.juror.api.bureau.domain;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.moj.domain.QUser;
import uk.gov.hmcts.juror.api.moj.domain.User;
import uk.gov.hmcts.juror.api.moj.repository.UserRepository;

import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static uk.gov.hmcts.juror.api.bureau.domain.UserQueries.active;

/**
 * Tests {@link UserRepository} repository.
 */

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserRepositoryTest extends AbstractIntegrationTest {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/UserRepositoryTest.userRepository_happy_findStaffMember.sql")
    @Test
    @Transactional
    @SuppressWarnings("PMD.JUnitAssertionsShouldIncludeMessage")//False positive
    public void userRepository_happy_findStaffMember() {
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(4);

        assertThat(userRepository.findAll())
            .isNotNull()
            .describedAs("Names in order have correct rank, active status")
            .extracting("name", "level", "active")
            .containsExactlyInAnyOrder(
                tuple("Joe McBob", 0, true),
                tuple("Sarah McBob", 1, true),
                tuple("Joe Bobson", 1, false),
                tuple("AUTO", 0, true)
            );
        assertThat(userRepository.findAll()).isNotNull()
            .describedAs("Teams associated and loaded eagerly")
            .extracting("team.id", "team.teamName").containsExactlyInAnyOrder(
                tuple(1L, "London & Wales"),
                tuple(2L, "South East, North East & North West"),
                tuple(3L, "Midlands & South West"),
                tuple(null, null));
    }


    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/UserRepositoryTest.userRepository_happy_findActiveStaffMembers.sql")
    @Test
    @Transactional
    public void userRepository_happy_findActiveStaffMembers() {
        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(6);

        final List<User> staffList = Lists.newArrayList(userRepository.findAll(active()));
        assertThat(staffList)
            .describedAs("Has filtered out the inactive staff and " + JurorDigitalApplication.AUTO_USER)
            .hasSize(4)
            .extracting("name")
            .containsExactlyInAnyOrder("Alison Active", "Andy Active", "Bobbie McActive", "Xavier Activez")
        ;

        assertThat(jdbcTemplate.queryForObject("SELECT count(*) FROM juror_mod.users", Integer.class)).isEqualTo(6);
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    @Sql("/db/UserRepositoryTest_findByActiveOrderByNameAsc.sql")
    @Test
    public void findByActiveOrderByNameAsc_active() {
        final List<User> active = Lists.newArrayList(userRepository.findAll(active(), QUser.user.name.asc()));
        assertThat(active).hasSize(6).isSortedAccordingTo(Comparator.comparing(User::getName))
            .allMatch(staff -> staff.isActive())
            .extracting("name").containsOnly(
                "Joanna Powers",
                "Todd Sanchez",
                "Grant Beck",
                "Roxanne Price",
                "Preston Brewer",
                "Amelia Copeland");
    }
}