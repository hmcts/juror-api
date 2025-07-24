package uk.gov.hmcts.juror.api.moj.xerox.letters;

import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.xerox.LetterBase;

public class ExcusalLetter extends LetterBase {

    public ExcusalLetter(JurorPool jurorPool,
                         CourtLocation courtLocation,
                         CourtLocation bureauLocation) {
        super(LetterContext.builder()
            .jurorPool(jurorPool)
            .courtLocation(courtLocation)
            .bureauLocation(bureauLocation)
            .build());
    }

    public ExcusalLetter(JurorPool jurorPool,
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
        setFormCode(FormCode.BI_EXCUSAL);
        addData(LetterDataType.WELSH_DATE_OF_LETTER, 18);
        addData(LetterDataType.WELSH_CORRESPONDENCE_NAME, 40);
        addData(LetterDataType.BUREAU_NAME, 40);
        addBureauAddress();
        addData(LetterDataType.BUREAU_PHONE, 12);
        addData(LetterDataType.BUREAU_FAX, 12);
        sharedSetup();
        addData(LetterDataType.BUREAU_SIGNATORY, 30);
        addData(LetterDataType.JUROR_LAST_NAME, 25); // revised length and position
    }

    @Override
    protected void setupEnglish() {
        setFormCode(FormCode.ENG_EXCUSAL);
        addData(LetterDataType.DATE_OF_LETTER, 18);
        addData(LetterDataType.COURT_NAME, 59);
        addData(LetterDataType.BUREAU_NAME, 40);
        addBureauAddress();
        addData(LetterDataType.BUREAU_PHONE, 12);
        addData(LetterDataType.BUREAU_FAX, 12);
        sharedSetup();
        addData(LetterDataType.BUREAU_SIGNATORY, 30);
        addData(LetterDataType.JUROR_LAST_NAME, 25); // revised length and position
    }

    private void sharedSetup() {
        addData(LetterDataType.JUROR_TITLE, 10);
        addData(LetterDataType.JUROR_FIRST_NAME, 20);
        addData(LetterDataType.JUROR_LAST_NAME, 20); // keeping old length and position
        addJurorAddress();
        addData(LetterDataType.JUROR_NUMBER, 9);
    }
}
