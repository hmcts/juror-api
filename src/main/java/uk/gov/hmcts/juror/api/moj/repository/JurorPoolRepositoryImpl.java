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
import uk.gov.hmcts.juror.api.moj.domain.QJurorTrial;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.SortMethod;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.exception.MojException;
import uk.gov.hmcts.juror.api.moj.utils.PaginationUtil;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

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

        if (search.getJurorNumber() != null) {
            query.where(JUROR.jurorNumber.startsWith(search.getJurorNumber()));
        }

        if (search.getPostcode() != null) {
            query.where(JUROR.postcode.eq(search.getPostcode()));
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

        List<?> results = queryFactory.select(QPoolRequest.poolRequest.owner).from(QPoolRequest.poolRequest)
            .where(QPoolRequest.poolRequest.poolNumber.eq(poolNumber))
            .fetch();

        if (results.isEmpty()) {
            throw new MojException.NotFound("Pool number not found", null);
        }

        if (SecurityUtil.BUREAU_OWNER.equals(owner) || results.contains(SecurityUtil.BUREAU_OWNER)
            || results.contains(owner)) {

            return queryFactory.select(QJurorPool.jurorPool.juror.jurorNumber)
                .from(QJurorPool.jurorPool)
                .where(QJurorPool.jurorPool.pool.poolNumber.eq(poolNumber)
                           .and(QJurorPool.jurorPool.owner.eq(owner))
                           .and(QJurorPool.jurorPool.isActive.isTrue()))
                .fetch();
        }

        throw new MojException.Forbidden("You do not have access to this pool", null);
    }

    @Override
    @Transactional(readOnly = true)
    public JPAQuery<Tuple> fetchFilteredPoolMembers(PoolMemberFilterRequestQuery search, String owner) {
        JPAQueryFactory queryFactory = getQueryFactory();
        LocalDate today = LocalDate.now();
        JPAQuery<?> partialQuery = queryFactory.from(QJurorPool.jurorPool)
            .join(QJuror.juror).on(QJurorPool.jurorPool.juror.jurorNumber.eq(QJuror.juror.jurorNumber))
            .leftJoin(QAppearance.appearance)
            .on(QJurorPool.jurorPool.juror.jurorNumber.eq(QAppearance.appearance.jurorNumber)
                .and(QAppearance.appearance.attendanceDate.eq(today)))
            .leftJoin(QJurorStatus.jurorStatus)
            .on(QJurorPool.jurorPool.status.status.eq(QJurorStatus.jurorStatus.status))
            .leftJoin(QJurorTrial.jurorTrial)
            .on(QJurorPool.jurorPool.juror.jurorNumber.eq(QJurorTrial.jurorTrial.juror.jurorNumber))
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(QJurorPool.jurorPool.pool.poolNumber.eq(search.getPoolNumber()))
            .where(QJurorPool.jurorPool.owner.eq(owner));

        if (null != search.getJurorNumber()) {
            partialQuery.where(QJuror.juror.jurorNumber.like(search.getJurorNumber() + "%"));
        }
        if (null != search.getFirstName()) {
            partialQuery.where(QJuror.juror.firstName.like(search.getFirstName().toUpperCase() + "%"));
        }
        if (null != search.getLastName()) {
            partialQuery.where(QJuror.juror.lastName.like(search.getLastName().toUpperCase() + "%"));
        }
        if (null != search.getCheckedIn() && search.getCheckedIn()) {
            partialQuery.where(getCheckedInBoolean());
        }
        if (null != search.getNextDue()) {
            if (search.getNextDue().size() != 2 && "set".equals(search.getNextDue().get(0))) {
                partialQuery.where(QJurorPool.jurorPool.nextDate.isNotNull());
            } else if (search.getNextDue().size() != 2 && "notSet".equals(search.getNextDue().get(0))) {
                partialQuery.where(QJurorPool.jurorPool.nextDate.isNull());
            }
        }
        if (null != search.getStatuses()) {
            partialQuery.where(QJurorStatus.jurorStatus.statusDesc.in(search.getStatuses()));
        }
        if (null != search.getAttendance()) {
            partialQuery.where(
                (search.getAttendance().contains(PoolMemberFilterRequestQuery.AttendanceEnum.ON_CALL)
                    ? QJurorPool.jurorPool.onCall.eq(true)
                    : Expressions.FALSE)
                    .or(
                        search.getAttendance().contains(PoolMemberFilterRequestQuery.AttendanceEnum.ON_A_TRIAL)
                            ? QJurorTrial.jurorTrial.result.eq("J")
                            : Expressions.FALSE)
                    .or(search.getAttendance().contains(PoolMemberFilterRequestQuery.AttendanceEnum.IN_ATTENDANCE)
                        ? Expressions.booleanOperation(
                        Ops.AND,
                        QAppearance.appearance.appearanceStage.eq(AppearanceStage.CHECKED_IN),
                        QAppearance.appearance.attendanceDate.eq(LocalDate.now()))
                        : Expressions.FALSE)
                    .or(search.getAttendance().contains(PoolMemberFilterRequestQuery.AttendanceEnum.OTHER)
                        ? QJurorPool.jurorPool.nextDate.eq(LocalDate.now())
                        .and(QJurorPool.jurorPool.onCall.ne(true))
                        .and(QJurorTrial.jurorTrial.result.ne("J"))
                        .and(QAppearance.appearance.appearanceStage.ne(AppearanceStage.CHECKED_IN))
                        : Expressions.FALSE
                    ));
        }

        return partialQuery.select(
            QJurorPool.jurorPool.juror.jurorNumber,
            QJurorPool.jurorPool.juror.firstName,
            QJurorPool.jurorPool.juror.lastName,
            QJurorPool.jurorPool.juror.postcode,
            getAttendanceCase(),
            getCheckedInBoolean().as(CHECKED_IN_TODAY),
            QAppearance.appearance.timeIn,
            QJurorPool.jurorPool.nextDate,
            QJurorStatus.jurorStatus.statusDesc
        );
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
            .when(Expressions.asBoolean(QJurorPool.jurorPool.onCall.eq(true)))
            .then(PoolMemberFilterRequestQuery.AttendanceEnum.ON_CALL.getKeyString())
            .when(QJurorTrial.jurorTrial.result.eq("J"))
            .then(PoolMemberFilterRequestQuery.AttendanceEnum.ON_A_TRIAL.getKeyString())
            .when(getCheckedInBoolean())
            .then(PoolMemberFilterRequestQuery.AttendanceEnum.IN_ATTENDANCE.getKeyString())
            .when(QJurorPool.jurorPool.nextDate.eq(LocalDate.now()))
            .then(PoolMemberFilterRequestQuery.AttendanceEnum.OTHER.getKeyString())
            .otherwise("")
            .as(ATTENDANCE);
    }

    BooleanExpression getCheckedInBoolean() {
        return Expressions.booleanOperation(
            Ops.AND,
            QAppearance.appearance.appearanceStage.eq(AppearanceStage.CHECKED_IN),
            QAppearance.appearance.attendanceDate.eq(LocalDate.now())
        );
    }
}
