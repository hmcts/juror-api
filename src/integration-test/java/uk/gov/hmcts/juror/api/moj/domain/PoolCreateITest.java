package uk.gov.hmcts.juror.api.moj.domain;

import lombok.extern.slf4j.Slf4j;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.moj.service.VotersService;
import uk.gov.hmcts.juror.api.moj.service.VotersServiceImpl;
import uk.gov.hmcts.juror.api.testsupport.ContainerTest;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class PoolCreateITest extends ContainerTest {

    @Autowired
    VotersService votersService;

    /*
     * This test will invoke the Get_Voters function in the database to randomly
     * select a number of voters.
     */
    @Test
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_loadVoters.sql"})
    public void test_poolCreate_getVoters_success() {
        // load voters from the database
        List<Voters> resultSet = votersService.getVoters(List.of("CH1", "CH2", "CH3"),
            5,
            LocalDate.now(),
            "415",
            false);
        assertThat(resultSet).isNotEmpty();
        assertThat(resultSet.size()).isEqualTo(7); // should be 5 * 1.4 = 7
    }

    @Test
    @Ignore("This test is manually run to show the randomness of the voters selection function")
    @Sql({"/db/mod/truncate.sql", "/db/CreatePoolController_loadVoters.sql"})
    public void test_poolCreate_getVoters_distribution() {
        //update the loop count to select voters a number of times
        int loopCount = 100;
        Map<String, Integer> jurorIdMap = new ConcurrentHashMap<>();

        for (int i = 0; i < loopCount; i++) {
            // load voters from the database
            List<Voters> resultSet = votersService.getVoters(List.of("CH1", "CH2", "CH3"),
                5, LocalDate.now(),
                "415",
                false);
            assertThat(resultSet).isNotEmpty();

            resultSet.forEach(row -> {
                String jurorNumber = row.getJurorNumber();
                jurorIdMap.computeIfPresent(jurorNumber, (key, val) -> val + 1);
                jurorIdMap.putIfAbsent(jurorNumber, 1);
            });
        }
        jurorIdMap.forEach((key, val) -> log.info("There were {} occurrences of juror number {}", val, key));
    }
}
