package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.controller.request.JurorPoolSearch;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;

import java.util.List;
import java.util.Optional;

public interface JurorPoolService {
    PoolRequest getPoolRequest(String poolNumber);

    boolean hasPoolWithLocCode(String jurorNumber, List<String> locCodes);

    PaginatedList<JurorDetailsDto> search(JurorPoolSearch request);

    JurorPool getJurorPoolFromUser(String jurorNumber);

    JurorPool getJurorPoolFromUser(String jurorNumber, boolean allowBureauByPass);

    Optional<JurorPool> getJurorPoolFromUserOptional(String jurorNumber);

    Optional<JurorPool> getJurorPoolFromUserOptional(String jurorNumber, boolean allowBureau);

    JurorPool getLastJurorPoolForJuror(String locCode, String jurorNumber);
}
