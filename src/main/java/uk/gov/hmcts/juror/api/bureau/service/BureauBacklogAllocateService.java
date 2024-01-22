package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.controller.request.BureauBacklogAllocateRequestDto;

/**
 * Allocate juror responses to selected bureau officers.
 */
public interface BureauBacklogAllocateService {

    /**
     * Auto-assign juror response backlog to bureau officers.
     *
     * @param request        number of allocations to apply to each officer
     * @param requestingUser the username of the logged-in user (for audit purposes)
     * @throws RuntimeException if unable to allocate
     */
    void allocateBacklogReplies(BureauBacklogAllocateRequestDto request, String requestingUser);


}
