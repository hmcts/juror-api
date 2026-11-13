package uk.gov.hmcts.juror.api.moj.xerox.letters;

import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.juror.domain.WelshCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.FormCode;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;

public class DeferralDeniedLetter extends AbstractBureauDecisionLetter {
    public DeferralDeniedLetter(JurorPool jurorPool,
                                CourtLocation courtLocation,
                                CourtLocation bureauLocation) {
        super(jurorPool, courtLocation, bureauLocation);
    }

    public DeferralDeniedLetter(JurorPool jurorPool,
                                CourtLocation courtLocation,
                                CourtLocation bureauLocation,
                                WelshCourtLocation welshCourtLocation) {
        super(jurorPool, courtLocation, bureauLocation, welshCourtLocation);
    }

    @Override
    protected FormCode welshFormCode() {
        return FormCode.BI_DEFERRALDENIED;
    }

    @Override
    protected FormCode englishFormCode() {
        return FormCode.ENG_DEFERRALDENIED;
    }

    @Override
    protected LetterDataType signatory() {
        return LetterDataType.BUREAU_SIGNATORY;
    }

    @Override
    protected boolean includesCourtAddress() {
        return true;
    }
}
