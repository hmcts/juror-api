package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import com.querydsl.core.Tuple;
import uk.gov.hmcts.juror.api.moj.controller.request.summonsmanagement.JurorResponseRetrieveRequestDto;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseCommon;

import java.util.List;

public interface JurorResponseCommonRepositoryMod {
    List<Tuple> retrieveJurorResponseDetails(JurorResponseRetrieveRequestDto request,
                                             boolean isTeamLeader,
                                             int resultsLimit);

    AbstractJurorResponse findByJurorNumber(String jurorNumber);

    List<JurorResponseCommon> findByJurorNumberIn(List<String> jurorNumber);
}
