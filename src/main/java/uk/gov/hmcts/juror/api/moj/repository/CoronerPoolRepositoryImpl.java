package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.api.moj.controller.request.CoronerPoolFilterRequestQuery;
import uk.gov.hmcts.juror.api.moj.domain.QCoronerPool;

/**
 * Custom Repository implementation for extracting data related to coroner pools.
 */
@Slf4j
public class CoronerPoolRepositoryImpl implements ICoronerPoolRepository {

    private static final QCoronerPool CORONER_POOL = QCoronerPool.coronerPool;
    @PersistenceContext
    EntityManager entityManager;

    @Override
    public JPAQuery<Tuple> fetchFilteredCoronerPools(CoronerPoolFilterRequestQuery query) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        JPAQuery<?> partialQuery = queryFactory.from(CORONER_POOL);

        if (null != query.getPoolNumber()) {
            partialQuery.where(CORONER_POOL.poolNumber.like(query.getPoolNumber() + "%"));
        }

        if (null != query.getLocationCode()) {
            partialQuery.where(CORONER_POOL.courtLocation.locCode.eq(query.getLocationCode()));
        }

        if (null != query.getRequestedDate()) {
            partialQuery.where(CORONER_POOL.requestDate.eq(query.getRequestedDate()));
        }

        if (null != query.getRequestedBy()) {
            partialQuery.where(CORONER_POOL.name.containsIgnoreCase(query.getRequestedBy()));
        }

        return partialQuery.distinct().select(
            CORONER_POOL.poolNumber,
            CORONER_POOL.courtLocation.locCourtName,
            CORONER_POOL.requestDate,
            CORONER_POOL.name);
    }
}
