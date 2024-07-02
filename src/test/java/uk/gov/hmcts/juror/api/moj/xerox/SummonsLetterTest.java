package uk.gov.hmcts.juror.api.moj.xerox;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.xerox.letters.SummonsLetter;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
class SummonsLetterTest extends AbstractLetterTest {
    @Override
    protected void setupEnglishExpectedResult() {
        addEnglishField("415221201", 9);
        addEnglishField("MR", 10);
        addEnglishField("FNAMEEIGHTTHREEONE", 20);
        addEnglishField("LNAMEEIGHTTHREEONE", 20);
        addEnglishField("831 STREET NAME", 35);
        addEnglishField("ANYTOWN", 35);
        addEnglishField("JUROR_ADDRESS_3", 35);
        addEnglishField("JUROR_ADDRESS_4", 35);
        addEnglishField("JUROR_ADDRESS_5", 35);
        addEnglishField("", 35);
        addEnglishField("SY2 6LU", 10);
        addEnglishField("641500541", 9);
        addEnglishField("641500541", 9);
        addEnglishLetterDate();
        addEnglishField("MONDAY 6 FEBRUARY, 2017", 32);
        addEnglishField("10:00AM", 8);
        addEnglishField("457", 3);
        addEnglishField("TWO WEEKS", 20);
        addEnglishField("SWANSEA CROWN COURT", 59);
        addEnglishField("THE LAW COURTS", 35);
        addEnglishField("ST HELENS ROAD", 35);
        addEnglishField("SWANSEA", 35);
        addEnglishField("SHREWSBURY", 35);
        addEnglishField("COURT_ADDRESS_5", 35);
        addEnglishField("COURT_ADDRESS_6", 35);
        addEnglishField("SY2 6LU", 10);
        addEnglishField("01792637000", 12);
        addEnglishField("", 12);
        addEnglishField("JURY MANAGER", 30);
        addEnglishField("JURY CENTRAL SUMMONING BUREAU", 40);
        addEnglishField("THE COURT SERVICE", 35);
        addEnglishField("FREEPOST LON 19669", 35);
        addEnglishField("POCOCK STREET", 35);
        addEnglishField("LONDON", 35);
        addEnglishField("BUREAU_ADDRESS_5", 35);
        addEnglishField("BUREAU_ADDRESS_6", 35);
        addEnglishField("SE1 0YG", 10);
        addEnglishField("0845 3555567", 12);
        addEnglishField("", 12);
    }

    @Override
    protected void setupWelshExpectedResult() {
        addWelshField("415221201", 9);
        addWelshField("MR", 10);
        addWelshField("FNAMEEIGHTTHREEONE", 20);
        addWelshField("LNAMEEIGHTTHREEONE", 20);
        addWelshField("831 STREET NAME", 35);
        addWelshField("ANYTOWN", 35);
        addWelshField("JUROR_ADDRESS_3", 35);
        addWelshField("JUROR_ADDRESS_4", 35);
        addWelshField("JUROR_ADDRESS_5", 35);
        addWelshField("", 35);
        addWelshField("SY2 6LU", 10);
        addWelshField("641500541", 9);
        addWelshLetterDate();
        addWelshField("MONDAY 6 FEBRUARY, 2017", 32);
        addWelshField("DYDD LLUN 6 CHWEFROR, 2017", 32);
        addWelshField("10:00AM", 8);
        addWelshField("457", 3);
        addWelshField("TWO WEEKS", 20);
        addWelshField("SWANSEA CROWN COURT", 40);
        addWelshField("THE LAW COURTS", 35);
        addWelshField("ST HELENS ROAD", 35);
        addWelshField("SWANSEA", 35);
        addWelshField("SHREWSBURY", 35);
        addWelshField("COURT_ADDRESS_5", 35);
        addWelshField("COURT_ADDRESS_6", 35);
        addWelshField("SY2 6LU", 10);
        addWelshField("01792637000", 12);
        addWelshField("", 12);
        addWelshField("JURY MANAGER", 30);
        addWelshField("JURY CENTRAL SUMMONING BUREAU", 40);
        addWelshField("THE COURT SERVICE", 35);
        addWelshField("FREEPOST LON 19669", 35);
        addWelshField("POCOCK STREET", 35);
        addWelshField("LONDON", 35);
        addWelshField("BUREAU_ADDRESS_5", 35);
        addWelshField("BUREAU_ADDRESS_6", 35);
        addWelshField("SE1 0YG", 10);
        addWelshField("0845 3555567", 12);
        addWelshField("", 12);
        addWelshField("ABERTAWE", 40);
        addWelshField("Y LLYSOEDD BARN", 35);
        addWelshField("LON SAN HELEN", 35);
        addWelshField("ABERTAWE", 35);
        addWelshField("WELSH_COURT_ADDRESS_4", 35);
        addWelshField("WELSH_COURT_ADDRESS_5", 35);
        addWelshField("WELSH_COURT_ADDRESS_6", 35);
        // repeated field
        addWelshField("641500541", 9);
    }


    @Test
    void confirmEnglishLetterProducesCorrectOutput() {

        final LocalDate date = LocalDate.of(2017, Month.FEBRUARY, 6);
        setupEnglishExpectedResult();
        SummonsLetter summonsLetter = new SummonsLetter(LetterTestUtils.testJurorPool(date),
            LetterTestUtils.testCourtLocation(),
            LetterTestUtils.testBureauLocation());


        assertThat(summonsLetter.getLetterString()).isEqualTo(getExpectedEnglishResult());
        assertThat(summonsLetter.getFormCode()).isEqualTo(FormCode.ENG_SUMMONS.getCode());
        assertThat(summonsLetter.getJurorNumber()).isEqualTo(LetterTestUtils.testJuror().getJurorNumber());

        // Juror address 6 is always empty
        assertThat(summonsLetter.getData().get(9).getFormattedString()).isEqualTo(LetterTestUtils.emptyField(35));
        // Fax number is always empty
        assertThat(summonsLetter.getData().get(27).getFormattedString()).isEqualTo(LetterTestUtils.emptyField(12));
        assertThat(summonsLetter.getData().get(38).getFormattedString()).isEqualTo(LetterTestUtils.emptyField(12));

    }

    @Test
    void confirmWelshLetterProducesCorrectOutput() {

        final LocalDate date = LocalDate.of(2017, Month.FEBRUARY, 6);
        setupWelshExpectedResult();
        SummonsLetter summonsLetter = new SummonsLetter(LetterTestUtils.testWelshJurorPool(date),
            LetterTestUtils.testCourtLocation(),
            LetterTestUtils.testBureauLocation(),
            LetterTestUtils.testWelshCourtLocation());

        assertThat(summonsLetter.getLetterString()).isEqualTo(getExpectedWelshResult());
        assertThat(summonsLetter.getFormCode()).isEqualTo(FormCode.BI_SUMMONS.getCode());
        assertThat(summonsLetter.getJurorNumber()).isEqualTo(LetterTestUtils.testWelshJuror().getJurorNumber());

        // Juror address 6 is always empty
        assertThat(summonsLetter.getData().get(9).getFormattedString()).isEqualTo(LetterTestUtils.emptyField(35));
        // Fax number is always empty
        assertThat(summonsLetter.getData().get(27).getFormattedString()).isEqualTo(LetterTestUtils.emptyField(12));
        assertThat(summonsLetter.getData().get(38).getFormattedString()).isEqualTo(LetterTestUtils.emptyField(12));

    }

    @Test
    void confirmWelshWithoutWelshCourtProducesEnglishOutput() {
        final LocalDate date = LocalDate.of(2017, Month.FEBRUARY, 6);
        setupEnglishExpectedResult();
        SummonsLetter summonsLetter = new SummonsLetter(LetterTestUtils.testWelshJurorPool(date),
            LetterTestUtils.testCourtLocation(),
            LetterTestUtils.testBureauLocation());
        assertThat(summonsLetter.getLetterString()).isEqualTo(getExpectedEnglishResult());
    }
}
