package uk.gov.hmcts.juror.api.juror.service;

import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;

/**
 * Straight through processing interface for Juror Responses to be automatically completed without intervention from a.
 * Bureau officer.
 */
public interface StraightThroughProcessor {
    void processAcceptance(JurorResponse jurorResponse) throws StraightThroughProcessingServiceException;

    void processDeceasedExcusal(
        JurorResponse jurorResponse) throws StraightThroughProcessingServiceException.DeceasedExcusal;

    void processAgeExcusal(JurorResponse jurorResponse) throws StraightThroughProcessingServiceException.AgeExcusal;
}
