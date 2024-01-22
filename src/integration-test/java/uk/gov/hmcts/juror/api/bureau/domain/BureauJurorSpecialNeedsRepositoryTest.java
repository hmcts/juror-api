package uk.gov.hmcts.juror.api.bureau.domain;

import org.junit.Before;
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
 * Tests for {@link BureauJurorSpecialNeedsRepository}.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BureauJurorSpecialNeedsRepositoryTest extends AbstractIntegrationTest {
    @Autowired
    private BureauJurorSpecialNeedsRepository specialNeedsRepository;

    private BureauJurorSpecialNeed specialNeed;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        TSpecial tSpecial = new TSpecial("D", "DIET");
        specialNeed = new BureauJurorSpecialNeed();
        specialNeed.setJurorNumber("209092530");
        specialNeed.setSpecialNeed(tSpecial);
        specialNeed.setDetail("Some Details");
    }

    @Test
    @Sql("/db/truncate.sql")
    @Sql("/db/BureauJurorSpecialNeedsRepository_findByJurorNumber.sql")
    public void findBYJurorNumber_WithValidJurorNumber_ReturnsJurorSpecialNeeds() {
        List<BureauJurorSpecialNeed> actualSpecialNeed = specialNeedsRepository.findByJurorNumber("209092530");
        assertThat(actualSpecialNeed).hasSize(1);
        assertThat(actualSpecialNeed).extracting("jurorNumber", "specialNeed.code", "specialNeed.description", "detail")
            .contains(tuple(specialNeed.getJurorNumber(),
                specialNeed.getSpecialNeed().getCode(),
                specialNeed.getSpecialNeed().getDescription(),
                specialNeed.getDetail()));
    }
}
