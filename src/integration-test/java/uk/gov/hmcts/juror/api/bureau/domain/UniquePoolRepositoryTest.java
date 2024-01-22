package uk.gov.hmcts.juror.api.bureau.domain;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link UniquePoolRepository}.
 *
 * @since JDB-2042
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UniquePoolRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UniquePoolRepository uniquePoolRepository;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/UniquePoolRepositoryTest.sql")
    @Test
    public void findById() {
        assertThat(uniquePoolRepository.findById("101")).isNotNull();
    }

    @Sql("/db/truncate.sql")
    @Sql("/db/UniquePoolRepositoryTest.sql")
    @Test
    public void findAll() {
        assertThat(uniquePoolRepository.findAll()).isNotNull().hasSize(2).extracting("poolNumber")
            .containsExactlyInAnyOrder("101", "102");
    }
}
