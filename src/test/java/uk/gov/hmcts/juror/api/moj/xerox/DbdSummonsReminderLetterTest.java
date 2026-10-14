package uk.gov.hmcts.juror.api.moj.xerox;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.xerox.letters.DbdSummonsReminderLetter;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

class DbdSummonsReminderLetterTest extends AbstractLetterTest {

    @Override
    protected void setupEnglishExpectedResult() {
        addEnglishLetterDate();
        addEnglishField("THE CROWN COURT AT SWANSEA", 59);
        addEnglishField("MR", 10);
        addEnglishField("FNAMEEIGHTTHREEONE", 20);
        addEnglishField("LNAMEEIGHTTHREEONEFIVE", 25);
        addEnglishField("831 STREET NAME", 35);
        addEnglishField("ANYTOWN", 35);
        addEnglishField("JUROR_ADDRESS_3", 35);
        addEnglishField("JUROR_ADDRESS_4", 35);
        addEnglishField("JUROR_ADDRESS_5", 35);
        addEnglishField("SY2 6LU", 10);
        addEnglishField("641500541", 9);
        addEnglishField("MONDAY 6 FEBRUARY, 2017", 32);
    }

    @Override
    protected void setupWelshExpectedResult() {
        addWelshLetterDate();
        addWelshField("YN ABERTAWE", 40);
        addWelshField("THE CROWN COURT AT SWANSEA", 40);
        addWelshField("MR", 10);
        addWelshField("FNAMEEIGHTTHREEONE", 20);
        addWelshField("LNAMEEIGHTTHREEONEFIVE", 25);
        addWelshField("831 STREET NAME", 35);
        addWelshField("ANYTOWN", 35);
        addWelshField("JUROR_ADDRESS_3", 35);
        addWelshField("JUROR_ADDRESS_4", 35);
        addWelshField("JUROR_ADDRESS_5", 35);
        addWelshField("SY2 6LU", 10);
        addWelshField("641500541", 9);
        addWelshField("MONDAY 6 FEBRUARY, 2017", 32);
        addWelshField("DYDD LLUN 6 CHWEFROR, 2017", 32);
    }

    @Test
    void confirmEnglishLetterProducesCorrectOutput() {
        setupEnglishExpectedResult();

        DbdSummonsReminderLetter letter = new DbdSummonsReminderLetter(
            LetterTestUtils.testJurorPool(LocalDate.of(2017, Month.FEBRUARY, 6)),
            LetterTestUtils.testCourtLocation(),
            LetterTestUtils.testBureauLocation());

        assertThat(letter.getLetterString()).isEqualTo(getExpectedEnglishResult());
        assertThat(letter.getFormCode()).isEqualTo(FormCode.ENG_DBD_SUMMONS_REM.getCode());
        assertThat(letter.getJurorNumber()).isEqualTo(LetterTestUtils.testJuror().getJurorNumber());
    }

    @Test
    void confirmWelshLetterProducesCorrectOutput() {
        setupWelshExpectedResult();

        DbdSummonsReminderLetter letter = new DbdSummonsReminderLetter(
            LetterTestUtils.testWelshJurorPool(LocalDate.of(2017, Month.FEBRUARY, 6)),
            LetterTestUtils.testCourtLocation(),
            LetterTestUtils.testBureauLocation(),
            LetterTestUtils.testWelshCourtLocation());

        assertThat(letter.getLetterString()).isEqualTo(getExpectedWelshResult());
        assertThat(letter.getFormCode()).isEqualTo(FormCode.BI_DBD_SUMMONS_REM.getCode());
        assertThat(letter.getJurorNumber()).isEqualTo(LetterTestUtils.testWelshJuror().getJurorNumber());
    }

    @Test
    void confirmWelshWithoutWelshCourtProducesEnglishOutput() {
        setupEnglishExpectedResult();

        DbdSummonsReminderLetter letter = new DbdSummonsReminderLetter(
            LetterTestUtils.testWelshJurorPool(LocalDate.of(2017, Month.FEBRUARY, 6)),
            LetterTestUtils.testCourtLocation(),
            LetterTestUtils.testBureauLocation());

        assertThat(letter.getLetterString()).isEqualTo(getExpectedEnglishResult());
    }
}
