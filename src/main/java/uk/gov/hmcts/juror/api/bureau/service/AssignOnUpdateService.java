package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;

/**
 * Operations for assigning officers to responses in backlog whenever a change/update is made.
 */
public interface AssignOnUpdateService {

    /**
     * Called when a bureau officer is making a change to an unassigned response, and assigned to them.
     *
     * @param jurorResponse   The response entity
     * @param auditorUsername The user performing the merge
     */
    void assignToCurrentLogin(DigitalResponse jurorResponse, String auditorUsername);
}
