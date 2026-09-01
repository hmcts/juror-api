package uk.gov.hmcts.juror.api.moj.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BulkPrintDataRepositoryITest extends AbstractIntegrationTest {

    @Autowired
    private BulkPrintDataRepository bulkPrintDataRepository;

    @Test
    @Sql({
        "/db/mod/truncate.sql",
        "/db/BulkPrintDataRepository_deleteDbdEmails.sql"
    })
    @SuppressWarnings("PMD.UnitTestShouldIncludeAssert") // false positive
    void deleteDbdEmailsAppliesFormCodeStatusRules() {
        bulkPrintDataRepository.deleteDbdEmails();

        List.of(
            "111111111",
            "111111112",
            "111111113",
            "111111114",
            "111111115",
            "111111116",
            "111111117",
            "111111118",
            "111111119",
            "111111120",
            "111111121",
            "111111122",
            "111111123"
        ).forEach(this::assertBulkPrintDataRetained);

        List.of(
            "222222222",
            "222222223",
            "222222224",
            "222222225",
            "222222226",
            "222222227",
            "222222228",
            "222222229",
            "222222230",
            "222222231",
            "222222232"
        ).forEach(this::assertBulkPrintDataDeleted);
    }

    @Test
    @Sql({
        "/db/mod/truncate.sql",
        "/db/BulkPrintDataRepository_deletePrintfilesDbdSummonsReminders.sql"
    })
    @SuppressWarnings("PMD.UnitTestShouldIncludeAssert") // false positive
    void deletePrintfilesAppliesDbdSummonsReminderStatusRules() {
        bulkPrintDataRepository.deletePrintfiles();

        List.of(
            "333333333",
            "333333334"
        ).forEach(this::assertBulkPrintDataRetained);

        List.of(
            "444444444",
            "444444445"
        ).forEach(this::assertBulkPrintDataDeleted);
    }

    private void assertBulkPrintDataRetained(String jurorNumber) {
        assertThat(bulkPrintDataRepository.countByJurorNo(jurorNumber))
            .as("Bulk print data retained for juror %s", jurorNumber)
            .isEqualTo(1);
    }

    private void assertBulkPrintDataDeleted(String jurorNumber) {
        assertThat(bulkPrintDataRepository.countByJurorNo(jurorNumber))
            .as("Bulk print data deleted for juror %s", jurorNumber)
            .isZero();
    }
}
