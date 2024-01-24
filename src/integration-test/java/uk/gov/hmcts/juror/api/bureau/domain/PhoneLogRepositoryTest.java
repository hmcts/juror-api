package uk.gov.hmcts.juror.api.bureau.domain;

import org.assertj.core.groups.Tuple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PhoneLogRepository}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PhoneLogRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private PhoneLogRepository phoneLogRepository;
    private PhoneLog phoneLog;
    private Date startCall;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        startCall = Date.from(LocalDateTime.of(2017, Month.MARCH, 25, 10, 30, 00)
            .atZone(ZoneId.systemDefault())
            .toInstant());
        phoneLog = new PhoneLog();
        phoneLog.setJurorNumber("209092530");
        phoneLog.setStartCall(startCall);
        phoneLog.setUsername("APerson");
        phoneLog.setPhoneCode("FA");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/BureauLogRepository_findByLogKey.sql")
    public void findByLogKey_WithValidCompositeKey_ReturnsBureauJurorLog() {
        List<PhoneLog> actualJurorLog = phoneLogRepository.findByJurorNumber(phoneLog.getJurorNumber());
        assertThat(actualJurorLog).hasSize(1);
        assertThat(actualJurorLog.get(0)).as("Expect the juror number to match")
            .extracting("jurorNumber").isEqualTo(phoneLog.getJurorNumber());
        assertThat(actualJurorLog.get(0).getStartCall()).as("Expect start call to match")
            .isEqualToIgnoringMillis(startCall);
        assertThat(actualJurorLog).as("Expect the username and phone code to match")
            .extracting("username", "phoneCode").contains(Tuple.tuple("APerson", "FA"));
    }
}
