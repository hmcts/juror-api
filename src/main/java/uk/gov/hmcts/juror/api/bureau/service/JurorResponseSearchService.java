package uk.gov.hmcts.juror.api.bureau.service;

import uk.gov.hmcts.juror.api.bureau.controller.request.JurorResponseSearchRequest;
import uk.gov.hmcts.juror.api.bureau.controller.response.JurorResponseSearchResults;

/**
 * Service to search for juror responses.
 */
public interface JurorResponseSearchService { //NOSONAR

    /**
     * Search for juror responses which match specified filters.
     * Note that any supplied filters are ANDed together in the resulting database query
     *
     * @param searchRequest search criteria, not null
     * @param isTeamLeader  whether the search is being made by a team leader (false = bureau officer filters only)
     * @return search results, not null
     */
    JurorResponseSearchResults searchForResponses(JurorResponseSearchRequest searchRequest, boolean isTeamLeader);
}
