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
 * Tests for {@link BureauJurorCJSRepository}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BureauJurorCjsRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private BureauJurorCJSRepository cjsRepository;

    private BureauJurorCJS bureauJurorCJS;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        bureauJurorCJS = new BureauJurorCJS();
        bureauJurorCJS.setJurorNumber("209092530");
        bureauJurorCJS.setEmployer("HMP");
        bureauJurorCJS.setDetails("Prison guard");
    }

    @Ignore("Composite key removed")
    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/BureauJurorCJSRepository_findByCjsKey.sql")
    public void findByCjsKey_WithValidCjsCompositeKey_ReturnsCorrectCjSEmploymentDetails() {
        List<BureauJurorCJS> actualBureauJurorCJS = cjsRepository.findByJurorNumber(bureauJurorCJS.getJurorNumber());
        assertThat(actualBureauJurorCJS).hasSize(1);
        assertThat(actualBureauJurorCJS).extracting("jurorNumber", "employer", "details")
            .contains(tuple(bureauJurorCJS.getJurorNumber(), bureauJurorCJS.getEmployer(),
                bureauJurorCJS.getDetails()));
    }
}