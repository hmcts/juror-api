package uk.gov.hmcts.juror.api.moj.xerox;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.xerox.letters.DbdSummonsReminderLetter;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

class DbdSummonsReminderLetterTest {

    @Test
    void confirmEnglishLetterUsesDbdSummonsReminderFormCode() {
        DbdSummonsReminderLetter letter = new DbdSummonsReminderLetter(
            LetterTestUtils.testJurorPool(LocalDate.of(2017, Month.FEBRUARY, 6)),
            LetterTestUtils.testCourtLocation(),
            LetterTestUtils.testBureauLocation());

        assertThat(letter.getFormCode()).isEqualTo(FormCode.ENG_DBD_SUMMONS_REM.getCode());
    }

    @Test
    void confirmWelshLetterUsesDbdSummonsReminderFormCode() {
        DbdSummonsReminderLetter letter = new DbdSummonsReminderLetter(
            LetterTestUtils.testWelshJurorPool(LocalDate.of(2017, Month.FEBRUARY, 6)),
            LetterTestUtils.testCourtLocation(),
            LetterTestUtils.testBureauLocation(),
            LetterTestUtils.testWelshCourtLocation());

        assertThat(letter.getFormCode()).isEqualTo(FormCode.BI_DBD_SUMMONS_REM.getCode());
    }
}
