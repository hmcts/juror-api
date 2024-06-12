package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.domain.Juror;

public interface JurorServiceMod {
    Juror getJurorFromJurorNumber(String jurorNumber);
}
