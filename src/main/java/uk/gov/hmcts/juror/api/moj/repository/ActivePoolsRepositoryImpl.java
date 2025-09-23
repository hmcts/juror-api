package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.juror.api.moj.controller.request.ActivePoolFilterQuery;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolRequestedFilterQuery;
import uk.gov.hmcts.juror.api.moj.controller.response.PoolRequestActiveDataDto;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.PoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.utils.PaginationUtil;
import uk.gov.hmcts.juror.api.moj.utils.PoolRequestUtils;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;


/**
 * Custom Repository implementation for the ActivePools at court entity.
 */
@Component
@Slf4j
public class ActivePoolsRepositoryImpl implements IActivePoolsRepository {

    private static final QPoolRequest POOL_REQUEST = QPoolRequest.poolRequest;
    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;

    private static final String BUREAU_TAB = "bureau";
    private static final String COURT_TAB = "court";
    private static final int ACTIVE_POOL_DAYS_LIMIT = 28;

    public static NumberExpression<Integer> CONFIRMED_FROM_BUREAU = new CaseBuilder()
        .when(JUROR_POOL.owner.eq(SecurityUtil.BUREAU_OWNER)
            .and(JUROR_POOL.status.status.eq(IJurorStatus.RESPONDED)))
        .then(1)
        .otherwise(0).sum();

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public PaginatedList<PoolRequestActiveDataDto> getActivePoolRequests(ActivePoolFilterQuery filterQuery) {
        if (filterQuery.getTab().equals(BUREAU_TAB)) {
            return getActiveBureauTabRequests(filterQuery);
        } else if (filterQuery.getTab().equals(COURT_TAB)) {
            return getActiveCourtTabRequests(filterQuery);
        } else {
            throw new MojException.BadRequest("Invalid tab type", null);
        }
    }

    @Override
    public PaginatedList<PoolRequestActiveDataDto> getActivePoolUnderResponded(ActivePoolFilterQuery filterQuery) {
        if (filterQuery.getTab().equals(BUREAU_TAB)) {
            return getActiveBureauTabUnderResponded(filterQuery);
        } else if (filterQuery.getTab().equals(COURT_TAB)) {
            return getActiveCourtTabRequests(filterQuery);
        } else {
            throw new MojException.BadRequest("Invalid tab type", null);
        }
    }


    private PaginatedList<PoolRequestActiveDataDto> getActiveBureauTabRequests(ActivePoolFilterQuery filterQuery) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        JPAQuery<Tuple> query = queryFactory.select(POOL_REQUEST, CONFIRMED_FROM_BUREAU)
            .from(POOL_REQUEST)
            .leftJoin(JUROR_POOL).on(POOL_REQUEST.eq(JUROR_POOL.pool))
            .where(POOL_REQUEST.owner.eq(SecurityUtil.BUREAU_OWNER))
            .where(POOL_REQUEST.newRequest.eq('N'))
            .where(POOL_REQUEST.numberRequested.ne(0))
            .where(POOL_REQUEST.poolType.description.in(PoolRequestUtils.POOL_TYPES_DESC_LIST))
            .groupBy(POOL_REQUEST, POOL_REQUEST.courtLocation.name);

        if (StringUtils.isNotBlank(filterQuery.getLocCode())) {
            query.where(POOL_REQUEST.courtLocation.locCode.eq(filterQuery.getLocCode()));
        }
        if (SecurityUtil.isCourt()) {
            query.where(POOL_REQUEST.courtLocation.locCode.in(SecurityUtil.getCourts()));
        }

        return PaginationUtil.toPaginatedList(
            query,
            filterQuery,
            PoolRequestedFilterQuery.SortField.POOL_NUMBER,
            SortMethod.ASC,
            data -> {
                PoolRequest poolRequest = Objects.requireNonNull(data.get(POOL_REQUEST));
                return PoolRequestActiveDataDto.builder()
                    .poolNumber(poolRequest.getPoolNumber())
                    .requestedFromBureau(poolRequest.getNumberRequested())
                    .confirmedFromBureau(data.get(CONFIRMED_FROM_BUREAU))
                    .courtName(poolRequest.getCourtLocation().getName())
                    .poolType(poolRequest.getPoolType().getDescription())
                    .attendanceDate(poolRequest.getReturnDate())
                    .build();
            }
        );
    }

    private PaginatedList<PoolRequestActiveDataDto>
        getActiveBureauTabUnderResponded(ActivePoolFilterQuery filterQuery) {

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        JPAQuery<Tuple> query = queryFactory.select(POOL_REQUEST, CONFIRMED_FROM_BUREAU)
            .from(POOL_REQUEST)
            .leftJoin(JUROR_POOL).on(POOL_REQUEST.eq(JUROR_POOL.pool))
            .where(POOL_REQUEST.owner.eq(SecurityUtil.BUREAU_OWNER))
            .where(POOL_REQUEST.newRequest.eq('N'))
            .where(POOL_REQUEST.numberRequested.ne(0))
            .where(POOL_REQUEST.poolType.description.in(PoolRequestUtils.POOL_TYPES_DESC_LIST))
            .where(POOL_REQUEST.returnDate.loe(LocalDate.now().plusDays(35)))
            .groupBy(POOL_REQUEST, POOL_REQUEST.courtLocation.name);

        if (StringUtils.isNotBlank(filterQuery.getLocCode())) {
            query.where(POOL_REQUEST.courtLocation.locCode.eq(filterQuery.getLocCode()));
        }
        if (SecurityUtil.isCourt()) {
            query.where(POOL_REQUEST.courtLocation.locCode.in(SecurityUtil.getCourts()));
        }

        // return PaginationUtil.toPaginatedList(
        PaginatedList<PoolRequestActiveDataDto> allResults = PaginationUtil.toPaginatedList(
            query,
            filterQuery,
            PoolRequestedFilterQuery.SortField.POOL_NUMBER,
            SortMethod.ASC,
            data -> {
                PoolRequest poolRequest = Objects.requireNonNull(data.get(POOL_REQUEST));
                return PoolRequestActiveDataDto.builder()
                    .poolNumber(poolRequest.getPoolNumber())
                    .requestedFromBureau(poolRequest.getNumberRequested())
                    .confirmedFromBureau(data.get(CONFIRMED_FROM_BUREAU))
                    .courtName(poolRequest.getCourtLocation().getName())
                    .poolType(poolRequest.getPoolType().getDescription())
                    .attendanceDate(poolRequest.getReturnDate())
                    .build();
            }
        );
        List<PoolRequestActiveDataDto> filtered = allResults.getData().stream()
            .filter(dto -> dto.getRequired() > 0)
            .filter(dto -> dto.getAttendanceDate() != null
                &&
                dto.getAttendanceDate().isBefore(LocalDate.now().plusDays(35)))
            .sorted(Comparator.comparing(PoolRequestActiveDataDto::getAttendanceDate))
            .toList();
        return new PaginatedList<>(
            allResults.getCurrentPage(),
            (long) filtered.size(),
            allResults.getTotalItems(),
            filtered
        );
    }




    private PaginatedList<PoolRequestActiveDataDto> getActiveCourtTabRequests(ActivePoolFilterQuery filterQuery) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        JPAQuery<Tuple> activePoolsQuery = queryFactory.select(
                POOL_REQUEST.poolNumber,
                POOL_REQUEST.totalNoRequired,
                JUROR_POOL.isActive.count(),
                POOL_REQUEST.courtLocation.name,
                POOL_REQUEST.poolType.description,
                POOL_REQUEST.returnDate
            )
            .from(POOL_REQUEST)
            .leftJoin(JUROR_POOL)
            .on(POOL_REQUEST.eq(JUROR_POOL.pool).and(POOL_REQUEST.owner.eq(JUROR_POOL.owner)))
            .where(POOL_REQUEST.owner.ne(SecurityUtil.BUREAU_OWNER))
            .where(POOL_REQUEST.newRequest.eq('N'))
            .where(POOL_REQUEST.poolType.description.in(PoolRequestUtils.POOL_TYPES_DESC_LIST))
            .where(POOL_REQUEST.nilPool.eq(false).and(POOL_REQUEST.returnDate.after(LocalDate.now()))
                .or(JUROR_POOL.isActive.isTrue()
                    .and(JUROR_POOL.status.status.in(
                        Arrays.asList(IJurorStatus.PANEL, IJurorStatus.JUROR, IJurorStatus.RESPONDED)))));

        if (SecurityUtil.isCourt()) {
            activePoolsQuery = activePoolsQuery.where(POOL_REQUEST.courtLocation.locCode.in(SecurityUtil.getCourts()));
        }
        if (StringUtils.isNotBlank(filterQuery.getLocCode())) {
            activePoolsQuery = activePoolsQuery.where(POOL_REQUEST.courtLocation.locCode.eq(filterQuery.getLocCode()));
        } else if (SecurityUtil.isBureau()) {
            LocalDate returnDateAfter = LocalDate.now().minusDays(ACTIVE_POOL_DAYS_LIMIT);
            activePoolsQuery.where(POOL_REQUEST.returnDate.after(returnDateAfter));
        }

        activePoolsQuery = activePoolsQuery
            .groupBy(POOL_REQUEST.poolNumber,
                POOL_REQUEST.totalNoRequired,
                POOL_REQUEST.courtLocation.name,
                POOL_REQUEST.poolType.description,
                POOL_REQUEST.returnDate);

        return PaginationUtil.toPaginatedList(
            activePoolsQuery,
            filterQuery,
            PoolRequestedFilterQuery.SortField.POOL_NUMBER,
            SortMethod.ASC,
            data -> PoolRequestActiveDataDto.builder()
                .poolNumber(data.get(POOL_REQUEST.poolNumber))
                .poolCapacity(data.get(POOL_REQUEST.totalNoRequired).intValue())
                .jurorsInPool(data.get(JUROR_POOL.isActive.count()).intValue())
                .courtName(data.get(POOL_REQUEST.courtLocation.name))
                .poolType(data.get(POOL_REQUEST.poolType.description))
                .attendanceDate(data.get(POOL_REQUEST.returnDate))
                .build()
        );
    }
}
