package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolSearchRequestDto;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolSearchRequestDto.PoolStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.QPoolType;
import uk.gov.hmcts.juror.api.moj.domain.SortDirection;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Enable a user to search for a specific pool request (or a result set containing multiple pool requests)
 * matching provided search criteria.
 * <p/>
 * As part of the basic search criteria, at least one of the below search criteria is mandatory:
 * <ul>
 *     <li>Pool Number (could be a complete pool number or a partial match)</li>
 *     <li>Court Location (3 digit location code)</li>
 *     <li>Court Location (3 digit location code)</li>
 * </ul>
 * <p/>
 * In conjunction with <b>at least</b> one mandatory basic search criteria item, any combination of the below
 * additional, optional advanced search criteria can also be supplied:
 * <p/>
 * Pool Status
 * <ul>
 *     <li>Requested - Pool has been requested but not yet created by the Bureau (no citizens summonsed)</li>
 *     <li>Active - Pool members are present in the pool who have not yet completed their service or been marked
 *     inactive</li>
 *     <li>Completed - Pool members are present in the pool who have all completed their service or been marked
 *     inactive</li>
 * </ul>
 * <p/>
 * Pool Stage (who owns the latest, active copy of the pool request)
 * <ul>
 *     <li>With the Bureau</li>
 *     <li>At court</li>
 * </ul>
 * <p/>
 * Pool Type
 * <ul>
 *     <li>Crown Court</li>
 *     <li>Civil Court</li>
 *     <li>High Court</li>
 * </ul>
 */
public class PoolRequestSearchQueries implements IPoolRequestSearchQueries {

    @PersistenceContext
    EntityManager entityManager;

    private static final QPoolRequest POOL_REQUEST = QPoolRequest.poolRequest;
    private static final QCourtLocation COURT_LOCATION = QCourtLocation.courtLocation;
    private static final QPoolType POOL_TYPE = QPoolType.poolType1;
    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final List<PoolStatus> REQUESTED_AND_ACTIVE = Arrays.asList(PoolStatus.REQUESTED, PoolStatus.ACTIVE);
    private static final List<PoolStatus> REQUESTED_AND_COMPLETED = Arrays.asList(
        PoolStatus.REQUESTED,
        PoolStatus.COMPLETED
    );
    private static final List<PoolStatus> COMPLETED_AND_ACTIVE = Arrays.asList(PoolStatus.COMPLETED, PoolStatus.ACTIVE);

    /**
     * Query builder to define the initial SELECT statement from the POOL_REQUEST view using the PoolRequest entity.
     */
    @Override
    public JPAQuery<Tuple> selectFromPoolRequest() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFactory.select(
                POOL_REQUEST.poolNumber.as("POOL_NO"),
                POOL_REQUEST.owner.as("OWNER"),
                COURT_LOCATION.name.as("COURT_NAME"),
                POOL_TYPE.description.as("POOL_TYPE"),
                POOL_REQUEST.returnDate.as("SERVICE_START_DATE"),
                POOL_REQUEST.newRequest.as("NEW_REQUEST"),
                countActivePoolMembers().as("ACTIVE_POOL_MEMBERS"),
                POOL_REQUEST.numberRequested.as("NUMBER_REQUESTED"),
                POOL_REQUEST.nilPool.as("NIL_POOL")
            )
            .from(POOL_REQUEST)
            .innerJoin(COURT_LOCATION)
            .on(POOL_REQUEST.courtLocation.eq(COURT_LOCATION))
            .innerJoin(POOL_TYPE)
            .on(POOL_REQUEST.poolType.eq(POOL_TYPE))
            .leftJoin(JUROR_POOL)
            .on(POOL_REQUEST.eq(JUROR_POOL.pool))
            .where(JUROR_POOL.isActive.isTrue().or(JUROR_POOL.isActive.isNull()))
            .groupBy(POOL_REQUEST.poolNumber)
            .groupBy(POOL_REQUEST.owner)
            .groupBy(COURT_LOCATION.name)
            .groupBy(POOL_TYPE.description)
            .groupBy(POOL_REQUEST.returnDate)
            .groupBy(POOL_REQUEST.newRequest)
            .groupBy(POOL_REQUEST.numberRequested)
            .groupBy(POOL_REQUEST.nilPool);
    }

    /**
     * When a complete Pool Number is specified, apply a predicate to match exactly on the supplied criteria.
     */
    @Override
    public void addPoolNumberPredicate(JPAQuery<Tuple> query, String poolNumber) {
        query.where(POOL_REQUEST.poolNumber.eq(poolNumber));
    }

    /**
     * When a partial Pool Number is specified, apply a predicate to match Pool Request whose Pool Number starts with
     * the provided search criteria.
     */
    @Override
    public void addPartialPoolNumberPredicate(JPAQuery<Tuple> query, String poolNumberPrefix) {
        query.where(POOL_REQUEST.poolNumber.startsWith(poolNumberPrefix));
    }

    /**
     * When a Court Location code is specified, apply a predicate to match exactly on the supplied criteria.
     */
    @Override
    public void addCourtLocationPredicate(JPAQuery<Tuple> query, String courtLocationCode) {
        query.where(COURT_LOCATION.locCode.eq(courtLocationCode));
    }

    /**
     * Court Users always have search results filtered based on the courts they have access to.
     */
    @Override
    public void addCourtUserPredicate(JPAQuery<Tuple> query, List<String> courtLocationCodes) {
        query.where(COURT_LOCATION.locCode.in(courtLocationCodes));
    }

    /**
     * When a Service Start Date is specified, apply a predicate to match exactly against the Return Date on the
     * supplied criteria.
     */
    @Override
    public void addServiceStartDatePredicate(JPAQuery<Tuple> query, LocalDate returnDate) {
        query.where(POOL_REQUEST.returnDate.eq(returnDate));
    }

    /**
     * Requested - The pool has been requested by the court (NEW_REQUEST = 'Y').
     * or the pool has been transferred to the bureau but not yet created (NEW_REQUEST = 'T')
     * <p/>
     * Active - Either a Nil Pool with a future start date
     * (NIL_POLL = TRUE, NUMBER_REQUESTED = 0, NEW_REQUEST = 'N' and RETURN_DATE >= Today's date)
     * or a pool that has been created by the Bureau and is still owned by the Bureau
     * (NUMBER_REQUESTED > 0, NEW_REQUEST = 'N' and OWNER = '400')
     * or a pool that has been created by the Bureau containing actively summonsed Jurors
     * (NUMBER_REQUESTED > 0, NEW_REQUEST = 'N' and Active Juror Count > 0)
     * or a pool that has been created by the Court and either contains actively summonsed Jurors
     * or has a future start date
     * NUMBER_REQUESTED IS NULL (created via transfer journey) or NUMBER_REQUESTED > 0,
     * Active Juror Count > 0 or RETURN_DATE >= Today's date
     * and NEW_REQUEST = 'N'
     * <p/>
     * Completed - Either a Nil Pool with a past start date
     * (NIL_POLL = TRUE, NUMBER_REQUESTED = 0, NEW_REQUEST = 'N' and RETURN_DATE < Today's date)
     * or a pool that has been created by the Bureau containing no active Juror's
     * (NEW_REQUEST = 'N' and Active Juror Count = 0)
     *
     * @param query      The current JPA query object to apply conditional expressions -
     *                   effectively emulating a builder pattern implementation
     * @param poolStatus Whether the Pool is in Requested, Active or Completed status.
     *                   Status is a concept and is not represented as a static value in the database.
     */
    @Override
    public void addPoolStatusPredicate(JPAQuery<Tuple> query, Collection<PoolStatus> poolStatus) {

        if (poolStatus.containsAll(REQUESTED_AND_ACTIVE)) {
            query.having(isRequested()
                .or(isCreated().and(poolMembersRequested().and(isWithBureau())))
                .or(isCreated().and(poolMembersRequested().and(hasActivePoolMembers())
                    .or((isNilPool().or(isStagingPool())).and(hasActiveReturnDate())))));

        } else if (poolStatus.containsAll(REQUESTED_AND_COMPLETED)) {
            query.where(isRequested().or(isCreated().and(hasElapsedReturnDate())));
            query.having(hasNoActivePoolMembers());

        } else if (poolStatus.containsAll(COMPLETED_AND_ACTIVE)) {
            query.where(isCreated());

        } else if (poolStatus.contains(PoolStatus.REQUESTED)) {
            query.where(isRequested());

        } else if (poolStatus.contains(PoolStatus.ACTIVE)) {
            query.where((isCreated().and(poolMembersRequested())
                .or(isNilPool().and(hasActiveReturnDate()))
                .or(isStagingPool())));
            query.having(isNilPool().or(hasActivePoolMembers().or(isWithBureau())
                .or(isStagingPool().and(hasActivePoolMembers().or(hasActiveReturnDate())))));

        } else if (poolStatus.contains(PoolStatus.COMPLETED)) {
            query.where(isCreated().and(hasElapsedReturnDate()));
            query.having(hasNoActivePoolMembers());
        }
    }

    /**
     * Pools can either be with the Court or the Bureau - a predicate should only be applied if pool stage option is
     * selected. If both stages (court and bureau) are selected then the result set will be the same as if neither
     * were selected, so no predicate is required.
     *
     * @param query     The current JPA query object to apply conditional expressions -
     *                  effectively emulating a builder pattern implementation
     * @param poolStage Indicates whether the Pool is currently owned by the Bureau or the Court
     */
    @Override
    public void addPoolStagePredicate(JPAQuery<Tuple> query, List<PoolSearchRequestDto.PoolStage> poolStage) {
        if (poolStage.size() == 1) {
            if (poolStage.get(0).equals(PoolSearchRequestDto.PoolStage.BUREAU)) {
                query.where(POOL_REQUEST.owner.eq(JurorDigitalApplication.JUROR_OWNER));
            } else {
                query.where(POOL_REQUEST.owner.ne(JurorDigitalApplication.JUROR_OWNER));
            }
        }
    }

    @Override
    public void addPoolTypePredicate(JPAQuery<Tuple> query, List<String> poolType) {
        query.where(POOL_REQUEST.poolType.poolType.in(poolType));
    }

    @Override
    public void orderByStringColumn(JPAQuery<Tuple> query, StringPath simpleColumn, SortDirection sortDirection) {
        if (Objects.requireNonNull(sortDirection) == SortDirection.DESC) {
            query.orderBy(simpleColumn.desc());
        } else {
            query.orderBy(simpleColumn.asc());
        }
    }

    @Override
    public void orderByDateColumn(JPAQuery<Tuple> query, DatePath<LocalDate> simpleColumn,
                                  SortDirection sortDirection) {
        if (Objects.requireNonNull(sortDirection) == SortDirection.DESC) {
            query.orderBy(simpleColumn.desc());
        } else {
            query.orderBy(simpleColumn.asc());
        }
    }

    @Override
    public void orderByPoolStage(JPAQuery<Tuple> query, SortDirection sortDirection) {
        NumberExpression<Integer> poolStage = new CaseBuilder()
            .when(POOL_REQUEST.owner.eq(JurorDigitalApplication.JUROR_OWNER))
            .then(1)
            .otherwise(2);

        orderByNumberExpression(query, poolStage, sortDirection);
    }

    @Override
    public void orderByPoolStatus(JPAQuery<Tuple> query, SortDirection sortDirection) {
        NumberExpression<Integer> poolStatus = new CaseBuilder()
            .when(POOL_REQUEST.newRequest.ne('N'))
            .then(1)
            .when(POOL_REQUEST.nilPool.eq(true))
            .then(2)
            .when(POOL_REQUEST.owner.eq(JurorDigitalApplication.JUROR_OWNER))
            .then(3)
            .when(countActivePoolMembers().gt(0))
            .then(3)
            .when(POOL_REQUEST.nilPool.eq(false).and(POOL_REQUEST.returnDate.gt(LocalDate.now())))
            .then(3)
            .otherwise(4);

        orderByNumberExpression(query, poolStatus, sortDirection);
    }

    private static NumberExpression<Integer> countActivePoolMembers() {
        return new CaseBuilder()
            .when(Expressions.asBoolean(JUROR_POOL.status.status.in(3, 4)))
            .then(1)
            .when(JUROR_POOL.status.status.eq(2))
            .then(1)
            .otherwise(0).sum();
    }

    private static void orderByNumberExpression(JPAQuery<Tuple> query, NumberExpression<Integer> expression,
                                                SortDirection sortDirection) {
        if (Objects.requireNonNull(sortDirection) == SortDirection.DESC) {
            query.orderBy(expression.desc());
        } else {
            query.orderBy(expression.asc());
        }
    }

    private static BooleanExpression hasActivePoolMembers() {
        return countActivePoolMembers().gt(0);
    }

    private static BooleanExpression hasNoActivePoolMembers() {
        return countActivePoolMembers().eq(0);
    }

    private static BooleanExpression isRequested() {
        return POOL_REQUEST.newRequest.ne('N');
    }

    private static BooleanExpression isCreated() {
        return POOL_REQUEST.newRequest.eq('N');
    }

    private static BooleanExpression isWithBureau() {
        return POOL_REQUEST.owner.eq(JurorDigitalApplication.JUROR_OWNER);
    }

    private static BooleanExpression hasActiveReturnDate() {
        return POOL_REQUEST.returnDate.goe(LocalDate.now());
    }

    private static BooleanExpression hasElapsedReturnDate() {
        return POOL_REQUEST.returnDate.before(LocalDate.now());
    }

    private static BooleanExpression isNilPool() {
        return POOL_REQUEST.nilPool.eq(true).and(POOL_REQUEST.numberRequested.eq(0));
    }

    private static BooleanExpression isStagingPool() {
        return POOL_REQUEST.numberRequested.isNull();
    }

    private static BooleanExpression poolMembersRequested() {
        return POOL_REQUEST.numberRequested.gt(0);
    }

}
