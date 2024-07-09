package uk.gov.hmcts.juror.api.moj.xerox.letters;

import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.xerox.LetterBase;

public class SummonsLetter extends LetterBase {
    public SummonsLetter(JurorPool jurorPool,
                         CourtLocation courtLocation,
                         CourtLocation bureauLocation) {
        super(LetterContext.builder()
            .jurorPool(jurorPool)
            .courtLocation(courtLocation)
            .bureauLocation(bureauLocation)
            .build());
    }

    public SummonsLetter(JurorPool jurorPool,
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
    protected void setup(Juror juror) {
        if (ContextType.WELSH_COURT_LOCATION.validate(letterContext)) {
            setupWelsh();
        } else {
            setupEnglish();
        }
    }


    @Override
    protected void setupWelsh() {
        setFormCode(FormCode.BI_SUMMONS);
        sharedJurorSetup();
        addData(LetterDataType.DATE_OF_LETTER, 18);
        addData(LetterDataType.DATE_OF_ATTENDANCE, 32);
        addData(LetterDataType.WELSH_DATE_OF_ATTENDANCE, 32);
        addData(LetterDataType.TIME_OF_ATTENDANCE, 8);
        addData(LetterDataType.COURT_LOCATION_CODE, 3);
        addData(LetterDataType.INSERT_INDICATORS, 20);
        addData(LetterDataType.COURT_NAME, 40);
        sharedCourtSetup();
        addData(LetterDataType.WELSH_COURT_NAME, 40);
        addData(LetterDataType.WELSH_COURT_ADDRESS1, 35);
        addData(LetterDataType.WELSH_COURT_ADDRESS2, 35);
        addData(LetterDataType.WELSH_COURT_ADDRESS3, 35);
        addData(LetterDataType.WELSH_COURT_ADDRESS4, 35);
        addData(LetterDataType.WELSH_COURT_ADDRESS5, 35);
        addData(LetterDataType.WELSH_COURT_ADDRESS6, 35);
        // repeated field
        addData(LetterDataType.JUROR_NUMBER, 9);
    }

    @Override
    protected void setupEnglish() {
        setFormCode(FormCode.ENG_SUMMONS);
        sharedJurorSetup();
        // barcodeInformation
        addData(LetterDataType.JUROR_NUMBER, 9);
        addData(LetterDataType.DATE_OF_LETTER, 18);
        addData(LetterDataType.DATE_OF_ATTENDANCE, 32);
        addData(LetterDataType.TIME_OF_ATTENDANCE, 8);
        addData(LetterDataType.COURT_LOCATION_CODE, 3);
        addData(LetterDataType.INSERT_INDICATORS, 20);
        addData(LetterDataType.COURT_NAME, 59);
        sharedCourtSetup();
    }

    private void sharedJurorSetup() {
        addData(LetterDataType.POOL_NUMBER, 9);
        addData(LetterDataType.JUROR_TITLE, 10);
        addData(LetterDataType.JUROR_FIRST_NAME, 20);
        addData(LetterDataType.JUROR_LAST_NAME, 20);
        addData(LetterDataType.JUROR_ADDRESS1, 35);
        addDataShuffle(
            new DataShuffle(LetterDataType.JUROR_ADDRESS2, 35),
            new DataShuffle(LetterDataType.JUROR_ADDRESS3, 35),
            new DataShuffle(LetterDataType.JUROR_ADDRESS4, 35),
            new DataShuffle(LetterDataType.JUROR_ADDRESS5, 35),
            new DataShuffle(LetterDataType.JUROR_ADDRESS6, 35),
            new DataShuffle(LetterDataType.JUROR_POSTCODE, 10)
        );
        addData(LetterDataType.JUROR_NUMBER, 9);
    }

    private void sharedCourtSetup() {
        addData(LetterDataType.COURT_ADDRESS1, 35);
        addData(LetterDataType.COURT_ADDRESS2, 35);
        addData(LetterDataType.COURT_ADDRESS3, 35);
        addData(LetterDataType.COURT_ADDRESS4, 35);
        addData(LetterDataType.COURT_ADDRESS5, 35);
        addData(LetterDataType.COURT_ADDRESS6, 35);
        addData(LetterDataType.COURT_POSTCODE, 10);
        addData(LetterDataType.COURT_PHONE, 12);
        addData(LetterDataType.COURT_FAX, 12);
        addData(LetterDataType.COURT_SIGNATORY, 30);
        addData(LetterDataType.BUREAU_NAME, 40);
        addData(LetterDataType.BUREAU_ADDRESS1, 35);
        addData(LetterDataType.BUREAU_ADDRESS2, 35);
        addData(LetterDataType.BUREAU_ADDRESS3, 35);
        addData(LetterDataType.BUREAU_ADDRESS4, 35);
        addData(LetterDataType.BUREAU_ADDRESS5, 35);
        addData(LetterDataType.BUREAU_ADDRESS6, 35);
        addData(LetterDataType.BUREAU_POSTCODE, 10);
        addData(LetterDataType.BUREAU_PHONE, 12);
        addData(LetterDataType.BUREAU_FAX, 12);
    }
}
