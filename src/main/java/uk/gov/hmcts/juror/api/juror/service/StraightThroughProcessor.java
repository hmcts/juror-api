package uk.gov.hmcts.juror.api.juror.service;

import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;

/**
 * Straight through processing interface for Juror Responses to be automatically completed without intervention from a.
 * Bureau officer.
 */
public interface StraightThroughProcessor {

    void processAcceptance(DigitalResponse digitalResponse);

    void processDeceasedExcusal(DigitalResponse digitalResponse);

    void processAgeExcusal(DigitalResponse digitalResponse);
}
