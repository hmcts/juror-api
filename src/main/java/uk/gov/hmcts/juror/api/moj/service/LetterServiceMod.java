package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.domain.Juror;

public interface LetterServiceMod {
    void createDisqualificationLetter(String jurorNumber, String disqualifyCode);

    default void createDisqualificationLetter(Juror juror) {
        createDisqualificationLetter(juror.getJurorNumber(), juror.getDisqualifyCode());
    }
}
