package uk.gov.hmcts.juror.api.moj.service;

import uk.gov.hmcts.juror.api.moj.controller.request.JurorPoolSearch;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorDetailsDto;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;

import java.util.List;

public interface JurorPoolService {
    PoolRequest getPoolRequest(String poolNumber);

    boolean hasPoolWithLocCode(String jurorNumber, List<String> locCodes);

    PaginatedList<JurorDetailsDto> search(JurorPoolSearch request);

    JurorPool getJurorPoolFromUser(String jurorNumber);

    JurorPool getLastJurorPoolForJuror(String locCode, String jurorNumber);

    JurorPool getJurorPoolForJuror(String jurorNumber, String poolNumber);

    JurorPool save(JurorPool jurorPool);

    int getCountJurorsDueToAttendCourtNextWeek(String locCode, boolean reasonableAdjustmentRequired);
}
