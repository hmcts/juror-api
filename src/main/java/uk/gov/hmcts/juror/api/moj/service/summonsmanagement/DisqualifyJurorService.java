package uk.gov.hmcts.juror.api.moj.service.summonsmanagement;

import uk.gov.hmcts.juror.api.config.bureau.BureauJwtPayload;
import uk.gov.hmcts.juror.api.moj.controller.request.summonsmanagement.DisqualifyJurorDto;
import uk.gov.hmcts.juror.api.moj.controller.response.summonsmanagement.DisqualifyReasonsDto;

public interface DisqualifyJurorService {
    /**
     * Get disqualification reasons (codes).
     */
    DisqualifyReasonsDto getDisqualifyReasons(BureauJwtPayload payload);

    /**
     * Disqualify a juror.
     */
    void disqualifyJuror(String jurorNumber, DisqualifyJurorDto disqualifyJuror, BureauJwtPayload payload);

    void disqualifyJurorDueToAgeOutOfRange(String jurorNumber, BureauJwtPayload payload);
}
