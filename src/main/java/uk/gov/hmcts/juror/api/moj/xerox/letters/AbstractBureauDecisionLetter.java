package uk.gov.hmcts.juror.api.moj.xerox.letters;

import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.xerox.LetterBase;

/**
 * Shared layout for the bureau decision letters (excusal, withdrawal, excusal denied, deferral denied).
 *
 * <p>Every letter has the same header (date, name, bureau address and contact), juror block, signatory and
 * trailing surname. They differ only by form code, signatory, and whether a court address follows the header.
 *
 * <p>{@link LetterBase}'s constructor calls {@code setup}, so the hook methods run before any subclass field
 * is initialised. They must return constants and must not read instance fields.
 */
abstract class AbstractBureauDecisionLetter extends LetterBase {

    protected AbstractBureauDecisionLetter(JurorPool jurorPool,
                                           CourtLocation courtLocation,
                                           CourtLocation bureauLocation) {
        super(LetterContext.builder()
                  .jurorPool(jurorPool)
                  .courtLocation(courtLocation)
                  .bureauLocation(bureauLocation)
                  .build());
    }

    protected AbstractBureauDecisionLetter(JurorPool jurorPool,
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

    /**
     * Signatory field printed after the juror number. Called from the super constructor; return a constant.
     */
    protected abstract LetterDataType signatory();

    /**
     * Whether the court address follows the bureau contact details. Called from the super constructor;
     * return a constant.
     */
    protected abstract boolean includesCourtAddress();

    @Override
    protected void setupWelsh() {
        setFormCode(welshFormCode());
        addData(LetterDataType.WELSH_DATE_OF_LETTER, 18);
        addData(LetterDataType.WELSH_CORRESPONDENCE_NAME, 40);
        addBureauDetails();
        if (includesCourtAddress()) {
            addWelshCourtAddress();
        }
        addJurorDetailsAndSignatory();
    }

    @Override
    protected void setupEnglish() {
        setFormCode(englishFormCode());
        addData(LetterDataType.DATE_OF_LETTER, 18);
        addData(LetterDataType.COURT_NAME, 59);
        addBureauDetails();
        if (includesCourtAddress()) {
            addEnglishCourtAddress();
        }
        addJurorDetailsAndSignatory();
    }

    private void addBureauDetails() {
        addData(LetterDataType.BUREAU_NAME, 40);
        addBureauAddress();
        addData(LetterDataType.BUREAU_PHONE, 12);
        addData(LetterDataType.BUREAU_FAX, 12);
    }

    private void addJurorDetailsAndSignatory() {
        addData(LetterDataType.JUROR_TITLE, 10);
        addData(LetterDataType.JUROR_FIRST_NAME, 20);
        addData(LetterDataType.JUROR_LAST_NAME, 20); // keeping old length and position
        addJurorAddress();
        addData(LetterDataType.JUROR_NUMBER, 9);
        addData(signatory(), 30);
        addData(LetterDataType.JUROR_LAST_NAME, 25); // revised length and position
    }
}
