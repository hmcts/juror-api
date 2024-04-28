package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import uk.gov.hmcts.juror.api.moj.controller.request.CoronerPoolFilterRequestQuery;

public interface ICoronerPoolRepository {

    JPAQuery<Tuple> fetchFilteredCoronerPools(CoronerPoolFilterRequestQuery query);

}


