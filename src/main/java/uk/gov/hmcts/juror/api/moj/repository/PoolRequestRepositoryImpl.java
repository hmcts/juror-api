package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.constraints.NotNull;
import uk.gov.hmcts.juror.api.JurorDigitalApplication;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolRequestedFilterQuery;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestDataDto;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolComment;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.utils.PaginationUtil;
import uk.gov.hmcts.juror.api.moj.utils.PoolRequestUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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


    /**
     * Retrieve database records from the JUROR_DIGITAL_USER.POOL_REQUEST view using a LIKE expression to filter
     * results, returning only those where the pool number start with a defined prefix
     *
     * @param poolNumberPrefix The first 7 characters of a Pool Number containing the Court Location Code,
     *                         Attendance Date Year (YY) and Attendance Date Month (MM)
     * @return the first Pool Request to match the provided Pool Number prefix, ordered by Pool Number (descending)
     *          therefor the first record returned will have the current highest sequence number
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
     *          <ol>
     *              <li>Pool Number</li>
     *              <li>Service Start Date</li>
     *              <li>Number of Pool Members requested for the Bureau to supply</li>
     *              <li>Number of Active Pool Members in a responded state</li>
     *          </ol>
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

    @Override
    public PaginatedList<PoolRequestDataDto> getPoolRequestList(PoolRequestedFilterQuery filterQuery) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        JPAQuery<PoolRequest> query = queryFactory.selectFrom(POOL_REQUEST)
            .where(filterByPoolTypeAndLocation(PoolRequestUtils.POOL_TYPES_DESC_LIST, filterQuery.getLocCode()))
            .where(POOL_REQUEST.newRequest.ne('N'));

        if (SecurityUtil.isBureau()) {
            query.where(POOL_REQUEST.owner.eq(JurorDigitalApplication.JUROR_OWNER));
        } else if (SecurityUtil.isCourt()) {
            query.where(filterByCourtLocations(SecurityUtil.getCourts()));
        }
        return PaginationUtil.toPaginatedList(
            query,
            filterQuery,
            PoolRequestedFilterQuery.SortField.POOL_NUMBER,
            SortMethod.ASC,
            poolRequest -> PoolRequestDataDto.builder()
                .numberRequested(poolRequest.getNumberRequested())
                .attendanceDate(poolRequest.getReturnDate())
                .courtName(poolRequest.getCourtLocation().getName())
                .poolNumber(poolRequest.getPoolNumber())
                .poolType(poolRequest.getPoolType().getPoolType())
                .build()
        );
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
