package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.QPoolStatisticsWithPoolJoin;

import java.time.LocalDate;
import java.util.List;

public class PoolStatisticsRepositoryImpl implements IPoolStatisticsRepository {
    @PersistenceContext
    EntityManager entityManager;

    private static final QPoolStatisticsWithPoolJoin POOL_STATS_WITH_POOL_JOIN =
        QPoolStatisticsWithPoolJoin.poolStatisticsWithPoolJoin;

    private static final QPoolRequest POOL_REQUEST = QPoolRequest.poolRequest;

    /**
     * Gets a list of tuples, providing statistics about active pools within a court location for a pool type over a
     * span of a number of weeks.
     * <p/>
     * The usecase for this is to be used by bureau users to get metrics about the courts (filtered by location and
     * type) over a span of eight weeks.
     *
     * @param owner Three-digit number representing a primary court location who currently has write access to the pool
     * @param courtLocationCode Three-digit number representing a court location
     * @param poolType Three-letter code to represent the type of pool e.g. crown court etc
     * @param numberOfWeeks total amount of weeks of data to fetch
     * @return List<Tuple/>
     */
    @Override
    public List<Tuple> getStatisticsByCourtLocationAndPoolType(String owner, String courtLocationCode,
                                                               String poolType, LocalDate weekCommencing,
                                                               int numberOfWeeks) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFactory
            .select(
                POOL_STATS_WITH_POOL_JOIN.returnDate,
                POOL_STATS_WITH_POOL_JOIN.poolNumber,
                POOL_STATS_WITH_POOL_JOIN.totalSummoned,
                POOL_STATS_WITH_POOL_JOIN.courtSupply,
                POOL_STATS_WITH_POOL_JOIN.available,
                POOL_STATS_WITH_POOL_JOIN.unavailable,
                POOL_STATS_WITH_POOL_JOIN.unresolved,
                POOL_STATS_WITH_POOL_JOIN.numberRequested
            )
            .from(POOL_STATS_WITH_POOL_JOIN)
            .where(POOL_STATS_WITH_POOL_JOIN.returnDate.goe(weekCommencing))
            .where(POOL_STATS_WITH_POOL_JOIN.returnDate.loe(weekCommencing.plusWeeks(numberOfWeeks)))
            .where(POOL_STATS_WITH_POOL_JOIN.locCode.eq(courtLocationCode))
            .where(POOL_STATS_WITH_POOL_JOIN.poolType.eq(poolType))
            .where(POOL_STATS_WITH_POOL_JOIN.owner.eq(owner))
            .orderBy(POOL_STATS_WITH_POOL_JOIN.returnDate.asc())
            .fetch();
    }

    @Override
    public List<Tuple> getNilPools(String owner, String courtLocationCode, String poolType,
                                   LocalDate weekCommencing, int numberOfWeeks) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFactory.select(
                POOL_REQUEST.poolNumber,
                POOL_REQUEST.numberRequested,
                POOL_REQUEST.returnDate)
            .from(POOL_REQUEST)
            .where(POOL_REQUEST.nilPool.isTrue())
            .where(POOL_REQUEST.numberRequested.isNotNull().and(POOL_REQUEST.numberRequested.eq(0)))
            .where(POOL_REQUEST.returnDate.goe(weekCommencing))
            .where(POOL_REQUEST.returnDate.loe(weekCommencing.plusWeeks(numberOfWeeks)))
            .where(POOL_REQUEST.courtLocation.locCode.eq(courtLocationCode))
            .where(POOL_REQUEST.poolType.poolType.eq(poolType))
            .where(POOL_REQUEST.owner.eq(owner))
            .where().orderBy(POOL_REQUEST.returnDate.asc()).fetch();
    }
}
