package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.controller.response.CourtCatchmentStatusDto;

public interface CourtCatchmentService {

    /**
     * The Service to match the post code agaist the court code.
     */

    CourtCatchmentStatusDto courtCatchmentFinder(String jurorNumber);
}

