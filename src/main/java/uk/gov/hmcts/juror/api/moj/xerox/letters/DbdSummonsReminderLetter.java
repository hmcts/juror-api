package uk.gov.hmcts.juror.api.moj.xerox.letters;

import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.xerox.LetterBase;

public class DbdSummonsReminderLetter extends LetterBase {
    public DbdSummonsReminderLetter(JurorPool jurorPool,
                                    CourtLocation courtLocation,
                                    CourtLocation bureauLocation) {
        super(LetterContext.builder()
            .jurorPool(jurorPool)
            .courtLocation(courtLocation)
            .bureauLocation(bureauLocation)
            .build());
    }

    public DbdSummonsReminderLetter(JurorPool jurorPool,
                                    CourtLocation courtLocation,
                                    CourtLocation bureauLocation,
                                    WelshCourtLocation welshCourtLocation) {
        super(LetterContext.builder()
            .jurorPool(jurorPool)
            .courtLocation(courtLocation)
            .welshCourtLocation(welshCourtLocation)
            .bureauLocation(bureauLocation)
            .build());
    }

    @Override
    protected void setupWelsh() {
        setFormCode(FormCode.BI_DBD_SUMMONS_REM);
        addData(LetterDataType.WELSH_DATE_OF_LETTER, 18);
        addData(LetterDataType.WELSH_CORRESPONDENCE_NAME, 40);
        addData(LetterDataType.COURT_NAME, 40);
        sharedSetup();
    }

    @Override
    protected void setupEnglish() {
        setFormCode(FormCode.ENG_DBD_SUMMONS_REM);
        addData(LetterDataType.DATE_OF_LETTER, 18);
        addData(LetterDataType.COURT_NAME, 59);
        sharedSetup();
    }

    private void sharedSetup() {
        addData(LetterDataType.JUROR_TITLE, 10);
        addData(LetterDataType.JUROR_FIRST_NAME, 20);
        addData(LetterDataType.JUROR_LAST_NAME, 25);
        addJurorAddressDbd();
        addData(LetterDataType.JUROR_NUMBER, 9);
    }
}
