package uk.gov.hmcts.juror.api.moj.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;

import java.util.List;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PoolHistoryMigrationITest extends AbstractIntegrationTest {

    private static final String MIGRATION =
        "db/migrationv2/V2_133__remove_duplicate_deferrals_in_pool_history.sql";

    @Autowired
    private DataSource dataSource;

    @Test
    @Sql({"/db/mod/truncate.sql", "/db/PoolHistoryMigration_duplicateDeferralsIn.sql"})
    void removesOnlyEarlierDefectGeneratedHistoryAndIsIdempotent() {
        executeMigration();

        assertThat(poolHistoryIds()).containsExactly(1002L, 1004L, 1005L, 1006L, 1007L, 1008L, 1009L,
            1010L, 1011L);

        executeMigration();

        assertThat(poolHistoryIds()).containsExactly(1002L, 1004L, 1005L, 1006L, 1007L, 1008L, 1009L,
            1010L, 1011L);
    }

    private void executeMigration() {
        new ResourceDatabasePopulator(new ClassPathResource(MIGRATION)).execute(dataSource);
    }

    private List<Long> poolHistoryIds() {
        return jdbcTemplate.queryForList("SELECT id FROM juror_mod.pool_history ORDER BY id", Long.class);
    }
}
