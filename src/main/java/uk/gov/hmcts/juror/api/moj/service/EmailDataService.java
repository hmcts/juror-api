package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.domain.JurorPool;

public interface EmailDataService {

    void emailConfirmationLetter(JurorPool jurorPool);

    void emailDeferralLetter(JurorPool jurorPool);

    void emailDeferralDeniedLetter(JurorPool jurorPool);

    void emailExcusalGrantedLetter(JurorPool jurorPool);

    void emailExcusalDeniedLetter(JurorPool jurorPool, String refusedExcusal);

    void emailWithdrawalLetter(JurorPool jurorPool, String code);

    void emailPostponementLetter(JurorPool jurorPool);

}
