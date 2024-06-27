package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.domain.Juror;

import java.util.Optional;

public interface JurorServiceMod {
    Juror getJurorFromJurorNumber(String jurorNumber);

    Optional<Juror> getJurorOptionalFromJurorNumber(String jurorNumber);
}
