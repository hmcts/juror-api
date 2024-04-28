package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.controller.request.CoronerPoolFilterRequestQuery;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolSearchRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestSearchListDto;
import uk.gov.hmcts.juror.api.moj.domain.FilterCoronerPool;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;

import java.util.List;

public interface PoolRequestSearchService {

    PoolRequestSearchListDto searchForPoolRequest(PoolSearchRequestDto poolSearchRequestDto, List<String> courts);

    PaginatedList<FilterCoronerPool> searchForCoronerPools(CoronerPoolFilterRequestQuery query);
}
