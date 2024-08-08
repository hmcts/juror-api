package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.constraints.NotNull;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequestListAndCount;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequestStatus;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolComment;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.hmcts.juror.api.moj.domain.PoolRequestQueries.filterByActiveFlag;
import static uk.gov.hmcts.juror.api.moj.domain.PoolRequestQueries.filterByCourtLocations;
import static uk.gov.hmcts.juror.api.moj.domain.PoolRequestQueries.filterByPoolTypeAndLocation;

/**
 * Custom Repository implementation for the PoolRequest entity.
 */
public class PoolRequestRepositoryImpl extends PoolRequestSearchQueries implements IPoolRequestRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QPoolRequest POOL_REQUEST = QPoolRequest.poolRequest;
    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;

    /**
     * Retrieve database records from the JUROR_DIGITAL_USER.POOL_REQUEST view using predicates to filter the results
     * Only return records owned by the Bureau (owner = '400')
     *
     * @param poolTypes     List of court type description that the pool has been summoned for
     * @param courtLocation Unique 3 digit code to identify a specific court location
     * @return a list of Pool Requests which are owned by the Bureau and filtered further based on the supplied criteria
     */
    @Override
    public PoolRequestListAndCount findBureauPoolRequestsList(@NotNull List<String> poolTypes, String courtLocation,
                                                              int offset, int pageSize, OrderSpecifier<?> order) {

        JPAQuery<PoolRequest> query = findFilteredPoolRequests(poolTypes, courtLocation)
            .where(POOL_REQUEST.owner.eq(JurorDigitalApplication.JUROR_OWNER))
            .offset((long) offset * pageSize)
            .limit(pageSize)
            .orderBy(order);

        QueryResults<PoolRequest> queryResults = query.fetchResults();
        long poolRequestCount = queryResults.getTotal();
        List<PoolRequest> poolRequestList = queryResults.getResults();

        return new PoolRequestListAndCount(poolRequestList, poolRequestCount);
    }

    @Override
    public PoolRequestListAndCount findCourtsPoolRequestsList(List<String> courts, List<String> poolTypes,
                                                              String courtLocation, int offset, int pageSize,
                                                              OrderSpecifier<?> order) {
        JPAQuery<PoolRequest> query = findFilteredPoolRequests(poolTypes, courtLocation)
            .where(filterByCourtLocations(courts))
            .offset((long) offset * pageSize)
            .limit(pageSize)
            .orderBy(order);

        QueryResults<PoolRequest> queryResults = query.fetchResults();
        long poolRequestCount = queryResults.getTotal();
        List<PoolRequest> poolRequestList = queryResults.getResults();

        return new PoolRequestListAndCount(poolRequestList, poolRequestCount);
    }

    /**
     * Retrieves a list of distinct pools for a given court, year and month from JUROR_DIGITAL_USER.POOL_REQUEST view
     *
     * @param poolNumberPrefix the first 7 characters of a pool number containing the court location code,
     *                         attendance date year (yy) and attendance date month (mm)
     * @return a list of pools (tuples: pool number and attendance date)
     */
    @Override
    public List<Tuple> findAllPoolNumbersByPoolNumberPrefix(@NotNull String poolNumberPrefix) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFactory.from(POOL_REQUEST)
            .where(POOL_REQUEST.poolNumber.startsWith(poolNumberPrefix))
            .distinct()
            .select(POOL_REQUEST.poolNumber, POOL_REQUEST.returnDate)
            .fetch();
    }

    private JPAQuery<PoolRequest> findFilteredPoolRequests(List<String> poolTypes, String courtLocation) {
        QPoolRequest pr = new QPoolRequest("pr");
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFactory.selectFrom(POOL_REQUEST)
            .where(filterByPoolTypeAndLocation(poolTypes, courtLocation)
                .and(filterByActiveFlag(POOL_REQUEST, pr, PoolRequestStatus.REQUESTED)));
    }

    /**
     * Retrieve database records from the JUROR_DIGITAL_USER.POOL_REQUEST view using a LIKE expression to filter
     * results, returning only those where the pool number start with a defined prefix
     *
     * @param poolNumberPrefix The first 7 characters of a Pool Number containing the Court Location Code,
     *                         Attendance Date Year (YY) and Attendance Date Month (MM)
     * @return the first Pool Request to match the provided Pool Number prefix, ordered by Pool Number (descending)
     *     therefor the first record returned will have the current highest sequence number
     */
    @Override
    public PoolRequest findLatestPoolRequestByPoolNumberPrefix(@NotNull String poolNumberPrefix) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFactory.selectFrom(POOL_REQUEST)
            .where(POOL_REQUEST.poolNumber.startsWith(poolNumberPrefix))
            .orderBy(POOL_REQUEST.poolNumber.desc())
            .fetchFirst();
    }


    /**
     * Query the database to find an active pool with the pool number provided.
     *
     * @param poolNumber 9-digit number that identifies a unique pool
     * @return a boolean value
     */
    @Override
    public boolean isActive(String poolNumber) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFactory.selectFrom(POOL_REQUEST)
            .where(POOL_REQUEST.poolNumber.eq(poolNumber))
            .where(POOL_REQUEST.newRequest.eq('N'))
            .stream().findAny().isPresent();
    }

    /**
     * Deletes the pool identified by the poolNumber passed in.
     *
     * @param poolNumber 9-digit number that identifies a unique pool
     */
    @Override
    public void deletePoolRequestByPoolNumber(@NotNull String poolNumber) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        queryFactory.delete(QPoolComment.poolComment)
            .where(QPoolComment.poolComment.pool.poolNumber.eq(poolNumber))
            .execute();

        queryFactory.delete(POOL_REQUEST)
            .where(POOL_REQUEST.poolNumber.eq(poolNumber))
            .execute();
    }

    /**
     * Check for active pools (NEW_REQUEST = 'Y' and READ_ONLY = 'N') excluding nil pools (NO_REQUESTED <> 0) for a
     * given court location (based on POOL_REQUEST.LOC_CODE) and, optionally between a given date range
     * where POOR_REQUEST.RETURN_DATE >= minDate (if provided) and POOR_REQUEST.RETURN_DATE >= maxDate (if provided)
     * <p/>
     * Include summons statistics for the pool request by counting the number of active pool members in a summoned or
     * responded state
     *
     * @param owner   3-digit numeric string to restrict current user's read permission
     * @param locCode 3-digit numeric string to uniquely identify a court location
     * @param minDate start date (inclusive) for date range predicate
     * @param maxDate end date (inclusive) for date range predicate
     * @return a Tuple4 containing:
     *     <ol>
     *         <li>Pool Number</li>
     *         <li>Service Start Date</li>
     *         <li>Number of Pool Members requested for the Bureau to supply</li>
     *         <li>Number of Active Pool Members in a responded state</li>
     *     </ol>
     */
    @Override
    public List<Tuple> findActivePoolsForDateRange(String owner, String locCode, LocalDate minDate, LocalDate maxDate,
                                                   boolean isReassign) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        JPAQuery<Tuple> query = queryFactory.select(
                POOL_REQUEST.poolNumber.as("POOL_NO"),
                POOL_REQUEST.returnDate.as("SERVICE_START_DATE"),
                POOL_REQUEST.numberRequested.as("NUMBER_REQUESTED"),
                countRespondedPoolMembers(owner).as("ACTIVE_POOL_MEMBERS")
            )
            .from(POOL_REQUEST)
            .leftJoin(JUROR_POOL)
            .on(POOL_REQUEST.poolNumber.eq(JUROR_POOL.pool.poolNumber))
            .where(POOL_REQUEST.nilPool.isFalse())
            .where(POOL_REQUEST.numberRequested.isNull().or(POOL_REQUEST.numberRequested.ne(0)))
            .where(POOL_REQUEST.newRequest.eq('N'))
            .where(POOL_REQUEST.courtLocation.locCode.eq(locCode))
            .groupBy(POOL_REQUEST.poolNumber)
            .groupBy(POOL_REQUEST.returnDate)
            .groupBy(POOL_REQUEST.numberRequested);

        if (owner.equalsIgnoreCase(JurorDigitalApplication.JUROR_OWNER) || isReassign) {
            query.where(POOL_REQUEST.owner.eq(owner));
        }

        if (minDate != null) {
            query.where(POOL_REQUEST.returnDate.goe(minDate));
        }

        if (maxDate != null) {
            query.where(POOL_REQUEST.returnDate.loe(maxDate));
        }

        return query.fetch();
    }

    @Override
    public List<Tuple> findActivePoolsForDateRangeWithCourtCreatedRestriction(String owner, String locCode,
                                                                              LocalDate minDate,
                                                                              LocalDateTime courtCreationMinDate) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFactory.select(
                POOL_REQUEST.poolNumber.as("POOL_NO"),
                POOL_REQUEST.returnDate.as("SERVICE_START_DATE"),
                POOL_REQUEST.numberRequested.as("NUMBER_REQUESTED"),
                countRespondedPoolMembers(owner).as("ACTIVE_POOL_MEMBERS")
            )
            .from(POOL_REQUEST)
            .leftJoin(JUROR_POOL)
            .on(POOL_REQUEST.poolNumber.eq(JUROR_POOL.pool.poolNumber))
            .where(POOL_REQUEST.nilPool.isFalse())
            .where(
                POOL_REQUEST.numberRequested.ne(0)
                    .and(POOL_REQUEST.returnDate.goe(minDate))
                    .or(POOL_REQUEST.numberRequested.isNull()
                        .and(POOL_REQUEST.dateCreated.goe(courtCreationMinDate))
                    ))
            .where(POOL_REQUEST.newRequest.eq('N'))
            .where(POOL_REQUEST.courtLocation.locCode.eq(locCode))
            .where(POOL_REQUEST.owner.eq(owner))
            .groupBy(POOL_REQUEST.poolNumber)
            .groupBy(POOL_REQUEST.returnDate)
            .groupBy(POOL_REQUEST.numberRequested).fetch();
    }

    /**
     * Count the number of Pool Members who have been responded positively to their summons for a given pool request.
     *
     * @return a number representing the count of jurors meeting the status criteria
     */
    private static NumberExpression<Integer> countRespondedPoolMembers(String owner) {
        return new CaseBuilder()
            .when(Expressions.asBoolean(JUROR_POOL.status.status.eq(IJurorStatus.RESPONDED)
                .and(JUROR_POOL.owner.eq(owner)))).then(1)
            .otherwise(0).sum();
    }

}
