package uk.gov.hmcts.juror.api.moj.repository;

import uk.gov.hmcts.juror.api.moj.controller.request.ActivePoolFilterQuery;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestActiveDataDto;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;

/**
 * Custom Repository definition for the ActivePoolsCourt entity.
 * Allowing for additional query functions to be explicitly declared
 */
public interface IActivePoolsRepository {

    PaginatedList<PoolRequestActiveDataDto> getActivePoolRequests(ActivePoolFilterQuery filterQuery);
}
