package uk.gov.hmcts.juror.api.moj.service;


import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.DigitalResponse;

public interface AssignOnUpdateServiceMod {
    /**
     * Called when a bureau officer is making a change to an unassigned response, and assigned to them.
     * This is the new service pointing to the new schema
     *
     * @param jurorResponse   The response entity
     * @param auditorUsername The user performing the merge
     */
    void assignToCurrentLogin(DigitalResponse jurorResponse, String auditorUsername);
}
