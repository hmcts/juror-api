package uk.gov.hmcts.juror.api.moj.xerox;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.xerox.letters.DbdResponseLetter;
import uk.gov.hmcts.juror.api.moj.xerox.letters.DbdSummonsLetter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class RefactoredLetterTest {

    static Stream<Arguments> letterCases() {
        return Stream.of(FormCode.ENG_DBD_SUMMONS, FormCode.ENG_DBD_RESPONSE,
                FormCode.ENG_DEFERRALDENIED, FormCode.ENG_EXCUSALDENIED,
                FormCode.ENG_EXCUSAL, FormCode.ENG_WITHDRAWAL)
            .flatMap(code -> Stream.of(false, true).flatMap(welshJuror -> Stream.of(false, true)
                .flatMap(welshCourt -> Stream.of(false, true)
                    .map(sparse -> Arguments.of(code, welshJuror, welshCourt, sparse)))));
    }

    @ParameterizedTest(name = "{0}: Welsh juror={1}, Welsh court={2}, sparse addresses={3}")
    @MethodSource("letterCases")
    void preservesCompleteRecordAndLanguageSelection(FormCode englishCode, boolean welshJuror,
                                                      boolean hasWelshCourt, boolean sparse) {
        JurorPool pool = LetterTestUtils.testJurorPool(LocalDate.of(2017, 2, 6));
        pool.getJuror().setWelsh(welshJuror);
        CourtLocation court = LetterTestUtils.testCourtLocation();
        CourtLocation bureau = LetterTestUtils.testBureauLocation();
        WelshCourtLocation welshCourt = hasWelshCourt ? LetterTestUtils.testWelshCourtLocation() : null;
        court.setSignatory("COURT SIGNATORY");
        bureau.setSignatory("BUREAU SIGNATORY");
        if (sparse) {
            pool.getJuror().setAddressLine3(null);
            pool.getJuror().setAddressLine5("   ");
            court.setAddress2(null);
            bureau.setAddress2("");
            if (hasWelshCourt) {
                welshCourt.setAddress2("   ");
            }
        }
        boolean bilingual = hasWelshCourt && (englishCode.name().contains("DBD") || welshJuror);
        FormCode expectedCode = bilingual
            ? FormCode.valueOf(englishCode.name().replace("ENG_", "BI_")) : englishCode;

        LetterBase letter = LetterLayoutTestUtils.createLetter(englishCode, pool, court, bureau, welshCourt);

        assertThat(letter.getFormCode()).isEqualTo(expectedCode.getCode());
        assertThat(letter.getJurorNumber()).isEqualTo("641500541");
        assertThat(letter.getLetterString()).isEqualTo(LetterLayoutTestUtils.expectedRecord(expectedCode, sparse));
    }

    @ParameterizedTest
    @EnumSource(value = FormCode.class, names = {"ENG_DBD_SUMMONS", "ENG_DBD_RESPONSE"})
    void threeArgumentDbdConstructorFallsBackToEnglishForWelshJuror(FormCode code) {
        JurorPool pool = LetterTestUtils.testWelshJurorPool(LocalDate.of(2017, 2, 6));
        List<LetterBase> letters = List.of(
            new DbdSummonsLetter(pool, LetterTestUtils.testCourtLocation(), LetterTestUtils.testBureauLocation()),
            new DbdResponseLetter(pool, LetterTestUtils.testCourtLocation(), LetterTestUtils.testBureauLocation()));
        LetterBase letter = letters.get(code == FormCode.ENG_DBD_SUMMONS ? 0 : 1);

        assertThat(letter.getFormCode()).isEqualTo(code.getCode());
        assertThat(letter.getLetterString()).isEqualTo(LetterLayoutTestUtils.expectedRecord(code, false));
    }
}
