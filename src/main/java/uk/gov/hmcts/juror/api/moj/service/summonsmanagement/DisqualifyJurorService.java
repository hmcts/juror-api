package uk.gov.hmcts.juror.api.moj.service.summonsmanagement;

import uk.gov.hmcts.juror.api.config.bureau.BureauJWTPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.summonsmanagement.DisqualifyJurorDto;
import uk.gov.hmcts.juror.api.moj.controller.response.summonsmanagement.DisqualifyReasonsDto;

public interface DisqualifyJurorService {
    /**
     * Get disqualification reasons (codes).
     */
    DisqualifyReasonsDto getDisqualifyReasons(BureauJWTPayload payload);

    /**
     * Disqualify a juror.
     */
    void disqualifyJuror(String jurorNumber, DisqualifyJurorDto disqualifyJuror, BureauJWTPayload payload);

    void disqualifyJurorDueToAgeOutOfRange(String jurorNumber, BureauJWTPayload payload);
}
