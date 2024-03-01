package uk.gov.hmcts.juror.api.moj.repository;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.moj.controller.request.jurormanagement.RetrieveAttendanceDetailsDto;
import uk.gov.hmcts.juror.api.moj.controller.response.JurorAppearanceResponseDto;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.JurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QAppearance;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.enumeration.AppearanceStage;
import uk.gov.hmcts.juror.api.moj.enumeration.jurormanagement.RetrieveAttendanceDetailsTag;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Custom Repository implementation for the Appearance entity.
 */
@Slf4j
@SuppressWarnings({"PMD.LawOfDemeter", "PMD.TooManyMethods"})
public class IAppearanceRepositoryImpl implements IAppearanceRepository {
    @PersistenceContext
    EntityManager entityManager;

    private static final QJurorPool JUROR_POOL = QJurorPool.jurorPool;
    private static final QJuror JUROR = QJuror.juror;
    private static final QAppearance APPEARANCE = QAppearance.appearance;

    private static final QCourtLocation COURT_LOCATION = QCourtLocation.courtLocation;

    @Override
    public List<JurorAppearanceResponseDto.JurorAppearanceResponseData> getAppearanceRecords(
        String locCode, LocalDate date, String jurorNumber) {

        List<Integer> jurorStatuses = Arrays.asList(IJurorStatus.RESPONDED, IJurorStatus.PANEL, IJurorStatus.JUROR);
        JPAQuery<Tuple> query = sqlFetchAppearanceRecords(locCode, date, jurorStatuses);

        // check if we need to just return one juror's record
        if (jurorNumber != null) {
            query = query.where(APPEARANCE.jurorNumber.eq(jurorNumber));
        }

        List<Tuple> tuples = sqlOrderQueryResults(query);

        return buildJurorAppearanceResponseData(tuples);
    }

    @Override
    public List<Tuple> retrieveAttendanceDetails(RetrieveAttendanceDetailsDto request) {
        final RetrieveAttendanceDetailsDto.CommonData commonData = request.getCommonData();

        // depending on what is to be updated, filter the query based on juror status
        List<Integer> jurorStatuses = sqlFilterQueryJurorStatus(commonData.getTag());

        // start building the query
        JPAQuery<Tuple> query = sqlFetchAppearanceRecords(commonData.getLocationCode(), commonData.getAttendanceDate(),
            jurorStatuses);

        if (commonData.getTag().equals(RetrieveAttendanceDetailsTag.JUROR_NUMBER)) {
            query = query.where(APPEARANCE.jurorNumber.in(request.getJuror()));
        } else if (commonData.getTag().equals(RetrieveAttendanceDetailsTag.NOT_CHECKED_OUT)
            || commonData.getTag().equals(RetrieveAttendanceDetailsTag.PANELLED)) {
            query = query.where(APPEARANCE.appearanceStage.eq(AppearanceStage.CHECKED_IN));
        } else if (commonData.getTag().equals(RetrieveAttendanceDetailsTag.CONFIRM_ATTENDANCE)) {
            // a confirmed juror can have appStage of CheckedIn or CheckedOut therefore cannot rely on appStage. A
            // confirmed juror will always have TimeIn.
            query = query.where(APPEARANCE.timeIn.isNotNull());
        }

        // execute the query and return the results
        return sqlOrderQueryResults(query);
    }

    @Override
    public List<Tuple> retrieveNonAttendanceDetails(RetrieveAttendanceDetailsDto.CommonData commonData) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.select(JUROR.jurorNumber.as("juror_number"),
                JUROR.firstName.as("first_name"),
                JUROR.lastName.as("last_name"),
                JUROR_POOL.status.status.as("status"))
            .from(JUROR)
            .join(JUROR_POOL)
            .on(JUROR.jurorNumber.eq(JUROR_POOL.juror.jurorNumber))
            .where(JUROR_POOL.owner.eq(commonData.getLocationCode()))
            .where(JUROR_POOL.nextDate.eq(commonData.getAttendanceDate()))
            .where(JUROR_POOL.status.status.eq(IJurorStatus.RESPONDED))
            .where(JUROR.jurorNumber.notIn(
                JPAExpressions.select(APPEARANCE.jurorNumber)
                    .from(APPEARANCE)
                    .where(APPEARANCE.courtLocation.locCode.eq(commonData.getLocationCode()))
                    .where(APPEARANCE.attendanceDate.eq(commonData.getAttendanceDate())))
            )
            .orderBy(JUROR.jurorNumber.asc()).fetch();
    }

    private List<Integer> sqlFilterQueryJurorStatus(@NotNull RetrieveAttendanceDetailsTag tag) {
        if (tag.equals(RetrieveAttendanceDetailsTag.CONFIRM_ATTENDANCE)) {
            return List.of(IJurorStatus.RESPONDED);
        } else if (tag.equals(RetrieveAttendanceDetailsTag.PANELLED)) {
            return List.of(IJurorStatus.PANEL);
        } else {
            return Arrays.asList(IJurorStatus.RESPONDED, IJurorStatus.PANEL, IJurorStatus.JUROR);
        }
    }

    private JPAQuery<Tuple> sqlFetchAppearanceRecords(String locCode, LocalDate date, List<Integer> jurorStatuses) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        return queryFactory.select(
                JUROR.jurorNumber.as("juror_number"),
                JUROR.firstName.as("first_name"),
                JUROR.lastName.as("last_name"),
                JUROR_POOL.status.status.as("status"),
                APPEARANCE.timeIn.as("time_in"),
                APPEARANCE.timeOut.as("time_out"),
                APPEARANCE.noShow.as("no_show"),
                APPEARANCE.appearanceStage.as("app_stage")
            )
            .from(JUROR)
            .join(JUROR_POOL)
            .on(JUROR.jurorNumber.eq(JUROR_POOL.juror.jurorNumber))
            .join(APPEARANCE)
            .on(JUROR.jurorNumber.eq(APPEARANCE.jurorNumber))
            .where(APPEARANCE.courtLocation.locCode.eq(locCode))
            .where(APPEARANCE.attendanceDate.eq(date))
            .where(JUROR_POOL.status.status.in(jurorStatuses));
    }

    private List<Tuple> sqlOrderQueryResults(JPAQuery<Tuple> query) {
        return query.orderBy(APPEARANCE.jurorNumber.asc()).fetch();
    }

    private List<JurorAppearanceResponseDto.JurorAppearanceResponseData> buildJurorAppearanceResponseData(
        List<Tuple> tuples) {

        List<JurorAppearanceResponseDto.JurorAppearanceResponseData> appearanceDataList = new ArrayList<>();

        for (Tuple tuple : tuples) {
            JurorAppearanceResponseDto.JurorAppearanceResponseData
                appearanceData = JurorAppearanceResponseDto.JurorAppearanceResponseData.builder()
                .jurorNumber(tuple.get(0, String.class))
                .firstName(tuple.get(1, String.class))
                .lastName(tuple.get(2, String.class))
                .jurorStatus(tuple.get(3, Integer.class))
                .checkInTime(tuple.get(4, LocalTime.class))
                .checkOutTime(tuple.get(5, LocalTime.class))
                .noShow(tuple.get(6, Boolean.class))
                .appStage(tuple.get(7, AppearanceStage.class))
                .build();
            appearanceDataList.add(appearanceData);
        }

        return appearanceDataList;
    }

    @Override
    public List<Tuple> getAvailableJurors(String locCode) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFactory.select(
                JUROR_POOL.pool.poolNumber,
                APPEARANCE.count(),
                JUROR_POOL.pool.returnDate,
                COURT_LOCATION.name,
                COURT_LOCATION.locCode
            )
            .from(APPEARANCE)
            .join(JUROR_POOL).on(JUROR_POOL.juror.jurorNumber.eq(APPEARANCE.jurorNumber))
            .join(COURT_LOCATION).on(COURT_LOCATION.locCode.eq(locCode))
            .where(JUROR_POOL.status.status.eq(IJurorStatus.RESPONDED))
            .where(APPEARANCE.timeIn.isNotNull())
            .groupBy(JUROR_POOL.pool.poolNumber)
            .groupBy(JUROR_POOL.pool.returnDate)
            .groupBy(COURT_LOCATION.locCode)
            .fetch();
    }

    @Override
    public List<JurorPool> retrieveAllJurors() {
        return buildJurorPoolQuery().fetch();
    }

    @Override
    public List<JurorPool> getJurorsInPools(List<String> poolNumbers) {
        JPAQuery<JurorPool> query = buildJurorPoolQuery();
        return query.where(JUROR_POOL.pool.poolNumber.in(poolNumbers)).fetch();
    }

    /**
     * Builds query for getting juror pool information using the appearance table.
     *
     * @return JPAQuery
     */
    @Override
    public JPAQuery<JurorPool> buildJurorPoolQuery() {
        ArrayList<Integer> activeStatuses = new ArrayList<>();
        activeStatuses.add(IJurorStatus.RESPONDED);
        activeStatuses.add(IJurorStatus.PANEL);
        activeStatuses.add(IJurorStatus.JUROR);

        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);
        return queryFactory.select(
                JUROR_POOL
            )
            .from(APPEARANCE)
            .join(JUROR_POOL)
            .on(JUROR_POOL.juror.jurorNumber.eq(APPEARANCE.jurorNumber)
                .and(JUROR_POOL.status.status.in(activeStatuses))
                .and(JUROR_POOL.isActive.isTrue()));
    }

    @Override
    public Integer countJurorExpenseForApproval(String jurorNumber, String poolNumber) {
        JPAQueryFactory queryFactory = new JPAQueryFactory(entityManager);

        NumberExpression<Integer> countAwaitingApproval = new CaseBuilder()
            .when(APPEARANCE.isDraftExpense.eq(false)
                .and(APPEARANCE.appearanceStage.eq(AppearanceStage.EXPENSE_ENTERED))).then(1)
            .otherwise(0)
            .sum();

        return queryFactory
            .select(countAwaitingApproval.as("forApproval"))
            .from(APPEARANCE)
            .where(APPEARANCE.jurorNumber.eq(jurorNumber))
            .where(APPEARANCE.poolNumber.eq(poolNumber))
            .groupBy(APPEARANCE.jurorNumber)
            .groupBy(APPEARANCE.poolNumber)
            .fetchOne();
    }

}
