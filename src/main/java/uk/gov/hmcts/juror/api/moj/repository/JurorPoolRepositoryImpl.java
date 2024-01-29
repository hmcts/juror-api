package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;

import java.time.LocalDate;
import java.util.List;

/**
 * Custom Repository implementation for the JurorPool entity.
 */
@SuppressWarnings("PMD.LawOfDemeter")
public class JurorPoolRepositoryImpl implements IJurorPoolRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QJuror JUROR = QJuror.juror;
    private static final QPoolRequest POOL_REQUEST = QPoolRequest.poolRequest;
    private static final QAppearance APPEARANCE = QAppearance.appearance;

    @Override
    public String findLatestPoolSequence(String poolNumber) {


        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        JurorPool jurorPool = queryFactory.selectFrom(JUROR_POOL)
            .join(POOL_REQUEST).on(POOL_REQUEST.eq(JUROR_POOL.pool))
            .where(POOL_REQUEST.poolNumber.startsWith(poolNumber))
            .orderBy(JUROR_POOL.poolSequence.desc())
            .fetchFirst();

        return jurorPool == null
            ? null
            : jurorPool.getPoolSequence();
    }

    @Override
    public JurorPool findByJurorNumberAndIsActiveAndCourt(String jurorNumber, Boolean isActive, CourtLocation locCode) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.selectFrom(JUROR_POOL)
            .where(JUROR_POOL.juror.jurorNumber.eq(jurorNumber))
            .where(JUROR_POOL.isActive.eq(isActive))
            .join(POOL_REQUEST).on(POOL_REQUEST.eq(JUROR_POOL.pool))
            .where(POOL_REQUEST.courtLocation.eq(locCode))
            .fetchOne();
    }

    @Override
    public List<JurorPool> findByJurorNumberInAndIsActiveAndPoolNumberAndCourtAndStatusIn(List<String> jurorNumbers,
                                                                                          boolean isActive,
                                                                                          String poolNumber,
                                                                                          CourtLocation court,
                                                                                          List<Integer> status) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.selectFrom(JUROR_POOL)
            .where(JUROR_POOL.juror.jurorNumber.in(jurorNumbers))
            .where(JUROR_POOL.isActive.eq(isActive))
            .where(JUROR_POOL.status.status.in(status))
            .join(POOL_REQUEST).on(POOL_REQUEST.eq(JUROR_POOL.pool))
            .where(POOL_REQUEST.poolNumber.eq(poolNumber))
            .where(POOL_REQUEST.courtLocation.eq(court))
            .fetch();
    }

    @Override
    public List<JurorPool> findByJurorNumberInAndIsActiveAndPoolNumberAndCourt(List<String> jurorNumbers,
                                                                               boolean isActive, String poolNumber,
                                                                               CourtLocation court) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.selectFrom(JUROR_POOL)
            .where(JUROR_POOL.juror.jurorNumber.in(jurorNumbers))
            .where(JUROR_POOL.isActive.eq(isActive))
            .join(POOL_REQUEST).on(POOL_REQUEST.eq(JUROR_POOL.pool))
            .where(POOL_REQUEST.poolNumber.eq(poolNumber))
            .where(POOL_REQUEST.courtLocation.eq(court))
            .fetch();
    }

    @Override
    public List<JurorPool> findJurorsOnCallAtCourtLocation(String locCode, List<String> poolNumbers) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.selectFrom(JUROR_POOL)
            .join(POOL_REQUEST).on(POOL_REQUEST.eq(JUROR_POOL.pool))
            .where(JUROR_POOL.status.status.eq(IJurorStatus.RESPONDED))
            .where(JUROR_POOL.onCall.eq(true))
            .where(POOL_REQUEST.courtLocation.locCode.eq(locCode))
            .where(JUROR_POOL.isActive.eq(true))
            .where(JUROR_POOL.pool.poolNumber.in(poolNumbers))
            .fetch();
    }

    @Override
    public List<JurorPool> findJurorsInAttendanceAtCourtLocation(String locCode, List<String> poolNumbers) {
        // jurors in attendance but not on trial

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.selectFrom(JUROR_POOL)
            .join(POOL_REQUEST).on(POOL_REQUEST.eq(JUROR_POOL.pool))
            .leftJoin(APPEARANCE).on((JUROR_POOL.juror.jurorNumber.eq(APPEARANCE.jurorNumber)).and(
                JUROR_POOL.pool.poolNumber.eq(APPEARANCE.poolNumber)))
            .where(JUROR_POOL.status.status.eq(IJurorStatus.RESPONDED))
            .where(JUROR_POOL.onCall.isNull().or(JUROR_POOL.onCall.eq(false)))  // may not need this condition
            .where(POOL_REQUEST.courtLocation.locCode.eq(locCode))
            .where(JUROR_POOL.isActive.eq(true))
            .where(JUROR_POOL.pool.poolNumber.in(poolNumbers))
            .where(APPEARANCE.appearanceStage.eq(AppearanceStage.valueOf("CHECKED_IN")))
            .fetch();
    }

    @Override
    public List<JurorPool> findJurorsNotInAttendanceAtCourtLocation(String locCode, List<String> poolNumbers) {
        // jurors not in attendance and not on call (others)

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.selectFrom(JUROR_POOL)
            .join(POOL_REQUEST).on(POOL_REQUEST.eq(JUROR_POOL.pool))
            .where(JUROR_POOL.status.status.eq(IJurorStatus.RESPONDED))
            .where(JUROR_POOL.onCall.isNull().or(JUROR_POOL.onCall.eq(false)))   // may not need this condition
            .where(POOL_REQUEST.courtLocation.locCode.eq(locCode))
            .where(JUROR_POOL.isActive.eq(true))
            .where(JUROR_POOL.pool.poolNumber.in(poolNumbers))
            .where(JUROR_POOL.notIn(findJurorsInAttendanceAtCourtLocation(locCode, poolNumbers)))
            .fetch();

    }

    @Override
    public List<Tuple> getJurorsToDismiss(List<String> poolNumbers, List<String> jurorNumbers, String locCode) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFactory.select(
                JUROR_POOL.juror.jurorNumber.as("juror_number"),
                JUROR_POOL.juror.firstName.as("first_name"),
                JUROR_POOL.juror.lastName.as("last_name"),
                getJurorAttendance().as("attending"),
                APPEARANCE.timeIn.as("checked_in"),
                getNextDueAtCourt().as("next_due_at_court"),
                JUROR_POOL.pool.returnDate.as("service_start_date")
            )
            .from(JUROR_POOL)
            .join(POOL_REQUEST).on(POOL_REQUEST.eq(JUROR_POOL.pool))
            .leftJoin(APPEARANCE).on((JUROR_POOL.juror.jurorNumber.eq(APPEARANCE.jurorNumber)).and(
                JUROR_POOL.pool.poolNumber.eq(APPEARANCE.poolNumber)))
            .where(JUROR_POOL.status.status.eq(IJurorStatus.RESPONDED))
            .where(JUROR_POOL.isActive.eq(true))
            .where(JUROR_POOL.pool.poolNumber.in(poolNumbers))
            .where(JUROR_POOL.juror.jurorNumber.in(jurorNumbers))
            .where(POOL_REQUEST.courtLocation.locCode.eq(locCode))
            .fetch();

    }

    private static StringExpression getJurorAttendance() {
        return new CaseBuilder()
            .when(Expressions.asBoolean(JUROR_POOL.onCall.eq(true)))
            .then("On call")
            .when(APPEARANCE.appearanceStage.eq(AppearanceStage.valueOf("CHECKED_IN")))
            .then("In attendance")
            .otherwise("Other");
    }

    private static StringExpression getNextDueAtCourt() {
        return new CaseBuilder()
            .when(Expressions.asBoolean(JUROR_POOL.onCall.eq(true)))
            .then("On call")
            .otherwise(JUROR_POOL.nextDate.stringValue());
    }

}
