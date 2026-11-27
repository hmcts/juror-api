package uk.gov.hmcts.juror.api.moj.xerox.letters;

import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;

public class DbdSummonsLetter extends AbstractDbdLetter {
    public DbdSummonsLetter(JurorPool jurorPool,
                            CourtLocation courtLocation,
                            CourtLocation bureauLocation) {
        super(jurorPool, courtLocation, bureauLocation);
    }

    public DbdSummonsLetter(JurorPool jurorPool,
                            CourtLocation courtLocation,
                            CourtLocation bureauLocation,
                            WelshCourtLocation welshCourtLocation) {
        super(jurorPool, courtLocation, bureauLocation, welshCourtLocation);
    }

    @Override
    protected FormCode welshFormCode() {
        return FormCode.BI_DBD_SUMMONS;
    }

    @Override
    protected FormCode englishFormCode() {
        return FormCode.ENG_DBD_SUMMONS;
    }
}
