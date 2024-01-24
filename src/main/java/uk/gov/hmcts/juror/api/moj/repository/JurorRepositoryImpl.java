package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.Juror;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * Custom Repository implementation for the Juror entity.
 */
@SuppressWarnings("PMD.LawOfDemeter")
public class JurorRepositoryImpl implements IJurorRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QJuror JUROR = QJuror.juror;
    private static final QPoolRequest POOL_REQUEST = QPoolRequest.poolRequest;

    @Override
    public Juror findByJurorNumberAndOwnerAndDeferralDate(String jurorNumber, String owner, LocalDate deferralDate) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.selectFrom(JUROR)
            .where(JUROR.jurorNumber.eq(jurorNumber))
            .join(JUROR_POOL).on(JUROR_POOL.juror.eq(JUROR))
            .where(JUROR_POOL.owner.eq(owner))
            .where(JUROR_POOL.deferralDate.eq(deferralDate))
            .fetchOne();
    }

    @Override
    public List<Juror> findByPoolNumberAndWasDeferredAndIsActive(String poolNumber, boolean wasDeferred,
                                                                 boolean isActive) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.selectFrom(JUROR)
            .join(JUROR_POOL).on(JUROR_POOL.juror.eq(JUROR))
            .where(JUROR_POOL.isActive.eq(isActive))
            .where(JUROR_POOL.wasDeferred.eq(wasDeferred))
            .join(POOL_REQUEST).on(POOL_REQUEST.eq(JUROR_POOL.pool))
            .where(POOL_REQUEST.poolNumber.eq(poolNumber))
            .fetch();
    }

    @Override
    public List<Juror> findByPoolNumberAndIsActive(String poolNumber, boolean isActive) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.selectFrom(JUROR)
            .join(JUROR_POOL).on(JUROR_POOL.juror.eq(JUROR))
            .where(JUROR_POOL.isActive.eq(isActive))
            .join(POOL_REQUEST).on(POOL_REQUEST.eq(JUROR_POOL.pool))
            .where(POOL_REQUEST.poolNumber.eq(poolNumber))
            .fetch();
    }

    @Override
    public Juror findByJurorNumberAndPoolNumberAndIsActive(String jurorNumber, String poolNumber, boolean isActive) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.selectFrom(JUROR)
            .where(JUROR.jurorNumber.eq(jurorNumber))
            .join(JUROR_POOL).on(JUROR_POOL.juror.eq(JUROR))
            .where(JUROR_POOL.isActive.eq(isActive))
            .join(POOL_REQUEST).on(POOL_REQUEST.eq(JUROR_POOL.pool))
            .where(POOL_REQUEST.poolNumber.eq(poolNumber))
            .fetchOne();
    }

    @Override
    public List<Juror> findByJurorNumberAndIsActive(String jurorNumber, boolean isActive) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.selectFrom(JUROR)
            .where(JUROR.jurorNumber.eq(jurorNumber))
            .join(JUROR_POOL).on(JUROR_POOL.juror.eq(JUROR))
            .where(JUROR_POOL.isActive.eq(isActive))
            .fetch();
    }

    @Override
    public Juror findByJurorNumberAndIsActiveAndCourt(String jurorNumber, boolean isActive, CourtLocation locCode) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.selectFrom(JUROR)
            .where(JUROR.jurorNumber.eq(jurorNumber))
            .join(JUROR_POOL).on(JUROR_POOL.juror.eq(JUROR))
            .where(JUROR_POOL.isActive.eq(isActive))
            .join(POOL_REQUEST).on(POOL_REQUEST.eq(JUROR_POOL.pool))
            .where(POOL_REQUEST.courtLocation.eq(locCode))
            .fetchOne();
    }

    @Override
    public List<Juror> findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(List<String> jurorNumbers,
                                                                                      boolean isActive,
                                                                                      String poolNumber,
                                                                                      CourtLocation court,
                                                                                      List<Integer> status) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.selectFrom(JUROR)
            .where(JUROR.jurorNumber.in(jurorNumbers))
            .join(JUROR_POOL).on(JUROR_POOL.juror.eq(JUROR))
            .where(JUROR_POOL.isActive.eq(isActive))
            .where(JUROR_POOL.status.status.in(status))
            .join(POOL_REQUEST).on(POOL_REQUEST.eq(JUROR_POOL.pool))
            .where(POOL_REQUEST.courtLocation.eq(court))
            .fetch();
    }

    @Override
    public List<Juror> findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(List<String> jurorNumbers, boolean isActive,
                                                                           String poolNumber, CourtLocation court) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.selectFrom(JUROR)
            .where(JUROR.jurorNumber.in(jurorNumbers))
            .join(JUROR_POOL).on(JUROR_POOL.juror.eq(JUROR))
            .where(JUROR_POOL.isActive.eq(isActive))
            .join(POOL_REQUEST).on(POOL_REQUEST.eq(JUROR_POOL.pool))
            .where(POOL_REQUEST.courtLocation.eq(court))
            .fetch();
    }

    @Override
    public Juror findByOwnerAndJurorNumberAndPoolNumber(String owner, String jurorNumber, String poolNumber) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.selectFrom(JUROR)
            .where(JUROR.jurorNumber.eq(jurorNumber))
            .join(JUROR_POOL).on(JUROR_POOL.juror.eq(JUROR))
            .where(JUROR_POOL.owner.eq(owner))
            .join(POOL_REQUEST).on(POOL_REQUEST.eq(JUROR_POOL.pool))
            .where(POOL_REQUEST.poolNumber.eq(poolNumber))
            .fetchOne();
    }

    @Override
    public List<Juror> findByPoolNumberAndOwnerAndIsActive(String poolNumber, String owner, boolean isActive) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.selectFrom(JUROR)
            .join(JUROR_POOL).on(JUROR_POOL.juror.eq(JUROR))
            .where(JUROR_POOL.owner.eq(owner))
            .where(JUROR_POOL.isActive.eq(isActive))
            .join(POOL_REQUEST).on(POOL_REQUEST.eq(JUROR_POOL.pool))
            .where(POOL_REQUEST.poolNumber.eq(poolNumber))
            .fetch();
    }

}
