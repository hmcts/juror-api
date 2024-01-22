package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.controller.request.AutoAssignRequest;
import uk.gov.hmcts.juror.api.bureau.controller.response.AutoAssignResponse;
import uk.gov.hmcts.juror.api.bureau.exception.AutoAssignException;

/**
 * Automatically assigns juror responses to bureau officers.
 */
public interface AutoAssignmentService {

    /**
     * Auto-assign juror response backlog to bureau officers.
     *
     * @param request        auto-assignment parameters
     * @param requestingUser the username of the logged-in user (for audit purposes)
     * @throws AutoAssignException if unable to auto-assign
     */
    void autoAssign(AutoAssignRequest request, String requestingUser) throws AutoAssignException;

    /**
     * Get auto-assignment capacity data.
     *
     * @return default capacity level and current workload for all active bureau officers
     */
    AutoAssignResponse getAutoAssignmentData();
}
