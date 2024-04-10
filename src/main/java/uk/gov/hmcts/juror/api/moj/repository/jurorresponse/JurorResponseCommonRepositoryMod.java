package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.juror.api.bureau.domain.ReadOnlyRepository;
import uk.gov.hmcts.juror.api.juror.domain.JurorResponse;
import uk.gov.hmcts.juror.api.moj.controller.request.summonsmanagement.JurorResponseRetrieveRequestDto;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.AbstractJurorResponse;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.JurorResponseCommon;

import java.util.List;

public interface JurorResponseCommonRepositoryMod {
    List<Tuple> retrieveJurorResponseDetails(JurorResponseRetrieveRequestDto request,
                                             boolean isTeamLeader,
                                             int resultsLimit);

    AbstractJurorResponse findByJurorNumber(String jurorNumber);
}
