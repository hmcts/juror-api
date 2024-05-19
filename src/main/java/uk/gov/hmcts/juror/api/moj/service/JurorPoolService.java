package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;

import java.util.List;

public interface JurorPoolService {
    PoolRequest getPoolRequest(String poolNumber);

    boolean hasPoolWithLocCode(String jurorNumber, List<String> locCodes);
}
