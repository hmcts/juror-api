package uk.gov.hmcts.juror.api.bureau.domain;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TeamRepository}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TeamRepositoryTest extends AbstractIntegrationTest {

    private static final List<String> STANDING_DATA_TEAM_NAMES = Arrays.asList(
        "London & Wales",
        "South East, North East & North West",
        "Midlands & South West");

    @Autowired
    private TeamRepository teamRepository;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void cleanUp() {
        teamRepository.deleteAll();
    }

    /**
     * Tests that findAll returns the three teams inserted as standing data
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    public void standingDataRetrieved() {
        assertThat(teamRepository.findAll())
            .hasSize(3)
            .extracting("teamName")
            .containsExactlyElementsOf(STANDING_DATA_TEAM_NAMES);
    }

    /**
     * Tests that findByName returns the correct values
     */
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    public void testFindByName() {
        for (String name : STANDING_DATA_TEAM_NAMES) {
            assertThat(teamRepository.findByTeamName(name)).isNotNull().matches(t -> name.equals(t.getTeamName()));
        }
    }

    /**
     * Tests that saving a team with a non-unique name results in an error
     */
    @Test(expected = DataIntegrityViolationException.class)
    @Sql("/db/truncate.sql")
    @Sql("/db/standing_data.sql")
    public void testSaveDuplicateName_causesError() {
        final Team testTeam = new Team();
        testTeam.setId((long) Integer.MAX_VALUE);

        // Use a name already inserted as standing data
        testTeam.setTeamName(STANDING_DATA_TEAM_NAMES.get(0));

        teamRepository.save(testTeam);
    }
}
