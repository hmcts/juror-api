package uk.gov.hmcts.juror.api.moj.xerox.letters;

import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.xerox.LetterBase;

public class PostponeLetter extends LetterBase {

    public PostponeLetter(JurorPool jurorPool,
                         CourtLocation courtLocation,
                         CourtLocation bureauLocation) {
        super(LetterContext.builder()
                   .juror(jurorPool.getJuror())
                   .jurorPool(jurorPool)
                   .courtLocation(courtLocation)
                   .bureauLocation(bureauLocation)
                   .build());
        setup(jurorPool.getJuror());
    }

    public PostponeLetter(JurorPool jurorPool,
                         CourtLocation courtLocation,
                         CourtLocation bureauLocation,
                         WelshCourtLocation welshCourtLocation) {
        super(LetterContext.builder()
            .juror(jurorPool.getJuror())
            .jurorPool(jurorPool)
            .courtLocation(courtLocation)
            .welshCourtLocation(welshCourtLocation)
            .bureauLocation(bureauLocation)
            .build());
        setup(jurorPool.getJuror());
    }

    private void setup(Juror juror) {
        if (Boolean.TRUE.equals(juror.getWelsh())) {
            setupWelsh();
        } else {
            setupEnglish();
        }
    }

    private void setupWelsh() {
        setFormCode(FormCode.BI_POSTPONE);
        addData(LetterDataType.DATE_OF_LETTER, 18);
        addData(LetterDataType.WELSH_COURT_NAME, 40);
        addData(LetterDataType.COURT_NAME, 40);
        addData(LetterDataType.BUREAU_NAME, 35);
        addData(LetterDataType.BUREAU_ADDRESS1, 35);
        addData(LetterDataType.BUREAU_ADDRESS2, 35);
        addData(LetterDataType.BUREAU_ADDRESS3, 35);
        addData(LetterDataType.BUREAU_ADDRESS4, 35);
        addData(LetterDataType.BUREAU_ADDRESS5, 35);
        sharedSetup();
        addData(LetterDataType.WELSH_DEFERRAL_DATE, 32);
        addData(LetterDataType.DEFERRAL_TIME, 8);
    }

    private void setupEnglish() {
        setFormCode(FormCode.ENG_POSTPONE);
        addData(LetterDataType.DATE_OF_LETTER, 18);
        addData(LetterDataType.COURT_NAME, 59);
        addData(LetterDataType.BUREAU_NAME, 40);
        addData(LetterDataType.BUREAU_ADDRESS1, 35);
        addData(LetterDataType.BUREAU_ADDRESS2, 35);
        addData(LetterDataType.BUREAU_ADDRESS3, 35);
        addData(LetterDataType.BUREAU_ADDRESS4, 35);
        addData(LetterDataType.BUREAU_ADDRESS5, 35);
        addData(LetterDataType.BUREAU_ADDRESS6, 35);
        sharedSetup();
        addData(LetterDataType.DEFERRAL_DATE, 32);
        addData(LetterDataType.DEFERRAL_TIME, 8);
    }

    private void sharedSetup() {
        addData(LetterDataType.BUREAU_POSTCODE, 10);
        addData(LetterDataType.BUREAU_PHONE, 12);
        addData(LetterDataType.BUREAU_FAX, 12);
        addData(LetterDataType.JUROR_TITLE, 10);
        addData(LetterDataType.JUROR_FIRST_NAME, 20);
        addData(LetterDataType.JUROR_LAST_NAME, 20);
        addData(LetterDataType.JUROR_ADDRESS1, 35);
        addData(LetterDataType.JUROR_ADDRESS2, 35);
        addData(LetterDataType.JUROR_ADDRESS3, 35);
        addData(LetterDataType.JUROR_ADDRESS4, 35);
        addData(LetterDataType.JUROR_ADDRESS5, 35);
        addData(LetterDataType.JUROR_ADDRESS6, 35);
        addData(LetterDataType.JUROR_POSTCODE, 10);
        addData(LetterDataType.JUROR_NUMBER, 9);
        addData(LetterDataType.BUREAU_SIGNATORY, 30);
    }
}
