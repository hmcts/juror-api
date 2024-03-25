package uk.gov.hmcts.juror.api.bureau.domain;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

/**
 * Tests for {@link BureauJurorCjsRepository}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BureauJurorCjsRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private BureauJurorCjsRepository cjsRepository;

    private BureauJurorCjs bureauJurorCjs;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        bureauJurorCjs = new BureauJurorCjs();
        bureauJurorCjs.setJurorNumber("209092530");
        bureauJurorCjs.setEmployer("HMP");
        bureauJurorCjs.setDetails("Prison guard");
    }

    @Ignore("Composite key removed")
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/BureauJurorCJSRepository_findByCjsKey.sql")
    public void findByCjsKeyWithValidCjsCompositeKeyReturnsCorrectCjSEmploymentDetails() {
        List<BureauJurorCjs> actualBureauJurorCjs = cjsRepository.findByJurorNumber(bureauJurorCjs.getJurorNumber());
        assertThat(actualBureauJurorCjs).hasSize(1);
        assertThat(actualBureauJurorCjs).extracting("jurorNumber", "employer", "details")
            .contains(tuple(bureauJurorCjs.getJurorNumber(), bureauJurorCjs.getEmployer(),
                bureauJurorCjs.getDetails()));
    }
}