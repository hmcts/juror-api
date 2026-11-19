package uk.gov.hmcts.juror.api.moj.xerox.letters;

import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.xerox.LetterBase;

/**
 * Shared layout for the Digital by Default summons and response letters, which differ only by form code.
 *
 * <p>{@link LetterBase}'s constructor calls {@link #setup}, so it runs before any subclass field is
 * initialised. {@link #welshFormCode()} and {@link #englishFormCode()} must therefore return constants and
 * must not read instance fields.
 */
abstract class AbstractDbdLetter extends LetterBase {

    protected AbstractDbdLetter(JurorPool jurorPool,
                                CourtLocation courtLocation,
                                CourtLocation bureauLocation) {
        super(LetterContext.builder()
                  .jurorPool(jurorPool)
                  .courtLocation(courtLocation)
                  .bureauLocation(bureauLocation)
                  .build());
    }

    protected AbstractDbdLetter(JurorPool jurorPool,
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

    /**
     * Form code for the bilingual (Welsh) letter. Called from the super constructor; return a constant.
     */
    protected abstract FormCode welshFormCode();

    /**
     * Form code for the English letter. Called from the super constructor; return a constant.
     */
    protected abstract FormCode englishFormCode();

    @Override
    protected void setup(Juror juror) {
        if (ContextType.WELSH_COURT_LOCATION.validate(letterContext)) {
            setupWelsh();
        } else {
            setupEnglish();
        }
    }

    @Override
    protected void setupWelsh() {
        setFormCode(welshFormCode());
        sharedJurorSetupDbd();
        addData(LetterDataType.DATE_OF_LETTER, 18);
        addData(LetterDataType.DATE_OF_ATTENDANCE, 32);
        addData(LetterDataType.WELSH_DATE_OF_ATTENDANCE, 32);
        addData(LetterDataType.TIME_OF_ATTENDANCE, 8);
        addData(LetterDataType.COURT_LOCATION_CODE, 3);
        addData(LetterDataType.COURT_NAME, 40);
        sharedCourtSetup();
        addData(LetterDataType.WELSH_COURT_NAME, 40);
        addDataShuffle(
            new DataShuffle(LetterDataType.WELSH_COURT_ADDRESS1, 35),
            new DataShuffle(LetterDataType.WELSH_COURT_ADDRESS2, 35),
            new DataShuffle(LetterDataType.WELSH_COURT_ADDRESS3, 35),
            new DataShuffle(LetterDataType.WELSH_COURT_ADDRESS4, 35),
            new DataShuffle(LetterDataType.WELSH_COURT_ADDRESS5, 35),
            new DataShuffle(LetterDataType.COURT_POSTCODE, 35)
        );
    }

    @Override
    protected void setupEnglish() {
        setFormCode(englishFormCode());
        sharedJurorSetupDbd();
        addData(LetterDataType.DATE_OF_LETTER, 18);
        addData(LetterDataType.DATE_OF_ATTENDANCE, 32);
        addData(LetterDataType.TIME_OF_ATTENDANCE, 8);
        addData(LetterDataType.COURT_LOCATION_CODE, 3);
        addData(LetterDataType.COURT_NAME, 59);
        sharedCourtSetup();
    }

    private void sharedJurorSetupDbd() {
        addData(LetterDataType.POOL_NUMBER, 9);
        addData(LetterDataType.JUROR_TITLE, 10);
        addData(LetterDataType.JUROR_FIRST_NAME, 20);
        addData(LetterDataType.JUROR_LAST_NAME, 25);
        addJurorAddressDbd();
        addData(LetterDataType.JUROR_NUMBER, 9);
    }

    private void sharedCourtSetup() {
        addEnglishCourtAddress();
        addData(LetterDataType.COURT_PHONE, 12);
    }
}
