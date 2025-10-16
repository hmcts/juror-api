package uk.gov.hmcts.juror.api.moj.repository.jurorresponse;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.juror.api.juror.domain.ProcessingStatus;
import uk.gov.hmcts.juror.api.juror.domain.QCourtLocation;
import uk.gov.hmcts.juror.api.moj.domain.IJurorStatus;
import uk.gov.hmcts.juror.api.moj.domain.QCurrentlyDeferred;
import uk.gov.hmcts.juror.api.moj.domain.QJuror;
import uk.gov.hmcts.juror.api.moj.domain.QJurorPool;
import uk.gov.hmcts.juror.api.moj.domain.QPoolRequest;
import uk.gov.hmcts.juror.api.moj.domain.jurorresponse.QCombinedJurorResponse;
import uk.gov.hmcts.juror.api.moj.repository.PoolTransferDayRepository;
import uk.gov.hmcts.juror.api.moj.utils.SecurityUtil;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;




@Slf4j
public class IJurorCommonResponseRepositoryModImpl implements IJurorCommonResponseRepositoryMod {
    private final PoolTransferDayRepository poolTransferDayRepository;

    public IJurorCommonResponseRepositoryModImpl(PoolTransferDayRepository poolTransferDayRepository) {
        this.poolTransferDayRepository = poolTransferDayRepository;
    }

    @PersistenceContext

    EntityManager entityManager;

    @Override
    public List<Tuple> getJurorResponseDetailsByUsernameAndStatus(String staffLogin,
                                                                  Collection<ProcessingStatus> processingStatus,
                                                                  Predicate... predicates) {
        JPAQuery<Tuple> query = getJpaQueryFactory().select(
                QCombinedJurorResponse.combinedJurorResponse,
                QJurorPool.jurorPool,
                QCourtLocation.courtLocation,
                QPoolRequest.poolRequest
            )
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .join(QPoolRequest.poolRequest).on(QPoolRequest.poolRequest.eq(QJurorPool.jurorPool.pool))
            .where(QCombinedJurorResponse.combinedJurorResponse.staff.username.eq(staffLogin))
            .where(QCombinedJurorResponse.combinedJurorResponse.processingStatus.in(processingStatus))
            .where(QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNull())
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.BUREAU_OWNER));

        if (predicates != null && predicates.length > 0) {
            query.where(predicates);
        }
        query.orderBy(QCombinedJurorResponse.combinedJurorResponse.dateReceived.asc());
        return query.fetch();
    }

    @Override
    public Map<ProcessingStatus, Long> getJurorResponseCounts(Predicate... predicates) {
        JPAQuery<Tuple> query = getJpaQueryFactory().select(
                QCombinedJurorResponse.combinedJurorResponse.processingStatus,
                QCombinedJurorResponse.combinedJurorResponse.count()
            )
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNull())
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.BUREAU_OWNER));

        if (predicates != null && predicates.length > 0) {
            query.where(predicates);
        }

        return query.groupBy(QCombinedJurorResponse.combinedJurorResponse.processingStatus)
            .fetch()
            .stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(QCombinedJurorResponse.combinedJurorResponse.processingStatus),
                tuple -> tuple.get(QCombinedJurorResponse.combinedJurorResponse.count())
            ));
    }




    @Override
    public List<Tuple> getJurorResponseDetailsByCourtAndStatus(String locCode,
                                                                Collection<ProcessingStatus> processingStatus,
                                                                Predicate... predicates) {
        JPAQuery<Tuple> query = getJpaQueryFactory().select(
                QCombinedJurorResponse.combinedJurorResponse,
                QJurorPool.jurorPool,
                QPoolRequest.poolRequest
            )
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .join(QPoolRequest.poolRequest).on(QPoolRequest.poolRequest.eq(QJurorPool.jurorPool.pool))
            .where(QCombinedJurorResponse.combinedJurorResponse.processingStatus.in(processingStatus))
            .where(QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNotNull())
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.getActiveOwner()));

        if (predicates != null && predicates.length > 0) {
            query.where(predicates);
        }
        query.orderBy(QCombinedJurorResponse.combinedJurorResponse.dateReceived.asc());
        return query.fetch();
    }

    @Override
    public Map<ProcessingStatus, Long> getJurorCourtResponseCounts(Predicate... predicates) {
        JPAQuery<Tuple> query = getJpaQueryFactory().select(
                QCombinedJurorResponse.combinedJurorResponse.processingStatus,
                QCombinedJurorResponse.combinedJurorResponse.count()
            )
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNotNull())
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.getActiveOwner()));

        if (predicates != null && predicates.length > 0) {
            query.where(predicates);
        }

        return query.groupBy(QCombinedJurorResponse.combinedJurorResponse.processingStatus)
            .fetch()
            .stream()
            .collect(Collectors.toMap(
                tuple -> tuple.get(QCombinedJurorResponse.combinedJurorResponse.processingStatus),
                tuple -> tuple.get(QCombinedJurorResponse.combinedJurorResponse.count())
            ));
    }


    JPAQueryFactory getJpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }


    @Override
    public int getOpenResponsesAtCourt(String locationCode) {
        return getJpaQueryFactory().select(
                QCombinedJurorResponse.combinedJurorResponse
            )
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(QCombinedJurorResponse.combinedJurorResponse.processingStatus.ne(ProcessingStatus.CLOSED))
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.SUMMONED))
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.getActiveOwner()))
            .where(QJurorPool.jurorPool.pool.courtLocation.locCode.eq(locationCode))
            .fetch().size();

    }

    @Override
    public int getOpenResponsesAtBureau(String locCode) {

        return getJpaQueryFactory()
            .select(QCombinedJurorResponse.combinedJurorResponse)
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(
                QCombinedJurorResponse.combinedJurorResponse.processingStatus.ne(
                    ProcessingStatus.CLOSED))
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.SUMMONED))
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.BUREAU_OWNER))
            .where(QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNull())
            .fetch()
            .size();
    }

    @Override
    public int getSummonsRepliesFourWeeks(String locCode) {

        return getJpaQueryFactory()
            .select(QCombinedJurorResponse.combinedJurorResponse)
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(
                QCombinedJurorResponse.combinedJurorResponse.processingStatus.ne(
                    ProcessingStatus.CLOSED))
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.SUMMONED))
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.BUREAU_OWNER))
            .where(QJurorPool.jurorPool.nextDate.before(LocalDateTime.now().plusWeeks(4).toLocalDate()))
            .where(QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNull())
            .fetch()
            .size();
    }


    @Override
    public int getDeferredJurorsStartDateNextWeek(String locCode) {
        return getJpaQueryFactory()
            .select(QCurrentlyDeferred.currentlyDeferred)
            .from(QCurrentlyDeferred.currentlyDeferred)
            .where(QCurrentlyDeferred.currentlyDeferred.owner.eq(SecurityUtil.BUREAU_OWNER))
            .where(
                QCurrentlyDeferred.currentlyDeferred.deferredTo.between(
                    LocalDateTime.now().toLocalDate(), LocalDateTime.now().plusWeeks(1).toLocalDate()))
            .fetch()
            .size();
    }

    @Override
    public int getPoolsNotYetSummonedCount(String locCode) {
        return getJpaQueryFactory()
            .select(
                QPoolRequest.poolRequest)
            .from(QPoolRequest.poolRequest)
            .leftJoin(QJurorPool.jurorPool)
            .on(QPoolRequest.poolRequest.poolNumber.eq(QJurorPool.jurorPool.pool.poolNumber))
            .where(QPoolRequest.poolRequest.owner.eq(SecurityUtil.BUREAU_OWNER))
            .where(QJurorPool.jurorPool.pool.poolNumber.isNull()).fetch().size();
    }

    @Override
    public int getPoolsTransferringNextWeekCount(String locCode) {
        LocalDate today = LocalDate.now();
        DayOfWeek currentDay = today.getDayOfWeek();

        //Return 0 on Friday, Saturday, Sunday
        //Only count Monday through Thursday JS-534

        if (currentDay == DayOfWeek.FRIDAY
            ||
            currentDay == DayOfWeek.SATURDAY
            ||
            currentDay == DayOfWeek.SUNDAY) {
            return 0;
        }

        // Calculate latest return date using proper stored procedure logic
        LocalDate latestReturnDate = calculateLatestReturnDate();



        List<String> distinctPoolNumbers = getJpaQueryFactory()
            .select(QPoolRequest.poolRequest.poolNumber)
            .distinct()
            .from(QPoolRequest.poolRequest)
            .join(QJurorPool.jurorPool).on(QPoolRequest.poolRequest.poolNumber.eq(QJurorPool.jurorPool.pool.poolNumber))
            .join(QJuror.juror).on(QJurorPool.jurorPool.juror.jurorNumber.eq(QJuror.juror.jurorNumber))
            .join(QCourtLocation.courtLocation)
            .on(QPoolRequest.poolRequest.courtLocation.locCode.eq(QCourtLocation.courtLocation.locCode))
            .where(QPoolRequest.poolRequest.owner.eq(SecurityUtil.BUREAU_OWNER))
            .where(QPoolRequest.poolRequest.returnDate.loe(latestReturnDate))
            .where(QJurorPool.jurorPool.status.status.in(

                IJurorStatus.SUMMONED,IJurorStatus.RESPONDED,IJurorStatus.ADDITIONAL_INFO))


            .where(QJuror.juror.bureauTransferDate.isNull())
            .where(QCourtLocation.courtLocation.owner.ne(SecurityUtil.BUREAU_OWNER))
            .fetch();
        return distinctPoolNumbers.size();
    }

    private LocalDate calculateLatestReturnDate() {
        LocalDate effectiveDate = LocalDate.now();

        // If running before 6pm on transfer day (Thursday), use previous day
        if (LocalTime.now().isBefore(LocalTime.of(18, 0))
            &&
            effectiveDate.getDayOfWeek() == DayOfWeek.THURSDAY) {
            effectiveDate = effectiveDate.minusDays(1);
        }

        // Get the day AFTER applying 6pm rule
        String runDay = effectiveDate.getDayOfWeek().toString().toLowerCase().substring(0, 3);

        // weekday adjustment lookup
        int weekdayAdjustment = getWeekdayAdjustmentFromDB("thu", runDay);



        // Apply adjustments
        effectiveDate = effectiveDate.plusDays(weekdayAdjustment);
        LocalDate latestReturnDate = effectiveDate.plusDays(7);  // 1 week

        return latestReturnDate;
    }

    private int getWeekdayAdjustmentFromDB(String transferDay, String runDay) {
        if (transferDay == null || runDay == null) {
            return 0;
        }
        String t = transferDay.trim();
        String r = runDay.trim();
        return poolTransferDayRepository
            .findByTransferDayAndRunDayIgnoreCase(t, r)
            .map(ptw -> ptw.getAdjustment())
            .orElse(0);
    }



    @Override
    public Tuple getAllSummonsCountsTuple(String locCode) {
        JPAQueryFactory queryFactory = getJpaQueryFactory();
        LocalDateTime fourWeeksFromNow = LocalDateTime.now().plusWeeks(4);

        return queryFactory.select(
                // Total count
                QCombinedJurorResponse.combinedJurorResponse.jurorNumber.count(),

                // Standard (not urgent)
                new CaseBuilder()
                    .when(QCombinedJurorResponse.combinedJurorResponse.urgent.eq(false))
                    .then(1)
                    .otherwise(0)
                    .sum(),

                // Urgent/Overdue
                new CaseBuilder()
                    .when(QCombinedJurorResponse.combinedJurorResponse.urgent.eq(true))
                    .then(1)
                    .otherwise(0)
                    .sum(),

                // Four weeks before start date
                new CaseBuilder()
                    .when(QJurorPool.jurorPool.nextDate.before(fourWeeksFromNow.toLocalDate()))
                    .then(1)
                    .otherwise(0)
                    .sum(),

                // Assigned (has staff)
                new CaseBuilder()
                    .when(QCombinedJurorResponse.combinedJurorResponse.staff.isNotNull())
                    .then(1)
                    .otherwise(0)
                    .sum(),

                // Unassigned (no staff)
                new CaseBuilder()
                    .when(QCombinedJurorResponse.combinedJurorResponse.staff.isNull())
                    .then(1)
                    .otherwise(0)
                    .sum()
            )
            .from(QCombinedJurorResponse.combinedJurorResponse)
            .join(QJurorPool.jurorPool)
            .on(QJurorPool.jurorPool.juror.eq(QCombinedJurorResponse.combinedJurorResponse.juror))
            .where(QJurorPool.jurorPool.isActive.isTrue())
            .where(QCombinedJurorResponse.combinedJurorResponse.processingStatus.ne(ProcessingStatus.CLOSED))
            .where(QJurorPool.jurorPool.status.status.eq(IJurorStatus.SUMMONED))
            .where(QJurorPool.jurorPool.owner.eq(SecurityUtil.BUREAU_OWNER))
            .where(QCombinedJurorResponse.combinedJurorResponse.juror.bureauTransferDate.isNull())
            .fetchOne();
    }


}
