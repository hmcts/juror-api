package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.controller.response.PoolSummaryResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.PoolStatistics;

public interface PoolStatisticsService {

    PoolSummaryResponseDto calculatePoolStatistics(String poolNumber);

    PoolStatistics getPoolStatistics(String poolNumber);
}
