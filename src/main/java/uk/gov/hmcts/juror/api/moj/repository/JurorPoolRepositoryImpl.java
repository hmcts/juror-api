package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.Column;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.juror.api.juror.domain.CourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.JurorPoolSearch;
import uk.gov.hmcts.juror.api.moj.controller.request.PoolMemberFilterRequestQuery;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.PaginatedList;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.domain.trial.QPanel;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.trial.PanelResult;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.utils.DataUtils;
import uk.gov.hmcts.juror.api.moj.utils.NumberUtils;
import uk.gov.hmcts.juror.api.moj.utils.PaginationUtil;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;


/**
 * Custom Repository implementation for the JurorPool entity.
 */
public class JurorPoolRepositoryImpl implements IJurorPoolRepository {

    @PersistenceContext
    EntityManager entityManager;

    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QJuror JUROR = QJuror.juror;
    private static final QPoolRequest POOL_REQUEST = QPoolRequest.poolRequest;
    private static final QAppearance APPEARANCE = QAppearance.appearance;
    private static final QPanel PANEL = QPanel.panel;

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
            .leftJoin(APPEARANCE).on(JUROR_POOL.juror.jurorNumber.eq(APPEARANCE.jurorNumber).and(
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
            .leftJoin(APPEARANCE).on(JUROR_POOL.juror.jurorNumber.eq(APPEARANCE.jurorNumber).and(
                JUROR_POOL.pool.poolNumber.eq(APPEARANCE.poolNumber)))
            .where(JUROR_POOL.status.status.eq(IJurorStatus.RESPONDED))
            .where(JUROR_POOL.isActive.eq(true))
            .where(JUROR_POOL.pool.poolNumber.in(poolNumbers))
            .where(JUROR_POOL.juror.jurorNumber.in(jurorNumbers))
            .where(POOL_REQUEST.courtLocation.locCode.eq(locCode))
            .fetch();

    }

    @Override
    public <T> PaginatedList<T> findJurorPoolsBySearch(JurorPoolSearch search, String owner,
                                                       Consumer<JPQLQuery<JurorPool>> queryModifiers,
                                                       Function<JurorPool, T> dataMapper,
                                                       Long maxItems) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        JPQLQuery<JurorPool> query = queryFactory
            .selectFrom(JUROR_POOL)
            .where(JUROR_POOL.owner.eq(owner));

        if (queryModifiers != null) {
            queryModifiers.accept(query);
        }

        if (search.getJurorName() != null) {
            query.where(JUROR.firstName.concat(" ").concat(JUROR.lastName)
                .likeIgnoreCase("%" + search.getJurorName() + "%"));
        }

        if (search.getJurorStatus() != null) {
            query.where(JUROR_POOL.status.status.eq(search.getJurorStatus()));
        }

        if (search.getJurorNumber() != null) {
            query.where(JUROR.jurorNumber.startsWith(search.getJurorNumber()));
        }

        if (search.getPostcode() != null) {
            query.where(JUROR.postcode.eq(DataUtils.toUppercase(search.getPostcode())));
        }

        if (search.getPoolNumber() != null) {
            query.where(JUROR_POOL.pool.poolNumber.startsWith(search.getPoolNumber()));
        }

        return PaginationUtil.toPaginatedList(query, search, JurorPoolSearch.SortField.JUROR_NUMBER, SortMethod.ASC,
            dataMapper, maxItems);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> fetchThinPoolMembers(String poolNumber, String owner) {
        JPAQueryFactory queryFactory = getQueryFactory();

        List<?> results = queryFactory.select(POOL_REQUEST.owner).from(POOL_REQUEST)
            .where(POOL_REQUEST.poolNumber.eq(poolNumber))
            .fetch();

        if (results.isEmpty()) {
            throw new MojException.NotFound("Pool number not found", null);
        }

        if (SecurityUtil.BUREAU_OWNER.equals(owner) || results.contains(SecurityUtil.BUREAU_OWNER)
            || results.contains(owner)) {

            return queryFactory.select(JUROR_POOL.juror.jurorNumber)
                .from(JUROR_POOL)
                .where(JUROR_POOL.pool.poolNumber.eq(poolNumber)
                    .and(JUROR_POOL.owner.eq(owner))
                    .and(JUROR_POOL.isActive.isTrue()))
                .fetch();
        }

        throw new MojException.Forbidden("You do not have access to this pool", null);
    }

    @Override
    public boolean hasPoolWithLocCode(String jurorNumber, List<String> locCode) {
        JPAQueryFactory queryFactory = getQueryFactory();
        return queryFactory.from(JUROR_POOL)
            .where(JUROR_POOL.juror.jurorNumber.eq(jurorNumber))
            .where(JUROR_POOL.pool.courtLocation.locCode.in(locCode))
            .fetchFirst() != null;
    }

    @Override
    @Transactional(readOnly = true)
    public JPAQuery<Tuple> fetchFilteredPoolMembers(PoolMemberFilterRequestQuery search, String owner) {
        JPAQueryFactory queryFactory = getQueryFactory();
        LocalDate today = LocalDate.now();
        JPAQuery<?> partialQuery = queryFactory.from(JUROR_POOL)
            .join(JUROR).on(JUROR_POOL.juror.eq(JUROR))
            .leftJoin(APPEARANCE)
            .on(JUROR.jurorNumber.eq(APPEARANCE.jurorNumber)
                .and(APPEARANCE.attendanceDate.eq(today)))
            .leftJoin(QJurorStatus.jurorStatus)
            .on(JUROR_POOL.status.status.eq(QJurorStatus.jurorStatus.status))
            .leftJoin(PANEL)
            .on(JUROR.eq(PANEL.juror))
            .where(JUROR_POOL.isActive.isTrue())
            .where(JUROR_POOL.pool.poolNumber.eq(search.getPoolNumber()))
            .where(JUROR_POOL.owner.eq(owner));

        if (null != search.getJurorNumber()) {
            partialQuery.where(JUROR.jurorNumber.like(search.getJurorNumber() + "%"));
        }
        if (null != search.getFirstName()) {
            partialQuery.where(JUROR.firstName.startsWithIgnoreCase(search.getFirstName()));
        }
        if (null != search.getLastName()) {
            partialQuery.where(JUROR.lastName.startsWithIgnoreCase(search.getLastName()));
        }
        if (null != search.getCheckedIn() && search.getCheckedIn()) {
            partialQuery.where(getCheckedInBoolean());
        }
        if (null != search.getNextDue()) {
            if (search.getNextDue().size() != 2 && "set".equals(search.getNextDue().get(0))) {
                partialQuery.where(JUROR_POOL.nextDate.isNotNull());
            } else if (search.getNextDue().size() != 2 && "notSet".equals(search.getNextDue().get(0))) {
                partialQuery.where(JUROR_POOL.nextDate.isNull());
            }
        }
        if (null != search.getStatuses()) {
            partialQuery.where(QJurorStatus.jurorStatus.statusDesc.in(search.getStatuses()));
        }
        if (null != search.getAttendance()) {
            partialQuery.where(
                (search.getAttendance().contains(PoolMemberFilterRequestQuery.AttendanceEnum.ON_CALL)
                    ? JUROR_POOL.onCall.eq(true)
                    : Expressions.FALSE)
                    .or(
                        search.getAttendance().contains(PoolMemberFilterRequestQuery.AttendanceEnum.ON_A_TRIAL)
                            ? PANEL.result.eq(PanelResult.JUROR)
                            : Expressions.FALSE)
                    .or(search.getAttendance().contains(PoolMemberFilterRequestQuery.AttendanceEnum.IN_ATTENDANCE)
                        ? Expressions.booleanOperation(
                        Ops.AND,
                        APPEARANCE.appearanceStage.eq(AppearanceStage.CHECKED_IN),
                        APPEARANCE.attendanceDate.eq(LocalDate.now()))
                        : Expressions.FALSE)
                    .or(search.getAttendance().contains(PoolMemberFilterRequestQuery.AttendanceEnum.OTHER)
                        ? JUROR_POOL.nextDate.eq(LocalDate.now())
                        .and(JUROR_POOL.onCall.ne(true))
                        .and(PANEL.result.ne(PanelResult.JUROR))
                        .and(APPEARANCE.appearanceStage.ne(AppearanceStage.CHECKED_IN))
                        : Expressions.FALSE
                    ));
        }

        return partialQuery.distinct().select(
                JUROR_POOL.juror.jurorNumber,
                JUROR_POOL.juror.firstName,
                JUROR_POOL.juror.lastName,
                JUROR_POOL.juror.postcode,
                getAttendanceCase(),
                getCheckedInBoolean().as(CHECKED_IN_TODAY),
                APPEARANCE.timeIn,
                JUROR_POOL.nextDate,
                QJurorStatus.jurorStatus.statusDesc)
            .groupBy(JUROR_POOL.juror.jurorNumber,
                JUROR_POOL.juror.firstName,
                JUROR_POOL.juror.lastName,
                JUROR_POOL.juror.postcode,
                CHECKED_IN_TODAY,
                APPEARANCE.timeIn,
                JUROR_POOL.nextDate,
                QJurorStatus.jurorStatus.statusDesc);
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

    JPAQueryFactory getQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }

    StringExpression getAttendanceCase() {
        return new CaseBuilder()
            .when(Expressions.asBoolean(JUROR_POOL.onCall.eq(true)))
            .then(PoolMemberFilterRequestQuery.AttendanceEnum.ON_CALL.getKeyString())
            .when(PANEL.result.eq(PanelResult.JUROR))
            .then(PoolMemberFilterRequestQuery.AttendanceEnum.ON_A_TRIAL.getKeyString())
            .when(getCheckedInBoolean())
            .then(PoolMemberFilterRequestQuery.AttendanceEnum.IN_ATTENDANCE.getKeyString())
            .when(JUROR_POOL.nextDate.eq(LocalDate.now()))
            .then(PoolMemberFilterRequestQuery.AttendanceEnum.OTHER.getKeyString())
            .otherwise("").max()
            .as(ATTENDANCE);
    }

    BooleanExpression getCheckedInBoolean() {
        return Expressions.booleanOperation(
            Ops.AND,
            APPEARANCE.appearanceStage.eq(AppearanceStage.CHECKED_IN),
            APPEARANCE.attendanceDate.eq(LocalDate.now())
        );
    }

    @Override
    public int getCountJurorsDueToAttendCourt(String locCode, LocalDate startDate, LocalDate endDate,
                                              boolean reasonableAdjustmentRequired) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        JPAQuery<Long> partialQuery = queryFactory
            .select(JUROR_POOL.count())
            .from(JUROR_POOL)
            .where(JUROR_POOL.pool.courtLocation.locCode.eq(locCode))
            .where(JUROR_POOL.status.status.in(IJurorStatus.RESPONDED, IJurorStatus.PANEL, IJurorStatus.JUROR))
            .where(JUROR_POOL.isActive.isTrue())
            .where(JUROR_POOL.nextDate.between(startDate, endDate))
            .where(JUROR_POOL.owner.eq(SecurityUtil.getActiveOwner()));

        if (reasonableAdjustmentRequired) {
            partialQuery.where(JUROR_POOL.juror.reasonableAdjustmentCode.isNotNull());
        }

        Long count = partialQuery.fetchOne();

        if (count == null) {
            return 0;
        } else {
            return count.intValue();
        }
    }


    public static List<YieldPerformanceData> getYieldPerformanceData(JurorPoolRepository jurorPoolRepository,
                                                                     String courtLocCodes, LocalDate fromDate,
                                                                     LocalDate toDate) {
        try {
            return jurorPoolRepository.getYieldPerformanceReportStats(courtLocCodes,
                fromDate, toDate);
        } catch (Exception e) {
            throw new MojException.InternalServerError("Error getting yield performance report by court", e);
        }
    }


    public interface YieldPerformanceData {
        @Column(name = "loc_code")
        String getLocCode();

        @Column(name = "court_name")
        String getCourtName();

        @Column(name = "no_requested")
        int getNoRequested();

        @Column(name = "supplied")
        int getSupplied();


        default String getCourt() {
            return getCourtName();
        }

        default int getBalance() {
            return getConfirmed() - getNoRequested();
        }

        default int getConfirmed() {
            return this.getSupplied();
        }

        default double getDifference() {
            return NumberUtils.calculatePercentage(getBalance(), getNoRequested());
        }

        default int getRequested() {
            return getNoRequested();
        }
    }
}
