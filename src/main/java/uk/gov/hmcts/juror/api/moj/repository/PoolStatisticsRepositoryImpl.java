package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.QPoolStatistics;
import uk.gov.hmcts.juror.api.moj.utils.DateUtils;

import java.time.LocalDate;
import java.util.List;

public class PoolStatisticsRepositoryImpl implements IPoolStatisticsRepository {
    @PersistenceContext
    EntityManager entityManager;

    private static final QPoolStatistics POOL_STATS = QPoolStatistics.poolStatistics;

    private static final QPoolRequest POOL_REQUEST = QPoolRequest.poolRequest;

    /***
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
                                                               String poolType, int numberOfWeeks) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        LocalDate weekCommencing = DateUtils.getStartOfWeekFromDate(LocalDate.now());
        return queryFactory
            .select(
                POOL_REQUEST.returnDate,
                POOL_STATS.poolNumber,
                POOL_STATS.totalSummoned,
                POOL_STATS.courtSupply,
                POOL_STATS.available,
                POOL_STATS.unavailable,
                POOL_STATS.unresolved,
                POOL_REQUEST.numberRequested
            )
            .from(POOL_STATS)
            .join(POOL_REQUEST)
            .on(POOL_REQUEST.poolNumber.eq(POOL_STATS.poolNumber))
            .where(POOL_REQUEST.returnDate.goe(weekCommencing))
            .where(POOL_REQUEST.returnDate.loe(weekCommencing.plusWeeks(numberOfWeeks)))
            .where(POOL_REQUEST.courtLocation.locCode.eq(courtLocationCode))
            .where(POOL_REQUEST.poolType.poolType.eq(poolType))
            .where(POOL_REQUEST.owner.eq(owner))
            .orderBy(POOL_REQUEST.returnDate.asc())
            .fetch();
    }

    @Override
    public List<Tuple> getNilPools(String owner, String courtLocationCode, String poolType, int numberOfWeeks) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        LocalDate weekCommencing = DateUtils.getStartOfWeekFromDate(LocalDate.now());
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
