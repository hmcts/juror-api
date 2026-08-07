package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.domain.JurorPool;

public interface EmailDataService {

    void emailDeferralLetter(JurorPool jurorPool);

}
