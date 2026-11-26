package uk.gov.hmcts.juror.api.moj.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.AbstractIntegrationTest;
import uk.gov.hmcts.juror.api.moj.domain.BulkPrintData;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.repository.JurorPoolRepository;
import uk.gov.hmcts.juror.api.moj.xerox.LetterLayoutTestUtils;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
@Sql({"/db/mod/truncate.sql", "/db/letter/LetterController_initReissueDbdSummons.sql",
    "/db/letter/PrintDataService_refactoredLetters.sql"})
class PrintDataServiceITest extends AbstractIntegrationTest {

    @Autowired
    private PrintDataService printDataService;

    @Autowired
    private JurorPoolRepository jurorPoolRepository;

    @ParameterizedTest
    @EnumSource(value = FormCode.class, names = {
        "ENG_DBD_SUMMONS", "BI_DBD_SUMMONS", "ENG_DBD_RESPONSE", "BI_DBD_RESPONSE",
        "ENG_DEFERRALDENIED", "BI_DEFERRALDENIED", "ENG_EXCUSALDENIED", "BI_EXCUSALDENIED",
        "ENG_EXCUSAL", "BI_EXCUSAL", "ENG_WITHDRAWAL", "BI_WITHDRAWAL"
    })
    void queuesUnchangedLetterRecordWithCorrectFormCode(FormCode code) {
        boolean welsh = code.name().startsWith("BI_");
        String jurorNumber = welsh ? "555555562" : "555555561";
        String poolNumber = welsh ? "774241001" : "415241001";
        JurorPool jurorPool = jurorPoolRepository.findByJurorJurorNumberAndPoolPoolNumber(jurorNumber, poolNumber);

        code.getLetterPrinter().accept(printDataService, jurorPool);

        List<BulkPrintData> queued = printDataService.getLettersQueuedForJuror(jurorNumber);
        assertThat(queued).hasSize(1);
        BulkPrintData letter = queued.getFirst();
        assertThat(letter.getJurorNo()).isEqualTo(jurorNumber);
        assertThat(letter.getFormAttribute().getFormType()).isEqualTo(code.getCode());
        assertThat(letter.getCreationDate()).isEqualTo(LocalDate.now());
        assertThat(letter.isExtractedFlag()).isFalse();
        String expected = LetterLayoutTestUtils.expectedRecord(code, false)
            .replace("641500541", jurorNumber)
            .replace("415221201", poolNumber)
            .replace("457", welsh ? "774" : "415");
        assertThat(letter.getDetailRec()).isEqualTo(expected);
        assertThat(jdbcTemplate.queryForObject(
            "SELECT detail_rec FROM juror_mod.bulk_print_data WHERE juror_no = ? AND extracted_flag = false",
            String.class, jurorNumber)).isEqualTo(expected);
    }
}
